package com.yimei.activity.deviceproduction.commsub;

import android.app.TabActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TabHost;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnEditorAction;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.shade.com.alibaba.fastjson.JSONArray;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.shade.com.alibaba.fastjson.JSONObject;
import com.yimei.activity.R;
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
import com.zyao89.view.zloading.ZLoadingDialog;
import com.zyao89.view.zloading.Z_TYPE;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * 前段上料Activity
 * 简单逻辑放到Activity，复杂的逻辑放到Controller里面处理，如何服务端交互
 * 在该Activity里面全部都借用了ChargingMaterial这个上料子表对象用于表格数据展示
 * @Auther: fengzejiang1987@163.com
 * @Date : 2018/12/23 21:27
 */
public class ChargingActivity extends TabActivity implements BaseStationBindingView {

    @BindView(R.id.table_view)
    TablePanelView tablePanel;//tab1页面的表格视图
    @BindView(R.id.data_list_content)
    ListView dataListViewContent;//tab1页面的表格ListView
    private ArrayList<JSONObject> dataList = new ArrayList<>();//上料物料列表
    BaseTableDataAdapter adapter;

    @BindView(R.id.table_view_material)
    TablePanelView tablePanelMaterialList;//工单所需物料表格视图
    @BindView(R.id.data_list_content_material)
    ListView dataListViewContentMaterial;//工单所需物料表格视图
    private ArrayList<JSONObject> materialDataList = new ArrayList<>();//物料数据列表
    private ArrayList<JSONObject> newUpList = new ArrayList<>();//物料数据列表
    BaseTableDataAdapter adapterMaterial;
    private CommStationZCPresenter commStationZCPresenter;

    @BindView(R.id.edt_material)
    EditText edtMaterial;
    @BindView(R.id.edt_lot)
    EditText edtLot;
    @BindView(R.id.edt_yf_qty)
    EditText edtYfQty;
    @BindView(R.id.edt_qty)
    EditText edtSfQty;

    ZCInfo zCnoInfo;
    String currOP;
    String currSbId;
    MESPRecord record;
    private ZLoadingDialog zLoadingView;
    private String cont;

    private boolean mainRecordExit = false;

    @BindView(R.id.btn_save)
    Button btnSave;

