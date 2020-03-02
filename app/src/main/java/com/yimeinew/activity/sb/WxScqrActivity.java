package com.yimeinew.activity.sb;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import butterknife.*;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.shade.com.alibaba.fastjson.JSONArray;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.shade.com.alibaba.fastjson.JSONObject;
import com.yimeinew.activity.R;
import com.yimeinew.activity.base.BaseActivity;
import com.yimeinew.activity.base.BaseApplication;
import com.yimeinew.adapter.tabledataadapter.BaseTableDataAdapter;
import com.yimeinew.modelInterface.CommBaseView;
import com.yimeinew.network.schedulers.SchedulerProvider;
import com.yimeinew.presenter.CommOtherPresenter;
import com.yimeinew.tableui.CommMultiChoiceModeCallBack;
import com.yimeinew.tableui.TablePanelView;
import com.yimeinew.tableui.entity.HeaderRowInfo;
import com.yimeinew.utils.CommCL;
import com.yimeinew.utils.CommonUtils;
import com.yimeinew.utils.DateUtil;
import com.yimeinew.utils.ICL;
import com.yimeinew.view.AuxText;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class WxScqrActivity extends BaseActivity implements CommBaseView {
    private static final String Title = "维修生产确认";
    private CommOtherPresenter commPresenter;
    private CommMultiChoiceModeCallBack commChoice;

    final int GETZCKEY = 1, GETZCWXYY = 2, GETSBINFO = 3;//keys

    //    @BindView(R.id.spinner_zc)
//    EditText zcSpinner;
    @BindView(R.id.edt_op)
    AuxText edtOP;
    @BindView(R.id.edt_equipment_no)
    EditText edtSbid;
    @BindView(R.id.scqr)
    Button scqr;
    //数据表格
    @BindView(R.id.table_view)
    TablePanelView tableView;
    @BindView(R.id.data_list_content)
    ListView dataListViewContent;
    List<JSONObject> dataList;
    BaseTableDataAdapter adapter;

    String zcno;
    String wx;
    String error1;
    JSONObject ngInfo = new JSONObject();
    JSONArray reasons;
    int num=0;//计算有几次结束维修
    private JSONArray jar;
    private HashMap<String, String> bh = new HashMap<>();//维修设备的编号，不是设备号

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wxqr_sc);
        this.setTitle(Title);
        ButterKnife.bind(this);
        /*主持人*/
        commPresenter = new CommOtherPresenter(this,SchedulerProvider.getInstance());
        //设备维修原因加载
        commPresenter.getAssistInfo(CommCL.AID_SBWXYY,"",6);
        edtOP.setOPAux();

    }

    /***
     * 初始化表格头数据，设备维修确认
     * @return 设备维修确认
     */
    public  List<HeaderRowInfo> getRowDataListSbwxqr() {
        List<HeaderRowInfo> rowList = new ArrayList<>();
        HeaderRowInfo sidColumn = new HeaderRowInfo("sbid", "设备号", 150);
        rowList.add(sidColumn);

        sidColumn = new HeaderRowInfo("sopr", "报修人", 150);
        HashMap<String, String> bxr=(HashMap<String, String>) CommCL.sharedPreferences.getAll();
        sidColumn.setAttr(3);
        sidColumn.setContrastMap(bxr);
        rowList.add(sidColumn);

        sidColumn = new HeaderRowInfo("wxstate", "维修状态", 120);
        sidColumn.setAttr(3);
        sidColumn.setContrastMap(CommCL.STATESbwx);
        sidColumn.setContrastColors(CommCL.STATEColorMap);
        rowList.add(sidColumn);

        sidColumn = new HeaderRowInfo("reason", "维修原因", 100);
        HashMap<String, String> hm = CommonUtils.JSONArrayToMap(jar, "tid", "qname");
        sidColumn.setAttr(3);
        sidColumn.setContrastMap(hm);
        rowList.add(sidColumn);

        sidColumn = new HeaderRowInfo("mkdate", "报修时间", 210);
        rowList.add(sidColumn);

        sidColumn = new HeaderRowInfo("sorg", "部门", 180);
        rowList.add(sidColumn);

        return rowList;
    }


    /***
     * 初始化表格
     */
    private void initTableView() {
        dataList = new ArrayList();
        List<HeaderRowInfo> header = getRowDataListSbwxqr();
        dataListViewContent.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        commChoice = new CommMultiChoiceModeCallBack(this, dataListViewContent, dataList);//添加选择器
        dataListViewContent.setMultiChoiceModeListener(commChoice);
        adapter = new BaseTableDataAdapter(this, tableView, dataListViewContent, dataList, header);
        adapter.setTitle("项次");
        adapter.setTitleHeight(100);
        adapter.setSwipeRefreshEnabled(false);
        adapter.setTitleWidth(80);
        tableView.setAdapter(adapter);
    }


    @OnEditorAction({R.id.edt_op, R.id.edt_equipment_no})
    public boolean OnEditorAction(EditText editText) {
        return onEditTextKeyDown(editText);
    }

    @Override
    public boolean onEditTextKeyDown(EditText editText) {
        int id = editText.getId();
        String operationUser = edtOP.getText().toString().toUpperCase();
        if (TextUtils.isEmpty(operationUser)) {
            showMessage("请输入操作员!");
            CommonUtils.textViewGetFocus(edtOP);
            return true;
        }
        String opv = CommCL.sharedPreferences.getString(operationUser, "");
        if (TextUtils.isEmpty(opv)) {
            showMessage("操作员【" + operationUser + "】不存在!");
            CommonUtils.textViewGetFocus(edtOP);
            return true;
        }
        if (id == R.id.edt_op) {
            CommonUtils.textViewGetFocus(edtSbid);
            return false;
        }
        String sbid = edtSbid.getText().toString().toUpperCase();
        if (TextUtils.isEmpty(sbid)) {
            showMessage("请输入设备号");
            CommonUtils.textViewGetFocus(edtSbid);
            return true;
        }
        if (id == R.id.edt_equipment_no) {
            //检验设备是否处于待维修
            String ws=GETZCWXYY+"";
            String time="2019-01-01";
            String cont="~wxstate='"+ws+"' and  mkdate>'"+time+"' and sbid='"+sbid+"'";
            commPresenter.getAssistInfo(CommCL.AID_SBWXQR2, cont, 3);


        }
        return true;
    }

    @OnClick(R.id.scqr)
    public void OnClick(View view) {
        int selectNum = 0;
        int id = view.getId();
        switch (id) {
            case R.id.scqr:
                selectNum = dataListViewContent.getCheckedItemCount();//选择条数
                if (selectNum >= 1) {
                    List<JSONObject> list = commChoice.getSelectList();//选择内容
                    List<Integer> selectIndex = commChoice.getSelectIndex();//选择所在列表的位置
                    for(int i=0;i<selectNum;i++){
                        JSONObject object = list.get(i);
                        String sid=object.getString("sid");
                        String wxstat=object.getString("wxstate");
                        String sorg=object.getString("sorg");
                        String sbid=object.getString("sbid");
                        String op =edtOP.getText().toString();
                        JSONObject info =new JSONObject();
                        info.put("schk2",op);
                        info.put("sid",sid);
                        info.put("qrtime",DateUtil.getCurrDateTime(ICL.DF_YMDT));
                        info.put("wxstate","3");
                        if(TextUtils.isEmpty(op)){
                            showError("请输入作业员");
                            return;
                        }

                        commPresenter.updateData(CommCL.CELL_ID_E6003,info,2);
                        JSONObject sbmanage =new JSONObject();
                        sbmanage.put("id",sbid);
                        sbmanage.put("sbstate","0");
                        commPresenter.updateData(CommCL.CELL_ID_B0003B,sbmanage,5);

                    }
                }else {
                    showMessage("请选择一条记录");
                }
                break;

        }


    }

    /***
     * 可选择菜单必须实现这个
     * 表格Item点击事件
     * @param parent
     * @param view
     * @param position
     * @param id
     */
    @OnItemClick(R.id.data_list_content)
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        switch (dataListViewContent.getChoiceMode()) {
            case ListView.CHOICE_MODE_NONE:
                return;
            case ListView.CHOICE_MODE_SINGLE:
                //单选
                boolean bSelect = view.isSelected();
                if (!bSelect)
                    dataListViewContent.setItemChecked(position, !bSelect);
                return;
            case ListView.CHOICE_MODE_MULTIPLE_MODAL:
            case ListView.CHOICE_MODE_MULTIPLE:
                bSelect = view.isSelected();
                dataListViewContent.setItemChecked(position, !bSelect);
        }
        boolean bSelect = view.isSelected();
        dataListViewContent.setItemChecked(position, !bSelect);
