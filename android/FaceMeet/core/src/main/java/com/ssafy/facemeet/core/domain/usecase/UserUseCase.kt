package com.ssafy.facemeet.core.domain.usecase

import android.os.Build
import androidx.annotation.RequiresApi
import com.ssafy.facemeet.core.data.remote.dto.request.UserInfoModRequest
import com.ssafy.facemeet.core.data.remote.dto.response.PartnerFaceInfoResponse
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
    @RequiresApi(Build.VERSION_CODES.O)
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
    @RequiresApi(Build.VERSION_CODES.O)
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