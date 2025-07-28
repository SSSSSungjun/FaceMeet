package com.ssafy.facemeet.client.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class MapViewModel @Inject constructor(
    @Named("googleMapsApiKey") val apiKey: String,
    val mapDataStore: MapDataStore
) : ViewModel() {

    private val _naviEvent = MutableSharedFlow<MapNaviEvent?>()
    val naviEvent: SharedFlow<MapNaviEvent?> = _naviEvent.asSharedFlow()

    fun navigateToAccept() {
        viewModelScope.launch {
            _naviEvent.emit(MapNaviEvent.ToAccept)
        }
    }

    fun navigateToBack() {
        viewModelScope.launch {
            _naviEvent.emit(MapNaviEvent.ToBack)
        }
    }

}
