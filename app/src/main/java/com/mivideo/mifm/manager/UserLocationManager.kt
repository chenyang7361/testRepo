package com.mivideo.mifm.manager

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Base64
import com.mivideo.mifm.SpManager
import timber.log.Timber

/**
 * 用户地理位置管理工具类
 * Created by yamlee on 25/05/2017.
 *
 * @LiYan
 */
class UserLocationManager(val context: Context) {
    private val locationManager: LocationManager =
            context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private val sp: SpManager = SpManager(context);


    var locationListener: LocationListener = object : LocationListener {

        override fun onLocationChanged(location: Location) {
            val latitude = location.latitude
            val longitude = location.longitude
            Timber.i("receive location....altitude:$latitude" +
                    " longitude:$longitude")
            sp.latitude = latitude.toString()
            sp.longitude = longitude.toString()
        }

        override fun onStatusChanged(provider: String, status: Int, extras: Bundle?) {}

        override fun onProviderEnabled(provider: String) {}

        override fun onProviderDisabled(provider: String) {}
    }

    @SuppressLint("MissingPermission")
    fun location() {
        Timber.i("start location.....")
        locationManager.allProviders?.forEach {
            Timber.i("location provider:" + it)
            if (LocationManager.NETWORK_PROVIDER == it) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                        0L, 0F, locationListener)
            }
        }
    }

    fun stopLocation() {
        Timber.i("stop location....")
        locationManager.removeUpdates(locationListener)
    }

    /**
     * 获取经过Base64加密过的缓存经度
     */
    fun getSecureLongitude(): String {
        val longitude = getLongitude()
        return Base64.encodeToString(longitude.toByteArray(charset("UTF-8")), Base64.DEFAULT)
    }

    /**
     * 获取经过Base64加密过的缓存维度
     */
    fun getSecureLatitude(): String {
        val latitude = getLatitude()
        return Base64.encodeToString(latitude.toByteArray(charset("UTF-8")), Base64.DEFAULT)
    }

    /**
     * 获取缓存经度
     */
    fun getLongitude(): String {
        return sp.longitude
    }

    /**
     * 获取缓存维度
     */
    fun getLatitude(): String {
        return sp.latitude
    }
}
