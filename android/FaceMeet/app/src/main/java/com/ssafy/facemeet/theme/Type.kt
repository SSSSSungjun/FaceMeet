package com.ssafy.facemeet.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp

// Set of Material typography styles to start with
val CustomFontFamily = FontFamily(
    Font(com.ssafy.facemeet.client.R.font.roboto),
)

val Typography = Typography(
    displayLarge = TextStyle(fontFamily = CustomFontFamily),
    displayMedium = TextStyle(fontFamily = CustomFontFamily),
    displaySmall = TextStyle(fontFamily = CustomFontFamily),
    headlineLarge = TextStyle(fontFamily = CustomFontFamily),
    headlineMedium = TextStyle(fontFamily = CustomFontFamily),
    headlineSmall = TextStyle(fontFamily = CustomFontFamily),
    titleLarge = TextStyle(fontFamily = CustomFontFamily),
    titleMedium = TextStyle(fontFamily = CustomFontFamily, fontSize = 20.sp),
    titleSmall = TextStyle(fontFamily = CustomFontFamily),
    bodyLarge = TextStyle(fontFamily = CustomFontFamily),
    bodyMedium = TextStyle(fontFamily = CustomFontFamily),
    bodySmall = TextStyle(fontFamily = CustomFontFamily),
    labelLarge = TextStyle(fontFamily = CustomFontFamily),
    labelMedium = TextStyle(fontFamily = CustomFontFamily),
    labelSmall = TextStyle(fontFamily = CustomFontFamily),
)
