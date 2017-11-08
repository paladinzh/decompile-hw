package com.android.contacts.hap.sim;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemProperties;
import android.text.TextUtils;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.hap.EmuiFeatureManager;
import com.android.contacts.hap.sprint.preload.HwCustPreloadContacts;
import com.android.contacts.util.ExceptionCapture;
import com.android.contacts.util.HwLog;
import com.huawei.android.telephony.IIccPhoneBookManagerEx;
import com.huawei.android.util.NoExtAPIException;
import com.huawei.contact.util.SettingsWrapper;

public class SimStateChangeReceiver extends BroadcastReceiver {
    private static boolean mSim1LoadedFlag = false;
    private static boolean mSim2LoadedFlag = false;

    public void onReceive(Context context, Intent intent) {
        if (HwLog.HWFLOW) {
            HwLog.i("SimStateChangeReceiver", "Broadcast received for SIM state changed");
        }
        if (context.checkSelfPermission("android.permission.READ_PHONE_STATE") == 0) {
            handleReceivedBroadCast(context, intent);
        }
    }

    @SuppressLint({"HwHardCodeDateFormat"})
    private void handleReceivedBroadCast(Context context, Intent intent) {
        if (intent != null && intent.getAction() != null) {
            int slotId = -1;
            String lSimState = null;
            String action = intent.getAction();
            if ("android.intent.action.SIM_STATE_CHANGED".equals(action)) {
                lSimState = intent.getStringExtra("ss");
                try {
                    slotId = IIccPhoneBookManagerEx.getDefault().getSoltIdInSimStateChangeIntent(intent);
                } catch (NoExtAPIException e) {
                    e.printStackTrace();
                    ExceptionCapture.captureReadSoltIdException("Call getSoltIdInSimStateChangeIntent error", e);
                }
            } else if ("android.intent.action.ACTION_SUBINFO_CONTENT_CHANGE".equals(action)) {
                slotId = intent.getIntExtra("_id", -1);
                if (slotId == -1) {
                    slotId = (int) intent.getLongExtra("_id", -1);
                }
                if (slotId == -1) {
                    slotId = 0;
                }
                lSimState = intent.getIntExtra("intContent", -1) + "";
            } else if ("android.intent.action.ACTION_SUBSCRIPTION_SET_UICC_RESULT".equals(action) && intent.getIntExtra("operationResult", 1) == 0) {
                int subId = intent.getIntExtra("subscription", -1);
                slotId = subId;
                SimFactoryManager.notifySimStateChanged(subId);
                if (isSimHasLoaded(subId)) {
                    lSimState = intent.getIntExtra("newSubState", 0) + "";
                    HwLog.i("SimStateChangeReceiver", "SIM is active is slowly lSimState:" + lSimState + "  slotId:" + subId);
                }
                handleSimStateChangeReceiver(context, lSimState, subId);
                setSimHasLoaded(subId, false);
            }
            if (HwLog.HWFLOW) {
                HwLog.i("SimStateChangeReceiver", "Broadcast received for SIM state changed With slot Id:" + slotId + ", action:" + action + ", SIMState: " + lSimState);
            }
            if (!SimFactoryManager.isCdma(SimFactoryManager.getSubscriptionIdBasedOnSlot(slotId)) || !SystemProperties.get("ro.config.cdma_subscription", "").equals(CallInterceptDetails.BRANDED_STATE) || SystemProperties.get("telephony.lteOnCdmaDevice", "").equals(CallInterceptDetails.BRANDED_STATE)) {
                if ("android.intent.action.SIM_STATE_CHANGED".equals(action) || ("android.intent.action.ACTION_SUBINFO_CONTENT_CHANGE".equals(action) && "sub_state".equals(intent.getStringExtra("columnName")))) {
                    if (HwLog.HWFLOW) {
                        HwLog.i("SimStateChangeReceiver", "SIM State broadcast is received about to start SIM handler Service with state:" + lSimState + ",Slot Id:" + slotId);
                    }
                    if (lSimState != null && lSimState.equals("LOADED")) {
                        setSimHasLoaded(slotId, true);
                    }
                    if (lSimState != null && lSimState.equals("ABSENT")) {
                        setSimHasLoaded(slotId, false);
                    }
                    if ("LOADED".equals(lSimState) && SimFactoryManager.isSimFactoryInit(slotId)) {
                        SimFactoryManager.getSharedPreferences("SimInfoFile", slotId).edit().remove("sim_max_limit").apply();
                        if (HwLog.HWFLOW) {
                            HwLog.i("SimStateChangeReceiver", "SIM_LOADED clear max size for reload");
                        }
                    }
                    if (HwLog.HWFLOW) {
                        HwLog.i("SimStateChangeReceiver", "SIM State change receive broadcast with state:" + lSimState + ", Slot Id:" + slotId);
                    }
                    handleSimStateChangeReceiver(context, lSimState, slotId);
                    if ("android.intent.action.SIM_STATE_CHANGED".equals(action) && "LOADED".equals(intent.getStringExtra("ss"))) {
                        if (SystemProperties.getBoolean("ro.config.hw_opt_pre_contact", false)) {
                            CommonUtilMethods.predefineCust(context, true);
                        }
                        storeRussiaNumberEnable(context, intent, slotId);
                    }
                }
            }
        }
    }

