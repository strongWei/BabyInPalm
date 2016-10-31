package com.hongsi.babyinpalm.Model;

import com.hongsi.babyinpalm.Exception.NetworkErrorException;
import com.hongsi.babyinpalm.Exception.OtherIOException;
import com.hongsi.babyinpalm.Utils.HttpUtilsWithSession;

import org.json.JSONException;

import java.io.File;
import java.util.List;

/**
 * Created by Administrator on 2016/10/28.
 */

public class AddData {

    public static int add(String content,String url,int type,List<File> files) throws OtherIOException, NetworkErrorException, JSONException {
        String result = "";
        result = HttpUtilsWithSession.postFiles(url,content, files);

        int code = HttpUtilsWithSession.parseJson(result);

        return code;
    }
}
