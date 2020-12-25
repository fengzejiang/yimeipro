package com.yimeinew.activity.deviceproduction.commsub;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnEditorAction;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.shade.com.alibaba.fastjson.JSONArray;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.shade.com.alibaba.fastjson.JSONObject;
import com.yimeinew.activity.R;
import com.yimeinew.activity.base.BaseActivity;
import com.yimeinew.activity.base.BaseApplication;
import com.yimeinew.adapter.tabledataadapter.BaseTableDataAdapter;
import com.yimeinew.data.MESPRecord;
import com.yimeinew.data.ZCInfo;
import com.yimeinew.entity.Pair;
import com.yimeinew.modelInterface.CommBaseView;
import com.yimeinew.network.schedulers.SchedulerProvider;
import com.yimeinew.presenter.CommBasePresenter;
import com.yimeinew.tableui.TablePanelView;
import com.yimeinew.tableui.entity.HeaderRowInfo;
import com.yimeinew.utils.CommCL;
import com.yimeinew.utils.CommonUtils;
import com.yimeinew.utils.DateUtil;
import com.yimeinew.utils.ICL;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class OutReceiveActivity extends BaseActivity implements CommBaseView {
    private final String TAG_NAME = CommGJActivity.class.getSimpleName();
    //数据表格
    @BindView(R.id.table_view)
    TablePanelView tableView;
    @BindView(R.id.data_list_content)
    ListView dataListViewContent;
    List<JSONObject> dataList;
    BaseTableDataAdapter adapter;
    @BindView(R.id.edt_op)
    EditText edtOP;//作业员
    @BindView(R.id.edt_sid1)
    EditText edtSid1;//批次号
    @BindView(R.id.edt_tishi)
    TextView edtTishi;
    private String currMONO = "";//当前工单号

    ArrayAdapter<Pair> zcAdapter;
    private List<Pair> zcAdapterData = new ArrayList<>();
    ArrayList<ZCInfo> zcInfoList = BaseApplication.zcList;
    private HashMap<String, String> bindSid1 = new HashMap<>();

    private String zcno="";//当前制程
    private HashMap<String,String> menu;
    private String menuId;
    private int key;
    private String GBKEY="kuaiguozhan_qj";
    IntentFilter intentFilter;
    private CommBasePresenter commPresenter;

    public static final String Title = "器件";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_out_receive);
        ButterKnife.bind(this);
        Intent i = getIntent();
        Bundle b = getIntent().getExtras();
        menu= (HashMap<String,String>) getIntent().getSerializableExtra(CommCL.COMM_ZC_INFO_FLD);
        menuId=menu.get("id");
        this.setTitle(menu.get("name"));
        key=getKeys();
        commPresenter = new CommBasePresenter(this, SchedulerProvider.getInstance());
        initTableView();
    }


    @OnEditorAction({R.id.edt_op,  R.id.edt_sid1})
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
            CommonUtils.textViewGetFocus(edtSid1);
            return false;
        }
        if (id == R.id.edt_sid1) {
            String sid1 = edtSid1.getText().toString().toUpperCase();
            if(CommonUtils.isRepeat(TAG_NAME+"edt_sid1",sid1)){
                //CommonUtils.textViewGetFocus(edtSid1);
                return true;
            }
            if (TextUtils.isEmpty(sid1)) {
                showMessage("请输入批次号");
                CommonUtils.textViewGetFocus(edtSid1);
                return true;
            }

            if (bindSid1.containsKey(sid1)) {
                showMessage("该批次号【" + sid1 + "】已经扫描过");
                CommonUtils.textViewGetFocus(edtSid1);
                return true;
            }
            if(!CommonUtils.isRepeat("ks_saomao_sid1",sid1)) {
                showLoading();
                //校验批次号
                String cont = "~a.sid1='" + sid1 + "'";
                commPresenter.getAssistInfo(CommCL.AID_MES_OUT_IN,cont,key);
            }
        }
        return false;
    }
