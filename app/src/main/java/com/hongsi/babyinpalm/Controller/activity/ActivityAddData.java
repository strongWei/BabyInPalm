package com.hongsi.babyinpalm.Controller.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.hongsi.babyinpalm.Exception.NetworkErrorException;
import com.hongsi.babyinpalm.Exception.OtherIOException;
import com.hongsi.babyinpalm.Interface.PopUpListener;
import com.hongsi.babyinpalm.Model.AddData;
import com.hongsi.babyinpalm.Model.Login;
import com.hongsi.babyinpalm.R;
import com.hongsi.babyinpalm.Utils.BaseActivity;
import com.hongsi.babyinpalm.Utils.Component.BottomPopView;
import com.hongsi.babyinpalm.Utils.Component.CustomApplication;
import com.hongsi.babyinpalm.Utils.Component.UsualHeaderLayout;
import com.hongsi.babyinpalm.Utils.Component.WaitingDialog;
import com.hongsi.babyinpalm.Utils.HttpUtils;
import com.hongsi.babyinpalm.Utils.ImageResUtils;
import com.hongsi.babyinpalm.Utils.LogUtil;
import com.hongsi.babyinpalm.Utils.ToastUtil;
import com.hongsi.babyinpalm.dll.EditTextSelector.EditTextCountWithoutEmoji;
import com.hongsi.babyinpalm.dll.SelectImage.imageloader.MultiSelectImageActivity;
import com.hongsi.babyinpalm.dll.recyclerLayout.ImageGLAdapter;
import com.hongsi.babyinpalm.dll.showImage.ActivityImageList;
import com.hongsi.babyinpalm.dll.showImage.ImageData;

import org.json.JSONException;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by Administrator on 2016/10/27.
 */

public class ActivityAddData extends BaseActivity implements View.OnClickListener {
    private static final String TAG = "ActivityAddData";
    private LinearLayout globalLayout = null;
    private GridView gridView = null;
    private final int IMAGE_MAX_SIZE = 9;

    private final int LOCAL = 1;
    private final int CAMERA = 2;
    private final int REFRESH = 3;      //关闭后进行刷新

    private ImageGLAdapter mAdapter = null;

    private List<ImageData> mImageList = new ArrayList<>();

    /** 头部件 */
    private UsualHeaderLayout headerLayout = null;

    /** 输入控件*/
    private EditTextCountWithoutEmoji editText = null;

    /** 当前发送的数据类型*/
    private int type;

