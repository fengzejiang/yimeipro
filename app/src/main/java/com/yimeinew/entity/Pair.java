package com.yimeinew.entity;

import com.aliyun.openservices.shade.com.alibaba.rocketmq.shade.com.alibaba.fastjson.JSONArray;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.shade.com.alibaba.fastjson.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Pair implements Serializable {

	public String key;//显示的值
    public String value;//代号
    
    
    
    public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public Pair(String key, String value) {
            this.key = key;
            this.value = value;
    }
    public String toString() {
            return key;
    }
    public static List<Pair> toListPair(JSONArray jsonArray, String keyColumn, String valueColumn){
		List<Pair> dicts = new ArrayList<Pair>();
		if(jsonArray!=null){
			for (int i=0;i<jsonArray.size();i++) {
				JSONObject jsonObject=jsonArray.getJSONObject(i);
				dicts.add(new Pair(jsonObject.getString(keyColumn),jsonObject.getString(valueColumn)));
			}
		}
		return dicts;
	}
}
