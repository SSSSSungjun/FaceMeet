from django.apps import AppConfig


class MakefaceConfig(AppConfig):
    default_auto_field = 'django.db.models.BigAutoField'
    name = 'makeface'

    def ready(self):
        """
        Django 앱이 준비되었을 때 실행됩니다.
        이곳에서 이미지 합성기를 초기화하여 파일 목록을 캐시합니다.
        """
        from . import composer
        try:
            composer.init_env()
        except FileNotFoundError as e:
            print(f"!!! [경고] 이미지 합성기 초기화 실패: {e}")
            print("!!! 'makeface/assets/composition_parts/' 경로에 기본 이미지(예: 귀.png)가 있는지 확인하세요.")
