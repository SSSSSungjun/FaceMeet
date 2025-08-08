package com.ssafy.facemeet.client.ui.profile.partner.dialog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ssafy.facemeet.client.ui.theme.ChosunGongseo
import com.ssafy.facemeet.core.util.constant.CommonColor

@Composable
fun BlockedDialog(
    showDialog: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {

    if(!showDialog) {
        return
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "차단하기",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Medium,
                    fontFamily = ChosunGongseo,
                    textAlign = TextAlign.Center,
                    fontSize = 20.sp,
                    modifier = Modifier
                        .wrapContentSize()

                )
            }
        },
        text = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "채팅방을 나가면 다시는 상대와 만날 수 없습니다.\n  정말 나가시겠습니까?",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
            }
        },

        confirmButton = {
            Text(
                text = "예",
                fontWeight = FontWeight.Medium,
                color = CommonColor.Orange,
                modifier = Modifier
                    .padding(start = 30.dp)
                    .clickable {
                        onConfirm()
                        onDismiss()
                    }
            )
        },
        dismissButton = {
            Text(
                text = "아니요 ",
                fontWeight = FontWeight.Medium,
                modifier = Modifier.clickable { onDismiss() }
            )
        },
        shape = RoundedCornerShape(16.dp),
        containerColor = Color.White
    )
}

@Preview(showBackground = true)
@Composable
fun blockedPreview() {
    BlockedDialog(false, { }, { })
}
