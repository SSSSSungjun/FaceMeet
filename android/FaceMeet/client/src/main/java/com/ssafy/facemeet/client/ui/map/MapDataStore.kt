package com.ssafy.facemeet.client.ui.map

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MapDataStore @Inject constructor() {
    var address: String? = null
    var latitude: Double? = null
    var longitude: Double? = null

    fun setLocation(lat: Double, lng: Double, addr: String? = null) {
        latitude = lat ?: -1.0
        longitude = lng ?: -1.0
        address = addr ?: ""
    }

    fun clear() {
        address = null
        latitude = null
        longitude = null
    }

    fun hasLocation(): Boolean = latitude != null && longitude != null

}