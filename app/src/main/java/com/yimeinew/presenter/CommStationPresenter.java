package com.yimeinew.presenter;

import android.content.Intent;
import com.yimeinew.activity.deviceproduction.CommonStationActivity;
import com.yimeinew.data.ZCInfo;
import com.yimeinew.modelInterface.BaseView;
import com.yimeinew.utils.CommCL;

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
