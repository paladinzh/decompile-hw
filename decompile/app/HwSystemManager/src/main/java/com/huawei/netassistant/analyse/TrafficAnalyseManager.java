package com.huawei.netassistant.analyse;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import com.huawei.netassistant.cardmanager.SimCardManager;
import com.huawei.netassistant.common.PhoneSimCardInfo;
import com.huawei.netassistant.common.SimCardInfo;
import com.huawei.netassistant.db.NetAssistantDBManager;
import com.huawei.netassistant.db.NetAssistantDBManager.TrafficSettingInfo;
import com.huawei.netassistant.db.NetAssistantStore.SettingTable;
import com.huawei.netassistant.db.NetAssistantStore.TrafficAdjustTable;
import com.huawei.netassistant.util.CommonConstantUtil;
import com.huawei.netassistant.util.CommonMethodUtil;
import com.huawei.netassistant.util.DateUtil;
import com.huawei.netassistant.util.NotificationUtil;
import com.huawei.permissionmanager.utils.ShareCfg;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.antimal.UserBehaviorManager;
import com.huawei.systemmanager.comm.concurrent.HsmSingleExecutor;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.comm.misc.ToastUtils;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.comm.simcard.HsmSubsciptionManager;
import com.huawei.systemmanager.comm.simcard.HsmSubsciptionManager.HsmSubInfo;
import com.huawei.systemmanager.netassistant.traffic.trafficstatistics.ITrafficInfo;
import com.huawei.systemmanager.netassistant.traffic.trafficstatistics.TrafficState;
import com.huawei.systemmanager.util.HwLog;

