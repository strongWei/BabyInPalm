package com.hongsi.babyinpalm.Controller.activity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.hongsi.babyinpalm.Domain.Student;
import com.hongsi.babyinpalm.Exception.NetworkErrorException;
import com.hongsi.babyinpalm.Exception.OtherIOException;
import com.hongsi.babyinpalm.Model.ChangeStuInfo;
import com.hongsi.babyinpalm.Model.Login;
import com.hongsi.babyinpalm.R;
import com.hongsi.babyinpalm.Utils.BaseActivity;
import com.hongsi.babyinpalm.Utils.Component.UsualHeaderLayout;
import com.hongsi.babyinpalm.Utils.Component.WaitingDialog;
import com.hongsi.babyinpalm.Utils.ToastUtil;

import org.json.JSONException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


/**
 * Created by Administrator on 2016/9/20.
 */
public class SetChildInfoActivity extends BaseActivity implements View.OnClickListener, RadioGroup.OnCheckedChangeListener {

    private Uri imageUri;

    private EditText nameView = null;
    private EditText birthView = null;
    private TextView gradeView = null;
    private TextView cardsView = null;
    private TextView startView = null;
    private RadioGroup radioGroup = null;
    private RadioButton maleBtn = null;
    private RadioButton femaleBtn = null;
    private Button dateBtn = null;
    private ImageView imageView = null;
    private UsualHeaderLayout headerLayout = null;

    private Student student = null;

    private Calendar calendar = Calendar.getInstance();

    private DateFormat dateFormate = new SimpleDateFormat("yyyy-MM-dd");

    //当前需要更改的项
    private String currentBirth;
    private String currentName;

    private WaitingDialog waitingDialog = null;

    //定义日期
    private DatePickerDialog dateDialog = null;

