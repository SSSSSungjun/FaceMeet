package com.ssafy.facemeet.client.ui.chat

// 필요한 import들
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ChattingScreen(
     matchingId : String
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9FAFB))
    ) {
        // 상단 헤더
        ChatHeader(
            userName = "닉네임",
            compatibilityScore = 87
        )

        // 메시지 리스트
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
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

        // 메시지 입력 영역
        MessageInput(
            messageText = messageText,
            onMessageTextChanged = { messageText = it },
            onSendClick = { messageText = "" }
        )
    }
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

@Composable
fun ChatHeader(
    userName: String,
    compatibilityScore: Int
) {
    Surface(
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "뒤로가기",
                        tint = Color(0xFF374151)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // 프로필 이미지
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFFA855F7),
                                    Color(0xFFEC4899)
                                )
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(
                                Color.White.copy(alpha = 0.3f),
                                CircleShape
                            )
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = userName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF111827)
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "궁합 ${compatibilityScore}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF6B7280)
                )

                IconButton(
                    onClick = { },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "더보기",
                        tint = Color(0xFF6B7280),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SystemMessage(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = Color.Transparent
            ),
            shape = RoundedCornerShape(20.dp)
        ) {
            Box(
                modifier = Modifier.background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFEDE9FE),
                            Color(0xFFFCE7F3)
                        )
                    )
                )
            ) {
                Text(
                    text = text,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF7C3AED)
                )
            }
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
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMyMessage) Arrangement.End else Arrangement.Start
    ) {
        Card(
            modifier = Modifier.widthIn(max = 280.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isMyMessage) Color.Transparent else Color.White
            ),
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isMyMessage) 16.dp else 4.dp,
                bottomEnd = if (isMyMessage) 4.dp else 16.dp
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = if (isMyMessage) 0.dp else 2.dp
            ),
            border = if (!isMyMessage) BorderStroke(1.dp, Color(0xFFF3F4F6)) else null
        ) {
            Box(
                modifier = if (isMyMessage) {
                    Modifier.background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFFA855F7),
                                Color(0xFFEC4899)
                            )
                        )
                    )
                } else Modifier
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        text = text,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isMyMessage) Color.White else Color(0xFF1F2937),
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = time,
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
                            color = if (isMyMessage) {
                                Color.White.copy(alpha = 0.7f)
                            } else {
                                Color(0xFF9CA3AF)
                            }
                        )

                        if (isMyMessage) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isRead) "읽음" else "안읽음",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isRead) {
                                    Color.White.copy(alpha = 0.7f)
                                } else {
                                    Color.White
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DateSeparator(date: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
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
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                style = MaterialTheme.typography.bodySmall,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF6B7280)
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
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                OutlinedTextField(
                    value = messageText,
                    onValueChange = onMessageTextChanged,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            "메시지를 입력하세요...",
                            color = Color(0xFF9CA3AF),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    maxLines = 4,
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFA855F7),
                        unfocusedBorderColor = Color(0xFFE5E7EB),
                        focusedContainerColor = Color(0xFFF9FAFB),
                        unfocusedContainerColor = Color(0xFFF9FAFB)
                    ),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Send,
                        keyboardType = KeyboardType.Text
                    ),
                    keyboardActions = KeyboardActions(
                        onSend = { onSendClick() }
                    )
                )

                // 이모지 버튼
                IconButton(
                    onClick = { },
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 8.dp)
                        .size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Face,
                        contentDescription = "이모지",
                        tint = Color(0xFF9CA3AF),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // 전송 버튼
            FloatingActionButton(
                onClick = onSendClick,
                modifier = Modifier.size(48.dp),
                containerColor = Color.Transparent,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 4.dp
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFFA855F7),
                                    Color(0xFFEC4899)
                                )
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "전송",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ChattingScreenPreview(){
    ChattingScreen("123")
}