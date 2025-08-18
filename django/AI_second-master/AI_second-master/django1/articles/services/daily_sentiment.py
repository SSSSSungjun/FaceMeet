from datetime import datetime
from django.utils import timezone
from django.db import transaction
from typing import Iterable
from transformers import pipeline

from articles.models import Message, ChatRoom

# ===== 설정 =====
MODEL_ID = "chunwoolee0/nsmc_roberta_base_model"
POS_TH = 0.55              # 평균 pos 확률 임계치
PER_ROOM_LIMIT = 100       # 방당 최근 메시지 개수
BATCH_SIZE = 16            # 추론 배치 크기

# 모델은 프로세스 1회만 로드
clf = pipeline("text-classification", model=MODEL_ID)

def _pos_probs(texts: list[str]) -> list[float]:
    probs: list[float] = []
    for i in range(0, len(texts), BATCH_SIZE):
        batch = texts[i:i+BATCH_SIZE]
        preds = clf(batch, truncation=True)
        for p in preds:
            if p["label"].lower().startswith("pos"):
                pos = float(p["score"])
            else:
                pos = 1.0 - float(p["score"])
            probs.append(pos)
    return probs

def _fetch_recent_texts(room_id: int, limit: int) -> list[str]:
    # 최근 기준으로 TEXT만 가져오고, 비어있는 content 제외
    qs = (Message.objects
          .filter(room_id=room_id, type="TEXT")
          .exclude(content__isnull=True)
          .exclude(content__exact="")
          .order_by("-send_at")
          .values_list("content", flat=True)[:limit])
    # 최신 → 과거 순서로 뽑혔으니 모델 입력 전 과거→최신 순서로 뒤집어도 되고 안 뒤집어도 성능 차이는 없음
    return list(qs)

@transaction.atomic
def analyze_and_write_to_chat_room(room_ids: Iterable[int] | None = None) -> dict[int, dict]:
    """
    room_ids가 None이면 chat_room 전체에 대해 실행.
    각 방의 최근 PER_ROOM_LIMIT개의 메시지로 평균 pos 확률을 계산하고
    chat_room.user1_selected / user2_selected 를 (1,1) 또는 (0,0) 으로 저장.
    """
    now = timezone.now()

    if room_ids is None:
        room_ids = ChatRoom.objects.values_list("room_id", flat=True)

    results: dict[int, dict] = {}
    for rid in room_ids:
        texts = _fetch_recent_texts(rid, PER_ROOM_LIMIT)
        if not texts:
            # 메시지 없으면 스킵
            continue

        pos_list = _pos_probs(texts)
        pos_avg = sum(pos_list) / len(pos_list)
        is_positive = pos_avg >= POS_TH

        # chat_room 업데이트 (둘 다 동일하게 1/1 또는 0/0)
        # selected_at 타임스탬프도 같이 갱신(요구에 맞게 주석 처리 가능)
        ChatRoom.objects.filter(room_id=rid).update(
            user1_selected=is_positive,
            user2_selected=is_positive,
            user1_selected_at=now,
            user2_selected_at=now,
        )

        results[rid] = {
            "count": len(texts),
            "pos_avg": float(pos_avg),
            "flag": 1 if is_positive else 0,
        }

    return results
