package com.hongsi.babyinpalm.Utils;

import android.content.Context;
import android.support.annotation.StringRes;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.hongsi.babyinpalm.R;


/**
 * Created by Administrator on 2016/6/14.
 * -------------------------------------------------------------------------------------------------
 * detail: show the toast (single object)
 * author: strong
 * version: 1.0.0
 */
public class ToastUtil {

    private static Toast toast = null;
    private static TextView textView = null;

    public static void showToast(Context context, @StringRes int res, int duration){
        if(toast == null) {
            toast = new Toast(context);
            View view = LayoutInflater.from(context).inflate(R.layout.toast_layout,null);
            toast.setView(view);
            textView = (TextView) view.findViewById(R.id.warn_text);
        }

        textView.setText(res);
        toast.setDuration(duration);
        toast.show();
    }

    public static void showToast(Context context, String res, int duration){
        if(toast == null) {
            toast = new Toast(context);
            View view = LayoutInflater.from(context).inflate(R.layout.toast_layout,null);
            toast.setView(view);
            textView = (TextView) view.findViewById(R.id.warn_text);
        }

        textView.setText(res);
        toast.setDuration(duration);
        toast.show();
    }
}
