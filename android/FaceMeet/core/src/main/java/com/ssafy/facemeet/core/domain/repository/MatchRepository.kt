package com.ssafy.facemeet.core.domain.repository

interface MatchRepository {
    suspend fun getMatchRemain(): Result<Int>
}