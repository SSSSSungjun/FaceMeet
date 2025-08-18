import os
import cv2
import mediapipe as mp
import numpy as np
from PIL import Image, ImageOps
import torch
import clip
import math
import tempfile
import logging
from typing import Dict, Any, Tuple, List, Union

logger = logging.getLogger(__name__)

# ======== 상수 정의 ========
LEFT_EYE_IDX = [33, 133, 159, 145, 153, 154]
RIGHT_EYE_IDX = [362, 263, 386, 374, 380, 381]
EYE_IDX_ALL = [
    33, 133, 160, 159, 158, 157, 173, 153, 154, 155, 246,
    263, 249, 390, 373, 374, 380, 381, 382, 362, 398,
    384, 385, 386, 387, 388, 466
]
FACE_BOX_IDX = [234, 454, 152, 10]
FACE_WIDTH_IDX = [234, 454]

# ======== CLIP 모델 클래스 ========
class EyeCLIPComparer:
    def __init__(self):
        self.device = "cuda" if torch.cuda.is_available() else "cpu"
        self.model, self.preprocess = clip.load("ViT-B/32", device=self.device)
        logger.info(f"Eye CLIP model loaded on {self.device}")

    def get_features(self, image_source: Union[str, Image.Image]) -> np.ndarray:
        """이미지 경로 또는 PIL 이미지 객체로부터 특징 벡터를 추출합니다."""
        if isinstance(image_source, str):
            image = Image.open(image_source).convert("RGB")
        else:
            image = image_source.convert("RGB")

        image_input = self.preprocess(image).unsqueeze(0).to(self.device)
        with torch.no_grad():
            features = self.model.encode_image(image_input)
            features /= features.norm(dim=-1, keepdim=True)
        return features.cpu().numpy()[0]

    def compare_with_precomputed(self, image_source: Union[str, Image.Image], reference_features_dict: Dict[str, np.ndarray]) -> Dict[str, float]:
        """미리 계산된 참조 특징과 비교하여 성능을 향상시킵니다."""
        input_feat = self.get_features(image_source)
        return {label: float(np.dot(input_feat, ref_feat)) for label, ref_feat in reference_features_dict.items()}


