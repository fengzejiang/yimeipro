package com.yimeinew.view;

import android.content.Context;
import android.graphics.Rect;
import android.text.Editable;
import android.text.Spannable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.EditText;
import android.widget.TextView;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.shade.com.alibaba.fastjson.JSONArray;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.shade.com.alibaba.fastjson.JSONObject;
import com.yimeinew.model.impl.BaseModel;
import com.yimeinew.network.response.ResponseTransformer;
import com.yimeinew.network.schedulers.SchedulerProvider;
import com.yimeinew.utils.CommCL;
import com.yimeinew.utils.CommonUtils;

import java.util.HashMap;

/**
 * hec
 * 带有辅助的输入框
 */
public class AuxText extends android.support.v7.widget.AppCompatEditText {
    private HashMap<String,String> aux;
    private String text="";
    EditText textView;
    private int keyLen=0;//输入长度
    private int valueLen=0;//参照的长度
    private String kongge="                                                                                                                               ";
    private boolean caseWrite=false;//true为区分大小写，false为不区分大小写，输入都会变成大写
    public AuxText(Context context) {
        super(context, (AttributeSet)null);
        textView=new EditText(context);
    }

    public AuxText(Context context, AttributeSet attrs) {
        super(context, attrs);
        textView=new EditText(context);
    }

    public AuxText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context,attrs,defStyleAttr);
        textView=new EditText(context);
    }

    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
        System.out.println("woshi="+direction+" focused="+focused);
        if(focused){
            setText(text);//获取焦点
        }else{//失去焦点
            if(caseWrite){
                text = super.getText().toString();
            }else{
                text = super.getText().toString().toUpperCase();
            }
            keyLen=text.length();
            textView.setText(text);
            if(aux!=null&&aux.containsKey(text)&&!TextUtils.isEmpty(text)){
                String str = this.aux.get(text);valueLen=str.length();
                if(valueLen>keyLen){
                    textView.setText(text+kongge.substring(0,valueLen-keyLen));//将长度变长
                }
                setText(str);//显示参照
                if(valueLen>keyLen) {
                    textView.setText(text);//又赋值回去
                }
            }

        }
    }


    /**
     * 获取失去焦点时候文本框数据
     * @return
     */
    public String getUnFocusValue(){
        String key1=getFocusValue();
        if(aux!=null&&aux.containsKey(key1)) {
            return aux.get(key1);
        }else{
            return key1;
        }
    }

    /**
     * 获取得到焦点时候文本框信息
     * @return
     */
    public String getFocusValue(){
        return getText().toString();
    }

    /**
     * 获取输入信息，非辅助显示值
     * @return
     */
    public Editable getText() {
        Editable editable = super.getText();
        String str=editable.toString();
        if(!hasFocus()&&aux!=null&&containsValue(str)){
            return textView.getText();
        }
        return editable;
    }

    /**
     * 获取辅助
     * @return
     */
    public HashMap<String, String> getAux() {
        return aux;
    }

    /**
     * 设置辅助
     * @param aux
     */
    public void setAux(HashMap<String, String> aux) {
        this.aux = aux;
    }



    /**
     * 设置辅助
     * @param auxArray 辅助查询结果  eg:{'id':'11','name':'固晶SMD1'}
     * @param keyId    key的键      eg:id
     * @param valueId  value的键    eg:name
     */
    public void setAuxArray(JSONArray auxArray,String keyId,String valueId) {
//        aux=new HashMap<>();
//        for(int i=0;i<auxArray.size();i++){
//            JSONObject obj=auxArray.getJSONObject(i);
//            this.aux.put(obj.getString(keyId),obj.getString(valueId));
//        }
        aux=CommonUtils.JSONArrayToMap(auxArray,keyId,valueId);
    }

    /**
     * 作业员辅助
     */
    public void setOPAux(){
        aux= (HashMap<String, String>) CommCL.sharedPreferences.getAll();
    }

    /**
     * 通过辅助去获取辅助信息--还未测试
     * @param assistId  辅助 "{WEB}"
     * @param cont      where条件 "~"
     *                  eg:{'id':'11','name':'固晶SMD1'}
     * @param keyId     eg:id
     * @param valueId   eg:name
     */
    public void setAssistAux(String assistId,String cont,String keyId,String valueId){
        BaseModel baseModel=new BaseModel();
        SchedulerProvider schedulerProvider = SchedulerProvider.getInstance();
        baseModel.getAssistInfo(assistId,cont).compose(ResponseTransformer.handleResult())
                .compose(schedulerProvider.applySchedulers()).subscribe(
                carBeans -> {
                    if (carBeans.getIntValue(CommCL.RTN_ID) != 0) {
                        //baseView.getAssistInfoBack(false, null,"获取服务器信息失败" + carBeans.toString(), 0);
                    }else if (carBeans.getIntValue(CommCL.RTN_ID) == 0) {
                        JSONObject msg = carBeans.getJSONObject(CommCL.RTN_DATA);//response里面的data，存放的是查询记录
                        JSONObject rtnMap = (JSONObject) msg.get(CommCL.RTN_DATA);//获取实际的查询结果
                        if (rtnMap.getInteger(CommCL.RTN_CODE) == 0) {
                            //baseView.getAssistInfoBack(false, null,"没有查询到记录"+assistId+cont+";", key);
                        } else {

                            JSONArray array = rtnMap.getJSONArray(CommCL.RTN_VALUES);//返回数据
                            if(array.size()==0){
                                //baseView.getAssistInfoBack(false, null,"没有查询到记录"+assistId+cont+";", key);
                            }else {
                                setAuxArray(array,keyId,valueId);
                                //JSONObject data = array.getJSONObject(0);
                                //baseView.getAssistInfoBack(true, array, "", key);
                            }
                        }
                    }
                },throwable -> {
                    //baseView.onRemoteFailed(throwable.getMessage());
                });
    }
    public boolean containsValue(String value){
        boolean b=false;
        for(String v:aux.values()){
            if(!TextUtils.isEmpty(v)&&!TextUtils.isEmpty(value)&&value.contains(v)){
                b=true;
            }
        }
        return b;
    }

    /**
     * 检验是否在参照里面
     * @param str
     * @return
     */
    public boolean checkUp(String str) {
        if(aux!=null){
            return aux.containsKey(str);
        }else {
            return true;
        }
    }

    /**
     * 是否区分大小写，默认是的不区分全部转成大写；
     * 为true时：区分大小写
     * @param caseWrite
     */
    public void setCaseWrite(boolean caseWrite) {
        this.caseWrite = caseWrite;
    }
}
