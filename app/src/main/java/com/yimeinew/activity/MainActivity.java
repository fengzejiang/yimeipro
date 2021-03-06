package com.yimeinew.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.view.menu.MenuView;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.Toast;
import com.yimeinew.activity.base.BaseActivity;
import com.yimeinew.activity.login.LoginActivity;
import com.yimeinew.adapter.MainAdapter;
import com.yimeinew.utils.CommonUtils;
import com.yimeinew.utils.DownloadAPK;

/**
 * @Auther: fengzejiang1987@163.com
 * @Date : 2018/12/8 22:55
 */
public class MainActivity extends BaseActivity {
    GridView gridView;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        gridView = findViewById(R.id.main_grid_view);
        gridView.setAdapter(new MainAdapter(this));
    }

    @Override
    public void jumpNextActivity(Class<?> descClass) {
        jumpNextActivity(this,descClass);
    }

    @Override
    public boolean onEditTextKeyDown(EditText editText) {
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id=item.getItemId();
        switch (id){
            case R.id.action_settings:
                jumpNextActivity(SystemSetActivity.class);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            exit();
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    // 定义一个变量，来标识是否退出
    private static boolean isExit = false;

    Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            isExit = false;
        }
    };

    private void exit() {
        if (!isExit) {
            isExit = true;
            Toast.makeText(getApplicationContext(), "再按一次退出程序",
                    Toast.LENGTH_SHORT).show();
            // 利用handler延迟发送更改状态信息
            mHandler.sendEmptyMessageDelayed(0, 2000);
        } else {
            finish();
            System.exit(0);
        }
    }

}
