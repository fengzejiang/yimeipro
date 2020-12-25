package com.yimeinew.model.impl;

import com.aliyun.openservices.shade.com.alibaba.rocketmq.shade.com.alibaba.fastjson.JSONArray;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.shade.com.alibaba.fastjson.JSONObject;
import com.yimeinew.data.MESPRecord;
import com.yimeinew.network.NetWorkManager;
import com.yimeinew.network.NetWorkPrintManager;
import com.yimeinew.network.response.Response;
import com.yimeinew.utils.CommCL;
import com.yimeinew.utils.CommonUtils;
import com.yimeinew.utils.ToolUtils;
import io.reactivex.Observable;

import java.util.HashMap;

/**
 * 获取服务端数据公用类
 * @Auther: fengzejiang1987@163.com
 * @Date : 2018/12/12 10:32
 */
public class BaseModel {
    public static Observable<Response<JSONObject>> doServer(HashMap<String,String> params){
        return NetWorkManager.getRequest().getAssistServer(params);
    }

    /***
     * 保存数据到系统的接口
     * @param jsonObject json格式的数据
     * @return
     */
    public static Observable<Response<JSONObject>> saveData(JSONObject jsonObject,String cellID){
        HashMap<String,String> params = CommonUtils.saveDataMap(jsonObject.toJSONString(),cellID);
        return saveData(params);
    }
    /***
     * 保存数据到系统的接口
     * @param jsonObject json格式的数据
     * @return
     */
    public static Observable<Response<JSONObject>> updateDataAID(JSONObject jsonObject,String cellID){
        HashMap<String,String> params = CommonUtils.updateDataMap(jsonObject.toJSONString(),cellID);
        //System.out.println(params.toString());
        return commonService(params);
    }
    /***
     * 保存数据到系统的接口
     * @param params
     * @return
     */
    public static Observable<Response<JSONObject>> saveData(HashMap<String,String> params){
        return NetWorkManager.getRequest().saveData(params);
    }

    /**650或者其他非通用
     * */
    public Observable<Response<JSONObject>> updateData(String values,String udpId) {
        HashMap<String,String> params = CommonUtils.commMesUDPDataMap(values,udpId);
        return doServer(params);
    }

    public static Observable<Response<JSONObject>> commonService(HashMap<String,String> params) {
        return NetWorkManager.getRequest().commonService(params);
    }

    /***
     * 查询料盒号
     * @param box
     * @return
     */
    public Observable<Response<JSONObject>> getMbox(String box){
        HashMap<String,String> params = CommonUtils.getAsistsReqHashMap();
        params.put(CommCL.PARAM_ASSIST_FLD, CommCL.AID_BOX_QUERY);
        String cont = "~id='"+box+"'";
        params.put(CommCL.PARAM_CONT_FLD, cont);
        return doServer(params);
    }

    public Observable<Response<JSONObject>> bindBox(String boxNo,String sid1,String zcNo,boolean binding) {
        JSONArray jsonArr = new JSONArray();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(CommCL.COMM_M_BOX_FLD,boxNo);
        jsonObject.put(CommCL.COMM_SID1_FLD,sid1);
        jsonObject.put(CommCL.COMM_BIND_FLD,binding?CommCL.COMM_BIND_ON_BIND:CommCL.COMM_BIND_UN_BIND);//0:解绑;1:绑定
        jsonObject.put(CommCL.COMM_ZC_NO_FLD,zcNo);
        jsonObject.put(CommCL.COMM_DCID_FLD, CommonUtils.getMacID());
        jsonArr.add(jsonObject);
        HashMap<String,String> params = CommonUtils.commMesUDPDataMap(jsonArr.toJSONString(),CommCL.COMM_MES_UDP_UNBIND_VALUE);
        return commonService(params);
    }

    /***
     * 通过设备Id和制成号获取设备信息，包含设备状态
     * @param sbId 设备编码
     * @param zcNo 制成编码
     * @return
     */
    public Observable<Response<JSONObject>> getEquipmentInf(String sbId, String zcNo) {
//        HashMap<String,String> params = CommonUtils.getAsistsReqHashMap();
//        params.put(CommCL.PARAM_ASSIST_FLD, CommCL.AID_QJ_EQUIPMENT_QUERY);
        String cont = "~id='"+sbId+"' and zc_id='"+zcNo+"'";
//        params.put(CommCL.PARAM_CONT_FLD, cont);
        return getAssistInfo(CommCL.AID_QJ_EQUIPMENT_QUERY,cont);
    }

