package com.android.settings;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import com.android.settingslib.drawer.SettingsDrawerActivity;
import huawei.android.widget.SubTabWidget;
import huawei.android.widget.SubTabWidget.SubTab;
import huawei.support.v13.app.SubTabFragmentPagerAdapter;

public class MSimIccLockSettings extends SettingsDrawerActivity {
    private ActionBar mActionBar;
    private int mCurrentSelectedPageIndex = -1;
    private FragmentManager mFragmentManager;
    private BroadcastReceiver mReceiver;
    private SimCardsAdapter mSimCardsAdapter;
    SubTabFragmentPagerAdapter mSubTabFragmentPagerAdapter;
    SubTabWidget mSubTabWidget;
    private ViewPagerEx mViewPager;

    class SimCardsAdapter extends PagerAdapter implements OnPageChangeListener {
        private FragmentTransaction mCurTransaction = null;
        private Fragment[] mFragments = new Fragment[2];

        SimCardsAdapter() {
        }

        public int getCount() {
            return 2;
        }

        public void addFragment(int index, Fragment fragment) {
            if (index >= 0 && index <= this.mFragments.length) {
                this.mFragments[index] = fragment;
            }
        }

        public Object instantiateItem(ViewGroup container, int position) {
            if (this.mCurTransaction == null) {
                this.mCurTransaction = MSimIccLockSettings.this.mFragmentManager.beginTransaction();
            }
            Fragment fragment = this.mFragments[position];
            this.mCurTransaction.show(fragment);
            return fragment;
        }

        public void destroyItem(View view, int position, Object object) {
            if (this.mCurTransaction == null) {
                this.mCurTransaction = MSimIccLockSettings.this.mFragmentManager.beginTransaction();
            }
            this.mCurTransaction.hide((Fragment) object);
        }

        public void finishUpdate(View view) {
            if (this.mCurTransaction != null) {
                this.mCurTransaction.commitAllowingStateLoss();
                this.mCurTransaction = null;
                MSimIccLockSettings.this.mFragmentManager.executePendingTransactions();
            }
        }

        public int getItemPosition(Object object) {
            return -2;
        }

        public boolean isViewFromObject(View arg0, Object arg1) {
            if (((Fragment) arg1).getView() == arg0) {
                return true;
            }
            return false;
        }

        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }

        public void onPageSelected(int position) {
            MSimIccLockSettings.this.selectTab(position);
        }

        public void onPageScrollStateChanged(int state) {
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Utils.isMonkeyRunning()) {
            finish();
            return;
        }
        Window win = getWindow();
        win.clearFlags(67108864);
        win.addFlags(Integer.MIN_VALUE);
        setContentView(2130968872);
        if (savedInstanceState != null) {
            int subscription = savedInstanceState.getInt("subscription");
        }
        this.mActionBar = getActionBar();
        if (this.mActionBar != null) {
            this.mActionBar.setTitle(SecuritySettingsHwBase.getSimLockTitleForMultiSim(this));
            this.mActionBar.setDisplayHomeAsUpEnabled(true);
            boolean card1Present = Utils.isSimCardPresent(0);
            boolean card2Present = Utils.isSimCardPresent(1);
            if (!card2Present || card1Present) {
                subscription = 0;
            } else {
                subscription = 1;
            }
            createFragments(subscription);
            selectTab(subscription);
            if ((card1Present || card2Present) && -1 != subscription) {
                selectTab(subscription);
            }
            this.mReceiver = new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {
                    if ("android.intent.action.AIRPLANE_MODE".equals(intent.getAction()) && intent.getBooleanExtra("state", false)) {
                        MSimIccLockSettings.this.finish();
                    }
                }
            };
            registerReceiver(this.mReceiver, new IntentFilter("android.intent.action.AIRPLANE_MODE"));
        }
    }

    public void onDestroy() {
        if (this.mReceiver != null) {
            unregisterReceiver(this.mReceiver);
        }
        super.onDestroy();
    }

    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt("subscription", this.mCurrentSelectedPageIndex);
    }

    private void createFragments(int subscription) {
        this.mViewPager = (ViewPagerEx) findViewById(2131886256);
        this.mSubTabWidget = (SubTabWidget) findViewById(2131886521);
        this.mSubTabFragmentPagerAdapter = new SubTabFragmentPagerAdapter(this, this.mViewPager, this.mSubTabWidget);
        this.mSimCardsAdapter = new SimCardsAdapter();
        this.mViewPager.setAdapter(this.mSimCardsAdapter);
        this.mViewPager.setOnPageChangeListener(this.mSimCardsAdapter);
        this.mFragmentManager = getFragmentManager();
        FragmentTransaction ft = this.mFragmentManager.beginTransaction();
        Fragment fragment = this.mFragmentManager.findFragmentByTag("card_1");
        if (fragment != null) {
            ft.remove(fragment);
        }
        fragment = this.mFragmentManager.findFragmentByTag("card_2");
        if (fragment != null) {
            ft.remove(fragment);
        }
        String card1Title = getResources().getString(2131627386);
        String card2Title = getResources().getString(2131627387);
        MSimIccLockSettingsFragment fragment1 = new MSimIccLockSettingsFragment(0);
        ft.add(2131886256, fragment1, "card_1");
        this.mSimCardsAdapter.addFragment(0, fragment1);
        SubTab subTab1 = this.mSubTabWidget.newSubTab(card1Title);
        subTab1.setSubTabId(2131886101);
        this.mSubTabFragmentPagerAdapter.addSubTab(subTab1, 0, fragment1, null, subscription == 0);
        MSimIccLockSettingsFragment fragment2 = new MSimIccLockSettingsFragment(1);
        ft.add(2131886256, fragment2, "card_2");
        this.mSimCardsAdapter.addFragment(1, fragment2);
        SubTab subTab2 = this.mSubTabWidget.newSubTab(card2Title);
        subTab2.setSubTabId(2131886102);
        this.mSubTabFragmentPagerAdapter.addSubTab(subTab2, 1, fragment2, null, subscription == 1);
        ft.hide(fragment2);
        ft.commitAllowingStateLoss();
        this.mFragmentManager.executePendingTransactions();
    }

    private void selectTab(int position) {
        this.mCurrentSelectedPageIndex = position;
        this.mSubTabWidget.selectSubTab(this.mSubTabWidget.getSubTabAt(position));
    }
}
