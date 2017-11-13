package xyz.jienan.pushpull;

import android.app.Application;

import com.facebook.stetho.Stetho;

/**
 * Created by Jienan on 2017/11/7.
 */

public class MemoApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.DEBUG) {
            Stetho.initializeWithDefaults(this);
        }
    }
}
