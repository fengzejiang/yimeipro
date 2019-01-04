package com.yimei.data;

import java.io.Serializable;

/**
 * 设备实体类
 * @Auther: fengzejiang1987@163.com
 * @Date : 2018/12/21 16:50
 */
public class EquipmentInfo implements Serializable {
    private String id; //设备编码
    private String name; //设备名称
    private String zcno; //归属制成
    private String prdno; //在产机种
    private String prtno; //胶杯号
    private int firstchk; //首件检验标志
    private int sbstate; //设备状态 0:正常;1:报修;2:维修中;3:待确认
    private String glue; //胶水料号
    private String stents; //支架类别

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getZcno() {
        return zcno;
    }

    public void setZcno(String zcno) {
        this.zcno = zcno;
    }

    public String getPrdno() {
        return prdno;
    }

    public void setPrdno(String prdno) {
        this.prdno = prdno;
    }

    public String getPrtno() {
        return prtno;
    }

    public void setPrtno(String prtno) {
        this.prtno = prtno;
    }

    public int getFirstchk() {
        return firstchk;
    }

    public void setFirstchk(int firstchk) {
        this.firstchk = firstchk;
    }

    public int getSbstate() {
        return sbstate;
    }

    public void setSbstate(int sbstate) {
        this.sbstate = sbstate;
    }

    public String getGlue() {
        return glue;
    }

    public void setGlue(String glue) {
        this.glue = glue;
    }

    public String getStents() {
        return stents;
    }

    public void setStents(String stents) {
        this.stents = stents;
    }
}
