package com.yimeinew.activity.ck;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.*;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.*;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnEditorAction;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.shade.com.alibaba.fastjson.JSON;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.shade.com.alibaba.fastjson.JSONArray;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.shade.com.alibaba.fastjson.JSONObject;
import com.yimeinew.activity.R;
import com.yimeinew.activity.base.BaseActivity;
import com.yimeinew.activity.base.BaseApplication;
import com.yimeinew.activity.deviceproduction.commsub.CommGJActivity;
import com.yimeinew.adapter.tabledataadapter.BaseTableDataAdapter;
import com.yimeinew.data.*;
import com.yimeinew.modelInterface.CommBaseView;
import com.yimeinew.network.schedulers.SchedulerProvider;
import com.yimeinew.presenter.CommBasePresenter;
import com.yimeinew.presenter.CommOtherPresenter;
import com.yimeinew.tableui.TablePanelView;
import com.yimeinew.tableui.entity.HeaderRowInfo;
import com.yimeinew.utils.*;
import com.yimeinew.view.AuxText;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ScrkActivity extends BaseActivity implements CommBaseView {

    private static final String Title = "生产入库";
    @BindView(R.id.edt_op)
    AuxText edtOP;
    @BindView(R.id.edt_bat_no)
    EditText edtBatNo;
    @BindView(R.id.edt_mm_no)
    EditText edtMMNO;
    @BindView(R.id.text_fill_pack)
    EditText edtTFP;
    @BindView(R.id.text_already_scan)
    EditText edtTAS;
    @BindView(R.id.bn_submit)
    Button button;

    //数据表格
    @BindView(R.id.table_view)
    TablePanelView tableView;
    @BindView(R.id.data_list_content)
    ListView dataListViewContent;
    List<JSONObject> dataList;
    BaseTableDataAdapter adapter;
    String mm_no="";//缓存缴库单号判断第一次和第二次
    String bat_no="";
    int fill_qty=0;
    boolean b_rk_no=false;//用于是否更新收料人和入库时间
    private CommBasePresenter commPresenter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scrk);
        this.setTitle(Title );
        ButterKnife.bind(this);
        initTableView1();
        commPresenter = new CommBasePresenter(this, SchedulerProvider.getInstance());
        edtOP.setOPAux();
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

    @OnEditorAction({R.id.edt_op,R.id.edt_bat_no})
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
            CommonUtils.textViewGetFocus(edtBatNo);
            return false;
        }
        String batNo = edtBatNo.getText().toString().toUpperCase();
        if (TextUtils.isEmpty(batNo)) {
            showMessage("请输入生产批号");
            CommonUtils.textViewGetFocus(edtBatNo);
            return true;
        }
        if (id == R.id.edt_bat_no) {
            //第一次扫描生产批号
            if(TextUtils.isEmpty(mm_no)){
                showLoading();
                //通过生产批号获取缴库单号，拿到缴库单号获取列表
                String cont="~tm.bat_no='"+batNo+"'";
                commPresenter.getAssistInfo(CommCL.AID_TMM0_WEB,cont,1);
            }else{
                showLoading();
                //第二次扫描无需获取直接缓存校验
                checkBatNoWeb(batNo);//检验是否在列表然后是未校验的去改状态
            }
        }
        return true;
    }

    /***
     * 初始化表格头数据
     * @return 返回表格头
     */
    public List<HeaderRowInfo> getRowDataList(){
        List<HeaderRowInfo> rowList = new ArrayList<>();
        HeaderRowInfo sidColumn = new HeaderRowInfo("checkid", "检验", 100);
        sidColumn.setAttr(3);
        sidColumn.setContrastMap(CommCL.CheckMap);
        sidColumn.setContrastColors(CommCL.CheckColorMap);
        rowList.add(sidColumn);
        sidColumn = new HeaderRowInfo("bat_no", "批号", 180);
        rowList.add(sidColumn);
        sidColumn = new HeaderRowInfo("mo_no", "制令单号", 180);
        rowList.add(sidColumn);
        sidColumn = new HeaderRowInfo("prd_no", "货品代号", 150);
        rowList.add(sidColumn);
        sidColumn = new HeaderRowInfo("prd_name", "货品名称", 320);
        rowList.add(sidColumn);
        sidColumn = new HeaderRowInfo("prd_mark", "Bincode", 150);
        rowList.add(sidColumn);
        sidColumn = new HeaderRowInfo("wh", "库位", 150);
        rowList.add(sidColumn);
        sidColumn = new HeaderRowInfo("qty", "数量", 150);
        rowList.add(sidColumn);
        sidColumn = new HeaderRowInfo("rem", "备注", 300);
        rowList.add(sidColumn);
        return rowList;
    }
    /***
     * 初始化表格
     */
    private void initTableView1() {
        dataList = new ArrayList();
        List<HeaderRowInfo> header =getRowDataList();
        dataListViewContent.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        adapter = new BaseTableDataAdapter(this, tableView, dataListViewContent, dataList, header);
        adapter.setTitle("项次");
        adapter.setTitleHeight(80);
        adapter.setSwipeRefreshEnabled(false);
        adapter.setTitleWidth(80);
        tableView.setAdapter(adapter);
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
                adapter.addRecord(jsonV);
            }
        }
    }
    @OnClick({R.id.bn_submit,R.id.edt_mm_no})
    public void OnClick(View view){
        switch (view.getId()){
            case R.id.bn_submit:
                unUseClick();
                showLoading();
                if(dataList==null||dataList.size()==0){
                    myError("请先扫描入库信息");
                    hideLoading();
                    useClick();
                    return;
                }
                if(isWholeAndSumCQty()){
                    //审核开始--先查询审核流--然后提交审核
                    JSONObject obj=dataList.get(0);
                    CeaPars ceaPars = new CeaPars();
                    ceaPars.setSid(obj.getString("mm_no"));
                    ceaPars.setSbuid(obj.getString("sbuid"));//根据PC端对于页面的菜单参数来确认是哪个sbuid
                    ceaPars.setStatefr(obj.getInteger("state"));
                    ceaPars.setStateto(obj.getInteger("state"));
                    commPresenter.getApprovalInfo(ceaPars);
                }else{
                    myError("没有检验完成！");
                }
                break;
            case R.id.edt_mm_no:
                if(!TextUtils.isEmpty(mm_no)) {
                    Bitmap bit = QRUtil.createQRImage(mm_no, 400, 400);
                    ImageView imageView = new ImageButton(this);
                    imageView.setImageBitmap(bit);
                    CommonUtils.alert(this, "缴库单号", mm_no, imageView, null);
                }
                break;
        }



    }

    /**
     * 批号查询获取缴库单号
     */
    public void getBatNoCallBack(Boolean bok, JSONArray info, String error, int key){
        if(bok){
            if(info!=null&&info.size()>0){
                JSONObject obj=info.getJSONObject(0);
                //获取缴库单号。和状态。。
                String mm_no=obj.getString("mm_no");
                String wh=obj.getString("wh");
                int state=obj.getInteger("state");
                int holdid=obj.getInteger("holdid");
                int rkqty=obj.getInteger("rkqty");
                b_rk_no=!obj.containsKey("rk_no");
                if(TextUtils.isEmpty(wh)){
                    myError("入库申请单库位为空。");
                }else if(holdid==CommCL.HOLD){
                    myError("改批次被HOLd");
                }else if(state==6){
                    myError("该缴库单已经是执行状态");
                }else if(rkqty==0){
                    myError("本次入库数量为0");
                }else{
                    //去获取整个缴库单 key=2
                    String cont="~tm.mm_no='"+mm_no+"'";
                    commPresenter.getAssistInfo(CommCL.AID_TMM0_WEB,cont,2);
                    this.mm_no=mm_no;
                    this.bat_no=obj.getString("bat_no");
                    this.fill_qty=rkqty;
                }
            }else{
                myError("没有查询到该批号");
            }
        }else{
            myError(error);
        }
    }
    /***获取整个缴库单返回*/
    public void getMMNoCallBack(Boolean bok, JSONArray info, String error, int key){
        if(bok){
            if(info!=null&&info.size()>0){
                addRow(info);
                edtMMNO.setText(mm_no);//缴库单号
                edtTFP.setText(""+fill_qty);//满箱数量
                checkBatNoWeb(bat_no);

            }else {
                myError("没有查询到该缴库单号");
            }
        }else{
            myError(error);
        }
    }



    /***
     * 调用审批返回
     * @param bok 是否成功
     * @param key 调用的是33还是34,33：获取审批信息，34执行审批信息
     * @param ceaPars 交互过程中的状态
     * @param cWorkInfo 审批流信息
     * @param error 错误信息
     */
    @Override
    public void checkActionBack(boolean bok, int key, CeaPars ceaPars, CWorkInfo cWorkInfo, String error) {
        if (bok) {
            hideLoading();
            if (key == CommCL.COMM_CHK_LIST) {
                //弹出dialog，并
                checkUp(ceaPars, cWorkInfo);
            }else {
                CommonUtils.showMessage(this,"提交成功");
                clearCache();
                useClick();
            }
        } else {
            hideLoading();
            showMessage(error);
            useClick();
            return;
        }
    }
    /**审核流查询后校验*/
    private int chooseIndex = 0;//选择项
    public void checkUp(CeaPars ceaPars, CWorkInfo cWorkInfo) {
        if (cWorkInfo.getList() == null) {
            showMessage("没有审批节点!");
            hideLoading();
            return;
        }
        ApprovalFlowObj approvalFlowObj = cWorkInfo.getList().get(0);
        if (approvalFlowObj.getUsers() == null) {

            //自己提交自己审核
            ceaPars.setStateto(Integer.parseInt(approvalFlowObj.getStateId()));
            ceaPars.setTousr(BaseApplication.currUser.getUserCode());
            showLoading();
            commPresenter.checkActionUp(ceaPars);
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("请选择审核人");
            builder.setCancelable(false); // 设置不可点击界面之外的区域让对话框消失
            final String[] Auditor = new String[approvalFlowObj.getUsers().size()];
            for (int j = 0; j < approvalFlowObj.getUsers().size(); j++) {
                User user = approvalFlowObj.getUsers().get(j);
                Auditor[j] = user.getUserName();
            }
            builder.setSingleChoiceItems(Auditor, chooseIndex, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    chooseIndex = which;
                }
            });
            builder.setPositiveButton("确定", new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ceaPars.setStateto(Integer.parseInt(approvalFlowObj.getStateId()));
                    ceaPars.setTousr(BaseApplication.currUser.getUserCode());
                    showLoading();
                    commPresenter.checkActionUp(ceaPars);
                }
            });
            builder.show();
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
            case 1:getBatNoCallBack(bok,info,error,key);break;//用batno获取、缴库单号
            case 2:getMMNoCallBack(bok,info,error,key);break;//用mm_no获取、整个缴库单号
        }
    }

    @Override
    public void saveDataBack(Boolean bok, JSONArray info,JSONObject record, String error, int key) {

    }

    @Override
    public void updateDataBack(Boolean bok, JSONArray info, String error, int key) {
        if (bok){
            switch (key){
                case 1:
                    if(info!=null&&info.size()>0) {
                        checkBatNo(info.getJSONObject(0).getString("bat_no"));//更新成功，改变缓存数据
                        if(b_rk_no){
                            b_rk_no=false;//第一次更新成功后。更新收料人
                            JSONObject rk=new JSONObject();
                            rk.put("mm_no",info.getJSONObject(0).getString("mm_no"));
                            rk.put("rk_no",edtOP.getText().toString().toUpperCase());
                            rk.put("rk_date", DateUtil.getCurrDateTime(ICL.DF_YMDT));
                            commPresenter.updateData(CommCL.CELL_ID_E0004WEB,rk,2);//更新收料员和入库时间
                        }
                    }else{
                        myError("更新失败。");
                    }
                    break;
            }
        }else{
            myError(error);
        }
    }

    @Override
    public void changeRecordStateBack(Boolean bok, JSONArray info, String error, int key) {

    }

    @Override
    public void onRemoteFailed(String message) {
        hideLoading();
        CommonUtils.showError(this, "onRemoteFailed="+message);
    }
    @Override
    public void clear() {
        adapter.clear();
        this.dataList.clear();
    }

    public void myError(String error){
        hideLoading();
        CommonUtils.textViewGetFocus(edtBatNo);
        showMessage(error);
    }
    public void checkBatNoWeb(String bat_no){
        boolean b=false;
        for(int i=0;i<dataList.size();i++){
            JSONObject obj=dataList.get(i);
            if(TextUtils.equals(obj.getString("bat_no"),bat_no)){
                if(TextUtils.equals(obj.getString("checkid"),"0")) {
                    JSONObject record=new JSONObject();
                    record.put("mm_no",obj.getString("mm_no"));
                    record.put("itm",obj.getInteger("itm"));
                    record.put("checkid",1);
                    record.put("bat_no",bat_no);
                    commPresenter.updateData(CommCL.CELL_ID_E0004AWEB,record,1);//更新状态
                    b=true;
                }else{
                    b=true;
                    isWholeAndSumCQty();
                    myError("已校验");
                }
                break;
            }
        }
        if(!b){
            myError("该批号不在该缴库单里");
        }
    }
    public void checkBatNo(String bat_no){
        int index=-1;
        for(int i=0;i<dataList.size();i++){
            JSONObject obj=dataList.get(i);
            if(TextUtils.equals(obj.getString("bat_no"),bat_no)){
                if(TextUtils.equals(obj.getString("checkid"),"0")) {
                    index = i;
                    obj.put("checkid", 1);
                    hideLoading();
                    CommonUtils.textViewGetFocus(edtBatNo);
                    isWholeAndSumCQty();
                }else{
                    isWholeAndSumCQty();
                    myError("已校验");
                }
                break;
            }
        }
        adapter.notifyDataSetChanged();
    }

    /**
     * 判断是否全部校验完成和已扫描数量合计
     * @return
     */
    public boolean isWholeAndSumCQty(){
        boolean b=true;
        int qty=0;
        for(JSONObject obj:dataList){
            if(TextUtils.equals(obj.getString("checkid"),"0")){
                b=false;
            }else{
                qty+=obj.getInteger("qty");
            }
        }
        edtTAS.setText(""+qty);
        return b;
    }
    public void clearCache(){
        mm_no="";
        bat_no="";
        fill_qty=0;
        edtBatNo.setText("");
        edtMMNO.setText("");
        edtTAS.setText("");
        edtTFP.setText("");
        clear();
    }
    public void unUseClick(){
        button.setEnabled(false);
    }
    public void useClick(){
        button.setEnabled(true);
    }
}
