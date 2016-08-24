package com.dex;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import java.lang.reflect.Field;
import java.util.zip.Inflater;

public class DynamicalActivity extends Activity
{
    public static Resources mResources;
    public static Resources.Theme mTheme;

    @Override
    protected void attachBaseContext(Context newBase)
    {
        //replaceContext(newBase);
        super.attachBaseContext(newBase);
    }

    private void replaceContext(Context newBase)
    {
        try
        {
            Field field = newBase.getClass().getDeclaredField("mResources");
            field.setAccessible(true);
            field.set(newBase, mResources);

            field = newBase.getClass().getDeclaredField("mTheme");
            field.setAccessible(true);
            field.set(newBase, mTheme);
        }
        catch (Exception e)
        {
            System.out.println("debug:repalceResources error");
            e.printStackTrace();
        }
    }

    /*@Override
    public Resources.Theme getTheme()
    {
        return mTheme;
    }

    @Override
    public Resources getResources()
    {
        return mResources;
    }*/

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        //View view = getLayoutInflater().inflate(R.layout.activity_dynamical,null);
        //setContentView(view);

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
}
