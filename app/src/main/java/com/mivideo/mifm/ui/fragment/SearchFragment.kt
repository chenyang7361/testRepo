package com.mivideo.mifm.ui.fragment

import android.content.Context
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import com.mivideo.mifm.NetworkManager
import com.mivideo.mifm.R
import com.mivideo.mifm.data.models.jsondata.SearchResult
import com.mivideo.mifm.ui.adapter.RVCompositeAdapter
import com.mivideo.mifm.ui.adapter.search.NewSearchResultAdapter
import com.mivideo.mifm.util.MJson
import com.mivideo.mifm.util.addTo
import com.mivideo.mifm.util.app.hideKeyboard
import com.mivideo.mifm.util.app.showToast
import com.mivideo.mifm.viewmodel.SearchViewModel
import com.trello.rxlifecycle.FragmentEvent
import kotlinx.android.synthetic.main.search_fragment.*
import kotlinx.android.synthetic.main.search_fragment_top.*
import org.jetbrains.anko.onClick
import timber.log.Timber

/**
 * Created by Jiwei Yuan on 18-8-9.
 */
class SearchFragment : BaseRefreshListFragment() {

    companion object {
        const val ARG_SEARCH_KEY = "arg_search_key"
    }


    private var isSearchAble: Boolean = false
    private var searchText: String? = null

    private lateinit var compositeAdapter: RVCompositeAdapter
    //    private lateinit var searchHistoryAdapter: SearchHistoryAdapter
//    private lateinit var searchHotAdapter: SearchHotAdapter
    private lateinit var searchResultAdapter: NewSearchResultAdapter

