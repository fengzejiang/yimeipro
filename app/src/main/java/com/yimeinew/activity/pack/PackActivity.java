package com.yimeinew.activity.pack;

import android.app.AlertDialog;
import android.content.*;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
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
import com.yimeinew.adapter.tabledataadapter.BaseTableDataAdapter;
import com.yimeinew.data.PackHeadInfo;
import com.yimeinew.data.PackInfo;
import com.yimeinew.data.ZCInfo;
import com.yimeinew.entity.PrintModel;
import com.yimeinew.listener.OnConfirmListener;
import com.yimeinew.modelInterface.CommPackView;
import com.yimeinew.network.schedulers.SchedulerProvider;
import com.yimeinew.presenter.CommPackPresenter;
import com.yimeinew.tableui.TablePanelView;
import com.yimeinew.tableui.entity.HeaderRowInfo;
import com.yimeinew.utils.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PackActivity extends BaseActivity implements CommPackView {
    private final String TAG_NAME = PackActivity.class.getSimpleName();


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
    EditText edtTray;//Tray
    @BindView(R.id.edt_sid1)
    EditText edtSid1;//喷码
    @BindView(R.id.text_fill_pack)
    EditText edtFillPack;//满箱数量
    @BindView(R.id.text_already_pack)
    EditText edtAlreadyPack;//已包装的数量
    @BindView(R.id.text_not_scanning)
    EditText edtNotScan;//未扫描的数量
    @BindView(R.id.text_totalqty)
    EditText textTotalQty;//投产数量
    @BindView(R.id.text_yipack)
    EditText textYiPack;//未包装数量
    @BindView(R.id.text_weipack)
    EditText textWeiPack;//未已包装数量
    @BindView(R.id.img_new)
    ImageView img_new;//满箱后新建
    @BindView(R.id.btn_print)
    Button printBotton;
    //缓存机制
    private String lotno="";//当前表主键
    String KEY_LOT_NO="KEY_LOT_NO"+TAG_NAME;//主键缓存，下次进入页面重新加载
    private String currMONO = "";//当前工单号
    private int moqty=0;
    private String tray;//Tray盘号
    private JSONObject traymo=new JSONObject();//Tray盘号和工单信息
    private HashMap<String,Integer> cache=new HashMap<String,Integer>();
    //定义常亮
    String KEY_FULL_BOX="KEY_FULL_BOX";//满箱数量
    String KEY_Tray_QTY="KEY_Tray_QTY";//满盘数量
    String KEY_SCAN_QTY="KEY_SCAN_QTY";//已扫数量
    String KEY_Tray_SCAN_QTY="KEY_Tray_SCAN_QTY";//单盘已扫数量
    ArrayList<ZCInfo> zcInfoList = BaseApplication.zcList;
    private HashMap<String, String> bindSid1 = new HashMap<>();

    private String zcno="";//当前制程
    private ZCInfo zCnoInfo;
    private String GBKEY="MZ_SAO_PEN_MA";
    IntentFilter intentFilter;
    private CommPackPresenter commPresenter;
    public static final String Title = "模组内装";
    private  boolean b1=false;
    private  boolean b2=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pack);
        ButterKnife.bind(this);
        zCnoInfo = (ZCInfo) getIntent().getSerializableExtra(CommCL.COMM_ZC_INFO_FLD);
        zcno=zCnoInfo.getId();
        this.setTitle(Title+"->"+zCnoInfo.getName());


        commPresenter = new CommPackPresenter(this,SchedulerProvider.getInstance());
        initTableView();
        cache.put(KEY_SCAN_QTY,0);//默认已装装箱为0

        lotno=CommCL.sharedPreferences.getString(KEY_LOT_NO,"");
        if(!TextUtils.isEmpty(lotno)){
            //获取这个一整包的信息
            showLoading();
            commPresenter.getPackInfoById(lotno,"F0028",1);
            //showMessage("lotno="+lotno);
        }
        //edtTray.setText("MOB18060052-0001");
        //edtSid1.setText("2447620940T-01182320001430000QAB0B51Y");
    }


    @OnEditorAction({R.id.edt_op, R.id.edt_tray, R.id.edt_sid1})
    public boolean OnEditorAction(EditText editText) {
        return onEditTextKeyDown(editText);
    }
    @Override
    public boolean onEditTextKeyDown(EditText editText) {
        int id = editText.getId();
        String operationUser = edtOP.getText().toString().toUpperCase();
        if (TextUtils.isEmpty(operationUser)) {
            //showMessage("请输入操作员!");
            CommonUtils.speak("请输入操作员");
            CommonUtils.textViewGetFocus(edtOP);
            return true;
        }
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
            CommonUtils.textViewGetFocus(edtTray);
            return false;
        }
        String tray1=edtTray.getText().toString().toUpperCase();
        if(TextUtils.isEmpty(tray1)){
            //showMessage("请输入Tray盘号");
            CommonUtils.speak("请输入炊盘号");
            CommonUtils.textViewGetFocus(edtTray);
            return true;
        }
        if(id==R.id.edt_tray){
            showLoading();
            //输入Tray号处理逻辑
            commPresenter.getTrayAndMo(tray1,1);
            return false;
        }
        if (id == R.id.edt_sid1) {
            String sid1 = edtSid1.getText().toString().toUpperCase();
            if (TextUtils.isEmpty(sid1)) {
                //showMessage("请输入批次号");
                CommonUtils.speak("请输入喷码");
                CommonUtils.textViewGetFocus(edtSid1);
                return true;
            }
            if (bindSid1.containsKey(sid1)) {
                //showMessage("该批次号【" + sid1 + "】已经扫描过");
                CommonUtils.speak("已扫");
                CommonUtils.textViewGetFocus(edtSid1);
                return true;
            }

            //校验批次号
            //commPresenter.checkQuickLot(sid1,zcno,1);
            if(TextUtils.equals(tray,tray1)) {
                showLoading();
                commPresenter.getMarkingInfo(sid1, traymo, 1);
            }else{
                //showMessage("手输Tray盘号必须回车！");
                CommonUtils.speak("手输炊盘号必须回车");
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
    }
    //退出当前Activity或者跳转到新Activity时被调用
    @Override
    protected void onStop() {
        super.onStop();
    }
    //Activity被覆盖到下面或者锁屏时被调用
    @Override
    protected void onPause() {
        super.onPause();
    }

    /***
     * 初始化表格
     */
    /***
     * 初始化表格
     */
    private void initTableView() {
        dataList = new ArrayList();
        List<HeaderRowInfo> header = CommonUtils.getRowDataList_Pack();
        initView();
        adapter = new BaseTableDataAdapter(this,tableView,dataListViewContent,dataList,header);
        adapter.setSwipeRefreshEnabled(false);
        adapter.setTitle("项次");
        adapter.setTitleHeight(70);
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
    @OnClick(R.id.img_new)
    public void onClick(View view){
        int id=view.getId();
        switch (id){
            case R.id.img_new:
                if(!CommonUtils.isRepeat(TAG_NAME+"aw",TAG_NAME+"aw",2000)) {
                    alertWindow1(this, "是否新建箱号");
                }
                break;
        }
    }
    @Override
    public void onRemoteFailed(String message) {

        hideLoading();
        CommonUtils.canDo(GBKEY);
        CommonUtils.showError(this, "onRemoteFailed="+message);

    }

    @Override
    public void commCallBack(Boolean bok, JSONObject info, String error, int key) {

    }

    @Override
    public void commCallBack(Boolean bok, JSONObject info, String error, String key) {

    }

    @Override
    public void getTrayAndMoCallBack(Boolean bok, JSONObject info, String error, int key) {
        if(bok){
            if(TextUtils.isEmpty(currMONO)) {
                currMONO = info.getString("sid");//缓存工单
            }
            //设置缓存数据
            tray=info.getString("sid1");
            info.put("minqty",info.getInteger("qty"));
            info.put("slkid",info.getString("sid"));
            traymo=info;
            //缓存数据//设置界面信息
            setFillPackQty(info.getInteger("binfullqty"));
            setTrayQty(info.getInteger("qty"),info.getInteger("tray_in_qty"));
            CommonUtils.textViewGetFocus(edtSid1);
            hideLoading();
        }else{
            CommonUtils.speak(error);
            //showMessage(error);
            CommonUtils.textViewGetFocus(edtTray);
            hideLoading();
        }
    }

    @Override
    public String getCurrMO() {
        return currMONO;
    }


    @Override
    public void getMarkingCallBack(Boolean bok, JSONObject headinfo,JSONObject info, String error, int key) {
        if(bok){
            //判定Tray号是否回车
            String tray1=edtTray.getText().toString().toUpperCase();
            if(!TextUtils.equals(tray1,tray)){
                //showMessage("手输炊盘号必须回车！");
                CommonUtils.speak("手输炊盘号必须回车");
                hideLoading();
                return;
            }
            //判定是否满箱

            if(isFullBox()){
                //showMessage("已满箱");
                CommonUtils.speak("已满箱");
                CommonUtils.textViewGetFocus(edtTray);
                hideLoading();
                return;
            }
            //判定是否满盘
            if(isFullTray()){
                //showMessage("已满盘");
                CommonUtils.speak("已满盘");
                CommonUtils.textViewGetFocus(edtTray);
                hideLoading();
                return;
            }

            if(TextUtils.isEmpty(lotno)) {
                //然后保存，第一次保存
                String slkid=headinfo.getString("sid");
                String prd_no=headinfo.getString("prd_no");
                String prd_name=headinfo.getString("prd_name");
                int binfullqty=headinfo.getInteger("binfullqty");
                String op=edtOP.getText().toString().toUpperCase();
                String cus_no=headinfo.getString("cus_no");
                String bincode=headinfo.getString("prd_mark");
                String printid=headinfo.getString("printid");
                PackHeadInfo packHeadInfo=new PackHeadInfo(slkid,prd_no,prd_name,binfullqty,op,cus_no,bincode);
                if(!TextUtils.isEmpty(printid)){
                    packHeadInfo.setPrintid(printid);
                }
                packHeadInfo.setPur_qty(headinfo.getInteger("totalqty"));
                packHeadInfo.setMinqty(headinfo.getInteger("qty"));
                packHeadInfo.setSid1(headinfo.getString("sid1"));
                packHeadInfo.setRem(headinfo.getString("rem"));
                commPresenter.savePackHeadInfo(packHeadInfo,info,CommCL.CELL_ID_F0028,1);
            }else{
                //直接保存子表
                String sid1=info.getString("allcode");
                String slkid=info.getString("mono");
                String tray=headinfo.getString("sid1");
                int minqty=headinfo.getInteger("minqty");
                PackInfo packInfo=new PackInfo(lotno,sid1,slkid,tray,minqty);
                commPresenter.savePackInfo(headinfo,packInfo,CommCL.CELL_ID_F0028AWEB,1);
            }

        }else{
            CommonUtils.speak(error);
            CommonUtils.textViewGetFocus(edtSid1);
            //showMessage(error);
            hideLoading();
        }
    }

    @Override
    public void saveMoCallBack(Boolean bok, JSONObject headinfo,JSONObject info, String error, int key) {
        if(bok){
            //保存表头完成，缓存主键
            lotno=headinfo.getString("lotno");
            CommCL.sharedPreferences.edit().putString(KEY_LOT_NO,lotno).commit();

            //保存表身
            String sid1=info.getString("allcode");
            String slkid=info.getString("mono");
            String tray=headinfo.getString("sid1");
            int minqty=headinfo.getInteger("minqty");

            moqty=headinfo.getInteger("totalqty");//缓存工单数量
            PackInfo packInfo=new PackInfo(lotno,sid1,slkid,tray,minqty);
            commPresenter.savePackInfo(headinfo,packInfo,CommCL.CELL_ID_F0028AWEB,1);
        }else{
            CommonUtils.speak(error);
            CommonUtils.textViewGetFocus(edtSid1);
            //showMessage(error);
            hideLoading();
        }
    }

    @Override
    public void saveMarkingCallBack(Boolean bok, JSONObject headinfo,JSONObject info, String error, int key) {
        if(bok){
            //添加缓存去重复
            String penma=info.getString("sid1");
            bindSid1.put(penma,penma);
            //保存表身成功完成，插入记录
            info.put("tray",headinfo.getString("sid1"));
            info.put("slkid",headinfo.getString("slkid"));
            info.put("prd_name",headinfo.getString("prd_name"));
            info.put("prd_mark",headinfo.getString("prd_mark"));
            info.put("totalqty",headinfo.getInteger("totalqty"));
            info.put("minqty",headinfo.getInteger("minqty"));
            info.put("remark",headinfo.getString("rem"));

            adapter.addRecord(CommonUtils.getJsonObjFromBean(info));
            //计数

            addQty(1);

            //更新表头
            JSONObject jsObj=new JSONObject();
            jsObj.put("lotno",lotno);
            jsObj.put("qty",cache.get(KEY_SCAN_QTY));
            commPresenter.updateInfo(jsObj,CommCL.CELL_ID_F0028WEB,1);
            //更新喷码状态
            JSONObject jsObj1=new JSONObject();
            jsObj1.put("allcode",info.getString("sid1"));
            jsObj1.put("mono",info.getString("slkid"));
            jsObj1.put("state","04");
            jsObj1.put("edate", DateUtil.getCurrDateTime(ICL.DF_YMDT));
            commPresenter.updateInfo(jsObj1,CommCL.CELL_ID_D5010AWEB,2);
            CommonUtils.textViewGetFocus(edtSid1);
            if(isFullTray()){
                clearFullTray();
            }
        }else{
            CommonUtils.speak(error);
            CommonUtils.textViewGetFocus(edtSid1);
            //showMessage(error);
            hideLoading();
        }
    }

    @Override
    public void updateCallBack(Boolean bok, JSONObject info, String error, int key) {
        if(bok){
            //showMessage("更新成功"+key);
            if(key==1) {
                b1=true;
            }
            if(key==2){
                b2=true;
            }
            if(b1&&b2){
                b1=false;b2=false;
                hideLoading();
                if(!TextUtils.isEmpty(currMONO)){
                    commPresenter.getMoInfoById(currMONO,1);
                }

            }
        }else{
            CommonUtils.speak(error);
            CommonUtils.textViewGetFocus(edtSid1);
            //showMessage(error);
            hideLoading();
        }
    }

    @Override
    public void getPackInfoCallBack(Boolean bok, JSONObject headinfo, JSONArray info, String error, int key) {
        if(bok){
            //获取信息成功
            //String op=headinfo.getString("op");
           // edtOP.setText(op);
            //设置满箱和已扫描
            setFillPackQty(headinfo.getInteger("binfullqty"));
            setScanPackQty(headinfo.getInteger("qty"));
            currMONO=headinfo.getString("slkid");
            moqty=headinfo.getInteger("pur_qty");//缓存工单数量
            //清理列表
            adapter.clear();
            //插入表格
            for(int i=0;i<info.size();i++){
                JSONObject temp=info.getJSONObject(i);
                //temp.put("tray",headinfo.getString("tray"));
                temp.put("slkid",headinfo.getString("slkid"));
                temp.put("prd_name",headinfo.getString("prd_name"));
                temp.put("prd_mark",headinfo.getString("bincode"));
                temp.put("totalqty",headinfo.getInteger("pur_qty"));
                temp.put("remark",headinfo.getString("rem"));
                //添加缓存去重复
                String penma=temp.getString("sid1");
                bindSid1.put(penma,penma);
                adapter.addRecord(CommonUtils.getJsonObjFromBean(temp));
            }
            if(!TextUtils.isEmpty(currMONO)) {
                commPresenter.getMoInfoById(currMONO, 1);
            }
            //showMessage("获取成功"+key);
            hideLoading();
        }else{
            CommCL.sharedPreferences.edit().putString(KEY_LOT_NO,"").commit();
            lotno="";
            showMessage(error);
            hideLoading();
        }
    }

    @Override
    public void getMoInfoCallBack(Boolean bok, JSONObject info, String error, int key) {
        if(bok){
           int qty=info.getInteger("qty");
           textYiPack.setText(""+qty);
           textTotalQty.setText(""+moqty);
           textWeiPack.setText(""+(moqty-qty));
            if (isFullBox()) {
                if(!CommonUtils.isRepeat(TAG_NAME+"aw",TAG_NAME+"aw",5000)) {
                    alertWindow1(this, "是否新建箱号");
                }
            }
            hideLoading();
        }else{
            showMessage(error);
            hideLoading();
        }
    }

    /*---------------alter 弹框----------------*/
    public void alertWindow1(Context context,String title){
        canGetMessage=false;
        LinearLayout layout=new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        if(!TextUtils.isEmpty(lotno)) {
            TextView text = new TextView(context);//文本框
            text.setTextSize(24);
            text.setText(lotno);
            text.setGravity(Gravity.CENTER);
            text.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    // 创建普通字符型ClipData
                    ClipData mClipData = ClipData.newPlainText("Label", lotno);
                    // 将ClipData内容放到系统剪贴板里。
                    cm.setPrimaryClip(mClipData);
                    Toast.makeText(context, "已复制", Toast.LENGTH_SHORT).show();
                }
            });
            /*条形码*/
            /*
            ImageView imgBat=new ImageView(context);
            Bitmap bitBat = QRUtil.creatBarcode(context,lotno,550,20,true);
            imgBat.setImageBitmap(bitBat);
            */
            /*二维码*/
            ImageView imgQR = new ImageView(context);
            Bitmap bitQR = QRUtil.createQRImage(lotno, 550, 550);
            imgQR.setImageBitmap(bitQR);
            layout.addView(text);
            //layout.addView(imgBat);
            layout.addView(imgQR);
        }
        CommonUtils.confirm(context, title, "", layout, new OnConfirmListener() {
            @Override
            public void OnConfirm(DialogInterface dialog) {
                canGetMessage=true;
                clearFullBox();
                Toast.makeText(context, "已新建" , Toast.LENGTH_SHORT).show();
                layout.removeAllViews();
            }

            @Override
            public void OnCancel(DialogInterface dialog) {
                canGetMessage=true;
                Toast.makeText(context, "取消新建" , Toast.LENGTH_SHORT).show();
                layout.removeAllViews();
            }

        });

