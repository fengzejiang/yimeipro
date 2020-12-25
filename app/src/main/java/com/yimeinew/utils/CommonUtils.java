package com.yimeinew.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.*;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.PlaybackParams;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.speech.tts.TextToSpeech;
import android.text.Html;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.shade.com.alibaba.fastjson.JSONArray;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.shade.com.alibaba.fastjson.JSONObject;
import com.sdsmdg.tastytoast.TastyToast;
import com.yimeinew.activity.base.BaseActivity;
import com.yimeinew.activity.base.BaseApplication;
import com.yimeinew.data.ZCInfo;
import com.yimeinew.data.util.RepeatValue;
import com.yimeinew.listener.OnAlertListener;
import com.yimeinew.listener.OnConfirmListener;
import com.yimeinew.listener.OnSendMessage;
import com.yimeinew.network.NetWorkManager;
import com.yimeinew.network.response.Response;
import com.yimeinew.tableui.AbstractTableViewAdapter;
import com.yimeinew.tableui.entity.HeaderRowInfo;
import com.zyao89.view.zloading.ZLoadingDialog;
import com.zyao89.view.zloading.Z_TYPE;
import io.reactivex.Observable;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @Auther: fengzejiang1987@163.com
 * @Date : 2018/12/7 14:05
 */
public class CommonUtils {

    public static int T_Time = Toast.LENGTH_SHORT;
    public static HashMap<String, RepeatValue> repeat=new HashMap<String,RepeatValue>();//用于防止重复扫描，缓存过滤法
    public static TextToSpeech textSpeech;
    public static String Str2Base64(String pwd) {
        pwd = TextUtils.isEmpty(pwd)?"":pwd;
        return Base64.encodeToString(pwd.getBytes(), Base64.DEFAULT);
    }

    public static ZLoadingDialog initLoadingView(Context context, String hintText, Z_TYPE z_type) {
        ZLoadingDialog zLoadingView = new ZLoadingDialog(context);
        zLoadingView.setLoadingBuilder(z_type)
                .setLoadingColor(Color.WHITE)//颜色
                .setHintText(hintText)
//                .setHintTextSize(16) // 设置字体大小 dp
//                .setHintTextColor(Color.WHITE)  // 设置字体颜色
                .setDurationTime(0.5) // 设置动画时间百分比 - 0.5倍
                .setCancelable(false)
                .setCanceledOnTouchOutside(false)
                .setDialogBackgroundColor(Color.parseColor("#CC111111"));// 设置背景色，默认白色
        return zLoadingView;
    }


    public static ZLoadingDialog loginLoadingView(Context context) {
        return initLoadingView(context,"登陆中",Z_TYPE.DOUBLE_CIRCLE);
    }

    /***
     * 组件获取焦点,组件自身不能申明nextFocusDown或者只能是自己！！
     * imeOptions="actionNone"
     * @param edt_next  目标输入框
     */
    public static void textViewGetFocus(EditText edt_next) {
        edt_next.setFocusable(true);
        edt_next.requestFocus();
        edt_next.setFocusableInTouchMode(true);
        edt_next.requestFocusFromTouch();
        edt_next.findFocus();
//        edt_next.selectAll();
        edt_next.setText("");
    }
    public static void textViewGetFocusNotClear(EditText edt_next) {
        edt_next.setFocusable(true);
        edt_next.requestFocus();
        edt_next.setFocusableInTouchMode(true);
        edt_next.requestFocusFromTouch();
        edt_next.findFocus();
    }

    /***
     * 组件获取焦点,组件自身不能申明nextFocusDown或者只能是自己！！
     * imeOptions="actionNone"
     * @param edt_next  目标输入框
     */
    public static void textViewDorpFocus(EditText edt_next) {
        if(edt_next.hasFocus()) {
            edt_next.setFocusable(true);
            edt_next.clearFocus();
            //edt_next.setFocusableInTouchMode(true);
            //edt_next.re;
            //edt_next.findFocus();
        }
    }

    public static HashMap<String, String> getBaseReqHashMap() {
        HashMap<String, String> params = new HashMap<>();
        params.put(CommCL.PARAMS_DBID, CommCL.DB_ID);
        params.put(CommCL.PARAMS_USRCODE, BaseApplication.currUser.getUserCode());
        return params;
    }

    public static HashMap<String, String> getAsistsReqHashMap() {
        HashMap<String, String> params = getBaseReqHashMap();
        params.put(CommCL.PARAMS_APIID, CommCL.PARAM_VALUE_API_ID_ASSIST);
        return params;
    }