    /***
     * 通过设备号和制成号获取生产记录
     * @param sbId 设备编码
     * @param zcNo 制成编码
     * @return 返回生产记录
     */
    public Observable<Response<JSONObject>> getRecordBySbIdAndZcno(String sbId,String zcNo) {
//        HashMap<String,String> params = CommonUtils.getAsistsReqHashMap();
//        params.put(CommCL.PARAM_ASSIST_FLD, CommCL.AID_QJ_PRO_RECORD_QUERY);
        String cont = "~sbid='"+sbId+"' and zcno='"+zcNo+"'";
//        params.put(CommCL.PARAM_CONT_FLD, cont);
        return getAssistInfo(CommCL.AID_QJ_PRO_RECORD_QUERY,cont);
    }
    /***
     * 通过设备号和制成号获取生产记录
     * @param sbId 设备编码
     * @param zcNo 制成编码
     * @return 返回生产记录
     */
    public Observable<Response<JSONObject>> getRecordBySbIdAndZcnoBD(String sbId,String zcNo) {
//        HashMap<String,String> params = CommonUtils.getAsistsReqHashMap();
//        params.put(CommCL.PARAM_ASSIST_FLD, CommCL.AID_QJ_PRO_RECORD_QUERY);
        String cont = "~p.sbid='"+sbId+"' and p.zcno='"+zcNo+"'";
//        params.put(CommCL.PARAM_CONT_FLD, cont);
        return getAssistInfo(CommCL.AID_QJ_PRO_RECORD_QUERY_BD,cont);
    }


    /***
     * 生产批次信息查询，获取创批批次记录
     * @param lotno 测试号
     * @param zcNo 制成编码
     * @return
     */
    public Observable<Response<JSONObject>> getBatchRecordByLotNoAndZcNo(String lotno,String zcNo) {
//        HashMap<String,String> params = CommonUtils.getAsistsReqHashMap();
//        params.put(CommCL.PARAM_ASSIST_FLD, CommCL.AID_QJ_BATCH_RECORD_QUERY);
        String cont = "~lotno='"+lotno+"' and zcno='"+zcNo+"'";
//        params.put(CommCL.PARAM_CONT_FLD, cont);
        return getAssistInfo(CommCL.AID_QJ_LOT_PRECORD_QUERY,cont);
    }
    /***
     * 生产批次信息查询，获取创批批次记录
     * @param sid1 创批批次
     * @param zcNo 制成编码
     * @return
     */
    public Observable<Response<JSONObject>> getBatchRecordBySid1AndZcNo(String sid1,String zcNo) {
//        HashMap<String,String> params = CommonUtils.getAsistsReqHashMap();
//        params.put(CommCL.PARAM_ASSIST_FLD, CommCL.AID_QJ_BATCH_RECORD_QUERY);
          String cont;
          if(CommCL.BAKE_USE_MBOX&& ToolUtils.containValue(CommCL.isBake,zcNo)){
              cont = "~ml_mbox='"+sid1+"' and zcno='"+zcNo+"'";//按料料盒查询
          }else {
              cont = "~sid1='"+sid1+"' and zcno='"+zcNo+"'";//按批次查询
          }
//        params.put(CommCL.PARAM_CONT_FLD, cont);
        return getAssistInfo(CommCL.AID_QJ_BATCH_RECORD_QUERY,cont);
    }

    /***
     * 获取点胶机机台最近一条胶水信息
     * @param sbid 点胶机台号
     * @param slkid 工单号
     * @return
     */
    public Observable<Response<JSONObject>> getMachineGlue(String sbid,String slkid) {
//        HashMap<String,String> params = CommonUtils.getAsistsReqHashMap();
//        params.put(CommCL.PARAM_ASSIST_FLD, CommCL.AID_QJ_BATCH_RECORD_QUERY);
        String cont = "~sbid='"+sbid+"' and slkid='"+slkid+"'";
//        params.put(CommCL.PARAM_CONT_FLD, cont);
        return getAssistInfo(CommCL.AID_ENSB_GLUE_INFO_ID,cont);
    }

