package com.yimeinew.adapter;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import com.yimeinew.activity.R;
import com.yimeinew.activity.base.BaseApplication;
import com.yimeinew.activity.databinding.MainCommStationItemBinding;
import com.yimeinew.activity.deviceproduction.commsub.FastActivity;
import com.yimeinew.activity.pack.PackActivity;
import com.yimeinew.data.ZCInfo;
import com.yimeinew.modelInterface.BaseView;
import com.yimeinew.presenter.CommStationPresenter;
import com.yimeinew.utils.CommCL;
import com.yimeinew.utils.CommonUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 包装管理适配器
 *
 * @Auther: fengzejiang1987@163.com
 * @Date : 2018/12/13 9:02
 */
public class PackStationAdapter extends BaseAdapter {
    private List<ZCInfo> zCnoInfos;
    private CommStationPresenter commStationPresenter;
    private Context context;

    public PackStationAdapter(Context context) {
        this.context = context;
        initZcInfo();
        commStationPresenter = new CommStationPresenter((BaseView) context);
    }

    private void initZcInfo() {

        zCnoInfos = new ArrayList<>();
        ZCInfo gjzc;
        String sorg= BaseApplication.currUser.getDeptCode();

        if(TextUtils.equals("05010000",sorg) || TextUtils.equals("0",sorg)) {//器件生产


        }
        if(TextUtils.equals("05040000",sorg)|| TextUtils.equals("0",sorg)){//模组生产
            //模组内装扫喷码
            gjzc = CommonUtils.getZCInfoById("S21");
            gjzc.setImgIndex(R.drawable.mz_bz_spm);
            gjzc.setClazz(PackActivity.class);
            //gjzc.setStartNum(2);
            int attr = 0;
            attr = gjzc.getAttr() | CommCL.ZC_ATTR_CHARGING;
            gjzc.setAttr(attr);

            System.out.println("部门："+sorg+gjzc.getName());
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