    /***
     * 获取审核提交的map
     * @return
     */
    public static HashMap<String, String> getCheckInfoReqHashMap() {
        HashMap<String, String> params = getBaseReqHashMap();
        params.put(CommCL.PARAMS_APIID, CommCL.PARAM_VALUE_API_ID_CHECK_UP);
        return params;
    }

    /***
     * 生成保存数据格式的请求参数
     * @param values json格式化的数据
     * @param cellId 平台中的对象的ID
     * @return
     */
    public static HashMap<String, String> saveDataMap(String values, String cellId) {
        HashMap<String, String> httpMapKeyValue = getBaseReqHashMap();
        httpMapKeyValue.put(CommCL.PARAMS_APIID, CommCL.PARAM_VALUE_API_ID_SAVE);
        httpMapKeyValue.put(CommCL.PARAMS_JSON_STR, values);
        httpMapKeyValue.put(CommCL.PARAMS_P_CELL, cellId);
        httpMapKeyValue.put(CommCL.PARAMS_DATA_TYPE, CommCL.PARAM_VALUE_API_ID_SAVE_DATA_TYPE_JSON);
        return httpMapKeyValue;
    }
    /***
     * 生成更新数据格式的请求参数
     * @param values json格式化的数据
     * @param cellId 平台中的对象的ID
     * @return
     */
    public static HashMap<String, String> updateDataMap(String values, String cellId) {
        HashMap<String, String> httpMapKeyValue = getBaseReqHashMap();
        httpMapKeyValue.put(CommCL.PARAMS_APIID, CommCL.PARAM_VALUE_API_ID_SAVE);
        httpMapKeyValue.put(CommCL.PARAMS_JSON_STR, values);
        httpMapKeyValue.put(CommCL.PARAMS_P_CELL, cellId);
        httpMapKeyValue.put(CommCL.PARAMS_DATA_TYPE, CommCL.PARAM_VALUE_API_ID_SAVE_DATA_TYPE_JSON);
        System.out.println("woshi="+httpMapKeyValue.toString());
        return httpMapKeyValue;
    }
    /***
     * 生产批次更改状态Map
     * @param values
     * @param udpId
     * @return
     */
    public static HashMap<String, String> commMesUDPDataMap(String values, String udpId) {
        HashMap<String, String> httpMapKeyValue = getBaseReqHashMap();
        httpMapKeyValue.put(CommCL.PARAMS_APIID, CommCL.PARAM_VALUE_API_ID_MES_UDP);
        httpMapKeyValue.put(CommCL.PARAMS_JSON_DATA, values);
        httpMapKeyValue.put(CommCL.PARAMS_MES_UPD_ID_FLD, udpId);

        return httpMapKeyValue;
    }
    /***
     * 生产批次更改状态Map快速过站，一条一条记录提交
     * @param values
     * @param udpId
     * @return
     */
    public static HashMap<String, String> commMesUDPDataMap2(HashMap<String, String>  values, String udpId) {
        HashMap<String, String> httpMapKeyValue = getBaseReqHashMap();
        httpMapKeyValue.put(CommCL.PARAMS_APIID, CommCL.PARAM_VALUE_API_ID_MES_UDP);
        httpMapKeyValue.put(CommCL.PARAMS_MES_UPD_ID_FLD, udpId);
        httpMapKeyValue.putAll(values);
        return httpMapKeyValue;
    }
    /***
     * 生产加胶或者添加锡膏的apiMap
     * @param sbId 设备号
     * @param prtNo 锡膏编号或者是胶杯号
     * @param udpId 300,410
     * @return
     */
    public static HashMap<String, String> commMesUDPDataGluingMap(final String sbId,final String prtNo, final String udpId) {
        HashMap<String, String> httpMapKeyValue = getBaseReqHashMap();
        httpMapKeyValue.put(CommCL.PARAMS_APIID, CommCL.PARAM_VALUE_API_ID_MES_UDP);
        httpMapKeyValue.put(CommCL.PARAMS_MES_SB_ID_FLD, sbId);
        httpMapKeyValue.put(CommCL.PARAMS_MES_PRT_NO_FLD, prtNo);
        httpMapKeyValue.put(CommCL.PARAMS_MES_UPD_ID_FLD, udpId);
        return httpMapKeyValue;
    }
    public  static HashMap<String, String> commPrintDataMap(String assist){
        HashMap<String, String> httpMapKeyValue=new HashMap<>();
        httpMapKeyValue.put("assist",assist);
        httpMapKeyValue.put("token","shineon_print");
        return httpMapKeyValue;
    }
    /***
     * 设置系统时间，PDA必须Root成功
     * @param cxt
     * @param datetimes long类型时间
     * @return 如果设置成功, 返回true，反之，false
     */
    public static boolean setSystemTime(final Context cxt, long datetimes) {
        // yyyyMMdd.HHmmss】
        boolean setOK = false;
        try {
            Process process = Runtime.getRuntime().exec("su");
            //String datetime = "20131023.112800"; // 测试的设置的时间【时间格式
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd.HHmmss");

            String datetime = ""; // 测试的设置的时间【时间格式 yyyyMMdd.HHmmss】
            datetime = simpleDateFormat.format(new Date(datetimes));
            DataOutputStream os = new DataOutputStream(
                    process.getOutputStream());
            os.writeBytes("setprop persist.sys.timezone GMT\n");
            os.writeBytes("/system/bin/date -s " + datetime + "\n");
            os.writeBytes("clock -w\n");
            os.writeBytes("exit\n");
            os.flush();
            setOK = true;
        } catch (IOException e) {
//            TastyToast.makeText(cxt, "请获取Root权限", TastyToast.LENGTH_LONG, TastyToast.ERROR).setGravity(Gravity.CENTER, 0, 0);
        }
        return setOK;
    }

