package com.huawei.harassmentinterception.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import com.huawei.android.app.ActionBarEx;
import com.huawei.harassmentinterception.db.DBAdapter;
import com.huawei.harassmentinterception.util.PreferenceHelper;
import com.huawei.harassmentinterception.util.SmsInterceptionHelper;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.emui.activities.HsmFragmentActivity;
import com.huawei.systemmanager.hsmstat.HsmStat;
import com.huawei.systemmanager.util.HwLog;
import huawei.android.widget.SubTabWidget;
import huawei.android.widget.SubTabWidget.SubTab;
import huawei.support.v13.app.SubTabFragmentPagerAdapter;
import java.lang.ref.WeakReference;

public class InterceptionActivity extends HsmFragmentActivity {
    private static final int MSG_INIT_AFTER_DELAY = 1;
    private static final int MSG_UPDATE_UNREAD_COUNT = 2;
    private static final String TAG = "InterceptionActivity";
    private final OnClickListener mActionBarListener = new OnClickListener() {
        public void onClick(View v) {
            switch (v.getId()) {
                case 16908296:
                    InterceptionActivity.this.onSettingMenuSelected();
                    return;
                default:
                    return;
            }
        }
    };
    private ImageView mActionBarSettingImg = null;
    private CallFragment mCallFragment = null;
    private Handler mHandler = new MyHandler(this);
    private Intent mIntent = null;
    private boolean mIsPause = false;
    private MessageFragment mMsgFragment = null;
    private ViewPager mPagerView;
    private SubTab mSubTabCall = null;
    private SubTab mSubTabMsg = null;
    private View mWarningView = null;

    class FragmentPagerAdapter extends SubTabFragmentPagerAdapter {
        private int mSelectedPosition = 0;

        public FragmentPagerAdapter(Activity activity, ViewPager pager, SubTabWidget subTabWidget) {
            super(activity, pager, subTabWidget);
        }

        public void onPageSelected(int position) {
            this.mSelectedPosition = position;
            super.onPageSelected(position);
        }

        public void onPageScrollStateChanged(int state) {
            if (state == 0 && -1 != this.mSelectedPosition) {
                InterceptionActivity.this.updateFragmentSelectState(this.mSelectedPosition);
                this.mSelectedPosition = -1;
            }
            super.onPageScrollStateChanged(state);
        }
    }

    static class MyHandler extends Handler {
        WeakReference<InterceptionActivity> mActivity = null;

