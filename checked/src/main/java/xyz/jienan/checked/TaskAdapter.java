package xyz.jienan.checked;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

import xyz.jienan.checked.network.TaskEntity;

/**
 * Created by Jienan on 2017/10/13.
 */

public class TaskAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements TaskItemTouchHelperAdapter {

    private final static int ITEM_ADD = 0;
    private final static int ITEM_TASK = 1;
    private Context mContext;
    private List<TaskEntity> tasks;

    public TaskAdapter(Context context, List<TaskEntity> taskList) {
        mContext = context;
        tasks = taskList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case ITEM_ADD:
                View addView = LayoutInflater.from(mContext).inflate(R.layout.item_add, parent, false);
                return new AddTaskViewHolder(addView);
            case ITEM_TASK:
                View taskView = LayoutInflater.from(mContext).inflate(R.layout.item_task, parent, false);
                return new TaskViewHolder(taskView);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case ITEM_ADD:
                AddTaskViewHolder addViewHolder = (AddTaskViewHolder) holder;
                break;
            case ITEM_TASK:
                TaskViewHolder taskViewHolder = (TaskViewHolder) holder;
                int posi = position -1;
                TaskEntity task = tasks.get(posi);
                taskViewHolder.tvTaskName.setText(task.getName());
                break;
            default:
                break;
        }
    }

    @Override
    public int getItemCount() {
        if (tasks == null || tasks.size() == 0) {
            return 1;
        }
        else
            return tasks.size() + 1;
    }

    public void updateTasks(List<TaskEntity> tasks) {
        this.tasks = tasks;
        notifyDataSetChanged();
    }

    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        Collections.swap(tasks, fromPosition - 1, toPosition - 1);
        notifyItemMoved(fromPosition, toPosition);
    }

    @Override
    public void onItemDismiss(int position) {

    }

    public class TaskViewHolder extends RecyclerView.ViewHolder {

        public View taskIndicator;
        public TextView tvTaskName;

        public TaskViewHolder(View itemView) {
            super(itemView);
            taskIndicator = itemView.findViewById(R.id.task_status_indicator);
            tvTaskName = (TextView) itemView.findViewById(R.id.task_name);
        }
    }

    public class AddTaskViewHolder extends RecyclerView.ViewHolder {

        public AddTaskViewHolder(View itemView) {
            super(itemView);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? ITEM_ADD : ITEM_TASK;
    }
}
