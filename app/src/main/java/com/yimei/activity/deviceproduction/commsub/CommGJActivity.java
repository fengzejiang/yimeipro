package com.yimei.activity.deviceproduction.commsub;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.*;
import android.view.Menu;
import android.widget.*;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnEditorAction;
import butterknife.OnItemClick;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.shade.com.alibaba.fastjson.JSON;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.shade.com.alibaba.fastjson.JSONArray;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.shade.com.alibaba.fastjson.JSONObject;
import com.yimei.activity.R;
import com.yimei.activity.base.BaseActivity;
import com.yimei.activity.databinding.ActivityCommGjBinding;
import com.yimei.adapter.tabledataadapter.BaseTableDataAdapter;
import com.yimei.data.*;
import com.yimei.modelInterface.BaseStationBindingView;
import com.yimei.network.schedulers.SchedulerProvider;
import com.yimei.presenter.CommStationZCPresenter;
import com.yimei.tableui.TablePanelView;
import com.yimei.tableui.entity.HeaderRowInfo;
import com.yimei.utils.CommCL;
import com.yimei.utils.CommonUtils;
import com.yimei.utils.DateUtil;
import com.yimei.utils.ICL;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @Auther: fengzejiang1987@163.com
 * @Date : 2018/12/13 11:07
 */
public class CommGJActivity extends BaseActivity implements BaseStationBindingView {

    private final String TAG_NAME = CommGJActivity.class.getSimpleName();

    private String currMONO = "";//当前工单号
    @BindView(R.id.edt_op)
    EditText edtOP;
    @BindView(R.id.edt_equipment_no)
    EditText edtSbid;
    @BindView(R.id.edt_box)
    EditText edtBox;
    @BindView(R.id.edt_sid1)
    EditText edtSid1;

    //数据表格
    @BindView(R.id.table_view)
    TablePanelView tableView;
    @BindView(R.id.data_list_content)
    ListView dataListViewContent;
    List<JSONObject> dataList;
    BaseTableDataAdapter adapter;


    private CommStationZCPresenter commPresenter;

    private GJViewEntity entity;
    private ZCInfo zCnoInfo;
    public static final String Title = "通用工站-->";

    private HashMap<String, String> bindBox = new HashMap<>();
    private HashMap<String, String> bindSid1 = new HashMap<>();
    private EquipmentInfo currEquipment;
    private CommMultiChoiceModeCallback commChoice;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        zCnoInfo = (ZCInfo) getIntent().getSerializableExtra(CommCL.COMM_ZC_INFO_FLD);
        entity = new GJViewEntity();
        entity.setZcno(zCnoInfo.getId());
        if ("11".equals(zCnoInfo.getId())) {
            entity.setGj1(true);
        }
        this.setTitle(Title + zCnoInfo.getName());
        ActivityCommGjBinding activityCommGjBinding = DataBindingUtil.setContentView(this, R.layout.activity_comm_gj);
        activityCommGjBinding.setZcInf(entity);
        ButterKnife.bind(this);

        registerReceiver(barcodeReceiver, new IntentFilter(
                CommCL.INTENT_ACTION_SCAN_RESULT)); // 注册广播
        initTableView();
        //生成假数据
//        initData();

