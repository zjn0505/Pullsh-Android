package xyz.jienan.pushpull.ui;

import android.content.Context;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;

/**
 * Created by Jienan on 2018/1/5.
 */

public class InputViewPager extends ViewPager {
    private View mCurrentView;
    private Context mContext;

    public InputViewPager(@NonNull Context context) {
        super(context);
        mContext = context;
    }
    public InputViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mCurrentView == null) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }
        int height = 0;
        mCurrentView.measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
        int h = mCurrentView.getMeasuredHeight();

        DisplayMetrics metrics = mContext.getResources().getDisplayMetrics();
        int screenHeight = metrics.heightPixels;
        int screenWidth = metrics.widthPixels;

        if (screenWidth > screenHeight) {
            if (h > screenHeight / 2) {
                h = screenHeight / 2;
            }
        } else {
            if (h > screenHeight / 3.5) {
                h = (int) (screenHeight / 3.5);
            }
        }

        if (h > height) height = h;

        heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void measureCurrentView(View currentView) {
        mCurrentView = currentView;
        requestLayout();
    }

    public int measureFragment(View view) {
        if (view == null)
            return 0;

        view.measure(0, 0);
        return view.getMeasuredHeight();
    }
}
