package com.yimeinew.activity.deviceproduction.commsub;

import android.content.*;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
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
import com.yimeinew.listener.OnAlertListener;
import com.yimeinew.listener.OnConfirmListener;
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

import java.lang.String;

public class FastMzActivity extends BaseActivity implements CommFastView {
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
    @BindView(R.id.edt_dpn)
    EditText edtDpn;//单盘数
    @BindView(R.id.edt_code)
    EditText edtCode;//喷码
    @BindView(R.id.text_jishu)
    EditText textJishu;//喷码
    @BindView(R.id.text_totalqty)
    EditText textTotalqty;//投产数
    @BindView(R.id.text_yikaban)
    EditText textYikaban;//卡板过站数
    @BindView(R.id.text_weikaban)
    EditText textWeikaban;//未过卡板数量

    private String currMONO = "";//当前工单号
    private int kbJishu=0;
    private int kbDpn=0;//计数缓存，判断是否回车。



    ArrayAdapter<Pair> zcAdapter;
    private List<Pair> zcAdapterData = new ArrayList<>();
    ArrayList<ZCInfo> zcInfoList = BaseApplication.zcList;
    private HashMap<String, String> bcode = new HashMap<>();

    private String zcno="";//当前制程
    private ZCInfo zCnoInfo;
    private String GBKEY="kabanguozhan_mz";
    IntentFilter intentFilter;
    private CommFastPresenter commPresenter;

    public static final String Title = "快速过站";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fast_mz);
        ButterKnife.bind(this);
        zCnoInfo = (ZCInfo) getIntent().getSerializableExtra(CommCL.COMM_ZC_INFO_FLD);
        zcno=zCnoInfo.getId();
        this.setTitle(Title+"->"+zCnoInfo.getName());

        commPresenter = new CommFastPresenter(this,SchedulerProvider.getInstance());
        initTableView();//表身的布局
        //edtCode.setText("2447620940T-01182320022740000QAB0B51Y");
