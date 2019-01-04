package com.yimei.presenter;

import android.util.Log;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.shade.com.alibaba.fastjson.JSONArray;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.shade.com.alibaba.fastjson.JSONObject;
import com.yimei.activity.MainActivity;
import com.yimei.activity.base.BaseApplication;
import com.yimei.activity.login.LoginActivity;
import com.yimei.data.Menu;
import com.yimei.data.User;
import com.yimei.model.LoginModelI;
import com.yimei.model.impl.LoginData;
import com.yimei.modelInterface.BaseView;
import com.yimei.network.response.Response;
import com.yimei.network.response.ResponseTransformer;
import com.yimei.network.schedulers.BaseSchedulerProvider;
import com.yimei.utils.*;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

import java.util.ArrayList;

/**
 * @Auther: fengzejiang1987@163.com
 * @Date : 2018/12/7 13:36
 */
public class LoginPresenter implements LoginModelI {
    private final String TAG_NAME = LoginPresenter.class.getSimpleName();
    private BaseView mvpView;

    private static long app_t=0;
    private static long tim_ce=0;
    LoginData loginData = new LoginData(this);
    private BaseSchedulerProvider schedulerProvider;

    private CompositeDisposable mDisposable;
    public LoginPresenter(BaseView mvpView, BaseSchedulerProvider baseSchedulerProvider) {
        this.mvpView = mvpView;
        schedulerProvider = baseSchedulerProvider;
        mDisposable = new CompositeDisposable();
    }
        @Override
    public void loginSuccess() {
        mvpView.hideLoading();
        mvpView.jumpNextActivity(MainActivity.class);
    }

    public void despose(){
        mDisposable.dispose();
    }

    @Override
    public void loginFailed(String message) {
        mvpView.hideLoading();
        mvpView.onRemoteFailed(message);
    }

    private void getMenusAndUser(JSONObject rtnJsonObj) {
        JSONArray bb = (JSONArray) rtnJsonObj.get("menulist");
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
        mvpView.showLoading();
        Disposable disposable = loginData.doLogin(user).compose(ResponseTransformer.handleResult())
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
                                loginSuccess();
                            }
                        },
                        throwable -> {
                            Log.i(TAG_NAME,"出错了！！");
                            mvpView.onRemoteFailed(throwable.getMessage());
                        }
                );

        mDisposable.add(disposable);
    }

    public void getZCInfo(){
        loginData.getZCInfo().compose(ResponseTransformer.handleResult()).compose(schedulerProvider.applySchedulers())
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

    public void getTime() {
        app_t = System.currentTimeMillis();
        CommonUtils.getServerTime().compose(ResponseTransformer.handleResult()).compose(schedulerProvider.applySchedulers())
                .subscribe(
                carBeans -> {
                    if(carBeans.getInteger("id")==0){
                        JSONObject serTime = (JSONObject) ((JSONObject) carBeans.get("data")).get("data");
                        long curr_time = serTime.getLong("time");
                        tim_ce = curr_time-app_t;
                        Log.i(TAG_NAME,app_t+"=="+tim_ce);
                        Log.i(TAG_NAME,curr_time+"=="+tim_ce);
                        DateUtil.TIMEDIFF = tim_ce;
                        boolean setOK = CommonUtils.setSystemTime((LoginActivity)mvpView,curr_time);
                        if(!setOK){
                            mvpView.showMessage("更改时间失败，请退出重新设置时间");
                        }
                    }else{
                        mvpView.showMessage(carBeans.getString("message"));
                    }

                }
        );
    }
}
