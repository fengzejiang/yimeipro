package com.yimeinew.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v4.print.PrintHelper;
import android.text.TextUtils;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.shade.com.alibaba.fastjson.JSONObject;

import com.yimeinew.activity.R;
import okhttp3.*;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 这个类放置与android无关 或者与UI关系不大的工具类
 */
public class ToolUtils {
    private int sourceVersion=0;
    private int targetVersion=0;

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
    public static boolean isDouble(String str) {
        if (null == str || "".equals(str)) {
            return false;
        }
        Pattern pattern = Pattern.compile("^[-\\+]?[.\\d]*$");
        return pattern.matcher(str).matches();
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
    public static String uploadFile(String url,File file) {

            OkHttpClient client = new OkHttpClient();
            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("file", file.getName(),
                            RequestBody.create(MediaType.parse("multipart/form-data"), file))
                    .build();

            Request request = new Request.Builder()
                    .header("Authorization", "Client-ID " + UUID.randomUUID())
                    .url(url)
                    .post(requestBody)
                    .build();
        try {
            Response response = client.newCall(request).execute();
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

            return response.body().string();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return "";
    }
    public static void openPhoto(Activity activity, String path,int REQ_CODE) {
        File cameraPhoto = new File(path);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        Uri photoUri = FileProvider.getUriForFile(
                activity.getApplicationContext(),
                activity.getPackageName() + ".fileprovider",
                cameraPhoto);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
        activity.startActivityForResult(intent, REQ_CODE);
    }

    /**
     * 缩放图片
     * @param imgpath
     * @param zoompath
     */
    public static void zoomImage(String imgpath,String zoompath) {
        File originFile = new File(imgpath);
        Bitmap bitmap = BitmapFactory.decodeFile(originFile.getAbsolutePath());
        //设置缩放比
        int radio = 2;
        int w=bitmap.getWidth();
        if(w>=1500){radio=3;}else if (w>=2000){radio=4;}
        Bitmap result = Bitmap.createBitmap(bitmap.getWidth() / radio, bitmap.getHeight() / radio, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);
        RectF rectF = new RectF(0, 0, bitmap.getWidth() / radio, bitmap.getHeight() / radio);
        //将原图画在缩放之后的矩形上
        canvas.drawBitmap(bitmap, null, rectF, null);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        result.compress(Bitmap.CompressFormat.JPEG, 100, bos);
        try {
            FileOutputStream fos = new FileOutputStream(new File(zoompath));
            fos.write(bos.toByteArray());
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 打印数据传值
     * @param assist 100标签打印，200标签复制，300标签重置
     *               700批量模板打印，800批量模板复制，900批量模板重置
     *               701批量id【记录】异步打印
     * @param sid  打印模板ID（可空）
     * @param tableName  主表数据库表名
     * @param key  主表数据库主键字段名
     * @param id   主表主键值
     * @param insobj 主表对象定义
     * @param sprn  打印人
     * @param print_id  打印机id-用于选择打印  为空时使用默认打印机
     * @return
     */
    public static HashMap<String, String> printLable(String assist,String sid,String tableName,String key,String id,String insobj,String sprn,String print_id){
        HashMap<String, String> datahm=CommonUtils.commPrintDataMap(assist);
        datahm.put("sid",sid);
        datahm.put("tableName",tableName);
        datahm.put("key",key);
        datahm.put("id", id);
        datahm.put("insobj", insobj);
        datahm.put("sprn", sprn);
        datahm.put("print_id", print_id);
        datahm.put(CommCL.COMM_MSG_ID,getTimeSid()+tableName);//用于区分请求的消息id
        return datahm;
    }
    public static HashMap<String,String> printNum(String msg_id){
        String assist = "702";
        HashMap<String, String> datahm=CommonUtils.commPrintDataMap(assist);
        datahm.put(CommCL.COMM_MSG_ID,msg_id);
        return datahm;
    }
    public static boolean containValue(String[] strings,String str){
        for(String s1:strings){
            if(TextUtils.equals(s1,str)){
                return true;
            }
        }
        return false;
    }
    public static void testPrintimg(Context context){
        PrintHelper ph=new PrintHelper(context);
        ph.setScaleMode(PrintHelper.SCALE_MODE_FIT);
    }
    public static boolean isInList(List<JSONObject> al, String key, String value){
        for(JSONObject obj:al){
            if(TextUtils.equals(obj.getString(key),value)){
                return true;
            }
        }
        return false;
    }
    public synchronized static String getTimeSid(){
        SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMdd_HHmm_ss");
        return sdf.format(new Date(DateUtil.getCurrDateTimeLong()));
    }
    private static int myid=0;
    public static int getMyid(){//全局自动递增id。用于view id赋值
        if(myid>14748364){
            myid=0;
        }
        myid++;
        return myid;
    }

    /**
     * 源版本++
     */
    public void addVersion(){
        sourceVersion++;
    }

    /**
     * 本地升级
     */
    public void updateVersion(){
        targetVersion=sourceVersion;
    }

    /**
     * 判断是否需要升级
     * @return
     */
    public boolean needUpdate(){
        if(sourceVersion>targetVersion){
            return true;
        }else{
            return false;
        }
    }
    public static JSONObject getListJSONObj(ArrayList<JSONObject> list,HashMap<String,String> keys){
        for(JSONObject obj:list){
            boolean b=true;
            for(String key:keys.keySet()){
                if(!TextUtils.equals(keys.get(key),obj.getString(key))){
                    b=false;
                    break;
                }
            }
            if(b){
                return obj;
            }
        }
        return null;
    }



}
