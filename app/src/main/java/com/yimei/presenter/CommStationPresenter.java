package com.yimei.presenter;

import android.content.Intent;
import com.yimei.activity.deviceproduction.CommonStationActivity;
import com.yimei.data.ZCInfo;
import com.yimei.modelInterface.BaseView;
import com.yimei.utils.CommCL;

/**
 * @Auther: fengzejiang1987@163.com
 * @Date : 2018/12/9 14:08
 */
public class CommStationPresenter {
    private CommonStationActivity menuView;
    public CommStationPresenter(BaseView menuView){
        this.menuView = (CommonStationActivity)menuView;
    }

    public void menuImgOnClick(ZCInfo zCnoInfo){
        if(zCnoInfo.getClazz()!=null){
            Intent intent = new Intent(menuView, zCnoInfo.getClazz());
            intent.putExtra(CommCL.COMM_ZC_INFO_FLD, zCnoInfo);
            menuView.startActivity(intent);
        }
    }
}
