package com.mivideo.mifm.ui.adapter.managedelete

import android.view.View
import org.jetbrains.anko.onClick

/**
 * Created by Jiwei Yuan on 18-8-6.
 */

class ManageControllerView {

    lateinit var holder: ManageDeleteViewHolder
    private var manageBar: View? = null
    private var ordinaryBar: View? = null

    var cancelBtn: View? = null
    var selectAllBtn: View? = null
    var deselectAllBtn: View? = null
    var deleteBtn: View? = null

    fun attachHolder(holder: ManageDeleteViewHolder) {
        this.holder = holder
    }

    fun fillWithViews(ordinaryBar: View?, manageBar: View?, deleteBar: View?,
                      cancel: View?, selectAll: View?) {
        this.ordinaryBar = ordinaryBar
        this.deleteBtn = deleteBar
        this.manageBar = manageBar
        this.cancelBtn = cancel
        this.selectAllBtn = selectAll
        this.cancelBtn?.onClick {
            holder.onSwitchToOriginal()
            convertExtendedToOrdinary()
        }

        this.selectAllBtn?.onClick {
            deselectAllBtn?.visibility = View.VISIBLE
            holder.onSelectAll()
        }

        this.deleteBtn?.onClick { view ->
            holder.doDelete(view?.context?.applicationContext)
        }
        this.ordinaryBar?.onClick {
            holder.onSwitchToManagement()
            convertOrdinaryToExtended()
        }
        convertExtendedToOrdinary()
    }

    private fun convertOrdinaryToExtended() {
        this.ordinaryBar?.visibility = View.GONE
        this.deleteBtn?.visibility = View.GONE
        this.manageBar?.visibility = View.VISIBLE
    }

    fun convertExtendedToOrdinary() {
        this.manageBar?.visibility = View.GONE
        this.ordinaryBar?.visibility = View.VISIBLE
    }

    fun onNoSelected() {
        this.deleteBtn?.visibility = View.GONE
    }

    fun onItemSelected() {
        this.deleteBtn?.visibility = View.VISIBLE
    }
}