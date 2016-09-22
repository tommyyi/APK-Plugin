package com.dex;

import android.app.AlertDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import net.steamcrafted.loadtoast.LoadToast;

import java.lang.reflect.Field;

import dalvik.system.DexClassLoader;

public class DynamicalActivity extends AppCompatActivity
{
    public static DexClassLoader mDexClassLoader;
    public static Resources mResources;
    //public static Resources.Theme mTheme;
    //public static AssetManager mAssetManager;

    /**
     * @param context
     * 如果只是想使这个Activity插件化，可以在这里调用replaceContext，将
     * 这个activity的context的resource替换成插件的resource
     *
     */
    @Override
    protected void attachBaseContext(Context context)
    {
        //replaceContext(context);
        super.attachBaseContext(context);
    }

    /**
     * 用于将插件的mResources设置到context
     * if you use context.getClass(), this will be not ok, you SHOULD use loadClass
     * 插件只能使用宿主APK中初始化的指向插件的mResources而不能自己生成，因为再这个位置mContext.getClassLoader()还不可用
     * 由于mContext.getClassLoader()不可用，无法生成mDexClassLoader，进而无法调用loadClass
     *
     * @param context
     */
    private void replaceContext(Context context)
    {
        Context applicationContext = context.getApplicationContext();
        Field field;
        try
        {
            Class<?> ContextImpl = mDexClassLoader.loadClass("android.app.ContextImpl");
            field = ContextImpl.getDeclaredField("mResources");
            field.setAccessible(true);
            field.set(context, mResources);

            Class<?> ContextWrapper = applicationContext.getClassLoader().loadClass("android.content.ContextWrapper");
            Field mBase = ContextWrapper.getDeclaredField("mBase");
            mBase.setAccessible(true);
            Object mBaseObject = mBase.get(applicationContext);
            field = ContextImpl.getDeclaredField("mResources");
            field.setAccessible(true);
            field.set(mBaseObject, mResources);

            /*类名-》加载的类*/
            Class<?> Application = applicationContext.getClassLoader().loadClass("android.app.Application");
            Field mLoadedApk = Application.getDeclaredField("mLoadedApk");
            mLoadedApk.setAccessible(true);
            Object mLoadedApkObject = mLoadedApk.get(applicationContext);

            Class<?> LoadedApk = applicationContext.getClassLoader().loadClass("android.app.LoadedApk");
            field = LoadedApk.getDeclaredField("mResources");
            field.setAccessible(true);
            field.set(mLoadedApkObject, mResources);

            Log.i("TAG", "OK");
        }
        catch (Exception e)
        {
            System.out.println("debug:repalceResources error");
            e.printStackTrace();
        }
    }

    //@Override
    //public AssetManager getAssets()
    //{
    //    return mAssetManager == null ? super.getAssets() : mAssetManager;
    //}
    //
    //@Override
    //public Resources getResources()
    //{
    //    return mResources == null ? super.getResources() : mResources;
    //}
    //
    //@Override
    //public Resources.Theme getTheme()
    //{
    //    return mTheme == null ? super.getTheme() : mTheme;
    //}

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dynamical);

        TextView textView=(TextView)findViewById(R.id.message);
        Log.e("lifeCycle", "onCreate");
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        Log.e("lifeCycle", "onStart");
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        Log.e("lifeCycle", "onResume");
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        Log.e("lifeCycle", "onPause");
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        Log.e("lifeCycle", "onStop");
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        Log.e("lifeCycle", "onDestroy");
    }

    public void showToast(View view)
    {
        //Toast.makeText(getBaseContext(), R.string.i_am_loaded_dynamically, Toast.LENGTH_LONG).show();

        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle(R.string.title);
        builder.setMessage(R.string.i_am_loaded_dynamically);
        builder.setPositiveButton("OK",null);
        builder.setNegativeButton("Cancel",null);
        builder.create().show();

        new LoadToast(this).setText("").setTranslationY(100).show();
    }
}
