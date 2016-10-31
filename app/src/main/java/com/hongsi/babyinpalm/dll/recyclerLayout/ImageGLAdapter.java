package com.hongsi.babyinpalm.dll.recyclerLayout;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.hongsi.babyinpalm.Interface.PopUpListener;
import com.hongsi.babyinpalm.R;
import com.hongsi.babyinpalm.Utils.ImageResUtils;
import com.hongsi.babyinpalm.dll.showImage.ImageData;

import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.List;

/**
 * Created by Administrator on 2016/10/23.
 * 加载本地图片
 */
public class ImageGLAdapter extends BaseAdapter{

    private static final String TAG = "ImageGLAdapter";

    private Context mContext;

    private LayoutInflater mLayooutInflater;

    public List<ImageData> mDataList;

    private PopUpListener mListener;

    public ImageGLAdapter(Context context, List<ImageData> dataList, PopUpListener listener) {
        mContext = context;
        mDataList = dataList;
        mLayooutInflater = LayoutInflater.from(mContext);
        mListener = listener;
    }

    @Override
    public int getCount() {
        if(mDataList != null){
            return mDataList.size();
        }
        return 0;
    }

    public class ViewHolder{
        public ImageView imageView;
        public ImageView mask;
    }

    public void addItem(int position,ImageData data){
        mDataList.add(position,data);
    }

    public void addItem(ImageData data){
        mDataList.add(data);
    }

    public void addItem(int position, Collection<ImageData> datas){
        mDataList.addAll(position,datas);
    }

    @Override
    public Object getItem(int position) {
        return mDataList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        ViewHolder viewHolder = null;

        if(convertView == null){
            convertView = mLayooutInflater.inflate(R.layout.base_image_item,parent,false);

            viewHolder = new ViewHolder();

            viewHolder.imageView = (ImageView) convertView.findViewById(R.id.grid_image);
            viewHolder.mask = (ImageView) convertView.findViewById(R.id.mask);

            /*
            viewHolder.imageView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {

                    LogUtil.i(TAG,"touch"+position);
                    LogUtil.i(TAG,mDataList.toString());

                    if(position == getCount() - 1){
                        //如果是最后一张图片,无特效
                        return false;
                    }

                    if(event.getAction() == event.ACTION_DOWN || event.getAction() == event.ACTION_MOVE){
                       mMask.setVisibility(View.VISIBLE);
                    }else if(event.getAction() == event.ACTION_UP || event.getAction() == event.ACTION_CANCEL){
                       mMask.setVisibility(View.GONE);
                    }

                    //Log.e("action",event.getAction() + "");
                    return false;
                }
            });
            viewHolder.imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LogUtil.i(TAG,"click"+position);
                    LogUtil.i(TAG,mDataList.toString());

                    if(position == getCount() - 1){
                        //如果是最后一张图片，进行图片的选项
                        mListener.popUp();
                        return;
                    }


                    Intent intent  = new Intent(mContext, ActivityPersonImage.class);
                    intent.putExtra("image_url",mDataList.get(position).getUrl());
                    intent.putExtra("no_change",true);
                    intent.putExtra("position",position-1);
                    Bundle bundle = new Bundle();
                    List<ImageData> imageDataList = new ArrayList<>();
                    for(int i=0;i<mDataList.size()-1;++i){
                        ImageData imageData = new ImageData();
                        imageData.setUrl(mDataList.get(i).getUrl());
                        imageDataList.add(imageData);
                    }
                    bundle.putSerializable("imagelist", (Serializable) imageDataList);
                    intent.putExtras(bundle);
                    mContext.startActivity(intent);

                }
            });
    */

            convertView.setTag(viewHolder);

        }else{

            viewHolder = (ViewHolder) convertView.getTag();

        }

        //LogUtil.i(TAG,"create"+position);

        //进行图片的加载
        int resId = mDataList.get(position).getResId();
        String url = mDataList.get(position).getUrl();


        //设置图片
        if(resId != 0){
            //本地图片
            viewHolder.imageView.setImageBitmap(ImageResUtils.getImageById(mContext,resId));
            viewHolder.mask.setVisibility(View.GONE);
        }else {

            try {
                viewHolder.imageView.setImageBitmap(ImageResUtils.rotateBitmapByDegree(ImageResUtils.getImageByUrl(url),
                        ImageResUtils.getBitmapDegree(url)));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

        }

        return convertView;
    }


}
