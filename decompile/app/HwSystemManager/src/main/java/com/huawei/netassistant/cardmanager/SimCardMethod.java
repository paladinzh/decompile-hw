package com.huawei.netassistant.cardmanager;

import android.content.Context;
import android.telephony.TelephonyManager;
import com.hsm.netmanager.M2NAdapter;
import com.huawei.netassistant.common.PhoneSimCardInfo;
import com.huawei.netassistant.common.SimCardInfo;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.util.HwLog;

public class SimCardMethod {
    public static final int SLOT0 = 0;
    public static final int SLOT1 = 1;
    public static final int SLOT_ERROR = -1;
    public static final String TAG = "SimCardMethod";

    private static TelephonyManager getDefault() {
        TelephonyManager tm = null;
        try {
            tm = TelephonyManager.from(GlobalContext.getContext());
        } catch (Exception e) {
            HwLog.e(TAG, "get telephony manager fail." + e);
        }
        return tm;
    }

    public static int getSimCardType(Context context) {
        TelephonyManager tm = getDefault();
        if (tm == null) {
            HwLog.e(TAG, "/getSimCardType Get MSimTelephonyManager faild");
            return -1;
        } else if (tm.isMultiSimEnabled()) {
            return 2;
        } else {
            return 1;
        }
    }

    public static String getSubscriberId(Context context, int simId) {
        return getDefault().getSubscriberId(simId);
    }

    public static SimCardInfo getSimCardInfo(Context context, int simId) {
        TelephonyManager tm = getDefault();
        if (tm == null) {
            HwLog.e(TAG, "/getSimCardInfo: Get MSimTelephonyManager faild");
            return null;
        }
        String imsi = getSubscriberId(context, simId);
        if (!tm.isMultiSimEnabled()) {
            imsi = getActiveSubscriberId(context);
        }
        if (imsi == null) {
            HwLog.e(TAG, "/getSimCardInfo: imsi is null");
            return null;
        }
        String operator = "";
        int state = getSimState(context, simId);
        boolean isMain = false;
        if (imsi.equals(getPreferredDataSubscriberId())) {
            isMain = true;
        }
        return new SimCardInfo(imsi, operator, state, isMain);
    }

