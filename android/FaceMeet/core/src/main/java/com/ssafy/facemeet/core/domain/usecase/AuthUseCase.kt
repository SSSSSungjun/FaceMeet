package com.ssafy.facemeet.core.domain.usecase

import com.ssafy.facemeet.core.data.remote.dto.request.OnboardingRequest
import com.ssafy.facemeet.core.domain.model.Auth
import com.ssafy.facemeet.core.domain.repository.AuthRepository

class OnboardingUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(request: OnboardingRequest): Result<Auth> {
        return authRepository.Onboarding(request)
    }
}

class LogoutUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(): Result<Auth> {
        return authRepository.logout()
    }
}