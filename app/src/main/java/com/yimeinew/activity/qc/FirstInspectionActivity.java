package com.yimeinew.activity.qc;

import android.app.AlertDialog;
import android.content.*;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.*;
import butterknife.*;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.shade.com.alibaba.fastjson.JSONArray;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.shade.com.alibaba.fastjson.JSONObject;
import com.yimeinew.activity.R;
import com.yimeinew.activity.base.BaseActivity;
import com.yimeinew.activity.base.BaseApplication;
import com.yimeinew.adapter.qc.QCTableDataAdapter;
import com.yimeinew.data.*;
import com.yimeinew.data.qc.MESQCRecord;
import com.yimeinew.data.qc.QCBatchInfo;
import com.yimeinew.modelInterface.BaseStationBindingView;
import com.yimeinew.network.schedulers.SchedulerProvider;
import com.yimeinew.presenter.CommStationZCPresenter;
import com.yimeinew.tableui.TablePanelView;
import com.yimeinew.tableui.entity.HeaderRowInfo;
import com.yimeinew.utils.CommCL;
import com.yimeinew.utils.CommonUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 首件检验处理
 *
 * @Auther: fengzejiang1987@163.com
 * @Date : 2018/12/26 18:45
 */
public class FirstInspectionActivity extends BaseActivity implements BaseStationBindingView {
    private static final String TAG_NAME = FirstInspectionActivity.class.getSimpleName();
    @BindView(R.id.sp_zc_list)
    Spinner zcSpinner;//制成list
    @BindView(R.id.sp_cause)
    Spinner zcCause;//发起原因list

    @BindView(R.id.sw_result)
    Switch okNg;

    @BindView(R.id.btn_save)
    Button btn_save;

    @BindView(R.id.edt_op)
    EditText edtOP;
    @BindView(R.id.edt_equipment_no)
    EditText edtSbId;
    @BindView(R.id.edt_sid1)
    EditText edtSid1;
    @BindView(R.id.edt_prd_no)
    EditText edtPrdName;

    ArrayList<ZCInfo> zcInfoList = BaseApplication.zcList;
    CommStationZCPresenter commPresenter;
    CheckReason currCheckReason;
    private ZCInfo zcInfo;
    private EquipmentInfo currSbInfo;
    private QCBatchInfo qcBatchInfo;//扫描批次后获取到的记录

    ArrayAdapter<String> zcAdapter;
    ArrayAdapter<CheckReason> causeAdapter;
    private List<String> zcAdapterData = new ArrayList<>();
    private List<CheckReason> causeAdapterData = new ArrayList<>();
    private List<CheckProjectInfo> checkProjectList = new ArrayList<>();

    //初始化表格
    //数据表格
    @BindView(R.id.table_view)
    TablePanelView tableView;
    @BindView(R.id.data_list_content)
    ListView dataListViewContent;
    List<JSONObject> dataList;
    QCTableDataAdapter tableAdapter;

    private EquipmentInfo infoOther;
    private MESPRecord recordOther;

    private String currOP;

