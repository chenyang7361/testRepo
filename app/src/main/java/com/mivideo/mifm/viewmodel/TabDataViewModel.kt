package com.mivideo.mifm.viewmodel

import android.content.Context
import com.github.salomonbrys.kodein.KodeinInjected
import com.google.gson.Gson
import com.mivideo.mifm.data.models.jsondata.BottomTabEntity
import com.mivideo.mifm.rx.asyncSchedulers
import org.json.JSONObject
import rx.Observable
import java.nio.charset.Charset

class TabDataViewModel(val context: Context) : BaseViewModel(context), KodeinInjected {

    fun getTabInfo(): Observable<BottomTabEntity?> {
        return mockTab()
    }

    fun mockTab(): Observable<BottomTabEntity?> {
        return getFromAssets("tab.json")
                .map { json ->
                    val t = JSONObject(json)
                    val entity = Gson().fromJson(t.toString(), BottomTabEntity::class.java)
                    entity
                }
                .compose(asyncSchedulers())
    }

    //从assets 文件夹中获取文件并读取数据
    fun getFromAssets(fileName: String): Observable<String> {
        return Observable.create<String> { subscriber ->
            var result = ""
            try {
                val `in` = context.resources.assets.open(fileName)
                //获取文件的字节数
                val lenght = `in`.available()
                //创建byte数组
                val buffer = ByteArray(lenght)
                //将文件中的数据读到byte数组中
                `in`.read(buffer)
                result = String(buffer, Charset.forName("UTF-8"))
                subscriber.onNext(result)
                subscriber.onCompleted()
            } catch (e: Exception) {
                subscriber.onError(e)
            }
        }
    }

    override fun release() {
    }

}
