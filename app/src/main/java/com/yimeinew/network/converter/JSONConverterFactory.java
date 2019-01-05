package com.yimeinew.network.converter;

import com.aliyun.openservices.shade.com.alibaba.rocketmq.shade.com.alibaba.fastjson.JSONObject;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;


import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * @Auther: fengzejiang1987@163.com
 * @Date : 2018/12/10 9:57
 */
public class JSONConverterFactory  extends Converter.Factory  {
    public static JSONConverterFactory create() {
        return create(new JSONObject());
    }

    public static JSONConverterFactory create(JSONObject jsonObject) {
        if (jsonObject == null) throw new NullPointerException("jsonObject == null");
        return new JSONConverterFactory(jsonObject);
    }

    private final JSONObject jsonObject;

    private JSONConverterFactory(JSONObject jsonObject) {
        this.jsonObject = jsonObject;
    }

    @Override
    public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations,
                                                            Retrofit retrofit) {
        return new JSONResponseBodyConverter<>(jsonObject,type);
    }


}
