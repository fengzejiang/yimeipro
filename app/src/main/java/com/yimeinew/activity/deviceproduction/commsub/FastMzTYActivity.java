package com.yimeinew.activity.deviceproduction.commsub;

import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.*;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnEditorAction;
import butterknife.OnItemSelected;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.shade.com.alibaba.fastjson.JSONArray;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.shade.com.alibaba.fastjson.JSONObject;
import com.yimeinew.activity.R;
import com.yimeinew.activity.base.BaseActivity;
import com.yimeinew.activity.base.BaseApplication;
import com.yimeinew.adapter.tabledataadapter.BaseTableDataAdapter;
import com.yimeinew.data.MESPRecord;
import com.yimeinew.data.ZCInfo;
import com.yimeinew.entity.Pair;
import com.yimeinew.modelInterface.CommFastView;
import com.yimeinew.network.schedulers.SchedulerProvider;
import com.yimeinew.presenter.CommFastPresenter;
import com.yimeinew.tableui.TablePanelView;
import com.yimeinew.tableui.entity.HeaderRowInfo;
import com.yimeinew.utils.CommCL;
import com.yimeinew.utils.CommonUtils;
import com.yimeinew.utils.DateUtil;
import com.yimeinew.utils.ICL;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FastMzTYActivity extends BaseActivity implements CommFastView {
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
    private String currMONO = "";//当前工单号

    ArrayAdapter<Pair> zcAdapter;
    private List<Pair> zcAdapterData = new ArrayList<>();
    ArrayList<ZCInfo> zcInfoList = BaseApplication.zcList;
    private HashMap<String, String> bindSid1 = new HashMap<>();

    private String zcno="";//当前制程
    private ZCInfo zCnoInfo;
    private String GBKEY="kuaiguozhan_MZ";
    IntentFilter intentFilter;
    private CommFastPresenter commPresenter;

    public static final String Title = "快速过站";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fast);
        ButterKnife.bind(this);
        zCnoInfo = (ZCInfo) getIntent().getSerializableExtra(CommCL.COMM_ZC_INFO_FLD);
        zcno=zCnoInfo.getId();
        this.setTitle(Title+"->"+zCnoInfo.getName());

        commPresenter = new CommFastPresenter(this,SchedulerProvider.getInstance());
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
                commPresenter.checkQuickLot(sid1,zcno,1);
             }
        }
        return false;
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
    public void getQuickLotBack(Boolean bok, JSONObject batchInfo, String error, int key) {
        if (bok) {
            //保存记录
            String sid1 = batchInfo.getString("sid1");//批次号
            String mono = batchInfo.getString("sid");
            String zcno1 = batchInfo.getString("zcno1");
            String remark = batchInfo.getString("remark");
            int qty = batchInfo.getInteger("qty");
            String prd_no = batchInfo.getString("prd_no");
            String prd_name = batchInfo.getString("prd_name");
            int fircheck = batchInfo.getInteger("fircheck");
            MESPRecord record = new MESPRecord(sid1, mono, zcno,"");
            String op=edtOP.getText().toString().toUpperCase();
            record.setOp(op);
            record.setOp_b(op);
            record.setOp_o(op);
            record.setHpdate(DateUtil.getCurrDateTime(ICL.DF_YMDT));
            record.setOutdate(DateUtil.getCurrDateTime(ICL.DF_YMDT));
            record.setZcno1(zcno1);
            record.setRemark(remark);
            record.setState1(CommCL.BATCH_STATUS_DONE);
            record.setQty(qty);
            record.setPrd_no(prd_no);
            record.setPrd_name(prd_name);
            record.setFirstchk(fircheck);
            commPresenter.makeProRecord(record);
        } else {
            hideLoading();
            CommonUtils.canDo(GBKEY);
            showMessage(error);
        }
    }

    @Override
    public String getCurrMO() {
        if (dataList != null && dataList.size() > 0) {
            JSONObject jsonObject = dataList.get(0);
            currMONO = jsonObject.getString("slkid");
        }
        return currMONO;
    }
    //保存记录成功
    @Override
    public void saveRecordBack(boolean bok, Object records, String error) {
        if (bok) {
            //放到扫描的列表中
            MESPRecord record = (MESPRecord) records;
            bindSid1.put(record.getSid1(), record.getSid1());
            // 添加到数据列表
            adapter.addRecord(CommonUtils.getJsonObjFromBean(record));
            //更改批次状态
            //commPresenter.changeRecordStateOneByOne(record, CommCL.BATCH_STATUS_IN);
            commPresenter.changeLotStateOneByOne(record, CommCL.BATCH_STATUS_DONE);
        } else {
            hideLoading();
            CommonUtils.canDo(GBKEY);
            showMessage(error);
        }
    }

    @Override
    public void saveMoCallBack(Boolean bok, JSONObject batchInfo, String error) {

    }

    @Override
    public void saveMarkingCallBack(Boolean bok, JSONObject batchInfo, String error) {

    }

    @Override
    public void getPackInfoCallBack(Boolean bok, JSONObject headinfo, JSONArray info, String error, int key) {

    }

    @Override
    public void getAssistInfoBack(Boolean bok, JSONArray info, String error, int key) {

    }

    @Override
    public void changeRecordStateBack(boolean bok, Object record2, String error) {
        if (bok) {
            hideLoading();
            int key = -1;
            MESPRecord record = (MESPRecord) record2;
            for (int i = 0; i < dataList.size(); i++) {
                MESPRecord record1 = JSONObject.parseObject(dataList.get(i).toJSONString(), MESPRecord.class);
                if (record1.getSid().equals(record.getSid())) {
                    key = i;
                    break;
                }
            }

            if (key == -1) {
                bindSid1.put(record.getSid1(), record.getSid1());
                adapter.addRecord(CommonUtils.getJsonObjFromBean(record));
            } else {
                dataList.set(key, CommonUtils.getJsonObjFromBean(record));
                adapter.notifyDataSetChanged();
            }
            CommonUtils.textViewGetFocus(edtSid1);
            CommonUtils.canDo(GBKEY);


        } else {
            hideLoading();
            CommonUtils.canDo(GBKEY);
            showMessage(error);
            CommonUtils.textViewGetFocus(edtSid1);
        }
    }

    @Override
    public void updateCallBack(boolean bok, Object record, String error, int key) {

    }

    @Override
    public void onRemoteFailed(String message) {

        hideLoading();
        CommonUtils.canDo(GBKEY);
        CommonUtils.showError(this, "onRemoteFailed="+message);

    }
}
