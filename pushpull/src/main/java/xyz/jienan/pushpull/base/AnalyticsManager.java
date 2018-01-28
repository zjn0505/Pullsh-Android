package xyz.jienan.pushpull.base;

import android.content.Context;
import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;

/**
 * Created by zhangjienan on 2018/1/27.
 */

public class AnalyticsManager {

    private AnalyticsManager() {}
    private Context context;
    private FirebaseAnalytics mFirebaseAnalytics;
    private static AnalyticsManager mInstance = new AnalyticsManager();
    private boolean isEnabled = true;

    public static AnalyticsManager getInstance() {
        return mInstance;
    }

    public void setContext(Context context) {
        this.context = context;
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(context);
    }

    public void switchAnalytics(boolean on) {
        isEnabled = on;
    }

    public void logEvent(String event, Bundle params) {
        if (context != null && isEnabled) {
            mFirebaseAnalytics.logEvent(event, params);
        }
    }

    public void logEvent(String event) {
        if (context != null && isEnabled) {
            mFirebaseAnalytics.logEvent(event, new Bundle());
        }
    }
}
