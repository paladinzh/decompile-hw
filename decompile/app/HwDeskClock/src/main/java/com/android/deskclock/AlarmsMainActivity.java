package com.android.deskclock;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.StrictMode;
import android.os.StrictMode.ThreadPolicy.Builder;
import android.os.StrictMode.VmPolicy;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import com.android.deskclock.alarmclock.AlarmClock;
import com.android.deskclock.alarmclock.LogcatService;
import com.android.deskclock.stopwatch.StopWatchPage;
import com.android.deskclock.timer.TimerPage;
import com.android.deskclock.timer.TimerService;
import com.android.deskclock.worldclock.TimeZoneUtils;
import com.android.deskclock.worldclock.WorldClockPage;
import com.android.util.ClockReporter;
import com.android.util.Config;
import com.android.util.HwLog;
import com.android.util.Log;
import com.android.util.ReflexUtil;
import com.android.util.Utils;
import com.huawei.deskclock.ui.ActionBarAdapter;
import com.huawei.deskclock.ui.ActionBarAdapter.Listener;
import com.huawei.deskclock.ui.DummyFragment;
import com.huawei.deskclock.ui.FragmentReplacer;
import com.huawei.deskclock.ui.FragmentReplacerTask;
import com.huawei.deskclock.ui.HwBaseActivity;

public class AlarmsMainActivity extends HwBaseActivity implements Listener {
    private static boolean mLockedEnter;
    boolean DEVELOPER_MODE = false;
    private boolean isClockWidget;
    protected boolean isFromOtherAPP;
    private boolean isMeWidet;
    private ActionBarAdapter mActionBarAdapter;
    private ClockFragment mAlarmClockFragment;
    private FragmentReplacer mFragmentReplacer;
    private FragmentReplacerTask mFragmentReplacerTask;
    private SharedPreferences mPreference;
    private boolean mRequestDialog = false;
    private ClockFragment mStopWatchFragment;
    private TabsAdapter mTabsAdapter;
    private ClockFragment mTimerFragment;
    private CustViewPage mViewPager;
    private ClockFragment mWorldClockFragment;

    public class TabsAdapter extends PagerAdapter implements OnPageChangeListener {
        private final ActionBar mActionBar;
        private FragmentTransaction mCurTransaction = null;
        private Fragment mCurrentPrimaryItem;
        private final FragmentManager mFragmentManager;
        private boolean[] mOldVisible = new boolean[4];
        private final ViewPager mSubViewPager;

        public TabsAdapter(Activity activity, ViewPager pager) {
            this.mFragmentManager = AlarmsMainActivity.this.getFragmentManager();
            this.mActionBar = activity.getActionBar();
            this.mSubViewPager = pager;
            this.mSubViewPager.setAdapter(this);
            this.mSubViewPager.setOnPageChangeListener(this);
        }

        public int getCount() {
            return 4;
        }

        public int getItemPosition(Object object) {
            if (object instanceof DummyFragment) {
                DummyFragment dummyFragment = (DummyFragment) object;
                if (dummyFragment.isReplaced()) {
                    return -2;
                }
                return dummyFragment.getTabIndex();
            } else if (object instanceof AlarmClock) {
                return 0;
            } else {
                if (object instanceof WorldClockPage) {
                    return 1;
                }
                if (object instanceof StopWatchPage) {
                    return 2;
                }
                if (object instanceof TimerPage) {
                    return 3;
                }
                return -2;
            }
        }

        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }

        public void onPageSelected(int position) {
            this.mActionBar.setSelectedNavigationItem(position);
            if (AlarmsMainActivity.this.mViewPager.getOffscreenPageLimit() < 3) {
                AlarmsMainActivity.this.mViewPager.setOffscreenPageLimit(3);
            }
            if (AlarmsMainActivity.ismLockedEnter() && position == 0) {
                AlarmsMainActivity.this.getWindow().setFlags(4194304, 4718592);
            }
            Log.dRelease("AlarmsMainActivity", "onPageSelected : change to page " + position);
            int i = 0;
            while (i < 4) {
                ClockFragment clockFragment = getFragment(i);
                if (clockFragment != null) {
                    this.mOldVisible = clockFragment.notifyFragmentChange(this.mOldVisible, i == position, i);
                }
                i++;
            }
            AlarmsMainActivity.this.mActionBarAdapter.setCurrentTab(position, false);
            AlarmsMainActivity.wakeLock(AlarmsMainActivity.this.mViewPager.getCurrentItem());
            Config.setmCurrentTab(position);
        }

