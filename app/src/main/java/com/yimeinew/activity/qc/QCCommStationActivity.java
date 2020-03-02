package com.yimeinew.activity.qc;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.yimeinew.activity.R;
import com.yimeinew.activity.base.BaseActivity;
import com.yimeinew.activity.deviceproduction.commsub.RepairActivity;
import com.yimeinew.adapter.QCCommStationAdapter;
import com.yimeinew.adapter.RepairStationAdapter;

/**
 * @Auther: fengzejiang1987@163.com
 * @Date : 2018/12/26 17:23
 */
public class QCCommStationActivity extends BaseActivity {
    private GridView gridView;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_common_station);
        gridView = findViewById(R.id.comm_grid_view);
        gridView.setAdapter(new QCCommStationAdapter(this));
    }

    @Override
    public boolean onEditTextKeyDown(EditText editText) {
        return false;
    }
}
