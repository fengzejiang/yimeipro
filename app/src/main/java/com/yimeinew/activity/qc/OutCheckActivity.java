package com.yimeinew.activity.qc;

import android.app.AlertDialog;
import android.content.*;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.*;
import butterknife.*;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.shade.com.alibaba.fastjson.JSONArray;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.shade.com.alibaba.fastjson.JSONObject;
import com.yimeinew.activity.R;
import com.yimeinew.activity.base.BaseActivity;
import com.yimeinew.activity.base.BaseApplication;
import com.yimeinew.activity.deviceproduction.commsub.CommGJActivity;
import com.yimeinew.adapter.tabledataadapter.BaseTableDataAdapter;
import com.yimeinew.data.MESPRecord;
import com.yimeinew.data.ZCInfo;
import com.yimeinew.entity.Pair;
import com.yimeinew.listener.OnAlertListener;
import com.yimeinew.listener.OnConfirmListener;
import com.yimeinew.modelInterface.CommBaseView;
import com.yimeinew.modelInterface.CommFastView;
import com.yimeinew.network.schedulers.SchedulerProvider;
import com.yimeinew.presenter.CommFastPresenter;
import com.yimeinew.presenter.CommOtherPresenter;
import com.yimeinew.tableui.TablePanelView;
import com.yimeinew.tableui.entity.HeaderRowInfo;
import com.yimeinew.utils.CommCL;
import com.yimeinew.utils.CommonUtils;
import com.yimeinew.utils.DateUtil;
import com.yimeinew.utils.ICL;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class OutCheckActivity extends BaseActivity implements CommBaseView {
    private final String TAG_NAME = OutCheckActivity.class.getSimpleName();


    //数据表格
    @BindView(R.id.table_view)
    TablePanelView tableView;
    @BindView(R.id.data_list_content)
    ListView dataListViewContent;
    List<JSONObject> dataList;
    BaseTableDataAdapter adapter;
    @BindView(R.id.edt_op)
    EditText edtOP;//作业员
    @BindView(R.id.edt_tray)
    EditText edtTray;//tray盘
    @BindView(R.id.edt_sid1)
    EditText edtSid1;//内装批号
    @BindView(R.id.edt_sidn)
    EditText edtSidn;//仓库内箱批号
    @BindView(R.id.edt_sidw)
    EditText edtSidw;//仓库内箱批号

    EditText edtQrn;//内箱二位码
    EditText edtQrw;//外箱二位码

    @BindView(R.id.text_okng)
    TextView textOkNg;//提示框
    @BindView(R.id.img_qrw)
    ImageView ImgQRw;//外箱二位码
    @BindView(R.id.img_qrn)
    ImageView ImgQRn;//内箱二位码
    @BindView(R.id.img_new)
    ImageView ImgNew;//新增单据
    @BindView(R.id.img_view)
    ImageView ImgView;//查看报表

    ArrayAdapter<Pair> zcAdapter;
    private List<Pair> zcAdapterData = new ArrayList<>();
    ArrayList<ZCInfo> zcInfoList = BaseApplication.zcList;
    private HashMap<String, String> bindSid1 = new HashMap<>();

    private String zcno="";//当前制程
    private ZCInfo zCnoInfo;
    private String GBKEY="chuhouqueren_qj";
    IntentFilter intentFilter;
    private CommOtherPresenter commPresenter;
    public static final String Title = "出货检验";

    private String sid="";//单据单号
    private String cus_no="";//客户代号
    private String spno="";//出库单号
    private String qrw="";//外箱二维码
    private String qrn="";//内箱二维码
    private JSONArray clist=new JSONArray();
    //弹出
    private AlertDialog qrnAlert;
    private AlertDialog qrwAlert;
    JSONObject info;
    JSONArray trayInfo=new JSONArray();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outcheck);
        ButterKnife.bind(this);
        this.setTitle(Title);
        commPresenter = new CommOtherPresenter(this,SchedulerProvider.getInstance());
        initTableView();

        //测试数据初始化
