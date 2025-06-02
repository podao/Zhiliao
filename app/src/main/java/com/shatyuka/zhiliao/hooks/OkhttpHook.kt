package com.shatyuka.zhiliao.hooks

import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge.hookAllMethods
import de.robv.android.xposed.XposedHelpers
import java.lang.reflect.Method

object OkhttpHook : BaseHook() {

    private var JSONBodyInterceptorList = ArrayList<JSONBodyInterceptor>()

    fun addInterceptors(interceptor: JSONBodyInterceptor) {
        JSONBodyInterceptorList.add(interceptor)
    }

    private lateinit var realCallClass: Class<*>
    private lateinit var toResponseBodyMethod: Method

    override fun getName(): String {
        return "okhttp hooker"
    }

    override fun init(classLoader: ClassLoader) {
        realCallClass = classLoader.loadClass("okhttp3.RealCall")
        toResponseBodyMethod = classLoader.loadClass("okhttp3.ResponseBody").getDeclaredMethod(
            "create", XposedHelpers.findClass("okhttp3.MediaType", classLoader), String::class.java
        )
    }

    override fun hook() {
        hookAllMethods(
            realCallClass, "getResponseWithInterceptorChain", object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    val responseObj = param.result ?: return

                    val bodyObj = XposedHelpers.callMethod(responseObj, "body") ?: return
                    val mediaTypeObj = XposedHelpers.callMethod(bodyObj, "contentType") ?: return
                    val type = XposedHelpers.callMethod(mediaTypeObj, "type")
                    val subType = XposedHelpers.callMethod(mediaTypeObj, "subtype")

                    // 只关心json内容
                    if (!"$type/$subType".contains("application/json")) {
                        return
                    }

                    var originalContent =
                        XposedHelpers.callMethod(bodyObj, "string") as? String ?: ""
                    val urlObj = XposedHelpers.callMethod(
                        XposedHelpers.callMethod(responseObj, "request"), "url"
                    )
                    for (interceptor in JSONBodyInterceptorList) {
                        if (interceptor.match(urlObj.toString())) {
                            originalContent = interceptor.intercept(originalContent)
                        }
                    }

                    val newBody = toResponseBodyMethod.invoke(null, mediaTypeObj, originalContent)
                    val builderObj = XposedHelpers.callMethod(responseObj, "newBuilder")
                    XposedHelpers.callMethod(builderObj, "body", newBody)

                    param.result = XposedHelpers.callMethod(builderObj, "build")
                }
            })
    }
}

interface JSONBodyInterceptor {
    fun match(url: String): Boolean
    fun intercept(body: String): String
}