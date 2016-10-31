package com.hongsi.babyinpalm.Controller.activity;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.hongsi.babyinpalm.Domain.ImageData;
import com.hongsi.babyinpalm.R;
import com.hongsi.babyinpalm.Utils.BaseActivity;
import com.hongsi.babyinpalm.Utils.Component.BottomPopView;
import com.hongsi.babyinpalm.Utils.Component.CustomApplication;
import com.hongsi.babyinpalm.Utils.Component.UsualHeaderLayout;
import com.hongsi.babyinpalm.Utils.ImageResUtils;
import com.hongsi.babyinpalm.dll.clipImage.ClipImageActivity;
import com.hongsi.babyinpalm.dll.SelectImage.imageloader.SingleSelectImageActivity;

import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.List;


/**
 * Created by Administrator on 2016/10/18.
 */
public class ActivityPersonImage extends BaseActivity implements View.OnClickListener {

    private final String TAG = "ActivityPersonImage";
    private final int LOCAL = 1;
    private final int CAMERA = 2;
    private final int CUT = 3;

    private int type;
    private String stuId; //当type==1时

    //加载图片的部件
    private ImageView personImage = null;
    private UsualHeaderLayout header = null;
    private String clipImagePath = "";          //被剪切后的图片路径
    private String imageUrl = "";           //旧大图的图片路径
    private RelativeLayout imageLayout = null;      //进行左右切换的区域

    private float cursorLastX = 0;
    private float cursorFirstX = 0;

