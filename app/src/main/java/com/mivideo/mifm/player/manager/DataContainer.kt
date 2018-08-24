package com.mivideo.mifm.player.manager

import com.mivideo.mifm.data.models.AudioInfo
import com.mivideo.mifm.data.models.jsondata.AlbumInfo
import com.mivideo.mifm.data.models.jsondata.PassageItem

/**
 * Created by Jiwei Yuan on 18-8-16.
 */
object DataContainer {

    var lastAbulm: AlbumInfo? = null
    var lastItem: PassageItem? = null
    var album: AlbumInfo? = null
    var lastIndex: Int = -1
    var index = 0
    var item: PassageItem? = null
    var items: ArrayList<PassageItem> = ArrayList()

    fun saveLast() {
        lastIndex = index
        lastAbulm = album
        lastItem = if (lastAbulm != null && items.size > 0 && index < items.size && index >= 0) {
            items[index]
        } else {
            null
        }
    }

    fun hasHistory(): Boolean {
        return lastAbulm != null && lastItem != null
    }

    fun getOrderedNext(order: Int): PassageItem? {
        return if (order == ListOrder.NORMAL) {
            getNext()
        } else {
            getLast()
        }
    }

    fun getOrderedLast(order: Int): PassageItem? {
        return if (order == ListOrder.NORMAL) {
            getLast()
        } else {
            getNext()
        }
    }

    private fun getNext(): PassageItem? {
        if (index < 0) {
            return null
        }
        ++index
        return if (index >= items.size) {
            index--
            null
        } else {
            item = items[index]
            item
        }
    }

    private fun getLast(): PassageItem? {
        index--
        return if (index < 0) {
            index = 0
            null
        } else {
            item = items[index]
            item
        }
    }

    private fun addAfter(list: List<PassageItem>) {
        items.addAll(list)
    }

    private fun addBefore(list: List<PassageItem>) {
        items.addAll(0, list)
        index += list.size
    }

    fun getAudioInfo(): AudioInfo {
        val info = AudioInfo()
        info.albumInfo = album!!
        info.passageItem = item!!
        return info
    }

    fun hasData(): Boolean {
        return item != null
    }

    fun hasNext(): Boolean {
        return index < items.size - 1
    }

    fun hasLast(): Boolean {
        return index > 0
    }

    fun addHeaderContent(albumId: String, list: List<PassageItem>) {
        checkAlbum()
        addBefore(list)
    }

    fun addFooterContent(albumId: String, list: List<PassageItem>) {
        checkAlbum()
        addAfter(list)
    }

    private fun checkAlbum() {
        //TODO  检查当前album是否一致
    }
}