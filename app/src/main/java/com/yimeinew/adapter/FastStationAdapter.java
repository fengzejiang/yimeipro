package com.yimeinew.adapter;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import com.yimeinew.activity.R;
import com.yimeinew.activity.databinding.MainCommStationItemBinding;
import com.yimeinew.activity.deviceproduction.LookBeltFastActivity;
import com.yimeinew.activity.deviceproduction.commsub.*;
import com.yimeinew.data.ZCInfo;
import com.yimeinew.modelInterface.BaseView;
import com.yimeinew.presenter.CommStationPresenter;
import com.yimeinew.utils.CommCL;
import com.yimeinew.utils.CommonUtils;
import com.yimeinew.activity.base.BaseApplication;

import java.util.ArrayList;
import java.util.List;

/**
 * 通用工站适配器
 *
 * @Auther: fengzejiang1987@163.com
 * @Date : 2018/12/13 9:02
 */
public class FastStationAdapter extends BaseAdapter {
    private List<ZCInfo> zCnoInfos;
    private CommStationPresenter commStationPresenter;
    private Context context;
    private String sorg = BaseApplication.currUser.getDeptCode();//部门//05010000 器件//05040000模组

    public FastStationAdapter(Context context) {
        this.context = context;
        initZcInfo();
        commStationPresenter = new CommStationPresenter((BaseView) context);
    }

    private void initZcInfo() {
        zCnoInfos = new ArrayList<>();
        if(sorg.equals("05010000")||sorg.equals("0")){
            //plasma清洗固晶
            ZCInfo gjzc = CommonUtils.getZCInfoById("211");
            gjzc.setImgIndex(R.drawable.qj_gjqx1);
            gjzc.setClazz(FastActivity.class);
//        gjzc.setStartNum(2);
            int attr = 0;
            attr = gjzc.getAttr() | CommCL.ZC_ATTR_CHARGING;
            gjzc.setAttr(attr);
            zCnoInfos.add(gjzc);

            //plasma清洗焊线
            gjzc = CommonUtils.getZCInfoById("1P");
            gjzc.setImgIndex(R.drawable.qj_hxqx1);
            gjzc.setClazz(FastActivity.class);
//        gjzc.setStartNum(2);
            attr = gjzc.getAttr() | CommCL.ZC_ATTR_CHARGING;
            gjzc.setAttr(attr);
            zCnoInfos.add(gjzc);

            //plasma清洗点胶
            gjzc = CommonUtils.getZCInfoById("313");
//        gjzc.setStartNum(2);
            gjzc.setImgIndex(R.drawable.qj_djqx1);
            gjzc.setClazz(FastActivity.class);
            attr = gjzc.getAttr() | CommCL.ZC_ATTR_CHARGING;
            gjzc.setAttr(attr);
            zCnoInfos.add(gjzc);

            //点胶支架预热除湿
            gjzc = CommonUtils.getZCInfoById("311");
//        gjzc.setStartNum(2);
            gjzc.setImgIndex(R.drawable.qj_djcs1);
            gjzc.setClazz(FastActivity.class);
            attr = gjzc.getAttr() | CommCL.ZC_ATTR_CHARGING;
            gjzc.setAttr(attr);
            zCnoInfos.add(gjzc);

            //离心
            gjzc = CommonUtils.getZCInfoById("31B");
//        gjzc.setStartNum(2);
            gjzc.setImgIndex(R.drawable.qj_djlx);
            gjzc.setClazz(FastActivity.class);
            attr = gjzc.getAttr() | CommCL.ZC_ATTR_CHARGING;
            gjzc.setAttr(attr);
            zCnoInfos.add(gjzc);

            //看带快速过站
            gjzc = CommonUtils.getZCInfoById("81");
//        gjzc.setStartNum(2);
            gjzc.setImgIndex(R.drawable.qj_kandai);
            gjzc.setClazz(LookBeltFastActivity.class);
            attr = gjzc.getAttr() | CommCL.ZC_ATTR_CHARGING;
            gjzc.setAttr(attr);
            zCnoInfos.add(gjzc);

        }
        if(sorg.equals("05040000")||sorg.equals("0")){
            //卡板
            ZCInfo gjzc = CommonUtils.getZCInfoById("S02");
            gjzc.setImgIndex(R.drawable.sc_kbgx);
            gjzc.setClazz(FastMzActivity.class);
//        gjzc.setStartNum(2);
            int attr = 0;
            attr = gjzc.getAttr() | CommCL.ZC_ATTR_CHARGING;
            gjzc.setAttr(attr);
            zCnoInfos.add(gjzc);
            //高温点亮
            gjzc = CommonUtils.getZCInfoById("S06");
            gjzc.setImgIndex(R.drawable.mz_gwdl);
            gjzc.setClazz(FastMzTYActivity.class);
//        gjzc.setStartNum(2);
            attr = 0;
            attr = gjzc.getAttr() | CommCL.ZC_ATTR_CHARGING;
            gjzc.setAttr(attr);
            zCnoInfos.add(gjzc);
            //外观
            gjzc = CommonUtils.getZCInfoById("S07");
            gjzc.setImgIndex(R.drawable.mz_gw);
            gjzc.setClazz(FastMzTYActivity.class);
//        gjzc.setStartNum(2);
            attr = 0;
            attr = gjzc.getAttr() | CommCL.ZC_ATTR_CHARGING;
            gjzc.setAttr(attr);
            zCnoInfos.add(gjzc);
            //贴背胶
            gjzc = CommonUtils.getZCInfoById("S08");
            gjzc.setImgIndex(R.drawable.mz_tbj);
            gjzc.setClazz(FastMzTYActivity.class);
//        gjzc.setStartNum(2);
            attr = 0;
            attr = gjzc.getAttr() | CommCL.ZC_ATTR_CHARGING;
            gjzc.setAttr(attr);
            zCnoInfos.add(gjzc);
            //低电流点亮
            gjzc = CommonUtils.getZCInfoById("S09");
            gjzc.setImgIndex(R.drawable.mz_ddldl);
            gjzc.setClazz(FastMzTYActivity.class);
//        gjzc.setStartNum(2);
            attr = 0;
            attr = gjzc.getAttr() | CommCL.ZC_ATTR_CHARGING;
            gjzc.setAttr(attr);
            zCnoInfos.add(gjzc);
            //点胶
            gjzc = CommonUtils.getZCInfoById("S12");
            gjzc.setImgIndex(R.drawable.mz_dj);
            gjzc.setClazz(FastMzTYActivity.class);
//        gjzc.setStartNum(2);
            attr = 0;
            attr = gjzc.getAttr() | CommCL.ZC_ATTR_CHARGING;
            gjzc.setAttr(attr);
            zCnoInfos.add(gjzc);
            //焊线
            gjzc = CommonUtils.getZCInfoById("S23");
            gjzc.setImgIndex(R.drawable.mz_hx);
            gjzc.setClazz(FastMzTYActivity.class);
//        gjzc.setStartNum(2);
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
