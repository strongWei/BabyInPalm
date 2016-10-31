package com.hongsi.babyinpalm.Controller.activity;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.hongsi.babyinpalm.Exception.NetworkErrorException;
import com.hongsi.babyinpalm.Exception.OtherIOException;
import com.hongsi.babyinpalm.Model.ChangeUserName;
import com.hongsi.babyinpalm.Model.Login;
import com.hongsi.babyinpalm.R;
import com.hongsi.babyinpalm.Utils.BaseActivity;
import com.hongsi.babyinpalm.Utils.Component.CustomApplication;
import com.hongsi.babyinpalm.Utils.Component.UsualHeaderLayout;
import com.hongsi.babyinpalm.Utils.Component.WaitingDialog;
import com.hongsi.babyinpalm.Utils.ToastUtil;

import org.json.JSONException;

/**
 * Created by Administrator on 2016/10/12 0012.
 */

public class ActivitySetUserInfo extends BaseActivity implements View.OnClickListener {

    private EditText userNameEdit = null;
    private UsualHeaderLayout header = null;
    private String userName = "";
    private WaitingDialog dialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.userinfo_set_layout);

        if(savedInstanceState!=null){

            userName = savedInstanceState.getString("userName");

        }else {
            userName = Login.user.getName();
        }

        initActivity();

        readUserName();
    }

    /** 界面初始化*/
    private void initActivity() {
        userNameEdit = (EditText) findViewById(R.id.set_user_name);

        header = (UsualHeaderLayout) findViewById(R.id.header);

        header.getBackView().setOnClickListener(this);
        header.getEdit2View().setOnClickListener(this);
        header.setEdit2Text(R.string.modify);
        header.setTitle(R.string.changeUserName);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        userNameEdit = null;
        header = null;

        if(dialog != null){
            if(dialog.isShowing()){
                dialog.stopAnimate();
                dialog.dismiss();
            }
        }
    }

    /** 读取当前的用户昵称*/
    private void readUserName(){
        userNameEdit.setText(userName);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString("userName",userName);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.edit_2_u: {
                String newName = userNameEdit.getText().toString().trim();
                switch (validateUserName(newName)){
                    case 0:
                    {
                        if(dialog == null){
                            dialog = new WaitingDialog(ActivitySetUserInfo.this,R.style.DialogStyle);
                            dialog.setText(R.string.waiting);
                            dialog.show();
                            dialog.startAnimate();
                        }
                        new ModifyUserNameAsync().execute(newName);
                    }
                    break;
                    case 1:
                    {
                        onBackPressed();
                    }
                    break;
                    case -1:
                    {
                        ToastUtil.showToast(this,R.string.user_name_empty, Toast.LENGTH_SHORT);
                    }
                    break;
                    case -2:
                    {
                        ToastUtil.showToast(this,R.string.user_name_more_40, Toast.LENGTH_SHORT);
                    }
                    break;
                }
            }
            break;

            case R.id.back_u:{
                onBackPressed();
            }
            break;
        }
    }

    /** 判断修改后的用户名是否合法
     *
     * @return 1(未修改用户名） -1（用户名为空） -2（用户名超过了40个字）0成功
     * */
    public int validateUserName(String newName){

        if(userName.equals(newName)){
            return 1;
        }

        if(newName.equals("")){
            return -1;
        }

        if(newName.length()>40){
            return -2;
        }

        return 0;
    }

    /** http请求修改用户名
     *
     */
    private class ModifyUserNameAsync extends AsyncTask<String,Integer,Integer>{

        private String name = Login.user.getName();

        @Override
        protected Integer doInBackground(String... params) {

            try {

                name = params[0];
                int code =  ChangeUserName.modify(name);
                switch (code){
                    case 0:
                    {
                        return R.string.modify_complete;
                    }
                    case -1:
                    {
                        //需要重新登陆后再进行修改
                        int code1 = Login.login(Login.user.getPhone(), Login.user.getPassword());
                        if(code1 == -1){
                            //用户名或密码错误
                            return R.string.login_error;
                        }else if(code1 == -2){
                            //服务器异常
                            return R.string.server_error;
                        }

                        //进行修改
                        code =  ChangeUserName.modify(params[0]);
                        switch (code){
                            case 0:
                                return R.string.modify_complete;
                            case -1:
                                return R.string.account_exception;
                            case -2:
                                return R.string.server_error;
                            case -3:
                                return R.string.user_name_empty;
                            case -4:
                                return R.string.user_name_more_40;
                        }
                    }
                    case -2:
                    {
                        return R.string.server_error;
                    }
                    case -3:
                    {
                        return R.string.user_name_empty;
                    }
                    case -4:
                    {
                        return R.string.user_name_more_40;
                    }
                }

            } catch (OtherIOException e) {

                return R.string.other_error;

            } catch (NetworkErrorException e) {

                return R.string.net_error;

            } catch (JSONException e) {

                return R.string.data_error;
            }

            return 0;
        }

        @Override
        protected void onPostExecute(Integer integer) {

            dialog.dismiss();
            dialog.startAnimate();

            int i = integer.intValue();

            ToastUtil.showToast(ActivitySetUserInfo.this,i,Toast.LENGTH_SHORT);

            //如果出现重新登陆后依然提示重新登陆或者是用户名或密码出现异常，则转到登陆界面
            if(i==R.string.account_exception || i==R.string.login_error){
                Intent intent = new Intent(ActivitySetUserInfo.this,ActivityLogin.class);
                startActivity(intent);
            }

            if(i==R.string.modify_complete) {
                modifyUserNameInSharePref();
                onBackPressed();
                finish();
            }
        }

    }

    /**在配置文件中进行名字的修改*/
    private void modifyUserNameInSharePref(){

        SQLiteDatabase db =  CustomApplication.getDbHelper().getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put("name",Login.user.getName());

        String whereClause = "phone=?";
        String[] whereArgs = {Login.user.getPhone()};

        db.update("user",cv,whereClause,whereArgs);

        db.close();
    }


}
