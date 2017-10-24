package xyz.jienan.checked;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.VectorDrawable;
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

    private TaskItemTouchHelperAdapter mAdapter;
    private static final float buttonWidth = 300;
    private static final float SWIPE_MAX_RATIO = 0.6f;
    private final static String TAG = TaskItemTouchHelperCallback.class.getSimpleName();
    private final static float SWIPE_DELETE_THRESOLD = 0.4f;

    private boolean returnToPosition = false;
    private boolean hasSwipeLeftHapticSent = false;
    private boolean hasSwipeRightPhaseOneHapticSent = false;
    private boolean hasSwipeRightPhaseTwoHapticSent = false;
    private float swipeMaxPixel;

    private Drawable drawableOngoing = null;
    private Drawable drawablePending = null;
    private Drawable drawableCompleted = null;
    private Drawable drawableDeleted = null;
    private Drawable[] drawables = {drawableOngoing, drawablePending, drawableCompleted};
    private enum Status {
        Ongoing,
        Pending,
        Completed
    }


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
                    float yText = viewHolder.itemView.getTop() + viewHolder.itemView.getHeight() / 4 * 3;
                    float yIcon = viewHolder.itemView.getTop() + viewHolder.itemView.getHeight() / 4;
                    int iconSize = viewHolder.itemView.getHeight() / 2;

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
                        setTouchListener(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                        int[] colorArray = null;
                        List<String> statusList = task.getStatus();
                        if (statusList != null && statusList.size() > 0) {

                            String status = statusList.get(0);
                            Status status1 = null, status2 = null;
                            if (status.equals("ongoing")) {
                                colorArray = new int[]{Color.TRANSPARENT, Color.GRAY, Color.YELLOW};
                                status1 = Status.Completed;
                                status2 = Status.Pending;
                            } else if (status.equals("pending")) {
                                colorArray = new int[]{Color.TRANSPARENT, Color.GREEN, Color.GRAY};
                                status1 = Status.Ongoing;
                                status2 = Status.Completed;
                            } else if (status.equals("completed")) {
                                colorArray = new int[]{Color.TRANSPARENT, Color.YELLOW, Color.GREEN};
                                status1 = Status.Pending;
                                status2 = Status.Ongoing;
                            }
                            float progress = dX / swipeMaxPixel;
                            progress = progress > 1 ? 1 : progress;
                            int color = ColorUtils.blendColor(colorArray, progress, 0, 0.5f,1);
                            ColorDrawable targetDrawable = new ColorDrawable(color);
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

                            float phaseOneSt = swipeMaxPixel * 0.3f, phaseOneEnd = swipeMaxPixel * 0.6f;
                            if (Math.abs(dX) >= phaseOneSt && Math.abs(dX) <= phaseOneEnd && isCurrentlyActive) {
                                if (!hasSwipeRightPhaseOneHapticSent) {
                                    viewHolder.itemView.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK);
                                    hasSwipeRightPhaseOneHapticSent = true;
                                }
                                float mid = (phaseOneEnd + phaseOneSt) / 2;
                                int alpha = (int)((1 - Math.abs(dX - mid)/ (mid - phaseOneSt)) * 255);
                                p.setAlpha(alpha);
//                                c.drawText(status1.toString(), 20, y, p);
                                Drawable drawable = drawables[status1.ordinal()];
                                drawable.setAlpha(alpha);
                                drawable.setBounds(20, (int)yIcon, 20 + iconSize, (int)yIcon + iconSize);
                                drawable.draw(c);
                            }

                            float phaseTwoSt = 0.7f, phaseTwoEnd = 0.85f;
                            if (Math.abs(dX) >= swipeMaxPixel*phaseTwoSt && isCurrentlyActive) {
                                if (!hasSwipeRightPhaseTwoHapticSent) {
                                    viewHolder.itemView.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK);
                                    hasSwipeRightPhaseTwoHapticSent = true;
                                }
                                int alpha = (int)((dX - swipeMaxPixel * phaseTwoSt) / swipeMaxPixel/ (phaseTwoEnd-phaseTwoSt) * 255);
                                alpha = dX >= swipeMaxPixel*phaseTwoEnd ? 255 : alpha;
                                p.setAlpha(alpha);
//                                c.drawText(status2.toString(), 20, y, p);
                                Drawable drawable = drawables[status2.ordinal()];
                                drawable.setAlpha(alpha);
                                drawable.setBounds(20, (int)yIcon, 20 + iconSize, (int)yIcon + iconSize);
                                drawable.draw(c);
                            }
                            super.onChildDraw(c, recyclerView, viewHolder, limitedDX, dY, actionState, isCurrentlyActive);
                        }

                    }
                }
            }

        }
        
    }

    @Override
    public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        super.clearView(recyclerView, viewHolder);
        hasSwipeLeftHapticSent = false;
        hasSwipeRightPhaseOneHapticSent = false;
        hasSwipeRightPhaseTwoHapticSent = false;
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