    public static String getActiveSubscriberId(Context context) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService("phone");
        if (tm != null) {
            return tm.getSubscriberId();
        }
        HwLog.e(TAG, "/getSimCardType Get MSimTelephonyManager faild");
        return null;
    }

    public static String getNetworkOperator(Context context, int subscription) {
        TelephonyManager tm = getDefault();
        if (tm != null) {
            return tm.getNetworkOperatorName(subscription);
        }
        HwLog.e(TAG, "/getSimCardType Get MSimTelephonyManager faild");
        return null;
    }

    public static int getSimState(Context context, int subscription) {
        TelephonyManager tm = getDefault();
        if (tm != null) {
            return tm.getSimState(subscription);
        }
        HwLog.e(TAG, "/getSimCardType Get MSimTelephonyManager faild");
        return 0;
    }

    public static String getPreferredDataSubscriberId() {
        TelephonyManager tm = getDefault();
        if (tm == null) {
            HwLog.e(TAG, "/getPreferredDataSubscription: Get MSimTelephonyManager faild");
            return null;
        } else if (!tm.isMultiSimEnabled()) {
            return getActiveSubscriberId(GlobalContext.getContext());
        } else {
            int slot = -1;
            try {
                slot = M2NAdapter.getDefaultDataSubscriptionId();
            } catch (NoSuchMethodError e) {
                HwLog.e(TAG, "getPreferredDataSubscription failed: NoSuchMethodError");
            } catch (Exception e2) {
                HwLog.e(TAG, "getPreferredDataSubscription failed: unknown exception", e2);
            }
            if (slot <= 1 && slot >= 0) {
                return tm.getSubscriberId(slot);
            }
            HwLog.e(TAG, "/getPreferredDataSubscription: invalid slot");
            return null;
        }
    }

    public static String getDefaultSmsSubscriberId() {
        TelephonyManager tm = getDefault();
        if (tm == null) {
            HwLog.e(TAG, "/getDefaultSubscriberId: Get MSimTelephonyManager faild");
            return null;
        }
        int slot = -1;
        try {
            slot = M2NAdapter.getDefaultSmsSubscriptionId();
        } catch (NoSuchMethodError e) {
            HwLog.e(TAG, "getDefaultSubscriberId failed: NoSuchMethodError");
        } catch (Exception e2) {
            HwLog.e(TAG, "getDefaultSubscriberId failed: unknown exception", e2);
        }
        if (slot <= 1 && slot >= 0) {
            return tm.getSubscriberId(slot);
        }
        HwLog.e(TAG, "/getDefaultSubscriberId: invalid slot");
        return null;
    }

    public static int getSimCardSlotNum(Context context, String imsi) {
        if (imsi == null) {
            HwLog.e(TAG, "/getSimCardSlotNum: ismi is null");
            return -1;
        }
        String imsiSlot0 = getSubscriberId(context, 0);
        String imsiSlot1 = getSubscriberId(context, 1);
        int slot = -1;
        if (imsi.equals(imsiSlot0)) {
            slot = 0;
        } else if (imsi.equals(imsiSlot1)) {
            slot = 1;
        } else {
            HwLog.e(TAG, "/getSimCardSlotNum: no slot match ismi");
        }
        return slot;
    }

    public static PhoneSimCardInfo getPhoneSimCardInfo(Context context) {
        PhoneSimCardInfo phoneCardInfo = new PhoneSimCardInfo();
        int cardType = getSimCardType(context);
        phoneCardInfo.setSimCardType(cardType);
        switch (cardType) {
            case -1:
                HwLog.w(TAG, "/getPhoneSimCardInfo: sim card type unknown");
                phoneCardInfo.setPhoneCardState(-1);
                break;
            case 1:
                setPhoneCardInfoForSingleCard(context, phoneCardInfo);
                break;
            case 2:
                setPhoneCardInfoForDualCard(context, phoneCardInfo);
                break;
        }
        return phoneCardInfo;
    }

    private static void setPhoneCardInfoForSingleCard(Context context, PhoneSimCardInfo phoneCardInfo) {
        SimCardInfo cardInfo = getSimCardInfo(context, 0);
        if (cardInfo == null) {
            HwLog.e(TAG, "/getPhoneSimCardInfo: single card not ready");
            phoneCardInfo.setPhoneCardState(1);
            return;
        }
        int phoneState = 1;
        if (5 == cardInfo.getSimCardState()) {
            phoneState = 2;
        }
        phoneCardInfo.setPhoneCardState(phoneState);
        phoneCardInfo.setCardInfoSlot0(cardInfo);
    }

    private static void setPhoneCardInfoForDualCard(Context context, PhoneSimCardInfo phoneCardInfo) {
        SimCardInfo cardInfoSlot0 = getSimCardInfo(context, 0);
        SimCardInfo cardInfoSlot1 = getSimCardInfo(context, 1);
        if (cardInfoSlot0 == null && cardInfoSlot1 == null) {
            HwLog.e(TAG, "/getPhoneSimCardInfo: boths card not recongnized");
            phoneCardInfo.setPhoneCardState(3);
            return;
        }
        int phoneState;
        if (cardInfoSlot0 != null && cardInfoSlot1 != null) {
            phoneState = doNocardInfo(cardInfoSlot0, cardInfoSlot1);
        } else if (cardInfoSlot0 == null || cardInfoSlot1 != null) {
            if (5 == cardInfoSlot1.getSimCardState()) {
                phoneState = 5;
            } else {
                phoneState = 3;
            }
        } else if (5 == cardInfoSlot0.getSimCardState()) {
            phoneState = 4;
        } else {
            phoneState = 3;
        }
        phoneCardInfo.setPhoneCardState(phoneState);
        phoneCardInfo.setCardInfoSlot0(cardInfoSlot0);
        phoneCardInfo.setCardInfoSlot1(cardInfoSlot1);
    }

    private static int doNocardInfo(SimCardInfo cardInfoSlot0, SimCardInfo cardInfoSlot1) {
        int stateSlot0 = cardInfoSlot0.getSimCardState();
        int stateSlot1 = cardInfoSlot1.getSimCardState();
        if (5 == stateSlot0 && 5 == stateSlot1) {
            return 6;
        }
        if (5 != stateSlot0 && 5 != stateSlot1) {
            return 3;
        }
        if (5 == stateSlot0) {
            return 4;
        }
        return 5;
    }
}
