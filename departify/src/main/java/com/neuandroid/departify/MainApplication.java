package com.neuandroid.departify;

import android.app.Application;
import android.content.Context;

/**
 * Created by Jienan on 2017/11/20.
 */

public class MainApplication extends Application {

    private static MainApplication instance;


    public static synchronized MainApplication getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleManager.onAttach(base, "en"));
    }
}
