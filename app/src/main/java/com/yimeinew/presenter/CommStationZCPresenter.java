package com.yimeinew.presenter;

import android.text.TextUtils;
import android.util.Log;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.shade.com.alibaba.fastjson.JSONArray;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.shade.com.alibaba.fastjson.JSONObject;
import com.yimeinew.activity.base.BaseApplication;
import com.yimeinew.data.*;
import com.yimeinew.data.qc.QCBatchInfo;
import com.yimeinew.model.impl.CommZCModel;
import com.yimeinew.modelInterface.BaseStationBindingView;
import com.yimeinew.network.response.ResponseTransformer;
import com.yimeinew.network.schedulers.SchedulerProvider;
import com.yimeinew.utils.CommCL;
import com.yimeinew.utils.CommonUtils;
import com.yimeinew.utils.DateUtil;
import com.yimeinew.utils.ICL;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * @Auther: fengzejiang1987@163.com
 * @Date : 2018/12/18 18:15
 */
public class CommStationZCPresenter {
    private final String TAG_NAME = CommStationZCPresenter.class.getSimpleName();
    private BaseStationBindingView baseView;//通用工站回调接口
    private SchedulerProvider schedulerProvider; //线程池
    private CommZCModel baseModel;//服务端处理接口
    public HashMap<String,String> cache=new HashMap<String,String>();

    public CommStationZCPresenter(BaseStationBindingView baseStationBindingView, SchedulerProvider schedulerProvider) {
        this.baseView = baseStationBindingView;
        this.schedulerProvider = schedulerProvider;
        if (baseModel == null) {
            baseModel = new CommZCModel();
        }

    }



    /***
     *  绑定料盒号
     * @param boxNo 料盒号
     * @param sid1 批次号
     * @param zcNo 制成号
     */
    public void bindingBox(String boxNo, String sid1, String zcNo) {
        baseModel.bindBox(boxNo, sid1, zcNo, true).compose(ResponseTransformer.handleResult())
                .compose(schedulerProvider.applySchedulers())
                .subscribe(jsonObj -> {
                    Log.i(TAG_NAME, jsonObj.toJSONString());
                }, throwable -> {
                    Log.i(TAG_NAME, throwable.getLocalizedMessage());
                    baseView.onRemoteFailed(throwable.getLocalizedMessage());
                });
    }

    public void checkJiaoShuiGuoQi(String sbid){

    }



