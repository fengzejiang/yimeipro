package com.yimeinew.activity.qc;

import android.app.AlertDialog;
import android.content.*;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
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
    EditText edtSid1;//内装批号
    @BindView(R.id.edt_sidn)
    EditText edtSidn;//仓库内箱批号
    @BindView(R.id.edt_sidw)
    EditText edtSidw;//仓库内箱批号
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outcheck);
        ButterKnife.bind(this);
        this.setTitle(Title);
        commPresenter = new CommOtherPresenter(this,SchedulerProvider.getInstance());
        initTableView();

        //测试数据初始化
        /*
        edtOP.setText("A0122");
        edtSidw.setText("PK19092700001W");
        edtSidn.setText("PK19092700001N");
        edtSid1.setText("MOB19090206001");
        */
        //setSid("OQC1909300001");
    }


    @OnEditorAction({R.id.edt_op,  R.id.edt_sid1,R.id.edt_sidw,R.id.edt_sidn})
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
            CommonUtils.textViewGetFocus(edtSidw);
            return false;
        }
        //外箱箱号不能为空
        String sidw=edtSidw.getText().toString().toUpperCase();
        if(TextUtils.isEmpty(sidw)){
            CommonUtils.textViewGetFocus(edtSidw);
            showMessage("请输入仓库外箱箱号");
            return true;
        }
        if(id==R.id.edt_sidw){
            showText();
            CommonUtils.textViewGetFocus(edtSidn);
            return false;
        }
        //内箱箱号不能为空
        String sidn=edtSidw.getText().toString().toUpperCase();
        if(TextUtils.isEmpty(text)){
            CommonUtils.textViewGetFocus(edtSidn);
            showMessage("请输入仓库内箱箱号");
            return true;
        }
        if(id==R.id.edt_sidn){
            CommonUtils.textViewGetFocus(edtSid1);
            return false;
        }
        String sid1 = edtSid1.getText().toString().toUpperCase();
        if (TextUtils.isEmpty(sid1)) {
            showMessage("请输入批次号");
            CommonUtils.textViewGetFocus(edtSid1);
            return true;
        }
        if (id == R.id.edt_sid1) {
            if (bindSid1.containsKey(sid1)) {
                showMessage("该批次号【" + sid1 + "】已经扫描过");
                CommonUtils.textViewGetFocus(edtSid1);
                return true;
            }
            if(!CommonUtils.isRepeat("OQC_saomao_sid1",sid1)) {
                showLoading();
                //获取内箱批次号
                commPresenter.getAssistInfo(CommCL.AID_OQC_BAT_INFO_HD,"~bat_no='"+sid1+"'",R.id.edt_sid1);
             }
        }
        return false;
    }

    //点击事件
    @OnClick({R.id.img_qrw,R.id.img_qrn,R.id.img_new,R.id.img_view})
    public void OnClick(View view){
        int id=view.getId();
        switch (id){
            case R.id.img_qrw:
                alertWindow(this,id,"外箱二维码",qrw);
                break;
            case R.id.img_qrn:
                alertWindow(this,id,"内箱二维码",qrn);
                break;
            case R.id.img_new:
                alertWindow1(this,"是否创建新的单子");
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
        commPresenter.getAssistInfo(CommCL.AID_OQC_INFO_HD,"~sid='"+getSid()+"'",R.id.table_view);//此处id不起任何作用，只是为了与其他id做区分，随便挑选一个。
        showLoading();
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
        ngText();
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
                case R.id.edt_sid1:
                    JSONObject rs = info.getJSONObject(0);
                    String rssidw=rs.getString("sidw").toUpperCase();
                    String rssidn=rs.getString("sidn").toUpperCase();
                    String sidw=edtSidw.getText().toString().toUpperCase();
                    String sidn=edtSidn.getText().toString().toUpperCase();
                    String bat_no=rs.getString("bat_no").toUpperCase();
                    int bok1=rs.getIntValue("bok1");
                    if(bok1==0){
                        showMessage(bat_no+"该批次已扫描！");
                        hideLoading();
                        ngText();
                        CommonUtils.textViewGetFocus(edtSid1);
                        break;
                    }
                    if(!TextUtils.equals(rssidw,sidw)){
                        hideLoading();
                        CommonUtils.textViewGetFocus(edtSidw);
                        ngText();
                        showMessage("仓库外箱箱号错误");
                    }
                    else if(!TextUtils.equals(rssidn,sidn)){
                        hideLoading();
                        CommonUtils.textViewGetFocus(edtSidn);
                        ngText();
                        showMessage("仓库内箱箱号错误");
                    }else {
                        if(!TextUtils.isEmpty(cus_no)&&!TextUtils.equals(cus_no,rs.getString("cus_no"))){
                            showMessage("当前客户是"+rs.getString("cus_no")+"已扫描的客户是"+cus_no+"不一致，请新增");
                            hideLoading();
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
                        rs.put("opc",edtOP.getText().toString().toUpperCase());
                        rs.put("state",0);
                        rs.put("qrw",qrw);
                        rs.put("qrn",qrn);
                        sid=getSid();
                        if(TextUtils.isEmpty(sid)){
                            //没有主表主键
                            commPresenter.saveData(CommCL.CELL_ID_Q00113A,rs,1);//保存主表
                        }else{
                            //有主表主键--直接保存子表
                            rs.put("sid",sid);
                            rs.put("indate",DateUtil.getCurrDateTime(ICL.DF_YMDT));
                            commPresenter.saveData(CommCL.CELL_ID_Q00113B,rs,0);
                        }
                        //信息加入缓存和列表里
                        //CommonUtils.textViewGetFocus(edtSidw);
                    }
                    break;
                case  R.id.table_view://用sid获取已经扫描的信息
                    clear();
                    if(info.size()>0){
                        JSONObject obj = info.getJSONObject(0);
                        cus_no=obj.getString("cus_no");
                    }
                    addRow(info);
                    hideLoading();
                    break;
            }
        }else {
            hideLoading();
            showMessage(error);
            switch (key){
                case R.id.edt_sid1:
                    CommonUtils.textViewGetFocus(edtSid1);
                    ngText();
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
                    hideLoading();
                    addRow(record);
                    if(getSidNum(record.getString("sid"))>=record.getInteger("num")){
                        CommonUtils.textViewGetFocus(edtSidw);
                        edtSidn.setText("");edtSid1.setText("");
                        qrw="";qrn="";
                    }else{
                        CommonUtils.textViewGetFocus(edtSid1);
                    }
                    okText();
                    break;
            }
        }else{
            hideLoading();
            CommonUtils.textViewGetFocus(edtSid1);
            showMessage(error);
            ngText();
        }
    }

    @Override
    public void updateDataBack(Boolean bok, JSONArray info, String error, int key) {

    }

    @Override
    public void changeRecordStateBack(Boolean bok, JSONArray info, String error, int key) {

    }


    /*OK   NG*/
    public void okText(){
        textOkNg.setText("OK");
        textOkNg.setBackgroundColor(Color.GREEN);
    }
    public void ngText(){
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
    //编带数量修改，弹框界面
    public void alertWindow(Context context,int vid,String title,String value){
        canGetMessage=false;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        LinearLayout layout=new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        /*
        TextView text=new TextView(context);
        text.setTextSize(18);
        text.setText("   ");
        layout.addView(text);
        */
        /*
        EditText qtyEid=new EditText(context);
        //qtyEid.setText(""+record.getQty());
        qtyEid.setInputType(InputType.TYPE_CLASS_TEXT);
        //qtyEid.setMinLines(3);
        qtyEid.setText(value);
        //qtyEid.setHeight(300);
        layout.addView(qtyEid);
        */
        MultiAutoCompleteTextView mact=new MultiAutoCompleteTextView(context);
        mact.setText(value);
        layout.addView(mact);

        builder.setView(layout);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                canGetMessage=true;
                String qr= mact.getText().toString();
                if(TextUtils.isEmpty(qr)){
                    Toast.makeText(context, "信息为空！" , Toast.LENGTH_SHORT).show();
                }else{
                    switch (vid){
                        case R.id.img_qrw:
                            qrw=qr;
                            break;
                        case R.id.img_qrn:
                            qrn=qr;
                            break;

                    }
                }

            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener()
        {

            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                canGetMessage=true;
                Toast.makeText(context, "取消" , Toast.LENGTH_SHORT).show();
            }
        });
        builder.show();
    }
    public void alertWindow1(Context context,String title){
        canGetMessage=false;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        LinearLayout layout=new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);

        TextView text=new TextView(context);
        text.setTextSize(18);
        text.setText("   ");
        layout.addView(text);
        builder.setView(layout);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                canGetMessage=true;
                sid="";
                spno="";
                edtSidw.setText("");
                edtSidn.setText("");
                edtSid1.setText("");
                setSid("");
                clear();
                Toast.makeText(context, "已新建" , Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener()
        {

            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                canGetMessage=true;
                Toast.makeText(context, "取消修改" , Toast.LENGTH_SHORT).show();
            }
        });
        builder.show();
    }
    public void alertWindow2(Context context,String title,ArrayList<String> array){
        canGetMessage=false;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        LinearLayout layout=new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        for(int i=0;i<array.size();i++) {
            TextView text = new TextView(context);
            text.setTextSize(18);
            text.setText("   "+array.get(i));
            layout.addView(text);
        }
        builder.setView(layout);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {

            }
        });

        builder.show();
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
}
