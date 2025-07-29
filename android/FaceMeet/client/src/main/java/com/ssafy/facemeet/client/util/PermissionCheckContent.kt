package com.ssafy.facemeet.client.util

import android.Manifest
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionCheckContent(
    onGranted: @Composable () -> Unit,
    onDenied: @Composable () -> Unit = { DefaultPermissionDeniedView() }
) {
    val permissionState = rememberMultiplePermissionsState(
        listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    // 권한 요청
    LaunchedEffect(Unit) {
        permissionState.launchMultiplePermissionRequest()
    }

    if (permissionState.allPermissionsGranted) {
        onGranted()
    } else {
        onDenied()
    }
}

@Composable
fun DefaultPermissionDeniedView() {
    Text("위치 권한이 필요합니다.")
}