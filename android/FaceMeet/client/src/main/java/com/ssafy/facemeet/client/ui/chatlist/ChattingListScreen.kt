package com.ssafy.facemeet.client.ui.chatlist

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Badge
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.ssafy.facemeet.client.R
import com.ssafy.facemeet.client.ui.profile.partner.dialog.RoomExitDialog
import com.ssafy.facemeet.client.ui.theme.ChosunGongseo
import com.ssafy.facemeet.core.domain.model.ChatListItem

private const val TAG = "ChattingListScreen"

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ChattingListScreen(
    onItemClick: (ChatListItem) -> Unit = {},
    viewModel: ChattingListViewModel = hiltViewModel()
) {

    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.initializeChatting()
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF4F3ED))
    ) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(15.dp),
            text = "채팅 목록",
            fontSize = 18.sp,
            fontFamily = ChosunGongseo
        )
        NoticeBanner()

        if (!uiState.isLoading) {
            // 로딩 중일 때 로딩 인디케이터 등을 표시
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            // 로딩이 완료되었을 때 LazyColumn을 표시
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(uiState.chatList) { item ->
                    ChatListElementItem(
                        item = item,
                        onClick = { onItemClick(item) },
                        onLeaveRoom = { roomId ->
                            viewModel.setSelectedRoomId(roomId)
                            viewModel.clickExitRoom()
                        }
                    )
                }
            }
        }
    }

    RoomExitDialog(
        showDialog = uiState.showExitDialog,
        onConfirm = {
            viewModel.dismissExitDialog()
            viewModel.exitRoom(viewModel.selectedRoomId)
        },
        onDismiss = {
            viewModel.dismissExitDialog()
        }
    )

}

@Composable
fun NoticeBanner() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)
            .background(Color(0xFFFBFBFB), RoundedCornerShape(15.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "말 한마디는 채팅방의 분위기를 바꿔요.\n" +
                    "서로 존중하며 기분 좋은 대화를 나눠봐요.",
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(10.dp),
            color = Color(0xFF666666)
        )
    }
}

// 2. 아이템 UI
@Composable
fun ChatListElementItem(
    item: ChatListItem,
    onClick: () -> Unit,
    onLeaveRoom: (roomId: Long) -> Unit
) {
    var isMenuVisible by rememberSaveable { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }

    val menuWidth = 40.dp
    val offsetX by animateDpAsState(
        targetValue = if (isMenuVisible) -menuWidth else 0.dp,
        animationSpec = tween(300),
        label = "slide"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp) // 높이 고정
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(menuWidth)
                .align(Alignment.CenterEnd), // 오른쪽 끝에 배치
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_exit_room), // ⛔ 아이콘 리소스
                contentDescription = "채팅나가기",
                tint = Color(0xFF666666),
                modifier = Modifier
                    .size(24.dp)
                    .clickable {
                        onLeaveRoom(item.chatRoomId)
                        isMenuVisible = false
                    },
            )
        }

        Row(
            modifier = Modifier
                .fillMaxSize()
                .offset(x = offsetX)
                .background(Color(0xFFF4F3ED))
                .clickable(
                    onClick = { onClick() },
                    interactionSource = interactionSource,
                    indication = ripple()
                )
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = {
                            if (isMenuVisible) {
                                isMenuVisible = false
                            } else {
                                onClick()
                            }
                        },
                        onLongPress = {
                            isMenuVisible = !isMenuVisible
                        }
                    )
                }
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .wrapContentSize()
                    .background(color = Color.White, RoundedCornerShape(50.dp))
                    .border(
                        width = 1.dp,
                        color = Color(0xFF9F8772),
                        shape = RoundedCornerShape(50.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = rememberAsyncImagePainter(model = item.imgUrl),
                    contentDescription = "프로필",
                    modifier = Modifier
                        .size(50.dp)
                        .padding(3.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.nickName,
                        fontSize = 15.sp,
                        maxLines = 1,
                        fontWeight = FontWeight.SemiBold,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = item.lastSendMessageTime ?: "",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.lastMessage,
                        fontSize = 13.sp,
                        color = Color(0xFF666666),
                        fontWeight = FontWeight.Normal,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    if (item.nonReadCnt > 0) {
                        Spacer(modifier = Modifier.width(10.dp))
                        Badge(
                            containerColor = Color.Red,
                            contentColor = Color.White,
                            modifier = Modifier.wrapContentSize(),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = item.nonReadCnt.toString(),
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


//@Preview(showBackground = true)
//@Composable
//fun ChatListPreview() {
//    Box(
//        modifier = Modifier.background(color = Color(0xFFF4F3ED))
//    ) {
//        ChatListElementItem(
//            ChatListItem(
//                nickName = "윤성준",
//                lastActivatedTime = "ㅇㅇ",
//                isOnline = true,
//                imgUrl = "",
//                chatRoomId = 7,
//                chatRoomStringId = "7",
//                lastMessage = "dfs\nsfsf\nsfs",
//                lastSendMessageTime = "",
//                nonReadCnt = 400,
//                blocked = false,
//                userId = 3,
//                deleted = false,
//            )
//        ) { }
//    }
//
//}

@Preview(showBackground = true)
@Composable
fun ChatListPreview2() {
    NoticeBanner()
}


