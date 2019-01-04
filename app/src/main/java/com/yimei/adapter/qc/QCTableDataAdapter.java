package com.yimei.adapter.qc;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.shade.com.alibaba.fastjson.JSONObject;
import com.yimei.activity.R;
import com.yimei.data.CheckProjectInfo;
import com.yimei.tableui.AbstractTableViewAdapter;
import com.yimei.tableui.CheckableLinearLayout;
import com.yimei.tableui.TablePanelView;
import com.yimei.tableui.entity.HeaderRowInfo;
import com.yimei.utils.CommonUtils;

import java.util.ArrayList;
import java.util.List;

/***
 * 检验表格适配器
 */
public class QCTableDataAdapter extends AbstractTableViewAdapter {

    private Context context;

    private ListView lv_content;
    List<JSONObject> dataList = new ArrayList<>();
    private List<HeaderRowInfo> headers;

    /**
     * constructor
     *
     * @param context     上下文
     * @param pl_root     根布局（PanelListLayout）
     * @param lv_content  content 部分的布局（ListView）
     * @param contentList content 部分的数据
     */
    public QCTableDataAdapter(Context context, TablePanelView pl_root, ListView lv_content,
                              List<JSONObject> contentList, List<HeaderRowInfo> headers) {
        super(context, pl_root, lv_content);
        this.context = context;
        this.lv_content = lv_content;
        this.dataList = contentList;
        setRowDataList(headers);
        this.headers = headers;

    }

    public void addRecord(JSONObject jsonObject) {
        if (this.dataList == null)
            dataList = new ArrayList<>();
        this.dataList.add(0, jsonObject);
        notifyDataSetChanged();
    }

    /**
     * 给该方法添加实现，返回Content部分的适配器
     *
     * @return adapter of content
     */
    @Override
    protected BaseAdapter getContentAdapter() {
        return new ContentAdapter(context, headers);
    }

    /**
     * content部分的adapter
     * <p>
     * 这里可以自由发挥，和普通的 ListView 的 Adapter 没区别
     */
    private class ContentAdapter extends ArrayAdapter {

        private List<HeaderRowInfo> headers;

        ContentAdapter(Context context, List<HeaderRowInfo> headers) {
            super(context, 0);
            this.headers = headers;
        }

        @Override
        public int getCount() {
            return dataList == null ? 0 : dataList.size();
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View view;
            ViewHolder viewHolder;
            if (convertView == null) {
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.qc_table_item_content, parent, false);
                viewHolder = new ViewHolder(parent.getContext(), view);
                view.setTag(viewHolder);
            } else {
                view = convertView;
                viewHolder = (ViewHolder) view.getTag();
            }
            viewHolder.setData(position);
            return view;
        }

        private class ViewHolder {
            TextView txbm;
            TextView txmc;
            Button btnSetting;
            Button btnOK;
            Context context;