        MyHandler(InterceptionActivity activity) {
            this.mActivity = new WeakReference(activity);
        }

        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            InterceptionActivity activity = (InterceptionActivity) this.mActivity.get();
            if (activity != null) {
                switch (msg.what) {
                    case 1:
                        activity.doDelayedInit();
                        break;
                    case 2:
                        int nSelectedFragment = ((Integer) msg.obj).intValue();
                        if (nSelectedFragment != 1) {
                            if (nSelectedFragment == 0) {
                                activity.updateUnReadMsgCount(0);
                                break;
                            }
                        }
                        activity.updateUnReadCallCount(1);
                        break;
                        break;
                }
            }
        }
    }

    private void postDelayInitMsg(long lDelay) {
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(1), lDelay);
    }

    private void doDelayedInit() {
        NotificationManager notificationMgr = (NotificationManager) getSystemService("notification");
        if (notificationMgr != null) {
            notificationMgr.cancel(R.string.harassmentInterceptionNotificationManagerTitle);
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Utility.isOwnerUser()) {
            setContentView(R.layout.interception_fragment_activity);
            ActionBar actionBar = getActionBar();
            actionBar.setTitle(R.string.systemmanager_module_title_blocklist);
            ActionBarEx.setEndIcon(actionBar, true, getDrawable(R.drawable.settings_menu_btn_selector), this.mActionBarListener);
            ActionBarEx.setEndContentDescription(actionBar, getString(R.string.ActionBar_AddAppSettings_Title));
            actionBar.show();
            initFragments();
            return;
        }
        finish();
    }

    protected boolean shouldUpdateActionBarStyle() {
        return false;
    }

    private void initFragments() {
        int nSelectedFragment;
        boolean z;
        boolean z2 = true;
        SubTabWidget subTabWidget = (SubTabWidget) findViewById(R.id.subTab_layout);
        this.mPagerView = (ViewPager) findViewById(R.id.interception_records_fragment_container);
        FragmentPagerAdapter pageAdapter = new FragmentPagerAdapter(this, this.mPagerView, subTabWidget);
        this.mPagerView.setAdapter(pageAdapter);
        this.mWarningView = findViewById(R.id.warning_layout);
        Intent intent = getIntent();
        if (intent != null) {
            nSelectedFragment = intent.getIntExtra("showTabsNumber", 0);
        } else {
            nSelectedFragment = 0;
        }
        if (!(nSelectedFragment == 0 || 1 == nSelectedFragment)) {
            nSelectedFragment = 0;
        }
        this.mSubTabMsg = subTabWidget.newSubTab(getResources().getString(R.string.harassmentInterceptionMessageTitle));
        this.mMsgFragment = new MessageFragment();
        this.mSubTabMsg.setSubTabId(R.id.systemmanager_harassment_interception_message_title);
        SubTab subTab = this.mSubTabMsg;
        Fragment fragment = this.mMsgFragment;
        if (nSelectedFragment == 0) {
            z = true;
        } else {
            z = false;
        }
        pageAdapter.addSubTab(subTab, fragment, null, z);
        this.mSubTabCall = subTabWidget.newSubTab(getResources().getString(R.string.harassmentInterceptionCallTitle));
        this.mCallFragment = new CallFragment();
        this.mSubTabCall.setSubTabId(R.id.systemmanager_harassment_interception_call_title);
        SubTab subTab2 = this.mSubTabCall;
        Fragment fragment2 = this.mCallFragment;
        if (1 != nSelectedFragment) {
            z2 = false;
        }
        pageAdapter.addSubTab(subTab2, fragment2, null, z2);
    }

    protected void onNewIntent(Intent intent) {
        HsmStat.checkOnNewIntent(this, intent);
        this.mIntent = intent;
        super.onNewIntent(intent);
    }

    protected void onResume() {
        if (SmsInterceptionHelper.isDefaultSmsApp()) {
            this.mWarningView.setVisibility(8);
        } else {
            this.mWarningView.setVisibility(0);
        }
        initSubTabStatus();
        postDelayInitMsg(500);
        super.onResume();
    }

    protected void onPause() {
        this.mIntent = null;
        this.mIsPause = true;
        super.onPause();
    }

    protected void onDestroy() {
        super.onDestroy();
    }

    private void initSubTabStatus() {
        if (this.mIntent == null && !this.mIsPause) {
            this.mIntent = getIntent();
        }
        if (this.mIntent != null) {
            int nSelectedFragment = this.mIntent.getIntExtra("showTabsNumber", 0);
            HwLog.d(TAG, "initSubTabStatus, nSelectedFragment = " + nSelectedFragment);
            this.mPagerView.setCurrentItem(nSelectedFragment, false);
            updateUnReadCount(nSelectedFragment);
        }
    }

    private void updateFragmentSelectState(int nSelectedFragment) {
        HwLog.d(TAG, "updateFragmentSelectState, nSelectedFragment = " + nSelectedFragment);
        updateUnReadCount(nSelectedFragment);
        switch (nSelectedFragment) {
            case 0:
                refreshMessageFragment();
                HsmStat.statE(59);
                return;
            case 1:
                refreshCallFragment();
                HsmStat.statE(64);
                return;
            default:
                return;
        }
    }

    private void updateUnReadCount(int nSelectedFragment) {
        updateUnReadCallCount(nSelectedFragment);
        updateUnReadMsgCount(nSelectedFragment);
    }

    private void updateUnReadCallCount(int nSelectedFragment) {
        int unreadCallCount = DBAdapter.getUnreadCallCount(this);
        String subTabCallTitle = getResources().getString(R.string.harassmentInterceptionCallTitle);
        if (unreadCallCount != 0) {
            subTabCallTitle = subTabCallTitle + String.format("(%1$d)", new Object[]{Integer.valueOf(unreadCallCount)});
        }
        this.mSubTabCall.setText(subTabCallTitle);
        HwLog.d(TAG, "updateUnReadCount: unreadCallCount = " + unreadCallCount);
        if (nSelectedFragment == 1) {
            PreferenceHelper.setLastWatchCallTime(this);
        }
    }

    private void updateUnReadMsgCount(int nSelectedFragment) {
        int unreadMsgCount = DBAdapter.getUnreadMsgCount(this);
        String subTabMsgTitle = getResources().getString(R.string.harassmentInterceptionMessageTitle);
        if (unreadMsgCount != 0) {
            subTabMsgTitle = subTabMsgTitle + String.format("(%1$d)", new Object[]{Integer.valueOf(unreadMsgCount)});
        }
        this.mSubTabMsg.setText(subTabMsgTitle);
        HwLog.d(TAG, "updateUnReadCount: unreadMsgCount = " + unreadMsgCount);
        if (nSelectedFragment == 0) {
            PreferenceHelper.setLastWatchMessageTime(this);
        }
    }

    public void updateUnReadCountInTab(int nSelectedFragment) {
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(2, Integer.valueOf(nSelectedFragment)), 400);
    }

    public void delMessageByPhone(String phone) {
        if (this.mMsgFragment != null) {
            this.mMsgFragment.delMsgItemByPhone(phone);
        }
    }

    public void onSettingMenuSelected() {
        Intent intent = new Intent();
        intent.setClass(this, RuleSettingsActivity.class);
        startActivity(intent);
    }

    public void refreshCallFragment() {
        FragmentManager fragmentManager = getFragmentManager();
        if (this.mCallFragment == null) {
            return;
        }
        if (this.mCallFragment.getTag() != null) {
            this.mCallFragment.refreshCallList();
            return;
        }
        Fragment fragment = fragmentManager.findFragmentByTag(makeFragmentName(this.mPagerView.getId(), 1));
        if (fragment != null) {
            ((CallFragment) fragment).refreshCallList();
        }
    }

    public void refreshMessageFragment() {
        FragmentManager fragmentManager = getFragmentManager();
        if (this.mMsgFragment == null) {
            return;
        }
        if (this.mMsgFragment.getTag() != null) {
            this.mMsgFragment.refreshMsgList();
            return;
        }
        Fragment fragment = fragmentManager.findFragmentByTag(makeFragmentName(this.mPagerView.getId(), 0));
        if (fragment != null) {
            ((MessageFragment) fragment).refreshMsgList();
        }
    }

    private String makeFragmentName(int viewId, int index) {
        return "android:switcher:" + viewId + ":" + index;
    }

    public int getCurrentFragment() {
        return this.mPagerView.getCurrentItem();
    }
}
