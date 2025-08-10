package com.ssafy.facemeet.client.ui.camera

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.ssafy.facemeet.client.R

class ShutterPlayer(context: Context) {
    private val soundPool = SoundPool.Builder()
        .setMaxStreams(1)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        ).build()

    private val shutterId = soundPool.load(context, R.raw.shutter_click, 1)

    fun play(volume: Float = 0.08f) { // 0.0f ~ 1.0f, 아주 작게 기본 0.08
        if (shutterId != 0) soundPool.play(shutterId, volume, volume, 1, 0, 1f)
    }

    fun release() = soundPool.release()
}