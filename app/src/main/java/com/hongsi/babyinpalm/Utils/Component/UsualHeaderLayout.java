package com.hongsi.babyinpalm.Utils.Component;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hongsi.babyinpalm.R;


/**
 * Created by Administrator on 2016/9/14.
 */
public class UsualHeaderLayout extends LinearLayout {

    private TextView titleView = null;
    private ImageView editView = null;
    private ImageView backView = null;
    private TextView edit2View = null;

    public UsualHeaderLayout(Context context, AttributeSet attrs){
        super(context,attrs);

        LayoutInflater.from(context).inflate(R.layout.usual_header_layout,this);

        titleView = (TextView) findViewById(R.id.page_title);
        editView = (ImageView) findViewById(R.id.edit_u);
        backView = (ImageView) findViewById(R.id.back_u);
        edit2View = (TextView) findViewById(R.id.edit_2_u);
    }

    public TextView getTitleView() {
        return titleView;
    }

    public void setTitleView(TextView titleView) {
        this.titleView = titleView;
    }

    public ImageView getEditView() {
        return editView;
    }

    public void setEditView(ImageView editView) {
        this.editView = editView;
    }

    public void setTitle(int resid){
        titleView.setText(resid);
    }

    public void setTitle(String name){
        titleView.setText(name);
    }

    public ImageView getBackView() {
        return backView;
    }

    public TextView getEdit2View(){return edit2View;}

    public void setEdit2Text(String name){edit2View.setText(name);}

    public void setEdit2Text(int res){edit2View.setText(res);}
}
