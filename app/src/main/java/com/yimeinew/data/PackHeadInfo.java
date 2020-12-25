package com.yimeinew.data;

import com.yimeinew.activity.base.BaseApplication;
import com.yimeinew.utils.CommonUtils;
import com.yimeinew.utils.DateUtil;
import com.yimeinew.utils.ICL;

import java.io.Serializable;

/**
 * 模组内装表头-扫喷码
 */
public class PackHeadInfo implements Serializable {
    private String lotno;//主键
    private String sbuid="F0028";//单据类型
    private String slkid;//工单号
    private String sbid;//测试设备
    private String prd_no;//货品代号
    private String prd_name;//货品名称
    private int minqty=0;//单盘数量
    private int binfullqty;//满箱数量
    private String op;//作业员
    private int qty=1;//已包装数量
    private String cus_no;//客户代号
    private String cus_prd_no="";//客户料号
    private String printid="330";//打印模板
    private String bincode;//bincode
    private String cus_pn;//内装包装包号
    private String pdate= DateUtil.getCurrDateTime(ICL.DF_YMDT);//包装时间
    private String os_no;//订单号
    private int pur_qty;//需求数量
    private int fin_qty;//完成数量
    private int prtqty=2;//打印数量
    private int state=0;//状态
    private String mkdate= DateUtil.getCurrDateTime(ICL.DF_YMDT);//制单时间
    private String sorg= BaseApplication.currUser.getDeptCode();//部门
    private String zcno="S21";
    private String rslkid;

    private String sid1;//只是为了缓存Tray号
    private int totalqty;
    private String prd_mark;
    private String rem;
    private String dcid= CommonUtils.getMacID();

    public PackHeadInfo(){}
    public PackHeadInfo(String slkid,String prd_no,String prd_name,int binfullqty,String op,String cus_no,String bincode){
        this.slkid=slkid;
        this.prd_no=prd_no;
        this.prd_name=prd_name;
        this.binfullqty=binfullqty;
        this.op=op;
        this.cus_no=cus_no;
        this.bincode=bincode;
        this.prd_mark=bincode;
    }
    public String getLotno() {
        return lotno;
    }

    public void setLotno(String lotno) {
        this.lotno = lotno;
    }

    public String getSbuid() {
        return sbuid;
    }

    public void setSbuid(String sbuid) {
        this.sbuid = sbuid;
    }

    public String getSlkid() {
        return slkid;
    }

    public void setSlkid(String slkid) {
        this.slkid = slkid;
    }

    public String getSbid() {
        return sbid;
    }

    public void setSbid(String sbid) {
        this.sbid = sbid;
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

    public int getMinqty() {
        return minqty;
    }

    public void setMinqty(int minqty) {
        this.minqty = minqty;
    }

    public int getBinfullqty() {
        return binfullqty;
    }

    public void setBinfullqty(int binfullqty) {
        this.binfullqty = binfullqty;
    }

    public String getOp() {
        return op;
    }

    public void setOp(String op) {
        this.op = op;
    }

    public int getQty() {
        return qty;
    }

    public void setQty(int qty) {
        this.qty = qty;
    }

    public String getCus_no() {
        return cus_no;
    }

    public void setCus_no(String cus_no) {
        this.cus_no = cus_no;
    }

    public String getCus_prd_no() {
        return cus_prd_no;
    }

    public void setCus_prd_no(String cus_prd_no) {
        this.cus_prd_no = cus_prd_no;
    }

    public String getPrintid() {
        return printid;
    }

    public void setPrintid(String printid) {
        this.printid = printid;
    }

    public String getBincode() {
        return bincode;
    }

    public void setBincode(String bincode) {
        this.bincode = bincode;
        this.prd_mark=bincode;
    }

    public String getCus_pn() {
        return cus_pn;
    }

    public void setCus_pn(String cus_pn) {
        this.cus_pn = cus_pn;
    }

    public String getPdate() {
        return pdate;
    }

    public void setPdate(String pdate) {
        this.pdate = pdate;
    }

    public String getOs_no() {
        return os_no;
    }

    public void setOs_no(String os_no) {
        this.os_no = os_no;
    }

    public int getPur_qty() {
        return pur_qty;
    }

    public void setPur_qty(int pur_qty) {
        this.pur_qty = pur_qty;
        this.totalqty=pur_qty;
    }

    public int getFin_qty() {
        return fin_qty;
    }

    public void setFin_qty(int fin_qty) {
        this.fin_qty = fin_qty;
    }

    public int getPrtqty() {
        return prtqty;
    }

    public void setPrtqty(int prtqty) {
        this.prtqty = prtqty;
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

    public String getSorg() {
        return sorg;
    }

    public void setSorg(String sorg) {
        this.sorg = sorg;
    }

    public String getZcno() {
        return zcno;
    }

    public void setZcno(String zcno) {
        this.zcno = zcno;
    }

    public String getSid1() {
        return sid1;
    }

    public void setSid1(String sid1) {
        this.sid1 = sid1;
    }


    public int getTotalqty() {
        return totalqty;
    }

    public void setTotalqty(int totalqty) {
        this.totalqty = totalqty;
    }

    public String getPrd_mark() {
        return prd_mark;
    }

    public void setPrd_mark(String prd_mark) {
        this.prd_mark = prd_mark;
    }

    public String getRem() {
        return rem;
    }

    public void setRem(String rem) {
        this.rem = rem;
    }

    public String getRslkid() {
        return rslkid;
    }

    public void setRslkid(String rslkid) {
        this.rslkid = rslkid;
    }

    public String getDcid() {
        return dcid;
    }

    public void setDcid(String dcid) {
        this.dcid = dcid;
    }
}