//
//        edtOP.setText("A0122");
//        edtSidw.setText("PK19092700001W");
//        edtSidn.setText("PK19092700001N");
//        edtSid1.setText("MOB19090206001");

        //setSid("OQC1909300001");
    }


    @OnEditorAction({R.id.edt_op, R.id.edt_tray, R.id.edt_sid1,R.id.edt_sidw,R.id.edt_sidn})
    public boolean OnEditorAction(EditText editText) {
        return onEditTextKeyDown(editText);
    }
    @Override
    public boolean onEditTextKeyDown(EditText editText) {
        int id = editText.getId();
        String text=editText.getText().toString().toUpperCase();
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
            CommonUtils.textViewGetFocus(edtTray);
            return false;
        }
        //扫描tray盘号
        String tray=edtTray.getText().toString().toUpperCase();
        if(TextUtils.isEmpty(tray)){
            showMessage("请输入Tray盘号");
            CommonUtils.textViewGetFocus(edtTray);
            return true;
        }
        if(id==R.id.edt_tray){
            showLoading();
            String cont="~m.tray='"+tray+"' or m.sid1='"+tray+"'";
            commPresenter.getAssistInfo(CommCL.AID_OQC_TRAY,cont,R.id.edt_tray);
            return false;
        }
        //扫描内装
        String sid1 = edtSid1.getText().toString().toUpperCase();
        if (TextUtils.isEmpty(sid1)) {
            showMessage("请输入内装批次号");
            CommonUtils.textViewGetFocus(edtSid1);
            return true;
        }
        //检验Tray盘是不是在内装里
        if(!checkTrayAndBatNo(tray,sid1)){
            return true;
        }
        if (id == R.id.edt_sid1) {
            if (bindSid1.containsKey(sid1)) {
                showMessage("该批次号【" + sid1 + "】已经扫描过");
                CommonUtils.textViewGetFocus(edtSid1);
                return true;
            }
            if(!CommonUtils.isRepeat("OQC_saomao_sid1",sid1)) {
                showText();
                showLoading();
                //获取内箱批次号
                commPresenter.getAssistInfo(CommCL.AID_OQC_BAT_INFO_HD,"~bat_no='"+sid1+"'",R.id.edt_sid1);
            }
            return false;
        }
        if(info==null||!TextUtils.equals(info.getString("bat_no"),sid1)){
            showMessage("手输内装批次号必须回车");return false;
        }
        if(id==edtQrn.getId()){
            checkQRN();
            return false;
        }
        //内箱箱号不能为空
        String sidn=edtSidn.getText().toString().toUpperCase();
        if(TextUtils.isEmpty(sidn)){
            CommonUtils.textViewGetFocus(edtSidn);
            showMessage("请输入仓库内箱箱号");
            return true;
        }
        if(id==R.id.edt_sidn){
            //判断内箱是否正确
            if(TextUtils.equals(sidn,info.getString("sidn"))){
                //判断是否必须内箱二位码校验
                if(!TextUtils.isEmpty(info.getString("qrn"))){
                    alertWindowN();
                    qrnAlert.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
                    CommonUtils.textViewGetFocus(edtQrn);
                }else{
                    CommonUtils.textViewGetFocus(edtSidw);
                }
            }else {
                CommonUtils.textViewGetFocus(edtSidn);
                showMessage("内箱箱号错误");

            }
            return false;
        }

        //判断是否一定要输入内箱二维码
        String infoqrn=info.getString("qrn");
        if(!TextUtils.isEmpty(infoqrn)&&!TextUtils.equals(qrn,infoqrn)){
            alertWindowN();
            qrnAlert.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
            showMessage("内箱二位码必填");
        }
        if(id==edtQrw.getId()){
            checkQRW();
            return false;
        }
        //外箱箱号不能为空
        String sidw=edtSidw.getText().toString();
        if(TextUtils.isEmpty(sidw)){
            CommonUtils.textViewGetFocus(edtSidw);
            showMessage("请输入仓库外箱箱号");
            return true;
        }
        if(id==R.id.edt_sidw){
            //判断内箱是否正确
            if(TextUtils.equals(sidw,info.getString("sidw"))){
                //判断是否必须外箱二位码校验
                if(!TextUtils.isEmpty(info.getString("qrw"))){
                    alertWindowW();
                    qrwAlert.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
                    CommonUtils.textViewGetFocus(edtQrw);
                }else{
                    //如果外箱二维码非必填直接保存
                    saveData();
                }
            }else {
                CommonUtils.textViewGetFocus(edtSidw);
                showMessage("外箱箱号错误");
            }

            return false;
        }

        return false;
    }

    //点击事件
    @OnClick({R.id.img_qrw,R.id.img_qrn,R.id.img_new,R.id.img_view})
    public void OnClick(View view){
        int id=view.getId();
        switch (id){
            case R.id.img_qrw:
                alertWindowW();
                break;
            case R.id.img_qrn:
                alertWindowN();
                break;
            case R.id.img_new:
                alertWindow1(this,"是否创建新的单据");
                break;
            case R.id.img_view:
                alertWindow2(this,"出货明细",tongji());
                break;
        }
    }
    //
    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG_NAME,"onResume");
        //通过sid获取数据填充列表
        if(!TextUtils.isEmpty(getSid())) {
            commPresenter.getAssistInfo(CommCL.AID_OQC_INFO_HD, "~sid='" + getSid() + "'", R.id.table_view);//此处id不起任何作用，只是为了与其他id做区分，随便挑选一个。
            showLoading();
        }
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
        List<HeaderRowInfo> header =getRowDataList();

        dataListViewContent.setChoiceMode(ListView.CHOICE_MODE_NONE);
        adapter = new BaseTableDataAdapter(this,tableView,dataListViewContent,dataList,header);
        adapter.setSwipeRefreshEnabled(false);
        adapter.setTitle("项次");
        adapter.setTitleHeight(90);
        adapter.setTitleWidth(90);
        tableView.setAdapter(adapter);
        edtQrn=newEditText(R.string.yimei_qrn);
        edtQrw=newEditText(R.string.yimei_qrw);

    }
    /***
     * 初始化表格头数据
     * @return 返回表格头
     */
    public List<HeaderRowInfo> getRowDataList(){
        List<HeaderRowInfo> rowList = new ArrayList<>();
        HeaderRowInfo sidColumn = new HeaderRowInfo("bat_no", "内装批号", 150);
        rowList.add(sidColumn);
        sidColumn = new HeaderRowInfo("prd_no", "货品代号", 150);
        rowList.add(sidColumn);
        sidColumn = new HeaderRowInfo("prd_name", "货品名称", 320);
        rowList.add(sidColumn);
        sidColumn = new HeaderRowInfo("prd_mark", "Bincode", 150);
        rowList.add(sidColumn);
        sidColumn = new HeaderRowInfo("qty", "数量", 150);
        rowList.add(sidColumn);
        sidColumn = new HeaderRowInfo("spno", "出库单", 150);
        rowList.add(sidColumn);
        sidColumn = new HeaderRowInfo("cus_no", "客户代号", 150);
        rowList.add(sidColumn);
        sidColumn = new HeaderRowInfo("num", "一箱数量", 150);
        rowList.add(sidColumn);
        return rowList;
    }


    @Override
    public void onRemoteFailed(String message) {
        hideLoading();
        CommonUtils.canDo(GBKEY);
        ngText("");
        CommonUtils.showError(this, "onRemoteFailed="+message);

    }

    @Override
    public void commCallBack(Boolean bok, JSONObject info, String error, int key) {

    }

    @Override
    public void commCallBack(Boolean bok, JSONObject info, String error, String key) {

    }

    @Override
    public void getAssistInfoBack(Boolean bok, JSONArray info, String error, int key) {
        if(bok) {
            switch (key) {
                case R.id.edt_tray:
                    this.trayInfo = info;
                    CommonUtils.textViewGetFocus(edtSid1);
                    hideLoading();
                    break;
                case R.id.edt_sid1:
                    this.info=info.getJSONObject(0);
                    if(this.info.getInteger("bok1")==0){
                        CommonUtils.textViewGetFocus(edtSid1);
                        hideLoading();
                        showMessage("该内装批次已扫描！");
                    }else {
                        CommonUtils.textViewGetFocus(edtSidn);
                        hideLoading();
                    }
                    break;
                case  R.id.table_view://用sid获取已经扫描的信息
                    clear();
                    if(info.size()>0){
                        JSONObject obj = info.getJSONObject(0);
                        cus_no=obj.getString("cus_no");
                        bindSid1.putAll(CommonUtils.JSONArrayToMap(info,"bat_no","sid"));
                        addRow(info);
                    }
                    hideLoading();
                    break;
            }
        }else {
            hideLoading();
            showMessage(error);
            switch (key){
                case R.id.edt_tray:
                    CommonUtils.textViewGetFocus(edtTray);
                    break;
                case R.id.edt_sid1:
                    CommonUtils.textViewGetFocus(edtSid1);
                    ngText("");
                    break;
                case R.id.table_view:
                    newClear();//单获取新失败自动新建
                    break;
            }
        }
    }

    @Override
    public void saveDataBack(Boolean bok, JSONArray info,JSONObject record, String error, int key) {

        if(bok) {
            switch (key) {
                case 1://主表保存完毕保存子表
                    sid = info.getJSONObject(0).getString("sid");
                    cus_no=info.getJSONObject(0).getString("cus_no");
                    setSid(sid);
                    record.put("sid", sid);
                    record.put("indate",DateUtil.getCurrDateTime(ICL.DF_YMDT));
                    commPresenter.saveData(CommCL.CELL_ID_Q00113B, record, 0);//0表示后面不做处理
                    break;
                case 0://插入记录完成添加列表
                    bindSid1.put(record.getString("bat_no"),record.getString("sid"));
                    this.info=null;
                    hideLoading();
                    addRow(record);
                    if(getSidNum(record.getString("sid"))>=record.getInteger("num")){
                        CommonUtils.textViewGetFocus(edtTray);
                        edtSidn.setText("");edtSid1.setText("");edtSidw.setText("");
                        qrw="";qrn="";this.info=null;
                    }else{
                        CommonUtils.textViewGetFocus(edtTray);
                        edtSidn.setText("");edtSid1.setText("");edtSidw.setText("");
                        qrw="";qrn="";this.info=null;
                    }
                    okText("");
                    break;
            }
        }else{
            hideLoading();
            CommonUtils.textViewGetFocus(edtSid1);
            showMessage(error);
            ngText("");
        }
    }

    @Override
    public void updateDataBack(Boolean bok, JSONArray info, String error, int key) {

    }

    @Override
    public void changeRecordStateBack(Boolean bok, JSONArray info, String error, int key) {

    }


    /*OK   NG*/
    public void okText(String msg){
        textOkNg.setText("OK");
        textOkNg.setBackgroundColor(Color.GREEN);
    }
    public void ngText(String msg){
        textOkNg.setText("NG");
        textOkNg.setBackgroundColor(Color.RED);
    }
    public void showText(){
        textOkNg.setText("判定结果");
        textOkNg.setBackgroundColor(Color.rgb(255,255,255));
    }
    @Override
    public void addRow(Object json) {

        if (json instanceof JSONObject) {
            JSONObject record = (JSONObject) json;
            clist.add(record);
            adapter.addRecord(record);
        }
        if (json instanceof JSONArray) {
            JSONArray arr = (JSONArray) json;
            for (int i = 0; i < arr.size(); i++) {
                JSONObject jsonV = arr.getJSONObject(i);
                //MESPRecord mespRecord = JSON.parseObject(jsonV.toJSONString(), MESPRecord.class);
                clist.add(jsonV);
                adapter.addRecord(jsonV);//CommonUtils.getJsonObjFromBean(mespRecord)
            }
        }
    }
    @Override
    public void clear() {
        cus_no="";
        clist.clear();
        adapter.clear();
        this.dataList.clear();
    }
    //二维码 弹框界面
    public void alertWindowN(){
        if(info==null||info.size()<=0){
            showMessage("请先扫描内装批号");
            return;
        }
        if(qrnAlert==null) {
            edtQrn.setOnEditorActionListener(new TextView.OnEditorActionListener() {//设置监听器
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    checkQRN();
                    return true;
                }
            });
            qrnAlert = CommonUtils.confirm(this, "内箱二维码", "", edtQrn, new OnConfirmListener() {
                @Override
                public void OnConfirm(DialogInterface dialog) {
                    checkQRN();
                    isAlert=false;
                }

                @Override
                public void OnCancel(DialogInterface dialog) {
                    isAlert=false;
                }
            });
        }else{
            qrnAlert.show();
        }
        alertView=edtQrn;
        isAlert=true;
    }
    //外箱二维码
    public void alertWindowW() {
        if(info==null||info.size()<=0){
            showMessage("请先扫描内装批号");
            return;
        }
        if(qrwAlert==null) {
            edtQrw.setOnEditorActionListener(new TextView.OnEditorActionListener() {//设置监听器
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    checkQRW();
                    return false;
                }
            });
            qrwAlert = CommonUtils.confirm(this, "外箱二维码", "", edtQrw, new OnConfirmListener() {
                @Override
                public void OnConfirm(DialogInterface dialog) {
                    checkQRW();
                    isAlert=false;
                }
                @Override
                public void OnCancel(DialogInterface dialog) {
                    isAlert=false;
                }
            });
        }else{
            qrwAlert.show();
        }
        alertView=edtQrw;
        isAlert=true;
    }
    //新建按钮
    public void alertWindow1(Context context,String title){
        canGetMessage=false;
        String msg="检验单号："+sid;
        CommonUtils.confirm(context, title, msg, null, new OnConfirmListener() {
            @Override
            public void OnConfirm(DialogInterface dialog) {
                canGetMessage=true;
                showSuccess("已新建");
                newClear();
            }
            @Override
            public void OnCancel(DialogInterface dialog) {
                canGetMessage=true;
                showMessage("取消新建");
            }
        });
    }
    //查看统计
    public void alertWindow2(Context context,String title,ArrayList<String> array){
        canGetMessage=false;
        LinearLayout layout=new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        for(int i=0;i<array.size();i++) {
            TextView text = new TextView(context);
            text.setTextSize(18);
            text.setText("   "+array.get(i));
            layout.addView(text);
        }
        CommonUtils.alert(context, title, "", layout, new OnAlertListener() {
            @Override
            public void OnConfirm(DialogInterface dialog) {
                canGetMessage=true;
            }
        });
    }
    public String getSid() {
        sid=CommCL.sharedPreferences.getString(TAG_NAME+"sid","");
        return sid;
    }
    public void setSid(String sid) {
        CommCL.sharedPreferences.edit().putString(TAG_NAME+"sid",sid).commit();
        this.sid = sid;
    }
    public int getSidNum(String sid){
        int num=0;
        for(int i=0;i<clist.size();i++){
            JSONObject obj=clist.getJSONObject(i);
            if(TextUtils.equals(sid,obj.getString("sid"))){
                num++;
            }
        }
        return num;
    }
    public ArrayList<String> tongji() {
        ArrayList<String> rs = new ArrayList<>();
        HashMap<String, Integer> temp = new HashMap<>();
        int all=0;
        for (int i = 0; i < clist.size(); i++) {
            JSONObject obj = clist.getJSONObject(i);
            String key=obj.getString("prd_mark");
            int nt=0;
            if(temp.containsKey(key)) {
                nt= temp.get(key);
            }
            temp.put(key, nt + obj.getInteger("qty"));
            all+=obj.getInteger("qty");
        }
        rs.add("总 数 量 :     "+all);
        for(String key:temp.keySet()){
            rs.add(key+":     "+temp.get(key));
        }
        return rs;
    }
    //新建
    public void newClear(){
        sid="";
        this.info=null;
        spno="";
        edtSidw.setText("");
        edtSidn.setText("");
        edtSid1.setText("");
        CommonUtils.textViewGetFocus(edtTray);
        setSid("");
        clear();
        Toast.makeText(this, "已新建" , Toast.LENGTH_SHORT).show();
    }
    //创建输入框
    public EditText newEditText(int hint){
        EditText text=new EditText(this);
        text.setId(View.generateViewId());
        text.setHint(hint);
        text.setTag(getResources().getString(hint));
        text.setImeOptions(EditorInfo.IME_ACTION_DONE);
        text.setEnabled(true);
        text.setTextSize(16);
        text.setTextColor(getResources().getColor(R.color.text_color));
        text.setInputType(EditorInfo.TYPE_TEXT_FLAG_AUTO_COMPLETE);
        return text;
    }
    //内箱二位码扫描校验
    public boolean checkQRN(){
        String tqrn = edtQrn.getText().toString().toUpperCase();
        String infoqrn=info.getString("qrn");
        if(TextUtils.isEmpty(infoqrn)||TextUtils.equals(infoqrn,tqrn)){
            qrnAlert.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(true);
            isAlert=false;
            qrn=tqrn;
            qrnAlert.dismiss();
            if(!TextUtils.isEmpty(infoqrn)) {
                CommonUtils.textViewGetFocus(edtSidw);
            }
            return true;
        }else {
            edtQrn.setText("");
            showMessage("内箱二维码输入错误");
            return false;
        }
    }
    //外箱二位码扫描校验
    public boolean checkQRW(){
        String tqrw = edtQrw.getText().toString().toUpperCase();
        String infoqrw=info.getString("qrw");
        if(TextUtils.isEmpty(infoqrw)||TextUtils.equals(infoqrw,tqrw)){
            qrwAlert.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(true);
            isAlert=false;
            qrw=tqrw;
            qrwAlert.dismiss();
            //保存数据
            if(!TextUtils.isEmpty(infoqrw)) {
                saveData();
            }
            return true;
        }else {
            edtQrw.setText("");
            showMessage("外箱二维码输入错误");
            return false;
        }
    }
    //保存前的校验
    public boolean checkSave(String op,String sid1,String sidn,String sidw,String tray){
        if(TextUtils.isEmpty(op)){
            showMessage("检验员不能为空");
            return false;
        }
        if(trayInfo.size()<=0){
            showMessage("手输tray盘号必须回车！");
            return false;
        }
        String trayStr = this.trayInfo.getJSONObject(0).getString("tray");
        if(!TextUtils.equals(trayStr,tray)){
            showMessage("手输tray盘号必须回车！");
            return false;
        }
        if(info==null||!TextUtils.equals(info.getString("bat_no"),sid1)){
            showMessage("手输内装批次号必须回车");
            return false;
        }
        if(!TextUtils.equals(info.getString("sidn"),sidn)){
            showMessage("内箱箱号错误！");
            return false;
        }
        if(!TextUtils.equals(info.getString("sidw"),sidw)){
            showMessage("外箱箱号错误！");
            return false;
        }
        String infoqrn=info.getString("qrn");
        if(!TextUtils.isEmpty(infoqrn)&&!TextUtils.equals(infoqrn,qrn)){
            showMessage("内箱二维码错误！");
            return false;
        }
        String infoqrw=info.getString("qrw");
        if(!TextUtils.isEmpty(infoqrw)&&!TextUtils.equals(infoqrw,qrw)){
            showMessage("内箱二维码错误！");
            return false;
        }

        return true;
    }
    public void saveData(){
        String opc=edtOP.getText().toString().toUpperCase();
        String sid1=edtSid1.getText().toString().toUpperCase();
        String sidn=edtSidn.getText().toString().toUpperCase();
        String sidw=edtSidw.getText().toString().toUpperCase();
        String tray=edtTray.getText().toString().toUpperCase();
        if(!checkSave(opc,sid1,sidn,sidw,tray)){
            return;
        }
        JSONObject rs=info;
        if(!TextUtils.isEmpty(cus_no)&&!TextUtils.equals(cus_no,rs.getString("cus_no"))){
            hideLoading();
            showMessage("当前客户是"+rs.getString("cus_no")+"已扫描的客户是"+cus_no+"不一致，请新增");
            CommonUtils.textViewGetFocus(edtSidw);
            return;
        }
        //信息插入动作
        //需要的公共信息
        MESPRecord record=new MESPRecord();
        rs.put("mkdate",record.getMkdate());
        rs.put("sbuid",record.getSbuid());
        rs.put("smake",record.getSmake());
        rs.put("bok",0);
        rs.put("dcid",record.getDcid());
        rs.put("sorg",record.getSorg());
        rs.put("opc",opc);
        rs.put("state",0);
        rs.put("qrw",qrw);
        rs.put("qrn",qrn);
        rs.put("tray",tray);
        sid=getSid();
        if(TextUtils.isEmpty(sid)){
            showLoading();
            //没有主表主键
            commPresenter.saveData(CommCL.CELL_ID_Q00113A,rs,1);//保存主表
        }else{
            showLoading();
            //有主表主键--直接保存子表
            rs.put("sid",sid);
            rs.put("indate",DateUtil.getCurrDateTime(ICL.DF_YMDT));
            commPresenter.saveData(CommCL.CELL_ID_Q00113B,rs,0);
        }
    }

    public boolean checkTrayAndBatNo(String tray,String batno){
        boolean trayb=false;
        boolean batnob=false;
        for(int i=0;i<trayInfo.size();i++){
            JSONObject obj=trayInfo.getJSONObject(i);
            if(TextUtils.equals(obj.getString("tray"),tray)){
                trayb=true;
            }
            if(TextUtils.equals(obj.getString("cus_pn"),batno)){
                batnob=true;
            }
        }
        if(!trayb){
            showMessage("手输Tray盘号必须回车");
            return false;
        }
        if(!batnob){
            showMessage("该Tray不在该包装批号里！");
            CommonUtils.textViewGetFocus(edtSid1);
            return false;
        }
        return true;
    }
}
