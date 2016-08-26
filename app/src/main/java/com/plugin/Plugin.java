package com.plugin;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Build;
import android.util.ArrayMap;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import dalvik.system.DexClassLoader;

/**
 * Created by Administrator on 2016/8/24.
 */
public class Plugin
{
    public static final String LAUNCHER_ACTIVITY = "com.dex.DynamicalActivity";
    private static final String LAUNCHER_ACTIVITY_CLASSLOADER = "mDexClassLoader";
    private static final String LAUNCHER_ACTIVITY_RESOURCES = "mResources";
    private static Plugin plugin;
    final String ENTITY_APK = "dex-debug.apk";
    final String ENTITY_DEX = "dex-debug.dex";
    Context mContext;
    String mPluginFilePath;
    AssetManager mAssetManager;

    public Resources getResources()
    {
        return mResources;
    }

    Resources mResources;
    Resources.Theme mTheme;
    DexClassLoader mDexClassLoader;

    private Plugin(Context context)
    {
        mContext = context;
        mPluginFilePath = context.getFilesDir().getAbsolutePath();
    }

    public static Plugin getPlugin(Context context)
    {
        if (plugin != null)
        {
            return plugin;
        }
        else
        {
            plugin = new Plugin(context);
            return plugin;
        }
    }

    public DexClassLoader getDexClassLoader()
    {
        return mDexClassLoader;
    }

    private void clean(String fileName)
    {
        File file = new File(mPluginFilePath, fileName);
        if (file.exists())
        {
            file.delete();
        }
    }

    private void copy(String fileName)
    {
        try
        {
            InputStream inputStream = mContext.getAssets().open(fileName);
            int size = inputStream.available();
            byte[] buffer = new byte[size];
            inputStream.read(buffer);

            FileOutputStream fileOutputStream = mContext.openFileOutput(fileName, Context.MODE_PRIVATE);
            fileOutputStream.write(buffer);
            fileOutputStream.flush();

            inputStream.close();
            fileOutputStream.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * 加载插件资源
     *
     * @param dexPath
     */
    private void loadPluginResources(String dexPath)
    {
        try
        {
            mAssetManager = AssetManager.class.newInstance();
            Method addAssetPath = mAssetManager.getClass().getMethod("addAssetPath", String.class);
            addAssetPath.invoke(mAssetManager, dexPath);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        Resources superRes = mContext.getResources();
        superRes.getDisplayMetrics();
        superRes.getConfiguration();
        mResources = new Resources(mAssetManager, superRes.getDisplayMetrics(), superRes.getConfiguration());
        mTheme = mResources.newTheme();
        mTheme.setTo(mContext.getTheme());
    }

    public void LoadPlugin()
    {
        clean(ENTITY_APK);
        clean(ENTITY_DEX);
        copy(ENTITY_APK);

        mDexClassLoader = new DexClassLoader(mPluginFilePath + "/" + ENTITY_APK, mPluginFilePath, null, mContext.getClassLoader());

        appendDex2Standard();
        loadPluginResources(mPluginFilePath + "/" + ENTITY_APK);
    }

    public void initLauncherActivity() throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException
    {
        Class<?> DynamicalActivity = mDexClassLoader.loadClass(LAUNCHER_ACTIVITY);
        Field resourceField = DynamicalActivity.getDeclaredField(LAUNCHER_ACTIVITY_RESOURCES);
        resourceField.setAccessible(true);
        resourceField.set(DynamicalActivity, mResources);

        Field mDexClassLoaderField = DynamicalActivity.getDeclaredField(LAUNCHER_ACTIVITY_CLASSLOADER);
        mDexClassLoaderField.setAccessible(true);
        mDexClassLoaderField.set(DynamicalActivity, mDexClassLoader);
    }

    /**
     * 使得未安装的apk包的activity，像正常的activity一样有生命周期
     */
    @SuppressLint("NewApi")
    private void appendDex2Standard()
    {
        try
        {
            String packageName = mContext.getPackageName();//当前apk的包名

            /*类名-》加载的类*/
            Class<?> ActivityThread = mContext.getClassLoader().loadClass("android.app.ActivityThread");
            /*加载的类-》类的方法*/
            Method currentActivityThreadMethod = ActivityThread.getMethod("currentActivityThread");
            /*类的方法+加载的类-》调用类的静态函数-》类的实例（这个静态方法返回了一个实例）*/
            Object activityThread = currentActivityThreadMethod.invoke(ActivityThread);

            /*加载的类+属性名称-》属性*/
            Field mPackagesField = ActivityThread.getDeclaredField("mPackages");
            mPackagesField.setAccessible(true);

            /*属性+类的实例-》属性值*/
            ArrayMap mPackages = (ArrayMap) mPackagesField.get(activityThread);
            WeakReference weakReference = (WeakReference) mPackages.get(packageName);

            /*类名-》加载的类*/
            Class<?> LoadedApk = mContext.getClassLoader().loadClass("android.app.LoadedApk");
            /*加载的类+属性名-》属性*/
            Field mClassLoader = LoadedApk.getDeclaredField("mClassLoader");
            mClassLoader.setAccessible(true);
            Object loadedApk = weakReference.get();
            /*属性+类的实例-》设置属性*/
            mClassLoader.set(loadedApk, mDexClassLoader);

            Log.i("demo", "classloader:" + mDexClassLoader);
        }
        catch (Exception e)
        {
            Log.i("demo", "load apk classloader error:" + Log.getStackTraceString(e));
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void appendRes(Activity activity, Resources resources)
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
            ArrayMap<Object, WeakReference<Resources>> mActiveResources = (ArrayMap<Object, WeakReference<Resources>>) mActiveResourcesField.get(mResourcesManager);

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
