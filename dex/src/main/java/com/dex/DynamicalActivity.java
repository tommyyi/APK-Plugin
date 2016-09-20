package com.dex;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.lang.reflect.Field;

import dalvik.system.DexClassLoader;

public class DynamicalActivity extends AppCompatActivity
{
    public static DexClassLoader mDexClassLoader;
    public static Resources mResources;
    //public static Resources.Theme mTheme;
    //public static AssetManager mAssetManager;

    @Override
    protected void attachBaseContext(Context context)
    {
        replaceContext(context);
        super.attachBaseContext(context);
    }

    /**
     * if you use context.getClass(), this will be not ok, you SHOULD use loadClass
     * 插件只能使用宿主APK中初始化的指向插件的mResources，因为再这个位置mContext.getClassLoader()t还不可用
     * 由于mContext.getClassLoader()不可用，无法生成mDexClassLoader，进而无法生成loadClass
     * @param context
     */
    private void replaceContext(Context context)
    {
        try
        {
            Class<?> ContextImpl = mDexClassLoader.loadClass("android.app.ContextImpl");
            Field field = ContextImpl.getDeclaredField("mResources");
            field.setAccessible(true);
            field.set(context, mResources);

            //field = context.getClass().getDeclaredField("mTheme");
            //field.setAccessible(true);
            //field.set(context, mTheme);
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
        Toast.makeText(getBaseContext(),R.string.i_am_loaded_dynamically,Toast.LENGTH_LONG).show();
    }
}