    /***
     * 获取相关制成的批次信息
     * @param sid1 批次号
     * @param zcNo 制成号
     */
    public void getBatchInfo(String sid1, String zcNo,EquipmentInfo currEquipment) {
        /*因为点胶烘烤改进太多，所以另起一个辅助。*/
        baseModel.getBatchRecordBySid1AndZcNo(sid1, zcNo).compose(ResponseTransformer.handleResult())
                .compose(schedulerProvider.applySchedulers()).subscribe(
                carBeans -> {
                    if (carBeans.getIntValue(CommCL.RTN_ID) != 0) {
                        baseView.checkSidCallBack(false, null, "获取服务器信息失败" + carBeans.toString());
                    } else {
                        JSONObject msg = carBeans.getJSONObject(CommCL.RTN_DATA);//response里面的data，存放的是查询记录
                        JSONObject rtnMap = (JSONObject) msg.get(CommCL.RTN_DATA);//获取实际的查询结果
                        if (rtnMap.getInteger(CommCL.RTN_CODE) == 0) {
                            baseView.checkSidCallBack(false, null, "在该工序【" + zcNo + "】没有查到【" + sid1 + "】批次号！");
                        } else {
                            JSONArray jsonArray = (JSONArray) rtnMap.get(CommCL.RTN_VALUES);
                            JSONObject jsonObject = jsonArray.getJSONObject(0);
                            Log.i(TAG_NAME, jsonObject.toJSONString());
                            //校验是否可以开工
                            int bok = jsonObject.getInteger("bok");
                            if (CommCL.BOK != bok) {
                                baseView.checkSidCallBack(false, null, "该批次【" + sid1 + "】上一工序未出站，不能入站！");
                                return;
                            }
                            //校验是否被HOLD
                            int holdID = jsonObject.getInteger("holdid");
                            if (CommCL.HOLD == holdID) {
                                baseView.checkSidCallBack(false, null, "该批次【" + sid1 + "】已经HOLD，不能入站！");
                                return;
                            }
                            //校验状态
                            String stateValue = jsonObject.getString("state");
                            if (CommCL.BATCH_STATUS_DONE.equals(stateValue)) {
                                baseView.checkSidCallBack(false, null, "该批次【" + sid1 + "】已经完工！");
                                return;
                            }
                            if (CommCL.BATCH_STATUS_WORKING.equals(stateValue)) {
                                baseView.checkSidCallBack(false, null, "该批次【" + sid1 + "】正在生产中,不能再次入站！");
                                return;
                            }
                            if (CommCL.BATCH_STATUS_CHARGING.equals(stateValue)) {
                                baseView.checkSidCallBack(false, null, "该批次【" + sid1 + "】已经上料,不能再次入站！");
                                return;
                            }
                            if (CommCL.BATCH_STATUS_IN.equals(stateValue)) {
                                baseView.checkSidCallBack(false, null, "该批次【" + sid1 + "】已经别的机台入站,不能再次入站！");
                                return;
                            }
                            String currMO = baseView.getCurrMO();
                            String sid = jsonObject.getString("sid");
                            if (currMO.length() > 0) {
                                if (!currMO.equals(sid)&&!"41".equals(zcNo)&&!"1A".equals(zcNo)&&!"1B".equals(zcNo)) {
                                    baseView.checkSidCallBack(false, null, "当前工单是【" + currMO + "】,扫描的工单是【" + sid + "】");
                                    return;
                                }
                            }
                            //如果是固晶1就验证料盒绑支架-是否支架预热
                            if("11".equals(zcNo)){
                                String mbox=cache.get(sid1);
                                String mboxzj= cache.get(mbox+"_prd_no");
                                String mozj=jsonObject.getString("zjprd_no");
                                String msbid=cache.get(mbox+"_sbid");
                                String mprogram=cache.get(mbox+"_program");
                                if(!cache.containsKey(mbox+"_prd_no")){
                                    baseView.checkMboxCallBack(false,"手输料盒号需要回车",1);
                                    return;
                                }
                                if(!TextUtils.equals(mboxzj,mozj)){
                                    baseView.checkMboxCallBack(false,  "当前工单支架是【" + mozj + "】,扫描的料盒绑定支架是【" + mboxzj + "】",1);
                                    return;
                                }
//                                if(TextUtils.isEmpty(msbid)||TextUtils.isEmpty(mprogram)){
//                                    baseView.checkMboxCallBack(false,  "当前料盒没做支架预热！",1);
//                                    return;
//                                }
                                cache.remove(mbox+"_prd_no");//这个料盒好使用后，清除缓存
                                cache.remove(mbox+"_sbid");
                                cache.remove(mbox+"_program");
                                if(cache.containsKey(sid1)) {
                                    cache.remove(sid1);
                                }
                            }
                            //固晶烘烤检验支架和胶水
                            if(TextUtils.equals("1A",zcNo)||TextUtils.equals("1B",zcNo)){
                                String MZHIJ=jsonObject.getString("mzhij");
                                //stype=0只有固晶烘烤1，烤的是绝缘胶
                                //stype=1固晶烘烤1是银胶，固晶烘烤2是绝缘胶
                                String jueyuanjiao=jsonObject.getString("jueyuanjiao");
                                String yinjiao=jsonObject.getString("yinjiao");
                                String stype=jsonObject.getString("stype");
                                if(!TextUtils.isEmpty(currEquipment.getStents())&&!CommonUtils.contentEquals(currEquipment.getStents(),MZHIJ,";")){
                                    baseView.checkSidCallBack(false, null,"当前烤箱支架是【" + currEquipment.getStents() + "】,扫描批次绑定支架是【" + MZHIJ + "】");
                                    return;
                                }
                                if(TextUtils.equals("0",stype)){
                                    if(!TextUtils.isEmpty(currEquipment.getGlue())&&!CommonUtils.contentEquals(currEquipment.getGlue(),jueyuanjiao,";")){
                                        baseView.checkSidCallBack(false,null , "当前烤箱胶水是【" + currEquipment.getGlue() + "】,扫描批次绑定胶水是【" + jueyuanjiao + "】");
                                        return;
                                    }
                                }
                                else if(TextUtils.equals("1",stype)){
                                    if (TextUtils.equals("1A",zcNo)&&!TextUtils.isEmpty(currEquipment.getGlue()) && !CommonUtils.contentEquals(currEquipment.getGlue(), yinjiao, ";")) {
                                        baseView.checkSidCallBack(false, null, "当前烤箱胶水是【" + currEquipment.getGlue() + "】,扫描批次绑定胶水是【" + yinjiao + "】");
                                        return;
                                    }
                                    if (TextUtils.equals("1B",zcNo)&&!TextUtils.isEmpty(currEquipment.getGlue()) && !CommonUtils.contentEquals(currEquipment.getGlue(), jueyuanjiao, ";")) {
                                        baseView.checkSidCallBack(false, null, "当前烤箱胶水是【" + currEquipment.getGlue() + "】,扫描批次绑定胶水是【" + jueyuanjiao + "】");
                                        return;
                                    }
                                }
                            }
                            //焊线判断plasma清洗(焊接)是否超过6小时,（当前是焊线，且上一站是焊线清洗）
                            String zcno0=jsonObject.getString("zcno0");
                            if((TextUtils.equals("21",zcNo)||TextUtils.equals("22",zcNo))&&TextUtils.equals("1P",zcno0)){
                                String qxTime=jsonObject.getString("edate");
                                int subTime=DateUtil.subSecond(DateUtil.getNowCurrDateTime(),qxTime);
                                if(subTime>CommCL.HANXIAN_QINGXI_MAX_WAIT_TIME){
                                    baseView.checkSidCallBack(false,null,"plasma清洗(焊接)时间="+qxTime+"超过6小时");
                                    return;
                                }
                            }
                            //点胶站判断支架预热不超过10小时、清洗不超过6小时
                            if(TextUtils.equals("31",zcNo)){
                                String edate=jsonObject.getString("edate");
                                int subeTime=DateUtil.subSecond(DateUtil.getNowCurrDateTime(),edate);

                                String zcno00=jsonObject.getString("zcno00");
                                String up_date00=jsonObject.getString("up_date00");
                                int subupTime=0;
                                if(!TextUtils.isEmpty(up_date00)) {
                                    subupTime = DateUtil.subSecond(DateUtil.getNowCurrDateTime(), up_date00);
                                }
                                //假如：zcno00='311'支架除湿
                                //假如：zcno0='313'支架清洗
                                if(TextUtils.equals("311",zcno00)&&subupTime>CommCL.DIANJIAO_YURE_MAX_WAIT_TIME){//大于10小时
                                    baseView.checkSidCallBack(false,null,"支架预热(点胶)时间="+up_date00+"超过10小时");
                                    return;
                                }
                                if(TextUtils.equals("311",zcno0)&&subeTime>CommCL.DIANJIAO_YURE_MAX_WAIT_TIME){//大于10小时
                                    baseView.checkSidCallBack(false,null,"支架预热(点胶)时间="+edate+"超过10小时");
                                    return;
                                }
                                if(TextUtils.equals("313",zcno00)&&subupTime>CommCL.HANXIAN_QINGXI_MAX_WAIT_TIME){//大于6小时
                                    baseView.checkSidCallBack(false,null,"plasma清洗(点胶)时间="+up_date00+"超过6小时");
                                    return;
                                }
                                if(TextUtils.equals("313",zcno0)&&subeTime>CommCL.HANXIAN_QINGXI_MAX_WAIT_TIME){//大于6小时
                                    baseView.checkSidCallBack(false,null,"plasma清洗(点胶)时间="+edate+"超过6小时");
                                    return;
                                }
                            }
                            //点胶烘烤判断支架和胶水--这里的胶水改成工单配方的胶水即可
                            if(TextUtils.equals("41",zcNo)||TextUtils.equals("42",zcNo)){
                                String MZHIJ=jsonObject.getString("mzhij");
                                String djabj_prd_no=jsonObject.getString("djhk_jiaoshui");//工单配方：djhk_jiaoshui  工单子表：djabj_prd_no
                                if(!TextUtils.isEmpty(currEquipment.getStents())&&!CommonUtils.contentEquals(currEquipment.getStents(),MZHIJ,";")){
                                    baseView.checkSidCallBack(false, null,"当前烤箱支架是【" + currEquipment.getStents() + "】,扫描批次绑定支架是【" + MZHIJ + "】");
                                    return;
                                }else if(!TextUtils.isEmpty(currEquipment.getGlue())&&!CommonUtils.contentEquals(currEquipment.getGlue(),djabj_prd_no,";")){
                                    baseView.checkSidCallBack(false,null , "当前烤箱胶水是【" + currEquipment.getGlue() + "】,扫描批次绑定胶水是【" + djabj_prd_no + "】");
                                    return;
                                }
                                //时间控制小于1小时不能进烤，大于2小时提示
                                String djTime=jsonObject.getString("edate");
                                if(TextUtils.isEmpty(djTime)){
                                    baseView.checkSidCallBack(false,null,"点胶出站时间为空！");
                                    return;
                                }
                                int subTime=DateUtil.subSecond(DateUtil.getNowCurrDateTime(),djTime);
                                if(subTime<CommCL.DIANJIAO_HONGKAO_MIN_WAIT_TIME){
                                    baseView.checkSidCallBack(false,null,"点胶出站时间="+djTime+"未到达1小时");
                                    return;
                                }else if(subTime>CommCL.DIANJIAO_HONGKAO_MAX_WAIT_TIME){
                                    baseView.checkSidCallBack(true,jsonObject,"点胶出站时间="+djTime+"超过2小时，请在流程单上贴异常标签");
                                    return;
                                }
                            }

                            baseView.checkSidCallBack(true, jsonObject, null);
                            Log.d(TAG_NAME, jsonObject.toJSONString());
                        }
                    }
                },
                throwable -> {
                    baseView.onRemoteFailed(throwable.getMessage());
                }
        );
    }




