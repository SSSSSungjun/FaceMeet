package com.ssafy.facemeet.client.ui.chat

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.rememberAsyncImagePainter
import com.ssafy.facemeet.client.ui.chat.component.CompactNoticeToggle
import com.ssafy.facemeet.client.ui.chat.model.ChatNaviEvent
import com.ssafy.facemeet.client.ui.chat.model.ChatUiState
import com.ssafy.facemeet.client.ui.chat.model.MessageStatus
import com.ssafy.facemeet.client.ui.chat.model.ScrollEvent
import com.ssafy.facemeet.core.data.socket.model.ChatMessageItem
import com.ssafy.facemeet.core.data.socket.model.ConnectionState
import com.ssafy.facemeet.core.data.socket.model.MessageType
import com.ssafy.facemeet.core.domain.model.ChatElement
import com.ssafy.facemeet.core.util.AppStateManager
import com.ssafy.facemeet.core.util.format.ParsingTimeData.toFullDateString
import com.ssafy.facemeet.core.util.format.ParsingTimeData.toHourMinuteString
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ChattingScreen(
    roomId: Long,
    onBackClick: () -> Unit = {},
    onPartnerProfile: (partnerId: Long) -> Unit = {},
    viewModel: ChattingViewModel = hiltViewModel()
) {

    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.onScreenResume()
            } else if (event == Lifecycle.Event.ON_PAUSE) {
                viewModel.onScreenPause()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    DisposableEffect(Unit) {
        AppStateManager.setCurrentScreen("ChattingScreen", roomId)
        onDispose {
            Log.d("ChattingScreen", "화면 나감 - AppStateManager 정리")
            AppStateManager.clearCurrentScreen()
        }
    }

    val uiState by viewModel.uiState.collectAsState()
    val messageState by viewModel.messageState.collectAsState()
    val navigationEvent by viewModel.naviEvent.collectAsStateWithLifecycle(null)
    val connectionState by viewModel.connectionState.observeAsState(ConnectionState.DISCONNECTED)

    val pagedMessages = messageState.pagedMessages.collectAsLazyPagingItems()
    val unifiedMessages by viewModel.unifiedMessages.collectAsState()

    val listState = rememberLazyListState()

    val keyboardHeight = WindowInsets.ime.getBottom(LocalDensity.current)
    var previousKeyboardHeight by remember { mutableIntStateOf(0) }


    LaunchedEffect(listState.isScrollInProgress, listState.firstVisibleItemIndex) {
        viewModel.onScrollStateChanged(
            listState.isScrollInProgress,
            listState.firstVisibleItemIndex
        )
    }

    LaunchedEffect(Unit) {
        viewModel.scrollEvent.collect { event ->
            when (event) {
                is ScrollEvent.ToBottom -> listState.scrollToItem(0)
                is ScrollEvent.WithKeyboard -> listState.scrollToItem(0)
            }
        }
    }

    LaunchedEffect(keyboardHeight) {
        if (keyboardHeight > 0 && previousKeyboardHeight == 0) {
            viewModel.onKeyboardShown()
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
        AppStateManager.setCurrentScreen("ChattingScreen", roomId)
        Log.d("FCM", "ChattingScreen:${AppStateManager.getCurrentScreen()} ")
    }

    DisposableEffect(Unit) {
        onDispose {
            AppStateManager.setCurrentScreen("", null)
        }
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
            uiState = uiState
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
                    count = unifiedMessages.size,
                    key = { index -> unifiedMessages[index].localId ?: index }
                ) { index ->
                    val messageItem = unifiedMessages[index]

                    RenderMessage(
                        message = messageItem.chatMessage,
                        isMyMessage = messageItem.chatMessage.chatElement.senderID == viewModel.currentUserId,
                        messageStatus = messageItem.status,
                        showReadStatus = messageItem.showReadStatus
                    )

                    val nextMessage = if (index < unifiedMessages.size - 1) {
                        unifiedMessages[index + 1].chatMessage
                    } else {
                        if (pagedMessages.itemCount > 0) pagedMessages.peek(0) else null
                    }

                    if (shouldShowDateSeparator(messageItem.chatMessage, nextMessage)) {
                        DateSeparator(date = messageItem.chatMessage.chatElement.createdAt.toFullDateString())
                    }


                }

                items(
                    count = pagedMessages.itemCount,
                    key = { index ->
                        pagedMessages.peek(index)?.chatElement?.let { chatElement ->
                            chatElement.generateKey()
                        } ?: "paged_fallback_$index"
                    }
                ) { index ->
                    pagedMessages[index]?.let { message ->

                        val isMyMessage = message.chatElement.senderID == viewModel.currentUserId
                        RenderMessage(
                            message = message,
                            isMyMessage = isMyMessage,
                            showReadStatus = isMyMessage && message.chatElement.isRead
                        )

                        val nextMessage = if (index < pagedMessages.itemCount - 1) {
                            pagedMessages.peek(index + 1)
                        } else null

                        if (shouldShowDateSeparator(message, nextMessage)) {
                            DateSeparator(date = message.chatElement.createdAt.toFullDateString())
                        }


                    }
                }

                val oldestMessage = if (pagedMessages.itemCount > 0) {
                    pagedMessages.peek(pagedMessages.itemCount - 1)
                } else if (unifiedMessages.isNotEmpty()) {
                    unifiedMessages.last().chatMessage
                } else {
                    null
                }
                if (oldestMessage != null) {
                    item(key = "oldest_date_separator") {
                        DateSeparator(date = oldestMessage.chatElement.createdAt.toFullDateString())
                    }
                }

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

                    else -> {}
                }
            }

            CompactNoticeToggle(
                modifier = Modifier.align(Alignment.TopEnd),
                isNoticeOpen = uiState.isNoticeOpen,
                onToggleNotice = viewModel::toggleNotice,
                similar = uiState.roomInfo.similar.toInt()
            )

            if (!uiState.scrollState.isAtBottom) {
                if (uiState.newMessageContent != null) {
                    // New message exists: Show the custom Box button
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 10.dp)
                            .clickable(onClick = viewModel::scrollToBottomManually)
                            .background(Color.White, shape = RoundedCornerShape(20.dp))
                            .padding(horizontal = 16.dp, vertical = 5.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "New message",
                                tint = Color.Gray,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = uiState.newMessageContent.toString(),
                                color = Color.Gray,
                                fontSize = 14.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                } else {
                    // No new message: Show the simple FloatingActionButton
                    FloatingActionButton(
                        onClick = viewModel::scrollToBottomManually,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(10.dp)
                            .size(30.dp),
                        containerColor = Color.White,
                        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 1.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Scroll to bottom",
                            tint = Color.Black,
                        )
                    }
                }
            }

        }

        if (uiState.roomInfo.blocked || uiState.roomInfo.deleted) {
            BlockedChat()
        } else {
            MessageInput(
                messageText = uiState.messageText,
                onMessageChange = viewModel::updateMessageText,
                onSendClick = viewModel::sendMessage,
                //canSend = uiState.canSendMessage && connectionState == ConnectionState.CONNECTED,
               // isConnected = connectionState != ConnectionState.DISCONNECTED
            )
        }

        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
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
    messageStatus: MessageStatus = MessageStatus.RECEIVED,
    showReadStatus: Boolean = false
) {
    if (isMyMessage) {
        Log.d(
            "WebSocket-ReadStatus",
            "🎨 UI 렌더링 - 메시지 [${message.chatElement.content}]: isMyMessage=$isMyMessage, showReadStatus=$showReadStatus messageState : $messageStatus"
        )
    }
    when (message.messageType) {
        MessageType.TEXT -> {
            ChatMessageBubble(
                message = message.chatElement,
                isMyMessage = isMyMessage,
                messageStatus = messageStatus,
                showReadStatus = showReadStatus
            )
        }

        MessageType.DATE -> DateSeparator(date = message.chatElement.content.toString())
        MessageType.CHAT_END -> ChatEndMessage(message.chatElement.content)
        MessageType.SYSTEM -> {}
    }
}

