package com.ssafy.facemeet.core.data.remote.mapper

import android.os.Build
import androidx.annotation.RequiresApi
import com.ssafy.facemeet.core.data.remote.dto.response.ChattingAllResponse
import com.ssafy.facemeet.core.domain.model.ChattingAll

@RequiresApi(Build.VERSION_CODES.O)
fun ChattingAllResponse.toDomain(): ChattingAll {
    return ChattingAll(
        messages = this.messages.map { it.toDomain() }.toMutableList(),
        totalPages = this.totalPages
    )
}

