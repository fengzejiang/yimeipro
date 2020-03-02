package com.yimeinew.activity.deviceproduction;

import android.app.AlertDialog;
import android.content.*;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.*;
import android.view.Menu;
import android.widget.*;
import butterknife.*;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.shade.com.alibaba.fastjson.JSON;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.shade.com.alibaba.fastjson.JSONArray;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.shade.com.alibaba.fastjson.JSONObject;
import com.yimeinew.activity.R;
import com.yimeinew.activity.base.BaseActivity;
import com.yimeinew.activity.qc.FirstInspectionActivity;
import com.yimeinew.adapter.tabledataadapter.BaseTableDataAdapter;
import com.yimeinew.data.*;
import com.yimeinew.listener.OnConfirmListener;
import com.yimeinew.modelInterface.BaseStationBindingView;
import com.yimeinew.network.schedulers.SchedulerProvider;
import com.yimeinew.presenter.BianDaiPresenter;
import com.yimeinew.tableui.TablePanelView;
import com.yimeinew.tableui.entity.HeaderRowInfo;
import com.yimeinew.utils.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @Auther: fengzejiang1987@163.com
 * @Date : 2018/12/13 11:07
 */
public class BianDaiActivity extends BaseActivity implements BaseStationBindingView {

    private final String TAG_NAME = BianDaiActivity.class.getSimpleName();

    private String currMONO = "";//当前工单号
    @BindView(R.id.edt_op)
    EditText edtOP;
    @BindView(R.id.edt_equipment_no)
    EditText edtSbid;

    @BindView(R.id.edt_sid1)
    EditText edtSid1;
    @BindView(R.id.gujing_shangliao)
    Button shangliao;
    @BindView(R.id.gujing_kaigong)
    Button kaigong;
    @BindView(R.id.gujing_chuzhan)
    Button chuzhan;
    @BindView(R.id.gujing_shuliang)
    Button shuliang;
    //数据表格
    @BindView(R.id.table_view)
    TablePanelView tableView;
    @BindView(R.id.data_list_content)
    ListView dataListViewContent;
    List<JSONObject> dataList;
    BaseTableDataAdapter adapter;
    boolean b=false;

    private BianDaiPresenter commPresenter;

    private GJViewEntity entity;
    private ZCInfo zCnoInfo;
    public static final String Title = "编带管理";

    private HashMap<String, String> bindBox = new HashMap<>();
    private HashMap<String, String> bindSid1 = new HashMap<>();
    private EquipmentInfo currEquipment;
    private CommMultiChoiceModeCallback commChoice;
    private String GBKEY="broadcastReceiverBD";
    //private String mbox;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_bian_dai);
        this.setTitle(Title );

        zCnoInfo = new ZCInfo();
        zCnoInfo.setId("71");
        /*
        entity = new GJViewEntity();
        entity.setZcno(zCnoInfo.getId());
        if ("11".equals(zCnoInfo.getId())) {
            entity.setGj1(true);

        }
        this.setTitle(Title + zCnoInfo.getName());
        setContentView(R.layout.activity_mbox_bindzj);
        //activityCommGjBinding.setZcInf(entity);
        */
        ButterKnife.bind(this);


        initTableView();
        //生成假数据
//        initData();

        commPresenter = new BianDaiPresenter(this, SchedulerProvider.getInstance());
        //initButtonVisibility();

