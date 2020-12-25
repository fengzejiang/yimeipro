package com.yimeinew.activity.deviceproduction.commsub;

import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnEditorAction;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.shade.com.alibaba.fastjson.JSONArray;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.shade.com.alibaba.fastjson.JSONObject;
import com.yimeinew.activity.R;
import com.yimeinew.activity.SystemSetActivity;
import com.yimeinew.activity.base.BaseActivity;
import com.yimeinew.activity.base.BaseApplication;
import com.yimeinew.adapter.SpinnerAdapterImpl;
import com.yimeinew.adapter.tabledataadapter.BaseTableDataAdapter;
import com.yimeinew.data.CheckReason;
import com.yimeinew.data.MESPRecord;
import com.yimeinew.data.ZCInfo;
import com.yimeinew.entity.Pair;
import com.yimeinew.listener.OnConfirmListener;
import com.yimeinew.modelInterface.CommBaseView;
import com.yimeinew.network.schedulers.SchedulerProvider;
import com.yimeinew.presenter.CommBasePresenter;
import com.yimeinew.tableui.TablePanelView;
import com.yimeinew.tableui.entity.HeaderRowInfo;
import com.yimeinew.utils.*;
import com.yimeinew.view.AuxText;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BadRepairActivity extends BaseActivity implements CommBaseView {
    private final String TAG_NAME = CommGJActivity.class.getSimpleName();
    //AB料分页
    @BindView(R.id.tabHost)
    TabHost tabHost;
    //数据表格
    @BindView(R.id.table_view_a)
    TablePanelView tableView;
    @BindView(R.id.data_list_content_a)
    ListView dataListViewContent;
    List<JSONObject> dataList;
    BaseTableDataAdapter adapter;
    @BindView(R.id.table_view_b)
    TablePanelView tableViewb;
    @BindView(R.id.data_list_content_b)
    ListView dataListViewContentb;
    List<JSONObject> dataListb;
    BaseTableDataAdapter adapterb;

    @BindView(R.id.sp_zcno)
    Spinner zcnoSpinner;//制程
    @BindView(R.id.edt_op)
    AuxText edtOP;//检验员
    @BindView(R.id.sp_reason)
    //AuxText spReanson;
    AuxText spReanson;//不良原因(类别)
    @BindView(R.id.edt_code)
    EditText edtCode;//喷码
    @BindView(R.id.edt_position)
    AuxText edtPosition;//位置
    @BindView(R.id.text_jishu)
    EditText textJishu;//计数
    @BindView(R.id.text_jishub)
    EditText textJishub;//计数
    private String lotnoA="";//当前表主键
    private String KEY_LOT_NO="KEY_LOT_NO_A"+TAG_NAME;//主键缓存，下次进入页面重新加载
    private String currMONOA = "";//当前工单号
    private String lotnoB="";//当前表主键
    private String KEY_LOT_NOB="KEY_LOT_NO_B"+TAG_NAME;//主键缓存，下次进入页面重新加载
    private String currMONOB = "";//当前工单号
    private int kbJishu=0;
    ArrayAdapter<CheckReason> causeAdapter;
    private List<CheckReason> causeAdapterData = new ArrayList<>();


    ArrayAdapter<Pair> zcAdapter;
    private List<Pair> zcAdapterData = new ArrayList<>();
    ArrayList<ZCInfo> zcInfoList = BaseApplication.zcList;
    private JSONArray jar;
    private HashMap<String, String> bcode = new HashMap<>();
    private ZCInfo zcInfo;
    private String name; //不合格原因
    private String spc_no;//不良原因代号
    private int wxngqty=0 ;
    private int wxngqtyb=0 ;
    private int cid=0;//获取序号。
    private int cidb=0;//获取序号。
    private String zcno="";//当前制程
    private ZCInfo zCnoInfo;
    private String GBKEY="kabanguozhan_mz";
    IntentFilter intentFilter;
    private CommBasePresenter commPresenter;
    public static final String Title = "不良品送修";
    String tableName="mes_precord";
    String tableKey="sid";
    String insobj="D5064";
    JSONObject savebs;//保存表身缓存数据
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bad_repair);
        ButterKnife.bind(this);
        zCnoInfo = (ZCInfo) getIntent().getSerializableExtra(CommCL.COMM_ZC_INFO_FLD);
        this.setTitle(Title+"->"+zCnoInfo.getName());
        commPresenter=new CommBasePresenter(this, SchedulerProvider.getInstance());
        edtOP.setOPAux();
        showLoading();
        //查询不良送修制程
        commPresenter.getAssistInfo(CommCL.AID_MZWX_ZCNO,"",1);
        //查询送修位置--数据在常量定义
        commPresenter.getAssistInfo(CommCL.AID_WEB_INSSYSCL,"~sname='WEB_WXWZ'",2);
        //不良原因
        commPresenter.getAssistInfo(CommCL.AID_BLYY,"",3);
    }

    @OnEditorAction({R.id.edt_op, R.id.edt_code,R.id.sp_reason,R.id.edt_position})
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
            CommonUtils.textViewGetFocus(spReanson);
            return false;
        }
        String reason = spReanson.getText().toString().toUpperCase();
        //不良原因
        if (TextUtils.isEmpty(reason)) {
            //showMessage("请输入喷码");
            CommonUtils.speak("请输入类别");
            CommonUtils.textViewGetFocus(spReanson);
            return true;
        }
        if(!spReanson.checkUp(reason)){
            CommonUtils.speak("输入类别代号不存在");
            CommonUtils.textViewGetFocus(spReanson);
            return true;
        }
        if(id==R.id.sp_reason){
            CommonUtils.textViewGetFocus(edtCode);
            return false;
        }
        String code = edtCode.getText().toString().toUpperCase();
        if (TextUtils.isEmpty(code)) {
            //showMessage("请输入喷码");
            CommonUtils.speak("请输入喷码");
            CommonUtils.textViewGetFocus(edtCode);
            return true;
        }
        if (id == R.id.edt_code) {
            if (bcode.containsKey(code)) {
                //showMessage("该喷码【" + code + "】已getQuickLotBack经扫描过");
                CommonUtils.speak("已扫描");
                CommonUtils.textViewGetFocus(edtCode);
                return true;
            }
            CommonUtils.textViewGetFocus(edtPosition);
            return true;
        }

        //位置
        if(id==R.id.edt_position){
            String position = edtPosition.getText().toString().toUpperCase();
            if (TextUtils.isEmpty(position)) {
                //showMessage("请输入喷码");
                CommonUtils.speak("请输入维修位置");
                CommonUtils.textViewGetFocus(edtPosition);
                return true;
            }
            if(!edtPosition.checkUp(position)){
                CommonUtils.speak("输入维修位置不存在");
                CommonUtils.textViewGetFocus(edtPosition);
                return true;
            }
            //第一次查询
            if(!CommonUtils.isRepeat(code,code,8000)) {
                showLoading();
                String cont="~allcode='"+code+"'";
                commPresenter.getAssistInfo(CommCL.AID_BLPSX,cont, 11);
            }
            return true;
        }
        return false;
    }
    private void getZcno(Boolean bok, JSONArray info, String error, int key){
        if(bok){
            ArrayList<Pair> pairList=new ArrayList<>();
            for(int i=0;i<info.size();i++){
                JSONObject obj=info.getJSONObject(i);
                Pair pair=new Pair(obj.getString("name"),obj.getString("id"));
                pairList.add(pair);
            }
            initZcnoSpinner(pairList);
        }else {
            showMessage(error);
        }
    }
    private void getWhere(Boolean bok, JSONArray info, String error, int key){
        if(bok){
            if(info!=null&&info.size()>0){
                String sbds=info.getJSONObject(0).getString("sbds");
                HashMap hm = ToolUtils.parseConstant(sbds);//常量格式转换
                edtPosition.setAux(hm);
            }
        }else {
            showMessage(error);
        }
    }
    private void getReason(Boolean bok, JSONArray info, String error, int key){
        if(bok) {
            jar = info;
            spReanson.setAuxArray(jar, "spc_no", "name");
            initTableView();//初始化表格
            lotnoA=CommCL.sharedPreferences.getString(KEY_LOT_NO,"");
            //lotnoA="SQA20113000001";
            lotnoB=CommCL.sharedPreferences.getString(KEY_LOT_NOB,"");
            //lotnoB="SQA20100700010";
            if(!TextUtils.isEmpty(lotnoA)) {
                //获取这个一整包的信息
                String cont="~sid='"+lotnoA+"'";
                commPresenter.getAssistInfo(CommCL.AID_BLPSX2, cont,88);
            }else{
                hideLoading();
            }
            if(!TextUtils.isEmpty(lotnoB)) {
                //获取这个一整包的信息
                String cont="~sid='"+lotnoB+"'";
                commPresenter.getAssistInfo(CommCL.AID_BLPSX2, cont,89);
            }
        }else {
            showMessage(error);
        }
    }
    private void getPenMa(Boolean bok, JSONArray info, String error, int key){
        if(bok){
            //判断
            JSONObject batchInfo = info.getJSONObject(0);
            String state=batchInfo.getString("state");
            int wxnum=batchInfo.getInteger("wxnum");
            String mono = batchInfo.getString("sid");//工单号
            batchInfo.put("mono",mono);
            savebs=batchInfo;
            if(!TextUtils.isEmpty(currMONOA)&&!TextUtils.equals(mono,currMONOA)&&key!=91){
                if(TextUtils.isEmpty(currMONOB)){
                    //不等A，且B工单为空时，判断是否AB料再回调
                    String cont="~asid='"+currMONOA+"' and bsid='"+mono+"'";
                    commPresenter.getAssistInfo(CommCL.AB_MO_CHECK,cont,91);
                    return;
                }else if(!TextUtils.equals(mono,currMONOB)){
                    //不等A也不等B
                    showPMError("工单不一致请新建");
                    return;
                }
            }
            if(wxnum>0){
                showPMError("已送修");
                return;
            }
            if(CommCL.BATCH_STATUS_READY.equals(state)){
                showPMError("未过卡板");
                return;
            }else if(CommCL.BATCH_STATUS_DONE.equals(state)) {
                showPMError("已包装不能送修");
                return;
            }else if(!CommCL.BATCH_STATUS_WORKING.equals(state)){
                showPMError("状态为"+state+"不能送修");
                return;
            }
            //保存
            if(TextUtils.isEmpty(lotnoA)&&TextUtils.isEmpty(currMONOA)) {
                //保存A表头
                saveHead(batchInfo,21);
            }else if(TextUtils.equals(mono,currMONOA)){
                //保存A子表
                cid++;
                saveBody(batchInfo,lotnoA,cid,22);
            }else if(TextUtils.isEmpty(lotnoB)&&TextUtils.isEmpty(currMONOB)){
                //保存B表头
                saveHead(batchInfo,31);
            }else if(TextUtils.equals(mono,currMONOB)){
                //保存B子表
                cidb++;
                saveBody(batchInfo,lotnoB,cidb,32);
            }
        }else {
            hideLoading();
            showMessage(error);
            CommonUtils.textViewGetFocus(edtCode);
        }
    }
    private void getAllInfo(Boolean bok, JSONArray info, String error, int key){
        if(bok){
            setScanAQty(info.size());
            currMONOA=info.getJSONObject(0).getString("slkid");
            //清理列表
            adapter.clear();
            cid=info.size();
            //插入表格
            for(int i=0;i<info.size();i++) {
                JSONObject temp = info.getJSONObject(i);
                //添加缓存去重复
                String penma=temp.getString("snrem");
                bcode.put(penma,penma);
                adapter.addRecord(CommonUtils.getJsonObjFromBean(temp));
            }
            hideLoading();
        }else{
            //mei查询到信息就自动新建
            lotnoA="";
            CommCL.sharedPreferences.edit().putString(KEY_LOT_NO,"").commit();
            edtCode.setText("");
            textJishu.setText("0");
            wxngqty=0;
            cid=0;
            currMONOA="";
            adapter.clear();
            hideLoading();
            showMessage(error);
        }
    }
    private void getAllInfoB(Boolean bok, JSONArray info, String error, int key){
        if(bok){
            setScanBQty(info.size());
            currMONOB=info.getJSONObject(0).getString("slkid");
            //清理列表
            adapterb.clear();
            cidb=info.size();
            //插入表格
            for(int i=0;i<info.size();i++) {
                JSONObject temp = info.getJSONObject(i);
                //添加缓存去重复
                String penma=temp.getString("snrem");
                bcode.put(penma,penma);
                adapterb.addRecord(CommonUtils.getJsonObjFromBean(temp));
            }
            hideLoading();
        }else{
            //mei查询到信息就自动新建
            lotnoB="";
            CommCL.sharedPreferences.edit().putString(KEY_LOT_NOB,"").commit();
            edtCode.setText("");
            textJishub.setText("0");
            wxngqtyb=0;
            cidb=0;
            currMONOB="";
            adapterb.clear();
            hideLoading();
            showMessage(error);
        }
    }
    private void getMOAB(Boolean bok, JSONArray info, String error, int key){
        if(bok){
            int checkAB=info.getJSONObject(0).getIntValue("bok");
            if(checkAB==1){
                //回调
                JSONArray objarr=new JSONArray();
                objarr.add(savebs);
                getPenMa(true,objarr,"",key);
            }else{
                savebs.clear();
                showPMError("不是AB料");
            }
        }else {
            savebs.clear();
            showPMError("不是AB料");
        }
    }
    @Override
    public void getAssistInfoBack(Boolean bok, JSONArray info, String error, int key) {
        switch (key){
            case 1:getZcno(bok,info,error,key);break;//不良品送修制程
            case 2:getWhere(bok,info,error,key);break;//不良品送修位置
            case 3:getReason(bok,info,error,key);break;//不良品送修原因
            case 11:getPenMa(bok,info,error,key);break;//获取喷码信息
            case 88:getAllInfo(bok,info,error,key);break;//获取完整记录A
            case 89:getAllInfoB(bok,info,error,key);break;//获取完整记录B
            case 91:getMOAB(bok,info,error,key);break;
        }
    }
    private void saveHeadDataBack(Boolean bok, JSONArray info, JSONObject record, String error, int key){
        if(bok){
            //保存表头完成，缓存主键
            JSONObject jsobj=info.getJSONObject(0);
            switch (key){
                case 21:
                    lotnoA=jsobj.getString("sid");
                    CommCL.sharedPreferences.edit().putString(KEY_LOT_NO,lotnoA).commit();
                    currMONOA=savebs.getString("sid");//工单号
                    cid++;
                    saveBody(savebs,lotnoA,cid,22);
                    break;
                case 31:
                    lotnoB=jsobj.getString("sid");
                    CommCL.sharedPreferences.edit().putString(KEY_LOT_NOB,lotnoB).commit();
                    currMONOB=savebs.getString("sid");//工单号
                    cidb++;
                    saveBody(savebs,lotnoB,cidb,32);
                    break;
            }
        }else {
            showPMError(error);
        }

    }
    private void saveInfoDataBack(Boolean bok, JSONArray info, JSONObject record, String error, int key){
        if(bok){
            //添加喷码缓存
            bcode.put(record.getString("snrem"),record.getString("snrem"));
            // 添加到数据列表
            record.put("slkid",savebs.getString("mono"));
            switch (key){
                case 22:
                    adapter.addRecord(record);
                    wxngqty++;
                    textJishu.setText(wxngqty + "");
                    update(record,lotnoA,wxngqty);
                    break;
                case 32:
                    adapterb.addRecord(record);
                    wxngqtyb++;
                    textJishub.setText(wxngqtyb + "");
                    update(record,lotnoB,wxngqtyb);
                    break;
            }
            hideLoading();
            CommonUtils.textViewGetFocus(edtCode);
        }else {
            showPMError(error);
        }
    }
    @Override
    public void saveDataBack(Boolean bok, JSONArray info, JSONObject record, String error, int key) {
        switch (key){
            case 21:
            case 31:saveHeadDataBack(bok,info,record,error,key);break;//保存表头
            case 22:
            case 32:saveInfoDataBack(bok,info,record,error,key);break;//保存表身
        }
    }

    @Override
    public void updateDataBack(Boolean bok, JSONArray info, String error, int key) {

    }

    @Override
    public void changeRecordStateBack(Boolean bok, JSONArray info, String error, int key) {

    }

    @Override
    public void commCallBack(Boolean bok, JSONObject info, String error, int key) {
        hideLoading();
        if(bok){
            showSuccess("打印成功");
        }else{
            showMessage(error);
        }
    }

    @Override
    public void commCallBack(Boolean bok, JSONObject info, String error, String key) {

    }

    private void saveHead(JSONObject batchInfo,int key){
        String mono = batchInfo.getString("sid");//工单号
        //保存
        String sid1=batchInfo.getString("sid1");//批次号
        MESPRecord record = new MESPRecord(sid1, mono, zcno,"");
        //保存记录表头
        //String remark = batchInfo.getString("remark");
        int qty=batchInfo.getInteger("qty");
        String prd_no = batchInfo.getString("prd_no");
        String prd_name=batchInfo.getString("prd_name");
//            String boks=batchInfo.getString("bok");
        String prd_mark=batchInfo.getString("prd_mark");
        String sbuid=batchInfo.getString("sbuid");

        String op=edtOP.getText().toString().toUpperCase();
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
        //record.setPrtno(code);
        record.setSbuid(sbuid);
        //制单人，制单时间
        record.getSmake();
        record.setHpdate(record.getMkdate());
        record.setZcno(zcno);
        record.setPrintid(batchInfo.getString("printid"));
        //commPresenter.blpsxRecord(record,batchInfo);//存入记录表
        commPresenter.saveData(CommCL.CELL_ID_D5064,CommonUtils.getJsonObjFromBean(record),key);
    }
    private void saveBody(JSONObject batchInfo,String lotno,int thecid,int key){
        String mono = batchInfo.getString("sid");//工单号
        //保存
        String sid1=batchInfo.getString("sid1");//批次号
        MESPRecord record = new MESPRecord(sid1, mono, zcno,"");
        //保存表身
        String snrem=batchInfo.getString("allcode");
        spc_no=spReanson.getText().toString();//不良品原因
        String position =edtPosition.getText().toString();//维修位置
        int ngqty=batchInfo.getInteger("ngqty");//不合格数量
        batchInfo.put("mono",batchInfo.getString("sid"));
        batchInfo.put("sid",lotno);
        batchInfo.put("snrem",snrem);
        batchInfo.put("spc_no",spc_no);
        batchInfo.put("position",position);
        batchInfo.put("ngqty",ngqty);
        batchInfo.put("cl_no",1);
        batchInfo.put("mkdate",record.getMkdate());
        batchInfo.put("cid",thecid);
        //commPresenter.savePackInfo(batchInfo,batchInfo);
        commPresenter.saveData(CommCL.CELL_ID_D5064A,batchInfo,key);
    }
    private void update(JSONObject record,String lotno,int ngqty){
        //更新表头
        JSONObject jsObj=new JSONObject();
        jsObj.put("sid",lotno);
        jsObj.put("wxngqty",ngqty);
        jsObj.put("ngqty",ngqty);
        commPresenter.updateData(CommCL.CELL_ID_D5064WEEB,jsObj,1);

        //改变喷码子表的状态,通过对象定义
        JSONObject jsObj1=new JSONObject();
        jsObj1.put("allcode",record.getString("snrem"));
        jsObj1.put("mono",record.getString("mono"));
        if(!record.containsKey("wxnum")){
            jsObj1.put("wxnum",1);
        }else {
            jsObj1.put("wxnum",record.getInteger("wxnum")+1);
        }
        commPresenter.updateData(CommCL.CELL_ID_D5010WEBC,jsObj1,2);
    }
    @OnClick(R.id.img_new)
    public void onClick(View view){
        int id=view.getId();
        switch (id){
            case R.id.img_new:
                alertWindow1(this,"是否新建不良送修单");
                break;
        }
    }
    /*---------------alter 弹框----------------*/
    public void alertWindow1(Context context, String title){
        LinearLayout layout=new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        if(!TextUtils.isEmpty(lotnoA)){
            TextView tv1=new TextView(context);
            tv1.setText(lotnoA);
            tv1.setTextSize(16);
            tv1.setGravity(Gravity.CENTER);
            layout.addView(tv1);
            LinearLayout lay1=new LinearLayout(context);
            lay1.setOrientation(LinearLayout.HORIZONTAL);
            lay1.setGravity(Gravity.CENTER);
            Button bn1=new Button(context);
            bn1.setText("打印标签A");
            bn1.setGravity(Gravity.CENTER);
            bn1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    print1();
                }
            });
            Button bn2=new Button(context);
            bn2.setText("复制标签A");
            bn2.setGravity(Gravity.CENTER);
            bn2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    print2();
                }
            });
            lay1.addView(bn1);
            lay1.addView(bn2);

            layout.addView(lay1);
        }
        if(!TextUtils.isEmpty(lotnoB)){
            TextView tv2=new TextView(context);
            tv2.setText(lotnoB);
            tv2.setTextSize(16);
            tv2.setGravity(Gravity.CENTER);
            layout.addView(tv2);
            LinearLayout lay2=new LinearLayout(context);
            lay2.setOrientation(LinearLayout.HORIZONTAL);
            lay2.setGravity(Gravity.CENTER);
            Button bn11=new Button(context);
            bn11.setText("打印标签B");
            bn11.setGravity(Gravity.CENTER);
            bn11.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    print3();
                }
            });
            Button bn22=new Button(context);
            bn22.setText("复制标签B");
            bn22.setGravity(Gravity.CENTER);
            bn22.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    print4();
                }
            });
            lay2.addView(bn11);
            lay2.addView(bn22);
            layout.addView(lay2);
        }
        CommonUtils.confirm(context, title, "", layout, new OnConfirmListener() {
            @Override
            public void OnConfirm(DialogInterface dialog) {
                lotnoA="";
                CommCL.sharedPreferences.edit().putString(KEY_LOT_NO,"").commit();
                edtCode.setText("");
                textJishu.setText("0");
                wxngqty=0;
                cid=0;
                currMONOA="";
                adapter.clear();
                lotnoB="";
                CommCL.sharedPreferences.edit().putString(KEY_LOT_NOB,"").commit();
                edtCode.setText("");
                textJishub.setText("0");
                wxngqtyb=0;
                cidb=0;
                currMONOB="";
                adapterb.clear();
                showSuccess("已新建");
            }
            @Override
            public void OnCancel(DialogInterface dialog) {
                showMessage("取消新建");
            }
        });
    }
    //标签打印
    public void print1(){
        showLoading();
        String sprn=BaseApplication.currUser.getUserCode();
        String print_id= SystemSetActivity.getData(SystemSetActivity.PRINT_ID_KEY);
        HashMap<String, String> hm = ToolUtils.printLable("100","", tableName, tableKey, lotnoA, insobj, sprn, print_id);
        hm.put("frompage","RepairBadActivity");//用于区别打印来源页面，然后做特殊操作处理。这个为了自动同步主表已包装数量
        commPresenter.printLable(hm,1);
    }
    //标签复制
    public void print2(){
        showLoading();
        String sprn2=BaseApplication.currUser.getUserCode();
        String print_id2= SystemSetActivity.getData(SystemSetActivity.PRINT_ID_KEY);
        HashMap<String, String> hm2 = ToolUtils.printLable("200","", tableName, tableKey, lotnoA, insobj, sprn2, print_id2);
        hm2.put("frompage","RepairBadActivity");//用于区别打印来源页面，然后做特殊操作处理。这个为了自动同步主表已包装数量
        commPresenter.printLable(hm2,2);
    }
    //标签打印
    public void print3(){
        showLoading();
        String sprn=BaseApplication.currUser.getUserCode();
        String print_id= SystemSetActivity.getData(SystemSetActivity.PRINT_ID_KEY);
        HashMap<String, String> hm = ToolUtils.printLable("100","", tableName, tableKey, lotnoB, insobj, sprn, print_id);
        hm.put("frompage","RepairBadActivity");//用于区别打印来源页面，然后做特殊操作处理。这个为了自动同步主表已包装数量
        commPresenter.printLable(hm,1);
    }
    //标签复制
    public void print4(){
        showLoading();
        String sprn2=BaseApplication.currUser.getUserCode();
        String print_id2= SystemSetActivity.getData(SystemSetActivity.PRINT_ID_KEY);
        HashMap<String, String> hm2 = ToolUtils.printLable("200","", tableName, tableKey, lotnoB, insobj, sprn2, print_id2);
        hm2.put("frompage","RepairBadActivity");//用于区别打印来源页面，然后做特殊操作处理。这个为了自动同步主表已包装数量
        commPresenter.printLable(hm2,2);
    }
    /**
     * 已扫描数量
     * @param scanqty
     */
    public void setScanAQty(int scanqty){
        // cache.put(KEY_SCAN_QTY,scanqty);//缓存满箱数量
        textJishu.setText(""+scanqty);
        wxngqty=scanqty;
    }
    public void setScanBQty(int scanqty){
        // cache.put(KEY_SCAN_QTY,scanqty);//缓存满箱数量
        textJishub.setText(""+scanqty);
        wxngqtyb=scanqty;
    }
    private void initTabHost(){
        /*-------TabHost  分页初始化-----------*/
        tabHost.setup();//用tabhost必须调用这个代码，不然在setContent会报错
        TabHost.TabSpec tab1 = tabHost.newTabSpec("tab1").setIndicator("A工单").setContent(R.id.tab1);
        tabHost.addTab(tab1);
        TabHost.TabSpec tab2 = tabHost.newTabSpec("tab2").setIndicator("B工单").setContent(R.id.tab2);
        tabHost.addTab(tab2);
        //设置Tab标题字体大小
        TabWidget tabWidget = tabHost.getTabWidget();
        for (int i = 0; i < tabWidget.getChildCount(); i++) {
            TextView tv=(TextView)tabWidget.getChildAt(i).findViewById(android.R.id.title);
            tv.setTextSize(16);//设置字体的大小；
            if(i==0){
                tv.setTextColor(Color.RED);
            }
        }
        textJishu.setTextColor(Color.RED);
        textJishub.setTextColor(Color.GRAY);
        tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                for (int i = 0; i < tabWidget.getChildCount(); i++) {
                    TextView tv=(TextView)tabWidget.getChildAt(i).findViewById(android.R.id.title);
                    if(tabHost.getCurrentTab()==i){
                        tv.setTextColor(Color.RED);//选中为红色
                    }else{
                        tv.setTextColor(Color.GRAY);//未选中为灰色
                    }
                }
                if(tabHost.getCurrentTab()==0){
                    //A
                    textJishu.setTextColor(Color.RED);
                    textJishub.setTextColor(Color.GRAY);
                }else{
                    //B
                    textJishu.setTextColor(Color.GRAY);
                    textJishub.setTextColor(Color.RED);
                }
            }
        });

    }    /***
     * 初始化表格
     */
    private void initTableView() {
        initTabHost();
        dataList = new ArrayList();
        List<HeaderRowInfo> header = getRowDataListBlpsx();
        dataListViewContent.setChoiceMode(ListView.CHOICE_MODE_NONE);
        adapter = new BaseTableDataAdapter(this,tableView,dataListViewContent,dataList,header);
        adapter.setSwipeRefreshEnabled(false);
        adapter.setTitle("项次");
        adapter.setTitleHeight(90);
        adapter.setTitleWidth(90);
        tableView.setAdapter(adapter);
        initTableViewb();
    }
    private void initTableViewb() {
        dataListb = new ArrayList();
        List<HeaderRowInfo> header = getRowDataListBlpsx();
        dataListViewContentb.setChoiceMode(ListView.CHOICE_MODE_NONE);
        adapterb = new BaseTableDataAdapter(this,tableViewb,dataListViewContentb,dataListb,header);
        adapterb.setSwipeRefreshEnabled(false);
        adapterb.setTitle("项次");
        adapterb.setTitleHeight(90);
        adapterb.setTitleWidth(90);
        tableViewb.setAdapter(adapterb);
    }
    /***
     * 初始化表格
     */
    /***
     * 不良品送修
     * 初始化表格头数据，所有工站通用，配置列表
     * @return 返回通用表格头
     */
    public  List<HeaderRowInfo> getRowDataListBlpsx() {
        List<HeaderRowInfo> rowList = new ArrayList<>();
        HeaderRowInfo sidColumn = new HeaderRowInfo("snrem", "生产喷码序号", 400);
        rowList.add(sidColumn);
        sidColumn = new HeaderRowInfo("spc_no", "不合格原因", 150);
        HashMap<String, String> hm = CommonUtils.JSONArrayToMap(jar, "spc_no", "name");
        sidColumn.setAttr(3);
        sidColumn.setContrastMap(hm);
        rowList.add(sidColumn);
        sidColumn = new HeaderRowInfo("ngqty", "不合格数量", 120);
        rowList.add(sidColumn);
        sidColumn = new HeaderRowInfo("position", "位置", 120);
        rowList.add(sidColumn);
        sidColumn = new HeaderRowInfo("slkid", "工单号", 220);
        rowList.add(sidColumn);
        sidColumn = new HeaderRowInfo("prd_no", "货品代号", 150);
        rowList.add(sidColumn);
        return rowList;
    }

    /**
     * 制程下拉框初始化
     * @param pairList
     */
    private void initZcnoSpinner(List<Pair> pairList){
        zcnoSpinner.setAdapter(SpinnerAdapterImpl.getSpinnerAdapter(getApplicationContext(),pairList));
        zcnoSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                zcno=((Pair) zcnoSpinner.getSelectedItem()).getValue();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }
    private void showPMError(String msg){
        CommonUtils.speak(msg);
        showMessage(msg);
        hideLoading();
        CommonUtils.textViewGetFocus(edtCode);
    }
}