        public void onPageScrollStateChanged(int state) {
            if (state == 1) {
                View lView = AlarmsMainActivity.this.getCurrentFocus();
                if (lView != null) {
                    lView.clearFocus();
                }
            }
        }

        private ClockFragment getFragment(int position) {
            switch (position) {
                case 0:
                    return AlarmsMainActivity.this.mAlarmClockFragment;
                case 1:
                    return AlarmsMainActivity.this.mWorldClockFragment;
                case 2:
                    return AlarmsMainActivity.this.mStopWatchFragment;
                case 3:
                    return AlarmsMainActivity.this.mTimerFragment;
                default:
                    throw new IllegalArgumentException("position: " + position);
            }
        }

        public Object instantiateItem(ViewGroup container, int position) {
            if (this.mCurTransaction == null) {
                this.mCurTransaction = this.mFragmentManager.beginTransaction();
            }
            Fragment fragment = getFragment(position);
            this.mCurTransaction.show(fragment);
            Log.dRelease("TabPagerAdapter", "Fragment show: " + fragment + "; position:" + position);
            fragment.setUserVisibleHint(fragment == this.mCurrentPrimaryItem);
            return fragment;
        }

        public void destroyItem(ViewGroup container, int position, Object object) {
            if (this.mCurTransaction == null) {
                this.mCurTransaction = this.mFragmentManager.beginTransaction();
            }
            this.mCurTransaction.hide((Fragment) object);
        }

        public void finishUpdate(ViewGroup container) {
            if (this.mCurTransaction != null) {
                this.mCurTransaction.commitAllowingStateLoss();
                this.mCurTransaction = null;
                this.mFragmentManager.executePendingTransactions();
            }
        }

