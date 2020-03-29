package com.kodeholic.itbook.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.material.tabs.TabLayout;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import com.kodeholic.itbook.R;
import com.kodeholic.itbook.common.BookManager;
import com.kodeholic.itbook.common.BitmapCacheManager;
import com.kodeholic.itbook.common.MyIntent;
import com.kodeholic.itbook.common.MySettings;
import com.kodeholic.itbook.common.Utils;
import com.kodeholic.itbook.lib.util.Log;
import com.kodeholic.itbook.ui.base.IBase;
import com.kodeholic.itbook.ui.main.SectionsPagerAdapter;

public class MainActivity extends AppCompatActivity implements IBase, View.OnClickListener {
    public static final String TAG = MainActivity.class.getSimpleName();

    private Context mContext;
    private SectionsPagerAdapter mVPAdapter;

    private TabLayout tl_holder;
    private ViewPager vp_holder;
    private ProgressBar pb_loading;
    private TextView    tv_sort;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate()");

        setContentView(R.layout.activity_main);

        mContext   = this;
        mVPAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());

        //view를 초기화한다.
        initView();

        //LB를 등록한다.
        IntentFilter filter = new IntentFilter();
        filter.addAction(MyIntent.Action.MAIN_ACTION);
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mControlRecevier, filter);

        //CacheManager를 초기화한다.
        BitmapCacheManager.getInstance(mContext).startLazyDownloaderTask();

        //BookManager 초기화 작업을 수행한다.
        Utils.runNew(new Runnable() {
            @Override
            public void run() {
                BookManager.getInstance(mContext).clearSearchResult();
                BookManager.getInstance(mContext).loadNewList(null, "onCreate");
                BookManager.getInstance(mContext).loadBookmark("onCreate");
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy()");

        //loading을 종료한다.
        hideLoading();

        //CacheManager를 해제한다.
        BitmapCacheManager.getInstance(mContext).stopLazyDownloaderTask();

        //LB를 해제한다.
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mControlRecevier);
    }

    private void initView() {
        tv_sort = findViewById(R.id.tv_sort);
        pb_loading = findViewById(R.id.pb_loading);

        //event
        tv_sort.setOnClickListener(this);

        //viewpager
        vp_holder = findViewById(R.id.vp_holder);
        vp_holder.setAdapter(mVPAdapter);
        vp_holder.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                mVPAdapter.update(MyIntent.Event.SEARCH_DEL_INPUT, null);

                //우상단 'sort' 문자열 노출 유무
                if (position == SectionsPagerAdapter.SECTIONS_HISTORY) {
                    tv_sort.setVisibility(View.VISIBLE);
                }
                else {
                    tv_sort.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { ; }

            @Override
            public void onPageScrollStateChanged(int state) { ; }
        });

        //tablayout
        tl_holder = findViewById(R.id.tl_holder);
        tl_holder.setupWithViewPager(vp_holder);
    }

    private BroadcastReceiver mControlRecevier = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            MyIntent.show(TAG, "onReceive()", intent);

            //intent 유효성 여부 검사
            if (intent == null) {
                Log.e(TAG, "onReceive(). Intent is null!!");
                return;
            }
            if (!MyIntent.Action.MAIN_ACTION.equals(intent.getAction())) {
                Log.e(TAG, "onReceive(). Invalid Action!! - " + intent.getAction());
                return;
            }

            int event = intent.getIntExtra(MyIntent.Extra.EVENT, 0);
            mVPAdapter.update(event, null);
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_sort:
                showOptionMenuPopup(v, "tv_sort");
                break;
        }
    }

    @Override
    public void showLoading() {
        Log.d(TAG, "showLoading()");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                pb_loading.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void hideLoading() {
        Log.d(TAG, "hideLoading()");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                pb_loading.setVisibility(View.GONE);
            }
        });
    }

    //////////////////////////////////////////////////////////////
    //
    //정렬을 위한 팝업 메뉴
    //
    //////////////////////////////////////////////////////////////
    private void showOptionMenuPopup(View view, String f) {
        Log.d(TAG, "showOptionMenuPopup() - f: " + f);

        Context wrapper = new ContextThemeWrapper(this, R.style.MyPopupMenu);
        PopupMenu pm = new PopupMenu(wrapper, view);
        Menu menu = pm.getMenu();
        menu.add(0, MyIntent.SORT_OPTION.RECENTS , 0, "recents");
        menu.add(0, MyIntent.SORT_OPTION.TITLE   , 0, "title");
        menu.add(0, MyIntent.SORT_OPTION.SUBTITLE, 0, "subtitle");
        menu.add(0, MyIntent.SORT_OPTION.ISBN13  , 0, "isbn13");
        pm.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Log.d(TAG, "onMenuItemClick() - item: " + item.toString());
                int sortOption = MySettings.getInstance(mContext).getSortOption();
                int orderOption= MySettings.getInstance(mContext).getOrderOption();

                //sortOption이 다르면, sortOption 수정
                if (item.getItemId() != sortOption) {
                    MySettings.getInstance(mContext).setSortOption(item.getItemId());
                }
                //sortOption이 동일하다면, orderOption을 toggle
                else {
                    if (orderOption == MyIntent.ORDER_OPTION.ASC) {
                        MySettings.getInstance(mContext).setOrderOption(MyIntent.ORDER_OPTION.DESC);
                    }
                    else {
                        MySettings.getInstance(mContext).setOrderOption(MyIntent.ORDER_OPTION.ASC);
                    }
                }

                mVPAdapter.update(MyIntent.Event.SORT_OPTION_CHANGED, item.getItemId());
                return false;
            }
        });
        pm.show();
    }
}