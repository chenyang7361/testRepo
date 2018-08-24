package com.mivideo.mifm.ui.fragment.home

import android.support.v7.widget.RecyclerView
import com.mivideo.mifm.data.models.jsondata.RecommendData
import com.mivideo.mifm.data.models.jsondata.RecommendList
import com.mivideo.mifm.rx.asyncSchedulers
import com.mivideo.mifm.ui.adapter.KRefreshDelegateAdapter
import com.mivideo.mifm.ui.adapter.homelist.RecommendStyle1Delegate
import com.mivideo.mifm.ui.adapter.homelist.RecommendStyle2Delegate
import com.mivideo.mifm.ui.adapter.homelist.RecommendStyle3Delegate
import com.mivideo.mifm.util.MJson
import com.mivideo.mifm.viewmodel.RecommendViewModel
import com.trello.rxlifecycle.FragmentEvent
import timber.log.Timber

class RecommendMediaListDelegate(val fragment: MediaListFragment) : IMediaListDelegate {

    private var recommendListAdapter = KRefreshDelegateAdapter<RecommendData>()
    private var recommendViewModel: RecommendViewModel = RecommendViewModel(fragment.context)

    init {
        fragment.refreshLayout?.isEnableLoadmore = false
        recommendListAdapter.mDelegatesManager
                .addDelegate(RecommendStyle1Delegate())
                .addDelegate(RecommendStyle2Delegate())
                .addDelegate(RecommendStyle3Delegate())
    }

    override fun refreshData() {
        recommendViewModel.loadRecommendData()
                .compose(asyncSchedulers())
                .compose(fragment.bindUntilEvent(FragmentEvent.DESTROY_VIEW))
                .subscribe({
                    fragment.refreshLayout?.finishRefresh()
                    val data = it.data as ArrayList<RecommendData>
                    Timber.i("init refresh get data from db size: ${data.size}")
                    recommendListAdapter.dataList.clear()
                    if (data != null && !data.isEmpty()) {
                        recommendListAdapter.addHeaderItems(data)
                        recommendViewModel.saveRefreshDataToDb(data)
                        fragment.hideTipView()
                    } else {
                        fragment.showEmpty()
                    }
                }, {
                    fragment.refreshLayout?.finishRefresh()
                    fragment.showLoadFail()
                })
//        //TODO delete
//        val data = mockData()
//        recommendListAdapter.dataList.clear()
//        recommendListAdapter.addDefaultDataList(data)
//        recommendViewModel.saveRefreshDataToDb(data)
//        fragment.refreshLayout?.finishRefresh()
    }

    override fun loadDataFromCache() {
        fragment.refreshLayout?.showHeader(false)
        Timber.i("init  refresh list from database")
        recommendViewModel.loadRefreshDataFromDb()
                .compose(fragment.bindUntilEvent(FragmentEvent.DESTROY_VIEW))
                .compose(asyncSchedulers())
                .subscribe(
                        {
                            val data = it as java.util.ArrayList<RecommendData>
                            Timber.i("init refresh get data from db size: ${data.size}")
                            if (data != null && !data.isEmpty()) {
                                recommendListAdapter.addHeaderItems(data)
                            } else {
                                fragment.showNetUnconnected()
                            }
                        },
                        {
                            Timber.i("init refresh get data from db error")
                            fragment.showNetUnconnected()
                        }
                )
    }

    override fun getAdapter(): RecyclerView.Adapter<RecyclerView.ViewHolder> {
        return recommendListAdapter
    }

    override fun loadMore() {
        //nothing need here
    }
}

