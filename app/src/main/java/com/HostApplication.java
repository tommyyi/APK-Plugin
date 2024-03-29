package com;

import android.app.Application;
import android.content.Context;

import com.plugin.Plugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import dalvik.system.DexClassLoader;

/**
 * Created by Administrator on 2016/8/26.
 */
public class HostApplication extends Application
{
    /**
     * 如果你想使整个APP插件化，就在这里初始化插件，插件APP与宿主没有耦合
     */
    @Override
    public void onCreate()
    {
        super.onCreate();
        try
        {
            Plugin.getPlugin(getApplicationContext()).LoadPlugin();
            Plugin.getPlugin(getApplicationContext()).initPlugin(getApplicationContext());

            try {
                System.loadLibrary("mg20pbase");
            } catch (Exception e) {
                e.printStackTrace();
            }
            //Plugin.getPlugin(getApplicationContext()).initLauncherActivity();

            /*DexClassLoader dexClassLoader = Plugin.getPlugin(getApplicationContext()).getDexClassLoader();
            Class YueApplication=dexClassLoader.loadClass("com.xk.m.YueApplication");
            Constructor constructor = YueApplication.getConstructor();
            Object instance = constructor.newInstance();

            Method setContext=YueApplication.getDeclaredMethod("setContext", Context.class);
            setContext.invoke(instance,getApplicationContext());

            Method onCreate=YueApplication.getDeclaredMethod("onCreate");
            onCreate.invoke(instance);*/
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        //super.onCreate();
    }
}
