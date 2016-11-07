package com.hongsi.babyinpalm.dll.showImage;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.hongsi.babyinpalm.Controller.activity.ActivityAddData;
import com.hongsi.babyinpalm.R;
import com.hongsi.babyinpalm.Utils.BaseActivity;
import com.hongsi.babyinpalm.Utils.Component.CustomApplication;
import com.hongsi.babyinpalm.Utils.ImageResUtils;
import com.hongsi.babyinpalm.dll.showImage.Interface.OnItemClickListener;

import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.List;

/**
 * Created by Administrator on 2016/10/27.
 */

public class ActivityImageList extends BaseActivity implements View.OnClickListener {

    private final  static String TAG = "ActivityImageList";

    /** 删除按纽*/
    private ImageView deleteBtn = null;

    /** dot recycler 部件*/
    private RecyclerView dotRecycleView = null;

    /** recycler 部件 */
    private RecyclerView recyclerView = null;

    /** 图片适配器 */
    private ImageAdapter mAdapter = null;

    /** 图片列表 */
    private List<ImageData> mImageList;

    /** 文字显示*/
    private TextView textView = null;

    /** 当前显示的图片编号 */
    private int position;

    /** 圆点适配器*/
    private DotAdapter dotAdapter = null;

    private RecyclerView.LayoutManager layoutManager = null;

    private float cursorLastX = 0;
    private float cursorFirstX = 0;

    private boolean delete = true;

