package com.yimeinew.activity.sb;

import android.content.DialogInterface;
import android.os.Bundle;
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
import com.yimeinew.activity.deviceproduction.LookBeltFastActivity;
import com.yimeinew.adapter.tabledataadapter.BaseTableDataAdapter;
import com.yimeinew.data.MESPRecord;
import com.yimeinew.listener.OnConfirmListener;
import com.yimeinew.modelInterface.CommBaseView;
import com.yimeinew.network.schedulers.SchedulerProvider;
import com.yimeinew.presenter.CommBasePresenter;
import com.yimeinew.tableui.TablePanelView;
import com.yimeinew.utils.CommCL;
import com.yimeinew.utils.CommonUtils;
import com.yimeinew.utils.DateUtil;
import com.yimeinew.utils.ICL;

import java.util.List;

/**
 * 看带不良登记
 */
public class WxGhpjActivity extends BaseActivity implements CommBaseView {
    String Title="维修生产确认";
    private final String TAG_NAME = LookBeltFastActivity.class.getSimpleName();

    private String currMONO = "";//当前工单号
    @BindView(R.id.edt_pjname1)
    EditText pjname1;
    @BindView(R.id.edt_pjnum1)
    EditText pjnum1;
    @BindView(R.id.edt_pjname2)
    EditText pjname2;
    @BindView(R.id.edt_pjnum2)
    EditText pjnum2;
    @BindView(R.id.edt_pjname3)
    EditText pjname3;
    @BindView(R.id.edt_pjnum3)
    EditText pjnum3;
    @BindView(R.id.edt_pjname4)
    EditText pjname4;
    @BindView(R.id.edt_pjnum4)
    EditText pjnum4;
    @BindView(R.id.edt_pjname5)
    EditText pjname5;
    @BindView(R.id.edt_pjnum5)
    EditText pjnum5;

    @BindView(R.id.bn_save_back)
    Button bnSaveBack;
    //数据表格