    private lateinit var mViewModel: SearchViewModel


    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.search_fragment, container, false)
    }

    override fun preInitRefresh() {
        mViewModel = SearchViewModel(context.applicationContext)
        lifecycle.addObserver(mViewModel)
        searchText = arguments?.getString(ARG_SEARCH_KEY)
        initLayoutView()
    }

    override fun initRefreshView(view: View) {
        compositeAdapter = RVCompositeAdapter(context)


//        searchHistoryAdapter = SearchHistoryAdapter(context)
//        searchHistoryAdapter.setItemListener(object : SearchHistoryHolder.OnItemListener {
//            override fun onClickItem(view: View, text: String) {
//                checkSearchHot(text)
//            }
//
//            override fun onClickDelete(view: View, text: String, originData: ArrayList<String>) {
//                mViewModel.deleteSearchItemData(text)
//                        .compose(bindUntilEvent(FragmentEvent.DESTROY_VIEW))
//                        .subscribe(
//                                {
//                                    originData.remove(text)
//                                    searchHistoryAdapter.notifyDataSetChanged()
//                                },
//                                { error ->
//                                    Timber.e(error)
//                                })
//
//            }
//
//            override fun onClickDeleteAll(view: View) {
//                mViewModel.clearSearchListData()
//            }
//
//            override fun onClickExpandBtn(view: View, expandHistoryList: Boolean) {
//                activity?.let {
//                    hideKeyboard(it)
//                }
//                if (expandHistoryList) {
//                    searchHistoryAdapter.expandHistoryList()
//                } else {
//                    searchHistoryAdapter.shortenHistoryList()
//                }
//            }
//
//        })

//        searchHotAdapter = SearchHotAdapter(context)
//        searchHotAdapter.setItemListener(object : SearchHotHeaderHolder.OnItemListener {
//            override fun onClickRefresh(refreshBtn: ImageView) {
//                startRotateView(refreshBtn)
//                mViewModel.getHotSearch()
//                        .compose(bindUntilEvent(FragmentEvent.DESTROY_VIEW))
//                        .subscribe(
//                                { data ->
//                                    refreshBtn.clearAnimation()
//                                    searchHotAdapter.setData(data)
//                                },
//                                { error ->
//                                    refreshBtn.clearAnimation()
//                                    showToast(context.applicationContext, getString(R.string.update_search_hot_fail))
//                                    Timber.e(error)
//                                })
//            }
//
//            override fun onClickItem(view: View, text: String) {
//                checkSearchHot(text)
//            }
//
//        })
        searchResultAdapter = NewSearchResultAdapter(context, mFragmentName)

//        compositeAdapter.addAdapter(searchHistoryAdapter)
//        compositeAdapter.addAdapter(searchHotAdapter)
        compositeAdapter.addAdapter(searchResultAdapter)

        refreshLayout = view.findViewById(R.id.recyclerView)
        refreshLayout?.isEnableRefresh = false
        refreshLayout?.getRecyclerView()?.layoutManager = LinearLayoutManager(context)
        refreshLayout?.getRecyclerView()?.adapter = compositeAdapter
        refreshLayout?.getRecyclerView()?.setCheckEmptyListener(null)

    }

    override fun postInitRefresh() {
        refreshLayout?.setOnLoadmoreListener {
            loadMore()
        }

        /**
         * 监听输入框内容变化
         */
        videoSearchEdit.observerTextChange()
                .compose(bindUntilEvent(FragmentEvent.DESTROY_VIEW))
                .subscribe({ s ->
                    showDeleteIcon(s.toString())
                    if (s.toString().isEmpty()) {
                        isSearchAble = false
                        videoSearchText.text = context.resources.getString(R.string.search_fragment_cancel)
//                        gotoSearchHot()
                    } else {
                        isSearchAble = true
                        videoSearchText.text = context.resources.getString(R.string.search_fragment_button)
                    }
                }, {})
                .addTo(mViewModel.compositeSubscription)

        /**
         * 监听输入框焦点状态变化
         */
        videoSearchEdit.observerFocusChange()
                .compose(bindUntilEvent(FragmentEvent.DESTROY_VIEW))
                .subscribe({ b ->
                    if ((b as Boolean && TextUtils.isEmpty(videoSearchEdit.text.toString())) || !b) {
                        isSearchAble = false
                        videoSearchText.text = context.resources.getString(R.string.search_fragment_cancel)
                    } else {
                        isSearchAble = true
                        videoSearchText.text = context.resources.getString(R.string.search_fragment_button)
                    }
                }, {})

        /**
         * 监听软键盘搜索按钮点击事件
         */
        videoSearchEdit.observerKeySearch()
                .compose(bindUntilEvent(FragmentEvent.DESTROY_VIEW))
                .subscribe({
                    if (!TextUtils.isEmpty(videoSearchEdit.text.toString())) {
                        activity?.let {
                            hideKeyboard(it)
                        }
                        videoSearchEdit.clearFocus()
//                        mViewModel.addSearchItem(videoSearchEdit.text.toString())
                        gotoSearchResult(videoSearchEdit.text.toString())
                    } else if (!TextUtils.isEmpty(videoSearchEdit.hint) && context.resources.getString(R.string.search_edit_hint) != videoSearchEdit.hint.toString()) {
                        val searchStr = videoSearchEdit.hint.toString()
                        activity?.let {
                            hideKeyboard(it)
                        }
                        videoSearchEdit.setText(searchStr)
                        videoSearchEdit.clearFocus()
//                        mViewModel.addSearchItem(searchStr)
                        gotoSearchResult(searchStr)
                    } else {
                        showToast(context, context.resources.getString(R.string.search_input_keyword))
                    }
                }, {})
    }

    override fun checkNetwork() {
        if (NetworkManager.isNetworkUnConnected()) {
            showNetUnconnected()
        } else {

//            showEmpty()
            // 跳转热搜页面
//            gotoSearchHot()
//            observeHistoryDataChange()
        }
    }

    override fun refresh() {
        if (NetworkManager.isNetworkUnConnected()) {
            showNetUnconnected()
        } else {
            hideTipView()
            gotoSearchResult(videoSearchEdit.text.toString())
//            if (TextUtils.isEmpty(videoSearchEdit.text.toString().trim())) {
//                gotoSearchHot()
//
//                observeHistoryDataChange()
//            } else {
//            gotoSearchResult(videoSearchEdit.text.toString())
//            }
        }
    }

    override fun loadMore() {
        mViewModel.loadMoreData()
                .compose(bindUntilEvent(FragmentEvent.DESTROY_VIEW))
                .subscribe(
                        {
                            searchResultAdapter.addFooterItems(it)
                            if (it.isEmpty()) {
                                recyclerView.finishLoadmoreWithNoMoreData()
                            } else {
                                recyclerView.resetNoMoreData()
                            }
                            recyclerView.finishLoadmore()
                        },
                        {
                            recyclerView.finishLoadmore()
                        }
                )
    }

