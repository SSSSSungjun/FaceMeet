package com.ssafy.facemeet.client.ui.block

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ssafy.facemeet.client.R
import com.ssafy.facemeet.client.ui.block.dialog.UnblockDialog
import com.ssafy.facemeet.client.ui.theme.ChosunCentennial
import com.ssafy.facemeet.core.util.constant.CommonColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyBlockListScreen(
    onBack: () -> Unit = {},
    vm: BlockListViewModel = hiltViewModel()
) {
    val items by vm.items.collectAsState()
    val loading by vm.loading.collectAsState()
    val error by vm.error.collectAsState()
    val snack = remember { SnackbarHostState() }

    // 다이얼로그 상태
    var showDialog by remember { mutableStateOf(false) }
    var pendingUnblockUserId by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(error) { if (error != null) snack.showSnackbar(error!!) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "차단목록",
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = ChosunCentennial,
                        color = CommonColor.Gray900,
                        fontSize = 16.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(R.drawable.ic_back),
                            contentDescription = "뒤로가기"
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snack) },
        modifier = Modifier.background(color = CommonColor.Beige100)
    ) { innerPadding ->
        when {
            loading -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }

            items.isEmpty() -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) { Text("차단한 사용자가 없습니다", color = Color.Gray) }

            else -> LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                itemsIndexed(items) { idx, block ->
                    BlockRow(
                        nickname = block.nickName, // 매퍼에서 이미 "탈퇴한 사용자" 처리됨
                        onUnblockClick = {
                            pendingUnblockUserId = block.userId.toLong()
                            showDialog = true
                        }
                    )
                    if (idx != items.lastIndex) {
                        Divider(color = Color(0xFFF2F2F2))
                    }
                }
            }
        }
    }

    // 차단해제 확인 다이얼로그 (BlockedDialog와 동일 톤)
    UnblockDialog(
        showDialog = showDialog,
        onConfirm = {
            pendingUnblockUserId?.let { vm.unblock(it) }
        },
        onDismiss = {
            showDialog = false
            pendingUnblockUserId = null
        }
    )
}

@Composable
private fun BlockRow(
    nickname: String,
    onUnblockClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Color(0xFFF7F4EE)),
            contentAlignment = Alignment.Center
        ) {
            // drawable 아이콘 사용
            Image(
                painter = painterResource(id = R.drawable.temp_face),
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(Modifier.width(12.dp))
        Text(
            text = nickname, // name 절대 사용 안 함
            modifier = Modifier.weight(1f),
            color = CommonColor.Gray900,
            fontSize = 14.sp
        )
        Text(
            "차단해제",
            modifier = Modifier
                .clickable { onUnblockClick() }
                .padding(horizontal = 12.dp, vertical = 6.dp),
            color = CommonColor.Gray400,
            fontSize = 12.sp
        )
    }
}