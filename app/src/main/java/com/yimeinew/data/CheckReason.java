package com.yimeinew.data;

import java.io.Serializable;

/**
 * 检验发起原因
 * @Auther: fengzejiang1987@163.com
 * @Date : 2018/12/28 10:46
 */
public class CheckReason implements Serializable {
    private String id;
    private String fid;
    private String name;
    private String qtype;

    public CheckReason() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFid() {
        return fid;
    }

    public void setFid(String fid) {
        this.fid = fid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    public String getQtype() {
        return qtype;
    }

    public void setQtype(String qtype) {
        this.qtype = qtype;
    }
}
