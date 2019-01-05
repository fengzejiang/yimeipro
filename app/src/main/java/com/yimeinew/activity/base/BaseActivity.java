package com.yimeinew.activity.base;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import com.yimeinew.activity.R;
import com.yimeinew.data.CWorkInfo;
import com.yimeinew.data.CeaPars;
import com.yimeinew.modelInterface.BaseView;
import com.yimeinew.utils.CommonUtils;
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
public class BaseActivity extends AppCompatActivity implements BaseView {
    public ZLoadingDialog zLoadingView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

    }


    /***
     * 显示加载页面
     */
    @Override
    public void showLoading() {
        if (zLoadingView == null) {
            zLoadingView = CommonUtils.initLoadingView(this, getString(R.string.loading), Z_TYPE.CIRCLE_CLOCK);
        }
        zLoadingView.show();
    }

    @Override
    public void hideLoading() {
        if (zLoadingView != null)
            zLoadingView.dismiss();
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

}
