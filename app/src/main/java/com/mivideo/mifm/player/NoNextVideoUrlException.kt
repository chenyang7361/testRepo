package com.mivideo.mifm.player

/**
 * Created by yamlee on 25/10/2017.
 */
class NoNextVideoUrlException : RuntimeException() {
    override val message: String?
        get() = "No Next VideoUrlManager ,Please Use hasNext method to judge first"
}