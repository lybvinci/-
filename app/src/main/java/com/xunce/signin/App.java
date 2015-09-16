package com.xunce.signin;

import android.app.Application;

import com.avos.avoscloud.AVOSCloud;


/**
 * Created by lyb on 2015/3/23.
 */
public class App extends Application {
    private static final String TAG = "App";

    @Override
    public void onCreate() {
        super.onCreate();

        //初始化leacloud
        AVOSCloud.initialize(this,
                "Ovp3aWBzky2v0SyEgK1530mp",
                "puai1mhv0crm0x4yMPBERoI0");



    }

}
