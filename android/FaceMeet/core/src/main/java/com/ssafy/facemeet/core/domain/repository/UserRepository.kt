package com.ssafy.facemeet.core.domain.repository

import com.ssafy.facemeet.core.data.remote.dto.request.UserInfoModRequest
import com.ssafy.facemeet.core.data.remote.dto.response.PartnerFaceInfoResponse
import com.ssafy.facemeet.core.data.remote.dto.response.UserInfoResponse

interface UserRepository {
    suspend fun postOnline(): Result<Unit>
    suspend fun postOffline(): Result<Unit>
    suspend fun getUserInfo(): Result<UserInfoResponse>
    suspend fun deleteUser(): Result<Unit>
    suspend fun patchUserInfo(request: UserInfoModRequest): Result<UserInfoResponse>
    suspend fun getPartnerFaceInfo(partnerId: Long): Result<PartnerFaceInfoResponse>
}