    /***
     * 生产批次信息查询，获取创批批次记录。固晶1专用
     * @param sid1 创批批次
     * @param zcNo 制成编码
     * @return
     */
    public Observable<Response<JSONObject>> getBatchRecordBySid1AndZcNo(String sid1,String zcNo,String mbox) {
//        HashMap<String,String> params = CommonUtils.getAsistsReqHashMap();
//        params.put(CommCL.PARAM_ASSIST_FLD, CommCL.AID_QJ_BATCH_RECORD_QUERY);
        String cont = "~sid1='"+sid1+"' and zcno='"+zcNo+"'"+"' and mbox='"+mbox+"'";
//        params.put(CommCL.PARAM_CONT_FLD, cont);
        return getAssistInfo(CommCL.AID_QJ_BATCH_RECORD_QUERY,cont);
    }
    public Observable<Response<JSONObject>> changeRecordState(String values,String udpId) {
        HashMap<String,String> params = CommonUtils.commMesUDPDataMap(values,udpId);
        return doServer(params);
    }

    /**
     * 200快速过站，一条一条记录提交
     * @param values
     * @param udpId
     * @return
     */
    public Observable<Response<JSONObject>> changeRecordState2(HashMap<String,String> values,String udpId) {
        HashMap<String,String> params = CommonUtils.commMesUDPDataMap2(values,udpId);
        return doServer(params);
    }



    /***
     * 生产批次添加锡膏或者是添加胶杯
     * @param sbId 设备号
     * @param prtNo 胶杯号或者锡膏号
     * @param udpId 300，添加锡膏，410 添加锡膏
     * @return
     */
    public Observable<Response<JSONObject>> LotAddGluingOrXg(String sbId,String prtNo,String udpId) {
        HashMap<String,String> params = CommonUtils.commMesUDPDataGluingMap(sbId,prtNo,udpId);
        return doServer(params);
    }

    public Observable<Response<JSONObject>> getPrdt(String prd_no){
        String cont="~prd_no='"+prd_no+"'";
        return getAssistInfo(CommCL.AID_ERP_PRDT,cont);
    }

    /**
     * 快速过站获取LOT表信息
     * @param sid1
     * @param zcno
     * @return
     */
    public Observable<Response<JSONObject>> getQuickLot(String sid1,String zcno){
        String cont="~sid1='"+sid1+"' and  zcno='"+zcno+"'";
        return getAssistInfo(CommCL.AID_QUICK_LOT,cont);
    }
    /**
     * 二次快速过站获取LOT表信息
     * @param sid1
     * @param zcno
     * @return
     */
    public Observable<Response<JSONObject>> getQuickLot2(String sid1,String zcno){
        String cont="~sid1='"+sid1+"' and  zcno='"+zcno+"'";
        return getAssistInfo(CommCL.AID_QUICK_LOT2,cont);
    }
    /***
     * 查询调用辅助
     * @param assistId
     * @param cont
     * @return
     */
    public Observable<Response<JSONObject>> getAssistInfo(String assistId,String cont) {
        HashMap<String,String> params = CommonUtils.getAsistsReqHashMap();
        params.put(CommCL.PARAM_ASSIST_FLD, assistId);
        params.put(CommCL.PARAM_CONT_FLD, cont);
        return doServer(params);
    }
    /***
     * 查询调用辅助
     * @param assistId
     * @param
     * @return
     */
    public Observable<Response<JSONObject>> getAssistInfo(String assistId) {
        HashMap<String,String> params = CommonUtils.getAsistsReqHashMap();
        params.put(CommCL.PARAM_ASSIST_FLD, assistId);
       // params.put(CommCL.PARAM_CONT_FLD, cont);
        return doServer(params);
    }

    /***
     * 审批流
     * @param ceaInfo
     * @param chk
     * @return
     */
    public Observable<Response<JSONObject>> getCeaCheckInfo(String ceaInfo,int chk) {
        HashMap<String,String> params = CommonUtils.getCheckInfoReqHashMap();
        params.put(CommCL.PARAM_CHK_ID_FLD, chk+"");
        params.put(CommCL.PARAM_CEA_FLD,ceaInfo);
        return doServer(params);
    }

    public static Observable<Response<JSONObject>> doPrintServer(HashMap<String,String> params){
        return NetWorkPrintManager.getRequest().getAssistServer(params);
    }


}
