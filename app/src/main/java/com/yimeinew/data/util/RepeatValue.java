package com.yimeinew.data.util;

public class RepeatValue {
    long time;
    String value;
    public RepeatValue(){}
    public RepeatValue(long time,String value){
        this.time=time;
        this.value=value;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
