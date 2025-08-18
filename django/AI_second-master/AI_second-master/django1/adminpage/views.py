import json
from datetime import datetime

from django.http import JsonResponse, HttpResponseNotAllowed
from django.views.decorators.csrf import csrf_exempt
from django.shortcuts import render
from django.contrib.auth.decorators import login_required, user_passes_test

from .models import Blacklist, Report, Setting
from .settings_service import get_all_settings, create_setting, get_setting, patch_setting
from .report_service import create_report, complete_report, get_report_list
from .blacklist_service import get_blacklist_list, create_blacklist, delete_blacklist

# 관리자 권한 체크 함수
def is_admin(user):
    return user.is_staff or user.is_superuser


# 기존 관리자 페이지 뷰 유지
# @login_required
# @user_passes_test(is_admin)
def dashboard(request):
    blacklist_count = Blacklist.objects.count()
    report_count = Report.objects.count()

    context = {
        'blacklist_count': blacklist_count,
        'report_count': report_count,
    }
    return render(request, 'adminpage/dashboard.html', context)


# @login_required
# @user_passes_test(is_admin)
def blacklist_list(request):
    # DB에서 조회하는 리스트 뷰나, 필요 시 API에서 받아서 보여주는 뷰로 교체 가능
    blacklists = Blacklist.objects.all().order_by('-created_at')
    context = {'blacklists': blacklists}
    return render(request, 'adminpage/blacklist_list.html', context)


# @login_required
# @user_passes_test(is_admin)
def report_list(request):
    reports = Report.objects.all().order_by('-created_at')
    context = {'reports': reports}
    return render(request, 'adminpage/report_list.html', context)


# @login_required
# @user_passes_test(is_admin)
def setting_list(request):
    # API를 통해 설정 목록을 가져옵니다.
    data, status_code = get_all_settings()
    
    context = {
        'settings': [],
        'error': None
    }
    if status_code == 200:
        for setting in data:
            # 'startTime'과 'endTime'이 존재하고, None이 아닐 경우에만 변환을 시도합니다.
            if setting.get('startTime'):
                try:
                    # API에서 받은 날짜 문자열을 datetime 객체로 변환합니다.
                    setting['startTime'] = datetime.fromisoformat(setting['startTime'])
                except (ValueError, TypeError):
                    # 파싱할 수 없는 형식이거나 타입이 맞지 않으면 원래 값을 유지합니다.
                    pass
            if setting.get('endTime'):
                try:
                    # API에서 받은 날짜 문자열을 datetime 객체로 변환합니다.
                    setting['endTime'] = datetime.fromisoformat(setting['endTime'])
                except (ValueError, TypeError):
                    pass
        context['settings'] = data
    else:
        context['error'] = data.get('error', '설정 정보를 불러오는 데 실패했습니다.')

    return render(request, 'adminpage/setting_list.html', context)


# ------- Settings API 연동 -------

def get_all_settings_view(request):
    if request.method == 'GET':
        data, status_code = get_all_settings()
        return JsonResponse(data, safe=False, status=status_code)
    return HttpResponseNotAllowed(['GET'])

@csrf_exempt
def create_setting_view(request):
    if request.method == 'POST':
        data = json.loads(request.body)
        res_data, status_code = create_setting(data)
        return JsonResponse(res_data, status=status_code)
    return HttpResponseNotAllowed(['POST'])

def get_setting_view(request, setting_id):
    if request.method == 'GET':
        data, status_code = get_setting(setting_id)
        return JsonResponse(data, status=status_code)
    return HttpResponseNotAllowed(['GET'])

@csrf_exempt
def patch_setting_view(request, setting_id):
    if request.method != 'PATCH':
        return HttpResponseNotAllowed(['PATCH'])

    try:
        data = json.loads(request.body)
    except json.JSONDecodeError:
        return JsonResponse({"error": "Invalid JSON."}, status=400)

    res_data, status_code = patch_setting(setting_id, data)
    return JsonResponse(res_data, status=status_code)


# ------- Report API 연동 -------

@csrf_exempt
def create_report_view(request):
    if request.method == 'POST':
        data = json.loads(request.body)
        res_data, status_code = create_report(data)
        return JsonResponse(res_data, status=status_code)
    return HttpResponseNotAllowed(['POST'])

@csrf_exempt
def complete_report_view(request, report_id):
    if request.method == 'POST':
        res_data, status_code = complete_report(report_id)
        return JsonResponse(res_data, status=status_code)
    return HttpResponseNotAllowed(['POST'])

def get_report_list_view(request):
    if request.method == 'GET':
        res_data, status_code = get_report_list()
        # 응답 결과가 리스트면 safe=False 유지
        return JsonResponse(res_data, status=status_code, safe=False)
    return HttpResponseNotAllowed(['GET'])


# ------- Blacklist API 연동 -------

def blacklist_list_view(request):
    if request.method == 'GET':
        data, status_code = get_blacklist_list()
        context = {
            'blacklists': data if status_code == 200 else [],
            'error': None if status_code == 200 else '블랙리스트를 불러오는 데 실패했습니다.',
        }
        return render(request, 'adminpage/blacklist_list.html', context)
    return HttpResponseNotAllowed(['GET'])

@csrf_exempt
def create_blacklist_view(request):
    if request.method == 'POST':
        data = json.loads(request.body)
        res_data, status_code = create_blacklist(data)
        return JsonResponse(res_data, status=status_code)
    return HttpResponseNotAllowed(['POST'])

@csrf_exempt
def delete_blacklist_view(request, blacklist_id):
    if request.method == 'DELETE':
        res_data, status_code = delete_blacklist(blacklist_id)
        return JsonResponse(res_data, status=status_code)
    return HttpResponseNotAllowed(['DELETE'])
