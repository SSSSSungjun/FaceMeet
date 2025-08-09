// GetDeviceTokensUseCase.kt
package com.ssafy.facemeet.core.domain.usecase

import com.ssafy.facemeet.core.data.remote.dto.request.fcm.FcmTokenRequest
import com.ssafy.facemeet.core.domain.model.FcmToken
import com.ssafy.facemeet.core.domain.model.TopicSubscription
import com.ssafy.facemeet.core.domain.repository.FcmRepository
import javax.inject.Inject

class GetDeviceTokensUseCase @Inject constructor(
    private val fcmRepository: FcmRepository
) {
    suspend operator fun invoke(): Result<List<FcmToken>> {
        return fcmRepository.getDeviceTokens()
    }
}

class RegisterDeviceUseCase @Inject constructor(
    private val fcmRepository: FcmRepository
) {
    suspend operator fun invoke(request: FcmTokenRequest): Result<FcmToken> {
        return fcmRepository.registerDevice(request)
    }
}

class DeleteDeviceUseCase @Inject constructor(
    private val fcmRepository: FcmRepository
) {
    suspend operator fun invoke(deviceToken: String): Result<Unit> {
        return fcmRepository.deleteDevice(deviceToken)
    }
}

class PostSubscriptionUseCase @Inject constructor(
    private val fcmRepository: FcmRepository
) {
    suspend operator fun invoke(topicId: String): Result<Unit> {
        return fcmRepository.postSubscription(topicId)
    }
}

class DeleteSubscriptionUseCase @Inject constructor(
    private val fcmRepository: FcmRepository
) {
    suspend operator fun invoke(topicId: String): Result<Unit> {
        return fcmRepository.deleteSubscription(topicId)
    }
}

class GetSubscriptionsUseCase @Inject constructor(
    private val fcmRepository: FcmRepository
) {
    suspend operator fun invoke(): Result<TopicSubscription> {
        return fcmRepository.getSubscriptions()
    }
}