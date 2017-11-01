package xyz.jienan.pushpull.network;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * Created by Jienan on 2017/10/11.
 */

public interface MemoService {
    @POST("memo")
    Call<CommonResponse> createMemo(@Body MemoEntity task);

    @GET("memo/{memoId}")
    Call<CommonResponse> readMemo(@Path("memoId") String memoId);
}
