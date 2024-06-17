package com.shatyuka.zhiliao.hooks

import com.shatyuka.zhiliao.Helper
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge.hookMethod
import de.robv.android.xposed.XposedHelpers.getObjectField
import de.robv.android.xposed.XposedHelpers.setObjectField
import java.util.stream.Collectors

/**
 * api: /root/tab
 */
class TopTabs : BaseHook() {

    private val PREFS_KEY_NAME = "edit_tabs"

    private var activityInfo: Class<*>? = null
    private var topTabs: Class<*>? = null

    override fun getName(): String {
        return "自定义首页顶部Tab(TopTabs)"
    }

    override fun init(classLoader: ClassLoader) {
        try {
            activityInfo = classLoader.loadClass("com.zhihu.android.api.model.ActivityInfo")
        } catch (e: Exception) {
            logE(e.message)
        }

        try {
            topTabs = classLoader.loadClass("com.zhihu.android.api.model.TopTabs")
        } catch (e: Exception) {
            logE(e.message)
        }
    }

    override fun hook() {
        if (!Helper.prefs.getBoolean("switch_mainswitch", false)) {
            return
        }

        hookMethod(Helper.JacksonHelper.ObjectReader_readValue, object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                if (param.result == null) {
                    return
                }
                when (param.result.javaClass) {
                    activityInfo -> {
                        setObjectField(param.result, "topActivity", null)
                    }

                    topTabs -> {
                        postProcessTopTabs(param.result)
                    }
                }
            }
        })
    }

    private fun postProcessTopTabs(topTabs: Any) {
        setObjectField(topTabs, "topActivity", null)
        filterTabs(topTabs)
    }

    private fun filterTabs(topTabs: Any) {
        val tabList = getObjectField(topTabs, "tabs") as List<*>

        val resultTabList = ArrayList<Any>()
        val tabTypeMap = HashMap<String, Any>()
        val originTabTypeList = ArrayList<String>()

        tabList.forEach {
            val tabType = getObjectField(it, "tabType") as String
            tabTypeMap[tabType] = it!!
            originTabTypeList.add(tabType)
        }

        getAllowedTabTypeList().forEach {
            val tab = tabTypeMap[it]
            if (tab != null) {
                resultTabList.add(tab)
            }
        }

        if (resultTabList.isNotEmpty()) {
            setObjectField(topTabs, "tabs", resultTabList)
        } else {
            resetAllowedTabTypeList(originTabTypeList)
        }
    }

    private fun getAllowedTabTypeList(): List<String> {
        return Helper.prefs.getString(PREFS_KEY_NAME, "")?.split("|")?.stream()
            ?.map { it.trim() }?.collect(Collectors.toList()) ?: ArrayList()
    }

    private fun resetAllowedTabTypeList(tabTypeList: List<String>) {
        Helper.prefs.edit().putString(
            PREFS_KEY_NAME, tabTypeList.stream().collect(Collectors.joining("|"))
        ).apply()
    }
}