package com.ssafy.facemeet.navigation.gate

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.common.api.ApiException
import com.ssafy.facemeet.core.data.datastore.TokenManager
import com.ssafy.facemeet.core.data.remote.dto.response.UserStatusResponse
import com.ssafy.facemeet.core.domain.usecase.GetUserStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import javax.inject.Inject

private const val TAG = "LoginGateViewModel"

@HiltViewModel
class LoginGateViewModel @Inject constructor(
    private val getUserStatusUseCase: GetUserStatusUseCase,
    private val tokenManager: TokenManager
) : ViewModel() {

    /** 화면 이동 이벤트 */
    sealed interface Nav {
        data object ToRegister : Nav
        data object ToCamera : Nav
        data object ToMain : Nav
        data object ToStart : Nav
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
        Log.d(TAG, "decideNext: ")
        _uiState.value = _uiState.value.copy(loading = true, error = null)

        getUserStatusUseCase()
            .onSuccess { s ->
                Log.d(TAG, "decideNext: ${s}")

                _uiState.value = UiState(loading = false, status = s)
                when {
                    !s.hasInfo -> _nav.tryEmit(Nav.ToRegister)
                    !s.hasFace -> _nav.tryEmit(Nav.ToCamera)
                    else -> _nav.tryEmit(Nav.ToMain)
                }
            }
            .onFailure { e ->
                Log.e(TAG, "decideNext: unknown error", )
                val isUnauthorized =
                    (e as? HttpException)?.code() == 401 || (e as? ApiException)?.statusCode == 401

                if (isUnauthorized) {
                    Log.w(TAG, "401 Unauthorized → 로그만 찍음")
                    return@onFailure
                }

                // 그 외 에러 발생 시 토큰 삭제
                tokenManager.clearTokens()
                _uiState.value = UiState(loading = false, error = e.message)
                _nav.tryEmit(Nav.ToStart)
            }

    }

    /** 사용자가 재시도 눌렀을 때 호출 */
    fun retry() = decideNext()
}
