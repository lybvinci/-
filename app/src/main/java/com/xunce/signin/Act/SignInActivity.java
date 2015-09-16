package com.xunce.signin.Act;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.FindCallback;
import com.avos.avoscloud.SaveCallback;
import com.xunce.signin.ActAccount.LoginActivity;
import com.xunce.signin.R;
import com.xunce.signin.Utils.DateUtil;
import com.xunce.signin.Utils.Historys;
import com.xunce.signin.Utils.ToastUtils;
import com.xunce.signin.View.RefreshableView;

import java.util.HashMap;
import java.util.List;

public class SignInActivity extends BaseActivity {

    private static final String TAG = "SignInActivity";
    private final String MAC = "1c:fa:68:f3:73:76";
    private RefreshableView refreshableView;
    private HashMap<String, String> timeSave = null;
    private ListView list_view;
    private MyAdapter mAdapter;
    private SharedPreferences sharePreferences;
    private boolean isSignIn = false;
    private Button btn_sign_in;
    private String username;
    //退出使用
    private boolean isExit = false;
    /**
     * The handler. to process exit()
     */
    private Handler exitHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            isExit = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_sign_in);
        super.onCreate(savedInstanceState);

    }

    @Override
    public void initViews() {
        sharePreferences = getSharedPreferences("default", MODE_PRIVATE);
        username = sharePreferences.getString("username", "");

        timeSave = new HashMap<>();
        mAdapter = new MyAdapter();
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle("签到有礼" + username);

        isSignIn = sharePreferences.getBoolean("isSignIn", false);

        refreshableView = (RefreshableView) findViewById(R.id.refreshable_view);
        list_view = (ListView) findViewById(R.id.list_view);
        btn_sign_in = (Button) findViewById(R.id.btn_sign_in);
    }

    @Override
    public void initEvents() {
        if (!isSignIn) {
            btn_sign_in.setText("签到");
        } else {
            btn_sign_in.setText("离签");
        }
        initTimeSave();
        refreshableView.setOnRefreshListener(new RefreshableView.PullToRefreshListener() {
            @Override
            public void onRefresh() {
                refreshData();
            }
        }, 0);
        list_view.setAdapter(mAdapter);
        refreshData();
    }

    //初始化天和今天的日期
    private void initTimeSave() {
        timeSave.put("day", getDay());
        long currentTime = DateUtil.getCurrentTimeInLong();
        String date = DateUtil.getFormatedDateTime(currentTime);
        timeSave.put("date", date);
    }

    //签到按钮
    public void signIn(View view) {
        WifiManager mWifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        if (mWifi.isWifiEnabled()) {
            try {
                WifiInfo wifiInfo = mWifi.getConnectionInfo();
                //String netName = wifiInfo.getSSID(); //获取被连接网络的名称
//            String localMac = wifiInfo.getMacAddress();// 获得本机的MAC地址
//            Log.d("SignInActivity","---netName:"+netName);   //---netName:HUAWEI MediaPad
//            Log.d("SignInActivity","---netMac:"+netMac);     //---netMac:78:f5:fd:ae:b9:97
//            Log.d("SignInActivity","---localMac:"+localMac); //---localMac:BC:76:70:9F:56:BD
                String netMac = wifiInfo.getBSSID(); //获取被连接网络的mac地址
                if (netMac.equals(MAC)) {
                    if (!isSignIn) {
                        //签到
                    initUploadingTime();
                    uploadSignIn();
                } else {
                        //离签
                        leaveAndSave();
                }
            } else {
                    ToastUtils.showShort(this, "你的WIFI不对哦~");
            }
            } catch (Exception e) {
                ToastUtils.showShort(this, "你的WIFI出现了谁也没见过的问题，你真是奇葩");
            }
        } else {
            ToastUtils.showShort(this, "请先连接WIFI");
        }
    }

    private void gotoSum() {
        Intent intent = new Intent(this, SummaryActivity.class);
        startActivity(intent);
    }

    private void loginOut() {
        AVUser currentUser = AVUser.getCurrentUser();
        currentUser.logOut();
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        this.finish();
    }

    //上传签到信息
    private void uploadSignIn() {
        AVQuery<AVObject> query = new AVQuery<>("SignIn");
        query.whereEqualTo("user", username);
        query.findInBackground(new FindCallback<AVObject>() {
            @Override
            public void done(List<AVObject> list, AVException e) {
                AVObject signIn = null;
                if (list != null) {
                    signIn = list.get(0);
                } else {
                    signIn = new AVObject("SignIn");
                }
                if (!(signIn.containsKey("allTime") &&
                        signIn.containsKey("weekTime") &&
                        signIn.containsKey("todayTime") &&
                        signIn.containsKey("week") &&
                        signIn.containsKey("date"))) {
                    signIn.put("allTime", "0");
                    signIn.put("weekTime", "0");
                    signIn.put("todayTime", "0");
                    signIn.put("date", timeSave.get("date"));
                    signIn.put("week", timeSave.get("week"));

                    signIn.put("user", username);
                    signIn.put("group", "704");
                } else {
                    if (!signIn.get("week").equals(timeSave.get("week"))) {
                        signIn.put("weekTime", "0");
                    }
                    if (!signIn.get("date").equals(timeSave.get("date"))) {
                        signIn.put("todayTime", "0");
                    }
                }
                signIn.put("week", timeSave.get("week"));
                signIn.put("currentTime", timeSave.get("currentTime"));
                signIn.put("date", timeSave.get("date"));
                signIn.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(AVException e) {
                        if (e == null) {
                            isSignIn = true;
                            sharePreferences.edit().putBoolean("isSignIn", isSignIn).commit();
                            btn_sign_in.setText("离签");
                            ToastUtils.showShort(SignInActivity.this, "签到成功，下一个进来的人是你的女朋友！");
                        } else {
                            e.printStackTrace();
                            ToastUtils.showShort(SignInActivity.this, "签到失败，请打自己一百下再试!");
                        }
                    }
                });
            }
        });
    }

    //初始化上传时间
    private void initUploadingTime() {
        long currentTime = DateUtil.getCurrentTimeInLong();
        String date = DateUtil.getFormatedDateTime(currentTime);
        String week = DateUtil.getWeekOfYear(date) + "";
        Log.e(TAG, "week:" + week);
        timeSave.put("currentTime", currentTime + "");
        timeSave.put("date", date);
        timeSave.put("week", week);
    }

    private void leaveAndSave() {
        AVQuery<AVObject> query = new AVQuery<>("SignIn");
        query.whereEqualTo("user", username);
        query.findInBackground(new FindCallback<AVObject>() {
            @Override
            public void done(List<AVObject> list, AVException e) {
                if (list != null && list.size() > 0) {

                    //init 变量
                    AVObject signIn = list.get(0);
                    String lastDate = (String) signIn.get("date");


                    //判断是否是同一天
                    if (lastDate.equals(timeSave.get("date"))) {

                        long lastCurrentTime = Long.valueOf((String) signIn.get("currentTime"));
                        long lastAllTime = Long.valueOf((String) signIn.get("allTime"));
                        long lastWeekTime = Long.valueOf((String) signIn.get("weekTime"));
                        long lastTodayTime = Long.valueOf((String) signIn.get("todayTime"));


                        //计算所有的时间
                        long currentTime = DateUtil.getCurrentTimeInLong();
                        long interval = currentTime - lastCurrentTime;
                        //Log.e(TAG,"currentTime:"+currentTime+",interval:"+interval);
                        int todayTime = DateUtil.millCastToMin(lastTodayTime + interval);
                        int nowAllTime = DateUtil.millCastToMin(lastAllTime + interval);
                        //Log.e(TAG,"lastAllTime + interval:"+lastAllTime + interval);
                        int nowWeekTime = DateUtil.millCastToMin(lastWeekTime + interval);
//                        Log.e(TAG,"lastWeekTime + interval:"+lastWeekTime + interval);
//                        Log.e(TAG,"todayTime:"+todayTime+",nowAllTime:"+nowAllTime+",nowWeekTime:"+nowWeekTime);

                        //保存到HashMap中
                        timeSave.put("todayTime", todayTime + "");
                        timeSave.put("allTime", nowAllTime + "");
                        timeSave.put("weekTime", nowWeekTime + "");

                        //更新保存到云端
                        signIn.put("allTime", lastAllTime + interval + "");
                        signIn.put("weekTime", lastWeekTime + interval + "");
                        signIn.put("todayTime", lastTodayTime + interval + "");

                        signIn.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(AVException e) {
                                if (e == null) {
                                    isSignIn = false;
                                    sharePreferences.edit().putBoolean("isSignIn", isSignIn).commit();
                                    btn_sign_in.setText("签到");
                                    ToastUtils.showShort(SignInActivity.this, "离签成功！");
                                    mAdapter.notifyDataSetChanged();
                                } else {
                                    ToastUtils.showShort(SignInActivity.this, "离签失败，一定是你长得太丑了。。。");
                                }
                            }
                        });

                    } else {
                        //不是同一天，则将今天的时间清0
                        signIn.put("todayTime", "0");
                        signIn.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(AVException e) {
                                if (e == null) {
                                    isSignIn = false;
                                    sharePreferences.edit().putBoolean("isSignIn", isSignIn).commit();
                                    btn_sign_in.setText("签到");
                                    ToastUtils.showShort(SignInActivity.this, "好吧，昨天忘记签了是吧，打自己10万下，昨天你都白干了。");
                                } else {
                                    ToastUtils.showShort(SignInActivity.this, "离签都失败了，真的该去撞墙了！");
                                }
                            }
                        });

                    }
                } else {
                    ToastUtils.showShort(SignInActivity.this, "竟然查不到你的记录，这科学么？？自杀算了！");
                }
            }
        });
    }

    //更新数据
    private void refreshData() {
        AVQuery<AVObject> query = new AVQuery<>("SignIn");
        query.whereEqualTo("user", username);
        query.findInBackground(new FindCallback<AVObject>() {
            @Override
            public void done(List<AVObject> list, AVException e) {
                if (list != null && list.size() > 0) {
                    //初始化变量保存云端获取的更新信息
                    AVObject signIn = list.get(0);
                    String lastDate = (String) signIn.get("date");
                    long lastCurrentTime = 0;

                    //判断是否是同一天
                    if (lastDate.equals(timeSave.get("date"))) {
                        lastCurrentTime = Long.valueOf((String) signIn.get("currentTime"));
                    }
                    //更新赋值
                    long lastAllTime = Long.valueOf((String) signIn.get("allTime"));
                    long lastWeekTime = Long.valueOf((String) signIn.get("weekTime"));
                    long lastTodayTime = Long.valueOf((String) signIn.get("todayTime"));

                    //初始化变量用来保存更新好用来展示的数据
                    int todayTime;
                    int nowAllTime;
                    int nowWeekTime;
                    //判断是否还在签到过程中
                    if (isSignIn) {
                        long currentTime = DateUtil.getCurrentTimeInLong();
                        long interval = currentTime - lastCurrentTime;
                        todayTime = DateUtil.millCastToMin(lastTodayTime + interval);
                        nowAllTime = DateUtil.millCastToMin(lastAllTime + interval);
                        nowWeekTime = DateUtil.millCastToMin(lastWeekTime + interval);
                    } else {
                        todayTime = DateUtil.millCastToMin(lastTodayTime);
                        nowAllTime = DateUtil.millCastToMin(Long.valueOf(lastAllTime));
                        nowWeekTime = DateUtil.millCastToMin(Long.valueOf(lastWeekTime));
                    }
                    //保存到Map中
                    if (!signIn.get("date").equals(timeSave.get("date"))) {
                        timeSave.put("todayTime", "0");
                    } else {
                        timeSave.put("todayTime", todayTime + "");
                    }
                    if (!signIn.get("week").equals(timeSave.get("week"))) {
                        timeSave.put("weekTime", todayTime + "");
                    } else {
                        timeSave.put("weekTime", nowWeekTime + "");
                    }
                    timeSave.put("allTime", nowAllTime + "");

                    timeSave.put("day", getDay());
                    //刷新数据
                    mAdapter.notifyDataSetChanged();
                    refreshableView.finishRefreshing();
                    ToastUtils.showShort(SignInActivity.this, "刷新成功");

                } else {
                    refreshableView.finishRefreshing();
                    ToastUtils.showShort(SignInActivity.this, "没有你的记录，第一次玩，瞎刷新什么玩意！");
                }
            }
        });
    }

    //计算已经多少天了
    private String getDay() {
        AVUser currentUser = AVUser.getCurrentUser();
        long beginTime = currentUser.getCreatedAt().getTime();
        long endTime = DateUtil.getCurrentTimeInLong();
        int dayCount = (int) ((endTime - beginTime) / (1000 * 3600 * 24)) + 1;//从间隔毫秒变成间隔天数
        return dayCount + "";
    }

    @Override
    protected void onPause() {
        super.onPause();
        sharePreferences.edit().putBoolean("isSignIn", isSignIn).commit();
    }

    @Override
    public void onBackPressed() {
        exit();
    }

    /**
     * 重复按下返回键退出app方法
     */
    public void exit() {
        if (!isExit) {
            isExit = true;
            Toast.makeText(getApplicationContext(),
                    "退出程序", Toast.LENGTH_SHORT).show();
            exitHandler.sendEmptyMessageDelayed(0, 2000);
        } else {
            sharePreferences.edit().putBoolean("isSignIn", isSignIn).commit();
            //返回桌面
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            this.startActivity(intent);
            Historys.exit();
        }
    }

    //菜单
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /*
         *
         * add()方法的四个参数，依次是：
         *
         * 1、组别，如果不分组的话就写Menu.NONE,
         *
         * 2、Id，这个很重要，Android根据这个Id来确定不同的菜单
         *
         * 3、顺序，那个菜单现在在前面由这个参数的大小决定
         *
         * 4、文本，菜单的显示文本
         */
        // setIcon()方法为菜单设置图标，这里使用的是系统自带的图标，同学们留意一下,以

        // android.R开头的资源是系统提供的，我们自己提供的资源是以R开头的
        menu.add(Menu.NONE, Menu.FIRST + 1, 1, "汇总").setIcon(
                android.R.drawable.ic_menu_add);
        menu.add(Menu.NONE, Menu.FIRST + 2, 2, "注销").setIcon(
                android.R.drawable.ic_menu_delete);
        menu.add(Menu.NONE, Menu.FIRST + 3, 3, "帮助").setIcon(
                android.R.drawable.ic_menu_help);
