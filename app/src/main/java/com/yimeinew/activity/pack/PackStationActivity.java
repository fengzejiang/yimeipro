package com.yimeinew.activity.pack;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.EditText;
import android.widget.GridView;
import com.yimeinew.activity.R;
import com.yimeinew.activity.base.BaseActivity;
import com.yimeinew.adapter.FastStationAdapter;
import com.yimeinew.adapter.PackStationAdapter;

/**
 * 包装管理，菜单配置在  PackStationAdapter
 * 这个是动态配置菜单
 * @Auther: fengzejiang1987@163.com
 * @Date: 2018/12/12 18:48
 */
public class PackStationActivity extends BaseActivity {
    private GridView gridView;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_common_station);
        gridView = findViewById(R.id.comm_grid_view);
        gridView.setAdapter(new PackStationAdapter(this));
    }

    @Override
    public boolean onEditTextKeyDown(EditText editText) {
        return false;
    }
}
