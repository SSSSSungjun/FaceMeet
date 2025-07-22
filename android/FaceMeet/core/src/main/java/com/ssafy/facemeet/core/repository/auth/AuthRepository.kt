package com.ssafy.facemeet.core.repository.auth

import com.ssafy.facemeet.core.network.dto.response.AuthResponse

interface AuthRepository {
    suspend fun signUp(
        nickname: String?,
        address: String?,
        latitude: Float,
        longitude: Float,
        preferAgeLower: Int,
        preferAgeUpper: Int
    ): Result<AuthResponse<Unit>>

    suspend fun logout(): Result<Unit>

    suspend fun refreshToken(): Result<Unit>
}