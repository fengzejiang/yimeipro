package com.yimei.activity.deviceproduction;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.GridView;
import com.yimei.activity.R;
import com.yimei.activity.base.BaseActivity;
import com.yimei.adapter.CommStationAdapter;

/**
 * 器件通用工站
 * @Auther: fengzejiang1987@163.com
 * @Date: 2018/12/12 18:48
 */
public class CommonStationActivity extends BaseActivity {
    private GridView gridView;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_common_station);
        gridView = findViewById(R.id.comm_grid_view);
        gridView.setAdapter(new CommStationAdapter(this));
    }
}
