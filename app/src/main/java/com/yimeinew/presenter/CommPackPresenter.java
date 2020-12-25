package com.yimeinew.presenter;

import android.text.TextUtils;
import android.util.Log;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.shade.com.alibaba.fastjson.JSONArray;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.shade.com.alibaba.fastjson.JSONObject;
import com.yimeinew.data.MESPRecord;
import com.yimeinew.data.PackHeadInfo;
import com.yimeinew.data.PackInfo;
import com.yimeinew.model.impl.CommZCModel;
import com.yimeinew.modelInterface.CommBaseView;
import com.yimeinew.modelInterface.CommFastView;
import com.yimeinew.modelInterface.CommPackView;
import com.yimeinew.network.response.ResponseTransformer;
import com.yimeinew.network.schedulers.SchedulerProvider;
import com.yimeinew.utils.CommCL;
import com.yimeinew.utils.CommonUtils;
import com.yimeinew.utils.ToolUtils;

import java.util.HashMap;

public class CommPackPresenter {
    private final String TAG_NAME = CommPackPresenter.class.getSimpleName();
    private CommPackView baseView;//通用工站回调接口
    private SchedulerProvider schedulerProvider; //线程池
    private CommZCModel baseModel;//服务端处理接口
    public CommPackPresenter(CommPackView baseStationBindingView, SchedulerProvider schedulerProvider) {
        this.baseView = baseStationBindingView;
        this.schedulerProvider = schedulerProvider;
        if (baseModel == null) {
            baseModel = new CommZCModel();
        }
    }

    /**
     * 按Tray获取Tray和工单信息
     * @param tray
     */
    public void getTrayAndMo(String tray,int key){
        String cont="~sid1='"+tray+"'";
        baseModel.getAssistInfo(CommCL.AID_PACK_TRAY_MO,cont).compose(ResponseTransformer.handleResult())
                .compose(schedulerProvider.applySchedulers()).subscribe(
                carBeans -> {
                    if (carBeans.getIntValue(CommCL.RTN_ID) != 0) {
                        //"获取服务器信息失败" + carBeans.toString()
                        baseView.getTrayAndMoCallBack(false, null,"获取炊盘失败", key);
                    }else if (carBeans.getIntValue(CommCL.RTN_ID) == 0) {
                        JSONObject msg = carBeans.getJSONObject(CommCL.RTN_DATA);//response里面的data，存放的是查询记录
                        JSONObject rtnMap = (JSONObject) msg.get(CommCL.RTN_DATA);//获取实际的查询结果
                        if (rtnMap.getInteger(CommCL.RTN_CODE) == 0) {
                            baseView.getTrayAndMoCallBack(false, null,"没有该炊盘",key);
                        } else {
                            JSONArray array = rtnMap.getJSONArray(CommCL.RTN_VALUES);
                            JSONObject jsonObject = array.getJSONObject(0);
                            String sid = jsonObject.getString("sid");
                            //校验是否可以开工
                            int bok = jsonObject.getInteger("bok");
                            if (CommCL.BOK != bok) {
                                baseView.getTrayAndMoCallBack(false, null, "上一站未出站",key);
                                return;
                            }
                            //校验是否被HOLD
                            int holdID = jsonObject.getInteger("holdid");
                            if (CommCL.HOLD == holdID) {
                                baseView.getTrayAndMoCallBack(false, null, "被HOLD",key);
                                return;
                            }
                            //判断工单是否结案
                            String close_id=jsonObject.getString("close_id");
                            if(TextUtils.equals(close_id,"T")){
                                baseView.getTrayAndMoCallBack(false, null, "工单已结案",key);
                                return;
                            }
                            //校验Tray是否装满
                            int qty = jsonObject.getInteger("qty");
                            int tray_in_qty = jsonObject.getInteger("tray_in_qty");
                            if (tray_in_qty>=qty ) {
                                baseView.getTrayAndMoCallBack(false, null, "已装满",key);
                                return;
                            }
                            //检验满箱数量是否维护
                            int binfullqty=jsonObject.getInteger("binfullqty");
                            if(binfullqty<=0){
                                baseView.getTrayAndMoCallBack(false, null, "请维护满箱数量",key);
                                return;
                            }
                            //检验是否满盘

                            //检验是否超工单

                            String currMO = baseView.getCurrMO();
                            if (key==1&&currMO.length() > 0) {//单key等于检验工单，AB的key={11,22}和尾数key=3
                                if (!currMO.equals(sid)) {//判断工单是否一致。
                                    baseView.getTrayAndMoCallBack(false, null, "工单不一致",key);
                                    return;
                                }
                            }
                            //A料工单校验
                            if(key==11){
                                String slkidA= ToolUtils.parseConstant(currMO).get("slkidA");
                                if(!TextUtils.isEmpty(slkidA)&&!TextUtils.equals(slkidA,sid)){
                                    baseView.getTrayAndMoCallBack(false, null, "A料工单不一致",key);
                                    return;
                                }
                            }
                            //B料工单校验
                            if(key==21){
                                String slkidB= ToolUtils.parseConstant(currMO).get("slkidB");
                                if(!TextUtils.isEmpty(slkidB)&&!TextUtils.equals(slkidB,sid)){
                                    baseView.getTrayAndMoCallBack(false, null, "B料工单不一致",key);
                                    return;
                                }
                            }
                            //尾数装箱
                            if(key==31){
                                String slkid=ToolUtils.parseConstant(currMO).get("slkid");
                                if(!TextUtils.isEmpty(slkid)&&!TextUtils.equals(slkid,sid)){
                                    baseView.getTrayAndMoCallBack(false, null, "工单不一致",key);
                                    return;
                                }
                            }
                            baseView.getTrayAndMoCallBack(true,jsonObject,"",key);
                        }
                    }
                });
    }