        commPresenter = new CommStationZCPresenter(this, SchedulerProvider.getInstance());

    }

    /***
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


    /***
     * 初始化表格
     */
    private void initTableView() {
        dataList = new ArrayList();
        List<HeaderRowInfo> header = CommonUtils.getRowDataList();
        dataListViewContent.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        commChoice = new CommMultiChoiceModeCallback();
        dataListViewContent.setMultiChoiceModeListener(commChoice);
        adapter = new BaseTableDataAdapter(this, tableView, dataListViewContent, dataList, header);
        adapter.setTitle("项次");
        adapter.setTitleHeight(100);
        adapter.setSwipeRefreshEnabled(false);
        adapter.setTitleWidth(80);
        tableView.setAdapter(adapter);
    }

    /***
     * 初始化假数据
     */
    private void initData() {
        for (int i = 0; i < 20; i++) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("sid1", "MOA18120001_000" + (i + 1));
            jsonObject.put("slkid", "MOA18120001");
            jsonObject.put("prd_no", "F000007");
            jsonObject.put("qty", "4000.00");
            jsonObject.put("remark", "测试信息");
            dataList.add(jsonObject);
        }
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
//        Log.i(TAG_NAME,"销毁我了");
        unregisterReceiver(barcodeReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG_NAME,"onResume");
        if(currEquipment!=null){
            showLoading();
            commChoice.clearChoice();
            dataList.clear();
            adapter.notifyDataSetChanged();
            commPresenter.getRecordBySbId(currEquipment.getId(),zCnoInfo.getId());
        }
        registerReceiver(barcodeReceiver, new IntentFilter(
                CommCL.INTENT_ACTION_SCAN_RESULT)); // 注册广播
    }

    @OnEditorAction({R.id.edt_op, R.id.edt_equipment_no, R.id.edt_box, R.id.edt_sid1})
    public boolean OnEditorAction(EditText editText) {
        return onEditTextKeyDown(editText);
    }

    private boolean onEditTextKeyDown(EditText editText) {
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
        String sbId = edtSbid.getText().toString().toUpperCase();
        if (TextUtils.isEmpty(sbId)) {
            showMessage("请输入设备号");
            CommonUtils.textViewGetFocus(edtSbid);
            return true;
        }

        if (id == R.id.edt_equipment_no) {
            //校验设备
            showLoading();
            dataList.clear();
            adapter.notifyDataSetChanged();
            commPresenter.getEquipmentInfo(sbId, zCnoInfo.getId());
            return false;
        }

        if (entity.isGj1()) {
            String boxId = edtBox.getText().toString().toUpperCase();
            if (TextUtils.isEmpty(boxId)) {
                showMessage("请输入料盒号");
                CommonUtils.textViewGetFocus(edtBox);
                return true;
            }
            if (bindBox.containsKey(boxId)) {
                showMessage("该料盒号【" + boxId + "】已经绑定过");
                CommonUtils.textViewGetFocus(edtBox);
                return true;
            }
            if (id == R.id.edt_box) {
                //校验料盒号
                commPresenter.checkBoxExit(boxId,1);
            }
        }

        if (id == R.id.edt_sid1) {
            String sid = edtSid1.getText().toString().toUpperCase();
            if (TextUtils.isEmpty(sid)) {
                showMessage("请输入批次号");
                CommonUtils.textViewGetFocus(edtSid1);
                return true;
            }
            if (bindSid1.containsKey(sid)) {
                showMessage("该批次号【" + sid + "】已经扫描过");
                CommonUtils.textViewGetFocus(edtSid1);
                return true;
            }
            showLoading();
            //校验批次号
            commPresenter.getBatchInfo(sid, zCnoInfo.getId());
        }


        return false;
    }

    /***
     * 注册广播事件，监听PDA扫描
     */
    private BroadcastReceiver barcodeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (CommCL.INTENT_ACTION_SCAN_RESULT.equals(intent.getAction())) {
                View rootView = getCurrentFocus();//获取光标当前所在组件
                Object tag = rootView.findFocus().getTag();
                if (tag == null) {
                    return;
                }
                String barCodeData = null;
                if (intent.getStringExtra(CommCL.SCN_CUST_HONEY).equals(null)) {
                    barCodeData = intent.getStringExtra(CommCL.SCN_CUST_EX_SCODE);
                } else {
                    barCodeData = intent.getStringExtra(CommCL.SCN_CUST_HONEY);
                }
                barCodeData = barCodeData.toUpperCase();
                int id = rootView.getId();
                if (id == edtOP.getId()) {
                    edtOP.setText(barCodeData);
                    onEditTextKeyDown(edtOP);
                }
                if (id == edtSbid.getId()) {
                    edtSbid.setText(barCodeData);
                    onEditTextKeyDown(edtSbid);
                }
                if (id == edtBox.getId()) {
                    edtBox.setText(barCodeData);
                    onEditTextKeyDown(edtBox);
                }
                if (edtSid1.getTag().equals(tag)) {
                    edtSid1.setText(barCodeData);
                    onEditTextKeyDown(edtSid1);
                }
            }
        }
    };

    /***
     *
     * @param bok 是否存在该料盒号
     * @param error 错误信息
     */
    @Override
    public void checkMboxCallBack(boolean bok, String error,int key) {
        hideLoading();
        if (bok) {
            JSONObject data = JSON.parseObject(error);
            int state = data.getInteger("state");
            String box = data.getString("id");
            if(state == CommCL.BOX_STATE_WORKING){
                showMessage("该料盒【" + box + "】在使用中，请更换料盒或修改料盒使用状态！");
                CommonUtils.textViewGetFocus(edtBox);
                return;
            }else
                CommonUtils.textViewGetFocus(edtSid1);
        } else {
            showMessage(error);
            CommonUtils.textViewGetFocus(edtBox);
        }
    }

    @Override
    public void onRemoteFailed(String message) {
        hideLoading();
        CommonUtils.showError(this, message);
    }

    /***
     *
     * @param bok 是否存在
     * @param batchInfo 如果存在，返回JSON格式的数据
     * @param error 如果不存在返回错误信息
     */
    @Override
    public void checkSidCallBack(boolean bok, JSONObject batchInfo, String error) {
        if (bok) {
//            hideLoading();
            //生成生产记录
            Log.i(TAG_NAME, batchInfo.toJSONString());
            String op = edtOP.getText().toString().toUpperCase();//操作员
            String mBox = edtBox.getText().toString().toUpperCase();//料盒号
            String sbId = edtSbid.getText().toString().toUpperCase();//设备号
            String sid1 = edtSid1.getText().toString().toUpperCase();//批次号
            String mono = batchInfo.getString("sid");
            String zcno1 = batchInfo.getString("zcno1");
            String remark = batchInfo.getString("remark");
            int qty = batchInfo.getInteger("qty");
            String prd_no = batchInfo.getString("prd_no");
            String prd_name = batchInfo.getString("prd_name");
            int fircheck = batchInfo.getInteger("fircheck");
            MESPRecord record = new MESPRecord(sid1, mono, zCnoInfo.getId(), sbId);
            record.setMbox(mBox);
            record.setOp(op);
            record.setZcno1(zcno1);
            record.setRemark(remark);
            record.setQty(qty);
            record.setPrd_no(prd_no);
            record.setPrd_name(prd_name);
            record.setFirstchk(fircheck);
            commPresenter.makeProRecord(record);

        } else {
            hideLoading();
            showMessage(error);
            CommonUtils.textViewGetFocus(edtSid1);
        }

    }

    /***
     *
     * @param bok 是否存在
     * @param sbInfo 如果存在，返回设备信息，并跳转到料盒号输入框
     * @param error 如果不存在返回错误信息
     */
    @Override
    public void checkSbIdCallBack(boolean bok, EquipmentInfo sbInfo, String error) {
        if (bok) {
            this.currEquipment = sbInfo;
            CommonUtils.textViewGetFocus(edtBox);
            commPresenter.getRecordBySbId(sbInfo.getId(),sbInfo.getZcno());//获取设备上绑定的生产记录
        } else {
            hideLoading();
            showMessage(error);
            this.currEquipment = null;
            CommonUtils.textViewGetFocus(edtSbid);
        }
    }

    @Override
    public void checkRecordCallBack(boolean bok, JSONObject sbInfo, String error) {
        hideLoading();
        if (!bok) {
            showMessage(error);
            CommonUtils.textViewGetFocus(edtSbid);
        }
    }

    /***
     * 获取当前工单号
     * @return
     */
    @Override
    public String getCurrMO() {
        currMONO = "";
        if (dataList != null && dataList.size() > 0) {
            JSONObject jsonObject = dataList.get(0);
            currMONO = jsonObject.getString("slkid");
        }
        return currMONO;
    }

    @Override
    public void saveRecordBack(boolean bok, Object records, String error) {
        if (bok) {
            //放到扫描的列表中
            MESPRecord record = (MESPRecord) records;
            bindSid1.put(record.getSid1(), record.getSid1());
            // 添加到数据列表
            adapter.addRecord(CommonUtils.getJsonObjFromBean(record));
            //更改批次状态
            commPresenter.changeRecordStateOneByOne(record, CommCL.BATCH_STATUS_IN);

        } else {
            hideLoading();
            showMessage(error);
        }
    }

    /***
     *
     * @param bok
     * @param record2 如果处理成功，返回当前记录
     * @param error 如果失败，返回当前错误信息,如果成功，error返回时最新状态
     */
    @Override
    public void changeRecordStateBack(boolean bok, Object record2, String error) {
        if (bok) {
            hideLoading();
            int key = -1;
            MESPRecord record = (MESPRecord) record2;
            for (int i = 0; i < dataList.size(); i++) {
                MESPRecord record1 = JSONObject.parseObject(dataList.get(i).toJSONString(), MESPRecord.class);
                if (record1.getSid().equals(record.getSid())) {
                    key = i;
                    break;
                }
            }
            if (key == -1) {
                bindSid1.put(record.getSid1(), record.getSid1());
                adapter.addRecord(CommonUtils.getJsonObjFromBean(record));
            } else {
                dataList.set(key, CommonUtils.getJsonObjFromBean(record));
                adapter.notifyDataSetChanged();
            }
            if (entity.isGj1()) {
                bindBox.put(record.getMbox(), record.getMbox());
                commPresenter.bindingBox(record.getMbox(), record.getSid1(), zCnoInfo.getId());
                CommonUtils.textViewGetFocus(edtBox);
            } else {
                CommonUtils.textViewGetFocus(edtSid1);
            }

        } else {
            hideLoading();
            showMessage(error);
            CommonUtils.textViewGetFocus(edtSid1);
        }
    }

    /***
     * 批次更改状态返回结果
     * @param bok 是否成功
     * @param recordList 如果处理成功，返回当前记录
     * @param error 如果失败，返回当前错误信息，error返回时最新状态
     */
    @Override
    public void changeMultiRecordStateBack(boolean bok, List<MESPRecord> recordList, String error) {
        hideLoading();
        if(bok){
            List<Integer> selectIndex = commChoice.selectIndex;
            boolean bDone = CommCL.BATCH_STATUS_DONE.equals(error);
            if(!bDone){
                for(int i=0;i<selectIndex.size();i++){
                    dataList.set(selectIndex.get(i),CommonUtils.getJsonObjFromBean(recordList.get(i)));
                }
                commChoice.clearChoice();
                adapter.notifyDataSetChanged();
            }else{
                commChoice.clearChoice();
                dataList.clear();
                adapter.notifyDataSetChanged();
                commPresenter.getRecordBySbId(currEquipment.getId(),zCnoInfo.getId());
            }
        }else{
            showMessage(error);
        }
    }

    /***
     * 获取材料信息回来以后
     * @param bok 是否成功
     * @param recordList 获取成功,返回JSONARRAY
     * @param error 错误信息
     * @param type 类型（是哪个返回的）
     */
    @Override
    public void getMultiRecordBack(boolean bok, JSONArray recordList, String error, int type) {

    }

    @Override
    public void loadReasonsBack(boolean bok, Object recordList, String error) {

    }

    @Override
    public void loadCheckProjectBack(boolean bok, Object recordList, String error) {

    }

    @Override
    public void checkQCBatInfoBack(boolean bok, Object o, String error) {

    }

    @Override
    public void addRow(Object json) {
        if (json instanceof JSONObject) {
            JSONObject record = (JSONObject) json;
            adapter.addRecord(record);
        }
        if (json instanceof JSONArray) {
            JSONArray arr = (JSONArray) json;
            for (int i = 0; i < arr.size(); i++) {
                JSONObject jsonV = arr.getJSONObject(i);
                MESPRecord mespRecord = JSON.parseObject(jsonV.toJSONString(), MESPRecord.class);
                adapter.addRecord(CommonUtils.getJsonObjFromBean(mespRecord));
            }
        }
    }


    private class CommMultiChoiceModeCallback implements AbsListView.MultiChoiceModeListener {

        private View actionBarView;
        private TextView tv_selectedCount;

        public List<Integer> selectIndex = new ArrayList<>();
        String error = "";
        @Override
        public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
            int selectedCount = dataListViewContent.getCheckedItemCount();
            tv_selectedCount.setText(String.valueOf(selectedCount));
            ((ArrayAdapter) dataListViewContent.getAdapter()).notifyDataSetChanged();
        }


        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // menu
            getMenuInflater().inflate(R.menu.menu_multichoice, menu);
            // actionBar
            if (actionBarView == null) {
                actionBarView = LayoutInflater.from(CommGJActivity.this).inflate(R.layout.actionbar_listviewmultichoice, null);
                tv_selectedCount = actionBarView.findViewById(R.id.id_tv_selectedCount);
            }
            if ((zCnoInfo.getAttr() & CommCL.ZC_ATTR_CHARGING) == 0)
                menu.findItem(R.id.id_menu_charging).setVisible(false);
            if ((zCnoInfo.getAttr() & CommCL.ZC_ATTR_START) == 0)
                menu.findItem(R.id.id_menu_start).setVisible(false);
            if ((zCnoInfo.getAttr() & CommCL.ZC_ATTR_DONE) == 0)
                menu.findItem(R.id.id_menu_done).setVisible(false);
            if ((zCnoInfo.getAttr() & CommCL.ZC_ATTR_GLUING) == 0)
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
            String currstate = "";
            error = "";
            switch (item.getItemId()) {
                case R.id.id_menu_selectAll:
                    for (int i = 0; i < dataListViewContent.getAdapter().getCount(); i++) {
                        dataListViewContent.setItemChecked(i, true);
                    }
                    tv_selectedCount.setText(String.valueOf(dataListViewContent.getAdapter().getCount()));
                    break;
                case R.id.id_menu_cancel:
                    clearChoice();
                    break;
                case R.id.id_menu_charging:
                    selectNum = dataListViewContent.getCheckedItemCount();
                    String currOP = edtOP.getText().toString().toUpperCase();
                    if (selectNum == 1) {
                        //上料
                        List<MESPRecord> list = getSelectList();
                        MESPRecord record = list.get(0);
                        HashMap<String, Serializable> map = new HashMap<>();
                        map.put(CommCL.COMM_ZC_INFO_FLD,zCnoInfo);
                        map.put(CommCL.COMM_OP_FLD,currOP);
                        map.put(CommCL.COMM_RECORD_FLD,record);
                        map.put(CommCL.COMM_SBID_FLD,currEquipment.getId());
                        unregisterReceiver(barcodeReceiver);
                        jumpNextActivity(ChargingActivity.class,map);
                    } else {
                        showMessage("请选择一条记录，执行上料！");
                    }
                    break;
                case R.id.id_menu_start:
                    List<MESPRecord> startList = getSelectList();
                    //开工
                    if (!TextUtils.isEmpty(error)) {
                        showMessage(error+"，不能开工！！");
                    } else {
                        currstate = startList.get(0).getState1();
                        if (currstate.equals(CommCL.BATCH_STATUS_IN) || currstate.equals(CommCL.BATCH_STATUS_CHARGING)) {
                            //只有在01入站或者是02上料的状态才可以开工
                            if(zCnoInfo.getStartnum()>0){
                                int canStartNum = zCnoInfo.getStartnum()-getStartCount();
                                if(canStartNum<=0){
                                    showMessage("制成"+zCnoInfo.getId()+"最多可开工数为【"+zCnoInfo.getStartnum()+"】");
                                    break;
                                }
                                if(startList.size()>canStartNum){
                                    showMessage("制成"+zCnoInfo.getId()+"最多可开工数为【"+zCnoInfo.getStartnum()+"】还可以开工:【"+canStartNum+"】");
                                    break;
                                }
                            }
                            MESPRecord cr = startList.get(0);//获取其中的一条记录，判断是否有首件检验标志
                            if(cr.getFirstchk()==1){//需要首件检验
                                if(currEquipment.getFirstchk()==1 && currEquipment.getPrdno().equals(cr.getPrd_no())){
                                    showLoading();
                                    commPresenter.changeRecordStateBatch(startList,CommCL.BATCH_STATUS_WORKING);
                                    break;
                                }else{
                                    showMessage("设备："+currEquipment.getId()+"没有做首件检验！");
                                    break;
                                }
                            }else {
                                showLoading();
                                commPresenter.changeRecordStateBatch(startList,CommCL.BATCH_STATUS_WORKING);
                                break;
                            }
                        } else {
                            showMessage("选中记录的状态不是上料或者是入站，不可以开工");
                            break;
                        }
                    }
                    break;
                case R.id.id_menu_done:
                    //出站
                    List<MESPRecord> outList = getSelectList();
                    if (!TextUtils.isEmpty(error)) {
                        showMessage(error+"，不能出站！！");
                    } else {
                        currstate = outList.get(0).getState1();
                        if (CommCL.BATCH_STATUS_WORKING.equals(currstate)) {
                            //校验出站时间
                            boolean canout = true;
                            String err = "";
                            if(zCnoInfo.getPtime()>0){
                                for(int i=0;i<outList.size();i++){
                                    MESPRecord record = outList.get(i);
                                    String hpdate = record.getHpdate();
                                    int key = DateUtil.subDate(DateUtil.getCurrDateTime(ICL.DF_YMDT),hpdate,4);
                                    if(key<zCnoInfo.getPtime()&&key>0){
                                        canout = false;
                                        err = record.getSid1()+"已开工:"+key+"分钟，需要等待"+zCnoInfo.getPtime()+"分钟，不能出站！";
                                        break;
                                    }
                                }
                            }
                            if(err.length()>0){
                                showMessage(err);
                                return false;
                            }
                            //执行出站操作
                            if(canout){
                                showLoading();
                                commPresenter.changeRecordStateBatch(outList,CommCL.BATCH_STATUS_DONE);
                            }
                        }else{
                            showMessage("选中记录的状态不是生产中，不可以出站");
                            break;
                        }
                    }
                    break;
            }
            return true;
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

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            dataListViewContent.clearChoices();
        }

        public List<MESPRecord> getSelectList() {
            String currstate = "";
            List<MESPRecord> selectList = new ArrayList<>();
            //获取选中列表
            selectIndex.clear();
            for (int i = 0; i < dataList.size(); i++) {
                if (dataListViewContent.isItemChecked(i)) {
                    MESPRecord mespRecord = JSONObject.parseObject(dataList.get(i).toJSONString(), MESPRecord.class);
                    if (currstate.length() == 0) {
                        currstate = mespRecord.getState1();
                    }
                    if (currstate.equals(mespRecord.getState1())){//判断两个选中的状态是否一致
                        selectList.add(mespRecord);
                        selectIndex.add(i);
                    }
                    else {
                        error = ("选中的记录，状态不一致");
                        break;
                    }
                }
            }
            return selectList;
        }
    }




}
