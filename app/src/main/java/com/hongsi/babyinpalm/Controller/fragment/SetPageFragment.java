package com.hongsi.babyinpalm.Controller.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hongsi.babyinpalm.Controller.activity.ActivityLogin;
import com.hongsi.babyinpalm.Controller.activity.ActivityPersonImage;
import com.hongsi.babyinpalm.Controller.activity.ActivitySetUserInfo;
import com.hongsi.babyinpalm.Controller.activity.ChildInfoActivity;
import com.hongsi.babyinpalm.Exception.NetworkErrorException;
import com.hongsi.babyinpalm.Exception.OtherIOException;
import com.hongsi.babyinpalm.Model.Login;
import com.hongsi.babyinpalm.Model.Logout;
import com.hongsi.babyinpalm.R;
import com.hongsi.babyinpalm.Utils.Component.CustomApplication;
import com.hongsi.babyinpalm.Utils.Component.WaitingDialog;
import com.hongsi.babyinpalm.Utils.ToastUtil;

import org.json.JSONException;


/**
 * Created by Administrator on 2016/6/16.
 */
public class SetPageFragment extends Fragment implements View.OnClickListener {
    private LinearLayout logoutB = null;
    private LinearLayout childInfoB = null;
    private LinearLayout changeUserInfo = null;
    private View view = null;
    private ImageView personImage = null;
    private TextView nameView = null;
    private TextView roleView = null;
    private WaitingDialog dialog= null;

    private Handler handle = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0: {
                    dialog.stopAnimate();
                    dialog.dismiss();


                    //注销
                    Activity act = getActivity();
                    Intent intent = new Intent(act, ActivityLogin.class);
                    startActivity(intent);
                    act.finish();
                }
                break;
                case -1:{
                    dialog.stopAnimate();
                    dialog.dismiss();
                    ToastUtil.showToast(view.getContext(),R.string.net_error,Toast.LENGTH_SHORT);
                }
                break;
                case -2:{
                    dialog.stopAnimate();
                    dialog.dismiss();
                    ToastUtil.showToast(view.getContext(),R.string.other_error,Toast.LENGTH_SHORT);
                }
                break;
                case -3: {
                    dialog.stopAnimate();
                    dialog.dismiss();
                    ToastUtil.showToast(view.getContext(), R.string.data_error, Toast.LENGTH_SHORT);
                }
                break;
                case -4: {
                    dialog.stopAnimate();
                    dialog.dismiss();
                    ToastUtil.showToast(view.getContext(), R.string.server_error, Toast.LENGTH_SHORT);
                }
                break;
            }

            super.handleMessage(msg);
        }
    };;

    //注销线程
    class LogoutThread implements Runnable{

        @Override
        public void run() {
            //code  0("成功")  -3("json数据异常") -4("远程服务器未开启") -5("网络异常") -6("超时")
            //     -7("其它")
            try {
                int result  = Logout.logout();

                if(result == 0){
                    SetPageFragment.this.handle.sendEmptyMessage(0);
                }else if(result == -2){
                    SetPageFragment.this.handle.sendEmptyMessage(4);
                }

            } catch (OtherIOException e) {
                SetPageFragment.this.handle.sendEmptyMessage(-2);
            } catch (NetworkErrorException e) {
                SetPageFragment.this.handle.sendEmptyMessage(-1);
            } catch (JSONException e) {
                SetPageFragment.this.handle.sendEmptyMessage(-3);
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.set_page_layout,container,false);

        initUi();

        //读取信息
        readSettings();


        if(!Login.user.getRole().equals("家长")){
            childInfoB.setVisibility(View.GONE);
        }

        return view;
    }

    /** 界面初始化*/
    private void initUi(){
        nameView = (TextView) view.findViewById(R.id.user_name);
        roleView = (TextView) view.findViewById(R.id.user_role);
        personImage = (ImageView) view.findViewById(R.id.person_image);

        logoutB = (LinearLayout) view.findViewById(R.id.logout);
        childInfoB = (LinearLayout) view.findViewById(R.id.childInfo);
        changeUserInfo = (LinearLayout) view.findViewById(R.id.changeUserName);
        logoutB.setOnClickListener(this);
        childInfoB.setOnClickListener(this);
        changeUserInfo.setOnClickListener(this);
        personImage.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.logout:
            {
                if(dialog == null)
                    dialog = new WaitingDialog(view.getContext(),R.style.DialogStyle);

                dialog.setText(R.string.log_outing);
                dialog.show();
                dialog.startAnimate();

                new Thread(new LogoutThread()).start();
            }
                break;

            case R.id.childInfo:
            {

                Intent toMainActivityIntent = new Intent(getActivity(), ChildInfoActivity.class);

                startActivity(toMainActivityIntent);

            }
                break;

            case R.id.changeUserName:
            {
                //显示修改用户昵称界面
                Intent toMainActivityIntent = new Intent(getActivity(), ActivitySetUserInfo.class);

                startActivity(toMainActivityIntent);
            }
                break;

            case R.id.person_image:
            {
                //放大用户图片
                Intent personImageIntent = new Intent(getActivity(), ActivityPersonImage.class);
                personImageIntent.putExtra("type",0);
                personImageIntent.putExtra("image_url",Login.user.getUrl());
                startActivity(personImageIntent);
            }
                break;
        }
    }

    @Override
    public void onDestroyView(){
        super.onDestroyView();
        logoutB = null;
        childInfoB = null;
        changeUserInfo = null;
        view = null;
        nameView = null;
        roleView = null;
        dialog= null;

        if(dialog != null){
            if(dialog.isShowing()){
                dialog.dismiss();
            }
        }
    }

    //获取个人简要信息
    private void readSettings(){

        if(Login.user.getName().isEmpty()){
            nameView.setText(Login.user.getPhone());
        }else {

            nameView.setText(Login.user.getName());
        }

        roleView.setText(Login.user.getRole());

        //如果头像url不为空，则进行头像的设置
        if(!Login.user.getUrl_scale().isEmpty()){
            personImage.setTag(Login.user.getUrl_scale());
            CustomApplication.getImageLoader().loadNetworkImage(personImage,Login.user.getUrl_scale());
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        //重新设置
        readSettings();
    }
}
