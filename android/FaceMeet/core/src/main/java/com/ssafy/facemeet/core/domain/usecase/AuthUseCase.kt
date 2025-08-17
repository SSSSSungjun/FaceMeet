package com.ssafy.facemeet.core.domain.usecase

import android.util.Log
import com.ssafy.facemeet.core.data.remote.dto.request.OnboardingRequest
import com.ssafy.facemeet.core.domain.model.Auth
import com.ssafy.facemeet.core.domain.repository.AuthRepository
import javax.inject.Inject


private const val TAG = "AuthUseCase"

class OnboardingUseCase @Inject constructor(private val authRepository: AuthRepository) {
    suspend operator fun invoke(request: OnboardingRequest): Result<Auth> {
        Log.d(TAG, "invoke: 진입 성공")
        return authRepository.onBoarding(request)
    }
}

class LogoutUseCase @Inject constructor(private val authRepository: AuthRepository) {
    suspend operator fun invoke(): Result<Auth> {
        return authRepository.logout()
    }
}