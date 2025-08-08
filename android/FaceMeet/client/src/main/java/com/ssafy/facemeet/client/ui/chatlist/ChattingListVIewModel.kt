package com.ssafy.facemeet.client.ui.chatlist

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.facemeet.core.domain.usecase.GetChattingListUseCase
import com.ssafy.facemeet.core.domain.usecase.PostChattingLeaveUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "ChattingListViewModel"

@HiltViewModel
class ChattingListViewModel @Inject constructor(
    private val getChattingListUseCase: GetChattingListUseCase,
    private val postChattingLeaveUseCase: PostChattingLeaveUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatListUiState())
    val uiState: StateFlow<ChatListUiState> = _uiState

    fun loadChattingList() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            getChattingListUseCase().onSuccess { chatList ->
                Log.d(TAG, "loadChattingList: 로드 성공")
                _uiState.value = _uiState.value.copy(
                    chatList = chatList,
                    isLoading = false
                )
            }.onFailure {
                Log.d(TAG, "${it.message} 방 리스트 불러오기 실패")
            }
        }
    }


}
