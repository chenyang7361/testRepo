package com.mivideo.mifm.download.support

class Size {
    private var value = 0L

    fun getValue(): Long {
        return value
    }

    fun setValue(v: Long) {
        var va = v
        if (va < 0) {
            va = 0
        }
        value = va
    }

    fun getStringSize(): String {
        return value.toString()
    }

    fun getKSize(): Double {
        return value.toDouble() / 1024.0
    }

    fun getMSize(): Double {
        return value.toDouble() / 1024.0 / 1024.0
    }
}