package com.mivideo.mifm.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mivideo.mifm.R
import com.mivideo.mifm.data.models.jsondata.ChannelList
import com.mivideo.mifm.data.models.jsondata.PassageItem
import com.mivideo.mifm.ui.adapter.KRefreshDelegateAdapter
import com.mivideo.mifm.ui.adapter.headline.HeadlineHeaderDelegete
import com.mivideo.mifm.ui.adapter.headline.HeadlineItemDelegate
import com.mivideo.mifm.util.MJson

/**
 * Created by Jiwei Yuan on 18-8-8.
 */
class HeadLineListFragment : BaseRefreshListFragment() {
    private var tabPosition: Int = 0
    private lateinit var tabId: String
    private lateinit var tabName: String
    private lateinit var listAdapter: KRefreshDelegateAdapter<PassageItem>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fm_list_fragment, container, false)
    }

    override fun preInitRefresh() {
        tabId = arguments?.getString("tabId") ?: ""
        tabName = arguments?.getString("tabName") ?: ""
        tabPosition = arguments?.getInt("position") ?: -1
    }

    override fun initRefreshView(view: View) {
        refreshLayout = view.findViewById(R.id.refreshLayout)
        listAdapter = KRefreshDelegateAdapter()
        listAdapter.mDelegatesManager.addDelegate(HeadlineItemDelegate())
                .addDelegate(HeadlineHeaderDelegete())
        refreshLayout?.setAdapter(listAdapter)
    }


    override fun refreshData() {
        val data = mockData().data
//        if (tabPosition == 0) {
//            val header = PassageItem()
//            header.headline = 1
//            data!!.add(0, header)
//        }
//        listAdapter.addDefaultDataList(data!!)
//        refreshLayout?.finishRefresh()
    }

    fun mockData(): ChannelList {
        //TODO delete
        val json = "{" +
                "  \"code\" : 1," +
                "  \"data\" : [" +
                "    {" +
                "      \"id\" : 1243," +
                "      \"title\" : \"这是内容标题1\"," +
                "    \"img\" : \"https://gss2.bdstatic.com/9fo3dSag_xI4khGkpoWK1HF6hhy/baike/s%3D220/sign=5d7eb37d48a7d933bba8e3719d4ad194/86d6277f9e2f07087af5a369e324b899a801f2e3.jpg\"," +
                "      \"desc\" : \"人在江湖拼搏的生死之交是财富，抓住财富财富\"," +
                "      \"ctype\" : 0" +
                "    }," +
                "    {" +
                "      \"id\" : 1244," +
                "      \"desc\" : \"人在江湖拼搏的生死之交是财富，抓住财富财富\"," +
                "      \"title\" : \"这是内容标题2\"," +
                "    \"img\" : \"https://gss2.bdstatic.com/9fo3dSag_xI4khGkpoWK1HF6hhy/baike/s%3D220/sign=5d7eb37d48a7d933bba8e3719d4ad194/86d6277f9e2f07087af5a369e324b899a801f2e3.jpg\"," + "      \"ctype\" : 0" +
                "    }," +
                "    {" +
                "      \"id\" : 1244," +
                "      \"desc\" : \"人在江湖拼搏的生死之交是财富，抓住财富财富\"," +
                "      \"title\" : \"这是内容标题2\"," +
                "    \"img\" : \"https://gss2.bdstatic.com/9fo3dSag_xI4khGkpoWK1HF6hhy/baike/s%3D220/sign=5d7eb37d48a7d933bba8e3719d4ad194/86d6277f9e2f07087af5a369e324b899a801f2e3.jpg\"," + "      \"ctype\" : 0" +
                "    }," +
                "    {" +
                "      \"id\" : 1244," +
                "      \"desc\" : \"人在江湖拼搏的生死之交是财富，抓住财富财富\"," +
                "      \"title\" : \"这是内容标题2\"," +
                "    \"img\" : \"https://gss2.bdstatic.com/9fo3dSag_xI4khGkpoWK1HF6hhy/baike/s%3D220/sign=5d7eb37d48a7d933bba8e3719d4ad194/86d6277f9e2f07087af5a369e324b899a801f2e3.jpg\"," + "      \"ctype\" : 0" +
                "    }," + "    {" +
                "      \"id\" : 1244," +
                "      \"desc\" : \"人在江湖拼搏的生死之交是财富，抓住财富财富\"," +
                "      \"title\" : \"这是内容标题2\"," +
                "    \"img\" : \"https://gss2.bdstatic.com/9fo3dSag_xI4khGkpoWK1HF6hhy/baike/s%3D220/sign=5d7eb37d48a7d933bba8e3719d4ad194/86d6277f9e2f07087af5a369e324b899a801f2e3.jpg\"," + "      \"ctype\" : 0" +
                "    }," +
                "    {" +
                "      \"id\" : 1244," +
                "      \"desc\" : \"人在江湖拼搏的生死之交是财富，抓住财富财富\"," +
                "      \"title\" : \"这是内容标题2\"," +
                "    \"img\" : \"https://gss2.bdstatic.com/9fo3dSag_xI4khGkpoWK1HF6hhy/baike/s%3D220/sign=5d7eb37d48a7d933bba8e3719d4ad194/86d6277f9e2f07087af5a369e324b899a801f2e3.jpg\"," + "      \"ctype\" : 0" +
                "    }," +
                "    {" +
                "      \"id\" : 1244," +
                "      \"desc\" : \"人在江湖拼搏的生死之交是财富，抓住财富财富\"," +
                "      \"title\" : \"这是内容标题2\"," +
                "    \"img\" : \"https://gss2.bdstatic.com/9fo3dSag_xI4khGkpoWK1HF6hhy/baike/s%3D220/sign=5d7eb37d48a7d933bba8e3719d4ad194/86d6277f9e2f07087af5a369e324b899a801f2e3.jpg\"," + "      \"ctype\" : 0" +
                "    }," +
                "    {" +
                "      \"id\" : 1244," +
                "      \"desc\" : \"人在江湖拼搏的生死之交是财富，抓住财富财富\"," +
                "      \"title\" : \"这是内容标题2\"," +
                "    \"img\" : \"https://gss2.bdstatic.com/9fo3dSag_xI4khGkpoWK1HF6hhy/baike/s%3D220/sign=5d7eb37d48a7d933bba8e3719d4ad194/86d6277f9e2f07087af5a369e324b899a801f2e3.jpg\"," + "      \"ctype\" : 0" +
                "    }," +
                "    {" +
                "      \"id\" : 1244," +
                "      \"desc\" : \"人在江湖拼搏的生死之交是财富，抓住财富财富\"," +
                "      \"title\" : \"这是内容标题2\"," +
                "    \"img\" : \"https://gss2.bdstatic.com/9fo3dSag_xI4khGkpoWK1HF6hhy/baike/s%3D220/sign=5d7eb37d48a7d933bba8e3719d4ad194/86d6277f9e2f07087af5a369e324b899a801f2e3.jpg\"," + "      \"ctype\" : 0" +
                "    }," +
                "    {" +
                "      \"id\" : 1244," +
                "      \"desc\" : \"人在江湖拼搏的生死之交是财富，抓住财富财富\"," +
                "      \"title\" : \"这是内容标题2\"," +
                "    \"img\" : \"https://gss2.bdstatic.com/9fo3dSag_xI4khGkpoWK1HF6hhy/baike/s%3D220/sign=5d7eb37d48a7d933bba8e3719d4ad194/86d6277f9e2f07087af5a369e324b899a801f2e3.jpg\"," + "      \"ctype\" : 0" +
                "    }," +
                "    {" +
                "      \"id\" : 1244," +
                "      \"desc\" : \"人在江湖拼搏的生死之交是财富，抓住财富财富\"," +
                "      \"title\" : \"这是内容标题2\"," +
                "    \"img\" : \"https://gss2.bdstatic.com/9fo3dSag_xI4khGkpoWK1HF6hhy/baike/s%3D220/sign=5d7eb37d48a7d933bba8e3719d4ad194/86d6277f9e2f07087af5a369e324b899a801f2e3.jpg\"," + "      \"ctype\" : 0" +
                "    }," +

                "    {" +
                "      \"id\" : 2343," +
                "      \"desc\" : \"人在江湖拼搏的生死之交是财富，抓住财富财富\"," +
                "      \"title\" : \"对白\"," +
                "    \"img\" : \"https://gss2.bdstatic.com/9fo3dSag_xI4khGkpoWK1HF6hhy/baike/s%3D220/sign=5d7eb37d48a7d933bba8e3719d4ad194/86d6277f9e2f07087af5a369e324b899a801f2e3.jpg\"," + "      \"ctype\" : 0" +
                "    }," +
                "    {" +
                "      \"id\" : 2355," +
                "      \"desc\" : \"人在江湖拼搏的生死之交是财富，抓住财富财富\"," +
                "      \"title\" : \"极简主义\"," +
                "    \"img\" : \"https://gss2.bdstatic.com/9fo3dSag_xI4khGkpoWK1HF6hhy/baike/s%3D220/sign=5d7eb37d48a7d933bba8e3719d4ad194/86d6277f9e2f07087af5a369e324b899a801f2e3.jpg\"," + "      \"ctype\" : 0" +
                "    }" +
                "  ]," +
                "  \"stype\" : 3," +
                "  \"next\" : -1" +
                "}"
        val list = MJson.getInstance().fromJson<ChannelList>(json, ChannelList::class.java)
        return list!!
    }


}

fun createHeadlineListFragment(tabId: String, tabName: String, position: Int): HeadLineListFragment {
    val fragment = HeadLineListFragment()
    val bundle = Bundle()
    bundle.putString("tabId", tabId)
    bundle.putString("tabName", tabName)
    bundle.putInt("position", position)
    fragment.arguments = bundle
    return fragment
}