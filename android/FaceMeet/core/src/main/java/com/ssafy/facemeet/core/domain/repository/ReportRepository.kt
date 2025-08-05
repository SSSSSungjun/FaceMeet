package com.ssafy.facemeet.core.domain.repository

import com.ssafy.facemeet.core.data.remote.dto.response.ReportCategoryResponse

interface ReportRepository {
    suspend fun getReportCategories(): Result<List<ReportCategoryResponse>>
}