package com.yimeinew.adapter;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import com.yimeinew.activity.R;
import com.yimeinew.activity.base.BaseApplication;

import com.yimeinew.activity.databinding.MainGridItemBinding;
import com.yimeinew.data.Menu;
import com.yimeinew.modelInterface.BaseView;
import com.yimeinew.presenter.MainMenuPresenter;

import java.util.ArrayList;

/**
 * @Auther: fengzejiang1987@163.com
 * @Date : 2018/12/8 23:15
 */
public class MainAdapter extends BaseAdapter {
    private static  final String TAG = MainAdapter.class.getName();
    private Context context;

    private ArrayList<Menu> menuList;
    MainMenuPresenter mainMenuPresenter;

    public MainAdapter(Context context){
        this.context = context;
        mainMenuPresenter = new MainMenuPresenter((BaseView) context);
        menuList = BaseApplication.getMenuList();
    }
    @Override
    public int getCount() {
        return menuList.size();
    }

    @Override
    public Object getItem(int position) {
        return menuList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MainGridItemBinding mainGridItemBinding = DataBindingUtil.inflate(LayoutInflater.from(this.context), R.layout.main_grid_item,parent,false);
        Menu mm = menuList.get(position);
        mainGridItemBinding.setMenu(mm);
        mainGridItemBinding.setPresenter(mainMenuPresenter);
        mainGridItemBinding.executePendingBindings();
        return mainGridItemBinding.getRoot();
    }
}
