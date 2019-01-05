package com.yimeinew.tableui.entity;

import java.util.HashMap;

/**
 * @Auther: fengzejiang1987@163.com
 * @Date : 2018/12/17 11:23
 */
public class HeaderRowInfo {
    private String id;//取横向值会用到
    private String name;//列名称
    private int width=120;//列的宽度
    private int height=100;//列的高度

    public int getAttr() {
        return attr;
    }

    public void setAttr(int attr) {
        this.attr = attr;
    }

    public HashMap<String, String> getContrastMap() {
        return contrastMap;
    }

    public void setContrastMap(HashMap<String, String> contrastMap) {
        this.contrastMap = contrastMap;
    }

    public HashMap<String, String> getContrastColors() {
        return contrastColors;
    }

    public void setContrastColors(HashMap<String, String> contrastColors) {
        this.contrastColors = contrastColors;
    }

    private int attr;

    private HashMap<String,String> contrastMap;

    private HashMap<String,String> contrastColors;


    public HeaderRowInfo(){}
    public HeaderRowInfo(String id,String name){
        this.id = id;
        this.name = name;
    }

    public HeaderRowInfo(String id,String name,int width){
        this.id = id;
        this.name = name;
        this.width =width;
    }
    public HeaderRowInfo(String id,String name,int width,int height){
        this.id = id;
        this.name = name;
        this.width =width;
        this.height = height;
    }
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

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }
}
