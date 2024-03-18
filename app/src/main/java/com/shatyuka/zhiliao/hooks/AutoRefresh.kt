package com.shatyuka.zhiliao.hooks

import com.shatyuka.zhiliao.Helper
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XC_MethodReplacement.returnConstant
import de.robv.android.xposed.XposedBridge.hookMethod
import org.luckypray.dexkit.query.matchers.MethodMatcher
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.util.Arrays

class AutoRefresh : IHook {

    private var feedAutoRefreshManager_shouldRefresh: Method? = null
    private var feedHotRefreshAbConfig_shouldRefresh: Method? = null
    private var mainPageFragment_getFragment: Method? = null

    override fun getName(): String {
        return "关闭首页自动刷新"
    }

    @Throws(Throwable::class)
    override fun init(classLoader: ClassLoader) {
        feedAutoRefreshManager_shouldRefresh =
            findFeedAutoRefreshManagerShouldRefreshMethod(classLoader)
        feedHotRefreshAbConfig_shouldRefresh =
            findFeedHotRefreshAbConfigShouldRefreshMethod(classLoader)

        val mainPageFragment =
            classLoader.loadClass("com.zhihu.android.app.feed.explore.view.MainPageFragment")
        val fragment = classLoader.loadClass("androidx.fragment.app.Fragment")
        try {
            mainPageFragment_getFragment = Arrays.stream(mainPageFragment.getDeclaredMethods())
                .filter { method: Method -> Modifier.isFinal(method.modifiers) }
                .filter { method: Method -> method.returnType == fragment }
                .filter { method: Method -> method.parameterCount == 0 }.findFirst().get()
        } catch (e: Exception) {
            logE(e)
        }
    }

    @Throws(Throwable::class)
    override fun hook() {
        if (!Helper.prefs.getBoolean("switch_mainswitch", false)
            || !Helper.prefs.getBoolean("switch_autorefresh", true)
        ) {
            return
        }

        if (feedAutoRefreshManager_shouldRefresh != null) {
            hookMethod(feedAutoRefreshManager_shouldRefresh, object : XC_MethodHook() {
                @Throws(Throwable::class)
                override fun beforeHookedMethod(param: MethodHookParam) {
                    param.args[0] = 0
                }
            })
        }

        if (feedHotRefreshAbConfig_shouldRefresh != null) {
            hookMethod(feedHotRefreshAbConfig_shouldRefresh, returnConstant(false))
        }

        if (mainPageFragment_getFragment != null) {
            hookMethod(mainPageFragment_getFragment, returnConstant(null))
        }
    }

    private fun findFeedHotRefreshAbConfigShouldRefreshMethod(classLoader: ClassLoader): Method? {
        val matcher: MethodMatcher = MethodMatcher.create()
            .returnType(Boolean::class.javaPrimitiveType as Class<*>)
            .paramCount(1)
            .paramTypes(Long::class.javaPrimitiveType)

        val methodList = Helper.findMethodList(
            listOf("com.zhihu.android.app.feed.util"),
            null,
            matcher,
            classLoader
        )
        if (methodList.isEmpty()) {
            logE(NoSuchMethodException("com.zhihu.android.app.feed.util.FeedHotRefreshAbConfig#shouldRefresh"))
            return null
        }
        if (methodList.size > 1) {
            logE(IllegalStateException("multi methods have bool(long)"))
            return null
        }

        return methodList[0]
    }

    private fun findFeedAutoRefreshManagerShouldRefreshMethod(classLoader: ClassLoader): Method? {
        val matcher: MethodMatcher = MethodMatcher.create()
            .returnType(Void::class.javaPrimitiveType as Class<*>)
            .paramCount(4)
            .paramTypes(Long::class.javaPrimitiveType, Int::class.javaPrimitiveType, null, null)

        val methodList = Helper.findMethodList(
            listOf("com.zhihu.android.app.feed.util"),
            null,
            matcher,
            classLoader
        )
        if (methodList.isEmpty()) {
            logE(NoSuchMethodException("com.zhihu.android.app.feed.util.FeedAutoRefreshManager#shouldRefresh"))
            return null
        }
        if (methodList.size > 1) {
            logE(IllegalStateException("multi methods have void(long,int,object,object)"))
            return null
        }

        return methodList[0]
    }

    private fun logE(e: Exception) {
        Helper.logD(this::class.simpleName, e)
    }

}
