package com.yimei.data;

import java.io.Serializable;

/**
 * 制程检验项目
 * @Auther: fengzejiang1987@163.com
 * @Date : 2018/12/28 11:14
 */
public class CheckProjectInfo implements Serializable {
    private int cid;//项次
    private String xmbm;//项目编码
    private String xmmc;//项目名称
    private String standard="";//标准值
    private String value1="";//标准值
    private String value2="";//标准值
    private String value3="";//标准值
    private String value4="";//标准值
    private String value5="";//标准值
    private String value6="";//标准值
    private String bok = "0";//检验结果
    private String remark = "";//备注
    private int itm;//项次

    public int getItm() {
        return itm;
    }

    public void setItm(int itm) {
        this.itm = itm;
        this.cid = itm;
    }

    public CheckProjectInfo() {
    }

    public int getCid() {
        return cid;
    }

    public void setCid(int cid) {
        this.cid = cid;
        this.itm = cid;
    }

    public String getXmbm() {
        return xmbm;
    }

    public void setXmbm(String xmbm) {
        this.xmbm = xmbm;
    }

    public String getXmmc() {
        return xmmc;
    }

    public void setXmmc(String xmmc) {
        this.xmmc = xmmc;
    }

    public String getStandard() {
        return standard;
    }

    public void setStandard(String standard) {
        this.standard = standard;
    }

    public String getValue1() {
        return value1;
    }

    public void setValue1(String value1) {
        this.value1 = value1;
    }

    public String getValue2() {
        return value2;
    }

    public void setValue2(String value2) {
        this.value2 = value2;
    }

    public String getValue3() {
        return value3;
    }

    public void setValue3(String value3) {
        this.value3 = value3;
    }

    public String getValue4() {
        return value4;
    }

    public void setValue4(String value4) {
        this.value4 = value4;
    }

    public String getValue5() {
        return value5;
    }

    public void setValue5(String value5) {
        this.value5 = value5;
    }

    public String getValue6() {
        return value6;
    }

    public void setValue6(String value6) {
        this.value6 = value6;
    }

    public String getBok() {
        return bok;
    }

    public void setBok(String bok) {
        this.bok = bok;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
