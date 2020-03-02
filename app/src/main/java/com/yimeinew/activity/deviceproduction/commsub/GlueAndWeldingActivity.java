package com.yimeinew.activity.deviceproduction.commsub;

import android.app.AlertDialog;
import android.content.*;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.*;
import android.widget.*;
import butterknife.*;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.shade.com.alibaba.fastjson.JSON;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.shade.com.alibaba.fastjson.JSONArray;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.shade.com.alibaba.fastjson.JSONObject;
import com.yimeinew.activity.R;
import com.yimeinew.activity.base.BaseActivity;
import com.yimeinew.activity.deviceproduction.AddGluingActivity;
import com.yimeinew.activity.qc.FirstInspectionActivity;
import com.yimeinew.adapter.SpinnerAdapterImpl;
import com.yimeinew.adapter.tabledataadapter.BaseTableDataAdapter;
import com.yimeinew.data.EquipmentInfo;
import com.yimeinew.data.MESPRecord;
import com.yimeinew.data.ZCInfo;
import com.yimeinew.entity.Pair;
import com.yimeinew.modelInterface.BaseStationBindingView;
import com.yimeinew.network.schedulers.SchedulerProvider;
import com.yimeinew.presenter.CommStationZCPresenter;
import com.yimeinew.tableui.TablePanelView;
import com.yimeinew.tableui.entity.HeaderRowInfo;
import com.yimeinew.utils.CommCL;
import com.yimeinew.utils.CommonUtils;
import com.yimeinew.utils.DateUtil;
import com.yimeinew.utils.ICL;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 点胶和焊接工序的Activity处理
 *
 * @Auther: fengzejiang1987@163.com
 * @Date : 2019/1/2 16:31
 */
public class GlueAndWeldingActivity extends BaseActivity implements BaseStationBindingView {

    @BindView(R.id.tx_box)
    TextView txbox;
    @BindView(R.id.tx_box1)
    TextView txbox1;
    private ZCInfo zcInfo;
    private String zcno;
    public static final String Title = "通用工站-->";
    //private boolean isDJ = false;

    private HashMap<String,Integer> cache=new HashMap<>();
    //注入输入框
    @BindView(R.id.edt_op)
    EditText editTextOp;//作业员
    @BindView(R.id.edt_equipment_no)
    EditText editTextSbId;//设备号
    @BindView(R.id.edt_box)
    EditText editTextBox;//料盒号
    @BindView(R.id.edt_box1)
    EditText editTextBox1;//料盒号1
    @BindView(R.id.edt_sid1)
    EditText editTextSid1;//批次号
    //按钮
    @BindView(R.id.gujing_shangliao)
    Button shangliao;
    @BindView(R.id.gujing_kaigong)
    Button kaigong;
    @BindView(R.id.gujing_chuzhan)
    Button chuzhan;
    @BindView(R.id.gujing_jiajiao)
    Button jiajiao;
    @BindView(R.id.gujing_yichang)
    Button dianjiaoyc;
    //数据表格
    @BindView(R.id.table_view)
    TablePanelView tableView;
    @BindView(R.id.data_list_content)
    ListView dataListViewContent;
    List<JSONObject> dataList;
    BaseTableDataAdapter dataAdapter;
    private MultiChoiceModeCallback commChoice;

    private CommStationZCPresenter commStationZCPresenter;
    private String GBKEY="broadcastReceiverHX";

