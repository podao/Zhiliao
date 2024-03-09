package com.shatyuka.zhiliao.hooks

import android.view.View
import com.shatyuka.zhiliao.Helper
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import java.lang.reflect.Field
import java.util.Arrays

class MineHybridView : IHook {

    private lateinit var mineTabFragment: Class<*>
    private lateinit var mineHybridView: Class<*>
    private lateinit var mineHybridViewField: Field

    override fun getName(): String {
        return "隐藏「我的」底部混合卡片"
    }

    @Throws(Throwable::class)
    override fun init(classLoader: ClassLoader) {
        mineTabFragment =
            classLoader.loadClass("com.zhihu.android.app.ui.fragment.more.mine.MineTabFragment")
        mineHybridView =
            classLoader.loadClass("com.zhihu.android.app.ui.fragment.more.mine.widget.MineHybridView")
        mineHybridViewField = Arrays.stream(mineTabFragment.declaredFields)
            .filter { field: Field -> field.type == mineHybridView }
            .findFirst().get()
        mineHybridViewField.isAccessible = true
    }

    @Throws(Throwable::class)
    override fun hook() {
        XposedBridge.hookAllMethods(mineTabFragment, "onCreateView", object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun afterHookedMethod(param: MethodHookParam) {
                if (Helper.prefs.getBoolean("switch_mainswitch", false)
                    && Helper.prefs.getBoolean("switch_minehybrid", false)
                ) {
                    val mineHybridView = mineHybridViewField.get(param.thisObject) as View
                    mineHybridView.visibility = View.GONE
                }
            }
        })
    }
}
