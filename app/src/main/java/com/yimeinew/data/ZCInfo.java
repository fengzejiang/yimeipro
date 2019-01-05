package com.yimeinew.data;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import com.yimeinew.utils.CommCL;

import java.io.Serializable;

/**
 * @Auther: fengzejiang1987@163.com
 * @Date : 2018/12/27 12:15
 */
public class ZCInfo extends BaseObservable implements Serializable {
    private String id;//制成编码
    private String name;//制成名称
    private int ptime;//加工时间（分钟）
    private int startnum;//最大批次数
    private String ctrlnode="02";//控制节点
    private int bfast;//是否是快速过站
    private int segid;//前段，中段，后段
    private Class clazz; //跳转的Class

    private int attr = CommCL.ZC_ATTR_START|CommCL.ZC_ATTR_DONE;//属性，上料、开工、出站、加胶
    private int imgIndex;

    public ZCInfo(){}

    public ZCInfo(String zno) {
        this.id = zno;
    }

    public ZCInfo(String zno, String zName) {
        this.id = zno;
        this.name = zName;
    }

    public ZCInfo(String zno, String name, Class clazz) {
        this.id = zno;
        this.name = name;
        this.clazz = clazz;
    }
    @Bindable
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    @Bindable
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPtime() {
        return ptime;
    }

    public void setPtime(int ptime) {
        this.ptime = ptime;
    }


    public String getCtrlnode() {
        return ctrlnode;
    }

    public void setCtrlnode(String ctrlnode) {
        this.ctrlnode = ctrlnode;
    }

    public int getBfast() {
        return bfast;
    }

    public void setBfast(int bfast) {
        this.bfast = bfast;
    }
    @Bindable
    public Class getClazz() {
        return clazz;
    }

    public void setClazz(Class clazz) {
        this.clazz = clazz;
    }

    public int getAttr() {
        return attr;
    }

    public void setAttr(int attr) {
        this.attr = attr;
    }
    @Bindable
    public int getImgIndex() {
        return imgIndex;
    }

    public void setImgIndex(int imgIndex) {
        this.imgIndex = imgIndex;
    }

    @Override
    public String toString() {
        return "ZCInfo{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", ptime=" + ptime +
                ", startNum=" + startnum +
                ", ctrlnode='" + ctrlnode + '\'' +
                ", bfast=" + bfast +
                ", clazz=" + clazz +
                ", attr=" + attr +
                ", imgIndex=" + imgIndex +
                '}';
    }

    public int getStartnum() {
        return startnum;
    }

    public void setStartnum(int startnum) {
        this.startnum = startnum;
    }

    public int getSegid() {
        return segid;
    }

    public void setSegid(int segid) {
        this.segid = segid;
    }
}
