package com.yimei.activity.login;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.EditText;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.yimei.activity.R;
import com.yimei.activity.base.BaseApplication;
import com.yimei.activity.databinding.ActivityLoginBinding;
import com.yimei.data.CWorkInfo;
import com.yimei.data.CeaPars;
import com.yimei.data.User;
import com.yimei.modelInterface.BaseView;
import com.yimei.network.schedulers.SchedulerProvider;
import com.yimei.presenter.LoginPresenter;
import com.yimei.utils.CommonUtils;
import com.zyao89.view.zloading.ZLoadingDialog;

public class LoginActivity extends AppCompatActivity implements BaseView {
    public ZLoadingDialog zLoadingView;
    private LoginPresenter presenter;
    private User user;

    @BindView(R.id.userName)
    EditText edtUser;
    @BindView(R.id.userPassword)
    EditText pwd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_login);
        ActivityLoginBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_login);
        user = new User("admin", "11");
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

    }

    @Override
    public void showLoading() {
        if(zLoadingView==null)
         zLoadingView = CommonUtils.loginLoadingView(LoginActivity.this);
        zLoadingView.show();
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
}
