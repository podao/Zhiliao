package com.shatyuka.zhiliao.hooks

import com.shatyuka.zhiliao.Helper
import de.robv.android.xposed.XC_MethodReplacement.DO_NOTHING
import de.robv.android.xposed.XC_MethodReplacement.returnConstant
import de.robv.android.xposed.XposedBridge.hookAllMethods
import de.robv.android.xposed.XposedBridge.hookMethod
import java.lang.reflect.Method

class VIPBanner : BaseHook() {

    private var handleVipData: Method? = null
    private var moreVipData: Class<*>? = null

    override fun getName(): String {
        return "隐藏会员卡片"
    }

    @Throws(Throwable::class)
    override fun init(classLoader: ClassLoader) {
        try {
            moreVipData = try {
                classLoader.loadClass("com.zhihu.android.api.MoreVipData")
            } catch (ignored: ClassNotFoundException) {
                classLoader.loadClass("com.zhihu.android.profile.data.model.MoreVipData")
            }
        } catch (e: Exception) {
            logE(e)
        }

        try {
            val mineTabFragment =
                classLoader.loadClass("com.zhihu.android.app.ui.fragment.more.mine.MineTabFragment")
            handleVipData = Helper.getMethodByParameterTypes(mineTabFragment, moreVipData)
        } catch (e: Exception) {
            logE(e)
        }
    }

    @Throws(Throwable::class)
    override fun hook() {
        if (!Helper.prefs.getBoolean("switch_mainswitch", false)
            || !Helper.prefs.getBoolean("switch_vipbanner", false)
        ) {
            return
        }

        if (handleVipData != null) {
            hookMethod(handleVipData, DO_NOTHING)
        }

        if (moreVipData != null) {
            hookAllMethods(moreVipData, "isLegal", returnConstant(false))
        }

    }
}
