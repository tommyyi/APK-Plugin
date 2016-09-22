package com.dexreload;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;

import com.plugin.Plugin;
import com.dex.UiToolInterface;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import dalvik.system.DexClassLoader;

public class MainActivity extends BaseActivity
{
    //private static final String LAUNCHER = "com.dex.DynamicalActivity";
    private static final String LAUNCHER = "com.yueyinyue.home.tianlaizhisheng.MainActivity";
    //private static final String ENTITY1_APK = "dex-debug1.apk";
    //private static final String ENTITY2_APK = "dex-debug2.apk";

    //DexClassLoader dexClassLoader1;
    //DexClassLoader dexClassLoader2;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        //setTheme(android.R.style.Theme_DeviceDefault_NoActionBar_TranslucentDecor);
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.main);

        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    //Plugin.getPlugin(getApplicationContext()).LoadPlugin();
                    //Plugin.getPlugin(getApplicationContext()).initLauncherActivity();

                    Intent intent = new Intent();
                    intent.setClassName(getApplicationContext(), LAUNCHER);
                    startActivity(intent);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void print(DexClassLoader dexClassLoader)
    {
        try
        {
            Class<?> Print = dexClassLoader.loadClass("com.dex.Print");
            Constructor constructor = Print.getConstructor();
            Object print = constructor.newInstance();
            Method method = Print.getMethod("output");
            method.invoke(print);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void loadBackgroundFromPlugin(View view)
    {
        try
        {
            Resources resources = Plugin.getPlugin(getApplicationContext()).getResources();
            DexClassLoader dexClassLoader = Plugin.getPlugin(getApplicationContext()).getDexClassLoader();
            if (dexClassLoader == null || resources == null)
            {
                return;
            }

            Class<?> UiTool = dexClassLoader.loadClass("com.dex.UiTool");
            Constructor constructor = UiTool.getConstructor();
            Object uiTool = constructor.newInstance();

        /*宿主中的接口定义要和主程序中的接口定义一样，包括package的路径*/
            UiToolInterface uiToolInterface = (UiToolInterface) uiTool;

            Drawable drawable = uiToolInterface.getDrawable(resources);
            view.setBackground(drawable);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void openActivityFromPlugin(View view)
    {
        Intent intent = new Intent();
        intent.setClassName(getApplicationContext(), Plugin.LAUNCHER_ACTIVITY);
        startActivity(intent);
    }
}
