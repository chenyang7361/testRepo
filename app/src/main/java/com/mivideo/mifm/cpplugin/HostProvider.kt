package com.mivideo.mifm.cpplugin

import android.compact.impl.TaskPayload
import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.util.Log
import com.github.salomonbrys.kodein.instance
import com.mivideo.mifm.EnvConfigModel
import com.mivideo.mifm.MainApp
import com.mivideo.mifm.cpplugin.PluginAction
import com.mivideo.mifm.cpplugin.PluginManager
import com.mivideo.mifm.cpplugin.PluginMethod
import com.mivideo.mifm.data.models.jsondata.plugins.ResultBean
import com.mivideo.mifm.util.MJson

class HostProvider : ContentProvider() {

    private val TAG = "HP"
    private var enableLogcat = false
    private var pluginManager: PluginManager? = null

    override fun onCreate(): Boolean {
        checkAndInitPluginManager("onCreate")
        return true
    }

    private fun checkAndInitPluginManager(where: String) {
        if (pluginManager == null) {
            val app = context.applicationContext as MainApp
            pluginManager = app.kodein.instance()
            Log.d(TAG, "init pluginManager@${System.currentTimeMillis()}@${where}")
        }
    }

    override fun call(method: String, arg: String?, extras: Bundle?): Bundle? {
        checkAndInitPluginManager("call")
        enableLogcat = EnvConfigModel(context.applicationContext).isOpenLog

        val result = handleAll(method, arg, extras)
        return if (result != null) {
            var ret = Bundle()
            ret.putString("result_json", MJson.getInstance().toGson(result))
            ret
        } else {
            super.call(method, arg, extras)
        }
    }

    private fun handleAll(method: String, arg: String?, extras: Bundle?): ResultBean? {
        var action = extras?.getString(PluginAction.NAME)
        var resultBean: ResultBean? = null
        if (enableLogcat) {
            Log.d(TAG, "${javaClass.getSimpleName()}|handleAll|method|${method}|extras|${extras?.toString()}|start|${System.currentTimeMillis()}")
        }
        when (method) {
            PluginMethod.METHOD_SEND_MESSAGES_TO_HOST -> resultBean = getPluginMessages(action, extras)
            PluginMethod.METHOD_GET_PLUGIN_INFOS -> resultBean = getPluginInfosByAction(action)
            else -> { /* 待扩展 */
            }
        }
        if (enableLogcat) {
            Log.d(TAG, "${javaClass.getSimpleName()}|handleAll|method|${method}|extras|${extras?.toString()}|end|${System.currentTimeMillis()}")
        }
        return resultBean
    }

    private fun getPluginInfosByAction(action: String?): ResultBean? {
        return if (PluginAction.ACTION_GET_IDENTIFY == action) {
            var resultBean = ResultBean()
            resultBean.identify = context.applicationContext.packageName
            resultBean.timestamp = System.currentTimeMillis()
            resultBean
        } else {
            // TODO: other actions
            null
        }
    }

    private fun getPluginMessages(action: String?, extras: Bundle?): ResultBean? {
        return if (PluginAction.ACTION_SET_TASK_PAYLOAD == action) {
            val strJson = extras?.getString("extra_json")
            val payload = MJson.getInstance().fromJson(strJson, TaskPayload::class.java)
            payload?.let {
                if (enableLogcat) {
                    Log.d(TAG, "HOST|getPluginMessages|TASK_PAYLOAD|${payload.identify}|${payload.ex}|${payload.state}|${payload.timestamp}")
                }
            }

            checkAndInitPluginManager("before calling receiveFromPlugin")
            pluginManager?.receiveFromPlugin(context.applicationContext, payload);

            var resultBean = ResultBean()
            resultBean.identify = context.applicationContext.packageName
            resultBean.timestamp = System.currentTimeMillis()
            resultBean
        } else {
            // TODO: other actions
            null
        }
    }

    override fun query(uri: Uri, strings: Array<String>?, s: String?, strings1: Array<String>?, s1: String?): Cursor? {
        return null
    }

    override fun getType(uri: Uri): String? {
        return null
    }

    override fun insert(uri: Uri, contentValues: ContentValues?): Uri? {
        return null
    }

    override fun delete(uri: Uri, s: String?, strings: Array<String>?): Int {
        return 0
    }

    override fun update(uri: Uri, contentValues: ContentValues?, s: String?, strings: Array<String>?): Int {
        return 0
    }
}
