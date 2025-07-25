package com.ssafy.facemeet.client.ui.camera

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class CameraShotViewModel : ViewModel() {
    private val _front = MutableStateFlow<Bitmap?>(null)
    val front: StateFlow<Bitmap?> = _front

    private val _side = MutableStateFlow<Bitmap?>(null)
    val side: StateFlow<Bitmap?> = _side

    fun setFront(b: Bitmap) {
        _front.value = b
    }

    fun setSide(b: Bitmap) {
        _side.value = b
    }

    fun resetFront() {
        _front.value = null
    }

    fun resetSide() {
        _side.value = null
    }

    fun readyBoth() = _front.value != null && _side.value != null
}
