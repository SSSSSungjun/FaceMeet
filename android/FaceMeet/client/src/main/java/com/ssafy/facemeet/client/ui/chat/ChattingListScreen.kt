package com.ssafy.facemeet.client.ui.chat

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Badge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ssafy.facemeet.client.R

data class ChatItem(
    val id: String,
    val name: String,
    val lastMessageTime: String,
    val lastMessage: String,
    val unreadCount: Int,
    val profileImageUrl: String? = null
)


@Composable
fun ChattingListScreen(
    chatItems: List<ChatItem> = dummyList, //UI보려고
    onItemClick: (ChatItem) -> Unit
) {
    Column(
        modifier=Modifier.fillMaxSize()
    ){
        Text(
            modifier = Modifier.fillMaxWidth().padding(10.dp),
            text="채팅 목록",
            fontSize = 15.sp
        )
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(chatItems) { item ->
                ChatListItem(item = item, onClick = { onItemClick(item) })
            }
        }
    }

}

// 2. 아이템 UI
@Composable
fun ChatListItem(
    item: ChatItem,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = R.drawable.tmp_profile_chat ),
            contentDescription = "프로필",
            modifier = Modifier.size(48.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = item.name,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = item.lastMessageTime,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = item.lastMessage,
                    fontSize = 14.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                if (item.unreadCount > 0) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Badge(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White
                    ) {
                        Text(text = item.unreadCount.toString())
                    }
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun ChatListPreview() {
    ChattingListScreen(chatItems = dummyList) {
        // 클릭 시 처리 (예: 채팅방 이동)
    }
}

val dummyList = listOf(
    ChatItem(
        id = "1",
        profileImageUrl = "https://randomuser.me/api/portraits/men/1.jpg",
        name = "김철수",
        lastMessageTime = "오후 2:14",
        lastMessage = "오늘 저녁에 시간 괜찮으세요? 확인 부탁드려요~",
        unreadCount = 3
    ),
    ChatItem(
        id = "2",
        profileImageUrl = null,
        name = "이영희",
        lastMessageTime = "어제",
        lastMessage = "감사합니다 :)",
        unreadCount = 0
    )
)