package com.yimeinew.data;

import android.databinding.BaseObservable;

import java.io.Serializable;

public class MboxBindEntity  implements Serializable {

    private  String mbox;
    private  String prd_no;
    private  String op;

    public String getMbox() {
        return mbox;
    }

    public void setMbox(String mbox) {
        this.mbox = mbox;
    }

    public String getPrd_no() {
        return prd_no;
    }

    public void setPrd_no(String prd_no) {
        this.prd_no = prd_no;
    }

    public String getOp() {
        return op;
    }

    public void setOp(String op) {
        this.op = op;
    }
}
