package com.yimeinew.tableui.entity;

import com.yimeinew.adapter.qc.TableItemEvent;

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
    private int display=0;//区分显示类型 0:文本,1:NGOK,2:点击弹框  当=1，=2时，表格初始化时，必须指定isKey

    private String backgroundColor;
    private String textColor;
    private TableItemEvent tableItemEvent;
    private boolean key;//数据key用来区分数据，key=1时表示这个字段是key。可以指定多个字段为key。目前主要用于，因为android表格itemview重用机制导致数据混乱
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
    public int getDisplay() {
        return display;
    }
    public void setDisplay(int display) {
        this.display = display;
    }
    public TableItemEvent getTableItemEvent() {
        return tableItemEvent;
    }

    public void setTableItemEvent(TableItemEvent tableItemEvent) {
        this.tableItemEvent = tableItemEvent;
    }

    public String getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(String backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public String getTextColor() {
        return textColor;
    }

    public void setTextColor(String textColor) {
        this.textColor = textColor;
    }

    public boolean isKey() {
        return key;
    }

    public void setKey(boolean isKey) {
        this.key = isKey;
    }
}
