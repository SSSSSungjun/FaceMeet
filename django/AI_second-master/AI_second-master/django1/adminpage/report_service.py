import requests
import logging
from django.conf import settings

logger = logging.getLogger(__name__)

API_BEARER_TOKEN = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiI4MCIsInJvbGUiOiJST0xFX0FETUlOIiwiaWF0IjoxNzU0MjA0NTAxLCJleHAiOjE3NTQyMTA1MDF9.kjG3duh0-Dj-fMA71LFB4mfjkejg0osZ81q_wtCaEaU"


BASE_URL = "https://i13d201.p.ssafy.io"

HEADERS = {
    "Authorization": f"Bearer {API_BEARER_TOKEN}",
    "Content-Type": "application/json"
}

def create_report(data):
    url = f"{BASE_URL}/api/v1/reports/"
    try:
        res = requests.post(url, headers=HEADERS, json=data)
        res.raise_for_status()
        return res.json(), res.status_code
    except requests.RequestException as e:
        logger.error(f"신고 생성 실패: {e}")
        return {"error": str(e)}, 500

def complete_report(report_id):
    url = f"{BASE_URL}/api/v1/admin/reports/{report_id}"
    try:
        res = requests.post(url, headers=HEADERS)
        res.raise_for_status()
        return res.json(), res.status_code
    except requests.RequestException as e:
        logger.error(f"신고 처리 완료 실패 ({report_id}): {e}")
        return {"error": str(e)}, 500

def get_report_list():
    url = f"{BASE_URL}/api/v1/admin/reports/"
    try:
        res = requests.get(url, headers=HEADERS)
        res.raise_for_status()
        return res.json(), res.status_code
    except requests.RequestException as e:
        logger.error(f"신고 목록 조회 실패: {e}")
        return {"error": str(e)}, 500
