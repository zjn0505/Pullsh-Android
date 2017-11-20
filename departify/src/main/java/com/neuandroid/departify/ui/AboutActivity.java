package com.neuandroid.departify.ui;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.neuandroid.departify.LocaleManager;
import com.neuandroid.departify.R;

import java.io.IOException;
import java.io.InputStream;

import static com.neuandroid.departify.Const.DEPARTIFY_QUOTA;
import static com.neuandroid.departify.Const.DEPARTIFY_SUB_TYPE;
import static com.neuandroid.departify.Const.DEV_MODE;


/**
 * Created by Jienan on 2017/11/16.
 */

public class AboutActivity extends BaseActivity {

    private SharedPreferences sharedPreferences;
    private int clickCountMax = 5;
    private Toast clickToast;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String subType = sharedPreferences.getString(DEPARTIFY_SUB_TYPE, "test");
        String dailyQuota = sharedPreferences.getString(DEPARTIFY_QUOTA, "10");

        String html = "";
        String lang = LocaleManager.getLanguage(this);
        try {
            InputStream is = getAssets().open(String.format("about-%s.html", lang));
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
        webView.setWebViewClient(new AboutWebViewClient());
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

    private class AboutWebViewClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return handleUrl(view, url);
        }

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            return handleUrl(view, request.getUrl().toString());
        }


        private boolean handleUrl(WebView view, final String url) {
            if (!TextUtils.isEmpty(url)) {
                 if (url.startsWith("http")) {
                     String urlAlert = String.format(getString(R.string.url_redirect), url);
                     new AlertDialog.Builder(AboutActivity.this)
                             .setMessage(urlAlert)
                             .setPositiveButton(R.string.proceed, new DialogInterface.OnClickListener() {
                                 @Override
                                 public void onClick(DialogInterface dialog, int which) {
                                     Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                                     startActivity(browserIntent);
                                 }
                             })
                             .setNegativeButton(R.string.dismiss, new DialogInterface.OnClickListener() {
                                 @Override
                                 public void onClick(DialogInterface dialog, int which) {
                                     if (dialog != null) {
                                         dialog.dismiss();
                                     }
                                 }
                             }).show();
                 } else if (url.startsWith("departify")) {
                     if (url.endsWith("enter-user-credentials"))
                        startActivity(new Intent(AboutActivity.this, CredActivity.class));
                     else if (url.endsWith("activate-developer-mode"))
                         activateDevMode();
                 }
            }

            return true;
        }
    }
    private Handler mHandler = new Handler();
    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            clickCountMax = 5;
        }
    };


    private void activateDevMode() {
        boolean devMode = sharedPreferences.getBoolean(DEV_MODE, false);
        if (clickToast == null) {
            clickToast = Toast.makeText(this, R.string.already_in_dev_mode, Toast.LENGTH_SHORT);
        }
        if (devMode) {
            clickToast.setText(R.string.already_in_dev_mode);
            clickToast.show();
        } else {
            mHandler.removeCallbacks(mRunnable);
            mHandler.postDelayed(mRunnable, 2500);

            if (clickCountMax != 0) {
                clickToast.setText(getResources().getQuantityString(R.plurals.click_to_enter_dev_mode, clickCountMax, clickCountMax));
                clickCountMax--;
            } else {
                clickToast.setText(getString(R.string.in_dev_mode));
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(DEV_MODE, true);
                devMode = true;
                editor.apply();
            }
            clickToast.show();
        }
    }
}
