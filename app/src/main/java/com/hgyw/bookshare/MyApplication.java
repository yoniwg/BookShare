package com.hgyw.bookshare;

import android.app.Application;
import android.content.Context;

/**
 * Created by haim7 on 25/05/2016.
 */
public class MyApplication extends Application {
    private static Context context;

    public void onCreate() {
        super.onCreate();
        MyApplication.context = getApplicationContext();
    }

    public static Context getAppContext() {
        if (context == null) throw new RuntimeException("The application is not yet initialized");
        return MyApplication.context;
    }

}
