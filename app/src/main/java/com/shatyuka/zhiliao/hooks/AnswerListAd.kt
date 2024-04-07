package com.shatyuka.zhiliao.hooks

import com.shatyuka.zhiliao.Helper
import de.robv.android.xposed.XC_MethodReplacement.DO_NOTHING
import de.robv.android.xposed.XposedBridge.hookAllMethods

class AnswerListAd : IHook {

    private var answerListWrapper: Class<*>? = null

    override fun getName(): String {
        return "去回答列表广告"
    }

    override fun init(classLoader: ClassLoader) {
        answerListWrapper =
            classLoader.loadClass("com.zhihu.android.question.api.model.AnswerListWrapper")
    }

    override fun hook() {
        if (!Helper.prefs.getBoolean("switch_mainswitch", false)
            || !Helper.prefs.getBoolean("switch_answerlistad", true)
        ) {
            return
        }

        hookAllMethods(answerListWrapper, "insertAdBrandToList", DO_NOTHING)
    }
}