    private HashMap<String, Integer[]> materInfo = new HashMap<>();//材料信息，应发数,已发数
    private HashMap<String, Integer> materLot = new HashMap<>();
    private HashMap<String, String> materLotUp = new HashMap<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_charging);
        zCnoInfo = (ZCInfo) getIntent().getSerializableExtra(CommCL.COMM_ZC_INFO_FLD);
        record = (MESPRecord) getIntent().getSerializableExtra(CommCL.COMM_RECORD_FLD);
        currOP =  getIntent().getStringExtra(CommCL.COMM_OP_FLD);
        currSbId =  getIntent().getStringExtra(CommCL.COMM_SBID_FLD);
        TabHost tabHost = this.getTabHost();
        TabHost.TabSpec tab1 = tabHost.newTabSpec("tab1").setIndicator("扫描区")
                .setContent(R.id.tab1);
        tabHost.addTab(tab1);
        TabHost.TabSpec tab2 = tabHost.newTabSpec("tab2").setIndicator("材料明细")
                .setContent(R.id.tab2);
        tabHost.addTab(tab2);
        ButterKnife.bind(this);//黄油刀绑定界面上的id，不用每次都findViewById
        dataListViewContent.setChoiceMode(ListView.CHOICE_MODE_NONE);
        dataListViewContentMaterial.setChoiceMode(ListView.CHOICE_MODE_NONE);
        List<HeaderRowInfo> headerList = getHeaderList();
        List<HeaderRowInfo> headerListMaterial = getMaterialHeaderList();
        //初始化扫描页面的已上料表格适配器
        adapter = new BaseTableDataAdapter(this,tablePanel,dataListViewContent,dataList,headerList);
        adapter.setTitleHeight(90);
        adapter.setTitleWidth(90);
        tablePanel.setAdapter(adapter);
        //初始化材料页面的表格适配器
        adapterMaterial = new BaseTableDataAdapter(this,tablePanelMaterialList,dataListViewContentMaterial,materialDataList,headerListMaterial);
        adapterMaterial.setTitleHeight(90);
        adapterMaterial.setTitleWidth(120);
        tablePanelMaterialList.setAdapter(adapterMaterial);
        //初始化中间层，用于和服务端交互，和UI交互，相当于Controller
        commStationZCPresenter = new CommStationZCPresenter(this, SchedulerProvider.getInstance());
        //初始化材料明细查询条件
        cont = "~mo_no='"+record.getSlkid()+"'";
        if(zCnoInfo.getId().equals("11")||zCnoInfo.getId().equals("12")||zCnoInfo.getId().equals("13")){
            cont+="  and (gzl='M01' OR gzl='M03') and upid>0";
        }
        if("21".equals(zCnoInfo.getId())){
            cont +=" and gzl='M04'";
        }
        showLoading();
        commStationZCPresenter.loadMaterialInfo(cont,0);
        commStationZCPresenter.checkMainMaterialExit(record.getSid1());
        registerReceiver(barcodeReceiver, new IntentFilter(
                CommCL.INTENT_ACTION_SCAN_RESULT)); // 注册广播
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(barcodeReceiver, new IntentFilter(
                CommCL.INTENT_ACTION_SCAN_RESULT)); // 注册广播
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(barcodeReceiver);
    }


    /***
     * 保存扫描记录
     */
    @OnClick(R.id.btn_save)
    public void setBtnSave(){
        if(newUpList.size()>0){
            showLoading();
            commStationZCPresenter.saveMaterialRecord(record,newUpList,!mainRecordExit);
        }

    }
    /***
     * 给输入框注册回车事件
     * @param editText
     * @return 返回true不跳转，返回false跳转
     */
    @OnEditorAction({R.id.edt_material,R.id.edt_lot})
    public boolean OnEditorAction(EditText editText){
        //无论哪个输入框的输入完成，首先判断操作员输入框是否有值，如果有值，则继续往下
        //如果操作员输入框没有值,则跳转到操作员输入框，
        Log.d("回车事件",editText.getText().toString());
        String str = editText.getText().toString().toUpperCase();
        String material = edtMaterial.getText().toString().toUpperCase();
        int id = editText.getId();
        if(id == R.id.edt_material){
            //判断材料号是否为空
            if(checkMaterial(str)){
                CommonUtils.textViewGetFocus(edtLot);
                return true;
            }
        }
        if(id == R.id.edt_lot){
            if(checkMaterial(material)){
                String key = material+"_"+str;
                if(materLotUp.containsKey(key)){
                    showMessage("该材料批次已经上过，请更换批次");
                    CommonUtils.textViewGetFocus(edtLot);
                    return false;
                }
                //检查批次是否存在
                if(!(materLot.containsKey(key)||materLot.containsKey(material+"_"))){
                    showMessage("该材料批次【"+str+"】不存在！");
                    CommonUtils.textViewGetFocus(edtLot);
                    return false;
                }
                boolean bBatch = materLot.containsKey(key);
                int index = bBatch?materLot.get(key):materLot.get(material+"_");
                JSONObject jsonstr = materialDataList.get(index);
                ChargingMaterial data = JSONObject.parseObject(jsonstr.toJSONString(),ChargingMaterial.class);
                if(!bBatch){
                    data.setBat_no(str);
                }
                data.setSid(record.getSid1());
                data.setOp(currOP);
                data.setDcid(CommonUtils.getMacID());
                data.setHpdate(DateUtil.getCurrDateTime(ICL.DF_YMDT));
                data.setSbid(currSbId);
                data.setCid(getMaxCid());
                data.setSys_stated(3);
                jsonstr = CommonUtils.getJsonObjFromBean(data);
                newUpList.add(jsonstr);
                adapter.addRecord(jsonstr);
                Integer[] vv = materInfo.get(material);
                vv[1] = vv[1]+data.getQty();
                materInfo.put(material,vv);
                initText(material);
                materLotUp.put(key,key);
                if(checkMaterialFull(material)){
                    CommonUtils.textViewGetFocus(edtMaterial);
                }else{
                    CommonUtils.textViewGetFocus(edtLot);
                }
                return true;
            }
        }
        return false;
    }

    /***
     * 获取上料记录的最大Cid，是子对象的主键之一,int类型
     * @return
     */
    private int getMaxCid() {
        int key = 1;
        if(dataList.size()==0){
            return key;
        }else{
            key = 1;
            for(JSONObject jsonObject : dataList){
                ChargingMaterial material = JSONObject.parseObject(jsonObject.toJSONString(),ChargingMaterial.class);
                if(material.getCid()>key){
                    key = material.getCid();
                }
            }
            return key+1;
        }
    }

    public boolean checkMaterial(String str) {
        if(TextUtils.isEmpty(str)){
            showMessage("材料号不能为空");
            CommonUtils.textViewGetFocus(edtMaterial);
            return false;
        }else{
            if(!materInfo.containsKey(str)){
                showMessage("材料号【"+str+"】不存在！");
                CommonUtils.textViewGetFocus(edtMaterial);
                return false;
            }
            initText(str);
            if(checkMaterialFull(str)){
                showMessage("材料号【"+str+"】上料数量够了！");
                CommonUtils.textViewGetFocus(edtMaterial);
                return false;
            }else{
                return true;
            }
        }
    }

    /***
     * 设置界面上的应发量和实发量
     * @param str
     */
    private void initText(String str) {
        Integer[] vv = materInfo.get(str);
        edtYfQty.setText(vv[0]+"");
        edtSfQty.setText(vv[1]+"");
    }

    /***
     * 校验材料是否上满，可以有一次超量
     * @param str 材料编码
     * @return 是否超量
     */
    private boolean checkMaterialFull(String str){
        Integer[] vv = materInfo.get(str);
        return !(vv[0]-vv[1]>0);
    }

    //初始化tab2的表格表头
    private List<HeaderRowInfo> getMaterialHeaderList() {
        List<HeaderRowInfo> rowList = new ArrayList<>();
        HeaderRowInfo prdNoColumn = new HeaderRowInfo("prd_no","材料编码",150);
        HeaderRowInfo nameColumn = new HeaderRowInfo("prd_name","材料名称",200);
        HeaderRowInfo batNoColumn = new HeaderRowInfo("bat_no","批次号",310);
        HeaderRowInfo qtyColumn = new HeaderRowInfo("qty","数量",200);
        rowList.add(prdNoColumn);
        rowList.add(nameColumn);
        rowList.add(batNoColumn);
        rowList.add(qtyColumn);
        return rowList;
    }
    //初始化tab1的表格表头
    private List<HeaderRowInfo> getHeaderList() {
        List<HeaderRowInfo> list =  getMaterialHeaderList();
        HeaderRowInfo sid1 = new HeaderRowInfo("sid","批次信息",300);
        list.add(0,sid1);
        return list;
    }


    /***
     * 借用通用工站的检查料盒号功能，用于检查上料主记录是否存在
     * @param bok true：存在主记录，false 不存在主记录
     * @param error 错误信息
     */
    @Override
    public void checkMboxCallBack(boolean bok, String error,int key) {
        mainRecordExit = bok;
    }

    @Override
    public void checkSidCallBack(boolean bok, JSONObject batchInfo, String error) {

    }

    @Override
    public void checkSbIdCallBack(boolean bok, EquipmentInfo sbInfo, String error) {
    }

    @Override
    public void checkRecordCallBack(boolean bok, JSONObject sbInfo, String error) {
    }

    @Override
    public String getCurrMO() {
        return null;
    }

    @Override
    public void saveRecordBack(boolean bok, Object record1, String error) {
        hideLoading();
        if(bok){
            newUpList.clear();
            record.setState1(CommCL.BATCH_STATUS_CHARGING);
        }else{
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
        if(type==0){//材料批次明细
            if(bok){
                materInfo.clear();
                materLot.clear();
                for(int i=0;i<recordList.size();i++){
                    ChargingMaterial materialInfo = JSONObject.parseObject(recordList.getJSONObject(i).toJSONString(),ChargingMaterial.class);
                    String prdNo = materialInfo.getPrd_no();
                    if(!materInfo.containsKey(prdNo)){
                        materInfo.put(materialInfo.getPrd_no(),new Integer[]{record.getQty(),0});
                    }
                    String lotNo = materialInfo.getBat_no();
                    lotNo = TextUtils.isEmpty(lotNo)?"":lotNo;
                    String lotMater = prdNo+"_"+lotNo;
                    if(!materLot.containsKey(lotMater)){
                        materLot.put(lotMater,i);
                    }
                    materialDataList.add(CommonUtils.getJsonObjFromBean(materialInfo));
                }
                adapterMaterial.notifyDataSetChanged();
                String cont = "~sid='"+record.getSid1()+"'";
                commStationZCPresenter.loadMaterialInfo(cont,1);
            }else{
                showMessage("该批次没有材料信息！！！");
                hideLoading();
            }

        }else{
            hideLoading();
            if(bok){
                materLotUp.clear();
                for(int i=0;i<recordList.size();i++){
                    ChargingMaterial materialInfo = JSONObject.parseObject(recordList.getJSONObject(i).toJSONString(),ChargingMaterial.class);
                    String prdNo = materialInfo.getPrd_no();
                    String lotNo = materialInfo.getBat_no();
                    String key = prdNo+"_"+lotNo;
                    if(!materLotUp.containsKey(key)){
                        materLotUp.put(key,key);
                    }
                    //取出上料数，放到材料集合中去，用于校验是否材料已经上够
                    if(materInfo.containsKey(prdNo)){
                        Integer[] qtys = materInfo.get(prdNo);
                        qtys[1] = materialInfo.getQty()+qtys[1];
                        materInfo.put(prdNo,qtys);
                    }
                    dataList.add(CommonUtils.getJsonObjFromBean(materialInfo));
                }
                adapter.notifyDataSetChanged();
            }
        }
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
    public void showLoading() {
        if(zLoadingView==null){
            zLoadingView = CommonUtils.initLoadingView(this,getString(R.string.loading), Z_TYPE.CIRCLE_CLOCK);
//            zLoadingView.setLoadingColor(Color.RED);
        }
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
        CommonUtils.showError(this,message);
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

    @Override
    public void addRow(Object unBindInfo) {

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
                if(id == R.id.edt_material){
                    edtMaterial.setText(barCodeData);
                    OnEditorAction(edtMaterial);
                    return ;
                }
                if(id == R.id.edt_lot){
                    edtLot.setText(barCodeData);
                    OnEditorAction(edtLot);
                    return ;
                }

            }
        }
    };
}
