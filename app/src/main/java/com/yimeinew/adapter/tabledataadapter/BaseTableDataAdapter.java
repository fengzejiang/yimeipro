package com.yimeinew.adapter.tabledataadapter;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.aliyun.openservices.shade.com.alibaba.rocketmq.shade.com.alibaba.fastjson.JSONObject;
import com.yimeinew.activity.R;
import com.yimeinew.tableui.AbstractTableViewAdapter;
import com.yimeinew.tableui.CheckableLinearLayout;
import com.yimeinew.tableui.TablePanelView;
import com.yimeinew.tableui.entity.HeaderRowInfo;
import com.yimeinew.utils.ICL;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * <pre>
 *     author : zyb
 *     e-mail : hbdxzyb@hotmail.com
 *     time   : 2017/05/23
 *     desc   : 整个页面的Adapter，内部使用了两个子Adapter
 *              开发者可自行定义两个子Adapter
 *     version: 1.0
 * </pre>
 * @author zyb
 */

public class BaseTableDataAdapter extends AbstractTableViewAdapter {

    private Context context;

    private ListView lv_content;
    private List<JSONObject> contentList = new ArrayList<>();
    private List<HeaderRowInfo> headers;
    /**
     * constructor
     *
     * @param context 上下文
     * @param pl_root 根布局（PanelListLayout）
     * @param lv_content content 部分的布局（ListView）
     * @param contentList content 部分的数据
     */
    public BaseTableDataAdapter(Context context, TablePanelView pl_root, ListView lv_content,
                                List<JSONObject> contentList, List<HeaderRowInfo> headers) {
        super(context, pl_root, lv_content);
        this.context = context;
        this.lv_content = lv_content;
        this.contentList = contentList;
        setRowDataList(headers);
        this.headers = headers;

    }

    public void addRecord(JSONObject jsonObject){
        if(this.contentList==null)
            contentList = new ArrayList<>();
        this.contentList.add(0,jsonObject);
        notifyDataSetChanged();
    }

    public void clear(){
        if(this.contentList==null)
            contentList = new ArrayList<>();
        this.contentList.clear();
        notifyDataSetChanged();
    }

    /**
     * 给该方法添加实现，返回Content部分的适配器
     *
     * @return adapter of content
     */
    @Override
    protected BaseAdapter getContentAdapter() {
        return new ContentAdapter(context,contentList,headers);
    }

    /**
     * content部分的adapter
     *
     * 这里可以自由发挥，和普通的 ListView 的 Adapter 没区别
     */
    private class ContentAdapter extends ArrayAdapter {

        private List<JSONObject> contentList;
        private List<HeaderRowInfo> headers;

        ContentAdapter(Context context, List<JSONObject> contentList,List<HeaderRowInfo> headers) {
            super(context,0);
            this.contentList = contentList;
            this.headers = headers;
        }

        @Override
        public int getCount() {
            return contentList==null?0:contentList.size();
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            final JSONObject data = contentList.get(position);
            View view;
            ViewHolder viewHolder;
            if (convertView == null) {
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.table_item_content, parent, false);
                viewHolder = new ViewHolder(view,data,headers);
                view.setTag(viewHolder);
            } else {
                view = convertView;
                viewHolder = (ViewHolder) view.getTag();
            }
            viewHolder.setData(data);

            if (lv_content.isItemChecked(position)){
                view.setBackgroundColor(context.getResources().getColor(R.color.colorSelected));
            } else {
                view.setBackgroundColor(context.getResources().getColor(R.color.colorDeselected));
            }
//            Log.d("ybz", "getView: itemview = "+ view.toString());

            return view;
        }

        private class ViewHolder {
            List<TextView> viewList;
//            CheckableLinearLayout itemView;
            private JSONObject data;
            List<HeaderRowInfo> headerRowInfos;
            ViewHolder(View itemView,JSONObject data,List<HeaderRowInfo> headerRowInfos) {
                this.headerRowInfos = headerRowInfos;
                this.data = data;
//                this.itemView = (CheckableLinearLayout)itemView.findViewById(R.id.cell_line);
                viewList  = new ArrayList<>();
                for(int i=0;i<headerRowInfos.size();i++){
                    HeaderRowInfo headerRowInfo = headerRowInfos.get(i);
                    TextView textView = new TextView(itemView.getContext());
                    textView.setTextSize(15);
                    textView.setWidth(headerRowInfo.getWidth());
                    textView.setHeight(headerRowInfo.getHeight());
                    textView.setPadding(0, 0, 0, 0);
                    textView.setGravity(Gravity.CENTER);
//                    String values = data.containsKey(headerRowInfo.getId())?data.getString(headerRowInfo.getId()):"";
//                    int attr = headerRowInfo.getAttr();
//                    if((attr& ICL.DATA_ATTR_MAP)>0){
//                        Map<String,String> maps = headerRowInfo.getContrastMap();
//                        if(maps!=null){
//                            values = maps.get(values);
//                            values= values==null?"":values;
//                        }
//                    }
//                    textView.setText(values);
//                    if((attr&ICL.DATA_ATTR_COLOR)>0){
//                        Map<String,String> maps = headerRowInfo.getContrastColors();
//                        if(maps!=null){
//                            String colorStr = maps.get(values);
//                            if(colorStr!=null)
//                                textView.setBackgroundColor(Color.parseColor(colorStr));
//                        }
//                    }

                    viewList.add(textView);
                    ((CheckableLinearLayout) itemView).addView(textView,i);
                }
            }
            public void setData(JSONObject data){
                for(int i=0;i<headerRowInfos.size();i++){
                    HeaderRowInfo headerRowInfo = headerRowInfos.get(i);
                    String values = data.containsKey(headerRowInfo.getId())?data.getString(headerRowInfo.getId()):"";
                    String newValue= values;
                    int attr = headerRowInfo.getAttr();
                    if((attr& ICL.DATA_ATTR_MAP)>0){
                        Map<String,String> maps = headerRowInfo.getContrastMap();
                        if(maps!=null){
                            newValue = maps.get(values);
                            newValue= newValue==null?"":newValue;
                        }
                    }
                    viewList.get(i).setText(newValue);
                    if((attr&ICL.DATA_ATTR_COLOR)>0){
                        Map<String,String> maps = headerRowInfo.getContrastColors();
                        if(maps!=null){
                            String colorStr = maps.get(values);
                            if(colorStr!=null)
                                viewList.get(i).setBackgroundColor(Color.parseColor(colorStr));
                        }
                    }
                }
            }
        }


    }
}
