package xyz.jienan.pushpull.network;

import com.facebook.stetho.okhttp3.StethoInterceptor;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;
import xyz.jienan.pushpull.BuildConfig;

/**
 * Created by Jienan on 2017/10/11.
 */

public class MemoService {

    private final static String BASE_URL = "https://api.jienan.xyz/";
    private MemoAPI memoAPI;
    private static MemoService instance;
    private MemoService() {
        Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create());
        if (BuildConfig.DEBUG) {
            OkHttpClient client = new OkHttpClient.Builder()
                    .addNetworkInterceptor(new StethoInterceptor()).build();
            builder.client(client);
        }

        Retrofit retrofit = builder.build();
        memoAPI = retrofit.create(MemoAPI.class);
    }

    public static MemoAPI getMemoAPI() {
        if (instance == null) {
            instance = new MemoService();
        }
        return instance.memoAPI;
    }

    public interface MemoAPI {
        @POST("memo")
        Call<CommonResponse> createMemo(@Body MemoEntity task);

        @GET("memo")
        Call<CommonResponse> readMemo(@Query("memoId") String memoId);
    }
}
