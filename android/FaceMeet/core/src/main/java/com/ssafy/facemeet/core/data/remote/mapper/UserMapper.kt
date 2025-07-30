package com.ssafy.facemeet.core.data.remote.mapper

import android.os.Build
import androidx.annotation.RequiresApi
import com.ssafy.facemeet.core.data.remote.dto.response.UserInfoResponse
import com.ssafy.facemeet.core.domain.model.UserInfo
import com.ssafy.facemeet.core.util.format.ParsingTimeData.toFullDateString

@RequiresApi(Build.VERSION_CODES.O)
fun UserInfoResponse.toDomain(): UserInfo {
    return UserInfo(
        name = this.name ,
        email = this.email,
        nickname = this.nickname ?:"",
        gender = this.gender,
        address = this.address ?:"", //임시로
        birth = birth.toFullDateString(),
        preferAgeLower = this.preferAgeLower,
        preferAgeUpper = this.preferAgeUpper
    )
}