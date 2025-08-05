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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
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
    receiverId: Long,
    onBackClick: () -> Unit = {},
    viewModel: ChattingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val navigationEvent by viewModel.naviEvent.collectAsStateWithLifecycle(null)

    val pagedMessagesFlow by viewModel.pagedMessages.collectAsState()
    val pagedMessages = pagedMessagesFlow.collectAsLazyPagingItems()

    val realtimeMessages by viewModel.realtimeMessages.collectAsState()
    val tempMessages by viewModel.tempMessages.collectAsState()
    val connectionState by viewModel.connectionState.observeAsState(ConnectionState.DISCONNECTED)

    val listState = rememberLazyListState()

    val imeInsets = WindowInsets.ime
    val density = LocalDensity.current
    val keyboardHeight by remember {
        derivedStateOf {
            imeInsets.getBottom(density)
        }
    }

    var previousKeyboardHeight by remember { mutableIntStateOf(0) }

    // 🔧 페이징 데이터 버퍼링
    var stablePagedMessages by remember { mutableStateOf<List<ChatMessageItem>>(emptyList()) }
    var lastStableCount by remember { mutableIntStateOf(0) }
    var stabilityCheckCount by remember { mutableIntStateOf(0) }

    // 🔧 페이징 데이터 안정성 체크 + 중복 제거
    LaunchedEffect(pagedMessages.itemCount, pagedMessages.loadState.refresh) {
        if (pagedMessages.loadState.refresh is LoadState.NotLoading && pagedMessages.itemCount > 0) {

            if (pagedMessages.itemCount == lastStableCount) {
                stabilityCheckCount++
            } else {
                stabilityCheckCount = 0
                lastStableCount = pagedMessages.itemCount
            }

            Log.d(TAG, "📊 페이징 안정성 체크: count=${pagedMessages.itemCount}, stability=${stabilityCheckCount}")

            if (stabilityCheckCount >= 2) {
                Log.d(TAG, "✅ 페이징 데이터 안정화 완료 - UI 업데이트")

                // 🔧 안정화된 데이터를 UI용 버퍼에 복사 (중복 제거)
                val bufferedMessages = mutableListOf<ChatMessageItem>()
                for (i in 0 until pagedMessages.itemCount) {
                    pagedMessages[i]?.let { pagedMessage ->
                        // 실시간 메시지와 중복되지 않는지 확인
                        val isDuplicate = realtimeMessages.any { realtimeMessage ->
                            realtimeMessage.chatElement.content == pagedMessage.chatElement.content &&
                                    realtimeMessage.chatElement.senderID == pagedMessage.chatElement.senderID &&
                                    realtimeMessage.chatElement.createdAt == pagedMessage.chatElement.createdAt
                        }

                        if (!isDuplicate) {
                            bufferedMessages.add(pagedMessage)
                        }
                    }
                }
                stablePagedMessages = bufferedMessages

                // 첫 번째 안정화 시에만 스크롤
                if (stablePagedMessages.isNotEmpty() && listState.firstVisibleItemIndex == 0) {
                    delay(100)
                    listState.scrollToItem(0)
                }
            }
        }
    }

    // 스크롤 상태 감지
    LaunchedEffect(listState.isScrollInProgress) {
        if (listState.isScrollInProgress) {
            viewModel.onUserScrollStart()
        } else {
            viewModel.onUserScrollEnd()
        }
    }

    // 스크롤 위치 추적
    LaunchedEffect(listState.firstVisibleItemIndex) {
        val totalItemCount = tempMessages.size + realtimeMessages.size + stablePagedMessages.size
        viewModel.onScrollPositionChanged(
            firstVisibleItemIndex = listState.firstVisibleItemIndex,
            totalItemCount = totalItemCount
        )
    }

    // 자동 스크롤 처리 (새 메시지, 메시지 전송 시)
    LaunchedEffect(Unit) {
        viewModel.scrollToBottom.collect {
            listState.animateScrollToItem(0)
        }
    }

    // 키보드와 함께 스크롤 처리
    LaunchedEffect(Unit) {
        viewModel.scrollWithKeyboard.collect {
            listState.animateScrollToItem(0)
        }
    }

    // 키보드 높이 변화 감지
    LaunchedEffect(keyboardHeight) {
        when {
            keyboardHeight > 0 && previousKeyboardHeight == 0 -> {
                viewModel.onKeyboardShown()
            }
            keyboardHeight == 0 && previousKeyboardHeight > 0 -> {
                // 키보드가 내려감 - 현재 위치 유지
            }
        }
        previousKeyboardHeight = keyboardHeight
    }

    LaunchedEffect(navigationEvent) {
        when (navigationEvent) {
            ChatNaviEvent.ToBack -> onBackClick()
            else -> {}
        }
    }

    LaunchedEffect(roomId) {
        viewModel.initializeChat(roomId)
        // 새 채팅방 진입 시 버퍼 초기화
        stablePagedMessages = emptyList()
        lastStableCount = 0
        stabilityCheckCount = 0
    }

    LaunchedEffect(connectionState) {
        viewModel.updateConnectionState(connectionState)
    }

    uiState.error?.let { error ->
        LaunchedEffect(error) {
            Log.d("ChattingScreen", "ChattingScreen: Error occur")
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
            compatibilityScore = 87,
            onBack = viewModel::navigateToBack
        )

        Box(modifier = Modifier.weight(1f)) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.Top,
                contentPadding = PaddingValues(bottom = 4.dp),
                reverseLayout = true
            ) {
                // 1. 임시 메시지 (실시간)
                items(
                    count = tempMessages.size,
                    key = { index ->
                        val tempMessage = tempMessages.reversed()[index]
                        "temp_${tempMessage.chatElement.content}_${tempMessage.chatElement.createdAt}_${index}"
                    }
                ) { index ->
                    val tempMessage = tempMessages.reversed()[index]
                    when (tempMessage.messageType) {
                        MessageType.TEXT -> {
                            ChatMessageBubble(
                                message = tempMessage.chatElement,
                                isMyMessage = tempMessage.chatElement.senderID == viewModel.currentUserId,
                                isTemporary = true
                            )
                        }

                        MessageType.DATE -> {
                            DateSeparator(date = tempMessage.chatElement.content.toString())
                        }

                        MessageType.CHAT_END -> {
                            ChatEndMessage(tempMessage.chatElement.content)
                        }

                        MessageType.SYSTEM -> {}
                    }
                }

                // 2. 실시간 메시지 (실시간)
                items(
                    count = realtimeMessages.size,
                    key = { index ->
                        val realtimeMessage = realtimeMessages.reversed()[index]
                        "realtime_${index}_${realtimeMessage.chatElement.senderID}_${realtimeMessage.chatElement.createdAt}_${realtimeMessage.chatElement.content.hashCode()}"
                    }
                ) { index ->
                    val realtimeMessage = realtimeMessages.reversed()[index]
                    when (realtimeMessage.messageType) {
                        MessageType.TEXT -> {
                            ChatMessageBubble(
                                message = realtimeMessage.chatElement,
                                isMyMessage = realtimeMessage.chatElement.senderID == viewModel.currentUserId,
                                isTemporary = false
                            )
                        }

                        MessageType.DATE -> {
                            DateSeparator(date = realtimeMessage.chatElement.content.toString())
                        }

                        MessageType.CHAT_END -> {
                            ChatEndMessage(realtimeMessage.chatElement.content)
                        }

                        MessageType.SYSTEM -> {}
                    }
                }

                // 3. 🔧 안정화된 페이징 메시지 (버퍼링됨)
                items(
                    count = stablePagedMessages.size,
                    key = { index ->
                        val messageItem = stablePagedMessages.reversed()[index]
                        "paged_${index}_${messageItem.chatElement.senderID}_${messageItem.chatElement.createdAt}_${messageItem.chatElement.content.hashCode()}"
                    }
                ) { index ->
                    val messageItem = stablePagedMessages.reversed()[index]
                    when (messageItem.messageType) {
                        MessageType.TEXT -> {
                            ChatMessageBubble(
                                message = messageItem.chatElement,
                                isMyMessage = messageItem.chatElement.senderID == viewModel.currentUserId,
                                isTemporary = false
                            )
                        }

                        MessageType.DATE -> {
                            DateSeparator(date = messageItem.chatElement.content.toString())
                        }

                        MessageType.CHAT_END -> {
                            ChatEndMessage(messageItem.chatElement.content)
                        }

                        MessageType.SYSTEM -> {}
                    }
                }

                // 🔧 로딩 상태는 원본 페이징 데이터 기준 (사용자 스크롤 시에만)
                if (stablePagedMessages.isNotEmpty()) {
                    when (pagedMessages.loadState.append) {
                        is LoadState.Loading -> {
                            item {
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
            }

            // 초기 로딩 중일 때만 로딩 표시
            if (stablePagedMessages.isEmpty() && pagedMessages.loadState.refresh is LoadState.Loading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFF824946))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "채팅을 불러오는 중...",
                            color = Color(0xFF8B5A2B),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            // 맨 아래로 스크롤 버튼
            if (listState.firstVisibleItemIndex > 3) {
                FloatingActionButton(
                    onClick = { viewModel.scrollToBottomManually() },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                        .size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "맨 아래로"
                    )
                }
            }
        }

        MessageInput(
            messageText = uiState.messageText,
            onMessageChange = { viewModel.updateMessageText(it) },
            onSendClick = { viewModel.sendMessage() },
            canSend = uiState.canSendMessage,
            isConnected = connectionState == ConnectionState.CONNECTED
        )
    }
}
// ChatMessageBubble에 isTemporary 파라미터만 추가
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
            .alpha(if (isTemporary) 0.6f else 1f) // 임시 메시지는 반투명
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

// 기존 UI 컴포넌트들은 그대로 유지
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
