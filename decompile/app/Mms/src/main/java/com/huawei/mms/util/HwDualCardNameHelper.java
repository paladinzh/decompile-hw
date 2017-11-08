package com.huawei.mms.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.provider.Settings.Global;
import android.telephony.MSimTelephonyManager;
import android.text.TextUtils;
import com.android.mms.MmsApp;
import com.android.mms.ui.MessageUtils;
import com.google.android.gms.R;
import com.huawei.cspcommon.MLog;
import java.util.Vector;

public class HwDualCardNameHelper {
    private static HwDualCardNameHelper selfInstance = null;
    private Vector<HwCardNameChangedListener> mCardNameChangedListeners = new Vector();
    private HwDualCardNameChangedReceiver mCardNameModifiedReceiver;
    private Context mContext = null;
    private String[] mCurrentCardNames = new String[]{"", ""};
    private Runnable mInitCardNameRunnable = new Runnable() {
        public void run() {
            HwDualCardNameHelper.this.mCurrentCardNames[0] = HwDualCardNameHelper.this.getCardNameString(HwDualCardNameHelper.this.mContext, 0);
            HwDualCardNameHelper.this.mCurrentCardNames[1] = HwDualCardNameHelper.this.getCardNameString(HwDualCardNameHelper.this.mContext, 1);
            MLog.v("Mms::HwDualCardNameHelper", "initCardName current card name sub1 " + HwDualCardNameHelper.this.mCurrentCardNames[0] + " sub1 " + HwDualCardNameHelper.this.mCurrentCardNames[1]);
        }
    };
    private Runnable mNotifyUIWhenCardNameChanged = new Runnable() {
        public void run() {
            if (HwDualCardNameHelper.this.mCardNameChangedListeners.size() < 1) {
                MLog.v("Mms::HwDualCardNameHelper", "mNotifyUIWhenCardNameChanged the listener is empty!!");
                return;
            }
            for (HwCardNameChangedListener modifiedListener : HwDualCardNameHelper.this.mCardNameChangedListeners) {
                modifiedListener.onCardNameChanged(HwDualCardNameHelper.this.mCurrentCardNames);
            }
        }
    };
    private String[] mSpnCardNames = new String[]{"", ""};
    private HwDualCardNameChangedReceiver mSpnStateChangedReceiver;

    public interface HwCardNameChangedListener {
        void onCardNameChanged(String[] strArr);
    }

