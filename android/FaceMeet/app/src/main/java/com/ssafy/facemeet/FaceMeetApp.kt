package com.ssafy.facemeet

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.ssafy.facemeet.client.ui.theme.FaceMeetTheme
import com.ssafy.facemeet.navigation.MainNavHost

@Composable
fun FaceMeetApp(
    startDestination: String
){
    val navController = rememberNavController()
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        MainNavHost(
            navController = navController,
            startDestination = startDestination
        )
    }

}

@Preview(showBackground = true)
@Composable
fun previewFaceMeetApp(){
    FaceMeetTheme {  }
}