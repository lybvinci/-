/**
 * Project Name:XPGSdkV4AppBase
 * File Name:LoginActivity.java
 * Package Name:com.gizwits.framework.activity.account
 * Date:2015-1-27 14:45:03
 * Copyright (c) 2014~2015 Xtreme Programming Group, Inc.
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), 
 * to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, 
 * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.xunce.signin.ActAccount;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.LogInCallback;
import com.avos.avoscloud.LogUtil;
import com.xunce.signin.Act.BaseActivity;
import com.xunce.signin.Act.SignInActivity;
import com.xunce.signin.R;
import com.xunce.signin.Utils.IntentUtils;
import com.xunce.signin.Utils.NetworkUtils;


/**
 * ClassName: Class LoginActivity. <br/>
 * 用户登陆，该类用于账号登陆<br/>
 * 
 * @author Lien
 */
public class LoginActivity extends BaseActivity implements OnClickListener {

	/**
	 * The et name.
	 */
	private EditText etName;

	/**
	 * The et psw.
	 */
	private EditText etPsw;

	/**
	 * The tv forgot.
	 */
	private Button tvForgot;

	/**
	 * The btn login.
	 */
	private Button btnLogin;

	/**
	 * The btn register.
	 */
	private Button btnRegister;

	/**
	 * The dialog.
	 */
	private ProgressDialog dialog;
	private SharedPreferences sharePreferences;

	/**
	 * ClassName: Enum handler_key. <br/>
	 * <br/>
	 * date: 2014-11-26 17:51:10 <br/>
	 * 
	 * @author Lien
	 */
	private enum handler_key {

		/** 登陆成功. */
		LOGIN_SUCCESS,

		/** 登陆失败. */
		LOGIN_FAIL,

		/** 登录超时. */
		LOGIN_TIMEOUT,

	}

	/**
	 * The handler.
	 */
	Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			handler_key key = handler_key.values()[msg.what];
			switch (key) {
			// 登陆成功
			case LOGIN_SUCCESS:
				handler.removeMessages(handler_key.LOGIN_TIMEOUT.ordinal());
				Toast.makeText(LoginActivity.this, "登录成功", Toast.LENGTH_SHORT)
						.show();
				dialog.cancel();
				sharePreferences = getSharedPreferences("default", MODE_PRIVATE);
				sharePreferences.edit().putString("username", etName.getText().toString()).commit();
				IntentUtils.getInstance().startActivity(LoginActivity.this,
						SignInActivity.class);
				finish();
				break;
			// 登陆失败
			case LOGIN_FAIL:
				handler.removeMessages(handler_key.LOGIN_TIMEOUT.ordinal());
				Toast.makeText(LoginActivity.this, "用户名或密码错误" + "",
						Toast.LENGTH_SHORT).show();
				dialog.cancel();
				break;
			// 登录超时
			case LOGIN_TIMEOUT:
				handler.removeMessages(handler_key.LOGIN_TIMEOUT.ordinal());
				Toast.makeText(LoginActivity.this, "登陆超时,请检查网络连接！", Toast.LENGTH_SHORT)
						.show();
				dialog.cancel();
				break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_login);
        super.onCreate(savedInstanceState);
	}



    /**
	 * 初始化交互监听器.
	 */
    @Override
	 public void initEvents() {

		tvForgot.setOnClickListener(this);
		btnLogin.setOnClickListener(this);
		btnRegister.setOnClickListener(this);

	}

	/**
	 * 初始化空间.
	 */
    @Override
	public void initViews() {
		Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
		mToolbar.setTitle("登陆");

		etName = (EditText) findViewById(R.id.etName);
		etPsw = (EditText) findViewById(R.id.etPsw);
		tvForgot = (Button) findViewById(R.id.tvForgot);
		btnLogin = (Button) findViewById(R.id.btnLogin);
		btnRegister = (Button) findViewById(R.id.btnRegister);
		
		dialog = new ProgressDialog(this);
		dialog.setMessage("登录中，请稍候...");

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.tvForgot:
			// 打开忘记密码Activity
			IntentUtils.getInstance().startActivity(this,
					ForgetPswActivity.class);
			break;
		case R.id.btnLogin:
            //登陆方法
            mLogin();
			break;
		case R.id.btnRegister:
				// 打开注册Activity
			IntentUtils.getInstance().startActivity(this,
					RegisterActivity.class);
			break;
		}

	}

    private void mLogin() {
        // 登陆
        if (TextUtils.isEmpty(etName.getText().toString())) {
            Toast.makeText(this, "请输入用户名", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(etPsw.getText().toString())) {
            Toast.makeText(this, "请输入密码", Toast.LENGTH_SHORT).show();
            return;
        }
        dialog.show();
        final String psw = etPsw.getText().toString().trim();
        final String name = etName.getText().toString().trim();
        handler.sendEmptyMessageDelayed(
                handler_key.LOGIN_TIMEOUT.ordinal(), 15000);
        // 调用登陆方法
        AVUser.logInInBackground(name, psw, new LogInCallback<AVUser>() {
			@Override
			public void done(AVUser avUser, AVException e) {
				if (e != null) {
					LogUtil.log.i(e.toString());
					if (AVException.CONNECTION_FAILED != e.getCode()) {
						handler.sendEmptyMessage(handler_key.LOGIN_FAIL.ordinal());
					} else {
						handler.sendEmptyMessage(handler_key.LOGIN_TIMEOUT.ordinal());
					}
				} else {
					if (avUser != null) {
						handler.sendEmptyMessage(handler_key.LOGIN_SUCCESS.ordinal());
					} else {
						handler.sendEmptyMessage(handler_key.LOGIN_FAIL.ordinal());
					}
				}
			}
		});
    }

	@Override
	public void onBackPressed() {
		exit();
	}
    @Override
    protected void onStart() {
        super.onStart();
        if(!NetworkUtils.isNetworkConnected(this)){
            if(builder == null) {
                builder = NetworkUtils.networkDialogNoCancel(this);
            }else{
                builder.show();
            }
        }else{
            builder = null;
        }
    }

}