   // @BindView(R.id.data_list_content)
  //  ListView dataListViewContent;
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
        setContentView(R.layout.activity_wx_ghpj);
        this.setTitle(Title );
        ButterKnife.bind(this);
        commPresenter =new CommBasePresenter(this, SchedulerProvider.getInstance());
        if(getIntent().hasExtra(CommCL.JUMP_KEY_MESPRecord)) {
            mesPRecord= (MESPRecord) getIntent().getSerializableExtra(CommCL.JUMP_KEY_MESPRecord);
////            ngInfo= CommonUtils.getJsonObjFromBean(mesPRecord);
////            edtLotNo.setText(mesPRecord.getLotno());
////            CommonUtils.textViewGetFocus(edtLotNo1);
//            //去服务器去lot信息并判断是否已看带，和已经登记过不良了   key=1//----检验是否已经添加了配件
//            commPresenter.getAssistInfo(CommCL.AID_BAD_RECODE,cont,1);
            ngInfo=new JSONObject();
            String sid=mesPRecord.getSid();
            ngInfo.put("sid",sid);
            String sbid=mesPRecord.getSbid();
            ngInfo.put("sbid",sbid);
        }

    }
    @OnEditorAction({R.id.edt_pjname1,  R.id.edt_pjnum1,R.id.edt_pjname2,R.id.edt_pjnum2,R.id.edt_pjname3,R.id.edt_pjnum3,R.id.edt_pjname4,R.id.edt_pjnum4,R.id.edt_pjname5,R.id.edt_pjnum5})
    public boolean OnEditorAction(EditText editText) {
        return onEditTextKeyDown(editText);
    }
    @Override
    public boolean onEditTextKeyDown(EditText editText) {
        int id = editText.getId();
        String name1 = pjname1.getText().toString();
        if (TextUtils.isEmpty(name1)) {
            showMessage("请输入配件名称1！");
            CommonUtils.textViewGetFocus(pjname1);
            return true;
        }
        if (id == R.id.edt_pjname1) {
            CommonUtils.textViewGetFocus(pjnum1);
            return true;
        }
        if (id == R.id.edt_pjnum1) {
            String num1 = pjnum1.getText().toString();
            if (TextUtils.isEmpty(num1)) {
                showMessage("请输入配件1的数量");
                CommonUtils.textViewGetFocus(pjnum1);
                return true;
            }
            CommonUtils.textViewGetFocus(pjname2);
            return true;
        }
        //配件2
        String name2=pjname2.getText().toString();
        if(TextUtils.isEmpty(name2)){
            showMessage("请输入配件名称2！");
            CommonUtils.textViewGetFocus(pjname2);
            return true;
        }
        if(id==R.id.edt_pjname2){
            CommonUtils.textViewGetFocus(pjnum2);
            return true;
        }
        if (id == R.id.edt_pjnum2) {
            String num2 = pjnum2.getText().toString();
            if (TextUtils.isEmpty(num2)) {
                showMessage("请输入配件2的数量");
                CommonUtils.textViewGetFocus(pjnum2);
                return true;
            }
            CommonUtils.textViewGetFocus(pjname3);
            return true;
        }

        //配件3
        String name3=pjname3.getText().toString();
        if(TextUtils.isEmpty(name3)){
            showMessage("请输入配件名称3！");
            CommonUtils.textViewGetFocus(pjname3);
            return true;
        }
        if(id==R.id.edt_pjname3){
            CommonUtils.textViewGetFocus(pjnum3);
            return true;
        }
        if (id == R.id.edt_pjnum3) {
            String num3 = pjnum3.getText().toString();
            if (TextUtils.isEmpty(num3)) {
                showMessage("请输入配件3的数量");
                CommonUtils.textViewGetFocus(pjnum3);
                return true;
            }
            CommonUtils.textViewGetFocus(pjname4);
            return true;
        }

        //配件4
        String name4=pjname4.getText().toString();
        if(TextUtils.isEmpty(name4)){
            showMessage("请输入配件名称4！");
            CommonUtils.textViewGetFocus(pjname4);
            return true;
        }
        if(id==R.id.edt_pjname4){
            CommonUtils.textViewGetFocus(pjnum4);
            return true;
        }
        if (id == R.id.edt_pjnum4) {
            String num4 = pjnum4.getText().toString();
            if (TextUtils.isEmpty(num4)) {
                showMessage("请输入配件4的数量");
                CommonUtils.textViewGetFocus(pjnum4);
                return true;
            }
            CommonUtils.textViewGetFocus(pjname5);
            return true;
        }
        //配件5
        String name5=pjname5.getText().toString();
        if(TextUtils.isEmpty(name5)){
            showMessage("请输入配件名称5！");
            CommonUtils.textViewGetFocus(pjname5);
            return true;
        }
        if(id==R.id.edt_pjname5){
            CommonUtils.textViewGetFocus(pjnum5);
            return true;
        }
        if (id == R.id.edt_pjnum5) {
            String num5 = pjnum5.getText().toString();
            if (TextUtils.isEmpty(num5)) {
                showMessage("请输入配件5的数量");
                CommonUtils.textViewGetFocus(pjnum5);
                return true;
            }
            CommonUtils.textViewGetFocus(pjname5);
            return true;
        }

        return false;
    }

    @OnClick(R.id.bn_save_back)
    public void OnClick(View view){
        int id =view.getId();
        switch (id){
            case R.id.bn_save_back:
                if(checkData()){
                    commPresenter.saveData(CommCL.CELL_ID_E6002C,ngInfo,2);
                }else{
                    //showMessage(error1);
                    CommonUtils.confirm(this, "是否返回维修界面", error1+";数据不完善，无法保存，点确定跳转页面，取消可以继续编辑", null, new OnConfirmListener() {
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
//    /**LotNo*/
//    public void getLotNoCallBack(Boolean bok, JSONArray info, String error, int key){
//        if(bok){
//            if(info!=null&&info.size()>0){
//                JSONObject obj=info.getJSONObject(0);
//                //showMessage(obj.getString("lotno"));
//                if(TextUtils.isEmpty(obj.getString("badlotno"))){
//                    ngInfo=obj;
//                    edtLotNo.setText(obj.getString("lotno"));
//                    CommonUtils.textViewGetFocus(edtLotNo1);
//                    hideLoading();
//                }else{
//                    myShowError("该Lot已经做过不良登记",edtLotNo);
//                }
//            }else {
//                myShowError("该Lot号没有查询到记录",edtLotNo);
//            }
//        }else{
//            myShowError(error,edtLotNo);
//        }
//    }
//    /**补料Lotno*/
//    public void getLotNo1CallBack(Boolean bok, JSONArray info, String error, int key){
//        if(bok){
//            if(info!=null&&info.size()>0){
//                JSONObject obj=info.getJSONObject(0);
//                if(TextUtils.equals(obj.getString("prd_no"),ngInfo.getString("prd_no"))&&TextUtils.equals(obj.getString("bincode"),ngInfo.getString("bincode"))){
//                    lot_no1=obj.getString("lotno");
//                    CommonUtils.textViewGetFocus(edtFracture);
//                    hideLoading();
//                }else{
//                    myShowError("补料的货品或者Bincode不一致",edtLotNo1);
//                }
//            }else {
//                myShowError("该Lot号没有查询到记录",edtLotNo1);
//            }
//        }else{
//            myShowError(error,edtLotNo1);
//        }
//    }

    @Override
    public void commCallBack(Boolean bok, JSONObject info, String error, int key) {

    }

    @Override
    public void commCallBack(Boolean bok, JSONObject info, String error, String key) {

    }



   @Override
   public void getAssistInfoBack(Boolean bok, JSONArray info, String error, int key) {
//        switch (key){
//            case 1:
//                getLotNoCallBack(bok,info,error,key);
//                break;
//            case 2:
//                getLotNo1CallBack(bok,info,error,key);
//                break;
//        }
    }

    @Override
    public void saveDataBack(Boolean bok, JSONArray info, JSONObject record, String error, int key) {
        if(bok){
            switch (key){
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
        int num1=CommonUtils.parseInt(pjnum1.getText().toString());
        String name1=pjname1.getText().toString();
        int num2=CommonUtils.parseInt(pjnum2.getText().toString());
        String name2=pjname2.getText().toString();
        int num3=CommonUtils.parseInt(pjnum2.getText().toString());
        String name3=pjname3.getText().toString();
        int num4=CommonUtils.parseInt(pjnum2.getText().toString());
        String name4=pjname4.getText().toString();
        int num5=CommonUtils.parseInt(pjnum2.getText().toString());
        String name5=pjname5.getText().toString();

        if(TextUtils.isEmpty(name1)){
           error1="配件名称1不能为空";
        }else if(num1==0){
            error1="数量1不能为空";
        } else {
            b=true;
            //设置保存数据
            ngInfo.put("pjname1",name1);
            ngInfo.put("pjnum1",num1);
            ngInfo.put("pjname2",name2);
            ngInfo.put("pjnum2",num2);
            ngInfo.put("pjname3",name3);
            ngInfo.put("pjnum3",num3);
            ngInfo.put("pjname4",name4);
            ngInfo.put("pjnum4",num4);
            ngInfo.put("pjnum5", num5);
            ngInfo.put("pjname5",name5);
        }

        return b;
    }

    public void myFinish(){
        this.finish();
    }

}
