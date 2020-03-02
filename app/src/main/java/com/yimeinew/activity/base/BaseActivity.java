package com.yimeinew.activity.base;

import android.app.Activity;
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
    public static boolean canGetMessage = true;
    IntentFilter intentFilter;
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
        if (zLoadingView == null) {
            zLoadingView = CommonUtils.initLoadingView(this, getString(R.string.loading), Z_TYPE.CIRCLE_CLOCK);
        }
        canGetMessage = false;
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
        CommonUtils.showError(this, message);
    }

    @Override
    public void showMessage(String message) {
        CommonUtils.showError(this, message);
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
        public void onReceive(Context context, Intent intent) {
            if (CommCL.INTENT_ACTION_SCAN_RESULT.equals(intent.getAction())||CommCL.INTENT_ACTION_UROVO_SCAN_RESULT.equals(intent.getAction())) {
                if(!canGetMessage){
                    return ;
                }
                View rootView = getCurrentFocus();//获取光标当前所在组件
                Object tag = rootView.findFocus().getTag();
                if (tag == null) {
                    return;
                }
                String barCodeData = "";
                if (!TextUtils.isEmpty(intent.getStringExtra(CommCL.SCN_CUST_HONEY))) {
                    barCodeData = intent.getStringExtra(CommCL.SCN_CUST_HONEY);//霍尼
                }else  if(!TextUtils.isEmpty(intent.getStringExtra(CommCL.SCN_CUST_UROVO))) {
                    barCodeData = intent.getStringExtra(CommCL.SCN_CUST_UROVO);//优博讯
                }else {
                    barCodeData = intent.getStringExtra(CommCL.SCN_CUST_EX_SCODE);//肖邦
                }
                barCodeData = barCodeData.toUpperCase();
                EditText et=(EditText)rootView;
                int it = et.getInputType();
                switch (it){
                    case InputType.TYPE_CLASS_NUMBER://如果是数字类型判断
                        if(!ToolUtils.isInteger(barCodeData)){
                            showMessage("该输入框只能输入数字，不能输入"+barCodeData);
                            return;
                        }
                        break;
                }
                et.setText(barCodeData);
                onEditTextKeyDown(et);
            }
        }
    };

    public abstract boolean onEditTextKeyDown(EditText editText);
    public  void myUnregisterReceiver(BroadcastReceiver barcodeReceiver){
        try{
            unregisterReceiver(barcodeReceiver);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public  void myRegisterReceiver(BroadcastReceiver barcodeReceiver,IntentFilter intentFilter){
        try{
            unregisterReceiver(barcodeReceiver);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            registerReceiver(barcodeReceiver, intentFilter); // 注册广播
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
//        Log.i(TAG_NAME,"销毁我了");
        myUnregisterReceiver(barcodeReceiver);
        System.out.println("woshi  fonDestroy"+barcodeReceiver.getDebugUnregister());
    }
    @Override
    protected void onResume() {
        super.onResume();
        myRegisterReceiver(barcodeReceiver, intentFilter);
        System.out.println("woshi  fonResume"+barcodeReceiver.getDebugUnregister());
    }
    //Activity被覆盖到下面或者锁屏时被调用
    @Override
    protected void onPause() {
        super.onPause();
        //Log.i(TAG, "onPause called.");
        //有可能在执行完onPause或onStop后,系统资源紧张将Activity杀死,所以有必要在此保存持久数据
        myUnregisterReceiver(barcodeReceiver);
        System.out.println("woshi  fonPause"+barcodeReceiver.getDebugUnregister());

    }
    //退出当前Activity或者跳转到新Activity时被调用
    @Override
    protected void onStop() {
        super.onStop();
        //Log.i(TAG, "onStop called.");
        myUnregisterReceiver(barcodeReceiver);
        System.out.println("woshi  fonStop");
    }
}
