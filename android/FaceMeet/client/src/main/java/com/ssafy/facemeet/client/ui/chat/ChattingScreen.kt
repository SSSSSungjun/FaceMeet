package com.ssafy.facemeet.client.ui.chat

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ssafy.facemeet.client.R


@Composable
fun ChattingScreen(
    matchingId: String
) {
    val messages = listOf(
        ChatUIMessage(
            id = 1,
            text = "관상 궁합 87%로 매칭되었습니다 ✨",
            isSystem = true
        ),
        ChatUIMessage(
            id = 2,
            text = "안녕하세요! 매칭이 되어서 반갑습니다 😊",
            isMine = false,
            time = "16:13"
        ),
        ChatUIMessage(
            id = 3,
            text = "안녕하세요! 저도 반가워요. 관상 궁합이 87%라니 신기해요!",
            isMine = true,
            time = "16:13",
            isRead = true
        ),
        ChatUIMessage(
            id = 4,
            text = "네! 관상으로 만난 인연이라니 더 특별한 것 같아요 ✨",
            isMine = false,
            time = "16:13"
        ),
        ChatUIMessage(
            id = 5,
            text = "어떤 관상 특징이 잘 맞는 거였는지 궁금해요",
            isMine = true,
            time = "16:13",
            isRead = true
        ),
        ChatUIMessage(
            id = 6,
            text = "2025년 07월 31일",
            isDate = true
        ),
        ChatUIMessage(
            id = 7,
            text = "어떤 관상 특징이 잘 맞는 거였는지 궁금해요",
            isMine = true,
            time = "00:13",
            isRead = true
        )
    )

    var messageText by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color(0xFFF4F3ED))
            .systemBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    color = Color(0xFFF4F3ED)
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ChatHeader(
                userName = "닉네임",
                compatibilityScore = 87
            )

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Color(0xFFF4F3ED)) // LazyColumn 배경도 통일
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(messages) { message ->
                    when {
                        message.isSystem -> SystemMessage(text = message.text)
                        message.isDate -> DateSeparator(date = message.text)
                        else -> ChatBubble(
                            text = message.text,
                            time = message.time,
                            isMyMessage = message.isMine,
                            isRead = message.isRead
                        )
                    }
                }
            }

            MessageInput(
                messageText = messageText,
                onMessageTextChanged = { messageText = it },
                onSendClick = { messageText = "" }
            )
        }
    }

}

@Composable
fun ChatHeader(
    userName: String,
    compatibilityScore: Int
) {
    Surface {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF4F3ED))
                .padding(horizontal = 16.dp, vertical = 5.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            IconButton(
                onClick = { },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "뒤로가기",
                    tint = Color(0xFF374151)
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 프로필 이미지
                Image(
                    painter = painterResource(id = R.drawable.temp_face),
                    contentDescription = "내 이미지 설명",
                    modifier = Modifier.size(40.dp)
                )

                Text(
                    text = userName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF111827)
                )
            }

            Text(
                text = "궁합 ${compatibilityScore}%",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF6B7280)
            )
        }
    }
}

@Composable
fun SystemMessage(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text(
                text = text,
                modifier = Modifier.padding(horizontal = 30.dp, vertical = 8.dp),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = Color.DarkGray
            )
        }
    }
}

@Composable
fun ChatBubble(
    text: String,
    time: String,
    isMyMessage: Boolean,
    isRead: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF4F3ED)), // 채팅 버블 배경도 통일
        horizontalArrangement = if (isMyMessage) Arrangement.End else Arrangement.Start
    ) {
        Box {
            Card(
                modifier = Modifier.widthIn(max = 280.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (!isMyMessage) Color.White else Color(0xFF824946)
                ),
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isMyMessage) 16.dp else 4.dp,
                    bottomEnd = if (isMyMessage) 4.dp else 16.dp
                ),
                border = if (!isMyMessage) BorderStroke(1.dp, Color(0xFFF3F4F6)) else null
            ) {
                Text(
                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp),
                    text = text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isMyMessage) Color.White else Color(0xFF1F2937),
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@Composable
fun DateSeparator(date: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF4F3ED)), // 날짜 구분자 배경도 통일
        horizontalArrangement = Arrangement.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFE5E7EB)
            ),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Text(
                text = date,
                modifier = Modifier.padding(horizontal = 30.dp, vertical = 8.dp),
                style = MaterialTheme.typography.bodySmall,
                fontSize = 11.sp,
                color = Color.DarkGray
            )
        }
    }
}

@Composable
fun MessageInput(
    messageText: String,
    onMessageTextChanged: (String) -> Unit,
    onSendClick: () -> Unit
) {
    Surface(
        color = Color(0xFFF4F3ED), // 입력창 배경도 통일
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 입력창과 버튼을 포함하는 둥근 프레임
            Box(
                modifier = Modifier
                    .weight(1f)
                    .shadow(
                        elevation = 1.dp,
                        shape = RoundedCornerShape(30.dp),
                        clip = true
                    )
                    .border(
                        width = 1.dp,
                        color = Color(0xFFE5E7EB),
                        shape = RoundedCornerShape(30.dp)
                    )
                    .background(
                        color = Color.White,
                        shape = RoundedCornerShape(30.dp),
                    )
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // 텍스트 입력
                    BasicTextField(
                        value = messageText,
                        onValueChange = onMessageTextChanged,
                        modifier = Modifier
                            .weight(1f)
                            .padding(vertical = 8.dp, horizontal = 5.dp),
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            color = Color.Black
                        ),
                        maxLines = 4,
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Send,
                            keyboardType = KeyboardType.Text
                        ),
                        keyboardActions = KeyboardActions(
                            onSend = { onSendClick() }
                        ),
                        decorationBox = { innerTextField ->
                            if (messageText.isEmpty()) {
                                Text(
                                    "메시지를 입력하세요...",
                                    color = Color(0xFF8F939C),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            innerTextField()
                        }
                    )

                    // 전송 버튼 (메시지가 있을 때만 표시)
                    if (!messageText.isNotBlank()) { //임시
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .background(
                                    color = Color(0xFF824946),
                                    shape = CircleShape
                                )
                                .clickable { onSendClick() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "전송",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun ChattingScreenPreview() {
    ChattingScreen("123")
}

data class ChatUIMessage(
    val id: Int,
    val text: String,
    val isMine: Boolean = false,
    val time: String = "",
    val isRead: Boolean = false,
    val isSystem: Boolean = false,
    val isDate: Boolean = false
)