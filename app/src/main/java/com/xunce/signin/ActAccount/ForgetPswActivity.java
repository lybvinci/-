/**
 * Project Name:XPGSdkV4AppBase
 * File Name:ForgetPswActivity.java
 * Package Name:com.gizwits.framework.activity.account
 * Date:2015-1-27 14:44:57
 * Copyright (c) 2014~2015 Xtreme Programming Group, Inc.
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * <p/>
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.xunce.signin.ActAccount;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.LogUtil;
import com.avos.avoscloud.RequestMobileCodeCallback;
import com.avos.avoscloud.UpdatePasswordCallback;
import com.xunce.signin.Act.BaseActivity;
import com.xunce.signin.R;
import com.xunce.signin.Utils.NetworkUtils;
import com.xunce.signin.Utils.ToastUtils;

import java.util.Timer;
import java.util.TimerTask;

/**
 * ClassName: Class ForgetPswActivity. <br/>
 * 忘记密码，该类主要包含了两个修改密码的方法，手机号注册的用户通过获取验证码修改密码.<br/>
 * date: 2014-12-09 17:27:10 <br/>
 *
 * @author StephenC
 */
public class ForgetPswActivity extends BaseActivity implements OnClickListener {
    /**
     * The tv phone switch.
     */
    // private TextView tvPhoneSwitch;

    /**
     * The et name.
     */
    private EditText etName;

    /**
     * The et input code.
     */
    private EditText etInputCode;

    /**
     * The et input psw.
     */
    private EditText etInputPsw;

    /**
     * The btn get code.
     */
    private Button btnGetCode;

    /**
     * The btn re get code.
     */
    private Button btnReGetCode;

    /**
     * The btn sure.
     */
    private Button btnSure;

    /**
     * The ll input code.
     */
    private LinearLayout llInputCode;

    /**
     * The ll input psw.
     */
    private LinearLayout llInputPsw;

    /**
     * The iv back.
     */
    //private ImageView ivBack;

    /**
     * The tb psw flag.
     */
    private ToggleButton tbPswFlag;

    /**
     * The secondleft.
     */
    int secondleft = 60;

    /**
     * The timer.
     */
    Timer timer;

    /**
     * The dialog.
     */
    ProgressDialog dialog;

    /**
     * ClassName: Enum handler_key. <br/>
     * <br/>
     * date: 2014-11-26 17:51:10 <br/>
     *
     * @author Lien
     */
    private enum handler_key {

        /**
         * 倒计时通知
         */
        TICK_TIME,

        /**
         * 修改成功
         */
        CHANGE_SUCCESS,

        /**
         * Toast弹出通知
         */
        TOAST,

    }

    /**
     * ClassName: Enum ui_statu. <br/>
     * UI状态枚举类<br/>
     * date: 2014-12-3 10:52:52 <br/>
     *
     * @author Lien
     */
    private enum ui_statu {

        /**
         * 默认状态
         */
        DEFAULT,

        /**
         * 手机注册的用户
         */
        PHONE,

        /**
         * email注册的用户
         */
        EMAIL,
    }

