package com.yimei.tableui;


import android.content.Context;
import android.util.AttributeSet;
import android.widget.HorizontalScrollView;

/***
 * 表格水平移动View
 */
public class BipHorizontalScrollView extends HorizontalScrollView{

    // 自定义的监听器
    private OnHorizontalScrollListener listener;

    public BipHorizontalScrollView(Context context){
        super(context);
    }

    public BipHorizontalScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BipHorizontalScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setOnHorizontalScrollListener(OnHorizontalScrollListener listener){
        this.listener = listener;
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        // 通知自定义的listener
        if (listener != null){
            listener.onHorizontalScrolled(this, l, t, oldl, oldt);
        }
    }

    //内部接口，用来监听系统的onScrollChangedListener监听到的数据
    interface OnHorizontalScrollListener {
        void onHorizontalScrolled(BipHorizontalScrollView view, int l, int t, int oldl, int oldt);
    }
}
