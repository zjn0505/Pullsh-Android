package xyz.jienan.pushpull.network;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * Created by Jienan on 2017/10/11.
 */

public interface MemoService {
    @POST("memo")
    Call<CommonResponse> createMemo(@Body MemoEntity task);

    @GET("memo")
    Call<CommonResponse> readMemo(@Query("memoId") String memoId);
}
