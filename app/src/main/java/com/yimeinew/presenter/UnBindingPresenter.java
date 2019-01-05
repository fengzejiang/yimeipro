package com.yimeinew.presenter;

import android.util.Log;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.shade.com.alibaba.fastjson.JSONArray;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.shade.com.alibaba.fastjson.JSONObject;
import com.yimeinew.data.BindInfo;
import com.yimeinew.model.impl.UnbindingModel;
import com.yimeinew.modelInterface.BaseStationBindingView;
import com.yimeinew.network.response.ResponseTransformer;
import com.yimeinew.network.schedulers.SchedulerProvider;
import com.yimeinew.utils.CommCL;


/**
 * @Auther: fengzejiang1987@163.com
 * @Date : 2018/12/9 17:23
 */
public class UnBindingPresenter {
    private final String TAG_NAME = UnbindingModel.class.getSimpleName();
    private BaseStationBindingView baseView;
    private UnbindingModel unbindingModel;
    private SchedulerProvider schedulerProvider;
    public  UnBindingPresenter(BaseStationBindingView base, SchedulerProvider schedulerProvider){
        this.baseView = base;
        this.schedulerProvider = schedulerProvider;
        if(unbindingModel==null){
            unbindingModel = new UnbindingModel();
        }
    }

    public void checkBoxExit(String box){
        baseView.showLoading();
        unbindingModel.getMbox(box).compose(ResponseTransformer.handleResult())
                .compose(schedulerProvider.applySchedulers()).subscribe(
                        carBeans -> {
                            baseView.hideLoading();
                            if(carBeans.getIntValue(CommCL.RTN_ID)!=0){
                                baseView.checkMboxCallBack(false,"获取服务器信息失败"+carBeans.toString(),0);
                            }else if(carBeans.getIntValue(CommCL.RTN_ID) == 0){
                                JSONObject msg = carBeans.getJSONObject(CommCL.RTN_DATA);//response里面的data，存放的是查询记录
                                JSONObject rtnMap = (JSONObject) msg.get(CommCL.RTN_DATA);//获取实际的查询结果
                                if(rtnMap.getInteger(CommCL.RTN_CODE)==0){
                                    baseView.checkMboxCallBack(false,"没有【"+box+"】料盒号",0);
                                }else{
                                    baseView.checkMboxCallBack(true,null,0);
                                }
                            }
                        }
                );
    }

    public void getBatchInfo(String sid1){
        unbindingModel.getUnBindBatchInfoBySid(sid1).compose(ResponseTransformer.handleResult())
                .compose(schedulerProvider.applySchedulers()).subscribe(
                carBeans->{
                    if(carBeans.getIntValue(CommCL.RTN_ID)!=0){
                        baseView.checkSidCallBack(false,null,"获取服务器信息失败"+carBeans.toString());
                    }else{
                        JSONObject msg = carBeans.getJSONObject(CommCL.RTN_DATA);//response里面的data，存放的是查询记录
                        JSONObject rtnMap = (JSONObject) msg.get(CommCL.RTN_DATA);//获取实际的查询结果
                        if(rtnMap.getInteger(CommCL.RTN_CODE)==0){
                            baseView.checkSidCallBack(false,null,"该【"+sid1+"】批次号没有绑定料盒");
                        }else{
                            JSONArray jsonArray = (JSONArray) rtnMap.get(CommCL.RTN_VALUES);
                            JSONObject jsonObject = jsonArray.getJSONObject(0);
                            baseView.checkSidCallBack(true,jsonObject,null);
                            Log.d(TAG_NAME,jsonObject.toJSONString());
                        }
                    }
                }
        );
    }

    public void saveBean(BindInfo unBindInfo) {
        unbindingModel.saveData(unBindInfo).compose(ResponseTransformer.handleResult()).compose(schedulerProvider.applySchedulers())
        .subscribe(carBeans->{
            if(carBeans.getInteger(CommCL.RTN_ID) != 0){
                baseView.onRemoteFailed(carBeans.getString(CommCL.RTN_MESSAGE));
            }else {
                Log.i(TAG_NAME,carBeans.toJSONString());
                unBindBox(unBindInfo);
            }

        },throwable -> {
            Log.i(TAG_NAME,throwable.getLocalizedMessage());
            baseView.onRemoteFailed(throwable.getLocalizedMessage());
        });
    }

    public void unBindBox(BindInfo unBindInfo) {
        unbindingModel.unBindBox(unBindInfo).compose(ResponseTransformer.handleResult()).compose(schedulerProvider.applySchedulers())
                .subscribe(bindRtn -> {
                    if(bindRtn.getInteger(CommCL.RTN_ID) == -1){
                        baseView.onRemoteFailed(bindRtn.getString(CommCL.RTN_MESSAGE));
                    }else{
                        baseView.addRow(unBindInfo);
                    }
                },throwable ->{
                    Log.i(TAG_NAME,throwable.getLocalizedMessage());
                    baseView.onRemoteFailed(throwable.getLocalizedMessage());
                });

    }
}
