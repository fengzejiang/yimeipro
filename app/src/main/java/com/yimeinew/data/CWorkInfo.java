package com.yimeinew.data;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * @Auther: fengzejiang1987@163.com
 * @Date : 2019/1/2 12:11
 */
public class CWorkInfo implements Serializable {
    private String state="";//当前节点
    private String upState="0";
    private User upUser;
    private boolean checked = false;//是否已经审核
    private ArrayList<User> chkInfos;//待审核人列表
    ArrayList<ApprovalFlowObj> list;//下一节点信息

    public CWorkInfo(){}
    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getUpState() {
        return upState;
    }

    public void setUpState(String upState) {
        this.upState = upState;
    }

    public User getUpUser() {
        return upUser;
    }

    public void setUpUser(User upUser) {
        this.upUser = upUser;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public ArrayList<User> getChkInfos() {
        return chkInfos;
    }

    public void setChkInfos(ArrayList<User> chkInfos) {
        this.chkInfos = chkInfos;
    }

    public ArrayList<ApprovalFlowObj> getList() {
        return list;
    }

    public void setList(ArrayList<ApprovalFlowObj> list) {
        this.list = list;
    }

    @Override
    public String toString() {
        return "CWorkInfo{" +
                "state='" + state + '\'' +
                ", upState='" + upState + '\'' +
                ", upUser=" + upUser +
                ", checked=" + checked +
                ", chkInfos=" + chkInfos +
                ", list=" + list +
                '}';
    }
}
