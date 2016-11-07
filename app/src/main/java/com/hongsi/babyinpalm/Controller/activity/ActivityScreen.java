package com.hongsi.babyinpalm.Controller.activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import com.hongsi.babyinpalm.Exception.NetworkErrorException;
import com.hongsi.babyinpalm.Exception.OtherIOException;
import com.hongsi.babyinpalm.Model.Login;
import com.hongsi.babyinpalm.R;
import com.hongsi.babyinpalm.Utils.BaseActivity;
import com.hongsi.babyinpalm.Utils.Component.CustomApplication;
import com.hongsi.babyinpalm.Utils.ToastUtil;

import org.json.JSONException;

import java.lang.ref.WeakReference;


/**
 * Created by Administrator on 2016/9/26.
 *
 */
public class ActivityScreen extends BaseActivity{

    private final static String TAG = "ActivityScreen";

    private Handler handler = new Handler();

    private boolean logined = false;        //是否已经登陆

    private LoginAsync loginAsync = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //设置布局
        setContentView(R.layout.screen_layout);


        //开始检测
        if(CustomApplication.hasNetwork()){
            //开始自动登陆
            loginAsync = new LoginAsync(this);
            loginAsync.execute();

        }else{
            ToastUtil.showToast(this,R.string.network_unavaibaled, Toast.LENGTH_SHORT);
        }

        handler.postDelayed(new Runnable(){
            @Override
            public void run() {

                if(logined){
                    Intent toMainActivityIntent = new Intent(ActivityScreen.this,ActivityMain.class);
                    startActivity(toMainActivityIntent);

                }else {
                    Intent toLoginActivityIntent = new Intent(ActivityScreen.this, ActivityLogin.class);

                    startActivity(toLoginActivityIntent);
                }
                ActivityScreen.this.finish();
            }
        },3000);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler = null;

        if(loginAsync != null){
            loginAsync.cancel(true);
            loginAsync = null;
        }


    }

    class LoginAsync extends AsyncTask<Void,Integer,Integer>{

        WeakReference<ActivityScreen> weakReference;

        public LoginAsync(ActivityScreen activityScreen){
            weakReference = new WeakReference<ActivityScreen>(activityScreen);
        }

        @Override
        protected Integer doInBackground(Void... params) {
            try {
                if(Login.user.getPhone()==null || ( Login.user.getPhone()!=null && Login.user.getPhone().isEmpty()) ||
                        Login.user.getPassword()==null || ( Login.user.getPassword()!=null && Login.user.getPassword().isEmpty())){
                    return -1;
                }
                int code = Login.login(Login.user.getPhone(),Login.user.getPassword());
                switch (code){
                    case 0:
                        return 0;
                    case 1:
                        return 0;
                    case -2:
                        return -1;
                    case -1:
                        return -1;
                }

                return code;


            } catch (OtherIOException e) {
                return R.string.other_error;
            } catch (NetworkErrorException e) {
                return R.string.net_error;
            } catch (JSONException e) {
                return R.string.data_error;
            }

        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            int i = integer.intValue();

            if(weakReference != null) {
                if(i==0){
                    logined = true;
                }else if(i == -1){
                    logined = false;
                }else{
                    ToastUtil.showToast(weakReference.get(),i,Toast.LENGTH_SHORT);
                    logined = false;
                }
            }

            weakReference.clear();
            weakReference = null;
        }
    }
}
