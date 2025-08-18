import os
import math
import numpy as np
from PIL import Image, ImageOps
import cv2
import mediapipe as mp
import torch
import clip

# 각종 인덱스 상수
EYEBROW_OUTLINE_IDX = [70, 63, 105, 66, 107, 55, 65, 52, 53, 46]
CHIN_IDX = [234, 454, 152, 377, 137, 365, 288, 58, 172, 136, 150]
LEFT_EYE_IDX = [33, 133, 159, 145, 153, 154]
RIGHT_EYE_IDX = [362, 263, 386, 374, 380, 381]
EYE_IDX_ALL = [
    33, 133, 160, 159, 158, 157, 173, 153, 154, 155, 246,
    263, 249, 390, 373, 374, 380, 381, 382, 362, 398,
    384, 385, 386, 387, 388, 466
]
FACE_BOX_IDX = [234, 454, 152, 10]
FACE_WIDTH_IDX = [234, 454]

def imread_for_korean_path(path, flags=cv2.IMREAD_COLOR):
    """
    Windows에서 한글 경로의 이미지를 읽기 위한 cv2.imread 대체 함수.
    파일을 바이너리로 읽은 후 numpy 배열로 디코딩합니다.
    """
    try:
        with open(path, 'rb') as f:
            img_array = np.fromfile(f, np.uint8)
        img = cv2.imdecode(img_array, flags)
        if img is None:
            raise IOError(f"cv2.imdecode가 이미지를 디코딩하지 못했습니다: {path}")
        return img
    except Exception as e:
        raise IOError(f"이미지 파일 로딩 실패: {path}") from e

def euclidean(p1, p2):
    return np.linalg.norm(np.array(p1) - np.array(p2))

def resize_image(image_path, output_path="resized_input.png", size=(1024, 1024)):
    img = imread_for_korean_path(image_path)
    resized = cv2.resize(img, size)
    cv2.imwrite(output_path, resized)
    return output_path

def extract_eyebrow_rgba(image_path, output_path="eyebrow_rgba.png"):
    img = imread_for_korean_path(image_path)
    h, w, _ = img.shape
    face_mesh = mp.solutions.face_mesh.FaceMesh(static_image_mode=True, max_num_faces=1, refine_landmarks=True)
    rgb = cv2.cvtColor(img, cv2.COLOR_BGR2RGB)
    results = face_mesh.process(rgb)
    if not results.multi_face_landmarks:
        raise ValueError("❌ 얼굴을 감지하지 못했습니다. (눈썹 추출 실패)")
    landmarks = results.multi_face_landmarks[0].landmark
    points = np.array([(int(landmarks[i].x * w), int(landmarks[i].y * h)) for i in EYEBROW_OUTLINE_IDX])
    mask = np.zeros((h, w), dtype=np.uint8)
    cv2.fillConvexPoly(mask, points, 255)
    rgba = np.dstack((cv2.cvtColor(img, cv2.COLOR_BGR2RGB), mask))
    Image.fromarray(rgba).save(output_path)
    return output_path, landmarks, w, h

class CLIPComparer:
    def __init__(self):
        self.device = "cuda" if torch.cuda.is_available() else "cpu"
        self.model, self.preprocess = clip.load("ViT-B/32", device=self.device)
    def get_feature(self, image_path):
        img = Image.open(image_path).convert("RGB")
        img_input = self.preprocess(img).unsqueeze(0).to(self.device)
        with torch.no_grad():
            feat = self.model.encode_image(img_input)
            feat /= feat.norm(dim=-1, keepdim=True)
        return feat.cpu().numpy()[0]
    def compare(self, input_path, ref_dict):
        input_feat = self.get_feature(input_path)
        return {label: np.dot(input_feat, self.get_feature(path)) for label, path in ref_dict.items()}

def analyze_eyebrow_geometry(landmarks, w, h):
    get_xy = lambda i: (landmarks[i].x * w, landmarks[i].y * h)
    right_eyebrow_len = euclidean(get_xy(70), get_xy(55))
    right_eye_len = euclidean(get_xy(226), get_xy(133))
    type2 = "긴 눈썹" if right_eye_len > right_eyebrow_len else "짧은 눈썹"
    eyebrow_gap = euclidean(get_xy(133), get_xy(362))
    type4 = "사이가 넓은 눈썹" if eyebrow_gap > right_eye_len else "사이가 좁은 눈썹"
    return type2, type4

