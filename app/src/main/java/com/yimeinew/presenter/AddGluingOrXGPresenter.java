package com.yimeinew.presenter;

import android.util.Log;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.shade.com.alibaba.fastjson.JSONArray;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.shade.com.alibaba.fastjson.JSONObject;
import com.yimeinew.data.EquipmentInfo;
import com.yimeinew.data.GluingInfo;
import com.yimeinew.data.MesGluingRecord;
import com.yimeinew.model.impl.BaseModel;
import com.yimeinew.modelInterface.BaseStationBindingView;
import com.yimeinew.network.response.ResponseTransformer;
import com.yimeinew.network.schedulers.SchedulerProvider;
import com.yimeinew.utils.CommCL;
import com.yimeinew.utils.CommonUtils;

/**
 * @Auther: fengzejiang1987@163.com
 * @Date : 2019/1/3 14:37
 */
public class AddGluingOrXGPresenter {
    private final String TAG_NAME = AddGluingOrXGPresenter.class.getSimpleName();
    private BaseStationBindingView baseView;
    private SchedulerProvider schedulerProvider;
    private BaseModel gluingModel;

    public AddGluingOrXGPresenter(BaseStationBindingView baseStationBindingView, SchedulerProvider schedulerProvider) {
        this.baseView = baseStationBindingView;
        this.schedulerProvider = schedulerProvider;
        if (gluingModel == null) {
            gluingModel = new BaseModel();
        }
    }

    /***
     * 获取设备上的生产记录
     * @param currSbId
     */
    public void getLatelyMesRecord(String currSbId) {
        String cont = "~sbid='" + currSbId + "'";
        gluingModel.getAssistInfo(CommCL.AID_P_RECORD_GLUING_ID, cont)
                .compose(ResponseTransformer.handleResult())
                .compose(schedulerProvider.applySchedulers())
                .subscribe(
                        jsonObject -> {
                            Log.i(TAG_NAME, jsonObject.toJSONString());
                            if (jsonObject.getIntValue(CommCL.RTN_ID) != 0) {
                                baseView.getMultiRecordBack(false, null, jsonObject.getString(CommCL.RTN_MESSAGE), 0);
                            } else {
                                JSONObject msg = jsonObject.getJSONObject(CommCL.RTN_DATA);//response里面的data，存放的是查询记录
                                JSONObject rtnMap = (JSONObject) msg.get(CommCL.RTN_DATA);//获取实际的查询结果
                                if (rtnMap.getInteger(CommCL.RTN_CODE) == 0) {
                                    baseView.getMultiRecordBack(false, null, "设备号【" + currSbId + "】没有生产记录", 0);
                                } else {
                                    JSONArray array = rtnMap.getJSONArray(CommCL.RTN_VALUES);
                                    baseView.getMultiRecordBack(true, array, null, 0);
                                }
                            }
                        }, throwable -> {
                            baseView.onRemoteFailed(CommonUtils.traceException0(throwable));
                        });
    }

    public void getEquipMentInfo(String sbId) {
        gluingModel.getEquipmentInf(sbId, "31").compose(ResponseTransformer.handleResult())
                .compose(schedulerProvider.applySchedulers())
                .subscribe(jsonObject -> {
                            Log.i(TAG_NAME, jsonObject.toJSONString());
                            if (jsonObject.getIntValue(CommCL.RTN_ID) != 0) {
                                baseView.checkSbIdCallBack(false, null, jsonObject.getString(CommCL.RTN_MESSAGE));
                            } else {
                                JSONObject msg = jsonObject.getJSONObject(CommCL.RTN_DATA);//response里面的data，存放的是查询记录
                                JSONObject rtnMap = (JSONObject) msg.get(CommCL.RTN_DATA);//获取实际的查询结果
                                if (rtnMap.getInteger(CommCL.RTN_CODE) == 0) {
                                    baseView.checkSbIdCallBack(false, null, "设备号【" + sbId + "】不存在");
                                } else {
                                    JSONArray array = rtnMap.getJSONArray(CommCL.RTN_VALUES);
                                    JSONObject equipJson = array.getJSONObject(0);
                                    EquipmentInfo equipmentInfo = JSONObject.parseObject(equipJson.toJSONString(), EquipmentInfo.class);
                                    baseView.checkSbIdCallBack(true, equipmentInfo, null);
                                }
                            }
                        },
                        throwable -> {
                            baseView.onRemoteFailed(throwable.getLocalizedMessage());
                        });
    }

