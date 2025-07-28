package com.ssafy.facemeet.client.ui.register


data class RegUiState(
    val nickname: String = "",
    val selectedAddress: String = "주소를 선택하세요",
    val hasLocation: Boolean = false
) {
    fun isValid(): Boolean = nickname.isNotEmpty() && hasLocation
}
