package com.huawei.netassistant.analyse;

import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkTemplate;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.SparseArray;
import com.huawei.android.app.KeyguardManagerEx;
import com.huawei.netassistant.calculator.CalculateTrafficManager;
import com.huawei.netassistant.cardmanager.SimCardManager;
import com.huawei.netassistant.common.ParcelableAppItem;
import com.huawei.netassistant.util.CommonMethodUtil;
import com.huawei.netassistant.util.DateUtil;
import com.huawei.netassistant.util.ExternMethodUtil;
import com.huawei.netassistant.util.NotificationUtil;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.netassistant.traffic.statusspeed.NatSettingInfo;
import com.huawei.systemmanager.util.HwLog;
import java.util.ArrayList;
import java.util.Collections;

public class TrafficNotifyAfterLocked extends TrafficNotifyDecorator {
    private static final int MSG_FREQ_TIME = 4000;
    private static final int MSG_LOCK = 1;
    private static final int MSG_UNLOCK = 2;
    private static final long SCREEN_LOCKED_TRAFFIC_LIMIT = 0;
    public static final long SCREEN_LOCK_NO_CHEK_DELAY = 300000;
    private static final String TAG = "TrafficNotifyAfterLocked";
    private static long mLastLockEndTime;
    private static long mLastLockStartTime;
    private static long mLockStartTraffic = 0;
    private static SparseArray<ParcelableAppItem> mLockStartTrafficList;
    private static TrafficNotifyAfterLocked mSingleton;
    private CalculateTrafficManager mCTM = CalculateTrafficManager.getInstance();
    private boolean mIsSameLockPeriod = false;
    private ArrayList<ParcelableAppItem> mItemsListInScreenLocking;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (context != null && intent != null && !TextUtils.isEmpty(intent.getAction())) {
                String action = intent.getAction();
                Message msg;
                if ("android.intent.action.SCREEN_OFF".equals(action)) {
                    if (!(TrafficNotifyAfterLocked.this.mIsSameLockPeriod || KeyguardManagerEx.getDefault(context).isLockScreenDisabled())) {
                        msg = TrafficNotifyAfterLocked.this.myLockingCheckHandler.obtainMessage();
                        msg.what = 1;
                        msg.sendToTarget();
                        TrafficNotifyAfterLocked.this.mIsSameLockPeriod = true;
                    }
                } else if ("android.intent.action.USER_PRESENT".equals(action)) {
                    if (!KeyguardManagerEx.getDefault(context).isLockScreenDisabled()) {
                        msg = TrafficNotifyAfterLocked.this.myLockingCheckHandler.obtainMessage();
                        msg.what = 2;
                        msg.sendToTarget();
                        TrafficNotifyAfterLocked.this.mIsSameLockPeriod = false;
                    }
                } else if ("android.intent.action.SCREEN_ON".equals(action) && !KeyguardManagerEx.getDefault(context).isLockScreenDisabled()) {
                    KeyguardManager keyguardManager = (KeyguardManager) GlobalContext.getContext().getSystemService("keyguard");
                    if (keyguardManager != null) {
                        if (keyguardManager.isKeyguardLocked()) {
                            HwLog.i(TrafficNotifyAfterLocked.TAG, "screen on");
                        } else {
                            msg = TrafficNotifyAfterLocked.this.myLockingCheckHandler.obtainMessage();
                            msg.what = 2;
                            msg.sendToTarget();
                            TrafficNotifyAfterLocked.this.mIsSameLockPeriod = false;
                        }
                    }
                }
            }
        }
    };
    private long mScreenLockTotalMobileTraffic;
    private NetworkTemplate mTemplate;
    private LockingCheckHandler myLockingCheckHandler;

    private class LockingCheckHandler extends Handler {
        public LockingCheckHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    HwLog.v(TrafficNotifyAfterLocked.TAG, "get lock message");
                    TrafficNotifyAfterLocked.this.analyseTrafficAfterLocked();
                    return;
                case 2:
                    HwLog.v(TrafficNotifyAfterLocked.TAG, "get unlock message");
                    TrafficNotifyAfterLocked.this.analyseTrafficAfterUnLocked();
                    return;
                default:
                    return;
            }
        }
    }

    public static synchronized TrafficNotifyAfterLocked getInstance() {
        TrafficNotifyAfterLocked trafficNotifyAfterLocked;
        synchronized (TrafficNotifyAfterLocked.class) {
            if (mSingleton == null) {
                mSingleton = new TrafficNotifyAfterLocked();
            }
            trafficNotifyAfterLocked = mSingleton;
        }
        return trafficNotifyAfterLocked;
    }

    public static synchronized void destroyInstance() {
        synchronized (TrafficNotifyAfterLocked.class) {
            mSingleton = null;
        }
    }

    public void notifyTraffic() {
        super.notifyTraffic();
        startLockingCheck();
        registerForBroadcasts();
    }

    private void registerForBroadcasts() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.SCREEN_OFF");
        intentFilter.addAction("android.intent.action.SCREEN_ON");
        intentFilter.addAction("android.intent.action.USER_PRESENT");
        GlobalContext.getContext().registerReceiver(this.mReceiver, intentFilter);
        HwLog.v(TAG, "screen lock and unlock receiver registed success.");
    }

    private void startLockingCheck() {
        HandlerThread handlerThread = new HandlerThread("screenLockHandlerThread");
        handlerThread.start();
        this.myLockingCheckHandler = new LockingCheckHandler(handlerThread.getLooper());
    }

    private void analyseTrafficAfterLocked() {
        this.mCTM.getStatsSession().forceUpdate();
        mLastLockStartTime = DateUtil.getCurrentTimeMills();
        this.mTemplate = CommonMethodUtil.getTemplateMobileAutomatically();
        if (this.mTemplate == null) {
            HwLog.e(TAG, "analyseTrafficAfterLocked: network template is null!");
            return;
        }
        mLockStartTraffic = this.mCTM.getLockScreenSpendTraffic(this.mTemplate, mLastLockStartTime - 4000, mLastLockStartTime);
        mLockStartTrafficList = this.mCTM.setAllUidSpendTrafficForCalc(this.mTemplate, mLastLockStartTime - 4000, mLastLockStartTime);
        HwLog.i(TAG, "mLockStartTraffic = " + mLockStartTraffic + ";mLockStartTrafficList = " + mLockStartTrafficList);
    }

    private void analyseTrafficAfterUnLocked() {
        this.mCTM.getStatsSession().forceUpdate();
        this.mScreenLockTotalMobileTraffic = 0;
        if (this.mItemsListInScreenLocking != null) {
            this.mItemsListInScreenLocking.clear();
        }
        if (this.mTemplate == null) {
            HwLog.e(TAG, "analyseTrafficAfterUnLocked: network template is null!");
        } else if (1 != this.mTemplate.getMatchRule()) {
            HwLog.e(TAG, "analyseTrafficAfterUnLocked: network template is not mobile type!");
        } else {
            HwLog.v(TAG, "analyseTrafficAfterUnLocked: start analyse.");
            mLastLockEndTime = DateUtil.getCurrentTimeMills();
            String prefferredImsi = SimCardManager.getInstance().getPreferredDataSubscriberId();
            if (TextUtils.isEmpty(prefferredImsi)) {
                HwLog.e(TAG, "prefer imsi is null");
                return;
            }
            if (checkWhetherNeedScreenLockedCheck(prefferredImsi)) {
                long totalTrafficAfterLocked = this.mCTM.getLockScreenSpendTraffic(this.mTemplate, mLastLockStartTime, mLastLockEndTime);
                if (this.mCTM.needTrackTwice(this.mTemplate, mLastLockStartTime, mLastLockEndTime)) {
                    HwLog.i(TAG, "need track twice");
                    if (totalTrafficAfterLocked - mLockStartTraffic > 0) {
                        this.mItemsListInScreenLocking = this.mCTM.setAllUidSpendTrafficInScreenLocking(mLockStartTrafficList, this.mCTM.setAllUidSpendTraffic(this.mTemplate, mLastLockStartTime, mLastLockEndTime));
                        this.mItemsListInScreenLocking = appCheckFilter(this.mItemsListInScreenLocking);
                        if (this.mItemsListInScreenLocking != null && this.mItemsListInScreenLocking.size() > 0) {
                            Collections.sort(this.mItemsListInScreenLocking);
                            sendNotification(prefferredImsi);
                        }
                    }
                } else {
                    HwLog.i(TAG, "need not track twice");
                    if (totalTrafficAfterLocked > 0) {
                        this.mItemsListInScreenLocking = this.mCTM.setAllUidSpendTraffic(this.mTemplate, mLastLockStartTime, mLastLockEndTime);
                        this.mItemsListInScreenLocking = appCheckFilter(this.mItemsListInScreenLocking);
                        if (this.mItemsListInScreenLocking != null && this.mItemsListInScreenLocking.size() > 0) {
                            Collections.sort(this.mItemsListInScreenLocking);
                            sendNotification(prefferredImsi);
                        }
                    }
                }
            }
        }
    }

    private void sendNotification(String imsi) {
        if (TrafficAnalyseManager.getInstance().isMainCard(imsi)) {
            NotificationUtil.sendNotification(this.mScreenLockTotalMobileTraffic, 0, this.mItemsListInScreenLocking, NotificationUtil.MAIN_CARD_CATEGORY_SCREEN_LOCK_TRAFFIC);
        } else {
            NotificationUtil.sendNotification(this.mScreenLockTotalMobileTraffic, 0, this.mItemsListInScreenLocking, NotificationUtil.SECONDARY_CARD_CATEGORY_SCREEN_LOCK_TRAFFIC);
        }
    }

    private ArrayList<ParcelableAppItem> appCheckFilter(ArrayList<ParcelableAppItem> itemList) {
        if (itemList == null) {
            HwLog.e(TAG, "appCheckFilter: itemList is null!");
            return null;
        } else if (itemList.size() == 0) {
            HwLog.e(TAG, "appCheckFilter: itemList size is zero!");
            return null;
        } else {
            ArrayList<ParcelableAppItem> itemListAfterCheck = new ArrayList();
            for (ParcelableAppItem item : itemList) {
                if (!ExternMethodUtil.needCheckByUid(item.key)) {
                    item.appType = 1;
                }
                itemListAfterCheck.add(item);
                this.mScreenLockTotalMobileTraffic += item.mobiletotal;
                HwLog.v(TAG, "screen lock time:" + DateUtil.millisec2String(mLastLockStartTime) + "~" + DateUtil.millisec2String(mLastLockEndTime) + "; Uid:" + item.key + "; mobile total bytes:" + item.mobiletotal + "; wifi total bytes:" + item.wifitotal);
            }
            return itemListAfterCheck;
        }
    }

    private boolean checkWhetherNeedScreenLockedCheck(String imsi) {
        if (NatSettingInfo.getUnlockScreenNotify(GlobalContext.getContext())) {
            HwLog.i(TAG, "screen switch on mLastLockEndTime : " + mLastLockEndTime + " mLastLockStartTime : " + mLastLockStartTime);
            if (mLastLockEndTime - mLastLockStartTime >= SCREEN_LOCK_NO_CHEK_DELAY) {
                HwLog.i(TAG, "lock screen time longer than check delay, will notify");
                return true;
            }
        }
        HwLog.e(TAG, "screen notification will not notify");
        return false;
    }
}
