package xyz.jienan.checked;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.widget.Toast;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import xyz.jienan.checked.network.HttpModel;
import xyz.jienan.checked.network.IHttpListener;
import xyz.jienan.checked.network.TaskEntity;

public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    private final static String TAG = MainActivity.class.getSimpleName();

    @Bind(R.id.container_todo)
    SwipeRefreshLayout container;
    @Bind(R.id.lv_todo)
    RecyclerView rvTodo;


    private LinearLayoutManager layoutManager;
    private TaskAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
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
        HttpModel.getInstance().getTasksList(getTaskListener);
    }

    @Override
    public void onRefresh() {
        HttpModel.getInstance().getTasksList(getTaskListener);
    }

    private IHttpListener getTaskListener = new IHttpListener<List<TaskEntity>>() {
        @Override
        public void onSuccess(List<TaskEntity> bean) {
            container.setRefreshing(false);
            adapter.updateTasks(bean);
        }

        @Override
        public void onFailure() {
            container.setRefreshing(false);
            Toast.makeText(MainActivity.this, "Load task failed", Toast.LENGTH_SHORT).show();
        }
    };
}

