# articles/jobs.py
import os
import sys
import subprocess
from django.conf import settings
from django.utils import timezone

from articles.services.daily_sentiment import analyze_and_write_to_chat_room

def run_sentiment_job():
    """채팅방 감정 분석 → chat_room 업데이트"""
    res = analyze_and_write_to_chat_room()
    print(f"[{timezone.now()}] sentiment job done: {len(res)} rooms")


def run_update_and_train_job():
    """데이터셋 갱신 + 모델 학습 (subprocess로 실행)"""
    base = settings.BASE_DIR  # manage.py 위치
    py = sys.executable       # 현재 venv 파이썬
    env = {**os.environ, "PYTHONIOENCODING": "utf-8"}  # 자식 프로세스 UTF-8 강제

    print("주기 작업: 데이터셋 갱신 + 모델 재학습 시작")

    # update_dataset.py
    upd_path = os.path.join(base, "articles", "update_dataset.py")
    upd = subprocess.run([py, upd_path], stdout=subprocess.PIPE, stderr=subprocess.PIPE, env=env)
    print("✅ update_dataset.py stdout:\n", upd.stdout.decode("utf-8", errors="replace"))
    if upd.stderr:
        print("[warn] update_dataset.py stderr:\n", upd.stderr.decode("utf-8", errors="replace"))

    print("✅ update_dataset.py stdout:\n", upd.stdout)
    if upd.stderr:
        print("[warn] update_dataset.py stderr:\n", upd.stderr)

    # train.py
    train_path = os.path.join(base, "train.py")
    tr = subprocess.run([py, train_path], capture_output=True, text=True, env=env)
    print("[ok] train.py stdout:\n", tr.stdout)
    if tr.stderr:
        print("[warn] train.py stderr:\n", tr.stderr)

    print(f"[{timezone.now()}] update+train job done (rc: {upd.returncode}, {tr.returncode})")


def run_append_dataset_job():
    """오늘 생성된 room들로 match_dataset.csv에 A(17)+B(17)+label 한 줄씩 추가"""
    from articles.update_dataset import main as append_dataset_main
    append_dataset_main()
    print(f"[{timezone.now()}] append_dataset job done")
