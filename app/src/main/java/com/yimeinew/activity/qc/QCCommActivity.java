package com.yimeinew.activity.qc;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.EditText;
import android.widget.ImageButton;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.yimeinew.activity.R;
import com.yimeinew.activity.base.BaseActivity;
import com.yimeinew.activity.deviceproduction.commsub.RepairActivity;

/**
 * @Auther: fengzejiang1987@163.com
 * @Date : 2018/12/26 17:23
 */
public class QCCommActivity extends BaseActivity {
    @BindView(R.id.img_sj)
    ImageButton btn_sj;
    @BindView(R.id.img_xj)
    ImageButton btn_xj;
    @BindView(R.id.img_oqc)
    ImageButton btn_oqc;
    @BindView(R.id.img_wxok)
    ImageButton btn_wxok;
    @BindView(R.id.img_wxng)
    ImageButton btn_wxng;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comm_qc);
        ButterKnife.bind(this);
    }
	@Override
    public boolean onEditTextKeyDown(EditText editText) {
        return false;
    }
    @OnClick({R.id.img_sj,R.id.img_xj,R.id.img_oqc,R.id.img_wxok,R.id.img_wxng})
    void ImgButtonOnClick(ImageButton imgBtn){
        int id = imgBtn.getId();
        Class clzz=null;
        if(id == R.id.img_sj){
            clzz=FirstInspectionActivity.class;
        }
        if(id == R.id.img_xj){
            clzz=null;
        }
        if(id == R.id.img_oqc){
            clzz=OutCheckActivity.class;
        }
        if(id==R.id.img_wxok){
            clzz= RepairActivity.class;
        }
        if(id==R.id.img_wxng){
            clzz= RepairActivity.class;
        }
        if(clzz!=null)
            jumpNextActivity(clzz);
    }
}
