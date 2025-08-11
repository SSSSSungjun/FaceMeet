package com.ssafy.facemeet.core.data.remote.mapper

import com.ssafy.facemeet.core.data.remote.dto.response.UserInfoResponse
import com.ssafy.facemeet.core.domain.model.UserInfo
import com.ssafy.facemeet.core.util.format.ParsingTimeData.toFullDateString

fun UserInfoResponse.toDomain(): UserInfo {
    return UserInfo(
        name = name,
        email = email,
        nickname = nickname,
        gender = gender,
        address = address,
        birth = birth.toFullDateString(),   // 기존 로직 유지
        preferAgeLower = preferAgeLower,
        preferAgeUpper = preferAgeUpper,
        latitude = latitude,
        longitude = longitude
    )
}
