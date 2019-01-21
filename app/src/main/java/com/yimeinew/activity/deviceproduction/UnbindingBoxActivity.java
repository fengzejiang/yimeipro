package com.yimeinew.activity.deviceproduction;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.*;
import butterknife.*;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.shade.com.alibaba.fastjson.JSONArray;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.shade.com.alibaba.fastjson.JSONObject;
import com.yimeinew.activity.R;
import com.yimeinew.activity.base.BaseApplication;
import com.yimeinew.adapter.tabledataadapter.BaseTableDataAdapter;
import com.yimeinew.data.*;
import com.yimeinew.modelInterface.BaseStationBindingView;
import com.yimeinew.network.schedulers.SchedulerProvider;
import com.yimeinew.presenter.UnBindingPresenter;
import com.yimeinew.tableui.TablePanelView;
import com.yimeinew.tableui.entity.HeaderRowInfo;
import com.yimeinew.utils.*;
import com.zyao89.view.zloading.ZLoadingDialog;
import com.zyao89.view.zloading.Z_TYPE;

import java.util.ArrayList;
import java.util.List;

/**
 * 解绑料盒号
 * @Auther: fengzejiang1987@163.com
 * @Date : 2018/12/8 22:16
 */
public class UnbindingBoxActivity extends AppCompatActivity implements BaseStationBindingView {

    private final String TAG_NAME = UnbindingBoxActivity.class.getSimpleName();
    @BindView(R.id.edt_op)
    EditText edtOp;
    @BindView(R.id.edt_box)
    EditText edtBox;
    @BindView(R.id.edt_sid1)
    EditText edtSid1;

    //数据表格
    @BindView(R.id.table_view)
    TablePanelView tableView;
    @BindView(R.id.data_list_content)
    ListView dataListViewContent;
    List<JSONObject> dataList;


    BaseTableDataAdapter adapter;

    private BindInfo unBindInfo;

    private ZLoadingDialog zLoadingView;

    private UnBindingPresenter presenter;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unbindbox);
        ButterKnife.bind(this);
        registerReceiver(barcodeReceiver, new IntentFilter(
                CommCL.INTENT_ACTION_SCAN_RESULT)); // 注册广播
        presenter = new UnBindingPresenter(this, SchedulerProvider.getInstance());
        dataList = new ArrayList();
        List<HeaderRowInfo> header = getRowDataList();
        initView();
        adapter = new BaseTableDataAdapter(this,tableView,dataListViewContent,dataList,header);
        adapter.setSwipeRefreshEnabled(false);