//    private fun startRotateView(view: View) {
//        val rotateAnimation = RotateAnimation(180f, 360f, Animation.RELATIVE_TO_SELF,
//                0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
//        rotateAnimation.duration = 80
//        view.startAnimation(rotateAnimation)
//    }

//    /**
//     * 监听搜索历史数据变化
//     */
//    private fun observeHistoryDataChange() {
//        mViewModel.observeHistoryDataChange()
//                .compose(asyncSchedulers())
//                .subscribe({ str ->
//                    if (TextUtils.isEmpty(str) || str == "[]") {
//                        searchHistoryAdapter.clearData()
//                    } else {
//                        searchHistoryAdapter.notifyDataSetChanged()
//                    }
//                }, {})
//    }

    /**
     * 初始化页面组件
     */
    private fun initLayoutView() {
        /**
         * 判断是否显示输入框中的delete图标
         * 默认进入搜索页面获取焦点
         */
        showDeleteIcon(videoSearchEdit.text.toString())
        requestFocus()
        requestInputDialog()

        if (!TextUtils.isEmpty(searchText)) {
            videoSearchEdit.hint = searchText
        }

        /**
         * 输入框清空处理逻辑
         */
        videoSearchClearImage.onClick {
            clearSearchEditInput()
        }

        /**
         * 搜索按钮点击处理逻辑
         */
        videoSearchText.onClick {
            if (isSearchAble) {
                if (!TextUtils.isEmpty(videoSearchEdit.text.toString())) {
                    activity?.let {
                        hideKeyboard(it)
                    }
                    videoSearchEdit.clearFocus()
//                    mViewModel.addSearchItem(videoSearchEdit.text.toString())
                    gotoSearchResult(videoSearchEdit.text.toString())
                } else {
                    showToast(context, context.resources.getString(R.string.search_input_keyword))
                }
            } else {
                activity?.let {
                    hideKeyboard(it)
                }
                if (TextUtils.isEmpty(videoSearchEdit.text.toString())) {
                    pop()
                } else {
                    clearSearchEditInput()
                }
            }
        }
    }

    private fun clearSearchEditInput() {
        videoSearchEdit.setText("")
        videoSearchEdit.setSelection("".length)
        requestFocus()
    }


    /**
     * 根据搜索框字符串内容判断是否显示搜索框的删除图标
     */
    private fun showDeleteIcon(str: String) {
        if (str.isNotEmpty()) {
            videoSearchClearImage.visibility = View.VISIBLE
        } else {
            videoSearchClearImage.visibility = View.GONE
        }
    }

    override fun onBackPressedSupport(): Boolean {
        if (TextUtils.isEmpty(videoSearchEdit.text.toString())) {
            pop()
        } else {
            clearSearchEditInput()
        }
        return true
    }

//    /**
//     * 点击热搜处理逻辑
//     */
//    fun checkSearchHot(searchStr: String) {
//        activity?.let {
//            hideKeyboard(it)
//        }
//        videoSearchEdit?.let {
//            videoSearchEdit.setText(searchStr)
//            videoSearchEdit.setSelection(searchStr.length)
//            videoSearchEdit.clearFocus()
//        }
//        gotoSearchResult(searchStr, true)
//    }

    /**
     * 跳转搜索结果页面
     */
    private fun gotoSearchResult(searchStr: String) {
//        if (searchStr == BuildConfig.CONFIG_ENTER_KEY) {
//            start(ConfigFragment.createFragment())
//        } else {
        gotoSearchResult(searchStr, false)
//        }
    }

    private fun gotoSearchResult(searchStr: String, hot: Boolean) {
        recyclerView.isEnableLoadmore = true
//        Statistics.logSearch(context, searchStr, hot)
        recyclerView.getRecyclerView().scrollToPosition(0)
//        searchHistoryAdapter.clearData()
//        searchHotAdapter.clearData()

        loadSearchResultView(searchStr)
    }