    private static boolean isSimHasLoaded(int sub) {
        if (1 == sub) {
            return mSim2LoadedFlag;
        }
        return mSim1LoadedFlag;
    }

    private static void setSimHasLoaded(int sub, boolean flag) {
        if (1 == sub) {
            mSim2LoadedFlag = flag;
        } else {
            mSim1LoadedFlag = flag;
        }
    }

    private void handleSimStateChangeReceiver(Context context, String state, int sub) {
        if (context == null) {
            HwLog.w("SimStateChangeReceiver", "Context is NULL");
            return;
        }
        if (HwLog.HWFLOW) {
            HwLog.i("SimStateChangeReceiver", "handleSimStateChangeReceiver");
        }
        Intent mIntent = new Intent();
        mIntent.setAction("com.huawei.settings.HANDLE_PHONESTATE");
        mIntent.putExtra("simstate", state);
        mIntent.putExtra("subscription", sub);
        mIntent.setPackage(context.getPackageName());
        context.startService(mIntent);
    }

    private void storeRussiaNumberEnable(Context context, Intent intent, int currentSlotId) {
        String mRussiaNumberRelevance = HwCustPreloadContacts.EMPTY_STRING;
        if (EmuiFeatureManager.isSupportRussiaNumberRelevance()) {
            mRussiaNumberRelevance = EmuiFeatureManager.getRussiaNumberRelevanceFeatureFlag();
            String firstValue = SettingsWrapper.getString(context.getContentResolver(), "enable_RussiaNumberRelevance");
            EmuiFeatureManager.setRussiaNumberSearchEnabled("true".equals(firstValue));
            if (TextUtils.isEmpty(firstValue)) {
                String firstMccMnc = "";
                Boolean value = Boolean.valueOf(false);
                if (CommonUtilMethods.getTelephonyManager(context).isMultiSimEnabled()) {
                    int otherSlotId;
                    boolean isOtherSimCardPresent;
                    String otherCardMccMnc = HwCustPreloadContacts.EMPTY_STRING;
                    if (currentSlotId == 0) {
                        otherSlotId = 1;
                        isOtherSimCardPresent = SimFactoryManager.checkSIM2CardPresentState();
                    } else {
                        otherSlotId = 0;
                        isOtherSimCardPresent = SimFactoryManager.checkSIM1CardPresentState();
                    }
                    int otherCardState = CommonUtilMethods.getTelephonyManager(context).getSimState(otherSlotId);
                    otherCardMccMnc = CommonUtilMethods.getTelephonyManager(context).getSimOperator(otherSlotId);
                    if (!isOtherSimCardPresent) {
                        value = Boolean.valueOf(checkfirstMccMncMatch(mRussiaNumberRelevance, CommonUtilMethods.getTelephonyManager(context).getSimOperator(currentSlotId)));
                    } else if (5 == otherCardState && !TextUtils.isEmpty(otherCardMccMnc)) {
                        if (checkfirstMccMncMatch(mRussiaNumberRelevance, CommonUtilMethods.getTelephonyManager(context).getSimOperator(currentSlotId))) {
                            value = Boolean.valueOf(true);
                        } else {
                            value = Boolean.valueOf(checkfirstMccMncMatch(mRussiaNumberRelevance, CommonUtilMethods.getTelephonyManager(context).getSimOperator(otherSlotId)));
                        }
                    } else {
                        return;
                    }
                }
                value = Boolean.valueOf(checkfirstMccMncMatch(mRussiaNumberRelevance, CommonUtilMethods.getTelephonyManager(context).getSimOperator()));
                SettingsWrapper.putString(context.getContentResolver(), "enable_RussiaNumberRelevance", String.valueOf(value));
                EmuiFeatureManager.setRussiaNumberSearchEnabled(value.booleanValue());
            }
        }
    }

    private boolean checkfirstMccMncMatch(String mRussiaNumberRelevance, String firstMccMnc) {
        for (String mRussiaNumberRelevanceTemp : mRussiaNumberRelevance.split(",")) {
            if (!(TextUtils.isEmpty(firstMccMnc) || TextUtils.isEmpty(mRussiaNumberRelevanceTemp))) {
                if (3 == mRussiaNumberRelevanceTemp.length() && firstMccMnc.startsWith(mRussiaNumberRelevanceTemp)) {
                    return true;
                }
                if (5 == mRussiaNumberRelevanceTemp.length() && firstMccMnc.equals(mRussiaNumberRelevanceTemp)) {
                    return true;
                }
            }
        }
        return false;
    }
}
