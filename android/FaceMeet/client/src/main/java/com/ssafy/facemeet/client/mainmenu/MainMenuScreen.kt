package com.ssafy.facemeet.client.mainmenu

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp

private const val TAG = "MainMenuScreen"

@Composable
fun MainMenuScreen(
    onNavigateBack: ()-> Unit
) {
    Column(
        modifier=Modifier.fillMaxSize()
    ){
        Text(
            text="tst",
            fontSize = 30.sp
        )
    }
}