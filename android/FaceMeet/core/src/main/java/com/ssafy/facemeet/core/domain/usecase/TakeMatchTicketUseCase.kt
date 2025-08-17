package com.ssafy.facemeet.core.domain.usecase

import com.ssafy.facemeet.core.domain.model.TakeMatchTicket
import com.ssafy.facemeet.core.domain.repository.MatchTicketRepository
import javax.inject.Inject

class TakeMatchTicketUseCase @Inject constructor(
    private val repository: MatchTicketRepository
) {
    suspend operator fun invoke(settingId: Long): Result<TakeMatchTicket> =
        repository.take(settingId)
}