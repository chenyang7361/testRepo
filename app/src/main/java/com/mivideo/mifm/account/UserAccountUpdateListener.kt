package com.mivideo.mifm.account

/**
 * 用户账户更新监听器接口
 */
interface UserAccountUpdateListener {

    /**
     * 用户账户发生更新

     * @param accountInfo 当前用户账户
     */
    fun onUserAccountUpdated(accountInfo: AccountInfo?)
}
