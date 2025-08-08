package com.ssafy.facemeet.client.ui.mainmenu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.facemeet.core.domain.usecase.GetHomeInfoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainMenuViewModel @Inject constructor(
    private val getHomeInfoUseCase: GetHomeInfoUseCase
) : ViewModel() {

    data class UiState(
        val img: String? = null,
        val nickname: String = "",
        val title: String = "",
        val remainingMatchTickets: Int = 0,
        val loading: Boolean = false,
        val error: String? = null
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState

    fun loadHome() = viewModelScope.launch {
        _uiState.value = _uiState.value.copy(loading = true, error = null)
        try {
            val result = getHomeInfoUseCase()
            result.onSuccess { home ->
                _uiState.value = _uiState.value.copy(
                    img = home.img,
                    nickname = home.nickname,
                    title = home.title,
                    remainingMatchTickets = home.remainingMatchTickets,
                    loading = false
                )
            }.onFailure {
                _uiState.value = _uiState.value.copy(loading = false, error = it.message)
            }
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(loading = false, error = e.message)
        }
    }
}
