package com.yimeinew.data;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import com.yimeinew.activity.BR;

/**
 * @Auther: fengzejiang1987@163.com
 * @Date : 2018/12/10 16:55
 */
public class BindInfo extends BaseObservable {
    private String sbuid="D0090";
    private String mbox;//料盒号
    private String sorg;//部门
    private String sopr;//操作员
    private String hpdate;//日期
    private String slkid;//工单号
    private String sid1;//批次号
    private String smake;//制单人
    private String mkdate;//制单时间
    private String dcid;//手持ID
    private int cid=1;//项次
    private String zcno;//制成号

    @Bindable
    public String getSbuid() {
        return sbuid;
    }

    public void setSbuid(String sbuid) {
        this.sbuid = sbuid;
        notifyPropertyChanged(BR.sbuid);
    }
    @Bindable
    public String getMbox() {
        return mbox;
    }

    public void setMbox(String mbox) {
        this.mbox = mbox;
        notifyPropertyChanged(BR.mbox);
    }
    @Bindable
    public String getSorg() {
        return sorg;
    }

    public void setSorg(String sorg) {
        this.sorg = sorg;
        notifyPropertyChanged(BR.sorg);
    }
    @Bindable
    public String getSopr() {
        return sopr;
    }

    public void setSopr(String sopr) {
        this.sopr = sopr;
        notifyPropertyChanged(BR.sopr);
    }
    @Bindable
    public String getHpdate() {
        return hpdate;
    }

    public void setHpdate(String hpdate) {
        this.hpdate = hpdate;
        notifyPropertyChanged(BR.hpdate);
    }
    @Bindable
    public String getSlkid() {
        return slkid;
    }

    public void setSlkid(String slkid) {
        this.slkid = slkid;
        notifyPropertyChanged(BR.slkid);
    }
    @Bindable
    public String getSid1() {
        return sid1;
    }

    public void setSid1(String sid1) {
        this.sid1 = sid1;
        notifyPropertyChanged(BR.sid1);
    }
    @Bindable
    public String getSmake() {
        return smake;
    }

    public void setSmake(String smake) {
        this.smake = smake;
        notifyPropertyChanged(BR.smake);
    }
    @Bindable
    public String getMkdate() {
        return mkdate;
    }

    public void setMkdate(String mkdate) {
        this.mkdate = mkdate;
        notifyPropertyChanged(BR.mkdate);
    }
    @Bindable
    public String getDcid() {
        return dcid;
    }

    public void setDcid(String dcid) {
        this.dcid = dcid;
        notifyPropertyChanged(BR.dcid);
    }
    @Bindable
    public int getCid() {
        return cid;
    }

    public void setCid(int cid) {
        this.cid = cid;
        notifyPropertyChanged(BR.cid);
    }
    @Bindable
    public String getZcno() {
        return zcno;
    }

    public void setZcno(String zcno) {
        this.zcno = zcno;
        notifyPropertyChanged(BR.zcno);
    }
}
