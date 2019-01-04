package com.yimei.data;

import java.io.Serializable;

/**
 * 与服务器交互，审核提交执行实体类
 * @Auther: fengzejiang1987@163.com
 * @Date : 2019/1/2 11:25
 */
public class CeaPars implements Serializable {
    private String sid="";//制单号码
    private String sbuid="";//业务码
    private String yjcontext="";//审批意见
    private int statefr;//来源状态
    private int stateto;//下一状态
    private String bup="1";// 0未审批 1审批通过 2驳回申请
    private String content="";//微信段内容
    private String tousr="";//下一个审批人Id
    private boolean ckd;

    public CeaPars(){}

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

    public String getYjcontext() {
        return yjcontext;
    }

    public void setYjcontext(String yjcontext) {
        this.yjcontext = yjcontext;
    }

    public int getStatefr() {
        return statefr;
    }

    public void setStatefr(int statefr) {
        this.statefr = statefr;
    }

    public int getStateto() {
        return stateto;
    }

    public void setStateto(int stateto) {
        this.stateto = stateto;
    }

    public String getBup() {
        return bup;
    }

    public void setBup(String bup) {
        this.bup = bup;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTousr() {
        return tousr;
    }

    public void setTousr(String tousr) {
        this.tousr = tousr;
    }

    public boolean isCkd() {
        return ckd;
    }

    public void setCkd(boolean ckd) {
        this.ckd = ckd;
    }
}