    /***
     * 获取相关制成的批次信息
     * @param sid1 批次号
     * @param zcNo 制成号
     */
    public void selectXdk(String sid1, String zcNo,EquipmentInfo currEquipment) {
        String cont = "~a.sid1='"+sid1+"' and a.zcno='"+zcNo+"'";
        baseModel.getAssistInfo(CommCL.AID_XDK,cont).compose(ResponseTransformer.handleResult())
                .compose(schedulerProvider.applySchedulers()).subscribe(
                carBeans -> {
                    if (carBeans.getIntValue(CommCL.RTN_ID) != 0) {
                        baseView.checkSidCallBack(false, null, "获取服务器信息失败" + carBeans.toString());
                    } else {
                        JSONObject msg = carBeans.getJSONObject(CommCL.RTN_DATA);//response里面的data，存放的是查询记录
                        JSONObject rtnMap = (JSONObject) msg.get(CommCL.RTN_DATA);//获取实际的查询结果
                        if (rtnMap.getInteger(CommCL.RTN_CODE) == 0) {
                            baseView.checkSidCallBack(false, null, "在该工序【" + zcNo + "】没有查到【" + sid1 + "】批次号！");
                        } else {
                            JSONArray jsonArray = (JSONArray) rtnMap.get(CommCL.RTN_VALUES);
                            JSONObject jsonObject = jsonArray.getJSONObject(0);
                            //Log.i(TAG_NAME, jsonObject.toJSONString());
                            //校验是否可以开工
                            int bok = jsonObject.getInteger("bok");
                            if (CommCL.BOK != bok) {
                                baseView.checkSidCallBack(false, null, "该批次【" + sid1 + "】上一工序未出站，不能入站！");
                                return;
                            }
                            //校验是否被HOLD
                            int holdID = jsonObject.getInteger("holdid");
                            if (CommCL.HOLD == holdID) {
                                baseView.checkSidCallBack(false, null, "该批次【" + sid1 + "】已经HOLD，不能入站！");
                                return;
                            }
                            //校验状态
                            String stateValue = jsonObject.getString("state");
                            if (CommCL.BATCH_STATUS_DONE.equals(stateValue)) {
                                baseView.checkSidCallBack(false, null, "该批次【" + sid1 + "】已经完工！");
                                return;
                            }
                            if (CommCL.BATCH_STATUS_WORKING.equals(stateValue)) {
                                baseView.checkSidCallBack(false, null, "该批次【" + sid1 + "】正在生产中,不能再次入站！");
                                return;
                            }
                            if (CommCL.BATCH_STATUS_CHARGING.equals(stateValue)) {
                                baseView.checkSidCallBack(false, null, "该批次【" + sid1 + "】已经上料,不能再次入站！");
                                return;
                            }
                            if (CommCL.BATCH_STATUS_IN.equals(stateValue)) {
                                baseView.checkSidCallBack(false, null, "该批次【" + sid1 + "】已经别的机台入站,不能再次入站！");
                                return;
                            }
                            String currMO = baseView.getCurrMO();
                            String sid = jsonObject.getString("sid");
                            if (currMO.length() > 0) {
                                if (!currMO.equals(sid)&&!"41".equals(zcNo)&&!"1A".equals(zcNo)&&!"1B".equals(zcNo)) {
                                    baseView.checkSidCallBack(false, null, "当前工单是【" + currMO + "】,扫描的工单是【" + sid + "】");
                                    return;
                                }
                            }
                            //下单颗判断是否委外
                            if(TextUtils.equals("514",zcNo)){
                                int wait_time=jsonObject.getInteger("wait_time");
                                String edate=jsonObject.getString("edate");
                                if(wait_time!=0) {
                                    int subtime = DateUtil.subSecond(DateUtil.getCurrDateTime(ICL.DF_YMDT), edate);
                                    int waitTime = wait_time* 60 * 60;
                                    if (subtime < waitTime) {
                                        baseView.checkSidCallBack(false, null, "委外加工单，还不能下单颗！");return;
                                    }
                                }
                            }
                            baseView.checkSidCallBack(true, jsonObject, null);
                            //Log.d(TAG_NAME, jsonObject.toJSONString());
                        }
                    }
                },
                throwable -> {
                    baseView.onRemoteFailed(throwable.getMessage());
                }
        );
    }


    /***
     * 获取相关制成的设备信息
     * @param sbId 设备编码
     * @param zcNo 制成编码
     */
    public void getEquipmentInfo(String sbId, String zcNo) {
        String zzcno = zcNo;//为了获取设备号信息将制程都改到第一个
        if (zzcno.equals("12") || zzcno.equals("13")|| zzcno.equals("14")) {
            zzcno = "11";
        }
        if (zzcno.equals("1A") || zzcno.equals("1B")) {
            zzcno = "1A";
        }
        if (zzcno.equals("22") ) {
            zzcno = "21";
        }
        if(zzcno.equals("32")){
            zzcno = "31";
        }
        if (zzcno.equals("42")) {
            zzcno = "41";
        }
        baseModel.getEquipmentInf(sbId, zzcno).compose(ResponseTransformer.handleResult())
                .compose(schedulerProvider.applySchedulers())
                .subscribe(jsonObj -> {
                    Log.i(TAG_NAME, jsonObj.toJSONString());
                    if (jsonObj.getIntValue(CommCL.RTN_ID) != 0) {
                        baseView.checkSbIdCallBack(false, null, jsonObj.getString(CommCL.RTN_MESSAGE));
                    } else {
                        JSONObject msg = jsonObj.getJSONObject(CommCL.RTN_DATA);//response里面的data，存放的是查询记录
                        JSONObject rtnMap = (JSONObject) msg.get(CommCL.RTN_DATA);//获取实际的查询结果
                        if (rtnMap.getInteger(CommCL.RTN_CODE) == 0) {
                            baseView.checkSbIdCallBack(false, null, "设备号【" + sbId + "】不存在");
                        } else {
//                            baseView.checkSbIdCallBack(true,null,"");
                            JSONArray array = rtnMap.getJSONArray(CommCL.RTN_VALUES);
                            if (array == null) {
                                baseView.checkSbIdCallBack(false, null, "设备号【" + sbId + "】不存在");
                            } else {
                                //从返回值中获取设备信息
                                JSONObject equipJson = array.getJSONObject(0);
                                EquipmentInfo equipmentInfo = JSONObject.parseObject(equipJson.toJSONString(), EquipmentInfo.class);
                                //获取设备状态,0:设备正常运行，1:设备处于维修当中
                                int state = equipmentInfo.getSbstate();
                                //设备正常运行
                                if (state == CommCL.EQUIPMENT_STATE_OK) {
                                    //一个机台不能再两个制程同时加工。
                                    if(TextUtils.equals("31",zcNo)||TextUtils.equals("32",zcNo)){
                                        String zc_id=equipmentInfo.getZcno();
                                        if(!TextUtils.equals(zc_id,zcNo)){
                                            String zcname="";
                                            for(ZCInfo zi:BaseApplication.zcList){
                                                if(TextUtils.equals(zi.getId(),zc_id)){
                                                    zcname=zi.getName();
                                                }
                                            }
                                            baseView.checkSbIdCallBack(false, null, "该设备在"+zc_id+":"+zcname+"制程,上面还有批次生产，请先出站，再使用该设备");
                                            return;
                                        }
                                    }
                                    baseView.checkSbIdCallBack(true, equipmentInfo, null);
                                } else {
                                    baseView.checkSbIdCallBack(false, null, "设备【" + sbId + "】处于维修中，请联系工程人员！");
                                }
                            }
                        }
                    }
                }, throwable -> {
                    Log.i(TAG_NAME, throwable.getLocalizedMessage());
                    baseView.onRemoteFailed(throwable.getLocalizedMessage());
                });
    }

