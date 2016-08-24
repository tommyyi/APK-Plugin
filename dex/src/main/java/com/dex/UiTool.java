package com.dex;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;

/**
 * Created by Administrator on 2016/8/19.
 */
public class UiTool implements UiToolInterface
{
    public UiTool()
    {
    }

    /**
     * @param context 被重载了getResources的activity，持有插件APK的resource对象
     * @return
     */
    @Override
    public Drawable getDrawable(Context context)
    {
        return context.getResources().getDrawable(R.drawable.dogs);
    }

    @Override
    public View getLayout(Activity context,String layoutName)
    {
        int id = context.getResources().getIdentifier(layoutName, "layout", context.getPackageName());
        return context.getLayoutInflater().inflate(id, null);
    }
}
