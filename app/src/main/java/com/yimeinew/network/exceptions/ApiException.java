package com.yimeinew.network.exceptions;

/**
 * @Auther: fengzejiang1987@163.com
 * @Date : 2018/12/7 16:16
 */
public class ApiException extends Exception {
    private int id;
    private String displayMessage;

    public ApiException(int code, String displayMessage) {
        this.id = code;
        this.displayMessage = displayMessage;
    }

    public ApiException(int code, String message, String displayMessage) {
        super(message);
        this.id = code;
        this.displayMessage = displayMessage;
    }

    public int getId() {
        return id;
    }

    public void setId(int code) {
        this.id = code;
    }

    public String getDisplayMessage() {
        return displayMessage;
    }

    public void setDisplayMessage(String displayMessage) {
        this.displayMessage = displayMessage;
    }
}
