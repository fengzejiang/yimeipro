package com.yimeinew.activity.deviceproduction.commsub;

import android.app.AlertDialog;
import android.content.*;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import butterknife.*;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.shade.com.alibaba.fastjson.JSONArray;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.shade.com.alibaba.fastjson.JSONObject;
import com.yimeinew.activity.R;
import com.yimeinew.activity.base.BaseActivity;
import com.yimeinew.activity.base.BaseApplication;
import com.yimeinew.adapter.SpinnerAdapterImpl;
import com.yimeinew.adapter.tabledataadapter.BaseTableDataAdapter;
import com.yimeinew.data.CheckReason;
import com.yimeinew.data.EquipmentInfo;
import com.yimeinew.data.MESPRecord;
import com.yimeinew.data.ZCInfo;
import com.yimeinew.entity.Pair;
import com.yimeinew.modelInterface.BaseStationBindingView;
import com.yimeinew.modelInterface.CommFastView;
import com.yimeinew.network.schedulers.SchedulerProvider;
import com.yimeinew.presenter.CommBasePresenter;
import com.yimeinew.presenter.CommFastPresenter;
import com.yimeinew.presenter.CommStationZCPresenter;
import com.yimeinew.tableui.TablePanelView;
import com.yimeinew.tableui.entity.HeaderRowInfo;
import com.yimeinew.utils.*;
import com.yimeinew.view.AuxText;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RepairBadActivity extends BaseActivity implements CommFastView,BaseStationBindingView{
    private final String TAG_NAME = CommGJActivity.class.getSimpleName();

    //数据表格
    @BindView(R.id.table_view)
    TablePanelView tableView;
    @BindView(R.id.data_list_content)
    ListView dataListViewContent;
    List<JSONObject> dataList;
    BaseTableDataAdapter adapter;
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



    private String lotno="";//当前表主键
    String KEY_LOT_NO="KEY_LOT_NO"+TAG_NAME;//主键缓存，下次进入页面重新加载
    private String currMONO = "";//当前工单号
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
    private int cid=0;//获取序号。

    private String zcno="";//当前制程
    private ZCInfo zCnoInfo;
    private String GBKEY="kabanguozhan_mz";
    IntentFilter intentFilter;
    private CommFastPresenter commPresenter;
    private CommBasePresenter commBPresenter;
    CommStationZCPresenter commZcPresenter;
    public static final String Title = "不良品送修";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bad_repair);
        ButterKnife.bind(this);
        zCnoInfo = (ZCInfo) getIntent().getSerializableExtra(CommCL.COMM_ZC_INFO_FLD);

        this.setTitle(Title+"->"+zCnoInfo.getName());
//        causeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, causeAdapterData);
//        spReanson.setAdapter(causeAdapter);
        commPresenter = new CommFastPresenter (this,SchedulerProvider.getInstance(),zCnoInfo);
        //commBPresenter=new CommBasePresenter(this,SchedulerProvider.getInstance());

        //edtCode.setText("2447620940T-01182320022980000QAB0B51Y");
        commZcPresenter = new CommStationZCPresenter(this,SchedulerProvider.getInstance());
        //不良原因查找
        commZcPresenter.selectBlyy();
        edtOP.setOPAux();
        //查询不良送修制程
        commPresenter.getAssistInfo(CommCL.AID_MZWX_ZCNO,"",1);
        //查询送修位置--数据在常量定义
        commPresenter.getAssistInfo(CommCL.AID_WEB_INSSYSCL,"~sname='WEB_WXWZ'",2);
    }
    /***
     * 不良原因下拉列表，发起原因选中项切换事件
     * */