    /**
     * 扫喷码
     * @param marking
     * @param key
     */
    public void getMarkingInfo(String marking,JSONObject headinfo,int key){
        String cont="~allcode='"+marking+"'";
        String aid=(key==3||key==4)?CommCL.AID_PACK_MARK2:CommCL.AID_PACK_MARKING;//尾数key=3
        baseModel.getAssistInfo(aid,cont).compose(ResponseTransformer.handleResult())
                .compose(schedulerProvider.applySchedulers()).subscribe(
                carBeans -> {
                    if (carBeans.getIntValue(CommCL.RTN_ID) != 0) {
                        baseView.getMarkingCallBack(false, headinfo,null,"获取喷码失败", key);
                    }else if (carBeans.getIntValue(CommCL.RTN_ID) == 0) {
                        JSONObject msg = carBeans.getJSONObject(CommCL.RTN_DATA);//response里面的data，存放的是查询记录
                        JSONObject rtnMap = (JSONObject) msg.get(CommCL.RTN_DATA);//获取实际的查询结果
                        if (rtnMap.getInteger(CommCL.RTN_CODE) == 0) {
                            baseView.getMarkingCallBack(false, headinfo,null,"喷码不存在",key);
                        } else {
                            JSONArray array = rtnMap.getJSONArray(CommCL.RTN_VALUES);
                            JSONObject jsonObject = array.getJSONObject(0);
                            String mono = jsonObject.getString("mono");

                            //校验是否被HOLD
                            int holdID = jsonObject.getInteger("holdid");
                            if (CommCL.HOLD == holdID) {
                                baseView.getMarkingCallBack(false, headinfo,null, "已HOLD",key);
                                return;
                            }
                            //检验生产状态
                            String state=jsonObject.getString("state");
                            if(TextUtils.equals(state,CommCL.BATCH_STATUS_READY)){//00状态
                                baseView.getMarkingCallBack(false, headinfo,null, "卡板未过站",key);
                                return;
                            }
                            if(TextUtils.equals(state,CommCL.BATCH_STATUS_DONE)){//04状态
                                baseView.getMarkingCallBack(false, headinfo,null, "已经包装",key);
                                return;
                            }
                            if(TextUtils.equals(state,CommCL.BATCH_STATUS_ABNORMAL)){//0A状态
                                baseView.getMarkingCallBack(false, headinfo,null, "打叉板",key);
                                return;
                            }
                            //维修判断
                            int wxnum=jsonObject.getInteger("wxnum");//维修次数
                            int scstate=jsonObject.getInteger("scstate");//生产确认
                            int qcstate=jsonObject.getInteger("qcstate");//品质确认
                            if(wxnum!=scstate){
                                if(wxnum-scstate==1) {
                                    baseView.getMarkingCallBack(false, headinfo, null, "维修品生产未确认", key);
                                }else{
                                    baseView.getMarkingCallBack(false, headinfo, null, "维修品生产判定NG", key);
                                }
                                return;
                            }
                            if(wxnum!=qcstate){
                                if(wxnum-qcstate==1) {
                                    baseView.getMarkingCallBack(false, headinfo, null, "维修品品质未确认", key);
                                }else{
                                    baseView.getMarkingCallBack(false, headinfo, null, "维修品品质判定NG", key);
                                }
                                return;
                            }
                            //检验是否满盘
                            //检验是否超工单
                            //判定
                            String currMO = baseView.getCurrMO();
                            if (key==1) {//单key等于检验工单，AB的key=2和AB料尾数key=3
                                if (!TextUtils.equals(currMO,mono)) {//判断工单是否一致。
                                    baseView.getMarkingCallBack(false, headinfo,null, "工单不一致",key);
                                    return;
                                }
                            }
                            if(key==2){//AB料
                                HashMap<String,String> hm=ToolUtils.parseConstant(currMO);
                                if(!TextUtils.equals(mono,hm.get("slkidA"))&&!TextUtils.equals(mono,hm.get("slkidB"))){
                                    baseView.getMarkingCallBack(false, headinfo,null, "工单不一致",key);
                                    return;
                                }
                            }
                            String prd_no=jsonObject.getString("prd_no");
                            String prd_mark=jsonObject.getString("prd_mark");
                            if(key==3){//AB料尾数
                                HashMap<String,String> hm=ToolUtils.parseConstant(currMO);

                                //货品校验
                                if(!TextUtils.equals(prd_no,hm.get("prd_noA"))&&!TextUtils.equals(prd_no,hm.get("prd_noB"))){
                                    baseView.getMarkingCallBack(false, headinfo,null, "货品不一致",key);
                                    return;
                                }

                                //bincode
                                if(!TextUtils.equals(prd_mark,hm.get("prd_markA"))&&!TextUtils.equals(prd_mark,hm.get("prd_markB"))){
                                    baseView.getMarkingCallBack(false, headinfo,null, "并级不一致",key);
                                    return;
                                }
                                if(!( TextUtils.equals(prd_no,hm.get("prd_noA")) && TextUtils.equals(prd_mark,hm.get("prd_markA")) )&&!( TextUtils.equals(prd_no,hm.get("prd_noB")) && TextUtils.equals(prd_mark,hm.get("prd_markB")) )){
                                    baseView.getMarkingCallBack(false, headinfo,null, "货品或并级不一致",key);
                                    return;
                                }
                            }
                            if(key==4){//尾数
                                HashMap<String,String> hm=ToolUtils.parseConstant(currMO);
                                if(!(TextUtils.equals(prd_no,hm.get("prd_no")) && TextUtils.equals(prd_mark,hm.get("prd_mark"))) ){
                                    baseView.getMarkingCallBack(false, headinfo,null, "货品或并级不一致",key);
                                    return;
                                }
                            }
                            if(key==5){//备品
                                if(!TextUtils.isEmpty(currMO)&&!TextUtils.equals(currMO,mono)){
                                    baseView.getMarkingCallBack(false, headinfo,null, "工单不一致",key);
                                    return;
                                }
                            }

                            baseView.getMarkingCallBack(true,headinfo,jsonObject,"",key);
                        }
                    }
                });

    }