    //日期设置监听器
    private DatePickerDialog.OnDateSetListener dateSet = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, monthOfYear);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            updateDate();
        }
    };


    //修改后学生的性别
    private int newSex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stuinfo_set_layout);

        headerLayout = (UsualHeaderLayout) findViewById(R.id.child_list_header);
        headerLayout.setTitle(R.string.child_info);

        headerLayout.setEdit2Text(R.string.complete);
        headerLayout.getEditView().setOnClickListener(this);
        headerLayout.getEdit2View().setOnClickListener(this);

        nameView = (EditText) findViewById(R.id.set_stu_name);
        birthView = (EditText) findViewById(R.id.set_stu_birth);
        gradeView = (TextView) findViewById(R.id.set_stu_grade);


        dateBtn = (Button) findViewById(R.id.date_btn);
        dateBtn.setOnClickListener(this);

        maleBtn = (RadioButton) findViewById(R.id.male_radio);
        femaleBtn = (RadioButton) findViewById(R.id.female_radio);

        cardsView = (TextView) findViewById(R.id.set_stu_cards);
        headerLayout.getBackView().setOnClickListener(this);

        startView = (TextView) findViewById(R.id.set_stu_start);

        dateDialog = new DatePickerDialog(this, R.style.DialogStyle, dateSet
        ,calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),calendar.get(Calendar.DAY_OF_MONTH));

        radioGroup = (RadioGroup) findViewById(R.id.group_radio);
        radioGroup.setOnCheckedChangeListener(this);

        if(savedInstanceState!=null){
            student = (Student) savedInstanceState.getSerializable("studentTemp");
        }else {
            //从另一个activity获得
            Intent intent = getIntent();
            student = (Student) intent.getSerializableExtra("student");
        }

        if(student!=null) {
            //设置学生信息
            nameView.setText(student.getName());

            String birth = student.getBirth();
            if(!birth.isEmpty()){
                try {
                    Date date = dateFormate.parse(birth);
                    calendar.setTime(date);
                } catch (ParseException e) {
                    ToastUtil.showToast(this,R.string.date_error, Toast.LENGTH_SHORT);
                }
            }

            birthView.setText(birth);
            gradeView.setText(student.getGrade());

            int sex = student.getSex();
            newSex = sex;
            if(sex == 0){

            }else{
                femaleBtn.setChecked(true);
            }

            String cards = student.getCards();
            cards = cards.substring(1, cards.length() - 1).replace(", ", "\n");
            cardsView.setText(cards);

            startView.setText(student.getStart());
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(dateDialog != null && dateDialog.isShowing()){
            dateDialog.dismiss();
        }

        if(waitingDialog != null && waitingDialog.isShowing()){
            waitingDialog.dismiss();
            waitingDialog.stopAnimate();
            waitingDialog = null;
        }

        dateDialog = null;
        nameView = null;
        birthView = null;
        gradeView = null;
        cardsView = null;
        dateBtn = null;
        startView = null;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(outState!=null){
            outState.putSerializable("studentTemp",student);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.back_u:
                //返回键
                onBackPressed();
                finish();
                break;
            case R.id.edit_2_u:
                //提交键
            {
                //可以进行更新
                if(updateOrNot() == 0){
                    if(waitingDialog == null){
                        waitingDialog = new WaitingDialog(this,R.style.DialogStyle);
                    }

                    waitingDialog.setText(R.string.waiting);
                    waitingDialog.show();
                    waitingDialog.startAnimate();

                    new ChangeStuInfoAsync().execute();

                }else if(updateOrNot() == -1){
                    onBackPressed();
                    finish();
                }
            }
                break;
            case R.id.date_btn:
                dateDialog.updateDate(calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),calendar.get(Calendar.DAY_OF_MONTH));
                dateDialog.show();
                break;

        }
    }

    //更新日期显示
    private void updateDate(){
        birthView.setText(dateFormate.format(calendar.getTime()));
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(resultCode == RESULT_OK && requestCode == 2){

            //选取图片成功
            File outputImage = new File(Environment.getExternalStorageDirectory(),
                    "appSchool/temp/output_image.jpg");

            try{
                if(outputImage.exists()){
                    outputImage.delete();
                }
                outputImage.createNewFile();
            }catch(IOException e){
                e.printStackTrace();
            }

            imageUri = Uri.fromFile(outputImage);

            //开启剪切程序
            Intent intent = new Intent("com.android.camera.action.CROP");
            intent.setDataAndType(data.getData(), "image/*");
            intent.putExtra("crop", "true");
            intent.putExtra("aspectX", 1);
            intent.putExtra("aspectY", 1);
            intent.putExtra("outputX", 120);
            intent.putExtra("outputY", 120);
            intent.putExtra("return-data", false);
            intent.putExtra("scale", true);//黑边
            intent.putExtra("scaleUpIfNeeded", true);//黑边
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);

            startActivityForResult(intent,3);

        }else if(requestCode == 3 && resultCode == RESULT_OK){
            try{
                BitmapFactory.Options opt = new BitmapFactory.Options();
                opt.inPreferredConfig = Bitmap.Config.RGB_565;
                Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri),null,opt);

                imageView.refreshDrawableState();
                imageView.setImageBitmap(bitmap);

            }catch(FileNotFoundException e){
                e.printStackTrace();
            }
        }

    }

    private int updateOrNot(){

        //判断学生姓名
        currentName = nameView.getEditableText().toString().trim();
        currentBirth = birthView.getEditableText().toString().trim();

        if(currentName.isEmpty()){
            //学生姓名不能为空
            ToastUtil.showToast(this,R.string.name_empty,Toast.LENGTH_SHORT);
            return 1;
        }

        if(currentName.length() > 40){
            //学生姓名不能超过40个字
            ToastUtil.showToast(this,R.string.stu_name_more_40,Toast.LENGTH_SHORT);
            return 1;
        }


        if(!currentBirth.isEmpty()) {
            try {
                Date birthDate = dateFormate.parse(currentBirth);
                Date startDate = dateFormate.parse(student.getStart());

                //出生日期比入学日期大
                if (birthDate.compareTo(startDate) > 0) {
                    ToastUtil.showToast(this, R.string.birth_after_start, Toast.LENGTH_SHORT);
                    return 1;
                }

            } catch (ParseException e) {
                ToastUtil.showToast(this, R.string.start_invalid, Toast.LENGTH_SHORT);
                return 1;
            }
        }

        if(!currentName.equals(student.getName())){
            //姓名已发生改变
            return 0;
        }

        if(!currentBirth.equals(student.getBirth())){
            //生日发生改变
            return 0;
        }

        if(newSex != student.getSex()){
            //学生性别发生改变
            return 0;
        }

        return -1;
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        if(checkedId == R.id.female_radio){
            newSex = 1;
        }else{
            newSex = 0;
        }
    }

    /** 修改学生信息线程*/
    private class ChangeStuInfoAsync extends AsyncTask<Void,Integer,Integer>{

        public ChangeStuInfoAsync(){
            super();
        }

        @Override
        protected Integer doInBackground(Void... params) {

            try {

                int code =  ChangeStuInfo.change(student.getId(),currentName,newSex,currentBirth,student.getStart());
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
                        code =  ChangeStuInfo.change(student.getId(),currentName,newSex,currentBirth,student.getStart());
                        switch (code){
                            case 0:
                                return R.string.modify_complete;
                            case -1:
                                return R.string.account_exception;
                            case -2:
                                return R.string.server_error;
                            case 1:
                                return R.string.stuid_invalid;
                            case 2:
                                return R.string.name_empty;
                            case 3:
                                return R.string.stu_name_more_40;
                            case 5:
                                return R.string.birth_after_start;

                        }
                    }
                    case -2:
                    {
                        return R.string.server_error;
                    }
                    case 1:
                        return R.string.stuid_invalid;
                    case 2:
                        return R.string.name_empty;
                    case 3:
                        return R.string.stu_name_more_40;
                    case 5:
                        return R.string.birth_after_start;
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
            waitingDialog.dismiss();
            waitingDialog.stopAnimate();

            int i = integer.intValue();

            ToastUtil.showToast(SetChildInfoActivity.this,i, Toast.LENGTH_SHORT);

            //如果出现重新登陆后依然提示重新登陆或者是用户名或密码出现异常，则转到登陆界面
            if(i==R.string.account_exception || i==R.string.login_error){
                Intent intent = new Intent(SetChildInfoActivity.this,ActivityLogin.class);
                startActivity(intent);
                finish();
                return;
            }

            if(i==R.string.modify_complete){
                Intent intent = new Intent(SetChildInfoActivity.this,ShowChildInfoActivity.class);
                setResult(RESULT_OK,intent);
                finish();
            }
        }
    }
}
