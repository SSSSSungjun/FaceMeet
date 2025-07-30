package com.ssafy.facemeet.core.data.remote.mapper

import android.os.Build
import androidx.annotation.RequiresApi
import com.ssafy.facemeet.core.data.remote.dto.response.ChatElementResponse
import com.ssafy.facemeet.core.domain.model.ChatElement
import com.ssafy.facemeet.core.util.format.ParsingTimeData.formatSmartDate
import com.ssafy.facemeet.core.util.format.ParsingTimeData.toHourMinuteString

@RequiresApi(Build.VERSION_CODES.O)
fun ChatElementResponse.toDomain() : ChatElement{
   return ChatElement(
       content = content,
       senderID = senderID,
       receiverID = receiverID,
       roomID = roomID,
       createdAt = createdAt.formatSmartDate(),
       isRead = isRead,
       readAt = readAt.toHourMinuteString()
   )
}