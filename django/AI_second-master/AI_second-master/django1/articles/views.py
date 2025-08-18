import os
import uuid
import mimetypes
from django.shortcuts import render, get_object_or_404
from django.http import JsonResponse, HttpResponse, Http404
from django.urls import reverse
from django.core.files.storage import FileSystemStorage

from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework.parsers import MultiPartParser
from rest_framework import status
import concurrent.futures
from drf_yasg.utils import swagger_auto_schema
from drf_yasg import openapi

from analysis.woo import run_chinfaceeyebrow_analysis
from analysis.kim import run_eyelip_analysis
from analysis.gpt_utils import get_gpt_response
from makeface.composer import compose_by_dbid

from .models import Chin, Faceshape, Mouth, Nose, Eyebrow, Eye
from .mappings import KEY_TO_MODEL_MAP
from articles.models import FaceAnalysis
import logging
logger = logging.getLogger(__name__)

# --- Helper Functions ---

def get_id_to_desc_map(model):
    """DB 모델에서 'id: desc' 맵 생성"""
    pk_name = model._meta.pk.name
    return {item[pk_name]: item['desc'] for item in model.objects.values(pk_name, 'desc')}

def get_keyword_to_id_map(model):
    """DB 모델에서 'keyword: id' 맵 생성"""
    pk_name = model._meta.pk.name
    return {item['keyword']: item[pk_name] for item in model.objects.values('keyword', pk_name)}

def get_descs_from_ids(result_ids, id_to_desc_maps):
    """분석 결과 (ID 딕셔너리) -> (key, desc) 리스트"""
    descs_with_keys = []
    if not result_ids:
        return descs_with_keys
    for key, feature_id in result_ids.items():
        model_name = KEY_TO_MODEL_MAP.get(key)
        if model_name and feature_id is not None:
            desc = id_to_desc_maps.get(model_name, {}).get(feature_id)
            if desc:
                descs_with_keys.append((key, desc))
    return descs_with_keys

# --- DB 데이터 캐싱 (앱 로딩 시 1회 실행) ---
MODELS = {'chin': Chin, 'faceshape': Faceshape, 'mouth': Mouth, 'nose': Nose, 'eyebrow': Eyebrow, 'eye': Eye}
try:
    KEYWORD_TO_ID_MAPS = {name: get_keyword_to_id_map(model) for name, model in MODELS.items()}
    ID_TO_DESC_MAPS = {name: get_id_to_desc_map(model) for name, model in MODELS.items()}
    ID_TO_KEYWORD_MAPS = {name: {v: k for k, v in kw_map.items()} for name, kw_map in KEYWORD_TO_ID_MAPS.items()}
except Exception as e:
    logger.critical(f"DB 맵 캐싱 실패: {e}. 앱이 정상 동작하지 않을 수 있습니다.")
    KEYWORD_TO_ID_MAPS, ID_TO_DESC_MAPS, ID_TO_KEYWORD_MAPS = {}, {}, {}

def _delete_files(paths):
    """파일 리스트를 안전하게 삭제"""
    for f in paths:
        if os.path.exists(f):
            try:
                os.remove(f)
            except Exception as e:
                logger.warning(f"파일 삭제 실패: {f} ({e})")

def generate_prompt(descs_with_keys, image_url=""):
    if not descs_with_keys:
        raise ValueError("desc list가 비어 있습니다.")

    FEATURE_GROUPS = {
        "face_shape": ["얼굴형"],
        "eyes": ["눈꼬리", "눈크기", "눈모양"],
        "eyebrows": ["눈썹 형태", "눈썹 길이", "눈썹 굵기", "눈썹 간격"],
        "nose": ["코 형태", "코 길이", "들창/화살코", "코 높이"],
        "chin": ["턱 형태", "턱 길이"],
        "lips": ["입크기", "입술두께", "입꼬리"],
    }

    features = {}
    for part, keys in FEATURE_GROUPS.items():
        labels = [desc for key, desc in descs_with_keys if key in keys]
        features[part] = {
            "label": ", ".join(labels),
            "comment": "..."
        }

    prompt = f"""
다음은 한 인물의 관상 분석 결과입니다.

1. 함께 들어온 이미지 URL 주소: {image_url}
2. 인물의 특징을 "키워드"로 요약하고 설명을 적어주세요.
3. 각각 이목구비의 형태를 label/comment로 JSON features 구조로 넣어주세요.
4. 상반·모순 특징은 조화롭게 설명.
5. title은 반드시 "~~상"으로 끝나고 10글자 이내, description은 반드시 "~~얼굴"로 끝나고 25글자 이내로 만들어주어야합니다. title은 재미있게 만들어 주세요.
6. title과 description을 제외한 것들은 2문장 정도로 나오게 해주고 문장으로 끝나게 해주세요. 너무 짧으면 안됩니다.
6. 전통적인 느낌이 나는 어투를 사용해줘. 중간에 직설적이고 자극적인 내용이 있으면 좋아.
7. 만약 이목구비의 관상 설명이 상반되는 경우가 있다면 우선 순위에 따라 설명을 하되, 자연스럽게 이어줘. 우선순위(눈 -> 입 -> 코 -> 턱 -> 눈썹)
출력(JSON):
{{
    "image_url": "{image_url}",
    "features": {{
        "face_shape": {features['face_shape']},
        "eyes": {features['eyes']},
        "eyebrows": {features['eyebrows']},
        "nose": {features['nose']},
        "chin": {features['chin']},
        "lips": {features['lips']}
    }},
    "summary_analysis": "...",
    "personality": "...",
    "interpersonal_relationships": "...",
    "career_traits": "...",
    "life_direction": "...",
    "title": "...",
    "description": "..."
}}
분석 특징:
{descs_with_keys}
"""
    return prompt

