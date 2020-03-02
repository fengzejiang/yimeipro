package com.yimeinew.adapter;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import com.yimeinew.activity.R;
import com.yimeinew.activity.base.BaseApplication;
import com.yimeinew.activity.databinding.MainCommStationItemBinding;
import com.yimeinew.activity.deviceproduction.commsub.FastActivity;
import com.yimeinew.activity.deviceproduction.commsub.FastMzActivity;
import com.yimeinew.activity.deviceproduction.commsub.RepairActivity;
import com.yimeinew.activity.deviceproduction.commsub.RepairBadActivity;
import com.yimeinew.data.ZCInfo;
import com.yimeinew.modelInterface.BaseView;
import com.yimeinew.presenter.CommStationPresenter;
import com.yimeinew.utils.CommCL;
import com.yimeinew.utils.CommonUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 通用工站适配器
 *
 * @Auther: fengzejiang1987@163.com
 * @Date : 2018/12/13 9:02
 */
public class RepairStationAdapter extends BaseAdapter {
    private List<ZCInfo> zCnoInfos;
    private CommStationPresenter commStationPresenter;
    private Context context;
    private String sorg = BaseApplication.currUser.getDeptCode();//部门//05010000 器件//05040000模组

    public RepairStationAdapter(Context context) {
        this.context = context;
        initZcInfo();
        commStationPresenter = new CommStationPresenter((BaseView) context);
    }

    private void initZcInfo() {
        zCnoInfos = new ArrayList<>();
        if(sorg.equals("05010000")||sorg.equals("0")){

        }
        if(sorg.equals("05040000")||sorg.equals("0")){
            //不良品送修
            ZCInfo gjzc = new ZCInfo();
            gjzc.setName("不良品送修-扫描");
            gjzc.setImgIndex(R.drawable.mz_blpsx);
            gjzc.setClazz(RepairBadActivity.class);
            //gjzc.setStartNum(2);
            int attr = 0;
            attr = gjzc.getAttr() | CommCL.ZC_ATTR_CHARGING;
            gjzc.setAttr(attr);
            zCnoInfos.add(gjzc);

            //生产维修确认OK
            gjzc = new ZCInfo();
            gjzc.setName("生产维修确认OK");
            gjzc.setBok("0");
            gjzc.setBokName("OK");
            gjzc.setSort("A");
            gjzc.setSbuid("D5080");
            gjzc.setImgIndex(R.drawable.mz_scqrok);
            gjzc.setClazz(RepairActivity.class);
            //gjzc.setStartNum(2);
            attr = 0;
            attr = gjzc.getAttr() | CommCL.ZC_ATTR_CHARGING;
            gjzc.setAttr(attr);
            zCnoInfos.add(gjzc);

            //生产维修确认NG
            gjzc = new ZCInfo();
            gjzc.setName("生产维修确认NG");
            gjzc.setBok("1");
            gjzc.setBokName("NG");
            gjzc.setSort("A");
            gjzc.setSbuid("D5082");
            gjzc.setImgIndex(R.drawable.mz_scqrng);
            gjzc.setClazz(RepairActivity.class);
            //gjzc.setStartNum(2);
            attr = 0;
            attr = gjzc.getAttr() | CommCL.ZC_ATTR_CHARGING;
            gjzc.setAttr(attr);
            zCnoInfos.add(gjzc);
        }





    }

    @Override
    public int getCount() {
        return zCnoInfos.size();
    }

    @Override
    public ZCInfo getItem(int position) {
        return zCnoInfos.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MainCommStationItemBinding mainCommStationItemBinding = DataBindingUtil.inflate(LayoutInflater.from(this.context), R.layout.main_comm_station_item, parent, false);
        mainCommStationItemBinding.setCommPresenter(commStationPresenter);
        mainCommStationItemBinding.setZcInf(getItem(position));
        mainCommStationItemBinding.executePendingBindings();//执行绑定
        return mainCommStationItemBinding.getRoot();
    }
}
