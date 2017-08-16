package com.zu.customview.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;

/**
 * Created by zu on 16-12-29.
 */

/**
 * 将drawable转换为bitmap
 * */
public class DrawableBitmapUtil
{
    public static Bitmap drawableToBitmap(Drawable drawable) {


        int width = drawable.getIntrinsicWidth();
        if(width <= 0 || width > 256)
        {
            width = 256;
        }

        int height = drawable.getIntrinsicHeight();
        if(height <= 0 || height > 256)
        {
            height = 256;
        }
        Bitmap bitmap = Bitmap.createBitmap(

                width,

                height,

                drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888

                        : Bitmap.Config.RGB_565);

        Canvas canvas = new Canvas(bitmap);

        //canvas.setBitmap(bitmap);

        drawable.setBounds(0, 0, drawable.getIntrinsicWidth() > 256 ? 256 : drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight() > 256  ? 256 : drawable.getIntrinsicHeight());

        drawable.draw(canvas);

        return bitmap;

    }
}
