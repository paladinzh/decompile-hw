package com.huawei.systemmanager.netassistant.traffic.datasaver;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Message;
import android.os.MessageQueue.IdleHandler;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.ListView;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.component.GenericHandler;
import com.huawei.systemmanager.comm.component.GenericHandler.MessageHandler;
import com.huawei.systemmanager.comm.widget.slideview.PullView;
import com.huawei.systemmanager.comm.widget.slideview.SlidingUpPanelLayout;
import com.huawei.systemmanager.emui.activities.HsmActivity;
import com.huawei.systemmanager.hsmstat.HsmStat;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst.Events;
import com.huawei.systemmanager.util.HSMConst;
import com.huawei.systemmanager.util.HwLog;
import huawei.android.widget.SubTabWidget;
import huawei.android.widget.SubTabWidget.SubTab;
import huawei.support.v13.app.SubTabFragmentPagerAdapter;

public class DataSaverActivity extends HsmActivity implements MessageHandler, OnGlobalLayoutListener {
    private static final int MSG_ADD_SEC_PAGE = 201;
    private static final String TAG = DataSaverActivity.class.getSimpleName();
    SlidingUpPanelLayout mDataSaverRootLayout;
    DataSaverSwitchFragment mDataSaverSwitchFragment;
    DataSaverTitleFragment mDataSaverTitleFragment;
    GenericHandler mHandler;
    SizeRefreshHandler mSizeRefreshHandler;
    SubTabAdapter mSubTabFragmentPagerAdapter;
    ViewPager mViewPager;

    public static class DataSaverViewPagerPullView implements PullView {
        private final View mDragView;
        private final ViewPager mViewPager;

        public DataSaverViewPagerPullView(ViewPager viewPager, View dragView) {
            this.mViewPager = viewPager;
            this.mDragView = dragView;
        }

        public boolean isContentTop() {
            ListView listView = getListViewInPagerFragment();
            if (listView == null || listView.getChildCount() <= 0) {
                return true;
            }
            return listView.getFirstVisiblePosition() == 0 && (listView.getChildAt(0).getTop() >= listView.getListPaddingTop());
        }

        public boolean isContentFit() {
            return this.mDragView.getTop() > this.mDragView.getPaddingTop();
        }

        private ListView getListViewInPagerFragment() {
            if (this.mViewPager.getAdapter() != null && (this.mViewPager.getAdapter() instanceof SubTabAdapter)) {
                SubTabAdapter adapter = (SubTabAdapter) this.mViewPager.getAdapter();
                if (adapter != null) {
                    DataSaverFragment fragment = (DataSaverFragment) adapter.getItem(this.mViewPager.getCurrentItem());
                    if (fragment.getView() == null) {
                        return null;
                    }
                    try {
                        return fragment.getListView();
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                }
            }
            return null;
        }

        public View getView() {
            return this.mViewPager;
        }
    }

    class SizeRefreshHandler implements IdleHandler {
        SizeRefreshHandler() {
        }

        public boolean queueIdle() {
            DataSaverActivity.this.setSlidingUpPanelMarginTop();
            return false;
        }
    }

    private static class SubTabAdapter extends SubTabFragmentPagerAdapter {
        public SubTabAdapter(Activity activity, ViewPager pager, SubTabWidget subTabWidget) {
            super(activity, pager, subTabWidget);
        }

        public void onPageSelected(int position) {
            int i;
            super.onPageSelected(position);
            if (position == 0) {
                i = Events.E_DATA_SAVER_SHOW_PERSONAL_APP_SELECTED;
            } else {
                i = Events.E_DATA_SAVER_SHOW_SYSTEM_APP_SELECTED;
            }
            HsmStat.statE(i);
        }
    }

    private void setSlidingUpPanelMarginTop() {
        View dataSaverHintView = findViewById(R.id.data_saver_container);
        int panelMarginTop = 0;
        if (dataSaverHintView != null) {
            panelMarginTop = dataSaverHintView.getHeight() + dataSaverHintView.getPaddingTop();
        }
        this.mDataSaverRootLayout.setPanelMarginTop(panelMarginTop);
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.data_saver_layout);
        initHandler();
        initView();
    }

