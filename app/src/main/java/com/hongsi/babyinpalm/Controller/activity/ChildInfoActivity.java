package com.hongsi.babyinpalm.Controller.activity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.hongsi.babyinpalm.Domain.Student;
import com.hongsi.babyinpalm.Exception.NetworkErrorException;
import com.hongsi.babyinpalm.Exception.OtherIOException;
import com.hongsi.babyinpalm.Model.GetChildModel;
import com.hongsi.babyinpalm.Model.Login;
import com.hongsi.babyinpalm.R;
import com.hongsi.babyinpalm.Utils.BaseActivity;
import com.hongsi.babyinpalm.Utils.Component.CustomApplication;
import com.hongsi.babyinpalm.Utils.Component.ImageTitleAdapter;
import com.hongsi.babyinpalm.Utils.Component.UsualEmptyLayout;
import com.hongsi.babyinpalm.Utils.Component.UsualHeaderLayout;
import com.hongsi.babyinpalm.Utils.Component.WaitingDialog;
import com.hongsi.babyinpalm.Utils.ToastUtil;

import org.json.JSONException;


/**
 * Created by Administrator on 2016/9/19.
 */
public class ChildInfoActivity extends BaseActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

    private final static int REPEAT = 5;

    private ImageView backU = null;
    private ImageView editU = null;
    private ListView listView = null;
    private WaitingDialog dialog = null;
    private UsualHeaderLayout headerLayout = null;

    private UsualEmptyLayout emptyLayout = null;

    private ImageTitleAdapter adapter = null;


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        //点击某个项目
        Student obj = (Student) adapter.getItem(position);
        if(obj==null){
            return;
        }


        //转到新的界面
        Intent intent = new Intent(ChildInfoActivity.this,ShowChildInfoActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("student",obj);
        intent.putExtras(bundle);
        startActivityForResult(intent,REPEAT);
        //finish();
    }



    //网络线程
    class GetChildAsync extends AsyncTask<Void,Integer,Integer>{
        @Override
        protected Integer doInBackground(Void... params) {
            int code;
            try {
                code = GetChildModel.getChilds();

                switch(code){
                    case 0:
                        return 0;
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

                        code = GetChildModel.getChilds();
                        switch (code){
                            case 0:
                                return 0;
                            case -1:
                                return R.string.account_exception;
                            case -2:
                                return R.string.server_error;
                        }

                    }
                        break;
                    case -2:
                        return R.string.server_error;
                }

            } catch (OtherIOException e) {
                return R.string.other_error;
            } catch (NetworkErrorException e) {
                return R.string.net_error;
            } catch (JSONException e) {
                return R.string.data_error;
            }

            return code;
        }

        @Override
        protected void onPostExecute(Integer integer) {

            int i = integer.intValue();

            if(i!=0) {
                dialog.dismiss();
                dialog.stopAnimate();

                ToastUtil.showToast(ChildInfoActivity.this, i, Toast.LENGTH_SHORT);

                return;
            }else{

                if(GetChildModel.students.size() == 0){
                    listView.setVisibility(View.GONE);
                    emptyLayout.setVisibility(View.VISIBLE);
                }else{

                    adapter.setData(GetChildModel.students);
                    adapter.notifyDataSetChanged();
                }

            }

            dialog.dismiss();
            dialog.stopAnimate();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.listchild_layout);

        backU = (ImageView) findViewById(R.id.back_u);
        backU.setOnClickListener(this);

        editU = (ImageView) findViewById(R.id.edit_u);
        editU.setVisibility(View.INVISIBLE);

        listView = (ListView) findViewById(R.id.child_list);
        adapter = new ImageTitleAdapter(ChildInfoActivity.this,null);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);


        dialog = new WaitingDialog(this, R.style.DialogStyle);

        emptyLayout = (UsualEmptyLayout) findViewById(R.id.empty);
        headerLayout = (UsualHeaderLayout) findViewById(R.id.child_list_header);
        headerLayout.setTitle(R.string.child_info);

        //从网络中获取
        getStudentInfo();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(resultCode == RESULT_OK){
            if(requestCode == REPEAT){
                //重新从数据库加载
                findStudentByDb();
            }

        }else{
            return;
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(dialog!=null && dialog.isShowing()){
            dialog.dismiss();
            dialog.stopAnimate();
            dialog = null;
        }

        backU = null;

        listView = null;

        editU = null;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.back_u:
                onBackPressed();
                finish();
                break;

            case R.id.edit_u:
                //刷新
                getStudentInfo();
                break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private void getStudentInfo(){
        dialog.show();
        dialog.startAnimate();

        //开始请求远程数据
        new GetChildAsync().execute();
    }

    @Override
    protected void onResume() {
        super.onResume();

        //adapter.setData(findStudentByDb());
        //adapter.notifyDataSetChanged();
        //getStudentInfo();
    }

    private void findStudentByDb() {

        SQLiteDatabase db = CustomApplication.getDbHelper().getReadableDatabase();

        Cursor cursor = db.query("student",new String[]{"id","name","grade","sex","birth","cards","pic_path","pic_scale_path","start"},null,null,null,null,null);

        GetChildModel.students.clear();

        while(cursor.moveToNext()){
            Student student = new Student();
            student.setId(cursor.getString(0));
            student.setName(cursor.getString(1));
            student.setGrade(cursor.getString(2));
            student.setSex(cursor.getInt(3));
            student.setBirth(cursor.getString(4));
            student.setCards(cursor.getString(5));
            student.setUrl(cursor.getString(6));
            student.setUrl_scale(cursor.getString(7));
            student.setStart(cursor.getString(8));
            GetChildModel.students.add(student);
        }

        db.close();

        adapter.setData(GetChildModel.students);
        adapter.notifyDataSetChanged();
    }
}