fun mockData(): ArrayList<RecommendData> {
    //TODO delete
    val json = "{" +
            "  \"code\" : 1," +
            "  \"data\" : [" +
            "    {" +
            "      \"has_more\" : 0," +
            "      \"name\" : \"听头条\"," +
            "      \"atype\" : 2," +
            "      \"list\" : [" +
            "        {" +
            "          \"id\" : 1243," +
            "          \"title\" : \"这是内容标题1\"," +
            "          \"url\" : \"http:\\/\\/test.com\\/mp3\"," +
            "          \"from_now\" : \"27分钟前\", " +
            "          \"ctype\" : 0" +
            "        }," +
            "        {" +
            "          \"id\" : 1244," +
            "          \"title\" : \"这是内容标题2\"," +
            "          \"url\" : \"http:\\/\\/test.com\\/mp3\"," +
            "          \"from_now\" : \"27分钟前\", " +
            "          \"ctype\" : 0" +
            "        }" +
            "      ]," +
            "      \"stype\" : 1" +
            "    }," +
            "    {" +
            "      \"has_more\" : 23," +
            "      \"name\" : \"热门小说\"," +
            "      \"list\" : [" +
            "        {" +
            "          \"id\" : 2343," +
            "          \"author\" : \"李雪教授\"," +
            "          \"title\" : \"对白\"," +
            "      \"desc\" : \"人在江湖拼搏的生死之交是财富，抓住财富财富,人在江湖拼搏的生死之交是财富，抓住财富财富人在江湖拼搏的生死之交是财富，抓住财富财富\"," +
            "          \"img\" : \"https:\\/\\/gss3.bdstatic.com\\/-Po3dSag_xI4khGkpoWK1HF6hhy\\/baike\\/w%3D268%3Bg%3D0\\/sign=c490620e9516fdfad86cc1e88cb4eb69\\/a08b87d6277f9e2f6477842d1730e924b899f351.jpg\"," +
            "          \"ctype\" : 1" +
            "        }," +
            "        {" +
            "          \"id\" : 2355," +
            "          \"title\" : \"极简主义\"," +
            "          \"author\" : \"李雪教授\"," +
            "      \"desc\" : \"人在江湖拼搏的生死之交是财富，抓住财富财富,人在江湖拼搏的生死之交是财富，抓住财富财富人在江湖拼搏的生死之交是财富，抓住财富财富\"," +
            "          \"img\" : \"http:\\/\\/test.com\\/images\"," +
            "          \"ctype\" : 1" +
            "        }" +
            "      ]," +
            "      \"stype\" : 2" +
            "    }," +
            "    {" +
            "      \"has_more\" : 25," +
            "      \"name\" : \"心理学\"," +
            "      \"list\" : [" +
            "        {" +
            "          \"id\" : 2473," +
            "          \"title\" : \"12天学会困境中自我疗愈\"," +
            "          \"author\" : \"作者\"," +
            "      \"desc\" : \"人在江湖拼搏的生死之交是财富，抓住财富财富\"," +
            "    \"img\" : \"https:\\/\\/gss3.bdstatic.com\\/-Po3dSag_xI4khGkpoWK1HF6hhy\\/baike\\/w%3D268%3Bg%3D0\\/sign=c490620e9516fdfad86cc1e88cb4eb69\\/a08b87d6277f9e2f6477842d1730e924b899f351.jpg\"," +
            "          \"ctype\" : 1" +
            "        }," +
            "        {" +
            "          \"id\" : 2475," +
            "          \"title\" : \"心理学入门\"," +
            "          \"author\" : \"作者\"," +
            "      \"desc\" : \"人在江湖拼搏的生死之交是财富，抓住财富财富\"," +
            "    \"img\" : \"https:\\/\\/gss3.bdstatic.com\\/-Po3dSag_xI4khGkpoWK1HF6hhy\\/baike\\/w%3D268%3Bg%3D0\\/sign=c490620e9516fdfad86cc1e88cb4eb69\\/a08b87d6277f9e2f6477842d1730e924b899f351.jpg\"," +
            "          \"ctype\" : 1" +
            "        }," +
            "        {" +
            "          \"id\" : 2475," +
            "          \"title\" : \"心理学入门\"," +
            "          \"author\" : \"作者\"," +
            "      \"desc\" : \"人在江湖拼搏的生死之交是财富，抓住财富财富\"," +
            "    \"img\" : \"https:\\/\\/gss3.bdstatic.com\\/-Po3dSag_xI4khGkpoWK1HF6hhy\\/baike\\/w%3D268%3Bg%3D0\\/sign=c490620e9516fdfad86cc1e88cb4eb69\\/a08b87d6277f9e2f6477842d1730e924b899f351.jpg\"," +
            "          \"ctype\" : 1" +
            "        }," +
            "        {" +
            "          \"id\" : 2475," +
            "          \"title\" : \"心理学入门\"," +
            "          \"author\" : \"作者\"," +
            "      \"desc\" : \"人在江湖拼搏的生死之交是财富，抓住财富财富\"," +
            "    \"img\" : \"https://gss2.bdstatic.com/9fo3dSag_xI4khGkpoWK1HF6hhy/baike/s%3D220/sign=5d7eb37d48a7d933bba8e3719d4ad194/86d6277f9e2f07087af5a369e324b899a801f2e3.jpg\"," + "          \"ctype\" : 1" +
            "        }," +
            "        {" +
            "          \"id\" : 2475," +
            "          \"title\" : \"心理学入门\"," +
            "          \"author\" : \"作者\"," +
            "      \"desc\" : \"人在江湖拼搏的生死之交是财富，抓住财富财富\"," +
            "    \"img\" : \"https:\\/\\/gss3.bdstatic.com\\/-Po3dSag_xI4khGkpoWK1HF6hhy\\/baike\\/w%3D268%3Bg%3D0\\/sign=c490620e9516fdfad86cc1e88cb4eb69\\/a08b87d6277f9e2f6477842d1730e924b899f351.jpg\"," +
            "          \"ctype\" : 1" +
            "        }," +
            "        {" +
            "          \"id\" : 2475," +
            "          \"title\" : \"心理学入门\"," +
            "          \"author\" : \"作者\"," +
            "      \"desc\" : \"人在江湖拼搏的生死之交是财富，抓住财富财富\"," +
            "    \"img\" : \"https://gss2.bdstatic.com/9fo3dSag_xI4khGkpoWK1HF6hhy/baike/s%3D220/sign=5d7eb37d48a7d933bba8e3719d4ad194/86d6277f9e2f07087af5a369e324b899a801f2e3.jpg\"," +
            "          \"ctype\" : 1" +
            "        }" +
            "      ]," +
            "      \"stype\" : 3" +
            "    }" +
            "  ]" +
            "}"
    val list = MJson.getInstance().fromJson<RecommendList>(json, RecommendList::class.java)
    return list.data!!
}