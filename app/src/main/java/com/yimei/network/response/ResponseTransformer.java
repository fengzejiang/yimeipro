package com.yimei.network.response;

import android.util.Log;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.shade.com.alibaba.fastjson.JSONObject;
import com.yimei.network.exceptions.ApiException;
import com.yimei.network.exceptions.CustomException;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.functions.Function;

/**
 * @Auther: fengzejiang1987@163.com
 * @Date : 2018/12/7 16:15
 */
public class ResponseTransformer {

    public static <T> ObservableTransformer<Response<T>, T> handleResult() {
        return upstream -> upstream
                .onErrorReturn(new Function<Throwable, Response<T>>() {
                    @Override
                    public Response<T> apply(Throwable throwable) {

                        ApiException err = CustomException.handleException(throwable);
                        Response<T> response = new Response<>(-1,err.getDisplayMessage());
//                        JSONObject jsonObject = new JSONObject();
//                        jsonObject.put("id",response.getId());
//                        jsonObject.put("message",err.getDisplayMessage());
                        return response;
                    }
                })
                .flatMap(new ResponseFunction<>());
    }


    /**
     * 非服务器产生的异常，比如本地无无网络请求，Json数据解析错误等等。
     *
     * @param <T>
     */
    private static class ErrorResumeFunction<T> implements Function<Throwable, ObservableSource<? extends Response<T>>> {

        @Override
        public ObservableSource<? extends Response<T>> apply(Throwable throwable) {
            return Observable.error(CustomException.handleException(throwable));
        }
    }

    /**
     * 服务其返回的数据解析
     * 正常服务器返回数据和服务器可能返回的exception
     *
     * @param <T>
     */
    private static class ResponseFunction<T> implements Function<Response<T>, ObservableSource<T>> {

        @Override
        public ObservableSource<T> apply(Response<T> tResponse) {
            int code = tResponse.getId();
            String message = tResponse.getMessage();
            Log.i("service",message);
            if (code>=-1&&code<=1) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("id",tResponse.getId());
                jsonObject.put("message",tResponse.getMessage());
                jsonObject.put("data",tResponse.getData());
                tResponse.setData((T) jsonObject);
                return Observable.just(tResponse.getData());
            } else {
                return Observable.error(new ApiException(code, message));
            }
        }
    }

}