    /***
     * 获取相关设备上已经绑定的批次信息，不含已经完工的
     * @param sbId 设备编码
     * @param zcNo 制成编码
     */
    public void getRecordBySbId(String sbId, String zcNo) {
        baseModel.getRecordBySbIdAndZcno(sbId, zcNo).compose(ResponseTransformer.handleResult())
                .compose(schedulerProvider.applySchedulers())
                .subscribe(jsonObj -> {
                    Log.i(TAG_NAME, jsonObj.toJSONString());
                    if (jsonObj.getIntValue(CommCL.RTN_ID) != 0) {
                        baseView.checkRecordCallBack(false, null, jsonObj.getString(CommCL.RTN_MESSAGE));
                    } else {
                        int key = 0;
                        JSONObject msg = jsonObj.getJSONObject(CommCL.RTN_DATA);//response里面的data，存放的是查询记录
                        JSONObject rtnMap = (JSONObject) msg.get(CommCL.RTN_DATA);//获取实际的查询结果
                        if (rtnMap.getInteger(CommCL.RTN_CODE) > 0) {
                            JSONArray array = rtnMap.getJSONArray(CommCL.RTN_VALUES);
                            baseView.clear();
                            baseView.addRow(array);
                            key = 1;
                        }
                        baseView.checkRecordCallBack(true, null, null);
                    }
                }, throwable -> {
                    Log.i(TAG_NAME, throwable.getLocalizedMessage());
                    baseView.onRemoteFailed(throwable.getLocalizedMessage());
                });
    }


    /***
     * 获取料盒号信息
     * @param box 料盒号
     */
    public void checkBoxExit(String box,int key) {
        baseModel.getMbox(box).compose(ResponseTransformer.handleResult())
                .compose(schedulerProvider.applySchedulers()).subscribe(
                carBeans -> {
                    if (carBeans.getIntValue(CommCL.RTN_ID) != 0) {
                        baseView.checkMboxCallBack(false, "获取服务器信息失败" + carBeans.toString(),key);
                    } else if (carBeans.getIntValue(CommCL.RTN_ID) == 0) {
                        JSONObject msg = carBeans.getJSONObject(CommCL.RTN_DATA);//response里面的data，存放的是查询记录
                        JSONObject rtnMap = (JSONObject) msg.get(CommCL.RTN_DATA);//获取实际的查询结果
                        if (rtnMap.getInteger(CommCL.RTN_CODE) == 0) {
                            baseView.checkMboxCallBack(false, "没有【" + box + "】料盒号",key);
                        } else {
                            JSONArray array = rtnMap.getJSONArray(CommCL.RTN_VALUES);
                            JSONObject data = array.getJSONObject(0);
//                            int state = data.getInteger("state");

                            cache.put(box+"_prd_no",data.getString("prd_no"));
                            cache.put(box+"_sbid",data.getString("sbid"));
                            cache.put(box+"_program",data.getString("program"));

                            baseView.checkMboxCallBack(true, data.toJSONString(),key);
//                            if (key==1&&state == CommCL.BOX_STATE_WORKING) {
//                                //如果料盒号是使用状态
//                                baseView.checkMboxCallBack(false, "该料盒【" + box + "】在使用中，请更换料盒或修改料盒使用状态！",key);
//                            } else {
//                                baseView.checkMboxCallBack(true, null,key);
//                            }
                        }
                    }
                }
        );
    }


    /***
     * 生产批次生产记录信息
     * @param record 生产记录
     */
    public void makeProRecord(MESPRecord record) {
        baseModel.saveData(record, "3").compose(ResponseTransformer.handleResult())
                .compose(schedulerProvider.applySchedulers()).subscribe(
                jsonValues -> {
                    Log.i(TAG_NAME, jsonValues.toJSONString());
                    if (jsonValues.getIntValue(CommCL.RTN_ID) != 0) {
                        baseView.saveRecordBack(false,null, "获取服务器信息失败" + jsonValues.toString());
                    } else if (jsonValues.getIntValue(CommCL.RTN_ID) == 0) {
                        JSONObject saveBack = jsonValues.getJSONObject(CommCL.RTN_DATA);
                        String sid = saveBack.getString("sid");
                        record.setSid(sid);
                        baseView.saveRecordBack(true, record, null);
                    }
                },
                throwable -> {
                    baseView.onRemoteFailed(throwable.getMessage());
                });
    }

    /**
     * 280修改生产批次状态（前端）
     * @param record
     * @param batchStatusIn
     */
    public void changeLotStateOneByOne(MESPRecord record, String batchStatusIn){
        JSONObject jsonObject = new JSONObject();
        String zcno=record.getZcno();
        if(TextUtils.equals("514",zcno)){//下单颗
            jsonObject.put(CommCL.COMM_OLD_STATE_FLD, CommCL.BATCH_STATUS_READY);
        }else{
            jsonObject.put(CommCL.COMM_OLD_STATE_FLD, CommCL.BATCH_STATUS_IN.equals(batchStatusIn)?"00":record.getState1());
        }
        jsonObject.put(CommCL.COMM_NEW_STATE_FLD, batchStatusIn);
        jsonObject.put(CommCL.COMM_RECORD_SID_FLD, record.getSid1());
        jsonObject.put(CommCL.COMM_SLKID_FLD, record.getSlkid());
        jsonObject.put(CommCL.COMM_ZC_NO_FLD, record.getZcno());
        JSONArray array = new JSONArray();
        array.add(jsonObject);
        baseModel.changeRecordState(array.toJSONString(),CommCL.COMM_MES_UDP_CHANGE_STATE_TESTLOT_VALUE)
                .compose(ResponseTransformer.handleResult())
                .compose(schedulerProvider.applySchedulers()).subscribe(
                jsonValues -> {
                    Log.i(TAG_NAME, jsonValues.toJSONString());
                    if (jsonValues.getIntValue(CommCL.RTN_ID) == -1) {//更改批次，成功返回id=1
                        baseView.changeRecordStateBack(false, record, "获取服务器信息失败" + jsonValues.toString());
                    } else {
                        record.setState1(batchStatusIn);
                        baseView.changeRecordStateBack(true, record, null);
                    }
                },
                throwable -> {
                    baseView.onRemoteFailed("280修改状态"+throwable.getMessage());
                });
    }

