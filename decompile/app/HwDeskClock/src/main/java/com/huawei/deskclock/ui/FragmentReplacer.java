package com.huawei.deskclock.ui;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.SystemClock;
import com.android.deskclock.AlarmsMainActivity;
import com.android.deskclock.R;
import com.android.deskclock.alarmclock.AlarmClock;
import com.android.deskclock.stopwatch.StopWatchPage;
import com.android.deskclock.timer.TimerPage;
import com.android.deskclock.worldclock.WorldClockPage;
import com.android.util.Config;
import com.android.util.Log;

public class FragmentReplacer {
    private AlarmClock mAlarmClockFragment;
    private boolean mCancel;
    private FragmentTransaction mFragmentTransaction;
    private Fragment mInitialAlarmClockFragment;
    private Fragment mInitialStopWatchFragment;
    private Fragment mInitialTimerFragment;
    private Fragment mInitialWorldClockFragment;
    private boolean mIsCommitPending;
    private AlarmsMainActivity mMainActivity;
    private StopWatchPage mStopWatchFragment;
    private Object mSyncLock = new Object();
    private TimerPage mTimerFragment;
    private WorldClockPage mWorldClockFragment;

    public FragmentReplacer(AlarmsMainActivity mainActivity) {
        Log.dRelease("FragmentReplacer", "FragmentReplacer");
        this.mMainActivity = mainActivity;
    }

    void showFragment(FragmentTransaction fragmentTransaction, Fragment fragment) {
        Log.dRelease("FragmentReplacer", "showFragment fragment: " + fragment + " mCancel:" + this.mCancel);
        if (!this.mCancel && fragment != null && fragment.isHidden()) {
            fragmentTransaction.show(fragment);
        }
    }

    void hideFragment(FragmentTransaction fragmentTransaction, Fragment fragment) {
        if (!this.mCancel && fragment != null && !fragment.isHidden()) {
            Log.dRelease("FragmentReplacer", "hideFragment fragment: " + fragment);
            fragmentTransaction.hide(fragment);
        }
    }

    public void replaceDummyFragments() {
        if (this.mCancel || this.mMainActivity == null) {
            Log.w("FragmentReplacer", "checkFragmentsAndPrepareAndWait mCancel: " + this.mCancel + "mMainActivity :" + this.mMainActivity);
            return;
        }
        FragmentManager fragmentManager = this.mMainActivity.getFragmentManager();
        newFragment();
        Log.dRelease("FragmentReplacer", "replaceDummyFragments for four fragment.");
        if (this.mMainActivity.isFinishing()) {
            this.mCancel = true;
            return;
        }
        this.mFragmentTransaction = fragmentManager.beginTransaction();
        addActualFragment();
        showFragment();
        if (this.mMainActivity.isSafeToCommitTransactions()) {
            Log.dRelease("FragmentReplacer", "luchunzhi replaceDummyFragments isSafeToCommitTransactions");
            this.mFragmentTransaction.commit();
            if (this.mAlarmClockFragment != null) {
                this.mMainActivity.setAlarmClockFragment(this.mAlarmClockFragment);
            }
            if (this.mWorldClockFragment != null) {
                this.mMainActivity.setWorldClockFragment(this.mWorldClockFragment);
            }
            if (this.mStopWatchFragment != null) {
                this.mMainActivity.setStopWatchPageFragment(this.mStopWatchFragment);
            }
            if (this.mTimerFragment != null) {
                this.mMainActivity.setTimerPageFragment(this.mTimerFragment);
            }
            this.mMainActivity.refreshPager();
        } else {
            this.mIsCommitPending = true;
        }
    }

    private void newFragment() {
        if (this.mInitialAlarmClockFragment instanceof DummyFragment) {
            this.mAlarmClockFragment = new AlarmClock();
        }
        if (this.mInitialWorldClockFragment instanceof DummyFragment) {
            this.mWorldClockFragment = new WorldClockPage();
        }
        if (this.mInitialStopWatchFragment instanceof DummyFragment) {
            this.mStopWatchFragment = new StopWatchPage();
        }
        if (this.mInitialTimerFragment instanceof DummyFragment) {
            this.mTimerFragment = new TimerPage();
        }
    }

