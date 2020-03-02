package com.yimeinew.presenter;

import android.text.TextUtils;
import android.util.Log;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.shade.com.alibaba.fastjson.JSONArray;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.shade.com.alibaba.fastjson.JSONObject;
import com.google.gson.JsonArray;
import com.yimeinew.model.impl.CommZCModel;
import com.yimeinew.modelInterface.BaseStationBindingView;
import com.yimeinew.modelInterface.CommBaseView;
import com.yimeinew.network.response.Response;
import com.yimeinew.network.response.ResponseTransformer;
import com.yimeinew.network.schedulers.SchedulerProvider;
import com.yimeinew.utils.CommCL;
import com.yimeinew.utils.CommonUtils;
import io.reactivex.Observable;

import java.util.HashMap;

public class CommOtherPresenter {
    private final String TAG_NAME = CommOtherPresenter.class.getSimpleName();
    private CommBaseView baseView;//通用工站回调接口
    private SchedulerProvider schedulerProvider; //线程池
    private CommZCModel baseModel;//服务端处理接口
    public CommOtherPresenter(CommBaseView baseStationBindingView, SchedulerProvider schedulerProvider) {
        this.baseView = baseStationBindingView;
        this.schedulerProvider = schedulerProvider;
        if (baseModel == null) {
            baseModel = new CommZCModel();
        }
    }

    /*----------支架绑料盒------------*/

    /**
     *校验货品代号
     * @param prdNo
     * @param key
     */
    public void checkPrdNo(String prdNo,int key) {
        baseModel.getPrdt(prdNo).compose(ResponseTransformer.handleResult())
                .compose(schedulerProvider.applySchedulers()).subscribe(
                carBeans -> {
                    if (carBeans.getIntValue(CommCL.RTN_ID) != 0) {
                        baseView.commCallBack(false, null,"获取服务器信息失败" + carBeans.toString(), key);
                    }else if (carBeans.getIntValue(CommCL.RTN_ID) == 0) {
                        JSONObject msg = carBeans.getJSONObject(CommCL.RTN_DATA);//response里面的data，存放的是查询记录
                        JSONObject rtnMap = (JSONObject) msg.get(CommCL.RTN_DATA);//获取实际的查询结果
                        if (rtnMap.getInteger(CommCL.RTN_CODE) == 0) {
                            baseView.commCallBack(false, null,"没有【" + prdNo + "货品】",key);
                        } else {
                            JSONArray array = rtnMap.getJSONArray(CommCL.RTN_VALUES);
                            JSONObject data = array.getJSONObject(0);
                            String prd_no=data.getString("prd_no");
                            CommCL.sharedPreferences.edit().putString(TAG_NAME+prdNo,data.toJSONString()).commit();
                            baseView.commCallBack(true, data,null,key);
                        }
                    }
                },throwable -> {
                    baseView.onRemoteFailed(throwable.getMessage());
                });

    }
    /**
     * 支架绑料盒:检验料盒
     * @param box
     * @param key
     */
    public void checkBoxExit(String box,String prdNo,int key) {
        baseModel.getMbox(box).compose(ResponseTransformer.handleResult())
                .compose(schedulerProvider.applySchedulers()).subscribe(
                carBeans -> {
                    if (carBeans.getIntValue(CommCL.RTN_ID) != 0) {
                        baseView.commCallBack(false, null,"获取服务器信息失败" + carBeans.toString(), key);
                    }else if (carBeans.getIntValue(CommCL.RTN_ID) == 0) {
                            JSONObject msg = carBeans.getJSONObject(CommCL.RTN_DATA);//response里面的data，存放的是查询记录
                            JSONObject rtnMap = (JSONObject) msg.get(CommCL.RTN_DATA);//获取实际的查询结果
                            if (rtnMap.getInteger(CommCL.RTN_CODE) == 0) {
                                baseView.commCallBack(false, null,"没有【" + box + "】料盒号",key);
                            } else {
                                JSONArray array = rtnMap.getJSONArray(CommCL.RTN_VALUES);
                                JSONObject data = array.getJSONObject(0);
                                String prd_no=data.getString("prd_no");
                                if(!TextUtils.isEmpty(prd_no)){
                                    baseView.commCallBack(false, null,"该料盒已绑定支架，无法继续绑定",key);
                                    return;
                                }
                                int state=data.getInteger("state");
                                if(state==1){
                                    baseView.commCallBack(false, null,"该料盒没有解绑",key);
                                    return;
                                }
                                //更新列表
                                String spjson=CommCL.sharedPreferences.getString(TAG_NAME+prdNo,"");
                                JSONObject objjson=JSONObject.parseObject(spjson);
                                objjson.put("mbox",data.getString("id"));


                                baseView.commCallBack(true, objjson,null,key);
                            }
                    }
                },throwable -> {
                    baseView.onRemoteFailed(throwable.getMessage());
                });
    }

