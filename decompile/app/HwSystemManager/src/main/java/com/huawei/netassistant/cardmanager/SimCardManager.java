package com.huawei.netassistant.cardmanager;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.provider.Settings.Global;
import android.text.TextUtils;
import com.google.common.collect.Lists;
import com.huawei.netassistant.common.PhoneSimCardInfo;
import com.huawei.netassistant.common.SimCardInfo;
import com.huawei.netassistant.db.NetAssistantDBManager;
import com.huawei.netassistant.ext.SimCardManagerExt;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.util.HwLog;
import java.util.List;

public class SimCardManager {
    public static final int PACKAGE_STATE_NO_CARD_SET = 3;
    public static final int PACKAGE_STATE_NO_SIMCARD = 1;
    public static final int PACKAGE_STATE_ONECARD_SET = 2;
    private static String TAG = "SimCardManager";
    private static final Object sMutexSimCardManager = new Object();
    private static SimCardManager sSingleton;
    private Context mContext = GlobalContext.getContext();
    private String mNameServSlot0;
    private String mNameServSlot1;
    private String mNameSlot0;
    private String mNameSlot1;
    private BroadcastReceiver mReceiverIntentReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (context != null && intent != null && !TextUtils.isEmpty(intent.getAction())) {
                String action = intent.getAction();
                if ("android.provider.Telephony.SPN_STRINGS_UPDATED".equals(action)) {
                    HwLog.d(SimCardManager.TAG, "action:" + action);
                } else if ("android.intent.action.ACTION_DSDS_SUB1_OPERATOR_CHANGED".equals(action)) {
                    SimCardManager.this.mNameServSlot0 = SimCardManager.this.getOperatorName(intent, 0);
                    HwLog.i(SimCardManager.TAG, "onReceive()-- mNameServSlot0");
                    if (!(TextUtils.isEmpty(SimCardManager.this.mNameServSlot0) || SimCardManager.this.mNameServSlot0.equals(SimCardManagerExt.getOperatorNameOnPreference(0, SimCardManager.this.mContext)))) {
                        SimCardManagerExt.setOperatorNameToPreference(0, SimCardManager.this.mNameServSlot0, SimCardManager.this.mContext);
                    }
                } else if ("android.intent.action.ACTION_DSDS_SUB2_OPERATOR_CHANGED".equals(action)) {
                    SimCardManager.this.mNameServSlot1 = SimCardManager.this.getOperatorName(intent, 1);
                    HwLog.i(SimCardManager.TAG, "onReceive()-- mNameServSlot1");
                    if (!(TextUtils.isEmpty(SimCardManager.this.mNameServSlot1) || SimCardManager.this.mNameServSlot1.equals(SimCardManagerExt.getOperatorNameOnPreference(1, SimCardManager.this.mContext)))) {
                        SimCardManagerExt.setOperatorNameToPreference(1, SimCardManager.this.mNameServSlot1, SimCardManager.this.mContext);
                    }
                }
            }
        }
    };

    private SimCardManager() {
        try {
            this.mContext.registerReceiver(this.mReceiverIntentReceiver, getReveiverIntentFilter());
        } catch (Exception e) {
            HwLog.e(TAG, "/SimCardManager: register receiver faild");
        }
        this.mNameSlot0 = null;
        this.mNameSlot1 = null;
    }

    public static SimCardManager getInstance() {
        SimCardManager simCardManager;
        synchronized (sMutexSimCardManager) {
            if (sSingleton == null) {
                sSingleton = new SimCardManager();
            }
            simCardManager = sSingleton;
        }
        return simCardManager;
    }

    public static void destroyInstance() {
        synchronized (sMutexSimCardManager) {
            sSingleton = null;
        }
    }

    public int getPhoneSimCardState() {
        return getPhoneSimCardInfo().getPhoneCardState();
    }

    public PhoneSimCardInfo getPhoneSimCardInfo() {
        PhoneSimCardInfo info = SimCardMethod.getPhoneSimCardInfo(this.mContext);
        SimCardInfo sim0 = info.getCardInfoSlot0();
        if (sim0 != null) {
            String slot0 = getOperatorNameFromExtPlatform(0);
            if (!TextUtils.isEmpty(slot0)) {
                this.mNameSlot0 = slot0;
            } else if (!TextUtils.isEmpty(this.mNameServSlot0)) {
                this.mNameSlot0 = this.mNameServSlot0;
            }
            sim0.setOperatorName(this.mNameSlot0);
            info.setCardInfoSlot0(sim0);
        }
        SimCardInfo sim1 = info.getCardInfoSlot1();
        if (sim1 != null) {
            String slot1 = getOperatorNameFromExtPlatform(1);
            if (!TextUtils.isEmpty(slot1)) {
                this.mNameSlot1 = slot1;
            } else if (!TextUtils.isEmpty(this.mNameServSlot1)) {
                this.mNameSlot1 = this.mNameServSlot1;
            }
            sim1.setOperatorName(this.mNameSlot1);
            info.setCardInfoSlot1(sim1);
        }
        return info;
    }

    public int getSimCardType() {
        return SimCardMethod.getSimCardType(this.mContext);
    }

    public String getSubscriberId(int simId) {
        return SimCardMethod.getSubscriberId(this.mContext, simId);
    }

    public String getPreferredDataSubscriberId() {
        return SimCardMethod.getPreferredDataSubscriberId();
    }

    public String getDefaultSmsSubscriberId() {
        return SimCardMethod.getDefaultSmsSubscriberId();
    }

    public SimCardInfo getPreferredDataSimCardInfo() {
        String imsi = SimCardMethod.getPreferredDataSubscriberId();
        PhoneSimCardInfo info = getPhoneSimCardInfo();
        if (TextUtils.isEmpty(imsi)) {
            HwLog.e(TAG, "/getPhoneSimCardInfo: no data using SIM card.");
            return null;
        }
        SimCardInfo simCardInfo = null;
        SimCardInfo sim0 = info.getCardInfoSlot0();
        SimCardInfo sim1 = info.getCardInfoSlot1();
        if (sim0 != null && imsi.equals(sim0.getImsiNumber())) {
            simCardInfo = new SimCardInfo(sim0);
        } else if (sim1 != null && imsi.equals(sim1.getImsiNumber())) {
            simCardInfo = new SimCardInfo(sim1);
        }
        return simCardInfo;
    }

    public boolean isPhoneSupportDualCard() {
        if (2 == getSimCardType()) {
            return true;
        }
        return false;
    }

    private IntentFilter getReveiverIntentFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.ACTION_DSDS_SUB1_OPERATOR_CHANGED");
        intentFilter.addAction("android.intent.action.ACTION_DSDS_SUB2_OPERATOR_CHANGED");
        intentFilter.addAction("android.provider.Telephony.SPN_STRINGS_UPDATED");
        return intentFilter;
    }

    private String getOperatorNameFromService(Intent intent) {
        String name = "";
        if (Global.getInt(getContentResolver(), "airplane_mode_on", 0) == 1) {
            HwLog.d(TAG, "/getOperatorNameFromService: in spn action airpalne mode on");
            return this.mContext.getString(R.string.network_no_service);
        }
        String plmn = intent.getStringExtra("plmn");
        String spn = intent.getStringExtra("spn");
        boolean showSpn = intent.getBooleanExtra("showSpn", false);
        boolean showPlmn = intent.getBooleanExtra("showPlmn", false);
        HwLog.d(TAG, "/getOperatorNameFromService: networkName showSpn = " + showSpn + " spn = " + spn + " showPlmn = " + showPlmn + " plmn = " + plmn);
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
        if ("".equals(name)) {
            HwLog.d(TAG, "/getOperatorNameFromService:network name is null,something wrong with network!!!");
            name = this.mContext.getString(R.string.network_no_service);
        }
        HwLog.d(TAG, "/getOperatorNameFromService:network name is: " + name);
        return name;
    }

    private ContentResolver getContentResolver() {
        return this.mContext.getContentResolver();
    }

    private String getOperatorNameFromExtPlatform(int slotId) {
        String imsi = getSubscriberId(slotId);
        if (imsi != null) {
            return SimCardManagerExt.getOperatorNameFromPlatform(slotId, imsi, this.mContext);
        }
        HwLog.d(TAG, "/getOperatorNameFromDB: imsi is null");
        return null;
    }

    private String getOperatorName(Intent intent, int slotId) {
        return getOperatorNameFromService(intent);
    }

    public int getSimcardIndex(String imsi) {
        if (TextUtils.equals(getSubscriberId(0), imsi)) {
            return 0;
        }
        if (TextUtils.equals(getSubscriberId(1), imsi)) {
            return 1;
        }
        return -1;
    }

    public String getSimcardByIndex(int index) {
        if (index == 0) {
            return getSubscriberId(0);
        }
        if (index == 1) {
            return getSubscriberId(1);
        }
        return null;
    }

    public int getSimPackageState() {
        List<SimCardInfo> cardsList = Lists.newArrayList();
        PhoneSimCardInfo info = getPhoneSimCardInfo();
        SimCardInfo cardInfo1 = info.getCardInfoSlot0();
        if (cardInfo1 != null) {
            if (TextUtils.isEmpty(cardInfo1.getImsiNumber())) {
                HwLog.i(TAG, "getSimPackageState cardinfo 1, cardInfo1 != null, but sim is null");
            } else {
                HwLog.i(TAG, "getSimPackageState cardinfo 1 is in");
                cardsList.add(cardInfo1);
            }
        }
        SimCardInfo cardInfo2 = info.getCardInfoSlot1();
        if (cardInfo2 != null) {
            if (TextUtils.isEmpty(cardInfo2.getImsiNumber())) {
                HwLog.i(TAG, "getSimPackageState cardinfo 2, cardInfo2 != null, but sim is null");
            } else {
                HwLog.i(TAG, "getSimPackageState cardinfo 2 is in");
                cardsList.add(cardInfo2);
            }
        }
        if (cardsList.isEmpty()) {
            HwLog.i(TAG, "getSimPackageState no simcard in");
            return 1;
        }
        int size = cardsList.size();
        for (int i = 0; i < size; i++) {
            if (NetAssistantDBManager.getInstance().getSettingTotalPackage(((SimCardInfo) cardsList.get(i)).getImsiNumber()) >= 0) {
                HwLog.i(TAG, "getSimPackageState packageSet >= 0, index:" + i);
                return 2;
            }
        }
        HwLog.i(TAG, "all card package is not set");
        return 3;
    }

    public String getOtherImsi(String imsi) {
        if (getSimcardIndex(imsi) != 0) {
            return getSimcardByIndex(0);
        }
        if (isPhoneSupportDualCard()) {
            return getSimcardByIndex(1);
        }
        HwLog.i(TAG, "not support dual card, getOtherImsi return null");
        return null;
    }

    public String getOpName(String imsi) {
        String opName = SimCardManagerExt.getOperatorNameOnQcom(imsi, getContentResolver());
        if (!TextUtils.isEmpty(opName)) {
            return opName;
        }
        if (getSimcardIndex(imsi) == 0) {
            opName = this.mNameServSlot0;
            if (this.mNameServSlot0 != null) {
                return opName;
            }
            HwLog.i(TAG, "getOpName()--mNameServSlot0 is null,will call SimCardManagerExt.getOperatorNameOnPreference()");
            return SimCardManagerExt.getOperatorNameOnPreference(0, this.mContext);
        } else if (getSimcardIndex(imsi) != 1) {
            return opName;
        } else {
            opName = this.mNameServSlot1;
            if (this.mNameServSlot1 != null) {
                return opName;
            }
            HwLog.i(TAG, "getOpName()--mNameServSlot1 is null,will call SimCardManagerExt.getOperatorNameOnPreference()");
            return SimCardManagerExt.getOperatorNameOnPreference(1, this.mContext);
        }
    }
}
