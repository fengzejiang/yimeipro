package com.yimeinew.data;

import java.io.Serializable;

/**
 * 产品物料信息
 * @Auther: fengzejiang1987@163.com
 * @Date : 2018/12/25 15:04
 */
public class ChargingMaterial implements Serializable {
    private String sid;//单号
    private int cid;//项次
    private String dcid;//PDAiD
    private String hpdate;//日期，含时分秒
    private String prd_no;//产品编码
    private String prd_name;//产品名称
    private String bat_no;//批次号
    private int qty;//数量
    private String op;//操作员
    private String prd_mark;//BinCode
    private String bincode;//BinCode
    private String sbid;//设备编码
    private String zcno;//制成编码

    private int sys_stated;//状态（系统使用字段）

    public int getSys_stated() {
        return sys_stated;
    }

    public void setSys_stated(int sys_stated) {
        this.sys_stated = sys_stated;
    }

    public String getSid() {
        return sid;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

    public int getCid() {
        return cid;
    }

    public void setCid(int cid) {
        this.cid = cid;
    }

    public String getDcid() {
        return dcid;
    }

    public void setDcid(String dcid) {
        this.dcid = dcid;
    }

    public String getHpdate() {
        return hpdate;
    }

    public void setHpdate(String hpdate) {
        this.hpdate = hpdate;
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

    public String getBat_no() {
        return bat_no;
    }

    public void setBat_no(String bat_no) {
        this.bat_no = bat_no;
    }

    public int getQty() {
        return qty;
    }

    public void setQty(int qty) {
        this.qty = qty;
    }

    public String getOp() {
        return op;
    }

    public void setOp(String op) {
        this.op = op;
    }

    public String getPrd_mark() {
        return prd_mark;
    }

    public void setPrd_mark(String prd_mark) {
        this.prd_mark = prd_mark;
        this.bincode = prd_mark;
    }

    public String getBincode() {
        return bincode;
    }

    public void setBincode(String bincode) {
        this.prd_mark = bincode;
        this.bincode = bincode;
    }

    public String getSbid() {
        return sbid;
    }

    public void setSbid(String sbid) {
        this.sbid = sbid;
    }

    public String getZcno() {
        return zcno;
    }

    public void setZcno(String zcno) {
        this.zcno = zcno;
    }
}