    /**
     * 保存支架绑料盒：表头数据
     * @param params
     * @param key
     */
    public void saveMboxZJ(HashMap<String,String> params,String key){
        baseModel.saveData(params).compose(ResponseTransformer.handleResult())
                .compose(schedulerProvider.applySchedulers()).subscribe(
                carBeans -> {
                    if (carBeans.getIntValue(CommCL.RTN_ID) != 0) {
                        baseView.commCallBack(false, null,"获取服务器信息失败" + carBeans.toString(), key);
                    }else if (carBeans.getIntValue(CommCL.RTN_ID) == 0) {
                        JSONObject msg = carBeans.getJSONObject(CommCL.RTN_DATA);//response里面的data，存放的是查询记录
                        msg.putAll(params);
                        String mysid=msg.getString("sid");
                        if (TextUtils.isEmpty(mysid)) {
                            baseView.commCallBack(false, null,"没有保存成功主键空",key);
                        } else {
                            JSONObject obj=JSONObject.parseObject(params.get(CommCL.PARAMS_JSON_STR));
                            msg.putAll(obj);
                            baseView.commCallBack(true, msg,"保存成功",key);
                        }
                    }
                },throwable -> {
                    baseView.onRemoteFailed(throwable.getMessage());
                });
    }

    /**
     * 保存料盒表身
     * @param params
     * @param key
     */
    public void saveMboxBoby(HashMap<String,String> params, String key, JSONObject info){
        baseModel.saveData(params).compose(ResponseTransformer.handleResult())
                .compose(schedulerProvider.applySchedulers()).subscribe(
                carBeans -> {
                    if (carBeans.getIntValue(CommCL.RTN_ID) != 0) {
                        baseView.commCallBack(false, null,"已绑定，不能插入重复值" , key);
                    }else if (carBeans.getIntValue(CommCL.RTN_ID) == 0) {
                       // JSONObject msg = carBeans.getJSONObject(CommCL.RTN_DATA);//response里面的data，存放的是查询记录

                        baseView.commCallBack(true, info, "对象定义被修改" , key);

                    }
                },throwable -> {
                    baseView.onRemoteFailed(throwable.getMessage());
                });
    }

    /**
     * 更新料盒支架信息
     * @param vaules
     * @param key
     */
    public void updateMbox(String vaules, String key){
        baseModel.updateData(vaules,key).compose(ResponseTransformer.handleResult())
                .compose(schedulerProvider.applySchedulers()).subscribe(
                carBeans -> {
                    if (carBeans.getIntValue(CommCL.RTN_ID) != 0) {
                        baseView.commCallBack(false, null,"获取服务器信息失败" + carBeans.toString(), key);
                    }else if (carBeans.getIntValue(CommCL.RTN_ID) == 0) {
                        // JSONObject msg = carBeans.getJSONObject(CommCL.RTN_DATA);//response里面的data，存放的是查询记录
                        baseView.commCallBack(true, null, "对象定义被修改" , key);
                    }
                },throwable -> {
                    baseView.onRemoteFailed(throwable.getMessage());
                });
    }
    /**-------------------设备维修----------------------**/

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
                            if(array.size()==0){
                                baseView.getAssistInfoBack(false, null,"没有查询到记录"+assistId+cont+";", key);
                            }else {
                                //JSONObject data = array.getJSONObject(0);
                                baseView.getAssistInfoBack(true, array, "", key);
                            }
                        }
                    }
                },throwable -> {
                    baseView.onRemoteFailed(throwable.getMessage());
                });
    }

    /**
     * 通用查询
     * @param assistId
     * @param cont
     * @param key
     */
    public void getAssistInfo1(String assistId,String cont,int key) {
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




    //通用插入
    public void saveData(String insObject,JSONObject record,int key){
        baseModel.comSaveData(record,insObject).compose(ResponseTransformer.handleResult())
                .compose(schedulerProvider.applySchedulers()).subscribe(
                carBeans -> {
                    if (carBeans.getIntValue(CommCL.RTN_ID) != 0) {
                        baseView.saveDataBack(false, null,record,"获取服务器信息失败" + carBeans.toString(), 0);
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
                        baseView.updateDataBack(false, null,"获取服务器信息失败" + carBeans.toString(), 0);
                    }else if (carBeans.getIntValue(CommCL.RTN_ID) == 0) {
                        Log.i(TAG_NAME, carBeans.toJSONString());
                        if (carBeans.getIntValue(CommCL.RTN_ID) != 0) {
                            baseView.updateDataBack(false, null,"记录更新失败", key);
                        } else if (carBeans.getIntValue(CommCL.RTN_ID) == 0) {
                            JSONArray array =new JSONArray();
                            array.add(record);
                            //int a=array.size();

                            baseView.updateDataBack(true,array,"",key);
                    }
                    }
                },throwable -> {
                    baseView.onRemoteFailed(throwable.getMessage());
                });
    }

}
