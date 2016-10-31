package com.hongsi.babyinpalm.Controller.activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import com.hongsi.babyinpalm.Exception.NetworkErrorException;
import com.hongsi.babyinpalm.Exception.OtherIOException;
import com.hongsi.babyinpalm.Model.GetData;
import com.hongsi.babyinpalm.Model.Login;
import com.hongsi.babyinpalm.R;
import com.hongsi.babyinpalm.Utils.BaseActivity;
import com.hongsi.babyinpalm.Utils.Component.UsualHeaderLayout;
import com.hongsi.babyinpalm.Utils.Component.WaitingDialog;
import com.hongsi.babyinpalm.Utils.HttpUtils;
import com.hongsi.babyinpalm.Utils.ToastUtil;
import com.hongsi.babyinpalm.dll.recyclerLayout.BaseData;
import com.hongsi.babyinpalm.dll.recyclerLayout.BaseItemAdapter;
import com.hongsi.babyinpalm.dll.recyclerLayout.DefineBAGRefreshWithLoadView;
import com.hongsi.babyinpalm.dll.recyclerLayout.DividerItemDecoration;
import com.hongsi.babyinpalm.dll.showImage.ActivityImageList;
import com.hongsi.babyinpalm.dll.showImage.ImageData;
import com.hongsi.babyinpalm.dll.showImage.Interface.TransImageDataListener;

import org.json.JSONException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import cn.bingoogolapple.refreshlayout.BGARefreshLayout;

/**
 * Created by Administrator on 2016/10/26.
 */

public class ActivityDataList extends BaseActivity implements BGARefreshLayout.BGARefreshLayoutDelegate, View.OnClickListener {

    private static int ADD = 7;

    //添加 refresh layout
    private BGARefreshLayout mBGARefreshLayout;

    //已经实现的加载框架
    private DefineBAGRefreshWithLoadView mDefineBAGRefreshWithLoadView = null;

    private RecyclerView mRecyclerView = null;

    private BaseItemAdapter mAdapter = null;

    private List<BaseData> mList = new ArrayList<BaseData>();

    private int type = -1;

    //访问远程数据成功
    private static int SUCCESS = 0;

    private WaitingDialog dialog = null;

    private UsualHeaderLayout headerLayout = null;

