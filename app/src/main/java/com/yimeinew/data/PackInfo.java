package com.yimeinew.data;

import com.yimeinew.activity.base.BaseApplication;
import com.yimeinew.utils.DateUtil;
import com.yimeinew.utils.ICL;

import java.io.Serializable;

/**
 * 模组内装表头-扫喷码
 */
public class PackInfo implements Serializable {
    private String lotno;//主键
    private String sid1;//喷码
    private String slkid;//工单
    private int qty=1;//数量
    private String tray;//tray批次号
    private int minqty;//单盘数量
    public PackInfo(){};
    public PackInfo(String lotno,String sid1,String slkid,String tray,int minqty){
        this.lotno=lotno;
        this.sid1=sid1;
        this.slkid=slkid;
        this.tray=tray;
        this.minqty=minqty;
    };

    public String getLotno() {
        return lotno;
    }

    public void setLotno(String lotno) {
        this.lotno = lotno;
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

    public int getQty() {
        return qty;
    }

    public void setQty(int qty) {
        this.qty = qty;
    }

    public String getTray() {
        return tray;
    }

    public void setTray(String tray) {
        this.tray = tray;
    }

    public int getMinqty() {
        return minqty;
    }

    public void setMinqty(int minqty) {
        this.minqty = minqty;
    }
}
