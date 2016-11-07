package com.hongsi.babyinpalm.Controller.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.hongsi.babyinpalm.Domain.ConnectData;
import com.hongsi.babyinpalm.Exception.NetworkErrorException;
import com.hongsi.babyinpalm.Exception.OtherIOException;
import com.hongsi.babyinpalm.Model.GetConnect;
import com.hongsi.babyinpalm.Model.Login;
import com.hongsi.babyinpalm.R;
import com.hongsi.babyinpalm.Utils.BaseActivity;
import com.hongsi.babyinpalm.Utils.Component.CustomApplication;
import com.hongsi.babyinpalm.Utils.Component.UsualHeaderLayout;
import com.hongsi.babyinpalm.Utils.Component.WaitingDialog;
import com.hongsi.babyinpalm.Utils.HttpUtils;
import com.hongsi.babyinpalm.Utils.ToastUtil;
import com.hongsi.babyinpalm.dll.recyclerLayout.DividerItemDecoration;
import com.hongsi.babyinpalm.dll.showImage.Interface.OnItemClickListener;

import org.json.JSONException;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/11/1.
 */

public class ActivityConnect extends BaseActivity implements View.OnClickListener {

    private RecyclerView recyclerView;

    private RecyclerView.LayoutManager manager ;

    private ConnectAdapter mAdapter;

    private List<ConnectData> mList = new ArrayList<>();

    private WaitingDialog waitDialog;

    private GetConnectsAsync getConnectAsync;

    private UsualHeaderLayout header;

