package com.ssafy.facemeet.client.ui.mypage

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
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
import com.ssafy.facemeet.client.ui.theme.ChosunCentennial
import com.ssafy.facemeet.core.util.constant.CommonColor

@Composable
fun WithdrawConfirmDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,

        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {

                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "경고",
                    tint = CommonColor.Orange,
                    modifier = Modifier.size(32.dp)
                )

                Text(
                    text = "회원탈퇴",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Medium,
                    fontFamily = ChosunCentennial,
                    textAlign = TextAlign.Center,
                    fontSize = 20.sp,
                    modifier = Modifier
                        .wrapContentSize()
                        .padding(start = 10.dp)
                )
            }


        },
        text = {
            Box(
                modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "정말 탈퇴하시겠어요? \n당신을 원하는 사람들이 있습니다.",
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
                    .clickable { onConfirm() }
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
fun WithDrawPreview() {
    WithdrawConfirmDialog({}, {})
}
