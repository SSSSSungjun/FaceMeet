package com.ssafy.facemeet.ui.web

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.facemeet.core.data.datastore.TokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WebLoginViewModel @Inject constructor(
    private val tokenManager: TokenManager
) : ViewModel() {

    fun saveToken(refreshToken : String, accessToken: String,isRegistration : Boolean){
        viewModelScope.launch {
            tokenManager.saveTokens(accessToken,refreshToken,isRegistration)
        }
    }
}