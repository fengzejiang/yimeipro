package com.yimei.network.response;

/**
 * @Auther: fengzejiang1987@163.com
 * @Date : 2018/12/7 16:10
 */
public class Response<T> {
    private int id; // 返回的code
    private T data; // 具体的数据结果
    private String message; // message 可用来返回接口的说明
    public Response(){


    }
    public Response(int id,String msg){
        this.id = id;
        this.message = msg;

    }
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
