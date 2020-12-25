package com.yimeinew.activity.deviceproduction.commsub;

import android.content.*;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.annotation.Nullable;
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
import com.yimeinew.activity.databinding.ActivityCommGjBinding;
import com.yimeinew.activity.qc.FirstInspectionActivity;
import com.yimeinew.adapter.tabledataadapter.BaseTableDataAdapter;
import com.yimeinew.data.*;
import com.yimeinew.modelInterface.BaseStationBindingView;
import com.yimeinew.network.schedulers.SchedulerProvider;
import com.yimeinew.presenter.CommStationZCPresenter;
import com.yimeinew.tableui.CommMultiChoiceModeCallBack;
import com.yimeinew.tableui.TablePanelView;
import com.yimeinew.tableui.entity.HeaderRowInfo;
import com.yimeinew.utils.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

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
    @BindView(R.id.gujing_shangliao)
    Button shangliao;
    @BindView(R.id.gujing_kaigong)
    Button kaigong;
    @BindView(R.id.gujing_chuzhan)
    Button chuzhan;
    @BindView(R.id.gujing_gettime)
    Button shijian;
    @BindView(R.id.tv_sid1)
    TextView tvSid1;
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
    private CommMultiChoiceModeCallBack commChoice;
    private String mbox;
    private String GBKEY="broadcastReceiverGj";
    private JSONArray zcghsj=new JSONArray();

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

        initTableView();
        //生成假数据
