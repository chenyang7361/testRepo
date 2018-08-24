package com.mivideo.mifm.player

import android.app.Activity
import android.content.Context
import android.media.AudioManager
import android.provider.Settings

/**
 * 系统功能帮助类，如调节屏幕亮度，声音大小等
 *
 * @author LiYan
 */
class SystemHelper(val activity: Activity) {
    companion object {
        val BRIGHTNESS_MAX_VALUE = 15
        val BRIGHTNESS_STEP = 255 / BRIGHTNESS_MAX_VALUE
    }

    private var mAudioManager: AudioManager
    var mMaxVolume = 0

    init {
        mAudioManager = activity.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        mMaxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
    }


    fun getBrightness(): Int {
        var currentValue = getActivityBrightness(activity) * 255
        if (currentValue < 0) {
            currentValue = getSystemBrightness(activity).toFloat()
        }
        return currentValue.toInt()
    }

    //0~255
    private fun getSystemBrightness(context: Context): Int {
        var result = 0
        val cr = context.contentResolver
        try {
            result = Settings.System.getInt(cr, Settings.System.SCREEN_BRIGHTNESS)
        } catch (e: Settings.SettingNotFoundException) {
            e.printStackTrace()
        }

        return result
    }

    private fun getActivityBrightness(activity: Activity): Float {
        val params = activity.window.attributes
        return params.screenBrightness
    }

    fun getNewBrightnessValue(distanceY: Float): Int {
        val currentValue = getBrightness()
        var newValue = 0
        if (distanceY > 0) {
            newValue = currentValue - BRIGHTNESS_STEP
        } else if (distanceY < 0) {
            newValue = currentValue + BRIGHTNESS_STEP
        } else {
            newValue = currentValue
        }
        if (newValue > 255) {
            newValue = 255
        }
        if (newValue < 2) {
            newValue = 2
        }
        return newValue
    }

    fun setBrightness(movementY: Float) {
        val newValue = getNewBrightnessValue(movementY)
        val params = activity.window.attributes
        params.screenBrightness = newValue / 255f
        activity.window.attributes = params
    }

    fun getNewVolumeValue(distanceY: Float): Int {
        val currentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        val maxVolume = mMaxVolume
        var newValue = currentVolume
        if (distanceY > 0) {
            newValue = currentVolume - 1
        } else if (distanceY < 0) {
            newValue = currentVolume + 1
        }
        if (newValue > maxVolume) {
            newValue = maxVolume
        }
        if (newValue < 0) {
            newValue = 0
        }
        return newValue
    }

    fun setNewVolumeValue(movementY: Float) {
        val newValue = getNewVolumeValue(movementY)
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newValue, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE)
    }

}