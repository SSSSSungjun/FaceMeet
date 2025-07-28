package com.ssafy.facemeet.client.ui.map

import android.Manifest
import android.app.Activity
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
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

    var currentPosition by remember { mutableStateOf(LatLng(37.5665, 126.9780)) } // 기본 위치: 서울 시청
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(currentPosition, 14f)
    }

    // 위치 불러오기
    LaunchedEffect(Unit) {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                val newPos = LatLng(it.latitude, it.longitude)
                currentPosition = newPos
                cameraPositionState.position = CameraPosition.fromLatLngZoom(newPos, 15f)
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
                if (latLng != null && address != null) {
                    cameraPositionState.position = CameraPosition.fromLatLngZoom(latLng, 15f)

                    mapData.setLocation(
                        lat = latLng.latitude,
                        lng = latLng.longitude,
                        addr = address
                    )

                }
            }
        }
    }


    // 검색 버튼 클릭 시 실행 함수
    fun openPlaceSearch() {
        val fields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS)
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
            properties = MapProperties(isMyLocationEnabled = true)
        ) {
            Marker(
                state = MarkerState(position = currentPosition),
                title = "현재 위치",
                snippet = "여기가 당신 위치!"
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
                    viewModel.navigateToAccept()
                }
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

@Preview(showBackground = true)
@Composable
fun MapScreenPreview() {
    MapScreen()
}
