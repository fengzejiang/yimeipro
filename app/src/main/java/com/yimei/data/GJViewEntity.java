package com.yimei.data;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.databinding.Observable;
import com.yimei.activity.BR;

/**
 * @Auther: fengzejiang1987@163.com
 * @Date : 2018/12/13 13:47
 */
public class GJViewEntity extends BaseObservable {
    private String op;//操作员
    private String zcno;//制成号
    private String box;//料盒号
    private String sid1;//批次号
    private String sbid;//设备号
    private boolean gj1;//是否时固晶1

    @Bindable
    public String getZcno() {
        return zcno;
    }

    public void setZcno(String zcno) {
        this.zcno = zcno.toUpperCase();
        notifyPropertyChanged(BR.zcno);
    }

    @Bindable
    public String getOp() {
        return op;
    }

    public void setOp(String op) {
        this.op = op.toUpperCase();
        notifyPropertyChanged(BR.op);
    }

    @Bindable
    public String getBox() {
        return box;
    }

    public void setBox(String box) {
        this.box = box.toUpperCase();
        notifyPropertyChanged(BR.box);
    }

    @Bindable
    public String getSid1() {
        return sid1;
    }

    public void setSid1(String sid1) {
        this.sid1 = sid1.toUpperCase();
        notifyPropertyChanged(BR.sid1);
    }
    @Bindable
    public String getSbid() {
        return sbid;
    }

    public void setSbid(String sbid) {
        this.sbid = sbid.toUpperCase();
        notifyPropertyChanged(BR.sbid);
    }

    @Bindable
    public boolean isGj1() {
        return gj1;
    }

    public void setGj1(boolean gj1) {
        this.gj1 = gj1;
        notifyPropertyChanged(BR.gj1);
    }

    @Override
    public String toString() {
        return "GJViewEntity{" +
                "op='" + op + '\'' +
                ", zcno='" + zcno + '\'' +
                ", box='" + box + '\'' +
                ", sid1='" + sid1 + '\'' +
                ", sbid='" + sbid + '\'' +
                ", gj1=" + gj1 +
                '}';
    }
}
