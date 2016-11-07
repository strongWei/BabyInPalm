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
import com.hongsi.babyinpalm.Interface.DialogListener;
import com.hongsi.babyinpalm.Model.GetData;
import com.hongsi.babyinpalm.Model.Login;
import com.hongsi.babyinpalm.R;
import com.hongsi.babyinpalm.Utils.BaseActivity;
import com.hongsi.babyinpalm.Utils.Component.UsualHeaderLayout;
import com.hongsi.babyinpalm.Utils.Component.WaitingDialog;
import com.hongsi.babyinpalm.Utils.Component.WarningDialog;
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
import java.lang.ref.WeakReference;
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

    public List<BaseData> mList = new ArrayList<BaseData>();

    private int type = -1;

    //访问远程数据成功
    private static int SUCCESS = 0;

    private WaitingDialog dialog = null;

    private WarningDialog warningDialog = null;

    private UsualHeaderLayout headerLayout = null;

    private String url;   //要访问远程网络的链接

    private boolean noMoreData = true;     //没有更多数据

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

                case 4:
                    //没有更多数据
                {
                    mBGARefreshLayout.endLoadingMore();
                }

            }
        }
    };

    private RefreshDataAsync refreshDataAsync = null;
    private ShowMoreDataAsync showMoreDataAsync = null;
    private DeleteItemAsync deleteItemAsync = null;

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.back_u:{
                onBackPressed();
                //finish();
            }
            break;

            case R.id.edit_2_u:{
                Intent intent = new Intent(ActivityDataList.this,ActivityAddData.class);
                intent.putExtra("type",type);
                startActivityForResult(intent,ADD);
            }
            break;
        }
    }

    /**  刷新查找最新数据线程 */
    static class RefreshDataAsync extends AsyncTask<Object,Integer,Integer>{
        private WeakReference<ActivityDataList> weakReference;

        public RefreshDataAsync(ActivityDataList activityAddData){
            weakReference = new WeakReference<ActivityDataList>(activityAddData);
        }

        @Override
        protected Integer doInBackground(Object... params) {

            String url = (String) params[0];
            long time = (long) params[1];

            try {
                int code = GetData.getNewData(url,time,weakReference.get().type
                );

                switch (code) {
                    case 0: {
                        return SUCCESS;
                    }
                    case -1: {
                        code = Login.login(Login.user.getPhone(), Login.user.getPassword());
                        switch (code) {
                            case 0: {
                                code = GetData.getNewData(url, time, weakReference.get().type);
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

                ToastUtil.showToast(weakReference.get(), i, Toast.LENGTH_SHORT);

            }else{

                if(weakReference.get()!=null){
                    weakReference.get().noMoreData = true;
                }
            }


            if(GetData.dataList.size() != 0) {
                if(weakReference.get()!=null) {
                    weakReference.get().mList.addAll(0, GetData.dataList);
                    weakReference.get().mRecyclerView.setBackgroundResource(0);

                    weakReference.get().mAdapter.notifyDataSetChanged();
                }
            }

            if(weakReference.get().mList.size() == 0){
                weakReference.get().mRecyclerView.setBackgroundResource(R.mipmap.none);
            }

            weakReference.get().mBGARefreshLayout.endRefreshing();

            weakReference.get().mRecyclerView.scrollToPosition(0);

        }
    }

    /** 显示更多数据线程 */
    static class ShowMoreDataAsync extends AsyncTask<Object,Integer,Integer>{

        private WeakReference<ActivityDataList> weakReference;
        private boolean init;
        private long time;

        public ShowMoreDataAsync(ActivityDataList activityDataList,boolean init) {
            super();
            weakReference = new WeakReference<ActivityDataList>(activityDataList);
            this.init = init;
        }

        @Override
        protected Integer doInBackground(Object... params) {

            String url = (String) params[0];
            time = (long) params[1];

            try {
                int code = GetData.getOldData(url,time,weakReference.get().type,init);
                switch (code){
                    case 0:
                    {
                        return SUCCESS;
                    }
                    case -1:{
                        code = Login.login(Login.user.getPhone(),Login.user.getPassword());
                        switch (code){
                            case 0:{
                                code = GetData.getOldData(url,time,weakReference.get().type,init);
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
                //e.printStackTrace();


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

                if(weakReference.get()!= null)
                    GetData.getDataFromDb(weakReference.get().type,time);

                ToastUtil.showToast(weakReference.get(), i, Toast.LENGTH_SHORT);

            }

            //dialog.dismiss();
            //dialog.stopAnimate();
            if(!init){

                if(GetData.dataList.size() < 10 && GetData.dataList.size() > 0){

                    if(weakReference.get()!=null) {
                        //没有更多数据显示，则向下显示更多不再去服务器请求
                        weakReference.get().noMoreData = false;

                        weakReference.get().mDefineBAGRefreshWithLoadView.updateLoadingMoreText("没有更多数据");
                        weakReference.get().mDefineBAGRefreshWithLoadView.hideLoadingMoreImg();

                        weakReference.get().mList.addAll(GetData.dataList);

                        weakReference.get().mAdapter.notifyDataSetChanged();

                        //weakReference.get().noMoreData = true;

                        weakReference.get().mBGARefreshLayout.endLoadingMore();
                    }

                }else if(GetData.dataList.size() == 0){
                    //没有更多数据显示，则向下显示更多不再去服务器请求
                    weakReference.get().noMoreData = false;

                    //weakReference.get().noMoreData = true;
                    weakReference.get().mDefineBAGRefreshWithLoadView.updateLoadingMoreText("没有更多数据");
                    weakReference.get().mDefineBAGRefreshWithLoadView.hideLoadingMoreImg();
                    weakReference.get().mBGARefreshLayout.endLoadingMore();
                }
                else {

                    if(weakReference.get()!=null) {
                        weakReference.get().mBGARefreshLayout.endLoadingMore();

                        weakReference.get().mList.addAll(GetData.dataList);
                        weakReference.get().mAdapter.notifyDataSetChanged();
                    }
                }

            }else{

                //首次初始化数据完成后会看到pulldown文字
//                if(GetData.dataList.size() == 0){
//                    //当前没有任何数据
//                    //mDefineBAGRefreshWithLoadView.setPullDownRefreshText("没有动态数据");
//                }else{
//                    mDefineBAGRefreshWithLoadView.setPullDownRefreshText("加载完成");
//                }


//                mDefineBAGRefreshWithLoadView.setPullDownRefreshText("下拉刷新");

                if(GetData.dataList.size() == 0){

                    if(weakReference.get()!=null) {

                        //没有更多数据显示，则向下显示更多不再去服务器请求
                        weakReference.get().noMoreData = false;

                        weakReference.get().mRecyclerView.setBackgroundResource(R.mipmap.none);

                        weakReference.get().mDefineBAGRefreshWithLoadView.updateLoadingMoreText("没有更多数据");
                        weakReference.get().mDefineBAGRefreshWithLoadView.hideLoadingMoreImg();
                        //weakReference.get().mAdapter.notifyDataSetChanged();
                    }

                }else if(GetData.dataList.size() < 10){

                    if(weakReference.get()!=null) {
                        //没有更多数据显示，则向下显示更多不再去服务器请求
                        weakReference.get().noMoreData = false;

                        weakReference.get().mRecyclerView.setBackgroundResource(0);

                        weakReference.get().mDefineBAGRefreshWithLoadView.updateLoadingMoreText("没有更多数据");
                        weakReference.get().mDefineBAGRefreshWithLoadView.hideLoadingMoreImg();
                        weakReference.get().mList.addAll(GetData.dataList);
                        weakReference.get().mAdapter.notifyDataSetChanged();
                    }

                }else{
                    if(weakReference.get()!=null) {
                        weakReference.get().mRecyclerView.setBackgroundResource(0);
                        weakReference.get().mList.addAll(GetData.dataList);
                    }
                }

                if(weakReference.get()!=null) {
                    weakReference.get().mDefineBAGRefreshWithLoadView.setInit(false);
                    weakReference.get().mBGARefreshLayout.endRefreshing();
                }
            }

        }
    }

    /*** 删除某一个项 的线程*/
    static class DeleteItemAsync extends AsyncTask<Object,Integer,Integer>{

        private WeakReference<ActivityDataList> weakReference;

        private int position;

        public DeleteItemAsync(ActivityDataList activityDataList,int position){
            weakReference = new WeakReference<ActivityDataList>(activityDataList);
            this.position = position;
        }

        @Override
        protected Integer doInBackground(Object... params) {
            String url = (String) params[0];
            String itemId = (String) params[1];

            try {
                int code = GetData.deleteData(url,itemId,weakReference.get().type);

                switch (code) {
                    case 0: {
                        return SUCCESS;
                    }
                    case -1: {
                        code = Login.login(Login.user.getPhone(), Login.user.getPassword());
                        switch (code) {
                            case 0: {
                                code = GetData.deleteData(url,itemId,weakReference.get().type);
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
            int i = integer.intValue();

            if(i!=SUCCESS) {
                //dialog.dismiss();
                //dialog.stopAnimate();

                ToastUtil.showToast(weakReference.get(), i, Toast.LENGTH_SHORT);

            }else{

                //表明是初始化数据
                weakReference.get().mList.remove(position);
                weakReference.get().mAdapter.notifyDataSetChanged();

                if(weakReference.get().mList.size() == 0){
                    weakReference.get().mRecyclerView.setBackgroundResource(R.mipmap.none);
                }

            }
            weakReference.get().dialog.dismiss();
            weakReference.get().dialog.stopAnimate();

            weakReference.clear();
            weakReference = null;
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
        showMoreDataAsync = new ShowMoreDataAsync(this,true);
        showMoreDataAsync.execute(url,System.currentTimeMillis());
    }

    //刷新时获取最新数据
    private void refreshData(){
        //不需要检查本地缓存，直接获取最新数据
        if(mList.isEmpty()){
            //表明上次初始化时没有获取到任何数据
            showMoreDataAsync = null;
            showMoreDataAsync = new ShowMoreDataAsync(this,true);
            showMoreDataAsync.execute(url,System.currentTimeMillis());
        }else{
            //获取最新的时间
            refreshDataAsync = null;
            refreshDataAsync = new RefreshDataAsync(this);
            refreshDataAsync.execute(url,mList.get(0).getTime());
        }
    }

    //显示更多数据
    private void showMoreData(){
        //需要检查本地缓存，但已经由后台完成
        showMoreDataAsync = null;
        showMoreDataAsync = new ShowMoreDataAsync(this,false);
        showMoreDataAsync.execute(url,mList.get(mList.size()-1).getTime());
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

            case 1:
            {
                //饮食
                StringBuffer buffer = new StringBuffer();
                buffer.append(HttpUtils.BASE_URL);
                buffer.append("/app/eat");
                url = buffer.toString();

            }
            break;

            case 2:
            {
                //课堂计划
                StringBuffer buffer = new StringBuffer();
                buffer.append(HttpUtils.BASE_URL);
                buffer.append("/app/class_list");
                url = buffer.toString();
            }
            break;

            case 3:
            {
                //宝宝动态
                StringBuffer buffer = new StringBuffer();
                buffer.append(HttpUtils.BASE_URL);
                buffer.append("/app/baby_dynamic");
                url = buffer.toString();
            }
            break;

            case 4:
            {
                //留言版
                StringBuffer buffer = new StringBuffer();
                buffer.append(HttpUtils.BASE_URL);
                buffer.append("/app/message_board");
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

        headerLayout.setEdit2Text("添加");

        headerLayout.getBackView().setOnClickListener(this);
        //headerLayout.getEditView().setOnClickListener(this);
        headerLayout.getEdit2View().setOnClickListener(this);

        switch (type){
            case 0: {
                headerLayout.setTitle(R.string.notice);
                if(Login.user.getRole().equals("家长")){
                    //不显示添加按纽
                    headerLayout.getEdit2View().setVisibility(View.GONE);
                }
            }
                break;
            case 1: {
                headerLayout.setTitle(R.string.eat);
                if (Login.user.getRole().equals("家长")) {
                    //不显示添加按纽
                    headerLayout.getEdit2View().setVisibility(View.GONE);
                }
            }
                break;
            case 2: {
                headerLayout.setTitle(R.string.class_record);
                if (Login.user.getRole().equals("家长")) {
                    //不显示添加按纽
                    headerLayout.getEdit2View().setVisibility(View.GONE);
                }
            }
                break;
            case 3:
                headerLayout.setTitle(R.string.baby_dynamic);
                break;

            case 4:
                headerLayout.setTitle(R.string.message_board);
                break;
        }



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

            @Override
            public void deleteItem(final int position) {

                if(warningDialog == null){
                    warningDialog = new WarningDialog(ActivityDataList.this, R.style.DialogStyle, new DialogListener() {
                        @Override
                        public void getResultBoolean(boolean b) {
                            if(b){
                                //删除
                                warningDialog.dismiss();

                                //删除某一个项
                                if(deleteItemAsync != null){
                                    deleteItemAsync.cancel(true);
                                    deleteItemAsync = null;
                                }

                                if(dialog == null){
                                    dialog = new WaitingDialog(ActivityDataList.this,R.style.DialogStyle);
                                }

                                dialog.setText(R.string.waiting);
                                dialog.show();
                                dialog.startAnimate();

                                deleteItemAsync = new DeleteItemAsync(ActivityDataList.this,position);
                                deleteItemAsync.execute(url,mList.get(position).getId());

                            }else{
                                warningDialog.dismiss();
                            }
                        }
                    });
                }

                warningDialog.setWarnText(R.string.delete_or_no);
                warningDialog.show();

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

        if(warningDialog != null && warningDialog.isShowing()){
            warningDialog.dismiss();
            warningDialog = null;
        }

        if(dialog != null && dialog.isShowing()){
            dialog.dismiss();
            dialog.stopAnimate();
            dialog = null;
        }

        //mRecyclerView = null;
        //mList.clear();
        //mList = null;
        //mAdapter = null;
    }

    @Override
    public void onBGARefreshLayoutBeginRefreshing(BGARefreshLayout refreshLayout) {
        if(!mDefineBAGRefreshWithLoadView.isInit())
             handler.sendEmptyMessageDelayed(1,1000);
    }

    @Override
    public boolean onBGARefreshLayoutBeginLoadingMore(BGARefreshLayout refreshLayout) {


        if(!noMoreData){
            //mDefineBAGRefreshWithLoadView.updateLoadingMoreText("没有更多数据");
           // mDefineBAGRefreshWithLoadView.hideLoadingMoreImg();
            //mBGARefreshLayout.endLoadingMore();
            handler.sendEmptyMessageDelayed(4,1000);

            return true;
        }

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
