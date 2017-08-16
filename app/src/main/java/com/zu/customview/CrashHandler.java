package com.zu.customview;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;

/**
 * Created by zu on 17-2-9.
 */

/**
 * 程序崩溃信息收集的类
 *
 * */
public class CrashHandler implements Thread.UncaughtExceptionHandler {
    private String path = "/sdcard/CustomView/crash.txt";
    private static CrashHandler crashHandler = null;
    private Thread.UncaughtExceptionHandler mDefaultCrashHandler;
    private Context context;

    private CrashHandler()
    {

    }

    public static CrashHandler getInstance()
    {
        if(crashHandler == null)
        {
            crashHandler = new CrashHandler();
        }
        return crashHandler;
    }

    /**
     * 如果有默认的异常处理类，就将它保存起来
     * */
    public void init(Context context)
    {
        mDefaultCrashHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
        this.context = context.getApplicationContext();
    }

    /**
     * 先将异常信息保存起来，然后再调用默认的异常处理类，如果默认的异常处理类为空，需要自己手动结束应用程序
     * */
    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        try {
            dumpException(ex);
        }catch (Exception e)
        {
            e.printStackTrace();
        }
        if(mDefaultCrashHandler != null)
        {
            mDefaultCrashHandler.uncaughtException(thread, ex);
        }else
        {
            android.os.Process.killProcess(android.os.Process.myPid());
        }
    }

    private void dumpException(Throwable ex) throws Exception
    {
        PackageManager packageManager = context.getPackageManager();
        File dir = new File(path.substring(0, path.lastIndexOf("/")));
        if(!dir.exists())
        {
            dir.mkdirs();
        }
//        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
//                new File(path),true
//        ), "utf-8"));
        PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(new FileOutputStream(
                new File(path),true
        ), "utf-8"));
        printWriter.println("********************");
        printWriter.println(new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").format(System.currentTimeMillis()));

        PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), PackageManager.GET_ACTIVITIES);
        printWriter.println("App Version:" + packageInfo.versionName);
        printWriter.println("OS Version:" + Build.VERSION.RELEASE + Build.VERSION.SDK_INT);
        printWriter.println("制造商:" + Build.MANUFACTURER);
        printWriter.println("型号:" + Build.MODEL);
        printWriter.println("Cpu架构:" + Build.SUPPORTED_ABIS);
        ex.printStackTrace(printWriter);
        printWriter.println("********************");
        printWriter.close();

    }
}