    /**
     * The handler.
     */
    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            handler_key key = handler_key.values()[msg.what];
            switch (key) {

                case TICK_TIME:
                    secondleft--;
                    if (secondleft <= 0) {
                        timer.cancel();
                        btnReGetCode.setEnabled(true);
                        btnReGetCode.setText("重新获取验证码");
                        btnReGetCode
                                .setBackgroundResource(R.drawable.button_blue_short);
                    } else {
                        btnReGetCode.setText(secondleft + "秒后\n重新获取");

                    }
                    break;

                case CHANGE_SUCCESS:
                    finish();
                    break;

                case TOAST:
                    ToastUtils.showShort(ForgetPswActivity.this, (String) msg.obj);
                    dialog.cancel();
                    break;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_forget_reset);
        super.onCreate(savedInstanceState);

    }

    /**
     * Inits the views.
     */
    @Override
    public void initViews() {
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle("忘记密码");

        etName = (EditText) findViewById(R.id.etName);
        etInputCode = (EditText) findViewById(R.id.etInputCode);
        etInputPsw = (EditText) findViewById(R.id.etInputPsw);
        btnGetCode = (Button) findViewById(R.id.btnGetCode);
        btnReGetCode = (Button) findViewById(R.id.btnReGetCode);
        btnSure = (Button) findViewById(R.id.btnSure);
        llInputCode = (LinearLayout) findViewById(R.id.llInputCode);
        llInputPsw = (LinearLayout) findViewById(R.id.llInputPsw);
        //ivBack = (ImageView) findViewById(R.id.ivBack);
        tbPswFlag = (ToggleButton) findViewById(R.id.tbPswFlag);
        toogleUI(ui_statu.DEFAULT);
        dialog = new ProgressDialog(this);
        dialog.setMessage("处理中，请稍候...");
    }

    /**
     * Inits the events.
     */
    @Override
    public void initEvents() {
        btnGetCode.setOnClickListener(this);
        btnReGetCode.setOnClickListener(this);
        btnSure.setOnClickListener(this);
//		ivBack.setOnClickListener(this);
        tbPswFlag
                .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                    @Override
                    public void onCheckedChanged(CompoundButton buttonView,
                                                 boolean isChecked) {
                        if (isChecked) {
                            etInputPsw
                                    .setInputType(InputType.TYPE_CLASS_TEXT
                                            | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                        } else {
                            etInputPsw.setInputType(InputType.TYPE_CLASS_TEXT
                                    | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        }
                    }

                });
    }

    /*
     * (non-Javadoc)
     *
     * @see android.view.View.OnClickListener#onClick(android.view.View)
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnGetCode:
                getVerifyCode();
                break;
            case R.id.btnReGetCode:
                getVerifyCode();
                break;
            case R.id.btnSure:
                doChangePsw();
                break;
//		case R.id.tvPhoneSwitch:
//			ToastUtils.showShort(this, "该功能暂未实现，敬请期待。^_^");
//			break;
//		case R.id.ivBack:
//			onBackPressed();
//			break;
        }
    }

    private void getVerifyCode() {
        String name = etName.getText().toString().trim();
        if (!TextUtils.isEmpty(name) && name.length() == 11) {
            toogleUI(ui_statu.PHONE);
            sendVerifyCode(name);
        } else {
            ToastUtils.showShort(this, "请输入正确的手机号码!");
        }
    }

    /**
     * 执行手机号重置密码操作
     */
    private void doChangePsw() {

        String phone = etName.getText().toString().trim();
        String code = etInputCode.getText().toString().trim();
        String password = etInputPsw.getText().toString();
        if (phone.length() != 11) {
            Toast.makeText(this, "电话号码格式不正确", Toast.LENGTH_SHORT).show();
            return;
        }
        if (code.length() == 0) {
            Toast.makeText(this, "请输入验证码", Toast.LENGTH_SHORT).show();
            return;
        }
        if (password.contains(" ")) {
            Toast.makeText(this, "密码不能有空格", Toast.LENGTH_SHORT).show();
            return;
        }
        if (password.length() < 6 || password.length() > 16) {
            Toast.makeText(this, "密码长度应为6~16", Toast.LENGTH_SHORT).show();
            return;
        }
        AVUser.resetPasswordBySmsCodeInBackground(code, password, new UpdatePasswordCallback() {
            @Override
            public void done(AVException e) {
                if (e == null) {
                    Message msg = new Message();
                    msg.what = handler_key.TOAST.ordinal();
                    msg.obj = "修改成功";
                    handler.sendMessage(msg);
                    handler.sendEmptyMessageDelayed(
                            handler_key.CHANGE_SUCCESS.ordinal(), 2000);
                } else {
                    LogUtil.log.i(e.toString());
                    ToastUtils.showShort(getApplicationContext(), "验证码错误");
                    Message msg = new Message();
                    msg.what = handler_key.TOAST.ordinal();
                    msg.obj = "修改失败,验证码错误";
                    handler.sendMessage(msg);
                }
            }
        });
        dialog.show();
    }

    /**
     * 发送验证码
     *
     * @param phone
     *            the phone
     */
    private void sendVerifyCode(final String phone) {
        dialog.show();
        btnReGetCode.setEnabled(false);
        btnReGetCode.setBackgroundResource(R.drawable.button_gray_short);
        secondleft = 60;
        timer = new Timer();
        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                //倒计时通知
                handler.sendEmptyMessage(handler_key.TICK_TIME.ordinal());
            }
        }, 1000, 1000);
        //发送请求验证码指令
        AVUser.requestPasswordResetBySmsCodeInBackground(phone, new RequestMobileCodeCallback() {
            @Override
            public void done(AVException e) {
                if (e == null) {
                    Message msg = new Message();
                    msg.what = handler_key.TOAST.ordinal();
                    msg.obj = "发送成功";
                    handler.sendMessage(msg);
                } else {
                    LogUtil.log.i(e.toString());
                    Message msg = new Message();
                    msg.what = handler_key.TOAST.ordinal();
                    msg.obj = "发送失败";
                    handler.sendMessage(msg);
                }
            }
        });
    }

    /**
     * Toogle ui.
     *
     * @param statu
     *            the statu
     */
    private void toogleUI(ui_statu statu) {
        if (statu == ui_statu.DEFAULT) {
            llInputCode.setVisibility(View.GONE);
            llInputPsw.setVisibility(View.GONE);
            btnSure.setVisibility(View.GONE);
            btnGetCode.setVisibility(View.VISIBLE);
        } else if (statu == ui_statu.PHONE) {
            llInputCode.setVisibility(View.VISIBLE);
            llInputPsw.setVisibility(View.VISIBLE);
            btnSure.setVisibility(View.VISIBLE);
            btnGetCode.setVisibility(View.GONE);
        } else {

        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!NetworkUtils.isNetworkConnected(this)) {
            if (builder == null) {
                builder = NetworkUtils.networkDialogNoCancel(this);
            } else {
                builder.show();
            }
        } else {
            builder = null;
        }
    }

}
