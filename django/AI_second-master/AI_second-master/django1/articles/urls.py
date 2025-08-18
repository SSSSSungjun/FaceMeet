from django.urls import path
from . import views
from .views import RecommendAPIView
app_name = "articles"

urlpatterns = [
    path("", views.article_upload, name="article_upload"),
    path("api/analyze/", views.AnalyzeAPIView.as_view(), name="analyze_api"),
    path("image/<str:filename>/", views.serve_and_delete_image, name="serve_and_delete_image"),
    path('recommend/<int:user_id>/', RecommendAPIView.as_view(), name='recommend'),
]
