package com.hongsi.babyinpalm.Controller.activity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.hongsi.babyinpalm.Domain.Student;
import com.hongsi.babyinpalm.R;
import com.hongsi.babyinpalm.Utils.BaseActivity;
import com.hongsi.babyinpalm.Utils.Component.CustomApplication;
import com.hongsi.babyinpalm.Utils.Component.UsualHeaderLayout;


/**
 * Created by Administrator on 2016/9/20.
 */
public class ShowChildInfoActivity  extends BaseActivity implements View.OnClickListener {

    private TextView nameView = null;
    private TextView birthView = null;
    private TextView gradeView = null;
    private TextView sexView = null;
    private TextView cardsView = null;
    private ImageView imageView = null;
    private TextView startView = null;
    private UsualHeaderLayout headerLayout = null;
    private Student student = null;

    private final static int REFRESH = 6;
    private final static int REFRESH_PIC = 7;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stuinfo_get_layout);

        headerLayout = (UsualHeaderLayout) findViewById(R.id.child_list_header);
        headerLayout.setEdit2Text(R.string.modify);
        headerLayout.getBackView().setOnClickListener(this);
        headerLayout.getEdit2View().setOnClickListener(this);

        nameView = (TextView) findViewById(R.id.get_stu_name);
        birthView = (TextView) findViewById(R.id.get_stu_birth);
        gradeView = (TextView) findViewById(R.id.get_stu_grade);
        sexView = (TextView) findViewById(R.id.get_stu_sex);
        cardsView = (TextView) findViewById(R.id.get_stu_cards);
        imageView = (ImageView) findViewById(R.id.get_stu_pic);
        startView = (TextView) findViewById(R.id.get_stu_start);

        imageView.setOnClickListener(this);


        if(savedInstanceState!=null){
            student = (Student) savedInstanceState.getSerializable("stu");
        }else {
            //从另一个activity获得
            Intent intent = getIntent();
            student = (Student) intent.getExtras().getSerializable("student");
        }


        //设置学生信息
        setStudentToView();
    }

    private void setStudentToView() {
        if(student == null){
            return;
        }
        nameView.setText(student.getName());
        birthView.setText(student.getBirth());
        gradeView.setText(student.getGrade());
        sexView.setText(student.getSex()==0?"男":"女");
        startView.setText(student.getStart());

        String cards = student.getCards();
        cards = cards.substring(1,cards.length()-1).replace(", ","\n");
        cardsView.setText(cards);

        headerLayout.setTitle(student.getName() + getString(R.string.message));

        if(!student.getUrl().isEmpty()){
            imageView.setTag(student.getUrl());
            CustomApplication.getImageLoader().loadNetworkImage(imageView,student.getUrl());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        nameView = null;
        birthView = null;
        gradeView = null;
        sexView = null;
        cardsView = null;
        startView = null;
        imageView = null;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(outState!=null){
            outState.putSerializable("stu",student);
        }
    }

    private Student findStudentById(String id){
        Student student = null;

        SQLiteDatabase db = CustomApplication.getDbHelper().getWritableDatabase();
        Cursor cursor = db.query("student",new String[]{"name","grade","sex","cards","birth","pic_path","pic_scale_path","start"},"id=?",new String[]{id},null,null,null);

        if(cursor.moveToFirst()){
            student = new Student();
            student.setId(id);
            student.setName(cursor.getString(0));
            student.setGrade(cursor.getString(1));
            student.setSex(cursor.getInt(2));
            student.setCards(cursor.getString(3));
            student.setBirth(cursor.getString(4));
            student.setUrl(cursor.getString(5));
            student.setUrl_scale(cursor.getString(6));
            student.setStart(cursor.getString(7));
        }

        db.close();

        return student;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.back_u: {
                //返回键
                onBackPressed();

            }
                break;
            case R.id.edit_2_u:
                //编辑键
            {
                Intent intent = new Intent(ShowChildInfoActivity.this,SetChildInfoActivity.class);
                intent.putExtra("student",student);

                startActivityForResult(intent,REFRESH);
            }
                break;

            case R.id.get_stu_pic:
            {
                //放大用户图片
                Intent personImageIntent = new Intent(ShowChildInfoActivity.this, ActivityPersonImage.class);
                personImageIntent.putExtra("type",1);
                personImageIntent.putExtra("stuId",student.getId());
                personImageIntent.putExtra("image_url",student.getUrl());
                startActivityForResult(personImageIntent,REFRESH_PIC);
            }
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(ShowChildInfoActivity.this,ChildInfoActivity.class);

        setResult(RESULT_OK,intent);

        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_OK){
            if(requestCode == REFRESH ){
                //从数据库中读取数据(出现了信息修改）
                student = findStudentById(student.getId());
                setStudentToView();
            }else if(requestCode == REFRESH_PIC){
                student = findStudentById(student.getId());
                setStudentToView();
            }
        }else{
            return;
        }
    }
}
