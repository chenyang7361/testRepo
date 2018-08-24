package com.mivideo.mifm.ui.adapter.managedelete

import android.content.Context

class ManageDeleteViewHolder {


    var keysToBeDelete = HashSet<String>()
    private lateinit var adapter: DeleteManageAdapter
    private lateinit var deleteManager: DeleteManager
    private lateinit var view: ManageControllerView

    private var management = false

    fun canSwitch(): Boolean {
        return adapter?.hasData()
    }

    fun onSelectAll() {
        keysToBeDelete.addAll(adapter.selectAll())
        if (adapter.hasData()) {
            view.onItemSelected()
        }
    }

    fun switchToOriginal(){
        view.convertExtendedToOrdinary()
    }

    fun deSelectAll(): List<String> {
        return adapter.deSelectAll()
    }

    fun onSwitchToManagement() {
        management = true
        adapter.switchToManagement()
    }

    fun onSwitchToOriginal() {
        management = false
        adapter.switchToOriginal()
    }

    fun doDelete(context: Context?) {
        deleteData(context)
    }

    private fun deleteData(context: Context?) {
        deleteManager.delete(context, keysToBeDelete.toList())
    }

    fun onFinishDelete() {
        if (!adapter.hasData()) {
            view.convertExtendedToOrdinary()
        }
        keysToBeDelete.clear()
    }

    fun attachAdapter(adapter: DeleteManageAdapter) {
        this.adapter = adapter
    }

    fun attachDeleteManager(mgr: DeleteManager) {
        this.deleteManager = mgr
    }

    fun attachView(controller: ManageControllerView) {
        view = controller
        controller.attachHolder(this@ManageDeleteViewHolder)
    }

    fun addToDelete(id: String) {
        keysToBeDelete.add(id)
        view.onItemSelected()
    }

    fun removeFromDelete(id: String) {
        keysToBeDelete.remove(id)
        if (keysToBeDelete.isEmpty()) {
            view.onNoSelected()
        }
    }
}