    /***
     * 显示消息
     * @param context
     * @param message
     */
    public static void showMessage(Context context, String message) {
        TastyToast.makeText(context, message, T_Time, TastyToast.INFO)
                .setGravity(Gravity.CENTER, 10, 10);
    }

    /***
     * 显示消息
     * @param context
     * @param message
     */
    public static void showSuccess(Context context, String message) {
        TastyToast.makeText(context, message, T_Time, TastyToast.SUCCESS)
                .setGravity(Gravity.CENTER, 10, 10);
    }

    /***
     * 显示错误信息
     * @param context
     * @param error
     */
    public static void showError(Context context, String error) {
        TastyToast.makeText(context, error, T_Time, TastyToast.ERROR).setGravity(Gravity.CENTER, 10, 10);
    }

    /**
     * 得到终端的Mac地址
     *
     * @return
     */
    public static String getMacID() {
        String Mac = null;
        try {

            String path = "sys/class/net/wlan0/address";
            if ((new File(path)).exists()) {
                FileInputStream fis = new FileInputStream(path);
                byte[] buffer = new byte[8192];
                int byteCount = fis.read(buffer);
                if (byteCount > 0) {
                    Mac = new String(buffer, 0, byteCount, StandardCharsets.UTF_8);
                }
            }
            if (TextUtils.isEmpty(Mac) || Mac.length() == 0) {
                path = "sys/class/net/eth0/address";
                FileInputStream fis_name = new FileInputStream(path);
                byte[] buffer_name = new byte[8192];
                int byteCount_name = fis_name.read(buffer_name);
                if (byteCount_name > 0) {
                    Mac = new String(buffer_name, 0, byteCount_name, StandardCharsets.UTF_8);
                }
            }
            if (TextUtils.isEmpty(Mac)||Mac.length() == 0 ) {
                return "";
            }
        } catch (Exception io) {
         //  Log.d("daming.zou**exception*", "" + io.toString());
           //System.out.println(io.toString());
        }
        System.out.println("Mac="+Mac);
        if(TextUtils.isEmpty(Mac)){
            Mac=CommCL.WIFIMAC;
        }
//        Log.d("xulongheng*Mac", Mac);
        return Mac.trim();
    }
    public static String getWifiMac(Context context) {
        StringBuilder deviceId = new StringBuilder();
        WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifi.getConnectionInfo();
        String wifiMac = info.getMacAddress();
        if (!TextUtils.isEmpty(wifiMac)) {
            deviceId.append(wifiMac);
            return deviceId.toString();
        }
        return deviceId.toString();
    }
    /***
     * 本地缓存key和Value
     * @param key 缓存的Key值
     * @param value 缓存的Value
     */
    public static void cacheKeyValue(String key, String value) {
        SharedPreferences.Editor editor = CommCL.sharedPreferences.edit();
        editor.putString(key, value);
        editor.commit();
    }

    /***
     * 将实体类转换成JSONObject
     * @param bean 实体类对象
     * @return 返回JSONObject
     */
    public static JSONObject getJsonObjFromBean(Object bean) {
        String json = JSONObject.toJSONString(bean);
        return JSONObject.parseObject(json);
    }
    public static Observable<Response<JSONObject>> getServerTime() {
        HashMap<String, String> bb = new HashMap<>();
        bb.put(CommCL.PARAMS_APIID, "systime");
        return NetWorkManager.getRequest().getServerTime(bb);
    }

    public static void initZcList(JSONArray array) {
        if (array != null) {
            ArrayList<ZCInfo> zcInfoList = new ArrayList<>();
            for (int i = 0; i < array.size(); i++) {
                ZCInfo zcInfo = JSONObject.parseObject(array.getJSONObject(i).toJSONString(), ZCInfo.class);
                zcInfoList.add(zcInfo);
//                Log.i("CommUtils",zcInfo.toString());
            }
            BaseApplication.zcList = zcInfoList;
        }
    }