//    /**
//     * 跳转热搜页面
//     */
//    private fun gotoSearchHot() {
//        recyclerView.isEnableLoadmore = false
//        recyclerView.getRecyclerView().scrollToPosition(0)
//        mViewModel.updatePageNoByRefresh()
//        searchResultAdapter.clearData()
//        searchEmptyAdapter.clearData()
//        loadHistoryView()
//        loadHotSearchView()
//
//    }

//    private fun loadHotSearchView() {
//        Timber.i("loadHotSearchView")
//        mViewModel.getHotSearch()
//                .compose(bindUntilEvent(FragmentEvent.DESTROY_VIEW))
//                .subscribe(
//                        { data ->
//                            searchHotAdapter.setData(data)
//                        },
//                        { error ->
//                            Timber.e(error)
//                        })
//    }
//
//    private fun loadHistoryView() {
//        Timber.i("loadHistoryView")
//        mViewModel.getSearchHistory()
//                .compose(bindUntilEvent(FragmentEvent.DESTROY_VIEW))
//                .subscribe(
//                        { data ->
//                            searchHistoryAdapter.setData(data)
//                        },
//                        { error ->
//                            Timber.e(error)
//                        }
//                )
//    }

    private fun loadSearchResultView(searchStr: String) {
        Timber.i("loadSearchResultView")

        mViewModel.getSearchResult(searchStr)
                .compose(bindUntilEvent(FragmentEvent.DESTROY_VIEW))
                .subscribe(
                        {
                            //TODO
                            val data = mockData()
                            if (mViewModel.isDataNull(data)) {
                                gotoSearchEmpty()
                            } else {
                                searchResultAdapter.setData(data.data!!)
                                count.text = getString(R.string.search_count, data.count)
                            }
                        },
                        { error ->
                            Timber.e(error)
                            gotoSearchEmpty()
                            val data = mockData()
                            if (mViewModel.isDataNull(data)) {
                                gotoSearchEmpty()
                            } else {
                                searchResultAdapter.setData(data.data!!)
                                count.visibility = View.VISIBLE
                                count.text = getString(R.string.search_count, data.count)
                            }
                        }
                )
    }


    fun mockData(): SearchResult {
        //TODO delete
        val json = "{" +
                "  \"code\" : 1," +
                "  \"count\" : 10," +
                "  \"data\" : [" +
                "    {" +
                "      \"id\" : 1243," +
                "      \"author\" : \"李雪教授\"," +
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
                "  \"has_next\" : false" +
                "}"
        val list = MJson.getInstance().fromJson<SearchResult>(json, SearchResult::class.java)
        return list!!
    }

    /**
     * 跳转搜索结果为空
     */
    private fun gotoSearchEmpty() {
        recyclerView.isEnableLoadmore = false
        recyclerView.getRecyclerView().scrollToPosition(0)
//        loadHotSearchView()
        mViewModel.updatePageNoByRefresh()
        searchResultAdapter.clearData()
//        searchHistoryAdapter.clearData()
    }

    /**
     * 请求打开输入法弹窗
     */
    private fun requestInputDialog() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS)
    }

    /**
     * 请求获取焦点
     */
    private fun requestFocus() {
        videoSearchEdit.isCursorVisible = true
        videoSearchEdit.isFocusable = true
        videoSearchEdit.isFocusableInTouchMode = true
        videoSearchEdit.requestFocus()
    }
}

fun createSearchFragment(): SearchFragment {
    return SearchFragment()
}