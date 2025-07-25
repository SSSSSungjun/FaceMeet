package com.ssafy.facemeet

import androidx.lifecycle.ViewModel
import com.ssafy.facemeet.core.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

private const val TAG = "MainViewModel"

@HiltViewModel
class MainViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    // Compose용 State 사용
    private val _isLoggedIn = MutableStateFlow<Boolean?>(false) // null = 로딩중
    val isLoggedIn: StateFlow<Boolean?> = _isLoggedIn

//    init {
//        checkInitialAuthStatus()
//    }

}
