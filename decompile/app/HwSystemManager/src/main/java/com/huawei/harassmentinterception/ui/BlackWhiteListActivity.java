package com.huawei.harassmentinterception.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import com.huawei.harassmentinterception.common.ConstValues;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.emui.activities.HsmActivity;
import com.huawei.systemmanager.util.HwLog;
import huawei.android.widget.SubTabWidget;
import huawei.android.widget.SubTabWidget.SubTab;
import huawei.support.v13.app.SubTabFragmentPagerAdapter;

public class BlackWhiteListActivity extends HsmActivity {
    private static final String TAG = BlackWhiteListActivity.class.getSimpleName();
    private ActionBar mActionBar;
    private BlacklistFragment mBlackListFragment;
    private boolean mIsTabChange = false;
    private int mSelectedFragment = 0;
    private SubTab mSubTabBlackList = null;
    private SubTab mSubTabWhiteList = null;
    private WhitelistFragment mWhitelistFragment;

    class FragmentPagerAdapter extends SubTabFragmentPagerAdapter {
        private int mSelectedPosition = 0;

        public FragmentPagerAdapter(Activity activity, ViewPager pager, SubTabWidget subTabWidget) {
            super(activity, pager, subTabWidget);
        }

        public void onPageSelected(int position) {
            BlackWhiteListActivity.this.mIsTabChange = true;
            this.mSelectedPosition = position;
            super.onPageSelected(position);
        }

        public void onPageScrollStateChanged(int state) {
            if (state == 0 && -1 != this.mSelectedPosition) {
                BlackWhiteListActivity.this.updateFragmentSelectState(this.mSelectedPosition);
                this.mSelectedPosition = -1;
            }
            super.onPageScrollStateChanged(state);
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.blackwhite_fragment_activity);
        setUpActionBar();
        initFragments();
    }

    protected boolean shouldUpdateActionBarStyle() {
        return false;
    }

    public boolean getTabChangeStatus() {
        return this.mIsTabChange;
    }

    public void resetTabChangeStatus() {
        this.mIsTabChange = false;
    }

    public int getSelectedFragment() {
        return this.mSelectedFragment;
    }

    private void initFragments() {
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        this.mSelectedFragment = getTabIndexFromIntent();
        if (this.mSelectedFragment == 0) {
            if (this.mBlackListFragment == null) {
                setTitle(R.string.harassmentInterception_blacklist_number);
                getActionBar().setTitle(R.string.harassmentInterception_blacklist_number);
                this.mBlackListFragment = new BlacklistFragment();
                fragmentTransaction.replace(R.id.contentFrame, this.mBlackListFragment);
                fragmentTransaction.commit();
            }
        } else if (this.mSelectedFragment == 1 && this.mWhitelistFragment == null) {
            setTitle(R.string.harassmentInterception_whitelist_number);
            getActionBar().setTitle(R.string.harassmentInterception_whitelist_number);
            this.mWhitelistFragment = new WhitelistFragment();
            fragmentTransaction.replace(R.id.contentFrame, this.mWhitelistFragment);
            fragmentTransaction.commit();
        }
    }

    protected void onResume() {
        super.onResume();
        HwLog.i(TAG, "onResume is called");
    }

    private void setUpActionBar() {
        this.mActionBar = getActionBar();
        this.mActionBar.setDisplayHomeAsUpEnabled(true);
    }

    private int getTabIndexFromIntent() {
        Intent intent = getIntent();
        if (intent == null) {
            return 0;
        }
        return intent.getIntExtra(ConstValues.KEY_BLACKLIST_ENTRANCE_FLAG, 0);
    }

    private void updateFragmentSelectState(int nSelectedFragment) {
        HwLog.d(TAG, "updateFragmentSelectState, nSelectedFragment = " + nSelectedFragment);
        this.mSelectedFragment = nSelectedFragment;
        switch (nSelectedFragment) {
            case 0:
                this.mBlackListFragment.refreshBlackList();
                return;
            case 1:
                this.mWhitelistFragment.refreshWhiteList();
                return;
            default:
                return;
        }
    }
}