        //

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
        List<HeaderRowInfo> header = CommonUtils.getRowDataListBD();
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
    }

    @OnEditorAction({R.id.edt_op, R.id.edt_equipment_no, R.id.edt_sid1})
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
            return false;
        }
        String opv = CommCL.sharedPreferences.getString(operationUser, "");
        if (TextUtils.isEmpty(opv)) {
            if (id == R.id.edt_op) {
                showMessage("操作员【" + operationUser + "】不存在!");
            }
            CommonUtils.textViewGetFocus(edtOP);
            return false;
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
        if(currEquipment==null||!TextUtils.equals(currEquipment.getId(),sbId)){
            showMessage("手输设备号必须回车！");
            return false;
        }

        if (id == R.id.edt_sid1) {

            String sid = edtSid1.getText().toString().toUpperCase();
            if(CommonUtils.isRepeat(TAG_NAME+"edt_sid1",sid)){
                //CommonUtils.textViewGetFocus(edtSid1);
                return true;
            }
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


            //校验批次号
            //commPresenter.cache.put(sid,edtBox.getText().toString().toUpperCase());
            if(!CommonUtils.isRepeat("biandai_edt",sid)) {
                showLoading();
                CommonUtils.banDo(GBKEY);//禁用广播
                commPresenter.getBatchInfo(sid, zCnoInfo.getId(), currEquipment,this);
            }
        }


        return false;
    }
    //用于手输信息不回车的时候触发的
    //@OnFocusChange({R.id.edt_equipment_no,R.id.edt_box})
    public void OnFocusChangeListener(EditText editText,boolean hasFocus){

        if(!hasFocus) {
            if(!CommonUtils.isRepeat("gj_action_focus"+editText.getId(),"sb"+editText.getText()))
            {
                if(!TextUtils.isEmpty(editText.getText().toString().toUpperCase())) {
                    onEditTextKeyDown(editText);
                }
            }
        }

    }
    //Activity被覆盖到下面或者锁屏时被调用
    @Override
    protected void onPause() {
        super.onPause();
        //Log.i(TAG, "onPause called.");
        //有可能在执行完onPause或onStop后,系统资源紧张将Activity杀死,所以有必要在此保存持久数据

    }
    //退出当前Activity或者跳转到新Activity时被调用
    @Override
    protected void onStop() {
        super.onStop();
        //Log.i(TAG, "onStop called.");
    }

    /***
     *
     * @param bok 是否存在该料盒号
     * @param error 错误信息
     */
    @Override
    public void checkMboxCallBack(boolean bok, String error,int key) {
        /*
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
        */
    }

    @Override
    public void onRemoteFailed(String message) {
        hideLoading();
        CommonUtils.canDo(GBKEY);
        CommonUtils.showError(this,"如果不能加载，请重新" +message);
        /*
        showLoading();
        commChoice.clearChoice();
        dataList.clear();
        adapter.notifyDataSetChanged();
        commPresenter.getRecordBySbId(currEquipment.getId(),zCnoInfo.getId());
        */

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
            //如果error不为空就弹出提示页面
            if(!TextUtils.isEmpty(error)){
                CommonUtils.showOK(this,"提示",error);
            }

            //生成生产记录
            Log.i(TAG_NAME, batchInfo.toJSONString());
            String op = edtOP.getText().toString().toUpperCase();//操作员
            //String mBox = edtBox.getText().toString().toUpperCase();//料盒号
            String sbId = edtSbid.getText().toString().toUpperCase();//设备号
            //String sid1 = edtSid1.getText().toString().toUpperCase();//批次号
            String sid1 = batchInfo.getString("sid1");//批次号
            String mono = batchInfo.getString("sid");
            String zcno1 = batchInfo.getString("zcno1");
            String remark = batchInfo.getString("remark");
            int qty = batchInfo.getInteger("qty");
            String prd_no = batchInfo.getString("prd_no");
            String prd_name = batchInfo.getString("prd_name");
            int fircheck = batchInfo.getInteger("fircheck");
            String lotno=batchInfo.getString("lotno");
            String bincode=batchInfo.getString("bincode");
            MESPRecord record = new MESPRecord(sid1, mono, zCnoInfo.getId(), sbId);
            //record.setMbox(mBox);
            record.setOp(op);
            record.setZcno1(zcno1);
            record.setRemark(remark);
            record.setState1("01");
            record.setQty(qty);
            record.setPrd_no(prd_no);
            record.setPrd_name(prd_name);
            record.setFirstchk(fircheck);
            record.setLotno(lotno);
            record.setBincode(bincode);
            commPresenter.makeProRecord(record);
        } else {
            hideLoading();
            CommonUtils.canDo(GBKEY);
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
            /*
            if (entity.isGj1()) {
                //CommonUtils.textViewGetFocus(edtBox);
            }else{
                CommonUtils.textViewGetFocus(edtSid1);
            }
            */
            CommonUtils.textViewGetFocus(edtSid1);
            //System.err.println(sbInfo.getId()+" z="+zCnoInfo.getId());

            commChoice.clearChoice();
            dataList.clear();
            adapter.notifyDataSetChanged();

            commPresenter.getRecordBySbId(sbInfo.getId(),zCnoInfo.getId());//获取设备上绑定的生产记录(currEquipment.getId(),zCnoInfo.getId()) sbInfo.getZcno()
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
    public List<JSONObject> getDataList() {
        return dataList;
    }

    @Override
    public void saveRecordBack(boolean bok, Object records, String error) {
        if (bok) {
            //放到扫描的列表中
            MESPRecord record = (MESPRecord) records;
            bindSid1.put(record.getSid1(), record.getSid1());
            // 添加到数据列表
            adapter.addRecord(CommonUtils.getJsonObjFromBean(record));
            //更改批次状态280
            commPresenter.changeTestLotStateOneByOne(record, CommCL.BATCH_STATUS_IN);

        } else {
            CommonUtils.canDo(GBKEY);
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
            /*
            if (entity.isGj1()) {
                bindBox.put(record.getMbox(), record.getMbox());
                commPresenter.bindingBox(record.getMbox(), record.getSid1(), zCnoInfo.getId());
                //CommonUtils.textViewGetFocus(edtBox);
            } else {
                CommonUtils.textViewGetFocus(edtSid1);
            }
             */
            CommonUtils.textViewGetFocus(edtSid1);
            CommonUtils.canDo(GBKEY);
        } else {
            hideLoading();
            CommonUtils.canDo(GBKEY);
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
            showMessage("批次更新错误提示="+error);
        }
    }

    /***
     * 在本页面作为一个通用的返回函数
     * @param bok 是否成功
     * @param recordList 获取成功,返回JSONARRAY
     * @param error 错误信息
     * @param type 类型（是哪个返回的）
     */
    @Override
    public void getMultiRecordBack(boolean bok, JSONArray recordList, String error, int type) {
        if(!bok) {
            hideLoading();
            showMessage(error);
            return;
        }
        switch (type){
            case 1://编带数量修改test_lot
                break;
            case 2://编带数量修改mes_precord
                break;
            case 3://编带数量修改(修改记录)
                if(recordList.size()>0) {
                    MESPRecord record= (MESPRecord) recordList.get(0);
                    List<Integer> selectIndex = commChoice.selectIndex;
                    for(int i=0;i<selectIndex.size();i++){
                        dataList.set(selectIndex.get(i),CommonUtils.getJsonObjFromBean(recordList.get(i)));
                    }
                    commChoice.clearChoice();
                    adapter.notifyDataSetChanged();
                }
                hideLoading();
                break;

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
    public void commonBack(boolean bok, Object recordList, String error, int key) {

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
                //MESPRecord mespRecord = JSON.parseObject(jsonV.toJSONString(), MESPRecord.class);
                adapter.addRecord(jsonV);//CommonUtils.getJsonObjFromBean(mespRecord)
            }
        }
    }

    @Override
    public void clear() {
        adapter.clear();
        dataList.clear();
    }

    //按钮显示与否
    public void initButtonVisibility(){
        if ((zCnoInfo.getAttr() & CommCL.ZC_ATTR_CHARGING) == 0)
            //menu.findItem(R.id.id_menu_charging).setVisible(false);
            shangliao.setVisibility(View.GONE);//隐藏上料按钮
        if ((zCnoInfo.getAttr() & CommCL.ZC_ATTR_START) == 0)
            //menu.findItem(R.id.id_menu_start).setVisible(false);
            if ((zCnoInfo.getAttr() & CommCL.ZC_ATTR_DONE) == 0)
                //menu.findItem(R.id.id_menu_done).setVisible(false);
                if ((zCnoInfo.getAttr() & CommCL.ZC_ATTR_GLUING) == 0)
                    //menu.findItem(R.id.id_menu_gluing).setVisible(false);
                    return;
    }

    //按钮点击动作
    @OnClick({R.id.gujing_shangliao,R.id.gujing_kaigong,R.id.gujing_chuzhan,R.id.gujing_shuliang})
    public  void OnClick(View v){
        int selectNum = 0;
        String currstate = "";
        String currOP = edtOP.getText().toString().toUpperCase();
        String error = commChoice.error;

        switch (v.getId()) {

            case R.id.gujing_shangliao:
                selectNum = dataListViewContent.getCheckedItemCount();

                if (selectNum == 1) {
                    //上料
                    List<MESPRecord> list = commChoice.getSelectList();
                    MESPRecord record = list.get(0);
                    HashMap<String, Serializable> map = new HashMap<>();
                    map.put(CommCL.COMM_ZC_INFO_FLD,zCnoInfo);
                    map.put(CommCL.COMM_OP_FLD,currOP);
                    map.put(CommCL.COMM_RECORD_FLD,record);
                    map.put(CommCL.COMM_SBID_FLD,currEquipment.getId());
                    //unregisterReceiver(barcodeReceiver);
                    //jumpNextActivity(ChargingActivity.class,map);
                } else {
                    showMessage("请选择一条记录，执行上料！");
                }
                break;
            case R.id.gujing_kaigong:
                List<MESPRecord> startList = commChoice.getSelectList();
                selectNum = dataListViewContent.getCheckedItemCount();
                if(selectNum<=0){
                    showMessage("请选择记录");
                    return;
                }
                //开工
                if (!TextUtils.isEmpty(error)) {
                    showMessage(error+"，不能开工！！");
                } else {
                    currstate = startList.get(0).getState1();
                    if ("00".equals(currstate)||currstate.equals(CommCL.BATCH_STATUS_IN) || currstate.equals(CommCL.BATCH_STATUS_CHARGING)) {
                        //只有在01入站或者是02上料的状态才可以开工
                        if(zCnoInfo.getStartnum()>0){
                            int canStartNum = zCnoInfo.getStartnum()-commChoice.getStartCount();
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
                                MESPRecord record = startList.get(0);
                                CommonUtils.showOKCancel(BianDaiActivity.this, "开工首检", "设备："+currEquipment.getId()+"没有做首件检验,是否打开首检界面", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        HashMap<String, Serializable> map = new HashMap<>();
                                        map.put(CommCL.COMM_ZC_INFO_FLD,zCnoInfo);
                                        map.put(CommCL.COMM_OP_FLD,currOP);
                                        map.put(CommCL.COMM_RECORD_FLD,record);
                                        map.put(CommCL.COMM_SBID_FLD,currEquipment);
                                        myUnregisterReceiver(barcodeReceiver);
                                        jumpNextActivity(FirstInspectionActivity.class,map);
                                    }
                                });
                                break;
                            }
                        }else {

                            //给缓存数据添加hpdate时间

                            //
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
            case R.id.gujing_chuzhan:
                //出站
                selectNum = dataListViewContent.getCheckedItemCount();
                if(selectNum<=0){
                    showMessage("请选择记录");
                    return;
                }
                List<MESPRecord> outList = commChoice.getSelectList();
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
                                if(key<zCnoInfo.getPtime()&&key>=0){
                                    canout = false;
                                    err = record.getSid1()+"已开工:"+key+"分钟，需要等待"+zCnoInfo.getPtime()+"分钟，不能出站！";
                                    break;
                                }
                            }
                        }
                        if(err.length()>0){
                            showMessage(err);
                            return ;
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
            case R.id.gujing_shuliang:
                //showMessage("时间"+DateUtil.getCurrDateTime(ICL.DF_YMDHM)+"cs="+ ServicesTimeThread.sycnum);
                selectNum = dataListViewContent.getCheckedItemCount();

                if (selectNum == 1) {
                    //编带数量修改
                    List<MESPRecord> list = commChoice.getSelectList();
                    MESPRecord record = list.get(0);
                    String op = edtOP.getText().toString().toUpperCase();//操作员
                    alertWindow(BianDaiActivity.this,record,op);
                } else {
                    showMessage("请选择一条记录，执行数量修改！");
                }
                break;
        }
        return ;
    }
    //编带数量修改，弹框界面
    public void alertWindow(Context context,MESPRecord record,String op){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("编带数量修改");
        LinearLayout layout=new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        TextView text=new TextView(context);
        text.setTextSize(18);
        text.setText("   测试号："+record.getLotno());
        layout.addView(text);
        EditText qtyEid=new EditText(context);
        //qtyEid.setText(""+record.getQty());
        qtyEid.setInputType(InputType.TYPE_CLASS_NUMBER);

        layout.addView(qtyEid);
        builder.setView(layout);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                String qty= qtyEid.getText().toString();
                //Toast.makeText(context, "用户名: " + qty , Toast.LENGTH_SHORT).show();
                int oldQty=record.getQty();
                if(TextUtils.isEmpty(qty)){
                    Toast.makeText(context, "请输入数量，才能修改！" , Toast.LENGTH_SHORT).show();
                    return;
                }
                int qtyi=Integer.parseInt(qty);
                if(qtyi==0||qtyi==oldQty){
                    Toast.makeText(context, "数量为0或者数量与之前相等不能修改" , Toast.LENGTH_SHORT).show();
                    return;
                }
                showLoading();
                commPresenter.updateTestLotQty(record,op,qtyi);
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                Toast.makeText(context, "取消修改" , Toast.LENGTH_SHORT).show();
            }
        });
        builder.show();
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
                actionBarView = LayoutInflater.from(BianDaiActivity.this).inflate(R.layout.actionbar_listviewmultichoice, null);
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
            String currOP = edtOP.getText().toString().toUpperCase();
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

                    if (selectNum == 1) {
                        //上料
                        List<MESPRecord> list = getSelectList();
                        MESPRecord record = list.get(0);
                        HashMap<String, Serializable> map = new HashMap<>();
                        map.put(CommCL.COMM_ZC_INFO_FLD,zCnoInfo);
                        map.put(CommCL.COMM_OP_FLD,currOP);
                        map.put(CommCL.COMM_RECORD_FLD,record);
                        map.put(CommCL.COMM_SBID_FLD,currEquipment.getId());
                        //unregisterReceiver(barcodeReceiver);
                        //jumpNextActivity(ChargingActivity.class,map);
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
                                    MESPRecord record = startList.get(0);
                                    CommonUtils.showOKCancel(BianDaiActivity.this, "开工首检", "设备："+currEquipment.getId()+"没有做首件检验,是否打开首检界面", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            HashMap<String, Serializable> map = new HashMap<>();
                                            map.put(CommCL.COMM_ZC_INFO_FLD,zCnoInfo);
                                            map.put(CommCL.COMM_OP_FLD,currOP);
                                            map.put(CommCL.COMM_RECORD_FLD,record);
                                            map.put(CommCL.COMM_SBID_FLD,currEquipment);
                                            myUnregisterReceiver(barcodeReceiver);
                                            jumpNextActivity(FirstInspectionActivity.class,map);
                                        }
                                    });
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
            error = "";
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
