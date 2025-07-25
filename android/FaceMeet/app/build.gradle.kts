import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

val localProperties = Properties().apply {
    load(rootProject.file("local.properties").inputStream())
}
val kakaoNativeAppKey = localProperties["kakao_native_app_key"] as String


android {
    namespace = "com.ssafy.facemeet"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.ssafy.facemeet"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        manifestPlaceholders["kakaoScheme"] = "kakao$kakaoNativeAppKey"
        buildConfigField("String", "KAKAO_NATIVE_APP_KEY", "\"$kakaoNativeAppKey\"")
    }


    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }

    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {

    implementation(projects.client)
    implementation(projects.admin)
    implementation(projects.core)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    ksp(libs.hilt.android.compiler)
    implementation(libs.hilt.android)

    implementation(libs.androidx.navigation.compose) // 네비게이션
    implementation("androidx.core:core-splashscreen:1.0.1")

    implementation("com.kakao.sdk:v2-user:2.21.5")
    implementation("com.kakao.sdk:v2-common:2.21.5")


}