package com.mivideo.mifm.ui.card

import android.content.Context
import android.widget.LinearLayout
import android.widget.TextView
import com.mivideo.mifm.R
import com.mivideo.mifm.data.models.AudioInfo
import com.mivideo.mifm.data.models.jsondata.AlbumInfo
import com.mivideo.mifm.data.models.jsondata.PassageItem
import com.mivideo.mifm.events.DetailPassageClickedEvent
import com.mivideo.mifm.player.manager.DataContainer
import com.mivideo.mifm.ui.widget.DownloadView
import com.mivideo.mifm.util.app.postEvent
import org.jetbrains.anko.onClick

class PassageItemCard(context: Context) : MCard(context) {
    private lateinit var title: TextView
    private lateinit var time: TextView
    private lateinit var downloadView: DownloadView
    private lateinit var titleLayoutParams: LinearLayout.LayoutParams

    override fun init() {
        rootViews = mInflater?.inflate(R.layout.item_passage, null)
        title = rootViews!!.findViewById(R.id.title)
        time = rootViews!!.findViewById(R.id.time)
        downloadView = rootViews!!.findViewById(R.id.download)

        titleLayoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        titleLayoutParams.marginStart = mContext!!.resources.getDimensionPixelSize(R.dimen.media_detail_fragment_passage_margin_start)
        titleLayoutParams.marginEnd = mContext!!.resources.getDimensionPixelSize(R.dimen.media_detail_fragment_passage_margin_end)
    }

    fun setData(data: PassageItem, albumInfo: AlbumInfo?, position: Int) {
        if (albumInfo == null) {
            downloadView.isClickable = false
        } else {
            downloadView.isClickable = true
            downloadView.setCurrentAudioInfo(AudioInfo(albumInfo, data))
        }
        title.layoutParams = titleLayoutParams // RecyclerView item布局复用，导致title的宽度有问题
        title.text = data.name
        time.text = data.duration
        rootViews?.onClick {
            postEvent(DetailPassageClickedEvent(data.id, position))
        }
        if (DataContainer.item != null && data.id.equals(DataContainer.item!!.id)) {
            title.setTextColor(mContext!!.resources.getColor(R.color.themeColor))
        } else {
            title.setTextColor(mContext!!.resources.getColor(R.color.text_dark_black))
        }
    }

}
