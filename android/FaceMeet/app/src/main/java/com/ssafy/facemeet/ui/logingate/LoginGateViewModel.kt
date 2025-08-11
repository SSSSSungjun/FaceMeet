package com.ssafy.facemeet.navigation.gate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.facemeet.core.data.remote.dto.response.UserStatusResponse
import com.ssafy.facemeet.core.domain.usecase.GetUserStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginGateViewModel @Inject constructor(
    private val getUserStatusUseCase: GetUserStatusUseCase
) : ViewModel() {

    /** 화면 이동 이벤트 */
    sealed interface Nav {
        data object ToRegister : Nav
        data object ToCamera : Nav
        data object ToMain : Nav
    }

    private val _nav = MutableSharedFlow<Nav>(extraBufferCapacity = 1)
    val nav: SharedFlow<Nav> = _nav

    /** 로딩/에러 표시용 (필요 없으면 빼도 됨) */
    data class UiState(
        val loading: Boolean = false,
        val error: String? = null,
        val status: UserStatusResponse? = null
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState

    /** 진입하자마자 호출해서 다음 화면 결정 */
    fun decideNext() = viewModelScope.launch {
        _uiState.value = _uiState.value.copy(loading = true, error = null)

        getUserStatusUseCase()
            .onSuccess { s ->
                _uiState.value = UiState(loading = false, status = s)
                when {
                    !s.hasInfo -> _nav.tryEmit(Nav.ToRegister)
                    !s.hasFace -> _nav.tryEmit(Nav.ToCamera)
                    else -> _nav.tryEmit(Nav.ToMain)
                }
            }
            .onFailure { e ->
                // 정책에 맞게 처리: 일단 등록으로 유도하거나, 에러 노출 후 재시도 버튼 제공
                _uiState.value = UiState(loading = false, error = e.message)
                _nav.tryEmit(Nav.ToRegister)
            }
    }

    /** 사용자가 재시도 눌렀을 때 호출 */
    fun retry() = decideNext()
}