    private boolean editable = true;        //是否可编辑
    private boolean collape = false;        //是否可切换
    private int currentPosition;            //当前图片在数据哪个位置
    private List<ImageData> imageList;      //切换的图片数组

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.person_image);

        if(savedInstanceState!=null){
            imageUrl = savedInstanceState.getString("image_url");
            clipImagePath = savedInstanceState.getString("clipImagePath");

            type = savedInstanceState.getInt("type");
            if(type==1)
                stuId = savedInstanceState.getString("stuId");

            editable = savedInstanceState.getBoolean("editable");
            collape = savedInstanceState.getBoolean("collape");

            if(collape){
                currentPosition = savedInstanceState.getInt("position");
                imageList = (List<ImageData>) savedInstanceState.getSerializable("imagelist");
            }

        }else{
            Intent intent = getIntent();

            imageUrl = intent.getStringExtra("image_url");
            type = intent.getIntExtra("type",-1);
            if(type == 1){
                stuId = intent.getStringExtra("stuId");
            }

            //获取是否可编辑选项
            editable = intent.getBooleanExtra("no-change",false);

            //获取是否可切换选项
            currentPosition = intent.getIntExtra("position",-1);
            if(currentPosition == -1){
                collape = false;
            }else{
                collape = true;
                imageList = (List<ImageData>) intent.getSerializableExtra("imagelist");
            }
        }

        initUi();


        //加载大图
        if(!imageUrl.isEmpty()){
            personImage.setTag(imageUrl);
            CustomApplication.getImageLoader().loadNetworkImage(personImage,imageUrl);
        }

    }


    /**界面初始化*/
    private void initUi(){
        personImage = (ImageView) findViewById(R.id.big_image);
        /*
        ViewGroup.LayoutParams lp = personImage.getLayoutParams();

        int width = this.getWindowManager().getDefaultDisplay().getWidth();
        lp.width = width;
        lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        personImage.setLayoutParams(lp);
        personImage.setMinimumWidth(width);
        personImage.setMaxWidth(width);
        personImage.setMaxHeight(5* width);
        */

        header = (UsualHeaderLayout) findViewById(R.id.header);

        String title = this.getIntent().getStringExtra("title");
        header.setTitle(title);

        header.setEdit2Text(R.string.modify);

        header.getEdit2View().setOnClickListener(this);
        header.getBackView().setOnClickListener(this);

        if(!editable){
            //隐藏
            header.getEditView().setVisibility(View.GONE);
        }

        if(collape) {
            imageLayout = (RelativeLayout) findViewById(R.id.imagelayout);
            imageLayout.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    //进行监听左右滑动
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN: {
                            cursorFirstX = event.getX();
                        }
                        break;
                        case MotionEvent.ACTION_MOVE: {
                            cursorLastX = event.getX();
                        }
                        break;

                        case MotionEvent.ACTION_UP: {
                            if (cursorFirstX - cursorLastX > 0 && Math.abs(cursorFirstX - cursorLastX) > 25) {
                                //向左滑动
                                toLeftImage();
                            } else if (cursorFirstX - cursorLastX < 0 && Math.abs(cursorFirstX - cursorLastX) > 25) {
                                //向右滑动
                                toRightImage();
                            }
                        }
                        break;
                    }
                    return true;
                }
            });
        }
    }

    /**
     * 切换到左边的照片
     */
    private void toLeftImage() {

        if(currentPosition -1 >= 0){
            imageUrl = imageList.get(--currentPosition).getUrl();
        }else{
            return;
        }

        personImage.setTag(imageUrl);
        CustomApplication.getImageLoader().loadNetworkImage(personImage,imageUrl);
    }

    /**
     * 切换到右边的照片
     */
    private void toRightImage() {

        if(currentPosition + 1 <= imageList.size() - 1){
            imageUrl = imageList.get(++currentPosition).getUrl();
        }else{
            return;
        }

        personImage.setTag(imageUrl);
        CustomApplication.getImageLoader().loadNetworkImage(personImage,imageUrl);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        personImage = null;
        header = null;

        if(imageList != null) {
            imageList.clear();
            imageList = null;
        }
    }



    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.back_u:
                onBackPressed();
                break;
            case R.id.edit_2_u: {
                final BottomPopView bottomPopView = new BottomPopView(this, personImage) {
                    @Override
                    public void onTopButtonClick() {
                        //拍照
                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        //intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(ClipImageActivity.getTempImageBig()));
                        startActivityForResult(intent, CAMERA);
                        dismiss();
                    }

                    @Override
                    public void onBottomButtonClick() {
                        //选择图片
                        Intent intent = new Intent(ActivityPersonImage.this, SingleSelectImageActivity.class);
                        startActivityForResult(intent,LOCAL);
                        dismiss();
                    }
                };
                bottomPopView.setTopText("拍照");
                bottomPopView.setBottomText("选择图片");
                // 显示底部菜单
                bottomPopView.show();
            }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode != RESULT_OK){
            return;
        }

        switch (requestCode) {
            case CUT: {
                /*
                String path = data.getStringExtra(ClipImageActivity.RESULT_PATH);
                Bitmap photo = BitmapFactory.decodeFile(path);
                personImage.setImageBitmap(photo);
                */
                /*
                imageUrl = Login.url;


                //加载大图
                if(!imageUrl.isEmpty()){
                    personImage.setTag(imageUrl);
                    CustomApplication.getImageLoader().loadNetworkImage(personImage,imageUrl);
                }
                */

                //这里要返回是因为放大缩小在这里不起作用，这是一个bug

                //覆盖旧的图片

                Intent intent = new Intent(ActivityPersonImage.this,ShowChildInfoActivity.class);
                setResult(RESULT_OK,intent);
                finish();


            }
                break;
            case LOCAL:
                startCropImageActivity(data.getStringExtra("local_path"));
                break;
            case CAMERA:
                // 照相机程序返回的,再次调用图片剪辑程序去修剪图片
                if(data != null) {
                    Uri uri = data.getData();
                    startCropImageActivity(ImageResUtils.getRealFilePath(this,uri));
                }
                break;
        }
    }

    /**
     * 开启 截图进程
     * @param path
     */
    private void startCropImageActivity(String path){
        Intent intent = new Intent(this, ClipImageActivity.class);
        intent.putExtra(ClipImageActivity.PASS_PATH, path);
        intent.putExtra("type",type);
        if(type==1){
            intent.putExtra("stuId",stuId);
        }
        startActivityForResult(intent, CUT);
    }

    /**
     * 通过uri获取文件路径
     *
     * @param mUri
     * @return
     */
    public String getFilePath(Uri mUri) {
        try {
            if (mUri.getScheme().equals("file")) {
                return mUri.getPath();
            } else {
                return getFilePathByUri(mUri);
            }
        } catch (FileNotFoundException ex) {
            return null;
        }
    }

    // 获取文件路径通过url
    private String getFilePathByUri(Uri mUri) throws FileNotFoundException {
        Cursor cursor = getContentResolver() .query(mUri, null, null, null, null);
        cursor.moveToFirst();
        return cursor.getString(1);
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("clipImagePath", clipImagePath);
        outState.putString("image_url",imageUrl);
        outState.putInt("type",type);
        if(type==1){
            outState.putString("stuId",stuId);
        }

        outState.putBoolean("editable",editable);
        outState.putBoolean("collape",collape);

        if(collape){
            outState.putInt("position",currentPosition);
            outState.putSerializable("imagelist", (Serializable) imageList);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

}
