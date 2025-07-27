package com.ssafy.facemeet.client.ui.camera

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun CameraScreen(
    onNavigateBack : () -> Unit = {},
    onLaunchCamera : () -> Unit = {},

) {

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column {
            Button(onClick = onLaunchCamera) {
                Text("click")
            }

            Text("안경을 벗어주세요")

        }

    }
}
