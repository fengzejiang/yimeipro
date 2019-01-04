package com.yimei.activity.deviceproduction;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnEditorAction;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.shade.com.alibaba.fastjson.JSONArray;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.shade.com.alibaba.fastjson.JSONObject;
import com.yimei.activity.R;
import com.yimei.adapter.tabledataadapter.BaseTableDataAdapter;
import com.yimei.data.*;
import com.yimei.modelInterface.BaseStationBindingView;
import com.yimei.network.schedulers.SchedulerProvider;
import com.yimei.presenter.AddGluingOrXGPresenter;
import com.yimei.tableui.TablePanelView;
import com.yimei.tableui.entity.HeaderRowInfo;
import com.yimei.utils.CommCL;
import com.yimei.utils.CommonUtils;
import com.zyao89.view.zloading.ZLoadingDialog;
import com.zyao89.view.zloading.Z_TYPE;

import java.util.ArrayList;
import java.util.List;

/**
 * 加胶或者添加锡膏
 * 现在只实现了添加胶，后期需要实现加锡膏
 * @Auther: fengzejiang1987@163.com
 * @Date : 2019/1/3 14:30
 */
public class AddGluingActivity extends AppCompatActivity implements BaseStationBindingView {

    //绑定xml布局文件中的输入框
    @BindView(R.id.edt_op)
    EditText editTextOP;
    @BindView(R.id.edt_equipment_no)
    EditText editTextSbId;
    @BindView(R.id.edt_gluing_no)
    EditText editTextGluingNo;

    private String currOP;
    private String currSbId;
    private AddGluingOrXGPresenter gluingPresenter;

    private ZLoadingDialog zLoadingView;
    private EquipmentInfo currEquipInfo;

    //数据表格
    @BindView(R.id.table_view)
    TablePanelView tableView;
    @BindView(R.id.data_list_content)
    ListView dataListViewContent;
    List<JSONObject> dataList;//
    BaseTableDataAdapter adapter;

