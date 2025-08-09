package com.ssafy.facemeet.core.data.repository

import com.ssafy.facemeet.core.data.remote.datasource.FcmRemoteDataSource
import com.ssafy.facemeet.core.data.remote.dto.request.fcm.FcmTokenRequest
import com.ssafy.facemeet.core.data.remote.mapper.toDomain
import com.ssafy.facemeet.core.domain.model.FcmToken
import com.ssafy.facemeet.core.domain.model.TopicSubscription
import com.ssafy.facemeet.core.domain.repository.FcmRepository
import javax.inject.Inject

class FcmRepositoryImpl @Inject constructor(
    private val remoteDataSource: FcmRemoteDataSource
) : FcmRepository {

    override suspend fun getDeviceTokens(): Result<List<FcmToken>> = runCatching {
        val response = remoteDataSource.getDeviceTokens()
        if (response.isSuccessful) {
            response.body()?.map { it.toDomain() } ?: emptyList()
        } else {
            throw Exception("Failed to get device tokens: ${response.message()}")
        }
    }

    override suspend fun registerDevice(request: FcmTokenRequest): Result<FcmToken> = runCatching {
        val response = remoteDataSource.registerDevice(request)
        if (response.isSuccessful) {
            response.body()?.toDomain() ?: throw Exception("Response body is null")
        } else {
            throw Exception("Failed to register device: ${response.message()}")
        }
    }

    override suspend fun deleteDevice(deviceToken: String): Result<Unit> = runCatching {
        val response = remoteDataSource.deleteDevice(deviceToken)
        if (response.isSuccessful) {
            response.body()
        } else {
            throw Exception("Failed to delete device: ${response.message()}")
        }
    }

    override suspend fun postSubscription(topicId: Long): Result<Unit> = runCatching {
        val response = remoteDataSource.postSubscription(topicId)
        if (response.isSuccessful) {
            response.body()
        } else {
            throw Exception("Failed to subscribe to topic: ${response.message()}")
        }
    }

    override suspend fun deleteSubscription(topicId: Long): Result<Unit> = runCatching {
        val response = remoteDataSource.deleteSubscription(topicId)
        if (response.isSuccessful) {
            response.body()
        } else {
            throw Exception("Failed to unsubscribe from topic: ${response.message()}")
        }
    }

    override suspend fun getSubscriptions(): Result<TopicSubscription> = runCatching {
        val response = remoteDataSource.getSubscriptions()
        if (response.isSuccessful) {
            response.body()?.toDomain() ?: throw Exception("Response body is null")
        } else {
            throw Exception("Failed to get subscriptions: ${response.message()}")
        }
    }
}