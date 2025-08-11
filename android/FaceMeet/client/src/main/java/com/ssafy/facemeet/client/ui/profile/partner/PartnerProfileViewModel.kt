package com.ssafy.facemeet.client.ui.profile.partner

import android.content.Context
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.facemeet.core.data.remote.api.UserApiService
import com.ssafy.facemeet.core.data.remote.dto.request.ReportRequest
import com.ssafy.facemeet.core.data.remote.dto.response.PartnerFaceInfoResponse
import com.ssafy.facemeet.core.data.remote.dto.response.ReportCategoryResponse
import com.ssafy.facemeet.core.data.repository.ReportRepositoryImpl
import com.ssafy.facemeet.core.data.socket.ChatWebSocketManager
import com.ssafy.facemeet.core.domain.usecase.PostBlockUserUseCase
import com.ssafy.facemeet.core.domain.usecase.PostChattingLeaveUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "PartnerProfileViewModel"

@HiltViewModel
class PartnerProfileViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val chatWebSocketManager: ChatWebSocketManager,
    private val userApiService: UserApiService,
    private val reportRepository: ReportRepositoryImpl,
    private val postBlockUserUseCase: PostBlockUserUseCase,
    private val postChattingLeaveUseCase: PostChattingLeaveUseCase
) : ViewModel() {

    private val _partnerFaceInfo = MutableStateFlow<PartnerFaceInfoResponse?>(null)
    val partnerFaceInfo: StateFlow<PartnerFaceInfoResponse?> = _partnerFaceInfo.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _categories = MutableStateFlow<List<ReportCategoryResponse>>(emptyList())
    val categories: StateFlow<List<ReportCategoryResponse>> = _categories

    private val _reportResult = MutableStateFlow<Result<Unit>?>(null)
    val reportResult: StateFlow<Result<Unit>?> = _reportResult

    private val _blockResult = MutableStateFlow(false)
    val blockResult: StateFlow<Boolean> = _blockResult

    private val _exitRoomEvent = MutableSharedFlow<Unit>()
    val exitRoomEvent: SharedFlow<Unit> = _exitRoomEvent.asSharedFlow()

    @RequiresApi(Build.VERSION_CODES.O)
    fun exitChatRoom(roomId: Long) {
        viewModelScope.launch {
            postChattingLeaveUseCase.invoke(roomId).onSuccess {
                _exitRoomEvent.emit(Unit)
                Toast.makeText(context, "채팅방 나가기 완료!!", Toast.LENGTH_SHORT).show()
            }.onFailure {
                Log.d(TAG, "exitChatRoom: 방 나가기 실패")
            }
        }
    }

    fun loadPartnerFaceInfo(partnerId: Long) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null

            try {
                val response = userApiService.getPartnerFaceInfo(partnerId)
                if (response.isSuccessful) {
                    _partnerFaceInfo.value = response.body()
                } else {
                    _error.value = "Error: ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }

    //블랙리스트 요청
    fun requestBlockUser(partnerId: Long) {
        viewModelScope.launch {
            postBlockUserUseCase.invoke(partnerId).onSuccess {
                _blockResult.value = true
                Toast.makeText(context, "차단 요청 완료!!", Toast.LENGTH_SHORT).show()
            }.onFailure {
                _blockResult.value = false
            }
        }
    }

    fun fetchCategories() {
        viewModelScope.launch {
            reportRepository.getReportCategories()
                .onSuccess { _categories.value = it }
        }
    }

    fun sendReport(
        roomId: Long,
        categoryId: Int,
        reportedId: Long,
        reason: String,
        img: String = ""
    ) {
        viewModelScope.launch {
            val request = ReportRequest(
                roomId = roomId,
                categoryId = categoryId.toLong(),
                reportedId = reportedId,
                reason = reason,
                img = img
            )
            val result = reportRepository.reportUser(request)
            _reportResult.value = result
        }
    }


}
