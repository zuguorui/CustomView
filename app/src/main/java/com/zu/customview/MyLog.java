package com.zu.customview;

import android.util.Log;

/**
 * Created by zu on 17-2-9.
 */

/**
 * 支持在一个类里统一设置的log类，相比于static的更加方便管理
 * */
public class MyLog {
    private static boolean DEBUG = true;


    public boolean show = true;
    public String tag;

    public MyLog(String tag, boolean show)
    {
        this.tag = tag;
        this.show = show;
    }

    public void v(String content)
    {
        if(DEBUG && show)
        {
            Log.v(tag, content);
        }
    }

    public void i(String content)
    {
        if(DEBUG && show)
        {
            Log.i(tag, content);
        }
    }

    public void d(String content)
    {
        if(DEBUG && show)
        {
            Log.d(tag, content);
        }
    }

    public void w(String content)
    {
        if(DEBUG && show)
        {
            Log.w(tag, content);
        }
    }

    public void e(String content)
    {
        if(DEBUG && show)
        {
            Log.e(tag, content);
        }
    }


}
