package com.android.settings;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ActivityManagerNative;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import huawei.android.widget.SubTabWidget;
import huawei.android.widget.SubTabWidget.SubTab;
import huawei.support.v13.app.SubTabFragmentPagerAdapter;

public class DisplayModeSettingsActivity extends Activity {
    private static final int[] TAB_INDEX = new int[]{0, 1, 2};
    private int mAppliedMode = -1;
    private int mCurrentSelectedPageIndex = -1;
    private FragmentManager mFragmentManager;
    private Handler mMyHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    Intent reboot = new Intent("android.intent.action.REBOOT");
                    reboot.putExtra("android.intent.extra.KEY_CONFIRM", false);
                    reboot.setFlags(268435456);
                    DisplayModeSettingsActivity.this.startActivity(reboot);
                    return;
                default:
                    return;
            }
        }
    };
    private MyPagerAdapter mMyPageAdapter;
    private AlertDialog mNoticeDialog;
    private AlertDialog mRebootConfirmDialog;
    SubTabFragmentPagerAdapter mSubTabFragmentPagerAdapter;
    SubTabWidget mSubTabWidget;
    private ViewPager mViewPager;

    private class MyPageChangeListener implements OnPageChangeListener {
        public void onPageSelected(int position) {
            DisplayModeSettingsActivity.this.mCurrentSelectedPageIndex = position;
            DisplayModeSettingsActivity.this.mSubTabWidget.selectSubTab(DisplayModeSettingsActivity.this.mSubTabWidget.getSubTabAt(position));
            DisplayModeSettingsActivity.this.invalidateOptionsMenu();
        }

        public void onPageScrollStateChanged(int state) {
        }

        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }
    }

    class MyPagerAdapter extends PagerAdapter {
        private FragmentTransaction mCurTransaction = null;
        private Fragment[] mFragments = new Fragment[DisplayModeSettingsActivity.TAB_INDEX.length];

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
                this.mCurTransaction = DisplayModeSettingsActivity.this.mFragmentManager.beginTransaction();
            }
            Fragment fragment = this.mFragments[position];
            this.mCurTransaction.show(fragment);
            return fragment;
        }

        public void destroyItem(View view, int position, Object object) {
            if (this.mCurTransaction == null) {
                this.mCurTransaction = DisplayModeSettingsActivity.this.mFragmentManager.beginTransaction();
            }
            this.mCurTransaction.hide((Fragment) object);
        }

        public void finishUpdate(View view) {
            if (this.mCurTransaction != null) {
                this.mCurTransaction.commitAllowingStateLoss();
                this.mCurTransaction = null;
                DisplayModeSettingsActivity.this.mFragmentManager.executePendingTransactions();
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
        if (!Utils.isTablet()) {
            setRequestedOrientation(1);
        }
        setContentView(2130968750);
        this.mAppliedMode = Utils.getCurrentDisplayModeIndex(getContentResolver(), this);
        this.mCurrentSelectedPageIndex = this.mAppliedMode;
        initActionBars();
        initViews();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        boolean z = true;
        MenuItem add = menu.add(0, 1, 0, 2131627449);
        if (this.mCurrentSelectedPageIndex == this.mAppliedMode) {
            z = false;
        }
        add.setEnabled(z).setIcon(SettingsExtUtils.getAlphaStateListDrawable(getResources(), 2130838278)).setShowAsAction(2);
        menu.add(0, 2, 0, 2131627944).setIcon(SettingsExtUtils.getAlphaStateListDrawable(getResources(), 2130838282)).setShowAsAction(2);
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 1:
                if (this.mCurrentSelectedPageIndex != this.mAppliedMode) {
                    showRebootConfirmDialog();
                    ItemUseStat.getInstance().handleClick(this, 2, "apply_display_mode");
                    break;
                }
                break;
            case 2:
                showDisplaymodeNotice();
                break;
            case 16908332:
                finish();
                break;
        }
        return true;
    }

    public void onDestroy() {
        super.onDestroy();
        if (this.mNoticeDialog != null) {
            this.mNoticeDialog.dismiss();
        }
        if (this.mRebootConfirmDialog != null) {
            this.mRebootConfirmDialog.dismiss();
        }
    }

    private void showDisplaymodeNotice() {
        Builder builder = new Builder(this);
        builder.setTitle(2131627944);
        builder.setMessage(getString(2131627947));
        builder.setPositiveButton(2131627945, null);
        this.mNoticeDialog = builder.show();
    }

    private void showRebootConfirmDialog() {
        Builder builder = new Builder(this);
        builder.setTitle(2131627359);
        builder.setMessage(getString(2131627946));
        builder.setNegativeButton(2131624572, null);
        builder.setPositiveButton(2131625656, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                DisplayModeSettingsActivity.this.changeDisplayMode();
            }
        });
        this.mRebootConfirmDialog = builder.show();
    }

    private void changeDisplayMode() {
        if (!Utils.isMonkeyRunning()) {
            String appliedMode = Utils.getActualDpiArrayForDevice(this)[this.mCurrentSelectedPageIndex];
            try {
                Configuration curConfig = new Configuration();
                curConfig.updateFrom(ActivityManagerNative.getDefault().getConfiguration());
                curConfig.extraConfig.setDensityDPI(Integer.parseInt(appliedMode));
                Log.d("DisplayModeSettingsActivity", "Put display mode: " + appliedMode);
            } catch (RemoteException e) {
                Log.e("DisplayModeSettingsActivity", "Unable to set dpi scale");
            } catch (Exception e2) {
                Log.e("DisplayModeSettingsActivity", "get configuration error, error msg: " + e2.getMessage());
            }
            if (this.mCurrentSelectedPageIndex == 0) {
                ItemUseStat.getInstance().handleClick(this, 2, "display_mode_small");
            } else if (1 == this.mCurrentSelectedPageIndex) {
                ItemUseStat.getInstance().handleClick(this, 2, "display_mode_middle");
            } else if (2 == this.mCurrentSelectedPageIndex) {
                ItemUseStat.getInstance().handleClick(this, 2, "display_mode_big");
            }
            this.mMyHandler.sendMessageDelayed(this.mMyHandler.obtainMessage(1), 1000);
        }
    }

    private void initActionBars() {
        ActionBar actionBar = getActionBar();
        actionBar.setTitle(2131627943);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.show();
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
            Fragment fragment = this.mFragmentManager.findFragmentByTag(String.valueOf(valueOf));
            if (fragment != null) {
                ft.remove(fragment);
            }
        }
        for (i = 0; i < TAB_INDEX.length; i++) {
            DisplayModeSettingsFragment displayModeSettingsfragment = new DisplayModeSettingsFragment();
            displayModeSettingsfragment.setPageIndex(i);
            ft.add(2131886522, displayModeSettingsfragment, String.valueOf(TAB_INDEX[i]));
            this.mMyPageAdapter.addFragment(i, displayModeSettingsfragment);
            if (i != this.mAppliedMode) {
                ft.hide(displayModeSettingsfragment);
            }
            SubTab subTab = this.mSubTabWidget.newSubTab(getResources().getStringArray(2131361943)[i]);
            subTab.setSubTabId(2131886100);
            if (i == this.mAppliedMode) {
                this.mSubTabFragmentPagerAdapter.addSubTab(subTab, TAB_INDEX[i], displayModeSettingsfragment, null, true);
            } else {
                this.mSubTabFragmentPagerAdapter.addSubTab(subTab, TAB_INDEX[i], displayModeSettingsfragment, null, false);
            }
        }
        ft.commitAllowingStateLoss();
        this.mFragmentManager.executePendingTransactions();
        this.mViewPager.setCurrentItem(this.mAppliedMode);
    }
}
