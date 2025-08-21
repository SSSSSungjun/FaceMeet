// core/data/repository/MatchRepositoryImpl.kt
package com.ssafy.facemeet.core.data.remote.repository

import com.ssafy.facemeet.core.data.remote.datasource.MatchRemoteDataSource
import com.ssafy.facemeet.core.domain.repository.MatchRepository
import javax.inject.Inject

class MatchRepositoryImpl @Inject constructor(
    private val remote: MatchRemoteDataSource
) : MatchRepository {

    override suspend fun getMatchRemain(): Result<Int> = try {
        val res = remote.getMatchRemain()
        if (res.isSuccessful) {
            res.body()?.let { Result.success(it) }
                ?: Result.failure(IllegalStateException("Empty body"))
        } else {
            Result.failure(IllegalStateException("HTTP ${res.code()}"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}
