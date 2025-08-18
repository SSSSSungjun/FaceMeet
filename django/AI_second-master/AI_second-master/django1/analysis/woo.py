import os
import cv2
import numpy as np
from PIL import Image, ImageOps
import mediapipe as mp
import torch
import clip
import logging
from typing import Dict, Any, Tuple, List, Union

logger = logging.getLogger(__name__)

# ======== 상수 정의 ========
EYEBROW_OUTLINE_IDX = [70, 63, 105, 66, 107, 55, 65, 52, 53, 46]
NOSE_IDX = [168, 174, 209, 49, 48, 235, 19, 455, 278, 279, 456]

# ======== CLIP 모델 클래스 ========
class CLIPComparer:
    def __init__(self):
        self.device = "cuda" if torch.cuda.is_available() else "cpu"
        self.model, self.preprocess = clip.load("ViT-B/32", device=self.device)
        logger.info(f"CLIP model loaded on {self.device}")

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


class ChinFaceEyebrowAnalyzer:
    """눈썹, 턱, 얼굴형, 코 분석을 위한 모든 모델과 로직을 캡슐화한 클래스."""

    def __init__(self):
        self.face_mesh = mp.solutions.face_mesh.FaceMesh(
            static_image_mode=True, max_num_faces=1, refine_landmarks=True
        )
        self.clip_comparer = CLIPComparer()
        self.reference_features = self._initialize_reference_features()

    def _initialize_reference_features(self) -> Dict[str, Dict[str, np.ndarray]]:
        """서버 시작 시 참조 이미지의 특징을 미리 계산하여 캐싱합니다."""
        logger.info("참조 이미지(눈썹, 코 등) 특징 초기화 시작...")
        ref_paths = {
            "eyebrow_thickness": {"굵은 눈썹": "eyebrow/thick.png", "가는 눈썹": "eyebrow/thin.png"},
            "eyebrow_shape": {"일자 눈썹": "eyebrow/straight.png", "올라간 눈썹": "eyebrow/up.png", "내려간 눈썹": "eyebrow/down.png"},
            "nose_shape": {"폭 넓은 코": "nose/galic.png", "폭 좁은 코": "nose/knife.png"},
            "nose_length": {"긴 코": "nose/long.png", "짧은 코": "nose/short.png"},
            "nose_updown": {"코 끝이 올라간": "nose/up.png", "코 끝이 내려간": "nose/down.png"},
            "nose_height": {"높은 코": "nose/high.png", "낮은 코": "nose/low.png"},
        }
        ref_features = {key: {} for key in ref_paths}
        

        for category, paths in ref_paths.items():
            for label, path in paths.items():
                if not os.path.exists(path):
                    logger.warning(f"참조 이미지 없음: {path}. 건너뜁니다.")
                    continue
                try:
                    img_array = cv2.imread(path)
                    if img_array is None: continue
                    landmarks, w, h = self._get_landmarks(img_array)
                    if not landmarks: continue
                    
                    indices = EYEBROW_OUTLINE_IDX if 'eyebrow' in category else NOSE_IDX
                    feature_pil = self._extract_feature_pil(img_array, landmarks, w, h, indices)
                    ref_features[category][label] = self.clip_comparer.get_features(feature_pil)
                except (ValueError, FileNotFoundError) as e:
                    logger.error(f"참조 이미지 처리 실패 {path}: {e}")
        logger.info("참조 이미지 특징 초기화 완료.")
        print("기준 이미지 벡터 저장이 완료 되었습니다.")
        return ref_features

    def _get_landmarks(self, image_array: np.ndarray) -> Tuple[Any, int, int]:
        """이미지 배열에서 얼굴 랜드마크를 추출합니다."""
        h, w, _ = image_array.shape
        rgb_img = cv2.cvtColor(image_array, cv2.COLOR_BGR2RGB)
        results = self.face_mesh.process(rgb_img)
        if not results.multi_face_landmarks:
            return None, 0, 0
        return results.multi_face_landmarks[0].landmark, w, h

    def _extract_feature_pil(self, image_array: np.ndarray, landmarks: Any, w: int, h: int, indices: List[int]) -> Image.Image:
        """주어진 랜드마크 인덱스를 기반으로 신체 부위를 추출하여 PIL 이미지로 반환합니다."""
        points = np.array([(int(landmarks[i].x * w), int(landmarks[i].y * h)) for i in indices])
        mask = np.zeros((h, w), dtype=np.uint8)
        cv2.fillConvexPoly(mask, points, 255)
        
        # BGR 이미지를 RGBA로 변환하고 마스크 적용
        b, g, r = cv2.split(image_array)
        rgba = cv2.merge([r, g, b, mask])
        
        return Image.fromarray(rgba)

    def _euclidean(self, p1, p2):
        return np.linalg.norm(np.array(p1) - np.array(p2))

    def _analyze_eyebrow_geometry(self, landmarks: Any, w: int, h: int) -> Tuple[str, str]:
        get_point = lambda i: (landmarks[i].x * w, landmarks[i].y * h)
        right_eyebrow_len = self._euclidean(get_point(70), get_point(55))
        right_eye_len = self._euclidean(get_point(226), get_point(133))
        eyebrow_gap = self._euclidean(get_point(55), get_point(285))
        
        len_kw = "긴 눈썹" if right_eyebrow_len > right_eye_len*1.3 else "짧은 눈썹"
        gap_kw = "사이가 넓은 눈썹" if eyebrow_gap > right_eye_len*0.8 else "사이가 좁은 눈썹"
        return len_kw, gap_kw

    def _analyze_chin_geometry(self, landmarks: Any, w: int, h: int) -> Tuple[str, str]:
        get_xy = lambda i: np.array([landmarks[i].x * w, landmarks[i].y * h])
        left, right = get_xy(234), get_xy(454)
        middle_indices = [93, 132, 58, 172, 136, 150, 176, 148, 152, 288, 365, 137, 377]
        
        angles = []
        for mid_idx in middle_indices:
            try:
                mid = get_xy(mid_idx)
                a, b = left - mid, right - mid
                cos_angle = np.dot(a, b) / (np.linalg.norm(a) * np.linalg.norm(b))
                angles.append(np.degrees(np.arccos(np.clip(cos_angle, -1.0, 1.0))))
            except (ZeroDivisionError, ValueError):
                continue
        
        if not angles: return "분석 불가", "분석 불가"

        avg_angle = np.mean(angles)
        if avg_angle >= 80: chin_shape = "둥글고 통통한 턱"
        elif avg_angle >= 78: chin_shape = "각진 턱"
        else: chin_shape = "뾰족한 턱"

        jaw_height = self._euclidean(get_xy(17), get_xy(152))
        face_height = self._euclidean(get_xy(10), get_xy(152))
        ratio = round((jaw_height / (face_height)),3)
        chin_length = "긴 턱" if ratio >= 0.15 else "작은 턱"
        print("뭐가 문제일까~~:","얼굴 높이:",face_height,"턱 높이:", jaw_height, "비율 :" , ratio, "턱 유형" ,chin_length)

        return chin_shape, chin_length

    def _analyze_face_shape(self, landmarks: Any, w: int, h: int) -> str:
        get_xy = lambda i: (landmarks[i].x * w, landmarks[i].y * h)
        height = self._euclidean(get_xy(10), get_xy(152))
        width = self._euclidean(get_xy(234), get_xy(454))
        ratio = width / (height + 1e-8)
        
        if ratio >= 0.95: return "둥근 얼굴"
        elif ratio <= 0.85: return "긴 얼굴"
        else: return "일반형 얼굴"

    def analyze(self, front_image_path: str, side_image_path: str = None) -> Dict[str, str]:
        """정면, 측면 이미지를 분석하여 특징 키워드 딕셔너리를 반환합니다."""
        # 1. 정면 이미지 분석
        front_img = cv2.imread(front_image_path)
        if front_img is None: raise FileNotFoundError(f"정면 이미지를 로드할 수 없습니다: {front_image_path}")
        
        landmarks, w, h = self._get_landmarks(front_img)
        if not landmarks: raise ValueError("정면 얼굴 랜드마크를 찾을 수 없습니다.")

        # 2. 기하학적 분석
        eyebrow_len_kw, eyebrow_gap_kw = self._analyze_eyebrow_geometry(landmarks, w, h)
        chin_shape_kw, chin_length_kw = self._analyze_chin_geometry(landmarks, w, h)
        face_type_kw = self._analyze_face_shape(landmarks, w, h)

        # 3. CLIP 비교 (눈썹, 코 정면)
        eyebrow_pil = self._extract_feature_pil(front_img, landmarks, w, h, EYEBROW_OUTLINE_IDX)
        nose_pil = self._extract_feature_pil(front_img, landmarks, w, h, NOSE_IDX)

        thickness_res = self.clip_comparer.compare_with_precomputed(eyebrow_pil, self.reference_features["eyebrow_thickness"])
        shape_res = self.clip_comparer.compare_with_precomputed(eyebrow_pil, self.reference_features["eyebrow_shape"])
        nose_shape_res = self.clip_comparer.compare_with_precomputed(nose_pil, self.reference_features["nose_shape"])
        nose_len_res = self.clip_comparer.compare_with_precomputed(nose_pil, self.reference_features["nose_length"])
        nose_updown_res = self.clip_comparer.compare_with_precomputed(nose_pil, self.reference_features["nose_updown"])

        # 4. 측면 이미지 분석 (코 높이)
        nose_height_kw = None
        if side_image_path and os.path.exists(side_image_path):
            side_img = cv2.imread(side_image_path)
            if side_img is not None:
                side_landmarks, side_w, side_h = self._get_landmarks(side_img)
                if side_landmarks:
                    side_nose_pil = self._extract_feature_pil(side_img, side_landmarks, side_w, side_h, NOSE_IDX)
                    nose_height_res = self.clip_comparer.compare_with_precomputed(side_nose_pil, self.reference_features["nose_height"])
                    if nose_height_res:
                        nose_height_kw = max(nose_height_res, key=nose_height_res.get)

        # 5. 결과 통합
        return {
            "눈썹 굵기": max(thickness_res, key=thickness_res.get) if thickness_res else None,
            "눈썹 길이": eyebrow_len_kw,
            "눈썹 형태": max(shape_res, key=shape_res.get) if shape_res else None,
            "눈썹 간격": eyebrow_gap_kw,
            "턱 형태": chin_shape_kw,
            "턱 길이": chin_length_kw,
            "얼굴형": face_type_kw,
            "코 형태": max(nose_shape_res, key=nose_shape_res.get) if nose_shape_res else None,
            "코 길이": max(nose_len_res, key=nose_len_res.get) if nose_len_res else None,
            "들창/화살코": max(nose_updown_res, key=nose_updown_res.get) if nose_updown_res else None,
            "코 높이": nose_height_kw,
        }

