package com.mivideo.mifm.ui.adapter.managedelete

import android.content.Context
import android.util.Log
import com.mivideo.mifm.ui.adapter.KRefreshDelegateAdapter

/**
 * Created by Jiwei Yuan on 18-8-3.
 */

class DeleteManageAdapter : KRefreshDelegateAdapter<Managable>() {
    var selectAll = false
    var management = false
    @Synchronized
    fun hasData(): Boolean {
        return dataList != null && dataList.size > 0
    }

    @Synchronized
    fun switchToManagement() {
        // 切换到"管理"界面
        management = true
        for (data in dataList) {
            data.manage = true
            data.delete = false
        }
        notifyDataSetChanged()
    }

    @Synchronized
    fun switchToOriginal() {
        // 切换到原界面
        management = false
        for (data in dataList) {
            data.manage = false
        }
        notifyDataSetChanged()
    }

    fun deleteData(deletedList: List<String>): Boolean {
        var before = dataList.size
        if (dataList != null && dataList.size > 0) {
            val it = dataList.iterator()
            while (it.hasNext()) {
                var l = it.next()
                if (l != null && deletedList.contains(l.id)) {
                    it.remove()
                }
            }
        }
        var after = dataList.size
        notifyDataSetChanged()
        return before != after
    }

    @Synchronized
    fun updateClicked(context: Context?, key: String) {
        var modified = false
        for (data in dataList) {
            if (data.id == key) {
                data.clicked = true
                Log.d("CA", "updateClicked|" + key)
                modified = true
            }
        }
        if (modified) {
            notifyDataSetChanged() // 未防止点击后马上更换文案造成误解，这种情况下次依赖resume时再刷新即可
        }
    }

    @Synchronized
    fun updateCheck(context: Context?, key: String, check: Boolean): CheckState {
        var checkedSize = 0
        var modified = false
        var totalSize = dataList.size
        for (data in dataList) {
            if (data.id == key) {
                data.delete = check
                Log.d("CA", "updateCheck|" + key + "|" + check)
                modified = true
            }
            if (data.delete) {
                checkedSize = checkedSize + 1
            }
        }
        if (modified) {
            notifyDataSetChanged()
        }
        if (checkedSize == 0) {
            return CheckState.NO_CHECKED
        } else if (checkedSize == totalSize) {
            return CheckState.ALL_CHECKED
        } else {
            return CheckState.SOME_CHECKED
        }
    }

    @Synchronized
    fun clearData(context: Context?) {
        super.clearData()
    }


    @Synchronized
    fun selectAll(): List<String> {
        // 全选
        this.selectAll = true
        var selectedKeys = ArrayList<String>()
        for (data in dataList) {
            selectedKeys.add(data.id)
            data.delete = true
        }
        notifyDataSetChanged()
        return selectedKeys
    }

    @Synchronized
    fun deSelectAll(): List<String> {
        // 全不选
        this.selectAll = false
        for (data in dataList) {
            data.delete = true
        }
        notifyDataSetChanged()
        return ArrayList<String>()
    }

    fun removeData() {
        for (data in dataList) {
            if (data.delete) {
                dataList.remove(data)
            }
        }
        notifyDataSetChanged()
    }


}