class EyeLipAnalyzer:
    """눈과 입 분석을 위한 모든 모델과 로직을 캡슐화한 클래스."""

    def __init__(self):
        self.face_mesh = mp.solutions.face_mesh.FaceMesh(
            static_image_mode=True, max_num_faces=1, refine_landmarks=True
        )
        self.clip_comparer = EyeCLIPComparer()
        self.reference_features = self._initialize_reference_features()

    def _initialize_reference_features(self) -> Dict[str, np.ndarray]:
        """서버 시작 시 참조 이미지의 특징을 미리 계산하여 캐싱합니다."""
        logger.info("참조 눈 이미지 특징(feature) 초기화 시작...")
        ref_labels = {"올라간 눈": "eye/upeye.png", "내려간 눈": "eye/downeye.png", "둥근 눈": "eye/roundeye.png", "가는 눈": "eye/thineye.png"}
        ref_features = {}

        with tempfile.TemporaryDirectory() as temp_dir:
            for label, path in ref_labels.items():
                if not os.path.exists(path):
                    logger.warning(f"참조 이미지 없음: {path}. 건너뜁니다.")
                    continue
                try:
                    # 참조 이미지에서 눈 영역만 추출하여 특징 계산
                    img = cv2.imread(path)
                    if img is None: continue
                    landmarks, w, h = self._get_landmarks(img)
                    if not landmarks: continue
                    
                    eye_pil = self._preprocess_eye_image(img, landmarks, w, h, EYE_IDX_ALL)
                    ref_features[label] = self.clip_comparer.get_features(eye_pil)
                except (ValueError, FileNotFoundError) as e:
                    logger.error(f"참조 이미지 처리 실패 {path}: {e}")
        logger.info("참조 눈 이미지 특징 초기화 완료.")
        return ref_features

    def _get_landmarks(self, image_array: np.ndarray) -> Tuple[Any, int, int]:
        """이미지 배열에서 얼굴 랜드마크를 추출합니다."""
        h, w, _ = image_array.shape
        rgb_img = cv2.cvtColor(image_array, cv2.COLOR_BGR2RGB)
        results = self.face_mesh.process(rgb_img)
        if not results.multi_face_landmarks:
            return None, 0, 0
        return results.multi_face_landmarks[0].landmark, w, h

    def _judge_bigeye(self, landmarks: Any, w: int, h: int, threshold: float = 0.0075) -> str:
        """랜드마크를 기반으로 눈 크기를 판별합니다."""
        get_point = lambda i: (landmarks[i].x * w, landmarks[i].y * h)
        
        left = [get_point(idx) for idx in LEFT_EYE_IDX]
        right = [get_point(idx) for idx in RIGHT_EYE_IDX]
        
        left_width = abs(left[0][0] - left[1][0])
        right_width = abs(right[0][0] - right[1][0])
        
        left_height = np.mean([math.hypot(left[2][0]-left[3][0], left[2][1]-left[3][1]), math.hypot(left[4][0]-left[5][0], left[4][1]-left[5][1])])
        right_height = np.mean([math.hypot(right[2][0]-right[3][0], right[2][1]-right[3][1]), math.hypot(right[4][0]-right[5][0], right[4][1]-right[5][1])])
        
        avg_eye_area = (left_width * left_height + right_width * right_height) / 2
        
        face_pts = [get_point(i) for i in FACE_BOX_IDX]
        face_area = abs(face_pts[0][0] - face_pts[1][0]) * abs(face_pts[2][1] - face_pts[3][1])
        
        ratio = avg_eye_area / (face_area + 1e-8)
        return "큰 눈" if ratio > threshold else "작은 눈"

    def _preprocess_eye_image(self, image_array: np.ndarray, landmarks: Any, w: int, h: int, eye_idx: List[int], 
                              padding_top: int = 15, padding_side: int = 5, target_size: Tuple[int, int] = (224, 224), 
                              auto_eq: bool = True, to_gray: bool = False) -> Image.Image:
        """이미지 배열과 랜드마크로부터 눈 영역을 추출하고 전처리하여 PIL 이미지로 반환합니다."""
        points = [(int(landmarks[idx].x * w), int(landmarks[idx].y * h)) for idx in eye_idx]
        xs, ys = [x for x, _ in points], [y for _, y in points]
        x1, y1 = max(min(xs) - padding_side, 0), max(min(ys) - padding_top, 0)
        x2, y2 = min(max(xs) + padding_side, w), min(max(ys) + padding_side, h)
        
        eye_img = image_array[y1:y2, x1:x2]
        eye_pil = Image.fromarray(cv2.cvtColor(eye_img, cv2.COLOR_BGR2RGB))
        
        if auto_eq: eye_pil = ImageOps.autocontrast(eye_pil)
        if to_gray: eye_pil = ImageOps.grayscale(eye_pil).convert("RGB")
        
        eye_pil = eye_pil.resize(target_size)
        return eye_pil

    def _classify_lip_and_corner(self, landmarks: Any, w: int, h: int) -> Tuple[str, str, str]:
        """랜드마크를 기반으로 입술 크기, 두께, 입꼬리 방향을 분류합니다."""
        mouth_width = abs(landmarks[146].x - landmarks[61].x) * w
        face_width = abs(landmarks[FACE_WIDTH_IDX[1]].x - landmarks[FACE_WIDTH_IDX[0]].x) * w
        width_ratio = mouth_width / (face_width + 1e-8)
        
        lip_thickness = abs(landmarks[14].y - landmarks[13].y) * h
        thickness_ratio = lip_thickness / (mouth_width + 1e-8) 
        
        angle = math.degrees(math.atan2((landmarks[291].y - landmarks[61].y), (landmarks[291].x - landmarks[61].x)))
        
        size_result = "큰입" if width_ratio >= 0.017 else "작은입" # 임계값은 상수로 관리 가능
        thickness_result = "두꺼운 입술" if thickness_ratio >= 0.1 else "얇은 입술" # 임계값은 상수로 관리 가능
        corner_result = "입가가 올라간 입" if angle < 0.4 else "입가가 내려간 입" # 임계값은 상수로 관리 가능
        
        return size_result, thickness_result, corner_result

    def analyze(self, image_path: str) -> Dict[str, str]:
        """
        하나의 이미지 파일을 분석하여 특징 키워드 딕셔너리를 반환합니다.
        """
        img_array = cv2.imread(image_path)
        if img_array is None:
            raise FileNotFoundError(f"이미지를 로드할 수 없습니다: {image_path}")

        landmarks, w, h = self._get_landmarks(img_array)
        if not landmarks:
            raise ValueError("얼굴 랜드마크를 찾을 수 없습니다.")

        # 1. 기하학적 분석
        size_result_kw = self._judge_bigeye(landmarks, w, h)
        mouth_size_kw, mouth_thickness_kw, mouth_corner_kw = self._classify_lip_and_corner(landmarks, w, h)

        # 2. CLIP 비교를 위한 눈 이미지 전처리 및 비교
        user_eye_pil = self._preprocess_eye_image(img_array, landmarks, w, h, EYE_IDX_ALL)
        clip_results = self.clip_comparer.compare_with_precomputed(user_eye_pil, self.reference_features)

        # 3. 결과 키워드 결정
        eyecorner_kw = max(["내려간 눈", "올라간 눈"], key=lambda k: clip_results.get(k, -1.0))
        eyeshape_kw = max(["둥근 눈", "가는 눈"], key=lambda k: clip_results.get(k, -1.0))

        return {
            "눈꼬리": eyecorner_kw,
            "눈모양": eyeshape_kw,
            "눈크기": size_result_kw,
            "입크기": mouth_size_kw,
            "입술두께": mouth_thickness_kw,
            "입꼬리": mouth_corner_kw,
        }

# --- 분석기 인스턴스 생성 (모듈 로딩 시 1회) ---
try:
    analyzer = EyeLipAnalyzer()
except Exception as e:
    logger.critical(f"EyeLipAnalyzer 초기화 실패: {e}", exc_info=True)
    analyzer = None # 초기화 실패 시 None으로 설정

# --- 외부 호출용 함수 ---
def run_eyelip_analysis(input_face_img: str, keyword_to_id_maps: Dict) -> Union[Dict[str, int], None]:
    """
    views.py에서 호출하는 메인 분석 함수.
    분석 결과를 키워드 ID 딕셔너리로 변환하여 반환합니다.
    """
    if analyzer is None:
        logger.error("분석기(Analyzer)가 초기화되지 않아 분석을 진행할 수 없습니다.")
        return None

    try:
        keyword_results = analyzer.analyze(input_face_img)
        eye_map = keyword_to_id_maps.get('eye', {})
        mouth_map = keyword_to_id_maps.get('mouth', {})
        return {key: (eye_map if '눈' in key else mouth_map).get(value) for key, value in keyword_results.items()}
    except (FileNotFoundError, ValueError) as e:
        logger.error(f"분석 실패 ({os.path.basename(input_face_img)}): {e}")
        return None
    except Exception as e:
        logger.error(f"분석 중 예기치 않은 오류 발생 ({os.path.basename(input_face_img)}): {e}", exc_info=True)
        return None
