package com.yimeinew.activity.login;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.provider.SyncStateContract;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnEditorAction;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.shade.com.alibaba.fastjson.JSONArray;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.shade.com.alibaba.fastjson.JSONObject;
import com.yimeinew.activity.R;
import com.yimeinew.activity.base.BaseApplication;
import com.yimeinew.activity.databinding.ActivityLoginBinding;
import com.yimeinew.data.CWorkInfo;
import com.yimeinew.data.CeaPars;
import com.yimeinew.data.User;
import com.yimeinew.modelInterface.BaseView;
import com.yimeinew.network.schedulers.SchedulerProvider;
import com.yimeinew.presenter.LoginPresenter;
import com.yimeinew.utils.*;
import com.zyao89.view.zloading.ZLoadingDialog;

import java.io.File;

public class LoginActivity extends AppCompatActivity implements BaseView {
    public ZLoadingDialog zLoadingView;
    private LoginPresenter presenter;
    private User user;
    private static int REQUEST_PERMISSION_CODE = 1;
    private  static String[]PERMISSIONS_STORAGE={  //需要的權限數組
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS,
            Manifest.permission.INTERNET,
            Manifest.permission.INSTALL_PACKAGES,
            Manifest.permission.CAMERA
    };

    @BindView(R.id.userName)
    EditText edtUser;
    @BindView(R.id.userPassword)
    EditText pwd;
    @BindView(R.id.login_tv_ip)
    TextView iptv;
    @BindView(R.id.login_tv_version)
    TextView versiontv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_login);
        ActivityLoginBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_login);
        if(CommCL.isDev){
            user = new User("admin", "shine_admin");
        }else{
            user = new User();
        }
        binding.setUser(user);
        presenter = new LoginPresenter(this, SchedulerProvider.getInstance());
