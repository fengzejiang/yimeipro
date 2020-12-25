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
import com.yimeinew.activity.qc.*;
import com.yimeinew.data.ZCInfo;
import com.yimeinew.modelInterface.BaseView;
import com.yimeinew.presenter.CommStationPresenter;
import com.yimeinew.utils.CommCL;

import java.util.ArrayList;
import java.util.List;

/**
 * 通用工站适配器
 *
 * @Auther: fengzejiang1987@163.com
 * @Date : 2018/12/13 9:02
 */
public class QCCommStationAdapter extends BaseAdapter {
    private List<ZCInfo> zCnoInfos;
    private CommStationPresenter commStationPresenter;
    private Context context;
    private String sorg = BaseApplication.currUser.getDeptCode();//部门//05010000 器件//05040000模组

    public QCCommStationAdapter(Context context) {
        this.context = context;
        initZcInfo();
        commStationPresenter = new CommStationPresenter((BaseView) context);
    }

    private void initZcInfo() {
        zCnoInfos = new ArrayList<>();
        //首件检验
        ZCInfo gjzc = new ZCInfo();
        gjzc.setName("首件检验");
        gjzc.setImgIndex(R.drawable.shoujian);
        gjzc.setClazz(FirstInspectionActivity.class);
        //gjzc.setStartNum(2);
        int attr = 0;
        attr = gjzc.getAttr() | CommCL.ZC_ATTR_CHARGING;
        gjzc.setAttr(attr);
        zCnoInfos.add(gjzc);

        //巡检
        gjzc = new ZCInfo();
        gjzc.setName("巡检");
        gjzc.setImgIndex(R.drawable.xunjian);
        gjzc.setClazz(PatrolInspectionActivity.class);
        //gjzc.setStartNum(2);
        attr = 0;
        attr = gjzc.getAttr() | CommCL.ZC_ATTR_CHARGING;
        gjzc.setAttr(attr);
        zCnoInfos.add(gjzc);

        //抽检
        gjzc = new ZCInfo();
        gjzc.setName("抽检");
        gjzc.setImgIndex(R.drawable.mz_pz_spot);
        gjzc.setClazz(SpotCheckActivity.class);
        //gjzc.setStartNum(2);
        attr = 0;
        attr = gjzc.getAttr() | CommCL.ZC_ATTR_CHARGING;
        gjzc.setAttr(attr);
        zCnoInfos.add(gjzc);

        if(sorg.equals("05010000")||sorg.equals("0")){//器件
            //出货检验记录
            gjzc = new ZCInfo();
            gjzc.setName("出货检验");
            gjzc.setImgIndex(R.drawable.qc_chuhuo_jianyan);
            gjzc.setClazz(OutCheckQJActivity.class);
            //gjzc.setStartNum(2);
            attr = 0;
            attr = gjzc.getAttr() | CommCL.ZC_ATTR_CHARGING;
            gjzc.setAttr(attr);
            zCnoInfos.add(gjzc);
            //测试站检验记录
            gjzc = new ZCInfo();
            gjzc.setName("测试站检验记录");
            gjzc.setImgIndex(R.drawable.qj_csjyjl);
            gjzc.setClazz(CheckRecordActivity.class);
            //gjzc.setStartNum(2);
            attr = 0;
            attr = gjzc.getAttr() | CommCL.ZC_ATTR_CHARGING;
            gjzc.setAttr(attr);
            zCnoInfos.add(gjzc);
            //点胶烘烤放行
            gjzc = new ZCInfo();
            gjzc.setName("点胶烘烤放行");
            gjzc.setImgIndex(R.drawable.qj_pz_djhkfx);
            gjzc.setClazz(BakeReleaseActivity.class);
            //gjzc.setStartNum(2);
            attr = 0;
            attr = gjzc.getAttr() | CommCL.ZC_ATTR_CHARGING;
            gjzc.setAttr(attr);
            zCnoInfos.add(gjzc);

        }
        if(sorg.equals("05040000")||sorg.equals("0")){//模组

            //FQC检验--入库检验
            gjzc = new ZCInfo();
            gjzc.setName("入库检验");
            gjzc.setImgIndex(R.drawable.mz_rkjy);
            gjzc.setClazz(FQCCheckActivity.class);
            //gjzc.setStartNum(2);
            attr = 0;
            attr = gjzc.getAttr() | CommCL.ZC_ATTR_CHARGING;
            gjzc.setAttr(attr);
            zCnoInfos.add(gjzc);

            //出货检验
            gjzc = new ZCInfo();
            gjzc.setName("出货检验");
            gjzc.setImgIndex(R.drawable.qc_chuhuo_jianyan);
            gjzc.setClazz(OutCheckActivity.class);
            //gjzc.setStartNum(2);
            attr = 0;
            attr = gjzc.getAttr() | CommCL.ZC_ATTR_CHARGING;
            gjzc.setAttr(attr);
            zCnoInfos.add(gjzc);

            //品质维修确认OK
            gjzc = new ZCInfo();
            gjzc.setName("品质维修确认OK");
            gjzc.setBok("0");
            gjzc.setBokName("OK");
            gjzc.setSort("B");
            gjzc.setSbuid("D5084");
            gjzc.setImgIndex(R.drawable.mz_pzwxqrok);
            gjzc.setClazz(RepairActivity.class);
            //gjzc.setStartNum(2);
            attr = 0;
            attr = gjzc.getAttr() | CommCL.ZC_ATTR_CHARGING;
            gjzc.setAttr(attr);
            zCnoInfos.add(gjzc);

            //品质维修确认NG
            gjzc = new ZCInfo();
            gjzc.setName("品质维修确认NG");
            gjzc.setBok("1");
            gjzc.setBokName("NG");
            gjzc.setSort("B");
            gjzc.setSbuid("D5086");
            gjzc.setImgIndex(R.drawable.mz_pzwxqrng);
            gjzc.setClazz(RepairActivity.class);
            //gjzc.setStartNum(2);
            attr = 0;
            attr = gjzc.getAttr() | CommCL.ZC_ATTR_CHARGING;
            gjzc.setAttr(attr);
            zCnoInfos.add(gjzc);

            //直下式抽检
            gjzc = new ZCInfo();
            gjzc.setId("penma");
            gjzc.setName("直下式抽检");
            gjzc.setImgIndex(R.drawable.mz_pz_pm_spot);
            gjzc.setClazz(SpotCheckActivity.class);
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