# --- 분석기 인스턴스 생성 (모듈 로딩 시 1회) ---
try:
    analyzer = ChinFaceEyebrowAnalyzer()
except Exception as e:
    logger.critical(f"ChinFaceEyebrowAnalyzer 초기화 실패: {e}", exc_info=True)
    analyzer = None

# --- 외부 호출용 함수 ---
def run_chinfaceeyebrow_analysis(input_front_path: str, input_side_path: str = None, keyword_to_id_maps: Dict = None) -> Union[Dict[str, int], None]:
    """
    views.py에서 호출하는 메인 분석 함수.
    분석 결과를 키워드 ID 딕셔너리로 변환하여 반환합니다.
    """
    if analyzer is None:
        logger.error("분석기(Analyzer)가 초기화되지 않아 분석을 진행할 수 없습니다.")
        return None

    try:
        keyword_results = analyzer.analyze(input_front_path, input_side_path)
        print("keyword!!!!!:",keyword_results)
        # 키워드를 ID로 매핑
        chin_map = keyword_to_id_maps.get('chin', {})
        print("keyword_results['턱 형태']:", keyword_results.get("턱 형태"))
        print("chin_map.get(턱 형태):", chin_map.get(keyword_results.get("턱 형태")))
        print("chin_map:", chin_map)

        faceshape_map = keyword_to_id_maps.get('faceshape', {})
        eyebrow_map = keyword_to_id_maps.get('eyebrow', {})
        nose_map = keyword_to_id_maps.get('nose', {})

        # 각 키워드에 맞는 맵을 사용하여 ID 조회
        return {
            "턱 형태": chin_map.get(keyword_results["턱 형태"]),
            "턱 길이": chin_map.get(keyword_results["턱 길이"].strip()),
            "얼굴형": faceshape_map.get(keyword_results["얼굴형"]),
            "코 형태": nose_map.get(keyword_results["코 형태"]),
            "코 길이": nose_map.get(keyword_results["코 길이"]),
            "들창/화살코": nose_map.get(keyword_results["들창/화살코"]),
            "코 높이": nose_map.get(keyword_results["코 높이"]),
            "눈썹 굵기": eyebrow_map.get(keyword_results["눈썹 굵기"]),
            "눈썹 길이": eyebrow_map.get(keyword_results["눈썹 길이"]),
            "눈썹 형태": eyebrow_map.get(keyword_results["눈썹 형태"]),
            "눈썹 간격": eyebrow_map.get(keyword_results["눈썹 간격"]),
        }
    except (FileNotFoundError, ValueError) as e:
        logger.error(f"분석 실패 ({os.path.basename(input_front_path)}): {e}")
        return None
    except Exception as e:
        logger.error(f"분석 중 예기치 않은 오류 발생 ({os.path.basename(input_front_path)}): {e}", exc_info=True)
        return None