    private void addActualFragment() {
        if (this.mAlarmClockFragment != null) {
            this.mFragmentTransaction.remove(this.mInitialAlarmClockFragment);
            this.mFragmentTransaction.add(R.id.pager, this.mAlarmClockFragment, "tab-pager-alarm");
            this.mFragmentTransaction.hide(this.mAlarmClockFragment);
            if (this.mInitialAlarmClockFragment instanceof DummyFragment) {
                ((DummyFragment) this.mInitialAlarmClockFragment).setReplaced(true);
            }
        }
        if (this.mWorldClockFragment != null) {
            this.mFragmentTransaction.remove(this.mInitialWorldClockFragment);
            this.mFragmentTransaction.add(R.id.pager, this.mWorldClockFragment, "tab-pager-world-clcok");
            this.mFragmentTransaction.hide(this.mWorldClockFragment);
            if (this.mInitialWorldClockFragment instanceof DummyFragment) {
                ((DummyFragment) this.mInitialWorldClockFragment).setReplaced(true);
            }
        }
        if (this.mStopWatchFragment != null) {
            this.mFragmentTransaction.remove(this.mInitialStopWatchFragment);
            this.mFragmentTransaction.add(R.id.pager, this.mStopWatchFragment, "tab-pager-stopwatch");
            this.mFragmentTransaction.hide(this.mStopWatchFragment);
            if (this.mInitialStopWatchFragment instanceof DummyFragment) {
                ((DummyFragment) this.mInitialStopWatchFragment).setReplaced(true);
            }
        }
        if (this.mTimerFragment != null) {
            this.mFragmentTransaction.remove(this.mInitialTimerFragment);
            this.mFragmentTransaction.add(R.id.pager, this.mTimerFragment, "tab-pager-timer");
            this.mFragmentTransaction.hide(this.mTimerFragment);
            if (this.mInitialTimerFragment instanceof DummyFragment) {
                ((DummyFragment) this.mInitialTimerFragment).setReplaced(true);
            }
        }
    }

    private void showFragment() {
        switch (Config.clockTabIndex()) {
            case 1:
                hideFragment(this.mFragmentTransaction, this.mAlarmClockFragment);
                showFragment(this.mFragmentTransaction, this.mInitialWorldClockFragment);
                hideFragment(this.mFragmentTransaction, this.mStopWatchFragment);
                hideFragment(this.mFragmentTransaction, this.mTimerFragment);
                return;
            case 2:
                hideFragment(this.mFragmentTransaction, this.mAlarmClockFragment);
                hideFragment(this.mFragmentTransaction, this.mWorldClockFragment);
                showFragment(this.mFragmentTransaction, this.mInitialStopWatchFragment);
                hideFragment(this.mFragmentTransaction, this.mTimerFragment);
                return;
            case 3:
                hideFragment(this.mFragmentTransaction, this.mAlarmClockFragment);
                hideFragment(this.mFragmentTransaction, this.mWorldClockFragment);
                hideFragment(this.mFragmentTransaction, this.mStopWatchFragment);
                showFragment(this.mFragmentTransaction, this.mInitialTimerFragment);
                return;
            default:
                showFragment(this.mFragmentTransaction, this.mInitialAlarmClockFragment);
                hideFragment(this.mFragmentTransaction, this.mWorldClockFragment);
                hideFragment(this.mFragmentTransaction, this.mStopWatchFragment);
                hideFragment(this.mFragmentTransaction, this.mTimerFragment);
                return;
        }
    }

    public void cencal() {
        this.mCancel = true;
    }

    public boolean isCancelled() {
        return this.mCancel;
    }

    public void commitIfPending() {
        if (this.mIsCommitPending) {
            Log.dRelease("Replacer", "commitIfPending");
            this.mIsCommitPending = false;
            if (this.mFragmentTransaction != null) {
                this.mFragmentTransaction.commitAllowingStateLoss();
            }
            if (this.mAlarmClockFragment != null) {
                this.mMainActivity.setAlarmClockFragment(this.mAlarmClockFragment);
            }
            if (this.mWorldClockFragment != null) {
                this.mMainActivity.setWorldClockFragment(this.mWorldClockFragment);
            }
            if (this.mStopWatchFragment != null) {
                this.mMainActivity.setStopWatchPageFragment(this.mStopWatchFragment);
            }
            if (this.mTimerFragment != null) {
                this.mMainActivity.setTimerPageFragment(this.mTimerFragment);
            }
            this.mMainActivity.refreshPager();
            return;
        }
        Log.w("FragmentReplacer", "checkFragmentsAndPrepareAndWait mIsCommitPending: " + this.mIsCommitPending + "mMainActivity :" + this.mMainActivity);
    }

    void checkFragmentsAndPrepareAndWait() {
        if (this.mCancel || this.mMainActivity == null) {
            Log.d("FragmentReplacer", "checkFragmentsAndPrepareAndWait mCancel: " + this.mCancel + "mMainActivity :" + this.mMainActivity);
            return;
        }
        Log.dRelease("FragmentReplacer", "checkFragmentsAndPrepareAndWait");
        FragmentManager fragmentManager = this.mMainActivity.getFragmentManager();
        this.mInitialAlarmClockFragment = fragmentManager.findFragmentByTag("tab-pager-alarm");
        Log.dRelease("FragmentReplacer", "mInitialAlarmClockFragment " + this.mInitialAlarmClockFragment);
        this.mInitialWorldClockFragment = fragmentManager.findFragmentByTag("tab-pager-world-clcok");
        this.mInitialStopWatchFragment = fragmentManager.findFragmentByTag("tab-pager-stopwatch");
        this.mInitialTimerFragment = fragmentManager.findFragmentByTag("tab-pager-timer");
        SystemClock.sleep(1000);
    }
}
