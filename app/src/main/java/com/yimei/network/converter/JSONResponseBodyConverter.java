package com.yimei.network.converter;


import android.util.JsonReader;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.shade.com.alibaba.fastjson.JSON;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.shade.com.alibaba.fastjson.JSONObject;
import com.yimei.network.response.Response;
import okhttp3.ResponseBody;
import retrofit2.Converter;

import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;

/**
 * @Auther: fengzejiang1987@163.com
 * @Date : 2018/12/10 9:57
 */
public class JSONResponseBodyConverter<T> implements Converter<ResponseBody, T> {
    private final JSONObject json;
    private final Type type;

    JSONResponseBodyConverter(JSONObject json,Type type){
        this.json = json;
        this.type = type;
    }
    @Override
    public T convert(ResponseBody value) throws IOException {
        String values = value.string();
        return JSON.parseObject(values,type);
    }
}