//        initData();
        commPresenter = new CommStationZCPresenter(this, SchedulerProvider.getInstance());
        initButtonVisibility();
        //点胶烘烤改变加工时间--去获取制程规划的时间
        if(CommonUtils.contentEquals(zCnoInfo.getId(),"41,42",",")){
            String cont="~isnull(unit_jg_time,0)>0 or isnull(wait_time,0)>0";
            commPresenter.getAssistInfo(CommCL.AID_MES_ZCGH_SJ,cont,"",66);
        }
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
        if(zCnoInfo.getId().equals("514")){
            header = CommonUtils.getRowDataListXdk();
        }
        dataListViewContent.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        commChoice = new CommMultiChoiceModeCallBack(this,dataListViewContent,dataList);
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

    @OnEditorAction({R.id.edt_op, R.id.edt_equipment_no, R.id.edt_box, R.id.edt_sid1})
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
            if (id == R.id.edt_op) {
                showMessage("操作员【" + operationUser + "】不存在!");
            }
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
//            dataListViewContent.notifyAll();
            commPresenter.getEquipmentInfo(sbId, zCnoInfo.getId());
            return false;
        }
        if(currEquipment==null||!TextUtils.equals(currEquipment.getId(),sbId)){
            showMessage("手输设备号必须回车！");
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
                showLoading();

                commPresenter.checkBoxExit(boxId,1);
                return false;
            }
            if(!TextUtils.equals(mbox,boxId)){
                showMessage("手输料盒号必须回车！");
                return false;
            }
        }
        //入站动作
        if (id == R.id.edt_sid1) {

            String sid = edtSid1.getText().toString().toUpperCase();
            if(CommonUtils.isRepeat(TAG_NAME+"edt_sid1",sid)){
                //CommonUtils.textViewGetFocus(edtSid1);
                return true;
            }
            if (TextUtils.isEmpty(sid)) {
                showMessage(isBake()?"请输入料盒":"请输入批次号");
                CommonUtils.textViewGetFocus(edtSid1);
                return true;
            }

            if (bindSid1.containsKey(sid)) {
                showMessage(isBake()?"料盒【" + sid + "】已扫描":"该批次号【" + sid + "】已经扫描过");
                CommonUtils.textViewGetFocus(edtSid1);
                return true;
            }
            //烘烤控制批数
            if(CommonUtils.contentEquals(zCnoInfo.getId(),"1A,1B,41,42",",")){
                if(zCnoInfo.getStartnum()>0&&zCnoInfo.getStartnum()<=dataList.size()){
                    showMessage("最大可入站批数【"+zCnoInfo.getStartnum()+"】批");
                    CommonUtils.textViewGetFocus(edtSid1);
                    return true;
                }
            }
            if(!CommonUtils.isRepeat("GJ_saomao_sid1",sid)) {
                showLoading();
                //校验批次号
                CommonUtils.banDo(GBKEY);//禁用广播
                commPresenter.cache.put(sid, edtBox.getText().toString().toUpperCase());
                String zcno_id=zCnoInfo.getId();
                if(zCnoInfo.getId().equals("514")){
                    commPresenter.selectXdk(sid, zCnoInfo.getId(), currEquipment);

                }else{
                    commPresenter.getBatchInfo(sid, zCnoInfo.getId(), currEquipment);
                }
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

    }
    //退出当前Activity或者跳转到新Activity时被调用
    @Override
    protected void onStop() {
        super.onStop();

    }


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
                mbox=box;
                CommonUtils.textViewGetFocus(edtSid1);
        } else {
            showMessage(error);
            CommonUtils.textViewGetFocus(edtBox);

        }
    }

    @Override
    public void onRemoteFailed(String message) {
        /*
        commChoice.clearChoice();
        dataList.clear();
        adapter.notifyDataSetChanged();
        commPresenter.getRecordBySbId(currEquipment.getId(),zCnoInfo.getId());
        */
        hideLoading();
        CommonUtils.canDo(GBKEY);
        CommonUtils.showError(this, "onRemoteFailed="+message);

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
         //hideLoading();
            //如果bok为true  而error不为空就弹出提示页面
            if(!TextUtils.isEmpty(error)){
                //抛出异常
                MESPRecord record=JSONObject.parseObject(batchInfo.toJSONString(),MESPRecord.class);
                record.setSbid(currEquipment.getId());
                record.setOp(edtOP.getText().toString().toUpperCase());
                String djTime=batchInfo.getString("edate");
                int subTime=DateUtil.subSecond(DateUtil.getNowCurrDateTime(),djTime);
                commPresenter.saveMESPerrLog(record,"点胶入烤已超时"+(subTime/60)+"分钟,请在流程卡上张贴超时异常标识!");
                CommonUtils.showOK(this,"异常提示",error);
            }
            //生成生产记录
            Log.i(TAG_NAME, batchInfo.toJSONString());
            String op = edtOP.getText().toString().toUpperCase();//操作员
            if(entity.isGj1()&&TextUtils.isEmpty(mbox)){
                CommonUtils.canDo(GBKEY);
                hideLoading();
                showMessage("请输入料盒号,手输料盒号必须回车");
                CommonUtils.textViewGetFocus(edtBox);
                return;
            }
            if(entity.isGj1()&&!TextUtils.equals(edtBox.getText().toString().toUpperCase(),mbox)) {
                CommonUtils.canDo(GBKEY);
                hideLoading();
                showMessage("手输料盒号必须回车");
                CommonUtils.textViewGetFocus(edtBox);
                return;
            }
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
            String zcno=batchInfo.getString("zcno");
            String zcname=batchInfo.getString("zcname");
            //下单颗
            String prd_mark=batchInfo.getString("prd_mark");
            MESPRecord record = new MESPRecord(sid1, mono, zCnoInfo.getId(), sbId);
            record.setMbox(mbox);
            record.setOp(op);
            record.setZcno1(zcno1);
            record.setRemark(remark);
            record.setState1("01");
            record.setQty(qty);
            record.setPrd_no(prd_no);
            record.setPrd_name(prd_name);
            record.setFirstchk(fircheck);
            //下单颗
            record.setPrd_mark(prd_mark);
            if(TextUtils.equals("514",zcno)){
                record.setState1("03");

            }else{
                record.setState1("01");
            }
            commPresenter.makeProRecord(record);
        } else {
            CommonUtils.canDo(GBKEY);
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
            if (entity.isGj1()) {
                CommonUtils.textViewGetFocus(edtBox);
            }else{
                CommonUtils.textViewGetFocus(edtSid1);
            }
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
            if(isBake()) {
                bindSid1.put(record.getMl_mbox(),record.getMl_mbox());//缓存料盒
            }else{
                bindSid1.put(record.getSid1(), record.getSid1());
            }
            // 添加到数据列表
            adapter.addRecord(CommonUtils.getJsonObjFromBean(record));
            //更改批次状态
            String zcno=record.getZcno();
            //commPresenter.changeRecordStateOneByOne(record, CommCL.BATCH_STATUS_IN);
            if(TextUtils.equals("514",zcno)){
                commPresenter.changeLotStateOneByOne(record, CommCL.BATCH_STATUS_WORKING);
            }else{
                commPresenter.changeLotStateOneByOne(record, CommCL.BATCH_STATUS_IN);
            }


        } else {
            hideLoading();
            CommonUtils.canDo(GBKEY);
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
                CommonUtils.canDo(GBKEY);

            } else {
                CommonUtils.textViewGetFocus(edtSid1);
                CommonUtils.canDo(GBKEY);
            }
            hideLoading();
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
            showMessage("====是否是粗错了："+error+"====");

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
    public void commonBack(boolean bok, Object recordList, String error, int key) {
        switch(key){
            case 1://固晶出站判断返回结果
                if(bok){
                    List<MESPRecord> outList= (List<MESPRecord>) recordList;
                    for(MESPRecord mp:outList){
                        mp.setOp(edtOP.getText().toString().toUpperCase());
                    }
                    commPresenter.changeRecordStateBatch(outList, CommCL.BATCH_STATUS_DONE);
                }
                break;
            case 66:
                if(bok){
                    zcghsj=(JSONArray) recordList;
                }
                break;
            default:
                if(!bok){
                    hideLoading();
                    showMessage(error+"key="+key);
                }

        }
        if(!bok){
            hideLoading();
            showMessage(error+"key="+key);
        }
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
                //MESPRecord mespRecord = JSON.parseObject(jsonV.toJSONString(), MESPRecord.class);//不用转，转了会报错
                adapter.addRecord(jsonV);//CommonUtils.getJsonObjFromBean(mespRecord)
            }
        }
    }

    @Override
    public void clear() {
        adapter.clear();
        this.dataList.clear();
    }

    //按钮显示与否
    public void initButtonVisibility(){
        if(isBake()){
            tvSid1.setText("料盒号");
            edtSid1.setHint("料盒号");
        }
        if ((zCnoInfo.getAttr() & CommCL.ZC_ATTR_CHARGING) == 0)
            //menu.findItem(R.id.id_menu_charging).setVisible(false);
            shangliao.setVisibility(View.GONE);//隐藏上料按钮
        if ((zCnoInfo.getAttr() & CommCL.ZC_ATTR_START) == 0)
            //menu.findItem(R.id.id_menu_start).setVisible(false);
           kaigong.setVisibility(View.GONE);
        if ((zCnoInfo.getAttr() & CommCL.ZC_ATTR_DONE) == 0)
            //menu.findItem(R.id.id_menu_done).setVisible(false);
        if ((zCnoInfo.getAttr() & CommCL.ZC_ATTR_GLUING) == 0)
            //menu.findItem(R.id.id_menu_gluing).setVisible(false);
            return;

    }

    //按钮点击动作
    @OnClick({R.id.gujing_shangliao,R.id.gujing_kaigong,R.id.gujing_chuzhan,R.id.gujing_gettime})
    public  void OnClick(View v){
        int selectNum = 0;
        String currstate = "";
        String currOP = edtOP.getText().toString().toUpperCase();

        String error = "";//commChoice.error;

        switch (v.getId()) {

            case R.id.gujing_shangliao:
                selectNum = dataListViewContent.getCheckedItemCount();

                if (selectNum == 1) {
                    //上料
                    List<MESPRecord> list = JSONArray.parseArray(JSON.toJSONString(commChoice.getSelectList()),MESPRecord.class);
                    MESPRecord record = list.get(0);
                    HashMap<String, Serializable> map = new HashMap<>();
                    map.put(CommCL.COMM_ZC_INFO_FLD,zCnoInfo);
                    map.put(CommCL.COMM_OP_FLD,currOP);
                    map.put(CommCL.COMM_RECORD_FLD,record);
                    map.put(CommCL.COMM_SBID_FLD,currEquipment.getId());
                    myUnregisterReceiver(barcodeReceiver);
                    jumpNextActivity(ChargingActivity.class,map);
                } else {
                    showMessage("请选择一条记录，执行上料！");
                }
                break;
            case R.id.gujing_kaigong:
                List<MESPRecord> startList = JSONArray.parseArray(JSON.toJSONString(commChoice.getSelectList()),MESPRecord.class);
                selectNum = dataListViewContent.getCheckedItemCount();
                if(selectNum<=0){
                    showMessage("请选择记录");
                    return;
                }
                String temp="";
                for(MESPRecord mr:startList){
                    if(TextUtils.isEmpty(temp)){
                        temp=mr.getState1();
                    }
                    if(!TextUtils.equals(temp,mr.getState1())){
                        error="选中的记录，状态不一致";
                    }
                }
                //开工
                if (!TextUtils.isEmpty(error)) {
                    showMessage(error+"，不能开工！！");
                } else {
                    currstate = startList.get(0).getState1();

                    if ("00".equals(currstate)||currstate.equals(CommCL.BATCH_STATUS_IN) || currstate.equals(CommCL.BATCH_STATUS_CHARGING)) {
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
                        String op=edtOP.getText().toString().toUpperCase();
                        String opv = CommCL.sharedPreferences.getString(op, "");
                        if(TextUtils.isEmpty(opv)){
                            showMessage("请输入正确操作员");
                            return;
                        }
                        for(MESPRecord mp:startList){
                            mp.setOp(op);
                        }
                        if(cr.getFirstchk()==1){//需要首件检验
                            if(currEquipment.getFirstchk()==1 && currEquipment.getPrdno().equals(cr.getPrd_no())){

                                showLoading();
                                commPresenter.changeRecordStateBatch(startList,CommCL.BATCH_STATUS_WORKING);
                                break;
                            }else{
                                MESPRecord record = startList.get(0);
                                CommonUtils.showOKCancel(CommGJActivity.this, "开工首检", "设备："+currEquipment.getId()+"没有做首件检验,是否打开首检界面", new DialogInterface.OnClickListener() {
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
                List<MESPRecord> outList = JSONArray.parseArray(JSON.toJSONString(commChoice.getSelectList()),MESPRecord.class);
                String op=edtOP.getText().toString().toUpperCase();
                String opv = CommCL.sharedPreferences.getString(op, "");
                if(TextUtils.isEmpty(opv)){
                    showMessage("请输入正确操作员");
                    return;
                }
                for(MESPRecord mp:outList){
                    mp.setOp(op);
                }
                String temp1="";
                for(MESPRecord mr:outList){
                    if(TextUtils.isEmpty(temp1)){
                        temp1=mr.getState1();
                    }
                    if(!TextUtils.equals(temp1,mr.getState1())){
                        error="选中的记录，状态不一致";
                    }
                }
                if (!TextUtils.isEmpty(error)) {
                    showMessage(error+"，不能出站！！");
                } else {
                    currstate = outList.get(0).getState1();
                    if (CommCL.BATCH_STATUS_WORKING.equals(currstate)) {
                        //校验出站时间
                        boolean canout = true;
                        String err = "";
                        int ptime=zCnoInfo.getPtime();
                        MESPRecord m1=outList.get(0);
                        int ghpt=getZCGHSJ(m1.getProc_id(),m1.getZcno(),"unit_jg_time");
                        if(ghpt>0){ptime=ghpt;}
                        if(ptime>0){
                            for(int i=0;i<outList.size();i++){
                                MESPRecord record = outList.get(i);
                                String hpdate = record.getHpdate();
                                int key = DateUtil.subDate(DateUtil.getCurrDateTime(ICL.DF_YMDT),hpdate,4);
                                if(key<ptime&&key>=0&&!CommCL.isTest){
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
                            if(TextUtils.indexOf(zCnoInfo.getName(),"固晶SMD")<0) {
                                commPresenter.changeRecordStateBatch(outList, CommCL.BATCH_STATUS_DONE);//非固晶可以直接出站
                            }else{
                                commPresenter.getCheckWaferQty(zCnoInfo.getId(),outList,1);
                            }
                        }
                    }else{
                        showMessage("选中记录的状态不是生产中，不可以出站");
                        break;
                    }
                }
                break;
            case R.id.gujing_gettime:
                //showMessage("时间"+DateUtil.getCurrDateTime(ICL.DF_YMDHM)+"cs="+ ServicesTimeThread.sycnum);
                List<MESPRecord> list = JSONArray.parseArray(JSON.toJSONString(commChoice.getSelectList()),MESPRecord.class);
                String str="";
                for(MESPRecord mr:list){
                    str+=mr.getSid1()+";";
                }
                edtBox.setText(list.size()+":"+str);
                break;

        }
        return ;
    }


    public boolean isBake(){
        if(CommCL.BAKE_USE_MBOX&& ToolUtils.containValue(CommCL.isBake,zCnoInfo.getId())){
            return true;
        }else{
            return false;
        }
    }
    private int getZCGHSJ(String proc_id,String zcno,String key){
        for(int i=0;i<zcghsj.size();i++){
            JSONObject obj=zcghsj.getJSONObject(i);
            if(TextUtils.equals(proc_id,obj.getString("sid"))&&TextUtils.equals(zcno,obj.getString("zcno"))){
                return (obj.containsKey(key))?obj.getInteger(key):0;
            }
        }
        return 0;
    }
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


}
