package com.yimei.data;

import java.io.Serializable;
import java.util.List;

/**
 * 审批流节点信息
 * @Auther: fengzejiang1987@163.com
 * @Date : 2019/1/2 12:11
 */
public class ApprovalFlowObj implements Serializable {

    private String stateId;
    private String stateName;

    private List<User> users;

    public ApprovalFlowObj(){}
    public String getStateId() {
        return stateId;
    }

    public void setStateId(String stateId) {
        this.stateId = stateId;
    }

    public String getStateName() {
        return stateName;
    }

    public void setStateName(String stateName) {
        this.stateName = stateName;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    @Override
    public String toString() {
        return "ApprovalFlowObj{" +
                "stateId='" + stateId + '\'' +
                ", stateName='" + stateName + '\'' +
                ", users=" + users +
                '}';
    }
}