    /***
     * 修改生产记录的状态，在服务端内部，自动更新创批批次记录
     * @param record
     * @param batchStatusIn
     */
    public void changeRecordStateOneByOne(MESPRecord record, String batchStatusIn) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(CommCL.COMM_OLD_STATE_FLD, record.getState1());
        jsonObject.put(CommCL.COMM_NEW_STATE_FLD, batchStatusIn);
        jsonObject.put(CommCL.COMM_RECORD_SID_FLD, record.getSid());
        jsonObject.put(CommCL.COMM_OP_FLD, record.getOp());
        jsonObject.put(CommCL.COMM_ZC_NO_FLD, record.getZcno());
        JSONArray array = new JSONArray();
        array.add(jsonObject);
        baseModel.changeRecordState(array.toJSONString(),CommCL.COMM_MES_UDP_CHANGE_STATE_RECORD_VALUE)
                .compose(ResponseTransformer.handleResult())
                .compose(schedulerProvider.applySchedulers()).subscribe(
                jsonValues -> {
                    Log.i(TAG_NAME, jsonValues.toJSONString());
                    if (jsonValues.getIntValue(CommCL.RTN_ID) == -1) {//更改批次，成功返回id=1
                        baseView.changeRecordStateBack(false, record, "获取服务器信息失败" + jsonValues.toString());
                    } else {
                        record.setState1(batchStatusIn);
                        baseView.changeRecordStateBack(true, record, null);
                    }
                },
                throwable -> {
                    baseView.onRemoteFailed("290修改状态"+throwable.getMessage());
                });
    }

    /***
     * 批次提交修改生产状态
     * @param recordList 生产记录数组
     * @param batchStatusIn 新状态
     */
    public void changeRecordStateBatch(List<MESPRecord> recordList, String batchStatusIn) {
        JSONArray array = new JSONArray();
        for (int i = 0; i < recordList.size(); i++) {
            MESPRecord record = recordList.get(i);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(CommCL.COMM_OLD_STATE_FLD, record.getState1());
            jsonObject.put(CommCL.COMM_NEW_STATE_FLD, batchStatusIn);
            jsonObject.put(CommCL.COMM_RECORD_SID_FLD, record.getSid());
            jsonObject.put(CommCL.COMM_OP_FLD, record.getOp());
            jsonObject.put(CommCL.COMM_ZC_NO_FLD, record.getZcno());
            array.add(jsonObject);
        }
        baseModel.changeRecordState(array.toJSONString(), CommCL.COMM_MES_UDP_CHANGE_STATE_RECORD_VALUE)
                .compose(ResponseTransformer.handleResult())
                .compose(schedulerProvider.applySchedulers()).subscribe(
                jsonValues -> {
                    Log.i(TAG_NAME, jsonValues.toJSONString());
                    if (jsonValues.getIntValue(CommCL.RTN_ID) == -1) {//更改批次，成功返回id=1
                        baseView.changeMultiRecordStateBack(false, recordList, "获取服务器信息失败" + jsonValues.toString());
                    } else {
                        for (int i = 0; i < recordList.size(); i++){
                            recordList.get(i).setState1(batchStatusIn);
                            if(batchStatusIn.equals(CommCL.BATCH_STATUS_WORKING))
                                recordList.get(i).setHpdate(DateUtil.getCurrDateTime(ICL.DF_YMDT));
                        }
                        baseView.changeMultiRecordStateBack(true, recordList, batchStatusIn);
                    }
                },
                throwable -> {
                    Log.e(TAG_NAME,"ERRor",throwable);
                    baseView.onRemoteFailed(throwable.getMessage());
                });
    }

    //更新生产记录异常标志位表数量
    public void updatePrecodeYCState(MESPRecord record,String op,int abnormal){
        JSONObject jsonObject=new JSONObject();
        jsonObject.put(CommCL.COMM_RECORD_SID_FLD,record.getSid());
        jsonObject.put(CommCL.COMM_SID1_FLD,record.getSid1());
        jsonObject.put(CommCL.COMM_ZC_NO_FLD,record.getZcno());
        jsonObject.put(CommCL.SAVE_DATA_STATE,2);
        jsonObject.put(CommCL.COMM_ABNORMAL_FLD,abnormal);
        jsonObject.put(CommCL.COMM_ABNORMALOP_FLD,op);
        Log.i(TAG_NAME,jsonObject.toJSONString());
        baseModel.updateDataAID(jsonObject,CommCL.CELL_ID_D300101WEBAB).compose(ResponseTransformer.handleResult())
                .compose(schedulerProvider.applySchedulers()).subscribe(
                carBeans -> {
                    if (carBeans.getIntValue(CommCL.RTN_ID) != 0) {
                        baseView.commonBack(false,null,"mes_precord 修改失败",1);
                    }else if (carBeans.getIntValue(CommCL.RTN_ID) == 0) {
                        record.setAbnormal(abnormal);
                        baseView.commonBack(true,record,"",1);
                        //JSONObject msg = carBeans.getJSONObject(CommCL.RTN_DATA);//response里面的data，存放的是查询记录
                    }
                });
    }

    /***
     * 获取工单所需材料信息
     * @param cont 条件
     *@param key 0:代表材料明细，1代表上料明细
     */
    public void loadMaterialInfo(String cont, int key) {
        baseModel.getAssistInfo(key == 0 ? CommCL.AID_MATERIAL_LIST : CommCL.AID_MATERIAL_RECORD_LIST, cont).compose(ResponseTransformer.handleResult())
                .compose(schedulerProvider.applySchedulers())
                .subscribe(jsonValues -> {
                            Log.i(TAG_NAME, jsonValues.toJSONString());
                            if (jsonValues.getIntValue(CommCL.RTN_ID) == -1) {//更改批次，成功返回id=1
                                baseView.getMultiRecordBack(false, null, "获取服务器信息失败" + jsonValues.toString(), key);
                            } else {
                                JSONObject msg = jsonValues.getJSONObject(CommCL.RTN_DATA);//response里面的data，存放的是查询记录
                                JSONObject rtnMap = (JSONObject) msg.get(CommCL.RTN_DATA);//获取实际的查询结果
                                if (rtnMap.getInteger(CommCL.RTN_CODE) == 0) {
                                    baseView.getMultiRecordBack(false, null, rtnMap.getString(CommCL.RTN_MESSAGE), key);
                                } else {
                                    baseView.getMultiRecordBack(true, rtnMap.getJSONArray(CommCL.RTN_VALUES), "", key);
                                }

                            }
                        },
                        throwable -> {
                            baseView.onRemoteFailed(throwable.getMessage());
                        });
    }
    /***
     * 点胶开工、校验胶水是否过期，然后再开工
     * @param sbId 设备编码
     * @param slkid 工单号
     */
    public void getMachineGlueInfo(String sbId, String slkid,List<MESPRecord> startList) {

        baseModel.getMachineGlue(sbId, slkid).compose(ResponseTransformer.handleResult())
                .compose(schedulerProvider.applySchedulers())
                .subscribe(jsonObj -> {
                    Log.i(TAG_NAME, jsonObj.toJSONString());
                    if (jsonObj.getIntValue(CommCL.RTN_ID) != 0) {
                        baseView.getMultiRecordBack(false, null, jsonObj.getString(CommCL.RTN_MESSAGE),1);
                    } else {
                        JSONObject msg = jsonObj.getJSONObject(CommCL.RTN_DATA);//response里面的data，存放的是查询记录
                        JSONObject rtnMap = (JSONObject) msg.get(CommCL.RTN_DATA);//获取实际的查询结果
                        if (rtnMap.getInteger(CommCL.RTN_CODE) == 0) {
                            baseView.getMultiRecordBack(false, null, sbId+"机台没有添加【" +slkid + "】的胶水",1);
                        } else {
//                            baseView.checkSbIdCallBack(true,null,"");
                            JSONArray array = rtnMap.getJSONArray(CommCL.RTN_VALUES);
                            if (array == null) {
                                baseView.getMultiRecordBack(false, null, sbId+"机台没有添加【" +slkid + "】的胶水",1);
                            } else {
                                //从返回值中获取设备信息
                                JSONObject jiaoshui = array.getJSONObject(0);
                                String prtno=jiaoshui.getString("prtno");
                                Date mkdate=jiaoshui.getDate("mkdate");//
                                int tms=jiaoshui.getInteger("tms");
                                if(tms>CommCL.DIANJIAO_VALIDITY_MAX_WAIT_TIME){
                                    baseView.getMultiRecordBack(false, null, sbId+"机台的["+prtno+"]胶水时间["+tms+"]超过"+CommCL.DIANJIAO_VALIDITY_MAX_WAIT_TIME+"分钟！请联系工程,或者加新胶",1);
                                }else {
                                    //调开工代码
                                    this.changeRecordStateBatch(startList, CommCL.BATCH_STATUS_WORKING);
                                }

                            }
                        }
                    }
                }, throwable -> {
                    Log.i(TAG_NAME, throwable.getLocalizedMessage());
                    baseView.onRemoteFailed(throwable.getLocalizedMessage());
                });
    }
    /***
     * 上料保存记录，第一次并更新状态
     * @param record
     * @param newUpList
     */
    public void saveMaterialRecord(MESPRecord record, ArrayList<JSONObject> newUpList, boolean bFirst) {
        Log.i(TAG_NAME, record.getState1());
        if (CommCL.BATCH_STATUS_IN.equals(record.getState1())) {
            Log.i(TAG_NAME, "修改上料状态");
            changeRecordByMaterialUp(record, CommCL.BATCH_STATUS_CHARGING);
        }
        JSONObject jsonstr = new JSONObject();
        // =======================主对象=======================================
        jsonstr.put("sid", record.getSid1());
        jsonstr.put("sid1", record.getSid1());
        jsonstr.put("slkid", record.getSlkid());
        jsonstr.put("zcno", record.getZcno());
        jsonstr.put("checkid", "0");
        jsonstr.put("sbid", record.getSbid());
        jsonstr.put("op", record.getOp());
        jsonstr.put("sbuid","D0004");
        jsonstr.put(CommCL.SAVE_DATA_STATE, bFirst ? "3" : "2");
        jsonstr.put("dcid", CommonUtils.getMacID());
        jsonstr.put("hpdate",DateUtil.getNowCurrDateTime());
        jsonstr.put("mkdate",DateUtil.getNowCurrDateTime());
        //newUpList.put("zcno",jsonstr.getString("zcno"));
        jsonstr.put("D0040AWEB", newUpList);
        baseModel.saveData(jsonstr, CommCL.CELL_ID_D0040WEB)
                .compose(ResponseTransformer.handleResult()).compose(schedulerProvider.applySchedulers()).subscribe(
                jsonObject -> {
                    if (jsonObject.getIntValue(CommCL.RTN_ID) == -1) {//更改批次，成功返回id=1
                        baseView.saveRecordBack(false, null, jsonObject.toJSONString());
                    } else {
                        baseView.saveRecordBack(true, null, jsonObject.toJSONString());
                    }
                },
                throwable -> {
                    baseView.onRemoteFailed(throwable.getMessage());
                }
        );

    }

    /***
     * 修改生产记录的状态，在服务端内部，自动更新创批批次记录
     * @param record 生产记录实体类
     * @param batchStatusIn 新状态
     */
    public void changeRecordByMaterialUp(MESPRecord record, String batchStatusIn) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(CommCL.COMM_OLD_STATE_FLD, record.getState1());
        jsonObject.put(CommCL.COMM_NEW_STATE_FLD, batchStatusIn);
        jsonObject.put(CommCL.COMM_RECORD_SID_FLD, record.getSid());
        jsonObject.put(CommCL.COMM_OP_FLD, record.getOp());
        jsonObject.put(CommCL.COMM_ZC_NO_FLD, record.getZcno());
        JSONArray array = new JSONArray();
        array.add(jsonObject);
        baseModel.changeRecordState(array.toJSONString(), CommCL.COMM_MES_UDP_CHANGE_STATE_RECORD_VALUE)
                .compose(ResponseTransformer.handleResult()).compose(schedulerProvider.applySchedulers())
                .subscribe(jsonValues -> {
                            Log.i(TAG_NAME, jsonValues.toJSONString());
                        },
                        throwable -> {
                            baseView.onRemoteFailed(throwable.getMessage());
                        }
                );
    }

    /***
     * 根据生产批次号，获取上料主记录
     * @param sid1
     */
    public void checkMainMaterialExit(String sid1) {
        String cont = "~sid='" + sid1 + "'";
        baseModel.getAssistInfo(CommCL.AID_MAIN_MATERIAL_SID, cont).compose(ResponseTransformer.handleResult())
                .compose(schedulerProvider.applySchedulers()).subscribe(jsonObject -> {
            if (jsonObject.getIntValue(CommCL.RTN_ID) == -1) {//调用返回失败
                baseView.onRemoteFailed(jsonObject.toJSONString());
            } else {
                JSONObject msg = jsonObject.getJSONObject(CommCL.RTN_DATA);//response里面的data，存放的是查询记录
                JSONObject rtnMap = (JSONObject) msg.get(CommCL.RTN_DATA);//获取实际的查询结果
                if (rtnMap.getInteger(CommCL.RTN_CODE) == 0) {
                    baseView.checkMboxCallBack(false, null,0);
                } else {
                    baseView.checkMboxCallBack(true, null,0);
                }
            }
        }, throwable -> {
            baseView.onRemoteFailed(throwable.getMessage());
        });
    }

    /***
     * 检验发起原因查询，根据制成编码获取发起原因
     * @param zcId 制程代号
     */
    public void getLaunchingReasons(String zcId) {
        String cont = "~id='" + zcId + "'";
        baseModel.getAssistInfo(CommCL.AID_ZC_QCREASON, cont).compose(ResponseTransformer.handleResult())
                .compose(schedulerProvider.applySchedulers())
                .subscribe(jsonObject -> {
                    if (jsonObject.getIntValue(CommCL.RTN_ID) == -1) {//调用返回失败
                        baseView.onRemoteFailed(jsonObject.toJSONString());
                    } else {
                        JSONObject msg = jsonObject.getJSONObject(CommCL.RTN_DATA);//response里面的data，存放的是查询记录
                        JSONObject rtnMap = (JSONObject) msg.get(CommCL.RTN_DATA);//获取实际的查询结果
                        if (rtnMap.getInteger(CommCL.RTN_CODE) == 0) {
                            //没有获取到数据
                            baseView.loadReasonsBack(false, null, "制程【" + zcId + "】没有查询到发起原因");
                        } else {
                            //有发起原因
                            baseView.loadReasonsBack(true, rtnMap.getJSONArray(CommCL.RTN_VALUES), null);
                        }
                    }
                }, throwable -> {
                    baseView.onRemoteFailed(throwable.getMessage());
                });
    }

    /***
     * 根据制程代号，检验标志，获取检验项目
     * @param zcId 制程代号
     * @param bFirst 是否首件
     */
    public void getCheckProject(String zcId, boolean bFirst) {
        getCheckProject(zcId, bFirst ? CommCL.COMM_CHECK_FIRST : CommCL.COMM_CHECK_ROUNT);
    }

    /***
     * 根据制程代号，检验标志，获取检验项目
     * @param zcId 制程代号
     * @param type 检验类型
     */
    public void getCheckProject(String zcId, String type) {
        String cont = "~id='" + zcId + "' and chtype='" + type + "'";
        baseModel.getAssistInfo(CommCL.AID_PROCESS_ID, cont).compose(ResponseTransformer.handleResult())
                .compose(schedulerProvider.applySchedulers())
                .subscribe(jsonObject -> {
                    if (jsonObject.getIntValue(CommCL.RTN_ID) == -1) {//调用返回失败
                        baseView.onRemoteFailed(jsonObject.toJSONString());
                    } else {
                        JSONObject msg = jsonObject.getJSONObject(CommCL.RTN_DATA);//response里面的data，存放的是查询记录
                        JSONObject rtnMap = (JSONObject) msg.get(CommCL.RTN_DATA);//获取实际的查询结果
                        if (rtnMap.getInteger(CommCL.RTN_CODE) == 0) {
                            //没有获取到数据
                            baseView.loadCheckProjectBack(false, null, "制程【" + zcId + "】没有查询到检验项目");
                        } else {
                            //有检验项目
                            baseView.loadCheckProjectBack(true, rtnMap.getJSONArray(CommCL.RTN_VALUES), null);
                        }
                    }
                }, throwable -> {
                    baseView.onRemoteFailed(throwable.getMessage());
                });
    }

    /***
     * 根据制程代号，检验标志，获取检验项目
     * @param zcno 制程代号
     * @param outList 出站批次信息
     * @param key 分支标志
     */
    public void getCheckWaferQty(String zcno,List<MESPRecord> outList,int key) {
        String temp="";
        for(MESPRecord mp:outList){
            temp+="'"+mp.getSid1()+"',";
        }
        temp=temp.substring(0,temp.length()-1);
        String cont = "~zcno='" + zcno + "' and sid1 in (" + temp + ")";
        baseModel.getAssistInfo(CommCL.AID_GJ_WAFER_QTY, cont).compose(ResponseTransformer.handleResult())
                .compose(schedulerProvider.applySchedulers())
                .subscribe(jsonObject -> {
                    if (jsonObject.getIntValue(CommCL.RTN_ID) == -1) {//调用返回失败
                        baseView.onRemoteFailed(jsonObject.toJSONString());
                    } else {
                        JSONObject msg = jsonObject.getJSONObject(CommCL.RTN_DATA);//response里面的data，存放的是查询记录
                        JSONObject rtnMap = (JSONObject) msg.get(CommCL.RTN_DATA);//获取实际的查询结果
                        if (rtnMap.getInteger(CommCL.RTN_CODE) == 0) {
                            //没有获取到数据
                            baseView.commonBack(false,outList,"没有查询到任何信息",key);
                        } else {
                            //有数据
                            JSONArray data = rtnMap.getJSONArray(CommCL.RTN_VALUES);
                            String error="";
                            for(int i=0;i<data.size();i++){
                                JSONObject rs=data.getJSONObject(i);
                                if(rs.getInteger("bok")!=1){
                                    error+=" 【"+rs.getString("sid1")+"】"+"晶片没有上够还差"+(rs.getInteger("dw_num")-rs.getInteger("gjnumber"))+"";
                                }
                            }
                            if(error.length()<=0) {
                                baseView.commonBack(true, outList, "", key);
                            }else {
                                baseView.commonBack(false, outList, error, key);
                            }
                        }
                    }
                }, throwable -> {
                    baseView.onRemoteFailed(throwable.getMessage());
                });
    }

    /***
     * 根据制程代号，检验标志，获取检验项目
     *
     * @param key 分支标志
     */
    public void getCheckWaferBatNoNum(String key_str,String str,String material,int key) {

        String cont = "~bat_no='"+str+"'";
        baseModel.getAssistInfo(CommCL.AID_MATERIAL_USE, cont).compose(ResponseTransformer.handleResult())
                .compose(schedulerProvider.applySchedulers())
                .subscribe(jsonObject -> {
                    if (jsonObject.getIntValue(CommCL.RTN_ID) == -1) {//调用返回失败
                        baseView.onRemoteFailed(jsonObject.toJSONString());
                    } else {
                        JSONObject msg = jsonObject.getJSONObject(CommCL.RTN_DATA);//response里面的data，存放的是查询记录
                        JSONObject rtnMap = (JSONObject) msg.get(CommCL.RTN_DATA);//获取实际的查询结果
                        if (rtnMap.getInteger(CommCL.RTN_CODE) == 0) {
                            //没有获取到数据
                            baseView.commonBack(false,null,"没有查询到任何信息",key);
                        } else {
                            //有数据
                            JSONArray data = rtnMap.getJSONArray(CommCL.RTN_VALUES);
                            if(data.size()>0&&data.getJSONObject(0).getInteger("num")>=CommCL.ADDGLUING_P_REPEAT_NUM){//当有上料记录时，这个批次不能超过三次
                                baseView.commonBack(false,data,"该晶片批次已经绑定3次，不能再绑定",key);
                            }else{
                                HashMap<String,String> hashMap=new HashMap<>();
                                hashMap.put("key",key_str);hashMap.put("str",str);hashMap.put("material",material);
                                baseView.commonBack(true,hashMap,"",key);
                            }
                        }
                    }
                }, throwable -> {
                    baseView.onRemoteFailed(throwable.getMessage());
                });
    }

    /***
     * QC检验批次查询
     * @param sid1 批次号
     * @param zcId 制程ID
     * @param bhd 是否是后段工序
     */
    public void getQCBatchInfo(String sid1, String zcId, boolean bhd) {
        String cont = "";
        if (bhd) {
            cont = "~a.lotno='" + sid1 + "'";
        } else {
            cont = "~sid1='" + sid1 + "' and zcno='" + zcId + "'";
        }
        baseModel.getAssistInfo(bhd ? CommCL.AID_QC_BAT_INFO_HD : CommCL.AID_QC_BAT_INFO_QD, cont).compose(ResponseTransformer.handleResult())
                .compose(schedulerProvider.applySchedulers())
                .subscribe(jsonObject -> {
                    if (jsonObject.getIntValue(CommCL.RTN_ID) == -1) {//调用返回失败
                        baseView.onRemoteFailed(jsonObject.toJSONString());
                    } else {
                        JSONObject msg = jsonObject.getJSONObject(CommCL.RTN_DATA);//response里面的data，存放的是查询记录
                        JSONObject rtnMap = (JSONObject) msg.get(CommCL.RTN_DATA);//获取实际的查询结果
                        if (rtnMap.getInteger(CommCL.RTN_CODE) == 0) {
                            //没有获取到数据
                            baseView.checkQCBatInfoBack(false, null, "没有获取到批次信息:" + sid1);
                        } else {
                            //有批次记录
                            JSONObject jj = rtnMap.getJSONArray(CommCL.RTN_VALUES).getJSONObject(0);
                            QCBatchInfo batchInfo = JSONObject.parseObject(jj.toJSONString(), QCBatchInfo.class);
                            batchInfo.setHuoduan(bhd);
                            baseView.checkQCBatInfoBack(true, batchInfo, null);
                        }
                    }
                }, throwable -> {
                    baseView.onRemoteFailed(throwable.getLocalizedMessage());
                });
    }
    //插入生产异常表
    public void saveMESPerrLog(MESPRecord record,String reason){
        MESPerrLog perrLog=new MESPerrLog(record);
        perrLog.setReason(reason);
        JSONObject jsonObject=CommonUtils.getJsonObjFromBean(perrLog);
        jsonObject.put(CommCL.SAVE_DATA_STATE,3);
        Log.i(TAG_NAME,jsonObject.toJSONString());
        baseModel.saveData(jsonObject,CommCL.CELL_ID_D0074W).compose(ResponseTransformer.handleResult())
                .compose(schedulerProvider.applySchedulers()).subscribe(
                carBeans -> {
                    if (carBeans.getIntValue(CommCL.RTN_ID) != 0) {
                        baseView.getMultiRecordBack(false,null,"添加生产异常表失败",3);
                    }else if (carBeans.getIntValue(CommCL.RTN_ID) == 0) {
                        //JSONObject msg = carBeans.getJSONObject(CommCL.RTN_DATA);//response里面的data，存放的是查询记录
                        //JSONObject rtnMap = (JSONObject) msg.get(CommCL.RTN_DATA);//获取实际的查询结果
                        //JSONArray jsonArray=new JSONArray();

                        //jsonArray.add(record);
                        //baseView.getMultiRecordBack(true,null,"",3);
                    }
                });
    }

    public void saveRecord(JSONObject saveJ, String commSaveQcRecordCellId) {
        baseModel.saveData(saveJ, commSaveQcRecordCellId)
                .compose(ResponseTransformer.handleResult())
                .compose(schedulerProvider.applySchedulers())
                .subscribe(jsonObject -> {
                    Log.i(TAG_NAME, jsonObject.toJSONString());
                    if (jsonObject.getIntValue(CommCL.RTN_ID) != 0) {
                        baseView.saveRecordBack(false, null, "获取服务器信息失败" + jsonObject.toString());
                    } else if (jsonObject.getIntValue(CommCL.RTN_ID) == 0) {
                        JSONObject saveBack = jsonObject.getJSONObject(CommCL.RTN_DATA);
                        String sid = saveBack.getString("sid");
                        baseView.saveRecordBack(true, null, sid);
                    }
                }, throwable -> {
                    baseView.onRemoteFailed(throwable.getLocalizedMessage());
                });
    }

    public void getApprovalInfo(CeaPars ceaPars) {
        baseModel.getCeaCheckInfo(CommonUtils.getJsonObjFromBean(ceaPars).toJSONString(), CommCL.COMM_CHK_LIST)
                .compose(ResponseTransformer.handleResult()).compose(schedulerProvider.applySchedulers())
                .subscribe(jsonObject -> {
                    if (jsonObject.getIntValue(CommCL.RTN_ID) != 0) {
                        baseView.checkActionBack(false,CommCL.COMM_CHK_LIST,ceaPars,null,"获取服务器信息失败" + jsonObject.toString());
                    }else{
                        JSONObject data = jsonObject.getJSONObject(CommCL.RTN_DATA);
                        JSONObject chkInfo = data.getJSONObject("info");
                        CWorkInfo cWorkInfo = JSONObject.parseObject(chkInfo.toJSONString(),CWorkInfo.class);
                        Log.i(TAG_NAME,cWorkInfo.toString());
                        baseView.checkActionBack(true,CommCL.COMM_CHK_LIST,ceaPars,cWorkInfo,"");
                    }
                }, throwable -> {
                    baseView.onRemoteFailed(throwable.getLocalizedMessage());
                });
    }

    public void checkActionUp(CeaPars ceaPars) {
        baseModel.getCeaCheckInfo(CommonUtils.getJsonObjFromBean(ceaPars).toJSONString(), CommCL.COMM_CHK_DO)
                .compose(ResponseTransformer.handleResult()).compose(schedulerProvider.applySchedulers())
                .subscribe(jsonObject -> {
                    if (jsonObject.getIntValue(CommCL.RTN_ID) != 0) {
                        baseView.checkActionBack(false,CommCL.COMM_CHK_DO,ceaPars,null,"获取服务器信息失败" + jsonObject.toString());
                    }else{
                        JSONObject data = jsonObject.getJSONObject(CommCL.RTN_DATA);
                        Log.i(TAG_NAME,jsonObject.toString());
                        String chkInfo = data.getString("info");

                        baseView.checkActionBack(true,CommCL.COMM_CHK_DO,ceaPars,null,chkInfo);
                    }
                }, throwable -> {
                    baseView.onRemoteFailed(throwable.getLocalizedMessage());
                });
    }

