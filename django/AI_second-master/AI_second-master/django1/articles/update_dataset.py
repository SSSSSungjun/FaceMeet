# articles/test_append_dataset_room17.py
import os
import sys
import csv
from datetime import datetime
import django

# --- 콘솔 출력 UTF-8 강제 (윈도우 cp949 이슈 회피) ---
if hasattr(sys.stdout, "reconfigure"):
    try:
        sys.stdout.reconfigure(encoding="utf-8")
        sys.stderr.reconfigure(encoding="utf-8")
    except Exception:
        pass

# --- Django 설정 로드 ---
PROJECT_ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
sys.path.append(PROJECT_ROOT)
os.environ.setdefault("DJANGO_SETTINGS_MODULE", "firstpjt.settings")
django.setup()

from django.conf import settings
from django.utils import timezone
from articles.models import (
    ChatRoom, User, FaceAnalysis, EyeComb, EyebrowComb, NoseComb, MouthComb, ChinComb
)
from django.core.exceptions import ObjectDoesNotExist


def get_parts(face_id):
    """
    face_id로부터 17차원 이목구비 벡터 생성
    실패 시 None 반환
    """
    try:
        fa = FaceAnalysis.objects.get(face_id=face_id)
        eye = EyeComb.objects.get(eye_comb_id=fa.eye_comb_id)
        eyebrow = EyebrowComb.objects.get(eyebrow_comb_id=fa.eyebrow_comb_id)
        nose = NoseComb.objects.get(nose_comb_id=fa.nose_comb_id)
        mouth = MouthComb.objects.get(mouth_comb_id=fa.mouth_comb_id)
        chin = ChinComb.objects.get(chin_comb_id=fa.chin_comb_id)

        return (
            [fa.faceshape_id] +
            [getattr(eyebrow, f"eyebrow_parts_id{i}") for i in range(1, 5)] +
            [getattr(eye, f"eye_parts_id{i}") for i in range(1, 4)] +
            [getattr(nose, f"nose_parts_id{i}") for i in range(1, 5)] +
            [getattr(mouth, f"mouth_parts_id{i}") for i in range(1, 4)] +
            [getattr(chin, f"chin_parts_id{i}") for i in range(1, 3)]
        )
    except ObjectDoesNotExist:
        return None


def append_to_dataset(A_vector, B_vector, label):
    """
    BASE_DIR/match_dataset.csv 에 UTF-8 BOM으로 누적 저장 (엑셀 호환)
    신규 파일이면 헤더도 기록
    """
    dataset_path = os.path.join(settings.BASE_DIR, "match_dataset.csv")
    file_exists = os.path.exists(dataset_path)

    # 현재 데이터 로드 후 중복 여부 확인
    if file_exists:
        with open(dataset_path, "r", encoding="utf-8-sig", newline="") as f:
            reader = csv.reader(f)
            header = next(reader, None)  # 첫 줄이 헤더라면 건너뜀
            for row in reader:
                if row == list(map(str, A_vector + B_vector + [label])):
                    print(f"[SKIP] 중복 데이터로 저장 안 함")
                    return  # 이미 있으면 종료
                
    with open(dataset_path, "a", newline="", encoding="utf-8-sig") as f:
        w = csv.writer(f)
        if not file_exists:
            header = [f"A{i}" for i in range(1, 18)] + [f"B{i}" for i in range(1, 18)] + ["label"]
            w.writerow(header)
        w.writerow(A_vector + B_vector + [label])


def _today_start():
    """
    settings.USE_TZ 기준으로 오늘 00:00 반환
    """
    if getattr(settings, "USE_TZ", False):
        # 현재 타임존의 오늘 00:00 aware datetime
        local_now = timezone.localtime()
        return local_now.replace(hour=0, minute=0, second=0, microsecond=0)
    else:
        # naive KST 운영이면 그냥 now 기준
        return datetime.now().replace(hour=0, minute=0, second=0, microsecond=0)


def main():
    try:
        today_start = _today_start()
        qs = ChatRoom.objects.filter(created_at__gte=today_start).only(
            "room_id", "user1_id", "user2_id", "user1_selected", "user2_selected"
        )

        saved = skipped = 0
        for room in qs:
            like1, like2 = room.user1_selected, room.user2_selected
            if like1 is None or like2 is None:
                skipped += 1
                continue

            label = 1 if (like1 and like2) else 0

            u1 = User.objects.filter(user_id=room.user1_id).only("face_id").first()
            u2 = User.objects.filter(user_id=room.user2_id).only("face_id").first()
            if not u1 or not u2 or not u1.face_id or not u2.face_id:
                print(f"[SKIP] face_id 없음: room_id={room.room_id}")
                skipped += 1
                continue

            A = get_parts(u1.face_id)
            B = get_parts(u2.face_id)
            if A is None or B is None:
                print(f"[SKIP] 조합 파트 조회 실패: room_id={room.room_id}")
                skipped += 1
                continue

            append_to_dataset(A, B, label)
            print(f"[OK] 저장 성공: room_id={room.room_id}, u1={room.user1_id}, u2={room.user2_id}, label={label}")
            saved += 1

        print(f"[DONE] saved={saved}, skipped={skipped}")
    except Exception as e:
        # 이모지 없이 출력 (윈도우 콘솔 인코딩 이슈 방지)
        print(f"[ERR] 오류 발생: {e}")


if __name__ == "__main__":
    main()
