package com.mivideo.mifm.viewmodel

import android.content.Context
import android.text.TextUtils
import com.f2prateek.rx.preferences.RxSharedPreferences
import com.github.salomonbrys.kodein.instance
import com.google.gson.reflect.TypeToken
import com.mivideo.mifm.data.models.jsondata.ChannelItem
import com.mivideo.mifm.data.models.jsondata.SearchResult
import com.mivideo.mifm.data.repositories.SearchRepository
import com.mivideo.mifm.rx.asyncSchedulers
import com.mivideo.mifm.util.MJson
import rx.Observable
import java.util.*

/**
 * Created by Jiwei Yuan on 18-8-9.
 */
class SearchViewModel(val context: Context) : ListViewModel<ArrayList<ChannelItem>>(context) {
    companion object {

        /**
         * 保存搜索历史本地保存的SP名称
         * 保存搜索历史本地保存的SPKey
         */
        val SEARCHSPNAME = "daily_search_history_spname"
        val SEARCHSPKEY = "daily_search_history_spkey"
    }

    private val searchRepo: SearchRepository by instance()

    private var rxSharedPreferences: RxSharedPreferences = RxSharedPreferences.create(context.getSharedPreferences(SEARCHSPNAME, Context.MODE_PRIVATE))

    private var searchKey: String = ""

//    /**
//     * 获取热搜数据
//     */
//    fun getHotSearch(): Observable<ArrayList<HotWord>> {
//        return searchRepo.getHotSearch()
//                .map {
//                    it.data!!.hotword
//                }
//                .compose(asyncSchedulers())
//    }

    /**
     * 获取搜索历史列表
     */
    fun getSearchHistory(): Observable<List<String>> {
        return Observable
                .create(Observable.OnSubscribe<List<String>> { subscribe ->
                    var hotStr = rxSharedPreferences.getString(SEARCHSPKEY).get()
                    if (TextUtils.isEmpty(hotStr)) {
                        hotStr = ""
                    }
                    var mList = MJson.getInstance()
                            .fromJson<List<String>>(hotStr, object : TypeToken<List<String>>() {}.type)
                    if (mList == null) {
                        mList = ArrayList<String>()
                    }
                    subscribe.onNext(mList)
                    subscribe.onCompleted()
                })
                .compose(asyncSchedulers())
    }

//    /**
//     * 将搜索过的保存到历史记录里面
//     * @param item
//     */
//    fun addSearchItem(item: String?) {
//        if (item == null) {
//            throw IllegalArgumentException("参数传递错误...")
//        }
//
//        Observable.create(Observable.OnSubscribe<String> {
//            val t = rxSharedPreferences.getString(SEARCHSPKEY).get()
//            var mList: MutableList<String>? = MJson.getInstance().fromJson(t, object : TypeToken<List<String>>() {}.type)
//
//            if (mList == null) {
//                mList = ArrayList<String>()
//            }
//            if (mList.size > 0) {
//                val mList1 = ArrayList<String>()
//                for (i in mList.indices) {
//                    if (mList[i] != item) {
//                        mList1.add(mList[i])
//                    }
//                }
//                mList = mList1
//            }
//
//            if (mList.size >= MainConfig.SEARCH_QUEUE_HISTORY_SIZE) {
//                mList.removeAt(0)
//            }
//
//            mList.add(item)
//            rxSharedPreferences.getString(SEARCHSPKEY).set(MJson.getInstance().toGson(mList))
//        })
//                .compose(asyncSchedulers())
//                .subscribe({}, {})
//    }

    /**
     * 删除某一项历史记录
     */
    fun deleteSearchItemData(itemStr: String): Observable<Boolean> {
        return Observable
                .create(Observable.OnSubscribe<Boolean> { subscribe ->
                    try {
                        val t = rxSharedPreferences.getString(SEARCHSPKEY).get()
                        val mList: MutableList<String>? = MJson.getInstance().fromJson(t, object : TypeToken<List<String>>() {}.type)

                        if (mList != null && mList.size > 0) {
                            val tempList = ArrayList<String>()
                            for (i in 0..mList.size - 1) {
                                if (itemStr != mList[i]) {
                                    tempList.add(mList[i])
                                }
                            }

                            rxSharedPreferences.getString(SEARCHSPKEY).set(MJson.getInstance().toGson(tempList))
                        }
                        subscribe.onNext(true)
                        subscribe.onCompleted()
                    } catch (e: Exception) {
                        subscribe.onError(e)
                    }
                })
                .compose(asyncSchedulers())
    }

    /**
     * 清空搜索历史
     */
    fun clearSearchListData() {
        Observable.create(Observable.OnSubscribe<Boolean> { subscribe ->
            rxSharedPreferences.getString(SEARCHSPKEY).delete()
            subscribe.onNext(true)
            subscribe.onCompleted()
        })
                .compose(asyncSchedulers())
                .subscribe({
                }, {})
    }

    /**
     * 监听搜索历史记录数据变化
     */
    fun observeHistoryDataChange(): Observable<String> {
        return rxSharedPreferences.getString(SEARCHSPKEY).asObservable()
    }

    fun getSearchResult(searchKey: String): Observable<SearchResult> {
        this.searchKey = searchKey
        return searchRepo.search(searchKey, pageNo)
                .compose(asyncSchedulers())

    }

    /**
     * 判断返回结果是否为空(当加载第一页时若为空则跳转空数据页面)
     */
    fun isDataNull(searchList: SearchResult): Boolean {
        return (searchList.data == null || searchList.data!!.size == 0)
                && pageNo == 1
    }

    override fun onRefreshData(): Observable<ArrayList<ChannelItem>> {
        //搜索结果页没有下拉刷新操作
        return Observable.empty()
    }

    override fun onLoadMoreData(): Observable<ArrayList<ChannelItem>> {
        return searchRepo.search(searchKey, pageNo)
                .map {
                    it.data!!
                }
                .compose(asyncSchedulers())
    }

    override fun release() {

    }
}