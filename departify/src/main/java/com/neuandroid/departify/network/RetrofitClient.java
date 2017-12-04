package com.neuandroid.departify.network;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import com.neuandroid.departify.BuildConfig;
import com.neuandroid.departify.MainApplication;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Jienan on 2017/12/4.
 */

public class RetrofitClient {

    private final static String BASE_URL = "https://api.deeparteffects.com/v1/";
    private static Retrofit retrofit;
    private static RetrofitClient instance = null;
    private DepartifyService departifyService = null;

    private RetrofitClient() {
        String packageName = MainApplication.getInstance().getPackageName();
        String apiKey = "";
        try {
            ApplicationInfo appInfo = MainApplication.getInstance().getPackageManager().getApplicationInfo(packageName, PackageManager.GET_META_DATA);
            if (appInfo.metaData != null) {
                apiKey = appInfo.metaData.getString("DEEP_ART_EFFECT_API_KEY");
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        OkHttpClient.Builder builder = new OkHttpClient().newBuilder();
        builder.readTimeout(10, TimeUnit.SECONDS);
        builder.connectTimeout(9, TimeUnit.SECONDS);
        final String finalApiKey = apiKey;
        builder.addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request request = chain.request().newBuilder().addHeader("x-api-key", finalApiKey).build();
                return chain.proceed(request);
            }
        });
        if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            builder.addInterceptor(interceptor);
        }

        retrofit = new Retrofit.Builder().baseUrl(BASE_URL).client(builder.build())
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();

        departifyService = retrofit.create(DepartifyService.class);
    }

    public static RetrofitClient getInstance() {
        if (instance == null) {
            instance = new RetrofitClient();
        }

        return instance;
    }

    public DepartifyService getDepartifyService() {
        return departifyService;
    }
}
