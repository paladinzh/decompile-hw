package com.android.contacts.hap.rcs.list;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import com.android.contacts.hap.EmuiFeatureManager;
import com.android.contacts.hap.rcs.RcsContactsUtils;
import com.android.contacts.list.ContactEntryListFragment;
import com.android.contacts.util.HwLog;
import com.autonavi.amap.mapcore.VTMCDataCache;
import com.huawei.rcs.RCSServiceListener;
import com.huawei.rcs.capability.CapabilityService;
import com.huawei.rcs.commonInterface.IfMsgplusCb;
import com.huawei.rcs.commonInterface.IfMsgplusCb.Stub;

public class RcsContactEntryListFragment implements RCSServiceListener {
    private static boolean isScrolling = false;
    private static boolean sLastLoginStatus = false;
    private ContactEntryListFragment mFragment;
    public CapabilityService mRCS;
    private IfMsgplusCb mRcsCallback = new Stub() {
        public void handleEvent(int wEvent, Bundle bundle) {
            Message msg = RcsContactEntryListFragment.this.mRcseEventHandler.obtainMessage(wEvent);
            msg.obj = bundle;
            RcsContactEntryListFragment.this.mRcseEventHandler.sendMessage(msg);
        }
    };
    private Handler mRcseEventHandler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int status = msg.what;
            if (HwLog.HWDBG) {
                HwLog.d("RcsContactEntryListFragment", " handleMessage " + msg.what);
            }
            switch (status) {
                case 1502:
                    if (HwLog.HWDBG) {
                        HwLog.d("RcsContactEntryListFragment", "enter function [handleMessage], insert record to local database, UI refresh");
                    }
                    RcsContactEntryListFragment.this.reloadData();
                    return;
                default:
                    return;
            }
        }
    };
    private LoginStatusReceiver statusReceiver = null;

    public class LoginStatusReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            if (intent.getExtras() != null) {
                boolean newLoginStatus;
                int new_status = intent.getExtras().getInt("new_status");
                if (new_status == 1) {
                    newLoginStatus = true;
                    RcsContactEntryListFragment.this.reloadData();
                } else if (new_status == 2) {
                    newLoginStatus = false;
                    RcsContactEntryListFragment.this.reloadData();
                } else {
                    newLoginStatus = false;
                }
                if (HwLog.HWDBG) {
                    HwLog.d("RcsContactEntryListFragment", "login state changed, new_status is [" + new_status + "], newLoginStatus is [" + newLoginStatus + "], sLastLoginStatus is [" + RcsContactEntryListFragment.sLastLoginStatus + "]");
                }
                if (newLoginStatus == RcsContactEntryListFragment.sLastLoginStatus) {
                    if (HwLog.HWDBG) {
                        HwLog.d("RcsContactEntryListFragment", "login state changed, but not in the two cases, return");
                    }
                    return;
                }
                RcsContactEntryListFragment.sLastLoginStatus = newLoginStatus;
            }
        }
    }

    public void onServiceConnected() {
        reloadData();
    }

    public void handleCustomizationsOnStart(Context context, ContactEntryListFragment fragment) {
        if (EmuiFeatureManager.isRcsFeatureEnable()) {
            this.mRCS = CapabilityService.getInstance("contacts");
            this.mRCS.checkRcsServiceBind();
            this.mRCS.registerBindStatusListen(this);
            this.mRCS.setRcsCallBack(Integer.valueOf(1502), this.mRcsCallback);
            this.mFragment = fragment;
            if (this.statusReceiver == null) {
                this.statusReceiver = new LoginStatusReceiver();
            }
            IntentFilter statusFilter = new IntentFilter();
            statusFilter.addAction("com.huawei.rcs.loginstatus");
            if (context != null) {
                context.registerReceiver(this.statusReceiver, statusFilter, "com.huawei.rcs.RCS_BROADCASTER", null);
            }
        }
    }

    public void handleCustomizationsOnStop(Context context) {
        if (EmuiFeatureManager.isRcsFeatureEnable() && this.mRCS != null) {
            this.mRCS.removeRcsCallBack(Integer.valueOf(1502), this.mRcsCallback);
            this.mRCS.unRegisterBindStatusListen(this);
            if (!(this.statusReceiver == null || context == null)) {
                context.unregisterReceiver(this.statusReceiver);
            }
        }
    }

    private void reloadData() {
        if (this.mFragment != null) {
            this.mFragment.reloadData();
        }
    }

    public void handleCustomizationsForScroll(int scrollState) {
        if (EmuiFeatureManager.isRcsFeatureEnable()) {
            if (scrollState == 2 || scrollState == 1) {
                setScrollState(true);
            } else {
                setScrollState(false);
            }
            if (scrollState == 0 && this.mFragment != null && !this.mFragment.isSearchMode() && RcsContactsUtils.isRCSContactIconEnable() && sLastLoginStatus) {
                reloadData();
            }
        }
    }

    public static boolean getScrollState() {
        return isScrolling;
    }

    private static void setScrollState(boolean flag) {
        isScrolling = flag;
    }

    public void handleCustomizationsOnPartitionLoaded(ContactEntryListFragment fragment, Cursor cursor) {
        if (!(!EmuiFeatureManager.isRcsFeatureEnable() || fragment == null || fragment.getContactsRequest() == null || cursor == null || fragment.getContactsRequest().getActionCode() != VTMCDataCache.MAX_EXPIREDTIME)) {
            Bundle bundle = cursor.getExtras();
            if (bundle != null) {
                int[] counts = bundle.getIntArray("android.provider.extra.ADDRESS_BOOK_INDEX_COUNTS");
                if (counts != null && counts.length > 0) {
                    int iSize = 0;
                    for (int i : counts) {
                        iSize += i;
                    }
                    if (cursor.getCount() != iSize) {
                        counts[0] = counts[0] + (cursor.getCount() - iSize);
                        bundle.putIntArray("android.provider.extra.ADDRESS_BOOK_INDEX_COUNTS", counts);
                        if (HwLog.HWDBG) {
                            HwLog.d("RcsContactEntryListFragment", "enter function [handleCustomizationsOnPartitionLoaded] adjust the counts, before value is [" + iSize + "], after value is [" + cursor.getCount() + "]");
                        }
                    }
                }
            }
        }
    }
}
