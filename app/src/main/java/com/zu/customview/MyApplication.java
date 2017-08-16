package com.zu.customview;

import android.app.Application;


/**
 * Created by zu on 16-9-13.
 */
public class MyApplication extends Application
{
    private static MyApplication instance;
    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        CrashHandler crashHandler = CrashHandler.getInstance();
        crashHandler.init(this);

    }

    public static MyApplication getInstance()
    {
        return instance;
    }
}
