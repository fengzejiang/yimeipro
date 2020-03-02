package com.yimeinew.tableui;

import android.content.Context;
import android.view.*;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.yimeinew.activity.R;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.shade.com.alibaba.fastjson.JSONObject;
import com.yimeinew.activity.base.BaseActivity;
import com.yimeinew.activity.deviceproduction.LookBeltFastActivity;
import com.yimeinew.data.MESPRecord;
import com.yimeinew.utils.CommCL;

import java.util.ArrayList;
import java.util.List;

public class CommMultiChoiceModeCallBack  implements AbsListView.MultiChoiceModeListener{

    public View actionBarView;
    public TextView tv_selectedCount;
    public List<Integer> selectIndex = new ArrayList<>();
    public BaseActivity baseActivity;
    public ListView dataListViewContent;
    public List<JSONObject> dataList;

    public CommMultiChoiceModeCallBack(BaseActivity baseActivity, ListView dataListViewContent, List<JSONObject> dataList){
        this.dataListViewContent=dataListViewContent;
        this.dataList=dataList;
        this.baseActivity=baseActivity;
    }


    @Override
    public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
        int selectedCount = dataListViewContent.getCheckedItemCount();
        tv_selectedCount.setText(String.valueOf(selectedCount));
        ((ArrayAdapter) dataListViewContent.getAdapter()).notifyDataSetChanged();
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        baseActivity.getMenuInflater().inflate(R.menu.menu_multichoice, menu);
        // actionBar
        if (actionBarView == null) {
            actionBarView = LayoutInflater.from(baseActivity).inflate(R.layout.actionbar_listviewmultichoice, null);
            tv_selectedCount = actionBarView.findViewById(R.id.id_tv_selectedCount);
        }
        /*按钮隐藏*/
        menu.findItem(R.id.id_menu_charging).setVisible(false);
        menu.findItem(R.id.id_menu_start).setVisible(false);
        menu.findItem(R.id.id_menu_done).setVisible(false);
        menu.findItem(R.id.id_menu_gluing).setVisible(false);
        mode.setCustomView(actionBarView);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        int selectNum = 0;
        switch (item.getItemId()) {
            case R.id.id_menu_selectAll://全选
                for (int i = 0; i < dataListViewContent.getAdapter().getCount(); i++) {
                    dataListViewContent.setItemChecked(i, true);
                }
                tv_selectedCount.setText(String.valueOf(dataListViewContent.getAdapter().getCount()));
                break;
            case R.id.id_menu_cancel://取消
                clearChoice();
                break;
        }
        return true;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        dataListViewContent.clearChoices();
    }
    public List<JSONObject> getSelectList() {
        List<JSONObject> selectList = new ArrayList<>();
        //获取选中列表
        selectIndex.clear();
        for (int i = 0; i < dataList.size(); i++) {
            if (dataListViewContent.isItemChecked(i)) {
                selectList.add(dataList.get(i));
                selectIndex.add(i);
            }
        }
        return selectList;
    }
    public List<Integer> getSelectIndex() {
        //获取选中列表
        selectIndex.clear();
        for (int i = 0; i < dataList.size(); i++) {
            if (dataListViewContent.isItemChecked(i)) {
                selectIndex.add(i);
            }
        }
        return selectIndex;
    }
    /***
     * 获取正在生产的数量
     * @return
     */
    private int getStartCount() {
        int count=0;
        for(int i=0;i<dataList.size();i++){
            MESPRecord record = JSONObject.parseObject(dataList.get(i).toJSONString(),MESPRecord.class);
            if(CommCL.BATCH_STATUS_WORKING.equals(record.getState1())){
                count++;
            }
        }
        return count;
    }

    public void clearChoice() {
        for (int i = 0; i < dataListViewContent.getAdapter().getCount(); i++) {
            if (dataListViewContent.isItemChecked(i))
                dataListViewContent.setItemChecked(i, false);
        }
    }

}
