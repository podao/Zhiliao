package com.shatyuka.zhiliao.hooks

import com.shatyuka.zhiliao.Helper
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge

class FeedTopHotBanner : IHook {

    private lateinit var feedTopHotAutoJacksonDeserializer: Class<*>

    override fun getName(): String {
        return "隐藏推荐页置顶热门"
    }

    @Throws(Throwable::class)
    override fun init(classLoader: ClassLoader) {
        feedTopHotAutoJacksonDeserializer =
            classLoader.loadClass("com.zhihu.android.api.model.FeedTopHotAutoJacksonDeserializer")
    }

    override fun hook() {
        XposedBridge.hookAllMethods(
            feedTopHotAutoJacksonDeserializer,
            "deserialize",
            object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    if (Helper.prefs.getBoolean("switch_mainswitch", false)
                        && Helper.prefs.getBoolean("switch_feedtophot", false)
                    ) {
                        param.setResult(null)
                    }
                }
            })
    }
}
