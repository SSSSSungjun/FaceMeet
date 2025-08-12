package com.ssafy.facemeet.core.data.remote.api

import com.ssafy.facemeet.core.data.remote.dto.response.TakeMatchTicketResponse
import retrofit2.http.POST
import retrofit2.http.Path

interface MatchTicketApi {
    @POST("/api/v1/match/{settingid}")
    suspend fun take(@Path("settingid") settingId: Long): TakeMatchTicketResponse
}