def cleanup_analysis_folder(folder_path="media/analysis", max_files=20):
    """폴더 파일 개수가 max_files 초과시 오래된 파일 삭제"""
    try:
        if not os.path.isdir(folder_path):
            return
        item_paths = [os.path.join(folder_path, f) for f in os.listdir(folder_path)]
        files = [p for p in item_paths if os.path.isfile(p)]
        if len(files) <= max_files:
            return
        files.sort(key=os.path.getctime)
        files_to_delete = files[:len(files) - max_files]
        logger.info(f"분석 폴더 정리: 총 {len(files)}개, {len(files_to_delete)}개 삭제")
        _delete_files(files_to_delete)
    except Exception as e:
        logger.error(f"분석 폴더 정리 중 오류 발생: {e}")

def serve_and_delete_image(request, filename):
    """media/analysis 이미지를 전송 후 즉시 삭제"""
    safe_filename = os.path.basename(filename)
    file_path = os.path.join("media/analysis", safe_filename)
    if not os.path.exists(file_path):
        raise Http404("이미지를 찾을 수 없거나 이미 삭제되었습니다.")
    try:
        content_type, _ = mimetypes.guess_type(file_path)
        if content_type is None:
            content_type = 'application/octet-stream'
        with open(file_path, 'rb') as f:
            response = HttpResponse(f.read(), content_type=content_type)
        return response
    finally:
        _delete_files([file_path])

def _save_uploaded_files(files):
    """업로드 파일 media/analysis에 고유 이름으로 저장하고 경로 반환"""
    fs = FileSystemStorage(location="media/analysis", base_url="/media/analysis/")
    saved_paths = {}
    for key, f in files.items():
        fname = f"{uuid.uuid4().hex}_{f.name}"
        saved_fname = fs.save(fname, f)
        saved_paths[key] = fs.path(saved_fname)
    return saved_paths

def _perform_analysis(front_path, side_path):
    """
    분석 모듈 호출. 성공시 (결과 ID dict, None), 실패시 (None, 에러)
    """
    try:
        woo_results = run_chinfaceeyebrow_analysis(front_path, side_path, KEYWORD_TO_ID_MAPS)
        kim_results = run_eyelip_analysis(front_path, KEYWORD_TO_ID_MAPS)
        if woo_results is None or kim_results is None:
            error_msg = f"얼굴 특징 분석 실패. ({os.path.basename(front_path)})"
            logger.warning(error_msg)
            return None, "얼굴 특징을 분석할 수 없습니다. 다른 사진을 사용해 주세요."
        analysis_ids = {**woo_results, **kim_results}
        return analysis_ids, None
    except Exception as e:
        logger.error(f"분석 중 예외 발생: {e}", exc_info=True)
        return None, "분석 중 서버 오류가 발생했습니다."

def _group_ids_by_part(result_ids):
    """
    분석 결과 ID dict를 부위별로 그룹화
    {'턱 형태': 1, ...} -> {'chin': [1], ...}
    """
    composition_ids = {}
    if not result_ids:
        return composition_ids
    for key, feature_id in result_ids.items():
        model_name = KEY_TO_MODEL_MAP.get(key)
        if model_name and feature_id is not None:
            composition_ids.setdefault(model_name, []).append(feature_id)
    return composition_ids
import json

