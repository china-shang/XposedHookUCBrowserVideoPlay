package com.example.zh.xposed1;


import android.app.Activity;
import android.app.AndroidAppHelper;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.SurfaceHolder;
import android.widget.TextView;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import java.util.Map;

/**
 * Created by zh on 18-3-8.
 */

public class Hook implements IXposedHookLoadPackage {
    @Override

    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {



        XposedHelpers.findAndHookMethod(Application.class, "attach", Context.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                ClassLoader cl = ((Context)param.args[0]).getClassLoader();
                Context context=(Context)param.args[0];

                Class<?> hookClass1=null;
                try {
                    hookClass1=cl.loadClass("com.uc.apollo.media.widget.MediaViewImpl");
                }catch (Exception e){
                    XposedBridge.log("not find");

                }
                XposedBridge.log("find MediaViewImpl");

                XposedHelpers.findAndHookConstructor(hookClass1, Context.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        super.beforeHookedMethod(param);
                        XposedBridge.log("in constructor");
                        XposedHelpers.setAdditionalStaticField(param.thisObject.getClass(),"content",param.args[0]);
                    }
                });

                XposedHelpers.findAndHookConstructor(hookClass1, Context.class,int.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        super.beforeHookedMethod(param);
                        XposedBridge.log("in constructor");
                        XposedHelpers.setAdditionalStaticField(param.thisObject.getClass(),"content",param.args[0]);
                    }
                });

                XposedHelpers.findAndHookMethod(hookClass1,"setVideoURI",Uri.class,
                        Map.class,new XC_MethodHook(){
                            @Override
                            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                                super.beforeHookedMethod(param);
                                XposedBridge.log("Also Success");
                                String uri=param.args[0].toString();
                                String map=param.args[1].toString();

                                XposedBridge.log("uri="+uri);

                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                                intent.setDataAndType(Uri.parse(uri), "video/*");

                                Context context = (Context) XposedHelpers.getAdditionalStaticField(param.thisObject.getClass(),"content");
                                context.startActivity(intent);
//                                con.(intent);



                            }
                        });

            }
        });
        if(!lpparam.packageName.startsWith("com.UC")){
            return;

        }




    }
}