    String currOP = "";//当前操作员
    EquipmentInfo currEquipMent;
    String currBox = "", currBox1 = "";//两个料盒号
    String currMONO = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comm_gluing);
        ButterKnife.bind(this);
        zcInfo = (ZCInfo) getIntent().getSerializableExtra(CommCL.COMM_ZC_INFO_FLD);
        zcno=zcInfo.getId();
        //isDJ = zcInfo.getId().equals("31");
        this.setTitle(Title + zcInfo.getName());//设置标题
        txbox.setText(getTextByIndex(0));
        txbox1.setText(getTextByIndex(1));
        setButtonVisibility();
        initTableView();
        commStationZCPresenter = new CommStationZCPresenter(this, SchedulerProvider.getInstance());

    }

    public void broadEdit(EditText editText,String value){
        if(canGetMessage){
           editText.setText(value);
           OnEditorAction(editText);
        }
    }

    /***
     * 用ButterKnife，注入输入框回车事件
     * @param editText 文本框
     * @return
     */
    @OnEditorAction({R.id.edt_op, R.id.edt_equipment_no, R.id.edt_sid1, R.id.edt_box, R.id.edt_box1})
    public boolean OnEditorAction(EditText editText) {
        int id = editText.getId();
        currOP = editTextOp.getText().toString().toUpperCase();
        String str1 = editText.getText().toString().toUpperCase();
        if (TextUtils.isEmpty(currOP)) {
            showMessage("请输入作业员！");
            CommonUtils.textViewGetFocus(editTextOp);
            return false;
        }
        String op = CommCL.sharedPreferences.getString(currOP, "");
        if (TextUtils.isEmpty(op)) {
            if (id == R.id.edt_op) {
                showMessage("输入的作业员" + currOP + "不存在！");
            }
            CommonUtils.textViewGetFocus(editTextOp);
            return false;
        }

        if (id == R.id.edt_op) {
            CommonUtils.textViewGetFocus(editTextSbId);
            return true;
        }

        if (id == R.id.edt_equipment_no) {
            if (TextUtils.isEmpty(str1)) {
                showMessage("请输入设备号！");
                CommonUtils.textViewGetFocus(editTextSbId);
                return false;
            }
            if (currEquipMent == null||true) {
                showLoading();
                //获取设备和设备上生产占用的制程
                commStationZCPresenter.getEquipmentInfo(str1, zcInfo.getId());
                return true;
            } else {
                if (currEquipMent.getId().equals(str1)) {
                    CommonUtils.textViewGetFocus(editTextBox);
                    return true;
                } else {
                    commStationZCPresenter.getEquipmentInfo(str1, zcInfo.getId());
                    return true;
                }
            }
        }
        if (currEquipMent == null) {
            showMessage("请输入设备号！");
            CommonUtils.textViewGetFocus(editTextSbId);
            return false;
        }

        if (R.id.edt_box == id) {
            if (checkBoxEmpt(str1, 0))
                return false;
            showLoading();
            commStationZCPresenter.checkBoxExit(str1, 0);
            return true;
        }

        if (checkBoxEmpt(currBox, 0))
            return false;

        if (R.id.edt_box1 == id) {
            if (checkBoxEmpt(str1, 1))
                return false;
            showLoading();
            commStationZCPresenter.checkBoxExit(str1, 1);

            return true;
        }

        if (checkBoxEmpt(currBox1, 1))
            return false;

        /*入站动作*/
        if (R.id.edt_sid1 == id) {
            if (TextUtils.isEmpty(str1)) {
                showMessage("请输入生产批次！");
                CommonUtils.textViewGetFocus(editTextSid1);
                return false;
            }
            if(!CommonUtils.isRepeat("hanxian_sid1",str1)) {

                showLoading();
                CommonUtils.banDo(GBKEY);//禁用广播

                commStationZCPresenter.getBatchInfo(str1, zcInfo.getId(), currEquipMent);
            }
            return true;
        }
        return false;
    }
    @Override
    public void onRemoteFailed(String message) {
        hideLoading();
        CommonUtils.canDo(GBKEY);
        CommonUtils.showError(this, "onRemoteFailed="+message);
        /*
            showLoading();
            dataList.clear();
            dataAdapter.notifyDataSetChanged();
            commChoice.clearChoice();
            commStationZCPresenter.getRecordBySbId(currEquipMent.getId(), zcInfo.getId());
        */

    }

    /***
     * 注册表格的行点击事件
     * 表格行点击事件
     * @param parent
     * @param view
     * @param position
     * @param id
     */
    @OnItemClick(R.id.data_list_content)
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        boolean bSelect = view.isSelected();
        dataListViewContent.setItemChecked(position, !bSelect);
    }

    public boolean checkBoxState(String str1,int key){

        String err = getTextByIndex(key);
        switch (key) {
            case 0:
                if(!(cache.get(currBox)== CommCL.BOX_STATE_WORKING)){
                    showMessage(err+"是空闲状态！");
                    return false;
                }
                break;
            case 1:
                if(cache.get(currBox1)== CommCL.BOX_STATE_WORKING){
                    showMessage(err+"料盒没有解绑");
                    CommonUtils.textViewGetFocus(key == 0 ? editTextBox : editTextBox1);
                    return false;
                }else{
                    cache.put(currBox1,CommCL.BOX_STATE_WORKING);
                }
                break;
        }


        return true;

    }
    /**
     * 检查料盒号是否为空
     *
     * @param str1 料盒编码
     * @param key  第几个
     * @return 空返回true, 否则返回false
     */
    public boolean checkBoxEmpt(String str1, int key) {
        if (TextUtils.isEmpty(str1)) {
            String err = getTextByIndex(key);
            showMessage("请输入" + err+",或者手输料盒号必须回车");
            CommonUtils.textViewGetFocus(key == 0 ? editTextBox : editTextBox1);
            return true;
        }
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        String str1=editTextSbId.getText().toString().toUpperCase();
        dataList.clear();
        dataAdapter.notifyDataSetChanged();
        if(currEquipMent!=null){
            showLoading();
            commStationZCPresenter.getEquipmentInfo(currEquipMent.getId(), zcInfo.getId());
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
    /**
     * 锁屏的管理类叫KeyguardManager，
     * 通过调用其内部类KeyguardLockmKeyguardLock的对象的disableKeyguard方法可以取消系统锁屏，
     * newKeyguardLock的参数用于标识是谁隐藏了系统锁屏
     */
    /*
    private BroadcastReceiver mScreenOReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action.equals("android.intent.action.SCREEN_ON")) {
                System.out.println("—— SCREEN_ON ——");
            } else if (action.equals("android.intent.action.SCREEN_OFF")) {
                System.out.println("—— SCREEN_OFF ——");
            }
        }

    };
*/
    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    /***
     * 初始化表格
     */
    private void initTableView() {
        dataList = new ArrayList();
        List<HeaderRowInfo> header = CommonUtils.getRowDataList();
        dataListViewContent.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        commChoice = new MultiChoiceModeCallback();
        dataListViewContent.setMultiChoiceModeListener(commChoice);
        dataAdapter = new BaseTableDataAdapter(this, tableView, dataListViewContent, dataList, header);
        dataAdapter.setTitle("项次");
        dataAdapter.setTitleHeight(100);
        dataAdapter.setSwipeRefreshEnabled(false);
        dataAdapter.setTitleWidth(80);
        tableView.setAdapter(dataAdapter);
    }

    /***
     *
     * @param bok 是否存在该料盒号
     * @param error 错误信息,或者是json字符串
     * @param key 界面上会有多个box输入框，key值代表第几个
     */
    @Override
    public void checkMboxCallBack(boolean bok, String error, int key) {
        hideLoading();
        if (bok) {
            JSONObject json = JSONObject.parseObject(error);
            String id = json.getString("id");
            int state = json.getInteger("state");
            switch (key) {
                case 0:
                    if(!TextUtils.isEmpty(id)){
                        if (state == CommCL.BOX_STATE_WORKING) {
                            currBox = id;
                            CommonUtils.textViewGetFocus(editTextBox1);
                            cache.put(id, state);
                        }else {
                            showMessage("该料盒号【" + id + "】的状态是空闲状态");
                            currBox = "";
                            CommonUtils.textViewGetFocus(editTextBox);
                            break;
                        }
                    }else{
                        showMessage("该料盒号不存在");
                    }
                    break;

                    /*
                    if (state == CommCL.BOX_STATE_WORKING) {
                        currBox = id;
                        CommonUtils.textViewGetFocus(editTextBox1);
                        break;
                    } else {
                        showMessage("该料盒号【" + id + "】的状态是空闲状态");
                        currBox = "";
                        CommonUtils.textViewGetFocus(editTextBox);
                        break;
                    }
                    */
                case 1:
                    if(!TextUtils.isEmpty(id)){
                        if (state == CommCL.BOX_STATE_WORKING) {
                            showMessage("该料盒号【" + id + "】没有解绑");
                            currBox1 = "";
                            CommonUtils.textViewGetFocus(editTextBox1);
                            break;
                        } else {
                            currBox1 = id;
                            CommonUtils.textViewGetFocus(editTextSid1);
                            cache.put(id, state);
                        }
                    }else{
                        showMessage("该料盒号不存在");
                    }
                    break;
                    /*
                    if (state == CommCL.BOX_STATE_WORKING) {
                        showMessage("该料盒号【" + id + "】没有解绑");
                        currBox1 = "";
                        CommonUtils.textViewGetFocus(editTextBox1);
                        break;
                    } else {
                        currBox1 = id;
                        CommonUtils.textViewGetFocus(editTextSid1);
                        break;
                    }
                    */
            }
        } else {
            showMessage(error);
            switch (key) {
                case 0:
                    CommonUtils.textViewGetFocus(editTextBox);
                    currBox = "";
                    break;
                case 1:
                    currBox1 = "";
                    CommonUtils.textViewGetFocus(editTextBox1);
                    break;
            }
        }
    }

    /***
     * 获取批次记录
     * @param bok 是否存在
     * @param batchInfo 如果存在，返回JSON格式的数据
     * @param error 如果不存在返回错误信息
     */
    @Override
    public void checkSidCallBack(boolean bok, JSONObject batchInfo, String error) {
        if (bok) {
            String box = batchInfo.getString("mbox");
            String sid1 = editTextSid1.getText().toString().toUpperCase();//批次号
            if(TextUtils.isEmpty(currBox1)||TextUtils.isEmpty(currBox)) {
                hideLoading();
                CommonUtils.canDo(GBKEY);
                showMessage("手输料盒号需要回车,或者信息未返回！");
                return;
            }
            if(!TextUtils.equals(currBox1,editTextBox1.getText().toString().toUpperCase())){
                hideLoading();
                CommonUtils.canDo(GBKEY);
                CommonUtils.textViewGetFocus(editTextBox1);
                showMessage("手输料盒号需要回车,或者信息未返回！");
                return;
            }
            if(!TextUtils.equals(currBox,editTextBox.getText().toString().toUpperCase())){
                hideLoading();
                CommonUtils.canDo(GBKEY);
                CommonUtils.textViewGetFocus(editTextBox);
                showMessage("手输料盒号需要回车,或者信息未返回！");
                return;
            }
            if(!checkBoxState(currBox1,1)){
                hideLoading();
                CommonUtils.canDo(GBKEY);
                return;
            }
            if(TextUtils.equals(currBox,currBox1)){
                showMessage("两个料盒一样无法倒料盒！");
                hideLoading();
                CommonUtils.canDo(GBKEY);
                return;
            }
            if (!TextUtils.equals(currBox,box)) {
                hideLoading();
                CommonUtils.canDo(GBKEY);
                if (TextUtils.isEmpty(box)) {
                    showMessage("当前批次【" + sid1 + "】没有绑定的料盒");
                    return;
                } else {
                    showMessage("当前料盒号绑定不是该批次【" + sid1 + "】，请找领班确认");
                    return;
                }
            }
            /*
            if(!checkBoxState(currBox,0)){
                hideLoading();
                return;
            }
            */
            //保存生产记录
            String op = editTextOp.getText().toString().toUpperCase();//操作员
            String mono = batchInfo.getString("sid");
            String zcno1 = batchInfo.getString("zcno1");
            String remark = batchInfo.getString("remark");
            int qty = batchInfo.getInteger("qty");
            String prd_no = batchInfo.getString("prd_no");
            String prd_name = batchInfo.getString("prd_name");
            int fircheck = batchInfo.getInteger("fircheck");
            MESPRecord record = new MESPRecord(sid1, mono, zcInfo.getId(), currEquipMent.getId());
            record.setSlkid(mono);
            record.setZcno1(zcno1);
            record.setOp(op);
            record.setState1("01");
            record.setRemark(remark);
            record.setQty(qty);
            record.setPrd_no(prd_no);
            record.setPrd_name(prd_name);
            record.setFirstchk(fircheck);
            record.setMbox(currBox1);


            commStationZCPresenter.makeProRecord(record);
            return;

        } else {
            hideLoading();
            CommonUtils.canDo(GBKEY);
            showMessage(error);
            CommonUtils.textViewGetFocus(editTextSid1);
        }
    }

    /***
     *
     * @param bok 是否存在
     * @param sbInfo 设备信息
     * @param error 如果不存在返回错误信息
     */
    @Override
    public void checkSbIdCallBack(boolean bok, EquipmentInfo sbInfo, String error) {
        dataList.clear();
        dataAdapter.notifyDataSetChanged();
        commChoice.clearChoice();
        if (bok) {
            currEquipMent = sbInfo;
            //判断是否点胶制程，如果是，就不要直接查记录。而且查改设备上面是否有记录，且这个记录是否与当前相同

            commStationZCPresenter.getRecordBySbId(sbInfo.getId(), zcInfo.getId());
//          CommonUtils.textViewGetFocus(editTextBox);
        } else {
            hideLoading();
            currEquipMent = null;
            showMessage(error);
            CommonUtils.textViewGetFocus(editTextSbId);
        }
    }

    /***
     * 添加行记录到当前列表
     * @param jsons json格式的数据
     */
    @Override
    public void addRow(Object jsons) {
        if (jsons instanceof JSONObject) {
            JSONObject record = (JSONObject) jsons;
            dataAdapter.addRecord(record);
        }
        if (jsons instanceof JSONArray) {
            JSONArray array = (JSONArray) jsons;
            for (int k = 0; k < array.size(); k++) {
                JSONObject jsonObject = array.getJSONObject(k);
                //MESPRecord mespRecord = JSON.parseObject(jsonObject.toJSONString(), MESPRecord.class);
                dataAdapter.addRecord(jsonObject);//CommonUtils.getJsonObjFromBean(mespRecord)
            }
        }
    }

    @Override
    public void clear() {
        this.dataAdapter.clear();
        this.dataList.clear();
    }

    @Override
    public boolean onEditTextKeyDown(EditText editText) {
        return OnEditorAction(editText);
    }

    /***
     *
     * @param bok
     * @param sbInfo
     * @param error
     */
    @Override
    public void checkRecordCallBack(boolean bok, JSONObject sbInfo, String error) {
        hideLoading();
        if (!bok) {
            showMessage(error);
        }

        CommonUtils.textViewGetFocus(editTextBox);
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
    /***
     * 保存生产记录回调
     * @param bok  是否成功
     * @param record 如果成功，返回生产记录，反之，返回null
     * @param error 保存失败，返回错误信息
     */
    @Override
    public void saveRecordBack(boolean bok, Object record, String error) {
        if (bok) {
            MESPRecord mespRecord = (MESPRecord) record;
            dataAdapter.addRecord(CommonUtils.getJsonObjFromBean(record));
            //更改生产记录状态//绑定料盒号
            commStationZCPresenter.changeLotStateOneByOne(mespRecord, CommCL.BATCH_STATUS_IN);
        } else {
            hideLoading();
            CommonUtils.canDo(GBKEY);
            showMessage(error);
        }
    }

    @Override
    public void changeRecordStateBack(boolean bok, Object record, String error) {

        if (bok) {
            MESPRecord mespRecord = (MESPRecord) record;
            int key = -1;
            for (int i = 0; i < dataList.size(); i++) {
                MESPRecord record1 = JSONObject.parseObject(dataList.get(i).toJSONString(), MESPRecord.class);
                if (record1.getSid().equals(mespRecord.getSid())) {
                    key = i;
                }
            }
            if (key == -1) {
                dataList.add(CommonUtils.getJsonObjFromBean(mespRecord));

            } else {
                dataList.set(key, CommonUtils.getJsonObjFromBean(mespRecord));

            }
            dataAdapter.notifyDataSetChanged();
            commStationZCPresenter.bindingBox(mespRecord.getMbox(), mespRecord.getSid1(), zcInfo.getId());
            CommonUtils.textViewGetFocus(editTextSid1);
            /*入站清理缓存*/
            /*入站清理缓存*/
            currBox="";
            currBox1="";
            cache.clear();
            CommonUtils.canDo(GBKEY);
            hideLoading();
            return;
        } else {
            hideLoading();
            showMessage(error);
            CommonUtils.canDo(GBKEY);
            CommonUtils.textViewGetFocus(editTextSid1);
        }
    }

    /***
     * 批次更改生产记录回调
     * @param bok 是否成功
     * @param recordList 如果处理成功，返回当前记录
     * @param error 如果失败，返回当前错误信息
     */
    @Override
    public void changeMultiRecordStateBack(boolean bok, List<MESPRecord> recordList, String error) {
        hideLoading();
        if (bok) {
            List<Integer> selectIndex = commChoice.selectIndex;
            boolean bDone = CommCL.BATCH_STATUS_DONE.equals(error);
            if (!bDone) {

                for (int i = 0; i < selectIndex.size(); i++) {
                    dataList.set(selectIndex.get(i), CommonUtils.getJsonObjFromBean(recordList.get(i)));
                }
                dataAdapter.notifyDataSetChanged();
                commChoice.clearChoice();
            } else {
                commChoice.clearChoice();
                dataList.clear();
                dataAdapter.notifyDataSetChanged();
                commStationZCPresenter.getRecordBySbId(currEquipMent.getId(), zcInfo.getId());
            }
        } else {
            showMessage(error);
        }
    }

    @Override
    public void getMultiRecordBack(boolean bok, JSONArray recordList, String error, int type) {

        if(bok){

        }else{
            hideLoading();
            showMessage(error);
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
        switch(key){
            case 1:
                if(bok){
                    MESPRecord record= (MESPRecord) recordList;
                    List<Integer> selectIndex = commChoice.selectIndex;
                    for(int i=0;i<selectIndex.size();i++){
                        dataList.set(selectIndex.get(i),CommonUtils.getJsonObjFromBean(record));
                    }
                    commChoice.clearChoice();
                    dataAdapter.notifyDataSetChanged();
                    hideLoading();

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

    //按钮点击动作
    @OnClick({R.id.gujing_shangliao,R.id.gujing_kaigong,R.id.gujing_chuzhan,R.id.gujing_jiajiao,R.id.gujing_yichang})
    public  void OnClick(View v){

        int selectNum = 0;
        String currState = "";
        String error = "";
        switch (v.getId()) {

            case R.id.gujing_shangliao:
                selectNum = dataListViewContent.getCheckedItemCount();
                if (selectNum == 1) {
                    //上料
                    List<MESPRecord> list = commChoice.getSelectItem();
                    MESPRecord record = list.get(0);
                    HashMap<String, Serializable> map = new HashMap<>();
                    map.put(CommCL.COMM_ZC_INFO_FLD, zcInfo);
                    map.put(CommCL.COMM_OP_FLD, currOP);
                    map.put(CommCL.COMM_RECORD_FLD, record);
                    map.put(CommCL.COMM_SBID_FLD, currEquipMent.getId());
                    myUnregisterReceiver(barcodeReceiver);
                    jumpNextActivity(ChargingActivity.class, map);
                } else {
                    showMessage("请选择一条记录上料！");
                }
                break;
            case R.id.gujing_kaigong:
                //开工
                List<MESPRecord> startList = commChoice.getSelectItem();
                selectNum = dataListViewContent.getCheckedItemCount();
                if(selectNum<=0){
                    showMessage("请选择记录");
                    return;
                }
                if (!TextUtils.isEmpty(error)) {
                    showMessage(error + "，不能开工！！");
                    break;
                } else {
                    currState = startList.get(0).getState1();
                    if ("00".equals(currState)||currState.equals(CommCL.BATCH_STATUS_IN) || currState.equals(CommCL.BATCH_STATUS_CHARGING)) {
                        //只有在01入站或者是02上料的状态才可以开工
                        int start = zcInfo.getStartnum();
                        if (start > 0) {
                            int canStartNum = zcInfo.getStartnum() - commChoice.getStartCount();
                            if (canStartNum <= 0) {
                                showMessage("制成" + zcInfo.getId() + "最多可开工数为【" + zcInfo.getStartnum() + "】");
                                break;
                            }
                            if (startList.size() > canStartNum) {
                                showMessage("制成" + zcInfo.getId() + "最多可开工数为【" + zcInfo.getStartnum() + "】还可以开工:【" + canStartNum + "】");
                                break;
                            }
                        }
                        MESPRecord record = startList.get(0);
                        if (record.getFirstchk() == 1) {
                            int bf = currEquipMent.getFirstchk();
                            if (bf == 1 && currEquipMent.getPrdno().equals(record.getPrd_no())) {
                                if(TextUtils.equals(zcInfo.getId(),"31")){//点胶校验胶水是否过期，再调用开工代码
                                    showLoading();
                                    //commStationZCPresenter.changeRecordStateBatch(startList, CommCL.BATCH_STATUS_WORKING);
                                    commStationZCPresenter.getMachineGlueInfo(record.getSbid(),record.getSlkid(),startList);
                                }else {
                                    showLoading();
                                    commStationZCPresenter.changeRecordStateBatch(startList, CommCL.BATCH_STATUS_WORKING);
                                }
                                break;
                            } else {
                                //showMessage("设备：" + currEquipMent.getId() + "没有做首件检验！");
                                //MESPRecord record = startList.get(0);

                                CommonUtils.showOKCancel(GlueAndWeldingActivity.this, "开工首检", "设备："+currEquipMent.getId()+"没有做首件检验,是否打开首检界面", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        HashMap<String, Serializable> map = new HashMap<>();
                                        map.put(CommCL.COMM_ZC_INFO_FLD,zcInfo);
                                        map.put(CommCL.COMM_OP_FLD,currOP);
                                        map.put(CommCL.COMM_RECORD_FLD,record);
                                        map.put(CommCL.COMM_SBID_FLD,currEquipMent);
                                        myUnregisterReceiver(barcodeReceiver);
                                        jumpNextActivity(FirstInspectionActivity.class,map);
                                    }
                                });
                                break;
                            }
                        } else {
                            showLoading();
                            commStationZCPresenter.changeRecordStateBatch(startList, CommCL.BATCH_STATUS_WORKING);
                            break;
                        }
                    } else {
                        showMessage("选中的记录不可以开工!");
                        break;
                    }
                }
            case R.id.gujing_chuzhan:
                //完工出站
                List<MESPRecord> outList = commChoice.getSelectItem();
                selectNum = dataListViewContent.getCheckedItemCount();
                if(selectNum<=0){
                    showMessage("请选择记录");
                    return;
                }
                if (!TextUtils.isEmpty(error)) {
                    showMessage(error + "，不能出站！！");
                } else {
                    currState = outList.get(0).getState1();
                    if (CommCL.BATCH_STATUS_WORKING.equals(currState)) {
                        //校验出站时间
                        boolean doDone = true;
                        String errStr = "";
                        if (zcInfo.getPtime() > 0) {
                            for (int i = 0; i < outList.size(); i++) {
                                MESPRecord cr = outList.get(i);
                                int key = DateUtil.subDate(DateUtil.getCurrDateTime(ICL.DF_YMDT), cr.getHpdate(), 4);
                                if (key < zcInfo.getPtime() && key >= 0&&!CommCL.isTest) {
                                    doDone = false;
                                    errStr = cr.getSid1() + "已开工:" + key + "分钟，需要等待" + zcInfo.getPtime() + "分钟，不能出站！";
                                    break;
                                }
                            }
                        }
                        if (!TextUtils.isEmpty(errStr)) {
                            showMessage(errStr);
                            return ;
                        }
                        if (doDone) {
                            showLoading();
                            commStationZCPresenter.changeRecordStateBatch(outList, CommCL.BATCH_STATUS_DONE);
                            return ;
                        }
                    } else {
                        showMessage("选中记录的状态不是生产中，不可以出站");
                        break;
                    }
                }

            case R.id.gujing_jiajiao:
                //加胶
                selectNum = dataListViewContent.getCheckedItemCount();
                if(selectNum<=0){
                    showMessage("请选择记录");
                    return;
                }
                HashMap<String, Serializable> map = new HashMap<>();
                map.put(CommCL.COMM_OP_FLD, currOP);
                map.put(CommCL.COMM_SBID_FLD, currEquipMent.getId());
                myUnregisterReceiver(barcodeReceiver);
                jumpNextActivity(AddGluingActivity.class, map);
                break;
            case R.id.gujing_yichang:
                //点胶异常按钮
                selectNum = dataListViewContent.getCheckedItemCount();

                if (selectNum == 1) {

                    List<MESPRecord> list = commChoice.getSelectItem();
                    MESPRecord record = list.get(0);
                    String op = editTextOp.getText().toString().toUpperCase();//操作员
                    alertWindow(this,record,op);
                } else {
                    showMessage("请选择一条记录，执行数量修改！");
                }

                break;
        }
        return ;
    }
    //点胶异常，弹框界面
    public void alertWindow(Context context,MESPRecord record,String op){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("点胶异常登记");
        LinearLayout layout=new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        TextView text=new TextView(context);
        text.setTextSize(18);
        text.setText("   批次号："+record.getSid1());
        layout.addView(text);
        //添加UI选择器
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View layout_list = layoutInflater.inflate(R.layout.comm_spinner_body, null);
        Spinner spinner=(Spinner)layout_list.findViewById(R.id.comm_spinner);
        spinner.setAdapter(SpinnerAdapterImpl.getSpinnerAdapter(getApplicationContext(),CommCL.DJYCSM));
        layout.addView(spinner);
        builder.setView(layout);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                Pair p= (Pair) spinner.getSelectedItem();
                String value=p.value;
                if(!TextUtils.isEmpty(value)){
                    showLoading();
                    int v=Integer.parseInt(value);
                    commStationZCPresenter.updatePrecodeYCState(record,op,v);
                }else{
                    Toast.makeText(context, "没选择不会修改" , Toast.LENGTH_SHORT).show();
                }
                //showMessage(p.key+" "+p.value);

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
    private class MultiChoiceModeCallback implements AbsListView.MultiChoiceModeListener {
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
            getMenuInflater().inflate(R.menu.menu_multichoice_gluing, menu);
            // actionBar
            if (actionBarView == null) {
                actionBarView = LayoutInflater.from(GlueAndWeldingActivity.this).inflate(R.layout.actionbar_listviewmultichoice, null);
                tv_selectedCount = actionBarView.findViewById(R.id.id_tv_selectedCount);
            }
            if ((zcInfo.getAttr() & CommCL.ZC_ATTR_CHARGING) == 0)
                menu.findItem(R.id.id_menu_charging).setVisible(false);
            if ((zcInfo.getAttr() & CommCL.ZC_ATTR_START) == 0)
                menu.findItem(R.id.id_menu_start).setVisible(false);
            if ((zcInfo.getAttr() & CommCL.ZC_ATTR_DONE) == 0)
                menu.findItem(R.id.id_menu_done).setVisible(false);
            if ((zcInfo.getAttr() & CommCL.ZC_ATTR_GLUING) == 0)
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
            String currState = "";
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
                        List<MESPRecord> list = getSelectItem();
                        MESPRecord record = list.get(0);
                        HashMap<String, Serializable> map = new HashMap<>();
                        map.put(CommCL.COMM_ZC_INFO_FLD, zcInfo);
                        map.put(CommCL.COMM_OP_FLD, currOP);
                        map.put(CommCL.COMM_RECORD_FLD, record);
                        map.put(CommCL.COMM_SBID_FLD, currEquipMent.getId());
                        myUnregisterReceiver(barcodeReceiver);
                        jumpNextActivity(ChargingActivity.class, map);
                    } else {
                        showMessage("请选择一条记录上料！");
                    }
                    break;
                case R.id.id_menu_start:
                    //开工
                    List<MESPRecord> startList = getSelectItem();
                    if (!TextUtils.isEmpty(error)) {
                        showMessage(error + "，不能开工！！");
                        break;
                    } else {
                        currState = startList.get(0).getState1();
                        if (currState.equals(CommCL.BATCH_STATUS_IN) || currState.equals(CommCL.BATCH_STATUS_CHARGING)) {
                            //只有在01入站或者是02上料的状态才可以开工
                            int start = zcInfo.getStartnum();
                            if (start > 0) {
                                int canStartNum = zcInfo.getStartnum() - getStartCount();
                                if (canStartNum <= 0) {
                                    showMessage("制成" + zcInfo.getId() + "最多可开工数为【" + zcInfo.getStartnum() + "】");
                                    break;
                                }
                                if (startList.size() > canStartNum) {
                                    showMessage("制成" + zcInfo.getId() + "最多可开工数为【" + zcInfo.getStartnum() + "】还可以开工:【" + canStartNum + "】");
                                    break;
                                }
                            }
                            MESPRecord record = startList.get(0);
                            if (record.getFirstchk() == 1) {
                                int bf = currEquipMent.getFirstchk();
                                if (bf == 1 && currEquipMent.getPrdno().equals(record.getPrd_no())) {
                                    showLoading();
                                    commStationZCPresenter.changeRecordStateBatch(startList, CommCL.BATCH_STATUS_WORKING);
                                    break;
                                } else {
                                    showMessage("设备：" + currEquipMent.getId() + "没有做首件检验！");
                                    break;
                                }
                            } else {
                                showLoading();
                                commStationZCPresenter.changeRecordStateBatch(startList, CommCL.BATCH_STATUS_WORKING);
                                break;
                            }
                        } else {
                            showMessage("选中的记录不可以开工!");
                            break;
                        }
                    }
                case R.id.id_menu_done:
                    //完工出站
                    List<MESPRecord> outList = getSelectItem();
                    if (!TextUtils.isEmpty(error)) {
                        showMessage(error + "，不能出站！！");
                    } else {
                        currState = outList.get(0).getState1();
                        if (CommCL.BATCH_STATUS_WORKING.equals(currState)) {
                            //校验出站时间
                            boolean doDone = true;
                            String errStr = "";
                            if (zcInfo.getPtime() > 0) {
                                for (int i = 0; i < outList.size(); i++) {
                                    MESPRecord cr = outList.get(i);
                                    int key = DateUtil.subDate(DateUtil.getCurrDateTime(ICL.DF_YMDT), cr.getHpdate(), 4);
                                    if (key < zcInfo.getPtime() && key > 0&&!CommCL.isTest) {
                                        doDone = false;
                                        errStr = cr.getSid1() + "已开工:" + key + "分钟，需要等待" + zcInfo.getPtime() + "分钟，不能出站！";
                                        break;
                                    }
                                }
                            }
                            if (!TextUtils.isEmpty(errStr)) {
                                showMessage(errStr);
                                return false;
                            }
                            if (doDone) {
                                showLoading();
                                commStationZCPresenter.changeRecordStateBatch(outList, CommCL.BATCH_STATUS_DONE);
                                return true;
                            }
                        } else {
                            showMessage("选中记录的状态不是生产中，不可以出站");
                            break;
                        }
                    }

                case R.id.id_menu_gluing:
                    //加胶
                    HashMap<String, Serializable> map = new HashMap<>();
                    map.put(CommCL.COMM_OP_FLD, currOP);
                    map.put(CommCL.COMM_SBID_FLD, currEquipMent.getId());
                    myUnregisterReceiver(barcodeReceiver);
                    jumpNextActivity(AddGluingActivity.class, map);
                    break;
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {

        }

        public List<MESPRecord> getSelectItem() {
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
                    if (currstate.equals(mespRecord.getState1())) {//判断两个选中的状态是否一致
                        selectList.add(mespRecord);
                        selectIndex.add(i);
                    } else {
                        error = ("选中的记录，状态不一致");
                        break;
                    }
                }
            }
            return selectList;
        }


        /***
         * 获取正在生产的数量
         * @return
         */
        private int getStartCount() {
            int count = 0;
            for (int i = 0; i < dataList.size(); i++) {
                MESPRecord record = JSONObject.parseObject(dataList.get(i).toJSONString(), MESPRecord.class);
                if (CommCL.BATCH_STATUS_WORKING.equals(record.getState1())) {
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






    public String getTextByIndex(int i){
        switch (getIndexByZcno(zcno)){
            case 21:
            case 22:
                return (i==0)?getString(R.string.yimei_lab_box_gj):getString(R.string.yimei_lab_box_hj);
            case 31:
                return (i==0)?getString(R.string.yimei_lab_box_hj):getString(R.string.yimei_lab_box_dj);
            case 32:
                return (i==0)?getString(R.string.yimei_lab_box_dj1):getString(R.string.yimei_lab_box_dj2);
            default:
                return getString(R.string.yimei_lab_box);
        }
    }
    public void setButtonVisibility(){
        switch (getIndexByZcno(zcno)){
            case 21:
            case 22:dianjiaoyc.setVisibility(View.GONE);
                    jiajiao.setVisibility(View.GONE);
                break;
            case 31:
            case 32:shangliao.setVisibility(View.GONE);
                break;
        }
    }
    public int getIndexByZcno(String zcno){
        if(TextUtils.equals("21",zcno)){
            return 21;
        }
        if(TextUtils.equals("22",zcno)){
            return 22;
        }
        if(TextUtils.equals("31",zcno)){
            return 31;
        }
        if(TextUtils.equals("32",zcno)){
            return 32;
        }
        return 0;
    }
}
