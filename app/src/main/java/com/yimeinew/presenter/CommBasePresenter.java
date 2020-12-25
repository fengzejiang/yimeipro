package com.yimeinew.presenter;

import android.text.TextUtils;
import android.util.Log;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.shade.com.alibaba.fastjson.JSONArray;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.shade.com.alibaba.fastjson.JSONObject;
import com.yimeinew.data.CWorkInfo;
import com.yimeinew.data.CeaPars;
import com.yimeinew.model.impl.CommZCModel;
import com.yimeinew.modelInterface.CommBaseView;
import com.yimeinew.network.response.ResponseTransformer;
import com.yimeinew.network.schedulers.SchedulerProvider;
import com.yimeinew.utils.CommCL;
import com.yimeinew.utils.CommonUtils;
import com.yimeinew.utils.ToolUtils;

import java.util.HashMap;

public class CommBasePresenter {
    private final String TAG_NAME = CommBasePresenter.class.getSimpleName();
    private CommBaseView baseView;//通用工站回调接口
    private SchedulerProvider schedulerProvider; //线程池
    private CommZCModel baseModel;//服务端处理接口
    public CommBasePresenter(CommBaseView baseStationBindingView, SchedulerProvider schedulerProvider) {
        this.baseView = baseStationBindingView;
        this.schedulerProvider = schedulerProvider;
        if (baseModel == null) {
            baseModel = new CommZCModel();
        }
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
                        baseView.getAssistInfoBack(false, null,"获取服务器信息失败" + carBeans.toString(), key);
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
    //通用插入
    public void saveData(String insObject,JSONObject record,int key){
        baseModel.comSaveData(record,insObject).compose(ResponseTransformer.handleResult())
                .compose(schedulerProvider.applySchedulers()).subscribe(
                carBeans -> {
                    if (carBeans.getIntValue(CommCL.RTN_ID) != 0) {
                        baseView.saveDataBack(false, null,record,"获取服务器信息失败" + carBeans.toString(), key);
                    }else if (carBeans.getIntValue(CommCL.RTN_ID) == 0) {
                        JSONObject msg = carBeans.getJSONObject(CommCL.RTN_DATA);//response里面的data，存放的是查询记录
                        //JSONObject rtnMap = (JSONObject) msg.get(CommCL.RTN_DATA);//获取实际的查询结果
                        if (msg.size() == 0) {
                            baseView.saveDataBack(true, null,record,"记录新增没有返回主键", key);
                        } else {
                           JSONArray array = new JSONArray();//返回数据
                            //JSONObject data = array.getJSONObject(0);
                            array.add(msg);
                            baseView.saveDataBack(true,array,record,"",key);
                        }
                    }
                },throwable -> {
                    baseView.onRemoteFailed(throwable.getMessage());
                });
    }
    //通用更新
    public void updateData(String insObject,Object record,int key){
        baseModel.comUpdateData(record,insObject).compose(ResponseTransformer.handleResult())
                .compose(schedulerProvider.applySchedulers()).subscribe(
                carBeans -> {
                    if (carBeans.getIntValue(CommCL.RTN_ID) != 0) {
                        baseView.updateDataBack(false, null,"获取服务器信息失败" + carBeans.toString(), key);
                    }else if (carBeans.getIntValue(CommCL.RTN_ID) == 0) {
                        Log.i(TAG_NAME, carBeans.toJSONString());
                        if (carBeans.getIntValue(CommCL.RTN_ID) != 0) {
                            baseView.updateDataBack(false, null,"记录更新失败", key);
                        } else if (carBeans.getIntValue(CommCL.RTN_ID) == 0) {
                            JSONArray array=new JSONArray();
                            array.add(record);
                            baseView.updateDataBack(true,array,"",key);
                        }
                    }
                },throwable -> {
                    baseView.onRemoteFailed(throwable.getMessage());
                });
    }

    /**
     * 通调用200、等等
     * @param jsonObject
     * @param udpid
     * @param key
     */
    public void changeLotStateOneByOne(HashMap<String,String> jsonObject,String udpid,int key){
        JSONArray array = new JSONArray();
        array.add(jsonObject);
        baseModel.changeRecordState2(jsonObject,udpid)
                .compose(ResponseTransformer.handleResult())
                .compose(schedulerProvider.applySchedulers()).subscribe(
                jsonValues -> {
                    Log.i(TAG_NAME, jsonValues.toJSONString());
                    if (jsonValues.getIntValue(CommCL.RTN_ID) == -1) {//更改批次，成功返回id=1
                        baseView.changeRecordStateBack(false, array, "获取服务器信息失败" + jsonValues.toString(),key);
                    } else {
                        baseView.changeRecordStateBack(true, array, null,key);
                    }
                },
                throwable -> {
                    baseView.onRemoteFailed("280修改状态"+throwable.getMessage());
                });
    }
    /**
     * 通调用280、等等批量更新
     * @param array
     * @param udpid
     * @param key
     */
    public void changeLotStateMore(JSONArray array ,String udpid,int key){
        baseModel.changeRecordState(array.toJSONString(),udpid)
                .compose(ResponseTransformer.handleResult())
                .compose(schedulerProvider.applySchedulers()).subscribe(
                jsonValues -> {
                    Log.i(TAG_NAME, jsonValues.toJSONString());
                    if (jsonValues.getIntValue(CommCL.RTN_ID) == -1) {//更改批次，成功返回id=1
                        baseView.changeRecordStateBack(false, array, "获取服务器信息失败" + jsonValues.toString(),key);
                    } else {
                        baseView.changeRecordStateBack(true, array, null,key);
                    }
                },
                throwable -> {
                    baseView.onRemoteFailed("280修改状态"+throwable.getMessage());
                });
    }
    //-------------审核流---------------

    /**
     * 获取审核流程
     * @param ceaPars
     */
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

    /**
     * 提交审核流程
     * @param ceaPars
     */
    public synchronized void checkActionUp(CeaPars ceaPars) {
        //重复提交拦截
        if(CommonUtils.isRepeat("shenhe"+ceaPars.getSid(),"shenhe"+ceaPars.getSid(),8000)){
            return;
        }
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
                    baseView.onRemoteFailed("34"+throwable.getLocalizedMessage());
                });
    }

    public void printLable(HashMap<String,String> hm,int key){
        if(TextUtils.isEmpty(hm.get("print_id"))){
            baseView.commCallBack(false, null,"请设置打印机", key);
            return;
        }
        baseModel.doPrintServer(hm).compose(ResponseTransformer.handleResult())
                .compose(schedulerProvider.applySchedulers()).subscribe(
                carBeans -> {
                    if (carBeans.getIntValue(CommCL.RTN_ID) != 0) {
                        baseView.commCallBack(false, null,"获取信息失败21", key);
                    }else if (carBeans.getIntValue(CommCL.RTN_ID) == 0) {
                        JSONObject msg = carBeans.getJSONObject(CommCL.RTN_DATA);//response里面的data，存放的是查询记录
                        JSONObject rtnMap = msg.getJSONObject(CommCL.RTN_DATA);//获取实际的查询结果
                        System.out.println("woshi"+rtnMap.toJSONString());
                        baseView.commCallBack(rtnMap.getBoolean("bok"),rtnMap.getJSONObject("info"),rtnMap.getString("error"),key);
                    }
                });
    }
    public void printNum(HashMap<String,String> hm,int num,int key){
        baseModel.doPrintServer(hm).compose(ResponseTransformer.handleResult())
                .compose(schedulerProvider.applySchedulers()).subscribe(
                carBeans -> {
                    if (carBeans.getIntValue(CommCL.RTN_ID) != 0) {
                        baseView.commCallBack(false, null,"获取打印数量失败", key);
                        int printnum = 1;
                        getNextPrintNum(hm,num,key,printnum);
                    }else if (carBeans.getIntValue(CommCL.RTN_ID) == 0) {
                        JSONObject msg = carBeans.getJSONObject(CommCL.RTN_DATA);//response里面的data，存放的是查询记录
                        JSONObject rtnMap = msg.getJSONObject(CommCL.RTN_DATA);//获取实际的查询结果
                        JSONObject info = rtnMap.getJSONObject("info");
                        int printnum = CommonUtils.parseInt(info.getString("printnum"));
                        baseView.commCallBack(rtnMap.getBoolean("bok"), info, rtnMap.getString("error"), key);
                        getNextPrintNum(hm,num,key,printnum);
                    }
                });
    }

    public void getNextPrintNum(HashMap<String,String> hm,int num,int key,int printnum) {
        if (printnum < num) {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
//                        int m = (num - printnum) / 2;
//                        m = (m > 0) ? m : 2;
                        int m=2;
                        Thread.sleep(1000 * m);
                        printNum(hm, num, key);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            });
            thread.start();
        }
    }

}