    private void initHandler() {
        this.mHandler = new GenericHandler(this);
        this.mSizeRefreshHandler = new SizeRefreshHandler();
        getMainLooper().getQueue().addIdleHandler(this.mSizeRefreshHandler);
    }

    protected void onResume() {
        super.onResume();
    }

    protected void onDestroy() {
        super.onDestroy();
        this.mHandler = null;
        this.mSubTabFragmentPagerAdapter = null;
        this.mViewPager = null;
    }

    private void initView() {
        initDataSaverModeSwitcher();
        initDataSaverTitleView();
        initAppListPage();
        initSlidingUpView();
    }

    private void initSlidingUpView() {
        this.mDataSaverRootLayout = (SlidingUpPanelLayout) findViewById(R.id.data_saver_root_layout);
        View dragView = this.mDataSaverRootLayout.getDragView();
        if (dragView != null) {
            this.mDataSaverRootLayout.setPullView(new DataSaverViewPagerPullView(this.mViewPager, dragView));
        }
        if (HSMConst.isSupportSubfiled(null)) {
            HSMConst.setCfgForSlidingUp(getIntent(), this.mDataSaverRootLayout);
        }
    }

    private void initAppListPage() {
        this.mViewPager = (ViewPager) findViewById(R.id.pager);
        addSubTab(getString(R.string.net_assistant_installed_app_title), 0, true);
        this.mHandler.sendEmptyMessageDelayed(201, 100);
    }

    private void addSubTab(String title, int appType, boolean currentPage) {
        DataSaverFragment frag = new DataSaverFragment();
        SubTabWidget subTabWidget = (SubTabWidget) findViewById(R.id.subTab_layout);
        SubTab subTab = subTabWidget.newSubTab(title);
        Bundle bundle = new Bundle();
        bundle.putInt("app_type", appType);
        if (this.mSubTabFragmentPagerAdapter == null) {
            this.mSubTabFragmentPagerAdapter = new SubTabAdapter(this, this.mViewPager, subTabWidget);
        }
        subTab.setSubTabId(R.id.systemmanager_datasaveractivity_net_assistant_system_app_title);
        this.mSubTabFragmentPagerAdapter.addSubTab(subTab, frag, bundle, currentPage);
    }

    private void initDataSaverModeSwitcher() {
        FragmentManager fmg = getFragmentManager();
        this.mDataSaverSwitchFragment = (DataSaverSwitchFragment) fmg.findFragmentById(R.id.data_saver_container);
        if (this.mDataSaverSwitchFragment == null) {
            this.mDataSaverSwitchFragment = new DataSaverSwitchFragment();
            fmg.beginTransaction().add(R.id.data_saver_container, this.mDataSaverSwitchFragment, DataSaverConstants.KEY_PREF_DATA_SAVER_SWITCH).commit();
        }
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        this.mDataSaverRootLayout.getViewTreeObserver().addOnGlobalLayoutListener(this);
    }

    public void onGlobalLayout() {
        if (this.mDataSaverRootLayout == null) {
            HwLog.e(TAG, "onGlobalLayout , mDataSaverRootLayout is null ! ");
            return;
        }
        this.mDataSaverRootLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
        HwLog.d(TAG, "onGlobalLayout , start setSlidingUpPanelMarginTop");
        setSlidingUpPanelMarginTop();
    }

    private void initDataSaverTitleView() {
        FragmentManager fmg = getFragmentManager();
        this.mDataSaverTitleFragment = (DataSaverTitleFragment) fmg.findFragmentById(R.id.data_saver_title_container);
        if (this.mDataSaverTitleFragment == null) {
            this.mDataSaverTitleFragment = new DataSaverTitleFragment();
            fmg.beginTransaction().add(R.id.data_saver_title_container, this.mDataSaverTitleFragment, DataSaverConstants.KEY_PREF_DATA_SAVER_TITLE).commit();
        }
    }

    public void onHandleMessage(Message msg) {
        switch (msg.what) {
            case 201:
                addSubTab(getString(R.string.net_assistant_system_app_title), 1, false);
                return;
            default:
                return;
        }
    }
}
