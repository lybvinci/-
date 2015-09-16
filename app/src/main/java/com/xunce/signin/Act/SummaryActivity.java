package com.xunce.signin.Act;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.internal.view.menu.MenuBuilder;
import android.support.v7.widget.ActionMenuPresenter;
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

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.FindCallback;
import com.xunce.signin.R;
import com.xunce.signin.Utils.DateUtil;
import com.xunce.signin.Utils.ToastUtils;
import com.xunce.signin.View.RefreshableView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SummaryActivity extends BaseActivity {

    private RefreshableView refreshableView;
    private ListView list_view;
    private ArrayList<String> userName = new ArrayList<>();
    private ArrayList<String> allTime = new ArrayList<>();
    private MyAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_summary);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void initViews() {
        mAdapter = new MyAdapter();
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle("汇总");
        refreshableView = (RefreshableView) findViewById(R.id.refreshable_view);
        list_view = (ListView) findViewById(R.id.list_view);
    }


    @Override
    public void initEvents() {
        refreshableView.setOnRefreshListener(new RefreshableView.PullToRefreshListener() {
            @Override
            public void onRefresh() {
                refreshData();
            }
        }, 0);
        refreshData();
        list_view.setAdapter(mAdapter);
    }

    private void refreshData(){
        userName.clear();
        allTime.clear();
        AVQuery<AVObject> query = new AVQuery<>("SignIn");
        query.whereEqualTo("group", "704");
        query.findInBackground(new FindCallback<AVObject>() {
            @Override
            public void done(List<AVObject> list, AVException e) {
                if (list != null && list.size() > 0) {
                    //初始化变量保存云端获取的更新信息
                    AVObject signIn;
                    for(int i =0;i<list.size();i++) {
                        signIn = list.get(i);
                        long lastWeekTime = 0;

                        //判断是否是首次
                        if (signIn.containsKey("weekTime") ) {
                            //更新赋值
                            lastWeekTime = Long.valueOf((String) signIn.get("weekTime"));
                        }

                        //初始化变量用来保存更新好用来展示的数据
                        int nowWeekTime;
                        //判断是否还在签到过程中
                        nowWeekTime = DateUtil.millCastToMin(Long.valueOf(lastWeekTime));

                        //保存到Map中
                        allTime.add(i, nowWeekTime + "");
                        userName.add(i, (String) signIn.get("user"));

                        //刷新数据
                        mAdapter.notifyDataSetChanged();
                        refreshableView.finishRefreshing();
                        ToastUtils.showShort(SummaryActivity.this, "刷新成功");
                    }
                } else {
                    refreshableView.finishRefreshing();
                    ToastUtils.showShort(SummaryActivity.this, "没有你的记录，第一次玩，瞎刷新什么玩意！");
                }
            }
        });
    }

    class MyAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return userName.size();
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
                LayoutInflater inflater = SummaryActivity.this.getLayoutInflater();
                mView = inflater.inflate(R.layout.listview_summary, null);
            } else {
                mView = view;
            }
            TextView tv_username = (TextView) mView.findViewById(R.id.tv_name);
            TextView tv_all_time = (TextView) mView.findViewById(R.id.tv_week_time);
            if (userName.size() > 0) {
//                Log.e("i:"+i,userName.toString());
//                Log.e("i:"+i,userName.get(i));
                tv_username.setText("姓名：" + userName.get(i));
                tv_all_time.setText("累计时长：" +
                        DateUtil.minCastHour(allTime.get(i)) + "小时" +
                        DateUtil.minCastMin(allTime.get(i)) + "分钟");
            }
            return mView;
        }
    }

}
