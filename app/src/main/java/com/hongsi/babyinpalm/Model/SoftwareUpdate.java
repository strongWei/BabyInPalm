package com.hongsi.babyinpalm.Model;


import com.hongsi.babyinpalm.Exception.NetworkErrorException;
import com.hongsi.babyinpalm.Exception.OtherIOException;
import com.hongsi.babyinpalm.Utils.Component.CustomApplication;
import com.hongsi.babyinpalm.Utils.HttpUtils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Administrator on 2016/9/26.
 */
public class SoftwareUpdate {

    public static String softwareUrl="";

    public static boolean softwareUpdate() throws NetworkErrorException, JSONException, OtherIOException {

        StringBuffer urlBuffer = new StringBuffer();
        urlBuffer.append(HttpUtils.BASE_URL);
        urlBuffer.append("/app/update?type=1");
        urlBuffer.append("&version=");
        urlBuffer.append(CustomApplication.getVersionName());

        String result = HttpUtils.get(urlBuffer.toString());


        //解析json
        JSONObject objJson = new JSONObject(result);

        //boolean ret = objJson.getBoolean("return");
        int code = objJson.getInt("code");

        if(code == 0){
            //需要进行更新软件
            softwareUrl = objJson.getString("url");
            return true;
        }else{
            return false;
        }

    }

}
