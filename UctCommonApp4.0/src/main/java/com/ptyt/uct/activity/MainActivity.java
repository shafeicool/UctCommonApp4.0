package com.ptyt.uct.activity;

import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.uct.service.UctClientApi;
import com.android.uct.utils.PrintLog;
import com.ptyt.uct.R;
import com.ptyt.uct.adapter.MainFragmentPagerAdapter;
import com.ptyt.uct.callback.LoginCallBack;
import com.ptyt.uct.common.HeadsetPlugBroadcastReceiver;
import com.ptyt.uct.common.NetworkChangeReceiver;
import com.ptyt.uct.common.SettingsConstant;
import com.ptyt.uct.common.UctApplication;
import com.ptyt.uct.entity.EventBean;
import com.ptyt.uct.fragment.CallFragment;
import com.ptyt.uct.fragment.ConversationFragment;
import com.ptyt.uct.fragment.MapFragment;
import com.ptyt.uct.utils.AppUtils;
import com.ptyt.uct.utils.ConstantUtils;
import com.ptyt.uct.utils.DensityUtils;
import com.ptyt.uct.utils.ScreenManager;
import com.ptyt.uct.utils.StrUtils;
import com.ptyt.uct.viewinterface.IMainView;
import com.ptyt.uct.widget.GroupCallWindow;
import com.ptyt.uct.widget.MViewPager;
import com.ptyt.uct.widget.PTYTCallWindowView;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;

/**
 * @Description: 主界面显示
 * @Data: 2017/4/24
 * @Author: ShaFei
 * @Version: V1.0
 */
public class MainActivity extends BaseActivity implements View.OnClickListener, IMainView {

    private MViewPager viewPager;
    private TabLayout tabLayout;
    private List<Fragment> fragments;
    private String[] pagerTitles = {"信息", "对讲", "地图"};
    private CardView cardView;
    //当组呼来时，除了在CallFragment页面不展示组呼悬浮窗，其他界面都需展示
    public static int currentFragmentPosition = 1;
    private TextView messageUnreadView;
    private TextView recordUnreadView;
    private CallFragment callFragment;
    private View parentView;
    private TextView tv_offline;
    private ConversationFragment conversationFragment;
    // 通话记录未读数
    private int recordUnreadCount = 0;
    private HeadsetPlugBroadcastReceiver plugBroadcastReceiver;

    public MapFragment getMapFragment() {
        return mapFragment;
    }

