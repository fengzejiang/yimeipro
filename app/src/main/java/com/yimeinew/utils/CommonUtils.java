package com.yimeinew.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.text.Html;
import android.text.TextUtils;
import android.util.Base64;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.Toast;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.shade.com.alibaba.fastjson.JSONArray;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.shade.com.alibaba.fastjson.JSONObject;
import com.sdsmdg.tastytoast.TastyToast;
import com.yimeinew.activity.R;
import com.yimeinew.activity.base.BaseApplication;
import com.yimeinew.data.ZCInfo;
import com.yimeinew.network.NetWorkManager;
import com.yimeinew.network.response.Response;
import com.yimeinew.tableui.entity.HeaderRowInfo;
import com.zyao89.view.zloading.ZLoadingDialog;
import com.zyao89.view.zloading.Z_TYPE;
import io.reactivex.Observable;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * @Auther: fengzejiang1987@163.com
 * @Date : 2018/12/7 14:05
 */
public class CommonUtils {

    public static int T_Time = Toast.LENGTH_LONG;

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
            if (Mac == null || Mac.length() == 0) {
                path = "sys/class/net/eth0/address";
                FileInputStream fis_name = new FileInputStream(path);
                byte[] buffer_name = new byte[8192];
                int byteCount_name = fis_name.read(buffer_name);
                if (byteCount_name > 0) {
                    Mac = new String(buffer_name, 0, byteCount_name, StandardCharsets.UTF_8);
                }
            }
            if (Mac.length() == 0 || Mac == null) {
                return "";
            }
        } catch (Exception io) {
//            Log.d("daming.zou**exception*", "" + io.toString());
        }

//        Log.d("xulongheng*Mac", Mac);
        return Mac.trim();
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
     * 初始化表格头数据，所有工站通用
     * @return 返回通用表格头
     */
    public static List<HeaderRowInfo> getRowDataList() {
        List<HeaderRowInfo> rowList = new ArrayList<>();
        HeaderRowInfo sidColumn = new HeaderRowInfo("sid1", "生产批次", 320);
        rowList.add(sidColumn);
        sidColumn = new HeaderRowInfo("state1", "生产状态", 180);
        sidColumn.setAttr(3);
        sidColumn.setContrastMap(CommCL.STATEMap);
        sidColumn.setContrastColors(CommCL.STATEColorMap);
        rowList.add(sidColumn);
        sidColumn = new HeaderRowInfo("slkid", "制令单号", 210);
        rowList.add(sidColumn);

        sidColumn = new HeaderRowInfo("prd_no", "机种名称", 180);
        rowList.add(sidColumn);
        sidColumn = new HeaderRowInfo("qty", "数量");
        rowList.add(sidColumn);

        sidColumn = new HeaderRowInfo("remark", "备注", 400);
        rowList.add(sidColumn);
        return rowList;
    }

    public static void showOK(Context context, String title, String msg) {
        final AlertDialog.Builder normalDialog = new AlertDialog.Builder(
                context);
        normalDialog.setTitle(title);
        normalDialog.setMessage(Html.fromHtml("<font color='red'>" + msg
                + "</font>"));
        normalDialog.setCancelable(false); // 设置不可点击界面之外的区域让对话框消失
        normalDialog.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        // 显示
        normalDialog.show();
    }

    public static void showOKCancel(Context context, String title, String msg, DialogInterface.OnClickListener listener) {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(
                context);
        dialog.setTitle(title);
        dialog.setMessage(Html.fromHtml("<font color='red'>" + msg
                + "</font>"));
        dialog.setCancelable(false); // 设置不可点击界面之外的区域让对话框消失
        dialog.setPositiveButton("确定", listener);
        //设置反面按钮
        dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        // 显示
        dialog.show();
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
}
