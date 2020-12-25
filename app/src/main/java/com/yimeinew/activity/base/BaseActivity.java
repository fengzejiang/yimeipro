package com.yimeinew.activity.base;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.text.TextUtils;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import com.yimeinew.activity.R;
import com.yimeinew.data.CWorkInfo;
import com.yimeinew.data.CeaPars;
import com.yimeinew.modelInterface.BaseView;
import com.yimeinew.utils.CommCL;
import com.yimeinew.utils.CommonUtils;
import com.yimeinew.utils.ToolUtils;
import com.zyao89.view.zloading.ZLoadingDialog;
import com.zyao89.view.zloading.Z_TYPE;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @Auther: fengzejiang1987@163.com
 * @Date : 2018/12/7 12:11
 */
public abstract class BaseActivity extends AppCompatActivity implements BaseView {
    public ZLoadingDialog zLoadingView;
    ProgressDialog progressDialog;
    public static boolean canGetMessage = true;
    IntentFilter intentFilter;
    public View alertView;
    public boolean isAlert=false;//当它为true的时候，扫描数据将赋值给alertView这是后的不能为空。为空会奔溃。
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        CommonUtils.initTextSpeech(this);
        intentFilter = new IntentFilter(CommCL.INTENT_ACTION_SCAN_RESULT);//霍尼
        intentFilter.addAction(CommCL.INTENT_ACTION_UROVO_SCAN_RESULT);//优博讯
        myRegisterReceiver(barcodeReceiver,intentFilter); // 注册广播

    }

    /***
     * 显示加载页面
     */
    @Override
    public void showLoading() {
        canGetMessage = false;
        if (zLoadingView == null) {
            zLoadingView = CommonUtils.initLoadingView(this, getString(R.string.loading), Z_TYPE.CIRCLE_CLOCK);
        }
        zLoadingView.show();

    }
    @Override
    public void showLoading(String message) {
        canGetMessage = false;
        if (zLoadingView == null) {
            zLoadingView = CommonUtils.initLoadingView(this, message, Z_TYPE.CIRCLE_CLOCK);
        }
        zLoadingView.show();
    }
    @Override
    public void hideLoading() {
        if (zLoadingView != null)
            zLoadingView.dismiss();
        canGetMessage = true;
    }


    @Override
    public void onRemoteFailed(String message) {
        hideLoading();
        CommonUtils.showError(this, message);
    }

    @Override
    public void showMessage(String message) {//异步显示提示信息最好在hideLoading之后。
        CommonUtils.showError(this, message);
    }
    public void showSuccess(String message){
        CommonUtils.showSuccess(this,message);
    }
    @Override
    public void jumpNextActivity(Class<?> descClass) {
        jumpNextActivity(this, descClass);
    }

    public void jumpNextActivity(Context srcContent, Class<?> descClass) {
        Intent intent = new Intent(srcContent, descClass);
        startActivity(intent);
    }

    public void jumpNextActivity(Class<?> descClass, HashMap<String, Serializable> params) {
        Intent intent = new Intent(this, descClass);
        Iterator<Map.Entry<String, Serializable>> entryKeyIterator = params.entrySet().iterator();
        while (entryKeyIterator.hasNext()) {
            Map.Entry<String, Serializable> e = entryKeyIterator.next();
            Serializable value = e.getValue();
            intent.putExtra(e.getKey(), value);
        }
        startActivity(intent);
    }

    /**
     * 跳转过去要求对方页面关闭时返回数据
     * @param descClass
     * @param params
     */
    public void jumpNextActivityForResult(Class<?> descClass, HashMap<String, Serializable> params) {
        Intent intent = new Intent(this, descClass);
        Iterator<Map.Entry<String, Serializable>> entryKeyIterator = params.entrySet().iterator();
        while (entryKeyIterator.hasNext()) {
            Map.Entry<String, Serializable> e = entryKeyIterator.next();
            Serializable value = e.getValue();
            intent.putExtra(e.getKey(), value);
        }
        startActivityForResult(intent,0);
    }
    /**
     * 使用finish关闭时用这个传递数据。
     * @param params
     */
    public void setIntentDatas(HashMap<String, Serializable> params){
        Intent intent = this.getIntent();
        Iterator<Map.Entry<String, Serializable>> entryKeyIterator = params.entrySet().iterator();
        while (entryKeyIterator.hasNext()) {
            Map.Entry<String, Serializable> e = entryKeyIterator.next();
            Serializable value = e.getValue();
            intent.putExtra(e.getKey(), value);
        }
        setResult(ActivityInfo.FLAG_FINISH_ON_CLOSE_SYSTEM_DIALOGS,intent);
    }
    @Override
    public void addRow(Object unBindInfo) {

    }

    @Override
    public void checkActionBack(boolean bok, int key, CeaPars ceaPars, CWorkInfo cWorkInfo, String error) {

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.base_menu, menu);
        return true;
    }
    @Override
    public void clear() {
    }

    public BroadcastReceiver  barcodeReceiver = new BroadcastReceiver() {
        @Override
        public synchronized void onReceive(Context context, Intent intent) {
             if (CommCL.INTENT_ACTION_SCAN_RESULT.equals(intent.getAction())||CommCL.INTENT_ACTION_UROVO_SCAN_RESULT.equals(intent.getAction())) {
                     if (!canGetMessage) {
                         return;
                     }
                     View rootView;
                     if (isAlert && alertView != null) {//这个标志位要控制好不然会报错
                         rootView = alertView;//获取alert焦点
                     } else {
                         rootView = getCurrentFocus();//获取光标当前所在组件
                     }
                     Object tag = rootView.findFocus().getTag();
                     if (tag == null) {
                         return;
                     }
                     String barCodeData = "";
                     if (!TextUtils.isEmpty(intent.getStringExtra(CommCL.SCN_CUST_HONEY))) {
                         barCodeData = intent.getStringExtra(CommCL.SCN_CUST_HONEY);//霍尼
                     } else if (!TextUtils.isEmpty(intent.getStringExtra(CommCL.SCN_CUST_UROVO))) {
                         barCodeData = intent.getStringExtra(CommCL.SCN_CUST_UROVO);//优博讯
                     } else {
                         barCodeData = intent.getStringExtra(CommCL.SCN_CUST_EX_SCODE);//肖邦
                     }
                     barCodeData = barCodeData.toUpperCase();
                     //System.out.println("woshi="+barCodeData+flag);
                     EditText et = (EditText) rootView;
                     int it = et.getInputType();
                     switch (it) {
                         case InputType.TYPE_CLASS_NUMBER://如果是数字类型判断
                             if (!ToolUtils.isInteger(barCodeData)) {
                                 showMessage("该输入框只能输入整数，不能输入" + barCodeData);
                                 return;
                             }
                             break;
                         case 8194://InputType=numberDecimal
                             if (!ToolUtils.isDouble(barCodeData)) {
                                 showMessage("该输入框只能输入数字，不能输入" + barCodeData);
                                 return;
                             }
                             break;
                     }

                     et.setText(barCodeData);
                     //System.out.println("woshi=结束睡眠" + et.getText());
                     onEditTextKeyDown(et);


             }
        }
    };

    public abstract boolean onEditTextKeyDown(EditText editText);
    public  void myUnregisterReceiver(BroadcastReceiver barcodeReceiver){
        try{
            unregisterReceiver(barcodeReceiver);
        }catch (Exception e){
            //e.printStackTrace();
        }
    }
    public  void myRegisterReceiver(BroadcastReceiver barcodeReceiver,IntentFilter intentFilter){
        try{
            unregisterReceiver(barcodeReceiver);
        }catch (Exception e){
            //e.printStackTrace();
        }finally {
            registerReceiver(barcodeReceiver, intentFilter); // 注册广播
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
//        Log.i(TAG_NAME,"销毁我了");
        myUnregisterReceiver(barcodeReceiver);
    }
    @Override
    protected void onResume() {
        super.onResume();
        //ServicesTimeThread.getServiceTime();
        myRegisterReceiver(barcodeReceiver, intentFilter);

    }
    //Activity被覆盖到下面或者锁屏时被调用
    @Override
    protected void onPause() {
        super.onPause();
        //Log.i(TAG, "onPause called.");
        //有可能在执行完onPause或onStop后,系统资源紧张将Activity杀死,所以有必要在此保存持久数据
        myUnregisterReceiver(barcodeReceiver);

    }
    //退出当前Activity或者跳转到新Activity时被调用
    @Override
    protected void onStop() {
        super.onStop();
        //Log.i(TAG, "onStop called.");
        myUnregisterReceiver(barcodeReceiver);

    }
}
