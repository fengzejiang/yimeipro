package com.yimeinew.activity.deviceproduction;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.text.TextUtils;

import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnEditorAction;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.shade.com.alibaba.fastjson.JSON;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.shade.com.alibaba.fastjson.JSONArray;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.shade.com.alibaba.fastjson.JSONObject;

import com.yimeinew.activity.R;
import com.yimeinew.activity.base.BaseActivity;
import com.yimeinew.activity.base.BaseApplication;
import com.yimeinew.activity.databinding.ActivityMboxBindzjBinding;
import com.yimeinew.adapter.tabledataadapter.BaseTableDataAdapter;

import com.yimeinew.data.MESPRecord;
import com.yimeinew.data.MboxBindEntity;
import com.yimeinew.modelInterface.CommBaseView;
import com.yimeinew.network.schedulers.SchedulerProvider;
import com.yimeinew.presenter.CommOtherPresenter;
import com.yimeinew.tableui.TablePanelView;
import com.yimeinew.tableui.entity.HeaderRowInfo;
import com.yimeinew.utils.CommCL;
import com.yimeinew.utils.CommonUtils;
import com.yimeinew.utils.DateUtil;
import com.yimeinew.utils.ICL;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MboxBindzjActivity extends BaseActivity implements CommBaseView {

    private CommOtherPresenter commPresenter;
    private MboxBindEntity mboxBindEntity;
    private static final String Title = "支架绑料盒";
    @BindView(R.id.edt_op)
    EditText edtOP;
    @BindView(R.id.edt_lot_no)
    EditText edtLotNo;
    @BindView(R.id.edt_prd_no)
    EditText edtPrdNo;
    @BindView(R.id.edt_box)
    EditText edtMbox;

    //数据表格
    @BindView(R.id.table_view)
    TablePanelView tableView;
    @BindView(R.id.data_list_content)
    ListView dataListViewContent;
    List<JSONObject> dataList;
    BaseTableDataAdapter adapter;

    private String KEY_SAVE_MBOX_ZJ="mes_mbox_zj";
    private String KEY_SAVE_MBOX_ZJA="mes_mbox_zja";
    private String KEY_SAVE_COMMON="mes_mbox_zj_common";
    public HashMap<String,String> cache=new HashMap<String,String>();
    ArrayList<String> mboxBing=new ArrayList<String>();
    String prdNo="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mbox_bindzj);
        this.setTitle(Title );
        /*ActivityMboxBindzjBinding 这类不存在的 通过 引入import com.yimeinew.activity.databinding.ActivityMboxBindzjBinding;*/
        ActivityMboxBindzjBinding activityCommGjBinding = DataBindingUtil.setContentView(this, R.layout.activity_mbox_bindzj);
        /*setMboxInfo 根据xml里面定义的mboxInfo按set规则生成的 <variable name="mboxInfo" type="com.yimeinew.data.MboxBindEntity" />*/
        mboxBindEntity=new MboxBindEntity();
        activityCommGjBinding.setMboxInfo(mboxBindEntity);
        /*黄油刀，注释注入*/
        ButterKnife.bind(this);
        /*主持人*/
        commPresenter = new CommOtherPresenter(this, SchedulerProvider.getInstance());
        initTableView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cache.put("nowBindPrdNo","");
        cache.put("nowBindMbox","");
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
//        Log.i(TAG_NAME,"销毁我了");
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
        sidColumn = new HeaderRowInfo("lotno", "批次号", 320);
        rowList.add(sidColumn);
        return rowList;
    }
    /***
     * 初始化表格
     */
    private void initTableView() {
        dataList = new ArrayList();
        List<HeaderRowInfo> header = getRowDataList();
        initView();
        adapter = new BaseTableDataAdapter(this,tableView,dataListViewContent,dataList,header);
        adapter.setSwipeRefreshEnabled(false);
        adapter.setTitle("项次");
        adapter.setTitleHeight(90);
        adapter.setTitleWidth(90);
        tableView.setAdapter(adapter);
    }
    @Override
    public void onRemoteFailed(String message) {
        hideLoading();
        CommonUtils.showError(this, "onRemoteFailed="+message);
    }
    private void initView() {
        dataListViewContent.setChoiceMode(ListView.CHOICE_MODE_NONE);
    }
    @OnEditorAction({R.id.edt_op, R.id.edt_lot_no,R.id.edt_prd_no, R.id.edt_box})
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
            CommonUtils.textViewGetFocus(edtLotNo);
            return false;
        }
        String lotno = edtLotNo.getText().toString().toUpperCase();
        if (id == R.id.edt_lot_no) {
            CommonUtils.textViewGetFocus(edtPrdNo);
            return false;
        }
        String prdNo = edtPrdNo.getText().toString().toUpperCase();
        if (TextUtils.isEmpty(prdNo)) {
            showMessage("请输入支架货号");
            CommonUtils.textViewGetFocus(edtPrdNo);
            return true;
        }
        if (id == R.id.edt_prd_no) {
            //检查货品代号是不是M03
            if(!prdNo.contains("M03")){
                showMessage("请输入中类为M03货号的货号");
                CommonUtils.textViewGetFocus(edtPrdNo);
                return true;
            }
//            if(TextUtils.equals(cache.get("nowBindPrdNo"),prdNo)){
//                CommonUtils.textViewGetFocus(edtMbox);
//                return true;
//            }
//            cache.put("nowBindPrdNo",prdNo);

            //检查货品代号是不是BOM里头的
            showLoading();
            commPresenter.checkPrdNo(prdNo,R.id.edt_prd_no);
            return false;
        }
        if(id==R.id.edt_box){
            String mbox = edtMbox.getText().toString().toUpperCase();
            if (TextUtils.isEmpty(mbox)) {
                showMessage("请输入料盒号");
                CommonUtils.textViewGetFocus(edtMbox);
                return true;
            }
            //检查料盒
            showLoading();
            commPresenter.checkBoxExit(mbox,prdNo,R.id.edt_box);

        }
        return true;
    }



    /**
     * 检验支架货品返回处理函数
     * @param bok
     * @param info
     * @param error
     * @param key
     */
    public void checkPrdtCallBack(boolean bok,JSONObject info, String error, int key) {

        if(bok){
            //缓存prd_on
            prdNo=info.getString("prd_no");
            CommonUtils.textViewGetFocus(edtMbox);
            hideLoading();
            //CommonUtils.textViewGetFocus(edtMbox);

        }else{
            CommonUtils.textViewGetFocus(edtPrdNo);
            hideLoading();
            showMessage(error);
        }
    }

    /**
     * 检验料盒返回处理
     * @param bok
     * @param info
     * @param error
     * @param key
     */
    public void checkMboxCallBack(boolean bok,JSONObject info, String error, int key) {

        if(bok){
            String op=edtOP.getText().toString().toUpperCase();
            String lotno=edtLotNo.getText().toString().toUpperCase();
            String prdNo1=edtPrdNo.getText().toString().toUpperCase();
            if(!TextUtils.equals(prdNo1,prdNo)){
                showMessage("手输货品代号必须回车");
                CommonUtils.textViewGetFocus(edtPrdNo);
                hideLoading();
                return;
            }
            if(!isSame(op,lotno,prdNo1)){
                //保存表头
                //扫描货品代号保存表头数据
                JSONObject jsonObject = new JSONObject();
                jsonObject.putAll(info);
                jsonObject.put("op",edtOP.getText().toString().toUpperCase());
                jsonObject.put("prd_no",prdNo);
                jsonObject.put("lotno",edtLotNo.getText().toString().toUpperCase());
                jsonObject.put("mkdate", DateUtil.getCurrDateTime(ICL.DF_YMDHM));
                jsonObject.put("sbuid","D0092");
                jsonObject.put("dcid",CommonUtils.getMacID());
                jsonObject.put("sorg", BaseApplication.currUser.getDeptCode());
                HashMap<String,String> hashMap=CommonUtils.saveDataMap(jsonObject.toJSONString(),CommCL.CELL_ID_D0092A);
                commPresenter.saveMboxZJ(hashMap,KEY_SAVE_MBOX_ZJ);
            }else{
                //保存表身

                //扫描货品代号保存表身数据
                JSONObject jsonObject=new JSONObject();
                jsonObject.put("sid", getCacheSid());
                jsonObject.put("mbox",info.getString("mbox"));
                HashMap<String, String> hashMap = CommonUtils.saveDataMap(jsonObject.toJSONString(), CommCL.CELL_ID_D0092B);
                info.put("lotno",edtLotNo.getText().toString().toUpperCase());
                commPresenter.saveMboxBoby(hashMap, KEY_SAVE_MBOX_ZJA,info);
            }







            CommonUtils.textViewGetFocus(edtMbox);
        }else{
            CommonUtils.textViewGetFocus(edtMbox);
            showMessage(error);
            hideLoading();
        }
    }

    /**
     * 输入货品代号保存表头返回函数
     * @param bok
     * @param info
     * @param error
     * @param key
     */
    public void savePrdNoCallBack(boolean bok,JSONObject info, String error, String key){
        if(bok){
            //保存表头成功
            clearCache();
            //成功返回主键sid，然后建立缓存
            //cache.put("sid",info.getString("sid"));
            setCacheDate(info.getString("sid"),info.getString("op"),info.getString("lotno"),info.getString("prd_no"));
            //保存表身
            //扫描货品代号保存表身数据
            JSONObject jsonObject=new JSONObject();
            jsonObject.put("sid", getCacheSid());
            jsonObject.put("mbox",info.getString("mbox"));
            HashMap<String, String> hashMap = CommonUtils.saveDataMap(jsonObject.toJSONString(), CommCL.CELL_ID_D0092B);

            commPresenter.saveMboxBoby(hashMap, KEY_SAVE_MBOX_ZJA,info);

//            hideLoading();
//            CommonUtils.textViewGetFocus(edtMbox);
        }else{
            hideLoading();
            CommonUtils.textViewGetFocus(edtPrdNo);
            showMessage(error);

        }
    }

    /**
     * 保存表身返回函数，一般不会弹出信息。如果弹出说明对象定义被人修改
     * 保存后插入列表
     * @param bok
     * @param info
     * @param error
     * @param key
     */
    public void saveMboxCallBack(boolean bok,JSONObject info, String error, String key){
        if(bok){
            //调用料盒基础表更新接口
            HashMap<String,String> hm=new HashMap<String,String>();
            //hm.put("mbox",info.getString("mbox"));
            //hm.put("prdno",info.getString("prd_no"));
            hm.put("lotno",edtLotNo.getText().toString().toUpperCase());
            hm.put("id",info.getString("mbox"));
            hm.put("prd_no",info.getString("prd_no"));

            //commPresenter.updateMbox(ja.toJSONString(),CommCL.COMM_MES_UDP_MBOXZJ_VALUE);
            commPresenter.updateData(CommCL.CELL_ID_B0055WEB1,hm,3);

            addRow(info);
            CommonUtils.textViewGetFocus(edtMbox);
        }else{
            hideLoading();
            CommonUtils.textViewGetFocus(edtMbox);
            showMessage(error);
        }
    }

    /**
     * 用于按钮触发的回调函数key=R.id.
     * @param bok
     * @param info
     * @param error
     * @param key
     */
    @Override
    public void commCallBack(Boolean bok, JSONObject info, String error, int key) {
        switch (key){
            case R.id.edt_prd_no:
                checkPrdtCallBack(bok,info,error,key);
                break;
            case R.id.edt_box:
                checkMboxCallBack(bok,info,error,key);
                break;

        }
    }

    @Override
    public void commCallBack(Boolean bok, JSONObject info, String error, String key) {
        if(TextUtils.equals(KEY_SAVE_MBOX_ZJ,key)){
            savePrdNoCallBack(bok,info,error, key);
        }else if(TextUtils.equals(KEY_SAVE_MBOX_ZJA,key)){
            saveMboxCallBack(bok,info,error, key);
        }

    }

    @Override
    public void getAssistInfoBack(Boolean bok, JSONArray info, String error, int key) {

    }

    @Override
    public void saveDataBack(Boolean bok, JSONArray info,JSONObject record, String error, int key) {

    }

    @Override
    public void updateDataBack(Boolean bok, JSONArray info, String error, int key) {
        hideLoading();
    }

    @Override
    public void changeRecordStateBack(Boolean bok, JSONArray info, String error, int key) {

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
        this.dataList.clear();
    }
    /*-------------Tool-------------*/
    /**设置缓存*/
    public void setCacheDate(String sid,String op,String lotno,String prd_no){
        CommCL.sharedPreferences.edit().putString(KEY_SAVE_COMMON+"sid",sid).commit();
        CommCL.sharedPreferences.edit().putString(KEY_SAVE_COMMON+"op",op).commit();
        CommCL.sharedPreferences.edit().putString(KEY_SAVE_COMMON+"lotno",lotno).commit();
        CommCL.sharedPreferences.edit().putString(KEY_SAVE_COMMON+"prd_no",prd_no).commit();
    }

    /**获取缓存*/
    public HashMap<String,String> getCacheDate(){
        HashMap<String,String> hm=new HashMap<>();
        hm.put("sid",CommCL.sharedPreferences.getString(KEY_SAVE_COMMON+"sid",""));
        hm.put("op",CommCL.sharedPreferences.getString(KEY_SAVE_COMMON+"op",""));
        hm.put("lotno",CommCL.sharedPreferences.getString(KEY_SAVE_COMMON+"lotno",""));
        hm.put("prd_no",CommCL.sharedPreferences.getString(KEY_SAVE_COMMON+"prd_no",""));
        return hm;
    }
    /**缓存是否一致*/
    public boolean isSame(String op,String lotno,String prd_no){
        boolean b=false;
        HashMap<String,String> hm=getCacheDate();
        if(!TextUtils.isEmpty(hm.get("sid"))&&TextUtils.equals(op,hm.get("op"))&&TextUtils.equals(lotno,hm.get("lotno"))&&TextUtils.equals(prd_no,hm.get("prd_no"))){
            b=true;
        }
        return b;
    }
    /**获取缓存sid*/
    public String getCacheSid(){
       return CommCL.sharedPreferences.getString(KEY_SAVE_COMMON+"sid","");
    }

    public void clearCache(){
        CommCL.sharedPreferences.edit().putString(KEY_SAVE_COMMON+"sid","").commit();
        CommCL.sharedPreferences.edit().putString(KEY_SAVE_COMMON+"op","").commit();
        CommCL.sharedPreferences.edit().putString(KEY_SAVE_COMMON+"lotno","").commit();
        CommCL.sharedPreferences.edit().putString(KEY_SAVE_COMMON+"prd_no","").commit();
        dataList.clear();
        adapter.notifyDataSetChanged();
    }
}
