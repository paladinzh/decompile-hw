package com.android.settings.deviceinfo;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.provider.Settings.Global;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import com.android.settings.Utils;
import com.android.settings.ViewPagerEx;
import com.android.settingslib.drawer.SettingsDrawerActivity;
import huawei.android.widget.SubTabWidget;
import huawei.android.widget.SubTabWidget.SubTab;
import huawei.support.v13.app.SubTabFragmentPagerAdapter;

public class MSimSubscriptionStatusTab extends SettingsDrawerActivity {
    private ActionBar mActionBar;
    private int mCurrentSelectedPageIndex = -1;
    private FragmentManager mFragmentManager;
    private BroadcastReceiver mReceiver;
    private SimCardsAdapter mSimCardsAdapter;
    SubTabFragmentPagerAdapter mSubTabFragmentPagerAdapter;
    SubTabWidget mSubTabWidget;
    private ViewPagerEx mViewPager;
    private int subscription = 0;

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
                this.mCurTransaction = MSimSubscriptionStatusTab.this.mFragmentManager.beginTransaction();
            }
            Fragment fragment = this.mFragments[position];
            this.mCurTransaction.show(fragment);
            return fragment;
        }

        public void destroyItem(View view, int position, Object object) {
            if (this.mCurTransaction == null) {
                this.mCurTransaction = MSimSubscriptionStatusTab.this.mFragmentManager.beginTransaction();
            }
            this.mCurTransaction.hide((Fragment) object);
        }

        public void finishUpdate(View view) {
            if (this.mCurTransaction != null) {
                this.mCurTransaction.commitAllowingStateLoss();
                this.mCurTransaction = null;
                MSimSubscriptionStatusTab.this.mFragmentManager.executePendingTransactions();
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
            MSimSubscriptionStatusTab.this.mCurrentSelectedPageIndex = position;
            MSimSubscriptionStatusTab.this.mSubTabWidget.selectSubTab(MSimSubscriptionStatusTab.this.mSubTabWidget.getSubTabAt(position));
        }

        public void onPageScrollStateChanged(int state) {
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(2130968872);
        this.mActionBar = getActionBar();
        if (isAirplaneModeOn() || !Utils.isMultiSimEnabled()) {
            finish();
            return;
        }
        int sub = getIntent().getIntExtra("subscription", -1);
        if (sub != -1) {
            this.subscription = sub;
        }
        if (savedInstanceState != null) {
            this.subscription = savedInstanceState.getInt("subscription");
        }
        if (this.mActionBar != null) {
            createFragments(this.subscription);
            this.mActionBar.setTitle(2131625246);
            this.mActionBar.setDisplayHomeAsUpEnabled(true);
            this.mViewPager.setCurrentItem(this.subscription);
            this.mReceiver = new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();
                    if (action != null && action.equals("android.intent.action.AIRPLANE_MODE") && intent.getBooleanExtra("state", false)) {
                        MSimSubscriptionStatusTab.this.finish();
                    }
                }
            };
            registerReceiver(this.mReceiver, new IntentFilter("android.intent.action.AIRPLANE_MODE"));
        }
        if (!(Utils.isCardReady(0) || Utils.isCardReady(1))) {
            finish();
        }
    }

    private boolean isAirplaneModeOn() {
        return Global.getInt(getContentResolver(), "airplane_mode_on", 0) != 0;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() != 16908332) {
            return super.onOptionsItemSelected(item);
        }
        finish();
        return true;
    }

    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt("subscription", this.mCurrentSelectedPageIndex);
    }

    protected void onResume() {
        super.onResume();
    }

    protected void onDestroy() {
        if (this.mReceiver != null) {
            unregisterReceiver(this.mReceiver);
        }
        super.onDestroy();
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
        MSimSubscriptionStatusTabFragment fragment1 = new MSimSubscriptionStatusTabFragment(0);
        ft.add(2131886256, fragment1, "card_1");
        this.mSimCardsAdapter.addFragment(0, fragment1);
        SubTab subTab1 = this.mSubTabWidget.newSubTab(String.format(getResources().getString(2131627388), new Object[]{Integer.valueOf(1)}));
        subTab1.setSubTabId(2131886103);
        this.mSubTabFragmentPagerAdapter.addSubTab(subTab1, 0, fragment1, null, subscription == 0);
        MSimSubscriptionStatusTabFragment fragment2 = new MSimSubscriptionStatusTabFragment(1);
        ft.add(2131886256, fragment2, "card_2");
        this.mSimCardsAdapter.addFragment(1, fragment2);
        SubTab subTab2 = this.mSubTabWidget.newSubTab(String.format(getResources().getString(2131627389), new Object[]{Integer.valueOf(2)}));
        subTab2.setSubTabId(2131886104);
        this.mSubTabFragmentPagerAdapter.addSubTab(subTab2, 1, fragment2, null, subscription == 1);
        ft.hide(fragment2);
        ft.commitAllowingStateLoss();
        this.mFragmentManager.executePendingTransactions();
    }
}
