# articles/mappings.py

# 각 DB 모델의 이름
MODELS = {
    'chin': 'Chin',
    'faceshape': 'Faceshape',
    'mouth': 'Mouth',
    'nose': 'Nose',
    'eyebrow': 'Eyebrow',
    'eye': 'Eye'
}

# 분석 결과로 나오는 키(Key)와, 해당 키가 어떤 DB 모델에 속하는지를 매핑합니다.
# 이 정보를 통해 '턱 형태'라는 결과가 나오면 'chin' 모델의 DB를 조회해야 함을 알 수 있습니다.
KEY_TO_MODEL_MAP = {
    # woo.py 결과
    "턱 형태": "chin",
    "턱 길이": "chin",
    "얼굴형": "faceshape",
    "코 형태": "nose",
    "코 길이": "nose",
    "들창/화살코": "nose",
    "코 높이": "nose",
    "눈썹 굵기": "eyebrow",
    "눈썹 길이": "eyebrow",
    "눈썹 형태": "eyebrow",
    "눈썹 간격": "eyebrow",
    # kim.py 결과
    "눈꼬리": "eye",
    "눈모양": "eye",
    "눈크기": "eye",
    "입크기": "mouth",
    "입술두께": "mouth",
    "입꼬리": "mouth",
}

# --- 분석 모듈에서 생성되는 키워드와 DB ID를 매핑하는 딕셔너리 ---
# 주의: 이 딕셔너리의 '키'는 분석 코드(woo.py, kim.py)에서 반환하는 문자열과 정확히 일치해야 합니다.

CHIN_KEYWORDS = {
    "둥글고 통통한 턱": 1,
    "뾰족한 턱": 2,
    "각진 턱": 3,
    "긴 턱": 4,
    "작은 턱": 5,
}

EYE_KEYWORDS = {
    "큰 눈": 1,
    "작은 눈": 2,
    "올라간 눈": 3,
    "내려간 눈": 4,
    "둥근 눈": 5,
    "가는 눈": 6
}

EYEBROW_KEYWORDS = {
    "굵은 눈썹": 1,
    "가는 눈썹": 2,
    "긴 눈썹": 3,
    "짧은 눈썹": 4,
    "일자 눈썹": 5,
    "올라간 눈썹": 6,
    "내려간 눈썹": 7,
    "사이가 넓은 눈썹": 8,
    "사이가 좁은 눈썹": 9
}

FACESHAPE_KEYWORDS = {
    "둥근 얼굴": 1,
    "긴 얼굴": 2,
    "일반형 얼굴": 3
}

NOSE_KEYWORDS = {
    "높은 코": 1,
    "낮은 코": 2,
    "긴 코": 3,
    "짧은 코": 4,
    "코 끝이 올라간": 5, # woo.py에서 '들창코'에 해당
    "코 끝이 내려간": 6, # woo.py에서 '화살코'에 해당
    "폭 넓은 코": 7,     # woo.py에서 '마늘코'에 해당
    "폭 좁은 코": 8      # woo.py에서 '날카로운코'에 해당
}

MOUTH_KEYWORDS = {
    "큰입": 1,
    "작은입": 2,
    "얇은 입술": 3,
    "두꺼운 입술": 4,
    "입가가 올라간 입": 5,
    "입가가 내려간 입": 6
}

# 모든 키워드 맵을 하나로 묶어서 views.py에서 사용하기 편하게 만듭니다.
KEYWORD_MAPS = {
    'chin': CHIN_KEYWORDS,
    'eye': EYE_KEYWORDS,
    'eyebrow': EYEBROW_KEYWORDS,
    'faceshape': FACESHAPE_KEYWORDS,
    'nose': NOSE_KEYWORDS,
    'mouth': MOUTH_KEYWORDS,
}