    /**
     * 保存表头
     * @param record
     * @param body  表身
     * @param insObject
     * @param key
     */
    public void savePackHeadInfo(PackHeadInfo record,JSONObject body, String insObject, int key){
        JSONObject recordObj=CommonUtils.getJsonObjFromBean(record);
        baseModel.comSaveData(record,insObject).compose(ResponseTransformer.handleResult())
                .compose(schedulerProvider.applySchedulers()).subscribe(
                jsonValues -> {
                    Log.i(TAG_NAME, jsonValues.toJSONString());
                    if (jsonValues.getIntValue(CommCL.RTN_ID) != 0) {
                        baseView.saveMoCallBack(false,recordObj,null, "保存表头失败",key);
                    } else if (jsonValues.getIntValue(CommCL.RTN_ID) == 0) {
                        JSONObject saveBack = jsonValues.getJSONObject(CommCL.RTN_DATA);
                        String lotno = saveBack.getString("lotno");
                        record.setLotno(lotno);
                        baseView.saveMoCallBack(true, CommonUtils.getJsonObjFromBean(record),body, null,key);
                    }
                },
                throwable -> {
                    baseView.onRemoteFailed(throwable.getMessage());
                });

    }

    /**
     * 保存表身
     * @param headObj
     * @param record
     * @param insObject
     * @param key
     */
    public void savePackInfo(JSONObject headObj,PackInfo record, String insObject, int key){
        JSONObject recordObj=CommonUtils.getJsonObjFromBean(record);
        baseModel.comSaveData(record,insObject).compose(ResponseTransformer.handleResult())
                .compose(schedulerProvider.applySchedulers()).subscribe(
                jsonValues -> {
                    Log.i(TAG_NAME, jsonValues.toJSONString());
                    if (jsonValues.getIntValue(CommCL.RTN_ID) != 0) {
                        baseView.saveMarkingCallBack(false,headObj,recordObj,"保存表身失败",key);
                    } else if (jsonValues.getIntValue(CommCL.RTN_ID) == 0) {
                        baseView.saveMarkingCallBack(true, headObj,recordObj, null,key);
                    }
                },
                throwable -> {
                    baseView.onRemoteFailed(throwable.getMessage());
                });

    }

