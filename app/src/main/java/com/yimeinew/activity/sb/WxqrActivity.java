package com.yimeinew.activity.sb;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.*;
import butterknife.*;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.shade.com.alibaba.fastjson.JSONArray;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.shade.com.alibaba.fastjson.JSONObject;
import com.yimeinew.activity.R;
import com.yimeinew.activity.base.BaseActivity;
import com.yimeinew.activity.base.BaseApplication;
import com.yimeinew.adapter.tabledataadapter.BaseTableDataAdapter;
import com.yimeinew.data.MESPRecord;
import com.yimeinew.modelInterface.CommBaseView;
import com.yimeinew.network.schedulers.SchedulerProvider;
import com.yimeinew.presenter.CommOtherPresenter;
import com.yimeinew.tableui.TablePanelView;
import com.yimeinew.tableui.entity.HeaderRowInfo;
import com.yimeinew.utils.CommCL;
import com.yimeinew.utils.CommonUtils;
import com.yimeinew.tableui.CommMultiChoiceModeCallBack;
import com.yimeinew.utils.DateUtil;
import com.yimeinew.utils.ICL;
import com.yimeinew.view.AuxText;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class WxqrActivity extends BaseActivity implements CommBaseView {
    private static final String Title = "设备维修确认";
    private CommOtherPresenter commPresenter;
    private CommMultiChoiceModeCallBack commChoice;

    final int GETZCKEY = 1, GETZCWXYY = 2, GETSBINFO = 3;//keys

    //    @BindView(R.id.spinner_zc)
//    EditText zcSpinner;
    @BindView(R.id.edt_op)
    AuxText edtOP;
    @BindView(R.id.edt_equipment_no)
    EditText edtSbid;
    @BindView(R.id.yimei_weixiu_remark)
    EditText remark;
    @BindView(R.id.weixiu_start)
    Button wxStart;
    @BindView(R.id.weixiu_stop)
    Button wxStop;
    @BindView(R.id.wx_ghpj)
    Button wx_ghpj;
    //数据表格
    @BindView(R.id.table_view)
    TablePanelView tableView;
    @BindView(R.id.data_list_content)
    ListView dataListViewContent;
    List<JSONObject> dataList;
    BaseTableDataAdapter adapter;

    String zcno;
    String wx;
    String error1;
    JSONObject ngInfo = new JSONObject();
    JSONArray reasons;
    int num=0;//计算有几次结束维修
    private JSONArray jar;
    private HashMap<String, String> bh = new HashMap<>();//维修设备的编号，不是设备号

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wxqr);
        this.setTitle(Title);
        ButterKnife.bind(this);

        /*主持人*/
        commPresenter = new CommOtherPresenter(this,SchedulerProvider.getInstance());

        //设备维修原因加载
        commPresenter.getAssistInfo(CommCL.AID_SBWXYY,"",6);
        edtOP.setOPAux();



    }
    /***
     * 初始化表格头数据，设备维修确认
     * @return 设备维修确认
     */
    public  List<HeaderRowInfo> getRowDataListSbwxqr() {
        List<HeaderRowInfo> rowList = new ArrayList<>();
        HeaderRowInfo sidColumn = new HeaderRowInfo("sbid", "设备号", 150);
        rowList.add(sidColumn);

        sidColumn = new HeaderRowInfo("sopr", "报修人", 150);
        HashMap<String, String> bxr=(HashMap<String, String>) CommCL.sharedPreferences.getAll();
        sidColumn.setAttr(3);
        sidColumn.setContrastMap(bxr);
        rowList.add(sidColumn);

        sidColumn = new HeaderRowInfo("wxstate", "维修状态", 120);
        sidColumn.setAttr(3);
        sidColumn.setContrastMap(CommCL.STATESbwx);
        sidColumn.setContrastColors(CommCL.STATEColorMap);
        rowList.add(sidColumn);

        sidColumn = new HeaderRowInfo("reason", "维修原因", 120);
        HashMap<String, String> hm = CommonUtils.JSONArrayToMap(jar, "tid", "qname");
        sidColumn.setAttr(3);
        sidColumn.setContrastMap(hm);
        rowList.add(sidColumn);

        sidColumn = new HeaderRowInfo("mkdate", "报修时间", 210);
        rowList.add(sidColumn);

        sidColumn = new HeaderRowInfo("sorg", "部门", 180);
        rowList.add(sidColumn);

        return rowList;
    }



    /***
     * 初始化表格
     */
    private void initTableView() {
        dataList = new ArrayList();
        List<HeaderRowInfo> header = getRowDataListSbwxqr();
        dataListViewContent.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        commChoice = new CommMultiChoiceModeCallBack(this, dataListViewContent, dataList);//添加选择器
        dataListViewContent.setMultiChoiceModeListener(commChoice);
        adapter = new BaseTableDataAdapter(this, tableView, dataListViewContent, dataList, header);
        adapter.setTitle("项次");
        adapter.setTitleHeight(100);
        adapter.setSwipeRefreshEnabled(false);
        adapter.setTitleWidth(80);
        tableView.setAdapter(adapter);
    }


    @OnEditorAction({R.id.edt_op, R.id.edt_equipment_no})
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
            CommonUtils.textViewGetFocus(edtSbid);
            return false;
        }
        String sbid = edtSbid.getText().toString().toUpperCase();
        if (TextUtils.isEmpty(sbid)) {
            showMessage("请输入设备号");
            CommonUtils.textViewGetFocus(edtSbid);
            return true;
        }
        if (id == R.id.edt_equipment_no) {
            //检验设备是否处于待维修
            commPresenter.getAssistInfo(CommCL.AID_SBWXQR, "~sbid='" + sbid + "'", 3);


        }
        return true;
    }

    @OnClick({R.id.weixiu_start, R.id.weixiu_stop,R.id.wx_ghpj})
    public void OnClick(View view) {
        int selectNum = 0;
        int id = view.getId();
        switch (id) {
            case R.id.weixiu_start:
                selectNum = dataListViewContent.getCheckedItemCount();//选择条数
                if (selectNum >= 1) {
                    List<JSONObject> list = commChoice.getSelectList();//选择内容
                    List<Integer> selectIndex = commChoice.getSelectIndex();//选择所在列表的位置
                    for(int i=0;i<selectNum;i++){
                         JSONObject object = list.get(i);
                        String sid=object.getString("sid");
                        String schk1=object.getString("schk1");
                        String bgtime= DateUtil.getCurrDateTime(ICL.DF_YMDT);
                        String wxstate=object.getString("wxstate");
                        JSONObject ngInfo =new JSONObject();
                        ngInfo.put("sid",sid);
                       String op =edtOP.getText().toString();
                       if(TextUtils.isEmpty(op)){
                           showError("请输入操作员");
                           return;
                       }else{
                           ngInfo.put("schk1",op);
                       }

                        ngInfo.put("bgtime",bgtime);
                        ngInfo.put("wxstate","1");
                        commPresenter.updateData(CommCL.CELL_ID_E6002A,ngInfo,1);
                    }

                } else {
                    showMessage("请选择一条记录，进行开始维修");
                }
                break;
            case R.id.weixiu_stop:
                selectNum = dataListViewContent.getCheckedItemCount();//选择条数
                if (selectNum >= 1) {
                    List<JSONObject> list = commChoice.getSelectList();//选择内容
                    List<Integer> selectIndex = commChoice.getSelectIndex();//选择所在列表的位置
                    for(int i=0;i<selectNum;i++){
                        JSONObject object = list.get(i);
                        String sid=object.getString("sid");
                        String wxstat=object.getString("wxstate");
                        String sorg=object.getString("sorg");
                        String sbid=object.getString("sbid");
                        JSONObject info =new JSONObject();
                        info.put("sid",sid);
                        info.put("edtime",DateUtil.getCurrDateTime(ICL.DF_YMDT));
                        info.put("wxstate","2");
                        if(TextUtils.equals(wxstat,"0")){
                            showError("请点开始维修");
                            return;
                        }
                        String rm=remark.getText().toString();
                        if(TextUtils.equals(sorg,"05010000")){//05010000
                            if(TextUtils.isEmpty(rm)){
                                showError("请填写故障描述");
                                return;
                            }
                        }
                        info.put("remark",rm);
                        commPresenter.updateData(CommCL.CELL_ID_E6002B,info,2);
                        JSONObject sbmanage=new JSONObject();
                        sbmanage.put("id",sbid);
                        sbmanage.put("sbstate","3");
                        commPresenter.updateData(CommCL.CELL_ID_B0003B,sbmanage,5);

                    }
                }else {
                    showMessage("请选择一条记录，进行结束维修");
                }
                break;
            case R.id.wx_ghpj:
                selectNum = dataListViewContent.getCheckedItemCount();//选择条数
                if (selectNum == 1) {
                    List<JSONObject> list = commChoice.getSelectList();//选择内容
                    List<Integer> selectIndex = commChoice.getSelectIndex();//选择所在列表的位置
                    JSONObject object=list.get(0);
                    String sid=object.getString("sid");
                    String cont="~sid='"+sid+"'";
                    //校验是否已填写配件
                    commPresenter.getAssistInfo(CommCL.AID_WXGHPJ,cont,4);
                    HashMap<String, Serializable> hm = new HashMap<String, Serializable>();
                    MESPRecord mp=JSONObject.toJavaObject(object,MESPRecord.class);
                    hm.put(CommCL.JUMP_KEY_MESPRecord,mp);
                    jumpNextActivity(WxGhpjActivity.class, hm);
                }else{
                    showMessage("请选择一条记录，进行填写配件");
                }
                break;

        }

    }

    /***
     * 可选择菜单必须实现这个
     * 表格Item点击事件
     * @param parent
     * @param view
     * @param position
     * @param id
     */
    @OnItemClick(R.id.data_list_content)
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        switch (dataListViewContent.getChoiceMode()) {
            case ListView.CHOICE_MODE_NONE:
                return;
            case ListView.CHOICE_MODE_SINGLE:
                //单选
                boolean bSelect = view.isSelected();
                if (!bSelect)
                    dataListViewContent.setItemChecked(position, !bSelect);
                return;
            case ListView.CHOICE_MODE_MULTIPLE_MODAL:
            case ListView.CHOICE_MODE_MULTIPLE:
                bSelect = view.isSelected();
                dataListViewContent.setItemChecked(position, !bSelect);
        }
        boolean bSelect = view.isSelected();
        dataListViewContent.setItemChecked(position, !bSelect);
