package com.huawei.netassistant.common;

import com.huawei.systemmanager.util.HwLog;

public class PhoneSimCardInfo {
    public static final int DUAL_PHONE_BOTH_CARD_READY = 6;
    public static final int DUAL_PHONE_BOTH_CARD_UNRECOGNIZED = 3;
    public static final int DUAL_PHONE_ONE_READY_SEC_UNRECOGNIZED = 4;
    public static final int DUAL_PHONE_SEC_READY_ONE_UNRECOGNIZED = 5;
    public static final int PHONE_CARD_STATE_UNKNOW = -1;
    public static final int SIM_CARD_TYPE_DUAL = 2;
    public static final int SIM_CARD_TYPE_SINGLE = 1;
    public static final int SIM_CARD_TYPE_UNKNOW = -1;
    public static final int SINGLE_PHONE_CARD_READY = 2;
    public static final int SINGLE_PHONE_CARD_UNRECOGNIZED = 1;
    public static final String TAG = "PhoneSimCardInfo";
    private SimCardInfo mCardInfoSlot0;
    private SimCardInfo mCardInfoSlot1;
    private int mPhoneCardState;
    private int mSimCardType;

    public PhoneSimCardInfo() {
        this.mSimCardType = -1;
        this.mPhoneCardState = -1;
        this.mCardInfoSlot0 = null;
        this.mCardInfoSlot1 = null;
    }

    public PhoneSimCardInfo(PhoneSimCardInfo phoneInfo) {
        if (phoneInfo != null) {
            this.mSimCardType = phoneInfo.mSimCardType;
            this.mPhoneCardState = phoneInfo.mPhoneCardState;
            this.mCardInfoSlot0 = phoneInfo.mCardInfoSlot0;
            this.mCardInfoSlot1 = phoneInfo.mCardInfoSlot1;
        }
    }

    public void setSimCardType(int type) {
        this.mSimCardType = type;
    }

    public int getSimCardType() {
        return this.mSimCardType;
    }

    public void setPhoneCardState(int state) {
        this.mPhoneCardState = state;
    }

    public int getPhoneCardState() {
        return this.mPhoneCardState;
    }

    public void setCardInfoSlot0(SimCardInfo info) {
        this.mCardInfoSlot0 = new SimCardInfo(info);
    }

    public SimCardInfo getCardInfoSlot0() {
        return this.mCardInfoSlot0;
    }

    public void setCardInfoSlot1(SimCardInfo info) {
        this.mCardInfoSlot1 = new SimCardInfo(info);
    }

    public SimCardInfo getCardInfoSlot1() {
        return this.mCardInfoSlot1;
    }

    public SimCardInfo getDataUsedSimCard() {
        if (-1 == this.mSimCardType) {
            HwLog.e(TAG, "/getDataUsedSimCard: get SIM card type faild");
            return null;
        }
        SimCardInfo simCardInfo = null;
        if (1 == this.mSimCardType) {
            HwLog.e(TAG, "/getDataUsedSimCard: SIM card type: single");
            return new SimCardInfo(this.mCardInfoSlot0);
        }
        if (this.mCardInfoSlot0 != null && this.mCardInfoSlot0.getMainCard()) {
            simCardInfo = new SimCardInfo(this.mCardInfoSlot0);
        } else if (this.mCardInfoSlot1 != null && this.mCardInfoSlot1.getMainCard()) {
            simCardInfo = new SimCardInfo(this.mCardInfoSlot1);
        }
        return simCardInfo;
    }
}
