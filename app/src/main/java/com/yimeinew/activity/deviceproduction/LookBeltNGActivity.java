package com.yimeinew.activity.deviceproduction;

import android.content.DialogInterface;
import android.os.Bundle;
import android.app.Activity;
import android.os.TestLooperManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnEditorAction;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.shade.com.alibaba.fastjson.JSONArray;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.shade.com.alibaba.fastjson.JSONObject;
import com.yimeinew.activity.R;
import com.yimeinew.activity.base.BaseActivity;
import com.yimeinew.adapter.tabledataadapter.BaseTableDataAdapter;
import com.yimeinew.data.MESPRecord;
import com.yimeinew.listener.OnConfirmListener;
import com.yimeinew.modelInterface.BaseView;
import com.yimeinew.modelInterface.CommBaseView;
import com.yimeinew.network.schedulers.SchedulerProvider;
import com.yimeinew.presenter.CommBasePresenter;
import com.yimeinew.tableui.CommMultiChoiceModeCallBack;
import com.yimeinew.tableui.TablePanelView;
import com.yimeinew.utils.CommCL;
import com.yimeinew.utils.CommonUtils;
import com.yimeinew.utils.DateUtil;
import com.yimeinew.utils.ICL;

import java.util.List;

/**
 * 看带不良登记
 */
public class LookBeltNGActivity extends BaseActivity implements CommBaseView {
    String Title="看带不良登记";
    private final String TAG_NAME = LookBeltFastActivity.class.getSimpleName();

    private String currMONO = "";//当前工单号
    @BindView(R.id.edt_lot_no)
    EditText edtLotNo;
    @BindView(R.id.edt_lot_no1)
    EditText edtLotNo1;
    @BindView(R.id.edt_fracture)
    EditText edtFracture;
    @BindView(R.id.edt_crushed)
    EditText edtCrushed;
    @BindView(R.id.edt_poinrem)
    EditText edtPoinrem;
    @BindView(R.id.edt_broken)
    EditText edtBroken;
    @BindView(R.id.edt_pressing)
    EditText edtPressing;
    @BindView(R.id.edt_overflow)
    EditText edtOverflow;
    @BindView(R.id.edt_impurity)
    EditText edtImpurity;
    @BindView(R.id.edt_cutting)
    EditText edtCutting;

    @BindView(R.id.bn_save_new)
    Button bnSaveNew;
    @BindView(R.id.bn_save_back)
    Button bnSaveBack;
    //数据表格
    @BindView(R.id.table_view)
    TablePanelView tableView;
    @BindView(R.id.data_list_content)
    ListView dataListViewContent;
    List<JSONObject> dataList;
    JSONObject ngInfo;
    MESPRecord mesPRecord;
    String lot_no1;
    String error1="";