//        adapter.setTitle("项次");
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
        HeaderRowInfo boxColumn = new HeaderRowInfo("mbox","料盒号",120);
        HeaderRowInfo soprColumn = new HeaderRowInfo("sopr","操作员",150);
        HeaderRowInfo sidColumn = new HeaderRowInfo("sid1","批次号",310);
        rowList.add(boxColumn);
        rowList.add(soprColumn);
        rowList.add(sidColumn);
        return rowList;
    }

    /***
     * Activity销货事件
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(barcodeReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(barcodeReceiver, new IntentFilter(
                CommCL.INTENT_ACTION_SCAN_RESULT)); // 注册广播
    }

    /***
     * 注册广播事件，监听PDA扫描
     */
    private BroadcastReceiver barcodeReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if(CommCL.INTENT_ACTION_SCAN_RESULT.equals(intent.getAction())){
                View rootView = getCurrentFocus();//获取光标当前所在组件
                Object tag = rootView.findFocus().getTag();
                if (tag == null) {
                    return;
                }
                String barCodeData = null;
                if(TextUtils.isEmpty(intent.getStringExtra(CommCL.SCN_CUST_HONEY))){
                    barCodeData = intent.getStringExtra(CommCL.SCN_CUST_EX_SCODE);
                }else{
                    barCodeData = intent.getStringExtra(CommCL.SCN_CUST_HONEY);
                }
                barCodeData = barCodeData.toUpperCase();
                if(edtOp.getTag().equals(tag)){
                    edtOp.setText(barCodeData);
                    onEditTextKeyDown(edtOp);
                }
                if(edtBox.getTag().equals(tag)){
                    edtBox.setText(barCodeData);
                    onEditTextKeyDown(edtBox);
                }
                if(edtSid1.getTag().equals(tag)){
                    edtSid1.setText(barCodeData);
                    onEditTextKeyDown(edtSid1);
                }
            }
        }
    };


    /***
     * 给输入框注册回车事件
     * @param editText
     * @return 返回true不跳转，返回false跳转
     */
    @OnEditorAction({R.id.edt_op,R.id.edt_box,R.id.edt_sid1})
    public boolean OnEditorAction(EditText editText,int id){
        //无论哪个输入框的输入完成，首先判断操作员输入框是否有值，如果有值，则继续往下
        //如果操作员输入框没有值,则跳转到操作员输入框，
        Log.d("回车事件",id+"");
        editText.setText(editText.getText().toString().toUpperCase());
        return onEditTextKeyDown(editText);
    }

    /***
     * 输入框回车事件方法
     * @param editText
     * @return
     */
    private boolean onEditTextKeyDown(EditText editText) {
        if(TextUtils.isEmpty(edtOp.getText().toString())){
            showMessage("请输入操作员！");
            CommonUtils.textViewGetFocus(edtOp);
            return true;
        }
        String opv = edtOp.getText().toString();
        String op1 = CommCL.sharedPreferences.getString(opv,"");
        if(op1.length()==0){
            showMessage("该操作员"+opv+"不存在!");
            int id = getCurrentFocus().getId();
            if(id==edtOp.getId()){
                edtOp.selectAll();
                return true;
            }else{
                CommonUtils.textViewGetFocus(edtOp);
                return true;
            }
        }
        if(R.id.edt_op == editText.getId()){
            CommonUtils.textViewGetFocus(edtBox);
            return false;
        }
        if(TextUtils.isEmpty(edtBox.getText().toString())){
            showMessage("请输入料盒号！");
            CommonUtils.textViewGetFocus(edtBox);
            return true;
        }
        if(R.id.edt_box == editText.getId()){
            showLoading();
            presenter.checkBoxExit(editText.getText().toString());
            return false;
        }
        if(TextUtils.isEmpty(edtSid1.getText().toString())){
            showMessage("请输入批次号！");
            CommonUtils.textViewGetFocus(edtSid1);
            return true;
        }
        if(R.id.edt_sid1 == editText.getId()){
            showLoading();
            presenter.getBatchInfo(edtSid1.getText().toString());
            return false;
        }
        return false;
    }

    /***
     * 显示加载页面
     */
    @Override
    public void showLoading() {
        if(zLoadingView==null){
            zLoadingView = CommonUtils.initLoadingView(this,getString(R.string.loading), Z_TYPE.CIRCLE_CLOCK);
        }
        zLoadingView.show();
    }


    /***
     * 隐藏加载模态框
     */
    @Override
    public void hideLoading() {
        if(zLoadingView!=null)
            zLoadingView.dismiss();
    }

    /***
     * 料盒号输入框远程访问回调
     * @param bok 是否存在该料盒号
     */
    @Override
    public void checkMboxCallBack(boolean bok,String error,int key) {
        hideLoading();
        if(bok){
            CommonUtils.textViewGetFocus(edtSid1);
        }else{
            showMessage(error);
            CommonUtils.textViewGetFocus(edtBox);
        }
    }

    /***
     * 解绑批次输入框远程访问回调处理
     * @param bok 是否存在
     * @param batchInfo 如果存在，返回JSON格式的数据
     * @param errorInf 如果不存在返回错误信息
     */
    @Override
    public void checkSidCallBack(final boolean bok, final JSONObject batchInfo,final String errorInf) {
        if(bok){
            //添加一条记录到列表中
            String batchBox = batchInfo.containsKey(CommCL.COMM_M_BOX_FLD)?batchInfo.getString(CommCL.COMM_M_BOX_FLD):"";
            String currBox = edtBox.getText().toString();
            String sid1 = edtSid1.getText().toString();
            String op = edtOp.getText().toString();
            if(currBox.equals(batchBox)){
                unBindInfo = makeUnBindInfo(batchInfo, currBox, sid1, op);
                presenter.saveBean(unBindInfo);
            }else{
                hideLoading();
                showMessage("该【"+sid1+"】绑定的料盒号为:【"+batchBox+"】与扫描的不一样！");
                edtSid1.selectAll();
            }
        }else{
            hideLoading();
            CommonUtils.textViewGetFocus(edtSid1);
            showMessage(errorInf);
        }
    }

    /***
     *
     * @param bok 是否存在
     * @param sbInfo 如果存在，返回JSON格式的数据
     * @param error 如果不存在返回错误信息
     */
    @Override
    public void checkSbIdCallBack(boolean bok, EquipmentInfo sbInfo, String error) {
    }

    @Override
    public void checkRecordCallBack(boolean bok, JSONObject sbInfo, String error) {

    }

    /***
     *
     * @return
     */
    @Override
    public String getCurrMO() {
        return null;
    }

    /***
     *
     * @param bok  是否成功
     * @param record 如果成功，返回生产记录，反之，返回null
     * @param error 保存失败，返回错误信息
     */
    @Override
    public void saveRecordBack(boolean bok, Object record, String error) {
    }

    @Override
    public void changeRecordStateBack(boolean b, Object record, String s) {

    }

    /***
     * 批次更改批次状态返回处理
     * @param bok 是否成功
     * @param recordList 如果处理成功，返回当前记录
     * @param error 如果失败，返回当前错误信息
     */
    @Override
    public void changeMultiRecordStateBack(boolean bok, List<MESPRecord> recordList, String error) {

    }

    /***
     *
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

    /***
     * 初始化解绑数据对象
     * @param batchInfo 服务端返回的批次信息
     * @param currBox 当前料盒号
     * @param sid1 批次号
     * @param op 当前作业员
     * @return 返回解绑对象
     */
    private BindInfo makeUnBindInfo(JSONObject batchInfo, String currBox, String sid1, String op) {
        BindInfo unBindInfo = new BindInfo();
        unBindInfo.setSid1(sid1);
        unBindInfo.setMbox(currBox);
        unBindInfo.setSopr(op);
        unBindInfo.setDcid(CommonUtils.getMacID());
        unBindInfo.setHpdate(DateUtil.getCurrDateTime(ICL.DF_YMD));
        unBindInfo.setMkdate(DateUtil.getCurrDateTime(ICL.DF_YMDT));
        unBindInfo.setSlkid(batchInfo.getString("slkid"));
        unBindInfo.setSmake(BaseApplication.currUser.getUserCode());
        unBindInfo.setSorg(BaseApplication.currUser.getDeptCode());
        String zcno = batchInfo.containsKey(CommCL.COMM_ZC_NO_FLD)?batchInfo.getString(CommCL.COMM_ZC_NO_FLD):"";
        unBindInfo.setZcno(zcno);
        return unBindInfo;
    }


    /***
     * 点成调用失败
     * @param message 错误消息
     */
    @Override
    public void onRemoteFailed(String message) {
        hideLoading();
        showMessage(message);
    }

    /***
     * 显示提示消息
     * @param message 消息内容
     */
    @Override
    public void showMessage(String message) {
        CommonUtils.showMessage(this,message);
//        TastyToast.makeText(this, message, TastyToast.LENGTH_LONG, TastyToast.ERROR).setGravity(Gravity.TOP, 10, 10);
    }


    @Override
    public void jumpNextActivity(Class<?> descClass) {
    }

    @Override
    public void jumpNextActivity(Context srcContent, Class<?> descClass) {
    }

    /***
     * 添加数据到当前tableView
     * @param unBindInfo 数据对象
     */
    @Override
    public void addRow(Object unBindInfo) {
        hideLoading();
        adapter.addRecord(CommonUtils.getJsonObjFromBean(unBindInfo));
        CommonUtils.textViewGetFocus(edtBox);
    }

    @Override
    public void checkActionBack(boolean bok, int key, CeaPars ceaPars, CWorkInfo cWorkInfo, String error) {

    }

}
