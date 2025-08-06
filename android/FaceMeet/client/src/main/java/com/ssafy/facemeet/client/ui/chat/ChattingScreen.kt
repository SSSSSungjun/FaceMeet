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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.ssafy.facemeet.client.R
import com.ssafy.facemeet.client.ui.chat.component.CompactNoticeToggle
import com.ssafy.facemeet.core.data.socket.model.ChatMessageItem
import com.ssafy.facemeet.core.data.socket.model.ConnectionState
import com.ssafy.facemeet.core.data.socket.model.MessageType
import com.ssafy.facemeet.core.domain.model.ChatElement
import kotlinx.coroutines.delay

private const val TAG = "ChattingScreen"

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ChattingScreen(
    roomId: Long,
    onBackClick: () -> Unit = {},
    onPartnerProfile: (partnerId: Long) -> Unit = {},
    viewModel: ChattingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val messageState by viewModel.messageState.collectAsState()
    val navigationEvent by viewModel.naviEvent.collectAsStateWithLifecycle(null)
    val connectionState by viewModel.connectionState.observeAsState(ConnectionState.DISCONNECTED)

    val pagedMessages = messageState.pagedMessages.collectAsLazyPagingItems()
    val liveMessages by viewModel.liveMessages.collectAsState()

    val listState = rememberLazyListState()

    val keyboardHeight = WindowInsets.ime.getBottom(LocalDensity.current)
    var previousKeyboardHeight by remember { mutableIntStateOf(0) }

    LaunchedEffect(
        listState.isScrollInProgress,
        listState.firstVisibleItemIndex
    ) {
        val totalItemCount = liveMessages.size + pagedMessages.itemCount
        viewModel.onScrollStateChanged(
            isScrolling = listState.isScrollInProgress,
            firstVisibleItemIndex = listState.firstVisibleItemIndex,
            totalItemCount = totalItemCount
        )
    }

    LaunchedEffect(Unit) {
        viewModel.scrollEvent.collect { event ->
            when (event) {
                is ScrollEvent.ToBottom -> {
                    listState.scrollToItem(0)
                }

                is ScrollEvent.WithKeyboard -> {
                    listState.scrollToItem(0)
                }
            }
        }
    }

    LaunchedEffect(keyboardHeight) {
        when {
            keyboardHeight > 0 && previousKeyboardHeight == 0 -> {
                viewModel.onKeyboardShown()
            }
        }
        previousKeyboardHeight = keyboardHeight
    }

    LaunchedEffect(pagedMessages.loadState.refresh) {
        if (pagedMessages.loadState.refresh is LoadState.NotLoading && pagedMessages.itemCount > 0) {
            delay(100)
            viewModel.onInitialLoadComplete()
        }
    }

    LaunchedEffect(navigationEvent) {
        when (navigationEvent) {
            ChatNaviEvent.ToBack -> onBackClick()
            ChatNaviEvent.ToProfile -> onPartnerProfile(uiState.roomInfo.partnerID)
            else -> {}
        }
    }

    LaunchedEffect(roomId) {
        viewModel.initializeChat(roomId)
    }

    LaunchedEffect(connectionState) {
        viewModel.updateConnectionState(connectionState)
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            Log.d("ChattingScreen", "Error: $it")
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
            userName = uiState.roomInfo.partnerNickname,
            compatibilityScore = uiState.roomInfo.similar,
            onBack = viewModel::navigateToBack,
            onPartnerProfile = viewModel::navigateToProfile,
        )
        CompactNoticeToggle(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            isNoticeOpen = uiState.isNoticeOpen,
            onToggleNotice = viewModel::toggleNotice
        )

        Box(modifier = Modifier.weight(1f)) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.Top,
                contentPadding = PaddingValues(bottom = 4.dp),
                reverseLayout = true
            ) {
                items(
                    count = liveMessages.size,
                    key = { index ->
                        val message = liveMessages[index]
                        "${message.chatElement.senderID}_${message.chatElement.roomID}_${message.chatElement.content.hashCode()}_${message.chatElement.createdAt.hashCode()}"
                    }
                ) { index ->
                    val message = liveMessages[index]
                    Log.d("DEBUG_RENDER", "실시간 메시지 렌더링: index=$index, 총=${liveMessages.size}")
                    RenderMessage(
                        message = message,
                        isMyMessage = message.chatElement.senderID == viewModel.currentUserId,
                        isTemporary = false
                    )
                }

                items(pagedMessages.itemCount) { index ->
                    pagedMessages[index]?.let { message ->
                        Log.d("DEBUG_RENDER", "페이징 메시지 렌더링: index=$index, 총=${pagedMessages.itemCount}")
                        val isDuplicate = liveMessages.any { liveMsg ->
                            liveMsg.chatElement.content == message.chatElement.content &&
                                    liveMsg.chatElement.senderID == message.chatElement.senderID &&
                                    liveMsg.chatElement.createdAt == message.chatElement.createdAt
                        }

                        if (!isDuplicate) {
                            RenderMessage(
                                message = message,
                                isMyMessage = message.chatElement.senderID == viewModel.currentUserId,
                                isTemporary = false
                            )
                        }
                    }
                }

                // 로딩 상태
                when (pagedMessages.loadState.append) {
                    is LoadState.Loading -> {
                        item {
                            Log.d("DEBUG_RENDER", "로딩 UI 렌더링됨")
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }

                    is LoadState.Error -> {
                        item {
                            Text("메시지를 불러오는데 실패했습니다")
                        }
                    }

                    else -> {}
                }
            }


            if (!uiState.scrollState.isAtBottom) {
                FloatingActionButton(
                    onClick = viewModel::scrollToBottomManually,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                        .size(48.dp),
                    containerColor = Color.White.copy(alpha = 0.8f),
                    elevation = FloatingActionButtonDefaults.elevation(
                        defaultElevation = 1.dp, // 기본 그림자 크기
                        pressedElevation = 4.dp  // 눌렀을 때 그림자 크기
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "맨 아래로",
                        tint = Color.Black
                    )
                }
            }
        }

        MessageInput(
            messageText = uiState.messageText,
            onMessageChange = viewModel::updateMessageText,
            onSendClick = viewModel::sendMessage,
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


@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun RenderMessage(
    message: ChatMessageItem,
    isMyMessage: Boolean,
    isTemporary: Boolean
) {
    when (message.messageType) {
        MessageType.TEXT -> {
            ChatMessageBubble(
                message = message.chatElement,
                isMyMessage = isMyMessage,
                isTemporary = isTemporary
            )
        }

        MessageType.DATE -> {
            DateSeparator(date = message.chatElement.content.toString())
        }

        MessageType.CHAT_END -> {
            ChatEndMessage(message.chatElement.content)
        }

        MessageType.SYSTEM -> {
            // 시스템 메시지 처리
        }
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ChatMessageBubble(
    message: ChatElement,
    isMyMessage: Boolean,
    isTemporary: Boolean = false
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
            .alpha(if (isTemporary) 0.6f else 1f)

    ) {
        if (isMyMessage) {
            // 내 메시지: 읽음/시간 - 메시지 (우측 정렬)
            Row(
                modifier = Modifier.align(Alignment.CenterEnd),
                verticalAlignment = Alignment.Bottom
            ) {
                // 임시 메시지 상태 표시
                if (isTemporary) {
                    Text(
                        text = "전송중...",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                }

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
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        fontSize = 15.sp
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
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}

// 기존 UI 컴포넌트들은 그대로 유지
@Composable
fun ChatHeader(
    userName: String,
    compatibilityScore: Long,
    onBack: () -> Unit,
    onPartnerProfile: () -> Unit,
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
                color = Color(0xFF8B5A2B),
                modifier = Modifier.clickable {
                    onPartnerProfile()
                }
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
fun DateSeparator(date: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF4F3ED)),
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
fun ChatEndMessage(content: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(horizontal = 10.dp)
            .background(
                color = Color(0xFFE5E4DD), shape = RoundedCornerShape(10.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = content,
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            color = Color(0xFF666666),
            modifier = Modifier.padding(7.dp)
        )
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
    Column {
        ChatEndMessage("채팅을 할 수 가 없다")
        DateSeparator("2020-01-01")
    }

}