    BaseTableDataAdapter adapter;
    private CommBasePresenter commPresenter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_look_belt_ng);
        this.setTitle(Title );
        ButterKnife.bind(this);
        commPresenter =new CommBasePresenter(this, SchedulerProvider.getInstance());
        if(getIntent().hasExtra(CommCL.JUMP_KEY_MESPRecord)) {
            mesPRecord= (MESPRecord) getIntent().getSerializableExtra(CommCL.JUMP_KEY_MESPRecord);
//            ngInfo= CommonUtils.getJsonObjFromBean(mesPRecord);
//            edtLotNo.setText(mesPRecord.getLotno());
//            CommonUtils.textViewGetFocus(edtLotNo1);
            //去服务器去lot信息并判断是否已看带，和已经登记过不良了   key=1
            String cont="~lot.lotno='"+mesPRecord.getLotno()+"'";
            commPresenter.getAssistInfo(CommCL.AID_BAD_RECODE,cont,1);
        }else{
            ngInfo=new JSONObject();
        }

        //edtLotNo.setText("180516TS003062_3");
    }
    @OnEditorAction({R.id.edt_lot_no,  R.id.edt_lot_no1,R.id.edt_fracture,R.id.edt_crushed,R.id.edt_poinrem,R.id.edt_broken,R.id.edt_pressing,R.id.edt_overflow,R.id.edt_impurity,R.id.edt_cutting})
    public boolean OnEditorAction(EditText editText) {
        return onEditTextKeyDown(editText);
    }
    @Override
    public boolean onEditTextKeyDown(EditText editText) {
        int id= editText.getId();
        String lotNo=edtLotNo.getText().toString().toUpperCase();
        if(TextUtils.isEmpty(lotNo)){
            showMessage("请输入Lot号！");
            CommonUtils.textViewGetFocus(edtLotNo);
            return true;
        }
        String ln = ngInfo.getString("lotno");
        if(id==R.id.edt_lot_no){
            //批号与缓存对比
            if(TextUtils.equals(ln,lotNo)){
                //相等就直接跳到下格
                CommonUtils.textViewGetFocus(edtLotNo1);
            }else{
                //去服务器去lot信息并判断是否已看带   key=1
                String cont="~lot.lotno='"+lotNo+"'";
                commPresenter.getAssistInfo(CommCL.AID_BAD_RECODE,cont,1);
            }
            return true;
        }
        //批号与缓存对比
        if(!TextUtils.equals(ln,lotNo)){
            showMessage("手输lot必须回车！");
            return true;
        }
        String lotNo1=edtLotNo1.getText().toString().toUpperCase();
        if(TextUtils.isEmpty(lotNo1)){
            showMessage("请输入补料的Lot号！");
            CommonUtils.textViewGetFocus(edtLotNo1);
            return true;
        }
        if(TextUtils.equals(lotNo1,lotNo)){
            //如果测试号和补料测试号一样就不应许补料
            showMessage("补料lot不能与lot号一样。");
            CommonUtils.textViewGetFocus(edtLotNo1);
            return true;
        }
        if(id==R.id.edt_lot_no1){
            //去查询lot表对比 prd_no, bincode   key=2
            String cont="~lot.lotno='"+lotNo1+"'";
            commPresenter.getAssistInfo(CommCL.AID_BAD_RECODE,cont,2);
            return true;
        }
        //批号与缓存对比
        if(!TextUtils.equals(lotNo1,lot_no1)){
            showMessage("手输补料lot必须回车！");
            return true;
        }
        //输入不良数量回车
        switch (id){
            case R.id.edt_fracture:
                CommonUtils.textViewGetFocusNotClear(edtCrushed);
                break;
            case R.id.edt_crushed:
                CommonUtils.textViewGetFocusNotClear(edtPoinrem);
                break;
            case R.id.edt_poinrem:
                CommonUtils.textViewGetFocusNotClear(edtBroken);
                break;
            case R.id.edt_broken:
                CommonUtils.textViewGetFocusNotClear(edtPressing);
                break;
            case R.id.edt_pressing:
                CommonUtils.textViewGetFocusNotClear(edtOverflow);
                break;
            case R.id.edt_overflow:
                CommonUtils.textViewGetFocusNotClear(edtImpurity);
                break;
            case R.id.edt_impurity:
                CommonUtils.textViewGetFocusNotClear(edtCutting);
                break;
        }
        return false;
    }
    @OnClick({R.id.bn_save_back,R.id.bn_save_new})
    public void OnClick(View view){
        int id =view.getId();
        switch (id){
            case R.id.bn_save_new:
                if(checkData()){
                    commPresenter.saveData(CommCL.CELL_ID_D0071A,ngInfo,1);
                }else{
                    showMessage(error1);
                }
                break;
            case R.id.bn_save_back:
                if(checkData()){
                    commPresenter.saveData(CommCL.CELL_ID_D0071A,ngInfo,2);
                }else{
                    //showMessage(error1);
                    CommonUtils.confirm(this, "是否返回看带快速过站", error1+";数据不完善，无法保存，点确定跳转页面，取消可以继续编辑", null, new OnConfirmListener() {
                        @Override
                        public void OnConfirm(DialogInterface dialog) {
                            myFinish();
                        }

                        @Override
                        public void OnCancel(DialogInterface dialog) {

                        }
                    });

                }
                break;

        }
    }
    /**LotNo*/
    public void getLotNoCallBack(Boolean bok, JSONArray info, String error, int key){
        if(bok){
            if(info!=null&&info.size()>0){
                JSONObject obj=info.getJSONObject(0);
                //showMessage(obj.getString("lotno"));
                if(TextUtils.isEmpty(obj.getString("badlotno"))){
                    ngInfo=obj;
                    edtLotNo.setText(obj.getString("lotno"));
                    CommonUtils.textViewGetFocus(edtLotNo1);
                    hideLoading();
                }else{
                    myShowError("该Lot已经做过不良登记",edtLotNo);
                }
            }else {
                myShowError("该Lot号没有查询到记录",edtLotNo);
            }
        }else{
            myShowError(error,edtLotNo);
        }
    }
    /**补料Lotno*/
    public void getLotNo1CallBack(Boolean bok, JSONArray info, String error, int key){
        if(bok){
            if(info!=null&&info.size()>0){
                JSONObject obj=info.getJSONObject(0);
                if(TextUtils.equals(obj.getString("prd_no"),ngInfo.getString("prd_no"))&&TextUtils.equals(obj.getString("bincode"),ngInfo.getString("bincode"))){
                    lot_no1=obj.getString("lotno");
                    CommonUtils.textViewGetFocus(edtFracture);
                    hideLoading();
                }else{
                    myShowError("补料的货品或者Bincode不一致",edtLotNo1);
                }
            }else {
                myShowError("该Lot号没有查询到记录",edtLotNo1);
            }
        }else{
            myShowError(error,edtLotNo1);
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
            case 1:
                getLotNoCallBack(bok,info,error,key);
                break;
            case 2:
                getLotNo1CallBack(bok,info,error,key);
                break;
        }
    }

    @Override
    public void saveDataBack(Boolean bok, JSONArray info, JSONObject record, String error, int key) {
        if(bok){
            switch (key){
                case 1://保存成功空数据和缓存
                    myClear();
                    break;
                case 2://保存成功返回上一页页面
                    myFinish();
                    break;
            }
        }else{
            showMessage(error);
        }
    }

    @Override
    public void updateDataBack(Boolean bok, JSONArray info, String error, int key) {

    }

    @Override
    public void changeRecordStateBack(Boolean bok, JSONArray info, String error, int key) {

    }

    public void myShowError(String error,EditText editText){
        hideLoading();
        if(editText!=null) {
            CommonUtils.textViewGetFocus(editText);
        }
        showMessage(error);
    }
    public boolean checkData(){
        boolean b=false;
        String lot=edtLotNo.getText().toString().toUpperCase();
        String lot1=edtLotNo1.getText().toString().toUpperCase();
        String clot="";
        if(ngInfo!=null){
            clot=ngInfo.getString("lotno");
        }
        int fracture=CommonUtils.parseInt(edtFracture.getText().toString());
        int crushed=CommonUtils.parseInt(edtCrushed.getText().toString());
        int poinrem=CommonUtils.parseInt(edtPoinrem.getText().toString());
        int broken=CommonUtils.parseInt(edtBroken.getText().toString());
        int pressing=CommonUtils.parseInt(edtPressing.getText().toString());
        int overflow=CommonUtils.parseInt(edtOverflow.getText().toString());
        int impurity=CommonUtils.parseInt(edtImpurity.getText().toString());
        int cutting=CommonUtils.parseInt(edtCutting.getText().toString());
        if(TextUtils.isEmpty(lot)){
           error1="lot号不能为空";
        }else if(TextUtils.isEmpty(lot1)){
            error1="补料lot号不能为空";
        }else if(!TextUtils.equals(clot,lot)){
            error1="手输lot号必须回车";
        }else if(!TextUtils.equals(lot1,lot_no1)){
            error1="手输补料lot号必须回车";
        }else if(fracture+crushed+poinrem+broken+pressing+overflow+impurity+cutting<=0){
            error1="请输入任意一个不良数量，才能保存";
        }else {
            b=true;
            //设置保存数据
            ngInfo.put("sid",lot);
            ngInfo.put("lotno1",lot1);
            ngInfo.put("fracture",fracture);
            ngInfo.put("crushed",crushed);
            ngInfo.put("poinrem",poinrem);
            ngInfo.put("broken",broken);
            ngInfo.put("pressing",pressing);
            ngInfo.put("overflow",overflow);
            ngInfo.put("impurity",impurity);
            ngInfo.put("cutting",cutting);
            ngInfo.put("mkdate", DateUtil.getCurrDateTime(ICL.DF_YMDT));
        }

        return b;
    }

    public void myFinish(){
        this.finish();
    }
    public void myClear(){
        ngInfo=null;
        lot_no1="";
        error1="";
        edtLotNo.setText("");
        edtLotNo1.setText("");
        edtFracture.setText("");
        edtCrushed.setText("");
        edtPoinrem.setText("");
        edtBroken.setText("");
        edtPressing.setText("");
        edtOverflow.setText("");
        edtImpurity.setText("");

    }
}