@SuppressLint("ServiceCast")
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ChatMessageBubble(
    message: ChatElement,
    isMyMessage: Boolean,
    messageStatus: MessageStatus,
    showReadStatus: Boolean
) {
    var showCopyDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val hapticFeedback = LocalHapticFeedback.current

    val isFailed = messageStatus == MessageStatus.FAILED
    val bubbleColor = when (isMyMessage) {
        true -> if (isFailed) Color(0xFFFFCDD2) else Color(0xFF824946)
        false -> Color.White
    }
    val textColor =
        if (isMyMessage && isFailed) Color.Black else if (isMyMessage) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
    val bubbleShape = when (isMyMessage) {
        true -> RoundedCornerShape(
            topStart = 12.dp,
            topEnd = 12.dp,
            bottomStart = 12.dp,
            bottomEnd = 4.dp
        )

        false -> RoundedCornerShape(
            topStart = 12.dp,
            topEnd = 12.dp,
            bottomStart = 4.dp,
            bottomEnd = 12.dp
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .alpha(if (messageStatus == MessageStatus.PENDING) 0.7f else 1f)
    ) {
        Row(
            modifier = Modifier.align(if (isMyMessage) Alignment.CenterEnd else Alignment.CenterStart),
            verticalAlignment = Alignment.Bottom
        ) {
            if (isMyMessage) {
                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.padding(end = 4.dp)
                ) {
                    if (showReadStatus) {
                        Text(
                            text = "읽음",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            fontSize = 10.sp
                        )
                    }
                    when (messageStatus) {
                        MessageStatus.PENDING -> Text(
                            text = "전송중",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            fontSize = 10.sp
                        )

                        MessageStatus.FAILED -> Text(
                            text = "전송실패",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Red,
                            fontSize = 10.sp
                        )

                        else -> {}
                    }
                    Text(
                        text = message.createdAt.toHourMinuteString(),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        fontSize = 10.sp
                    )
                }
            }

            Card(
                modifier = Modifier
                    .widthIn(max = 240.dp)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onLongPress = {
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                showCopyDialog = true
                            }
                        )
                    },
                colors = CardDefaults.cardColors(containerColor = bubbleColor),
                shape = bubbleShape
            ) {
                Text(
                    text = message.content.toString(),
                    color = textColor,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    fontSize = 15.sp
                )
            }

            if (!isMyMessage) {
                Text(
                    text = message.createdAt.toHourMinuteString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    modifier = Modifier.padding(start = 4.dp),
                    fontSize = 10.sp
                )
            }
        }
    }

    // 복사 다이얼로그
    if (showCopyDialog) {
        AlertDialog(
            containerColor = Color.White,
            titleContentColor = Color.Black,
            textContentColor = Color.Black,
            onDismissRequest = { showCopyDialog = false },
            text = {
                Text("이 메시지를 복사하시겠습니까?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val clipboard =
                            context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("message", message.content.toString())
                        clipboard.setPrimaryClip(clip)

                        Toast.makeText(context, "메시지가 복사되었습니다", Toast.LENGTH_SHORT).show()
                        showCopyDialog = false
                    }
                ) {
                    Text("복사")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showCopyDialog = false }
                ) {
                    Text("취소")
                }
            }
        )
    }
}


