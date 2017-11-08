package com.android.settings.applications;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import com.android.settings.ItemUseStat;
import com.android.settingslib.drawer.SettingsDrawerActivity;
import huawei.android.widget.SubTabWidget;
import huawei.android.widget.SubTabWidget.SubTab;
import huawei.support.v13.app.SubTabFragmentPagerAdapter;

public class ProcessStatsSummaryActivity extends SettingsDrawerActivity {
    private static final String[] APP_DURATION_KEY = new String[]{"app_duration_3_hours", "app_duration_6_hours", "app_duration_12_hours", "app_duration_1_day"};
    private static final int[] DURATION_COUNT = new int[]{3, 6, 12, 1};
    private static final String[] DURATION_KEY = new String[]{"progress_duration_3_hours", "progress_duration_6_hours", "progress_duration_12_hours", "progress_duration_1_day"};
    private static final int[] TAB_INDEX = new int[]{0, 1, 2, 3};
    private boolean isFirstLoaded = true;
    private boolean isFromProcessStatusUi = false;
    private int mCurrentSelectedPageIndex = 0;
    private FragmentManager mFragmentManager;
    private MenuItem mMenuAvg;
    private MenuItem mMenuMax;
    private MyPagerAdapter mMyPageAdapter;
    private int mPageIndex = 0;
    private boolean mShowMax = false;
    SubTabFragmentPagerAdapter mSubTabFragmentPagerAdapter;
    SubTabWidget mSubTabWidget;
    private ViewPager mViewPager;
    protected final int[] sDurationLabels_ex = new int[]{2131627249, 2131627250, 2131627251, 2131627252};

    private class MyPageChangeListener implements OnPageChangeListener {
        public void onPageSelected(int position) {
            ProcessStatsSummaryActivity.this.mPageIndex = position;
            ProcessStatsSummaryActivity.this.mSubTabWidget.selectSubTab(ProcessStatsSummaryActivity.this.mSubTabWidget.getSubTabAt(position));
            if (ProcessStatsSummaryActivity.this.isFirstLoaded) {
                ProcessStatsSummaryActivity.this.isFirstLoaded = false;
            } else {
                ItemUseStat.getInstance().handleClick(ProcessStatsSummaryActivity.this.getApplicationContext(), 2, ProcessStatsSummaryActivity.this.isFromProcessStatusUi ? ProcessStatsSummaryActivity.APP_DURATION_KEY[position] : ProcessStatsSummaryActivity.DURATION_KEY[position]);
            }
        }

