package com.shatyuka.zhiliao.hooks

import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.shatyuka.zhiliao.Helper
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XC_MethodReplacement.returnConstant
import de.robv.android.xposed.XposedBridge
import java.lang.reflect.Field


class FollowButtonFeatureUI : IHook {

    private lateinit var followWithAvatarView: Class<*>
    private lateinit var bottomReactionViewImpl: Class<*>
    private lateinit var followButtonViewImpl: Class<*>
    private lateinit var followPeopleButton: Class<*>
    private lateinit var followWithAvatarViewFromImplField: Field
    private lateinit var followPeopleButtonField: Field
    private var followModelKt: Class<*>? = null

    override fun getName(): String {
        return "去关注按钮(FeatureUI)"
    }

    @Throws(Throwable::class)
    override fun init(classLoader: ClassLoader) {
        bottomReactionViewImpl =
            classLoader.loadClass("com.zhihu.android.feature.short_container_feature.ui.widget.impl.BottomReactionViewImpl")
        followWithAvatarView =
            classLoader.loadClass("com.zhihu.android.unify_interactive.view.follow.FollowWithAvatarView")
        followWithAvatarViewFromImplField =
            Helper.findFieldByType(bottomReactionViewImpl, followWithAvatarView)
        followButtonViewImpl =
            classLoader.loadClass("com.zhihu.android.feature.short_container_feature.ui.widget.impl.FollowButtonViewImpl")
        followPeopleButton =
            classLoader.loadClass("com.zhihu.android.unify_interactive.view.follow.FollowPeopleButton")
        followPeopleButtonField = Helper.findFieldByType(followButtonViewImpl, followPeopleButton)

        try {
            followModelKt =
                classLoader.loadClass("com.zhihu.android.unify_interactive.model.follow.FollowModelKt")
        } catch (e: Exception) {
            Helper.logD(this::class.simpleName, e)
        }
    }

    @Throws(Throwable::class)
    override fun hook() {
        XposedBridge.hookAllMethods(bottomReactionViewImpl, "setData", object : XC_MethodHook() {
            @Throws(IllegalAccessException::class)
            override fun afterHookedMethod(param: MethodHookParam) {
                if (Helper.prefs.getBoolean("switch_mainswitch", false)
                    && Helper.prefs.getBoolean("switch_subscribe", false)
                ) {
                    val followWithAvatarViewInstance =
                        followWithAvatarViewFromImplField.get(param.thisObject) as FrameLayout
                    followWithAvatarViewInstance.visibility = View.GONE
                }
            }
        })
        XposedBridge.hookAllMethods(followButtonViewImpl, "setData", object : XC_MethodHook() {
            @Throws(IllegalAccessException::class)
            override fun afterHookedMethod(param: MethodHookParam) {
                if (Helper.prefs.getBoolean("switch_mainswitch", false)
                    && Helper.prefs.getBoolean("switch_subscribe", false)
                ) {
                    val followPeopleButtonInstance =
                        followPeopleButtonField.get(param.thisObject) as ViewGroup
                    followPeopleButtonInstance.visibility = View.GONE
                }
            }
        })

        if (followModelKt != null) {
            XposedBridge.hookAllMethods(
                followModelKt,
                "showFollow",
                returnConstant(false)
            )
        }

    }
}
