package com.yimeinew.data;

import com.yimeinew.activity.base.BaseApplication;
import com.yimeinew.utils.CommonUtils;
import com.yimeinew.utils.DateUtil;
import com.yimeinew.utils.ICL;

import java.io.Serializable;

/**
 * 加胶实体类对象-->
 * @Auther: fengzejiang1987@163.com
 * @Date : 2019/1/3 15:43
 */
public class MesGluingRecord implements Serializable {
    private String sid;//单号
    private String zcno="31";//制成代号
    private String op;//作业员
    private String sbid;//设备号
    private String prtno;//胶杯批号
    private String slkid;//制令单号
    private String sbuid="D2010";//业务号
    private String prd_no;//机型
    private int qty=0;//数量
    private int sys_stated=3;//系统数据状态
    private int state=0;//状态
    private String indate = DateUtil.getCurrDateTime(ICL.DF_YMDT);//系统时间
    private String dcid = CommonUtils.getMacID();//PDAID
    private String mkdate;//胶杯打印时间
    private String edate;//到期时间
    private String prd_name;//产品名称
    private String sorg = BaseApplication.currUser.getDeptCode();//部门

    public String getSid() {
        return sid;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

    public String getZcno() {
        return zcno;
    }

    public void setZcno(String zcno) {
        this.zcno = zcno;
    }

    public String getOp() {
        return op;
    }

    public void setOp(String op) {
        this.op = op;
    }

    public String getSbid() {
        return sbid;
    }

    public void setSbid(String sbid) {
        this.sbid = sbid;
    }

    public String getPrtno() {
        return prtno;
    }

    public void setPrtno(String prtno) {
        this.prtno = prtno;
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

    public String getIndate() {
        return indate;
    }

    public void setIndate(String indate) {
        this.indate = indate;
    }

    public String getDcid() {
        return dcid;
    }

    public void setDcid(String dcid) {
        this.dcid = dcid;
    }

    public String getMkdate() {
        return mkdate;
    }

    public void setMkdate(String mkdate) {
        this.mkdate = mkdate;
    }

    public String getEdate() {
        return edate;
    }

    public void setEdate(String edate) {
        this.edate = edate;
    }

    public String getPrd_name() {
        return prd_name;
    }

    public void setPrd_name(String prd_name) {
        this.prd_name = prd_name;
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
}
