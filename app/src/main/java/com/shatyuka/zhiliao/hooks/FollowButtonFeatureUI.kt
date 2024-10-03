package com.shatyuka.zhiliao.hooks

import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.shatyuka.zhiliao.Helper
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XC_MethodReplacement.returnConstant
import de.robv.android.xposed.XposedBridge.hookAllMethods
import java.lang.reflect.Field


class FollowButtonFeatureUI : BaseHook() {

    private lateinit var followWithAvatarView: Class<*>
    private var followButtonViewImpl: Class<*>? = null
    private lateinit var followPeopleButton: Class<*>
    private lateinit var followPeopleButtonField: Field
    private var followModelKt: Class<*>? = null

    override fun getName(): String {
        return "去关注按钮(FeatureUI)"
    }

    @Throws(Throwable::class)
    override fun init(classLoader: ClassLoader) {
        try {
            followWithAvatarView =
                classLoader.loadClass("com.zhihu.android.unify_interactive.view.follow.FollowWithAvatarView")
        } catch (e: Exception) {
            logE(e)
        }

        try {
            followButtonViewImpl =
                classLoader.loadClass("com.zhihu.android.feature.short_container_feature.ui.widget.impl.FollowButtonViewImpl")
            followPeopleButton =
                classLoader.loadClass("com.zhihu.android.unify_interactive.view.follow.FollowPeopleButton")
            followPeopleButtonField =
                Helper.findFieldByType(followButtonViewImpl, followPeopleButton)
        } catch (e: Exception) {
            logE(e)
        }

        try {
            followModelKt =
                classLoader.loadClass("com.zhihu.android.unify_interactive.model.follow.FollowModelKt")
        } catch (e: Exception) {
            logE(e.message)
        }
    }

    @Throws(Throwable::class)
    override fun hook() {
        if (!Helper.prefs.getBoolean("switch_mainswitch", false)
            || !Helper.prefs.getBoolean("switch_subscribe", false)
        ) {
            return
        }

        if (followButtonViewImpl != null) {
            hookAllMethods(followButtonViewImpl, "setData", object : XC_MethodHook() {
                @Throws(IllegalAccessException::class)
                override fun afterHookedMethod(param: MethodHookParam) {
                    val followPeopleButtonInstance =
                        followPeopleButtonField.get(param.thisObject) as ViewGroup
                    followPeopleButtonInstance.visibility = View.GONE
                }
            })
        }

        if (followModelKt != null) {
            hookAllMethods(followModelKt, "showFollow", returnConstant(false))
        }

    }
}
