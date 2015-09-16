package com.xunce.signin.Act;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import com.avos.avoscloud.AVUser;
import com.xunce.signin.ActAccount.LoginActivity;
import com.xunce.signin.R;
import com.xunce.signin.Utils.NetworkUtils;



public class SplashActivity extends BaseActivity {

    private TextView welcome;
    private AVUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_splash);
        super.onCreate(savedInstanceState);
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle("广告");
    }

    @Override
    public void initViews() {
        welcome = (TextView) findViewById(R.id.welcome);
        currentUser = AVUser.getCurrentUser();
        if (currentUser != null) {
            welcome.setText("欢迎" + currentUser.getUsername() + "回来~");
        }
    }



    @Override
    protected void onStart() {
        super.onStart();
        final Context context = this;
        if(!NetworkUtils.isNetworkConnected(this)){
            NetworkUtils.networkDialogNoCancel(context);
        }else {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (currentUser != null) {
                        Intent intent = new Intent(SplashActivity.this, SignInActivity.class);
                        startActivity(intent);
                        SplashActivity.this.finish();
                    } else {
                        Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                        startActivity(intent);
                        SplashActivity.this.finish();
                    }
                }
            }, 2000);
        }
    }

}
