package xyz.jienan.pushpull;

import android.app.Application;

import com.facebook.stetho.Stetho;

import xyz.jienan.pushpull.base.AnalyticsManager;
import xyz.jienan.pushpull.base.RxBus;

/**
 * Created by Jienan on 2017/11/7.
 */

public class MemoApplication extends Application {

    private RxBus bus;

    @Override
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.DEBUG) {
            Stetho.initializeWithDefaults(this);
        }
        bus = new RxBus();
        AnalyticsManager.getInstance().setContext(this);
    }

    public RxBus bus() {
        return bus;
    }
}
