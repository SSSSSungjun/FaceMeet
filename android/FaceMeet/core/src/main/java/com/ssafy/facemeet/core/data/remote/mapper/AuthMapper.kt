package com.ssafy.facemeet.core.data.remote.mapper

import com.ssafy.facemeet.core.data.remote.dto.response.AuthResponse
import com.ssafy.facemeet.core.domain.model.Auth

fun AuthResponse<Unit>.toDomain(): Auth {
    return Auth(
        status = this.status,
        accessToken = this.accessToken,
        data = {}
    )
}
