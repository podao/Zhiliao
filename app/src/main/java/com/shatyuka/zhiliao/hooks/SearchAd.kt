package com.shatyuka.zhiliao.hooks

import com.shatyuka.zhiliao.Helper
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge.hookMethod
import de.robv.android.xposed.XposedHelpers
import java.util.function.Function

class SearchAd : BaseHook() {

    private val PROCESSORS: MutableMap<Class<*>, Function<Any, Any>> = HashMap()

    override fun getName(): String {
        return "去搜索推荐"
    }

    override fun init(classLoader: ClassLoader) {
        try {
            val searchTopTabsItemList =
                classLoader.loadClass("com.zhihu.android.api.model.SearchTopTabsItemList")

            PROCESSORS.put(searchTopTabsItemList) {
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
            PROCESSORS.put(searchRecommendQuery) {
                field.set(it, null)
            }
        } catch (e: Exception) {
            logE("SearchRecommendQuery.content ${e.message}")
        }

        try {
            val presetWords = classLoader.loadClass("com.zhihu.android.api.model.PresetWords")

            PROCESSORS.put(presetWords) {
                XposedHelpers.setObjectField(it, "preset", null)
            }
        } catch (e: Exception) {
            logE("PresetWords.preset ${e.message}")
        }

        try {
            val presetWords =
                classLoader.loadClass("com.zhihu.android.service.search_service.model.PresetWords")

            PROCESSORS.put(presetWords) {
                XposedHelpers.setObjectField(it, "preset", null)
            }
        } catch (e: Exception) {
            logE("PresetWords2.preset ${e.message}")
        }

        try {
            val hotSearchBeanClass =
                classLoader.loadClass("com.zhihu.android.api.model.HotSearchBean")

            PROCESSORS.put(hotSearchBeanClass) {
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

        hookMethod(Helper.JacksonHelper.ObjectReader_readValue, object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                if (param.result == null) {
                    return
                }
                val processor = PROCESSORS[param.result.javaClass]
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
}
