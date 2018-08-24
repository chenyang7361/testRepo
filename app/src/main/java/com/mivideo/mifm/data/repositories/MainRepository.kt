package com.mivideo.mifm.data.repositories

import android.content.Context
import com.mivideo.mifm.data.db.DbTabList
import com.mivideo.mifm.data.jsondata.TabList
import com.mivideo.mifm.network.service.MainService
import com.mivideo.mifm.util.MJson
import com.sabres.SabresObject
import rx.Observable
import java.nio.charset.Charset

/**
 * 应用启动，首页加载相关配置数据仓储
 *
 * @author LiYan
 */
class MainRepository(service: MainService) : MainService by service {
    /**
     * 从Db加载Tab数据
     */
    fun loadTabListFromDb(): Observable<TabList> {
        return Observable.create<TabList> { subscribe ->
            DbTabList.getQuery().findInBackground { objects, e ->
                if (objects != null && objects.size > 0) {
                    val dbTabList = MJson.getInstance().fromJson(objects[0].getTabList(), TabList::class.java)
                    subscribe.onNext(dbTabList)
                    subscribe.onCompleted()
                } else {
                    subscribe.onNext(TabList())
                    subscribe.onCompleted()
                }
            }
        }
    }

    /**
     * 从文件加载Tab数据
     */
    fun loadTabListFromFile(fileName: String, context: Context): Observable<TabList> {
        return getFromAssets(fileName, context)
                .map { jsonObject ->
                    MJson.getInstance().fromJson(jsonObject, TabList::class.java)
                }
    }

    /**
     * 更新DB中的TabList数据
     */
    fun updateDbTabList(tabList: TabList) {
        DbTabList.getQuery().findInBackground { objects, e ->
            if (e == null) {
                SabresObject.deleteAllInBackground(objects, {
                    DbTabList.saveTabList(MJson.getInstance().toGson(tabList))
                })
            }
        }
    }

    /**
     * 获取Observable TabList对象
     */
    fun getObservableTabList(tabList: TabList): Observable<TabList> {
        return Observable.create<TabList> { subscribe ->
            subscribe.onNext(tabList)
            subscribe.onCompleted()
        }
    }

    //从assets 文件夹中获取文件并读取数据
    private fun getFromAssets(fileName: String, context: Context): Observable<String> {
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
}