def extract_analysis_from_gpt_response(gpt_response, return_as_json_string=False):
    try:
        if isinstance(gpt_response, str):
            response_json = json.loads(gpt_response)
        elif isinstance(gpt_response, dict):
            response_json = gpt_response
        else:
            print("지원하지 않는 GPT 응답 타입:", type(gpt_response))
            return None

        result = {
            "features": response_json.get("features", {}),
            "summary_analysis": response_json.get("summary_analysis", ""),
            "personality": response_json.get("personality", ""),
            "interpersonal_relationships": response_json.get("interpersonal_relationships", ""),
            "career_traits": response_json.get("career_traits", ""),
            "life_direction": response_json.get("life_direction", ""),
            "image_url": response_json.get("image_url", ""),
            "title": response_json.get("title", ""),
            "description" : response_json.get("description", "")
        }

        if return_as_json_string:
            return json.dumps(result, ensure_ascii=False, indent=2)
        return result

    except json.JSONDecodeError as e:
        print("JSON 파싱 실패:", e)
        print("문제 응답:\n", gpt_response)
        return None




##저장 가능 한지 확인 용 코드
def save_test_view(request):
    # 저장 함수
    FaceAnalysis.objects.create(
        img='test.jpg',  # 누락된 필드는 채워줘야 함
        faceshape_id=1,
        eyebrow_comb_id=1,
        eye_comb_id=1,
        nose_comb_id=1,
        mouth_comb_id=1,
        chin_comb_id=1,
        summary_analysis='123',
        personality='123',
        interpersonal_relationships='123',
        career_traits='123',
        life_direction='123',
    )
    return JsonResponse({"status": "ok", "message": "저장 완료!"})

# --- REST API: 모바일/외부용 ---

class AnalyzeAPIView(APIView):
    parser_classes = [MultiPartParser]

    @swagger_auto_schema(
        operation_summary="관상 분석 API",
        operation_description="정면(image1)과 측면(side_image1) 이미지를 받아 관상 분석 결과를 반환합니다.",
        # multipart/form-data 를 사용하는 경우, request_body 대신 manual_parameters를 사용해야 합니다.
        manual_parameters=[
            openapi.Parameter(
                'front_image',
                openapi.IN_FORM,
                description="정면 얼굴 이미지",
                type=openapi.TYPE_FILE,
                required=True
            ),
            openapi.Parameter(
                'side_image',
                openapi.IN_FORM,
                description="측면 얼굴 이미지",
                type=openapi.TYPE_FILE,
                required=True
            ),
        ],
        responses={
            200: openapi.Response(description="분석 성공"),
            400: "Bad Request - 파일이 누락된 경우",
            500: "Internal Server Error - 분석 중 오류 발생"
        }
    )





    def post(self, request, *args, **kwargs):
        image1 = request.FILES.get("image1")
        side_image1 = request.FILES.get("side_image1")
        if not all([image1, side_image1]):
            return Response({"error": "image1과 side_image1 파일이 모두 필요합니다."}, status=status.HTTP_400_BAD_REQUEST)
        saved_paths = _save_uploaded_files({"front": image1, "side": side_image1})
        try:
            # 1. 분석 수행
            result_ids, error = _perform_analysis(saved_paths["front"], saved_paths["side"])
            if error:
                return Response({"error": error}, status=status.HTTP_400_BAD_REQUEST)

            # 2. 부위별 desc 작성
            descs_with_keys = get_descs_from_ids(result_ids, ID_TO_DESC_MAPS)
            formatted_descs = "\n".join([f"- {key}: {desc}" for key, desc in descs_with_keys])
            composition_ids = _group_ids_by_part(result_ids)
            
            # 3. 합성 이미지/텍스트 병렬
            image_url = None
            summary = None

            print('분석 결과 result ids : ' , result_ids)
            with concurrent.futures.ThreadPoolExecutor(max_workers=2) as executor:
                future_compose = executor.submit(compose_by_dbid, composition_ids)
                future_gpt = executor.submit(get_gpt_response, generate_prompt(descs_with_keys, image_url="(생성중)"))  # URL 입력은 이후 대체도 가능
                image_url = future_compose.result()
                # 프롬프트에 진짜 image_url이 필요하다면 여기에 추가 기능 필요
                summary = future_gpt.result()
                print("gpt확인용 프린트:" , summary)

                analysis_data = extract_analysis_from_gpt_response(summary)



                ##face shape feature
                face_shape_feature = analysis_data['features']
                ##fase shpae comb들의 각 pk들 -> 지금은 label 가져오는데 추후 pk 값 가져오게 해야함 
                face_shape_comb_pk = face_shape_feature['face_shape']['label']
                eye_comb_pk = face_shape_feature['eyes']['label']
                eyebrow_comb_pk = face_shape_feature ['eyebrows']['label']
                nose_comb_pk = face_shape_feature['nose']['label']
                chin_comb_pk = face_shape_feature['chin']['label']
                lips_comb_pk = face_shape_feature['lips']['label']

                #gpt 종합 특징들
                personality = analysis_data['personality']
                interpersonal_relationships = analysis_data['interpersonal_relationships']
                career_traits = analysis_data['career_traits']
                life_direction = analysis_data['life_direction']
                summary_analysis = analysis_data['summary_analysis']

                FaceAnalysis.objects.create(
         
                faceshape_id=face_shape_comb_pk,
                eyebrow_comb_id=eyebrow_comb_pk,
                eye_comb_id=eye_comb_pk,
                nose_comb_id=nose_comb_pk,
                mouth_comb_id=lips_comb_pk,
                chin_comb_id=chin_comb_pk,
                summary_analysis=summary_analysis,
                personality=personality,
                interpersonal_relationships= interpersonal_relationships,
                career_traits=career_traits,
                life_direction=life_direction
                )


                print('summery 로그 찍음' , summary)

            return Response({
                "result": summary,  # (GPT의 상세 JSON, features 포함)
                "image_url": image_url,
                "status": "success"
            }, status=status.HTTP_200_OK)
        except Exception as e:
            logger.error(f"API 분석 뷰 오류: {e}", exc_info=True)
            return Response({"error": "서버 내부 오류가 발생했습니다."}, status=status.HTTP_500_INTERNAL_SERVER_ERROR)
        finally:
            _delete_files(list(saved_paths.values()))


