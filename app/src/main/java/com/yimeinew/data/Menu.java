package com.yimeinew.data;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import com.yimeinew.activity.BR;
import com.yimeinew.activity.R;
import com.yimeinew.utils.CommCL;

/***
 * App中菜单实体类
 */
public class Menu extends BaseObservable {
    private String mid;//菜单编码
    private String name;//菜单名称
    private int imgIndex;//图片ID

    private Class menuClass;
    public Menu(){}
    public Menu(String id, String name){
        this.mid = id;
        this.name = name;
        imgIndex = CommCL.menuImgMap.containsKey(id)? CommCL.menuImgMap.get(id): R.drawable.mozu;
        if("D0050".equals(id)){
            this.name = "解绑料盒";
        }
        if("K0".equals(id)){
            this.name = "ORT取样";
        }
        menuClass = CommCL.menuActivitys.get(id);//绑定对应的Activity
    }

    @Bindable
    public String getMid() {
        return mid;
    }

    public void setMid(String mid) {
        this.mid = mid;
        notifyPropertyChanged(BR.mid);
    }
    @Bindable
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        notifyPropertyChanged(BR.name);
    }
    @Bindable
    public int getImgIndex() {
        return imgIndex;
    }

    public void setImgIndex(int img_index) {
        this.imgIndex = img_index;
        notifyPropertyChanged(BR.imgIndex);
    }

    public Class getMenuClass() {
        return menuClass;
    }

    @Override
    public String toString() {
        return "Menu{" +
                "mid='" + mid + '\'' +
                ", name='" + name + '\'' +
                ", imgIndex=" + imgIndex +
                '}';
    }
}
