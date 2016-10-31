package com.hongsi.babyinpalm.dll.recyclerLayout;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.hongsi.babyinpalm.R;
import com.hongsi.babyinpalm.Utils.Component.CustomApplication;
import com.hongsi.babyinpalm.dll.showImage.ImageData;
import com.hongsi.babyinpalm.dll.showImage.Interface.TransImageDataListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by Administrator on 2016/10/22.
 */
public class BaseItemAdapter extends RecyclerView.Adapter<BaseViewHolder> {

    /** 数据列表*/
    private List<BaseData> dataList;

    private Context mContext;

    private LayoutInflater mLayoutInflater;

    private TransImageDataListener transImageDataListener;

    /** 构造函数*/
    public BaseItemAdapter(Context context,List<BaseData> datas,TransImageDataListener listener){
        mContext = context;
        dataList = datas;
        mLayoutInflater = LayoutInflater.from(mContext);
        transImageDataListener = listener;
    }

    /**
     * 初始化ViewHolder
     * @param parent
     * @param viewType
     * @return
     */
    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mLayoutInflater.inflate(R.layout.base_item,parent,false);

        BaseViewHolder baseViewHolder = new BaseViewHolder(view);

        return baseViewHolder;
    }

    /**
     * 数据绑定
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(final BaseViewHolder holder, int position) {
        //获取数据
        BaseData baseData = dataList.get(position);

        //发送人信息
        holder.userIdView.setText(baseData.getUser().getId());
        holder.userNameView.setText(baseData.getUser().getName());


        String url_scale = baseData.getUser().getUrl_scale();
        if(url_scale.isEmpty()){
            //不进行图片的获取
            holder.userImageView.setImageResource(R.mipmap.app_icon);
        }else{
            //从图片缓存中获取
            holder.userImageView.setTag(url_scale);
            CustomApplication.getImageLoader().loadNetworkImage(holder.userImageView,url_scale);
        }

        //数据信息
        if(baseData.getContent()!= null && !baseData.getContent().isEmpty()){
            holder.contentView.setText(baseData.getContent());
            holder.contentView.setVisibility(View.VISIBLE);
        }else{
            holder.contentView.setVisibility(View.GONE);
        }

        holder.timeView.setText(transToDateFromTimeUtc(baseData.getTime()));
        holder.baseIdView.setText(baseData.getId());

        //TODO: GridView
        if(baseData.getImageList() == null) {
           holder.imageGridView.setVisibility(View.GONE);
        }else{
            holder.imageGridView.setVisibility(View.VISIBLE);

            ImageGAdapter adapter = new ImageGAdapter(mContext, baseData.getImageList());
            holder.imageGridView.setAdapter(adapter);
        }

        holder.imageGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ImageGAdapter adapter = (ImageGAdapter) parent.getAdapter();

                if(adapter.getCount()>0){
                    List<ImageData> dataList = new ArrayList<>();
                    //获取大图
                    List<com.hongsi.babyinpalm.Domain.ImageData> datas = adapter.mDataList;

                    if(datas != null){
                        for(com.hongsi.babyinpalm.Domain.ImageData data : datas){
                            String url = data.getUrl();
                            String url_scale = data.getUrl_scale();
                            ImageData imageData = new ImageData();
                            imageData.setUrl_scale(url_scale);
                            imageData.setUrl(url);
                            dataList.add(imageData);
                        }
                    }

                    transImageDataListener.setImageDataToActivityImageList(dataList,position);
                }
            }
        });

    }

    /**
     * 从时间戮中转换成一个字符串
     * @param time
     * @return
     */
    private String transToDateFromTimeUtc(long time) {
        Calendar calendar = Calendar.getInstance();

        int year =calendar.get(Calendar.YEAR);

        calendar.setTimeInMillis(time);
        int oldYear = calendar.get(Calendar.YEAR);

        String result = new SimpleDateFormat("MM-dd HH:mm").format(calendar.getTime());

        if(year != oldYear){
            StringBuffer buffer = new StringBuffer();
            buffer.append(oldYear);
            buffer.append("-");
            buffer.append(result);
            return buffer.toString();
        }

        return result;
    }

    @Override
    public int getItemCount() {
        if(dataList != null){
            return dataList.size();
        }

        return 0;
    }
}