//    @OnItemSelected({ R.id.sp_reason})
//    void OnItemSelected(AdapterView<?> parent, View view, int position, long id1) {
//        int id = parent.getId();
//        if (id == R.id.sp_reason) {
//            JSONObject jobj = jar.getJSONObject(position);
//            name =jobj.getString("name");
//            String op =edtOP.getText().toString();
//            if(TextUtils.isEmpty(op)){
//                CommonUtils.textViewGetFocus(edtOP);
//            }else{
//                CommonUtils.textViewGetFocus(edtCode);
//            }
//
//
//        }
//    }


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
        if (id == R.id.edt_code) {
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
            String code = edtCode.getText().toString().toUpperCase();
            //第一次查询
            commPresenter.selectBlpsx(code,1);
            CommonUtils.textViewGetFocus(edtCode);
            return true;
        }
        return false;
    }


    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG_NAME,"onResume");
        lotno=CommCL.sharedPreferences.getString(KEY_LOT_NO,"");
        if(!TextUtils.isEmpty(lotno)) {
            //获取这个一整包的信息
            commPresenter.getPackInfoById(lotno, 2);
            //showMessage("lotno="+lotno);
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
     * 不良品送修
     * 初始化表格头数据，所有工站通用，配置列表
     * @return 返回通用表格头
     */
    public  List<HeaderRowInfo> getRowDataListBlpsx() {
        List<HeaderRowInfo> rowList = new ArrayList<>();
        HeaderRowInfo sidColumn = new HeaderRowInfo("snrem", "生产喷码序号", 350);
        rowList.add(sidColumn);
        sidColumn = new HeaderRowInfo("spc_no", "不合格原因", 130);
        HashMap<String, String> hm = CommonUtils.JSONArrayToMap(jar, "spc_no", "name");
        sidColumn.setAttr(3);
        sidColumn.setContrastMap(hm);
        rowList.add(sidColumn);

        sidColumn = new HeaderRowInfo("ngqty", "不合格数量", 120);
        rowList.add(sidColumn);
        sidColumn = new HeaderRowInfo("prd_no", "货品代号", 150);
        rowList.add(sidColumn);
        sidColumn = new HeaderRowInfo("prd_name", "货品名称", 150);
        rowList.add(sidColumn);
        sidColumn = new HeaderRowInfo("prd_mark", "BinCode", 120);
        rowList.add(sidColumn);

        return rowList;
    }
    /***
     * 初始化表格
     */
    private void initTableView() {
        dataList = new ArrayList();
        List<HeaderRowInfo> header = getRowDataListBlpsx();
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
            String mono = batchInfo.getString("sid");//工单号
            String sid1=batchInfo.getString("sid1");//批次号
            MESPRecord record = new MESPRecord(sid1, mono, zcno,"");
            if(TextUtils.isEmpty(lotno)) {
                //保存记录
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
                batchInfo.put("mono",mono);
                commPresenter.blpsxRecord(record,batchInfo);//存入记录表

            }else{

                String snrem=batchInfo.getString("allcode");
                spc_no=spReanson.getText().toString();//不良品原因
                String position =edtPosition.getText().toString();//维修位置
                int ngqty=batchInfo.getInteger("ngqty");//不合格数量
//                record.setCode(snrem);
//                record.setSpc_no(spc_no);
//                record.setQty(ngqty);
                batchInfo.put("mono",batchInfo.getString("sid"));
                batchInfo.put("sid",lotno);
                batchInfo.put("snrem",snrem);
                batchInfo.put("spc_no",spc_no);
                batchInfo.put("position",position);
                batchInfo.put("ngqty",ngqty);
                batchInfo.put("cl_no",1);
                batchInfo.put("mkdate",record.getMkdate());
                cid++;
                batchInfo.put("cid",cid);
                commPresenter.savePackInfo(batchInfo,batchInfo);

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
    public void saveMoCallBack(Boolean bok, JSONObject batchInfo, String error) {
        if (bok) {
            //保存表头完成，缓存主键
            lotno=batchInfo.getString("lotno");
            CommCL.sharedPreferences.edit().putString(KEY_LOT_NO,lotno).commit();
            //保存表身
            String mono = batchInfo.getString("sid");//工单号
            currMONO=mono;
            String sid1=batchInfo.getString("sid1");//批次号
            String snrem=batchInfo.getString("allcode");//喷码
            spc_no=spReanson.getText().toString();//不良品原因
            String position=edtPosition.getText().toString();//维修位置
            int ngqty=batchInfo.getInteger("ngqty");//不合格数量
//            MESPRecord record = new MESPRecord(sid1, mono, zcno,"");
//            record.setCode(snrem);
//            record.setSpc_no(spc_no);
//            record.setQty(ngqty);
//            record.setSid(lotno);
            batchInfo.put("sid",lotno);
            batchInfo.put("snrem",snrem);

            batchInfo.put("spc_no",spc_no);
            batchInfo.put("ngqty",ngqty);
            batchInfo.put("cl_no",1);
            batchInfo.put("mkdate",DateUtil.getCurrDateTime(ICL.DF_YMDT));
            cid++;
            batchInfo.put("cid",cid);
            batchInfo.put("mono",mono);
            batchInfo.put("position",position);

            commPresenter.savePackInfo(batchInfo,batchInfo);

        } else {
            hideLoading();
            CommonUtils.speak(error);
            showMessage(error);
            CommonUtils.textViewGetFocus(edtCode);

        }
    }

    //保存记录成功，将数据展示在PDA
    @Override
    public void saveMarkingCallBack(Boolean bok, JSONObject batchInfo, String error) {
        if(bok){
            //添加喷码缓存
            bcode.put(batchInfo.getString("allcode"),batchInfo.getString("allcode"));
            // 添加到数据列表
            adapter.addRecord(batchInfo);
            wxngqty++;
            textJishu.setText(wxngqty + "");
            //lotno=batchInfo.getString("lotno");

            //更新表头
            JSONObject jsObj=new JSONObject();
            jsObj.put("sid",lotno);
            jsObj.put("wxngqty",wxngqty);
            jsObj.put("ngqty",wxngqty);
            commPresenter.updateInfo(jsObj,CommCL.CELL_ID_D5064WEEB,1);

            //改变喷码子表的状态,通过对象定义
            JSONObject jsObj1=new JSONObject();
            jsObj1.put("allcode",batchInfo.getString("snrem"));
            jsObj1.put("mono",batchInfo.getString("mono"));
            if(!batchInfo.containsKey("wxnum")){
                jsObj1.put("wxnum",1);
            }else {
                jsObj1.put("wxnum",batchInfo.getInteger("wxnum")+1);
            }

            batchInfo.getString("mono");
            commPresenter.updateInfo(jsObj1,CommCL.CELL_ID_D5010WEBC,2);
            hideLoading();
            CommonUtils.textViewGetFocus(edtCode);

        }else{

            hideLoading();
            CommonUtils.canDo(GBKEY);
            CommonUtils.speak(error);
            showMessage(error);
        }



    }


    /*---------------alter 弹框----------------*/
    public void alertWindow1(Context context,String title){
        canGetMessage=false;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        LinearLayout layout=new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        if(!TextUtils.isEmpty(lotno)) {
            TextView text = new TextView(context);//文本框
            text.setTextSize(24);
            text.setText(lotno);
            text.setGravity(Gravity.CENTER);
            text.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    // 创建普通字符型ClipData
                    ClipData mClipData = ClipData.newPlainText("Label", lotno);
                    // 将ClipData内容放到系统剪贴板里。
                    cm.setPrimaryClip(mClipData);
                    Toast.makeText(context, "已复制" , Toast.LENGTH_SHORT).show();
                }
            });
            /*条形码*/
            /*
            ImageView imgBat=new ImageView(context);
            Bitmap bitBat = QRUtil.creatBarcode(context,lotno,550,20,true);
            imgBat.setImageBitmap(bitBat);
            */
            /*二维码*/
            ImageView imgQR=new ImageView(context);
            Bitmap bitQR = QRUtil.createQRImage(lotno, 550, 550);
            imgQR.setImageBitmap(bitQR);
            layout.addView(text);
            //layout.addView(imgBat);
            layout.addView(imgQR);
        }
        builder.setView(layout);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                canGetMessage=true;
                //clearFullBox();
                lotno="";
                CommCL.sharedPreferences.edit().putString(KEY_LOT_NO,"").commit();
                edtCode.setText("");
                textJishu.setText("0");
                wxngqty=0;
                cid=0;
                currMONO="";
                adapter.clear();
                Toast.makeText(context, "已新建" , Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener()
        {

            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                canGetMessage=true;
                Toast.makeText(context, "取消新建" , Toast.LENGTH_SHORT).show();
            }
        });
        builder.show();
    }




    /**
     * 已扫描数量
     * @param scanqty
     */
    public void setScanPackQty(int scanqty){
        // cache.put(KEY_SCAN_QTY,scanqty);//缓存满箱数量
        textJishu.setText(""+scanqty);
        wxngqty=scanqty;
    }


    @Override
    public void getPackInfoCallBack(Boolean bok, JSONObject headinfo, JSONArray info, String error, int key) {
        if(bok){
            setScanPackQty(headinfo.getInteger("wxngqty"));
            currMONO=headinfo.getString("slkid");
            //清理列表
            adapter.clear();
            cid=info.size();
            //插入表格
            for(int i=0;i<info.size();i++) {
                JSONObject temp = info.getJSONObject(i);
//                temp.put("sid1", headinfo.getString("sid1"));
//                temp.put("prd_no", headinfo.getString("prd_no"));
//                temp.put("snrem", headinfo.getString("snrem"));
//                temp.put("spc_no", headinfo.getString("spc_no"));
                //添加缓存去重复
                String penma=temp.getString("snrem");
                bcode.put(penma,penma);
                adapter.addRecord(CommonUtils.getJsonObjFromBean(temp));
            }
            hideLoading();


        }else{
            showMessage(error);
            //mei查询到信息就自动新建
            lotno="";
            CommCL.sharedPreferences.edit().putString(KEY_LOT_NO,"").commit();
            edtCode.setText("");
            textJishu.setText("0");
            wxngqty=0;
            cid=0;
            currMONO="";
            adapter.clear();
            hideLoading();
        }

    }

    @Override
    public void getAssistInfoBack(Boolean bok, JSONArray info, String error, int key) {
        if(bok){
            switch (key){
                case 1://不良品送修制程
                    ArrayList<Pair> pairList=new ArrayList<>();
                    for(int i=0;i<info.size();i++){
                        JSONObject obj=info.getJSONObject(i);
                        Pair pair=new Pair(obj.getString("name"),obj.getString("id"));
                        pairList.add(pair);
                    }
                    initZcnoSpinner(pairList);
                    break;
                case 2://不良品送修位置
                    if(info!=null&&info.size()>0){
                        String sbds=info.getJSONObject(0).getString("sbds");
                        HashMap hm = ToolUtils.parseConstant(sbds);//常量格式转换
                        edtPosition.setAux(hm);
                    }
                    break;
            }
        }else{
            showMessage(error);
        }
    }


    /***
     * 获取发起原因回调事件
     * @param bok 是否成功
     * @param recordList 成功返回JsonArray,失败返回null
     * @param error 错误信息
     */
    @Override
    public void loadReasonsBack(boolean bok, Object recordList, String error) {
        if (bok) {
            jar = (JSONArray) recordList;
            initTableView();
            spReanson.setAuxArray(jar,"spc_no","name");

//            for (int i = 0; i < jar.size(); i++) {
//                if(i==0){
//                    name=jar.getJSONObject(i).getString("name");
//                }
////                ZCInfo zc = zcInfoList.get(i);
////                String name=zc.getName();
////               CheckReason checkReason = JSONObject.parseObject(jar.getJSONObject(i).toJSONString(), CheckReason.class);
////                causeAdapterData.add(checkReason);
//            }
//            causeAdapter.notifyDataSetChanged();

        } else {
//            causeAdapterData.clear();
//            causeAdapter.notifyDataSetChanged();
//            dataList.clear();
            hideLoading();
            showMessage(error);
        }
    }

    @Override
    public void loadCheckProjectBack(boolean bok, Object recordList, String error) {

    }

    @Override
    public void checkQCBatInfoBack(boolean bok, Object o, String error) {

    }

    @Override
    public void commonBack(boolean bok, Object recordList, String error, int key) {

    }

    @Override
    public void checkMboxCallBack(boolean bok, String error, int key) {

    }

    @Override
    public void checkSidCallBack(boolean bok, JSONObject batchInfo, String error) {

    }

    @Override
    public void checkSbIdCallBack(boolean bok, EquipmentInfo sbInfo, String error) {

    }

    @Override
    public void checkRecordCallBack(boolean bok, JSONObject sbInfo, String error) {

    }

    @Override
    public String getCurrMO() {
        return currMONO;
    }

    @Override
    public List<JSONObject> getDataList() {
        return null;
    }

    //    //保存记录成功
    @Override
    public void saveRecordBack(boolean bok, Object records, String error) {
//        if (bok) {
//            //放到扫描的列表中
//            //JSONObject batchInfo= (JSONObject) records;
//            MESPRecord record=(MESPRecord) records;
//            //添加 喷码code 的页面缓存
//            //bcode.put(batchInfo.getString("allcode"), batchInfo.getString("allcode"));
//            bcode.put(record.getPrtno(),record.getPrtno());
//            // 添加到数据列表
//            adapter.addRecord(CommonUtils.getJsonObjFromBean(record));
//            //给 计数赋值
//            kbJishu++;
//            textJishu.setText(kbJishu + "");
//
//            //改变喷码子表的状态,通过对象定义
//            JSONObject jsObj1=new JSONObject();
//            jsObj1.put("allcode",record.getPrtno());
//            jsObj1.put("mono",record.getSlkid());
//            //要判断是OK还是NG
//            String judgement=record.getBok();
//            String sort=record.getSort();
//            if(judgement.equals("0")){
//                if(sort.equals("A")){
//                    jsObj1.put("scstate","1");//OK
//                }else if(sort.equals("B")){
//                    jsObj1.put("qcstate","1");//OK
//                }
//
//            }else if(judgement.equals("1")){
//                if(sort.equals("A")){
//                    jsObj1.put("scstate","-1");//NG
//                }else if(sort.equals("B")){
//                    jsObj1.put("qcstate","-1");//OK
//                }
//
//            }else{
//                CommonUtils.speak("判定结果不存在");
//            }
//            if(sort.equals("A")){
//                jsObj1.put("scdate", DateUtil.getCurrDateTime(ICL.DF_YMDT));
//                commPresenter.updateInfo(jsObj1,CommCL.CELL_ID_D5080WEB,2);
//            }else if(sort.equals("B")){
//                jsObj1.put("qcdate", DateUtil.getCurrDateTime(ICL.DF_YMDT));
//                commPresenter.updateInfo(jsObj1,CommCL.CELL_ID_D5084WEB,2);
//            }
//
//            hideLoading();
//            CommonUtils.textViewGetFocus(edtCode);
//
//
//        } else {
//            hideLoading();
//            CommonUtils.canDo(GBKEY);
//            CommonUtils.speak(error);
//            showMessage(error);
//        }
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
            edtPosition.setText("");
            CommonUtils.textViewGetFocus(edtCode);
        }
    }

    @Override
    public void changeMultiRecordStateBack(boolean bok, List<MESPRecord> recordList, String error) {

    }

    @Override
    public void getMultiRecordBack(boolean bok, JSONArray recordList, String error, int type) {

    }

    @Override
    public void updateCallBack(boolean bok, Object record, String error, int key) {

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

    @Override
    public void onRemoteFailed(String message) {

        hideLoading();
        CommonUtils.canDo(GBKEY);
        CommonUtils.showError(this, "onRemoteFailed="+message);

    }

    public void initZcnoSpinner(List<Pair> pairList){
        zcnoSpinner.setAdapter(SpinnerAdapterImpl.getSpinnerAdapter(getApplicationContext(),pairList));
        zcnoSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,int position, long id) {
                zcno=((Pair) zcnoSpinner.getSelectedItem()).getValue();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }
}
