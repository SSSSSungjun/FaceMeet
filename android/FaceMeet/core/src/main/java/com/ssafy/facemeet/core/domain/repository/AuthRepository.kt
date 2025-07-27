package com.ssafy.facemeet.core.domain.repository

import com.ssafy.facemeet.core.data.remote.dto.request.OnboardingRequest
import com.ssafy.facemeet.core.data.remote.dto.request.RefreshTokenRequest
import com.ssafy.facemeet.core.domain.model.Auth

interface AuthRepository {
    suspend fun onBoarding(request: OnboardingRequest): Result<Auth>
    suspend fun refreshToken(request: RefreshTokenRequest): Result<Auth>
    suspend fun logout():Result<Auth>
}