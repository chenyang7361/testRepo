package com.mivideo.mifm.ui.adapter.detail

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.hannesdorfmann.adapterdelegates2.AdapterDelegate
import com.mivideo.mifm.data.models.jsondata.AlbumInfo
import com.mivideo.mifm.data.models.jsondata.PassageItem
import com.mivideo.mifm.ui.card.PassageItemCard

class PassageItemDelegate : AdapterDelegate<List<PassageItem>> {

    private var albumInfo: AlbumInfo? = null

    fun setCurrentAlbumInfo(albumInfo: AlbumInfo) {
        this.albumInfo = albumInfo
    }

    override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        return PassageItemHolder(PassageItemCard(parent.context))
    }


    override fun isForViewType(items: List<PassageItem>, position: Int): Boolean {
        return true
    }


    override fun onBindViewHolder(items: List<PassageItem>, position: Int, holder: RecyclerView.ViewHolder) {
        val recomHolder = holder as PassageItemHolder
        recomHolder.item.setData(items[position], albumInfo, position)
    }

}

class PassageItemHolder(val item: PassageItemCard) : RecyclerView.ViewHolder(item.getView())
