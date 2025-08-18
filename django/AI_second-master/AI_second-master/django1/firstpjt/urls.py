"""
URL configuration for firstpjt project.

The `urlpatterns` list routes URLs to views. For more information please see:
    https://docs.djangoproject.com/en/5.2/topics/http/urls/
Examples:
Function views
    1. Add an import:  from my_app import views
    2. Add a URL to urlpatterns:  path('', views.home, name='home')
Class-based views
    1. Add an import:  from other_app.views import Home
    2. Add a URL to urlpatterns:  path('', Home.as_view(), name='home')
Including another URLconf
    1. Import the include() function: from django.urls import include, path
    2. Add a URL to urlpatterns:  path('blog/', include('blog.urls'))
"""

from django.conf import settings
from django.conf.urls.static import static
from django.contrib import admin
from django.urls import path, include, re_path
from rest_framework import permissions
from drf_yasg.views import get_schema_view
from drf_yasg import openapi
from django.http import HttpResponse
from articles.views import save_test_view
from articles.views import protected_view
from makeface.composer import upload_image_to_s3
from articles.views import face

def home(request):
    return HttpResponse("This is the home page.")

# urls.py


schema_view = get_schema_view(
    openapi.Info(
        title="관상 분석 API",  # 원하시는 이름으로 변경
        default_version="v1.0",  # 원하시는 버전으로 변경
        description="얼굴 사진을 업로드하여 관상을 분석하는 API 문서입니다.",  # 원하시는 설명으로 변경
        # 필요하다면 아래 주석을 해제하고 내용을 채워넣으세요.
        # terms_of_service="https://www.google.com/policies/terms/",
        # contact=openapi.Contact(name="개발자 이름", email="dev@example.com"),
        # license=openapi.License(name="BSD License"),
    ),
    public=True,
    permission_classes=(permissions.AllowAny,),
)

urlpatterns = [
    path("admin/", admin.site.urls),
    path("adminpage/", include("adminpage.urls")),
    path("articles/", include("articles.urls")),
    path('save-test/', save_test_view, name='save_test'),
    path('protected_view/' , protected_view, name = 'protected_view'),
    path('upload_image_to_s3/', upload_image_to_s3, name='upload_image_to_s3'),
    path('api/v1/analyze/',face, name ='face' ),

    re_path(
        r"^swagger(?P<format>\.json|\.yaml)$",
        schema_view.without_ui(cache_timeout=0),
        name="schema-json",
    ),
    re_path(
        r"^swagger/$",
        schema_view.with_ui("swagger", cache_timeout=0),
        name="schema-swagger-ui",
    ),
    re_path(r"^redoc/$", schema_view.with_ui("redoc", cache_timeout=0), name="schema-redoc"),
    
]

if settings.DEBUG:
    urlpatterns += static(settings.MEDIA_URL, document_root=settings.MEDIA_ROOT)