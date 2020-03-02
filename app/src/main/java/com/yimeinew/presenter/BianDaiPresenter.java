package com.yimeinew.presenter;

import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.util.Log;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.shade.com.alibaba.fastjson.JSONArray;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.shade.com.alibaba.fastjson.JSONObject;

import com.yimeinew.data.CWorkInfo;
import com.yimeinew.data.CeaPars;
import com.yimeinew.data.EquipmentInfo;
import com.yimeinew.data.MESPRecord;
import com.yimeinew.data.qc.QCBatchInfo;
import com.yimeinew.listener.OnConfirmListener;
import com.yimeinew.model.impl.CommZCModel;
import com.yimeinew.modelInterface.BaseStationBindingView;
import com.yimeinew.modelInterface.CommBaseView;
import com.yimeinew.network.response.ResponseTransformer;
import com.yimeinew.network.schedulers.SchedulerProvider;
import com.yimeinew.utils.CommCL;
import com.yimeinew.utils.CommonUtils;
import com.yimeinew.utils.DateUtil;
import com.yimeinew.utils.ICL;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BianDaiPresenter {
    private final String TAG_NAME = BianDaiPresenter.class.getSimpleName();
    private BaseStationBindingView baseView;//通用工站回调接口
    private SchedulerProvider schedulerProvider; //线程池
    private CommZCModel baseModel;//服务端处理接口
    public BianDaiPresenter(BaseStationBindingView baseStationBindingView, SchedulerProvider schedulerProvider) {
        this.baseView = baseStationBindingView;
        this.schedulerProvider = schedulerProvider;
        if (baseModel == null) {
            baseModel = new CommZCModel();
        }
    }

    /***
     * 获取相关制成的批次信息
     * @param sid1 批次号
     * @param zcNo 制成号
     */
    public void getBatchInfo(String sid1, String zcNo, EquipmentInfo currEquipment, Context context) {
        baseModel.getBatchRecordByLotNoAndZcNo(sid1, zcNo).compose(ResponseTransformer.handleResult())
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

                            String stateValue = jsonObject.getString("lotstate");
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

                            if (TextUtils.equals(CommCL.LOT_STATUS_KANDAI,stateValue)) {
                                baseView.checkSidCallBack(false, null, "该批次【" + sid1 + "】已经看带！");
                                return;
                            }
                            String state1 = jsonObject.getString("state2");
                            if (CommCL.BATCH_STATUS_WORKING.equals(state1)) {
                                baseView.checkSidCallBack(false, null, "该批次【" + sid1 + "】正在生产中,不能再次入站！");
                                return;
                            }
                            if (CommCL.BATCH_STATUS_CHARGING.equals(state1)) {
                                baseView.checkSidCallBack(false, null, "该批次【" + sid1 + "】已经上料,不能再次入站！");
                                return;
                            }
                            if (CommCL.BATCH_STATUS_IN.equals(state1)) {
                                baseView.checkSidCallBack(false, null, "该批次【" + sid1 + "】已经别的机台入站,不能再次入站！");
                                return;
                            }
                            //校验状态

                            if (TextUtils.equals(CommCL.LOT_STATUS_BIANDAI,stateValue)) {
                                baseView.checkSidCallBack(false, null, "该批次【" + sid1 + "】已经编带！");
                                return;
                            }
                            if (CommCL.BATCH_STATUS_DONE.equals(state1)) {
                                baseView.checkSidCallBack(false, null, "该批次【" + sid1 + "】已出站但是lotstate还没有处理完请稍后再试！");
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

                            //编带机种校验，bincode校验
                            List<JSONObject> dataList=baseView.getDataList();
                            if(dataList!=null&&dataList.size()>0){
                                JSONObject jsonObj=dataList.get(0);//获取最后一个
                                String d_prd_no=jsonObj.getString("prd_no");
                                String d_bincode=jsonObj.getString("bincode");
                                String prd_no=jsonObject.getString("prd_no");
                                String bincode=jsonObject.getString("bincode");
                                if(!TextUtils.equals(d_prd_no,prd_no)){
                                    baseView.checkSidCallBack(true, jsonObject, "当前的机种是：【"+d_prd_no+"】，扫描的机种是：【"+prd_no+"】，两者不一致");
                                    return;
                                }
                                if(!TextUtils.equals(d_bincode,bincode)){
                                    baseView.checkSidCallBack(true, jsonObject, "当前的Bincode是：【"+d_bincode+"】，扫描的Bincode是：【"+bincode+"】，两者不一致");
                                    return;
                                }
                            }
                            if(jsonObject.getInteger("qty")<=jsonObject.getInteger("rollqty")){
                                CommonUtils.confirm(context, "不是整卷", "此卷物料不是整卷数量是否加工【" + jsonObject.getInteger("qty")+"】", null, new OnConfirmListener() {
                                    @Override
                                    public void OnConfirm(DialogInterface dialog) {
                                        baseView.checkSidCallBack(true, jsonObject, null);
                                    }
                                    @Override
                                    public void OnCancel(DialogInterface dialog) {
                                        baseView.checkSidCallBack(false, null, "取消入站");
                                    }
                                });
                            }else {
                                baseView.checkSidCallBack(true, jsonObject, null);
                            }
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
        String zzcno = zcNo;
        if (zzcno.equals("12") || zzcno.equals("13")) {
            zzcno = "11";
        }
        if (zzcno.equals("1A") || zzcno.equals("1B")) {
            zzcno = "1A";
        }
        if (zzcno.equals("41")) {
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
        baseModel.getRecordBySbIdAndZcnoBD(sbId, zcNo).compose(ResponseTransformer.handleResult())
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

                            //cache.put(box,data.getString("prd_no"));

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
                        baseView.checkMboxCallBack(false, "获取服务器信息失败" + jsonValues.toString(),0);
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
        jsonObject.put(CommCL.COMM_LOTNO_FLD, record.getLotno());
        jsonObject.put(CommCL.COMM_OP_FLD, record.getOp());
        jsonObject.put(CommCL.COMM_ZC_NO_FLD, record.getZcno());
        JSONArray array = new JSONArray();
        array.add(jsonObject);
        baseModel.changeRecordState(array.toJSONString(), CommCL.COMM_MES_UDP_CHANGE_STATE_RECORD_VALUE)
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
                    baseView.onRemoteFailed(throwable.getMessage());
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
                        //出站开工入站返回数据调用280
                            changeRecordStateBatch280(recordList, batchStatusIn);

                        for (int i = 0; i < recordList.size(); i++){
                            recordList.get(i).setState1(batchStatusIn);
                            if(batchStatusIn.equals(CommCL.BATCH_STATUS_WORKING))
                                recordList.get(i).setHpdate(DateUtil.getCurrDateTime(ICL.DF_YMDT));
                        }

                        baseView.changeMultiRecordStateBack(true, recordList, batchStatusIn);
                    }
                },
                throwable -> {
                    baseView.onRemoteFailed(throwable.getMessage());
                });
    }

    /**
     * 280修改编带生产批次状态（前端）入站专用
     * @param record
     * @param batchStatusIn
     */
    public void changeTestLotStateOneByOne(MESPRecord record, String batchStatusIn){
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(CommCL.COMM_OLD_STATE_FLD, CommCL.BATCH_STATUS_IN.equals(batchStatusIn)?"00":record.getState1());
        jsonObject.put(CommCL.COMM_NEW_STATE_FLD, batchStatusIn);
        jsonObject.put(CommCL.COMM_SLKID_FLD, record.getSlkid());
        jsonObject.put(CommCL.COMM_ZC_NO_FLD, record.getZcno());
        jsonObject.put(CommCL.COMM_RECORD_SID_FLD, record.getSid1()+";"+record.getLotno()+";"+record.getSbid());
        jsonObject.put(CommCL.COMM_OP_FLD, record.getOp());
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
     * 批次提交修改生产状态mes_test_lot出站专用
     * @param recordList 生产记录数组
     * @param batchStatusIn 新状态
     */
    public void changeRecordStateBatch280(List<MESPRecord> recordList, String batchStatusIn) {
        JSONArray array = new JSONArray();
        for (int i = 0; i < recordList.size(); i++) {
            MESPRecord record = recordList.get(i);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(CommCL.COMM_OLD_STATE_FLD, record.getState1());
            jsonObject.put(CommCL.COMM_NEW_STATE_FLD, batchStatusIn);
            jsonObject.put(CommCL.COMM_RECORD_SID_FLD, record.getSid1()+";"+record.getLotno()+";"+record.getSbid());
            jsonObject.put(CommCL.COMM_SLKID_FLD, record.getSlkid());
            jsonObject.put(CommCL.COMM_OP_FLD, record.getOp());
            jsonObject.put(CommCL.COMM_ZC_NO_FLD, record.getZcno());
            array.add(jsonObject);

        }
        baseModel.changeRecordState(array.toJSONString(), CommCL.COMM_MES_UDP_CHANGE_STATE_TESTLOT_VALUE)
                .compose(ResponseTransformer.handleResult())
                .compose(schedulerProvider.applySchedulers()).subscribe(
                jsonValues -> {
                    Log.i(TAG_NAME, jsonValues.toJSONString());
                    if (jsonValues.getIntValue(CommCL.RTN_ID) == -1) {//更改批次，成功返回id=1
                        baseView.changeMultiRecordStateBack(false, recordList, "获取服务器信息失败" + jsonValues.toString());
                    } else {
                        //280更新成功
                    }
                },
                throwable -> {
                    baseView.onRemoteFailed(throwable.getMessage());
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
        jsonstr.put(CommCL.SAVE_DATA_STATE, bFirst ? "3" : "2");
        jsonstr.put("dcid", CommonUtils.getMacID());
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
        jsonObject.put(CommCL.COMM_LOTNO_FLD, record.getLotno());
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
    //更新编带test_lot表数量
    public void updateTestLotQty(MESPRecord record,String op,int qty){
        JSONObject jsonObject=new JSONObject();
        jsonObject.put(CommCL.COMM_RECORD_SID_FLD,record.getSlkid());
        jsonObject.put(CommCL.COMM_LOTNO_FLD,record.getLotno());
        jsonObject.put(CommCL.COMM_QTY_FLD,qty);
        jsonObject.put(CommCL.SAVE_DATA_STATE,2);
        Log.i(TAG_NAME,jsonObject.toJSONString());
        baseModel.updateDataAID(jsonObject,CommCL.LOT_QJ_BIANDAI_LOTQTY_XIUGAI).compose(ResponseTransformer.handleResult())
                .compose(schedulerProvider.applySchedulers()).subscribe(
                carBeans -> {
                    if (carBeans.getIntValue(CommCL.RTN_ID) != 0) {
                        baseView.getMultiRecordBack(false,null,"mes_test_lot 修改失败",1);
                    }else if (carBeans.getIntValue(CommCL.RTN_ID) == 0) {
                        updateMesPrecordQty(record,op,qty);
                        //JSONObject msg = carBeans.getJSONObject(CommCL.RTN_DATA);//response里面的data，存放的是查询记录
                    }
                });
    }
    //更新批次数量
    public void updateMesPrecordQty(MESPRecord record,String op,int qty){
        JSONObject jsonObject=new JSONObject();
        jsonObject.put(CommCL.COMM_RECORD_SID_FLD,record.getSid());
        jsonObject.put(CommCL.COMM_LOTNO_FLD,record.getLotno());
        jsonObject.put(CommCL.COMM_QTY_FLD,qty);
        jsonObject.put(CommCL.SAVE_DATA_STATE,2);
        Log.i(TAG_NAME,jsonObject.toJSONString());
        baseModel.updateDataAID(jsonObject,CommCL.LOT_QJ_BIANDAI_QTY_XIUGAI).compose(ResponseTransformer.handleResult())
                .compose(schedulerProvider.applySchedulers()).subscribe(
                carBeans -> {
                    if (carBeans.getIntValue(CommCL.RTN_ID) != 0) {
                        baseView.getMultiRecordBack(false,null,"mes_precode 修改失败",2);
                    }else if (carBeans.getIntValue(CommCL.RTN_ID) == 0) {
                        //JSONObject msg = carBeans.getJSONObject(CommCL.RTN_DATA);//response里面的data，存放的是查询记录
                        //System.out.println("woshifh="+msg.toJSONString());
                        saveMesPrecord_EMQty( record, op,qty);
                    }
                });
    }
    //插入编带数量[修改记录表]
    public void saveMesPrecord_EMQty(MESPRecord record,String op,int qty){
        JSONObject jsonObject=new JSONObject();
        jsonObject.put("c_corp",0);//c_corp
        jsonObject.put(CommCL.COMM_RECORD_SID_FLD,record.getSlkid());//sid工单
        jsonObject.put(CommCL.COMM_LOTNO_FLD,record.getLotno());//lotno
        jsonObject.put("c_time",DateUtil.getCurrDateTime(ICL.DF_YMDT));//c_time
        jsonObject.put("c_user",op);//c_user
        jsonObject.put("c_bz","");
        jsonObject.put(CommCL.COMM_QTY_FLD,record.getQty());
        jsonObject.put("qty_fr",qty);
        jsonObject.put(CommCL.SAVE_DATA_STATE,3);
        Log.i(TAG_NAME,jsonObject.toJSONString());
        baseModel.saveData(jsonObject,CommCL.LOT_QJ_BIANDAI_QTY_XIUGAI_JILU).compose(ResponseTransformer.handleResult())
                .compose(schedulerProvider.applySchedulers()).subscribe(
                carBeans -> {
                    if (carBeans.getIntValue(CommCL.RTN_ID) != 0) {
                        baseView.getMultiRecordBack(false,null,"添加修改记录失败",3);
                    }else if (carBeans.getIntValue(CommCL.RTN_ID) == 0) {
                        //JSONObject msg = carBeans.getJSONObject(CommCL.RTN_DATA);//response里面的data，存放的是查询记录
                        //JSONObject rtnMap = (JSONObject) msg.get(CommCL.RTN_DATA);//获取实际的查询结果
                        JSONArray jsonArray=new JSONArray();
                        record.setQty(qty);
                        jsonArray.add(record);
                        baseView.getMultiRecordBack(true,jsonArray,"",3);
                    }
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


}
