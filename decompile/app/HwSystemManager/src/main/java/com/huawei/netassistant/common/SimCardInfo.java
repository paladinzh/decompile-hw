package com.huawei.netassistant.common;

import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.util.HwLog;

public class SimCardInfo {
    private static final String TAG = SimCardInfo.class.getSimpleName();
    private String mIMSI;
    private boolean mIsMainCard;
    private String mOperatorName;
    private int mSimCardState;

    public SimCardInfo() {
        this.mIMSI = null;
        this.mOperatorName = null;
        this.mSimCardState = 0;
        this.mIsMainCard = false;
    }

    public SimCardInfo(String imsi, String name, int state, boolean ismain) {
        this.mIMSI = imsi;
        this.mOperatorName = name;
        this.mSimCardState = state;
        this.mIsMainCard = ismain;
    }

    public SimCardInfo(SimCardInfo info) {
        if (info != null) {
            this.mIMSI = info.mIMSI;
            this.mOperatorName = info.mOperatorName;
            this.mSimCardState = info.mSimCardState;
            this.mIsMainCard = info.mIsMainCard;
        }
    }

    public String getImsiNumber() {
        return this.mIMSI;
    }

    public void setOperatorName(String name) {
        this.mOperatorName = name;
    }

    public String getOperatorName() {
        return this.mOperatorName;
    }

    public int getSimCardState() {
        return this.mSimCardState;
    }

    public boolean getMainCard() {
        return this.mIsMainCard;
    }

    public boolean isCardActive(int slot) {
        int state = 0;
        try {
            state = TelephonyManager.from(GlobalContext.getContext()).getSimState(getSubId(slot));
        } catch (Exception e) {
            e.printStackTrace();
        }
        HwLog.d(TAG, "sim state is " + state + " slot = " + slot);
        if (state != 0) {
            return true;
        }
        return false;
    }

    private int getSubId(int slotId) {
        int[] subIds = SubscriptionManager.getSubId(slotId);
        if (subIds != null && subIds.length > 0) {
            return subIds[0];
        }
        HwLog.d(TAG, "get subid is null");
        return 0;
    }
}