    /** 等待窗口 */
    private WaitingDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_base_item);

        if(savedInstanceState == null){
            type = getIntent().getIntExtra("type",-1);
        }

        //初始化界面
        initView();
    }

    private void initView() {
        editText = (EditTextCountWithoutEmoji) findViewById(R.id.edittext);
        editText.setTEXT_MAX_SIZE(200);
        headerLayout = (UsualHeaderLayout) findViewById(R.id.header);
        //设置监听
        headerLayout.getBackView().setOnClickListener(this);
        headerLayout.getEdit2View().setOnClickListener(this);
        switch (type){
            case 0:
                headerLayout.setTitle(R.string.add_notice);
                break;
        }

        headerLayout.setEdit2Text(R.string.complete);

        gridView = (GridView) findViewById(R.id.imageGridView);

        //设置图片选择
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ImageGLAdapter adapter = (ImageGLAdapter) parent.getAdapter();
                //ImageGLAdapter.ViewHolder viewHolder = (ImageGLAdapter.ViewHolder) adapter.getView(position,view,parent).getTag();

                if(position == adapter.getCount() -1){
                    //这是最后一个图标,可以进行选择
                    if(mAdapter.getCount() == 10){
                        ToastUtil.showToast(ActivityAddData.this,"最多只能上传九张图片!", Toast.LENGTH_SHORT);
                        return;
                    }

                    //提示弹出选项
                    final BottomPopView bottomPopView = new BottomPopView(ActivityAddData.this, globalLayout) {
                        @Override
                        public void onTopButtonClick() {
                            //拍照
                            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            //intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(ActivityAddData.getTempCameraFile()));
                            startActivityForResult(intent, CAMERA);
                            dismiss();
                        }

                        @Override
                        public void onBottomButtonClick() {
                            //选择图片
                            Intent intent = new Intent(ActivityAddData.this, MultiSelectImageActivity.class);
                            intent.putExtra("selectNum", IMAGE_MAX_SIZE + 1 - mImageList.size());
                            startActivityForResult(intent,LOCAL);
                            dismiss();
                        }
                    };
                    bottomPopView.setTopText("拍照");
                    bottomPopView.setBottomText("选择图片");
                    // 显示底部菜单
                    bottomPopView.show();
                }else{
                    //用于显示图片
                    List<ImageData> imageLDatas = new ArrayList<ImageData>();
                    imageLDatas.addAll(mImageList);
                    imageLDatas.remove(imageLDatas.size() -1);

                    Intent intent = new Intent(ActivityAddData.this, ActivityImageList.class);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("imageList", (Serializable) imageLDatas);
                    bundle.putInt("position",position);

                    intent.putExtras(bundle);

                    startActivityForResult(intent,REFRESH);
                }
            }

        });


        //全局部件
        globalLayout = (LinearLayout) findViewById(R.id.global_layout);


        ImageData imageData = new ImageData();
        imageData.setResId(R.mipmap.add);
        mImageList.add(imageData);


        mAdapter = new ImageGLAdapter(this, mImageList, new PopUpListener() {
            @Override
            public void popUp() {
                //do nothing
            }
        });
        gridView.setAdapter(mAdapter);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(dialog != null && dialog.isShowing()){
            dialog.dismiss();
            dialog.stopAnimate();
            dialog = null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(resultCode == RESULT_OK){
            if(requestCode == LOCAL){
                //选取本地的图片
                List<String> imageUrlList = (List<String>) data.getSerializableExtra("selectImageList");
                if(imageUrlList !=null && imageUrlList.size() > 0){

                    List<ImageData> tempImageList = new ArrayList<>();
                    for(String url : imageUrlList){
                        ImageData imageLData = new ImageData();

                        imageLData.setUrl(url);

                        tempImageList.add(imageLData);
                    }

                    mAdapter.addItem(mAdapter.getCount() -1,tempImageList);

                    mAdapter.notifyDataSetChanged();
                }
            }else if(requestCode == CAMERA){
                Uri uri = data.getData();          //直接拿取图片路径

                String url = ImageResUtils.getRealFilePath(this,uri);

                LogUtil.e(TAG,url);

                ImageData imageLData = new ImageData();
                //直接获取bitmap

                imageLData.setUrl(url);

                mAdapter.addItem(mAdapter.getCount()-1,imageLData);

                mAdapter.notifyDataSetChanged();

            }else if(requestCode == REFRESH){
                //返回最终的图片集
                List<ImageData> finalImageLDatas = (List<ImageData>) data.getSerializableExtra("finalImageList");

                //旧的imagelist 清空掉除最后一个的项，再重新添加
                if(finalImageLDatas.size() == mImageList.size() - 1){
                    //数据没有发生变化
                }else{
                    //删除旧的imageData,再重新添加新的
                    mImageList.clear();
                    ImageData imageData = new ImageData();
                    imageData.setResId(R.mipmap.add);
                    mImageList.add(imageData);

                    mImageList.addAll(0,finalImageLDatas);
                    mAdapter.notifyDataSetChanged();
                }
            }
        }

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.back_u:
                onBackPressed();
                finish();

                break;

            case R.id.edit_2_u:
                sumitFormData();
                break;
        }
    }

    /**
     * 添加数据
     */
    private void sumitFormData() {
        //获取文字信息
        String text = editText.getText().trim();
        if(text.isEmpty() && mImageList.size() == 1){
            //空字符串和空图片
            ToastUtil.showToast(this,"没有添加任何数据！",Toast.LENGTH_SHORT);
            return;
        }

        if(dialog == null){
            dialog = new WaitingDialog(this,R.style.DialogStyle);
        }

        dialog.setText(R.string.uploading);
        dialog.show();
        dialog.startAnimate();

        StringBuffer urlB = new StringBuffer();
        urlB.append(HttpUtils.BASE_URL);

        //TODO:
        type = 0;

        if(type == 0){
            urlB.append("/app/notice?type=1");
        }
        new AddDataAsync().execute(urlB.toString(),text);
    }

    /**
     * 添加数据线程
     *
     */
    class AddDataAsync extends AsyncTask<String,Integer,Integer>{

        private List<File> files = new ArrayList<>();

        public AddDataAsync() {
            super();
        }

        @Override
        protected Integer doInBackground(String... params) {
            scaleImageAndSave();


            //获取网络链接
            String url = params[0];
            String context = params[1];
            try {
                int code = AddData.add(context,url,type,files);

                switch(code){
                    case 0:
                        return R.string.upload_success;
                    case -1:{
                        //TODO:
                        code = Login.login("15814877246","12345678");
                        if(code == -1){
                            //用户名或密码错误
                            return R.string.login_error;
                        }else if(code == -2){
                            //服务器异常
                            return R.string.server_error;
                        }

                        //进行修改
                        code =  AddData.add(context,url,type,files);
                        switch (code){
                            case 0:
                                return R.string.upload_success;
                            case -1:
                                return R.string.account_exception;
                            case -2:
                                return R.string.server_error;
                            case -3:{
                                //TODO:
                            }
                            case -4:{
                                //TODO:
                                return R.string.fileName_empty;
                            }
                            case -5:{
                                //TODO:
                            }
                            case -6:{
                                return R.string.saveImage_fail;
                            }

                        }
                    }
                        break;
                    case -2:{
                        return R.string.server_error;
                    }
                    case -3:{
                        //TODO:
                    }
                    case -4:{
                        //TODO:
                        return R.string.fileName_empty;
                    }
                    case -5:{
                        //TODO:
                    }
                    case -6:{
                        return R.string.saveImage_fail;
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
        protected void onPreExecute() {
            super.onPreExecute();

        }

        private void scaleImageAndSave() {
            //进行图片的缩放和保存
            if(mImageList.size() == 1){
                //没有添加任何图片，不需要进行图片处理
                return;
            }

            String tempDir = getTempDir();

            for(int i = 0;i<mImageList.size() - 1;++i){
                ImageData imageData =  mImageList.get(i);
                StringBuffer buffer = new StringBuffer();

                //获取临时保存目录文件夹
                buffer.append(tempDir);
                buffer.append("/");
                buffer.append("scale_image_");
                buffer.append(i);
                buffer.append(".jpg");

                File file = ImageResUtils.scaleBitmapAuto(imageData.getUrl(),buffer.toString(),640,480,ImageResUtils.getBitmapDegree(imageData.getUrl()));
                files.add(file);
            }
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);

            dialog.dismiss();
            dialog.stopAnimate();

            int i = integer.intValue();

            ToastUtil.showToast(ActivityAddData.this,i, Toast.LENGTH_SHORT);

            //如果出现重新登陆后依然提示重新登陆或者是用户名或密码出现异常，则转到登陆界面
            if(i==R.string.account_exception || i==R.string.login_error){
                Intent intent = new Intent(ActivityAddData.this,ActivityLogin.class);
                startActivity(intent);
            }

            if(i==R.string.upload_success){
                //
                Intent intent = new Intent(ActivityAddData.this,ActivityDataList.class);
                intent.putExtra("refresh",true);
                setResult(RESULT_OK,intent);
                finish();
            }
        }
    }

    /**
     * 获取临时目录
     * @return
     */
    private String getTempDir() {
        File file = null;
        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) || !Environment.isExternalStorageRemovable()){
            file = new File(CustomApplication.getContext().getExternalCacheDir(),"image_temp");
        }else{
            file = new File(CustomApplication.getContext().getCacheDir(),"image_temp");
        }

        if(!file.exists()){
            file.mkdirs();
        }

        return file.getAbsolutePath();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(ActivityAddData.this,ActivityDataList.class);
        setResult(RESULT_OK,intent);
        finish();
    }
}
