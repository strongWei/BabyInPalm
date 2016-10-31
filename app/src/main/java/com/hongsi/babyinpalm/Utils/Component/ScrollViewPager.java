package com.hongsi.babyinpalm.Utils.Component;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by Administrator on 2016/6/16.
 * -------------------------------------------------------------------------------------------------
 * detail: the view pager that can be scrolled or no by touch, just override the function  onTouchEvent and onInterceptTouchEvent
 * author: strong
 * version: 1.0.0
 */
public class ScrollViewPager extends ViewPager {

    private boolean canScroll = false;

    public ScrollViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    //important:
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if(canScroll){
            return super.onInterceptTouchEvent(ev);
        }else
            return false;
    }

    public boolean isCanScroll() {
        return canScroll;
    }

    public void setCanScroll(boolean canScroll){
        this.canScroll = canScroll;
    }

    //important:
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if(canScroll){
            return super.onTouchEvent(ev);
        }else
            return false;
    }
}
