package com.ssafy.facemeet.core.data.remote.mapper

import android.os.Build
import androidx.annotation.RequiresApi
import com.ssafy.facemeet.core.data.remote.dto.response.UserInfoResponse
import com.ssafy.facemeet.core.domain.model.UserInfo
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
fun UserInfoResponse.toDomain(): UserInfo {
    val formatter = DateTimeFormatter.ISO_DATE_TIME
    return UserInfo(
        name = this.name,
        email = this.email,
        nickname = this.nickname,
        gender = this.gender,
        address = this.address,
        birth = this.birth.format(formatter), // LocalDateTime → String
        preferAgeLower = this.preferAgeLower,
        preferAgeUpper = this.preferAgeUpper
    )
}