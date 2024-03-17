package com.shatyuka.zhiliao.hooks

import com.shatyuka.zhiliao.Helper
import de.robv.android.xposed.XC_MethodReplacement.DO_NOTHING
import de.robv.android.xposed.XC_MethodReplacement.returnConstant
import de.robv.android.xposed.XposedBridge
import java.lang.reflect.Method

class VIPBanner : IHook {

    private lateinit var handleVipData: Method
    private lateinit var moreVipData: Class<*>
    override fun getName(): String {
        return "隐藏会员卡片"
    }

    @Throws(Throwable::class)
    override fun init(classLoader: ClassLoader) {
        moreVipData = try {
            classLoader.loadClass("com.zhihu.android.api.MoreVipData")
        } catch (ignored: ClassNotFoundException) {
            classLoader.loadClass("com.zhihu.android.profile.data.model.MoreVipData")
        }
        val mineTabFragment =
            classLoader.loadClass("com.zhihu.android.app.ui.fragment.more.mine.MineTabFragment")
        handleVipData = Helper.getMethodByParameterTypes(mineTabFragment, moreVipData)
    }

    @Throws(Throwable::class)
    override fun hook() {
        if (Helper.prefs.getBoolean("switch_mainswitch", false)
            && Helper.prefs.getBoolean("switch_vipbanner", false)
        ) {
            XposedBridge.hookMethod(handleVipData, DO_NOTHING)
            XposedBridge.hookAllMethods(moreVipData, "isLegal", returnConstant(false))
        }
    }
}
