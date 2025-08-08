package com.ssafy.facemeet.core.domain.usecase

import com.ssafy.facemeet.core.domain.repository.MatchRepository
import javax.inject.Inject

class GetMatchRemainUseCase @Inject constructor(
    private val repo: MatchRepository
) {
    suspend operator fun invoke(): Result<Int> = repo.getMatchRemain()
}