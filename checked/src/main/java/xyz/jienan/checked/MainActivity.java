package xyz.jienan.checked;

import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.animation.LayoutAnimationController;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import xyz.jienan.checked.network.TasksListEntity;
import xyz.jienan.checked.network.TodoService;
import xyz.jienan.checked.network.TaskEntity;

public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    private final static String TAG = MainActivity.class.getSimpleName();
    private final static String BASE_URL = "https://api.jienan.xyz/";

    @Bind(R.id.container_todo)
    SwipeRefreshLayout container;
    @Bind(R.id.lv_todo)
    RecyclerView rvTodo;

    private TodoService todoService;
    private LinearLayoutManager layoutManager;
    private TaskAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        prepareTodoService();
        container.setOnRefreshListener(this);

        rvTodo.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        rvTodo.setLayoutManager(layoutManager);
        rvTodo.setItemAnimator(new DefaultItemAnimator());
        adapter = new TaskAdapter(this, null);
        rvTodo.setAdapter(adapter);
        ItemTouchHelper.Callback callback = new TaskItemTouchHelperCallback(adapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(rvTodo);
        getTasksList();
    }

    @Override
    public void onRefresh() {
        getTasksList();
    }

    private void prepareTodoService() {
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

    private void getTasksList() {
        Call<TasksListEntity> call = todoService.getTasks();
        call.enqueue(new Callback<TasksListEntity>() {
            @Override
            public void onResponse(Call<TasksListEntity> call, Response<TasksListEntity> response) {
                Log.d(TAG, "onResponse: " + response.body().toString());
                container.setRefreshing(false);
                adapter.updateTasks(response.body().getTasks());
                Log.d(TAG, "getTasksList result size " + response.body().getTasks().size());
            }

            @Override
            public void onFailure(Call<TasksListEntity> call, Throwable t) {
                Log.e(TAG, "onFailure: " + t.getMessage());
                container.setRefreshing(false);
            }
        });
    }

    private void getOneTask(String id) {
        Call<TasksListEntity> call = todoService.getOneTask(id);
        call.enqueue(new Callback<TasksListEntity>() {
            @Override
            public void onResponse(Call<TasksListEntity> call, Response<TasksListEntity> response) {
                Log.d(TAG, "onResponse: " + response.body().toString());
                container.setRefreshing(false);
            }

            @Override
            public void onFailure(Call<TasksListEntity> call, Throwable t) {
                Log.e(TAG, "onFailure: " + t.getMessage());
                container.setRefreshing(false);
            }
        });
    }

    private void addTask(TaskEntity task) {
        Call<TasksListEntity> call = todoService.addTask(task);
        call.enqueue(new Callback<TasksListEntity>() {
            @Override
            public void onResponse(Call<TasksListEntity> call, Response<TasksListEntity> response) {
                Log.d(TAG, "onResponse: " + response.body().toString());
                container.setRefreshing(false);
            }

            @Override
            public void onFailure(Call<TasksListEntity> call, Throwable t) {
                Log.e(TAG, "onFailure: " + t.getMessage());
                container.setRefreshing(false);
            }
        });
    }

    private void updateTask(TaskEntity task) {
        Call<TasksListEntity> call = todoService.updateOneTask(task.getId(), task);
        call.enqueue(new Callback<TasksListEntity>() {
            @Override
            public void onResponse(Call<TasksListEntity> call, Response<TasksListEntity> response) {
                Log.d(TAG, "onResponse: " + response.body().toString());
                container.setRefreshing(false);
            }

            @Override
            public void onFailure(Call<TasksListEntity> call, Throwable t) {
                Log.e(TAG, "onFailure: " + t.getMessage());
                container.setRefreshing(false);
            }
        });
    }

    private void deleteTask(String id) {
        Call<TasksListEntity> call = todoService.deleteOneTask(id);
        call.enqueue(new Callback<TasksListEntity>() {
            @Override
            public void onResponse(Call<TasksListEntity> call, Response<TasksListEntity> response) {
                Log.d(TAG, "onResponse: " + response.body().toString());
                container.setRefreshing(false);
            }

            @Override
            public void onFailure(Call<TasksListEntity> call, Throwable t) {
                Log.e(TAG, "onFailure: " + t.getMessage());
                container.setRefreshing(false);
            }
        });
    }

}