def analyze_chin_geometry(landmarks, w, h):
    def get_xy(index):
        return np.array([landmarks[index].x * w, landmarks[index].y * h])
    left = get_xy(234)
    right = get_xy(454)
    middle_indices = [93, 132, 58, 172, 136, 150, 176, 148, 152, 288, 365, 137, 377]
    angles = []
    for mid_idx in middle_indices:
        mid = get_xy(mid_idx)
        a = left - mid
        b = right - mid
        cos_angle = np.dot(a, b) / (np.linalg.norm(a) * np.linalg.norm(b))
        angle = np.degrees(np.arccos(np.clip(cos_angle, -1.0, 1.0)))
        angles.append(angle)
    avg_angle = np.mean(angles)
    if avg_angle >= 80:
        chin_shape = "둥근 턱"
    elif avg_angle >= 75:
        chin_shape = "각진 턱"
    else:
        chin_shape = "뾰족한 턱"
    chin_bottom = get_xy(152)
    under_lip = get_xy(17)
    forehead = get_xy(10)
    jaw_height = euclidean(under_lip, chin_bottom)
    face_height = euclidean(forehead, chin_bottom)
    ratio = jaw_height / face_height
    chin_length = "긴 턱" if ratio >= 0.21 else "짧은 턱"
    return chin_shape, chin_length, avg_angle

def analyze_face_shape(landmarks, w, h):
    get_xy = lambda i: (landmarks[i].x * w, landmarks[i].y * h)
    top = get_xy(10)
    bottom = get_xy(152)
    left = get_xy(234)
    right = get_xy(454)
    height = euclidean(top, bottom)
    width = euclidean(left, right)
    ratio = width / height
    if ratio >= 0.8:
        face_type = "둥근 얼굴"
    elif ratio <= 1.3:
        face_type = "긴 얼굴"
    else:
        face_type = "일반형 얼굴"
    return face_type, round(ratio, 2)

def extract_landmarks(image_path):
    img = imread_for_korean_path(image_path)
    if img is None:
        raise ValueError(f"이미지를 로드할 수 없습니다: {image_path}")
    h, w, _ = img.shape
    mp_face_mesh = mp.solutions.face_mesh
    with mp_face_mesh.FaceMesh(static_image_mode=True, max_num_faces=1, refine_landmarks=True) as face_mesh:
        rgb_img = cv2.cvtColor(img, cv2.COLOR_BGR2RGB)
        results = face_mesh.process(rgb_img)
        if not results.multi_face_landmarks:
            raise ValueError("얼굴을 찾지 못했습니다")
        landmarks = results.multi_face_landmarks[0].landmark
        left = [((landmarks[idx].x * w), (landmarks[idx].y * h)) for idx in LEFT_EYE_IDX]
        right = [((landmarks[idx].x * w), (landmarks[idx].y * h)) for idx in RIGHT_EYE_IDX]
        left_width = abs(left[0][0] - left[1][0])
        right_width = abs(right[0][0] - right[1][0])
        left_height = np.mean([
            math.hypot(left[2][0] - left[3][0], left[2][1] - left[3][1]),
            math.hypot(left[4][0] - left[5][0], left[4][1] - left[5][1])
        ])
        right_height = np.mean([
            math.hypot(right[2][0] - right[3][0], right[2][1] - right[3][1]),
            math.hypot(right[4][0] - right[5][0], right[4][1] - right[5][1])
        ])
        left_area = left_width * left_height
        right_area = right_width * right_height
        face_pts = [((landmarks[i].x * w), (landmarks[i].y * h)) for i in FACE_BOX_IDX]
        face_w = abs(face_pts[0][0] - face_pts[1][0])
        face_h = abs(face_pts[2][1] - face_pts[3][1])
        face_area = face_w * face_h
        avg_eye_area = (left_area + right_area) / 2
        return avg_eye_area, face_area

def judge_bigeye(image_path, threshold=0.0075):
    eye_area, face_area = extract_landmarks(image_path)
    ratio = eye_area / (face_area + 1e-8)
    result = "큰눈" if ratio > threshold else "작은눈"
    return result

