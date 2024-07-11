package com.shatyuka.zhiliao.hooks

import com.shatyuka.zhiliao.Helper
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge.hookMethod
import java.lang.reflect.Field

/**
 * api: /commercial_api/banners_v3/app_topstory_banner
 */
class TopstoryBanner : BaseHook() {

    private var adFocusData: Class<*>? = null
    private var bannerField: Field? = null

    override fun getName(): String {
        return "去除推荐页顶部横幅"
    }

    override fun init(classLoader: ClassLoader) {
        try {
            adFocusData = classLoader.loadClass("com.zhihu.android.adbase.model.AdFocusData")
            bannerField = adFocusData?.declaredFields?.filter {
                it.type == String::class.java
            }?.get(0)
        } catch (e: Exception) {
            logE("cannot find banner in AdFocusData: $e.message")
        }
    }

    override fun hook() {
        if (!Helper.prefs.getBoolean("switch_mainswitch", false)) {
            return
        }

        hookMethod(Helper.JacksonHelper.ObjectReader_readValue, object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                if (param.result == null || param.result.javaClass != adFocusData) {
                    return
                }
                bannerField?.set(param.result, "")
            }
        })
    }
}