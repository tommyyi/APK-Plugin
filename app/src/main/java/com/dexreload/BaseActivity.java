package com.dexreload;

import android.app.Activity;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.support.v7.app.AppCompatActivity;

import java.lang.reflect.Method;

public class BaseActivity extends AppCompatActivity
{
    private AssetManager mAssetManager;
    private Resources mResources;
    private Resources.Theme mTheme;

    /**
     * 加载插件资源
     * @param dexPath
     */
    public void loadResources(String dexPath)
    {
        try
        {
            AssetManager assetManager = AssetManager.class.newInstance();
            Method addAssetPath = assetManager.getClass().getMethod("addAssetPath", String.class);
            addAssetPath.invoke(assetManager, dexPath);
            mAssetManager = assetManager;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        Resources superRes = super.getResources();
        superRes.getDisplayMetrics();
        superRes.getConfiguration();
        mResources = new Resources(mAssetManager, superRes.getDisplayMetrics(), superRes.getConfiguration());
        mTheme = mResources.newTheme();
        mTheme.setTo(super.getTheme());
    }

    @Override
    public AssetManager getAssets()
    {
        return mAssetManager == null ? super.getAssets() : mAssetManager;
    }

    @Override
    public Resources getResources()
    {
        return mResources == null ? super.getResources() : mResources;
    }

    @Override
    public Resources.Theme getTheme()
    {
        return mTheme == null ? super.getTheme() : mTheme;
    }
}
