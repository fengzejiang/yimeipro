package com.yimei.data;

import com.yimei.activity.base.BaseApplication;
import com.yimei.utils.CommonUtils;
import com.yimei.utils.DateUtil;
import com.yimei.utils.ICL;

import java.io.Serializable;

/**
 *
 * @Auther: fengzejiang1987@163.com
 * @Date : 2018/12/20 13:09
 */
public class MESPRecord implements Serializable {
    private String sid;//生产记录主键
    private String sid1;//生产批次号
    private String slkid;//工单号
    private String sbuid="D0001";//业务号
    private String prd_no;//产品编码
    private String prd_name;//产品名称
    private String op;//操作员
    private String remark;//备注
    private String smake = BaseApplication.currUser.getUserCode();//制单人
    private String mkdate = DateUtil.getCurrDateTime(ICL.DF_YMDT);//制单时间
    private String bok="1";//是否可以开工
    private int state = 0;//生产记录表状态
    private String hpdate;//开工时间
    private int firstchk = 0;//是否首件检
    private int qty ;//数量
    private String dcid = CommonUtils.getMacID();//设备ID
    private String mbox ;//料盒号
    private String state1 = "00";//生产状态
    private String bfirst="0";//是否首工序
    private int fircheck = firstchk;//首件检验
    private String zcno;//当前制成
    private String zcno1;//下一制成
    private String sbid;//设备编码
    private String prtno;//胶杯号
    private String sorg = BaseApplication.currUser.getDeptCode();//胶杯号

    public MESPRecord(){}
    public MESPRecord(String sid1, String slkid, String zcno, String sbid) {
        this.sid1 = sid1;
        this.slkid = slkid;
        this.zcno = zcno;
        this.sbid = sbid;
    }

    public String getPrtno() {
        return prtno;
    }

    public void setPrtno(String prtno) {
        this.prtno = prtno;
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

    public String getBok() {
        return bok;
    }

    public void setBok(String bok) {
        this.bok = bok;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getHpdate() {
        return hpdate;
    }

    public void setHpdate(String hpdate) {
        this.hpdate = hpdate;
    }

    public int getFirstchk() {
        return firstchk;
    }

    public void setFirstchk(int firstchk) {
        this.firstchk = firstchk;
        this.fircheck = firstchk;
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

    public String getMbox() {
        return mbox;
    }

    public void setMbox(String mbox) {
        this.mbox = mbox;
    }

    public String getState1() {
        return state1;
    }

    public void setState1(String state1) {
        this.state1 = state1;
    }

    public String getBfirst() {
        return bfirst;
    }

    public void setBfirst(String bfirst) {
        this.bfirst = bfirst;
    }

    public int getFircheck() {
        return fircheck;
    }

    public void setFircheck(int fircheck) {
        this.fircheck = fircheck;
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
}