    public static ZCInfo getZCInfoById(String id) {
        if (BaseApplication.zcList == null)
            return null;
        ArrayList<ZCInfo> zcInfoList = BaseApplication.zcList;
        ZCInfo zcInfo = null;
        for (int i = 0; i < zcInfoList.size(); i++) {
            ZCInfo zcInfo1 = zcInfoList.get(i);
            if (zcInfo1.getId().equals(id)) {
                zcInfo = zcInfo1;
                break;
            }
        }
        return zcInfo;
    }

    /***
     * 初始化表格头数据，所有工站通用，配置列表
     * @return 返回通用表格头
     */
    public static List<HeaderRowInfo> getRowDataList() {
        List<HeaderRowInfo> rowList = new ArrayList<>();
        HeaderRowInfo sidColumn = new HeaderRowInfo("sid1", "生产批次", 320);
        rowList.add(sidColumn);

        sidColumn = new HeaderRowInfo("state1", "生产状态", 150);
        sidColumn.setAttr(3);
        sidColumn.setContrastMap(CommCL.STATEMap);
        sidColumn.setContrastColors(CommCL.STATEColorMap);
        rowList.add(sidColumn);

        sidColumn = new HeaderRowInfo("mbox", "料盒号", 120);
        rowList.add(sidColumn);

        sidColumn = new HeaderRowInfo("zcno", "制程", 100);
        rowList.add(sidColumn);

        sidColumn = new HeaderRowInfo("slkid", "制令单号", 210);
        rowList.add(sidColumn);

        sidColumn = new HeaderRowInfo("prd_no", "机种名称", 180);
        rowList.add(sidColumn);
        sidColumn = new HeaderRowInfo("qty", "数量");
        rowList.add(sidColumn);//abnormal

        sidColumn = new HeaderRowInfo("abnormal", "异常标识",180);
        rowList.add(sidColumn);//

        sidColumn = new HeaderRowInfo("remark", "备注", 400);
        rowList.add(sidColumn);
        sidColumn = new HeaderRowInfo("proc_id", "制程规划", 180);
        rowList.add(sidColumn);
        return rowList;
    }

