package com.dex;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.zip.Inflater;

import dalvik.system.DexClassLoader;

public class DynamicalActivity extends Activity
{
    public static DexClassLoader mDexClassLoader;
    public static Resources mResources;
    public static Resources.Theme mTheme;
    public static AssetManager mAssetManager;

    @Override
    protected void attachBaseContext(Context context)
    {
        replaceContext(context);
        super.attachBaseContext(context);
    }

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