        public boolean isViewFromObject(View view, Object object) {
            return ((Fragment) object).getView() == view;
        }

        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            Fragment fragment = (Fragment) object;
            if (this.mCurrentPrimaryItem != fragment) {
                this.mCurrentPrimaryItem = fragment;
            }
        }

        public Parcelable saveState() {
            return null;
        }

        public void restoreState(Parcelable state, ClassLoader loader) {
        }
    }

    public static boolean ismLockedEnter() {
        return mLockedEnter;
    }

    public static void setmLockedEnter(boolean lockedEnter) {
        mLockedEnter = lockedEnter;
    }

    public boolean dispatchKeyEvent(KeyEvent event) {
        boolean up = event.getAction() == 1;
        switch (event.getKeyCode()) {
            case 82:
                if (up) {
                    Log.d("AlarmsMainActivity", "the fuction of keycode menu has not use.");
                }
                return true;
            default:
                return super.dispatchKeyEvent(event);
        }
    }

    private void createViewsAndFragments(Bundle saveState) {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        this.mViewPager = new CustViewPage(this);
        this.mViewPager.setId(R.id.pager);
        setContentView(this.mViewPager);
        this.mViewPager.setOffscreenPageLimit(1);
        this.mTabsAdapter = new TabsAdapter(this, this.mViewPager);
        this.mActionBarAdapter = new ActionBarAdapter(this, getActionBar(), this);
        this.mAlarmClockFragment = (ClockFragment) fragmentManager.findFragmentByTag("tab-pager-alarm");
        this.mWorldClockFragment = (ClockFragment) fragmentManager.findFragmentByTag("tab-pager-world-clcok");
        this.mStopWatchFragment = (ClockFragment) fragmentManager.findFragmentByTag("tab-pager-stopwatch");
        this.mTimerFragment = (ClockFragment) fragmentManager.findFragmentByTag("tab-pager-timer");
        if (this.mAlarmClockFragment == null) {
            int currentTab = Config.clockTabIndex();
            Log.d("AlarmsMainActivity", "onCreat currentTab = " + currentTab);
            switch (currentTab) {
                case 1:
                    this.mAlarmClockFragment = new DummyFragment("tab-pager-alarm", 0);
                    this.mWorldClockFragment = new WorldClockPage();
                    this.mStopWatchFragment = new DummyFragment("tab-pager-stopwatch", 2);
                    this.mTimerFragment = new DummyFragment("tab-pager-timer", 3);
                    break;
                case 2:
                    this.mAlarmClockFragment = new DummyFragment("tab-pager-alarm", 0);
                    this.mWorldClockFragment = new DummyFragment("tab-pager-world-clcok", 1);
                    this.mStopWatchFragment = new StopWatchPage();
                    this.mTimerFragment = new DummyFragment("tab-pager-timer", 3);
                    break;
                case 3:
                    this.mAlarmClockFragment = new DummyFragment("tab-pager-alarm", 0);
                    this.mWorldClockFragment = new DummyFragment("tab-pager-world-clcok", 1);
                    this.mStopWatchFragment = new DummyFragment("tab-pager-stopwatch", 2);
                    this.mTimerFragment = new TimerPage();
                    break;
                default:
                    this.mAlarmClockFragment = new AlarmClock();
                    this.mWorldClockFragment = new DummyFragment("tab-pager-world-clcok", 1);
                    this.mStopWatchFragment = new DummyFragment("tab-pager-stopwatch", 2);
                    this.mTimerFragment = new DummyFragment("tab-pager-timer", 3);
                    break;
            }
        }
        transaction.add(R.id.pager, this.mAlarmClockFragment, "tab-pager-alarm");
        transaction.add(R.id.pager, this.mWorldClockFragment, "tab-pager-world-clcok");
        transaction.add(R.id.pager, this.mStopWatchFragment, "tab-pager-stopwatch");
        transaction.add(R.id.pager, this.mTimerFragment, "tab-pager-timer");
        transaction.hide(this.mAlarmClockFragment);
        transaction.hide(this.mWorldClockFragment);
        transaction.hide(this.mStopWatchFragment);
        transaction.hide(this.mTimerFragment);
        transaction.commitAllowingStateLoss();
        fragmentManager.executePendingTransactions();
        this.mActionBarAdapter.update();
    }

    protected void onCreate(Bundle savedInstanceState) {
        if (this.DEVELOPER_MODE) {
            StrictMode.setThreadPolicy(new Builder().detectDiskReads().detectDiskWrites().detectNetwork().detectCustomSlowCalls().penaltyLog().build());
            StrictMode.setVmPolicy(new VmPolicy.Builder().detectLeakedSqlLiteObjects().detectLeakedClosableObjects().penaltyLog().penaltyDeath().build());
        }
        DeskClockApplication.getDeskClockApplication().setTranslucentStatus(true, this);
        super.onCreate(savedInstanceState);
        this.mPreference = Utils.getDefaultSharedPreferences(this);
        Log.iRelease("AlarmsMainActivity", "onCreate");
        Config.doSetExit_count(1);
        Intent i = getIntent();
        setmLockedEnter(false);
        if (i != null) {
            if ("android.security.action.START_APP_SECURE".equals(i.getAction())) {
                getWindow().addFlags(524288);
                if (savedInstanceState == null || !savedInstanceState.containsKey("lock_enter_tab")) {
                    Config.updateCurrentTab(3);
                } else {
                    HwLog.i("AlarmsMainActivity", "Restore pre tab in lock entered");
                    Config.updateCurrentTab(savedInstanceState.getInt("lock_enter_tab"));
                    savedInstanceState.remove("lock_enter_tab");
                }
                this.isFromOtherAPP = true;
                setmLockedEnter(true);
            } else {
                int tab = i.getIntExtra("deskclock.select.tab", -1);
                if (tab != -1) {
                    Config.updateCurrentTab(tab);
                    this.isFromOtherAPP = true;
                }
            }
        }
        DeskClockApplication.getDeskClockApplication().openAccelerated(true, this);
        initFromBundle(savedInstanceState);
        createViewsAndFragments(savedInstanceState);
        this.mActionBarAdapter.updateClickTabState(false);
        TimeZoneUtils.nSyncLoadLoadInitData(DeskClockApplication.getDeskClockApplication());
        if (savedInstanceState != null) {
            new Bundle().putLong("currentTime", savedInstanceState.getLong("currentTime"));
        }
        this.mViewPager.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (AlarmsMainActivity.this.isResumed() && AlarmsMainActivity.this.mViewPager.getOffscreenPageLimit() < 3) {
                    AlarmsMainActivity.this.mViewPager.setOffscreenPageLimit(3);
                }
                return false;
            }
        });
        if (!LogcatService.isRun()) {
            LogcatService.judgeAlarms(getApplicationContext());
        }
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.dRelease("AlarmsMainActivity", "onConfigurationChanged");
    }

    protected void onResume() {
        super.onResume();
        Utils.setDeskClockForeground(true);
        Log.iRelease("AlarmsMainActivity", "onResume asyncthreadstate = " + AsyncHandler.getState());
        this.mActionBarAdapter.registerListener(this);
        restartFragmentReplacement();
        updateFragmentsVisibility(Config.clockTabIndex());
        wakeLock(this.mViewPager.getCurrentItem());
        if (Utils.getSharedPreferences(this, "timer", 0).getInt("state", 3) != 3) {
            destoryTimerNotify();
        }
    }

    public void destoryTimerNotify() {
        HwLog.d("timer_notify", "AlarmsMainActivity destoryTimerNotify");
        Intent timerIntent = new Intent(getApplicationContext(), TimerService.class);
        timerIntent.setAction("kill_nofity");
        startService(timerIntent);
    }

    protected void onPause() {
        Log.dRelease("AlarmsMainActivity", "onPause");
        Utils.setDeskClockForeground(false);
        AlarmAlertWakeLock.releaseFullLock();
        super.onPause();
        if (Utils.getSharedPreferences(this, "timer", 0).getInt("state", 3) != 3) {
            showTimerNotify();
        }
    }

    public void showTimerNotify() {
        HwLog.d("timer_notify", "AlarmsMainActivity showTimerNotify");
        Intent intent = new Intent(getApplicationContext(), TimerService.class);
        intent.setAction("show_notify");
        startService(intent);
    }

    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.iRelease("AlarmsMainActivity", "onNewIntent");
        if (intent == null) {
            Log.e("AlarmsMainActivity", "intent is null");
            return;
        }
        setIntent(intent);
        this.isMeWidet = intent.getBooleanExtra("MeWidget", false);
        this.isClockWidget = intent.getBooleanExtra("isClockWidget", false);
        int tab = intent.getIntExtra("deskclock.select.tab", -1);
        if (tab != -1) {
            Config.updateCurrentTab(tab);
            this.isFromOtherAPP = true;
        }
    }

    private void updateFragmentsVisibility(int currentTab) {
        this.mViewPager.setCurrentItem(currentTab);
    }

    protected void onStart() {
        Log.dRelease("AlarmsMainActivity", "onStart");
        this.mActionBarAdapter.updateClickTabState(false);
        if (this.isClockWidget || this.isMeWidet) {
            int eventId = 0;
            if (this.isMeWidet) {
                eventId = 2;
            } else if (this.isClockWidget) {
                eventId = 1;
            }
            ClockReporter.reportEventMessage(this, eventId, "");
            Config.updateCurrentTab(1);
            this.mActionBarAdapter.setCurrentTab(1);
            this.isMeWidet = false;
            this.isClockWidget = false;
        } else if (this.isFromOtherAPP) {
            this.isFromOtherAPP = true;
        } else {
            Config.updateCurrentTab(this.mPreference.getInt("currentTab", 0));
            this.mActionBarAdapter.setCurrentTab(this.mPreference.getInt("currentTab", 0));
        }
        android.util.Log.e("page", "tab index =" + Config.clockTabIndex());
        restartFragmentReplacement();
        this.mViewPager.setCurrentItem(Config.clockTabIndex());
        super.onStart();
    }

    protected void onRestart() {
        super.onRestart();
        Log.dRelease("AlarmsMainActivity", "onRestart");
    }

    public void onBackPressed() {
        super.onBackPressed();
        Log.dRelease("TAG", "onBackPressed");
        finish();
    }

    protected void onDestroy() {
        super.onDestroy();
        Log.dRelease("AlarmsMainActivity", "onDestroy");
        if (AlarmClock.queryBackStack() > 0) {
            Log.i("AlarmsMainActivity", "queryBackStack() > 0 onDestroy");
            return;
        }
        cancelReplacerTask();
        if (this.mActionBarAdapter != null) {
            this.mActionBarAdapter.registerListener(null);
        }
        setObject2Null();
        ReflexUtil.fixInputMethodManagerLeak(this);
    }

    public static void wakeLock(int pos) {
        if (pos == 2 && StopWatchPage.isRunning()) {
            AlarmAlertWakeLock.acquireFullWakeLock();
        } else {
            AlarmAlertWakeLock.releaseFullLock();
        }
    }

    protected void onStop() {
        Log.dRelease("AlarmsMainActivity", "onStop");
        saveCurTabInfo();
        super.onStop();
        setIntent(null);
    }

    protected void onSaveInstanceState(Bundle outState) {
        Log.dRelease("AlarmsMainActivity", "onSaveInstanceState");
        saveCurTabInfo();
        if (this.mTimerFragment instanceof TimerPage) {
            outState.putLong("currentTime", ((TimerPage) this.mTimerFragment).queryCurTime().longValue());
        }
    }

    private void saveCurTabInfo() {
        Editor mEditor = this.mPreference.edit();
        int curTab = this.mViewPager.getCurrentItem();
        mEditor.putInt("currentTab", curTab);
        mEditor.commit();
        Log.d("AlarmsMainActivity", "saveCurTabInfo : saveCurTabInfo curTab = " + curTab);
    }

    private void setObject2Null() {
        this.mPreference = null;
        this.mTabsAdapter = null;
        this.mViewPager = null;
    }

    public void setAlarmClockFragment(ClockFragment fragment) {
        this.mAlarmClockFragment = fragment;
    }

    public void setWorldClockFragment(ClockFragment fragment) {
        this.mWorldClockFragment = fragment;
    }

    public void setStopWatchPageFragment(ClockFragment fragment) {
        this.mStopWatchFragment = fragment;
    }

    public void setTimerPageFragment(ClockFragment fragment) {
        this.mTimerFragment = fragment;
    }

    public void refreshPager() {
        this.mTabsAdapter.notifyDataSetChanged();
    }

    private void cancelReplacerTask() {
        if (!(this.mFragmentReplacer == null || this.mFragmentReplacer.isCancelled())) {
            this.mFragmentReplacer.cencal();
        }
        if (this.mFragmentReplacerTask != null && !this.mFragmentReplacerTask.isCancelled()) {
            this.mFragmentReplacerTask.cancel(true);
        }
    }

    private void restartFragmentReplacement() {
        if ((this.mFragmentReplacerTask != null && this.mFragmentReplacerTask.isCancelled()) || this.mFragmentReplacerTask == null) {
            Log.dRelease("AlarmsMainActivity", "restartFragmentReplacement new mFragmentReplacer!");
            this.mFragmentReplacer = new FragmentReplacer(this);
            this.mFragmentReplacerTask = new FragmentReplacerTask();
            this.mFragmentReplacerTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new FragmentReplacer[]{this.mFragmentReplacer});
        } else if (this.mFragmentReplacer != null) {
            Log.dRelease("AlarmsMainActivity", "restartFragmentReplacement mFragmentReplacer commitIfPending!");
            this.mFragmentReplacer.commitIfPending();
        }
    }

    public void onSelectedTabChanged(int tabIndex) {
        Log.dRelease("AlarmsMainActivity", "onSelectedTabChanged");
        Config.updateCurrentTab(tabIndex);
        updateFragmentsVisibility(tabIndex);
    }

    private void initFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            Log.dRelease("AlarmsMainActivity", "onCreate : savedInstanceState == null");
            Intent intent = getIntent();
            if (intent != null) {
                this.isMeWidet = intent.getBooleanExtra("MeWidget", false);
                this.isClockWidget = intent.getBooleanExtra("isClockWidget", false);
            }
        }
        if (this.isClockWidget || this.isMeWidet) {
            Config.updateCurrentTab(1);
        } else if (this.isFromOtherAPP) {
            this.isFromOtherAPP = true;
        } else {
            Config.updateCurrentTab(this.mPreference.getInt("currentTab", 0));
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        HwLog.i("AlarmsMainActivity", "onActivityResult");
        super.onActivityResult(requestCode, resultCode, data);
        if (-1 == resultCode && requestCode == 0 && !TextUtils.isEmpty(data.getExtras().getString("unique_id", ""))) {
            HwLog.i("AlarmsMainActivity", "The result of add city UI from settings");
            this.mWorldClockFragment.onActivityResult(requestCode, resultCode, data);
        }
    }
}
