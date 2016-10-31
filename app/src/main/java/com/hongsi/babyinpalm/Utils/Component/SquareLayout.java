package com.hongsi.babyinpalm.Utils.Component;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;


/**
 * Created by Administrator on 2016/6/9.
 */
public class SquareLayout extends LinearLayout{

    public SquareLayout(Context context, AttributeSet attrs){
        super(context,attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(getDefaultSize(0,widthMeasureSpec),getDefaultSize(0,heightMeasureSpec));

        int childWidthSize = getMeasuredWidth();
        //int childHeightSize = getMeasuredHeight();

        heightMeasureSpec = widthMeasureSpec = MeasureSpec.makeMeasureSpec(childWidthSize,MeasureSpec.EXACTLY);

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
