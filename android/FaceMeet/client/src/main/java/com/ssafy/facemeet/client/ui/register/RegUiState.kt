package com.ssafy.facemeet.client.ui.register


data class RegUiState(
    val nickname: String = "",
    val selectedAddress: String = "",
    val selectedAgeRange : IntRange = 20..65,
    val hasLocation: Boolean = false
) {
    fun isValid(): Boolean = nickname.isNotEmpty() && hasLocation
}
