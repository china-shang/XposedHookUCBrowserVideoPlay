package com.example.zh.xposed1;


import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import java.util.Map;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Created by zh on 18-3-8.
 */
public class Hook implements IXposedHookLoadPackage {

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

        if (!lpparam.packageName.toLowerCase().startsWith("com.uc")) {
            return;
        }
        XposedBridge.log(lpparam.packageName);

        XposedHelpers.findAndHookMethod(Application.class, "attach", Context.class, new HookIniter());
    }

    private static class HookIniter extends XC_MethodHook {
        private ClassLoader realClassLoader;

        public ClassLoader getRealClassLoader() {
            return realClassLoader;
        }

        @Override
        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
            this.realClassLoader = ((Context) param.args[0]).getClassLoader();

            hook1();
            hook2();
        }


        private void hook1() {
            Class<?> mediaPlayClass = null;
            try {
                mediaPlayClass = this.getRealClassLoader().loadClass("com.UCMobile.Apollo.MediaPlayer");
            } catch (Exception e) {
                XposedBridge.log("not find MediaPlayer");
            }

            XposedBridge.log("find MediaPlayer");
            XposedHelpers.findAndHookMethod(mediaPlayClass, "setDataSource", String.class, Map.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
//                        String url=param.args[0].toString();
                    String url = (String) XposedHelpers.getObjectField(param.thisObject, "w");
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    intent.setDataAndType(Uri.parse(url), "video/*");

                    Context content = (Context) XposedHelpers.getObjectField(param.thisObject, "m");
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    XposedHelpers.callMethod(param.thisObject, "pause");
                    content.startActivity(intent);

                    XposedBridge.log("in setDataSource");
                    XposedBridge.log("url:" + url);

                }
            });
        }

        private void hook2() {
            Class<?> hookClass1 = null;
            try {
                hookClass1 = this.getRealClassLoader().loadClass("com.uc.apollo.media.widget.MediaViewImpl");
            } catch (Exception e) {
                XposedBridge.log("not find");

            }
            XposedBridge.log("find MediaViewImpl");

            XposedHelpers.findAndHookConstructor(hookClass1, Context.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                    XposedBridge.log("in constructor");
                    XposedHelpers.setAdditionalStaticField(param.thisObject.getClass(), "content", param.args[0]);
                }
            });

            XposedHelpers.findAndHookConstructor(hookClass1, Context.class, int.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                    XposedBridge.log("in constructor");
                    XposedHelpers.setAdditionalStaticField(param.thisObject.getClass(), "content", param.args[0]);
                }
            });
            XposedHelpers.findAndHookConstructor(hookClass1, Context.class, int.class, boolean.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                    XposedBridge.log("in constructor");
                    XposedHelpers.setAdditionalStaticField(param.thisObject.getClass(), "content", param.args[0]);
                }
            });

            XposedHelpers.findAndHookMethod(hookClass1, "setVideoURI", Uri.class,
                    Map.class, new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            super.beforeHookedMethod(param);
                            XposedBridge.log("Also Success");
                            String uri = param.args[0].toString();

                            XposedBridge.log("uri=" + uri);

                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                            intent.setDataAndType(Uri.parse(uri), "video/*");

                            XposedHelpers.callMethod(param.thisObject, "pause");

                            Context context = (Context) XposedHelpers.getAdditionalStaticField(param.thisObject.getClass(), "content");
                            context.startActivity(intent);

                        }
                    });

        }
    }
}