    /**
     * 更新信息
     * @param record
     * @param insObject
     * @param key
     */
    public void updateInfo(Object record, String insObject, int key){
        JSONObject recordObj=CommonUtils.getJsonObjFromBean(record);
        baseModel.comUpdateData(record,insObject).compose(ResponseTransformer.handleResult())
                .compose(schedulerProvider.applySchedulers()).subscribe(
                jsonValues -> {
                    Log.i(TAG_NAME, jsonValues.toJSONString());
                    if (jsonValues.getIntValue(CommCL.RTN_ID) != 0) {
                        baseView.updateCallBack(false,recordObj,"更新失败",key);
                    } else if (jsonValues.getIntValue(CommCL.RTN_ID) == 0) {
                        baseView.updateCallBack(true, recordObj, null,key);
                    }
                },
                throwable -> {
                    baseView.onRemoteFailed(throwable.getMessage());
                });
    }
    public void getPackInfoById(String id,String sbuid,int key){
        String cont="~lotno='"+id+"' and sbuid='"+sbuid+"'";
        baseModel.getAssistInfo(CommCL.AID_PACK_Get_INFO,cont).compose(ResponseTransformer.handleResult())
                .compose(schedulerProvider.applySchedulers()).subscribe(
                carBeans -> {
                    if (carBeans.getIntValue(CommCL.RTN_ID) != 0) {
                        baseView.getPackInfoCallBack(false, null,null,"获取包装信息失败", key);
                    }else if (carBeans.getIntValue(CommCL.RTN_ID) == 0) {
                        JSONObject msg = carBeans.getJSONObject(CommCL.RTN_DATA);//response里面的data，存放的是查询记录
                        JSONObject rtnMap = (JSONObject) msg.get(CommCL.RTN_DATA);//获取实际的查询结果
                        if (rtnMap.getInteger(CommCL.RTN_CODE) == 0) {
                            baseView.getPackInfoCallBack(false, null,null,"包装信息不存在",key);
                        } else {
                            JSONArray array = rtnMap.getJSONArray(CommCL.RTN_VALUES);
                            if(array.size()<=0){
                                baseView.getPackInfoCallBack(false, null,null,"包装信息不存在",key);
                                return;
                            }
                            JSONObject jsonObject = array.getJSONObject(0);
                            //校验是否被HOLD
                            int holdID = jsonObject.getInteger("holdid");
                            if (CommCL.HOLD == holdID) {
                                baseView.getPackInfoCallBack(false, null,null, "该箱号已经HOLD",key);
                                return;
                            }
                            JSONArray arr=new JSONArray();
                            for(int i=0;i<array.size();i++){
                                JSONObject temp=array.getJSONObject(i);
                                String lotno=temp.getString("lotno");
                                String sid1=temp.getString("sid1");
                                String slkid=temp.getString("slkid1");
                                String tray=temp.getString("tray");
                                int minqty=temp.getInteger("minqty1");
                                int qty=temp.getInteger("qty1");
                                PackInfo packInfo=new PackInfo(lotno,sid1,slkid,tray,minqty);
                                packInfo.setQty(qty);
                                arr.add(packInfo);
                            }
                            baseView.getPackInfoCallBack(true,jsonObject,arr,"",key);
                        }
                    }
                },
                throwable -> {
                    baseView.onRemoteFailed(throwable.getMessage());
                });
    }
    public void getABPackInfoById(String rslkid,String sbuid,int key){//AB料包装数据恢复
        String cont="~rslkid='"+rslkid+"' and sbuid='"+sbuid+"'";
        baseModel.getAssistInfo(CommCL.AID_PACK_Get_INFO,cont).compose(ResponseTransformer.handleResult())
                .compose(schedulerProvider.applySchedulers()).subscribe(
                carBeans -> {
                    if (carBeans.getIntValue(CommCL.RTN_ID) != 0) {
                        baseView.getABPackInfoCallBack(false, null,null,null,null,"获取包装信息失败", key);
                    }else if (carBeans.getIntValue(CommCL.RTN_ID) == 0) {
                        JSONObject msg = carBeans.getJSONObject(CommCL.RTN_DATA);//response里面的data，存放的是查询记录
                        JSONObject rtnMap = (JSONObject) msg.get(CommCL.RTN_DATA);//获取实际的查询结果
                        if (rtnMap.getInteger(CommCL.RTN_CODE) == 0) {
                            baseView.getABPackInfoCallBack(false, null,null,null,null,"包装信息不存在",key);
                        } else {
                            JSONArray array = rtnMap.getJSONArray(CommCL.RTN_VALUES);
                            if(array.size()<=0){
                                baseView.getABPackInfoCallBack(false, null,null,null,null,"包装信息不存在",key);
                                return;
                            }
                            String moa="";String mob="";
                            JSONObject obja=new JSONObject();JSONObject objb=new JSONObject();
                            JSONArray arr_a=new JSONArray();
                            JSONArray arr_b=new JSONArray();
                            for(int i=0;i<array.size();i++){
                                JSONObject temp=array.getJSONObject(i);
                                String mo_no=temp.getString("slkid");
                                if(TextUtils.isEmpty(moa)&&!TextUtils.equals(mob,mo_no)){
                                    moa=mo_no;
                                    obja=temp;
                                }
                                if(TextUtils.isEmpty(mob)&&!TextUtils.equals(moa,mo_no)){
                                    mob=mo_no;
                                    objb=temp;
                                }
                                String lotno=temp.getString("lotno");
                                String sid1=temp.getString("sid1");
                                String slkid=temp.getString("slkid1");
                                String tray=temp.getString("tray");
                                int minqty=temp.getInteger("minqty1");
                                int qty=temp.getInteger("qty1");
                                PackInfo packInfo=new PackInfo(lotno,sid1,slkid,tray,minqty);
                                packInfo.setQty(qty);
                                if(TextUtils.equals(mo_no,moa)){//分配A工单
                                    arr_a.add(packInfo);
                                }
                                if(TextUtils.equals(mo_no,mob)){//分配B工单
                                    arr_b.add(packInfo);
                                }
                            }
                            //AB工单同时存在才能恢复数据
                            if(TextUtils.isEmpty(moa)||TextUtils.isEmpty(mob)){
                                baseView.getABPackInfoCallBack(false, null,null,null,null, "可能只有一半的数据系统不给恢复",key);
                                return;
                            }
                            //校验是否被HOLD
                            int holdID = obja.getInteger("holdid");
                            if (CommCL.HOLD == holdID) {
                                baseView.getABPackInfoCallBack(false, null,null,null,null, "该箱号已经HOLD",key);
                                return;
                            }
                            holdID = objb.getInteger("holdid");
                            if (CommCL.HOLD == holdID) {
                                baseView.getABPackInfoCallBack(false, null,null,null,null, "该箱号已经HOLD",key);
                                return;
                            }
                            baseView.getABPackInfoCallBack(true,obja,arr_a,objb,arr_b,"",key);
                        }
                    }
                },
                throwable -> {
                    baseView.onRemoteFailed(throwable.getMessage());
                });
    }
    public void getMoInfoById(String slkid,int key){
        String cont="~slkid='"+slkid+"'";
        baseModel.getAssistInfo(CommCL.AID_PACK_ALREADY,cont).compose(ResponseTransformer.handleResult())
                .compose(schedulerProvider.applySchedulers()).subscribe(
                carBeans -> {
                    if (carBeans.getIntValue(CommCL.RTN_ID) != 0) {
                        baseView.getMoInfoCallBack(false, null,"获取包装信息失败21", key);
                    }else if (carBeans.getIntValue(CommCL.RTN_ID) == 0) {
                        JSONObject msg = carBeans.getJSONObject(CommCL.RTN_DATA);//response里面的data，存放的是查询记录
                        JSONObject rtnMap = (JSONObject) msg.get(CommCL.RTN_DATA);//获取实际的查询结果
                        if (rtnMap.getInteger(CommCL.RTN_CODE) == 0) {
                            baseView.getMoInfoCallBack(false,null,"包装信息不存在22",key);
                        } else {
                            JSONArray array = rtnMap.getJSONArray(CommCL.RTN_VALUES);
                            if(array.size()<=0){
                                baseView.getMoInfoCallBack(false, null,"包装信息不存在23",key);
                                return;
                            }
                            baseView.getMoInfoCallBack(true,array.getJSONObject(0),"",key);
                        }
                    }
                });
    }

