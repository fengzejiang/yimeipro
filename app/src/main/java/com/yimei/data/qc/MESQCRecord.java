package com.yimei.data.qc;

import com.yimei.activity.base.BaseApplication;
import com.yimei.utils.CommonUtils;
import com.yimei.utils.DateUtil;
import com.yimei.utils.ICL;

import java.io.Serializable;

/**
 * MES系统QC检验记录主对象
 * @Auther: fengzejiang1987@163.com
 * @Date : 2018/12/30 11:54
 */
public class MESQCRecord  implements Serializable {
    private String sid;//单号
    private String sbuid="Q00101";//业务号
    private String chtype;//QC检验类别
    private String zcno;//工序
    private String caused;//发起原因
    private String sbid;//设备编号
    private String sid1;//生产批次号
    private String slkid;//制令单号
    private String lotno;//测试批次号
    private String prd_no;//机型代号
    private String prd_name;//货品名称
    private String op;//检验员
    private String op_c;//质检作业员
    private int bok;//结果判定
    private int ps;//结果判定
    private int qty;//数量
    private int state;//工单状态
    private String mkdate = DateUtil.getCurrDateTime(ICL.DF_YMDT);//制单时间
    private String dcid = CommonUtils.getMacID();//设备，marc

    private String state1;//进度状态
    private String smake = BaseApplication.currUser.getUserCode();//制单人
    private String sorg = BaseApplication.currUser.getDeptCode();//部门

    public String getState1() {
        return state1;
    }

    public void setState1(String state1) {
        this.state1 = state1;
    }

    public String getSmake() {
        return smake;
    }

    public void setSmake(String smake) {
        this.smake = smake;
    }

    public String getSorg() {
        return sorg;
    }

    public void setSorg(String sorg) {
        this.sorg = sorg;
    }

    public int getSys_stated() {
        return sys_stated;
    }

    public void setSys_stated(int sys_stated) {
        this.sys_stated = sys_stated;
    }

    private int sys_stated=3;//系统状态

    public String getSid() {
        return sid;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

    public String getSbuid() {
        return sbuid;
    }

    public void setSbuid(String sbuid) {
        this.sbuid = sbuid;
    }

    public String getChtype() {
        return chtype;
    }

    public void setChtype(String chtype) {
        this.chtype = chtype;
    }

    public String getZcno() {
        return zcno;
    }

    public void setZcno(String zcno) {
        this.zcno = zcno;
    }

    public String getCaused() {
        return caused;
    }

    public void setCaused(String caused) {
        this.caused = caused;
    }

    public String getSbid() {
        return sbid;
    }

    public void setSbid(String sbid) {
        this.sbid = sbid;
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

    public String getLotno() {
        return lotno;
    }

    public void setLotno(String lotno) {
        this.lotno = lotno;
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

    public String getOp_c() {
        return op_c;
    }

    public void setOp_c(String op_c) {
        this.op_c = op_c;
    }

    public int getBok() {
        return bok;
    }

    public void setBok(int bok) {
        this.bok = bok;
        this.ps = bok;
    }

    public int getPs() {
        return ps;
    }

    public void setPs(int ps) {
        this.ps = ps;
        this.bok = ps;
    }

    public int getQty() {
        return qty;
    }

    public void setQty(int qty) {
        this.qty = qty;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getMkdate() {
        return mkdate;
    }

    public void setMkdate(String mkdate) {
        this.mkdate = mkdate;
    }

    public String getDcid() {
        return dcid;
    }

    public void setDcid(String dcid) {
        this.dcid = dcid;
    }
}