# --- 웹 업로드/분석 ---

##토큰 받는거 테스트
from django.http import JsonResponse
from util.jwt_auth import jwt_required

@jwt_required
def protected_view(request):
    user_info = request.user_info
    return JsonResponse({
        'message': '성공적으로 인증된 요청입니다.',
        'user': user_info  # 예: {'sub': '5', 'role': 'ROLE_admin', ...}
    })

from .models import EyeComb
from .models import NoseComb
from .models import MouthComb
from .models import NoseComb
from .models import EyebrowComb
from .models import ChinComb
from .models import User
from django.views.decorators.csrf import csrf_exempt
from rest_framework.decorators import api_view
import requests
import logging

@csrf_exempt
@jwt_required
@api_view(['POST'])
def face(request):
    try:
        print('현재 face함수를 실행중입니다')
        # ✅ JWT 토큰 추출
        auth_header = request.headers.get('Authorization')
        if not auth_header or not auth_header.startswith('Bearer '):
            return JsonResponse({"error": "Authorization header not found"}, status=401)
        
        jwt_token = auth_header.split(' ')[1]  # "Bearer <token>"에서 토큰 부분만 추출
        print(f'추출된 JWT 토큰: {jwt_token[:50]}...')  # 보안상 일부만 출력
        
        # ✅ user_info 추출
        user_info = None
        if hasattr(request, '_request'):
            user_info = getattr(request._request, 'user_info', None)
        else:
            user_info = getattr(request, 'user_info', None)
        
        if user_info is None:
            logger.error("JWT user_info not found on request. Is jwt_required decorator working?")
            return JsonResponse({"error": "Authentication required or invalid token."}, status=401)
        
        print('request에서 추출한 user_info는 :', user_info)
        user_pk = user_info.get('sub')
        print('user_pk :', user_pk)
        
        if not user_pk:
            logger.error("user_pk (sub) not found in JWT payload")
            return JsonResponse({"error": "Invalid token payload"}, status=401)

    except Exception as e:
        logger.error(f"JWT 토큰/user_info 추출 중 예외 발생: {e}", exc_info=True)
        return JsonResponse({"error": "Authentication error"}, status=401)

    cleanup_analysis_folder(max_files=100)

    if request.method != "POST":
        return JsonResponse({"error": "Only POST requests are allowed."}, status=405)

    try:
        # 1. 파일 유효성 검사 및 저장
        front_image = request.FILES.get("front_image")
        side_image = request.FILES.get("side_image")

        if not all([front_image, side_image]):
            return JsonResponse({"error": "정면/측면 이미지 각 1장씩 총 2장을 올려주세요"}, status=400)

        files_to_save = {
            "image1": front_image, 
            "side_image1": side_image,
        }
        saved_paths = _save_uploaded_files(files_to_save)

        # 2. 분석 수행
        result1_ids, error1 = _perform_analysis(saved_paths["image1"], saved_paths["side_image1"])
        print("result_ids : ", result1_ids)

        errors = []
        if error1: 
            errors.append(f"첫 번째 인물 분석 실패: {error1}")
        if errors:
            return JsonResponse({"error": "\n".join(errors)}, status=500)

        # 3. 분석 결과를 클라이언트 반환용으로 변환
        processed_result = {
            key: ID_TO_KEYWORD_MAPS.get(KEY_TO_MODEL_MAP.get(key), {}).get(fid, "N/A")
            for key, fid in result1_ids.items() if fid is not None
        }

        # 4. GPT-4 및 합성이미지 병렬 처리
        descs1 = get_descs_from_ids(result1_ids, ID_TO_DESC_MAPS)
        descs_with_keys = get_descs_from_ids(result1_ids, ID_TO_DESC_MAPS)
        formatted_descs1 = "\n".join([f"- {key}: {desc}" for key, desc in descs1])
        composition_ids1 = _group_ids_by_part(result1_ids)

        with concurrent.futures.ThreadPoolExecutor(max_workers=4) as executor:
            # DB 조회 (병렬 처리)
            future_eye_comb = executor.submit(
                EyeComb.objects.get,
                eye_parts_id1=result1_ids.get('눈크기'),
                eye_parts_id2=result1_ids.get('눈꼬리'),
                eye_parts_id3=result1_ids.get('눈모양')
            )
            future_nose_comb = executor.submit(
                NoseComb.objects.get,
                nose_parts_id1=result1_ids.get('코 높이'),
                nose_parts_id2=result1_ids.get('코 길이'),
                nose_parts_id3=result1_ids.get('들창/화살코'),
                nose_parts_id4=result1_ids.get('코 형태')
            )
            future_eyebrow_comb = executor.submit(
                EyebrowComb.objects.get,
                eyebrow_parts_id1=result1_ids.get('눈썹 굵기'),
                eyebrow_parts_id2=result1_ids.get('눈썹 길이'),
                eyebrow_parts_id3=result1_ids.get('눈썹 형태'),
                eyebrow_parts_id4=result1_ids.get('눈썹 간격')
            )
            future_mouth_comb = executor.submit(
                MouthComb.objects.get,
                mouth_parts_id1=result1_ids.get('입크기'),
                mouth_parts_id2=result1_ids.get('입술두께'),
                mouth_parts_id3=result1_ids.get('입꼬리')
            )
            future_chin_comb = executor.submit(
                ChinComb.objects.get,
                chin_parts_id1=result1_ids.get('턱 형태'),
                chin_parts_id2=result1_ids.get('턱 길이')
            )

           

            # 결과 가져오기
            try:
                found_eye_comb = future_eye_comb.result()
                found_nose_comb = future_nose_comb.result()
                found_eyebrow_comb = future_eyebrow_comb.result()
                found_mouth_comb = future_mouth_comb.result()
                found_chin_comb = future_chin_comb.result()
            except Exception as e:
                logger.error(f"DB 조합 검색 실패: {e}", exc_info=True)
                return JsonResponse({"error": f"얼굴 특징 조합 검색 중 오류 발생: {e}"}, status=500)

            face_shape_comb_pk = result1_ids.get('얼굴형')
            eye_comb_pk = found_eye_comb.eye_comb_id
            eyebrow_comb_pk = found_eyebrow_comb.eyebrow_comb_id
            nose_comb_pk = found_nose_comb.nose_comb_id
            chin_comb_pk = found_chin_comb.chin_comb_id
            lips_comb_pk = found_mouth_comb.mouth_comb_id

            try:
                # DB에서 기존 FaceAnalysis 조합 찾기
                face_instance = FaceAnalysis.objects.filter(
                    faceshape_id=face_shape_comb_pk,
                    eye_comb_id=eye_comb_pk,
                    eyebrow_comb_id=eyebrow_comb_pk,
                    nose_comb_id=nose_comb_pk,
                    chin_comb_id=chin_comb_pk,
                    mouth_comb_id=lips_comb_pk
                ).first()

                if face_instance:
                    logger.info(f"해당 조합(face_id: {face_instance.face_id})이 존재합니다.")
                    # 해당 유저의 face_id를 업데이트
                    user = User.objects.get(user_id=user_pk)
                    user.face_id = face_instance.face_id
                    user.save(update_fields=["face_id"])
                    logger.info(f"User {user_pk}의 face_id가 {face_instance.face_id}로 업데이트되었습니다.")
                    
                    processed_result['personality'] = face_instance.personality
                    processed_result['interpersonal_relationships'] = face_instance.interpersonal_relationships
                    processed_result['career_traits'] = face_instance.career_traits
                    processed_result['life_direction'] = face_instance.life_direction
                    processed_result['summary_analysis'] = face_instance.summary_analysis
                    processed_result['img_url'] = face_instance.img
                    processed_result['title'] = face_instance.title
                    processed_result['description'] = face_instance.description
                    
                else:
                    logger.info("해당 조합이 없습니다. GPT로부터 데이터를 가져와 FaceAnalysis를 추가합니다.")
                 
                    future_compose1 = executor.submit(compose_by_dbid, composition_ids1)
                    print('s3 이미지 url :', future_compose1.result())

                    future_gpt = executor.submit(get_gpt_response, generate_prompt(descs_with_keys))
                    gpt_response_str = future_gpt.result()
                    print('gpt_response_Str :', gpt_response_str)
                    analysis_data = extract_analysis_from_gpt_response(gpt_response_str)
                    print('analysis_data :', analysis_data)
                    
                    personality = analysis_data['personality']
                    interpersonal_relationships = analysis_data['interpersonal_relationships']
                    career_traits = analysis_data['career_traits']
                    life_direction = analysis_data['life_direction']
                    summary_analysis = analysis_data['summary_analysis']
                    title = analysis_data['title']
                    description = analysis_data['description']

                    # 새로운 FaceAnalysis 인스턴스 생성
                    FaceAnalysis.objects.create(
                        img=future_compose1.result(),
                        faceshape_id=face_shape_comb_pk,
                        eyebrow_comb_id=eyebrow_comb_pk,
                        eye_comb_id=eye_comb_pk,
                        nose_comb_id=nose_comb_pk,
                        mouth_comb_id=lips_comb_pk,
                        chin_comb_id=chin_comb_pk,
                        summary_analysis=summary_analysis,
                        personality=personality,
                        interpersonal_relationships=interpersonal_relationships,
                        career_traits=career_traits,
                        life_direction=life_direction,
                        title=title,
                        description=description
                    )
                    logger.info("새로운 FaceAnalysis 조합이 DB에 추가되었습니다.")
                    
                    face_instance = FaceAnalysis.objects.filter(
                        faceshape_id=face_shape_comb_pk,
                        eye_comb_id=eye_comb_pk,
                        eyebrow_comb_id=eyebrow_comb_pk,
                        nose_comb_id=nose_comb_pk,
                        chin_comb_id=chin_comb_pk,
                        mouth_comb_id=lips_comb_pk
                    ).first()
                    
                    processed_result['personality'] = face_instance.personality
                    processed_result['interpersonal_relationships'] = face_instance.interpersonal_relationships
                    processed_result['career_traits'] = face_instance.career_traits
                    processed_result['life_direction'] = face_instance.life_direction
                    processed_result['summary_analysis'] = face_instance.summary_analysis
                    processed_result['img_url'] = face_instance.img
                    processed_result['title'] = face_instance.title
                    processed_result['description'] = face_instance.description
                    
                    user = User.objects.get(user_id=user_pk)
                    user.face_id = face_instance.face_id
                    user.save(update_fields=["face_id"])
                    logger.info(f"User {user_pk}의 face_id가 {face_instance.face_id}로 업데이트되었습니다.")

                # ✅ Spring API로 요청 보내기 (DB 저장 후)
                try:
                    spring_api_url = "https://i13d201.p.ssafy.io/api/v1/users/me/face"

                    headers = {
                        'Authorization': f'Bearer {jwt_token}',
                        'Content-Type': 'application/json'
                    }

                    logger.info(f"Spring API 요청 시작: {spring_api_url}")

                    # 빈 바디를 보내는 POST 요청
                    spring_response = requests.post(spring_api_url, headers=headers, json={})

                    if spring_response.status_code == 200:
                        logger.info("Spring API 요청 성공")

                        content_type = spring_response.headers.get("Content-Type", "")
                        if spring_response.content and content_type.startswith("application/json"):
                            json_data = spring_response.json()
                            print(f"Spring API 응답: {json_data}")
                        else:
                            print(f"JSON 응답이 아니거나 빈 바디입니다. 응답 내용: {spring_response.text}")

                    else:
                        logger.warning(f"Spring API 요청 실패 - Status: {spring_response.status_code}, Response: {spring_response.text}")

                except requests.exceptions.RequestException as e:
                    logger.error(f"Spring API 요청 중 네트워크 오류: {e}")

                except Exception as e:
                    logger.error(f"Spring API 요청 중 예상치 못한 오류: {e}")

            except Exception as e:
                logger.error(f"FaceAnalysis DB 처리 중 예외 발생: {e}", exc_info=True)
                return JsonResponse({"error": f"DB 처리 중 오류 발생: {e}"}, status=500)

        # 최종 성공 응답 반환 (JSON)
        response_data = processed_result
        return JsonResponse(response_data, status=200)

    except Exception as e:
        logger.error(f"face 함수 처리 중 예상치 못한 예외 발생: {e}", exc_info=True)
        return JsonResponse({"error": f"요청 처리 중 서버 오류가 발생했습니다: {str(e)}"}, status=500)


