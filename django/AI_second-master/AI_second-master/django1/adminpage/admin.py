from django.contrib import admin
from .models import Blacklist, BlacklistCategory, Report, ReportCategory, Setting

@admin.register(BlacklistCategory)
class BlacklistCategoryAdmin(admin.ModelAdmin):
    list_display = ('category_id', 'name')
    search_fields = ('name',)

@admin.register(Blacklist)
class BlacklistAdmin(admin.ModelAdmin):
    list_display = ('blacklist_id', 'user_id', 'reporter_id', 'admin_id', 'category', 'created_at')
    list_filter = ('category', 'created_at')
    search_fields = ('user_id', 'reporter_id')

@admin.register(ReportCategory)
class ReportCategoryAdmin(admin.ModelAdmin):
    list_display = ('category_id', 'name')
    search_fields = ('name',)

@admin.register(Report)
class ReportAdmin(admin.ModelAdmin):
    list_display = ('report_id', 'room_id', 'category', 'reporter_id', 'reported_id', 'is_solved', 'created_at')
    list_filter = ('category', 'is_solved', 'created_at')
    search_fields = ('reporter_id', 'reported_id')

@admin.register(Setting)
class SettingAdmin(admin.ModelAdmin):
    list_display = ('setting_id', 'admin_id', 'coupon_count', 'start_time', 'end_time', 'current_cnt')
    search_fields = ('admin_id',)
