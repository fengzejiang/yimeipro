package com.yimeinew.tableui;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.yimeinew.tableui.entity.HeaderRowInfo;
import com.yimeinew.utils.ICL;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractTableViewAdapter {

    private static final String TAG = AbstractTableViewAdapter.class.getSimpleName();

    private Context context;

    /**
     * 两个横向滑动layout
     */
    private BipHorizontalScrollView headerHorizontalScrollView;
    private BipHorizontalScrollView dataHorizontalScrollView;

    /**
     * 整个页面的所有布局
     */
    private TablePanelView tablePanelView;//外层的根布局
    private TextView tv_title;//左上角的title
    private LinearLayout headerLinearLayout;//上方的表头
    private ListView leftHeaderListView;//左边的表头
    private ListView centerDataListView;//中间的内容部分
    private LinearLayout dataItemLinearLayout;//中间的内容部分的子布局
    private SwipeRefreshLayout swipeRefreshLayout;//中间ListView外层的下拉刷新布局

    /**
     * 标题的宽和高,同时也是列表头的宽和列表头的高
     */
    private int titleWidth = 150;
    private int titleHeight = 100;
    private int columnItemHeight = 100;

    private String title = "序号";
    private int titleBackgroundResource;
    private List<String> columnDataList;
    private List<HeaderRowInfo> rowDataList;

    private String columnColor = "#F1F1F1";//default color of column
    private String titleColor = "#3F51B5";//default color of title
    private String rowColor = "#3F51B5";//default color of title
    private String headerTextColor = "#F1F1F1";//default color of title

    private Drawable rowDivider;
    private Drawable columnDivider;

    /**
     * 默认关闭下拉刷新
     */
    private boolean swipeRefreshEnable = false;
    /**
     * 标志位，是否使用了默认的column实现
     */
    private boolean defaultColumn = false;

    private int initPosition = 0;//列表显示的初始值，默认第一条数据显示在最上面

    private BaseAdapter columnAdapter;
    private BaseAdapter contentAdapter;

    private SwipeRefreshLayout.OnRefreshListener onRefreshListener = new DefaultRefreshListener();

    /**
     * 两个监听器，分别控制水平和垂直方向上的同步滑动
     */
    private HorizontalScrollListener horizontalScrollListener = new HorizontalScrollListener();
    private VerticalScrollListener verticalScrollListener = new VerticalScrollListener();


    /**
     * constructor
     *
     * @param centerDataListView 内容的ListView
     */
    public AbstractTableViewAdapter(Context context, TablePanelView tablePanelView, ListView centerDataListView) {
        this.context = context;
        this.tablePanelView = tablePanelView;
        this.centerDataListView = centerDataListView;
    }

    //region APIs

    /**
     * 设置表的标题
     *
     * @param title title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * 设置表标题的背景
     *
     * @param resourceId a drawable resource id
     */
    public void setTitleBackgroundResource(int resourceId) {
        this.titleBackgroundResource = resourceId;
    }

    /**
     * 设置表头的宽度
     *
     * @param titleWidth title width
     */
    public void setTitleWidth(int titleWidth) {
        this.titleWidth = titleWidth;
    }

    /**
     * 设置表头的高度
     *
     * @param titleHeight title height
     */
    public void setTitleHeight(int titleHeight) {
        this.titleHeight = titleHeight;
    }


    /**
     * 设置横向表头的标题（！！必须调用！！）
     *
     * @param rowDataList data list of row layout, must be a List<String>
     */
    public void setRowDataList(List<HeaderRowInfo> rowDataList) {
        this.rowDataList = rowDataList;
    }

    /**
     * 设置纵向表头的内容
     *
     * @param columnDataList data list of column layout, must be a List<String>. if you don`t call
     *                       this method, the default column list will be used
     */
    public void setColumnDataList(List<String> columnDataList) {
        this.columnDataList = columnDataList;
    }

    /**
     * 横向表头的分割线
     */
    public void setRowDivider(Drawable rowDivider) {
        this.rowDivider = rowDivider;
    }

    /**
     * 纵向表头的分割线
     */
    public void setColumnDivider(Drawable columnDivider) {
        this.columnDivider = columnDivider;
    }

    /**
     * 设置纵向表头的背景色
     *
     * @param columnColor background color of column
     */
    public void setColumnColor(String columnColor) {
        this.columnColor = columnColor;
    }

    /**
     * 设置标题的背景色
     *
     * @param titleColor background color of title
     */
    public void setTitleColor(String titleColor) {
        this.titleColor = titleColor;
    }

    /**
     * 设置横向表头的背景色
     *
     * @param rowColor background color of row
     */
    public void setRowColor(String rowColor) {
        this.rowColor = rowColor;
    }

    /**
     * 设置纵向表头的适配器
     *
     * @param columnAdapter adapter of column ListView
     */
    public void setColumnAdapter(BaseAdapter columnAdapter) {
        this.columnAdapter = columnAdapter;
    }

    /**
     * 设置content的初始position
     * <p>
     * 比如你想进入这个Activity的时候让第300条数据显示在屏幕上（前提是该数据存在）
     * 那么在这里传入299即可
     *
     * @param initPosition position
     */
    public void setInitPosition(int initPosition) {
        this.initPosition = initPosition;
    }

    /**
     * 返回中间内容部分的ListView
     *
     * @return listView of content
     */
    public ListView getContentListView() {
        return centerDataListView;
    }

    /**
     * 返回左边表头的ListView
     *
     * @return listView of column(left)
     */
    public ListView getColumnListView() {
        return leftHeaderListView;
    }

    /**
     * 返回上访表头的最外层布局
     *
     * @return a CheckableLinearLayout
     */
    public LinearLayout getRowLayout() {
        return headerLinearLayout;
    }

    /**
     * 设置是否开启下拉刷新（默认关闭）
     *
     * @param bool pass true to enable pullToRefresh
     */
    public void setSwipeRefreshEnabled(boolean bool) {
        swipeRefreshEnable = bool;
    }

    /**
     * 这里有点蛋疼，因为控件是在initAdapter中赋值的，但是这里要用
     * 所以如果开发者在setAdapter之前调用了该方法，则必须对控件进行赋值
     * 但如果赋值了，还得判断开发者是否设置了初始位置，因为控件默认开启，如果初始位置不为0，则控件启用
     * 这样会造成在中间阶段下拉会触发监听，因此对initPosition再进行一次判断
     * 当用户发生了滑动操作，控件的状态会被随即改变
     *
     * @param listener
     */
    public void setOnRefreshListener(SwipeRefreshLayout.OnRefreshListener listener) {
        this.onRefreshListener = listener;
        if (swipeRefreshLayout == null) {
            swipeRefreshLayout = new SwipeRefreshLayout(context);
            if (initPosition != 0) {
                swipeRefreshLayout.setEnabled(false);
            }
        }
        swipeRefreshLayout.setOnRefreshListener(listener);
        Log.d(TAG, "setOnRefreshListener: " + onRefreshListener.toString());
    }

    public SwipeRefreshLayout getSwipeRefreshLayout() {
        return swipeRefreshLayout;
    }

    //endregion

    /**
     * 在该方法中返回contentList的adapter
     *
     * @return content部分的adapter
     */
    protected abstract BaseAdapter getContentAdapter();

    /**
     * 初始化总Adapter，加载数据到视图
     */
    void initAdapter() {

        contentAdapter = getContentAdapter();

        if (contentAdapter == null){
            try {
                throw new Exception("content adapter can NOT be null");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        reorganizeViewGroup();

        headerHorizontalScrollView.setOnHorizontalScrollListener(horizontalScrollListener);
        dataHorizontalScrollView.setOnHorizontalScrollListener(horizontalScrollListener);

        centerDataListView.setOnScrollListener(verticalScrollListener);
        leftHeaderListView.setOnScrollListener(verticalScrollListener);
    }

    /**
     * 更新ContentList数据后需要调用此方法来刷新列表
     * <p>
     * 该方法会判断是否使用了默认的纵向表头，如果是，则自动更新表头
     * 如果不是，则不更新纵向表头，交给开发者自己去更新
     * 开发者可以调用{@link #getColumnAdapter()}以获得columnAdapter
     */
    public void notifyDataSetChanged() {
        // 先刷新lv_content的数据，然后根据判断决定是否要刷新表头的数据
        contentAdapter.notifyDataSetChanged();
        if (defaultColumn) {
            // 最好是让columnList跟着contentList变，不要new对象
            // 所以要获得contentList的新长度,即要获得contentList对象
            int newLength = contentAdapter.getCount();
            if (newLength < columnDataList.size()) {
                //删除了部分数据
                //从尾部开始删除元素，直到长度和contentList相同
                while (columnDataList.size() != newLength) {
                    columnDataList.remove(columnDataList.size() - 1);
                }
            } else {
                //增加了部分数据
                while (columnDataList.size() != newLength) {
                    columnDataList.add(String.valueOf(columnDataList.size() + 1));
                }
            }
            columnAdapter.notifyDataSetChanged();
        }
    }


    /**
     * 核心代码：
     * 整理重组整个表的布局
     * <p>
     * 主要包含4个部分
     * 1. title
     * 2. row
     * 3. column
     * 4. content
     */
    private void reorganizeViewGroup() {

        centerDataListView.setAdapter(contentAdapter);
        centerDataListView.setVerticalScrollBarEnabled(true);

        // clear root viewGroup
        tablePanelView.removeView(centerDataListView);

        // 1. title (TextView --> PanelListLayout)
        tv_title = new TextView(context);
        tv_title.setText(title);
        if (titleBackgroundResource != 0) {
            tv_title.setBackgroundResource(titleBackgroundResource);
        }
//        tv_title.getPaint().setFakeBoldText(true);
        tv_title.setTextSize(ICL.TABLE_DATA_TEXT_SIZE);
        tv_title.setGravity(Gravity.CENTER);
        tv_title.setBackgroundColor(Color.parseColor(titleColor));
        tv_title.setTextColor(Color.parseColor(headerTextColor));
        tv_title.setId(View.generateViewId());//设置一个随机id，这样可以保证不冲突
        RelativeLayout.LayoutParams lp_tv_title = new RelativeLayout.LayoutParams(titleWidth, titleHeight);
        tablePanelView.addView(tv_title, lp_tv_title);

        // 2. row（LinearLayout --> MyHorizontalScrollView --> PanelListLayout）
        headerLinearLayout = new LinearLayout(context);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        headerLinearLayout.setLayoutParams(lp);

        headerHorizontalScrollView = new BipHorizontalScrollView(context);
        headerHorizontalScrollView.setHorizontalScrollBarEnabled(false);
        headerHorizontalScrollView.setOverScrollMode(View.OVER_SCROLL_NEVER);//去除滑动到边缘时出现的阴影
        headerHorizontalScrollView.addView(headerLinearLayout);//暂时先不给ll_row添加子view，等布局画出来了再添加
        headerHorizontalScrollView.setId(View.generateViewId());
        RelativeLayout.LayoutParams lp_mhsv_row = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, titleHeight);
        lp_mhsv_row.addRule(RelativeLayout.END_OF, tv_title.getId());
        lp_mhsv_row.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        tablePanelView.addView(headerHorizontalScrollView, lp_mhsv_row);

        // 3. column （ListView --> PanelListLayout）
        leftHeaderListView = new ListView(context);
        leftHeaderListView.setBackgroundColor(Color.parseColor(columnColor));
        leftHeaderListView.setId(View.generateViewId());
        leftHeaderListView.setVerticalScrollBarEnabled(false);//去掉滚动条
//        leftHeaderListView.setDivider(context.getResources().getDrawable(R.drawable.column_item_divider));
        RelativeLayout.LayoutParams lp_lv_column = new RelativeLayout.LayoutParams(titleWidth, ViewGroup.LayoutParams.MATCH_PARENT);
        lp_lv_column.addRule(RelativeLayout.BELOW, tv_title.getId());
        tablePanelView.addView(leftHeaderListView, lp_lv_column);

        // 4. content (ListView --> MyHorizontalScrollView --> SwipeRefreshLayout --> PanelListLayout)
        dataHorizontalScrollView = new BipHorizontalScrollView(context);
        dataHorizontalScrollView.addView(centerDataListView);//因为 centerDataListView 在 xml 文件中已经设置了 layout 为 match_parent，所以这里add时不需要再加 LayoutParameter 对象
        dataHorizontalScrollView.setOverScrollMode(View.OVER_SCROLL_NEVER);//去除滑动到边缘时出现的阴影
        RelativeLayout.LayoutParams lp_mhsv_content = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        if (swipeRefreshLayout == null) {
            swipeRefreshLayout = new SwipeRefreshLayout(context);
        }
        swipeRefreshLayout.addView(dataHorizontalScrollView, lp_mhsv_content);
        swipeRefreshLayout.setOnRefreshListener(onRefreshListener);
        Log.d(TAG, "reorganizeViewGroup: " + onRefreshListener.toString());
        RelativeLayout.LayoutParams lp_srl = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        lp_srl.addRule(RelativeLayout.RIGHT_OF, leftHeaderListView.getId());
        lp_srl.addRule(RelativeLayout.BELOW, tv_title.getId());
        tablePanelView.addView(swipeRefreshLayout, lp_srl);
        if (initPosition == 0) {
            swipeRefreshLayout.setEnabled(swipeRefreshEnable);
        }

        // 发一个消息出去。当布局渲染完成之后会执行消息内容，此时
        tablePanelView.post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "post--centerDataListView = " + centerDataListView.toString());
//                dataItemLinearLayout = (LinearLayout) centerDataListView.getChildAt(centerDataListView.getFirstVisiblePosition());//获得content的第一个可见item
                dataItemLinearLayout = (LinearLayout) centerDataListView.getChildAt(0);//获得content的第一个可见item
                initColumnLayout();
                initRowLayout();
                // 当ListView绘制完成后设置初始位置，否则ll_contentItem会报空指针
                centerDataListView.setSelection(initPosition);
                leftHeaderListView.setSelection(initPosition);
            }
        });
    }

    private void initColumnLayout() {
        columnItemHeight = rowDataList.get(0).getHeight();
        leftHeaderListView.setAdapter(getColumnAdapter());
        if (columnDivider != null) {
            leftHeaderListView.setDivider(columnDivider);
        }
    }


    /**
     * 初始化横向表头的布局，必须在所有的布局都载入完之后才能调用
     * <p>
     * must be called in tablePanelView.post();
     */
    private void initRowLayout() {

        if (rowDataList == null) {
            Log.e("PanelList", "custom Row data list is strongly recommended! Call setRowDataList(List<String> rowDataList) in your panel adapter");
        }
//        int rowCount = dataItemLinearLayout.getChildCount();
        int rowCount = rowDataList.size();

        List<HeaderRowInfo> rowDataList1 = getRowDataList(rowCount);

        //分隔线的设置，如果content的item设置了分割线，那row使用相同的分割线，除非单独给row设置了分割线
        headerLinearLayout.setBackgroundColor(Color.parseColor(rowColor));
        if (rowDivider == null) {
            headerLinearLayout.setDividerDrawable(dataItemLinearLayout ==null?null: dataItemLinearLayout.getDividerDrawable());
        } else {
            headerLinearLayout.setDividerDrawable(rowDivider);
        }

        // 获得row一共有多少个item，然后使用循环往里面添加对应个数个TextView（简单粗暴）
        for (int i = 0; i < rowCount; i++) {
            TextView rowItem = new TextView(context);
            rowItem.setText(rowDataList1.get(i).getName());//设置文字
            rowItem.setTextColor(Color.parseColor(headerTextColor));
//            rowItem.getPaint().setFakeBoldText(true);//设置加粗
            rowItem.setWidth(rowDataList1.get(i).getWidth());//设置宽度
            rowItem.setHeight(titleHeight);//设置高度
            rowItem.setGravity(Gravity.CENTER);
            rowItem.setTextSize(ICL.TABLE_DATA_TEXT_SIZE);
            headerLinearLayout.addView(rowItem);
        }
    }

    /**
     * 返回横向表头的内容列表
     * <p>
     * 如果设置了自定义的表头内容，则直接返回引用
     * 如果用户没设置，则根据传进来的count数生成一个默认表头
     */
    private List<HeaderRowInfo> getRowDataList(int count) {
        if (rowDataList == null) {
            List<HeaderRowInfo> defaultRowDataList = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                HeaderRowInfo headerRowInfo = new HeaderRowInfo();
                String s = "Row" + i;
                headerRowInfo.setName(s);
                headerRowInfo.setId("id"+i);
                headerRowInfo.setWidth(120);
                defaultRowDataList.add(headerRowInfo);
            }
            return defaultRowDataList;
        } else {
            return rowDataList;
        }
    }

    /**
     * 返回纵向表头的数据列表
     * 如果开发者没有自定义纵向表头，则生成默认的表头，其内容为1~n，并且将标志位置true
     * 方便{@link #notifyDataSetChanged()}方法作出判断
     * 如果开发者自定义了纵向表头，则直接返回其自定义的内容
     *
     * @return data list of column ListView
     */
    private List<String> getColumnDataList() {
        if (columnDataList == null) {
            defaultColumn = true;
            columnDataList = new ArrayList<>();
            for (int i = 1; i <= getContentAdapter().getCount(); i++) {
                columnDataList.add(String.valueOf(i));
            }
        }
        return columnDataList;
    }

    /**
     * 返回纵向表头的适配器
     *
     * @return adapter of column ListView
     */
    public BaseAdapter getColumnAdapter() {
        if (columnAdapter == null) {
            columnAdapter = new ColumnAdapter(context, android.R.layout.simple_list_item_1, getColumnDataList());
        }
        return columnAdapter;
    }

    /**
     * HorizontalScrollView的滑动监听（水平方向同步控制）
     */
    private class HorizontalScrollListener implements BipHorizontalScrollView.OnHorizontalScrollListener {
        @Override
        public void onHorizontalScrolled(BipHorizontalScrollView view, int l, int t, int oldl, int oldt) {
            if (view == dataHorizontalScrollView) {
                headerHorizontalScrollView.scrollTo(l, t);
            } else {
                dataHorizontalScrollView.scrollTo(l, t);
            }
        }
    }

    /**
     * 两个ListView的滑动监听（垂直方向同步控制）
     */
    private class VerticalScrollListener implements AbsListView.OnScrollListener {

        int scrollState;

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            this.scrollState = scrollState;
            if (scrollState == SCROLL_STATE_IDLE || scrollState == SCROLL_STATE_TOUCH_SCROLL) {
                View subView = view.getChildAt(0);
                if (subView != null && view == centerDataListView) {
                    int top = subView.getTop();
                    int position = view.getFirstVisiblePosition();
                    leftHeaderListView.setSelectionFromTop(position, top);
                } else if (subView != null && view == leftHeaderListView) {
                    int top = subView.getTop();
                    int position = view.getFirstVisiblePosition();
                    centerDataListView.setSelectionFromTop(position, top);
                }
            }

            // 滑动事件冲突的解决：如果ListView的首条item的position != 0，即此时不再顶上，则将下拉刷新禁用
            if (swipeRefreshEnable) {
                if (view.getFirstVisiblePosition() != 0 && swipeRefreshLayout.isEnabled()) {
                    swipeRefreshLayout.setEnabled(false);
                }

                if (view.getFirstVisiblePosition() == 0) {
                    swipeRefreshLayout.setEnabled(true);
                }
            }
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            //判断滑动是否终止，以停止自动对齐，否则该方法会一直被调用，影响性能
            if (scrollState == SCROLL_STATE_IDLE) {
                return;
            }
            View subView = view.getChildAt(0);
            if (subView != null && view == centerDataListView) {
                int top = subView.getTop();
                leftHeaderListView.setSelectionFromTop(firstVisibleItem, top);
            } else if (subView != null && view == leftHeaderListView) {
                int top = subView.getTop();
                centerDataListView.setSelectionFromTop(firstVisibleItem, top);
            }
        }
    }

    /**
     * 默认的columnAdapter
     * <p>
     * 之所以重写是为了根据content的item之高度动态设置column的item之高度
     */
    private class ColumnAdapter extends ArrayAdapter {

        private int resourceId;
        private List<String> columnDataList;

        ColumnAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<String> objects) {
            super(context, resource, objects);
            resourceId = resource;
            columnDataList = objects;
        }

        @Override
        public int getCount() {
            return super.getCount();
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View view;

            if (convertView == null) {
                view = new TextView(context);
                ((TextView) view).setHeight(columnItemHeight);
            } else {
                view = convertView;
            }

            ((TextView) view).setText(columnDataList.get(position));
            ((TextView) view).setTextSize(ICL.TABLE_DATA_TEXT_SIZE);
            view.setPadding(ICL.TABLE_DATA_PADDING_LEFT, ICL.TABLE_DATA_PADDING_TOP, ICL.TABLE_DATA_PADDING_RIGHT, ICL.TABLE_DATA_PADDING_BOTTOM);
            view.setBackgroundColor(Color.WHITE);
            ((TextView) view).setGravity(Gravity.CENTER);

            return view;
        }
    }

    private class DefaultRefreshListener implements SwipeRefreshLayout.OnRefreshListener {
        @Override
        public void onRefresh() {
            Toast.makeText(context, "请调用PanelListAdapter的setOnRefreshListener()并传入你的Listener", Toast.LENGTH_SHORT).show();
            if (swipeRefreshLayout.isRefreshing()) {

                swipeRefreshLayout.setRefreshing(false);
            }
        }
    }
}