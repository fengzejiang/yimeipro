package com.yimeinew.entity;

import java.util.HashMap;

/**
 * @program: printserver
 * @description:
 * @author: HuangEnCi
 * @create: 2020-02-26 09:35
 */
public class PrintModel {
    private HashMap<String,String> moban;
    private HashMap<String,String> xitongziduan;
    private String fileName;
    private int num;
    private boolean bok;
    public PrintModel(){}
    public PrintModel(HashMap<String,String> moban, HashMap<String,String> xitongziduan){
        this.moban=moban;
        this.xitongziduan=xitongziduan;
    }
    public PrintModel(HashMap<String,String> moban, HashMap<String,String> xitongziduan, boolean bok){
        this.moban=moban;
        this.xitongziduan=xitongziduan;
        this.bok=bok;
    }
    public PrintModel(HashMap<String,String> moban, HashMap<String,String> xitongziduan, boolean bok, String fileName, int num){
        this.moban=moban;
        this.xitongziduan=xitongziduan;
        this.bok=bok;
        this.fileName=fileName;
        this.num=num;
    }
    public PrintModel(boolean bok){
        this.bok=bok;
    }

    public HashMap<String, String> getMoban() {
        return moban;
    }

    public void setMoban(HashMap<String, String> moban) {
        this.moban = moban;
    }

    public HashMap<String, String> getXitongziduan() {
        return xitongziduan;
    }

    public void setXitongziduan(HashMap<String, String> xitongziduan) {
        this.xitongziduan = xitongziduan;
    }

    public boolean isBok() {
        return bok;
    }

    public void setBok(boolean bok) {
        this.bok = bok;
    }
    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

}
