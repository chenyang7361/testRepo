package com.mivideo.mifm.extensions

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet

inline fun <reified T : Enum<T>> AttributeSet.getEnum(name: String, defaultValue: T): T {
    val value = getAttributeValue(null, "initialState") ?: return defaultValue
    return java.lang.Enum.valueOf(T::class.java, value)
}

fun AttributeSet.getDimension(context: Context, name: String, defValue: Float): Float {
    try {
        val s = getAttributeValue(null, name)
        val id = if (s.startsWith("android:")) {
            context.resources.getIdentifier(s.substring("android:".length), "id", "android")
        } else {
            context.resources.getIdentifier(s, "id", context.packageName)
        }
        return context.resources.getDimension(id)
    } catch (e: Exception) {
        return defValue
    }
}

fun AttributeSet.getDimensionPixelOffset(context: Context, name: String, defValue: Int): Int {
    try {
        val s = getAttributeValue(null, name)
        val id = if (s.startsWith("android:")) {
            context.resources.getIdentifier(s.substring("android:".length), "id", "android")
        } else {
            context.resources.getIdentifier(s, "id", context.packageName)
        }
        return context.resources.getDimensionPixelOffset(id)
    } catch (e: Exception) {
        return defValue
    }
}

fun AttributeSet.getDimensionPixelSize(context: Context, name: String, defValue: Int): Int {
    try {
        val s = getAttributeValue(null, name)
        if (s.endsWith("dp")) {
            val density = context.resources.displayMetrics.density
            return (s.substringBefore("dp").toFloat() * density).toInt()
        }
        val id = if (s.startsWith("android:")) {
            context.resources.getIdentifier(s.substring("android:".length), "id", "android")
        } else {
            context.resources.getIdentifier(s, "id", context.packageName)
        }
        return context.resources.getDimensionPixelSize(id)
    } catch (e: Exception) {
        return defValue
    }
}

fun AttributeSet.getInt(context: Context, name: String, defValue: Int): Int {
    try {
        return getAttributeIntValue(null, name, defValue)
    } catch (e: Exception) {
        return defValue
    }
}

fun AttributeSet.getBoolean(context: Context, name: String, defValue: Boolean): Boolean {
    try {
        return getAttributeBooleanValue(null, name, defValue)
    } catch (e: Exception) {
        return defValue
    }
}

fun AttributeSet.getFloat(context: Context, name: String, defValue: Float): Float {
    try {
        return getAttributeFloatValue(null, name, defValue)
    } catch (e: Exception) {
        return defValue
    }
}

fun AttributeSet.getColor(context: Context, name: String, defValue: Int): Int {
    try {
        val s = getAttributeValue(null, name)
        return Color.parseColor(s)
    } catch (e: Exception) {
        return defValue
    }
}

fun AttributeSet.getString(context: Context, name: String, defValue: String): String {
    try {
        return getAttributeValue(null, name)
    } catch (e: Exception) {
        return defValue
    }
}

fun AttributeSet.getResourceId(context: Context, name: String, defValue: Int): Int {
    try {
        return getAttributeResourceValue(null, name, defValue)
    } catch (e: Exception) {
        return defValue
    }
}
