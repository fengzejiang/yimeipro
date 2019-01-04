package com.yimei.adapter;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import com.yimei.activity.R;
import com.yimei.activity.databinding.MainCommStationItemBinding;
import com.yimei.activity.deviceproduction.commsub.CommGJActivity;
import com.yimei.activity.deviceproduction.commsub.GlueAndWeldingActivity;
import com.yimei.data.ZCInfo;
import com.yimei.modelInterface.BaseView;
import com.yimei.presenter.CommStationPresenter;
import com.yimei.utils.CommCL;
import com.yimei.utils.CommonUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 通用工站适配器
 *
 * @Auther: fengzejiang1987@163.com
 * @Date : 2018/12/13 9:02
 */
public class CommStationAdapter extends BaseAdapter {
    private List<ZCInfo> zCnoInfos;
    private CommStationPresenter commStationPresenter;
    private Context context;

    public CommStationAdapter(Context context) {
        this.context = context;
        initZcInfo();
        commStationPresenter = new CommStationPresenter((BaseView) context);
    }

    private void initZcInfo() {

        zCnoInfos = new ArrayList<>();
//        ZCInfo gjzc = new ZCInfo("11","固晶1");
        ZCInfo gjzc = CommonUtils.getZCInfoById("11");
        gjzc.setImgIndex(R.drawable.qj_gj1);
        gjzc.setClazz(CommGJActivity.class);
//        gjzc.setStartNum(2);
        int attr = 0;
        attr = gjzc.getAttr() | CommCL.ZC_ATTR_CHARGING;
        gjzc.setAttr(attr);
        zCnoInfos.add(gjzc);
        gjzc = CommonUtils.getZCInfoById("12");
//        gjzc.setStartNum(2);
        gjzc.setImgIndex(R.drawable.qj_gj2);
        gjzc.setClazz(CommGJActivity.class);
        attr = gjzc.getAttr() | CommCL.ZC_ATTR_CHARGING;
        gjzc.setAttr(attr);
        zCnoInfos.add(gjzc);
        gjzc = CommonUtils.getZCInfoById("13");
        gjzc.setImgIndex(R.drawable.qj_gj3);
//        gjzc.setStartNum(2);
        gjzc.setClazz(CommGJActivity.class);
        attr = gjzc.getAttr() | CommCL.ZC_ATTR_CHARGING;
        gjzc.setAttr(attr);
        zCnoInfos.add(gjzc);
        gjzc = CommonUtils.getZCInfoById("1A");
        gjzc.setImgIndex(R.drawable.qj_gj_hk1);
        gjzc.setClazz(CommGJActivity.class);
        zCnoInfos.add(gjzc);
        gjzc = CommonUtils.getZCInfoById("1B");
        gjzc.setImgIndex(R.drawable.qj_gj_hk2);
        gjzc.setClazz(CommGJActivity.class);
        zCnoInfos.add(gjzc);
        gjzc = CommonUtils.getZCInfoById("21");
//        gjzc.setStartNum(1);
        gjzc.setImgIndex(R.drawable.qj_hj);
        attr = gjzc.getAttr() | CommCL.ZC_ATTR_CHARGING;
        gjzc.setAttr(attr);
        gjzc.setClazz(GlueAndWeldingActivity.class);
        zCnoInfos.add(gjzc);
        gjzc = CommonUtils.getZCInfoById("31");
//        gjzc.setStartNum(1);
        gjzc.setImgIndex(R.drawable.qj_dianjiao);
        attr = gjzc.getAttr() | CommCL.ZC_ATTR_GLUING;
        gjzc.setAttr(attr);
        gjzc.setClazz(GlueAndWeldingActivity.class);
        zCnoInfos.add(gjzc);
        gjzc = CommonUtils.getZCInfoById("41");
        gjzc.setImgIndex(R.drawable.qj_dianjiao_hk);
        gjzc.setClazz(CommGJActivity.class);
        zCnoInfos.add(gjzc);
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
