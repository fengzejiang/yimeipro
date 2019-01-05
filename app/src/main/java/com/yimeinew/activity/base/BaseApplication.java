package com.yimeinew.activity.base;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import com.yimeinew.data.Menu;
import com.yimeinew.data.User;
import com.yimeinew.data.ZCInfo;
import com.yimeinew.network.NetWorkManager;
import com.yimeinew.utils.CommCL;
import com.yimeinew.utils.ServicesTimeThread;

import java.util.ArrayList;
import java.util.List;

/**
 * @Auther: fengzejiang1987@163.com
 * @Date : 2018/12/7 16:31
 */
public class BaseApplication extends Application {

    public static User currUser;
    private static ArrayList<Menu> menuList;
    public static ArrayList<ZCInfo> zcList;

    @Override
    public void onCreate() {
        super.onCreate();
        NetWorkManager.getInstance().init();
        CommCL.sharedPreferences = getSharedPreferences(CommCL.OPCaCheDir, Context.MODE_WORLD_READABLE);
        ServicesTimeThread thread = new ServicesTimeThread();
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.start();
    }

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
