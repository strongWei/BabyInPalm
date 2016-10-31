package com.hongsi.babyinpalm.Model;

import com.hongsi.babyinpalm.Exception.NetworkErrorException;
import com.hongsi.babyinpalm.Exception.OtherIOException;
import com.hongsi.babyinpalm.Utils.HttpUtils;
import com.hongsi.babyinpalm.Utils.HttpUtilsWithSession;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Administrator on 2016/10/12 0012.
 */

public class Logout {
    public static int logout() throws OtherIOException, NetworkErrorException, JSONException {
        StringBuffer bufferUrl = new StringBuffer();
        bufferUrl.append(HttpUtils.BASE_URL);

        bufferUrl.append("/app/login");

        //params
        StringBuffer buffer = new StringBuffer();
        buffer.append("type=");
        buffer.append(2);

        String result = HttpUtilsWithSession.post(bufferUrl.toString(),buffer.toString());

        return HttpUtilsWithSession.parseJson(result);
    }

}
