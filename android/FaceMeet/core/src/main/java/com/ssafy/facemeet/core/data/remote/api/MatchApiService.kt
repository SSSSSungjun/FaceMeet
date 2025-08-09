package com.ssafy.facemeet.core.data.remote.api

import com.ssafy.facemeet.core.data.remote.dto.response.MatchResponse
import retrofit2.Response
import retrofit2.http.GET

interface MatchApiService {
    @GET("/api/v1/match/")
    suspend fun getMatch(): MatchResponse

    @GET("/api/v1/match/remain")
    suspend fun getMatchRemain(): Response<Int>

}