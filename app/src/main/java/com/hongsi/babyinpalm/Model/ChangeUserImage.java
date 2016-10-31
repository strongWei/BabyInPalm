package com.hongsi.babyinpalm.Model;

import com.hongsi.babyinpalm.Exception.NetworkErrorException;
import com.hongsi.babyinpalm.Exception.OtherIOException;
import com.hongsi.babyinpalm.Utils.HttpUtils;
import com.hongsi.babyinpalm.Utils.HttpUtilsWithSession;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

/**
 * Created by Administrator on 2016/10/20.
 */

public class ChangeUserImage {

    public static int modify(File file) throws OtherIOException, NetworkErrorException, JSONException {
        StringBuffer bufferUrl = new StringBuffer();
        bufferUrl.append(HttpUtils.BASE_URL);

        bufferUrl.append("/app/user");

        bufferUrl.append("?type=");
        bufferUrl.append(2);

        String result = HttpUtilsWithSession.postFile(bufferUrl.toString(),file,System.currentTimeMillis()+".png");

        int code = parseJson(result);

        return code;
    }

    public static int parseJson(String result) throws JSONException {
        JSONObject object = new JSONObject(result);

        //boolean r = object.getBoolean("return");
        int code = object.getInt("code");

        //如果code为0，则代表修改图片成功
        if(code == 0){
            Login.user.setUrl(object.getString("url"));
            Login.user.setUrl_scale(object.getString("url_scale"));
        }

        return code;
    }

}
