package com.yimeinew.activity;

import com.aliyun.openservices.shade.com.alibaba.rocketmq.shade.com.alibaba.fastjson.JSONObject;
import com.yimeinew.network.NetWorkManager;
import com.yimeinew.network.response.Response;
import com.yimeinew.network.response.ResponseTransformer;
import io.reactivex.Observable;
import okhttp3.RequestBody;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void t1(){
//        HashMap<String,String> params = new HashMap<>();
//        JSONObject  jsonObject = new JSONObject();
//        jsonObject.put("id","200");
//        jsonObject.put("sid","4000");
//        bb(jsonObject).compose(ResponseTransformer.handleResult()).subscribe(jsonObj -> {
//            System.out.println(jsonObj.toJSONString());
//        });

    }

    public Observable<Response<JSONObject>> bb(JSONObject jsonObject){
        NetWorkManager nt1 = NetWorkManager.getInstance();
        nt1.init();
        RequestBody requestBody = RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"),jsonObject.toJSONString());
        return nt1.getRequest().postBody(requestBody);
    }
}