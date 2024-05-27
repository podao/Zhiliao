package com.shatyuka.zhiliao.hooks

import com.shatyuka.zhiliao.Helper
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge.hookMethod
import java.io.Reader
import java.lang.reflect.Method
import java.util.function.Function

class SearchAd : BaseHook() {

    private var processors: MutableMap<Class<*>, Function<Any, Any>> = HashMap()

    private lateinit var readObject: Method

    override fun getName(): String {
        return "去搜索推荐"
    }

    override fun init(classLoader: ClassLoader) {

        readObject = findReadObjectMethod(classLoader)

        try {
            val searchTopTabsItemList =
                classLoader.loadClass("com.zhihu.android.api.model.SearchTopTabsItemList")
            val commercialData = searchTopTabsItemList.getField("commercialData")

            processors.put(searchTopTabsItemList) {
                commercialData.set(it, null)
            }
        } catch (e: Exception) {
            logE("SearchTopTabsItemList.commercialData ${e.message}")
        }

        try {
            val searchRecommendQuery =
                classLoader.loadClass("com.zhihu.android.api.model.SearchRecommendQuery")
            val field = try {
                searchRecommendQuery.getField("recommendQueries")
            } catch (ignore: Exception) {
                searchRecommendQuery.getField("content")
            }
            processors.put(searchRecommendQuery) {
                field.set(it, null)
            }
        } catch (e: Exception) {
            logE("SearchRecommendQuery.content ${e.message}")
        }

        try {
            val presetWords = classLoader.loadClass("com.zhihu.android.api.model.PresetWords")
            val preset = presetWords.getField("preset")
            processors.put(presetWords) {
                preset.set(it, null)
            }
        } catch (e: Exception) {
            logE("PresetWords.preset ${e.message}")
        }

        try {
            val presetWords =
                classLoader.loadClass("com.zhihu.android.service.search_service.model.PresetWords")
            val preset = presetWords.getField("preset")
            processors.put(presetWords) {
                preset.set(it, null)
            }
        } catch (e: Exception) {
            logE("PresetWords2.preset ${e.message}")
        }

        try {
            val hotSearchBeanClass =
                classLoader.loadClass("com.zhihu.android.api.model.HotSearchBean")
            val searchHotList = hotSearchBeanClass.getField("searchHotList")
            processors.put(hotSearchBeanClass) {
                searchHotList.set(it, ArrayList<Any>())
            }
        } catch (e: Exception) {
            logE("HotSearchBean.searchHotList ${e.message}")
        }
    }

    override fun hook() {
        if (!Helper.prefs.getBoolean("switch_mainswitch", false)
            || !Helper.prefs.getBoolean("switch_searchad", true)
        ) {
            return
        }

        hookMethod(readObject, object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                if (param.result != null) {
                    // logE("resultClass: ${param.result.javaClass}")
                    processors[param.result.javaClass]?.apply(param.result)
                }
            }
        })
    }

    private fun findReadObjectMethod(classloader: ClassLoader): Method {
        return classloader.loadClass("com.fasterxml.jackson.databind.ObjectReader").declaredMethods
            .filter {
                it.parameterCount == 1 && it.parameterTypes[0] == Reader::class.java
            }.filter {
                it.returnType == Object::class.java
            }[0]
    }

}
