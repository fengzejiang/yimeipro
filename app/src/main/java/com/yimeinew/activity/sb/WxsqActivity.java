package com.yimeinew.activity.sb;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnEditorAction;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.shade.com.alibaba.fastjson.JSONArray;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.shade.com.alibaba.fastjson.JSONObject;
import com.yimeinew.activity.R;
import com.yimeinew.activity.base.BaseActivity;
import com.yimeinew.adapter.SpinnerAdapterImpl;
import com.yimeinew.data.CheckReason;
import com.yimeinew.data.MESPRecord;
import com.yimeinew.entity.Pair;
import com.yimeinew.modelInterface.CommBaseView;
import com.yimeinew.network.schedulers.SchedulerProvider;
import com.yimeinew.presenter.CommOtherPresenter;
import com.yimeinew.utils.CommCL;
import com.yimeinew.utils.CommonUtils;
import com.yimeinew.view.AuxText;

import java.util.ArrayList;
import java.util.List;

public class WxsqActivity extends BaseActivity implements CommBaseView {
    private static final String Title = "设备维修申请";
    private CommOtherPresenter commPresenter;
    final int GETZCKEY=1,GETZCWXYY=2,GETSBINFO=3;//keys

    @BindView(R.id.spinner_zc)
    EditText zcSpinner;
    @BindView(R.id.edt_op)
    AuxText edtOP;
    @BindView(R.id.edt_sbid)
    EditText edtSbid;
    @BindView(R.id.spinner_zcwxyy)
    Spinner wxSpinner;
    @BindView(R.id.yimei_weixiu_save)
    Button wxSave;
    String zcno;
    String wx;
    String error1;
    JSONObject ngInfo =new JSONObject();
    JSONArray reasons;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wxsq);
        this.setTitle(Title );
        ButterKnife.bind(this);
        /*主持人*/
      commPresenter = new CommOtherPresenter(this, SchedulerProvider.getInstance());
        edtOP.setOPAux();
    }



    public void initWXSpinner(List<Pair> pairList){
        wxSpinner.setAdapter(SpinnerAdapterImpl.getSpinnerAdapter(getApplicationContext(),pairList));
        wxSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,int position, long id) {
                wx=((Pair) wxSpinner.getSelectedItem()).getValue();
                //commPresenter.getAssistInfo();
                String qtype="";
                String qcqr="";
                for(int i=0;i<reasons.size();i++){
                    JSONObject rea = reasons.getJSONObject(i);
                    if(TextUtils.equals(rea.getString("fid"),wx)){
                        qtype=rea.getString("qtype");
                        ngInfo.put("qtype",qtype);
                        qcqr=rea.getString("qcqr");
                        ngInfo.put("qcqr",qcqr);

                    }
                }

            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @OnEditorAction({R.id.edt_op, R.id.edt_sbid})
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
            CommonUtils.textViewGetFocus(edtSbid);
            return false;
        }
        String sbid = edtSbid.getText().toString().toUpperCase();
        if (TextUtils.isEmpty(sbid)) {
            showMessage("请输入设备号");
            CommonUtils.textViewGetFocus(edtSbid);
            return true;
        }
        if(id ==R.id.edt_sbid){
            //检验设备号是否在选择的制程里，检验设备是否在维修
            commPresenter.getAssistInfo(CommCL.AID_QJ_EQUIPMENT,"~id='"+sbid+"'",GETZCKEY);
        }
        return true;
    }

@OnClick(R.id.yimei_weixiu_save)
    public void OnClick(View view){
    int id =view.getId();
    if(checkData()){
        commPresenter.saveData(CommCL.CELL_ID_E6001,ngInfo,1);
    }else{
        showMessage(error1);
    }

    }


    public boolean checkData(){
        MESPRecord mespRecord=new MESPRecord();
        boolean b=false;
        String sopr=edtOP.getText().toString();
        String sbid=edtSbid.getText().toString();
        String zcno=zcSpinner.getText().toString();
        if(TextUtils.isEmpty(sbid)){
            error1="设备号不能为空";
        }else if(wxSpinner==null||wxSpinner.getSelectedItem()==null||TextUtils.isEmpty(((Pair) wxSpinner.getSelectedItem()).getValue())){
            error1="维修原因不能为空";
        } else{
            b=true;
            ngInfo.put("sbuid","E6001");
            String smake=mespRecord.getSmake();
            ngInfo.put("smake",smake);
            String mksate=mespRecord.getMkdate();
            ngInfo.put("mkdate",mksate);
            String sorg=mespRecord.getSorg();
            ngInfo.put("sorg",sorg);
            ngInfo.put("wxstate","0");
            ngInfo.put("sbid",sbid);
            ngInfo.put("sopr",sopr);
            ngInfo.put("zcno",zcno);
            String reason=((Pair) wxSpinner.getSelectedItem()).getValue();
            ngInfo.put("reason",reason);
        }
        return b;
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
            case GETZCKEY://获取制程
                if(bok) {
                   // initZCspace(Pair.toListPair(info, "name", "id"));
                    zcno =info.getJSONObject(0).getString("zcno");
                    int sbstate=info.getJSONObject(0).getInteger("sbstate");
                    if(sbstate!=0){
                        showMessage("该设备不能重复报修！");
                        CommonUtils.textViewGetFocus(edtSbid);
                        return;
                    }else {
                        //String name=info.getJSONObject(0).getString("name");设备名称
                        zcSpinner.setText(zcno);
                        commPresenter.getAssistInfo(CommCL.AID_ZC_QCREASON, "~id='" + zcno + "'", GETZCWXYY);
                    }
                }else{
                    showError(error);
                }
                break;
            case GETZCWXYY://获取制程维修原因
                reasons=info;
                initWXSpinner(Pair.toListPair(info, "name", "fid"));
                if(!bok){
                    showError(error);
                }
                break;
            case GETSBINFO://获取设备信息并校验
                if(!bok){
                    showError(error);
                }

                break;
        }

        if(!bok&&key==0){
            showError(error);
        }
    }

    @Override
    public void saveDataBack(Boolean bok, JSONArray info,JSONObject record, String error, int key) {
        if(bok){
            String id=record.getString("sbid");
            JSONObject sbmanage=new JSONObject();
            sbmanage.put("id",id);
            sbmanage.put("sbstate","1");
            commPresenter.updateData(CommCL.CELL_ID_B0003B,sbmanage,5);
            myClear();
            CommonUtils.textViewGetFocus(edtOP);
        }else{
            showMessage(error);
        }

    }
    public void myClear(){
        ngInfo.clear();
        error1="";
        edtOP.setText("");
        edtSbid.setText("");
        wxSpinner.clearAnimation();


    }

    @Override
    public void updateDataBack(Boolean bok, JSONArray info, String error, int key) {

    }

    @Override
    public void changeRecordStateBack(Boolean bok, JSONArray info, String error, int key) {

    }

    @Override
    public void onRemoteFailed(String message) {
        hideLoading();
        CommonUtils.showError(this, "onRemoteFailed="+message);
    }
    /*---------------工具方法----------*/
    public void showError(String message) {
        CommonUtils.showError(this,message);
    }
    public void showMessage(String message) {
        CommonUtils.showMessage(this,message);
    }
    public void showSuccess(String message) {
        CommonUtils.showSuccess(this,message);
    }

    @Override
    protected void onResume() {
        super.onResume();
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
}
