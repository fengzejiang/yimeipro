package com.yimeinew.activity.deviceproduction;

import android.os.Bundle;
import android.app.Activity;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ListView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnEditorAction;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.shade.com.alibaba.fastjson.JSONArray;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.shade.com.alibaba.fastjson.JSONObject;
import com.yimeinew.activity.R;
import com.yimeinew.activity.base.BaseActivity;
import com.yimeinew.adapter.tabledataadapter.BaseTableDataAdapter;
import com.yimeinew.data.EquipmentInfo;
import com.yimeinew.data.MESPRecord;
import com.yimeinew.data.MboxBindEntity;
import com.yimeinew.modelInterface.CommBaseView;
import com.yimeinew.network.schedulers.SchedulerProvider;
import com.yimeinew.presenter.CommBasePresenter;
import com.yimeinew.presenter.CommOtherPresenter;
import com.yimeinew.tableui.TablePanelView;
import com.yimeinew.tableui.entity.HeaderRowInfo;
import com.yimeinew.utils.CommCL;
import com.yimeinew.utils.CommonUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PreheatZjActivity extends BaseActivity implements CommBaseView {
    private CommBasePresenter commPresenter;
    private MboxBindEntity mboxBindEntity;
    private static final String Title = "支架除湿";
    private String KEY_SAVE_COMMON="PreheatZj_save";
    private String zcno="10";
    @BindView(R.id.edt_op)
    EditText edtOP;
    @BindView(R.id.edt_equipment_no)
    EditText edtEquNo;
    @BindView(R.id.edt_program)
    EditText edtProNo;
    @BindView(R.id.edt_box)
    EditText edtMbox;

    //数据表格
    @BindView(R.id.table_view)
    TablePanelView tableView;
    @BindView(R.id.data_list_content)
    ListView dataListViewContent;
    List<JSONObject> dataList;
    BaseTableDataAdapter adapter;
    EquipmentInfo equinfo=new EquipmentInfo();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preheat_zj);
        this.setTitle(Title );
        /*黄油刀，注释注入*/
        ButterKnife.bind(this);
        commPresenter=new CommBasePresenter(this, SchedulerProvider.getInstance());
        initTableView();
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
    /***
     * 初始化表格头数据
     * @return 返回表格头
     */
    public List<HeaderRowInfo> getRowDataList(){
        List<HeaderRowInfo> rowList = new ArrayList<>();
        HeaderRowInfo sidColumn = new HeaderRowInfo("mbox", "料盒号", 150);
        rowList.add(sidColumn);
        sidColumn = new HeaderRowInfo("sbid", "设备号", 150);
        rowList.add(sidColumn);
        sidColumn = new HeaderRowInfo("program", "程式", 150);
        rowList.add(sidColumn);
        sidColumn = new HeaderRowInfo("prd_no", "货品代号", 150);
        rowList.add(sidColumn);
        sidColumn = new HeaderRowInfo("gmc", "货品名称", 320);
        rowList.add(sidColumn);
        sidColumn = new HeaderRowInfo("lotno", "支架批次号", 320);
        rowList.add(sidColumn);
        return rowList;
    }
    private void initView() {
        dataListViewContent.setChoiceMode(ListView.CHOICE_MODE_NONE);
    }
    @Override
    public void onRemoteFailed(String message) {
        hideLoading();
        CommonUtils.showError(this, "onRemoteFailed="+message);
    }
    @OnEditorAction({R.id.edt_op, R.id.edt_equipment_no,R.id.edt_program, R.id.edt_box})
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
            CommonUtils.textViewGetFocus(edtEquNo);
            return false;
        }
        String equ=edtEquNo.getText().toString().toUpperCase();
        if(TextUtils.isEmpty(equ)){
            showMessage("请输入设备号!");
            CommonUtils.textViewGetFocus(edtEquNo);
            return true;
        }
        if (id == R.id.edt_equipment_no) {
            //通过设备号去取设备信息 key=1
            showLoading();
            String cont="~id='"+equ+"' and  zc_id='"+zcno+"'";
            commPresenter.getAssistInfo(CommCL.AID_QJ_EQUIPMENT_QUERY,cont,1);
            return false;
        }
        String proNo = edtProNo.getText().toString().toUpperCase();
        if (TextUtils.isEmpty(proNo)) {
            showMessage("请输入烘烤程式");
            CommonUtils.textViewGetFocus(edtProNo);
            return true;
        }
        if (id == R.id.edt_program&&checkProgram(equ,proNo)) {//判断设备上是否有这个程式
            //设备和程式校验完毕
            CommonUtils.textViewGetFocus(edtMbox);
            return false;
        }
        if(id==R.id.edt_box){
            String mbox = edtMbox.getText().toString().toUpperCase();
            if (TextUtils.isEmpty(mbox)) {
                showMessage("请输入料盒号");
                CommonUtils.textViewGetFocus(edtMbox);
                return true;
            }else if(checkProgram(equ,proNo)) {
                showLoading();
                //检查料盒key=2  #查询料号绑定的支架是不是跟程式一致#
                String cont = "~id='" + mbox + "'";
                commPresenter.getAssistInfo(CommCL.AID_MES_BOX_PRD_SORT, cont, 2);
            }
        }
        return true;
    }
    //设备查询返回
    public void getEquipmentInfoCallBack(Boolean bok, JSONArray info, String error, int key){
        if(bok){
            hideLoading();
            if(info.size()>0){
                equinfo=JSONObject.parseObject(info.getJSONObject(0).toJSONString(),EquipmentInfo.class);
                CommonUtils.textViewGetFocus(edtProNo);
            }else{
                showMessage("没有查询到设备号");
                CommonUtils.textViewGetFocus(edtEquNo);
            }
        }else{
            hideLoading();
            showMessage(error);
            CommonUtils.textViewGetFocus(edtEquNo);
        }
    }
    //检验设备上是否有这个程式
    public boolean checkProgram(String sbid,String program){
        boolean b=true;
        if(!TextUtils.equals(equinfo.getId(),sbid)){
            showMessage("手输设备号必须回车！");
            CommonUtils.textViewGetFocus(edtEquNo);
            b=false;
        }else if(!TextUtils.isEmpty(equinfo.getStents())){
            b=false;
            String[] stents = equinfo.getStents().split(";");
            for(String s:stents){
                if(TextUtils.equals(s,program)){
                    b=true;
                    break;
                }
            }
            if(!b) {
                showMessage("设备上没有该程式，请在设备管理维护");
                CommonUtils.textViewGetFocus(edtProNo);
            }
        }
        return b;
    }
    //料盒号查询返回
    public void getMboxInfoCallBack(Boolean bok, JSONArray info, String error, int key){
        if(bok){
            if(info.size()>0) {
                JSONObject obj=info.getJSONObject(0);
                String id = obj.getString("id");
                obj.put("mbox",id);
                String prd_no = obj.getString("prd_no");
                int state=obj.getInteger("state");
                String gmc=obj.getString("gmc");
                String mzhij=obj.getString("mzhij");
                String sbid=obj.getString("sbid");
                String program=obj.getString("program");
                String proNo = edtProNo.getText().toString().toUpperCase();
                String equ = edtEquNo.getText().toString().toUpperCase();
                String op=edtOP.getText().toString().toUpperCase();
                if(state==1){
                    hideLoading();
                    showMessage("该料盒号没有解绑");
                    CommonUtils.textViewGetFocus(edtMbox);return;
                }
                if(TextUtils.isEmpty(prd_no)){
                    hideLoading();
                    showMessage("该料盒号没有料号绑支架");
                    CommonUtils.textViewGetFocus(edtMbox);return;
                }
                if(!TextUtils.isEmpty(program)||!TextUtils.isEmpty(sbid)){
                    hideLoading();
                    showMessage("该料盒号已经绑定过烘箱和程式，无法再次除湿");
                    CommonUtils.textViewGetFocus(edtMbox);return;
                }
                MESPRecord mp=new MESPRecord();
                obj.put("sbuid","D009B");
                obj.put("op",op);
                obj.put("mkdate",mp.getMkdate());
                obj.put("sorg",mp.getSorg());
                obj.put("dcid",mp.getDcid());
                obj.put("sbid",equ);
                obj.put("program",proNo);
                if(TextUtils.equals(proNo,mzhij)){
                    //保存数据
                    if(!isSame(op,equ,proNo)){
                        //保存表头 key =1
                        clearCache();
                        commPresenter.saveData(CommCL.CELL_ID_D009BA,obj,1);
                    }else{
                        //保存表身 key=2
                        String sid=getCacheSid();
                        obj.put("sid",sid);
                        commPresenter.saveData(CommCL.CELL_ID_D009BBWEB,obj,2);
                    }

                }else{
                    hideLoading();
                    showMessage("该料盒号绑定的支架是"+gmc+"使用的程式是"+mzhij);
                    CommonUtils.textViewGetFocus(edtMbox);return;
                }

            }else{
                hideLoading();
                showMessage("没有该料盒号");
                CommonUtils.textViewGetFocus(edtMbox);
            }
        }else{
            hideLoading();
            showMessage(error);
            CommonUtils.textViewGetFocus(edtMbox);
        }
    }
    public void savePZJHeadBack(Boolean bok, JSONArray info, JSONObject record, String error, int key){
        if(bok){
            //保存表头成功
            if(info.size()>0){
                JSONObject obj=info.getJSONObject(0);
                String sid = obj.getString("sid");
                record.put("sid",sid);
                String sbid = record.getString("sbid");
                String op=record.getString("op");
                String program=record.getString("program");
                setCacheDate(sid,op,sbid,program);
                commPresenter.saveData(CommCL.CELL_ID_D009BBWEB,record,2);
            }else{
                hideLoading();
                showMessage("保存失败");
                CommonUtils.textViewGetFocus(edtMbox);
            }
        }else{
            hideLoading();
            showMessage(error);
            CommonUtils.textViewGetFocus(edtMbox);
        }
    }
    public void savePZJBodyBack(Boolean bok, JSONArray info, JSONObject record, String error, int key){
        if(bok){
            //保存子表成功
            adapter.addRecord(record);
            adapter.notifyDataSetChanged();
            //更新料盒基础资料
            commPresenter.updateData(CommCL.CELL_ID_B0055WEB2,record,1);
        }else{
            hideLoading();
            showMessage(error);
            CommonUtils.textViewGetFocus(edtMbox);
        }
    }

    @Override
    public void commCallBack(Boolean bok, JSONObject info, String error, int key) {

    }

    @Override
    public void commCallBack(Boolean bok, JSONObject info, String error, String key) {

    }

    @Override
    public void getAssistInfoBack(Boolean bok, JSONArray info, String error, int key) {
        switch (key){
            case 1://查询设备信息
                getEquipmentInfoCallBack(bok,info,error,key);
                break;
            case 2://查询料号信息
                getMboxInfoCallBack(bok,info,error,key);
                break;
            default:
                hideLoading();
                showMessage(error);
                break;
        }
    }

    @Override
    public void saveDataBack(Boolean bok, JSONArray info, JSONObject record, String error, int key) {
        switch (key){
            case 1://保存表头成功
                savePZJHeadBack(bok,info,record,error,key);
                break;
            case 2://保存表身成功
                savePZJBodyBack(bok,info,record,error,key);
                break;
            default:
                hideLoading();
                showMessage(error);
                break;
        }
    }

    @Override
    public void updateDataBack(Boolean bok, JSONArray info, String error, int key) {
        if(bok) {
            switch (key) {
                case 1:
                    hideLoading();
                    CommonUtils.textViewGetFocus(edtMbox);
                    break;
                default:
                    hideLoading();
                    showMessage(error);
                    break;
            }
        }else {
            hideLoading();
            showMessage(error);
        }
    }

    @Override
    public void changeRecordStateBack(Boolean bok, JSONArray info, String error, int key) {

    }

    /*-------------Tool-------------*/
    /**设置缓存*/
    public void setCacheDate(String sid,String op,String sbid,String program){
        CommCL.sharedPreferences.edit().putString(KEY_SAVE_COMMON+"sid",sid).commit();
        CommCL.sharedPreferences.edit().putString(KEY_SAVE_COMMON+"op",op).commit();
        CommCL.sharedPreferences.edit().putString(KEY_SAVE_COMMON+"sbid",sbid).commit();
        CommCL.sharedPreferences.edit().putString(KEY_SAVE_COMMON+"program",program).commit();
    }

    /**获取缓存*/
    public HashMap<String,String> getCacheDate(){
        HashMap<String,String> hm=new HashMap<>();
        hm.put("sid",CommCL.sharedPreferences.getString(KEY_SAVE_COMMON+"sid",""));
        hm.put("op",CommCL.sharedPreferences.getString(KEY_SAVE_COMMON+"op",""));
        hm.put("sbid",CommCL.sharedPreferences.getString(KEY_SAVE_COMMON+"sbid",""));
        hm.put("program",CommCL.sharedPreferences.getString(KEY_SAVE_COMMON+"program",""));
        return hm;
    }
    /**缓存是否一致*/
    public boolean isSame(String op,String sbid,String program){
        boolean b=false;
        HashMap<String,String> hm=getCacheDate();
        if(!TextUtils.isEmpty(hm.get("sid"))&&TextUtils.equals(op,hm.get("op"))&&TextUtils.equals(sbid,hm.get("sbid"))&&TextUtils.equals(program,hm.get("program"))){
            b=true;
        }
        return b;
    }
    /**获取缓存sid*/
    public String getCacheSid(){
        return CommCL.sharedPreferences.getString(KEY_SAVE_COMMON+"sid","");
    }

    /**
     * 清理缓存
     */
    public void clearCache(){
        CommCL.sharedPreferences.edit().putString(KEY_SAVE_COMMON+"sid","").commit();
        CommCL.sharedPreferences.edit().putString(KEY_SAVE_COMMON+"op","").commit();
        CommCL.sharedPreferences.edit().putString(KEY_SAVE_COMMON+"sbid","").commit();
        CommCL.sharedPreferences.edit().putString(KEY_SAVE_COMMON+"program","").commit();
        adapter.clear();
        adapter.notifyDataSetChanged();
    }



}
