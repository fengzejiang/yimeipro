package com.yimeinew.presenter;

import android.text.TextUtils;
import android.util.Log;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.shade.com.alibaba.fastjson.JSONArray;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.shade.com.alibaba.fastjson.JSONObject;
import com.yimeinew.data.MESPRecord;
import com.yimeinew.data.ZCInfo;
import com.yimeinew.model.impl.CommZCModel;
import com.yimeinew.modelInterface.CommFastView;
import com.yimeinew.network.response.ResponseTransformer;
import com.yimeinew.network.schedulers.SchedulerProvider;
import com.yimeinew.utils.CommCL;
import com.yimeinew.utils.CommonUtils;

import java.util.HashMap;

public class CommFastPresenter {
    private final String TAG_NAME = CommFastPresenter.class.getSimpleName();
    private CommFastView baseView;//通用工站回调接口
    private SchedulerProvider schedulerProvider; //线程池
    private CommZCModel baseModel;//服务端处理接口
    ZCInfo zcInfo;
    public CommFastPresenter(CommFastView baseStationBindingView, SchedulerProvider schedulerProvider) {
        this.baseView = baseStationBindingView;
        this.schedulerProvider = schedulerProvider;
        if (baseModel == null) {
            baseModel = new CommZCModel();
        }
    }
    public CommFastPresenter(CommFastView baseStationBindingView, SchedulerProvider schedulerProvider,ZCInfo zcInfo) {
        this.baseView = baseStationBindingView;
        this.schedulerProvider = schedulerProvider;
        this.zcInfo=zcInfo;
        if (baseModel == null) {
            baseModel = new CommZCModel();
        }
    }
    /*-------获取------*/
    /**
     *获取lot信息入站校验
     * @param sid1
     * @param zcno
     * @param key
     */
    public void checkQuickLot(String sid1,String zcno,int key) {

        baseModel.getQuickLot(sid1,zcno).compose(ResponseTransformer.handleResult())
                .compose(schedulerProvider.applySchedulers()).subscribe(
                carBeans -> {
                    if (carBeans.getIntValue(CommCL.RTN_ID) != 0) {
                        baseView.getQuickLotBack(false, null,"获取服务器信息失败" + carBeans.toString(), key);
                    }else if (carBeans.getIntValue(CommCL.RTN_ID) == 0) {
                        JSONObject msg = carBeans.getJSONObject(CommCL.RTN_DATA);//response里面的data，存放的是查询记录
                        JSONObject rtnMap = (JSONObject) msg.get(CommCL.RTN_DATA);//获取实际的查询结果
                        if (rtnMap.getInteger(CommCL.RTN_CODE) == 0) {
                            baseView.getQuickLotBack(false, null,"没有【" + sid1 + "】批次",key);
                        } else {
                            JSONArray array = rtnMap.getJSONArray(CommCL.RTN_VALUES);
                            JSONObject jsonObject = array.getJSONObject(0);
                            //校验是否可以开工
                            int bok = jsonObject.getInteger("bok");
                            if (CommCL.BOK != bok) {
                                baseView.getQuickLotBack(false, null, "该批次【" + sid1 + "】上一工序未出站，不能入站！",key);
                                return;
                            }
                            //校验是否被HOLD
                            int holdID = jsonObject.getInteger("holdid");
                            if (CommCL.HOLD == holdID) {
                                baseView.getQuickLotBack(false, null, "该批次【" + sid1 + "】已经HOLD，不能入站！",key);
                                return;
                            }
                            //校验状态
                            String stateValue = jsonObject.getString("state");
                            if (CommCL.BATCH_STATUS_DONE.equals(stateValue)) {
                                baseView.getQuickLotBack(false, null, "该批次【" + sid1 + "】已经完工！",key);
                                return;
                            }
                            if (CommCL.BATCH_STATUS_WORKING.equals(stateValue)) {
                                baseView.getQuickLotBack(false, null, "该批次【" + sid1 + "】正在生产中,不能再次入站！",key);
                                return;
                            }
                            if (CommCL.BATCH_STATUS_CHARGING.equals(stateValue)) {
                                baseView.getQuickLotBack(false, null, "该批次【" + sid1 + "】已经上料,不能再次入站！",key);
                                return;
                            }
                            if (CommCL.BATCH_STATUS_IN.equals(stateValue)) {
                                baseView.getQuickLotBack(false, null, "该批次【" + sid1 + "】已经别的机台入站,不能再次入站！",key);
                                return;
                            }
                            String currMO = baseView.getCurrMO();
                            String sid = jsonObject.getString("sid");
                            if (currMO.length() > 0) {
                                if (!currMO.equals(sid)&&!"41".equals(zcno)&&!"1A".equals(zcno)&&!"1B".equals(zcno)) {
                                    baseView.getQuickLotBack(false, null, "当前工单是【" + currMO + "】,扫描的工单是【" + sid + "】",key);
                                    return;
                                }
                            }
                            baseView.getQuickLotBack(true,jsonObject,"",key);
                        }
                    }
                });

    }

