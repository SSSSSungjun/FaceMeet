# articles/apps.py
from django.apps import AppConfig
import os
from pytz import timezone as tz

class ArticlesConfig(AppConfig):
    default_auto_field = "django.db.models.BigAutoField"
    name = "articles"

    def ready(self):
        # 개발 서버 자동 리로드 중복 방지
        if os.environ.get("RUN_MAIN") != "true":
            return
        
        try:
            from django_apscheduler.models import DjangoJob, DjangoJobExecution
            keep_ids = {"sentiment-midnight", "update-train-midnight", "append-dataset-midnight"}
            DjangoJobExecution.objects.all().delete()
            DjangoJob.objects.exclude(id__in=keep_ids).delete()
        except Exception as e:
            import traceback
            print("[APS] 초기화 실패:", e)
            traceback.print_exc()

        from django.conf import settings
        from apscheduler.schedulers.background import BackgroundScheduler
        from django_apscheduler.jobstores import DjangoJobStore, register_events
        
        scheduler = BackgroundScheduler(timezone=tz(getattr(settings, "TIME_ZONE", "Asia/Seoul")))
        scheduler.add_jobstore(DjangoJobStore(), "default")

                # 00:00 매일 실행 (KST)
        scheduler.add_job(
            "articles.jobs:run_sentiment_job",
            trigger="cron",
            hour=0, minute=0, second=0,
            id="sentiment-midnight",
            replace_existing=True,
            max_instances=1,
            coalesce=True,
            misfire_grace_time=600,
        )

        scheduler.add_job(
            "articles.jobs:run_update_and_train_job",
            trigger="cron",
            hour=0, minute=0, second=0,
            id="update-train-midnight",
            replace_existing=True,
            max_instances=1,
            coalesce=True,
            misfire_grace_time=1800,
        )

        scheduler.add_job(
            "articles.jobs:run_append_dataset_job",
            trigger="cron",
            hour=0, minute=0, second=0,
            id="append-dataset-midnight",
            replace_existing=True,
            max_instances=1,
            coalesce=True,
            misfire_grace_time=600,
        )


        register_events(scheduler)
        scheduler.start()