    private class HwDualCardNameChangedReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            if (MessageUtils.isMultiSimEnabled() && intent != null) {
                String action = intent.getAction();
                MLog.d("Mms::HwDualCardNameHelper", "The action of receiver is: " + action);
                if ("android.intent.action.ACTION_DSDS_SUB1_OPERATOR_CHANGED".equals(action)) {
                    HwDualCardNameHelper.this.handleSpnStateChanged(intent, 0);
                } else if ("android.intent.action.ACTION_DSDS_SUB2_OPERATOR_CHANGED".equals(action)) {
                    HwDualCardNameHelper.this.handleSpnStateChanged(intent, 1);
                } else if ("com.huawei.android.dsdscardmanager.SIM_CARD_NAME_SUB1_UPDATED_ACTION".equals(action)) {
                    HwDualCardNameHelper.this.handleCardNameModified(0);
                } else if ("com.huawei.android.dsdscardmanager.SIM_CARD_NAME_SUB2_UPDATED_ACTION".equals(action)) {
                    HwDualCardNameHelper.this.handleCardNameModified(1);
                }
            }
        }
    }

    private HwDualCardNameHelper(Context context) {
        this.mContext = context;
    }

    public static void init(Context context) {
        if (selfInstance == null) {
            selfInstance = new HwDualCardNameHelper(context);
        }
        selfInstance.registerSpnChangedMoniter();
        selfInstance.registerCardNameModifiedMoniter();
    }

    public static final HwDualCardNameHelper self() {
        return selfInstance;
    }

    public void unregisterReceiver() {
        unregisterSpnChangedMoniter();
        unregisterCardNameModifiedMoniter();
    }

    public void clearAndResetCurrentCardName(int subId) {
        if (isCardIdValid(subId)) {
            this.mCurrentCardNames[subId] = "";
            this.mCurrentCardNames[subId] = readCardName(subId);
        } else if (-1 == subId) {
            this.mCurrentCardNames[0] = "";
            this.mCurrentCardNames[0] = readCardName(0);
            this.mCurrentCardNames[1] = "";
            this.mCurrentCardNames[1] = readCardName(1);
        }
    }

    public String readCardName(int subId) {
        if (!isCardIdValid(subId)) {
            MLog.w("Mms::HwDualCardNameHelper", "readCardName current cardId is not valid, show no service!!");
            return this.mContext.getResources().getString(R.string.mms_name_no_service);
        } else if (!isCardNameEmpty(this.mCurrentCardNames[subId])) {
            return this.mCurrentCardNames[subId];
        } else {
            String cardName = getCardNameString(this.mContext, subId);
            this.mCurrentCardNames[subId] = cardName;
            MLog.v("Mms::HwDualCardNameHelper", "readCardName current card" + subId + " name is empty, get card name string: " + cardName);
            return cardName;
        }
    }

    public static boolean isCardIdValid(int subId) {
        return subId == 0 || 1 == subId;
    }

    public void addCardNameChangedListener(HwCardNameChangedListener listener) {
        this.mCardNameChangedListeners.add(listener);
    }

    public void initCardName() {
        HwBackgroundLoader.getBackgroundHandler().removeCallbacks(this.mInitCardNameRunnable);
        HwBackgroundLoader.getBackgroundHandler().post(this.mInitCardNameRunnable);
    }

    public void removeCardNameChangedListener(HwCardNameChangedListener listener) {
        this.mCardNameChangedListeners.remove(listener);
    }

    private void handleSpnStateChanged(final Intent intent, final int subId) {
        HwBackgroundLoader.getBackgroundHandler().post(new Runnable() {
            public void run() {
                boolean needUpdate = HwDualCardNameHelper.this.refreshSpnCardNameFromBroadcast(HwDualCardNameHelper.this.mContext, intent, subId);
                HwDualCardNameHelper.this.clearAndResetCurrentCardName(subId);
                MLog.v("Mms::HwDualCardNameHelper", "handleSpnStateChanged::card" + subId + " needUpdate or not:" + needUpdate);
                if (needUpdate) {
                    HwBackgroundLoader.getUIHandler().removeCallbacks(HwDualCardNameHelper.this.mNotifyUIWhenCardNameChanged);
                    HwBackgroundLoader.getUIHandler().postDelayed(HwDualCardNameHelper.this.mNotifyUIWhenCardNameChanged, 500);
                }
            }
        });
    }

    private void handleCardNameModified(final int subId) {
        HwBackgroundLoader.getBackgroundHandler().post(new Runnable() {
            public void run() {
                HwDualCardNameHelper.this.clearAndResetCurrentCardName(subId);
                HwBackgroundLoader.getUIHandler().removeCallbacks(HwDualCardNameHelper.this.mNotifyUIWhenCardNameChanged);
                HwBackgroundLoader.getUIHandler().postDelayed(HwDualCardNameHelper.this.mNotifyUIWhenCardNameChanged, 500);
            }
        });
    }

    private void registerSpnChangedMoniter() {
        HwBackgroundLoader.getUIHandler().postDelayed(new Runnable() {
            public void run() {
                if (HwDualCardNameHelper.this.mSpnStateChangedReceiver == null) {
                    HwDualCardNameHelper.this.mSpnStateChangedReceiver = new HwDualCardNameChangedReceiver();
                }
                IntentFilter filter = new IntentFilter();
                filter.addAction("android.intent.action.ACTION_DSDS_SUB1_OPERATOR_CHANGED");
                filter.addAction("android.intent.action.ACTION_DSDS_SUB2_OPERATOR_CHANGED");
                HwDualCardNameHelper.this.mContext.registerReceiver(HwDualCardNameHelper.this.mSpnStateChangedReceiver, filter);
            }
        }, 2000);
    }

    private void unregisterSpnChangedMoniter() {
        if (this.mSpnStateChangedReceiver != null) {
            this.mContext.unregisterReceiver(this.mSpnStateChangedReceiver);
            this.mSpnStateChangedReceiver = null;
        }
    }

    private void registerCardNameModifiedMoniter() {
        if (this.mCardNameModifiedReceiver == null) {
            this.mCardNameModifiedReceiver = new HwDualCardNameChangedReceiver();
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.huawei.android.dsdscardmanager.SIM_CARD_NAME_SUB1_UPDATED_ACTION");
        filter.addAction("com.huawei.android.dsdscardmanager.SIM_CARD_NAME_SUB2_UPDATED_ACTION");
        this.mContext.registerReceiver(this.mCardNameModifiedReceiver, filter);
    }

    private void unregisterCardNameModifiedMoniter() {
        if (this.mCardNameModifiedReceiver != null) {
            this.mContext.unregisterReceiver(this.mCardNameModifiedReceiver);
            this.mCardNameModifiedReceiver = null;
        }
    }

    private String getCardNameString(Context context, int subId) {
        String cardNameInDB = readCardNameFromDatabase(subId, context);
        if (!isCardNameEmpty(cardNameInDB)) {
            return cardNameInDB;
        }
        if (!isCardNameEmpty(this.mSpnCardNames[subId])) {
            return this.mSpnCardNames[subId];
        }
        String readNameFromNetWork = null;
        try {
            readNameFromNetWork = MmsApp.getDefaultMSimTelephonyManager().getNetworkOperatorName(subId);
        } catch (Exception e) {
            MLog.e("Mms::HwDualCardNameHelper", "get network operation name has an  Exception >>>" + e);
        }
        if (isCardNameEmpty(readNameFromNetWork)) {
            return context.getResources().getString(R.string.mms_name_no_service);
        }
        return readNameFromNetWork;
    }

    private String readCardNameFromDatabase(int subId, Context context) {
        MSimTelephonyManager simTelMgr = MmsApp.getDefaultMSimTelephonyManager();
        if (simTelMgr == null) {
            return MmsApp.getDefaultTelephonyManager().getNetworkOperatorName();
        }
        String str = null;
        try {
            str = simTelMgr.getSubscriberId(subId);
        } catch (Exception e) {
            MLog.e("Mms::HwDualCardNameHelper", "Read IMSI Number Exception");
        }
        String cardName = null;
        if (str != null) {
            try {
                cardName = Global.getString(context.getContentResolver(), "sim_card_name_" + MessageUtils.encode(str));
                if (TextUtils.isEmpty(cardName)) {
                    cardName = Global.getString(context.getContentResolver(), "sim_card_name_" + str);
                }
            } catch (Exception e2) {
                MLog.e("Mms::HwDualCardNameHelper", "read Settings.Global Exception " + e2);
            }
        }
        return cardName;
    }

    private static boolean isCardNameEmpty(String name) {
        if (name == null || name.isEmpty() || name.equalsIgnoreCase("null") || name.equals("0")) {
            return true;
        }
        return false;
    }

    private boolean refreshSpnCardNameFromBroadcast(Context context, Intent intent, int subId) {
        boolean needRefresh = true;
        if (!isCardNameEmpty(readCardNameFromDatabase(subId, context))) {
            needRefresh = false;
        }
        String name = "";
        boolean showSpn = intent.getBooleanExtra("showSpn", false);
        boolean showPlmn = intent.getBooleanExtra("showPlmn", false);
        String spn = intent.getStringExtra("spn");
        String plmn = intent.getStringExtra("plmn");
        StringBuilder str = new StringBuilder();
        boolean something = false;
        if (showPlmn && plmn != null) {
            str.append(plmn);
            something = true;
        }
        if (showSpn && spn != null) {
            if (something) {
                str.append('|');
            }
            str.append(spn);
        }
        name = str.toString();
        MLog.v("Mms::HwDualCardNameHelper", "name of card" + subId + " from Broadcast is " + name);
        if (!isCardNameEmpty(name) && name.equals(this.mSpnCardNames[subId])) {
            return false;
        }
        this.mSpnCardNames[subId] = name;
        return needRefresh;
    }
}
