package com.yimeinew.presenter;

import com.yimeinew.activity.MainActivity;
import com.yimeinew.data.Menu;
import com.yimeinew.modelInterface.BaseView;

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
