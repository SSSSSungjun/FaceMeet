package com.ssafy.facemeet.core.data.remote.datasource

import com.ssafy.facemeet.core.data.remote.api.MatchApiService
import com.ssafy.facemeet.core.data.remote.dto.response.MatchResponse

class MatchRemoteDataSource(
    private val apiService: MatchApiService
) {
    suspend fun getMatch(): MatchResponse {
        return apiService.getMatch()
    }

    suspend fun getMatchRemain() = apiService.getMatchRemain()
}
