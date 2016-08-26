package com.dexreload;

import android.app.Activity;
import android.content.res.AssetManager;
import android.content.res.Resources;

import java.lang.reflect.Method;

public class BaseActivity extends Activity
{
    public AssetManager mAssetManager;
    public Resources mResources;
    public Resources.Theme mTheme;

    /**
     * 加载插件资源
     * @param dexPath
     */
    public void loadPluginResources(String dexPath)
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

    /*@Override
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
    }*/
}
