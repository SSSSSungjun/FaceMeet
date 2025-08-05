package com.ssafy.facemeet.core.data.remote.datasource

import com.ssafy.facemeet.core.data.remote.api.ReportApiService
import javax.inject.Inject

class ReportRemoteDataSource @Inject constructor(
    private val api: ReportApiService
) {
    suspend fun getCategories() = api.getReportCategories()
}