//        Toast.makeText(CommGJActivity.this, "你选中的position为：" + position, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void commCallBack(Boolean bok, JSONObject info, String error, int key) {

    }

    @Override
    public void commCallBack(Boolean bok, JSONObject info, String error, String key) {

    }

    @Override
    public void getAssistInfoBack(Boolean bok, JSONArray info, String error, int key) {

        if (bok) {
            if(key==3){
                adapter.clear();
            }else if(key==6){
                jar=info;
                initTableView();
                String ws=GETZCWXYY+"";
                String time="2019-01-01";
                String sorg= BaseApplication.currUser.getDeptCode();
                String sorgCont=(TextUtils.equals(sorg,"0"))?"":" and sorg='"+sorg+"'";
                String cont="~wxstate='"+ws+"' and  mkdate>'"+time+"'"+sorgCont;
                commPresenter.getAssistInfo(CommCL.AID_SBWXQR2, cont, GETZCKEY);
                return;
            }
            //放到扫描的列表中
            for (int i = 0; i < info.size(); i++) {
                JSONObject record = info.getJSONObject(i);
                // 添加到数据列表
                // adapter.addRecord(record);
                adapter.addRecord(CommonUtils.getJsonObjFromBean(record));

            }


        }
        if(!bok&&key==0){
            showError(error);
        }
    }

    @Override
    public void saveDataBack(Boolean bok, JSONArray info, JSONObject record, String error, int key) {
        if (bok) {
            myClear();
        } else {
            showMessage(error);
        }

    }

    public void myClear() {
        ngInfo = null;
        error1 = "";
        edtOP.setText("");
        edtSbid.setText("");


    }

    @Override
    public void updateDataBack(Boolean bok, JSONArray info, String error, int key) {
        if(bok){
            List<Integer> selectIndex = commChoice.selectIndex;
            //boolean bDone = CommCL.BATCH_STATUS_DONE.equals(error);

            num++;
            commChoice.clearChoice();
            adapter.notifyDataSetChanged();
            if(num==selectIndex.size()){
                adapter.clear();
                String ws=GETZCWXYY+"";
                String time="2019-01-01";
                String cont="~wxstate='"+ws+"' and  mkdate>'"+time+"'";
                commPresenter.getAssistInfo(CommCL.AID_SBWXQR2, cont, GETZCKEY);
                num=0;

            }





        }

    }

    @Override
    public void changeRecordStateBack(Boolean bok, JSONArray info, String error, int key) {

    }

    @Override
    public void onRemoteFailed(String message) {
        hideLoading();
        CommonUtils.showError(this, "onRemoteFailed=" + message);
    }

    /*---------------工具方法----------*/
    public void showError(String message) {
        CommonUtils.showError(this, message);
    }

    public void showMessage(String message) {
        CommonUtils.showMessage(this, message);
    }

    public void showSuccess(String message) {
        CommonUtils.showSuccess(this, message);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        Log.i(TAG_NAME,"销毁我了");
    }

    //退出当前Activity或者跳转到新Activity时被调用
    @Override
    protected void onStop() {
        super.onStop();
        //Log.i(TAG, "onStop called.");
    }

    //Activity被覆盖到下面或者锁屏时被调用
    @Override
    protected void onPause() {
        super.onPause();
        //Log.i(TAG, "onPause called.");
        //有可能在执行完onPause或onStop后,系统资源紧张将Activity杀死,所以有必要在此保存持久数据

    }
}
