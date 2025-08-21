package com.ssafy.facemeet.core.data.remote.repository

import android.util.Log
import com.ssafy.facemeet.core.data.remote.datasource.UserRemoteDataSource
import com.ssafy.facemeet.core.data.remote.dto.request.UserInfoModRequest
import com.ssafy.facemeet.core.data.remote.dto.response.HomeInfoResponse
import com.ssafy.facemeet.core.data.remote.dto.response.NotificationResponse
import com.ssafy.facemeet.core.data.remote.dto.response.PartnerFaceInfoResponse
import com.ssafy.facemeet.core.data.remote.dto.response.UserInfoResponse
import com.ssafy.facemeet.core.data.remote.dto.response.UserStatusResponse
import com.ssafy.facemeet.core.domain.repository.UserRepository
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val userDataRemoteDataSource: UserRemoteDataSource
) : UserRepository {

    companion object {
        private const val TAG = "UserRepositoryImpl"
    }

    override suspend fun postOnline(): Result<Unit> =
        runCatching { userDataRemoteDataSource.postOnline() }.mapCatching { response ->
            if (response.isSuccessful) Unit
            else throw Exception("postOnline failed: ${response.code()}")
        }.onFailure {
            Log.e(TAG, "postOnline error", it)
        }


    override suspend fun postOffline(): Result<Unit> =
        runCatching { userDataRemoteDataSource.postOffline() }.mapCatching { response ->
            if (response.isSuccessful) Unit
            else throw Exception("postOffline failed: ${response.code()}")
        }.onFailure {
            Log.e(TAG, "postOffline error", it)
        }


    override suspend fun getUserInfo(): Result<UserInfoResponse> =
        runCatching { userDataRemoteDataSource.getUserInfo() }.mapCatching { response ->
            Log.d(TAG, "getUserInfo: ${response.body()}")
            if (response.isSuccessful) {
                response.body() ?: throw Exception("Empty body")
            } else throw Exception("getUserInfo failed: ${response.code()}")
        }.onFailure {
            Log.e(TAG, "getUserInfo error", it)
        }


    override suspend fun deleteUser(): Result<Unit> =
        runCatching { userDataRemoteDataSource.deleteUser() }.mapCatching { response ->
            if (response.isSuccessful) Unit
            else throw Exception("deleteUser failed: ${response.code()}")
        }.onFailure {
            Log.e(TAG, "deleteUser error", it)
        }


    override suspend fun patchUserInfo(request: UserInfoModRequest): Result<UserInfoResponse> =
        runCatching { userDataRemoteDataSource.patchUserInfo(request) }.mapCatching { response ->
            if (response.isSuccessful) {
                response.body() ?: throw Exception("Empty body")
            } else throw Exception("patchUserInfo failed: ${response.code()}")
        }.onFailure {
            Log.e(TAG, "patchUserInfo error", it)
        }

    override suspend fun getPartnerFaceInfo(partnerId: Long): Result<PartnerFaceInfoResponse> =
        runCatching { userDataRemoteDataSource.getPartnerFaceInfo(partnerId) }
            .mapCatching { response ->
                if (response.isSuccessful) {
                    response.body() ?: throw Exception("Empty body")
                } else throw Exception("getPartnerFaceInfo failed: ${response.code()}")
            }
            .onFailure {
                Log.e("UserRepository", "getPartnerFaceInfo error", it)
            }

    override suspend fun getUserStatus(): Result<UserStatusResponse> = runCatching {
        val res = userDataRemoteDataSource.getUserStatus()
        Log.d(TAG, "getUserStatus: ${res.body()}")
        if (res.isSuccessful) {
            res.body() ?: error("Empty body")
        } else {
            error("HTTP ${res.code()}")
        }
    }

    override suspend fun getHomeInfo(): Result<HomeInfoResponse> = try {
        val res = userDataRemoteDataSource.getHomeInfo()
        if (res.isSuccessful) {
            res.body()?.let { Result.success(it) }
                ?: Result.failure(IllegalStateException("Empty body"))
        } else {
            Result.failure(IllegalStateException("HTTP ${res.code()}"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun getNotifications(): Result<List<NotificationResponse>> =
        runCatching { userDataRemoteDataSource.getMyNotifications() }
            .mapCatching { res ->
                if (res.isSuccessful) res.body() ?: emptyList()
                else throw Exception("getNotifications failed: ${res.code()}")
            }
            .onFailure { Log.e(TAG, "getNotifications error", it) }

    override suspend fun readNotification(notificationId: Long): Result<Unit> =
        runCatching { userDataRemoteDataSource.readNotification(notificationId) }
            .mapCatching { res ->
                if (res.isSuccessful) Unit
                else throw Exception("readNotification failed: ${res.code()}")
            }
            .onFailure { Log.e(TAG, "readNotification error", it) }

    override suspend fun getUnreadNotificationCount(): Result<Int> =
        runCatching { userDataRemoteDataSource.getUnreadNotificationCount() }
            .mapCatching { res ->
                if (res.isSuccessful) res.body() ?: 0
                else throw Exception("getUnreadNotificationCount failed: ${res.code()}")
            }
            .onFailure { Log.e(TAG, "getUnreadNotificationCount error", it) }
}
