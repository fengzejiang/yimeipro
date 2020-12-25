package com.yimeinew.activity.base;

import android.app.Activity;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.TextUtils;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.shade.com.alibaba.fastjson.JSONObject;
import com.yimeinew.data.Menu;
import com.yimeinew.data.User;
import com.yimeinew.data.ZCInfo;
import com.yimeinew.network.NetWorkManager;
import com.yimeinew.network.NetWorkPrintManager;
import com.yimeinew.utils.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @Auther: fengzejiang1987@163.com
 * @Date : 2018/12/7 16:31
 * 这个启动是只会执行一次
 */
public class BaseApplication extends Application {

    public static User currUser;
    private static ArrayList<Menu> menuList;
    public static ArrayList<ZCInfo> zcList;
    IntentFilter intentFilter;
    @Override
    public void onCreate() {
        super.onCreate();
        CommCL.sharedPreferences = getSharedPreferences(CommCL.OPCaCheDir, Context.MODE_PRIVATE);
        /*其他数据初始化*/
        String tempURi = CommCL.sharedPreferences.getString(CommCL.URi_KEY, null);
        if(!TextUtils.isEmpty(tempURi)){
            CommCL.URi=tempURi;
            CommCL.IP = "IP:" + CommCL.URi.substring(7, CommCL.URi.lastIndexOf(":"));
        }
        NetWorkManager.getInstance().init();
        NetWorkPrintManager.getInstance().init();

//        ServicesTimeThread thread = new ServicesTimeThread();
//        thread.setPriority(Thread.MIN_PRIORITY);
//        thread.start();
        //时间监听器
        intentFilter=new IntentFilter(Intent.ACTION_TIME_CHANGED);//时间监听器
        intentFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED);//时间监听器
        registerReceiver(timebarcodeReceiver, intentFilter); // 注册广播
        //初始化Sqlite
        SqliteUtil.getInstance(this);
    }
    //时间监听器
    public BroadcastReceiver timebarcodeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(Intent.ACTION_TIME_CHANGED.equals(intent.getAction())||Intent.ACTION_TIMEZONE_CHANGED.equals(intent.getAction())){
                ServicesTimeThread.getServiceTimeOK();
            }
        }
    };
    /***
     * 获取菜单信息
     * @return
     */
    public static ArrayList<Menu> getMenuList(){
        return menuList;
    }

    /***
     * 设置菜单信息
     * @param menus
     */
    public static void initMenuList(ArrayList<Menu> menus){
        menuList = menus;
    }


    /*
     * 公共变量
     */
    private static List<Activity> aList;// 用于存放所有启动的Activity的集合

    /**
     * 添加Activity
     */
    public static void addActivity_(Activity activity) {
        // 判断当前集合中不存在该Activity
        if(aList==null){
            aList = new ArrayList<>();
        }
        if (!aList.contains(activity)) {
            aList.add(activity);// 把当前Activity添加到集合中
        }
    }

    /**
     * 销毁单个Activity
     */
    public static void removeActivity_(Activity activity) {
        // 判断当前集合中存在该Activity
        if(aList==null)
            return;
        if (aList.contains(activity)) {
            aList.remove(activity);// 从集合中移除

        }
        activity.finish();// 销毁当前Activity
    }


}
