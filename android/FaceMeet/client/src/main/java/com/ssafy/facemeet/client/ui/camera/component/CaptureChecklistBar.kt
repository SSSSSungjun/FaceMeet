package com.ssafy.facemeet.client.ui.camera.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CaptureChecklistBar(
    frontDone: Boolean,
    sideDone: Boolean,
    modifier: Modifier = Modifier
) {
    Row(modifier.padding(12.dp)) {

        Icon(
            imageVector = if (frontDone) Icons.Filled.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
            contentDescription = null,
            tint = if (frontDone) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
        Text(" 정면", modifier = Modifier.padding(end = 16.dp))

        Icon(
            imageVector = if (sideDone) Icons.Filled.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
            contentDescription = null,
            tint = if (sideDone) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
        Text(" 옆면")
    }
}
