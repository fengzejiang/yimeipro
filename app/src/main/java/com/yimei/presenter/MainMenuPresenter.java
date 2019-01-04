package com.yimei.presenter;

import android.content.Intent;
import android.util.Log;
import com.yimei.activity.MainActivity;
import com.yimei.data.Menu;
import com.yimei.modelInterface.BaseView;

/**
 * @Auther: fengzejiang1987@163.com
 * @Date : 2018/12/9 14:08
 */
public class MainMenuPresenter {
    private MainActivity menuView;
    public MainMenuPresenter(BaseView menuView){
        this.menuView = (MainActivity)menuView;
    }

    public void menuImgOnClick(Menu menu){
        if(menu.getMenuClass()!=null){
            menuView.jumpNextActivity(menu.getMenuClass());
        }
    }
}
