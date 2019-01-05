package com.yimeinew.tableui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

/**
 * 表格布局面板
 * @Auther: fengzejiang1987@163.com
 * @Date : 2018/12/14 17:05
 */
public class TablePanelView extends RelativeLayout {

    private AbstractTableViewAdapter tableAdapter;
    public TablePanelView(Context context) {
        super(context);
    }

    public TablePanelView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TablePanelView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public AbstractTableViewAdapter getAdapter() {
        return tableAdapter;
    }

    public void setAdapter(AbstractTableViewAdapter adapter) {
        this.tableAdapter = adapter;
        adapter.initAdapter();
    }

}
