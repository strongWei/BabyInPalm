package com.hongsi.babyinpalm.Controller.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.hongsi.babyinpalm.R;
import com.hongsi.babyinpalm.Utils.BaseActivity;
import com.hongsi.babyinpalm.Utils.Component.UsualHeaderLayout;
import com.hongsi.babyinpalm.dll.showRecord.OneDayRecordsData;
import com.hongsi.babyinpalm.dll.showRecord.RecordAdapter;
import com.hongsi.babyinpalm.dll.showRecord.RecordData;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Administrator on 2016/10/29.
 */

public class ActivityShowRecord extends BaseActivity {

    /**recycle view 部件*/
    private RecyclerView recyclerView;

    /** 管理器 */
    private RecyclerView.LayoutManager layoutManager;

    /** 适配器 */
    private RecordsAdapter mAdapter;

    /** 记录组列表*/
    private List<OneDayRecordsData> recordsDataList = new ArrayList<>();

    /** 头 */
    private UsualHeaderLayout headerLayout = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_listview);

        initView();

        //TODO:
        initData();
    }

    private void initData() {
        for(int i=1;i<10;i++) {
            OneDayRecordsData oneDayRecordData = new OneDayRecordsData();
            int day = new Random().nextInt(31);
            oneDayRecordData.setDay(day);
            int month = new Random().nextInt(12);
            oneDayRecordData.setMonth(month);

            List<RecordData> recordData = new ArrayList<RecordData>();
            for(int j=0;j<new Random().nextInt(10) +1;j++){
                RecordData record = new RecordData();
                record.setUrl("");
                record.setUrl_scale("");
                record.setId("");
                record.setName("孙小雨（校车接送）");
                record.setTime((j < 10 ? "0" + j : j )+":"+ (i < 10 ? "0" +i : i));
                record.setWay("家长接送");
                record.setType("入园");

                recordData.add(record);
            }

            oneDayRecordData.setRecordDataList(recordData);

            recordsDataList.add(oneDayRecordData);
        }


        mAdapter.notifyDataSetChanged();
    }

    private void initView() {
        headerLayout = (UsualHeaderLayout) findViewById(R.id.header);
        headerLayout.getEdit2View().setVisibility(View.INVISIBLE);


        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);

        //设置recycleView的管理器
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        //设置动画
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        //设置适配器
        mAdapter = new RecordsAdapter(this,recordsDataList);
        recyclerView.setAdapter(mAdapter);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        recyclerView = null;
        layoutManager = null;

    }

    /** 记录组的适配器*/
    class RecordsAdapter extends RecyclerView.Adapter<RecordsViewHolder>{

        private Context mContext;
        private LayoutInflater mInfalter;

        private List<OneDayRecordsData> dataList;

        public RecordsAdapter(Context context, List<OneDayRecordsData> dataList){
            mContext = context;
            mInfalter = LayoutInflater.from(mContext);
            this.dataList = dataList;
        }

        @Override
        public RecordsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = mInfalter.inflate(R.layout.records_item_one_day,parent,false);

            RecordsViewHolder viewHolder = new RecordsViewHolder(view);

            return viewHolder;
        }

        @Override
        public void onBindViewHolder(RecordsViewHolder holder, int position) {
            OneDayRecordsData oneDayRecordsData = dataList.get(position);
            int day = oneDayRecordsData.getDay();
            int month = oneDayRecordsData.getMonth();
            List<RecordData> recordDatas = oneDayRecordsData.getRecordDataList();


            holder.dayTextView.setText(day < 10 ? "0" + day : ""+day);
            holder.monthTextView.setText((month < 10 ? "0" + month : ""+month) + "月");
            holder.oneDayListView.setAdapter(new RecordAdapter(mContext,recordDatas));

        }

        @Override
        public int getItemCount() {
            if(dataList == null){
                return 0;
            }

            return dataList.size();
        }
    }

    /** 记录组适配器的绑定器 */
    class RecordsViewHolder extends RecyclerView.ViewHolder{

        public TextView dayTextView;       //日文本
        public TextView monthTextView;     //月文本
        public ListView oneDayListView;    //同一天的记录数组绑定器

        public RecordsViewHolder(View itemView) {
            super(itemView);
            dayTextView = (TextView) itemView.findViewById(R.id.day);
            monthTextView = (TextView) itemView.findViewById(R.id.month);
            oneDayListView = (ListView) itemView.findViewById(R.id.one_day_record_list);
        }
    }
}
