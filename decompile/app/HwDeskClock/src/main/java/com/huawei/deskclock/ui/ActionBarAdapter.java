package com.huawei.deskclock.ui;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
import android.app.FragmentTransaction;
import android.content.Context;
import com.android.deskclock.R;
import com.android.util.ClockReporter;
import com.android.util.Config;
import com.android.util.Log;
import com.android.util.ReflexUtil;

public class ActionBarAdapter {
    protected final ActionBar mActionBar;
    private Context mContext;
    private int mCurrentTab = 0;
    private boolean mIsClickTab = true;
    protected Listener mListener;
    private int mOldTabPosition = -1;
    private final MyTabListener mTabListener;

    public interface Listener {
        void onSelectedTabChanged(int i);
    }

    private class MyTabListener implements TabListener {
        public boolean mIgnoreTabSelected;

        private MyTabListener() {
        }

        public void onTabReselected(Tab tab, FragmentTransaction ft) {
        }

        public void onTabUnselected(Tab tab, FragmentTransaction ft) {
        }

        public void onTabSelected(Tab tab, FragmentTransaction ft) {
            if (!this.mIgnoreTabSelected) {
                Log.dRelease("ActionBarAdapter", "MyTabListener onTabSelected");
                int tabPosition = tab.getPosition();
                ActionBarAdapter.this.setCurrentTab(tabPosition);
                if (!ActionBarAdapter.this.mIsClickTab && ActionBarAdapter.this.mOldTabPosition != tabPosition) {
                    ActionBarAdapter.this.reportClockEvent(ActionBarAdapter.this.mContext, tabPosition);
                    ActionBarAdapter.this.mOldTabPosition = tabPosition;
                }
            }
        }
    }

    public void updateClickTabState(boolean isClickTab) {
        this.mIsClickTab = isClickTab;
    }

    public void setCurrentTab(int tab) {
        setCurrentTab(tab, true);
    }

    public void setCurrentTab(int tab, boolean notifyListener) {
        Log.dRelease("ActionBarAdapter", "setCurrentTab tab = " + tab);
        if (tab != this.mCurrentTab) {
            this.mCurrentTab = tab;
            if (this.mCurrentTab != this.mActionBar.getSelectedNavigationIndex()) {
                this.mActionBar.setSelectedNavigationItem(this.mCurrentTab);
            }
            Config.setmCurrentTab(tab);
            if (notifyListener && this.mListener != null) {
                this.mListener.onSelectedTabChanged(tab);
            }
        }
    }

    public void registerListener(Listener listener) {
        this.mListener = listener;
    }

    public ActionBarAdapter(Context context, ActionBar actionBar, Listener listener) {
        this.mActionBar = actionBar;
        this.mContext = context;
        this.mListener = listener;
        this.mTabListener = new MyTabListener();
        this.mActionBar.setDisplayHomeAsUpEnabled(true);
        this.mActionBar.setDisplayOptions(0);
        this.mTabListener.mIgnoreTabSelected = true;
        this.mActionBar.setNavigationMode(2);
        this.mTabListener.mIgnoreTabSelected = false;
        this.mActionBar.setTitle(null);
        setupTabs();
    }

    protected void addTab(int expectedTabIndex, int icon, int description, int id) {
        Tab tab = this.mActionBar.newTab();
        tab.setTabListener(this.mTabListener);
        tab.setText(icon);
        tab.setContentDescription(description);
        ReflexUtil.setActionBarExId(tab, id);
        this.mActionBar.addTab(tab);
        if (expectedTabIndex != tab.getPosition()) {
            Log.w("ActionBarAdapter", "sometimes focus is not in the current tab");
        }
    }

    protected void setupTabs() {
        addTab(0, R.string.alarm_title_new, R.string.alarm_title_new, R.id.tab_actionbar_alarm);
        addTab(1, R.string.world_clock_title_new, R.string.world_clock_title_new, R.id.tab_actionbar_world);
        addTab(2, R.string.stopwatch_title_new, R.string.stopwatch_title_new, R.id.tab_actionbar_stop);
        addTab(3, R.string.timer_title_new, R.string.timer_title_new, R.id.tab_actionbar_timer);
    }

    private void reportClockEvent(Context context, int tabIndex) {
        int eventId = 0;
        switch (tabIndex) {
            case 0:
                eventId = 4;
                break;
            case 1:
                eventId = 5;
                break;
            case 2:
                eventId = 7;
                break;
            case 3:
                eventId = 6;
                break;
        }
        ClockReporter.reportEventMessage(context, eventId, "");
    }

    public void update() {
        this.mTabListener.mIgnoreTabSelected = true;
        this.mActionBar.setNavigationMode(2);
        this.mActionBar.setSelectedNavigationItem(Config.clockTabIndex());
        this.mTabListener.mIgnoreTabSelected = false;
        this.mActionBar.setTitle(null);
    }
}
