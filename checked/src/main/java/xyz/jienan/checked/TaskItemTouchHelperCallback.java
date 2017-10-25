package xyz.jienan.checked;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.support.graphics.drawable.VectorDrawableCompat;
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

    private final static String TAG = TaskItemTouchHelperCallback.class.getSimpleName();
    private final static float SWIPE_DELETE_THRESOLD = 0.4f;
    private final static float SWIPE_MAX_RATIO = 0.6f;
    private final static int VIBRATION = HapticFeedbackConstants.LONG_PRESS;

    private boolean returnToPosition = false;
    private boolean hasSwipeLeftHapticSent = false;
    private boolean hasSwipeRightPhaseOneHapticSent = false;
    private boolean hasSwipeRightPhaseTwoHapticSent = false;
    private float swipeMaxPixel;
    private TaskItemTouchHelperAdapter mAdapter;

    private Drawable drawableOngoing = null;
    private Drawable drawablePending = null;
    private Drawable drawableCompleted = null;
    private Drawable drawableDeleted = null;
    private Drawable[] drawables = {drawableOngoing, drawablePending, drawableCompleted};

    private final float phaseOneSt = 0.3f, phaseOneEnd = 0.6f;
    private final float phaseTwoSt = 0.7f, phaseTwoEnd = 0.85f;
    private int swipeRightLastColor = 0;

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
            return super.convertToAbsoluteDirection(flags, layoutDirection);
        } else {
            returnToPosition = false;
            return 0;
        }
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        if (direction == ItemTouchHelper.LEFT)
            mAdapter.onItemDismiss(viewHolder.getAdapterPosition());
        Log.d(TAG, "onSwiped: triggered");
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
                    float yText = viewHolder.itemView.getTop() + viewHolder.itemView.getHeight() / 4 * 3;
                    float yIcon = viewHolder.itemView.getTop() + viewHolder.itemView.getHeight() / 4;
                    int iconSize = viewHolder.itemView.getHeight() / 2;

                    if (dX < 0) {
                        // Swipe left
                        ColorDrawable targetDrawable = new ColorDrawable(Color.RED);
                        if (isCurrentlyActive) {
                            float ratio = Math.abs(dX)/(recyclerView.getWidth() * SWIPE_DELETE_THRESOLD);
                            ratio = ratio >= 1 ? 1 : ratio;
                            targetDrawable.setAlpha((int)(ratio * ratio * 255));
                            targetDrawable.setBounds((int)(viewHolder.itemView.getRight()+ dX), viewHolder.itemView.getTop(), recyclerView.getRight(), viewHolder.itemView.getBottom());
                            targetDrawable.draw(c);
                            if (Math.abs(dX) >= viewHolder.itemView.getWidth()*SWIPE_DELETE_THRESOLD) {
                                if (!hasSwipeLeftHapticSent) {
                                    viewHolder.itemView.performHapticFeedback(VIBRATION);
                                    hasSwipeLeftHapticSent = true;
                                    Log.d(TAG, "onChildDraw: viber");
                                }
                            } else {
                                hasSwipeLeftHapticSent = false;
                                Log.d(TAG, "onChildDraw: disabled");
                            }
                            if (drawableDeleted == null) {
                                Context context = recyclerView.getContext();
                                drawableDeleted = VectorDrawableCompat.create(context.getResources(), R.drawable.ic_delete, null);
                            }

                            drawableDeleted.setBounds(viewHolder.itemView.getRight() - iconSize - 20, (int)yIcon,
                                    viewHolder.itemView.getRight() - 20, (int)yIcon + iconSize);
                            drawableDeleted.draw(c);

                            super.onChildDraw(c, recyclerView, viewHolder, limitedDX, dY, actionState, isCurrentlyActive);
                        } else {
                            int width = viewHolder.itemView.getWidth();
                            float ratio;
                            if (dX > -swipeMaxPixel) {
                                ratio = Math.abs(dX)/(recyclerView.getWidth() * SWIPE_DELETE_THRESOLD);
                                ratio = ratio * ratio;
                            } else {
                                ratio = (width+dX) / (width-swipeMaxPixel);
                            }
                            ratio = ratio > 1 ? 1 : ratio < 0 ? 0 : ratio;
                            targetDrawable.setAlpha((int)(ratio * 255));
                            targetDrawable.setBounds((int)(viewHolder.itemView.getRight()+ dX), viewHolder.itemView.getTop(), recyclerView.getRight(), viewHolder.itemView.getBottom());
                            targetDrawable.draw(c);
                            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                        }

                    } else {
                        // Swipe right
                        int[] colorArray = null;
                        List<String> statusList = task.getStatus();
                        if (statusList != null && statusList.size() > 0) {
                            setTouchListener(recyclerView, viewHolder, dX);
                            String status = statusList.get(0);
                            Status status1 = null, status2 = null;
                            if (status.equalsIgnoreCase(Status.Ongoing.toString())) {
                                colorArray = new int[]{Color.GREEN & 0x00FFFFFF, Color.GRAY, Color.YELLOW};
                                status1 = Status.Completed;
                                status2 = Status.Pending;
                            } else if (status.equalsIgnoreCase(Status.Pending.toString())) {
                                colorArray = new int[]{Color.YELLOW & 0x00FFFFFF, Color.GREEN, Color.GRAY};
                                status1 = Status.Ongoing;
                                status2 = Status.Completed;
                            } else if (status.equalsIgnoreCase(Status.Completed.toString())) {
                                colorArray = new int[]{Color.GRAY & 0x00FFFFFF, Color.YELLOW, Color.GREEN};
                                status1 = Status.Pending;
                                status2 = Status.Ongoing;
                            }
                            float progress = dX / swipeMaxPixel;
                            progress = progress > 1 ? 1 : progress;
                            int color = ColorUtils.blendColor(colorArray, progress, 0, 0.5f,1);
                            ColorDrawable targetDrawable = null;
                            if (isCurrentlyActive) {
                                swipeRightLastColor = color;
                                targetDrawable = new ColorDrawable(color);
                            } else {
                                targetDrawable = new ColorDrawable(swipeRightLastColor);
                                targetDrawable.setAlpha((int)(progress*255));
                            }
                            targetDrawable.setBounds(0, viewHolder.itemView.getTop(), (int)(viewHolder.itemView.getLeft()+dX), viewHolder.itemView.getBottom());
                            targetDrawable.draw(c);

                            Paint p = new Paint();
                            p.setTextSize(viewHolder.itemView.getHeight() / 2);
                            p.setAntiAlias(true);
                            p.setColor(Color.WHITE);
                            p.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));

                            if (drawableOngoing == null) {
                                Context context = recyclerView.getContext();
                                drawableOngoing = VectorDrawableCompat.create(context.getResources(), R.drawable.ic_ongoing, null);
                                drawablePending = VectorDrawableCompat.create(context.getResources(), R.drawable.ic_pending, null);
                                drawableCompleted = VectorDrawableCompat.create(context.getResources(), R.drawable.ic_check, null);
                                drawables[0] = drawableOngoing;
                                drawables[1] = drawablePending;
                                drawables[2] = drawableCompleted;
                            }

                            if (Math.abs(dX) >= phaseOneSt*swipeMaxPixel && Math.abs(dX) <= phaseOneEnd*swipeMaxPixel && isCurrentlyActive) {
                                if (!hasSwipeRightPhaseOneHapticSent) {
                                    viewHolder.itemView.performHapticFeedback(VIBRATION);
                                    hasSwipeRightPhaseOneHapticSent = true;
                                }
                                float mid = (phaseOneEnd + phaseOneSt) * swipeMaxPixel / 2;
                                int alpha = (int)(ColorUtils.converterB(1 - Math.abs(dX - mid)/ (mid - phaseOneSt*swipeMaxPixel)) * 255);
//                                p.setAlpha(alpha);
//                                c.drawText(status1.toString(), 20, y, p);
                                Drawable drawable = drawables[status1.ordinal()];
                                drawable.setAlpha(alpha);
                                drawable.setBounds(20, (int)yIcon, 20 + iconSize, (int)yIcon + iconSize);
                                drawable.draw(c);
                            } else {
                                hasSwipeRightPhaseOneHapticSent = false;
                            }

                            if (Math.abs(dX) >= swipeMaxPixel*phaseTwoSt && isCurrentlyActive) {
                                if (!hasSwipeRightPhaseTwoHapticSent) {
                                    viewHolder.itemView.performHapticFeedback(VIBRATION);
                                    hasSwipeRightPhaseTwoHapticSent = true;
                                }
                                int alpha = (int)(ColorUtils.converterB((dX - swipeMaxPixel * phaseTwoSt) / swipeMaxPixel / (phaseTwoEnd-phaseTwoSt)) * 255);
//                                p.setAlpha(alpha);
//                                c.drawText(status2.toString(), 20, y, p);
                                Drawable drawable = drawables[status2.ordinal()];
                                drawable.setAlpha(alpha);
                                drawable.setBounds(20, (int)yIcon, 20 + iconSize, (int)yIcon + iconSize);
                                drawable.draw(c);
                            } else {
                                hasSwipeRightPhaseTwoHapticSent = false;
                            }
                            colorArray[0] = colorArray[0] | 0xFF000000;
                            int colorIndicator = ColorUtils.blendColor(colorArray, progress, 0, 0.5f,1);
                            ((TaskAdapter.TaskViewHolder)viewHolder).taskIndicator.setBackgroundColor(colorIndicator);
                            super.onChildDraw(c, recyclerView, viewHolder, limitedDX, dY, actionState, isCurrentlyActive);
                        }

                    }
                }
            }

        } else {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        }

    }

    @Override
    public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        super.clearView(recyclerView, viewHolder);
        hasSwipeLeftHapticSent = false;
        hasSwipeRightPhaseOneHapticSent = false;
        hasSwipeRightPhaseTwoHapticSent = false;
    }

    private void setTouchListener(final RecyclerView recyclerView, final RecyclerView.ViewHolder viewHolder, final float dX) {
        final int[] position = {viewHolder.getAdapterPosition()};
        recyclerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (dX > 0) {
                    // When swipe to right, we need to disable swipe to dismiss.
                    returnToPosition = event.getAction() == MotionEvent.ACTION_CANCEL || event.getAction() == MotionEvent.ACTION_UP;
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        Log.e(TAG, "Position : "+ position[0]);
                        if (position[0] > 0) {
                            Status status = null;
                            String statusString = null;
                            if (dX >= phaseOneSt*swipeMaxPixel && dX <= phaseOneEnd*swipeMaxPixel ) {
                                status = getStatus(position[0], 1);
                                mAdapter.onItemUpdate(position[0], status);
                            } else if (dX >= phaseTwoSt*swipeMaxPixel) {
                                status = getStatus(position[0], 2);
                                mAdapter.onItemUpdate(position[0], status);
                            } else {
                                List<TaskEntity> tasks = ((TaskAdapter)recyclerView.getAdapter()).getTasks();
                                TaskEntity task = tasks.get(position[0] - 1);
                                statusString = task.getStatus().get(0);

                            }
                            statusString = statusString == null ? status.toString() : statusString;
                            ((TaskAdapter.TaskViewHolder)viewHolder).taskIndicator.setBackgroundColor(ColorUtils.getStatusColor(statusString));
                            position[0] = 0; // This is walk around since we only what the ACTION_UP logic triggered once for each swipe.
                        }
                    }
                }
                return false;
            }

            private Status getStatus(int position, int i) {
                Status result = null;
                int indexInList = position - 1;
                List<TaskEntity> tasks = ((TaskAdapter)recyclerView.getAdapter()).getTasks();
                TaskEntity task = tasks.get(indexInList);
                String status = task.getStatus().get(0);
                if (status.equalsIgnoreCase(Status.Ongoing.toString())) {
                    result = i == 1 ? Status.Completed : Status.Pending;
                } else if (status.equalsIgnoreCase(Status.Pending.toString())) {
                    result = i == 1 ? Status.Ongoing : Status.Completed;
                } else if (status.equalsIgnoreCase(Status.Completed.toString())) {
                    result = i == 1 ? Status.Pending : Status.Ongoing;
                }
                return result;
            }
        });

    }
}
