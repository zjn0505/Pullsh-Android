package com.neuandroid.departify;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.webkit.WebView;

import java.io.IOException;
import java.io.InputStream;


/**
 * Created by Jienan on 2017/11/16.
 */

public class AboutActivity extends AppCompatActivity {

    private final static String DEPARTIFY_CONFIG = "departify_config";
    private final static String DEPARTIFY_SUB_TYPE = "departify_sub_type";
    private final static String DEPARTIFY_QUOTA = "departify_quota";


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sharedPreferences = getSharedPreferences(DEPARTIFY_CONFIG, MODE_PRIVATE);
        String subType = sharedPreferences.getString(DEPARTIFY_SUB_TYPE, "test");
        String dailyQuota = sharedPreferences.getString(DEPARTIFY_QUOTA, "10");

        String html = "";
        try {
            InputStream is = getAssets().open("about.html");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String rawHtml = new String(buffer);
            rawHtml = rawHtml.replace("{"+DEPARTIFY_SUB_TYPE+"}", subType);
            html  = rawHtml.replace("{"+DEPARTIFY_QUOTA+"}", dailyQuota);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (TextUtils.isEmpty(html)) {
            html = getString(R.string.about_content_short);
        }

        WebView webView = new WebView(this);
        webView.loadDataWithBaseURL("file:///android_asset/", html , "text/html", "utf-8",null);
        setContentView(webView);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
