package com.shatyuka.zhiliao.hooks

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.view.ViewPropertyAnimator
import com.shatyuka.zhiliao.Helper
import de.robv.android.xposed.XC_MethodReplacement.DO_NOTHING
import de.robv.android.xposed.XposedBridge.hookMethod
import org.luckypray.dexkit.query.matchers.ClassMatcher
import java.lang.reflect.Method

class FullScreen : BaseHook() {

    private var setHandler: Method? = null

    override fun getName(): String {
        return "禁止进入全屏模式"
    }

    @Throws(Throwable::class)
    override fun init(classLoader: ClassLoader) {
        val clearScreenHelper = findClearScreenHelper(classLoader)
        if (clearScreenHelper != null) {
            setHandler = Helper.getMethodByParameterTypes(
                clearScreenHelper,
                Context::class.java,
                ViewGroup::class.java,
                View::class.java
            )
        }

        if (setHandler == null) {
            logE("no method: ClearScreenHelper#setHandler")
        }
    }

    @Throws(Throwable::class)
    override fun hook() {
        if (!Helper.prefs.getBoolean("switch_mainswitch", false)
            || !Helper.prefs.getBoolean("switch_fullscreen", true)
        ) {
            return
        }

        if (setHandler != null) {
            hookMethod(setHandler, DO_NOTHING)
        }
    }

    private fun findClearScreenHelper(classLoader: ClassLoader): Class<*>? {
        val classMather = ClassMatcher.create().fields {
            addForType(View::class.java)
            addForType(View::class.java)
            addForType(ViewPropertyAnimator::class.java)
            addForType(Integer::class.javaPrimitiveType as Class<*>)
            addForType(Boolean::class.javaPrimitiveType as Class<*>)
        }

        val clearScreenHelper = Helper.findClass(
            listOf("com.zhihu.android.feature.short_container_feature.ui.widget.toolbar.clearscreen"),
            null,
            classMather,
            classLoader
        )

        if (clearScreenHelper == null) {
            logE("no class: com.zhihu.android.feature.short_container_feature.ui.widget.toolbar.clearscreen.ClearScreenHelper")
        }

        return clearScreenHelper
    }
}
