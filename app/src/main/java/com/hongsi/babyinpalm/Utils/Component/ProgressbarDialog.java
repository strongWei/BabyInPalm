package com.hongsi.babyinpalm.Utils.Component;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.hongsi.babyinpalm.R;


/**
 * Created by Administrator on 2016/9/28.
 */
public class ProgressbarDialog extends Dialog{

    private ProgressBar progressBar = null;
    private TextView progressText = null;

    public ProgressbarDialog(Context context, int themeResId) {
        super(context, themeResId);

        setContentView(R.layout.progressbar_layout);

        initDialog();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private void initDialog(){
        progressBar = (ProgressBar) findViewById(R.id.progressbar);
        progressText = (TextView) findViewById(R.id.progress_text);
    }

    /** 显示进度条下面的文字 */
    public void showText(String text){
        progressText.setText(text);
    }

    /** 显示进度条的最大值 */
    public void setProgressBarMax(int max){
        progressBar.setMax(max);
    }

    /** 显示当前进度条的值 */
    public void setProgressBarValue(int value){
        progressBar.setProgress(value);
    }
}
