package xyz.jienan.checked.network;

import android.util.Log;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Jienan on 2017/10/25.
 */

public class HttpModel {
    private final static String TAG = HttpModel.class.getSimpleName();
    private final static String BASE_URL = "https://api.jienan.xyz/";

    private TodoService todoService;

    private static class SingletonHolder{
        private static final HttpModel INSTANCE = new HttpModel();
    }

    public static HttpModel getInstance(){
        return SingletonHolder.INSTANCE;
    }

    private HttpModel() {
        OkHttpClient client = new OkHttpClient.Builder().connectTimeout(90, TimeUnit.SECONDS)
                .readTimeout(90, TimeUnit.SECONDS)
                .writeTimeout(90, TimeUnit.SECONDS)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();
        todoService = retrofit.create(TodoService.class);
    }

    public void getTasksList(final IHttpListener listener) {
        Call<TasksListEntity> call = todoService.getTasks();
        call.enqueue(new Callback<TasksListEntity>() {
            @Override
            public void onResponse(Call<TasksListEntity> call, Response<TasksListEntity> response) {
                Log.d(TAG, "onResponse: " + response.body().toString());
                listener.onSuccess(response.body().getTasks());
                Log.d(TAG, "getTasksList result size " + response.body().getTasks().size());
            }

            @Override
            public void onFailure(Call<TasksListEntity> call, Throwable t) {
                Log.e(TAG, "onFailure: " + t.getMessage());
                listener.onFailure();
            }
        });
    }

    public void getOneTask(String id) {
        Call<TasksListEntity> call = todoService.getOneTask(id);
        call.enqueue(new Callback<TasksListEntity>() {
            @Override
            public void onResponse(Call<TasksListEntity> call, Response<TasksListEntity> response) {
                Log.d(TAG, "onResponse: " + response.body().toString());
            }

            @Override
            public void onFailure(Call<TasksListEntity> call, Throwable t) {
                Log.e(TAG, "onFailure: " + t.getMessage());
            }
        });
    }

    public void addTask(TaskEntity task) {
        Call<TasksListEntity> call = todoService.addTask(task);
        call.enqueue(new Callback<TasksListEntity>() {
            @Override
            public void onResponse(Call<TasksListEntity> call, Response<TasksListEntity> response) {
                Log.d(TAG, "onResponse: " + response.body().toString());
            }

            @Override
            public void onFailure(Call<TasksListEntity> call, Throwable t) {
                Log.e(TAG, "onFailure: " + t.getMessage());
            }
        });
    }

    public void updateTask(TaskEntity task) {
        Call<TasksListEntity> call = todoService.updateOneTask(task.getId(), task);
        call.enqueue(new Callback<TasksListEntity>() {
            @Override
            public void onResponse(Call<TasksListEntity> call, Response<TasksListEntity> response) {
                Log.d(TAG, "onResponse: " + response.body().toString());
            }

            @Override
            public void onFailure(Call<TasksListEntity> call, Throwable t) {
                Log.e(TAG, "onFailure: " + t.getMessage());
            }
        });
    }

    public void deleteTask(String id) {
        Call<TasksListEntity> call = todoService.deleteOneTask(id);
        call.enqueue(new Callback<TasksListEntity>() {
            @Override
            public void onResponse(Call<TasksListEntity> call, Response<TasksListEntity> response) {
                Log.d(TAG, "onResponse: " + response.body().toString());
            }

            @Override
            public void onFailure(Call<TasksListEntity> call, Throwable t) {
                Log.e(TAG, "onFailure: " + t.getMessage());
            }
        });
    }
}
