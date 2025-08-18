import os
import django
import torch
import numpy as np
import pandas as pd
from two_tower_model import TwoTowerModel
# articles/matching_engine.py 상단에 추가

model_path = os.path.join(settings.BASE_DIR, "two_tower_model.pt")
dataset_path = os.path.join(settings.BASE_DIR, "match_dataset.csv")
import os
from django.conf import settings
# Django 환경 설정
os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'firstpjt.settings')
django.setup()
from articles.models import User
from articles.models import FaceAnalysis, EyeComb, EyebrowComb, NoseComb, MouthComb, ChinComb, Matching
from math import radians, cos, sin, asin, sqrt

def haversine(lat1, lon1, lat2, lon2):
    # 지구 반지름 (킬로미터)
    R = 6371.0

    # 위도/경도 라디안 변환
    lat1, lon1, lat2, lon2 = map(radians, [lat1, lon1, lat2, lon2])

    dlat = lat2 - lat1
    dlon = lon2 - lon1

    a = sin(dlat/2)**2 + cos(lat1) * cos(lat2) * sin(dlon/2)**2
    c = 2 * asin(sqrt(a))
    return R * c

def get_parts_ids(comb_obj, parts_fields):
    return [getattr(comb_obj, field) for field in parts_fields if getattr(comb_obj, field) is not None]

