package com.android.contacts.hap.optimize;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import com.android.contacts.activities.ActionBarAdapter.TabState;
import com.android.contacts.activities.ContactDetailLayoutCache;
import com.android.contacts.activities.PeopleActivity;
import com.android.contacts.dialpad.DialpadFragment;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.list.DefaultContactBrowseListFragment;
import com.android.contacts.util.HwLog;
import com.google.android.gms.R;
import com.huawei.cspcommon.performance.PLog;

public class FragmentReplacer {
    private LayoutInflater inflator = null;
    private DefaultContactBrowseListFragment initialAllFragment;
    private Fragment initialDialpadFragment;
    private Fragment intialFavorOrYPFragment;
    private DefaultContactBrowseListFragment mAllFragment;
    private boolean mCancel;
    private DialpadFragment mDialpadFragment;
    private View mDiapadView = null;
    private Fragment mFavorOrYPFragment;
    private FragmentTransaction mFragmentTransaction;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 6001:
                    if (FragmentReplacer.this.mPeopleActivity != null) {
                        ContactDetailLayoutCache.inflateDetailsViewInBackground(FragmentReplacer.this.mPeopleActivity, R.layout.contact_detail_anim);
                        if (FragmentReplacer.this.mPeopleActivity.getCurrentTab() != TabState.DIALER && 1 == FragmentReplacer.this.mPeopleActivity.getResources().getConfiguration().orientation && BackgroundCacheHdlr.haveNotBeenInflate()) {
                            if (!CommonUtilMethods.isLargeThemeApplied(FragmentReplacer.this.mPeopleActivity.getResources()) && !CommonUtilMethods.isSpecialLanguageForDialpad()) {
                                FragmentReplacer.this.mDiapadView = FragmentReplacer.this.inflator.inflate(R.layout.contacts_dialpad, null);
                                break;
                            } else {
                                FragmentReplacer.this.mDiapadView = FragmentReplacer.this.inflator.inflate(R.layout.dialpad_huawei, null);
                                break;
                            }
                        }
                    }
                    if (HwLog.HWFLOW) {
                        HwLog.i("Optimization", "mPeopleActivity is null, return");
                    }
                    return;
                    break;
            }
            super.handleMessage(msg);
        }
    };
    private boolean mIsCommitPending;
    private PeopleActivity mPeopleActivity;
    private Object mSyncLock;

    public synchronized View getDiapadView() {
        View lDiapadView;
        lDiapadView = this.mDiapadView;
        this.mDiapadView = null;
        return lDiapadView;
    }

    public FragmentReplacer(PeopleActivity peopleActivity) {
        this.mPeopleActivity = peopleActivity;
        this.mSyncLock = new Object();
        Context context = new ContextThemeWrapper(this.mPeopleActivity.getApplicationContext(), this.mPeopleActivity.getResources().getIdentifier("androidhwext:style/Theme.Emui", null, null));
        context.getTheme().applyStyle(R.style.PeopleTheme, true);
        this.inflator = (LayoutInflater) context.getSystemService("layout_inflater");
    }

    void showFragment(FragmentTransaction fragmentTransaction, Fragment fragment) {
        if (!this.mCancel && fragment != null && fragment.isHidden()) {
            fragmentTransaction.show(fragment);
        }
    }

    public void inflateDetailsViewWithDelay() {
        this.mHandler.sendEmptyMessageDelayed(6001, 1000);
    }

    public void cancelInflateDetailsView() {
        if (this.mHandler.hasMessages(6001)) {
            this.mHandler.removeMessages(6001);
        }
    }

    public synchronized void updateFragmentInTab() {
        FragmentManager fragmentManager = this.mPeopleActivity.getFragmentManager();
        this.initialDialpadFragment = fragmentManager.findFragmentByTag("tab-pager-dialer");
        this.intialFavorOrYPFragment = fragmentManager.findFragmentByTag("tab-pager-favor-yellow");
        this.initialAllFragment = (DefaultContactBrowseListFragment) fragmentManager.findFragmentByTag("tab-pager-all");
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void replaceDummyFragments(int currentTab) {
        HwLog.i("Optimization", "entering replaceDummyFragments()");
        if (this.mCancel || this.mPeopleActivity == null) {
            HwLog.e("Optimization", "Ooops, some thing went wrong, mPeopleActivity is NULL, can't proceed is cancelled:" + this.mCancel);
            return;
        }
        FragmentManager fragmentManager = this.mPeopleActivity.getFragmentManager();
        updateFragmentInTab();
        this.mFragmentTransaction = fragmentManager.beginTransaction();
        boolean isNoNeedToReplace = false;
        if ((this.initialDialpadFragment instanceof DummyFragment) && currentTab == TabState.DIALER) {
            if (HwLog.HWDBG) {
                HwLog.d("Optimization", "creating original DialpadFragment");
            }
            this.mDialpadFragment = new DialpadFragment();
            if (this.mDialpadFragment != null) {
                if (HwLog.HWDBG) {
                    HwLog.d("Optimization", "replacing  DialpadFragment");
                }
                this.mFragmentTransaction.remove(this.initialDialpadFragment);
                this.mFragmentTransaction.add(R.id.tab_pager, this.mDialpadFragment, "tab-pager-dialer");
                this.mFragmentTransaction.hide(this.mDialpadFragment);
                showFragment(this.mFragmentTransaction, this.initialDialpadFragment);
                ((DummyFragment) this.initialDialpadFragment).setReplaced(true);
                isNoNeedToReplace = true;
            }
        }
        if ((this.intialFavorOrYPFragment instanceof DummyFragment) && currentTab == TabState.FAVOR_YELLOWPAGE) {
            if (HwLog.HWDBG) {
                HwLog.d("Optimization", "creating original FavorOrYPFragment");
            }
            this.mFavorOrYPFragment = this.mPeopleActivity.createFavorOrYpFragment();
            if (this.mFavorOrYPFragment != null) {
                if (HwLog.HWDBG) {
                    HwLog.d("Optimization", "replacing  FavorOrYPFragment");
                }
                this.mFragmentTransaction.remove(this.intialFavorOrYPFragment);
                this.mFragmentTransaction.add(R.id.tab_pager, this.mFavorOrYPFragment, "tab-pager-favor-yellow");
                this.mFragmentTransaction.hide(this.mFavorOrYPFragment);
                showFragment(this.mFragmentTransaction, this.intialFavorOrYPFragment);
                if (this.intialFavorOrYPFragment instanceof DummyFragment) {
                    ((DummyFragment) this.intialFavorOrYPFragment).setReplaced(true);
                }
                isNoNeedToReplace = true;
            }
        }
        if (this.initialAllFragment.isReplacable() && currentTab == TabState.ALL) {
            PLog.d(22, "FragmentReplacer replace DefaultContactBrowseListFragment");
            if (HwLog.HWDBG) {
                HwLog.d("Optimization", "creating original DefaultContactBrowseListFragment");
            }
            this.mAllFragment = new DefaultContactBrowseListFragment(null, false);
            this.mAllFragment.setDirectorySearchMode(this.initialAllFragment.getDirectorySearchMode());
            this.mAllFragment.setContactNameDisplayOrder(this.initialAllFragment.getContactNameDisplayOrder());
            this.mAllFragment.setSortOrder(this.initialAllFragment.getSortOrder());
            if (HwLog.HWDBG) {
                HwLog.d("Optimization", "replacing  DefaultContactBrowseListFragment");
            }
            if (this.initialAllFragment instanceof DummyContactBrowseListFragment) {
                this.mFragmentTransaction.remove(this.initialAllFragment);
                this.mFragmentTransaction.add(R.id.tab_pager, this.mAllFragment, "tab-pager-all");
                this.mFragmentTransaction.hide(this.mAllFragment);
                showFragment(this.mFragmentTransaction, this.initialAllFragment);
            }
            isNoNeedToReplace = true;
        }
        if (!isNoNeedToReplace) {
            return;
        }
        if (this.mPeopleActivity.isFinishing()) {
            if (HwLog.HWDBG) {
                HwLog.d("Optimization", "Activity is finishing so returning");
            }
            this.mCancel = true;
        } else if (this.mPeopleActivity.isSafeToCommitTransactions()) {
            this.mFragmentTransaction.commit();
            if (this.mDialpadFragment != null && currentTab == TabState.DIALER) {
                this.mPeopleActivity.setDialpadFragment(this.mDialpadFragment);
            }
            if (this.mFavorOrYPFragment != null && currentTab == TabState.FAVOR_YELLOWPAGE) {
                this.mPeopleActivity.setFavorOrYPFragment(this.mFavorOrYPFragment);
            }
            if (this.mAllFragment != null && currentTab == TabState.ALL) {
                this.mPeopleActivity.setAllFragment(this.mAllFragment);
            }
            this.mPeopleActivity.refreshPager();
        } else {
            this.mIsCommitPending = true;
            HwLog.e("Optimization", "TRANSACTION IS NOT COMMITED as its not safe to commit");
        }
    }

    public void cencal() {
        this.mCancel = true;
    }

    public boolean isCancelled() {
        return this.mCancel;
    }

    public synchronized void commitIfPending() {
        if (this.mIsCommitPending) {
            this.mIsCommitPending = false;
            HwLog.e("Optimization", "Commiting TRANSACTION now");
            this.mFragmentTransaction.commitAllowingStateLoss();
            if (this.mDialpadFragment != null) {
                this.mPeopleActivity.setDialpadFragment(this.mDialpadFragment);
            }
            if (this.mFavorOrYPFragment != null) {
                this.mPeopleActivity.setFavorOrYPFragment(this.mFavorOrYPFragment);
            }
            if (this.mAllFragment != null) {
                this.mPeopleActivity.setAllFragment(this.mAllFragment);
            }
            this.mPeopleActivity.refreshPager();
        }
    }
}