    private int cursor_size;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_list_layout);

        if(savedInstanceState != null){

        }else{
            Intent intent = getIntent();
            position = intent.getIntExtra("position",0);
            delete = intent.getBooleanExtra("delete",true);
            mImageList = (List<ImageData>) intent.getSerializableExtra("imageList");
        }

        //WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        cursor_size = 25;

        initView();
    }

    /**
     * 初始化界面
     */
    private void initView() {
        textView = (TextView) findViewById(R.id.textView);

        deleteBtn = (ImageView) findViewById(R.id.delete);
        if(!delete){
            deleteBtn.setVisibility(View.INVISIBLE);
        }else {
            deleteBtn.setOnClickListener(this);
        }

        recyclerView = (RecyclerView) findViewById(R.id.imageList_recycler);

        recyclerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(v.getId() == R.id.delete){
                    return false;
                }

                switch (event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        cursorFirstX = event.getX();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        cursorLastX = event.getX();
                        break;
                    case MotionEvent.ACTION_UP:
                        if(cursorFirstX - cursorLastX < 0 ){
                            //向左转
                            if(Math.abs(cursorFirstX - cursorLastX) > cursor_size){
                                recyclerView.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_CANCEL, 0, 0, 0));
                                if(position - 1 >= 0){
                                    //recyclerView.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_CANCEL, 0, 0, 0));
                                    layoutManager.scrollToPosition(--position);
                                    int tempPosition = position + 1;
                                    textView.setText(tempPosition + "/" + mImageList.size());
                                }else{

                                }
                            }else{

                                layoutManager.scrollToPosition(position);
                            }
                        }else if(cursorFirstX - cursorLastX > 0 ){

                            if(Math.abs(cursorFirstX - cursorLastX) > cursor_size){
                                recyclerView.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_CANCEL, 0, 0, 0));
                                //向右转
                                if(position + 1 < mImageList.size()) {
                                    layoutManager.scrollToPosition(++position);
                                    int tempPosition = position + 1;
                                    textView.setText(tempPosition + "/" + mImageList.size());
                                }
                            }else{
                                layoutManager.scrollToPosition(position);
                            }

                        }
                        break;
                }

                return false;
            }
        });


        //设置适配器
        mAdapter = new ImageAdapter(this,mImageList);

        recyclerView.setAdapter(mAdapter);

        layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL,false);

        //设置方向
        recyclerView.setLayoutManager(layoutManager);

        //设置动画
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        //跳到第二项
        layoutManager.scrollToPosition(position);

        int tempPosition = position + 1;
        textView.setText(tempPosition + "/" + mImageList.size());

        /*
        dotRecycleView = (RecyclerView) findViewById(R.id.dot_imagelist);
        //设置方向
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL,false));;
        dotAdapter = new DotAdapter(this,mImageList);
        dotRecycleView.setAdapter(dotAdapter);
        dotRecycleView.setItemAnimator(new DefaultItemAnimator());

        layoutManager.scrollToPosition(position);
        */
    }

    /**
     * 将结果发送回去
     */
    private void startActivityAndSendData() {
        Intent intent = new Intent(ActivityImageList.this, ActivityAddData.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("finalImageList", (Serializable) mImageList);
        intent.putExtras(bundle);
        setResult(RESULT_OK,intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        recyclerView = null;

        mAdapter = null;

        mImageList.clear();
        mImageList = null;
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.delete) {
            //获取当前的图片并删除
            mImageList.remove(position);
            mAdapter.notifyDataSetChanged();

            //
            if (position == mImageList.size()) {
                if (mImageList.isEmpty()) {
                    //是空的，直接关闭
                    startActivityAndSendData();
                    finish();
                } else {
                    //表明已经到达结尾了
                    position = position - 1;
                }

            }
            int tempPosition = position + 1;
            textView.setText(tempPosition + "/" + mImageList.size());
        }
    }

    /**
     * 圆点适配器
     */
    class DotAdapter extends RecyclerView.Adapter<ImageViewHolder>{

        private Context mContext;

        private LayoutInflater mLayoutInflater;

        private List<ImageData> urlList;

        public DotAdapter(Context context,List<ImageData> list){
            this.mContext = context;
            this.urlList = list;
            mLayoutInflater = LayoutInflater.from(mContext);
        }

        @Override
        public ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = mLayoutInflater.inflate(R.layout.dot_list_item,parent,false);
            ImageViewHolder viewHolder = new ImageViewHolder(view);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(ImageViewHolder holder, int position) {
            if(urlList == null){
                return;
            }

            if(ActivityImageList.this.position == position) {
                holder.imageView.setBackgroundResource(R.color.blue);
            }else{
                holder.imageView.setBackgroundResource(R.color.red);
            }
        }

        @Override
        public int getItemCount() {
            if(urlList == null){
                return 0;
            }

            return urlList.size();
        }
    }

    /**
     * 图片适配器
     */
    class ImageAdapter extends RecyclerView.Adapter<ImageViewHolder>{

        private Context mContext;

        private LayoutInflater mLayoutInflater;

        private List<ImageData> urlList;

        private OnItemClickListener mOnItemClickListener;

        public void setOnItemClickListener(OnItemClickListener listener){
            this.mOnItemClickListener = listener ;
        }


        public ImageAdapter(Context context,List<ImageData> list){
            this.mContext = context;
            this.urlList = list;
            mLayoutInflater = LayoutInflater.from(mContext);
        }

        @Override
        public ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = mLayoutInflater.inflate(R.layout.list_image_item,parent,false);
            ImageViewHolder viewHolder = new ImageViewHolder(view);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(ImageViewHolder holder, int position) {
            if(urlList == null){
                return;
            }


            String url = urlList.get(position).getUrl();
            String url_scale = urlList.get(position).getUrl_scale();

            if(!url.isEmpty()) {
                /*
                WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
                int width = wm.getDefaultDisplay().getWidth();
                holder.imageView.setMaxWidth(width);
                holder.imageView.setMinimumWidth(width);
                */

                if (url.startsWith("http://") || url.startsWith("https://")) {
                    //说明是网络的数据
                    holder.imageView.refreshDrawableState();
                    holder.imageView.setTag(url);

                    CustomApplication.getImageLoader().loadNetworkImage(holder.imageView, url);

                    holder.scaleImageView.refreshDrawableState();
                    holder.scaleImageView.setTag(url_scale);

                    CustomApplication.getImageLoader().loadNetworkImage(holder.scaleImageView, url_scale);
                } else {
                    //说明是本地的数据
                    try {
                        holder.imageView.refreshDrawableState();
                        holder.imageView.setImageBitmap(ImageResUtils.rotateBitmapByDegree(ImageResUtils.getImageByUrl(url,640,480),
                                ImageResUtils.getBitmapDegree(url)));
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        @Override
        public int getItemCount() {
            if(urlList == null){
                return 0;
            }

            return urlList.size();
        }


    }

    @Override
    public void onBackPressed() {
        startActivityAndSendData();
        finish();
    }
}
