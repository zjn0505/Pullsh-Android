package xyz.jienan.checked;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;

import java.util.List;

import xyz.jienan.checked.network.TaskEntity;

import static android.support.v7.widget.helper.ItemTouchHelper.ACTION_STATE_SWIPE;

/**
 * Created by Jienan on 2017/10/13.
 */

public class TaskItemTouchHelperCallback extends ItemTouchHelper.Callback {

    private TaskItemTouchHelperAdapter mAdapter;
    private static final float buttonWidth = 300;
    private static final float SWIPE_MAX_RATIO = 0.6f;
    private final static String TAG = TaskItemTouchHelperCallback.class.getSimpleName();
    private final static float SWIPE_DELETE_THRESOLD = 0.4f;

    private boolean returnToPosition = false;
    private boolean hasSwipeLeftHapticSent = false;
    private float swipeMaxPixel;

    public TaskItemTouchHelperCallback(TaskItemTouchHelperAdapter adapter) {
        mAdapter = adapter;
    }

    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
        int swipeFlags = ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
        Log.d(TAG, "getMovementFlags: ");
        return makeMovementFlags(dragFlags, swipeFlags);
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        mAdapter.onItemMove(viewHolder.getAdapterPosition(), target.getAdapterPosition());
        return true;
    }

    @Override
    public float getSwipeThreshold(RecyclerView.ViewHolder viewHolder) {
        return SWIPE_DELETE_THRESOLD;
    }


    @Override
    public float getSwipeEscapeVelocity(float defaultValue) {
        return 8000;
    }

    @Override
    public int convertToAbsoluteDirection(int flags, int layoutDirection) {
        if (!returnToPosition) {
            Log.d(TAG, "unable to swipe back: ");
            return super.convertToAbsoluteDirection(flags, layoutDirection);
        } else {
            Log.d(TAG, "swipe back: ");
            returnToPosition = false;
            return 0;
        }

    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        if (direction == ItemTouchHelper.LEFT)
            mAdapter.onItemDismiss(viewHolder.getAdapterPosition());
    }


    @Override
    public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {

        // init swipeMaxPixel to limit the swipe distance
        if (swipeMaxPixel == 0) {
            swipeMaxPixel =  viewHolder.itemView.getWidth() * SWIPE_MAX_RATIO;
        }
        if (actionState == ACTION_STATE_SWIPE) {
            float limitedDX = dX > swipeMaxPixel ? swipeMaxPixel : dX < -swipeMaxPixel ? -swipeMaxPixel : dX;
            int indexInList = viewHolder.getAdapterPosition() - 1;
            List<TaskEntity> tasks = ((TaskAdapter)recyclerView.getAdapter()).getTasks();

            if (tasks != null && indexInList >= 0) {
                TaskEntity task = tasks.get(indexInList);
                if (task != null) {
                    if (dX < 0) {
                        ColorDrawable targetDrawable = new ColorDrawable(Color.RED);
                        if (isCurrentlyActive) {
                            float ratio = Math.abs(dX)/(recyclerView.getWidth() * SWIPE_DELETE_THRESOLD);
                            ratio = ratio >= 1 ? 1 : ratio;
                            targetDrawable.setAlpha((int)(ratio * ratio * 255));
                            targetDrawable.setBounds((int)(viewHolder.itemView.getRight()+ dX), viewHolder.itemView.getTop(), recyclerView.getRight(), viewHolder.itemView.getBottom());
                            targetDrawable.draw(c);
                            if (Math.abs(dX) >= viewHolder.itemView.getWidth()*SWIPE_DELETE_THRESOLD
                                    && !hasSwipeLeftHapticSent) {
                                viewHolder.itemView.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK);
                                hasSwipeLeftHapticSent = true;
                            }
                            super.onChildDraw(c, recyclerView, viewHolder, limitedDX, dY, actionState, isCurrentlyActive);
                        } else {
                            int width = viewHolder.itemView.getWidth();
                            float ratio = (width+dX) / (width-swipeMaxPixel);
                            ratio = ratio > 1 ? 1 : ratio < 0 ? 0 : ratio;
                            Log.d(TAG, "onChildDraw: " + ratio);
                            targetDrawable.setAlpha((int)(ratio * 255));
                            targetDrawable.setBounds((int)(viewHolder.itemView.getRight()+ dX), viewHolder.itemView.getTop(), recyclerView.getRight(), viewHolder.itemView.getBottom());
                            targetDrawable.draw(c);
                            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                        }
                        
                    } else {
                        setTouchListener(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                        int[] colorArray = null;
                        List<String> statusList = task.getStatus();
                        if (statusList != null && statusList.size() > 0) {

                            String status = statusList.get(0);
                            if (status.equals("ongoing")) {
                                colorArray = new int[]{0x00FFFFFF, 0xFFAAAAAA, 0xFFFFFF00};
                            } else if (status.equals("pending")) {
                                colorArray = new int[]{0x00FFFFFF, 0xFF00FF00, 0xFFAAAAAA};
                            } else if (status.equals("completed")) {
                                colorArray = new int[]{0x00FFFFFF, 0xFFFFFF00, 0xFF00FF00};
                            }
                        }
                        float progress = dX / swipeMaxPixel;
                        progress = progress > 1 ? 1 : progress;
                        int color = ColorUtils.blendColor(colorArray, progress, 0, 0.5f,1);
                        ColorDrawable targetDrawable = new ColorDrawable(color);
                        targetDrawable.setBounds(0, viewHolder.itemView.getTop(), (int)(viewHolder.itemView.getLeft()+dX), viewHolder.itemView.getBottom());
                        targetDrawable.draw(c);
                        super.onChildDraw(c, recyclerView, viewHolder, limitedDX, dY, actionState, isCurrentlyActive);
                    }
                }
            }

        }
        
    }

    @Override
    public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        super.clearView(recyclerView, viewHolder);
        hasSwipeLeftHapticSent = false;
    }

    private void setTouchListener(final Canvas c, final RecyclerView recyclerView, final RecyclerView.ViewHolder viewHolder, final float dX, final float dY, final int actionState, final boolean isCurrentlyActive) {
        recyclerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (dX > 0) {
                    // When swipe to right, we need to disable swipe to dismiss.
                    returnToPosition = event.getAction() == MotionEvent.ACTION_CANCEL || event.getAction() == MotionEvent.ACTION_UP;
                }
                return false;
            }
        });

    }
}
