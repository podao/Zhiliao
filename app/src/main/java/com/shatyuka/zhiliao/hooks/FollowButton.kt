package com.shatyuka.zhiliao.hooks

import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.shatyuka.zhiliao.Helper
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge.hookAllMethods
import java.lang.reflect.Field

/**
 * 卡片视图去除关注按钮
 * todo: 整合web去除关注
 */
class FollowButton : BaseHook() {

    private var bottomReactionView: Class<*>? = null
    private lateinit var followWithAvatarView: Class<*>
    private lateinit var followWithAvatarViewField: Field

    private var zHAuthorInfoView: Class<*>? = null
    private lateinit var followPeopleButton: Class<*>
    private lateinit var followPeopleButtonField: Field

    override fun getName(): String {
        return "去关注按钮"
    }

    @Throws(Throwable::class)
    override fun init(classLoader: ClassLoader) {
        try {
            bottomReactionView =
                classLoader.loadClass("com.zhihu.android.mixshortcontainer.function.mixup.view.BottomReactionView")
            followWithAvatarView =
                classLoader.loadClass("com.zhihu.android.unify_interactive.view.follow.FollowWithAvatarView")
            followWithAvatarViewField =
                Helper.findFieldByType(bottomReactionView, followWithAvatarView)
        } catch (e: Exception) {
            logE(e)
        }

        try {
            zHAuthorInfoView =
                classLoader.loadClass("com.zhihu.android.mixshortcontainer.function.mixup.author.ZHAuthorInfoView")
            followPeopleButton =
                classLoader.loadClass("com.zhihu.android.unify_interactive.view.follow.FollowPeopleButton")
            followPeopleButtonField = Helper.findFieldByType(zHAuthorInfoView, followPeopleButton)
        } catch (e: Exception) {
            logE(e)
        }
    }

    @Throws(Throwable::class)
    override fun hook() {
        if (!Helper.prefs.getBoolean("switch_mainswitch", false)
            || !Helper.prefs.getBoolean("switch_subscribe", false)
        ) {
            return
        }

        if (bottomReactionView != null) {
            hookAllMethods(bottomReactionView, "setData", object : XC_MethodHook() {
                @Throws(Throwable::class)
                override fun afterHookedMethod(param: MethodHookParam) {
                    // 去除底部关注(带头像)
                    val followWithAvatarViewInstance =
                        followWithAvatarViewField.get(param.thisObject) as FrameLayout
                    followWithAvatarViewInstance.visibility = View.GONE
                }
            })
        }

        if (zHAuthorInfoView != null) {
            hookAllMethods(zHAuthorInfoView, "setData", object : XC_MethodHook() {
                @Throws(Throwable::class)
                override fun afterHookedMethod(param: MethodHookParam) {
                    // 去除顶部关注
                    val followPeopleButtonFieldInstance =
                        followPeopleButtonField.get(param.thisObject) as ViewGroup
                    followPeopleButtonFieldInstance.visibility = View.GONE
                }
            })
        }
    }

}