            ViewHolder(Context context, View itemView) {
                this.context = context;
                txbm = new TextView(itemView.getContext());
                txbm.setTextSize(15);
                txbm.setWidth(headers.get(0).getWidth());
                txbm.setHeight(headers.get(0).getHeight());
                txbm.setPadding(0, 0, 0, 0);
                txbm.setGravity(Gravity.CENTER);
                ((CheckableLinearLayout) itemView).addView(txbm, 0);

                txmc = new TextView(itemView.getContext());
                txmc.setTextSize(15);
                txmc.setWidth(headers.get(1).getWidth());
                txmc.setHeight(headers.get(1).getHeight());
                txmc.setPadding(0, 0, 0, 0);
                txmc.setGravity(Gravity.CENTER);
                ((CheckableLinearLayout) itemView).addView(txmc, 1);

                btnOK = new Button(itemView.getContext());
                btnOK.setTextSize(15);
                btnOK.setTextColor(Color.WHITE);
                btnOK.setWidth(headers.get(2).getWidth());
                btnOK.setHeight(headers.get(2).getHeight());
                btnOK.setPadding(0, 0, 0, 0);
                btnOK.setGravity(Gravity.CENTER);
                btnOK.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String str = btnOK.getText().toString();
                        String xmbm = txbm.getText().toString();
                        boolean bok = false;
                        if (str.equals("OK")) {
                            btnOK.setText("NG");
                            btnOK.setBackgroundColor(Color.parseColor("#e93a3a"));
                            bok = false;
                        } else {
                            btnOK.setText("OK");
                            btnOK.setBackgroundColor(Color.parseColor("#259b25"));
                            bok = true;
                        }
                        for (int i = 0; i < dataList.size(); i++) {
                            CheckProjectInfo cc1 = JSONObject.parseObject(dataList.get(i).toJSONString(), CheckProjectInfo.class);
                            if (cc1.getXmbm().equals(xmbm)) {
                                cc1.setBok(bok ? "0" : "1");
                                dataList.set(i, CommonUtils.getJsonObjFromBean(cc1));
                                break;
                            }
                        }
                    }
                });
                ((CheckableLinearLayout) itemView).addView(btnOK, 2);
                btnSetting = new Button(itemView.getContext());
                btnSetting.setTextSize(15);
                btnSetting.setText("更多");
                btnSetting.setWidth(headers.get(2).getWidth());
                btnSetting.setHeight(headers.get(2).getHeight());
                btnSetting.setPadding(0, 0, 0, 0);
                btnSetting.setGravity(Gravity.CENTER);
                btnSetting.setBackgroundColor(Color.parseColor("#FFFF99"));
                btnSetting.setTextColor(Color.parseColor("#99111111"));
                btnSetting.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String xmbm = txbm.getText().toString();
                        CheckProjectInfo cc = null;
                        //通过当前行的项目编码，获取当前行数据
                        for (int i = 0; i < dataList.size(); i++) {
                            CheckProjectInfo cc1 = JSONObject.parseObject(dataList.get(i).toJSONString(), CheckProjectInfo.class);
                            if (cc1.getXmbm().equals(xmbm)) {
                                cc = cc1;
                                break;
                            }
                        }
                        if (cc != null)
                            showMX(cc);
                    }
                });
                ((CheckableLinearLayout) itemView).addView(btnSetting, 3);
            }

            public void setData(int position) {
                CheckProjectInfo data = JSONObject.parseObject(dataList.get(position).toJSONString(), CheckProjectInfo.class);
                txbm.setText(data.getXmbm());
                txmc.setText(data.getXmmc());
                btnOK.setText(data.getBok());
                if (data.getBok().equals("0")) {
                    btnOK.setBackgroundColor(Color.parseColor("#259b25"));
                    btnOK.setText("OK");
                } else {
                    btnOK.setBackgroundColor(Color.parseColor("#e93a3a"));
                    btnOK.setText("NG");
                }
            }

            void showMX(CheckProjectInfo cc) {
                final View dialog = LayoutInflater.from(context).inflate(R.layout.first_qc_dialog, null);
                ((EditText) dialog.findViewById(R.id.standard)).setText(cc.getStandard());
                EditText v1 = dialog.findViewById(R.id.value1);
                v1.setText(cc.getValue1());
                EditText v2 = dialog.findViewById(R.id.value2);
                v2.setText(cc.getValue2());
                EditText v3 = dialog.findViewById(R.id.value3);
                v3.setText(cc.getValue3());
                EditText v4 = dialog.findViewById(R.id.value4);
                v4.setText(cc.getValue4());
                EditText v5 = dialog.findViewById(R.id.value5);
                v5.setText(cc.getValue5());
                EditText v6 = dialog.findViewById(R.id.value6);
                v6.setText(cc.getValue6());
                EditText rm = dialog.findViewById(R.id.remark);
                rm.setText(cc.getRemark());
                final AlertDialog.Builder normalDialog = new AlertDialog.Builder(
                        context);
                normalDialog.setTitle("首检->" + cc.getXmmc());
                normalDialog.setView(dialog);
                normalDialog.setCancelable(false); // 设置不可点击界面之外的区域让对话框消失
                normalDialog.setPositiveButton("确定",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                cc.setValue1(v1.getText().toString());
                                cc.setValue2(v2.getText().toString());
                                cc.setValue3(v3.getText().toString());
                                cc.setValue4(v4.getText().toString());
                                cc.setValue5(v5.getText().toString());
                                cc.setValue6(v6.getText().toString());
                                cc.setRemark(rm.getText().toString());
                                for(int i=0;i<dataList.size();i++){
                                    CheckProjectInfo c1 = JSONObject.parseObject(dataList.get(i).toJSONString(),CheckProjectInfo.class);
                                    if(c1.getXmbm().equals(cc.getXmbm())){
                                        dataList.set(i,CommonUtils.getJsonObjFromBean(cc));
                                        break;
                                    }
                                }
                            }
                        });
                //设置反面按钮
                normalDialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                // 显示
                normalDialog.show();
            }

        }


    }
}
