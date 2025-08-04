package com.ssafy.facemeet.client.ui.chat

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ssafy.facemeet.client.R
import com.ssafy.facemeet.core.data.socket.model.ConnectionState
import com.ssafy.facemeet.core.data.socket.model.MessageType
import com.ssafy.facemeet.core.domain.model.ChatElement

private const val TAG = "ChattingScreen"

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ChattingScreen(
    roomId: Long,
    receiverId: Long,
    onBackClick: () -> Unit = {},
    viewModel: ChattingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val navigationEvent by viewModel.naviEvent.collectAsStateWithLifecycle(null)

    val messages by viewModel.messages.observeAsState(emptyList())
    val connectionState by viewModel.connectionState.observeAsState(ConnectionState.DISCONNECTED)

    val listState = rememberLazyListState()

    val imeInsets = WindowInsets.ime
    val density = LocalDensity.current
    val keyboardHeight by remember {
        derivedStateOf {
            imeInsets.getBottom(density)
        }
    }


    LaunchedEffect(navigationEvent) {
        when (navigationEvent) {
            ChatNaviEvent.ToBack -> onBackClick()
            else -> {}
        }
    }

    LaunchedEffect(roomId, receiverId) {
        viewModel.getAllChattingMessage(roomId)
        viewModel.connectToChat(roomId, receiverId)
    }

    LaunchedEffect(connectionState) {
        viewModel.updateConnectionState(connectionState)
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    LaunchedEffect(keyboardHeight) {
        if (keyboardHeight > 0 && messages.isNotEmpty()) {
            val layoutInfo = listState.layoutInfo
            val lastVisibleIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            if (lastVisibleIndex >= messages.size - 2) {
                listState.scrollToItem(messages.size - 1)
            }
        }
    }

    LaunchedEffect(messages) {
        viewModel.markAsRead()
    }

    uiState.error?.let { error ->
        LaunchedEffect(error) {
            Log.d(TAG, "ChattingScreen: Error occur")
            viewModel.clearError()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF4F3ED))
            .systemBarsPadding()
            .imePadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ChatHeader(
            userName = "닉네임",
            compatibilityScore = 87,
            onBack = viewModel::navigateToBack
        )

        // 채팅 목록
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 8.dp),
            verticalArrangement = Arrangement.Top,
            contentPadding = PaddingValues(bottom = 4.dp),
            reverseLayout = false
        ) {
            items(messages) { messageItem ->
                when (messageItem.messageType) {
                    MessageType.TEXT -> {
                        ChatMessageBubble(
                            message = messageItem.chatElement,
                            isMyMessage = messageItem.chatElement.senderID == viewModel.currentUserId
                        )
                    }

                    MessageType.DATE -> {
                        DateSeparator(date = messageItem.chatElement.content.toString())
                    }

                    MessageType.CHAT_END -> {
                        //ChatEndMessage()
                    }

                    MessageType.SYSTEM -> {
                        TODO()
                    }

                    MessageType.CHAT_START -> {
                        TODO()
                    }
                }
            }
        }

        // 입력창은 하단에 고정
        MessageInput(
            messageText = uiState.messageText,
            onMessageChange = { viewModel.updateMessageText(it) },
            onSendClick = { viewModel.sendMessage() },
            canSend = uiState.canSendMessage,
            isConnected = connectionState == ConnectionState.CONNECTED
        )

        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}


@Composable
fun ChatHeader(
    userName: String,
    compatibilityScore: Int,
    onBack: () -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF4F3ED))
            .padding(horizontal = 16.dp, vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {

        IconButton(
            onClick = { onBack() },
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
                color = Color(0xFF8B5A2B)
            )
        }

        Text(
            text = "궁합 ${compatibilityScore}%",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF6B7280)
        )
    }

}

@Composable
fun ConnectionStatusText(connectionState: ConnectionState) {
    val (color, text) = when (connectionState) {
        ConnectionState.CONNECTING ->
            MaterialTheme.colorScheme.primary to "연결 중..."

        ConnectionState.CONNECTED ->
            Color.Green to "온라인"

        ConnectionState.DISCONNECTED ->
            Color.Red to "연결 끊김"

        ConnectionState.ERROR ->
            MaterialTheme.colorScheme.error to "연결 오류"
    }

    Text(
        text = text,
        color = color,
        fontSize = 12.sp
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ChatMessageBubble(
    message: ChatElement,
    isMyMessage: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
    ) {
        if (isMyMessage) {
            // 내 메시지: 읽음/시간 - 메시지 (우측 정렬)
            Row(
                modifier = Modifier.align(Alignment.CenterEnd),
                verticalAlignment = Alignment.Bottom
            ) {

                Card(
                    modifier = Modifier.widthIn(max = 240.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF824946)
                    ),
                    shape = RoundedCornerShape(
                        topStart = 12.dp,
                        topEnd = 12.dp,
                        bottomStart = 12.dp,
                        bottomEnd = 4.dp
                    )
                ) {
                    Text(
                        text = message.content.toString(),
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        fontSize = 14.sp
                    )
                }
            }
        } else {

            Row(
                modifier = Modifier.align(Alignment.CenterStart),
                verticalAlignment = Alignment.Bottom
            ) {

                Card(
                    modifier = Modifier.widthIn(max = 240.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    shape = RoundedCornerShape(
                        topStart = 12.dp,
                        topEnd = 12.dp,
                        bottomStart = 4.dp,
                        bottomEnd = 12.dp
                    )
                ) {
                    Text(
                        text = message.content.toString(),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        fontSize = 14.sp
                    )
                }

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
                containerColor = Color(0xFFE5E4DD)
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
    onMessageChange: (String) -> Unit,
    onSendClick: () -> Unit,
    canSend: Boolean,
    isConnected: Boolean
) {
    Box(modifier = Modifier.padding(5.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .background(
                    color = Color.White,
                    shape = RoundedCornerShape(28.dp)
                )
                .border(
                    width = 1.dp,
                    color = Color(0xFFE5E7EB),
                    shape = RoundedCornerShape(28.dp)
                )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .height(50.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
            ) {
                BasicTextField(
                    value = messageText,
                    onValueChange = { onMessageChange(it) },
                    modifier = Modifier.weight(1f),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.Black
                    ),
                    singleLine = false,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Default,
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
                    },
                    enabled = isConnected
                )

                if (canSend) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(32.dp)
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
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

    }

}

@RequiresApi(Build.VERSION_CODES.O)
@Preview
@Composable
fun ChattingScreenPreview() {

    ChatHeader("dbstjdwns", 89, {})
//    Box(modifier = Modifier.background(Color(0xFFF4F3ED))) {
//        ChatMessageBubble(
//            ChatElement("응 아니아", 1L, 1L, 0, "19:25", true, ""),
//            isMyMessage = true
//        )
//    }


}