    private MESQCRecord currRecord;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qc_first_inspection);
        ButterKnife.bind(this);
        registerReceiver(barcodeReceiver,new IntentFilter(
                CommCL.INTENT_ACTION_SCAN_RESULT));
        zcAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, zcAdapterData);
        zcSpinner.setAdapter(zcAdapter);
        getZCAdapterData();
        causeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, causeAdapterData);
        zcCause.setAdapter(causeAdapter);
        initCheck(okNg.isChecked());
        commPresenter = new CommStationZCPresenter(this, SchedulerProvider.getInstance());
        initTableView();
        //从Intent中获取数据
        ZCInfo zcInfo1 = (ZCInfo) getIntent().getSerializableExtra(CommCL.COMM_ZC_INFO_FLD);//制成信息
        String op = (String) getIntent().getSerializableExtra(CommCL.COMM_OP_FLD);//操作员
        infoOther = (EquipmentInfo) getIntent().getSerializableExtra(CommCL.COMM_SBID_FLD);//设备
        recordOther = (MESPRecord) getIntent().getSerializableExtra(CommCL.COMM_RECORD_FLD);//生产记录
        if(!TextUtils.isEmpty(op)){
            edtOP.setText(op);
        }
        if (zcInfoList != null) {
            showLoading();
            zcInfo = zcInfo1==null?zcInfoList.get(0):zcInfo1;
            int key = 0;
            if(zcInfo1!=null){
                for(int i=0;i<zcInfoList.size();i++){
                    if(zcInfoList.get(i).getId().equals(zcInfo.getId())){
                        key = i;
                        break;
                    }
                }
            }
            zcSpinner.setSelection(key);
            commPresenter.getLaunchingReasons(zcInfo.getId());
        }

        if(infoOther!=null){
            edtSbId.setText(infoOther.getId());
            currSbInfo = infoOther;
        }

    }


    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(barcodeReceiver,new IntentFilter(
                CommCL.INTENT_ACTION_SCAN_RESULT));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(barcodeReceiver);
    }

    /***
     * 初始化表格
     */
    private void initTableView() {
        dataList = new ArrayList();
        List<HeaderRowInfo> header = getRowDataList();
        dataListViewContent.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        tableAdapter = new QCTableDataAdapter(this, tableView, dataListViewContent, dataList, header);
        tableAdapter.setTitle("项次");
        tableAdapter.setTitleHeight(100);
        tableAdapter.setSwipeRefreshEnabled(false);
        tableAdapter.setTitleWidth(100);
        tableView.setAdapter(tableAdapter);
    }

    private List<HeaderRowInfo> getRowDataList() {
        List<HeaderRowInfo> rowList = new ArrayList<>();
        HeaderRowInfo sidColumn = new HeaderRowInfo("xmbm", "编码", 100);
        rowList.add(sidColumn);
        sidColumn = new HeaderRowInfo("xmmc", "项目名称", 220);
        rowList.add(sidColumn);
        sidColumn = new HeaderRowInfo("bok", "判定结果", 150);
        rowList.add(sidColumn);
        sidColumn = new HeaderRowInfo("oper", "操作", 130);
        rowList.add(sidColumn);
        return rowList;
    }

    private void getZCAdapterData() {
        if (zcInfoList == null) {
            return;
        }
        for (int i = 0; i < zcInfoList.size(); i++) {
            ZCInfo zc = zcInfoList.get(i);
            zcAdapterData.add(zc.getName());
        }
        zcAdapter.notifyDataSetChanged();
    }

    /***
     * 判定结果，更改事件
     * @param compoundButton
     * @param b
     */
    @OnCheckedChanged(R.id.sw_result)
    void SetSwitchColor(CompoundButton compoundButton, boolean b) {
        initCheck(b);
    }

    /***
     * 界面保存按钮点击事件
     * @param btn
     */
    @OnClick(R.id.btn_save)
    void SaveData(Button btn) {
        if (zcInfo == null) {
            showMessage("没有制程信息！");
            return;
        }
        if (currCheckReason == null) {
            showMessage("没有发起原因！");
            return;
        }
        if (dataList.size() == 0) {
            showMessage("没有检验项目！");
            return;
        }
        if (currSbInfo == null) {
            showMessage("请输入设备信息！");
            CommonUtils.textViewGetFocus(edtSbId);
            return;
        }
        if (qcBatchInfo == null) {
            showMessage("请输入生产批次！");
            CommonUtils.textViewGetFocus(edtSid1);
            return;
        }
        boolean bok = true;
        for (int i = 0; i < dataList.size(); i++) {
            JSONObject jsonObject = dataList.get(i);
            String ok = jsonObject.getString("bok");
            if (ok.equals("NG")) {
                bok = false;
                break;
            }
            Log.i(TAG_NAME, dataList.get(i).toJSONString());
        }
        if (!bok && okNg.isChecked()) {
            okNg.setChecked(bok);
            initCheck(bok);
        }
        if (!okNg.isChecked()) {
            bok = false;
        }
        currRecord = new MESQCRecord();
        currRecord.setBok(bok ? 0 : 1);
        currRecord.setCaused(currCheckReason.getId());
        currRecord.setChtype(CommCL.COMM_CHECK_FIRST);
        currRecord.setPrd_no(qcBatchInfo.getPrd_no());
        currRecord.setPrd_name(qcBatchInfo.getPrd_name());
        currRecord.setSid1(qcBatchInfo.getSid1());
        currRecord.setSlkid(qcBatchInfo.getSid());
        currRecord.setLotno(qcBatchInfo.getLotno());
        currRecord.setQty(qcBatchInfo.getQty());
        currRecord.setOp(currOP);
        currRecord.setOp_c(currOP);
        currRecord.setSbid(currSbInfo.getId());
        currRecord.setZcno(zcInfo.getId());
        currRecord.setState(0);
        currRecord.setState1("07");
        JSONObject saveJ = CommonUtils.getJsonObjFromBean(currRecord);
        saveJ.put("Q00101A", dataList);
        showLoading();
        commPresenter.saveRecord(saveJ, CommCL.CELL_ID_Q00101);


    }

    /***
     * 初始化开关界面样式
     * @param checked
     */
    void initCheck(boolean checked) {
        if (checked) {
            okNg.setSwitchTextAppearance(this, R.style.s_true);
        } else {
            okNg.setSwitchTextAppearance(this, R.style.s_false);
        }
    }

    /***
     * 制程下拉列表，发起原因选中项切换事件
     * @param parent
     * @param view
     * @param position
     * @param id1
     */
    @OnItemSelected({R.id.sp_zc_list, R.id.sp_cause})
    void OnItemSelected(AdapterView<?> parent, View view, int position, long id1) {
        int id = parent.getId();
        if (id == R.id.sp_zc_list) {
            zcInfo = zcInfoList.get(position);
            showLoading();
            commPresenter.getLaunchingReasons(zcInfo.getId());
        }
        if (id == R.id.sp_cause) {
            currCheckReason = causeAdapterData.get(position);
        }
    }

    @OnEditorAction({R.id.edt_sid1, R.id.edt_op, R.id.edt_equipment_no})
    public boolean OnEditorAction(EditText editText) {
        //无论哪个输入框的输入完成，首先判断操作员输入框是否有值，如果有值，则继续往下
        //如果操作员输入框没有值,则跳转到操作员输入框，
        int key = editText.getId();
        currOP = edtOP.getText().toString().toUpperCase();
        String opv = CommCL.sharedPreferences.getString(currOP, "");
        if (TextUtils.isEmpty(opv)) {
            showMessage("操作员不存在！【" + currOP + "】");
            CommonUtils.textViewGetFocus(edtOP);
            return false;
        }
        if (key == R.id.edt_op) {
            CommonUtils.textViewGetFocus(edtSbId);
            return true;
        }

        String eqv = edtSbId.getText().toString().toUpperCase();
        if (TextUtils.isEmpty(eqv)) {
            showMessage("设备号不能为空！");
            CommonUtils.textViewGetFocus(edtSbId);
            return false;
        }
        if (dataList.size() == 0) {
            showMessage("当前制程：" + zcInfo.getName() + "没有检验项目");
            return false;
        }
        if (key == R.id.edt_equipment_no) {
            if (currSbInfo == null) {
                showLoading();
                commPresenter.getEquipmentInfo(eqv, zcInfo.getId());
                return false;
            } else {
                if (currSbInfo.getId().equals(eqv)) {
                    CommonUtils.textViewGetFocus(edtSid1);
                    return true;
                }
                showLoading();
                commPresenter.getEquipmentInfo(eqv, zcInfo.getId());
                return false;
            }
        }
        String sid1 = edtSid1.getText().toString().toUpperCase();
        if (TextUtils.isEmpty(sid1)) {
            showMessage("批次号不能为空!!");
            return false;
        } else {
            showLoading();
            boolean bhd = CommCL.houDuan.containsKey(zcInfo.getId());
            commPresenter.getQCBatchInfo(sid1, zcInfo.getId(), bhd);
            return true;
        }
    }

    @Override
    public void checkMboxCallBack(boolean bok, String error,int key) {

    }

    @Override
    public void checkSidCallBack(boolean bok, JSONObject batchInfo, String error) {

    }

    @Override
    public void checkSbIdCallBack(boolean bok, EquipmentInfo sbInfo, String error) {
        hideLoading();
        if (bok) {
            currSbInfo = sbInfo;
            CommonUtils.textViewGetFocus(edtSid1);
        } else {
            CommonUtils.textViewGetFocus(edtSbId);
            showMessage(error);
        }
    }

    @Override
    public void checkRecordCallBack(boolean bok, JSONObject sbInfo, String error) {

    }

    @Override
    public String getCurrMO() {
        return null;
    }

    /***
     *
     * @param bok  是否成功
     * @param record 返回null
     * @param error 保存失败，返回错误信息,保存成功，返回sid
     */
    @Override
    public void saveRecordBack(boolean bok, Object record, String error) {
        if (bok) {
            currRecord.setSid(error);
            currRecord.setSys_stated(0);
            CeaPars ceaPars = new CeaPars();
            ceaPars.setSid(currRecord.getSid());
            ceaPars.setSbuid(currRecord.getSbuid());
            ceaPars.setStatefr(currRecord.getState());
            ceaPars.setStateto(currRecord.getState());
            commPresenter.getApprovalInfo(ceaPars);
        } else {
            hideLoading();
            showMessage(error);
        }
    }

    @Override
    public void changeRecordStateBack(boolean bok, Object record, String error) {
    }

    @Override
    public void changeMultiRecordStateBack(boolean bok, List<MESPRecord> recordList, String error) {

    }

    @Override
    public void getMultiRecordBack(boolean bok, JSONArray recordList, String error, int type) {

    }

    /***
     * 获取发起原因回调事件
     * @param bok 是否成功
     * @param recordList 成功返回JsonArray,失败返回null
     * @param error 错误信息
     */
    @Override
    public void loadReasonsBack(boolean bok, Object recordList, String error) {
        if (bok) {
            JSONArray array = (JSONArray) recordList;
            causeAdapterData.clear();
            for (int i = 0; i < array.size(); i++) {
                CheckReason checkReason = JSONObject.parseObject(array.getJSONObject(i).toJSONString(), CheckReason.class);
                causeAdapterData.add(checkReason);
            }
            causeAdapter.notifyDataSetChanged();
            currCheckReason = causeAdapterData.get(0);
//            Log.d(TAG_NAME,array.toJSONString());
            dataList.clear();
            tableAdapter.notifyDataSetChanged();
            commPresenter.getCheckProject(zcInfo.getId(), true);
        } else {
            currCheckReason = null;
            causeAdapterData.clear();
            causeAdapter.notifyDataSetChanged();
            dataList.clear();
            tableAdapter.notifyDataSetChanged();
            hideLoading();
            showMessage(error);
        }
    }

    /***
     * 获取检验项目回调事件
     * @param bok 是否成功
     * @param recordList 成功返回JsonArray,失败返回null
     * @param error 错误信息
     */
    @Override
    public void loadCheckProjectBack(boolean bok, Object recordList, String error) {
        hideLoading();
        if (bok) {
            JSONArray array = (JSONArray) recordList;
            if (dataList == null) {
                dataList = new ArrayList<>();
            }
            checkProjectList.clear();
            dataList.clear();
            for (int i = 0; i < array.size(); i++) {
                CheckProjectInfo info = JSONObject.parseObject(array.getJSONObject(i).toJSONString(), CheckProjectInfo.class);
                if(!checkProjectList.contains(info)){
                    checkProjectList.add(info);
                    dataList.add(CommonUtils.getJsonObjFromBean(info));
                }
            }
//            tableAdapter.notifyDataSetChanged();
            Log.d(TAG_NAME, array.toJSONString());
            if(recordOther != null){
                showLoading();
                boolean bhd = CommCL.houDuan.containsKey(zcInfo.getId());
                String sid1 = bhd?recordOther.getLotno():recordOther.getSid1();
                CommonUtils.textViewGetFocus(edtSid1);
                edtSid1.setText(sid1);
                commPresenter.getQCBatchInfo(sid1, zcInfo.getId(), bhd);
            }
        } else {
            showMessage(error);
            dataList.clear();
            checkProjectList.clear();
        }
        tableAdapter.notifyDataSetChanged();
    }

    /***
     * 检验获取批次信息返回回调处理
     * @param bok 是否成功
     * @param o 成功返回对象
     * @param error 失败返回错误信息
     */
    @Override
    public void checkQCBatInfoBack(boolean bok, Object o, String error) {
        hideLoading();
        recordOther = null;
        if (bok) {
            qcBatchInfo = (QCBatchInfo) o;
            if (qcBatchInfo.getHoldid() == 1) {
                showMessage("该批次" + (qcBatchInfo.isHuoduan() ? qcBatchInfo.getLotno() : qcBatchInfo.getSid1()) + "被HOLD！");
                CommonUtils.textViewGetFocus(edtSid1);
                qcBatchInfo = null;
                return;
            }
            if (!qcBatchInfo.isHuoduan()) {
                if (CommCL.BATCH_STATUS_DONE.equals(qcBatchInfo.getState())) {
                    showMessage("该批次【" + qcBatchInfo.getSid1() + "】，已完工!");
                    CommonUtils.textViewGetFocus(edtSid1);
                    qcBatchInfo = null;
                    return;
                }
                if (CommCL.BATCH_STATUS_READY.equals(qcBatchInfo.getState()) || CommCL.BATCH_STATUS_IN.equals(qcBatchInfo.getState()) || CommCL.BATCH_STATUS_CHARGING.equals(qcBatchInfo.getState()) || CommCL.BATCH_STATUS_WORKING.equals(qcBatchInfo.getState())
                        || CommCL.BATCH_STATUS_CHECKING.equals(qcBatchInfo.getState())) {
//                    edtSid1.setText(qcBatchInfo.getSid1());
                    CommonUtils.textViewGetFocus(edtPrdName);
                    edtPrdName.setText(qcBatchInfo.getPrd_name());
                    return;
                } else {
                    showMessage("该批次【" + qcBatchInfo.getSid1() + "】异常!!");
                    CommonUtils.textViewGetFocus(edtSid1);
                    qcBatchInfo = null;
                    return;
                }

            } else {
//                edtSid1.setText(qcBatchInfo.getLotno());
                CommonUtils.textViewGetFocus(edtPrdName);
                edtPrdName.setText(qcBatchInfo.getPrd_name());
                return;
            }
        } else {
            showMessage(error);
        }
    }

    /***
     * 调用服务器错误回调显示错误
     * @param message 错误消息
     */
    @Override
    public void onRemoteFailed(String message) {
        hideLoading();
        showMessage(message);
    }


    private int chooseIndex = 0;//选择项

    /***
     * 调用审批返回
     * @param bok 是否成功
     * @param key 调用的是33还是34,33：获取审批信息，34执行审批信息
     * @param ceaPars 交互过程中的状态
     * @param cWorkInfo 审批流信息
     * @param error 错误信息
     */
    @Override
    public void checkActionBack(boolean bok, int key, CeaPars ceaPars, CWorkInfo cWorkInfo, String error) {
        if (bok) {
            hideLoading();
            if (key == CommCL.COMM_CHK_LIST) {
                //弹出dialog，并
                checkUp(ceaPars, cWorkInfo);
            }else {
                CommonUtils.showMessage(this,"提交成功");
                CommonUtils.textViewGetFocus(edtSid1);
                currRecord = null;
                dataList.clear();
                tableAdapter.notifyDataSetChanged();
                for(int i=0;i<checkProjectList.size();i++)
                    dataList.add(CommonUtils.getJsonObjFromBean(checkProjectList.get(i)));
                tableAdapter.notifyDataSetChanged();
            }
        } else {
            hideLoading();
            showMessage(error);
            return;
        }
    }

    public void checkUp(CeaPars ceaPars, CWorkInfo cWorkInfo) {
        if (cWorkInfo.getList() == null) {
            showMessage("没有审批节点!");
            return;
        }
        ApprovalFlowObj approvalFlowObj = cWorkInfo.getList().get(0);
        if (approvalFlowObj.getUsers() == null) {
            showMessage("没有审批人!");
            return;
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("请选择审核人");
            builder.setCancelable(false); // 设置不可点击界面之外的区域让对话框消失
            final String[] Auditor = new String[approvalFlowObj.getUsers().size()];
            for (int j = 0; j < approvalFlowObj.getUsers().size(); j++) {
                User user = approvalFlowObj.getUsers().get(j);
                Auditor[j] = user.getUserName();
            }
            builder.setSingleChoiceItems(Auditor, chooseIndex, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    chooseIndex = which;
                }
            });
            builder.setPositiveButton("确定", new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ceaPars.setStateto(Integer.parseInt(approvalFlowObj.getStateId()));
                    ceaPars.setTousr(approvalFlowObj.getUsers().get(chooseIndex).getUserCode());
                    showLoading();
                    commPresenter.checkActionUp(ceaPars);
                }
            });
            builder.show();
        }
    }


    /***
     * 注册广播事件，监听PDA扫描
     */
    private BroadcastReceiver barcodeReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if(CommCL.INTENT_ACTION_SCAN_RESULT.equals(intent.getAction())){
                View rootView = getCurrentFocus();//获取光标当前所在组件
                String barCodeData = null;
                if(TextUtils.isEmpty(intent.getStringExtra(CommCL.SCN_CUST_HONEY))){
                    barCodeData = intent.getStringExtra(CommCL.SCN_CUST_EX_SCODE);
                }else{
                    barCodeData = intent.getStringExtra(CommCL.SCN_CUST_HONEY);
                }
                barCodeData = barCodeData.toUpperCase();
                int id = rootView.getId();
                if(id == R.id.edt_op){
                    edtOP.setText(barCodeData);
                    OnEditorAction(edtOP);
                    return;
                }
                if(id == R.id.edt_equipment_no){
                    edtSbId.setText(barCodeData);
                    OnEditorAction(edtSbId);
                    return;
                }
                if(id == R.id.edt_sid1){
                    edtSid1.setText(barCodeData);
                    OnEditorAction(edtSid1);
                    return;
                }

            }
        }
    };

}
