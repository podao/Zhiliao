package com.shatyuka.zhiliao.hooks

import com.shatyuka.zhiliao.Helper
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge

class FeedTopHotBanner : IHook {

    private lateinit var feedTopHot: Class<*>
    private lateinit var templateHeaderHolder: Class<*>

    override fun getName(): String {
        return "隐藏推荐页置顶热门"
    }

    @Throws(Throwable::class)
    override fun init(classLoader: ClassLoader) {
        feedTopHot = classLoader.loadClass("com.zhihu.android.api.model.FeedTopHot")
        templateHeaderHolder =
            classLoader.loadClass("com.zhihu.android.app.feed.ui.holder.template.optimal.TemplateHeaderHolder")
    }

    override fun hook() {
        XposedBridge.hookAllMethods(
            templateHeaderHolder,
            "onBindData",
            object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    if (Helper.prefs.getBoolean("switch_mainswitch", false)
                        && Helper.prefs.getBoolean("switch_feedtophot", false)
                    ) {
                        param.args[0] = feedTopHot.constructors[0].newInstance()
                    }
                }
            }
        )
    }
}