    /***
     * 器件下单颗
     * @return 返回通用表格头
     */
    public static List<HeaderRowInfo> getRowDataListXdk() {
        List<HeaderRowInfo> rowList = new ArrayList<>();
        HeaderRowInfo sidColumn = new HeaderRowInfo("sid1", "生产批次", 320);
        rowList.add(sidColumn);

        sidColumn = new HeaderRowInfo("state1", "生产状态", 150);
        sidColumn.setAttr(3);
        sidColumn.setContrastMap(CommCL.STATEMap);
        sidColumn.setContrastColors(CommCL.STATEColorMap);
        rowList.add(sidColumn);

//        sidColumn = new HeaderRowInfo("zcno", "制程", 100);
//        rowList.add(sidColumn);

        sidColumn = new HeaderRowInfo("slkid", "制令单号", 210);
        rowList.add(sidColumn);

        sidColumn = new HeaderRowInfo("prd_no", "机种名称", 180);
        rowList.add(sidColumn);
        sidColumn = new HeaderRowInfo("qty", "数量");
        rowList.add(sidColumn);//abnormal

        sidColumn = new HeaderRowInfo("remark", "备注", 400);
        rowList.add(sidColumn);
        return rowList;
    }
    /***
     * 模组卡板工序
     * 初始化表格头数据，所有工站通用，配置列表
     * @return 返回通用表格头
     */
    public static List<HeaderRowInfo> getRowDataListMz() {
        List<HeaderRowInfo> rowList = new ArrayList<>();
        HeaderRowInfo sidColumn = new HeaderRowInfo("mono", "制令单号", 210);
        rowList.add(sidColumn);
        sidColumn = new HeaderRowInfo("allcode", "序列号", 500);
        rowList.add(sidColumn);
        //sidColumn = new HeaderRowInfo("prd_no", "机种名称", 180);
        //rowList.add(sidColumn);
        sidColumn = new HeaderRowInfo("prd_name", "机型名称", 180);
        rowList.add(sidColumn);
        sidColumn = new HeaderRowInfo("prd_mark", "BinCode", 120);
        rowList.add(sidColumn);
        sidColumn = new HeaderRowInfo("totalqty", "投产数量", 150);
        rowList.add(sidColumn);
        sidColumn = new HeaderRowInfo("apqty", "已卡板数量", 150);
        rowList.add(sidColumn);
        sidColumn = new HeaderRowInfo("upqty", "未卡板数量", 150);
        rowList.add(sidColumn);
        return rowList;
    }
    /***
     * 模组通用工站
     * 初始化表格头数据，所有工站通用，配置列表
     * @return 返回通用表格头
     */
    public static List<HeaderRowInfo> getRowDataListMzTongyong() {
        List<HeaderRowInfo> rowList = new ArrayList<>();
        HeaderRowInfo sidColumn = new HeaderRowInfo("sid1", "批号", 180);
        rowList.add(sidColumn);
        sidColumn = new HeaderRowInfo("state1", "生产状态", 150);
        sidColumn.setAttr(3);
        sidColumn.setContrastMap(CommCL.STATEMap);
        sidColumn.setContrastColors(CommCL.STATEColorMap);
        rowList.add(sidColumn);
        sidColumn = new HeaderRowInfo("prd_name", "机型名称", 180);
        rowList.add(sidColumn);
//        sidColumn = new HeaderRowInfo("prd_mark", "BinCode", 120);
//        rowList.add(sidColumn);
        sidColumn = new HeaderRowInfo("qty", "投产数量", 150);
        rowList.add(sidColumn);
        sidColumn = new HeaderRowInfo("slkid", "制令单号", 150);
        rowList.add(sidColumn);
        sidColumn = new HeaderRowInfo("remark", "备注", 150);
        rowList.add(sidColumn);
        return rowList;
    }
    /***
     * 维修确认
     * 初始化表格头数据，所有工站通用，配置列表
     * @return 返回通用表格头
     */
    public static List<HeaderRowInfo> getRowDataListWxqr() {
        List<HeaderRowInfo> rowList = new ArrayList<>();
        HeaderRowInfo sidColumn = new HeaderRowInfo("slkid", "工单号", 200);
        rowList.add(sidColumn);
        sidColumn = new HeaderRowInfo("prtno", "序列号", 350);
        rowList.add(sidColumn);
        sidColumn = new HeaderRowInfo("prd_no", "货品代号", 150);
        rowList.add(sidColumn);
        sidColumn = new HeaderRowInfo("prd_name", "货品名称", 150);
        rowList.add(sidColumn);
        sidColumn = new HeaderRowInfo("prd_mark", "BinCode", 120);
        rowList.add(sidColumn);
        sidColumn = new HeaderRowInfo("bokName", "判定结果", 120);
        rowList.add(sidColumn);
        return rowList;
    }



    /***
     * 初始化表格头数据，编带看带，配置列表
     * @return 返回通用表格头
     */
    public static List<HeaderRowInfo> getRowDataListBD() {
        List<HeaderRowInfo> rowList = new ArrayList<>();
        HeaderRowInfo sidColumn = new HeaderRowInfo("lotno", "测试号", 320);
        rowList.add(sidColumn);
        sidColumn = new HeaderRowInfo("state1", "生产状态", 150);
        sidColumn.setAttr(3);
        sidColumn.setContrastMap(CommCL.STATEMap);
        sidColumn.setContrastColors(CommCL.STATEColorMap);
        rowList.add(sidColumn);
        sidColumn = new HeaderRowInfo("qty", "数量");
        rowList.add(sidColumn);
        sidColumn = new HeaderRowInfo("bincode", "binCode",180);
        rowList.add(sidColumn);
        sidColumn = new HeaderRowInfo("slkid", "制令单号", 210);
        rowList.add(sidColumn);

        sidColumn = new HeaderRowInfo("sid1", "批次号", 210);
        rowList.add(sidColumn);
        //sidColumn = new HeaderRowInfo("zcno", "制程", 100);
       // rowList.add(sidColumn);

        sidColumn = new HeaderRowInfo("prd_no", "机种名称", 180);
        rowList.add(sidColumn);


        sidColumn = new HeaderRowInfo("remark", "备注", 400);
        rowList.add(sidColumn);
        return rowList;
    }
    /***
     * 初始化表格头数据，所有工站通用，配置列表
     * @return 返回通用表格头
     */
    public static List<HeaderRowInfo> getRowDataListforMboxBind() {
        List<HeaderRowInfo> rowList = new ArrayList<>();
        HeaderRowInfo sidColumn = new HeaderRowInfo("mbox", "料盒号", 100);
        rowList.add(sidColumn);


        sidColumn = new HeaderRowInfo("prd_no", "货品代号", 100);
        rowList.add(sidColumn);

        sidColumn = new HeaderRowInfo("name", "货品名称", 320);
        rowList.add(sidColumn);

        sidColumn = new HeaderRowInfo("spc", "规格", 320);
        rowList.add(sidColumn);

        return rowList;
    }