    /***
     * 生产批次生产记录信息
     * @param record 生产记录
     */
    public void makeProRecord(MESPRecord record) {
        baseModel.saveQuickData(record, "3").compose(ResponseTransformer.handleResult())
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
     * 200修改生产批次状态（批次快速过站）
     * @param record
     * @param batchStatusIn
     */
    public void changeLotStateOneByOne(MESPRecord record, String batchStatusIn){
        HashMap<String,String> jsonObject = new HashMap<String,String>();
        jsonObject.put(CommCL.COMM_OLD_STATE_FLD, CommCL.BATCH_STATUS_READY);
        jsonObject.put(CommCL.COMM_NEW_STATE_FLD, batchStatusIn);
        jsonObject.put(CommCL.COMM_RECORD_SID_FLD, record.getSid1());
        jsonObject.put(CommCL.COMM_SLKID_FLD, record.getSlkid());
        jsonObject.put(CommCL.COMM_ZC_NO_FLD, record.getZcno());
        //JSONArray array = new JSONArray();
        //array.add(jsonObject);
        baseModel.changeRecordState2(jsonObject,CommCL.COMM_MES_UDP_CHANGE_STATE_QUICK_VALUE)
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
                    baseView.onRemoteFailed("200修改状态"+throwable.getMessage());
                });
    }

    /**
     * 200修改生产批次状态（批次快速过站） 状态从"03"-->batchStatusIn
     * @param record
     * @param batchStatusIn
     */
    public void changeLotStateOneByOne2(MESPRecord record, String batchStatusIn){
        HashMap<String,String> jsonObject = new HashMap<String,String>();
        jsonObject.put(CommCL.COMM_OLD_STATE_FLD, CommCL.BATCH_STATUS_WORKING);
        jsonObject.put(CommCL.COMM_NEW_STATE_FLD, batchStatusIn);
        jsonObject.put(CommCL.COMM_RECORD_SID_FLD, record.getSid1());
        jsonObject.put(CommCL.COMM_SLKID_FLD, record.getSlkid());
        jsonObject.put(CommCL.COMM_ZC_NO_FLD, record.getZcno());
        //JSONArray array = new JSONArray();
        //array.add(jsonObject);
        baseModel.changeRecordState2(jsonObject,CommCL.COMM_MES_UDP_CHANGE_STATE_QUICK_VALUE)
                .compose(ResponseTransformer.handleResult())
                .compose(schedulerProvider.applySchedulers()).subscribe(
                jsonValues -> {
                    Log.i(TAG_NAME, jsonValues.toJSONString());
                    if (jsonValues.getIntValue(CommCL.RTN_ID) == -1) {//更改批次，成功返回id=1
                        baseView.changeRecordStateBack(false, record, "获取信息失败" + jsonValues.toString());
                    } else {
                        record.setState1(batchStatusIn);
                        baseView.changeRecordStateBack(true, record, null);
                    }
                },
                throwable -> {
                    baseView.onRemoteFailed("200修改状态"+throwable.getMessage());
                });
    }

    /***
     * 卡板记录信息
     *
     */
    public void selectMzkb(String code,int key){
        String cont="~allcode='"+code+"'";
        baseModel.getAssistInfo(CommCL.AID_MZ_KB,cont).compose(ResponseTransformer.handleResult())
                .compose(schedulerProvider.applySchedulers()).subscribe(
                carBeans -> {
                    if (carBeans.getIntValue(CommCL.RTN_ID) != 0) {
                        baseView.getQuickLotBack(false, null,"获取信息失败" + carBeans.toString(), key);
                    }else if (carBeans.getIntValue(CommCL.RTN_ID) == 0) {
                        JSONObject msg = carBeans.getJSONObject(CommCL.RTN_DATA);//response里面的data，存放的是查询记录
                        JSONObject rtnMap = (JSONObject) msg.get(CommCL.RTN_DATA);//获取实际的查询结果
                        if (rtnMap.getInteger(CommCL.RTN_CODE) == 0) {
                            baseView.getQuickLotBack(false, null,"没有该"  + "喷码",key);
                        } else {
                            JSONArray array = rtnMap.getJSONArray(CommCL.RTN_VALUES);
                            JSONObject jsonObject = array.getJSONObject(0);
                            //校验是否可以开工
                            int bok = jsonObject.getInteger("bok");
                            if (CommCL.BOK != bok) {
                                baseView.getQuickLotBack(false, null, "上一工序未出站",key);
                                return;
                            }
                            //校验是否被HOLD
                            int holdID = jsonObject.getInteger("holdid");
                            if (CommCL.HOLD == holdID) {
                                baseView.getQuickLotBack(false, null, "该喷码被OLD",key);
                                return;
                            }
                            //校验状态
                            String stateValue = jsonObject.getString("cbstate");
                            String lotstate=jsonObject.getString("lotstate");
                            if (CommCL.BATCH_STATUS_DONE.equals(stateValue)) {
                                baseView.getQuickLotBack(false, null, "该喷码已完工！",key);
                                return;
                            }
                            if (CommCL.BATCH_STATUS_WORKING.equals(stateValue)) {
                                //baseView.getQuickLotBack(false, null, "该喷码【" + code + "】正在生产中,不能再次入站！",key);
                                baseView.getQuickLotBack(false, null, "已扫描",key);
                                return;
                            }
//                            if (CommCL.BATCH_STATUS_DONE.equals(lotstate)) {
//                                baseView.getQuickLotBack(false, null, "该工单已经完工！",key);
//                                return;
//                            }

//                            if (CommCL.BATCH_STATUS_CHARGING.equals(stateValue)) {
//                                baseView.getQuickLotBack(false, null, "该批次【" + sid1 + "】已经上料,不能再次入站！",key);
//                                return;
//                            }
                            if (CommCL.BATCH_STATUS_IN.equals(stateValue)) {
                                baseView.getQuickLotBack(false, null, "该喷码在别的机台入站",key);
                                return;
                            }
//                            String currMO = baseView.getCurrMO();
//                            String sid = jsonObject.getString("sid");
//                            if (!TextUtils.isEmpty(currMO)&&!TextUtils.equals(currMO,sid)) {
//                                baseView.getQuickLotBack(false, null, "当前工单是【" + currMO + "】,扫描的工单是【" + sid + "】",key);
//                                return;
//                            }
                            if(CommCL.BATCH_STATUS_ABNORMAL.equals(stateValue)){
                                baseView.getQuickLotBack(false, null, "打叉板",key);
                                return;
                            }
                            if(CommCL.BATCH_STATUS_READY.equals(stateValue)) {
                                baseView.getQuickLotBack(true, jsonObject, "", key);
                            }else{
                                baseView.getQuickLotBack(false, null, "该喷码状态不对",key);
                                return;
                            }
                        }
                    }
                });
    }

    public void   mzkbRecord(MESPRecord record,JSONObject batchInfo){
        baseModel.comSaveData(record,CommCL.CELL_ID_D50309).compose(ResponseTransformer.handleResult())
                .compose(schedulerProvider.applySchedulers()).subscribe(
                jsonValues -> {
                    Log.i(TAG_NAME, jsonValues.toJSONString());
                    if (jsonValues.getIntValue(CommCL.RTN_ID) != 0) {
                        baseView.saveRecordBack(false,null, "获取信息失败" + jsonValues.toString());
                    } else if (jsonValues.getIntValue(CommCL.RTN_ID) == 0) {
                        JSONObject saveBack = jsonValues.getJSONObject(CommCL.RTN_DATA);
                        String sid = saveBack.getString("sid");
                        record.setSid(sid);
                        batchInfo.put("sid",sid);
                        baseView.saveRecordBack(true, batchInfo, null);
                    }
                },
                throwable -> {
                    baseView.onRemoteFailed(throwable.getMessage());
                });
    }

    public void updateInfo(Object record, String insObject, int key){
        JSONObject recordObj= CommonUtils.getJsonObjFromBean(record);
        baseModel.comUpdateData(record,insObject).compose(ResponseTransformer.handleResult())
                .compose(schedulerProvider.applySchedulers()).subscribe(
                jsonValues -> {
                    Log.i(TAG_NAME, jsonValues.toJSONString());
                    if (jsonValues.getIntValue(CommCL.RTN_ID) != 0) {
                        baseView.updateCallBack(false,recordObj,"获取信息失败" + jsonValues.toString(),key);
                    } else if (jsonValues.getIntValue(CommCL.RTN_ID) == 0) {
                        baseView.updateCallBack(true, recordObj, null,key);
                    }
                },
                throwable -> {
                    baseView.onRemoteFailed(throwable.getMessage());
                });
    }

    /***
     * 维修确认信息
     *
     */
    public void selectWxqr(String code,int key){
        String cont="~allcode='"+code+"'";
        baseModel.getAssistInfo(CommCL.AID_WXQR,cont).compose(ResponseTransformer.handleResult())
                .compose(schedulerProvider.applySchedulers()).subscribe(
                carBeans -> {
                    if (carBeans.getIntValue(CommCL.RTN_ID) != 0) {
                        baseView.getQuickLotBack(false, null,"获取信息失败1" , key);
                    }else if (carBeans.getIntValue(CommCL.RTN_ID) == 0) {
                        JSONObject msg = carBeans.getJSONObject(CommCL.RTN_DATA);//response里面的data，存放的是查询记录
                        JSONObject rtnMap = (JSONObject) msg.get(CommCL.RTN_DATA);//获取实际的查询结果
                        if (rtnMap.getInteger(CommCL.RTN_CODE) == 0) {
                            baseView.getQuickLotBack(false, null,"没有该"  + "喷码",key);
                        } else {
                            JSONArray array = rtnMap.getJSONArray(CommCL.RTN_VALUES);
                            JSONObject jsonObject = array.getJSONObject(0);

                            //维修次数
                            int wxnum = jsonObject.getInteger("wxnum");
                            //生产检验状态
                            Integer scstate=jsonObject.getInteger("scstate");
                            //String scdate=jsonObject.getString("scdate");
                            if (wxnum<=0) {
                                baseView.getQuickLotBack(false, null, "未被送修！",key);
                                return;
                            }
                            boolean flag=false;
                            String sort=zcInfo.getSort();
                            //判断是否是品质检验
                            if(sort.equals("B")){
                                flag=true;
                            }
                            if(!TextUtils.isEmpty(jsonObject.getString("scdate"))||scstate!=null){
                                if(!flag){
                                    baseView.getQuickLotBack(false, null, "生产已检验",key);
                                    return;
                                }
                            }
                            //判定生产是否已检验,以及判定结果
                            if(flag){
                                if(scstate==null||TextUtils.isEmpty(jsonObject.getString("scdate"))){
                                    baseView.getQuickLotBack(false, null, "生产未检验",key);
                                    return;
                                }else if(scstate==-1){
                                    baseView.getQuickLotBack(false, null, "生产判定为NG",key);
                                    return;
                                }
                            }
                            //判断该喷码品质是否已经检验
                            if(flag){
                                if(!TextUtils.isEmpty(jsonObject.getString("qcdate"))||jsonObject.getInteger("qcstate")!=null){
                                    baseView.getQuickLotBack(false, null, "品质已检验",key);
                                    return;

                                }
                            }

                            baseView.getQuickLotBack(true,jsonObject,"",key);
                        }
                    }
                });
    }

    /***
     * 生产报修数量统计
     *
     */
    public void selectBxsl(String mono,int key){
        String cont="~mono='"+mono+"'";
        // String cont="~wxstate='"+ws+"' and  mkdate>'"+time+"'";
        baseModel.getAssistInfo(CommCL.AID_BXSL,cont).compose(ResponseTransformer.handleResult())
                .compose(schedulerProvider.applySchedulers()).subscribe(
                carBeans -> {
                    if (carBeans.getIntValue(CommCL.RTN_ID) != 0) {
                        baseView.getQuickLotBack(false, null,"获取信息失败1" , key);
                    }else if (carBeans.getIntValue(CommCL.RTN_ID) == 0) {
                        JSONObject msg = carBeans.getJSONObject(CommCL.RTN_DATA);//response里面的data，存放的是查询记录
                        JSONObject rtnMap = (JSONObject) msg.get(CommCL.RTN_DATA);//获取实际的查询结果
                        if (rtnMap.getInteger(CommCL.RTN_CODE) == 0) {
                            baseView.getQuickLotBack(false, null,"没有该"  + "工单",key);
                        } else {
                            JSONArray array = rtnMap.getJSONArray(CommCL.RTN_VALUES);
                            JSONObject jsonObject = array.getJSONObject(0);

                            baseView.getQuickLotBack(true,jsonObject,"",key);
                        }
                    }
                });
    }
    /***
     * 品质报修数量统计
     *
     */
    public void selectBxsl2(String mono,int key){
        String cont="~mono='"+mono+"'";
        // String cont="~wxstate='"+ws+"' and  mkdate>'"+time+"'";
        baseModel.getAssistInfo(CommCL.AID_BXSL2,cont).compose(ResponseTransformer.handleResult())
                .compose(schedulerProvider.applySchedulers()).subscribe(
                carBeans -> {
                    if (carBeans.getIntValue(CommCL.RTN_ID) != 0) {
                        baseView.getQuickLotBack(false, null,"获取信息失败1" , key);
                    }else if (carBeans.getIntValue(CommCL.RTN_ID) == 0) {
                        JSONObject msg = carBeans.getJSONObject(CommCL.RTN_DATA);//response里面的data，存放的是查询记录
                        JSONObject rtnMap = (JSONObject) msg.get(CommCL.RTN_DATA);//获取实际的查询结果
                        if (rtnMap.getInteger(CommCL.RTN_CODE) == 0) {
                            baseView.getQuickLotBack(false, null,"没有该"  + "工单",key);
                        } else {
                            JSONArray array = rtnMap.getJSONArray(CommCL.RTN_VALUES);
                            JSONObject jsonObject = array.getJSONObject(0);

                            baseView.getQuickLotBack(true,jsonObject,"",key);
                        }
                    }
                });
    }

    /***
     * ok数量统计
     *
     */
    public void selectOksl(String mono,int key){
        String cont="~slkid='"+mono+"'";
        baseModel.getAssistInfo(CommCL.AID_OKSL,cont).compose(ResponseTransformer.handleResult())
                .compose(schedulerProvider.applySchedulers()).subscribe(
                carBeans -> {
                    if (carBeans.getIntValue(CommCL.RTN_ID) != 0) {
                        baseView.getQuickLotBack(false, null,"获取信息失败1" , key);
                    }else if (carBeans.getIntValue(CommCL.RTN_ID) == 0) {
                        JSONObject msg = carBeans.getJSONObject(CommCL.RTN_DATA);//response里面的data，存放的是查询记录
                        JSONObject rtnMap = (JSONObject) msg.get(CommCL.RTN_DATA);//获取实际的查询结果
//                        if (rtnMap.getInteger(CommCL.RTN_CODE) == 0) {
//                            baseView.getQuickLotBack(false, null,"没有该"  + "工单",key);
//                        } else {
                        JSONArray array = rtnMap.getJSONArray(CommCL.RTN_VALUES);
                        if(array==null){
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("oksl",0);
                            jsonObject.put("slkid",mono);
                            baseView.getQuickLotBack(true,jsonObject,"",key);
                        }else{
                            JSONObject jsonObject = array.getJSONObject(0);
                            baseView.getQuickLotBack(true,jsonObject,"",key);
                        }
                        //                       }
                    }
                });
    }
    /***
     * 品质ok数量统计
     *
     */
    public void selectOksl2(String mono,int key){
        String cont="~slkid='"+mono+"'";
        baseModel.getAssistInfo(CommCL.AID_OKSL2,cont).compose(ResponseTransformer.handleResult())
                .compose(schedulerProvider.applySchedulers()).subscribe(
                carBeans -> {
                    if (carBeans.getIntValue(CommCL.RTN_ID) != 0) {
                        baseView.getQuickLotBack(false, null,"获取信息失败1" , key);
                    }else if (carBeans.getIntValue(CommCL.RTN_ID) == 0) {
                        JSONObject msg = carBeans.getJSONObject(CommCL.RTN_DATA);//response里面的data，存放的是查询记录
                        JSONObject rtnMap = (JSONObject) msg.get(CommCL.RTN_DATA);//获取实际的查询结果
//                        if (rtnMap.getInteger(CommCL.RTN_CODE) == 0) {
//                            baseView.getQuickLotBack(false, null,"没有该"  + "工单",key);
//                        } else {
                        JSONArray array = rtnMap.getJSONArray(CommCL.RTN_VALUES);
                        if(array==null){
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("oksl",0);
                            jsonObject.put("slkid",mono);
                            baseView.getQuickLotBack(true,jsonObject,"",key);
                        }else{
                            JSONObject jsonObject = array.getJSONObject(0);
                            baseView.getQuickLotBack(true,jsonObject,"",key);
                        }

                        //                       }
                    }
                });
    }

    /***
     * ng数量统计
     *
     */
    public void selectNgsl(String mono,int key){
        String cont="~slkid='"+mono+"'";
        baseModel.getAssistInfo(CommCL.AID_NGSL,cont).compose(ResponseTransformer.handleResult())
                .compose(schedulerProvider.applySchedulers()).subscribe(
                carBeans -> {
                    if (carBeans.getIntValue(CommCL.RTN_ID) != 0) {
                        baseView.getQuickLotBack(false, null,"获取信息失败1" , key);
                    }else if (carBeans.getIntValue(CommCL.RTN_ID) == 0) {
                        JSONObject msg = carBeans.getJSONObject(CommCL.RTN_DATA);//response里面的data，存放的是查询记录
                        JSONObject rtnMap = (JSONObject) msg.get(CommCL.RTN_DATA);//获取实际的查询结果
//                        if (rtnMap.getInteger(CommCL.RTN_CODE) == 0) {
//                            baseView.getQuickLotBack(false, null,"没有该"  + "工单",key);
//                        } else {
                        JSONArray array = rtnMap.getJSONArray(CommCL.RTN_VALUES);
                        if(array==null){
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("ngsl",0);
                            jsonObject.put("slkid",mono);
                            baseView.getQuickLotBack(true,jsonObject,"",key);
                        }else{
                            JSONObject jsonObject = array.getJSONObject(0);
                            baseView.getQuickLotBack(true,jsonObject,"",key);
                        }


                        //                       }
                    }
                });
    }
    /***
     * 品质ng数量统计
     *
     */
    public void selectNgsl2(String mono,int key){
        String cont="~slkid='"+mono+"'";
        baseModel.getAssistInfo(CommCL.AID_NGSL2,cont).compose(ResponseTransformer.handleResult())
                .compose(schedulerProvider.applySchedulers()).subscribe(
                carBeans -> {
                    if (carBeans.getIntValue(CommCL.RTN_ID) != 0) {
                        baseView.getQuickLotBack(false, null,"获取信息失败1" , key);
                    }else if (carBeans.getIntValue(CommCL.RTN_ID) == 0) {
                        JSONObject msg = carBeans.getJSONObject(CommCL.RTN_DATA);//response里面的data，存放的是查询记录
                        JSONObject rtnMap = (JSONObject) msg.get(CommCL.RTN_DATA);//获取实际的查询结果
//                        if (rtnMap.getInteger(CommCL.RTN_CODE) == 0) {
//                            baseView.getQuickLotBack(false, null,"没有该"  + "工单",key);
//                        } else {
                        JSONArray array = rtnMap.getJSONArray(CommCL.RTN_VALUES);
                        if(array==null){
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("ngsl",0);
                            jsonObject.put("slkid",mono);
                            baseView.getQuickLotBack(true,jsonObject,"",key);
                        }else{
                            JSONObject jsonObject = array.getJSONObject(0);
                            baseView.getQuickLotBack(true,jsonObject,"",key);
                        }

                        //                      }
                    }
                });
    }

    /**
     * 维修确认保存（确定对象定义）
     */

    public void   wxqrRecord(MESPRecord record,JSONObject batchInfo){
        baseModel.comSaveData(record,CommCL.CELL_ID_D5080).compose(ResponseTransformer.handleResult())
                .compose(schedulerProvider.applySchedulers()).subscribe(
                jsonValues -> {
                    Log.i(TAG_NAME, jsonValues.toJSONString());
                    if (jsonValues.getIntValue(CommCL.RTN_ID) != 0) {
                        baseView.saveRecordBack(false,null, "获取信息失败2" );
                    } else if (jsonValues.getIntValue(CommCL.RTN_ID) == 0) {
                        JSONObject saveBack = jsonValues.getJSONObject(CommCL.RTN_DATA);
                        String sid = saveBack.getString("sid");
                        record.setSid(sid);
                        batchInfo.put("sid",sid);
                        baseView.saveRecordBack(true, record, null);
                    }
                },
                throwable -> {
                    baseView.onRemoteFailed(throwable.getMessage());
                });
    }

    /***
     * 不良品送修信息
     *
     */
    public void selectBlpsx(String code,int key){
        String cont="~allcode='"+code+"'";
        baseModel.getAssistInfo(CommCL.AID_BLPSX,cont).compose(ResponseTransformer.handleResult())
                .compose(schedulerProvider.applySchedulers()).subscribe(
                carBeans -> {
                    if (carBeans.getIntValue(CommCL.RTN_ID) != 0) {
                        baseView.getQuickLotBack(false, null,"获取信息失败" , key);
                    }else if (carBeans.getIntValue(CommCL.RTN_ID) == 0) {
                        JSONObject msg = carBeans.getJSONObject(CommCL.RTN_DATA);//response里面的data，存放的是查询记录
                        JSONObject rtnMap = (JSONObject) msg.get(CommCL.RTN_DATA);//获取实际的查询结果
                        if (rtnMap.getInteger(CommCL.RTN_CODE) == 0) {
                            baseView.getQuickLotBack(false, null,"没有该"  + "喷码",key);
                        } else {
                            JSONArray array = rtnMap.getJSONArray(CommCL.RTN_VALUES);
                            JSONObject jsonObject = array.getJSONObject(0);
                            //校验状态
                            String state=jsonObject.getString("state");
                            int wxnum=0;
                            if(jsonObject.getInteger("wxnum")==null){
                                 wxnum=0;

                            } else{
                                wxnum=jsonObject.getInteger("wxnum");
                            }
                            if(wxnum>0){
                                baseView.getQuickLotBack(false, null, "已送修！",key);
                                return;
                            }
                            String currMONO=baseView.getCurrMO();
                            String sid=jsonObject.getString("sid");
                            if(!TextUtils.isEmpty(currMONO)&&!TextUtils.equals(sid,currMONO)){
                                baseView.getQuickLotBack(false, null, "工单不一致",key);
                                return;
                            }
                            if(CommCL.BATCH_STATUS_READY.equals(state)){
                                baseView.getQuickLotBack(false, null, "未过卡板",key);
                            }else if(CommCL.BATCH_STATUS_DONE.equals(state)){
                                baseView.getQuickLotBack(false, null, "已包装不能送修",key);
                            }else if (CommCL.BATCH_STATUS_WORKING.equals(state)) {
                                baseView.getQuickLotBack(true,jsonObject,"",key);
                            }else{
                                baseView.getQuickLotBack(false, null, "工单状态不对",key);
                                return;
                            }
                        }
                    }
                });
    }


    /**
     * 不良品送修（确定对象定义）
     */

    public void   blpsxRecord(MESPRecord record,JSONObject batchInfo){
        baseModel.comSaveData(record,CommCL.CELL_ID_D5064).compose(ResponseTransformer.handleResult())
                .compose(schedulerProvider.applySchedulers()).subscribe(
                jsonValues -> {
                    Log.i(TAG_NAME, jsonValues.toJSONString());
                    if (jsonValues.getIntValue(CommCL.RTN_ID) != 0) {
                        baseView.saveMoCallBack(false,null, "获取信息失败" );
                    } else if (jsonValues.getIntValue(CommCL.RTN_ID) == 0) {
                        JSONObject saveBack = jsonValues.getJSONObject(CommCL.RTN_DATA);
                        String lotno = saveBack.getString("sid");
                        record.setSid(lotno);
                        batchInfo.put("lotno",lotno);
                        baseView.saveMoCallBack(true, batchInfo ,null);
                    }
                },
                throwable -> {
                    baseView.onRemoteFailed(throwable.getMessage());
                });
    }

    /**
     * 不良品送修 表身（确定对象定义）
     */

    public void   savePackInfo(JSONObject record,JSONObject batchInfo){
        baseModel.comSaveData(record,CommCL.CELL_ID_D5064A).compose(ResponseTransformer.handleResult())
                .compose(schedulerProvider.applySchedulers()).subscribe(
                jsonValues -> {
                    Log.i(TAG_NAME, jsonValues.toJSONString());
                    if (jsonValues.getIntValue(CommCL.RTN_ID) != 0) {
                        baseView.saveMarkingCallBack(false,null, "获取信息失败" );
                    } else if (jsonValues.getIntValue(CommCL.RTN_ID) == 0) {
                        baseView.saveMarkingCallBack(true, batchInfo ,null);
                    }
                },
                throwable -> {
                    baseView.onRemoteFailed(throwable.getMessage());
                });
    }


    public void getPackInfoById(String id,int key){
        String cont="~sid='"+id+"'";
        baseModel.getAssistInfo(CommCL.AID_BLPSX2,cont).compose(ResponseTransformer.handleResult())
                .compose(schedulerProvider.applySchedulers()).subscribe(
                carBeans -> {
                    if (carBeans.getIntValue(CommCL.RTN_ID) != 0) {
                        baseView.getPackInfoCallBack(false, null,null,"获取喷码信息失败", key);
                    }else if (carBeans.getIntValue(CommCL.RTN_ID) == 0) {
                        JSONObject msg = carBeans.getJSONObject(CommCL.RTN_DATA);//response里面的data，存放的是查询记录
                        JSONObject rtnMap = (JSONObject) msg.get(CommCL.RTN_DATA);//获取实际的查询结果
                        if (rtnMap.getInteger(CommCL.RTN_CODE) == 0) {
                            baseView.getPackInfoCallBack(false, null,null,"喷码信息不存在",key);
                        } else {
                            JSONArray array = rtnMap.getJSONArray(CommCL.RTN_VALUES);
                            if(array==null||array.size()==0){
                                baseView.getPackInfoCallBack(false, null,null,id+"喷码信息不存在",key);
                                return;
                            }
                            JSONObject jsonObject = array.getJSONObject(0);

                            JSONArray arr=new JSONArray();
                            for(int i=0;i<array.size();i++){
                                JSONObject temp=array.getJSONObject(i);
                                String lotno=temp.getString("sid");
                                String sid1=temp.getString("sid1");
                                int qty=temp.getInteger("qty");
                               // int wxngqty=temp.getInteger("wxngqty");
                                MESPRecord record = new MESPRecord(sid1,"", "","");
                               // record.setQty(wxngqty);

                                arr.add(record);
                            }
                            jsonObject.put("wxngqty",array.size());
                            baseView.getPackInfoCallBack(true,jsonObject,array,"",key);
                        }
                    }
                });
    }
    /**
     * 通用查询
     * @param assistId
     * @param cont
     * @param key
     */
    public void getAssistInfo(String assistId,String cont,int key) {
        baseModel.getAssistInfo(assistId,cont).compose(ResponseTransformer.handleResult())
                .compose(schedulerProvider.applySchedulers()).subscribe(
                carBeans -> {
                    if (carBeans.getIntValue(CommCL.RTN_ID) != 0) {
                        baseView.getAssistInfoBack(false, null,"获取服务器信息失败" + carBeans.toString(), 0);
                    }else if (carBeans.getIntValue(CommCL.RTN_ID) == 0) {
                        JSONObject msg = carBeans.getJSONObject(CommCL.RTN_DATA);//response里面的data，存放的是查询记录
                        JSONObject rtnMap = (JSONObject) msg.get(CommCL.RTN_DATA);//获取实际的查询结果
                        if (rtnMap.getInteger(CommCL.RTN_CODE) == 0) {
                            baseView.getAssistInfoBack(false, null,"没有查询到记录"+assistId+cont+";", key);
                        } else {
                            JSONArray array = rtnMap.getJSONArray(CommCL.RTN_VALUES);//返回数据
                            baseView.getAssistInfoBack(true, array, "", key);
                        }
                    }
                },throwable -> {
                    baseView.onRemoteFailed(throwable.getMessage());
                });
    }





}