def preprocess_eye_image(input_path, output_path, eye_idx, padding_top=15, padding_side=5, target_size=(224, 224), auto_eq=True, to_gray=False):
    img = imread_for_korean_path(input_path)
    if img is None:
        raise ValueError(f"이미지를 로드할 수 없습니다: {input_path}")
    h, w, _ = img.shape
    mp_face_mesh = mp.solutions.face_mesh
    with mp_face_mesh.FaceMesh(static_image_mode=True, max_num_faces=1, refine_landmarks=True) as face_mesh:
        rgb_img = cv2.cvtColor(img, cv2.COLOR_BGR2RGB)
        results = face_mesh.process(rgb_img)
        if not results.multi_face_landmarks:
            raise ValueError("얼굴을 찾지 못했습니다")
        face_landmarks = results.multi_face_landmarks[0]
        points = [
            (int(face_landmarks.landmark[idx].x * w), int(face_landmarks.landmark[idx].y * h))
            for idx in eye_idx
        ]
        xs, ys = [x for x, _ in points], [y for _, y in points]
        x1 = max(min(xs) - padding_side, 0)
        y1 = max(min(ys) - padding_top, 0)
        x2 = min(max(xs) + padding_side, w)
        y2 = min(max(ys) + padding_side, h)
        eye_img = img[y1:y2, x1:x2]
        eye_pil = Image.fromarray(cv2.cvtColor(eye_img, cv2.COLOR_BGR2RGB))
        if auto_eq:
            eye_pil = ImageOps.autocontrast(eye_pil)
        if to_gray:
            eye_pil = ImageOps.grayscale(eye_pil)
        eye_pil = eye_pil.resize(target_size)
        eye_pil.save(output_path)
    return output_path

class EyeCLIPComparer:
    def __init__(self, device=None):
        self.device = device or ("cuda" if torch.cuda.is_available() else "cpu")
        self.model, self.preprocess = clip.load("ViT-B/32", device=self.device)
    def get_features(self, image_path):
        image = Image.open(image_path).convert("RGB")
        image_tensor = self.preprocess(image).unsqueeze(0).to(self.device)
        with torch.no_grad():
            features = self.model.encode_image(image_tensor)
            features /= features.norm(dim=-1, keepdim=True)
        return features.cpu().numpy()[0]
    def cosine_similarity(self, vec1, vec2):
        return float(np.dot(vec1, vec2))
    def compare(self, input_path, reference_dict):
        input_feat = self.get_features(input_path)
        similarities = {}
        for label, ref_path in reference_dict.items():
            ref_feat = self.get_features(ref_path)
            similarities[label] = self.cosine_similarity(input_feat, ref_feat)
        return similarities

def classify_lip_and_corner(image_path, width_thresh=0.0168, thickness_thresh=1.0000, angle_thresh=0.4):
    img = imread_for_korean_path(image_path)
    if img is None:
        raise ValueError(f"이미지를 로드할 수 없습니다: {image_path}")
    h, w, _ = img.shape
    mp_face_mesh = mp.solutions.face_mesh
    with mp_face_mesh.FaceMesh(static_image_mode=True, max_num_faces=1, refine_landmarks=True) as face_mesh:
        rgb = cv2.cvtColor(img, cv2.COLOR_BGR2RGB)
        results = face_mesh.process(rgb)
        if not results.multi_face_landmarks:
            raise ValueError("얼굴을 찾지 못했습니다.")
        landmarks = results.multi_face_landmarks[0].landmark
        x1 = landmarks[61].x * w
        x2 = landmarks[146].x * w
        mouth_width = abs(x2 - x1)
        x_face1 = landmarks[FACE_WIDTH_IDX[0]].x * w
        x_face2 = landmarks[FACE_WIDTH_IDX[1]].x * w
        face_width = abs(x_face2 - x_face1)
        width_ratio = mouth_width / (face_width + 1e-8)
        size_result = "큰입" if width_ratio >= width_thresh else "작은입"
        y_top = landmarks[13].y * h
        y_bottom = landmarks[14].y * h
        lip_thickness = abs(y_bottom - y_top)
        thickness_ratio = lip_thickness / (mouth_width + 1e-8)
        thickness_result = "두꺼운 입술" if thickness_ratio >= thickness_thresh else "얇은 입술"
        left = landmarks[61]
        right = landmarks[291]
        x1_c, y1_c = left.x * w, left.y * h
        x2_c, y2_c = right.x * w, right.y * h
        angle = math.degrees(math.atan2(y2_c - y1_c, x2_c - x1_c))
        corner_result = "올라간입" if angle < angle_thresh else "내려간입"
        return size_result, thickness_result, corner_result

