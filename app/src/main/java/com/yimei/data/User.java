package com.yimei.data;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import com.yimei.activity.BR;

import java.io.Serializable;

/**
 * @Auther: fengzejiang1987@163.com
 * @Date : 2018/12/7 12:27
 */
public class User extends BaseObservable implements Serializable {

    private String userCode;
    private String userName;

    private String userPassword;

    private String cmcName;
    private String cmcCode;
    private String deptCode;

    public User(){}
    public User(String userCode){
        this.userCode = userCode;
    }
    public User(String userCode, String userPassword) {
        this.userCode = userCode;
        this.userPassword = userPassword;
    }
    @Bindable
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
        notifyPropertyChanged(BR.userName);
    }
    @Bindable
    public String getUserPassword() {
        return userPassword;
    }

    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
        notifyPropertyChanged(BR.userPassword);
    }
    @Bindable
    public String getUserCode() {
        return userCode;
    }

    public void setUserCode(String userCode) {
        this.userCode = userCode;
    }
    @Bindable
    public String getCmcName() {
        return cmcName;
    }
    public void setCmcName(String cmcName) {
        this.cmcName = cmcName;
    }
    @Bindable
    public String getCmcCode() {
        return cmcCode;
    }

    public void setCmcCode(String cmcCode) {
        this.cmcCode = cmcCode;
    }
    @Bindable
    public String getDeptCode() {
        return deptCode;
    }

    public void setDeptCode(String deptCode) {
        this.deptCode = deptCode;
    }

    @Override
    public String toString() {
        return "User{" +
                "userCode='" + userCode + '\'' +
                ", userName='" + userName + '\'' +
                ", userPassword='" + userPassword + '\'' +
                ", cmcName='" + cmcName + '\'' +
                ", cmcCode='" + cmcCode + '\'' +
                ", deptCode='" + deptCode + '\'' +
                '}';
    }
}
