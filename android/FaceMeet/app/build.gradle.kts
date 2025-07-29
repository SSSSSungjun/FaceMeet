import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

val localPropertiesFile = rootProject.file("local.properties")
val localProperties = Properties()
if (localPropertiesFile.exists()) {
    localProperties.load(localPropertiesFile.inputStream())
}

val kakaoNativeKey = localProperties.getProperty("kakao_native_app_key")
    ?: error("kakao_native_app_key not found in local.properties")
val kakaoScheme = "kakao$kakaoNativeKey"

val baseUrl =
    localProperties.getProperty("BASE_URL") ?: error("BASE_URL not found in local.properties")

val googleMapAppKey = localProperties.getProperty("google_map_app_key") ?:error("googleMapAppKey Unknown Error")

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
        manifestPlaceholders["kakaoScheme"] = kakaoScheme
        manifestPlaceholders["googleMapApiKey"] = googleMapAppKey

        buildConfigField("String", "KAKAO_NATIVE_APP_KEY", "\"$kakaoNativeKey\"")
        buildConfigField("String", "BASE_URL", "\"$baseUrl\"")
        buildConfigField("String", "MAPS_API_KEY", "\"$googleMapAppKey\"")
//        resValue("string", "kakao_app_key", kakaoNativeKey)
//        resValue("string", "kakao_scheme", kakaoScheme)
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

    implementation("androidx.webkit:webkit:1.14.0")
    implementation("com.google.code.gson:gson:2.11.0")





}