def find_best_match(id):
    # 대상 face 가져오기
    print("user_id:",id)
    user = User.objects.filter(user_id=id).first()
    user_we = user.latitude
    user_gyung = user.longitude

    
    print("위도:",user_we, "경도:", user_gyung)
    print(user.user_id)
    if not user or not user.face_id:
        print("❌ 유효한 사용자 또는 face_id 없음")
        return None, None

    face_id = user.face_id
    print(face_id)
    target_face = FaceAnalysis.objects.get(face_id=face_id)

    # comb에서 parts id 추출
    eye_ids = get_parts_ids(EyeComb.objects.get(eye_comb_id=target_face.eye_comb_id), ['eye_parts_id1', 'eye_parts_id2', 'eye_parts_id3'])
    eyebrow_ids = get_parts_ids(EyebrowComb.objects.get(eyebrow_comb_id=target_face.eyebrow_comb_id), ['eyebrow_parts_id1', 'eyebrow_parts_id2', 'eyebrow_parts_id3', 'eyebrow_parts_id4'])
    nose_ids = get_parts_ids(NoseComb.objects.get(nose_comb_id=target_face.nose_comb_id), ['nose_parts_id1', 'nose_parts_id2', 'nose_parts_id3', 'nose_parts_id4'])
    mouth_ids = get_parts_ids(MouthComb.objects.get(mouth_comb_id=target_face.mouth_comb_id), ['mouth_parts_id1', 'mouth_parts_id2', 'mouth_parts_id3'])
    chin_ids = get_parts_ids(ChinComb.objects.get(chin_comb_id=target_face.chin_comb_id), ['chin_parts_id1', 'chin_parts_id2'])

    feature_vector = [target_face.faceshape_id] + eyebrow_ids + eye_ids + nose_ids + mouth_ids + chin_ids
    print("🎯 입력 벡터:", feature_vector)
    target_vec = torch.tensor(feature_vector, dtype=torch.int64)

    # 모델 로드
    model = TwoTowerModel(input_dim=17)
    model.load_state_dict(torch.load(model_path))
    model.eval()

    # match dataset 불러오기
    df = pd.read_csv(dataset_path)
    X_B = df.iloc[:, 17:34].values.astype(np.float32)
    X_B_tensor = torch.tensor(X_B, dtype=torch.float32)

    # B 임베딩 생성
    with torch.no_grad():
        _, _, B_embeds = model(torch.zeros_like(X_B_tensor), X_B_tensor)

    # A 임베딩 생성
    A_tensor = target_vec.detach().clone().to(dtype=torch.float32).unsqueeze(0)
    dummy_B = torch.zeros_like(A_tensor)
    with torch.no_grad():
        _, A_embed, _ = model(A_tensor, dummy_B)

    # 유사도 계산
    cos_sim = torch.nn.functional.cosine_similarity(A_embed, B_embeds)
    topk = torch.topk(cos_sim, k=5)

    # 추천 조합 순회
    print("📌 Top-5 추천 조합:")
    for idx in topk.indices:
        print(X_B[idx.item()].astype(int).tolist())

    for idx, score in zip(topk.indices, topk.values):
        b_vector = X_B[idx.item()].astype(int).tolist()
        faceshape_id = b_vector[0]
        eyebrow_ids = b_vector[1:5]
        eye_ids = b_vector[5:8]
        nose_ids = b_vector[8:12]
        mouth_ids = b_vector[12:15]
        chin_ids = b_vector[15:17]

        try:
            eye_comb = EyeComb.objects.filter(
                eye_parts_id1=eye_ids[0], eye_parts_id2=eye_ids[1], eye_parts_id3=eye_ids[2]
            ).first()
            eyebrow_comb = EyebrowComb.objects.filter(
                eyebrow_parts_id1=eyebrow_ids[0], eyebrow_parts_id2=eyebrow_ids[1],
                eyebrow_parts_id3=eyebrow_ids[2], eyebrow_parts_id4=eyebrow_ids[3]
            ).first()
            nose_comb = NoseComb.objects.filter(
                nose_parts_id1=nose_ids[0], nose_parts_id2=nose_ids[1],
                nose_parts_id3=nose_ids[2], nose_parts_id4=nose_ids[3]
            ).first()
            mouth_comb = MouthComb.objects.filter(
                mouth_parts_id1=mouth_ids[0], mouth_parts_id2=mouth_ids[1], mouth_parts_id3=mouth_ids[2]
            ).first()
            chin_comb = ChinComb.objects.filter(
                chin_parts_id1=chin_ids[0], chin_parts_id2=chin_ids[1]
            ).first()

            if all([eye_comb, eyebrow_comb, nose_comb, mouth_comb, chin_comb]):
                matched_face = FaceAnalysis.objects.filter(
                    faceshape_id=faceshape_id,
                    eyebrow_comb_id=eyebrow_comb.eyebrow_comb_id,
                    eye_comb_id=eye_comb.eye_comb_id,
                    nose_comb_id=nose_comb.nose_comb_id,
                    mouth_comb_id=mouth_comb.mouth_comb_id,
                    chin_comb_id=chin_comb.chin_comb_id
                ).first()
                result = 0
                if matched_face:
                    print(f"🎯 DB에서 매칭된 face_id: {matched_face.face_id}")

                    # 1. 해당 face_id를 가진 모든 유저 가져오기
                    matched_users = User.objects.filter(face_id=matched_face.face_id)
                    print("face_id를 가진 사용자:",matched_users)

                    # 2. 초기화: 최소 거리 및 가장 가까운 사용자 저장용
                    min_distance = float('inf')
                    closest_user = None

                    for user in matched_users:
                        already_matched = Matching.objects.filter(
                            requester_id=id, accepter_id=user.user_id
                        ).exists() or Matching.objects.filter(
                            requester_id=user.user_id, accepter_id=id
                        ).exists()

                        if already_matched:
                            print(f"⛔ 이미 매칭된 전적이 있는 user_id: {user.user_id}, 제외")
                            continue
                        if user.latitude is None or user.longitude is None:
                            continue  # 위치 정보 없는 유저는 건너뜀

                        match_we = user.latitude
                        match_gyung = user.longitude

                        # 3. 거리 계산
                        dist = haversine(user_we, user_gyung, match_we, match_gyung)

                        # 4. 최소 거리 갱신
                        if dist < min_distance:
                            min_distance = dist
                            closest_user = user

                    if closest_user:
                        print(f"✅ 가장 가까운 사용자 user_id: {closest_user.user_id} (거리: {min_distance:.2f} km)")
                        return closest_user.user_id, round(score.item(), 4)*100
                    else:
                        print("❌ 위도/경도 정보가 있는 사용자가 없음")
                else:
                    print("❌ DB에 해당 조합 없음")


    
        except:
            continue

    # top-5 중 매칭이 없다면 랜덤 반환
    random_match = User.objects.order_by('?').first()
    if random_match:
        print("🎲 Top-5 실패 → 랜덤 추천으로 fallback")
        return random_match.user_id, 0.0

    return None, None


if __name__ == "__main__":
    test_user_id = 1 # 테스트할 user_id
    match_id, similarity = find_best_match(test_user_id)

    if match_id:
        print(f"✅ Best match face_id: {match_id} (유사도: {similarity})")
    else:
        print("❌ 매칭되는 face 정보를 찾을 수 없습니다.")
