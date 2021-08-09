package com.zu.customview;

import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;


import com.zu.customview.swiftp.Cat;

import java.util.List;

/**
 * Created by zu on 17-8-4.
 */

public class App extends Application {
    private static App mInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        CrashHandler crashHandler = CrashHandler.getInstance();
        crashHandler.init(this);
    }

    /**
     * @return the Context of this application
     */
    public static Context getAppContext() {
        return mInstance.getApplicationContext();
    }

    /**
     * @return true if this is the free version
     */
    public static boolean isFreeVersion() {
        try {
            Context context = getAppContext();
            return context.getPackageName().contains("free");
        } catch (Exception ignored) {
        }
        return false;
    }

    public static boolean isPaidVersionInstalled() {
        Context context = getAppContext();
        PackageManager pm = context.getPackageManager();
        List<ApplicationInfo> packages = pm.getInstalledApplications(0);
        for (ApplicationInfo packageInfo : packages) {
            if(packageInfo.packageName.equals("be.ppareit.swiftp"))
                return true;
        }
        return false;
    }

    /**
     * Get the version from the manifest.
     *
     * @return The version as a String.
     */
    public static String getVersion() {
        Context context = getAppContext();
        String packageName = context.getPackageName();
        try {
            PackageManager pm = context.getPackageManager();
            return pm.getPackageInfo(packageName, 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            Cat.e("Unable to find the name " + packageName + " in the package");
            return null;
        }
    }
}
