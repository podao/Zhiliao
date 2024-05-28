package com.shatyuka.zhiliao.hooks

import com.shatyuka.zhiliao.Helper
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge.hookMethod
import de.robv.android.xposed.XposedHelpers
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

            processors.put(searchTopTabsItemList) {
                XposedHelpers.setObjectField(it, "commercialData", null)
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

            processors.put(presetWords) {
                XposedHelpers.setObjectField(it, "preset", null)
            }
        } catch (e: Exception) {
            logE("PresetWords.preset ${e.message}")
        }

        try {
            val presetWords =
                classLoader.loadClass("com.zhihu.android.service.search_service.model.PresetWords")

            processors.put(presetWords) {
                XposedHelpers.setObjectField(it, "preset", null)
            }
        } catch (e: Exception) {
            logE("PresetWords2.preset ${e.message}")
        }

        try {
            val hotSearchBeanClass =
                classLoader.loadClass("com.zhihu.android.api.model.HotSearchBean")

            processors.put(hotSearchBeanClass) {
                XposedHelpers.setObjectField(it, "searchHotList", ArrayList<Any>())
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
                if (param.result == null) {
                    return
                }
                val processor = processors[param.result.javaClass]
                if (processor != null) {
                    try {
                        processor.apply(param.result)
                    } catch (e: Exception) {
                        logE("can not apply process, resultClass:${param.result.javaClass}, error:$e")
                    }
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
