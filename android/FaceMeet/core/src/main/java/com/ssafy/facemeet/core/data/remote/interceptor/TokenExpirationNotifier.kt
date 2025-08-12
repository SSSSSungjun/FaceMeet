package com.ssafy.facemeet.core.data.remote.interceptor

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenExpirationNotifier @Inject constructor() {
    private val _tokenExpiredEvent = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val tokenExpiredEvent = _tokenExpiredEvent.asSharedFlow()

    suspend fun notifyTokenExpired() {
        _tokenExpiredEvent.emit(Unit)
    }
}