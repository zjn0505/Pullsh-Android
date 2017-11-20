package com.neuandroid.departify;

import android.app.Application;
import android.content.Context;

/**
 * Created by Jienan on 2017/11/20.
 */

public class MainApplication extends Application {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleManager.onAttach(base, "en"));
    }
}
