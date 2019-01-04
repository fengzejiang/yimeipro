package com.yimei.data.qc;

import java.io.Serializable;

/**
 * @Auther: fengzejiang1987@163.com
 * @Date : 2018/12/30 10:33
 */
public class QCBatchInfo implements Serializable {
    private String sid1;//工单批次号
    private String lotno;//后段批次号
    private String sid;//工单号
    private String prd_no;//产品编码
    private String prd_name;//产品名称
    private int qty;//批次数量
    private String state;//工单状态
    private int holdid;//是否HOLD
    private boolean huoduan;//是否是后段

    public QCBatchInfo() {
    }

    public String getSid1() {
        return sid1;
    }

    public void setSid1(String sid1) {
        this.sid1 = sid1;
    }

    public String getLotno() {
        return lotno;
    }

    public void setLotno(String lotno) {
        this.lotno = lotno;
    }

    public String getSid() {
        return sid;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

    public String getPrd_no() {
        return prd_no;
    }

    public void setPrd_no(String prd_no) {
        this.prd_no = prd_no;
    }

    public String getPrd_name() {
        return prd_name;
    }

    public void setPrd_name(String prd_name) {
        this.prd_name = prd_name;
    }

    public int getQty() {
        return qty;
    }

    public void setQty(int qty) {
        this.qty = qty;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public int getHoldid() {
        return holdid;
    }

    public void setHoldid(int holdid) {
        this.holdid = holdid;
    }

    public boolean isHuoduan() {
        return huoduan;
    }

    public void setHuoduan(boolean huoduan) {
        this.huoduan = huoduan;
    }

    @Override
    public String toString() {
        return "QCBatchInfo{" +
                "sid1='" + sid1 + '\'' +
                ", lotno='" + lotno + '\'' +
                ", sid='" + sid + '\'' +
                ", prd_no='" + prd_no + '\'' +
                ", prd_name='" + prd_name + '\'' +
                ", qty=" + qty +
                ", state='" + state + '\'' +
                ", holdid=" + holdid +
                ", huoduan=" + huoduan +
                '}';
    }
}