    public void printLable(HashMap<String,String> hm,int key){

        baseModel.doPrintServer(hm).compose(ResponseTransformer.handleResult())
                .compose(schedulerProvider.applySchedulers()).subscribe(
                carBeans -> {
                    if (carBeans.getIntValue(CommCL.RTN_ID) != 0) {
                        baseView.getPrintLableCallBack(false, null,"获取包装信息失败21", key);
                    }else if (carBeans.getIntValue(CommCL.RTN_ID) == 0) {
                        JSONObject msg = carBeans.getJSONObject(CommCL.RTN_DATA);//response里面的data，存放的是查询记录
                        JSONObject rtnMap = msg.getJSONObject(CommCL.RTN_DATA);//获取实际的查询结果
                        System.out.println("woshi"+rtnMap.toJSONString());
                        baseView.getPrintLableCallBack(rtnMap.getBoolean("bok"),rtnMap,rtnMap.getString("error"),key);
                    }
                });
    }
    /**检验是否AB料*/
    public void checkABMO(String slkidA,String slkidB,int key){
        String cont="~asid='"+slkidA+"' and bsid='"+slkidB+"'";
        baseModel.getAssistInfo(CommCL.AB_MO_CHECK,cont).compose(ResponseTransformer.handleResult())
                .compose(schedulerProvider.applySchedulers()).subscribe(
                carBeans -> {
                    if (carBeans.getIntValue(CommCL.RTN_ID) != 0) {
                        baseView.commCallBack(false, null,"获取AB料信息失败", key);
                    }else if (carBeans.getIntValue(CommCL.RTN_ID) == 0) {
                        JSONObject msg = carBeans.getJSONObject(CommCL.RTN_DATA);//response里面的data，存放的是查询记录
                        JSONObject rtnMap = (JSONObject) msg.get(CommCL.RTN_DATA);//获取实际的查询结果
                        if (rtnMap.getInteger(CommCL.RTN_CODE) == 0) {
                            baseView.commCallBack(false,null,"没有维护AB料关系",key);
                        } else {
                            JSONArray array = rtnMap.getJSONArray(CommCL.RTN_VALUES);
                            if(array.size()<=0){
                                baseView.commCallBack(false, null,"没有维护AB料关系1",key);
                                return;
                            }
                            baseView.commCallBack(true,array.getJSONObject(0),"",key);
                        }
                    }
                },
                throwable -> {
                    baseView.onRemoteFailed(throwable.getMessage());
                });
    }
}