/***
 * 初始化表格
 */
    /***
     * 初始化表格
     */
    private void initTableView() {
        dataList = new ArrayList();
        List<HeaderRowInfo> header = CommonUtils.getRowDataList();
        initView();
        adapter = new BaseTableDataAdapter(this,tableView,dataListViewContent,dataList,header);
        adapter.setSwipeRefreshEnabled(false);
        adapter.setTitle("项次");
        adapter.setTitleHeight(90);
        adapter.setTitleWidth(90);
        tableView.setAdapter(adapter);
    }
    private void initView() {
        dataListViewContent.setChoiceMode(ListView.CHOICE_MODE_NONE);
    }
    /***
     * 初始化表格头数据
     * @return 返回表格头
     */
    public List<HeaderRowInfo> getRowDataList(){
        List<HeaderRowInfo> rowList = new ArrayList<>();
        HeaderRowInfo sidColumn = new HeaderRowInfo("mbox", "料盒号", 150);
        rowList.add(sidColumn);
        sidColumn = new HeaderRowInfo("prd_no", "货品代号", 150);
        rowList.add(sidColumn);
        sidColumn = new HeaderRowInfo("name", "货品名称", 320);
        rowList.add(sidColumn);
        return rowList;
    }
    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG_NAME,"onResume");
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


    @Override
    public void commCallBack(Boolean bok, JSONObject info, String error, int key) {

    }

    @Override
    public void commCallBack(Boolean bok, JSONObject info, String error, String key) {

    }

    @Override
    public void getAssistInfoBack(Boolean bok, JSONArray info, String error, int key) {
        if(bok) {
            switch (key) {
                case 1://器件转出

                        if(!hasWaiGuan(info)){
                            hideLoading();
                            CommonUtils.textViewGetFocus(edtSid1);
                            showMessage("外观未入站");
                            return;
                        }
                        if(!hasQC(info)){
                            hideLoading();
                            CommonUtils.textViewGetFocus(edtSid1);
                            showMessage("外观品质未过检");
                            return;
                        }
                        if(hasZhuanChu(info)){
                            hideLoading();
                            CommonUtils.textViewGetFocus(edtSid1);
                            showMessage("已经转出");
                            return;
                        }
                        isWeiWai(info);
                        if(checkStateBok(info)) {
                            //保存转出记录
                            JSONObject temp = createSaveDate(info, "D0030");
                            commPresenter.saveData(CommCL.CELL_ID_D0030WEB, temp, key);
                        }
                        break;
                case 2://器件接收

                        if(!hasWaiGuan(info)){
                            hideLoading();
                            CommonUtils.textViewGetFocus(edtSid1);
                            showMessage("外观未入站");
                            return;
                        }
                        if(!hasQC(info)){
                            hideLoading();
                            CommonUtils.textViewGetFocus(edtSid1);
                            showMessage("外观品质未过检");
                            return;
                        }
                        if(!hasZhuanChu(info)){
                            hideLoading();
                            CommonUtils.textViewGetFocus(edtSid1);
                            showMessage("未转出");
                            return;
                        }
                        if(hasJieShou(info)){
                            hideLoading();
                            CommonUtils.textViewGetFocus(edtSid1);
                            showMessage("已接收");
                            return;
                        }
                        if(isWeiWai(info)){
                            showMessage("委外加工单，现在还不能接收！");
                            return;
                        }
                        if(checkStateBok(info)){
                            //保存接收记录
                            JSONObject temp = createSaveDate(info, "D0031");
                            commPresenter.saveData(CommCL.CELL_ID_D0031WEB,temp,key);
                        }
                        break;
            }
        }else{
            CommonUtils.textViewGetFocus(edtSid1);
            hideLoading();
            showMessage(error);

        }
    }


    @Override
    public void saveDataBack(Boolean bok, JSONArray info, JSONObject record, String error, int key) {
        if(bok){
            String s1=record.getString("sid1");
            bindSid1.put(s1,s1);
            adapter.addRecord(record);
            CommonUtils.textViewGetFocus(edtSid1);
            hideLoading();
        }else{
            hideLoading();
            CommonUtils.textViewGetFocus(edtSid1);
            showMessage(error);
        }
    }

    @Override
    public void updateDataBack(Boolean bok, JSONArray info, String error, int key) {

    }

    @Override
    public void changeRecordStateBack(Boolean bok, JSONArray info, String error, int key) {

    }


    /**
     * 查询检查状态
     * @param info
     * @return
     */
    public boolean checkStateBok(JSONArray info){

        JSONObject obj=info.getJSONObject(0);
        int holdid=obj.getInteger("holdid");
        String state =obj.getString("state");
        int bok=obj.getInteger("bok");
        boolean b=true;
        if(CommCL.HOLD==holdid){
            b=false;
            showMessage("该批次被HOLD");
        }else if(bok!=CommCL.BOK){
            b=false;
            showMessage("该批次不具备开工条件");
        }else if(TextUtils.equals(CommCL.BATCH_STATUS_READY,state)){
            b=false;
            showMessage("该批次未出站，状态是准备");
        }else if(TextUtils.equals(CommCL.BATCH_STATUS_WORKING,state)){
            b=false;
            showMessage("该批次未出站，状态是生产中");
        }else if(TextUtils.equals(CommCL.BATCH_STATUS_CHECKING,state)){
            b=false;
            showMessage("该批次未出站，状态是待检");
        }
        if(!b){
            CommonUtils.textViewGetFocus(edtSid1);
            hideLoading();
        }
        return b;
    }

    public JSONObject createSaveDate(JSONArray info,String sbuid){
        String op=edtOP.getText().toString().toUpperCase();
        JSONObject obj=info.getJSONObject(0);
        String slkid=obj.getString("sid");
        String sid1=obj.getString("sid1");
        String zcno=obj.getString("zcno");
        String prd_no=obj.getString("prd_no");
        String prd_name=obj.getString("prd_name");
        String zcno1=obj.getString("zcno1");
        int qty=obj.getInteger("qty");
        int totalqty=obj.getInteger("totalqty");
        String rem=obj.getString("remark");
        MESPRecord record=new MESPRecord(sid1,slkid,zcno,"");
        record.setSbuid(sbuid);
        record.setPrd_no(prd_no);
        record.setPrd_name(prd_name);
        record.setHpdate(record.getMkdate());
        record.setOutdate(record.getMkdate());
        record.setRemark(rem);
        record.setQty(qty);
        record.setTotalqty(totalqty);
        record.setState1(CommCL.BATCH_STATUS_DONE);
        record.setOp(op);record.setOp_b(op);record.setOp_o(op);
        record.setZcno1(zcno1);
        JSONObject temp = CommonUtils.getJsonObjFromBean(record);
        temp.put("cref3",1);
        temp.put("erid",0);
        temp.put("op_c",op);
        return temp;
    }

    /*--工具--*/

    public int getKeys(){
        if(TextUtils.equals("D0030",menuId)){
            return 1;//转出查外观
        }else if(TextUtils.equals("D0031",menuId)){
            return 2;
        }
        return 0;
    }
    public boolean hasWaiGuan(JSONArray array){
        for(int i=0;i<array.size();i++){
            JSONObject obj=array.getJSONObject(i);
            if(TextUtils.equals("D0050",obj.getString("sbuid"))){
                return true;
            }
        }
        return false;
    }
    public boolean hasQC(JSONArray array){
        for(int i=0;i<array.size();i++){
            JSONObject obj=array.getJSONObject(i);
            if(TextUtils.equals("D0050",obj.getString("sbuid"))){
                if(TextUtils.equals("1",obj.getString("nextbok"))){
                    return true;
                }
            }
        }
        return false;
    }
    public boolean hasZhuanChu(JSONArray array){
        for(int i=0;i<array.size();i++){
            JSONObject obj=array.getJSONObject(i);
            if(TextUtils.equals("D0030",obj.getString("sbuid"))){
                return true;
            }
        }
        return false;
    }
    public boolean hasJieShou(JSONArray array){
        for(int i=0;i<array.size();i++){
            JSONObject obj=array.getJSONObject(i);
            if(TextUtils.equals("D0031",obj.getString("sbuid"))){
                return true;
            }
        }
        return false;
    }
    public boolean isWeiWai(JSONArray array){
        for(int i=0;i<array.size();i++){
            JSONObject obj=array.getJSONObject(i);
            int wait_time=obj.getInteger("wait_time");
            String edate=obj.getString("edate");
            if(TextUtils.equals("D0050",obj.getString("sbuid"))&&wait_time!=0){
                int subtime = DateUtil.subSecond( DateUtil.getCurrDateTime(ICL.DF_YMDT),edate);
                int waitTime=wait_time*60*60;
                if(subtime<waitTime) {
                    CommonUtils.showTextAuto(edtTishi,"委外加工单","",800);
                    return true;
                }else{
                    return false;
                }
            }
        }
        return false;
    }

}