    private String url;   //要访问远程网络的链接

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what){
                case 0:
                    //开始初始化更多数据
                {
                    initData();
                }
                break;

                case 1:
                    //开始刷新获取新数据
                {
                    refreshData();
                }
                break;

                case 2:
                    //开始加载更多数据
                {
                    showMoreData();
                }
                break;
            }
        }
    };

    private RefreshDataAsync refreshDataAsync = null;
    private ShowMoreDataAsync showMoreDataAsync = null;

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.back_u:{
                onBackPressed();
                finish();
            }
            break;

            case R.id.edit_2_u:{
                Intent intent = new Intent(ActivityDataList.this,ActivityAddData.class);
                startActivityForResult(intent,ADD);
            }
            break;
        }
    }

    /**  刷新查找最新数据线程 */
    class RefreshDataAsync extends AsyncTask<String,Integer,Integer>{

        private long time;

        public RefreshDataAsync(long time){
            this.time = time;
        }

        @Override
        protected Integer doInBackground(String... params) {

            String url = params[0];

            try {
                int code = GetData.getNewData(url,time,type);

                switch (code) {
                    case 0: {
                        return SUCCESS;
                    }
                    case -1: {
                        code = Login.login(Login.user.getPhone(), Login.user.getPassword());
                        switch (code) {
                            case 0: {
                                code = GetData.getNewData(url, time, type);
                                switch (code) {
                                    case 0:
                                        return SUCCESS;
                                    case -1:
                                        return R.string.account_exception;
                                    case -2:
                                        return R.string.server_error;
                                }
                            }
                            case -1: {
                                return R.string.login_error;
                            }

                            case -2: {
                                return R.string.server_error;
                            }
                        }
                    }
                    case -2: {
                        return R.string.server_error;
                    }
                    case -3: {
                        return R.string.time_no_set;
                    }
                }
            } catch (OtherIOException e) {
                return R.string.other_error;
            } catch (NetworkErrorException e){
                return R.string.net_error;
            } catch (JSONException e) {
                e.printStackTrace();
                return R.string.data_error;
            }

            return 0;
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);

            int i = integer.intValue();


            if(i!=SUCCESS) {
                //dialog.dismiss();
                //dialog.stopAnimate();

                ToastUtil.showToast(ActivityDataList.this, i, Toast.LENGTH_SHORT);

            }else{

                if(GetData.dataList.size() == 0){
//                    mDefineBAGRefreshWithLoadView.setPullDownRefreshText("没有动态数据");

                }else{
                    mList.addAll(0,GetData.dataList);
                    mAdapter.notifyDataSetChanged();
                }

            }

            mBGARefreshLayout.endRefreshing();

        }
    }

    /** 显示更多数据线程 */
    class ShowMoreDataAsync extends AsyncTask<String,Integer,Integer>{

        private long time;
        private boolean init;

        public ShowMoreDataAsync(long time, boolean mInit) {
            super();
            this.time = time;
            this.init = mInit;
        }

        @Override
        protected Integer doInBackground(String... params) {

            String url = params[0];
            try {
                int code = GetData.getOldData(url,time,type);
                switch (code){
                    case 0:
                    {
                        return SUCCESS;
                    }
                    case -1:{
                        code = Login.login(Login.user.getPhone(),Login.user.getPassword());
                        switch (code){
                            case 0:{
                                code = GetData.getOldData(url,time,type);
                                switch (code){
                                    case 0:
                                        return SUCCESS;
                                    case -1:
                                        return R.string.account_exception;
                                    case -2:
                                        return R.string.server_error;
                                }
                            }
                            case -1:{
                                return R.string.login_error;
                            }

                            case -2:{
                                return R.string.server_error;
                            }
                        }
                    }
                    case -2:{
                        return R.string.server_error;
                    }
                    case -3:{
                        return R.string.time_no_set;
                    }
                }

            } catch (OtherIOException e) {
                return R.string.other_error;
            } catch (NetworkErrorException e){
                return R.string.net_error;
            } catch (JSONException e) {
                e.printStackTrace();
                return R.string.data_error;
            }

            return 0;
        }

        @Override
        protected void onPostExecute(Integer integer) {
            int i = integer.intValue();

            if(i!=SUCCESS) {
                //dialog.dismiss();
                //dialog.stopAnimate();

                ToastUtil.showToast(ActivityDataList.this, i, Toast.LENGTH_SHORT);

            }else{

                //表明是初始化数据
                mList.addAll(GetData.dataList);
                mAdapter.notifyDataSetChanged();

            }

            //dialog.dismiss();
            //dialog.stopAnimate();
            if(!init){
                if(GetData.dataList.size() < 10){
                    mBGARefreshLayout.endLoadingMore();
                    mDefineBAGRefreshWithLoadView.updateLoadingMoreText("没有更多数据");
                    mDefineBAGRefreshWithLoadView.hideLoadingMoreImg();

                }else{
                    mBGARefreshLayout.endLoadingMore();
                }

            }else{

                //首次初始化数据完成后会看到pulldown文字
//                if(GetData.dataList.size() == 0){
//                    //当前没有任何数据
//                    //mDefineBAGRefreshWithLoadView.setPullDownRefreshText("没有动态数据");
//                }else{
//                    mDefineBAGRefreshWithLoadView.setPullDownRefreshText("加载完成");
//                }

                mDefineBAGRefreshWithLoadView.setInit(false);
                mDefineBAGRefreshWithLoadView.setPullDownRefreshText("下拉刷新");
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_listview);

        if(savedInstanceState != null){
            type = savedInstanceState.getInt("type");
            url = savedInstanceState.getString("url");
        }else{
            type = getIntent().getIntExtra("type",0);
            getUrl(type);
        }

        initView();

        setBgaRefreshLayout();


        //数据初始化
        mDefineBAGRefreshWithLoadView.setInit(true);
        mBGARefreshLayout.beginRefreshing();

        handler.sendEmptyMessageDelayed(0,2000);
    }

    //第一次进入时初始化数据
    private void initData() {
        showMoreDataAsync = null;
        showMoreDataAsync = new ShowMoreDataAsync(System.currentTimeMillis(),true);
        showMoreDataAsync.execute(url);
    }

    //刷新时获取最新数据
    private void refreshData(){
        //不需要检查本地缓存，直接获取最新数据
        if(mList.isEmpty()){
            //表明上次初始化时没有获取到任何数据
            refreshDataAsync = null;
            refreshDataAsync = new RefreshDataAsync(System.currentTimeMillis());
            refreshDataAsync.execute(url);
        }else{
            //获取最新的时间
            refreshDataAsync = null;
            refreshDataAsync = new RefreshDataAsync(mList.get(0).getTime());
            refreshDataAsync.execute(url);
        }
    }

    //显示更多数据
    private void showMoreData(){
        //需要检查本地缓存，但已经由后台完成
        new ShowMoreDataAsync(mList.get(mList.size()-1).getTime(),false).execute(url);
    }

    //动态生成网络链接
    private void getUrl(int type){
        switch (type){
            case 0:
                //公告栏
            {
                StringBuffer buffer = new StringBuffer();
                buffer.append(HttpUtils.BASE_URL);
                buffer.append("/app/notice");
                url = buffer.toString();
            }
            break;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(outState != null){
            outState.putInt("type",type);
            outState.putString("url",url);
        }
    }

    /**
     * 自定义刷新设置
     */
    private void setBgaRefreshLayout() {
        mDefineBAGRefreshWithLoadView = new DefineBAGRefreshWithLoadView(this , true , true);
        //设置刷新样式
        mBGARefreshLayout.setRefreshViewHolder(mDefineBAGRefreshWithLoadView);
        mDefineBAGRefreshWithLoadView.updateLoadingMoreText("显示更多");
        mDefineBAGRefreshWithLoadView.setRefreshingText("玩命加载中");
        mDefineBAGRefreshWithLoadView.setPullDownRefreshText("下拉刷新");
        mDefineBAGRefreshWithLoadView.setReleaseRefreshText("释放刷新");
    }

    /**
     * 初始化界面
     */
    private void initView() {

        headerLayout = (UsualHeaderLayout) findViewById(R.id.header);
        switch (type){
            case 0:
                headerLayout.setEdit2Text(R.string.notice);
                break;
        }

        headerLayout.setEdit2Text("添加");

        headerLayout.getBackView().setOnClickListener(this);
        //headerLayout.getEditView().setOnClickListener(this);
        headerLayout.getEdit2View().setOnClickListener(this);

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        //设置适配器
        mAdapter = new BaseItemAdapter(this, mList, new TransImageDataListener() {
            @Override
            public void setImageDataToActivityImageList(List<ImageData> datas, int position) {

                Intent intent = new Intent(ActivityDataList.this, ActivityImageList.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("imageList", (Serializable) datas);
                bundle.putBoolean("delete",false);
                bundle.putInt("position",position);

                intent.putExtras(bundle);

                startActivity(intent);
            }
        });
        mRecyclerView.setAdapter(mAdapter);

        //设置
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST));

        //设置refresh layout
        mBGARefreshLayout  = (BGARefreshLayout) findViewById(R.id.refresh_layout);
        //加载监听
        mBGARefreshLayout.setDelegate(this);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(showMoreDataAsync != null && !showMoreDataAsync.isCancelled()){
            showMoreDataAsync.cancel(true);
        }

        if(refreshDataAsync != null && !refreshDataAsync.isCancelled()){
            refreshDataAsync.cancel(true);
        }

        mRecyclerView = null;
        mList.clear();
        mList = null;
        mAdapter = null;
    }

    @Override
    public void onBGARefreshLayoutBeginRefreshing(BGARefreshLayout refreshLayout) {
        handler.sendEmptyMessageDelayed(1,1000);
    }

    @Override
    public boolean onBGARefreshLayoutBeginLoadingMore(BGARefreshLayout refreshLayout) {
        handler.sendEmptyMessageDelayed(2,1000);
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_OK){
            if(requestCode == ADD){
                boolean refresh = data.getBooleanExtra("refresh",false)  ;
                if(refresh){
                    mBGARefreshLayout.beginRefreshing();
                }else{
                    //do nothing
                }
            }

        }else{
            return;
        }
    }
}
