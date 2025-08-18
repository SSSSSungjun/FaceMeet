# adminpage/settings_service.py
import requests
import logging
from django.conf import settings
from .models import Setting

logger = logging.getLogger(__name__)

API_BEARER_TOKEN = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiI4MCIsInJvbGUiOiJST0xFX0FETUlOIiwiaWF0IjoxNzU0MjA0NTAxLCJleHAiOjE3NTQyMTA1MDF9.kjG3duh0-Dj-fMA71LFB4mfjkejg0osZ81q_wtCaEaU"


BASE_URL = "https://i13d201.p.ssafy.io"
SETTINGS_API_URL = f"{BASE_URL}/api/v1/admin/settings" # 일관성을 위해 URL 끝 슬래시 제거
headers = {
    "Authorization": f"Bearer {API_BEARER_TOKEN}",
    "Content-Type": "application/json"
}

def get_all_settings():
    all_settings = []
    url = SETTINGS_API_URL # 슬래시가 없는 기본 URL 사용

    try:
        # 'next' 페이지가 없을 때까지 모든 페이지를 순회하며 데이터를 가져옵니다.
        while url:
            res = requests.get(url, headers=headers)
            res.raise_for_status()
            data = res.json()

            # API 응답이 페이지네이션 구조(e.g., {'count': 21, 'results': [...]})인 경우
            if isinstance(data, dict) and 'results' in data:
                all_settings.extend(data['results'])
                url = data.get('next')  # 다음 페이지 URL을 가져옵니다. 없으면 None이 되어 루프가 종료됩니다.
            # 페이지네이션 구조가 아닌, 단순 리스트일 경우
            elif isinstance(data, list):
                all_settings.extend(data)
                break # 루프 종료

        return all_settings, 200
    except requests.RequestException as e:
        logger.error(f"Failed to fetch settings: {e}")
        return {"error": str(e)}, 500

def create_setting(data):
    try:
        res = requests.post(SETTINGS_API_URL, headers=headers, json=data) # 컬렉션 URL에 POST
        res.raise_for_status()
        return res.json(), res.status_code
    except requests.RequestException as e:
        logger.error(f"Failed to create setting: {e}")
        return {"error": str(e)}, 500

def get_setting(setting_id):
    url = f"{SETTINGS_API_URL}/{setting_id}" # 상세 정보 URL 생성
    try:
        res = requests.get(url, headers=headers)
        res.raise_for_status()
        return res.json(), res.status_code
    except requests.RequestException as e:
        logger.error(f"Failed to fetch setting {setting_id}: {e}")
        return {"error": str(e)}, 500

def patch_setting(setting_id, data):
    url = f"{SETTINGS_API_URL}/{setting_id}" # 상세 정보 URL 생성
    try:
        res = requests.patch(url, headers=headers, json=data)
        res.raise_for_status()
        return res.json(), res.status_code
    except requests.RequestException as e:
        error_message = str(e)
        # 외부 API가 실패 이유를 응답 본문에 담아 보냈을 수 있습니다.
        # e.response가 None이 아닌지 확인하고, 응답 텍스트를 로그에 추가합니다.
        if e.response is not None:
            error_message += f" | API Response: {e.response.text}"

        logger.error(f"Failed to patch setting {setting_id}: {error_message}")
        # 브라우저에도 더 자세한 에러를 전달할 수 있습니다.
        return {"error": "API request failed", "details": error_message}, 500