def analyze_face(input_img_path):
    """
    Django 등에서 사용 가능한 얼굴 분석 함수
    (이미지 파일 경로를 인자로 받아 결과 문자열 반환)
    """
    result_prints = []

    # 눈 분석
    size_result = judge_bigeye(input_img_path)
    result_prints.append(f"눈 크기 판정: {size_result}")

    reference_labels = {
        "올라간눈": "eye/upeye.png",
        "내려간눈": "eye/downeye.png",
        "동그란눈": "eye/roundeye.png",
        "가는눈": "eye/thineye.png"
    }

    processed_refs = {}
    for label, fname in reference_labels.items():
        out_path = f"proc_{os.path.basename(fname)}"
        preprocess_eye_image(fname, out_path, EYE_IDX_ALL, padding_top=20, padding_side=15, target_size=(224,224), auto_eq=True)
        processed_refs[label] = out_path

    input_eye_img = preprocess_eye_image(
        input_img_path, "input_eye.png", EYE_IDX_ALL,
        padding_top=20, padding_side=15, target_size=(224,224), auto_eq=True
    )

    comparer = EyeCLIPComparer()
    eye_type_result = comparer.compare(input_eye_img, processed_refs)

    group_best = {
        "유형 1": max(["내려간눈", "올라간눈"], key=lambda label: eye_type_result.get(label, -float("inf"))),
        "유형 2": max(["동그란눈", "가는눈"], key=lambda label: eye_type_result.get(label, -float("inf"))),
        "유형 3": size_result
    }

    result_prints.append("\n[유형별 판별 결과]")
    result_prints.append(f"유형 1: {group_best['유형 1']} (유사도: {eye_type_result[group_best['유형 1']]:.4f})")
    result_prints.append(f"유형 2: {group_best['유형 2']} (유사도: {eye_type_result[group_best['유형 2']]:.4f})")
    result_prints.append(f"유형 3: {group_best['유형 3']}")

    # 눈썹 분석
    eyebrow_type1_refs = {
        "굵은 눈썹": "eyebrow/thick.png",
        "가는 눈썹": "eyebrow/thin.png"
    }
    eyebrow_type3_refs = {
        "일자형 눈썹": "eyebrow/straight.png",
        "올라간 눈썹": "eyebrow/up.png",
        "내려간 눈썹": "eyebrow/down.png"
    }

    eyebrow_rgba, eyebrow_landmarks, w, h = extract_eyebrow_rgba(resize_image(input_img_path))
    comparer2 = CLIPComparer()
    type1_result = comparer2.compare(eyebrow_rgba, eyebrow_type1_refs)
    type3_result = comparer2.compare(eyebrow_rgba, eyebrow_type3_refs)
    type2, type4 = analyze_eyebrow_geometry(eyebrow_landmarks, w, h)

    result_prints.append("\n[눈썹 결과]")
    result_prints.append("[1] 굵기:")
    for label, score in type1_result.items():
        result_prints.append(f" - {label}: {score:.4f}")
    result_prints.append(f" → 판단: {max(type1_result, key=type1_result.get)}")
    result_prints.append(f"[2] 길이: {type2}")
    result_prints.append("[3] 형태:")
    for label, score in type3_result.items():
        result_prints.append(f" - {label}: {score:.4f}")
    result_prints.append(f" → 판단: {max(type3_result, key=type3_result.get)}")
    result_prints.append(f"[4] 간격: {type4}")

    # 턱/얼굴형
    chin_shape, chin_length, angle = analyze_chin_geometry(eyebrow_landmarks, w, h)
    face_type, face_ratio = analyze_face_shape(eyebrow_landmarks, w, h)
    result_prints.append("\n[턱/얼굴형 결과]")
    result_prints.append(f"턱 형태: {chin_shape} (각도: {angle:.2f})")
    result_prints.append(f"턱 길이: {chin_length}")
    result_prints.append(f"얼굴형: {face_type} (비율: {face_ratio})")

    # 입 분석
    mouth_result = classify_lip_and_corner(input_img_path, angle_thresh=0.4)
    result_prints.append("\n[입 분석 결과]")
    result_prints.append(f"입 크기: {mouth_result[0]}")
    result_prints.append(f"입술 두께: {mouth_result[1]}")
    result_prints.append(f"입꼬리: {mouth_result[2]}")

    result_prints.append("분석 완료! 결과를 확인하세요.")

    return "\n".join(result_prints)
