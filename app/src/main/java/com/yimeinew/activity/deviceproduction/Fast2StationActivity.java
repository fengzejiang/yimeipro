package com.yimeinew.activity.deviceproduction;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.EditText;
import android.widget.GridView;
import com.yimeinew.activity.R;
import com.yimeinew.activity.base.BaseActivity;
import com.yimeinew.adapter.Fast2StationAdapter;
import com.yimeinew.adapter.FastStationAdapter;

/**
 * 器件通用工站
 * @Auther: fengzejiang1987@163.com
 * @Date: 2018/12/12 18:48
 */
public class Fast2StationActivity extends BaseActivity {
    private GridView gridView;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_common_station);
        gridView = findViewById(R.id.comm_grid_view);
        gridView.setAdapter(new Fast2StationAdapter(this));
    }

    @Override
    public boolean onEditTextKeyDown(EditText editText) {
        return false;
    }
}
