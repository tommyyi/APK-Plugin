package com.dex;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.view.View;

/**
 * Created by Administrator on 2016/8/19.
 */
public interface UiToolInterface
{
    Drawable getDrawable(Resources resources);
    View getLayout(Activity context, String layoutName);
}
