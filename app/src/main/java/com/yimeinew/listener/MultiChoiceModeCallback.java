package com.yimeinew.listener;

import android.content.Context;
import android.util.Log;
import android.view.*;
import android.widget.*;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.shade.com.alibaba.fastjson.JSONObject;
import com.yimeinew.activity.R;
import com.yimeinew.activity.base.BaseActivity;
import com.yimeinew.utils.CommonUtils;

import java.util.List;

/**
 * @Auther: fengzejiang1987@163.com
 * @Date : 2018/12/18 15:49
 */
public class MultiChoiceModeCallback implements AbsListView.MultiChoiceModeListener {
    public static final String TAG_NAME = MultiChoiceModeCallback.class.getSimpleName();
    private BaseActivity baseActivity;
    private ListView dataListViewContent;
    private List<JSONObject> dataList;
    private View actionBarView;
    private TextView tv_selectedCount;

    public MultiChoiceModeCallback(Context context,ListView listView,List<JSONObject> data){
        this.baseActivity = (BaseActivity) context;
        this.dataListViewContent = listView;
        this.dataList = data;
    }

    /**
     * 进入ActionMode时调用
     * 可设置一些菜单
     *
     * @param mode
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        // menu
        baseActivity.getMenuInflater().inflate(R.menu.menu_multichoice, menu);
        // actionBar
        if (actionBarView == null) {
            actionBarView = LayoutInflater.from(baseActivity).inflate(R.layout.actionbar_listviewmultichoice, null);
            tv_selectedCount = actionBarView.findViewById(R.id.id_tv_selectedCount);
            MenuItem item = menu.findItem(R.id.id_menu_charging);
//            item.setVisible(false);
        }
        mode.setCustomView(actionBarView);
        return true;
    }

    /**
     * 和onCreateActionMode差不多的时机调用，不写逻辑
     *
     * @param mode
     * @param menu
     * @return
     */
    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    /**
     * 当ActionMode的菜单项被点击时
     *
     * @param mode
     * @param item
     * @return
     */
    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.id_menu_selectAll:
                for (int i = 0; i < dataListViewContent.getAdapter().getCount(); i++) {
                    dataListViewContent.setItemChecked(i, true);
                }
                tv_selectedCount.setText(String.valueOf(dataListViewContent.getAdapter().getCount()));
                break;
            case R.id.id_menu_cancel:
                dataListViewContent.clearChoices();
                tv_selectedCount.setText(String.valueOf(0));
                ((ArrayAdapter) dataListViewContent.getAdapter()).notifyDataSetChanged();
                Log.i(TAG_NAME,"取消选择");
                break;
            case R.id.id_menu_charging:
                int selectNum = dataListViewContent.getCheckedItemCount();
                if(selectNum==1){
                    //上料
                }else{
                    CommonUtils.showMessage(baseActivity,"请选择一条记录，执行上料！");
                }
                break;
        }
        return true;
    }

    /**
     * 退出ActionMode时调用
     *
     * @param mode
     */
    @Override
    public void onDestroyActionMode(ActionMode mode) {
        dataListViewContent.clearChoices();
    }

    /**
     * 当item的选中状态发生改变时调用
     *
     * @param mode
     * @param position
     * @param id
     * @param checked
     */
    @Override
    public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
        int selectedCount = dataListViewContent.getCheckedItemCount();
        tv_selectedCount.setText(String.valueOf(selectedCount));
        ((ArrayAdapter) dataListViewContent.getAdapter()).notifyDataSetChanged();
    }
}
