// client/ui/ticketEvent/MatchTicketViewModel.kt
package com.ssafy.facemeet.client.ui.ticketEvent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.facemeet.core.domain.model.TakeMatchTicket
import com.ssafy.facemeet.core.domain.usecase.TakeMatchTicketUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MatchTicketViewModel @Inject constructor(
    private val takeMatchTicket: TakeMatchTicketUseCase
) : ViewModel() {

    sealed interface UiState {
        data object Idle : UiState
        data object Loading : UiState
        data class Taken(val data: TakeMatchTicket) : UiState
        data class Error(val message: String) : UiState
    }

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState

    fun take(settingId: Long) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            takeMatchTicket(settingId)
                .onSuccess { _uiState.value = UiState.Taken(it) }
                .onFailure { _uiState.value = UiState.Error(it.message ?: "알 수 없는 오류") }
        }
    }

    fun reset() {
        _uiState.value = UiState.Idle
    }
}