    /***
     * 获取胶杯记录
     * 借用加载检验发起原因回调函数，在前段界面与用户交互
     * @param str 胶杯号
     */
    public void getGluingInfo(String str) {
        String cont = "~prtno='" + str + "' and state=0";
        gluingModel.getAssistInfo(CommCL.AID_GLUING_INFO_ID, cont)
                .compose(ResponseTransformer.handleResult())
                .compose(schedulerProvider.applySchedulers())
                .subscribe(jsonObject -> {
                    Log.i(TAG_NAME, jsonObject.toJSONString());
                    if (jsonObject.getIntValue(CommCL.RTN_ID) != 0) {
                        baseView.loadReasonsBack(false, null, jsonObject.getString(CommCL.RTN_MESSAGE));
                    } else {
                        JSONObject msg = jsonObject.getJSONObject(CommCL.RTN_DATA);//response里面的data，存放的是查询记录
                        JSONObject rtnMap = (JSONObject) msg.get(CommCL.RTN_DATA);//获取实际的查询结果
                        if (rtnMap.getInteger(CommCL.RTN_CODE) == 0) {
                            baseView.loadReasonsBack(false, null, "胶杯号【" + str + "】不存在或者已经使用完");
                        } else {
                            JSONArray array = rtnMap.getJSONArray(CommCL.RTN_VALUES);
                            GluingInfo gluingInfo = JSONObject.parseObject(array.getJSONObject(0).toJSONString(), GluingInfo.class);
                            baseView.loadReasonsBack(true, gluingInfo, null);
                        }
                    }
                }, throwable -> {
                    baseView.onRemoteFailed(CommonUtils.traceException0(throwable));
                });
    }

    public void saveGluingRecord(MesGluingRecord mesGluingRecord) {
        JSONObject json = CommonUtils.getJsonObjFromBean(mesGluingRecord);
        gluingModel.saveData(json, CommCL.CELL_ID_D2010)
                .compose(ResponseTransformer.handleResult())
                .compose(schedulerProvider.applySchedulers())
                .subscribe(jsonObject -> {
                    if (jsonObject.getIntValue(CommCL.RTN_ID) != 0) {
                        baseView.saveRecordBack(false, null,"获取服务器信息失败" + jsonObject.toString());
                    }else{
                        JSONObject saveBack = jsonObject.getJSONObject(CommCL.RTN_DATA);
                        String sid = saveBack.getString("sid");
                        mesGluingRecord.setSid(sid);
                        baseView.saveRecordBack(true, mesGluingRecord,null);
                    }
                }, throwable -> {
                    baseView.onRemoteFailed(CommonUtils.traceException0(throwable));
                });
    }

    /***
     * 系统设备添加胶
     * @param sbId 设备号
     * @param prtNo 胶杯号
     */
    public void doServerAddGluing(String sbId, String prtNo) {
        gluingModel.LotAddGluingOrXg(sbId,prtNo,CommCL.COMM_MES_UDP_GLUING_VALUE)
        .compose(ResponseTransformer.handleResult())
        .compose(schedulerProvider.applySchedulers())
        .subscribe(jsonObject -> {
            Log.i(TAG_NAME, jsonObject.toJSONString());
            if (jsonObject.getIntValue(CommCL.RTN_ID) == -1) {//更改批次，成功返回id=1
                baseView.changeRecordStateBack(false, null, "获取服务器信息失败" + jsonObject.toString());
            } else {
                baseView.changeRecordStateBack(true, null, null);
            }
        },throwable -> {
            baseView.onRemoteFailed(CommonUtils.traceException0(throwable));
        });
    }

//    /***
//     * 系统设备添加锡膏
//     * @param sbId  设备号
//     * @param prtNo 锡膏号
//     */
//    public void doServerAddXG(String sbId, String prtNo) {
//        gluingModel.LotAddGluingOrXg(sbId,prtNo,CommCL.COMM_MES_UDP_XG_VALUE);
//    }
}