//        CommonUtils.confirm(this, "title", "mm", null, new OnConfirmListener() {
//            @Override
//            public void OnConfirm(DialogInterface dialog) {
//                showMessage("ok");
//            }
//
//            @Override
//            public void OnCancel(DialogInterface dialog) {
//                showMessage("ng");
//            }
//        });
//        CommonUtils.alert(this, "title", "mm", null, new OnAlertListener() {
//            @Override
//            public void OnConfirm(DialogInterface dialog) {
//                showMessage("alert");
//            }
//        });
    }


    @OnEditorAction({R.id.edt_op,R.id.edt_dpn, R.id.edt_code})
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
        //检测操作员是否存在
        String opv = CommCL.sharedPreferences.getString(operationUser, "");
        if (TextUtils.isEmpty(opv)) {
            if (id == R.id.edt_op) {
                //showMessage("操作员【" + operationUser + "】不存在!");
                CommonUtils.speak("操作员不存在");
            }
            CommonUtils.textViewGetFocus(edtOP);
            return true;
        }
        if (id == R.id.edt_op) {
            //CommonUtils.textViewGetFocus(edtCode);
            CommonUtils.textViewGetFocus(edtDpn);
            return false;
        }

        int dpn=Integer.parseInt(edtDpn.getText().toString()) ;



        if (dpn<0) {
            //showMessage("请输入正确单盘数!");
            CommonUtils.speak("输入单盘数");
            CommonUtils.textViewGetFocus(edtOP);
            return true;
        }

        if (id == R.id.edt_dpn) {
            //给卡板计数进行缓存
            kbJishu=dpn;
            //单盘量进行缓存
            kbDpn=dpn;
            //CommonUtils.textViewGetFocus(edtCode);
            textJishu.setText(dpn+"");
            CommonUtils.textViewGetFocus(edtCode);
            return false;
        }

        if(dpn!=kbDpn){
            //给卡板计数进行缓存
            kbJishu=dpn;
            //单盘量进行缓存
            kbDpn=dpn;
            //CommonUtils.textViewGetFocus(edtCode);
            textJishu.setText(dpn+"");
        }
        if (id == R.id.edt_code) {
            String code = edtCode.getText().toString().toUpperCase();
            //CommonUtils.textViewGetFocus(edtCode);

            if (TextUtils.isEmpty(code)) {
                //showMessage("请输入喷码");
                CommonUtils.speak("请输入喷码");
                CommonUtils.textViewGetFocus(edtCode);
                return true;
            }
            if (bcode.containsKey(code)) {
                //showMessage("该喷码【" + code + "】已经扫描过");
                CommonUtils.speak("已扫描");
                CommonUtils.textViewGetFocus(edtCode);
                return true;
            }
            showLoading();
            //校验喷码
            commPresenter.selectMzkb(code,1);
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
        List<HeaderRowInfo> header = CommonUtils.getRowDataListMz();
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

    /**-工具--**/
    private void getZCAdapterData() {
        if (zcInfoList == null) {
            return;
        }
        for (int i = 0; i < zcInfoList.size(); i++) {
            ZCInfo zc = zcInfoList.get(i);

            if(zc.getBfast()==1&&zc.getSegid()<=1) {//前段和中段快速过站
                System.out.println(zc.getName()+"ss="+zc.getSegid());
                zcAdapterData.add(new Pair(zc.getName(), zc.getId()));
            }
        }
        zcAdapter.notifyDataSetChanged();
    }
    @Override
    public void getQuickLotBack(Boolean bok, JSONObject batchInfo, String error, int key) {
        if (bok) {

            //保存记录
            String mono = batchInfo.getString("mono");//生产记录主键
            String zcno1 = batchInfo.getString("zcno1");
            String remark = batchInfo.getString("remark");
            int totalqty = batchInfo.getInteger("totalqty");
            int apqty=batchInfo.getInteger("apqty");
            int upqty=batchInfo.getInteger("upqty");
            int qty=batchInfo.getInteger("qty");
            String prd_no = batchInfo.getString("prd_no");
            String code=batchInfo.getString("allcode");
            String cbstate=batchInfo.getString("cbstate");
            String lotstate=batchInfo.getString("lotstate");

            MESPRecord record = new MESPRecord(mono, mono, zcno,"");
            String op=edtOP.getText().toString().toUpperCase();
            record.setOp(op);
            record.setOp_b(op);
            record.setOp_o(op);
            record.setZcno1(zcno1);
            record.setRemark(remark);
            record.setState1(CommCL.BATCH_STATUS_DONE);
            record.setQty(qty);
            record.setPrd_no(prd_no);
            record.setTotalqty(totalqty);
            record.setUpqty(upqty);
            record.setApqty(apqty);
            record.setPrtno(code);
            record.setHpdate(record.getMkdate());
            record.setOutdate(record.getMkdate());
            commPresenter.mzkbRecord(record,batchInfo);//存入记录表

        } else {
            hideLoading();
            CommonUtils.canDo(GBKEY);
            CommonUtils.speak(error);
            CommonUtils.textViewGetFocus(edtCode);
            //showMessage(error);

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
    public void saveRecordBack(boolean bok, Object record, String error) {
        if (bok) {
            //放到扫描的列表中
            JSONObject batchInfo= (JSONObject) record;
            //添加 喷码code 的页面缓存
            bcode.put(batchInfo.getString("allcode"), batchInfo.getString("allcode"));
            // 添加到数据列表
            adapter.addRecord(CommonUtils.getJsonObjFromBean(batchInfo));
            //给 卡板计数赋值
            kbJishu--;
            if(kbJishu<0)
            {
                kbJishu= kbDpn-1;
            }
            textJishu.setText(kbJishu + "");
            //提示赋值
            setTiShi(batchInfo.getInteger("totalqty"),batchInfo.getInteger("apqty"));
            //改变喷码子表的状态,通过对象定义
            JSONObject jsObj1=new JSONObject();
            jsObj1.put("allcode",batchInfo.getString("allcode"));
            jsObj1.put("mono",batchInfo.getString("mono"));
            jsObj1.put("state","03");
            jsObj1.put("bdate", DateUtil.getCurrDateTime(ICL.DF_YMDT));

            MESPRecord mr=JSONObject.parseObject(batchInfo.toJSONString(),MESPRecord.class);
            mr.setSid1(batchInfo.getString("mono"));
            mr.setSlkid(batchInfo.getString("mono"));
            //改变lot表卡板站该工单的状态为03
            if(TextUtils.equals(batchInfo.getString("lotstate"),CommCL.BATCH_STATUS_READY)){
                commPresenter.changeLotStateOneByOne(mr,"03");
            }
            //若投产量==已生产量，状态改为"04"
            if(batchInfo.getInteger("totalqty")==batchInfo.getInteger("apqty")){
                commPresenter.changeLotStateOneByOne2(mr, "04");
            }
            commPresenter.updateInfo(jsObj1,CommCL.CELL_ID_D50309WEB,2);
        } else {
            hideLoading();
            CommonUtils.textViewGetFocus(edtCode);
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
            //hideLoading();
//            int key = -1;
//            MESPRecord record = (MESPRecord) record2;
//            for (int i = 0; i < dataList.size(); i++) {
//                MESPRecord record1 = JSONObject.parseObject(dataList.get(i).toJSONString(), MESPRecord.class);
//                if (record1.getSid().equals(record.getSid())) {
//                    key = i;
//                    break;
//                }
//            }
//
//            if (key == -1) {
//                bcode.put(record.getSid1(), record.getSid1());
//                adapter.addRecord(CommonUtils.getJsonObjFromBean(record));
//            } else {
//                dataList.set(key, CommonUtils.getJsonObjFromBean(record));
//                adapter.notifyDataSetChanged();
//            }

            CommonUtils.canDo(GBKEY);


        } else {
            hideLoading();
            CommonUtils.canDo(GBKEY);
            showMessage(error);
        }
    }

    @Override
    public void updateCallBack(boolean bok, Object record, String error, int key) {
        switch (key){
            case 2://更新喷码子表
                hideLoading();
                CommonUtils.textViewGetFocus(edtCode);
                break;
        }
    }

    @Override
    public void onRemoteFailed(String message) {
        hideLoading();
        CommonUtils.canDo(GBKEY);
        CommonUtils.showError(this, "onRemoteFailed="+message);
    }
    public void setTiShi(int totalqty,int qty){
        textTotalqty.setText(""+totalqty);
        textYikaban.setText(""+qty);
        textWeikaban.setText(""+(totalqty-qty));
    }
    public void clearTiShi(){
        textTotalqty.setText("");
        textYikaban.setText("");
        textWeikaban.setText("");
    }
}