    private List<JSONObject> mesRecordList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_gluing);
        ButterKnife.bind(this);
        currOP =  getIntent().getStringExtra(CommCL.COMM_OP_FLD);
        currSbId =  getIntent().getStringExtra(CommCL.COMM_SBID_FLD);
        dataList = new ArrayList();
        List<HeaderRowInfo> header = getRowDataList();
        initView();
        adapter = new BaseTableDataAdapter(this,tableView,dataListViewContent,dataList,header);
        adapter.setSwipeRefreshEnabled(false);
        adapter.setTitleHeight(90);
        adapter.setTitleWidth(90);
        tableView.setAdapter(adapter);
        gluingPresenter = new AddGluingOrXGPresenter(this, SchedulerProvider.getInstance());
        if(!TextUtils.isEmpty(currOP)){
            editTextOP.setText(currOP);
        }
        if(!TextUtils.isEmpty(currSbId)){
            editTextSbId.setText(currSbId);
            showLoading();
            gluingPresenter.getLatelyMesRecord(currSbId);
        }

        registerReceiver(barcodeReceiver,new IntentFilter(
                CommCL.INTENT_ACTION_SCAN_RESULT));


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

    private void initView() {
        dataListViewContent.setChoiceMode(ListView.CHOICE_MODE_NONE);
    }

    private List<HeaderRowInfo> getRowDataList() {
        List<HeaderRowInfo> rowList = new ArrayList<>();
        HeaderRowInfo prtNoColumn = new HeaderRowInfo("prtno","胶杯号",250);
        HeaderRowInfo slkidColumn = new HeaderRowInfo("slkid","制令单号",250);
        HeaderRowInfo prdNoColumn = new HeaderRowInfo("prd_no","产品编码",150);
        HeaderRowInfo prdNameColumn = new HeaderRowInfo("prd_name","产品机型",310);
        HeaderRowInfo inDateColumn = new HeaderRowInfo("indate","加胶时间",310);
        rowList.add(prtNoColumn);
        rowList.add(slkidColumn);
        rowList.add(prdNoColumn);
        rowList.add(prdNameColumn);
        rowList.add(inDateColumn);
        return rowList;
    }

    @Override
    public void checkMboxCallBack(boolean bok, String error, int key) {

    }

    @Override
    public void checkSidCallBack(boolean bok, JSONObject batchInfo, String error) {

    }

    @Override
    public void checkSbIdCallBack(boolean bok, EquipmentInfo sbInfo, String error) {
        if(bok){
            currEquipInfo = sbInfo;
            currSbId = currEquipInfo.getId();
            gluingPresenter.getLatelyMesRecord(currSbId);
        }else {
            hideLoading();
            showMessage(error);
            CommonUtils.textViewGetFocus(editTextSbId);
        }
    }

    @Override
    public void checkRecordCallBack(boolean bok, JSONObject sbInfo, String error) {
    }

    @Override
    public String getCurrMO() {
        if(mesRecordList != null){
            JSONObject jsonObject = mesRecordList.get(0);
            return jsonObject.containsKey("slkid")?jsonObject.getString("slkid"):null;
        }
        return null;
    }

    @Override
    public void saveRecordBack(boolean bok, Object record, String error) {
        if(bok){
            MesGluingRecord mesGluingRecord = (MesGluingRecord) record;
            mesGluingRecord.setState(0);
            addRow(mesGluingRecord);
            gluingPresenter.doServerAddGluing(currSbId,mesGluingRecord.getPrtno());
        }else{
            hideLoading();
            showMessage(error);
        }
    }

    @Override
    public void changeRecordStateBack(boolean bok, Object record, String error) {
        hideLoading();
        if(bok){
            CommonUtils.textViewGetFocus(editTextSbId);
        }else{
            showMessage(error);
        }
    }

    @Override
    public void changeMultiRecordStateBack(boolean bok, List<MESPRecord> recordList, String error) {

    }

    @Override
    public void getMultiRecordBack(boolean bok, JSONArray recordList, String error, int type) {
        hideLoading();
        if(bok){
            if(mesRecordList==null){
                mesRecordList = new ArrayList<>();
            }
            mesRecordList.clear();
            if(recordList==null){
                showMessage("该设备"+currSbId+"上没有生产记录");
                CommonUtils.textViewGetFocus(editTextSbId);
            }
            for(int i=0;i<recordList.size();i++){
                mesRecordList.add(recordList.getJSONObject(i));
            }
            CommonUtils.textViewGetFocus(editTextGluingNo);
        }else{
            showMessage(error);
            CommonUtils.textViewGetFocus(editTextSbId);
        }
    }

    /***
     * 输入胶杯批次号回车以后的回调
     * 借用加载检验发起原因回调函数，如果成功返回胶杯信息
     * @param bok 是否成功
     * @param record
     * @param error 错误信息
     */
    @Override
    public void loadReasonsBack(boolean bok, Object record, String error) {
        hideLoading();
        if(bok){
            GluingInfo gluingInfo = (GluingInfo) record;
            if(TextUtils.isEmpty(gluingInfo.getMixing_time())){
                showMessage("该胶杯号【"+gluingInfo.getPrtno()+"】没有做混胶!");
                CommonUtils.textViewGetFocus(editTextGluingNo);
                return;
            }
            if(TextUtils.isEmpty(gluingInfo.getNewly_time())){
                showMessage("该胶杯号【"+gluingInfo.getPrtno()+"】没有最近到期时间!");
                CommonUtils.textViewGetFocus(editTextGluingNo);
                return;
            }
            if(gluingInfo.getMixtime()>gluingInfo.getFr_add_time()){
                showMessage("胶杯号【"+gluingInfo.getPrtno()+"】,已超过"+gluingInfo.getFr_add_time()+"分钟，请联系工程人员！");
                CommonUtils.textViewGetFocus(editTextGluingNo);
                return ;
            }
            String sbMo = getCurrMO();
            if(TextUtils.isEmpty(sbMo)){
                showMessage("当前设备【"+currSbId+"】的生产批次没有绑定工单号！");
                CommonUtils.textViewGetFocus(editTextGluingNo);
                return ;
            }
            if(!sbMo.equals(gluingInfo.getMo_no())){
                showMessage("当前设备【"+currSbId+"】绑定的工单号"+sbMo+",胶杯的工单号是："+gluingInfo.getMo_no());
                CommonUtils.textViewGetFocus(editTextGluingNo);
                return ;
            }

            MesGluingRecord mesGluingRecord = new MesGluingRecord();
            mesGluingRecord.setOp(currOP);
            mesGluingRecord.setPrtno(gluingInfo.getPrtno());
            mesGluingRecord.setPrd_no(gluingInfo.getPrdno());
            mesGluingRecord.setPrd_name(gluingInfo.getName());
            mesGluingRecord.setQty(1);
            mesGluingRecord.setSbid(currSbId);
            mesGluingRecord.setMkdate(gluingInfo.getTprn());
            mesGluingRecord.setSlkid(gluingInfo.getMo_no());
            mesGluingRecord.setState(0);
            gluingPresenter.saveGluingRecord(mesGluingRecord);
        }else{
            hideLoading();
            showMessage(error);
            CommonUtils.textViewGetFocus(editTextGluingNo);
        }
    }

    @Override
    public void loadCheckProjectBack(boolean bok, Object recordList, String error) {

    }

    @Override
    public void checkQCBatInfoBack(boolean bok, Object o, String error) {

    }

    @Override
    public void showLoading() {
        if(zLoadingView==null)
            zLoadingView = CommonUtils.initLoadingView(this,"加载中", Z_TYPE.CIRCLE_CLOCK);
        zLoadingView.show();
    }

    @Override
    public void hideLoading() {
        if(zLoadingView!=null)
            zLoadingView.dismiss();
    }

    @Override
    public void onRemoteFailed(String message) {
        hideLoading();
        showMessage(message);
    }

    @Override
    public void showMessage(String message) {
        CommonUtils.showError(this,message);
    }

    @Override
    public void jumpNextActivity(Class<?> descClass) {

    }

    @Override
    public void jumpNextActivity(Context srcContent, Class<?> descClass) {

    }

    /***
     * 添加数据到当前tableView
     * @param unBindInfo 数据对象
     */
    @Override
    public void addRow(Object unBindInfo) {
        hideLoading();
        adapter.addRecord(CommonUtils.getJsonObjFromBean(unBindInfo));
        CommonUtils.textViewGetFocus(editTextSbId);
    }

    @Override
    public void checkActionBack(boolean bok, int key, CeaPars ceaPars, CWorkInfo cWorkInfo, String error) {

    }

    /***
     * 注册广播事件，监听PDA扫描
     */
    private BroadcastReceiver barcodeReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if(CommCL.INTENT_ACTION_SCAN_RESULT.equals(intent.getAction())){
                View rootView = getCurrentFocus();//获取光标当前所在组件
                Object tag = rootView.findFocus().getTag();
                String barCodeData = null;
                if(intent.getStringExtra(CommCL.SCN_CUST_HONEY).equals(null)){
                    barCodeData = intent.getStringExtra(CommCL.SCN_CUST_EX_SCODE);
                }else{
                    barCodeData = intent.getStringExtra(CommCL.SCN_CUST_HONEY);
                }
                barCodeData = barCodeData.toUpperCase();
                int id = rootView.getId();
                if(id == R.id.edt_op){
                    editTextOP.setText(barCodeData);
                    onEditTextKeyDown(editTextOP);
                }
                if(id == R.id.edt_equipment_no){
                    editTextSbId.setText(barCodeData);
                    onEditTextKeyDown(editTextSbId);
                }
                if(id == R.id.edt_gluing_no){
                    editTextGluingNo.setText(barCodeData);
                    onEditTextKeyDown(editTextGluingNo);
                }
            }
        }
    };

    @OnEditorAction({R.id.edt_op,R.id.edt_equipment_no,R.id.edt_gluing_no})
    public  boolean onEditTextKeyDown(EditText editText) {
        currOP = editTextOP.getText().toString().trim().toUpperCase();
        String str = editText.getText().toString().trim().toUpperCase();
        int id = editText.getId();
        if(TextUtils.isEmpty(currOP)){
            showMessage("请输入操作员！");
            CommonUtils.textViewGetFocus(editTextOP);
            return false;
        }
        String opv = CommCL.sharedPreferences.getString(currOP,"");
        if(TextUtils.isEmpty(opv)){
            showMessage("操作员"+currOP+"不存在！");
            CommonUtils.textViewGetFocus(editTextOP);
            return false;
        }
        if(id == R.id.edt_op){
            CommonUtils.textViewGetFocus(editTextSbId);
            return true;
        }
        currSbId = editTextSbId.getText().toString().toUpperCase();
        if(TextUtils.isEmpty(currSbId)){
            showMessage("请输入设备号！");
            CommonUtils.textViewGetFocus(editTextSbId);
            return false;
        }
        if(id == R.id.edt_equipment_no){
            if(currEquipInfo!=null){
                if(!currEquipInfo.getId().equals(currSbId)){
                    showLoading();
                    gluingPresenter.getEquipMentInfo(currSbId);
                    return true;
                }else{
                    CommonUtils.textViewGetFocus(editTextGluingNo);
                    return true;
                }
            }else{
                showLoading();
                gluingPresenter.getEquipMentInfo(currSbId);
                return true;
            }

        }
        if(id == R.id.edt_gluing_no){
            if(TextUtils.isEmpty(str)){
                showMessage("请输入胶杯号！");
                CommonUtils.textViewGetFocus(editTextGluingNo);
                return false;
            }
            showLoading();
            gluingPresenter.getGluingInfo(str);
            return true;
        }
        return true;
    }

    public void showSuccess(String message){
        CommonUtils.showSuccess(this,message);
    }
}
