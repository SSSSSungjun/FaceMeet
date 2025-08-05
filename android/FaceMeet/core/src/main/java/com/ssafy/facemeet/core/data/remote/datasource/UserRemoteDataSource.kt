package com.ssafy.facemeet.core.data.remote.datasource

import com.ssafy.facemeet.core.data.remote.api.UserApiService
import com.ssafy.facemeet.core.data.remote.dto.request.UserInfoModRequest
import com.ssafy.facemeet.core.data.remote.dto.response.PartnerFaceInfoResponse
import com.ssafy.facemeet.core.data.remote.dto.response.UserInfoResponse
import retrofit2.Response
import javax.inject.Inject

class UserRemoteDataSource @Inject constructor(
    private val userApiService: UserApiService
) {
    suspend fun postOnline(): Response<Unit> {
        return userApiService.postOnline()
    }

    suspend fun postOffline(): Response<Unit> {
        return userApiService.postOffline()
    }

    suspend fun getUserInfo(): Response<UserInfoResponse> {
        return userApiService.getUserInfo()
    }

    suspend fun deleteUser(): Response<Unit> {
        return userApiService.deleteUser()
    }

    suspend fun patchUserInfo(request: UserInfoModRequest): Response<UserInfoResponse> {
        return userApiService.patchUserInfo(request)
    }

    suspend fun getPartnerFaceInfo(partnerId: Long): Response<PartnerFaceInfoResponse> {
        return userApiService.getPartnerFaceInfo(partnerId)
    }

    // 필요 시 주석 해제
//    suspend fun getUserStatus(): Response<AuthResponse<Unit>> {
//        return userApiService.getUserStatus()
//    }
}