    /***
     * 初始化表格头数据，所有工站通用，配置列表
     * @return 返回通用表格头
     */
    public static List<HeaderRowInfo> getRowDataList_Pack() {
        List<HeaderRowInfo> rowList = new ArrayList<>();
        HeaderRowInfo sidColumn = new HeaderRowInfo("sid1", "喷码", 350);
        rowList.add(sidColumn);
        sidColumn = new HeaderRowInfo("slkid", "制令单号", 210);
        rowList.add(sidColumn);
        sidColumn = new HeaderRowInfo("prd_name", "机种名称", 240);
        rowList.add(sidColumn);
        sidColumn = new HeaderRowInfo("prd_mark", "BinCode", 180);
        rowList.add(sidColumn);
        sidColumn = new HeaderRowInfo("totalqty", "投产数量", 180);
        rowList.add(sidColumn);
        sidColumn = new HeaderRowInfo("minqty", "单盘数量", 180);
        rowList.add(sidColumn);
        sidColumn = new HeaderRowInfo("tray", "Tray盘号", 180);
        rowList.add(sidColumn);
        sidColumn = new HeaderRowInfo("qty", "数量",180);
        rowList.add(sidColumn);

        sidColumn = new HeaderRowInfo("remark", "备注", 400);
        rowList.add(sidColumn);
        return rowList;
    }


    /**
     *
     * @param context
     * @param title
     * @param msg
     */
    public static AlertDialog showOK(Context context, String title, String msg) {
        final AlertDialog.Builder normalDialog = new AlertDialog.Builder(context);
        normalDialog.setTitle(title);
        normalDialog.setMessage(Html.fromHtml("<font color='red'>" + msg+ "</font>"));
        normalDialog.setCancelable(false); // 设置不可点击界面之外的区域让对话框消失
        normalDialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        // 显示
        return normalDialog.show();
    }

    public static AlertDialog showOKCancel(Context context, String title, String msg, DialogInterface.OnClickListener listener) {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setTitle(title);
        dialog.setMessage(Html.fromHtml("<font color='red'>" + msg + "</font>"));
        dialog.setCancelable(false); // 设置不可点击界面之外的区域让对话框消失
        dialog.setPositiveButton("确定", listener);
        //设置反面按钮
        dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        // 显示
        return dialog.show();
    }

