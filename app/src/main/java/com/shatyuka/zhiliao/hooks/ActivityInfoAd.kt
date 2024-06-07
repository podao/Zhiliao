package com.shatyuka.zhiliao.hooks

import com.shatyuka.zhiliao.Helper
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge.hookMethod
import de.robv.android.xposed.XposedHelpers
import java.io.Reader
import java.lang.reflect.Method

/**
 * api: /root/tab
 */
class ActivityInfoAd : BaseHook() {

    private lateinit var readObject: Method

    private var activityInfo: Class<*>? = null
    private var topTabs: Class<*>? = null

    override fun getName(): String {
        return "去除首页顶部Tab活动广告"
    }

    override fun init(classLoader: ClassLoader) {
        readObject = findReadObjectMethod(classLoader)
        try {
            activityInfo = classLoader.loadClass("com.zhihu.android.api.model.ActivityInfo")
        } catch (e: Exception) {
            logE(e.message)
        }

        try {
            topTabs = classLoader.loadClass("com.zhihu.android.api.model.TopTabs")
        } catch (e: Exception) {
            logE(e.message)
        }
    }

    override fun hook() {
        if (!Helper.prefs.getBoolean("switch_mainswitch", false)
            || !Helper.prefs.getBoolean("switch_activityinfoad", true)
        ) {
            return
        }

        hookMethod(readObject, object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                if (param.result != null
                    && (param.result.javaClass == activityInfo || param.result.javaClass == topTabs)
                ) {
                    postProcessActivityInfo(param.result)
                }
            }
        })
    }

    private fun postProcessActivityInfo(activityInfo: Any) {
        XposedHelpers.setObjectField(activityInfo, "topActivity", null)
    }

    private fun findReadObjectMethod(classloader: ClassLoader): Method {
        return classloader.loadClass("com.fasterxml.jackson.databind.ObjectReader").declaredMethods
            .filter {
                it.parameterCount == 1 && it.parameterTypes[0] == Reader::class.java
            }.filter {
                it.returnType == Object::class.java
            }[0]
    }
}