    private MapFragment mapFragment;

    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB_MR2)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        PrintLog.i("onCreate()");
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        //下面不要写东西 可以放在initView()
    }

    /* ptyt begin, 统一eventbus发送的都是eventbean对象_4053_shafei_20170906 */
    @Subscribe(threadMode = ThreadMode.MainThread)
    public void onEvent(EventBean eventBean) {
        String action = eventBean.getAction();
        switch (action) {
            case ConstantUtils.ACTION_TAB_TO_GCALL:
                if (mapFragment.getPpw_mapUserInfo() != null && mapFragment.getPpw_mapUserInfo().isShowing()) {
                    mapFragment.getPpw_mapUserInfo().dismiss();
                }
                if(mapFragment.getMapDialogFragment() != null && mapFragment.getMapDialogFragment().isVisible()){
                    mapFragment.getMapDialogFragment().dismiss();
                }
                viewPager.setCurrentItem(1);
                break;
            case ConstantUtils.ACTION_MESSAGE_TO_MAP:
                viewPager.setCurrentItem(2);
                break;
            case ConstantUtils.ACTION_CALL_UNREAD_NOTIFY:
                recordUnreadCount++;
                UctClientApi.saveUserData(SettingsConstant.SETTINGS_CALL_RECORD_UNREAD, recordUnreadCount);
                setRecordUnreadCount(recordUnreadCount);
        }
    }

    public MViewPager getViewPager() {
        return viewPager;
    }

    /* ptyt end */

    @Override
    protected void initEvent() {
        //目的:当为对讲fragment时,通知fragment，隐藏tabLayout当组呼窗口全屏时.
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentFragmentPosition = tab.getPosition();
                //解决单击Tab标签无法翻页的问题
                viewPager.setCurrentItem(currentFragmentPosition);
                EventBus.getDefault().post(new EventBean(ConstantUtils.ACTION_FRAGMENT_CHANGED));
                if (currentFragmentPosition == 1) {
                    if (PTYTCallWindowView.isExtentMode == false) {
                        UctApplication.getInstance().getGroupCallWindow().initWindowShow();
                    } else {
                        UctApplication.getInstance().getGroupCallWindow().hidePopupWindow();
                    }
                    //同时通知CallFragment,用于在全屏时隐藏导航栏切换到你了，
                    if (onTabChangeListener != null) {
                        onTabChangeListener.onChanged();
                    }
                } else {
                    cardView.setVisibility(View.VISIBLE);
                    //当在组呼时且不在组呼主页 -> 显示悬浮窗
                    if (UctApplication.getInstance().isInGroupCall) {
                        UctApplication.getInstance().getGroupCallWindow().show(MainActivity.this);
                    } else {
                        UctApplication.getInstance().getGroupCallWindow().hidePopupWindow();
                    }
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    @Override
    protected void initData() {
        initFragments();
        MainFragmentPagerAdapter fragmentPagerAdapter = new MainFragmentPagerAdapter(getSupportFragmentManager(), fragments, pagerTitles);
        viewPager.setAdapter(fragmentPagerAdapter);
        viewPager.setOffscreenPageLimit(2);
        tabLayout.setupWithViewPager(viewPager);
        viewPager.setCurrentItem(1);
    }

    /**
     * 初始化fragment
     */
    private void initFragments() {
        PrintLog.i("initFragments");
        fragments = new ArrayList<>();
        //信息
        conversationFragment = new ConversationFragment();
        fragments.add(conversationFragment);
        //对讲
        callFragment = new CallFragment();
        fragments.add(callFragment);
        //地图
        mapFragment = new MapFragment();
        fragments.add(mapFragment);
    }

    private NetworkChangeReceiver networkChangeReceiver;

    private void registerBroadReceiver() {
        //获取广播对象
        networkChangeReceiver = new NetworkChangeReceiver();
        //创建意图过滤器
        IntentFilter filter = new IntentFilter();
        //添加动作，监听网络
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkChangeReceiver, filter);

        plugBroadcastReceiver = new HeadsetPlugBroadcastReceiver();
        IntentFilter headsetFilter = new IntentFilter();
        headsetFilter.addAction(Intent.ACTION_HEADSET_PLUG);
        registerReceiver(plugBroadcastReceiver, headsetFilter);
    }

    @Override
    public Fragment getFragment(int position) {
        return fragments.get(position);
    }

    public int getOfflineView() {
        return tv_offline.getVisibility();
    }

    @Override
    public void initView() {
        currentFragmentPosition = 1;
        setContentView(R.layout.activity_main);
        UctApplication.getInstance().isInMainActivity = true;
        EventBus.getDefault().register(this);
        viewPager = ((MViewPager) findViewById(R.id.viewPager));
        tabLayout = ((TabLayout) findViewById(R.id.tabLayout));
        cardView = ((CardView) findViewById(R.id.cardView));
        messageUnreadView = (TextView) findViewById(R.id.tv_message_unread);
        recordUnreadView = (TextView) findViewById(R.id.tv_record_unread);
        parentView = findViewById(R.id.activity_main);
        tv_offline = ((TextView) findViewById(R.id.offline_tv));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {//4.4 全透明状态栏

        } else {
            RelativeLayout.MarginLayoutParams p = (RelativeLayout.MarginLayoutParams) cardView.getLayoutParams();
            p.setMargins(mContext.getResources().getDimensionPixelOffset(R.dimen.x100), mContext.getResources().getDimensionPixelOffset(R.dimen.y30), mContext.getResources().getDimensionPixelOffset(R.dimen.x100), 0);
            cardView.requestLayout();
        }
        initTopNavWindow();
        /*ptyt begin 修改设置及临时组等入口位置_kechuanqi_20171212*/
        findViewById(R.id.iv_setting).setOnClickListener(this);
        findViewById(R.id.iv_navMore).setOnClickListener(this);
        /*ptyt end*/
        ScreenManager.getInstance().prepare(((MainActivity) mContext));
        registerBroadReceiver();
    }

    /**
     * 初始化顶部nav的窗体
     */
    private BitmapDrawable popBitmapDrawable2;
    private PopupWindow ppw_topNav;
    private TextView recordUnreadCountTv;

    private void initTopNavWindow() {
        View view = View.inflate(this, R.layout.dialog_top_call_nav, null);
        view.findViewById(R.id.ll_address_book).setOnClickListener(this);
        view.findViewById(R.id.ll_call_history).setOnClickListener(this);
        view.findViewById(R.id.ll_create_call).setOnClickListener(this);
        recordUnreadCountTv = (TextView) view.findViewById(R.id.record_unread_count_tv);
        //popView即popupWindow的布局，ture设置focusAble.
        ppw_topNav = new PopupWindow(view, RecyclerView.LayoutParams.WRAP_CONTENT, RecyclerView.LayoutParams.WRAP_CONTENT, true);
        //必须设置BackgroundDrawable后setOutsideTouchable(true)才会有效。这里在XML中定义背景，所以这里设置为null;
        popBitmapDrawable2 = new BitmapDrawable();
        ppw_topNav.setBackgroundDrawable(popBitmapDrawable2);
        //点击外部关闭。
        ppw_topNav.setOutsideTouchable(true);
        //设置一个动画。
        ppw_topNav.setAnimationStyle(android.R.style.Animation_Dialog);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_setting://设置
                startActivity(new Intent(this, SettingsRootActivity.class));
                break;
            case R.id.iv_navMore://顶部导航
                int ppwLocationX = getResources().getDimensionPixelOffset(R.dimen.x55);
                ppw_topNav.showAtLocation(parentView, Gravity.RIGHT | Gravity.TOP, ppwLocationX, DensityUtils.dp2px(this, 52));
                break;
            case R.id.ll_address_book://通讯录
                Intent intent = new Intent(this, GMemberListActivity.class);
                intent.putExtra("intent", ConstantUtils.INTENT_ADDRESS_BOOK);
                startActivity(intent);
                ppw_topNav.dismiss();
                break;
            case R.id.ll_call_history://通话记录
                startActivity(new Intent(this, CallRecordActivity.class));
                ppw_topNav.dismiss();
                break;
            case R.id.ll_create_call://创建对讲
                startActivity(new Intent(this, CreateGroupAndMessageForwardActivity.class));
                ppw_topNav.dismiss();
                break;
        }
    }

    public void setOnTabChangeListener(OnTabChangeListener onTabChangeListener) {
        this.onTabChangeListener = onTabChangeListener;
    }

    /**
     * tab切换至callFragment时调用,用于在全屏时隐藏导航栏
     */
    OnTabChangeListener onTabChangeListener;

    public interface OnTabChangeListener {
        void onChanged();
    }

    /**
     * 控制隐藏显示cardView，供fragment调用
     */
    public void showCardView(boolean isShow) {
        if (isShow) {
            cardView.setVisibility(View.VISIBLE);
        } else {
            cardView.setVisibility(View.GONE);
        }
    }

    public void setMessageUnreadCount(int count) {
        if (count > 0) {
            messageUnreadView.setVisibility(View.VISIBLE);
            if (count >= 100) {
                messageUnreadView.setText("...");
            } else {
                messageUnreadView.setText(count + "");
            }
        } else {
            messageUnreadView.setVisibility(View.GONE);
        }
    }

    private void setRecordUnreadCount(int count) {
        if (count > 0) {
            recordUnreadCountTv.setVisibility(View.VISIBLE);
            recordUnreadView.setVisibility(View.VISIBLE);
            if (count >= 100) {
                recordUnreadCountTv.setText("...");
            } else {
                recordUnreadCountTv.setText(count + "");
            }
        } else {
            recordUnreadCountTv.setVisibility(View.GONE);
            recordUnreadView.setVisibility(View.GONE);
        }
    }

    public void setPtytLoginStatus(String prompt) {
        PrintLog.i("设置主页用户状态 prompt = " + prompt);
        if (tv_offline == null) {
            PrintLog.w("tv_offline为空");
            return;
        }
        if (!StrUtils.isEmpty(prompt)) {
            tv_offline.setVisibility(View.VISIBLE);
            tv_offline.setText(prompt.toString());
        } else {
            tv_offline.setVisibility(View.GONE);
        }
        callFragment.notifyLoginStatusChanged();
        conversationFragment.notifyLoginStatusChanged();
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(true);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onPause() {
        super.onPause();
        PrintLog.i("MainActivity-------onPause()");
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onStop() {
        super.onStop();
        PrintLog.i("MainActivity-------onStop()");
        UctApplication.getInstance().isInMainActivity = false;
        /*ptyt start #4848返回系统界面时，组呼缩略框没有收起 kechuanqi_20171207*/
        GroupCallWindow groupCallWindow = UctApplication.getInstance().getGroupCallWindow();
        if (groupCallWindow != null) {
            if (!UctApplication.getInstance().isInGroupCall) {
                groupCallWindow.hidePopupWindow();
            } else if (AppUtils.isHome(mContext)) {//在组呼时且在home界面
                /*ptyt start 解决组呼时（组呼界面）返回home界面，悬浮框不显示*/
                groupCallWindow.show(this);
            }
        }
        /*ptyt end*/
        ScreenManager.getInstance().clear(this);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onResume() {
        super.onResume();
        PrintLog.i("MainActivity-------onResume() PTYTCallWindowView.isExtentMode="+PTYTCallWindowView.isExtentMode + "  currentFragmentPosition="+currentFragmentPosition);
        recordUnreadCount = ((Integer) UctClientApi.getUserData(SettingsConstant.SETTINGS_CALL_RECORD_UNREAD, 0)).intValue();
        setRecordUnreadCount(recordUnreadCount);
        setPtytLoginStatus(LoginCallBack.getLoginCallBack().loginMessage);
        /**
         * 当当前显示CallFragment时,隐藏悬浮窗
         */
        if (currentFragmentPosition == 1) {
            if(PTYTCallWindowView.isExtentMode){
                UctApplication.getInstance().getGroupCallWindow().hidePopupWindow();
            }else {
                callFragment.showGCallWindow();//组呼缩略框显示时，onResume()后返回至初始状态
            }
        }
        UctApplication.getInstance().isInMainActivity = true;
    }

    /**
     * 其他界面返回主界面
     */
    @Override
    protected void onRestart() {
        super.onRestart();
        PrintLog.i("MainActivity-------onRestart()");
    }

    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    @Override
    protected void onDestroy() {
        PrintLog.i("onDestroy()");
        EventBus.getDefault().unregister(this);
        UctApplication.getInstance().getGroupCallWindow().hidePopupWindow();
        UctApplication.getInstance().isInGroupCall = false;
        UctApplication.getInstance().isInMainActivity = false;
        popBitmapDrawable2 = null;
        if (networkChangeReceiver != null) {
            unregisterReceiver(networkChangeReceiver);
            networkChangeReceiver = null;
        }
        if(plugBroadcastReceiver != null){
            unregisterReceiver(plugBroadcastReceiver);
            plugBroadcastReceiver = null;
        }
        super.onDestroy();
    }

    @Override
    protected void onStart() {
        super.onStart();
        PrintLog.i("MainActivity-------onStart()");
    }
    private static final String HEADSET_STATE_PATH = "/sys/class/switch/h2w/state";

    private boolean isHeadsetExists() {
        char[] buffer = new char[1024];

        int newState = 0;

        try {
            FileReader file = new FileReader(HEADSET_STATE_PATH);
            int len = file.read(buffer, 0, 1024);
            newState = Integer.valueOf((new String(buffer, 0, len)).trim());
        }
        catch (FileNotFoundException e) {
            Log.e("FMTest", "This kernel does not have wired headset support");
        }
        catch (Exception e) {
            Log.e("FMTest", "", e);
        }
        return newState != 0;
    }
}