        public void onPageScrollStateChanged(int state) {
        }

        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }
    }

    class MyPagerAdapter extends PagerAdapter {
        private FragmentTransaction mCurTransaction = null;
        private Fragment[] mFragments = new Fragment[ProcessStatsSummaryActivity.TAB_INDEX.length];

        MyPagerAdapter() {
        }

        public int getCount() {
            return this.mFragments.length;
        }

        public void addFragment(int index, Fragment fragment) {
            if (index >= 0 && index <= this.mFragments.length) {
                this.mFragments[index] = fragment;
            }
        }

        public Object instantiateItem(ViewGroup container, int position) {
            if (this.mCurTransaction == null) {
                this.mCurTransaction = ProcessStatsSummaryActivity.this.mFragmentManager.beginTransaction();
            }
            Fragment fragment = this.mFragments[position];
            this.mCurTransaction.show(fragment);
            return fragment;
        }

        public void destroyItem(View view, int position, Object object) {
            if (this.mCurTransaction == null) {
                this.mCurTransaction = ProcessStatsSummaryActivity.this.mFragmentManager.beginTransaction();
            }
            this.mCurTransaction.hide((Fragment) object);
        }

        public void finishUpdate(View view) {
            if (this.mCurTransaction != null) {
                this.mCurTransaction.commitAllowingStateLoss();
                this.mCurTransaction = null;
                ProcessStatsSummaryActivity.this.mFragmentManager.executePendingTransactions();
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
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(2130968750);
        Intent intent = getIntent();
        if (intent != null) {
            this.isFromProcessStatusUi = intent.getBooleanExtra("fragment_stats", false);
            this.mCurrentSelectedPageIndex = intent.getIntExtra("fragment_index", 0);
            this.mPageIndex = this.mCurrentSelectedPageIndex;
        }
        new Handler().postDelayed(new Runnable() {
            public void run() {
                ProcessStatsSummaryActivity.this.initActionBars();
            }
        }, 100);
        initViews();
    }

    private void initActionBars() {
        ActionBar actionBar = getActionBar();
        actionBar.setTitle(2131626963);
        if (this.isFromProcessStatusUi) {
            actionBar.setTitle(2131627015);
        }
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.show();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        if (!this.isFromProcessStatusUi) {
            return false;
        }
        this.mMenuAvg = menu.add(0, 1, 0, 2131627019);
        this.mMenuAvg.setShowAsAction(0);
        this.mMenuMax = menu.add(0, 2, 0, 2131627020);
        this.mMenuMax.setShowAsAction(0);
        updateMenu();
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 1:
            case 2:
                if (this.isFromProcessStatusUi) {
                    boolean z;
                    if (this.mShowMax) {
                        z = false;
                    } else {
                        z = true;
                    }
                    this.mShowMax = z;
                    for (int valueOf : TAB_INDEX) {
                        ProcessStatsBase fragment = (ProcessStatsBase) this.mFragmentManager.findFragmentByTag(String.valueOf(valueOf));
                        Log.d("ProcessMemory:", "onOptionsItemSelected" + String.valueOf(this.mPageIndex));
                        Log.d("ProcessMemory:", "setIsShowMax" + String.valueOf(true));
                        ItemUseStat.getInstance().handleClick(getApplicationContext(), 2, this.mShowMax ? "progress_show_max" : "progress_show_avg");
                        if (fragment != null) {
                            fragment.setIsShowMax(this.mShowMax);
                            fragment.refreshUi();
                        }
                    }
                    updateMenu();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateMenu() {
        this.mMenuMax.setVisible(!this.mShowMax);
        this.mMenuAvg.setVisible(this.mShowMax);
    }

    private void initViews() {
        int i;
        this.mViewPager = (ViewPager) findViewById(2131886522);
        this.mSubTabWidget = (SubTabWidget) findViewById(2131886521);
        this.mSubTabFragmentPagerAdapter = new SubTabFragmentPagerAdapter(this, this.mViewPager, this.mSubTabWidget);
        this.mMyPageAdapter = new MyPagerAdapter();
        this.mViewPager.setAdapter(this.mMyPageAdapter);
        this.mViewPager.setOnPageChangeListener(new MyPageChangeListener());
        this.mFragmentManager = getFragmentManager();
        FragmentTransaction ft = this.mFragmentManager.beginTransaction();
        for (int valueOf : TAB_INDEX) {
            int valueOf2;
            Fragment fragment = this.mFragmentManager.findFragmentByTag(String.valueOf(valueOf2));
            if (fragment != null) {
                ft.remove(fragment);
            }
        }
        for (i = 0; i < TAB_INDEX.length; i++) {
            ProcessStatsBase processStatsSummaryFragment;
            boolean z;
            if (this.isFromProcessStatusUi) {
                processStatsSummaryFragment = new ProcessStatsUi();
            } else {
                processStatsSummaryFragment = new ProcessStatsSummary();
            }
            processStatsSummaryFragment.setDurationIndex(i);
            processStatsSummaryFragment.setCurrentFragmentTag(this.isFromProcessStatusUi);
            processStatsSummaryFragment.setIsShowMax(this.mShowMax);
            ft.add(2131886522, processStatsSummaryFragment, String.valueOf(TAB_INDEX[i]));
            this.mMyPageAdapter.addFragment(i, processStatsSummaryFragment);
            if (i != 0) {
                ft.hide(processStatsSummaryFragment);
            }
            SubTab subTab = this.mSubTabWidget.newSubTab(String.format(getResources().getString(this.sDurationLabels_ex[i]), new Object[]{Integer.valueOf(DURATION_COUNT[i])}));
            subTab.setSubTabId(2131886105);
            SubTabFragmentPagerAdapter subTabFragmentPagerAdapter = this.mSubTabFragmentPagerAdapter;
            valueOf2 = TAB_INDEX[i];
            if (this.mCurrentSelectedPageIndex == i) {
                z = true;
            } else {
                z = false;
            }
            subTabFragmentPagerAdapter.addSubTab(subTab, valueOf2, processStatsSummaryFragment, null, z);
        }
        ft.commitAllowingStateLoss();
        this.mFragmentManager.executePendingTransactions();
        this.mViewPager.setCurrentItem(this.mCurrentSelectedPageIndex);
    }
}