//        canGetMessage=false;
//        lotno="BZ20191021046";
//        AlertDialog.Builder builder = new AlertDialog.Builder(context);
//        builder.setTitle(title);
//        LinearLayout layout=new LinearLayout(context);
//        layout.setOrientation(LinearLayout.VERTICAL);
//        if(!TextUtils.isEmpty(lotno)) {
//            TextView text = new TextView(context);//文本框
//            text.setTextSize(24);
//            text.setText(lotno);
//            text.setGravity(Gravity.CENTER);
//            text.setOnClickListener(new View.OnClickListener(){
//                @Override
//                public void onClick(View v) {
//                    ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
//                    // 创建普通字符型ClipData
//                    ClipData mClipData = ClipData.newPlainText("Label", lotno);
//                    // 将ClipData内容放到系统剪贴板里。
//                    cm.setPrimaryClip(mClipData);
//                    Toast.makeText(context, "已复制" , Toast.LENGTH_SHORT).show();
//                }
//            });
//            /*条形码*/
//            /*
//            ImageView imgBat=new ImageView(context);
//            Bitmap bitBat = QRUtil.creatBarcode(context,lotno,550,20,true);
//            imgBat.setImageBitmap(bitBat);
//            */
//            /*二维码*/
//            ImageView imgQR=new ImageView(context);
//            Bitmap bitQR = QRUtil.createQRImage(lotno, 550, 550);
//            imgQR.setImageBitmap(bitQR);
//            layout.addView(text);
//            //layout.addView(imgBat);
//            layout.addView(imgQR);
//        }
//        builder.setView(layout);
//        builder.setPositiveButton("确定", new DialogInterface.OnClickListener()
//        {
//            @Override
//            public void onClick(DialogInterface dialog, int which)
//            {
//                canGetMessage=true;
//                clearFullBox();
//                Toast.makeText(context, "已新建" , Toast.LENGTH_SHORT).show();
//            }
//        });
//        builder.setNegativeButton("取消", new DialogInterface.OnClickListener()
//        {
//
//            @Override
//            public void onClick(DialogInterface dialog, int which)
//            {
//                canGetMessage=true;
//                Toast.makeText(context, "取消新建" , Toast.LENGTH_SHORT).show();
//            }
//        });
//        builder.show();
    }

    /*------------算法工具函数--------------*/

    /**
     * 设置满箱数量
     * @param fillQty
     */
    public void setFillPackQty(int fillQty){
        cache.put(KEY_FULL_BOX,fillQty);//缓存满箱数量
        edtFillPack.setText(""+fillQty);
        edtAlreadyPack.setText(""+cache.get(KEY_SCAN_QTY));
    }
    /**
     * 设置已装箱数量
     * @param scanqty
     */
    public void setScanPackQty(int scanqty){
        cache.put(KEY_SCAN_QTY,scanqty);//缓存满箱数量
        edtAlreadyPack.setText(""+scanqty);
    }
    /**
     * 设置Tray数量
     * @param trayQty
     * @param inTrayQty
     */
    public void setTrayQty(int trayQty,int inTrayQty){
        cache.put(KEY_Tray_QTY,trayQty);//缓存Tray数量
        cache.put(KEY_Tray_SCAN_QTY,inTrayQty);//Tray已包装的数量
        edtNotScan.setText(""+(trayQty-inTrayQty));
    }

    /**
     * 插入一条记录
     * @param qty
     */
    public void addQty(int qty){
        cache.put(KEY_SCAN_QTY,cache.get(KEY_SCAN_QTY)+qty);//已包装数量
        cache.put(KEY_Tray_SCAN_QTY,cache.get(KEY_Tray_SCAN_QTY)+qty);//Tray已包装的数量
        edtAlreadyPack.setText(""+cache.get(KEY_SCAN_QTY));
        edtNotScan.setText(""+(cache.get(KEY_Tray_QTY)-cache.get(KEY_Tray_SCAN_QTY)));
    }

    /**
     * 判断是否满箱
     * @return
     */
    public boolean isFullBox(){
        if(cache.get(KEY_FULL_BOX)>cache.get(KEY_SCAN_QTY)){
            return false;
        }else {
            return true;
        }
    }

    /**
     * 判断是否满盘
     * @return
     */
    public boolean isFullTray(){
        if(cache.get(KEY_Tray_QTY)>cache.get(KEY_Tray_SCAN_QTY)){
            return false;
        }else {
            return true;
        }
    }

    /**
     * 满箱清理数量缓存
     */
    public void clearFullBox(){

        cache.clear();
        lotno="";
        adapter.clear();
        tray="";
        currMONO="";
        moqty=0;
        traymo.clear();
        CommCL.sharedPreferences.edit().putString(KEY_LOT_NO,"").commit();
        edtAlreadyPack.setText("");
        edtNotScan.setText("");
        edtFillPack.setText("");
        edtSid1.setText("");
        edtTray.setText("");
        textTotalQty.setText("");
        textYiPack.setText("");
        textWeiPack.setText("");
        cache.put(KEY_SCAN_QTY,0);
        CommonUtils.textViewGetFocus(edtTray);
    }

    /**
     * 满盘清理Tray
     */
    public void clearFullTray(){
        edtSid1.setText("");
        edtTray.setText("");
        CommonUtils.textViewGetFocus(edtTray);
    }
    public void getMoInfoBySlkid(){

    }
    @OnClick({R.id.btn_print})
    public void Onclick(View view){
        int id=view.getId();
        switch (id){
            case R.id.btn_print:
                new Thread(new Runnable() {//创建线程，不然会报错
                    @Override
                    public void run() {
                        PrintModel printModel =ToolUtils.printPack("BZ20200226030","F0028");
                        String rs = ToolUtils.printLable("D:\\shinecli\\TEMP\\", printModel);
                        System.out.println("woshi=pls="+rs);
                    }
                }).start();
                break;
        }
    }
}