public class TrafficAnalyseManager {
    private static final long DATA_CONNECTION_CHANGE_RESPONSE_DELAY = 2000;
    private static final long DB_CHANGE_RESPONSE_DELAY = 300;
    private static final int MSG_ADJUST_DB_CHANGE = 2;
    private static final int MSG_AUTO_ADJUST = 8;
    private static final int MSG_AUTO_ADJUST_DIANXIN_SMS = 13;
    private static final int MSG_AUTO_ADJUST_MAIN_TIME_OUT = 10;
    private static final int MSG_AUTO_ADJUST_QUERY_RESULT = 9;
    private static final int MSG_AUTO_ADJUST_SECONDARY_TIME_OUT = 11;
    public static final int MSG_EXCESS_DIALOG = 14;
    private static final int MSG_MAIN_SIM_CARD_NOT_READY = 6;
    private static final int MSG_MAIN_SIM_CARD_READY = 4;
    private static final int MSG_PREFER_CARD_CHANGE = 12;
    private static final int MSG_SECONDARY_SIM_CARD_NOT_READY = 7;
    private static final int MSG_SECONDARY_SIM_CARD_READY = 5;
    private static final int MSG_SETTINGS_DB_CHANGE = 1;
    private static final int MSG_TIME_CHANGE = 3;
    private static final String TAG = "TrafficAnalyseManager";
    private static final NetAssistantDBManager mDBManager = NetAssistantDBManager.getInstance();
    private static TrafficAnalyseManager mSingleton;
    private static final Object sMutexTrafficAnalyseManager = new Object();
    private Handler analyseHandler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    TrafficAnalyseManager.this.setSettingsValueFromDB();
                    return;
                case 2:
                    TrafficAnalyseManager.this.setAdjustValueFromDB();
                    return;
                case 3:
                    TrafficAnalyseManager.this.timeChangeOperation();
                    return;
                case 4:
                    TrafficAnalyseManager.this.initCardStatus();
                    TrafficAnalyseManager.this.initAutoAdjustAlarm(TrafficAnalyseManager.this.mMainCardImsi);
                    TrafficAnalyseManager.this.setSettingsValueFromDB();
                    TrafficAnalyseManager.this.setAdjustValueFromDB();
                    return;
                case 5:
                    TrafficAnalyseManager.this.initCardStatus();
                    TrafficAnalyseManager.this.initAutoAdjustAlarm(TrafficAnalyseManager.this.mSecondaryCardImsi);
                    TrafficAnalyseManager.this.setSettingsValueFromDB();
                    TrafficAnalyseManager.this.setAdjustValueFromDB();
                    return;
                case 6:
                    TrafficAutoAdjust.getInstance().cancelAutoAdjustAlarm(GlobalContext.getContext(), TrafficAnalyseManager.this.mMainCardImsi, true);
                    TrafficAnalyseManager.this.initCardStatus();
                    TrafficAnalyseManager.this.cancelMainCardNotification();
                    TrafficAnalyseManager.this.cancelNormalNotification(true);
                    return;
                case 7:
                    TrafficAutoAdjust.getInstance().cancelAutoAdjustAlarm(GlobalContext.getContext(), TrafficAnalyseManager.this.mSecondaryCardImsi, false);
                    TrafficAnalyseManager.this.initCardStatus();
                    TrafficAnalyseManager.this.cancelSecondaryCardNotification();
                    TrafficAnalyseManager.this.cancelNormalNotification(false);
                    return;
                case 8:
                    String imsi = msg.obj;
                    boolean isFstInPeriod = TrafficAnalyseManager.this.shouldAutoAdjust(imsi);
                    boolean isRoamingState = TrafficState.isRoamingState(imsi);
                    HwLog.i(TrafficAnalyseManager.TAG, "isFstInPeriod = " + isFstInPeriod + " isRoamingState = " + isRoamingState);
                    if (!isFstInPeriod || isRoamingState) {
                        HwLog.w(TrafficAnalyseManager.TAG, "should not auto adjust, because has auto adjust in this period");
                        return;
                    }
                    long currentTime = DateUtil.getCurrentTimeMills();
                    NetAssistantDBManager.getInstance().setSettingRegularAdjustBeginTime(imsi, currentTime);
                    HwLog.w(TrafficAnalyseManager.TAG, "first time to auto adjust in this period, adjust time is = " + DateUtil.millisec2String(currentTime));
                    TrafficAnalyseManager.this.startAutoAdjust(imsi);
                    return;
                case 10:
                case 11:
                    TrafficAnalyseManager.this.startTimeOutCheck();
                    return;
                case 12:
                    TrafficAnalyseManager.this.processDataChangeNotification();
                    return;
                case 14:
                    if (!TrafficAnalyseManager.this.isDialogShowing) {
                        Context ctx = Utility.getEmuiContext(GlobalContext.getContext());
                        if (noSimCardToChange()) {
                            TrafficAnalyseManager.createDialog(ctx);
                        } else {
                            TrafficAnalyseManager.this.createChangeSimDialog(ctx);
                        }
                        TrafficAnalyseManager.this.isDialogShowing = true;
                        return;
                    }
                    return;
                default:
                    return;
            }
        }

        private boolean noSimCardToChange() {
            if (HsmSubsciptionManager.isMultiSubs()) {
                int defSub = HsmSubsciptionManager.getDataDefaultSubId();
                for (HsmSubInfo info : HsmSubsciptionManager.createSubInfos()) {
                    if (info.getSubId() != defSub) {
                        ITrafficInfo trafficInfo = ITrafficInfo.create(info.getImsi(), DateUtil.getYearMonth(info.getImsi()), 301);
                        if (trafficInfo.getTotalLimit() >= 0 && trafficInfo.getLeftTraffic() <= 0) {
                            HwLog.i(TrafficAnalyseManager.TAG, "noSimCardToChange another card has package, and has over limit");
                            return true;
                        }
                    }
                }
                return false;
            }
            HwLog.i(TrafficAnalyseManager.TAG, "noSimCardToChange only one card, show normal dialog");
            return true;
        }
    };
    private boolean isDialogShowing = false;
    private long mDayEndTime;
    private long mDayStartTime;
    private AlertDialog mDialog;
    private HsmSingleExecutor mHsmSingleExecutor = new HsmSingleExecutor();
    private int mMainCardAccountDay;
    private String mMainCardImsi;
    private long mMainCardMonthEndDate;
    private long mMainCardMonthStartDate;
    private boolean mPreferCardChanging;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (context != null && intent != null) {
                String action = intent.getAction();
                if (TextUtils.isEmpty(action)) {
                    HwLog.w(TrafficAnalyseManager.TAG, "received an empty action, please check it.");
                    return;
                }
                HwLog.v(TrafficAnalyseManager.TAG, "received broadcast, action is " + action);
                if ("android.intent.action.SIM_STATE_CHANGED".equals(action)) {
                    TrafficAnalyseManager.this.actionSimcardChanged();
                } else if ("android.intent.action.DATE_CHANGED".equals(action) || "android.intent.action.TIMEZONE_CHANGED".equals(action) || UserBehaviorManager.ACTION_DATE_CHANGED.equals(action)) {
                    TrafficAnalyseManager.this.analyseHandler.removeMessages(3);
                    TrafficAnalyseManager.this.analyseHandler.sendEmptyMessage(3);
                } else if (TrafficAutoAdjust.ACTION_MAIN_AUTO_ADJUST.equals(action) || TrafficAutoAdjust.ACTION_SECONDARY_AUTO_ADJUST.equals(action)) {
                    String imsi = intent.getStringExtra(CommonConstantUtil.SIM_IMSI_FOR_TRANSPORT);
                    Message msg = TrafficAnalyseManager.this.analyseHandler.obtainMessage();
                    msg.what = 8;
                    msg.obj = imsi;
                    TrafficAnalyseManager.this.analyseHandler.sendMessage(msg);
                } else if ("android.intent.action.ANY_DATA_STATE".equals(action)) {
                    TrafficAnalyseManager.this.analyseHandler.removeMessages(12);
                    TrafficAnalyseManager.this.analyseHandler.sendEmptyMessage(12);
                }
            }
        }
    };
    private int mSecondaryCardAccountDay;
    private String mSecondaryCardImsi;
    private long mSecondaryCardMonthEndDate;
    private long mSecondaryCardMonthStartDate;
    private ContentObserver mSettingsDbObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            HwLog.v(TrafficAnalyseManager.TAG, "ContentObserver onChange, update settings config");
            TrafficAnalyseManager.this.analyseHandler.removeMessages(1);
            TrafficAnalyseManager.this.analyseHandler.sendEmptyMessage(1);
        }
    };
    private ContentObserver mTrafficAdjustDbObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            HwLog.v(TrafficAnalyseManager.TAG, "ContentObserver onChange, update traffic adjust");
            TrafficAnalyseManager.this.analyseHandler.removeMessages(2);
            TrafficAnalyseManager.this.analyseHandler.sendEmptyMessage(2);
        }
    };

    private class DlgListener implements OnClickListener {
        private DlgListener() {
        }

        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case -3:
                    Intent intent = new Intent("com.huawei.settings.intent.DUAL_CARD_SETTINGS");
                    intent.setFlags(ShareCfg.PERMISSION_MODIFY_CALENDAR);
                    GlobalContext.getContext().startActivity(intent);
                    ToastUtils.toastLongMsg((int) R.string.net_assistant_notification_notice_open_mobile);
                    try {
                        TrafficAnalyseManager.this.mDialog.dismiss();
                        return;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return;
                    }
                case -2:
                    CommonMethodUtil.toggleGprs(true);
                    try {
                        TrafficAnalyseManager.this.mDialog.dismiss();
                        return;
                    } catch (Exception e2) {
                        e2.printStackTrace();
                        return;
                    }
                case -1:
                    try {
                        TrafficAnalyseManager.this.mDialog.dismiss();
                        return;
                    } catch (Exception e22) {
                        e22.printStackTrace();
                        return;
                    }
                default:
                    return;
            }
        }
    }

    private class SetSettingsValueRunnable implements Runnable {
        private SetSettingsValueRunnable() {
        }

        public void run() {
            synchronized (TrafficAnalyseManager.this) {
                TrafficAnalyseManager.this.initCardStatus();
                TrafficAnalyseManager.this.setAccountDay(NetAssistantDBManager.getInstance().getTrafficSettingInfo(TrafficAnalyseManager.this.mMainCardImsi), NetAssistantDBManager.getInstance().getTrafficSettingInfo(TrafficAnalyseManager.this.mSecondaryCardImsi));
            }
        }
    }

    private TrafficAnalyseManager() {
        initCardStatus();
        initialSettingsValues();
        registerForBroadcasts();
        registerDBObserver();
    }

    public static TrafficAnalyseManager getInstance() {
        TrafficAnalyseManager trafficAnalyseManager;
        synchronized (sMutexTrafficAnalyseManager) {
            if (mSingleton == null) {
                mSingleton = new TrafficAnalyseManager();
            }
            trafficAnalyseManager = mSingleton;
        }
        return trafficAnalyseManager;
    }

    public static void destroyInstance() {
        synchronized (sMutexTrafficAnalyseManager) {
            mSingleton = null;
        }
    }

    public void analyseScreenLock() {
        TrafficNotifyDecorator decorator = new TrafficNotifyDecorator();
        TrafficNotifyAfterLocked afterLocked = TrafficNotifyAfterLocked.getInstance();
        afterLocked.decorateNotify(decorator);
        afterLocked.notifyTraffic();
    }

    private void initialSettingsValues() {
        initAutoAdjustAlarm();
        setSettingsValueFromDB();
        setAdjustValueFromDB();
    }

    private void initCardStatus() {
        PhoneSimCardInfo phoneSimCardInfo = SimCardManager.getInstance().getPhoneSimCardInfo();
        if (phoneSimCardInfo.getDataUsedSimCard() == null) {
            HwLog.w(TAG, "initCardStatus: preferredCardInfo is null.");
            return;
        }
        int phoneCardState = phoneSimCardInfo.getPhoneCardState();
        if (2 == phoneCardState) {
            this.mMainCardImsi = phoneSimCardInfo.getCardInfoSlot0().getImsiNumber();
        } else {
            if (!(4 == phoneCardState || 6 == phoneCardState)) {
                if (5 == phoneCardState) {
                }
            }
            SimCardInfo mainSimCardInfo = phoneSimCardInfo.getCardInfoSlot0();
            SimCardInfo secondarySimCardInfo = phoneSimCardInfo.getCardInfoSlot1();
            this.mMainCardImsi = mainSimCardInfo.getImsiNumber();
            this.mSecondaryCardImsi = secondarySimCardInfo.getImsiNumber();
        }
    }

    private void registerDBObserver() {
        ContentResolver contentResolver = GlobalContext.getContext().getContentResolver();
        if (contentResolver == null) {
            HwLog.e(TAG, "registerDBObserver failed: contentResolver is null!");
            return;
        }
        contentResolver.registerContentObserver(SettingTable.getContentUri(), true, this.mSettingsDbObserver);
        contentResolver.registerContentObserver(TrafficAdjustTable.getContentUri(), true, this.mTrafficAdjustDbObserver);
    }

    private void setSettingsValueFromDB() {
        if (this.mHsmSingleExecutor.getDequeTaskNum() <= 0) {
            this.mHsmSingleExecutor.execute(new SetSettingsValueRunnable());
        }
    }

    public void setDialogDismiss() {
        this.isDialogShowing = false;
    }

    private void cancelNormalNotification(boolean isMainCard) {
        if (isMainCard) {
            NotificationUtil.cancelNotification(NotificationUtil.MAIN_CARD_CATEGORY_NORMAL_TRAFFIC);
        } else {
            NotificationUtil.cancelNotification(NotificationUtil.SECONDARY_CARD_CATEGORY_NORMAL_TRAFFIC);
        }
    }

    private void setAdjustValueFromDB() {
        new Thread("thread_AdjustValueFromDB") {
            public void run() {
                synchronized (TrafficAnalyseManager.this) {
                    TrafficAnalyseManager.this.initCardStatus();
                }
            }
        }.start();
    }

    public boolean isMainCard(String imsi) {
        if (TextUtils.isEmpty(imsi)) {
            HwLog.w(TAG, "isMainCard: imsi is null.");
            return false;
        } else if (imsi.equals(this.mMainCardImsi)) {
            return true;
        } else {
            return imsi.equals(this.mSecondaryCardImsi) ? false : false;
        }
    }

    private void setAccountDay(TrafficSettingInfo mainSettingInfo, TrafficSettingInfo secSettingInfo) {
        this.mMainCardAccountDay = mainSettingInfo.beginDate;
        this.mSecondaryCardAccountDay = secSettingInfo.beginDate;
        this.mMainCardMonthStartDate = DateUtil.monthStart(this.mMainCardAccountDay);
        this.mMainCardMonthEndDate = DateUtil.monthEnd(this.mMainCardAccountDay);
        this.mSecondaryCardMonthStartDate = DateUtil.monthStart(this.mSecondaryCardAccountDay);
        this.mSecondaryCardMonthEndDate = DateUtil.monthEnd(this.mSecondaryCardAccountDay);
        timeChangeCancelNotification();
    }

    private void timeChangeCancelNotification() {
        long currentTime = DateUtil.getCurrentTimeMills();
        if (currentTime < this.mMainCardMonthStartDate || currentTime > this.mMainCardMonthEndDate) {
            NotificationUtil.cancelNotification(1074041823);
            NotificationUtil.cancelNotification(NotificationUtil.MAIN_CARD_CATEGORY_EXCESS_MONTH_MARK);
            this.mMainCardMonthStartDate = DateUtil.monthStart(this.mMainCardAccountDay);
            this.mMainCardMonthEndDate = DateUtil.monthEnd(this.mMainCardAccountDay);
        }
        if (currentTime < this.mSecondaryCardMonthStartDate || currentTime > this.mSecondaryCardMonthEndDate) {
            NotificationUtil.cancelNotification(NotificationUtil.SECONDARY_CARD_CATEGORY_EXCESS_MONTH_LIMIT);
            NotificationUtil.cancelNotification(NotificationUtil.SECONDARY_CARD_CATEGORY_EXCESS_MONTH_MARK);
            this.mSecondaryCardMonthStartDate = DateUtil.monthStart(this.mMainCardAccountDay);
            this.mSecondaryCardMonthEndDate = DateUtil.monthEnd(this.mMainCardAccountDay);
        }
    }

    public String getMainCardImsi() {
        return this.mMainCardImsi;
    }

    public String getSecondaryCardImsi() {
        return this.mSecondaryCardImsi;
    }

    public long getCardPackage(String imsi) {
        if (TextUtils.isEmpty(imsi)) {
            return -1;
        }
        return mDBManager.getSettingTotalPackage(imsi);
    }

    public int getAccountDay(String imsi) {
        if (TextUtils.isEmpty(imsi)) {
            return -1;
        }
        return mDBManager.getSettingBeginDate(imsi);
    }

    public int getOverMarkDayNeedNotify(String imsi) {
        if (TextUtils.isEmpty(imsi)) {
            return -1;
        }
        return mDBManager.getSettingOverMarkDay(imsi);
    }

    public int getNormalTrafficNeedNotify(String imsi) {
        if (TextUtils.isEmpty(imsi)) {
            return -1;
        }
        return mDBManager.getSettingNotify(imsi);
    }

    public int getOverMarkMonthNeedNotify(String imsi) {
        if (TextUtils.isEmpty(imsi)) {
            return -1;
        }
        return mDBManager.getSettingOverMarkMonth(imsi);
    }

    public int getExcessMonthBehavior(String imsi) {
        if (TextUtils.isEmpty(imsi)) {
            return -1;
        }
        return mDBManager.getSettingExcessMontyType(imsi);
    }

    public int getScreenLockNeedNotify(String imsi) {
        if (TextUtils.isEmpty(imsi)) {
            return -1;
        }
        return mDBManager.getSettingUnlockScreen(imsi);
    }

    public long getAdjustValue(String imsi) {
        if (TextUtils.isEmpty(imsi)) {
            return -1;
        }
        return mDBManager.getAdjustPackageValue(imsi);
    }

    public long getAdjustDate(String imsi) {
        if (TextUtils.isEmpty(imsi)) {
            return -1;
        }
        return mDBManager.getAdjustDate(imsi);
    }

    public Handler getAnalyseHandler() {
        return this.analyseHandler;
    }

    @FindBugsSuppressWarnings({"SIC_INNER_SHOULD_BE_STATIC_ANON"})
    private void createChangeSimDialog(Context context) {
        Builder builder = new Builder(context);
        builder.setTitle(R.string.net_assistant_notification_excess_disable_network_title);
        builder.setMessage(R.string.net_assistant_notification_changecard_message);
        this.mDialog = builder.create();
        this.mDialog.setOnDismissListener(new OnDismissListener() {
            public void onDismiss(DialogInterface dialog) {
                TrafficAnalyseManager.getInstance().setDialogDismiss();
            }
        });
        this.mDialog.getWindow().setType(2003);
        this.mDialog.setCanceledOnTouchOutside(false);
        DlgListener listen = new DlgListener();
        this.mDialog.setButton(-2, context.getString(R.string.net_assistant_notification_changecard_open_mobile), listen);
        this.mDialog.setButton(-3, context.getString(R.string.net_assistant_notification_changecard_change_another_card), listen);
        this.mDialog.setButton(-1, context.getString(R.string.net_assistant_notification_changecard_cancel), listen);
        CommonMethodUtil.toggleGprs(false);
        this.mDialog.show();
    }

    public static void createDialog(Context context) {
        Builder builder = new Builder(context);
        builder.setTitle(R.string.net_assistant_notification_excess_disable_network_title);
        builder.setMessage(R.string.net_assistant_notification_excess_disable_network_content);
        builder.setPositiveButton(17039370, null);
        builder.setNegativeButton(R.string.net_assistant_notification_excess_disable_network_button_reopen, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                CommonMethodUtil.toggleGprs(true);
            }
        });
        Dialog dialog = builder.create();
        dialog.getWindow().setType(2003);
        dialog.setOnDismissListener(new OnDismissListener() {
            public void onDismiss(DialogInterface dialog) {
                TrafficAnalyseManager.getInstance().setDialogDismiss();
            }
        });
        CommonMethodUtil.toggleGprs(false);
        dialog.show();
    }

    private boolean shouldAutoAdjust(String imsi) {
        boolean z = false;
        TrafficSettingInfo settingInfo = NetAssistantDBManager.getInstance().getTrafficSettingInfo(imsi);
        long lastAdjustTime = settingInfo.regularAdjustBeginTime;
        HwLog.i(TAG, "last adjust time is " + DateUtil.millisec2String(lastAdjustTime));
        int adjustType = settingInfo.regularAdjustType;
        if (adjustType == 0) {
            return false;
        }
        if (DateUtil.getCurrentTimeMills() - lastAdjustTime > ((long) adjustType) * 86400000) {
            z = true;
        }
        return z;
    }

    public void initAutoAdjustAlarm() {
        TrafficSettingInfo mainSettingInfo = NetAssistantDBManager.getInstance().getTrafficSettingInfo(this.mMainCardImsi);
        TrafficSettingInfo secSettingInfo = NetAssistantDBManager.getInstance().getTrafficSettingInfo(this.mSecondaryCardImsi);
        HwLog.i(TAG, "initAutoAdjustAlarm: start.");
        if (!TextUtils.isEmpty(this.mMainCardImsi)) {
            int mainCardAdjustType = mainSettingInfo.regularAdjustType;
            if (mainSettingInfo.packageTotal >= 0) {
                if (!(1 == mainCardAdjustType || 3 == mainCardAdjustType)) {
                    if (7 == mainCardAdjustType) {
                    }
                }
                TrafficAutoAdjust.getInstance().startAutoAdjustAlarm(GlobalContext.getContext(), mainCardAdjustType, this.mMainCardImsi, mainSettingInfo.regularAdjustBeginTime, DateUtil.getCurrentTimeMills(), true);
            }
        }
        if (!TextUtils.isEmpty(this.mSecondaryCardImsi)) {
            int secondaryCardAdjustType = secSettingInfo.regularAdjustType;
            if (secSettingInfo.packageTotal >= 0) {
                if (!(1 == secondaryCardAdjustType || 3 == secondaryCardAdjustType)) {
                    if (7 != secondaryCardAdjustType) {
                        return;
                    }
                }
                TrafficAutoAdjust.getInstance().startAutoAdjustAlarm(GlobalContext.getContext(), secondaryCardAdjustType, this.mSecondaryCardImsi, secSettingInfo.regularAdjustBeginTime, DateUtil.getCurrentTimeMills(), false);
            }
        }
    }

    public void initAutoAdjustAlarm(String imsi) {
        TrafficSettingInfo settingInfo = NetAssistantDBManager.getInstance().getTrafficSettingInfo(imsi);
        HwLog.v(TAG, "initAutoAdjustAlarm: start with imsi.");
        if (!TextUtils.isEmpty(imsi)) {
            int cardAdjustType = settingInfo.regularAdjustType;
            if (settingInfo.packageTotal >= 0) {
                if (!(1 == cardAdjustType || 3 == cardAdjustType)) {
                    if (7 != cardAdjustType) {
                        return;
                    }
                }
                long dbTime = settingInfo.regularAdjustBeginTime;
                TrafficAutoAdjust.getInstance().startAutoAdjustAlarm(GlobalContext.getContext(), cardAdjustType, imsi, dbTime, DateUtil.getCurrentTimeMills(), TextUtils.equals(imsi, this.mMainCardImsi));
            }
        }
    }

    public void startAutoAdjust(final String imsi) {
        new Thread("thread_startAutoAdjust") {
            public void run() {
                if (!TextUtils.isEmpty(imsi)) {
                    if (imsi.equals(TrafficAnalyseManager.this.mMainCardImsi)) {
                        if (TrafficAutoAdjust.getInstance().startAutoAdjust(imsi)) {
                            TrafficAnalyseManager.this.analyseHandler.sendEmptyMessageDelayed(10, TrafficNotifyAfterLocked.SCREEN_LOCK_NO_CHEK_DELAY);
                        }
                    } else if (imsi.equals(TrafficAnalyseManager.this.mSecondaryCardImsi) && TrafficAutoAdjust.getInstance().startAutoAdjust(imsi)) {
                        TrafficAnalyseManager.this.analyseHandler.sendEmptyMessageDelayed(11, TrafficNotifyAfterLocked.SCREEN_LOCK_NO_CHEK_DELAY);
                    }
                }
            }
        }.start();
    }

    public boolean isPhoneSimCardStateNormal(String preferCardImsi) {
        int state = SimCardManager.getInstance().getPhoneSimCardState();
        boolean isMainCard = isMainCard(preferCardImsi);
        switch (state) {
            case -1:
            case 1:
            case 3:
                return false;
            case 2:
            case 4:
                if (isMainCard) {
                    return true;
                }
                return false;
            case 5:
                if (isMainCard) {
                    return false;
                }
                return true;
            default:
                return true;
        }
    }

    public void processDataChangeNotification() {
        if (!this.mPreferCardChanging) {
            this.mPreferCardChanging = true;
            new Thread("thread_processDataChangeNotification") {
                public void run() {
                    try {
                        TrafficAnalyseManager.this.initCardStatus();
                        TrafficAnalyseManager.this.cancelAllExcessNotification();
                        TrafficAnalyseManager.this.setSettingsValueFromDB();
                        TrafficAnalyseManager.this.setAdjustValueFromDB();
                        Thread.sleep(TrafficAnalyseManager.DATA_CONNECTION_CHANGE_RESPONSE_DELAY);
                        TrafficAnalyseManager.this.mPreferCardChanging = false;
                        HwLog.v(TrafficAnalyseManager.TAG, "startCancelNotification: cancel ok.");
                    } catch (Exception e) {
                        HwLog.e(TrafficAnalyseManager.TAG, "startCancelNotification: sleep failed.", e);
                    }
                }
            }.start();
        }
    }

    private void startTimeOutCheck() {
        if (TrafficAutoAdjust.getInstance().getIsMainSameQueryBooleanResult()) {
            HwLog.v(TAG, "Auto adjust query Main Card time out!");
            TrafficAutoAdjust.getInstance().setIsMainSameQueryBoolean(false);
            this.analyseHandler.removeMessages(10);
        }
        if (TrafficAutoAdjust.getInstance().getIsSecondarySameQueryBooleanResult()) {
            HwLog.v(TAG, "Auto adjust query Secondary Card time out!");
            TrafficAutoAdjust.getInstance().setIsSecondarySameQueryBoolean(false);
            this.analyseHandler.removeMessages(11);
        }
    }

    public void cancelAllExcessNotification() {
        String prefferredImsi = SimCardManager.getInstance().getPreferredDataSubscriberId();
        if (TextUtils.isEmpty(prefferredImsi)) {
            cancelMainCardNotification();
            cancelSecondaryCardNotification();
        }
        if (TextUtils.isEmpty(this.mMainCardImsi)) {
            cancelMainCardNotification();
        }
        if (TextUtils.isEmpty(this.mSecondaryCardImsi)) {
            cancelSecondaryCardNotification();
        }
        if (isMainCard(prefferredImsi)) {
            cancelSecondaryCardNotification();
        } else {
            cancelMainCardNotification();
        }
    }

    private void cancelMainCardNotification() {
        NotificationUtil.cancelNotification(NotificationUtil.NOTIFICATION_ID_LIMIT_NOTIFY);
    }

    private void cancelSecondaryCardNotification() {
        NotificationUtil.cancelNotification(NotificationUtil.NOTIFICATION_ID_LIMIT_NOTIFY);
    }

    private void timeChangeOperation() {
        long currentTime = DateUtil.getCurrentTimeMills();
        timeChangeCancelNotification();
        if (currentTime < this.mDayStartTime || currentTime > this.mDayEndTime) {
            NotificationUtil.cancelNotification(NotificationUtil.MAIN_CARD_CATEGORY_EXCESS_DAILY_MARK);
            NotificationUtil.cancelNotification(NotificationUtil.SECONDARY_CARD_CATEGORY_EXCESS_DAILY_MARK);
            this.mDayStartTime = DateUtil.getDayStartTimeMills();
            this.mDayEndTime = DateUtil.getDayEndTimeMills();
        }
    }

    private void registerForBroadcasts() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.DATE_CHANGED");
        intentFilter.addAction("android.intent.action.TIMEZONE_CHANGED");
        intentFilter.addAction(UserBehaviorManager.ACTION_DATE_CHANGED);
        intentFilter.addAction("android.intent.action.SIM_STATE_CHANGED");
        IntentFilter netintentFilter = new IntentFilter();
        netintentFilter.addAction(TrafficAutoAdjust.ACTION_MAIN_AUTO_ADJUST);
        netintentFilter.addAction(TrafficAutoAdjust.ACTION_SECONDARY_AUTO_ADJUST);
        GlobalContext.getContext().registerReceiver(this.mReceiver, intentFilter);
        GlobalContext.getContext().registerReceiver(this.mReceiver, netintentFilter, "com.huawei.systemmanager.permission.ACCESS_INTERFACE", null);
    }

    private void actionSimcardChanged() {
        TelephonyManager tm = TelephonyManager.from(GlobalContext.getContext());
        int stateMain = tm.getSimState(0);
        int stateSec = tm.getSimState(1);
        switch (stateMain) {
            case 5:
                HwLog.d(TAG, "main sim card ready, should init card state");
                this.analyseHandler.removeMessages(4);
                this.analyseHandler.sendEmptyMessage(4);
                break;
            default:
                HwLog.d(TAG, "main sim card not ready, should cancel card state");
                this.analyseHandler.removeMessages(6);
                this.analyseHandler.sendEmptyMessage(6);
                break;
        }
        switch (stateSec) {
            case 5:
                HwLog.d(TAG, "sec sim card ready, should init card state");
                this.analyseHandler.removeMessages(5);
                this.analyseHandler.sendEmptyMessage(5);
                return;
            default:
                HwLog.d(TAG, "sec sim card not ready, should cancel card state");
                this.analyseHandler.removeMessages(7);
                this.analyseHandler.sendEmptyMessage(7);
                return;
        }
    }
}
