# articles/matching_engine.py
import torch
import numpy as np
import pandas as pd
from articles.models import User, FaceAnalysis, EyeComb, EyebrowComb, NoseComb, MouthComb, ChinComb, Matching
from two_tower_model import TwoTowerModel
from math import radians, cos, sin, asin, sqrt
from datetime import date, timedelta
import os
from django.conf import settings
from django.db.models import Q

model_path = os.path.join(settings.BASE_DIR, "two_tower_model.pt")
dataset_path = os.path.join(settings.BASE_DIR, "match_dataset.csv")

def haversine(lat1, lon1, lat2, lon2):
    R = 6371.0
    lat1, lon1, lat2, lon2 = map(radians, [lat1, lon1, lat2, lon2])
    dlat = lat2 - lat1
    dlon = lon2 - lon1
    a = sin(dlat/2)**2 + cos(lat1) * cos(lat2) * sin(dlon/2)**2
    c = 2 * asin(sqrt(a))
    return R * c

def get_parts_ids(comb_obj, parts_fields):
    return [getattr(comb_obj, field) for field in parts_fields if getattr(comb_obj, field) is not None]

def find_best_match(id):
    print(f"🎯 find_best_match 요청됨: user_id={id}")
    user = User.objects.filter(user_id=id).first()
    if not user or not user.face_id:
        print("user테이블에 사람이 하나도 없어요!")
        return None , None

    user_we = user.latitude
    user_gyung = user.longitude

    face_id = user.face_id
    target_face = FaceAnalysis.objects.get(face_id=face_id)

    eye_ids = get_parts_ids(EyeComb.objects.get(eye_comb_id=target_face.eye_comb_id), ['eye_parts_id1', 'eye_parts_id2', 'eye_parts_id3'])
    eyebrow_ids = get_parts_ids(EyebrowComb.objects.get(eyebrow_comb_id=target_face.eyebrow_comb_id), ['eyebrow_parts_id1', 'eyebrow_parts_id2', 'eyebrow_parts_id3', 'eyebrow_parts_id4'])
    nose_ids = get_parts_ids(NoseComb.objects.get(nose_comb_id=target_face.nose_comb_id), ['nose_parts_id1', 'nose_parts_id2', 'nose_parts_id3', 'nose_parts_id4'])
    mouth_ids = get_parts_ids(MouthComb.objects.get(mouth_comb_id=target_face.mouth_comb_id), ['mouth_parts_id1', 'mouth_parts_id2', 'mouth_parts_id3'])
    chin_ids = get_parts_ids(ChinComb.objects.get(chin_comb_id=target_face.chin_comb_id), ['chin_parts_id1', 'chin_parts_id2'])
    target_gender = 'f' if user.gender == 'm' else 'm' if user.gender == 'f' else None
    if not target_gender:
        print("⚠️ 성별 정보가 없거나 중립입니다. 매칭 중단.")
        return None, None
    feature_vector = [target_face.faceshape_id] + eyebrow_ids + eye_ids + nose_ids + mouth_ids + chin_ids
    target_vec = torch.tensor(feature_vector, dtype=torch.int64)

    model = TwoTowerModel(input_dim=17)
    model.load_state_dict(torch.load(model_path))
    model.eval()

    df = pd.read_csv(dataset_path)
    X_B = df.iloc[:, 17:34].values.astype(np.float32)
    X_B_tensor = torch.tensor(X_B, dtype=torch.float32)

    with torch.no_grad():
        _, _, B_embeds = model(torch.zeros_like(X_B_tensor), X_B_tensor)

    A_tensor = target_vec.to(dtype=torch.float32).unsqueeze(0)
    dummy_B = torch.zeros_like(A_tensor)
    with torch.no_grad():
        _, A_embed, _ = model(A_tensor, dummy_B)

    cos_sim = torch.nn.functional.cosine_similarity(A_embed, B_embeds)
    topk = torch.topk(cos_sim, k=5)

    for idx, score in zip(topk.indices, topk.values):
        b_vector = X_B[idx.item()].astype(int).tolist()
        faceshape_id, eyebrow_ids, eye_ids, nose_ids, mouth_ids, chin_ids = (
            b_vector[0], b_vector[1:5], b_vector[5:8], b_vector[8:12], b_vector[12:15], b_vector[15:17])

        try:
            eye_comb = EyeComb.objects.filter(eye_parts_id1=eye_ids[0], eye_parts_id2=eye_ids[1], eye_parts_id3=eye_ids[2]).first()
            eyebrow_comb = EyebrowComb.objects.filter(eyebrow_parts_id1=eyebrow_ids[0], eyebrow_parts_id2=eyebrow_ids[1], eyebrow_parts_id3=eyebrow_ids[2], eyebrow_parts_id4=eyebrow_ids[3]).first()
            nose_comb = NoseComb.objects.filter(nose_parts_id1=nose_ids[0], nose_parts_id2=nose_ids[1], nose_parts_id3=nose_ids[2], nose_parts_id4=nose_ids[3]).first()
            mouth_comb = MouthComb.objects.filter(mouth_parts_id1=mouth_ids[0], mouth_parts_id2=mouth_ids[1], mouth_parts_id3=mouth_ids[2]).first()
            chin_comb = ChinComb.objects.filter(chin_parts_id1=chin_ids[0], chin_parts_id2=chin_ids[1]).first()

            if all([eye_comb, eyebrow_comb, nose_comb, mouth_comb, chin_comb]):
                matched_face = FaceAnalysis.objects.filter(
                    faceshape_id=faceshape_id,
                    eyebrow_comb_id=eyebrow_comb.eyebrow_comb_id,
                    eye_comb_id=eye_comb.eye_comb_id,
                    nose_comb_id=nose_comb.nose_comb_id,
                    mouth_comb_id=mouth_comb.mouth_comb_id,
                    chin_comb_id=chin_comb.chin_comb_id
                ).first()

                if matched_face:
                    today = date.today()
                    lower_birth = today - timedelta(days=365.25 * user.prefer_age_upper)
                    upper_birth = today - timedelta(days=365.25 * user.prefer_age_lower)

                    # ⚠️ Matching 이력 있는 유저 제외
                    already_matched_ids = Matching.objects.filter(
                        Q(requester_id=id) | Q(accepter_id=id)
                    ).values_list('requester_id', 'accepter_id')

                    # 1. 튜플로 되어 있어서 단일 user_id로 풀기
                    already_matched_ids_flat = set()
                    for pair in already_matched_ids:
                        already_matched_ids_flat.update(pair)

                    # 2. 자기 자신도 제외
                    already_matched_ids_flat.add(id)

                    # 3. 필터링된 matched_users 쿼리셋
                    matched_users = User.objects.filter(
                        face_id=matched_face.face_id,
                        birth__range=(lower_birth, upper_birth),
                        gender=target_gender  # ✅ 이성만
                    ).exclude(user_id__in=already_matched_ids_flat)

                    min_distance = float('inf')
                    closest_user = None
                    for user in matched_users:
                        if user.latitude is not None and user.longitude is not None:
                            dist = haversine(user_we, user_gyung, user.latitude, user.longitude)
                            if dist < min_distance:
                                min_distance = dist
                                closest_user = user
                    raw_score = score.item()
                    scaled_score = round(30 + (raw_score * 50), 3)

                    if closest_user:
                        return closest_user.user_id, scaled_score
                    elif matched_users.exists():
                        return matched_users.first().user_id, scaled_score

        except:
            continue

        # 1. 매칭된 모든 user_id 가져오기
    already_matched_ids = Matching.objects.filter(
        Q(requester_id=id) | Q(accepter_id=id)
    ).values_list('requester_id', 'accepter_id')

    # 2. 튜플 -> flat set으로 변환
    already_matched_ids_flat = set()
    for pair in already_matched_ids:
        already_matched_ids_flat.update(pair)
    already_matched_ids_flat.add(id)  # 자기 자신도 제외

    # 3. fallback 후보에서 제외 조건 반영
    fallback = User.objects.exclude(
        user_id__in=already_matched_ids_flat
    ).filter(
        face_id__isnull=False,
        gender=target_gender,
        is_deleted = False
    ).order_by('?').first()

    if fallback:
        print("⚠️ fallback 사용자 리턴 중:", fallback.user_id)
        return fallback.user_id, 0.0

    print("❌ fallback도 실패. No match.")
    return "EO3" , "no more user"