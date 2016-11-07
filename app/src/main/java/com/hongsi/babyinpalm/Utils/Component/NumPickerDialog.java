package com.hongsi.babyinpalm.Utils.Component;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.hongsi.babyinpalm.Interface.DialogListener;
import com.hongsi.babyinpalm.R;

/**
 * Created by Administrator on 2016/9/27.
 */
public class NumPickerDialog extends Dialog implements View.OnClickListener {

    private TextView titleView = null;
    private Button okBtn = null;
    private Button cancelBtn = null;
    private NumberPicker numberPicker = null;

    //通过一个回调接口来传递是否进行选择‘sure’
    private DialogListener dialogListener = null;

    //当前正在使用的数据组
    private String[] values;


    public String getCurrentValue() {
        return currentValue;
    }

    //当前正在使用的值
    private String currentValue;

    public NumPickerDialog(Context context, int themeResId, DialogListener listener, final String[] values) {
        super(context, themeResId);
        this.dialogListener = listener;
        this.values = values;
        setContentView(R.layout.numberpicker_layout);

        initDialog();

        numberPicker.setMinValue(0);
        numberPicker.setMaxValue(values.length-1);
        numberPicker.setValue(0);
        numberPicker.setDisplayedValues(values);

        currentValue = values[0];

        numberPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                currentValue = values[newVal];
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /** 初始化界面*/
    private void initDialog(){
        okBtn = (Button) findViewById(R.id.ok_btn);
        cancelBtn = (Button) findViewById(R.id.cancel_btn);
        titleView = (TextView) findViewById(R.id.title);
        numberPicker = (NumberPicker) findViewById(R.id.numberPicker);

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

    /**设置标题*/
    public void setTitle(String s){
        titleView.setText(s);
    }
}
