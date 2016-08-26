package com.dexreload;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.util.ArrayMap;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import dalvik.system.DexClassLoader;

/**
 * Created by Administrator on 2016/8/24.
 */
public class Plugin
{
    /**
     * 使得未安装的apk包的activity，像正常的activity一样有生命周期
     * @param activity
     * @param dLoader 加载未安装apk的加载器
     */
    @SuppressLint("NewApi")
    public void appendDex2Standard(Activity activity, DexClassLoader dLoader)
    {
        try
        {
            String packageName = activity.getPackageName();//当前apk的包名

            /*类名-》加载的类*/
            Class<?> ActivityThread = activity.getClassLoader().loadClass("android.app.ActivityThread");
            /*加载的类-》类的方法*/
            Method currentActivityThreadMethod = ActivityThread.getMethod("currentActivityThread");
            /*类的方法+加载的类-》调用类的静态函数-》类的实例（这个静态方法返回了一个实例）*/
            Object activityThread = currentActivityThreadMethod.invoke(ActivityThread);

            /*加载的类+属性名称-》属性*/
            Field mPackagesField = ActivityThread.getDeclaredField("mPackages");
            mPackagesField.setAccessible(true);

            /*属性+类的实例-》属性值*/
            ArrayMap mPackages = (ArrayMap) mPackagesField.get(activityThread);
            WeakReference weakReference = (WeakReference)mPackages.get(packageName);

            /*类名-》加载的类*/
            Class<?> LoadedApk = activity.getClassLoader().loadClass("android.app.LoadedApk");
            /*加载的类+属性名-》属性*/
            Field mClassLoader=LoadedApk.getDeclaredField("mClassLoader");
            mClassLoader.setAccessible(true);
            Object loadedApk = weakReference.get();
            /*属性+类的实例-》设置属性*/
            mClassLoader.set(loadedApk,dLoader);

            Log.i("demo", "classloader:" + dLoader);
        }
        catch (Exception e)
        {
            Log.i("demo", "load apk classloader error:" + Log.getStackTraceString(e));
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void appendRes(Activity activity, Resources resources)
    {
        try
        {
            /*类名-》加载的类*/
            Class<?> ActivityThread = activity.getClassLoader().loadClass("android.app.ActivityThread");
            /*加载的类-》类的方法*/
            Method currentActivityThreadMethod = ActivityThread.getMethod("currentActivityThread");
            /*类的方法+加载的类-》调用类的静态函数-》类的实例（这个静态方法返回了一个实例）*/
            Object activityThread = currentActivityThreadMethod.invoke(ActivityThread);

            /*类名-》加载的类*/
            Field mResourcesManagerField = ActivityThread.getDeclaredField("mResourcesManager");
            mResourcesManagerField.setAccessible(true);
            Object mResourcesManager = mResourcesManagerField.get(activityThread);

            Class<?> ResourcesManager = activity.getClassLoader().loadClass("android.app.ResourcesManager");
            Field mActiveResourcesField = ResourcesManager.getDeclaredField("mActiveResources");
            mActiveResourcesField.setAccessible(true);
            ArrayMap<Object, WeakReference<Resources> > mActiveResources = (ArrayMap<Object, WeakReference<Resources> >) mActiveResourcesField.get(mResourcesManager);

            //Class<?> ResourcesKey = activity.getClassLoader().loadClass("android.content.res.ResourcesKey");
            //Constructor constructor = ResourcesKey.getConstructor(String.class, String[].class, String[].class, String[].class, int.class, Configuration.class, CompatibilityInfo.class);
            //String path=activity.getFilesDir()+"/"+"dex-debug.apk";
            //Object key = constructor.newInstance(path, null, null, null, null, null, null);
            //mActiveResources.put(key, new WeakReference<>(resources));
        }
        catch (Exception e)
        {
            Log.i("demo", "load apk classloader error:" + Log.getStackTraceString(e));
        }
    }
}
