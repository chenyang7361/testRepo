package com.mivideo.mifm.player

import android.content.Context
import android.net.Uri

/**
 * 视频播放数据源
 * @author LiYan
 */
class DataSource private constructor() {
    companion object {
        fun builder(): Builder {
            return Builder(DataSource())
        }
    }

    var context: Context? = null
    var uri: Uri? = null
    var headers: Map<String, String>? = null
    var offset: Int = 0
    private var url: String = ""

    override fun toString(): String {
        return "DataSource(context=$context, uri=$uri, " +
                "headers=$headers, offset=$offset)"
    }

    class Builder constructor(private val dataSource: DataSource) {
        /**
         * 播放数据源如果是URL,请求时增加的Header
         */
        fun headers(headers: Map<String, String>): Builder {
            dataSource.headers = headers
            return this
        }

        /**
         * 设置开始播放的位置
         */
        fun offset(startOffset: Int): Builder {
            dataSource.offset = startOffset
            return this
        }

        /**
         * 设置数据源URI
         */
        fun uri(uri: Uri): Builder {
            dataSource.uri = uri
            return this
        }

        fun url(url: String): Builder {
            val uri = Uri.parse(url)
            dataSource.url = url
            dataSource.uri = uri
            return this
        }

        @Throws(DataSourceErrorException::class)
        fun build(context: Context): DataSource {
            dataSource.context = context.applicationContext
            if (dataSource.context == null) {
                throw DataSourceErrorException("Context cannot be null")
            }
            if (dataSource.uri == null) {
                throw DataSourceErrorException("DataSource uri cannot be null")
            }
            return dataSource
        }
    }
}