package com.ssafy.facemeet.core.domain.usecase

import com.ssafy.facemeet.core.data.remote.dto.request.UserInfoModRequest
import com.ssafy.facemeet.core.data.remote.dto.response.HomeInfoResponse
import com.ssafy.facemeet.core.data.remote.dto.response.PartnerFaceInfoResponse
import com.ssafy.facemeet.core.data.remote.dto.response.UserStatusResponse
import com.ssafy.facemeet.core.data.remote.mapper.toDomain
import com.ssafy.facemeet.core.domain.model.UserInfo
import com.ssafy.facemeet.core.domain.repository.UserRepository
import javax.inject.Inject


class SetUserOnlineUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        return userRepository.postOnline()
    }
}

class SetUserOfflineUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        return userRepository.postOffline()
    }
}

class GetUserInfoUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(): Result<UserInfo> {
        return userRepository.getUserInfo().map { it.toDomain() }
    }
}

class DeleteUserUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        return userRepository.deleteUser()
    }
}

class UpdateUserInfoUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(request: UserInfoModRequest): Result<UserInfo> {
        return userRepository.patchUserInfo(request).map { it.toDomain() }
    }
}

class GetPartnerFaceInfoUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(partnerId: Long): Result<PartnerFaceInfoResponse> {
        return userRepository.getPartnerFaceInfo(partnerId)
    }
}

// core/domain/usecase/UserUseCase.kt (혹은 새 파일)
class GetUserStatusUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(): Result<UserStatusResponse> = userRepository.getUserStatus()
}


class GetHomeInfoUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(): Result<HomeInfoResponse> = userRepository.getHomeInfo()
}

// core/domain/usecase/GetNotificationsUseCase.kt
class GetNotificationsUseCase @Inject constructor(
    private val repo: UserRepository
) {
    suspend operator fun invoke() = repo.getNotifications()
}

// core/domain/usecase/ReadNotificationUseCase.kt
class ReadNotificationUseCase @Inject constructor(
    private val repo: UserRepository
) {
    suspend operator fun invoke(notificationId: Long) = repo.readNotification(notificationId)
}

// core/domain/usecase/GetUnreadNotificationCountUseCase.kt
class GetUnreadNotificationCountUseCase @Inject constructor(
    private val repo: UserRepository
) {
    suspend operator fun invoke() = repo.getUnreadNotificationCount()
}
