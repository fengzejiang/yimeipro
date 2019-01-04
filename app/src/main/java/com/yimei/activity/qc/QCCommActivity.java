package com.yimei.activity.qc;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.ImageButton;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.yimei.activity.R;
import com.yimei.activity.base.BaseActivity;

/**
 * @Auther: fengzejiang1987@163.com
 * @Date : 2018/12/26 17:23
 */
public class QCCommActivity extends BaseActivity {
    @BindView(R.id.img_sj)
    ImageButton btn_sj;
    @BindView(R.id.img_xj)
    ImageButton btn_xj;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comm_qc);
        ButterKnife.bind(this);
    }

    @OnClick({R.id.img_sj,R.id.img_xj})
    void ImgButtonOnClick(ImageButton imgBtn){
        int id = imgBtn.getId();
        Class clzz=null;
        if(id == R.id.img_sj){
            clzz=FirstInspectionActivity.class;
        }
        if(id == R.id.img_xj){
            clzz=null;
        }
        if(clzz!=null)
            jumpNextActivity(clzz);
    }
}
