package com.dexreload;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;

import com.dex.UiToolInterface;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import dalvik.system.DexClassLoader;

public class MainActivity extends BaseActivity
{
    private static final String ENTITY1_APK = "dex-debug1.apk";
    private static final String ENTITY2_APK = "dex-debug2.apk";
    private static final String ENTITY_APK = "dex-debug.apk";
    private static final String ENTITY_DEX = "dex-debug.dex";
    private String mEntityFilePath;

    DexClassLoader dexClassLoader1;
    DexClassLoader dexClassLoader2;
    private DexClassLoader mDexClassLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mEntityFilePath = getFilesDir().getAbsolutePath();

        Runnable runnable = new Runnable()
        {
            public void run()
            {
                clean(ENTITY_APK);
                clean(ENTITY_DEX);
                copyEntity(ENTITY_APK);
                mDexClassLoader = new DexClassLoader(mEntityFilePath+"/"+ ENTITY_APK,mEntityFilePath,null,getClassLoader());
            }
        };
        new Thread(runnable).start();
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

    private void clean(String fileName)
    {
        File file = new File(mEntityFilePath, fileName);
        if (file.exists())
        {
            file.delete();
        }
    }

    private void copyEntity(String fileName)
    {
        try
        {
            InputStream inputStream = getAssets().open(fileName);
            int size = inputStream.available();
            byte[] buffer = new byte[size];
            inputStream.read(buffer);

            FileOutputStream fileOutputStream = openFileOutput(fileName, Context.MODE_PRIVATE);
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

    public void load(View view)
    {
        loadResources(mEntityFilePath+"/"+ ENTITY_APK);

        try
        {
            Class<?> DynamicalActivity = mDexClassLoader.loadClass("com.dex.DynamicalActivity");
            Field resourceField=DynamicalActivity.getDeclaredField("mResources");
            resourceField.setAccessible(true);
            resourceField.set(DynamicalActivity,mResources);

            Field themeField=DynamicalActivity.getDeclaredField("mTheme");
            themeField.setAccessible(true);
            themeField.set(DynamicalActivity,mTheme);

            Field assetManagerField=DynamicalActivity.getDeclaredField("mAssetManager");
            assetManagerField.setAccessible(true);
            assetManagerField.set(DynamicalActivity,mAssetManager);

            testResourceLoadFromNotInstalledAPK(mDexClassLoader,view);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void testResourceLoadFromNotInstalledAPK(DexClassLoader dexClassLoader,View view)
    {
        try
        {
            Class<?> UiTool = dexClassLoader.loadClass("com.dex.UiTool");
            Constructor constructor = UiTool.getConstructor();
            Object uiTool = constructor.newInstance();

        /*宿主中的接口定义要和主程序中的接口定义一样，包括package的路径*/
            UiToolInterface uiToolInterface= (UiToolInterface) uiTool;

            Drawable drawable = uiToolInterface.getDrawable(this);
            view.setBackground(drawable);
        }
        catch (ClassNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (NoSuchMethodException e)
        {
            e.printStackTrace();
        }
        catch (InstantiationException e)
        {
            e.printStackTrace();
        }
        catch (IllegalAccessException e)
        {
            e.printStackTrace();
        }
        catch (InvocationTargetException e)
        {
            e.printStackTrace();
        }
    }

    public void open(View view)
    {
        ReplaceLoader2AppendDex loader = new ReplaceLoader2AppendDex();
        loader.appendApk(this,mDexClassLoader);

        Intent intent = new Intent();
        intent.setClassName(getApplicationContext(),"com.dex.DynamicalActivity");
        try
        {
            Class<?> DynamicalActivity = mDexClassLoader.loadClass("com.dex.DynamicalActivity");
            Field mDexClassLoaderField=DynamicalActivity.getDeclaredField("mDexClassLoader");
            mDexClassLoaderField.setAccessible(true);
            mDexClassLoaderField.set(DynamicalActivity,mDexClassLoader);
            startActivity(intent);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
