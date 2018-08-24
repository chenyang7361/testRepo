package com.mivideo.mifm.ui.fragment

import com.mivideo.mifm.SupportFragment

/**
 * 主页面Tab界面的父类Fragment，所有需要在主页面作为Tab显示的Fragment都需要继承此类
 *
 * @author LiYan
 */
open class TabFragment : BaseFragment() {

    /**
     * 从Tab页跳转到二级页面默认的start方法会与TabFragment同一个层级导致底部的Tab栏一直显示
     * 需要通过TabHostFragment作为跳转代理，这样二级页面才会完全盖到TabHostFragment上
     */
    override fun start(toFragment: SupportFragment?) {
        if (parentFragment != null && parentFragment is TabHostFragment) {
            (parentFragment as TabHostFragment).start(toFragment)
        } else {
            super.start(toFragment)
        }
    }

    override fun startForResult(toFragment: SupportFragment?, requestCode: Int) {
        if (parentFragment != null && parentFragment is TabHostFragment) {
            (parentFragment as TabHostFragment).startForResult(toFragment, requestCode)
        } else {
            super.startForResult(toFragment, requestCode)
        }
    }
}
