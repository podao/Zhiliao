package com.shatyuka.zhiliao.hooks

import android.view.View
import android.view.ViewGroup
import com.shatyuka.zhiliao.Helper
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XC_MethodReplacement
import de.robv.android.xposed.XC_MethodReplacement.DO_NOTHING
import de.robv.android.xposed.XC_MethodReplacement.returnConstant
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedBridge.hookMethod
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.XposedHelpers.findAndHookConstructor
import java.lang.reflect.Method

class RedDot : IHook {

    private var FeedsTabsFragment: Class<*>? = null
    private var NotiMsgModel: Class<*>? = null
    private var ViewModel: Class<*>? = null
    private var BottomNavMenuItemView_setUnreadCount: Method? = null
    private var BottomNavMenuItemViewForIconOnly_setUnreadCount: Method? = null
    private var BaseBottomNavMenuItemView_setNavBadge: Method? = null
    private var IconWithDotAndCountView_setUnreadCount: Method? = null
    private var CountDotView_setUnreadCount: Method? = null
    private var BaseFeedFollowAvatarViewHolder_setUnreadTipVisibility: Method? = null
    private var RevisitView_getCanShowRedDot: Method? = null
    private var customTabView: Class<*>? = null

    override fun getName(): String {
        return "不显示小红点"
    }

    @Throws(Throwable::class)
    override fun init(classLoader: ClassLoader) {
        FeedsTabsFragment =
            classLoader.loadClass("com.zhihu.android.app.feed.ui.fragment.FeedsTabsFragment")

        try {
            NotiMsgModel =
                classLoader.loadClass("com.zhihu.android.notification.model.viewmodel.NotiMsgModel")
        } catch (e: Exception) {
            logD(e)
        }

        try {
            ViewModel =
                classLoader.loadClass("com.zhihu.android.app.feed.ui.fragment.help.tabhelp.model.ViewModel")
        } catch (e: Exception) {
            logD(e)
        }
        try {
            BottomNavMenuItemView_setUnreadCount = Helper.getMethodByParameterTypes(
                classLoader.loadClass("com.zhihu.android.bottomnav.core.BottomNavMenuItemView"),
                Int::class.javaPrimitiveType
            )
        } catch (e: Exception) {
            logD(e)
        }
        try {
            BottomNavMenuItemViewForIconOnly_setUnreadCount = Helper.getMethodByParameterTypes(
                classLoader.loadClass("com.zhihu.android.bottomnav.core.BottomNavMenuItemViewForIconOnly"),
                Int::class.javaPrimitiveType
            )
        } catch (e: Exception) {
            logD(e)
        }

        try {
            BaseBottomNavMenuItemView_setNavBadge = Helper.getMethodByParameterTypes(
                classLoader.loadClass("com.zhihu.android.bottomnav.core.BaseBottomNavMenuItemView"),
                classLoader.loadClass("com.zhihu.android.bottomnav.api.model.NavBadge")
            )
        } catch (e: Exception) {
            logD(e)
        }
        try {
            IconWithDotAndCountView_setUnreadCount = Helper.getMethodByParameterTypes(
                classLoader.loadClass("com.zhihu.android.community_base.view.icon.IconWithDotAndCountView"),
                Int::class.javaPrimitiveType,
                Boolean::class.javaPrimitiveType,
                Int::class.javaPrimitiveType
            )
        } catch (e: Exception) {
            logD(e)
        }

        try {
            CountDotView_setUnreadCount = Helper.getMethodByParameterTypes(
                classLoader.loadClass("com.zhihu.android.notification.widget.CountDotView"),
                Int::class.javaPrimitiveType,
                Boolean::class.javaPrimitiveType
            )
        } catch (e: Exception) {
            logD(e)
        }

        try {
            BaseFeedFollowAvatarViewHolder_setUnreadTipVisibility =
                Helper.getMethodByParameterTypes(
                    classLoader.loadClass("com.zhihu.android.recentlyviewed.ui.viewholder.BaseFeedFollowAvatarViewHolder"),
                    View::class.java,
                    Boolean::class.javaPrimitiveType
                )
        } catch (e: Exception) {
            logD(e)
        }

        try {
            RevisitView_getCanShowRedDot =
                classLoader.loadClass("com.zhihu.android.app.feed.ui2.tab.RevisitView")
                    .getDeclaredMethod("getCanShowRedDot")
        } catch (e: Exception) {
            logD(e)
        }

        try {
            customTabView =
                classLoader.loadClass("com.zhihu.android.app.feed.explore.view.CustomTabContainerView\$CustomTabView")
        } catch (e: Exception) {
            logD(e)
        }

    }

    @Throws(Throwable::class)
    override fun hook() {
        if (!Helper.prefs.getBoolean("switch_mainswitch", false)
            || !Helper.prefs.getBoolean("switch_reddot", false)
        ) {
            return
        }

        XposedBridge.hookAllMethods(
            FeedsTabsFragment, "onUnReadCountLoaded", returnConstant(null)
        )
        if (BaseFeedFollowAvatarViewHolder_setUnreadTipVisibility != null) {
            hookMethod(
                BaseFeedFollowAvatarViewHolder_setUnreadTipVisibility, returnConstant(null)
            )
        }
        if (BottomNavMenuItemView_setUnreadCount != null) {
            hookMethod(
                BottomNavMenuItemView_setUnreadCount, returnConstant(null)
            )
        }
        if (BottomNavMenuItemViewForIconOnly_setUnreadCount != null) {
            hookMethod(
                BottomNavMenuItemViewForIconOnly_setUnreadCount, returnConstant(null)
            )
        }
        if (BaseBottomNavMenuItemView_setNavBadge != null) {
            hookMethod(
                BaseBottomNavMenuItemView_setNavBadge, returnConstant(null)
            )
        }
        if (NotiMsgModel != null) {
            XposedHelpers.findAndHookMethod(
                NotiMsgModel, "getUnreadCount", returnConstant(0)
            )
        }

        if (IconWithDotAndCountView_setUnreadCount != null) {
            hookMethod(
                IconWithDotAndCountView_setUnreadCount, returnConstant(null)
            )
        }
        if (CountDotView_setUnreadCount != null) {
            hookMethod(CountDotView_setUnreadCount, object : XC_MethodReplacement() {
                override fun replaceHookedMethod(param: MethodHookParam): Any? {
                    val obj = param.thisObject as View
                    obj.visibility = View.GONE
                    return null
                }
            })
        }
        if (ViewModel != null) {
            findAndHookConstructor(ViewModel, View::class.java, object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    val view = param.args[0] as ViewGroup
                    if (view.childCount == 2) { // red_parent
                        view.visibility = View.GONE
                    }
                }
            })
        }
        if (RevisitView_getCanShowRedDot != null) {
            hookMethod(RevisitView_getCanShowRedDot, returnConstant(false))
        }

        if (customTabView != null) {
            hookMethod(
                Helper.getMethodByParameterTypes(
                    customTabView,
                    String::class.java
                ), DO_NOTHING
            )
        }
    }

    private fun logD(e: Exception) {
        Helper.logD(this::class.simpleName, e)
    }
}
