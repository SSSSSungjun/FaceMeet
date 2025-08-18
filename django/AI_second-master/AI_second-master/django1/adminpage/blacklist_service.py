import requests
import logging
from django.conf import settings

logger = logging.getLogger(__name__)


BASE_URL = "https://i13d201.p.ssafy.io"
HEADERS = {
    "Authorization": f"Bearer {settings.API_BEARER_TOKEN}",
    "Content-Type": "application/json"
}

def get_blacklist_list():
    url = f"{BASE_URL}/api/v1/admin/blacklist/"
    try:
        res = requests.get(url, headers=HEADERS)
        res.raise_for_status()
        return res.json(), res.status_code
    except requests.RequestException as e:
        logger.error(f"블랙리스트 목록 조회 실패: {e}")
        return {"error": str(e)}, 500

def create_blacklist(data):
    url = f"{BASE_URL}/api/v1/admin/blacklist/"
    try:
        res = requests.post(url, headers=HEADERS, json=data)
        res.raise_for_status()
        return res.json(), res.status_code
    except requests.RequestException as e:
        logger.error(f"블랙리스트 생성 실패: {e}")
        return {"error": str(e)}, 500

def delete_blacklist(blacklist_id):
    url = f"{BASE_URL}/api/v1/admin/blacklist/{blacklist_id}"
    try:
        res = requests.delete(url, headers=HEADERS)
        # DELETE의 경우 정상적으로 삭제되면 보통 204 No Content가 반환됨
        if res.status_code == 204:
            return {"detail": "삭제 성공"}, 204
        return res.json(), res.status_code
    except requests.RequestException as e:
        logger.error(f"블랙리스트 삭제 실패({blacklist_id}): {e}")
        return {"error": str(e)}, 500
