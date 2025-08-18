import jwt
from django.conf import settings
from django.http import JsonResponse



SECRET_KEY = 'zvK1PHT0Jff6XGZMjvwv6B4o8GFlc7gVqK5ZfMjWp0ozkhjB7sJ4ya5qkRXYKIXx'
ALGORITHM = 'HS256'

def decode_jwt_token(token):
    try:
        payload = jwt.decode(token, SECRET_KEY, algorithms=[ALGORITHM])
        print(payload)
        return {"status": "success", "payload": payload} # 성공 시 payload 반환
    except jwt.ExpiredSignatureError:
        print("JWT ExpiredSignatureError: Token has expired.")
        return {"status": "expired"} # 만료 시 "expired" 상태 반환
    except jwt.InvalidTokenError:
        print("JWT InvalidTokenError: Invalid token.")
        return {"status": "invalid"} # 유효하지 않을 시 "invalid" 상태 반환
    except Exception as e:
        print(f"JWT Decoding Error: {e}")
        return {"status": "error", "message": str(e)} # 기타 오류

def jwt_required(view_func):
    def wrapper(request, *args, **kwargs):
        auth_header = request.headers.get('Authorization')
        if not auth_header or not auth_header.startswith('Bearer '):
            return JsonResponse({'error': '인증 토큰이 없습니다.'}, status=401)

        token = auth_header.split(' ')[1]
        decode_result = decode_jwt_token(token)

        if decode_result["status"] == "expired":
            return JsonResponse({'error': '토큰이 만료되었습니다. 다시 로그인해주세요.'}, status=401)
        elif decode_result["status"] == "invalid":
            return JsonResponse({'error': '유효하지 않은 토큰입니다.'}, status=401)
        elif decode_result["status"] == "error":
            return JsonResponse({'error': f'토큰 처리 중 오류: {decode_result["message"]}'}, status=500)

        # ✅ DRF Request 객체 대응: 원래 HttpRequest에 직접 붙이기
        if hasattr(request, "_request"):
            request._request.user_info = decode_result["payload"]
        else:
            request.user_info = decode_result["payload"]

        return view_func(request, *args, **kwargs)
    return wrapper