    public static AlertDialog confirm(Context context, String title, String msg,View view, OnConfirmListener confirmListener) {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setTitle(title);
        if(!TextUtils.isEmpty(msg)) {
            dialog.setMessage(Html.fromHtml("<font color='red'>" + msg + "</font>"));
        }
        if(view!=null){
            dialog.setView(view);
        }
        dialog.setCancelable(false); // 设置不可点击界面之外的区域让对话框消失
        dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(confirmListener!=null) {
                    confirmListener.OnConfirm(dialog);
                }
            }
        });
        //设置按钮
        dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(confirmListener!=null) {
                    confirmListener.OnCancel(dialog);
                }
            }
        });
        // 显示
       return dialog.show();
    }



    /**
     * 就一个确定
     * 可以返回确定按钮事件
     * @param context
     * @param title 标题
     * @param msg   内容
     */
    public static AlertDialog alert(Context context, String title, String msg,View view, OnAlertListener alertListener) {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setTitle(title);
        if(!TextUtils.isEmpty(msg)) {
            dialog.setMessage(Html.fromHtml("<font color='red'>" + msg + "</font>"));
        }
        if(view!=null){
            dialog.setView(view);
        }
        dialog.setCancelable(false); // 设置不可点击界面之外的区域让对话框消失
        dialog.setPositiveButton("确定",new  DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(alertListener!=null) {
                    alertListener.OnConfirm(dialog);
                }
            }
        });
        // 显示
        return dialog.show();
    }

    /**
     * 文本框提示然后自动复原【time不要太长】
     * @param textView 提示控件
     * @param text     提示文本
     * @param restore  恢复文本
     * @param time     提示时长(毫秒)
     */
    public static void showTextAuto(TextView textView, String text, String restore,int time){
        if(TextUtils.equals(text,textView.getText())){//如果刚刚提示和当前一样就不再提示
            return;
        }
        textView.setText(text);
        textView.setTextColor(Color.rgb(228,10,10));
        Handler mHandler = new Handler() { //使用handler刷新
            public void handleMessage(Message msg) {
                if(msg.what==2019001){
                    textView.setText(msg.obj.toString());
                    textView.setTextColor(Color.rgb(0,0,0));
                }
            }
        };
        Thread thread=new Thread(new Runnable(){//启用线程
            @Override
            public void run() {
                try {
                    Thread.sleep(time);
                    Message msg = Message.obtain();
                    msg.what=2019001;
                    msg.obj=restore;
                    mHandler.sendMessage(msg);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }


    public static String traceException0(Throwable err) {
        StringWriter sw0 = new StringWriter();
        PrintWriter out = new PrintWriter(sw0);
        if ((err instanceof InvocationTargetException))
            err = ((InvocationTargetException) err).getTargetException();
        err.printStackTrace(out);
        out.close();
        return sw0.toString();
    }


    /**
     * 防止重复扫描--
     * @param key
     * @param value
     * @return
     */
    public synchronized static boolean isRepeat(String key,String value){
        boolean b=true;
        long now=DateUtil.getCurrDateTimeLong();
        if(repeat.containsKey(key)){
            RepeatValue rv=repeat.get(key);
            long oldtime=rv.getTime();
            String v=rv.getValue();
            if(Math.abs(oldtime-now)<=CommCL.ALLOW_REPEAT_TIME&&TextUtils.equals(v,value)){
                b=true;
            }else{
                b=false;
                repeat.put(key,new RepeatValue(now,value));
            }
        }else {
            b=false;
            repeat.put(key,new RepeatValue(now,value));
        }

        return b;
    }
    public synchronized static boolean isRepeat(String key,String value,long time){
        boolean b=true;
        long now=DateUtil.getCurrDateTimeLong();
        if(repeat.containsKey(key)){
            RepeatValue rv=repeat.get(key);
            long oldtime=rv.getTime();
            String v=rv.getValue();
            if(Math.abs(oldtime-now)<=time&&TextUtils.equals(v,value)){
                b=true;
            }else{
                b=false;
                repeat.put(key,new RepeatValue(now,value));
            }
        }else {
            b=false;
            repeat.put(key,new RepeatValue(now,value));
        }

        return b;
    }
    public static long ctime=0;
    public synchronized static boolean isRepeat(int time){
        boolean b=true;
        long now=new Date().getTime();
        if(now-ctime>=time*1000){
            b=false;
            ctime=now;
        }
        return b;
    }
    /**
     * 防止操作过快控制8秒
     * @param key
     * @return
     */
    public static boolean isCanDo(String key){
        String value="no";
        boolean b=true;
        long now=DateUtil.getCurrDateTimeLong();
        if(repeat.containsKey(key)){
            RepeatValue rv=repeat.get(key);
            long oldtime=rv.getTime();
            String v=rv.getValue();
            if(Math.abs(oldtime-now)<=CommCL.ALLOW_CANDO_TIME&&TextUtils.equals(v,value)){
                b=true;
            }else{
                b=false;
                //repeat.put(key,new RepeatValue(now,value));
            }
        }else {
            b=false;
            //repeat.put(key,new RepeatValue(now,value));
        }
        return !b;
    }

    /**
     * 禁止执行
     * @param key
     * @return
     */
    public static void banDo(String key){
        long now=DateUtil.getCurrDateTimeLong();
        String value="no";
        repeat.put(key,new RepeatValue(now,value));
    }
    /**
     * 禁止执行
     * @param key
     * @return
     */
    public static void canDo(String key){
        long now=DateUtil.getCurrDateTimeLong();
        String value="yes";
        repeat.put(key,new RepeatValue(now,value));
    }


    /**
     * 用于保存或更新数据时非空的判断。有任何一个不允许为空的值就无法保存
     * @param values
     * @return
     */
    public static boolean isEmpty(String[] values){
        for(int i=0;i<values.length;i++) {
            if(TextUtils.isEmpty(values[i])){
                return true;
            }
        }
        return false;
    }
    public static int parseInt(String str){
        if(TextUtils.isEmpty(str)){
            return 0;
        }
        return Integer.parseInt(str);
    }
    public static float parseFloat(String str){
        if(TextUtils.isEmpty(str)){
            return 0;
        }
        return  Float.parseFloat(str);
    }
    public static HashMap<String,String> JSONArrayToMap(JSONArray jsonArray,String key,String value){
        HashMap<String,String> hm=new HashMap<>();
        if(jsonArray==null){return hm;}
        for(int i=0;i<jsonArray.size();i++){
            hm.put(jsonArray.getJSONObject(i).getString(key),jsonArray.getJSONObject(i).getString(value));
        }
        return hm;
    }
    /**
     * 判断 分隔 字符串是否内容相等。用于烘烤判断胶水 ，替代料判断
     * @param s1
     * @param s2
     * @param split
     * @return
     */
    public static boolean contentEquals(String s1,String s2,String split) {
        if(TextUtils.isEmpty(s1)||TextUtils.isEmpty(s2)){
            return false;
        }
        String[] str1=s1.split(split);
        String[] str2=s2.split(split);
        for(String st1:str1) {
            for(String st2:str2) {
                if(TextUtils.equals(st1,st2)){
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 初始化语言包，必须安装讯飞语言。才能使用中文
     * 在登录页面调用
     * @param context
     */
    public static void initTextSpeech(Context context){
        if(textSpeech==null) {
            textSpeech = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int status) {
                    /**
                     * 如果装载TTS成功
                     * */
                    if (status == TextToSpeech.SUCCESS) {
                        /**
                         * 有Locale.CHINESE,但是不支持中文
                         * */
                        int result = textSpeech.setLanguage(Locale.ENGLISH);
                        /**
                         * LANG_MISSING_DATA-->语言的数据丢失
                         * LANG_NOT_SUPPORTED-->语言不支持
                         * */
                        //下面是初始化完毕调试用的
                        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                            textSpeech.speak("Chinese is not supported, voice package needs to be installed", TextToSpeech.QUEUE_FLUSH, null);
                        } else {
                           //textSpeech.speak("中文语音初始化成功", TextToSpeech.QUEUE_FLUSH, null);
                        }
                    }
                }
            });
        }
    }

    /**
     * 直接输入文本
     * @param text
     */
    public static void speak(String text){
        textSpeech.speak(text,TextToSpeech.QUEUE_FLUSH,null);
    }

    /**
     * 播放音乐文件
     * @param context
     * @param id  eg:R.raw.sound_di 通过减小音乐时长才能达到快速播放
     */
    public static void playSound(Context context,int id){
        MediaPlayer mPlayer = MediaPlayer.create(context,id);
        mPlayer.start();
        mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                //结束后释放设备资源
                mPlayer.release();
            }
        });
    }

    /**
     * 在onItemClick调用这个之后子列表就可以选择了。【可以单选多选，再表格初始化里面设置】
     * @param view
     * @param position
     * @param id
     * @param dataListViewContent
     */
    public static void OnItemClick(View view, int position, long id,ListView dataListViewContent){
        switch (dataListViewContent.getChoiceMode()) {
            case ListView.CHOICE_MODE_NONE://不能选
                return;
            case ListView.CHOICE_MODE_SINGLE://单选
                dataListViewContent.clearChoices();
                dataListViewContent.setItemChecked(position, true);
                return;
            case ListView.CHOICE_MODE_MULTIPLE_MODAL://多选用这个不然会卡的选不动
            case ListView.CHOICE_MODE_MULTIPLE:
                boolean bSelect = view.isSelected();
                dataListViewContent.setItemChecked(position, !bSelect);
        }
        boolean bSelect = view.isSelected();
        dataListViewContent.setItemChecked(position, !bSelect);
    }

    /**
     * 禁用按钮几秒，自动恢复
     * @param btn
     * @param second
     */
    public static void disableBtn(Button btn,int second){
        btn.setEnabled(false);
        CharSequence text = btn.getText();
        long slp=1000;
        int[] ss=new int[]{second};
        Handler mHandler = new Handler() { //使用handler刷新
            public void handleMessage(Message msg) {
                if(msg.what==20200605){
                    btn.setText(msg.obj.toString());
                }
                if(msg.what==202006051){
                    btn.setText(msg.obj.toString());
                    btn.setEnabled(true);
                }
            }
        };
        Thread thread=new Thread(new Runnable() {
            @Override
            public void run() {
                while (ss[0]>=0) {
                    try {
                        if(ss[0]>0) {
                            Message msg = Message.obtain();
                            msg.what = 20200605;
                            msg.obj = text + " " + ss[0];
                            mHandler.sendMessage(msg);
                        }else{
                            Message msg = Message.obtain();
                            msg.what = 202006051;
                            msg.obj = text ;
                            mHandler.sendMessage(msg);
                        }
                        ss[0]--;
                        Thread.sleep(slp);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        thread.start();
    }
    public static void onItemClick(ListView dataListViewContent, AbstractTableViewAdapter adapter, AdapterView<?> parent, View view, int position, long id){
        switch (dataListViewContent.getChoiceMode()) {
            case ListView.CHOICE_MODE_NONE:
                return;
            case ListView.CHOICE_MODE_SINGLE:
                //单选
                boolean bSelect = view.isSelected();
                if (!bSelect) {
                    dataListViewContent.setItemChecked(position, !bSelect);
                    adapter.notifyDataSetChanged();
                }
                return;
            case ListView.CHOICE_MODE_MULTIPLE_MODAL:
            case ListView.CHOICE_MODE_MULTIPLE:
                bSelect = view.isSelected();
                dataListViewContent.setItemChecked(position, !bSelect);
        }
        boolean bSelect = view.isSelected();
        dataListViewContent.setItemChecked(position, !bSelect);
    }
}
