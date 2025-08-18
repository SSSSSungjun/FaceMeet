package com.ssafy.facemeet.core.data.repository

import com.ssafy.facemeet.core.data.remote.api.ReportApiService
import com.ssafy.facemeet.core.data.remote.dto.request.ReportRequest
import com.ssafy.facemeet.core.data.remote.dto.response.ReportCategoryResponse
import javax.inject.Inject

class ReportRepositoryImpl @Inject constructor(
    private val api: ReportApiService
) {
    suspend fun getReportCategories(): Result<List<ReportCategoryResponse>> {
        return runCatching { api.getReportCategories() }
    }
    
    suspend fun reportUser(request: ReportRequest): Result<Unit> {
        return runCatching {
            api.reportUser(request)
        }
    }
}