    private int type;
    private String url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_connect_layout);

        if(savedInstanceState!= null){
            type = savedInstanceState.getInt("type");
            url = savedInstanceState.getString("url");

        }else {
            type = getIntent().getIntExtra("type", -1);
            getUrl();
        }


        initView();

        if(waitDialog == null){
            waitDialog = new WaitingDialog(this,R.style.DialogStyle);
        }
        waitDialog.setText(R.string.getting);
        waitDialog.show();
        waitDialog.startAnimate();

        initData();
    }

    private void getUrl() {
        switch (type){
            case 0:
            {
                //教师联系方式
                StringBuffer buf = new StringBuffer();
                buf.append(HttpUtils.BASE_URL);
                buf.append("/app/connect?type=0");
                url = buf.toString();
            }
                break;
            case 1:
            {
                //家长联系方式
                StringBuffer buf = new StringBuffer();
                buf.append(HttpUtils.BASE_URL);
                buf.append("/app/connect?type=1");
                url = buf.toString();
            }
                break;
        }
    }

    private void initData() {
        if(getConnectAsync!=null){
            getConnectAsync.cancel(true);
            getConnectAsync = null;
        }
        getConnectAsync =  new GetConnectsAsync(this);
        getConnectAsync.execute();
    }

    private void initView() {
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);

        header = (UsualHeaderLayout) findViewById(R.id.header);
        if(type == 0){
            header.setTitle(R.string.tea_connect);
        }else if(type==1){
            header.setTitle(R.string.par_connect);
        }

        header.getEdit2View().setVisibility(View.GONE);
        header.getBackView().setOnClickListener(this);

        manager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(manager);

        recyclerView.setItemAnimator(new DefaultItemAnimator());

        mAdapter = new ConnectAdapter(this,mList);
        recyclerView.setAdapter(mAdapter);

        mAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                String phone =  mList.get(position).getPhone();

                if(v.getId() == R.id.call){
                    Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:"+phone));
                    startActivity(intent);

                }else{
                    Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:"+phone));
                    startActivity(intent);
                }
            }

            @Override
            public void onItemLongClick(View v, int position) {
                //do nothing
            }
        });

        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(waitDialog!=null && waitDialog.isShowing()){
            waitDialog.dismiss();
            waitDialog.stopAnimate();
            waitDialog = null;
        }

        if(getConnectAsync!=null){
            getConnectAsync.cancel(true);
            getConnectAsync = null;
        }

        recyclerView = null;
        manager = null;
        mList.clear();
        mList = null;
        mAdapter = null;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(outState!=null){
            outState.putInt("type",type);
            outState.putString("url",url);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.back_u:
                onBackPressed();
                break;
        }
    }

    /** 适配器 */
    class ConnectAdapter extends RecyclerView.Adapter<ConnectViewHolder>{

        private Context mContext;
        private LayoutInflater mInflater;
        private List<ConnectData> connectList;

        public void setOnItemClickListener(OnItemClickListener mListener) {
            this.mListener = mListener;
        }

        private OnItemClickListener mListener;

        public ConnectAdapter(Context context,List<ConnectData> data){
            mContext = context;
            mInflater =  mInflater.from(mContext);
            connectList = data;
        }

        @Override
        public ConnectViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = mInflater.inflate(R.layout.phone_item_layout,parent,false);

            ConnectViewHolder holder = new ConnectViewHolder(view);


            return holder;
        }

        @Override
        public void onBindViewHolder(final ConnectViewHolder holder, int position) {
            ConnectData connectData = connectList.get(position);

            holder.idView.setText(connectData.getId());

            StringBuffer bufferName = new StringBuffer();
            if(!connectData.getName().isEmpty()) {
                bufferName.append(connectData.getName());
            }else{
                bufferName.append(connectData.getPhone());
            }
            bufferName.append("（");
            bufferName.append(connectData.getRole());
            bufferName.append("）");
            holder.nameView.setText(bufferName.toString());
            bufferName = null;


            if(connectData.getUrl_scale().isEmpty()){

            }else{
                holder.personImage.setTag(connectData.getUrl_scale());
                CustomApplication.getImageLoader().loadNetworkImage(holder.personImage,connectData.getUrl_scale());
            }

            if(connectData.getDetail() == null || (connectData.getDetail()!=null && connectData.getDetail().isEmpty())) {
                holder.detailView.setVisibility(View.GONE);
            }else{
                holder.detailView.setVisibility(View.VISIBLE);
                holder.detailView.setText(connectData.getDetail());
            }

            holder.callBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int currentPosition = holder.getPosition();
                    mListener.onItemClick(v,currentPosition);
                }
            });

            holder.smsBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int currentPosition = holder.getPosition();
                    mListener.onItemClick(v,currentPosition);
                }
            });

        }

        @Override
        public int getItemCount() {
            if(connectList == null)
                 return 0;

            return connectList.size();
        }
    }

    /** 绑定器 */
    class ConnectViewHolder extends RecyclerView.ViewHolder{

        private TextView idView;
        private ImageView personImage;
        private TextView nameView;
        private TextView detailView;
        private ImageView callBtn;
        private ImageView smsBtn;

        public ConnectViewHolder(View itemView) {
            super(itemView);
            idView = (TextView) itemView.findViewById(R.id.id);
            personImage = (ImageView) itemView.findViewById(R.id.person_image);
            nameView = (TextView) itemView.findViewById(R.id.name);
            detailView = (TextView) itemView.findViewById(R.id.detail);
            callBtn = (ImageView) itemView.findViewById(R.id.call);
            smsBtn = (ImageView) itemView.findViewById(R.id.send_message);
        }
    }

    /** 获取数据线程 */
    class GetConnectsAsync extends AsyncTask<Void,Integer,Integer>{

        private WeakReference<ActivityConnect> weakRef;

        public GetConnectsAsync(ActivityConnect activity){
            weakRef = new WeakReference<ActivityConnect>(activity);
        }

        @Override
        protected Integer doInBackground(Void... params) {
            try {
                int code = GetConnect.getData(url,type);

                if(code == 0){
                    return 0;

                }else if(code == -1){
                    code = Login.login(Login.user.getPhone(), Login.user.getPassword());
                    if(code == 0 || code == 1){
                        code = GetConnect.getData(url,type);
                        if(code == 0){
                            return 0;
                        }else if(code == -1){
                            return R.string.account_exception;
                        }else if(code == -2){
                            return R.string.server_error;
                        }
                    }else if(code == -1){
                        return R.string.login_error;
                    }else if(code == -2){
                        return R.string.server_error;
                    }

                }else if(code == -2){
                    return R.string.server_error;
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

            waitDialog.dismiss();
            waitDialog.stopAnimate();

            int i = integer.intValue();
            if(i!=0){
                ToastUtil.showToast(weakRef.get(),i,Toast.LENGTH_SHORT);
            }else if(i==R.string.login_error || i == R.string.account_exception){
                Intent intent = new Intent(weakRef.get(), ActivityLogin.class);
                startActivity(intent);
                finish();
            }

            mList.clear();
            mList.addAll(GetConnect.datas);
            mAdapter.notifyDataSetChanged();


            weakRef.clear();
            weakRef = null;
        }
    }
}
