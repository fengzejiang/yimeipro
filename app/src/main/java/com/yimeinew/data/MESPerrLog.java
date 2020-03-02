package com.yimeinew.data;

import android.text.TextUtils;
import com.yimeinew.activity.base.BaseApplication;
import com.yimeinew.utils.CommonUtils;
import com.yimeinew.utils.DateUtil;
import com.yimeinew.utils.ICL;

import java.io.Serializable;

/**
 *
 * @Auther: fengzejiang1987@163.com
 * @Date : 2018/12/20 13:09
 */
public class MESPerrLog implements Serializable {
    private String sid;//生产记录主键
    private String sid1;//生产批次号
    private String slkid;//工单号
    private String sbuid="D0074";//业务号
    private String prd_no;//产品编码
    private String prd_name;//产品名称
    private String op;//操作员
    private String remark;//备注
    private String smake = BaseApplication.currUser.getUserCode();//制单人
    private String mkdate = DateUtil.getCurrDateTime(ICL.DF_YMDT);//制单时间
    private int state = 0;//生产记录表状态
    private int qty ;//数量
    private String dcid = CommonUtils.getMacID();//设备ID
    private String zcno;//当前制成
    private String zcno1;//下一制成
    private String sbid;//设备编码
    private String reason;//原因
    private String sorg = BaseApplication.currUser.getDeptCode();//胶杯号

    public MESPerrLog(){}
    public MESPerrLog(String sid1, String slkid, String zcno, String sbid) {
        this.sid1 = sid1;
        this.slkid = slkid;
        this.zcno = zcno;
        this.sbid = sbid;
    }
    public MESPerrLog(MESPRecord record){
        this.sid1=record.getSid1();
        this.slkid=(TextUtils.isEmpty(record.getSlkid()))?record.getSid():record.getSlkid();
        this.prd_no=record.getPrd_no();
        this.prd_name=record.getPrd_name();
        this.op=record.getOp();
        this.remark=record.getRemark();
        this.qty=record.getQty();
        this.zcno=record.getZcno();
        this.zcno1=record.getZcno1();
        this.sbid=record.getSbid();
    }
    public String getSid() {
        return sid;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

    public String getSid1() {
        return sid1;
    }

    public void setSid1(String sid1) {
        this.sid1 = sid1;
    }

    public String getSlkid() {
        return slkid;
    }

    public void setSlkid(String slkid) {
        this.slkid = slkid;
    }

    public String getSbuid() {
        return sbuid;
    }

    public void setSbuid(String sbuid) {
        this.sbuid = sbuid;
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

    public String getOp() {
        return op;
    }

    public void setOp(String op) {
        this.op = op;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getSmake() {
        return smake;
    }

    public void setSmake(String smake) {
        this.smake = smake;
    }

    public String getMkdate() {
        return mkdate;
    }

    public void setMkdate(String mkdate) {
        this.mkdate = mkdate;
    }



    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }



    public int getQty() {
        return qty;
    }

    public void setQty(int qty) {
        this.qty = qty;
    }

    public String getDcid() {
        return dcid;
    }

    public void setDcid(String dcid) {
        this.dcid = dcid;
    }



    public String getZcno() {
        return zcno;
    }

    public void setZcno(String zcno) {
        this.zcno = zcno;
    }

    public String getZcno1() {
        return zcno1;
    }

    public void setZcno1(String zcno1) {
        this.zcno1 = zcno1;
    }

    public String getSbid() {
        return sbid;
    }

    public void setSbid(String sbid) {
        this.sbid = sbid;
    }

    public String getSorg() {
        return sorg;
    }

    public void setSorg(String sorg) {
        this.sorg = sorg;
    }
    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

}
