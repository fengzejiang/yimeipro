package com.yimeinew.presenter;

import android.text.TextUtils;
import android.util.Log;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.shade.com.alibaba.fastjson.JSONArray;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.shade.com.alibaba.fastjson.JSONObject;
import com.yimeinew.activity.MainActivity;
import com.yimeinew.activity.base.BaseApplication;
import com.yimeinew.data.Menu;
import com.yimeinew.data.User;
import com.yimeinew.model.LoginModelI;
import com.yimeinew.model.impl.LoginModel;
import com.yimeinew.modelInterface.BaseView;
import com.yimeinew.network.response.ResponseTransformer;
import com.yimeinew.network.schedulers.BaseSchedulerProvider;
import com.yimeinew.utils.*;
import io.reactivex.disposables.CompositeDisposable;

import java.util.ArrayList;

/**
 * @Auther: fengzejiang1987@163.com
 * @Date : 2018/12/7 13:36
 */
public class LoginPresenter implements LoginModelI {
    private final String TAG_NAME = LoginPresenter.class.getSimpleName();
    private BaseView baseView;

    private static long app_t=0;
    private static long tim_ce=0;
    LoginModel loginModel = new LoginModel(this);
    private BaseSchedulerProvider schedulerProvider;

    private CompositeDisposable mDisposable;
    public LoginPresenter(BaseView mvpView, BaseSchedulerProvider baseSchedulerProvider) {
        this.baseView = mvpView;
        schedulerProvider = baseSchedulerProvider;
        mDisposable = new CompositeDisposable();
    }
        @Override
    public void loginSuccess() {
        baseView.hideLoading();
        baseView.jumpNextActivity(MainActivity.class);
    }

    public void despose(){
        mDisposable.dispose();
    }

    @Override
    public void loginFailed(String message) {
        baseView.hideLoading();
        baseView.onRemoteFailed(message);
    }

    private void getMenusAndUser(JSONObject rtnJsonObj) {
        JSONArray bb = (JSONArray) rtnJsonObj.get("menulist");
        bb.addAll(CommCL.addMenus);
        //初始化菜单列表
        ArrayList<Menu> menuList = new ArrayList<>();
        for(int i=0;i<bb.size();i++){
            JSONObject medalistJson = (JSONObject) bb.get(i);
            Menu menu = new Menu(medalistJson.getString("menuId"),medalistJson.getString("menuName"));
            menuList.add(menu);
        }
        JSONObject jsonUser = (JSONObject) rtnJsonObj.get("user");
        BaseApplication.currUser.setUserName(jsonUser.getString("userName"));
        JSONObject jsonDept = (JSONObject)jsonUser.get("deptInfo");
        BaseApplication.currUser.setCmcCode(jsonDept.getString("cmcCode"));
        BaseApplication.currUser.setCmcName(jsonDept.getString("cmcName"));
        BaseApplication.currUser.setDeptCode(jsonDept.getString("deptCode"));
        BaseApplication.initMenuList(menuList);
        OPUtils opUtils = new OPUtils();
        opUtils.start();
    }

    public void login(User user){
        Log.i(TAG_NAME,user.toString());
        String userCode = user.getUserCode();
        if(userCode!=null){
            userCode = userCode.trim();
        }
        if(TextUtils.isEmpty(userCode)){
            baseView.showMessage("请输入登录账户");
            return;
        }
        baseView.showLoading();
        loginModel.doLogin(user).compose(ResponseTransformer.handleResult())
                .compose(schedulerProvider.applySchedulers()).subscribe(
                        carBeans -> {
                            Log.i(TAG_NAME, carBeans.toString());
                            if (carBeans.getIntValue(CommCL.RTN_ID) != 0) {
                                loginFailed(carBeans.getString(CommCL.RTN_MESSAGE));
                            }
                            if (carBeans.getIntValue(CommCL.RTN_ID) == 0) {
                                BaseApplication.currUser = user;
                                getMenusAndUser((JSONObject) carBeans.get(CommCL.RTN_DATA));
                                getZCInfo();//登录成功以后获取制成信息
                                ServicesTimeThread.getServiceTimeOK();//登录成功同步时间
                                loginSuccess();
                            }
                        },
                        throwable -> {
                            Log.i(TAG_NAME,"出错了！！");
                            baseView.onRemoteFailed(throwable.getMessage()+"oo");
                        }
                );
    }

    public void getZCInfo(){
        loginModel.getZCInfo().compose(ResponseTransformer.handleResult()).compose(schedulerProvider.applySchedulers())
                .subscribe(jsonObject -> {
                    if (jsonObject.getIntValue(CommCL.RTN_ID) == -1){
                        Log.i(TAG_NAME,jsonObject.getString(CommCL.RTN_MESSAGE));
                    }else{
                        JSONObject msg = jsonObject.getJSONObject(CommCL.RTN_DATA);//response里面的data，存放的是查询记录
                        JSONObject rtnMap = (JSONObject) msg.get(CommCL.RTN_DATA);//获取实际的查询结果
                        Log.i(TAG_NAME,rtnMap.toJSONString());
                        if (rtnMap.getInteger(CommCL.RTN_CODE) == 1) {
                            JSONArray array = rtnMap.getJSONArray(CommCL.RTN_VALUES);
                            CommonUtils.initZcList(array);
                        }
                    }
                },throwable -> {
                    Log.i(TAG_NAME,throwable.getMessage());
                });
    }

}
