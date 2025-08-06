package com.ssafy.facemeet.client.ui.profile.partner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.facemeet.core.data.remote.api.UserApiService
import com.ssafy.facemeet.core.data.remote.dto.response.PartnerFaceInfoResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PartnerProfileViewModel @Inject constructor(
    private val userApiService: UserApiService
) : ViewModel() {

    private val _partnerFaceInfo = MutableStateFlow<PartnerFaceInfoResponse?>(null)
    val partnerFaceInfo: StateFlow<PartnerFaceInfoResponse?> = _partnerFaceInfo.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

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
}
