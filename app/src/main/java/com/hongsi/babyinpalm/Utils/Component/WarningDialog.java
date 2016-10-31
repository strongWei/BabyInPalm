package com.hongsi.babyinpalm.Utils.Component;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.hongsi.babyinpalm.Interface.DialogListener;
import com.hongsi.babyinpalm.R;

/**
 * Created by Administrator on 2016/9/27.
 */
public class WarningDialog extends Dialog implements View.OnClickListener {

    private TextView warnText = null;
    private Button okBtn = null;
    private Button cancelBtn = null;

    //通过一个回调接口来传递是否进行选择‘sure’
    private DialogListener dialogListener = null;

    public WarningDialog(Context context, int themeResId,DialogListener listener) {
        super(context, themeResId);
        this.dialogListener = listener;
        setContentView(R.layout.warning_layout);

        initDialog();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /** 初始化界面*/
    private void initDialog(){
        okBtn = (Button) findViewById(R.id.ok_btn);
        cancelBtn = (Button) findViewById(R.id.cancel_btn);
        warnText = (TextView) findViewById(R.id.warning_text);

        okBtn.setOnClickListener(this);
        cancelBtn.setOnClickListener(this);
    }

    /** 按纽点击*/
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.cancel_btn: {
                dialogListener.getResultBoolean(false);
            }
                break;
            case R.id.ok_btn:
                dialogListener.getResultBoolean(true);
                break;
        }
    }

    /** 设置显示的字符串*/
    public void setWarnText(int resId){
        warnText.setText(resId);
    }
}
