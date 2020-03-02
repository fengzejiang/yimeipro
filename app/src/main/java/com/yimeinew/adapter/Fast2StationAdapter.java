package com.yimeinew.adapter;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import com.yimeinew.activity.R;
import com.yimeinew.activity.databinding.MainCommStationItemBinding;
import com.yimeinew.activity.deviceproduction.commsub.Fast2Activity;
import com.yimeinew.activity.deviceproduction.commsub.FastActivity;
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
public class Fast2StationAdapter extends BaseAdapter {
    private List<ZCInfo> zCnoInfos;
    private CommStationPresenter commStationPresenter;
    private Context context;

    public Fast2StationAdapter(Context context) {
        this.context = context;
        initZcInfo();
        commStationPresenter = new CommStationPresenter((BaseView) context);
    }

    private void initZcInfo() {

        zCnoInfos = new ArrayList<>();
        //plasma清洗焊线
        ZCInfo gjzc = CommonUtils.getZCInfoById("1P");
        gjzc.setImgIndex(R.drawable.qj_hxqx2);
        gjzc.setClazz(Fast2Activity.class);
//        gjzc.setStartNum(2);
        int attr = 0;
        attr = gjzc.getAttr() | CommCL.ZC_ATTR_CHARGING;
        gjzc.setAttr(attr);
        zCnoInfos.add(gjzc);




        //plasma清洗点胶
        gjzc = CommonUtils.getZCInfoById("313");
//        gjzc.setStartNum(2);
        gjzc.setImgIndex(R.drawable.qj_djqx2);
        gjzc.setClazz(Fast2Activity.class);
        attr = gjzc.getAttr() | CommCL.ZC_ATTR_CHARGING;
        gjzc.setAttr(attr);
        zCnoInfos.add(gjzc);

        //点胶支架预热除湿
        gjzc = CommonUtils.getZCInfoById("311");
//        gjzc.setStartNum(2);
        gjzc.setImgIndex(R.drawable.qj_djcs2);
        gjzc.setClazz(Fast2Activity.class);
        attr = gjzc.getAttr() | CommCL.ZC_ATTR_CHARGING;
        gjzc.setAttr(attr);
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
