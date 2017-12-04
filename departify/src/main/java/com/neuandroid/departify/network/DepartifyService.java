package com.neuandroid.departify.network;

import com.neuandroid.departify.model.Result;
import com.neuandroid.departify.model.Styles;
import com.neuandroid.departify.model.UploadRequest;
import com.neuandroid.departify.model.UploadResponse;


import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;
import retrofit2.http.Url;

/**
 * Created by Jienan on 2017/12/4.
 */

public interface DepartifyService {

    @GET
    Observable<Styles> stylesGetFromProxy(@Url String url);

    @GET("noauth/styles")
    Observable<Styles> stylesGet();

    @POST("noauth/upload")
    Observable<UploadResponse> uploadPost(@Body UploadRequest uploadRequest);

    @GET("noauth/result")
    Observable<Result> resultGet(@Query("submissionId") String submissionId);
}