//        Toast.makeText(CommGJActivity.this, "你选中的position为：" + position, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void commCallBack(Boolean bok, JSONObject info, String error, int key) {

    }

    @Override
    public void commCallBack(Boolean bok, JSONObject info, String error, String key) {

    }

    @Override
    public void getAssistInfoBack(Boolean bok, JSONArray info, String error, int key) {

        if (bok) {
            if(key==3){
                adapter.clear();
            }else if(key==4){
                JSONObject record = info.getJSONObject(0);
                String name1=record.getString("pjname1");
                int num1=record.getInteger("pjnum1");
                if(!TextUtils.isEmpty(name1)){
                    showError("已填写配件");
                    return;
                }
            }else if(key==6){
                jar=info;
                initTableView();
                String ws=GETZCWXYY+"";
                String time="2019-01-01";
                String sorg= BaseApplication.currUser.getDeptCode();
                String sorgCont=(TextUtils.equals(sorg,"0"))?"":" and sorg='"+sorg+"'";
                String cont="~wxstate<'"+ws+"' and  mkdate>'"+time+"'"+sorgCont;
                commPresenter.getAssistInfo(CommCL.AID_SBWXQR2, cont, GETZCKEY);
                return;
            }
            //放到扫描的列表中
            for (int i = 0; i < info.size(); i++) {
                JSONObject record = info.getJSONObject(i);
                //页面缓存维修设备编号sid，不是设备号sbuid
               // bh.put(record.getString("sid"),record.getString("sid"));
                // 添加到数据列表
               // adapter.addRecord(record);
                adapter.addRecord(CommonUtils.getJsonObjFromBean(record));

            }




        }
        if(!bok&&key==0){
            showError(error);
        }
    }

    @Override
    public void saveDataBack(Boolean bok, JSONArray info, JSONObject record, String error, int key) {
        if (bok) {
            myClear();
        } else {
            showMessage(error);
        }

    }

    public void myClear() {
        ngInfo = null;
        error1 = "";
        edtOP.setText("");
        edtSbid.setText("");


    }

    @Override
    public void updateDataBack(Boolean bok, JSONArray info, String error, int key) {
        if(bok){
            List<Integer> selectIndex = commChoice.selectIndex;
            //boolean bDone = CommCL.BATCH_STATUS_DONE.equals(error);
           if(key==1){
                for(int i=0;i<selectIndex.size();i++){
                    JSONObject obj = dataList.get(selectIndex.get(i));
                    obj.put("wxstate",1);
                    dataList.set(selectIndex.get(i),obj);
                    //更改PC端设备管理的设备状态
                    String id=obj.getString("sbid");
                    JSONObject sbmanage =new JSONObject();
                    sbmanage.put("id",id);
                    sbmanage.put("sbstate","2");
                    commPresenter.updateData(CommCL.CELL_ID_B0003B,sbmanage,5);
                }

                commChoice.clearChoice();
                adapter.notifyDataSetChanged();

               }else if ( key==2){

               commChoice.clearChoice();
               adapter.notifyDataSetChanged();
                   num++;
                   if(num==selectIndex.size()){
                       adapter.clear();
                       commPresenter.getAssistInfo(CommCL.AID_SBWXQR, "", GETZCKEY);
                       num=0;
               }
//               commChoice.clearChoice();
//               adapter.notifyDataSetChanged();

            }


        }

    }

    @Override
    public void changeRecordStateBack(Boolean bok, JSONArray info, String error, int key) {

    }

    @Override
    public void onRemoteFailed(String message) {
        hideLoading();
       CommonUtils.showError(this, "onRemoteFailed=" + message);
    }

    /*---------------工具方法----------*/
    public void showError(String message) {
        CommonUtils.showError(this, message);
    }

    public void showMessage(String message) {
        CommonUtils.showMessage(this, message);
    }

    public void showSuccess(String message) {
        CommonUtils.showSuccess(this, message);
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
}