@Composable
fun ChatHeader(
    userName: String,
    compatibilityScore: Long,
    onBack: () -> Unit,
    onPartnerProfile: () -> Unit,
    uiState: ChatUiState
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF4F3ED))
            .padding(horizontal = 16.dp, vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack, modifier = Modifier.size(40.dp)) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color(0xFF374151)
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier=Modifier.weight(1f)
        ) {
            Image(
                painter = rememberAsyncImagePainter(model = uiState.roomInfo.imgURL),
                contentDescription = "Profile image",
                modifier = Modifier.size(40.dp)
            )
            Text(
                text = userName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF8B5A2B),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.clickable { onPartnerProfile() }
            )
        }

        Text(
            text = "궁합 ${compatibilityScore}점",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF6B7280)
        )
    }
}

@Composable
fun DateSeparator(date: String) {
    val today = LocalDate.now()
    val yesterday = today.minusDays(1)
    val formatter = DateTimeFormatter.ofPattern("yyyy년 M월 d일", Locale.KOREAN)
    val todayFormatDate = today.format(formatter)
    val yesterdayFormatDate = yesterday.format(formatter)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE5E4DD)),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Text(
                text = if (date == todayFormatDate) "오늘" else if (date == yesterdayFormatDate) "어제" else date,
                modifier = Modifier.padding(horizontal = 30.dp, vertical = 5.dp),
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
            .padding(horizontal = 30.dp, vertical = 5.dp)
            .background(color = Color(0xFFE5E4DD), shape = RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = content,
            fontSize = 13.sp,
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
    onSendClick: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    val isButtonEnabled = messageText.isNotBlank()

    Box(modifier = Modifier.padding(5.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .background(color = Color.White, shape = RoundedCornerShape(28.dp))
                .border(width = 1.dp, color = Color(0xFFE5E7EB), shape = RoundedCornerShape(28.dp))
                .padding(horizontal = 16.dp, vertical = 3.dp)
        ) {
            BasicTextField(
                value = messageText,
                onValueChange = onMessageChange,
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focusRequester)
                    .clickable(onClick = {
                        focusRequester.requestFocus()
                    }),
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color.Black),
                singleLine = false,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Default,
                    keyboardType = KeyboardType.Text
                ),
                decorationBox = { innerTextField ->
                    if (messageText.isEmpty()) {
                        Text(
                            "Enter a message...",
                            color = Color(0xFF8F939C),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    innerTextField()
                },
                enabled = true // 항상 입력 가능하도록 설정
            )

            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        color = if (isButtonEnabled) Color(0xFF824946) else Color(0xFFE0E0E0),
                        shape = CircleShape
                    )
                    .clickable(enabled = isButtonEnabled) {
                        onSendClick()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Send",
                    tint = if (isButtonEnabled) Color.White else Color(0xFF8F939C),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun BlockedChat() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 8.dp)
            .background(color = Color(0xFFDDDDDD), RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "상대방과 더 이상 채팅을 할 수 없습니다.",
            color = Color(0xFF666666),
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Normal,
            modifier = Modifier.padding(10.dp)
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun shouldShowDateSeparator(
    currentMessage: ChatMessageItem,
    nextMessage: ChatMessageItem?
): Boolean {
    if (nextMessage == null) {
        return false
    }

    val currentDate = currentMessage.chatElement.createdAt.toFullDateString()
    val nextDate = nextMessage.chatElement.createdAt.toFullDateString()

    return currentDate != nextDate
}


@Preview
@Composable
fun tmpPreview() {
    val context = LocalContext.current

    AlertDialog(
        containerColor = Color.White,
        titleContentColor = Color.Black,
        textContentColor = Color.Black,
        onDismissRequest = { },
        text = {
            Text("이 메시지를 복사하시겠습니까?")
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val clipboard =
                        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("message", " message.content.toString()")
                    clipboard.setPrimaryClip(clip)

                    Toast.makeText(context, "메시지가 복사되었습니다", Toast.LENGTH_SHORT).show()
                }
            ) {
                Text(text = "복사")
            }
        },
        dismissButton = {
            TextButton(
                onClick = { }
            ) {
                Text("취소")
            }
        }
    )
}