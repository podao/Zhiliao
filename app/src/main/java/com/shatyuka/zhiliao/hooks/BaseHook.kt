package com.shatyuka.zhiliao.hooks

import com.shatyuka.zhiliao.Helper

abstract class BaseHook : IHook {

    protected fun logE(msg: String?) {
        Helper.logE(this.javaClass.simpleName, msg)
    }

    protected fun logE(e: Exception) {
        Helper.logE(this.javaClass.simpleName, e)
    }

}