//------------contains-------------
    public boolean contains(String str1,String str2){
        if(TextUtils.isEmpty(str1)||TextUtils.isEmpty(str2)){
            return  false;
        }
        if(str1.contains(str2)){
            return true;
        }
        return false;
    }

    /**
     * 通用查询
     * @param assistId
     * @param cont
     * @param key
     */
    public void getAssistInfo(String assistId,String cont,String prd_no,int key) {
        baseModel.getAssistInfo(assistId,cont).compose(ResponseTransformer.handleResult())
                .compose(schedulerProvider.applySchedulers()).subscribe(
                carBeans -> {
                    if (carBeans.getIntValue(CommCL.RTN_ID) != 0) {
                        baseView.commonBack(false, null,"获取服务器信息失败" + carBeans.toString(), 0);
                    }else if (carBeans.getIntValue(CommCL.RTN_ID) == 0) {
                        JSONObject msg = carBeans.getJSONObject(CommCL.RTN_DATA);//response里面的data，存放的是查询记录
                        JSONObject rtnMap = (JSONObject) msg.get(CommCL.RTN_DATA);//获取实际的查询结果
                        if (rtnMap.getInteger(CommCL.RTN_CODE) == 0) {
                            baseView.commonBack(false, null,"没有查询到记录"+assistId+cont+";", key);
                        } else {

                            JSONArray array = rtnMap.getJSONArray(CommCL.RTN_VALUES);//返回数据
                            if(array.size()==0){
                                baseView.commonBack(false, null,"没有查询到记录"+assistId+cont+";", key);
                            }else {
                                //JSONObject data = array.getJSONObject(0);
                                baseView.commonBack(true, array, prd_no, key);
                            }
                        }
                    }
                },throwable -> {
                    baseView.onRemoteFailed(throwable.getMessage());
                });
    }


    /***
     * 不良原因查询
     * @param
     */
    public void selectBlyy() {

        baseModel.getAssistInfo(CommCL.AID_BLYY).compose(ResponseTransformer.handleResult())
                .compose(schedulerProvider.applySchedulers())
                .subscribe(jsonObject -> {
                    if (jsonObject.getIntValue(CommCL.RTN_ID) == -1) {//调用返回失败
                        baseView.onRemoteFailed(jsonObject.toJSONString());
                    } else {
                        JSONObject msg = jsonObject.getJSONObject(CommCL.RTN_DATA);//response里面的data，存放的是查询记录
                        JSONObject rtnMap = (JSONObject) msg.get(CommCL.RTN_DATA);//获取实际的查询结果
                        if (rtnMap.getInteger(CommCL.RTN_CODE) == 0) {
                            //没有获取到数据
                            baseView.loadReasonsBack(false, null, "原因不存在");
                        } else {
                            //有发起原因
                            JSONArray array=rtnMap.getJSONArray(CommCL.RTN_VALUES);
                            baseView.loadReasonsBack(true, rtnMap.getJSONArray(CommCL.RTN_VALUES), "");
                        }
                    }
                }, throwable -> {
                    baseView.onRemoteFailed(throwable.getMessage());
                });
    }

}
