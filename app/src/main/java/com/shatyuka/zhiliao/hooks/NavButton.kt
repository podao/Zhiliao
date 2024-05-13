package com.shatyuka.zhiliao.hooks

import android.view.View
import com.shatyuka.zhiliao.Helper
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge.hookMethod
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.util.Arrays

class NavButton : BaseHook() {

    private lateinit var bottomNavMenuView: Class<*>
    private lateinit var iMenuItem: Class<*>
    private lateinit var getItemId: Method
    private lateinit var tabView: Field

    override fun getName(): String {
        return "隐藏导航栏按钮"
    }

    @Throws(Throwable::class)
    override fun init(classLoader: ClassLoader) {
        bottomNavMenuView =
            classLoader.loadClass("com.zhihu.android.bottomnav.core.BottomNavMenuView")
        val tabLayoutTabClass =
            classLoader.loadClass("com.google.android.material.tabs.TabLayout\$Tab")
        iMenuItem = Arrays.stream(bottomNavMenuView.getDeclaredMethods())
            .filter { method: Method -> method.returnType == tabLayoutTabClass }
            .map { method: Method -> method.getParameterTypes()[0] }.findFirst().get()
        getItemId = Arrays.stream(iMenuItem.getDeclaredMethods())
            .filter { method: Method -> method.returnType == String::class.java }.findFirst().get()
        tabView = tabLayoutTabClass.getField("view")
    }

    @Throws(Throwable::class)
    override fun hook() {
        if (!Helper.prefs.getBoolean("switch_mainswitch", false)) {
            return
        }

        val switchMap: Map<String, Boolean> = object : HashMap<String, Boolean>() {
            init {
                put("market", Helper.prefs.getBoolean("switch_vipnav", false))
                put("video", Helper.prefs.getBoolean("switch_videonav", false))
                put("friend", Helper.prefs.getBoolean("switch_friendnav", false))
                put("panel", Helper.prefs.getBoolean("switch_panelnav", false))
                put("find", Helper.prefs.getBoolean("switch_findnav", false))
            }
        }

        hookMethod(Helper.getMethodByParameterTypes(bottomNavMenuView, iMenuItem),
            object : XC_MethodHook() {
                @Throws(Throwable::class)
                override fun afterHookedMethod(param: MethodHookParam) {
                    if (switchMap[getItemId.invoke(param.args[0])] == true) {
                        (tabView[param.result] as View).visibility = View.GONE
                    }
                }
            })

    }
}
