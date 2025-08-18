# -*- coding: utf-8 -*-
import io
from pathlib import Path
from typing import Dict, List
import logging

from PIL import Image
from django.conf import settings
from django.core.files.base import ContentFile
from django.core.files.storage import default_storage
from django.http import HttpResponse, Http404
from mimetypes import guess_type

logger = logging.getLogger(__name__)

# ===================== 설정 (Django settings.py에서 관리) ======================
ROOT_IMG = settings.BASE_DIR / "makeface" / "assets" / "composition_parts"

BASE_LAYER_NAMES = ["귀.png"]

PART_FOLDERS = {  # 파트명 : 하위폴더 (순서 = 위에 쌓이는 순서)
    "faceshape": "얼굴형",
    "eye": "눈",
    "eyebrow": "눈썹",
    "nose": "코",
    "mouth": "입",
    "chin": "턱",
}
# =======================================================

# 내부 캐시
_img_cache: Dict[Path, Image.Image] = {}


# -------------------------------------------------------
def _load_rgba(path: Path) -> Image.Image:
    """이미지를 RGBA로 로드(캐시 사용)."""
    if path not in _img_cache:
        if not path.exists():
            raise FileNotFoundError(f"이미지 파일을 찾을 수 없습니다: {path}")
        _img_cache[path] = Image.open(path).convert("RGBA")
    return _img_cache[path].copy()  # 합성 과정에서 손상 방지


def _compose(paths: List[Path], size=None) -> Image.Image:
    """경로 리스트를 아래→위 순서로 합성."""
    base = _load_rgba(paths[0])
    if size and base.size != size:
        base = base.resize(size, Image.LANCZOS)
    for p in paths[1:]:
        img = _load_rgba(p)
        if size and img.size != size:
            img = img.resize(size, Image.LANCZOS)
        base = Image.alpha_composite(base, img)
    return base
# -------------------------------------------------------

# ===================== 공개 API =========================
def init_env():
    """폴더/파일 존재 확인. Django 앱 시작 시 1회 호출."""
    # 고정 레이어 확인
    for n in BASE_LAYER_NAMES:
        p = ROOT_IMG / n
        if not p.exists():
            raise FileNotFoundError(f"기본 레이어 못 찾음: {p.resolve()}")

    # 파트별 폴더 확인
    for key, folder in PART_FOLDERS.items():
        part_dir = ROOT_IMG / folder
        if not part_dir.is_dir():
            print(f"[경고] '{folder}' 폴더를 찾을 수 없습니다: {part_dir}")

    print("[OK] 관상 이미지 합성기(ID 기반) 초기화 완료")

import boto3
from django.conf import settings
import os
from django.http import HttpResponse

def upload_image_to_s3(local_path: str) -> str:
    if not os.path.isabs(local_path):
        local_path = os.path.join(settings.MEDIA_ROOT, local_path)

    s3 = boto3.client(
        's3',
        aws_access_key_id=settings.AWS_ACCESS_KEY_ID,
        aws_secret_access_key=settings.AWS_SECRET_ACCESS_KEY,
        region_name=settings.AWS_S3_REGION_NAME
    )
    
    if not os.path.exists(local_path):
        raise FileNotFoundError(f"이미지 파일을 찾을 수 없습니다: {local_path}")

    s3_path = f"composed/{os.path.basename(local_path)}"
    s3.upload_file(
        local_path,
        settings.AWS_STORAGE_BUCKET_NAME,
        s3_path,
        ExtraArgs={'ContentType': 'image/png', 'ContentDisposition': 'inline'}
    )
    s3_url = f"https://{settings.AWS_STORAGE_BUCKET_NAME}.s3.{settings.AWS_S3_REGION_NAME}.amazonaws.com/{s3_path}"

    return s3_url
    
def compose_by_dbid(selection_ids: Dict[str, list[int]]) -> str:
    """
    DB ID 리스트로 이미지를 합성하고, 생성된 파일의 URL을 반환합니다.
    selection_ids 예: {"eye": [2, 5], "chin": [1, 2], ...}
    """
    # 1. 기본 레이어 준비
    base_paths = [ROOT_IMG / n for n in BASE_LAYER_NAMES]
    target_size = _load_rgba(base_paths[0]).size

    # 2. 선택된 파트 경로 구성 (PART_FOLDERS 순서대로)
    chosen_paths = []
    bits = []
    # PART_FOLDERS의 순서를 보장하기 위해 items() 사용
    for key, part_folder_name in PART_FOLDERS.items():
        ids = selection_ids.get(key)
        if not ids:
            continue  # 해당 파트에 대한 ID가 없으면 건너뜀

        part_folder_path = ROOT_IMG / part_folder_name
        ids_sorted = sorted([int(x) for x in ids])
        fname = "-".join(map(str, ids_sorted)) + ".png"
        file_path = part_folder_path / fname

        if not file_path.exists():
            # raise FileNotFoundError(f"이미지 파일 없음: {file_path}")
            logger.warning(f"요청된 이미지 파일 없음, 해당 파트를 건너뜁니다: {file_path}")
            continue  # 오류를 발생시키는 대신, 해당 파트를 건너뜁니다.

        chosen_paths.append(file_path)
        bits.append(f"{key}-{'-'.join(map(str, ids_sorted))}")

    # 3. 이미지 합성 및 저장
    img = _compose(base_paths + chosen_paths, size=target_size)
    filename = ("_".join(bits) if bits else "base") + ".png"

    buffer = io.BytesIO()
    img.save(buffer, format="PNG")
    file_content = ContentFile(buffer.getvalue(), name=filename)

    # media/composed/ 폴더에 저장하고 URL 반환
    saved_path = default_storage.save(f"composed/{filename}", file_content)

   
    return upload_image_to_s3( saved_path)
