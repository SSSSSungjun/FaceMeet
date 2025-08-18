from django.db import models

class BlacklistCategory(models.Model):
    category_id = models.AutoField(primary_key=True)  # PK, 자동증가
    name = models.CharField(max_length=100)

    class Meta:
        db_table = 'blacklist_category'
        managed = False

    def __str__(self):
        return self.name

class Blacklist(models.Model):
    blacklist_id = models.AutoField(primary_key=True)  # PK
    user_id = models.IntegerField()
    reporter_id = models.IntegerField()
    admin_id = models.IntegerField()
    created_at = models.DateTimeField()
    category = models.ForeignKey(
        BlacklistCategory, db_column='category_id', on_delete=models.DO_NOTHING
    )  # FK 연결

    class Meta:
        db_table = 'blacklist'
        managed = False

    def __str__(self):
        return f'Blacklist {self.blacklist_id}'

class ReportCategory(models.Model):
    category_id = models.AutoField(primary_key=True)
    name = models.CharField(max_length=100)

    class Meta:
        db_table = 'report_category'
        managed = False

    def __str__(self):
        return self.name

class Report(models.Model):
    report_id = models.AutoField(primary_key=True)
    room_id = models.IntegerField()
    category = models.ForeignKey(
        ReportCategory, db_column='category_id', on_delete=models.DO_NOTHING
    )
    reporter_id = models.IntegerField()
    reported_id = models.IntegerField()
    reason = models.TextField(blank=True, null=True)
    img = models.TextField(blank=True, null=True)  # 이미지를 파일이 아닌 경로나 URL로 저장한다고 추정
    created_at = models.DateTimeField()
    is_solved = models.BooleanField()

    class Meta:
        db_table = 'report'
        managed = False

    def __str__(self):
        return f'Report {self.report_id}'


class Setting(models.Model):
    setting_id = models.AutoField(primary_key=True)  # PK, 자동증가
    admin_id = models.IntegerField()  # 관리자 ID (관리자 테이블 FK일 수도 있음)
    coupon_count = models.IntegerField()  # 설정된 쿠폰 총 수량
    start_time = models.DateTimeField()  # 설정 적용 시작 시각
    end_time = models.DateTimeField()  # 설정 적용 종료 시각
    created_at = models.DateTimeField(blank=True, null=True)  # 생성 시각
    current_cnt = models.IntegerField(blank=True, null=True)  # 현재 사용(남은) 개수 등

    class Meta:
        db_table = 'setting'   # 실제 테이블명이 settings라면 이렇게, 만약 setting이면 'setting'
        managed = False

    def __str__(self):
        return f'Setting {self.setting_id}: {self.coupon_count} coupons'
