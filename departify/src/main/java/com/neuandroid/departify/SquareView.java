package com.neuandroid.departify;

import android.content.Context;
import android.util.AttributeSet;


/**
 * Created by jienanzhang on 13/07/2017.
 */

public class SquareView extends android.support.v7.widget.AppCompatTextView {


    public SquareView(Context context) {
        super(context);
    }

    public SquareView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SquareView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
    }
}
