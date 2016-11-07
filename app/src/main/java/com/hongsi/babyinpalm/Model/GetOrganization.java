package com.hongsi.babyinpalm.Model;

import com.hongsi.babyinpalm.Domain.Organization;
import com.hongsi.babyinpalm.Exception.NetworkErrorException;
import com.hongsi.babyinpalm.Exception.OtherIOException;
import com.hongsi.babyinpalm.Utils.HttpUtils;
import com.hongsi.babyinpalm.Utils.HttpUtilsWithSession;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/11/4.
 */

public class GetOrganization {
    public static List<Organization> organizationList = new ArrayList<>();

    public static int getGrade() throws OtherIOException, NetworkErrorException, JSONException {
        StringBuffer urlBuffer = new StringBuffer(HttpUtils.BASE_URL);
        urlBuffer.append("/app/grades?type=0");

        organizationList.clear();

        String result = HttpUtilsWithSession.get(urlBuffer.toString());

        int code = parseJson(result);

        return code;
    }

    public static int parseJson(String result) throws JSONException {
        JSONObject resObj = new JSONObject(result);

        int code = resObj.getInt("code");

        if(code == 0){
            String dataStr = resObj.getString("data");
            if(!dataStr.equals("[]")){
                JSONArray gradeArray = resObj.getJSONArray("data");
                for(int i=0;i<gradeArray.length();++i){
                    JSONObject gradeObj = gradeArray.getJSONObject(i);
                    Organization organization = new Organization();
                    organization.setId(gradeObj.getString("id"));
                    organization.setName(gradeObj.getString("name"));
                    //默认设置为true
                    organization.setSelected(true);
                    organizationList.add(organization);
                }
            }
        }

        return code;
    }
}
