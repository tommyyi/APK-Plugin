package com.plugin;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Build;
import android.util.ArrayMap;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
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
     * 执行完成后，mResources可以用于加载插件APK中的资源
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
        //mTheme = mResources.newTheme();
        //mTheme.setTo(mContext.getTheme());
    }

    /**
     * 我们可以通过DexClassLoader(String dexPath, String optimizedDirectory, String libraryPath, ClassLoader parent)的第3个参数
     * 先解压插件，得到“lib“路径
     */
    public void LoadPlugin()
    {
        //clean(ENTITY_APK);
        //clean(ENTITY_DEX);
        if (!new File(mPluginFilePath, ENTITY_APK).exists())
        {
            copy(ENTITY_APK);
        }

        String libPath = null;
        String dexPath = mPluginFilePath + "/" + ENTITY_APK;
        //Libs.UnzipSpecificFile(dexPath, mPluginFilePath, null);
        //libPath = mPluginFilePath + "/lib/armeabi";
        libPath = mPluginFilePath.replace("/files", "/lib");
        mDexClassLoader = new DexClassLoader(dexPath, mPluginFilePath, libPath, mContext.getClassLoader());

        fixLoadedAPK();
    }

    private void copylib(String libPath)
    {
        File libmg20pbase = new File(mPluginFilePath + "/lib/armeabi/libmg20pbase.so");
        File desFile = new File(mPluginFilePath.replace("/files", "/lib/armeabi") + "/libmg20pbase.so");
        FileInputStream fileInputStream;
        FileOutputStream fileOutputStream;

        File lib = new File("/data/data/com.xk.tianlaizhisheng2/lib");
        boolean result;
        if (!lib.exists())
        {
            result=lib.mkdir();
        }
        lib = new File("/data/data/com.xk.tianlaizhisheng2/lib/armeabi");
        if (!lib.exists())
        {
            result=lib.mkdir();
        }

        try
        {
            desFile.createNewFile();
            fileInputStream = new FileInputStream(libmg20pbase);
            fileOutputStream = new FileOutputStream(desFile);
            byte[] buffer = new byte[fileInputStream.available()];
            fileInputStream.read(buffer);
            fileOutputStream.write(buffer);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        libmg20pbase = new File(mPluginFilePath + "/lib/armeabi/libmusicopen.so");
        desFile = new File(mPluginFilePath.replace("/files", "/lib/armeabi") + "/libmusicopen.so");
        try
        {
            fileInputStream = new FileInputStream(libmg20pbase);
            fileOutputStream = new FileOutputStream(desFile);
            byte[] buffer = new byte[fileInputStream.available()];
            fileInputStream.read(buffer);
            fileOutputStream.write(buffer);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void initPlugin(Context context) throws Exception
    {
        Class<?> PluginApplication = mDexClassLoader.loadClass("com.plugin.PluginApplication");
        Method init = PluginApplication.getMethod("init", Context.class);
        init.setAccessible(true);
        init.invoke(null, context);
    }

    private void initLauncherActivity() throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException
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
     * 将持有插件dex的classloader放在标准的配置位置
     * 使得未安装的apk包的activity，像正常的activity一样有生命周期
     * （当然该activity申明在manifest当中，你可以预置很多的activity申明在manifest当中）
     * <p/>
     * LoadedApk中包含有
     * mAppDir（"/data/app/com.dexreload-2/base.apk"）、
     * mClassLoader（{PathClassLoader@3625} "dalvik.system.PathClassLoader[DexPathList[[zip file "/data/app/com.dexreload-2/base.apk"],nativeLibraryDirectories=[/vendor/lib, /system/lib]]]"）、
     * mLibDir（"/data/app/com.dexreload-2/lib/arm"）、
     * mResDir（"/data/app/com.dexreload-2/base.apk"）、
     * mResources（{Resources@3632}）
     * <p/>
     * 可以在application初始化阶段把mClassLoader、mLibDir、mResDir、mResources修改成插件的对应信息
     * 这样的话是否LoadedApk就完全指向了插件，后续产生的activity等的context是否就也完全指向了插件
     * 然后在初始化时执行插件的applicaiton初始化操作
     */
    @SuppressLint("NewApi")
    private void fixLoadedAPK()
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
            Object loadedApk = weakReference.get();

            setClassLoader(LoadedApk, loadedApk);
            setLibDir(LoadedApk, loadedApk);
            setResDir(LoadedApk, loadedApk);

            loadPluginResources(mPluginFilePath + "/" + ENTITY_APK);
            setRes(LoadedApk, loadedApk);

            /*下面的代码实际上是将宿主应用程序的Application（这里是HostApplication）的mBase的mResource也设置为插件的Resource*/
            Class<?> ContextWrapper = mContext.getClassLoader().loadClass("android.content.ContextWrapper");
            Class<?> ContextImpl = mContext.getClassLoader().loadClass("android.app.ContextImpl");
            Field field = ContextWrapper.getDeclaredField("mBase");
            field.setAccessible(true);
            Object mBaseObject = field.get(mContext);
            field = ContextImpl.getDeclaredField("mResources");
            field.setAccessible(true);
            field.set(mBaseObject, mResources);

            Log.i("demo", "mResources:" + mResources);
        }
        catch (Exception e)
        {
            Log.i("demo", "load apk classloader error:" + Log.getStackTraceString(e));
        }
    }

    private void setRes(Class<?> LoadedApk, Object loadedApk) throws NoSuchFieldException, IllegalAccessException
    {
        /*属性+类的实例-》设置属性*/
        Field mClassLoader = LoadedApk.getDeclaredField("mResources");
        mClassLoader.setAccessible(true);
        mClassLoader.set(loadedApk, mResources);
    }

    private void setClassLoader(Class<?> LoadedApk, Object loadedApk) throws NoSuchFieldException, IllegalAccessException
    {
    /*属性+类的实例-》设置属性*/
        Field mClassLoader = LoadedApk.getDeclaredField("mClassLoader");
        mClassLoader.setAccessible(true);
        mClassLoader.set(loadedApk, mDexClassLoader);

        Log.i("demo", "classloader:" + mDexClassLoader);
    }

    private void setLibDir(Class<?> LoadedApk, Object loadedApk) throws NoSuchFieldException, IllegalAccessException
    {
    /*属性+类的实例-》设置属性*/
        Field mClassLoader = LoadedApk.getDeclaredField("mLibDir");
        mClassLoader.setAccessible(true);
        mClassLoader.set(loadedApk, mContext.getFilesDir() + "/lib/armeabi");

        Log.i("demo", "mLibDir:" + mContext.getFilesDir() + "/lib/armeabi");
    }

    private void setResDir(Class<?> LoadedApk, Object loadedApk) throws NoSuchFieldException, IllegalAccessException
    {
    /*属性+类的实例-》设置属性*/
        Field mClassLoader = LoadedApk.getDeclaredField("mResDir");
        mClassLoader.setAccessible(true);
        mClassLoader.set(loadedApk, mContext.getFilesDir().getAbsolutePath() + "/" + ENTITY_APK);

        Log.i("demo", "mResDir:" + mContext.getFilesDir().getAbsolutePath() + "/" + ENTITY_APK);
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
