package com.ssafy.facemeet.client.ui.map

import android.Manifest
import android.app.Activity
import android.location.Geocoder
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

private const val TAG = "MapScreen"

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MapScreen(
    toBack: () -> Unit = {},
    toAccept: () -> Unit = {},
    viewModel: MapViewModel = hiltViewModel(),
) {
    val mapData = viewModel.mapDataStore
    val navigationEvent by viewModel.naviEvent.collectAsStateWithLifecycle(null)
    val scope = rememberCoroutineScope()

    LaunchedEffect(navigationEvent) {
        when (navigationEvent) {
            MapNaviEvent.ToBack -> {
                Log.d(TAG, "MapScreen: toBack")
                toBack()
            }
            MapNaviEvent.ToAccept -> {
                Log.d(TAG, "MapScreen: toAccept")
                toAccept()
            }
            null -> {}
        }
    }

    val context = LocalContext.current
    val mapKey = viewModel.apiKey
    LaunchedEffect(Unit) {
        if (!Places.isInitialized()) {
            Places.initialize(context.applicationContext, mapKey)
        }
    }

    // Geocoder 초기화
    val geocoder = remember {
        if (Geocoder.isPresent()) Geocoder(context, Locale.getDefault()) else null
    }

    // 위도/경도를 실제 주소로 변환하는 함수
    suspend fun getAddressFromLatLng(latLng: LatLng): String {
        return withContext(Dispatchers.IO) {
            try {
                geocoder?.getFromLocation(latLng.latitude, latLng.longitude, 1)?.let { addresses ->
                    if (addresses.isNotEmpty()) {
                        addresses[0].getAddressLine(0) ?: "주소를 찾을 수 없음"
                    } else {
                        "주소를 찾을 수 없음"
                    }
                } ?: "주소 변환 불가"
            } catch (e: Exception) {
                Log.e(TAG, "주소 변환 실패", e)
                "주소 변환 실패"
            }
        }
    }

    val permissionState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    // 권한 요청 시작
    LaunchedEffect(Unit) {
        permissionState.launchMultiplePermissionRequest()
    }

    // 권한 체크
    if (!permissionState.allPermissionsGranted) {
        Text("위치 권한이 필요합니다.")
        return
    }

    // 권한 승인됨
    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    var markerPosition by remember { mutableStateOf(LatLng(37.5665, 126.9780)) }
    var markerTitle by remember { mutableStateOf("현재 위치") }
    var markerSnippet by remember { mutableStateOf("여기가 당신 위치!") }
    var isLocationSelected by remember { mutableStateOf(false) }
    var currentSelectedAddress by remember { mutableStateOf("") }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(markerPosition, 14f)
    }

    LaunchedEffect(Unit) {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let { loc ->
                val newPos = LatLng(loc.latitude, loc.longitude)
                markerPosition = newPos
                cameraPositionState.position = CameraPosition.fromLatLngZoom(newPos, 15f)

                scope.launch {
                    val currentAddress = getAddressFromLatLng(newPos)

                    mapData.setLocation(
                        lat = loc.latitude,
                        lng = loc.longitude,
                        addr = currentAddress
                    )
                    markerTitle = "현재 위치"
                    markerSnippet = currentAddress
                    currentSelectedAddress = currentAddress
                    isLocationSelected = true

                    Log.d(TAG, "현재 위치 즉시 저장됨: $currentAddress")
                }
            }
        }
    }

    val placePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val intent = result.data
            intent?.let {
                val place = Autocomplete.getPlaceFromIntent(intent)
                val latLng = place.location
                val address = place.formattedAddress
                val placeName = place.name

                if (latLng != null && address != null) {
                    markerPosition = latLng
                    markerTitle = placeName ?: "선택된 장소"
                    markerSnippet = address
                    currentSelectedAddress = address
                    isLocationSelected = true
                    cameraPositionState.position = CameraPosition.fromLatLngZoom(latLng, 15f)

                    mapData.setLocation(
                        lat = latLng.latitude,
                        lng = latLng.longitude,
                        addr = "$address ($placeName)"
                    )

                    Log.d(TAG, "장소 즉시 저장됨: $placeName, $address")
                }
            }
        }
    }

    fun openPlaceSearch() {
        val fields = listOf(
            Place.Field.ID,
            Place.Field.NAME,
            Place.Field.LAT_LNG,
            Place.Field.ADDRESS
        )
        val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
            .build(context)
        placePickerLauncher.launch(intent)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
    ) {
        GoogleMap(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = true),
            onMapClick = { latLng ->
                markerPosition = latLng
                markerTitle = "선택된 위치"
                isLocationSelected = true

                scope.launch {
                    val clickAddress = getAddressFromLatLng(latLng)

                    markerSnippet = clickAddress
                    currentSelectedAddress = clickAddress

                    mapData.setLocation(
                        lat = latLng.latitude,
                        lng = latLng.longitude,
                        addr = clickAddress
                    )
                    Log.d(TAG, "누르기 즉시 저장됨: $clickAddress ")
                }
            }
        ) {
            Marker(
                state = MarkerState(position = markerPosition),
                title = markerTitle,
                snippet = markerSnippet,
            )
        }

        if (currentSelectedAddress.isNotEmpty()) {
            Text(
                text = "선택된 주소: $currentSelectedAddress",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                color = Color(0xFF2196F3)
            )
        } else {
            Text(
                text = "주소를 선택해주세요",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                color = Color.Gray
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(Alignment.CenterVertically)
        ) {
            Button(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp),
                onClick = { openPlaceSearch() }
            ) {
                Text("검색")
            }

            Button(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp),
                onClick = {
                    Log.d(TAG, "확인 버튼 클릭 - 선택된 주소: ${mapData.address}")
                    Log.d(TAG, "확인 버튼 클릭 - 선택된 위도: ${mapData.latitude}")
                    Log.d(TAG, "확인 버튼 클릭 - 선택된 경도: ${mapData.longitude}")
                    viewModel.navigateToAccept()
                },
                enabled = isLocationSelected
            ) {
                Text("확인")
            }

            Button(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp),
                onClick = {
                    viewModel.navigateToBack()
                }
            ) {
                Text("취소")
            }
        }
    }


}
