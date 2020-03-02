package com.yimeinew.utils;

import android.text.TextUtils;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.shade.com.alibaba.fastjson.JSON;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.shade.com.alibaba.fastjson.JSONObject;
import com.yimeinew.entity.PrintModel;
import okhttp3.*;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 这个类放置与android无关 或者与UI关系不大的工具类
 */
public class ToolUtils {
    /**判断是否是Integer类型*/
    public static boolean isInteger(String str){
        if(str!=null&&!"".equals(str.trim())){
            Pattern pattern = Pattern.compile("[0-9]*");
            Matcher isNum = pattern.matcher(str);
            Long number = 0l;
            if(isNum.matches()){
                number=Long.parseLong(str);
            }else{
                return false;
            }
            if(number>2147483647){
                return false;
            }
        }else{
            return false;
        }
        return true;
    }

    /**
     * MES系统定义的常量格式转换
     * @param cons
     * @return
     */
    public static HashMap<String, String> parseConstant(String cons){
        if(TextUtils.isEmpty(cons)){
            return new HashMap<String, String>();
        }
        //去掉{}
        if(cons.contains("{")){
            cons=cons.substring(cons.indexOf("{")+1);
        }
        if(cons.contains("}")){
            cons=cons.substring(0,cons.indexOf("}"));
        }
        System.out.println(cons);
        HashMap<String, String> hm = new HashMap<String, String>();
        String[] strs = cons.split(";");
        for(String str:strs){
            String[] temp = str.split(":");
            if(temp.length>=2) {
                hm.put(temp[0], temp[1]);
            }
        }
        return hm;
    }
    /**OKHttp get请求返回 可以是一个文件 等等*/
    public static String getUrl(String url){
        Request requestOk = new Request.Builder()
                .url(url)
                .get()
                .build();
        Response response;
        try {
            response = new OkHttpClient().newCall(requestOk).execute();
            String jsonString = response.body().string();
            if(response.isSuccessful()){
                return jsonString;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
        return "";
    }
    /**OKHttp get请求返回JSON数据*/
    public JSONObject getJson(String url){
        String str=getUrl(url);
        if(!TextUtils.isEmpty(str)) {
           return JSONObject.parseObject(str);
        }
        return new JSONObject();
    }
    /**OKHttp post请求返回 可以是一个文件 等等*/
    public static String postUrl(String url,HashMap<String,String> data){
        FormBody.Builder fb = new FormBody.Builder();
        for(String key:data.keySet()){
            fb.add(key,data.get(key));
        }
        RequestBody body = fb.build();
        Request requestOk = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        Response response;
        try {
            response = new OkHttpClient().newCall(requestOk).execute();
            String jsonString = response.body().string();
            if(response.isSuccessful()){
                return jsonString;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
        return "";
    }
    public static String printLable(String path,String fileName,String[] data,int num){
        HashMap<String, String> datahm=new HashMap<>();
        datahm.put("assist","print");
        datahm.put("fullName",fileName);
        datahm.put("num",""+num);
        datahm.put("path",path);
        datahm.put("attr", JSON.toJSONString(data));
        return ToolUtils.postUrl("http://192.168.6.14:8091/api?attr=sid",datahm);
    }
    public static PrintModel printPack(String lotno,String insobj){
        String url="http://192.168.6.14:8090/api?assist=100&insobj="+insobj+"&tableName=mes_pklist_prt&key=lotno&id="+lotno;
        String str=getUrl(url);
        return JSON.parseObject(str, PrintModel.class);
    }
    public static String printLable(String path,PrintModel printModel){
        HashMap<String, String> moban = printModel.getMoban();
        String[] data=new String[moban.size()];
        int i=0;
        for(String key:moban.keySet()){
            data[i]=key+":"+moban.get(key);
            i++;
        }
        return printLable(path,printModel.getFileName(),data,printModel.getNum());
    }
}
