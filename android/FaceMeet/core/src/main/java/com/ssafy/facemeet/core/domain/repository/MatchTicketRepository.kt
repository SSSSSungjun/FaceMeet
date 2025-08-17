package com.ssafy.facemeet.core.domain.repository

import com.ssafy.facemeet.core.domain.model.TakeMatchTicket

interface MatchTicketRepository {
    suspend fun take(settingId: Long): Result<TakeMatchTicket>
}