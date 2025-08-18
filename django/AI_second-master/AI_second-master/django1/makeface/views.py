from drf_yasg.utils import swagger_auto_schema
from rest_framework import status
from rest_framework.response import Response
from rest_framework.views import APIView

from . import composer
from .serializers import FaceCompositionDbidSerializer


class FaceCompositionView(APIView):
    """
    DB ID 기반으로 관상 이미지를 합성하는 API
    """

    @swagger_auto_schema(
        operation_summary="얼굴 이미지 합성 (DB ID 기반)",
        operation_description="각 파트별 DB ID 리스트를 받아 얼굴 이미지를 합성하고, 생성된 이미지의 URL을 반환합니다.",
        request_body=FaceCompositionDbidSerializer,
        responses={
            201: '{"image_url": "/media/composed/face-1_eye-2-5.png"}',
            400: "잘못된 요청 (예: 필수 파트 누락, 이미지 파일 없음)",
            500: "서버 내부 오류",
        },
    )
    def post(self, request, *args, **kwargs):
        """
        선택된 파트 ID 리스트로 이미지를 합성합니다.
        예시: {"eye": [2, 5], "chin": [1, 3]}
        """
        serializer = FaceCompositionDbidSerializer(data=request.data)
        if not serializer.is_valid():
            return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)
        
        try:
            image_url = composer.compose_by_dbid(serializer.validated_data)
            return Response({"image_url": image_url}, status=status.HTTP_201_CREATED)
        except FileNotFoundError as e:
            return Response({"error": str(e)}, status=status.HTTP_400_BAD_REQUEST)
        except Exception as e:
            return Response(
                {"error": "이미지 합성에 실패했습니다."},
                status=status.HTTP_500_INTERNAL_SERVER_ERROR,
            )
