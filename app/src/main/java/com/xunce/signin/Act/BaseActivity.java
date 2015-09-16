
package com.xunce.signin.Act;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.readystatesoftware.systembartint.SystemBarTintManager;
import com.xunce.signin.R;
import com.xunce.signin.Utils.Historys;
import com.xunce.signin.Utils.ToastUtils;


/**
 * 所有activity的基类。
 * .
 *
 * @author lyb
 */
public class BaseActivity extends Activity {

    private boolean isExit = false;
    public AlertDialog.Builder builder;


    /**
     * The handler.
     */
    private Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            isExit = false;
        }

        ;
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 把activity推入历史栈，退出app后清除历史栈，避免造成内存溢出
        SystemBarTintManager tintManager=new SystemBarTintManager(this);
        tintManager.setStatusBarTintResource(R.color.blue);
        tintManager.setStatusBarTintEnabled(true);
        Historys.put(this);
        initViews();
        initEvents();
    }

    public void initViews() {
    }

    public void initEvents() {
    }

    /**
     * 重复按下返回键退出app方法
     */
    public void exit() {
        if (!isExit) {
            isExit = true;
            ToastUtils.showShort(getApplicationContext(), "再按一次退出程序");
            handler.sendEmptyMessageDelayed(0, 2000);
        } else {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            this.startActivity(intent);
            Historys.exit();
        }
    }
}