//        menu.add(Menu.NONE, Menu.FIRST + 5, 4, "详细").setIcon(
//
//                android.R.drawable.ic_menu_info_details);
//        menu.add(Menu.NONE, Menu.FIRST + 1, 5, "删除").setIcon(
//
//                android.R.drawable.ic_menu_delete);
//        menu.add(Menu.NONE, Menu.FIRST + 3, 6, "帮助").setIcon(
//
//                android.R.drawable.ic_menu_help);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case Menu.FIRST + 1:
                //汇总
                gotoSum();
                break;
            case Menu.FIRST + 2:
                //注销
                loginOut();
                break;
            case Menu.FIRST + 3:
                //帮助
                ToastUtils.showShort(this, "以后再帮助！先自学");
                break;
//            case Menu.FIRST + 4:
//                Toast.makeText(this, "添加菜单被点击了", Toast.LENGTH_LONG).show();
//                break;
//            case Menu.FIRST + 5:
//                Toast.makeText(this, "详细菜单被点击了", Toast.LENGTH_LONG).show();
//                break;
//            case Menu.FIRST + 6:
//                Toast.makeText(this, "发送菜单被点击了", Toast.LENGTH_LONG).show();
//                break;
        }
        return false;

    }

    @Override
    public void onOptionsMenuClosed(Menu menu) {
        // Toast.makeText(this, "选项菜单关闭了", Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
//        Toast.makeText(this,
//                "选项菜单显示之前onPrepareOptionsMenu方法会被调用，你可以用此方法来根据打当时的情况调整菜单",
//                Toast.LENGTH_LONG).show();
        // 如果返回false，此方法就把用户点击menu的动作给消费了，onCreateOptionsMenu方法将不会被调用
        return true;

    }

    //设备列表listview 适配器
    class MyAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return 1;
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(final int i, View view, ViewGroup viewGroup) {
            View mView;
            if (view == null) {
                LayoutInflater inflater = SignInActivity.this.getLayoutInflater();
                mView = inflater.inflate(R.layout.listview_bind_list, null);
            } else {
                mView = view;
            }

            TextView tv_day = (TextView) mView.findViewById(R.id.tv_day);
            TextView tv_all_time = (TextView) mView.findViewById(R.id.tv_all_time);
            TextView tv_week_time = (TextView) mView.findViewById(R.id.tv_week_time);
            TextView tv_this_time = (TextView) mView.findViewById(R.id.tv_this_time);

            if (timeSave != null && timeSave.size() > 3) {
                tv_day.setText("累计天数：" + timeSave.get("day") + "天");
                tv_all_time.setText("累计时长：" +
                        DateUtil.minCastHour(timeSave.get("allTime")) + "小时" +
                        DateUtil.minCastMin(timeSave.get("allTime")) + "分钟");
                tv_week_time.setText("一周累计时长：" +
                        DateUtil.minCastHour(timeSave.get("weekTime")) + "小时" +
                        DateUtil.minCastMin(timeSave.get("weekTime")) + "分钟");
                tv_this_time.setText("今日累计时长：" +
                        DateUtil.minCastHour(timeSave.get("todayTime")) + "小时" +
                        DateUtil.minCastMin(timeSave.get("todayTime")) + "分钟");
            }

            return mView;
        }
    }
}
