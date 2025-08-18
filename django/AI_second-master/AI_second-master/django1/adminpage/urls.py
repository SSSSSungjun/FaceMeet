# adminpage/urls.py
from django.urls import path

from .views import (
    dashboard,
    blacklist_list,
    report_list,
    setting_list,

    get_all_settings_view,
    create_setting_view,
    get_setting_view,
    patch_setting_view,

    create_report_view,
    complete_report_view,
    get_report_list_view,

    blacklist_list_view,
    create_blacklist_view,
    delete_blacklist_view,
)

app_name = 'adminpage'

urlpatterns = [
    # 관리자 대시보드 및 기본 페이지
    path('', dashboard, name='dashboard'),
    path('blacklist-db/', blacklist_list, name='blacklist_list'),  # 기존 DB 기반 뷰 (필요 시)
    path('report-db/', report_list, name='report_list'),           # 기존 DB 기반 뷰 (필요 시)
    path('setting-db/', setting_list, name='setting_list'),        # 기존 DB 기반 뷰 (필요 시)

    # Settings API 연동
    path('settings/', get_all_settings_view, name='get_all_settings'),
    path('settings/create/', create_setting_view, name='create_setting'),
    path('settings/<int:setting_id>/', get_setting_view, name='api_get_setting'),
    path('settings/<int:setting_id>/patch/', patch_setting_view, name='patch_setting'),

    # Report API 연동
    path('reports/', get_report_list_view, name='get_report_list'),
    path('reports/create/', create_report_view, name='create_report'),
    path('reports/<int:report_id>/complete/', complete_report_view, name='complete_report'),

    # Blacklist API 연동
    path('blacklist/', blacklist_list_view, name='blacklist_list'),
    path('blacklist/create/', create_blacklist_view, name='create_blacklist'),
    path('blacklist/<int:blacklist_id>/delete/', delete_blacklist_view, name='delete_blacklist'),
]
