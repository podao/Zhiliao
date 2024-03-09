package com.shatyuka.zhiliao.hooks

import com.shatyuka.zhiliao.Helper
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import java.lang.reflect.Field

class HeadZoneBanner : IHook {

    private lateinit var rankFeedList: Class<*>
    private lateinit var feedsHotListFragment2: Class<*>
    private lateinit var headZone: Field

    override fun getName(): String {
        return "隐藏热榜顶部置顶"
    }

    @Throws(Throwable::class)
    override fun init(classLoader: ClassLoader) {
        rankFeedList = classLoader.loadClass("com.zhihu.android.api.model.RankFeedList")
        headZone = rankFeedList.getDeclaredField("head_zone")
        headZone.isAccessible = true

        feedsHotListFragment2 =
            classLoader.loadClass("com.zhihu.android.app.feed.ui.fragment.FeedsHotListFragment2")
    }

    @Throws(Throwable::class)
    override fun hook() {
        XposedBridge.hookAllMethods(
            feedsHotListFragment2,
            "postRefreshSucceed",
            object : XC_MethodHook() {
                @Throws(IllegalAccessException::class)
                override fun beforeHookedMethod(param: MethodHookParam) {
                    if (Helper.prefs.getBoolean("switch_mainswitch", false)) {
                        headZone.set(param.args[0], null)
                    }
                }
            })
    }
}