//        presenter.getTime();
        binding.setPresenter(presenter);
        ButterKnife.bind(this);
        Drawable drawable1 = getResources().getDrawable(R.drawable.ym_user);
        drawable1.setBounds(0, 0, 60, 60);//第一0是距左边距离，第二0是距上边距离，60分别是长宽
        edtUser.setCompoundDrawables(drawable1, null, null, null);//只放左边
        Drawable drawable2 = getResources().getDrawable(R.drawable.ym_lock);
        drawable2.setBounds(0, 0, 60, 60);//第一0是距左边距离，第二0是距上边距离，60分别是长宽
        pwd.setCompoundDrawables(drawable2, null, null, null);//只放左边
        BaseApplication.addActivity_(LoginActivity.this);
        /*其他数据初始化*/
        String tempURi = CommCL.sharedPreferences.getString(CommCL.URi_KEY, null);
        if(!TextUtils.isEmpty(tempURi)){
            CommCL.URi=tempURi;
            CommCL.IP = "IP:" + CommCL.URi.substring(7, CommCL.URi.lastIndexOf(":"));
        }

        //显示ip和版本信息
        iptv.setText(CommCL.IP);
        versiontv.setText(CommCL.SHOW_VERSION);
        //通过WIFI获取MAC
        String mac=CommonUtils.getWifiMac(getApplicationContext());
        if(!TextUtils.isEmpty(mac)) {
            CommCL.WIFIMAC = mac;
        }
        getStoragePermissions();
        DownloadAPK.checkVersion(LoginActivity.this);
    }

    @OnEditorAction({R.id.userName,R.id.userPassword})
    boolean OnEdit(EditText editText){
        int id = editText.getId();
        if(TextUtils.isEmpty(user.getUserCode())){
            CommonUtils.textViewGetFocus(edtUser);
            return true;
        }

        if(id == R.id.userName){
            CommonUtils.textViewGetFocus(pwd);
            return true;
        }
        if(id == R.id.userPassword){
            showMessage("我回车了");
            return true;
        }
        return false;
    }

    @Override
    public void showLoading() {
        if(zLoadingView==null)
         zLoadingView = CommonUtils.loginLoadingView(LoginActivity.this);
        zLoadingView.show();
    }

    @Override
    public void showLoading(String message) {

    }

    @Override
    public void showMessage(String message) {
        CommonUtils.showMessage(this,message);
//        TastyToast.makeText(this, message, TastyToast.CONFUSING, TastyToast.CONFUSING).setGravity(Gravity.TOP, 10, 10);
    }

    @Override
    public void jumpNextActivity(Class<?> descClass) {

        jumpNextActivity(LoginActivity.this,descClass);
    }

    @Override
    public void jumpNextActivity(Context srcContent, Class<?> descClass) {
        Intent intent = new Intent(srcContent, descClass);
        startActivity(intent);
        BaseApplication.removeActivity_(LoginActivity.this);
    }

    @Override
    public void addRow(Object unBindInfo) {

    }

    /***
     *
     * @param bok 是否成功
     * @param key 调用的是33还是34,33：获取审批信息，34执行审批信息
     * @param ceaPars 交互过程中的状态
     * @param cWorkInfo 审批流信息
     * @param error 错误信息
     */
    @Override
    public void checkActionBack(boolean bok, int key, CeaPars ceaPars, CWorkInfo cWorkInfo, String error) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.despose();
    }

    @Override
    public void hideLoading() {
        if(zLoadingView!=null)
            zLoadingView.dismiss();
    }

    @Override
    public void onRemoteFailed(String message) {
        CommonUtils.showError(this,message);
//        TastyToast.makeText(this, message, TastyToast.CONFUSING, TastyToast.ERROR).setGravity(Gravity.TOP, 10, 10);
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            exit();
        }
        return super.onKeyDown(keyCode, event);
    }
    public void exit() {

            finish();
            System.exit(0);

    }
    @OnClick({R.id.login_tv_ip,R.id.login_tv_version})
    public void onClick(View v){
        int id=v.getId();
        switch (id){
            case R.id.login_tv_version:
//
                if(TextUtils.equals(edtUser.getText(),CommCL.UP_APK_KEY)||true) {
                    getStoragePermissions();
                    ProgressDialog progressDialog = DownloadAPK.getProgressDialog(LoginActivity.this);
                    progressDialog.show();
                    new DownloadAPK(progressDialog, LoginActivity.this).execute(CommCL.APK_URL);
                }else{
                    showMessage("请在登录账户输入更新密钥！");
                }
                break;
            case R.id.login_tv_ip:
                if(TextUtils.isEmpty(edtUser.getText())){
                    showMessage("请在登录账户输入口令");
                    break;
                }
                if(TextUtils.equals(pwd.getText(),CommCL.UP_APK_KEY)){
                    if(TextUtils.equals(edtUser.getText(),"外网")){
                        CommCL.sharedPreferences.edit().putString(CommCL.URi_KEY,CommCL.WURi).commit();
                        iptv.setText("按返回键退出APP，重新进入方可生效");
                        break;
                    }else if(TextUtils.equals(edtUser.getText(),"内网")){
                        CommCL.sharedPreferences.edit().putString(CommCL.URi_KEY,CommCL.NURi).commit();
                        iptv.setText("按返回键退出APP，重新进入方可生效");
                        break;
                    }
                    iptv.setText("口令无效");
                }else{
                    showMessage("请在密码输入正确密钥！");
                }
                break;
        }
    }
    @Override
    public void clear() {

        
    }
    /*动态获取权限*/
    public void getStoragePermissions(){
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP) {   //判断是否android6.0以上
            int stotage=ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);//存储权限
            int camera=ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);//相机权限
            if ( stotage!= PackageManager.PERMISSION_GRANTED||camera!= PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, REQUEST_PERMISSION_CODE);
            }
            for(int i=0;i<PERMISSIONS_STORAGE.length;i++){
                int result=ActivityCompat.checkSelfPermission(this, PERMISSIONS_STORAGE[i]);
                System.out.println("woshi="+PERMISSIONS_STORAGE[i]+"  ="+result);
                //Log.i(SyncStateContract.Constants.TAG,"执行完"+PERMISSIONS_STORAGE[i]+"权限为--》"+result);
            }
        }
    }

}
