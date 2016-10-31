package com.hongsi.babyinpalm.Model;

import com.hongsi.babyinpalm.Exception.NetworkErrorException;
import com.hongsi.babyinpalm.Exception.OtherIOException;
import com.hongsi.babyinpalm.Utils.HttpUtils;
import com.hongsi.babyinpalm.Utils.HttpUtilsWithSession;

import org.json.JSONException;

/**
 * Created by Administrator on 2016/10/17.
 */
public class ChangeUserName {

    public static int modify(String userName) throws OtherIOException, NetworkErrorException, JSONException {
        StringBuffer bufferUrl = new StringBuffer();
        bufferUrl.append(HttpUtils.BASE_URL);

        bufferUrl.append("/app/user");

        //params
        StringBuffer buffer = new StringBuffer();
        buffer.append("type=");
        buffer.append(1);
        buffer.append("&name=");
        buffer.append(userName);


        String result = HttpUtilsWithSession.post(bufferUrl.toString(),buffer.toString());

        int code = HttpUtilsWithSession.parseJson(result);

        if(code == 0){
            Login.user.setName(userName);
        }

        return code;
    }

}