def article_upload(request):

    

    """웹 폼 기반: 4장 이미지 업로드 및 상세 분석/결과 페이지 렌더링"""
    cleanup_analysis_folder(max_files=100)
    context = {
        "error": None,
        "preview": {},
        "result1": {},
        "result2": {},
        "gpt_result1": None,
        "gpt_result2": None,
        "composed_image1_url": None,
        "composed_image2_url": None,
    }
    if request.method != "POST":
        return render(request, "articles/article.html", context)
    try:
        # 1. 파일 유효성 검사 및 저장
        image1 = request.FILES.get("image1")
        side_image1 = request.FILES.get("side_image1")
        image2 = request.FILES.get("image2")
        side_image2 = request.FILES.get("side_image2")
        if not all([image1, side_image1, image2, side_image2]):
            context["error"] = "정면/측면 이미지 각 2장씩, 총 4장을 모두 업로드해주세요."
            return render(request, "articles/article.html", context)
        files_to_save = {
            "image1": image1, "side_image1": side_image1,
            "image2": image2, "side_image2": side_image2,
        }
        saved_paths = _save_uploaded_files(files_to_save)
        # 2. 결과 페이지에서 이미지 미리보기용 url
        for key, path in saved_paths.items():
            context["preview"][key] = reverse('articles:serve_and_delete_image', args=[os.path.basename(path)])
        # 3. 인물 1, 2 분석
        result1_ids, error1 = _perform_analysis(saved_paths["image1"], saved_paths["side_image1"])
        result2_ids, error2 = _perform_analysis(saved_paths["image2"], saved_paths["side_image2"])
        errors = []
        if error1: errors.append(f"첫 번째 인물 분석 실패: {error1}")
        if error2: errors.append(f"두 번째 인물 분석 실패: {error2}")
        print("결과::::",result1_ids)
        if errors:
            context["error"] = "\n".join(errors)
            return render(request, "articles/article.html", context)
        # 4. 분석 결과를 화면 표시용으로 변환
        for key, fid in result1_ids.items():
            model_name = KEY_TO_MODEL_MAP.get(key)
            if model_name and fid is not None:
                context["result1"][key] = ID_TO_KEYWORD_MAPS.get(model_name, {}).get(fid, "N/A")
        for key, fid in result2_ids.items():
            model_name = KEY_TO_MODEL_MAP.get(key)
            if model_name and fid is not None:
                context["result2"][key] = ID_TO_KEYWORD_MAPS.get(model_name, {}).get(fid, "N/A")
        # 5. GPT-4 및 합성이미지 병렬
        descs1 = get_descs_from_ids(result1_ids, ID_TO_DESC_MAPS)
        formatted_descs1 = "\n".join([f"- {key}: {desc}" for key, desc in descs1])
        composition_ids1 = _group_ids_by_part(result1_ids)
        descs2 = get_descs_from_ids(result2_ids, ID_TO_DESC_MAPS)
        formatted_descs2 = "\n".join([f"- {key}: {desc}" for key, desc in descs2])
        composition_ids2 = _group_ids_by_part(result2_ids)
        with concurrent.futures.ThreadPoolExecutor(max_workers=4) as executor:
            future_gpt1 = executor.submit(get_gpt_response, generate_prompt(context["result1"], image_url="(생성중)"))

            future_compose1 = executor.submit(compose_by_dbid, composition_ids1)
            future_gpt2 = executor.submit(get_gpt_response, generate_prompt(context["result2"], image_url="(생성중)"))
            print('result1_ id 출력 : (웹에서)' , result1_ids)
            
            face_parts_id = result1_ids.get('얼굴형')

            eye_parts_id1_value = result1_ids.get('눈크기') # 4
            eye_parts_id2_value = result1_ids.get('눈꼬리') # 5
            eye_parts_id3_value = result1_ids.get('눈모양') # 1
            print('눈 -----------------')
            print(eye_parts_id1_value)
            print(eye_parts_id2_value)
            print(eye_parts_id3_value)
            print('눈 끝 -----------------')
            eye_combination = EyeComb.objects.get(
            eye_parts_id1=eye_parts_id1_value,
            eye_parts_id2=eye_parts_id2_value,
            eye_parts_id3=eye_parts_id3_value
            )
            # eye_comb_id를 성공적으로 찾았을 경우
            found_eye_comb_id = eye_combination.eye_comb_id
            print(f"해당하는 eye_comb_id: {found_eye_comb_id}")

            nose_parts_id1_value = result1_ids.get('코 높이')    # 8
            nose_parts_id2_value = result1_ids.get('코 길이')    # 4
            nose_parts_id3_value = result1_ids.get('들창/화살코') # 6
            nose_parts_id4_value = result1_ids.get('코 형태')    # 1

            nose_combination = NoseComb.objects.get(
            nose_parts_id1=nose_parts_id1_value,
            nose_parts_id2=nose_parts_id2_value,
            nose_parts_id3=nose_parts_id3_value,
            nose_parts_id4=nose_parts_id4_value
            )
            # nose_comb_id를 성공적으로 찾았을 경우
            found_nose_comb_id = nose_combination.nose_comb_id

            print(f"해당하는 nose_comb_id: {found_nose_comb_id}")


            eyebrow_parts_id1_value = result1_ids.get('눈썹 굵기')
            eyebrow_parts_id2_value = result1_ids.get('눈썹 길이')
            eyebrow_parts_id3_value = result1_ids.get('눈썹 형태')
            eyebrow_parts_id4_value = result1_ids.get('눈썹 간격')

            eyebrow_combination = EyebrowComb.objects.get(
            eyebrow_parts_id1=eyebrow_parts_id1_value,
            eyebrow_parts_id2=eyebrow_parts_id2_value,
            eyebrow_parts_id3=eyebrow_parts_id3_value,
            eyebrow_parts_id4=eyebrow_parts_id4_value
             )
            found_eyebrow_comb_id = eyebrow_combination.eyebrow_comb_id
            print(f"해당하는 eyebrow_comb_id: {found_eyebrow_comb_id}")


            mouth_parts_id1_value = result1_ids.get('입크기')
            mouth_parts_id2_value = result1_ids.get('입술두께')
            mouth_parts_id3_value = result1_ids.get('입꼬리')

            mouth_combination = MouthComb.objects.get(
            mouth_parts_id1=mouth_parts_id1_value,
            mouth_parts_id2=mouth_parts_id2_value,
            mouth_parts_id3=mouth_parts_id3_value
            )
            found_mouth_comb_id = mouth_combination.mouth_comb_id

            print(f"해당하는 mouth_comb_id: {found_mouth_comb_id}")

            chin_parts_id1_value = result1_ids.get('턱 형태') # 4
            chin_parts_id2_value = result1_ids.get('턱 길이')  # None

    # When querying with a field that can be None/NULL, you pass None directly.
    # Django ORM handles mapping Python's None to SQL's NULL.
            chin_combination = ChinComb.objects.get(
            chin_parts_id1=chin_parts_id1_value,
            chin_parts_id2=chin_parts_id2_value # This will be None
            )
            found_chin_comb_id = chin_combination.chin_comb_id
            print(f"해당하는 chin_comb_id: {found_chin_comb_id}")

            future_gpt1 = executor.submit(get_gpt_response, generate_prompt(result1_ids))

            future_compose1 = executor.submit(compose_by_dbid, composition_ids1)
            print("gpt 프롬프트 확인 용:", future_compose1.result())
            print("future_gpt1:",future_gpt1.result())  #json 형식

            analysis_data = extract_analysis_from_gpt_response(future_gpt1.result())
            print("gpt요약본 :", analysis_data) #python dict


    
            ##fase shpae comb들의 각 pk들 -> 지금은 label 가져오는데 추후 pk 값 가져오게 해야함 
            face_shape_comb_pk = face_parts_id
            eye_comb_pk = found_eye_comb_id
            eyebrow_comb_pk =   found_eyebrow_comb_id
            nose_comb_pk =  found_nose_comb_id
            chin_comb_pk = found_chin_comb_id 
            lips_comb_pk =   found_mouth_comb_id


            face_instance = FaceAnalysis.objects.filter(
            eye_comb_id=found_eye_comb_id,
            eyebrow_comb_id=found_eyebrow_comb_id,
            nose_comb_id=found_nose_comb_id,
            chin_comb_id=found_chin_comb_id,
            mouth_comb_id=found_mouth_comb_id
        ).first()

            
            
    except Exception as e:
        logger.error(f"article_upload 처리 예외: {e}", exc_info=True)
        context["error"] = "요청 처리 중 서버 오류 발생"
    return render(request, "articles/article.html", context)
 
# articles/views.py
from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework import status
from articles.matching_engine import find_best_match

class RecommendAPIView(APIView):
    def get(self, request, user_id):
        print("💡 RecommendAPIView 호출됨: user_id =", user_id)
        match_id, similarity = find_best_match(user_id)
        print("✅ 결과: ", match_id, similarity)
        if match_id:
            return Response({"match_user_id": match_id, "similarity": similarity}, status=status.HTTP_200_OK)
        else:
            return Response({"message": "No match found"}, status=status.HTTP_404_NOT_FOUND)
