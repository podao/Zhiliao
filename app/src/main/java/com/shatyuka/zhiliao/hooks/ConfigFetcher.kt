package com.shatyuka.zhiliao.hooks

import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers.findAndHookMethod

class ConfigFetcher : BaseHook() {

    private var handlers = listOf(
        // 首页听按钮
        Pair<String, Any?>("home_tabbar_audio_entry", null),
    )

    private lateinit var configFetcherImplClass: Class<*>

    override fun getName(): String {
        return "自定义知乎配置"
    }

    override fun init(classLoader: ClassLoader) {
        configFetcherImplClass =
            classLoader.loadClass("com.zhihu.android.zonfig.core.ConfigFetcherImpl")
    }

    override fun hook() {
        findAndHookMethod(
            configFetcherImplClass,
            "getStaticNode",
            String::class.java,
            object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    val configName = param.args[0]
                    handlers.forEach {
                        if (it.first == configName) {
                            param.result = it.second
                            return@forEach
                        }
                    }
                }
            })
    }
}