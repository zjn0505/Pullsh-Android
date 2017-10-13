package xyz.jienan.checked.network;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

/**
 * Created by Jienan on 2017/10/11.
 */

public interface TodoService {
    @GET("tasks")
    Call<TasksListEntity> getTasks();

    @GET("tasks/{taskId}")
    Call<TasksListEntity> getOneTask(@Path("taskId") String taskId);

    @POST("tasks")
    Call<TasksListEntity> addTask(@Body TaskEntity task);

    @PUT("tasks/{taskId}")
    Call<TasksListEntity> updateOneTask(@Path("taskId") String taskId, @Body TaskEntity task);

    @DELETE("tasks/{taskId}")
    Call<TasksListEntity> deleteOneTask(@Path("taskId") String taskId);
}
