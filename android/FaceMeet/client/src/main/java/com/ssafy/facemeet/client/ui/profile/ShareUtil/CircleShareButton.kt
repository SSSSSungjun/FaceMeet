package com.ssafy.facemeet.client.ui.profile.ShareUtil

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun CircleShareButton(
    @DrawableRes iconResId: Int,
    contentDescription: String,
    isPng: Boolean,             // PNG면 틴트 X
    onClick: () -> Unit,
    enabled: Boolean = true,    // (옵션) 외부에서 비활성화 제어하고 싶을 때
    iconSize: Dp = 33.dp,
    circleSize: Dp = 58.dp
) {
    Box(
        modifier = Modifier
            .size(circleSize)
            .clip(CircleShape)
            .background(Color.White)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        val painter = painterResource(id = iconResId)
        if (isPng) {
            Image(
                painter = painter,
                contentDescription = contentDescription,
                modifier = Modifier.size(iconSize)
            )
        } else {
            Image(
                painter = painter,
                contentDescription = contentDescription,
                modifier = Modifier.size(iconSize),
                colorFilter = ColorFilter.tint(Color(0xFF5B513F))
            )
        }
    }
}
