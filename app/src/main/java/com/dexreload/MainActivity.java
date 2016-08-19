package com.dexreload;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;

import com.dex.UiToolInterface;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import dalvik.system.DexClassLoader;

public class MainActivity extends BaseActivity
{
    private static final String ENTITY1_APK = "dex-debug1.apk";
    private static final String ENTITY2_APK = "dex-debug2.apk";
    private static final String ENTITY = "dex-debug.apk";
    private String mEntityFilePath;

    DexClassLoader dexClassLoader1;
    DexClassLoader dexClassLoader2;

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
                copyEntity(ENTITY);
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
        DexClassLoader dexClassLoader=new DexClassLoader(mEntityFilePath+"/"+ENTITY,mEntityFilePath,null,getClassLoader());
        loadResources(mEntityFilePath+"/"+ENTITY);

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
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void reload(View view)
    {
    }
}
