package com.ssafy.facemeet.core.data.remote.api

import com.ssafy.facemeet.core.data.remote.dto.request.ReportRequest
import com.ssafy.facemeet.core.data.remote.dto.response.ReportCategoryResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ReportApiService {
    @GET("/api/v1/reports/category")
    suspend fun getReportCategories(): List<ReportCategoryResponse>

    @POST("/api/v1/reports/")
    suspend fun reportUser(
        @Body request: ReportRequest
    )
}
