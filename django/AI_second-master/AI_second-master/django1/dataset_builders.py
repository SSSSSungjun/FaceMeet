import os
import csv
from natsort import natsorted
from analysis.woo import run_chinfaceeyebrow_analysis
from analysis.kim import run_eyelip_analysis

# 경로 및 파일 설정
DATASET_DIR = 'dataset'
OUTPUT_CSV = 'match_dataset.csv'

# 키워드 → ID 매핑
keyword_to_id_maps = {
    'faceshape': {
        "둥근 얼굴": 1, "긴 얼굴": 2, "일반형 얼굴": 3
    },
    'eyebrow': {
        "굵은 눈썹": 1, "가는 눈썹": 2, "일자 눈썹": 5, "올라간 눈썹": 6, "내려간 눈썹": 7,
        "긴 눈썹": 3, "짧은 눈썹": 4, "사이가 넓은 눈썹": 8, "사이가 좁은 눈썹": 9
    },
    'eye': {
        "올라간 눈": 3, "내려간 눈": 4, "둥근 눈": 5, "가는 눈": 6,
        "큰 눈": 1, "작은 눈": 2
    },
    'nose': {
        "폭 넓은 코": 7, "폭 좁은 코": 8, "긴 코": 3, "짧은 코": 4,
        "코 끝이 올라간": 5, "코 끝이 내려간": 6, "높은 코": 1, "낮은 코": 2
    },
    'mouth': {
        "큰입": 1, "작은입": 2, "두꺼운 입술": 4, "얇은 입술": 3,
        "입가가 올라간 입": 5, "입가가 내려간 입": 6
    },
    'chin': {
        "둥글고 통통한 턱": 1, "각진 턱": 3, "뾰족한 턱": 2, "긴 턱": 4, "작은 턱": 5
    },
}

# 헤더 정의
columns = [f"A_{i}" for i in range(1, 18)] + [f"B_{i}" for i in range(1, 18)] + ["label"]


def extract_features(front_img, side_img):
    try:
        chin_result = run_chinfaceeyebrow_analysis(front_img, side_img, keyword_to_id_maps)
        lip_result = run_eyelip_analysis(front_img, keyword_to_id_maps)

        if chin_result is None or lip_result is None:
            return None

        # 1. 원래 key → 새로 쓸 key로 매핑
        key_map = {
            # 얼굴형
            "얼굴형": "faceshape",

            # 눈썹
            "눈썹 굵기": "eyebrow_thickness",
            "눈썹 길이": "eyebrow_length",
            "눈썹 형태": "eyebrow_angle",
            "눈썹 간격": "eyebrow_gap",

            # 눈
            "눈크기": "eye_size",
            "눈꼬리": "eye_angle",
            "눈모양": "eye_shape",

            # 코
            "코 형태": "nose_width",
            "코 길이": "nose_length",
            "들창/화살코": "nose_tip",
            "코 높이": "nose_height",

            # 입
            "입크기": "mouth_size",
            "입술두께": "lip_thickness",
            "입꼬리": "mouth_angle",

            # 턱
            "턱 형태": "chin_shape",
            "턱 길이": "chin_length",
        }

        # 2. 병합된 결과를 새 키로 변환
        merged = {key_map[k]: v for k, v in {**chin_result, **lip_result}.items()}

        # 3. 최종 순서 정의
        ordered_keys = [
            "faceshape",
            "eyebrow_thickness", "eyebrow_length", "eyebrow_angle", "eyebrow_gap",
            "eye_size", "eye_angle", "eye_shape",
            "nose_height", "nose_length", "nose_tip","nose_width", 
            "mouth_size", "lip_thickness", "mouth_angle",
            "chin_shape", "chin_length"
        ]

        # 4. 순서에 맞춰 value 추출
        return [merged[k] for k in ordered_keys]

    except Exception as e:
        print(f"❌ 분석 실패: {front_img} + {side_img} → {e}")
        return None


def main():
    files = [f for f in os.listdir(DATASET_DIR) if f.endswith('.png')]
    files = natsorted(files)

    rows = []
    for i in range(0, len(files), 4):
        try:
            A_front = os.path.join(DATASET_DIR, files[i])
            A_side  = os.path.join(DATASET_DIR, files[i+1])
            B_front = os.path.join(DATASET_DIR, files[i+2])
            B_side  = os.path.join(DATASET_DIR, files[i+3])

            print(f"🔍 분석 중: {files[i]} + {files[i+1]} vs {files[i+2]} + {files[i+3]}")

            A_feat = extract_features(A_front, A_side)
            B_feat = extract_features(B_front, B_side)

            if A_feat and B_feat:
                rows.append(A_feat + B_feat + [""])  # label은 비워둠

        except Exception as e:
            print(f"⚠️ 예외 발생: {e}")
            continue

    # CSV 저장
    with open(OUTPUT_CSV, mode='w', newline='', encoding='utf-8') as f:
        writer = csv.writer(f)
        writer.writerow(columns)
        writer.writerows(rows)

    print(f"\n✅ CSV 저장 완료: {OUTPUT_CSV} ({len(rows)}개 샘플)")

if __name__ == "__main__":
    main()
