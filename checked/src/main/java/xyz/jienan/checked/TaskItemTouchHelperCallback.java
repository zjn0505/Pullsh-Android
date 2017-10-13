package xyz.jienan.checked;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;

/**
 * Created by Jienan on 2017/10/13.
 */

public class TaskItemTouchHelperCallback extends ItemTouchHelper.Callback {

    private TaskAdapter mAdapter;

    public TaskItemTouchHelperCallback(TaskAdapter adapter) {
        mAdapter = adapter;
    }



    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
        int swipeFlags = ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
        return makeMovementFlags(dragFlags, swipeFlags);
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        mAdapter.onItemMove(viewHolder.getAdapterPosition(), target.getAdapterPosition());
        return true;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        if (direction == ItemTouchHelper.RIGHT)
            mAdapter.onItemDismiss(viewHolder.getAdapterPosition());
    }
}
