package com.mivideo.mifm.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mivideo.mifm.R

/**
 * Created by Jiwei Yuan on 18-7-26.
 */
class DownLoadFragment : BaseRefreshListFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return  inflater.inflate(R.layout.fragment_download, container, false)
    }

    override fun initRefreshView(view: View) {
    }

}

fun createDownLoadFragment(): DownLoadFragment {
    return DownLoadFragment()
}