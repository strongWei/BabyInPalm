package com.hongsi.babyinpalm.Controller.fragment;


import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.hongsi.babyinpalm.Controller.activity.ActivityConnect;
import com.hongsi.babyinpalm.Controller.activity.ActivityDataList;
import com.hongsi.babyinpalm.R;


/**
 * Created by Administrator on 2016/6/16.
 */
public class TalkPageFragment extends Fragment implements View.OnClickListener {

    private LinearLayout teaConnectBtn;
    private LinearLayout parConnectBtn;
    private LinearLayout msgBoardBtn;
    private View layoutView = null;

    public TalkPageFragment() {
        super();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        layoutView = inflater.inflate(R.layout.talk_page_layout,container,false);
        teaConnectBtn = (LinearLayout) layoutView.findViewById(R.id.tea_connect);
        parConnectBtn = (LinearLayout) layoutView.findViewById(R.id.par_connect);
        msgBoardBtn = (LinearLayout) layoutView.findViewById(R.id.message_bord);

        teaConnectBtn.setOnClickListener(this);
        parConnectBtn.setOnClickListener(this);
        msgBoardBtn.setOnClickListener(this);

        return layoutView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tea_connect:
            {
                Intent intent = new Intent(getActivity(), ActivityConnect.class);
                intent.putExtra("type",0);
                startActivity(intent);
            }
                break;

            case R.id.par_connect: {
                Intent intent = new Intent(getActivity(), ActivityConnect.class);
                intent.putExtra("type", 1);
                startActivity(intent);
            }
                break;

            case R.id.message_bord:
            {
                Intent intent = new Intent(getActivity(), ActivityDataList.class);
                intent.putExtra("type",4);
                startActivity(intent);
            }
                break;
        }
    }
}
