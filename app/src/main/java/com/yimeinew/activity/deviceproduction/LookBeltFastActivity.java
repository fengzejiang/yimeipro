package com.yimeinew.activity.deviceproduction;

import android.content.DialogInterface;
import android.os.Bundle;
import android.app.Activity;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.view.*;
import android.widget.*;
import butterknife.*;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.shade.com.alibaba.fastjson.JSONArray;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.shade.com.alibaba.fastjson.JSONObject;
import com.yimeinew.activity.R;
import com.yimeinew.activity.base.BaseActivity;
import com.yimeinew.activity.deviceproduction.commsub.ChargingActivity;
import com.yimeinew.activity.deviceproduction.commsub.CommGJActivity;
import com.yimeinew.activity.qc.FirstInspectionActivity;
import com.yimeinew.adapter.tabledataadapter.BaseTableDataAdapter;
import com.yimeinew.data.EquipmentInfo;
import com.yimeinew.data.GJViewEntity;
import com.yimeinew.data.MESPRecord;
import com.yimeinew.data.ZCInfo;
import com.yimeinew.listener.OnAlertListener;
import com.yimeinew.listener.OnConfirmListener;
import com.yimeinew.modelInterface.BaseView;
import com.yimeinew.modelInterface.CommBaseView;
import com.yimeinew.network.schedulers.SchedulerProvider;
import com.yimeinew.presenter.CommBasePresenter;
import com.yimeinew.presenter.CommStationZCPresenter;
import com.yimeinew.tableui.CommMultiChoiceModeCallBack;
import com.yimeinew.tableui.TablePanelView;
import com.yimeinew.tableui.entity.HeaderRowInfo;
import com.yimeinew.utils.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class LookBeltFastActivity extends BaseActivity implements CommBaseView {

    private final String TAG_NAME = LookBeltFastActivity.class.getSimpleName();

    private String currMONO = "";//当前工单号
    @BindView(R.id.edt_op)
    EditText edtOP;
    @BindView(R.id.edt_sid1)
    EditText edtSid1;

    @BindView(R.id.bad_record)
    Button bnBadRecord;
    //数据表格
    @BindView(R.id.table_view)
    TablePanelView tableView;
    @BindView(R.id.data_list_content)
    ListView dataListViewContent;
    List<JSONObject> dataList;
    BaseTableDataAdapter adapter;
    private CommMultiChoiceModeCallBack commChoice;

    private CommBasePresenter commPresenter;
    private String fullCheckKEY=TAG_NAME+"fck_kandaiquanjian";
    private GJViewEntity entity;
    private ZCInfo zCnoInfo;
    public static final String Title = "快速过站-->";
    HashMap<String,String> fullCheck=new HashMap<>();
    HashMap<String,String> scqueren=new HashMap<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_look_belt_fast);
        ButterKnife.bind(this);
        zCnoInfo = (ZCInfo) getIntent().getSerializableExtra(CommCL.COMM_ZC_INFO_FLD);
        this.setTitle(Title +"全检"+ zCnoInfo.getName());
        String op=(String) getIntent().getSerializableExtra("op");
        if(!TextUtils.isEmpty(op)){
            edtOP.setText(op);
            CommonUtils.textViewGetFocus(edtSid1);
        }

        commPresenter=new CommBasePresenter(this, SchedulerProvider.getInstance());
        initTableView();
        //获取全检客户
        showLoading();
        commPresenter.getAssistInfo(CommCL.AID_V_KD_CT,"",20);
        //testTalbe();
        String cont="~type=2 and zcno='81'";
        commPresenter.getAssistInfo(CommCL.AID_WEIXIUOK_OP,cont,22);

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
            showMessage("操作员【" + operationUser + "】不存在!");
            CommonUtils.textViewGetFocus(edtOP);
            return true;
        }
        if (id == R.id.edt_op) {
            CommonUtils.textViewGetFocus(edtSid1);
            return false;
        }

        if(id==R.id.edt_sid1){
            String sid1 = edtSid1.getText().toString().toUpperCase();
            if (TextUtils.isEmpty(sid1)) {
                showMessage("请输入测试批号");
                CommonUtils.textViewGetFocus(edtSid1);
                return true;
            }
            //showLoading();
            //查询看带lot信息key=1
            if(!CommonUtils.isRepeat(TAG_NAME+"kandaisid",sid1,8000)){
                String cont = "~lotno='" + sid1 + "' and zcno='"+zCnoInfo.getId()+"'";
                commPresenter.getAssistInfo(CommCL.AID_TEST_LOT_WEB, cont, 1);
            }
        }
        return true;
    }
    /**看带lot查询返回*/
    public void getLotNoCallBack(Boolean bok, JSONArray info, String error, int key){
        if(bok){
            if(info!=null&&info.size()>0){
                JSONObject obj=info.getJSONObject(0);
                int testholdid=obj.getInteger("testholdid");
                int holdid=obj.getInteger("holdid");
                int bok1=obj.getInteger("bok");
                int lotstate=obj.getInteger("lotstate");
                String state=obj.getString("state");
                String state1=obj.getString("state1");
                String lotno=obj.getString("lotno");
                String sid1=obj.getString("sid1");
                String slkid=obj.getString("sid");
                String zcno=obj.getString("zcno");
                if(testholdid==1){
                    myShowSid1Error("该测试批号被HOLD"+lotno);return;
                }
                if(holdid==1){
                    myShowSid1Error("该批次被HOLD"+sid1);return;
                }
                if(lotstate==0){
                    myShowSid1Error("该测试批号未编带"+lotno);return;
                }
                if(lotstate==2){
                    myShowSid1Error("该测试批号已看带"+lotno);return;
                }
                if(bok1==0){
                    myShowSid1Error("该批次不具备开工条件bok="+bok);return;
                }
                if(!TextUtils.equals(CommCL.BATCH_STATUS_DONE,state1)){
                    myShowSid1Error("该测试批号"+lotno+"在编带"+obj.getString("bdsbid")+"未出站,状态为"+state1);return;
                }
                ArrayList<String> slkidal= SqliteUtil.getList(fullCheckKEY);
                if(!fullCheck.containsKey(obj.getString("cus_no"))&&!slkidal.contains(slkid)){//抽检页面遇到必须全检--跳转到全检页面
                    CommonUtils.confirm(this, "跳转看带抽检", "该物料属于抽检，点确定自动跳转到抽检页面。点取消让领班确认是否需要全检", null, new OnConfirmListener() {
                                @Override
                                public void OnConfirm(DialogInterface dialog) {
                                    HashMap<String,Serializable> tz=new HashMap<>();
                                    tz.put("op",edtOP.getText().toString().toUpperCase());
                                    tz.put(CommCL.COMM_ZC_INFO_FLD,zCnoInfo);
                                    jumpNextActivity(LookBeltSpotActivity.class,tz);
                                    finish();
                                }

                                @Override
                                public void OnCancel(DialogInterface dialog) {
                                    qiehuanQuanJian(slkid);
                                }
                            }
                    );
                    return;
                }
                //看带保存记录表--快速过站保存
                String op=edtOP.getText().toString().toUpperCase();
                MESPRecord mp=new MESPRecord(sid1,slkid,zcno,"");
                mp.setHpdate(mp.getMkdate());
                mp.setOutdate(mp.getMkdate());
                mp.setLotno(lotno);
                mp.setOp(op);mp.setOp_b(op);mp.setOp_o(op);
                mp.setState1(CommCL.BATCH_STATUS_DONE);
                mp.setPrd_name(obj.getString("prd_name"));
                mp.setPrd_no(obj.getString("prd_no"));
                mp.setPrd_mark(obj.getString("bincode"));
                mp.setZcno1(obj.getString("zcno1"));
                mp.setRemark(obj.getString("remark"));
                mp.setQty(obj.getInteger("qty"));
                mp.setFircheck(obj.getInteger("fircheck"));
                mp.setPrtno(lotno);
                commPresenter.saveData(CommCL.CELL_ID_D0071,CommonUtils.getJsonObjFromBean(mp),1);//保存记录表
            }else {
                myShowSid1Error("该批次不存在");
            }
        }else{
            myShowSid1Error(error);
        }
    }
    public void saveMESPRecordCallBack(Boolean bok, JSONArray info, JSONObject record, String error, int key){
        if(bok){
            if(info!=null&&info.size()>0){
                JSONObject obj=info.getJSONObject(0);
                String sid=obj.getString("sid");
                //更新[id:200,oldstate:04,newstate:04,sid:MOA19110313-0054;191201TS002004_2,slkid:MOA19110313,zcno:81] key=1
                HashMap<String,String> jsonObject=new HashMap<>();
                jsonObject.put(CommCL.COMM_OLD_STATE_FLD, record.getString("state1"));
                jsonObject.put(CommCL.COMM_NEW_STATE_FLD, record.getString("state1"));
                jsonObject.put(CommCL.COMM_RECORD_SID_FLD, record.getString("sid1")+";"+record.getString("lotno"));
                jsonObject.put(CommCL.COMM_SLKID_FLD, record.getString("slkid"));
                jsonObject.put(CommCL.COMM_ZC_NO_FLD, record.getString("zcno"));
                commPresenter.changeLotStateOneByOne(jsonObject,CommCL.COMM_MES_UDP_CHANGE_STATE_QUICK_VALUE,1);//200更新key=1
                //更新产能报表
                JSONObject jsObj=new JSONObject();
                jsObj.put("sid",sid);
                jsObj.put("capacity",1);
                jsObj.put("checktype","04");
                commPresenter.saveData(CommCL.CELL_ID_D0071Z,jsObj,77);
                adapter.addRecord(record);
            }else{
                myShowSid1Error("保存失败");
            }
        }else {
            myShowSid1Error(error);
        }
    }



    /***
     * 初始化表格
     */
    private void initTableView() {
        dataList = new ArrayList();
        List<HeaderRowInfo> header = CommonUtils.getRowDataListBD();
        dataListViewContent.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        commChoice = new CommMultiChoiceModeCallBack(this,dataListViewContent,dataList);//添加选择器
        dataListViewContent.setMultiChoiceModeListener(commChoice);
        adapter = new BaseTableDataAdapter(this, tableView, dataListViewContent, dataList, header);
        adapter.setTitle("项次");
        adapter.setTitleHeight(100);
        adapter.setSwipeRefreshEnabled(false);
        adapter.setTitleWidth(80);
        tableView.setAdapter(adapter);
    }
    /***
     * 可选择菜单必须实现这个
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
    @Override
    public void commCallBack(Boolean bok, JSONObject info, String error, int key) {

    }

    @Override
    public void commCallBack(Boolean bok, JSONObject info, String error, String key) {

    }
    private void getquanjianInfoBack(Boolean bok, JSONArray info, String error, int key) {
        hideLoading();
        if (bok) {
            fullCheck=CommonUtils.JSONArrayToMap(info,"slkid","oper");
        }else{

            showMessage(error);
        }
    }
    private void getSCQuerenInfoBack(Boolean bok, JSONArray info, String error, int key) {
        hideLoading();
        if (bok) {
            scqueren=CommonUtils.JSONArrayToMap(info,"usrcode","pwd");
        }else{

            showMessage(error);
        }
    }
    @Override
    public void getAssistInfoBack(Boolean bok, JSONArray info, String error, int key) {
        switch (key) {
            case 1:
                getLotNoCallBack(bok, info, error, key);
                break;
            case 20:
                getquanjianInfoBack(bok, info, error, key);
                break;
            case 22:
                getSCQuerenInfoBack(bok, info, error, key);
                break;
            default:
                hideLoading();
                showMessage("key=" + key + " error=" + error);
                break;
        }
    }

    @Override
    public void saveDataBack(Boolean bok, JSONArray info, JSONObject record, String error, int key) {
        switch (key) {
            case 1:
                saveMESPRecordCallBack(bok, info,record, error, key);
                break;
            case 77:
                break;
            default:
                if(!bok) {
                    myShowSid1Error("key=" + key + " error=" + error);
                }
                break;
        }
    }

    @Override
    public void updateDataBack(Boolean bok, JSONArray info, String error, int key) {

    }

    @Override
    public void changeRecordStateBack(Boolean bok, JSONArray info, String error, int key) {
        if(bok){
            hideLoading();
            CommonUtils.textViewGetFocus(edtSid1);
        }else{
            myShowSid1Error(error);
        }
    }

    @OnClick(R.id.bad_record)
    public void OnClick(View view){
        int selectNum = 0;
        int id=view.getId();
        switch (id){
            case R.id.bad_record:
                selectNum = dataListViewContent.getCheckedItemCount();//选择条数
                if(selectNum==1) {
                    List<JSONObject> list = commChoice.getSelectList();//选择内容
                    List<Integer> selectIndex = commChoice.getSelectIndex();//选择所在列表的位置
                    JSONObject object=list.get(0);
                    //showMessage(list.toString());
                    //showMessage(commChoice.getSelectIndex().toString());
                    HashMap<String, Serializable> hm = new HashMap<String, Serializable>();
                    MESPRecord mp=JSONObject.toJavaObject(object,MESPRecord.class);
                    hm.put(CommCL.JUMP_KEY_MESPRecord,mp);
                    jumpNextActivity(LookBeltNGActivity.class, hm);
                }else if(selectNum==0){
                    jumpNextActivity(LookBeltNGActivity.class);
                }else{
                    showMessage("请选择一条记录或者不选记录再点击不良记录");
                }
                break;
        }
    }



    public void myShowSid1Error(String error){
        hideLoading();
        showMessage(error);
        CommonUtils.textViewGetFocus(edtSid1);
    }


    public void testTalbe(){
        JSONObject object=new JSONObject();
        object.put("lotno","180516TS003062_3");
        object.put("state1","04");
        object.put("qty",888);
        adapter.addRecord(object);
        JSONObject obj1=new JSONObject();
        obj1.put("lotno","180TS7778880");
        obj1.put("state1","04");
        obj1.put("qty",999);
        adapter.addRecord(obj1);
        JSONObject obj2=new JSONObject();
        obj2.put("lotno","180524TS001013_2");
        obj2.put("state1","04");
        obj2.put("qty",99);
        adapter.addRecord(obj2);
        adapter.notifyDataSetChanged();
    }
    public void testTalbe1(){
        JSONObject object=new JSONObject();
        object.put("lotno","290TS7778880");
        object.put("state1","04");
        object.put("qty",888);
        adapter.addRecord(object);
        JSONObject obj1=new JSONObject();
        obj1.put("lotno","280TS7778880");
        obj1.put("state1","04");
        obj1.put("qty",999);
        adapter.addRecord(obj1);
        adapter.notifyDataSetChanged();
    }

    private void qiehuanQuanJian(String slkid){
        LinearLayout lay=new LinearLayout(this);
        TextView tishi=new TextView(this);
        tishi.setText("确认将"+slkid+"工单，切换成全检");
        EditText user=new EditText(this);
        user.setHint("用户名");
        EditText pass=new EditText(this);
        pass.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
        pass.setHint("密码");
        LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(10,10,10,10);
        tishi.setLayoutParams(params);
        user.setLayoutParams(params);
        pass.setLayoutParams(params);
        lay.addView(tishi);
        lay.addView(user);
        lay.addView(pass);
        lay.setOrientation(LinearLayout.VERTICAL);
        lay.setGravity(Gravity.CENTER);
        CommonUtils.confirm(this, "全检设置", "", lay, new OnConfirmListener() {
            @Override
            public void OnConfirm(DialogInterface dialog) {
                String username = user.getText().toString().toUpperCase();
                String password=pass.getText().toString();
                if(scqueren.containsKey(username)&&TextUtils.equals(scqueren.get(username),password)){
                    ArrayList<String> slkidal= SqliteUtil.getList(fullCheckKEY);
                    slkidal.add(slkid);
                    SqliteUtil.put(fullCheckKEY,slkidal);
                    showSuccess("正确");
                }else{
                    showMessage("用户名或密码错误!");
                }
            }

            @Override
            public void OnCancel(DialogInterface dialog) {
                showMessage("取消操作");
            }
        });
    }
}
