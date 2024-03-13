package com.shatyuka.zhiliao.hooks;

import android.view.View;
import android.view.ViewGroup;

import com.shatyuka.zhiliao.Helper;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedHelpers;

public class NextAnswer implements IHook {
    static Class<?> NextContentAnimationView;
    static Class<?> NextContentAnimationView_short;
    static Class<?> AnswerPagerFragment;

    @Override
    public String getName() {
        return "移除下一个回答按钮";
    }

    @Override
    public void init(ClassLoader classLoader) throws Throwable {
        NextContentAnimationView = classLoader.loadClass("com.zhihu.android.mix.widget.NextContentAnimationView");
        NextContentAnimationView_short = classLoader.loadClass("com.zhihu.android.mixshortcontainer.function.next.NextContentAnimationView");
        AnswerPagerFragment = classLoader.loadClass("com.zhihu.android.answer.module.pager.AnswerPagerFragment");
    }

    @Override
    public void hook() throws Throwable {
        if (Helper.prefs.getBoolean("switch_mainswitch", false) && Helper.prefs.getBoolean("switch_nextanswer", false)) {
            XposedHelpers.findAndHookMethod(AnswerPagerFragment, "setupNextAnswerBtn", XC_MethodReplacement.returnConstant(null));

            XposedHelpers.findAndHookMethod(ViewGroup.class, "addView", View.class, ViewGroup.LayoutParams.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    if (NextContentAnimationView.isAssignableFrom(param.args[0].getClass()) || (NextContentAnimationView_short != null && NextContentAnimationView_short.isAssignableFrom(param.args[0].getClass())))
                        ((View) param.args[0]).setVisibility(View.GONE);
                }
            });

            XposedHelpers.findAndHookMethod(View.class, "setVisibility", int.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    if (NextContentAnimationView.isAssignableFrom(param.thisObject.getClass()) || (NextContentAnimationView_short != null && NextContentAnimationView_short.isAssignableFrom(param.args[0].getClass())))
                        param.args[0] = View.GONE;
                }
            });

        }
    }
}
