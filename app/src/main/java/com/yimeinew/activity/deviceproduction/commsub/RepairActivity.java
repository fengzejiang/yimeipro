package com.yimeinew.activity.deviceproduction.commsub;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnEditorAction;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.shade.com.alibaba.fastjson.JSONArray;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.shade.com.alibaba.fastjson.JSONObject;
import com.yimeinew.activity.R;
import com.yimeinew.activity.base.BaseActivity;
import com.yimeinew.activity.base.BaseApplication;
import com.yimeinew.adapter.tabledataadapter.BaseTableDataAdapter;
import com.yimeinew.data.MESPRecord;
import com.yimeinew.data.ZCInfo;
import com.yimeinew.entity.Pair;
import com.yimeinew.modelInterface.CommBaseView;
import com.yimeinew.modelInterface.CommFastView;
import com.yimeinew.network.schedulers.SchedulerProvider;
import com.yimeinew.presenter.CommBasePresenter;
import com.yimeinew.presenter.CommFastPresenter;
import com.yimeinew.tableui.TablePanelView;
import com.yimeinew.tableui.entity.HeaderRowInfo;
import com.yimeinew.utils.CommCL;
import com.yimeinew.utils.CommonUtils;
import com.yimeinew.utils.DateUtil;
import com.yimeinew.utils.ICL;
import com.yimeinew.view.AuxText;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RepairActivity extends BaseActivity implements CommFastView {
    private final String TAG_NAME = CommGJActivity.class.getSimpleName();

    //数据表格
    @BindView(R.id.table_view)
    TablePanelView tableView;
    @BindView(R.id.data_list_content)
    ListView dataListViewContent;
    List<JSONObject> dataList;
    BaseTableDataAdapter adapter;
    @BindView(R.id.edt_opc)
    AuxText edtOPC;//检验员
    @BindView(R.id.edt_dpn)
    EditText edtDpn;//单盘数
    @BindView(R.id.edt_code)
    EditText edtCode;//喷码
    @BindView(R.id.text_jishu)
    EditText textJishu;//计数
    @BindView(R.id.bxsl)
    EditText bxsl;
    @BindView(R.id.oksl)
    EditText oksl;
    @BindView(R.id.ngsl)
    EditText ngsl;
    @BindView(R.id.wqr)
    EditText wqr;


    private String currMONO = "";//当前工单号
    private int kbJishu=0;
    private int kbDpn=0;


    ArrayAdapter<Pair> zcAdapter;
    private List<Pair> zcAdapterData = new ArrayList<>();
    ArrayList<ZCInfo> zcInfoList = BaseApplication.zcList;
    private HashMap<String, String>  bcode = new HashMap<>();

    private String zcno="";//当前制程
    private ZCInfo zCnoInfo;
    private String GBKEY="kabanguozhan_mz";
    IntentFilter intentFilter;
    private CommFastPresenter commPresenter;
    private CommBasePresenter commBPresenter;
    String num="";
    String num2="";
    String num3="";
    String num4="";

    String num5="";
    String num6="";
    String num7="";
    String num8="";

    public static final String Title = "维修确认";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weixiu_okng);
        ButterKnife.bind(this);
        zCnoInfo = (ZCInfo) getIntent().getSerializableExtra(CommCL.COMM_ZC_INFO_FLD);
        zcno=zCnoInfo.getId();
        this.setTitle(Title+"->"+zCnoInfo.getName());
        commPresenter = new CommFastPresenter(this,SchedulerProvider.getInstance(),zCnoInfo);
        //commBPresenter=new CommBasePresenter(this,SchedulerProvider.getInstance());
        initTableView();//表身的布局
        //edtCode.setText("2447620940T-01182330066270000PYB5B02Y");
        edtOPC.setOPAux();
    }


    @OnEditorAction({R.id.edt_opc,R.id.edt_dpn, R.id.edt_code})
    public boolean OnEditorAction(EditText editText) {
        return onEditTextKeyDown(editText);
    }
    @Override
    public boolean onEditTextKeyDown(EditText editText) {
        int id = editText.getId();
        String operationUser = edtOPC.getText().toString().toUpperCase();
        if (TextUtils.isEmpty(operationUser)) {
            showMessage("请输入检验员!");
            CommonUtils.textViewGetFocus(edtOPC);
            return true;
        }
        //检测操作员是否存在
        String opv = CommCL.sharedPreferences.getString(operationUser, "");
        if (TextUtils.isEmpty(opv)) {
            if (id == R.id.edt_opc) {
                //showMessage("操作员【" + operationUser + "】不存在!");
                CommonUtils.speak("操作员不存在");
            }
            CommonUtils.textViewGetFocus(edtOPC);
            return true;
        }
        if (id == R.id.edt_opc) {
            CommonUtils.textViewGetFocus(edtDpn);
            return false;
        }
        int dpn=Integer.parseInt(edtDpn.getText().toString()) ;
        if (dpn<0) {
            //showMessage("请输入正确单盘数!");
            CommonUtils.speak("输入单盘数");
            CommonUtils.textViewGetFocus(edtDpn);
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

        if (id == R.id.edt_code) {
            if(dpn>0&&kbDpn==0){
                //给卡板计数进行缓存
                kbJishu=dpn;
                //单盘量进行缓存
                kbDpn=dpn;
                //CommonUtils.textViewGetFocus(edtCode);
                textJishu.setText(dpn+"");
            }
            String code = edtCode.getText().toString().toUpperCase();
            if (TextUtils.isEmpty(code)) {
                //showMessage("请输入喷码");
                CommonUtils.speak("请输入喷码");
                CommonUtils.textViewGetFocus(edtCode);
                return true;
            }
            if (bcode.containsKey(code)) {
                //showMessage("该喷码【" + code + "】已getQuickLotBack经扫描过");
                CommonUtils.speak("已扫描");
                CommonUtils.textViewGetFocus(edtCode);
                return true;
            }

            commPresenter.selectWxqr(code,1);



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
        List<HeaderRowInfo> header = CommonUtils.getRowDataListWxqr();
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
//生产报修数量
            if(key==5){
                num= batchInfo.getString("bxsl");
                if(TextUtils.isEmpty(num)){
                    num="0";
                }
                bxsl.setText(num);
                String mono=batchInfo.getString("mono");
                commPresenter.selectOksl(mono,6);

            }else if(key==6){
                num2=batchInfo.getString("oksl");

                oksl.setText(num2);
                String mono=batchInfo.getString("slkid");
                commPresenter.selectNgsl(mono,7);
            }else if(key==7){
                num3=batchInfo.getString("ngsl");

                ngsl.setText(num3);
                int number=Integer.parseInt(num);
                int number2=Integer.parseInt(num2);
                int number3=Integer.parseInt(num3);
                int number4=number-number2-number3;
                wqr.setText(number4+"");
//品质报修数量
            } else if(key==9){
                num5= batchInfo.getString("bxsl");
                if(TextUtils.isEmpty(num5)){
                    num5="0";
                }
                bxsl.setText(num5);
                String mono=batchInfo.getString("mono");
                commPresenter.selectOksl2(mono,10);

            }else if(key==10){
                num6=batchInfo.getString("oksl");

                oksl.setText(num6);
                String mono=batchInfo.getString("slkid");
                commPresenter.selectNgsl2(mono,11);

            }else if(key==11){
                num7=batchInfo.getString("ngsl");

                ngsl.setText(num7);
                int number=Integer.parseInt(num5);
                int number2=Integer.parseInt(num6);
                int number3=Integer.parseInt(num7);
                int number4=number-number2-number3;
                wqr.setText(number4+"");

            }


            else{
                //保存记录
                String mono = batchInfo.getString("mono");//生产记录主键
                //String remark = batchInfo.getString("remark");
                int qty=batchInfo.getInteger("qty");
                String prd_no = batchInfo.getString("prd_no");
                String code=batchInfo.getString("allcode");
                String prd_name=batchInfo.getString("prd_name");
//            String boks=batchInfo.getString("bok");
                String prd_mark=batchInfo.getString("prd_mark");
                String sbuid=zCnoInfo.getSbuid();
                String judgementResult=zCnoInfo.getBok();
                String judgementName=zCnoInfo.getBokName();
                String sort=zCnoInfo.getSort();
                MESPRecord record = new MESPRecord(code, mono, zcno,"");
                String op=edtOPC.getText().toString().toUpperCase();
                record.setOp(op);
                record.setOp_b(op);
                record.setOp_o(op);
                //record.setRemark(remark);
                record.setState1(CommCL.BATCH_STATUS_DONE);
                record.setQty(qty);
                record.setPrd_no(prd_no);
                record.setPrd_name(prd_name);
                record.setPrd_mark(prd_mark);
                record.setSlkid(mono);
                record.setPrtno(code);
                record.setSbuid(sbuid);
                //保存判定结果
                record.setBok(judgementResult);
                record.setBokName(judgementName);
                record.setSort(sort);
                record.getSmake();
                record.getMkdate();
                commPresenter.wxqrRecord(record,batchInfo);//存入记录表
            }

        } else {
            hideLoading();
            CommonUtils.canDo(GBKEY);
            CommonUtils.speak(error);
            showMessage(error);
            CommonUtils.textViewGetFocus(edtCode);

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
            //JSONObject batchInfo= (JSONObject) records;
            MESPRecord record=(MESPRecord) records;
            //添加 喷码code 的页面缓存
            //bcode.put(batchInfo.getString("allcode"), batchInfo.getString("allcode"));
            bcode.put(record.getPrtno(),record.getPrtno());
            // 添加到数据列表
            adapter.addRecord(CommonUtils.getJsonObjFromBean(record));
            //给 卡板计数赋值
            kbJishu--;
            if(kbJishu<0)
            {
                kbJishu= kbDpn-1;
            }
            textJishu.setText(""+kbJishu);

            //改变喷码子表的状态,通过对象定义
            JSONObject jsObj1=new JSONObject();
            jsObj1.put("allcode",record.getPrtno());
            jsObj1.put("mono",record.getSlkid());
            //要判断是OK还是NG
            String judgement=record.getBok();
            String sort=record.getSort();
            if(judgement.equals("0")){
                if(sort.equals("A")){
                    jsObj1.put("scstate","1");//OK
                }else if(sort.equals("B")){
                    jsObj1.put("qcstate","1");//OK
                }

            }else if(judgement.equals("1")){
                if(sort.equals("A")){
                    jsObj1.put("scstate","-1");//NG
                }else if(sort.equals("B")){
                    jsObj1.put("qcstate","-1");//NG
                }

            }else{
                CommonUtils.speak("判定结果不存在");
            }
            if(sort.equals("A")){
                jsObj1.put("scdate", DateUtil.getCurrDateTime(ICL.DF_YMDT));
                commPresenter.updateInfo(jsObj1,CommCL.CELL_ID_D5080WEB,2);
            }else if(sort.equals("B")){
                jsObj1.put("qcdate", DateUtil.getCurrDateTime(ICL.DF_YMDT));
                commPresenter.updateInfo(jsObj1,CommCL.CELL_ID_D5084WEB,2);
            }
            //报修数量
            String mono=record.getSlkid();

            if(sort.equals("A")){
                commPresenter.selectBxsl(mono,5);
            }else if(sort.equals("B")){
                commPresenter.selectBxsl(mono,9);
            }else{
                CommonUtils.speak("类别不存在");
            }




            hideLoading();
            CommonUtils.textViewGetFocus(edtCode);


        } else {
            hideLoading();
            CommonUtils.canDo(GBKEY);
            CommonUtils.speak(error);
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
                bcode.put(record.getSid1(), record.getSid1());
                adapter.addRecord(CommonUtils.getJsonObjFromBean(record));
            } else {
                dataList.set(key, CommonUtils.getJsonObjFromBean(record));
                adapter.notifyDataSetChanged();
            }
            CommonUtils.textViewGetFocus(edtCode);
            CommonUtils.canDo(GBKEY);


        } else {
            hideLoading();
            CommonUtils.canDo(GBKEY);
            showMessage(error);
            CommonUtils.textViewGetFocus(edtCode);
        }
    }

    @Override
    public void updateCallBack(boolean bok, Object record, String error, int key) {
        if(bok){

        }
    }

    @Override
    public void onRemoteFailed(String message) {

        hideLoading();
        CommonUtils.canDo(GBKEY);
        CommonUtils.showError(this, "onRemoteFailed="+message);

    }

}
