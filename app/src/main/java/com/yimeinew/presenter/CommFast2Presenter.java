package com.yimeinew.presenter;

import android.text.TextUtils;
import android.util.Log;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.shade.com.alibaba.fastjson.JSONArray;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.shade.com.alibaba.fastjson.JSONObject;
import com.yimeinew.data.MESPRecord;
import com.yimeinew.model.impl.CommZCModel;
import com.yimeinew.modelInterface.CommFastView;
import com.yimeinew.network.response.ResponseTransformer;
import com.yimeinew.network.schedulers.SchedulerProvider;
import com.yimeinew.utils.CommCL;
import com.yimeinew.utils.DateUtil;

import java.util.HashMap;

public class CommFast2Presenter {
    private final String TAG_NAME = CommFast2Presenter.class.getSimpleName();
    private CommFastView baseView;//通用工站回调接口
    private SchedulerProvider schedulerProvider; //线程池
    private CommZCModel baseModel;//服务端处理接口
    public CommFast2Presenter(CommFastView baseStationBindingView, SchedulerProvider schedulerProvider) {
        this.baseView = baseStationBindingView;
        this.schedulerProvider = schedulerProvider;
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
        baseModel.getQuickLot2(sid1,zcno).compose(ResponseTransformer.handleResult())
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
                            if (!CommCL.BATCH_STATUS_DONE.equals(stateValue)) {
                                baseView.getQuickLotBack(false, null, "该批次【" + sid1 + "】没有做过一次清洗，请先做一次清洗",key);
                                return;
                            }

                            //校验状态
                            String stateValue2 = jsonObject.getString("state2");
                            if (CommCL.BATCH_STATUS_DONE.equals(stateValue2)) {
                                baseView.getQuickLotBack(false, null, "该批次【" + sid1 + "】已经做过二次清洗，无法再次清洗",key);
                                return;
                            }
                            if (CommCL.BATCH_STATUS_WORKING.equals(stateValue2)) {
                                baseView.getQuickLotBack(false, null, "该批次【" + sid1 + "】正在生产中,不能再次入站！",key);
                                return;
                            }
                            if (CommCL.BATCH_STATUS_CHARGING.equals(stateValue2)) {
                                baseView.getQuickLotBack(false, null, "该批次【" + sid1 + "】已经上料,不能再次入站！",key);
                                return;
                            }
                            if (CommCL.BATCH_STATUS_IN.equals(stateValue2)) {
                                baseView.getQuickLotBack(false, null, "该批次【" + sid1 + "】已经别的机台入站,不能再次入站！",key);
                                return;
                            }
                            //一次清洗超过6小时才能进行二次清洗
                            String qxTime=jsonObject.getString("edate");
                            int subTime= DateUtil.subSecond(DateUtil.getNowCurrDateTime(),qxTime);
                            if((TextUtils.equals("1P",zcno) || TextUtils.equals("313",zcno)) &&subTime<CommCL.HANXIAN_QINGXI_MAX_WAIT_TIME){
                                baseView.getQuickLotBack(false,null,"一次过站时间="+qxTime+"未超时不需要二次过站，请直接做下一道工序",key);
                                return;
                            }
                            //一次预热超过10小时才能进行二次清洗
                            if(TextUtils.equals("311",zcno) &&subTime<CommCL.DIANJIAO_YURE_MAX_WAIT_TIME){
                                baseView.getQuickLotBack(false,null,"一次过站时间="+qxTime+"未超时不需要二次过站，请直接做下一道工序",key);
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
        baseModel.saveQuickData2(record, "3").compose(ResponseTransformer.handleResult())
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
     * 200修改生产批次状态（批次快速过站）这里不需要
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

}
