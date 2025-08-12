package com.ssafy.facemeet.core.data.remote.datasource

import com.ssafy.facemeet.core.data.remote.api.MatchTicketApi
import com.ssafy.facemeet.core.data.remote.dto.response.TakeMatchTicketResponse
import javax.inject.Inject


interface MatchTicketRemoteDataSource {
    suspend fun take(settingId: Long): TakeMatchTicketResponse
}

class MatchTicketRemoteDataSourceImpl @Inject constructor(
    private val api: MatchTicketApi
) : MatchTicketRemoteDataSource {
    override suspend fun take(settingId: Long) = api.take(settingId)
}