package com.yimeinew.data;

import java.io.Serializable;

/**
 * 胶杯信息实体类对应的辅助 MESGLUEJOBN
 * @Auther: fengzejiang1987@163.com
 * @Date : 2019/1/3 18:16
 */
public class GluingInfo implements Serializable {
    private String sid;//配胶作业单号
    private String mkdate;//配胶时间
    private String mo_no;//配胶工单
    private String tprn;//配胶打印时间
    private String sopr;//配胶作业员
    private String prtno;//胶杯条码
    private String vdate;//胶杯到期时间
    private String mixing_time;//混胶时间
    private String newly_time;//最近到期时间
    private String prdno;//产品编码
    private String name;//产品型号
    private int effective_time;//有效时长(小时)
    private int fr_add_time;//首次加胶效期(分钟)
    private int fr_mixing_time;//首次搅拌效期(分钟)
    private int mi;//当前时间减去最近到期时间(分钟)
    private int zt;//当前时间减去最近到期时间<=0 =3，0~=30 =2，30>,<60=1 其他0(没有做混胶)
    private int mixtime;//当前时间和混胶时间的差额

    public int getFr_mixing_time() {
        return fr_mixing_time;
    }

    public void setFr_mixing_time(int fr_mixing_time) {
        this.fr_mixing_time = fr_mixing_time;
    }

    public int getMixtime() {
        return mixtime;
    }

    public void setMixtime(int mixtime) {
        this.mixtime = mixtime;
    }

    public String getSid() {
        return sid;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

    public String getMkdate() {
        return mkdate;
    }

    public void setMkdate(String mkdate) {
        this.mkdate = mkdate;
    }

    public String getMo_no() {
        return mo_no;
    }

    public void setMo_no(String mo_no) {
        this.mo_no = mo_no;
    }

    public String getTprn() {
        return tprn;
    }

    public void setTprn(String tprn) {
        this.tprn = tprn;
    }

    public String getSopr() {
        return sopr;
    }

    public void setSopr(String sopr) {
        this.sopr = sopr;
    }

    public String getPrtno() {
        return prtno;
    }

    public void setPrtno(String prtno) {
        this.prtno = prtno;
    }

    public String getVdate() {
        return vdate;
    }

    public void setVdate(String vdate) {
        this.vdate = vdate;
    }

    public String getMixing_time() {
        return mixing_time;
    }

    public void setMixing_time(String mixing_time) {
        this.mixing_time = mixing_time;
    }

    public String getNewly_time() {
        return newly_time;
    }

    public void setNewly_time(String newly_time) {
        this.newly_time = newly_time;
    }

    public String getPrdno() {
        return prdno;
    }

    public void setPrdno(String prdno) {
        this.prdno = prdno;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getEffective_time() {
        return effective_time;
    }

    public void setEffective_time(int effective_time) {
        this.effective_time = effective_time;
    }

    public int getFr_add_time() {
        return fr_add_time;
    }

    public void setFr_add_time(int fr_add_time) {
        this.fr_add_time = fr_add_time;
    }

    public int getMi() {
        return mi;
    }

    public void setMi(int mi) {
        this.mi = mi;
    }

    public int getZt() {
        return zt;
    }

    public void setZt(int zt) {
        this.zt = zt;
    }
}
