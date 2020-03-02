package com.yimeinew.presenter;

import android.content.Intent;
import com.yimeinew.activity.MainActivity;
import com.yimeinew.data.Menu;
import com.yimeinew.modelInterface.BaseView;
import com.yimeinew.utils.CommCL;

import java.util.HashMap;

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
            Intent intent = new Intent(menuView, menu.getMenuClass());
            HashMap<String,String> hm=new HashMap<>();
            hm.put("name",menu.getName());
            hm.put("id",menu.getMid());
            intent.putExtra(CommCL.COMM_ZC_INFO_FLD, hm);
            menuView.startActivity(intent);
            //menuView.jumpNextActivity(menu.getMenuClass());
        }
    }
}
