package com.hongsi.babyinpalm.Utils.Component;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;

import com.hongsi.babyinpalm.R;


/**
 * Created by Administrator on 2016/9/19.
 */
public class UsualEmptyLayout extends RelativeLayout {
    public UsualEmptyLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.usual_empty_layout,this);
    }
}
