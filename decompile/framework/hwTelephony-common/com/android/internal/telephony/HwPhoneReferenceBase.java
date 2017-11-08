package com.android.internal.telephony;

import android.os.AsyncResult;
import android.os.Message;
import android.os.SystemProperties;
import android.telephony.Rlog;
import android.telephony.TelephonyManager;

public abstract class HwPhoneReferenceBase {
    private static String LOG_TAG = "HwPhoneReferenceBase";
    private GsmCdmaPhone mGsmCdmaPhone;
    private String subTag = (LOG_TAG + "[" + this.mGsmCdmaPhone.getPhoneId() + "]");

    public HwPhoneReferenceBase(GsmCdmaPhone phone) {
        this.mGsmCdmaPhone = phone;
    }

    public boolean beforeHandleMessage(Message msg) {
        logd("beforeHandleMessage what = " + msg.what);
        boolean msgHandled = true;
        switch (msg.what) {
            case 104:
                AsyncResult ar = msg.obj;
                setEccNumbers((String) ar.result);
                logd("Handle EVENT_ECC_NUM:" + ((String) ar.result));
                break;
            default:
                msgHandled = false;
                if (msg.what >= 100) {
                    msgHandled = true;
                }
                if (!msgHandled) {
                    logd("unhandle event");
                    break;
                }
                break;
        }
        return msgHandled;
    }

    private void logd(String msg) {
        Rlog.d(this.subTag, msg);
    }

    private void loge(String msg) {
        Rlog.e(this.subTag, msg);
    }

    private void setEccNumbers(String value) {
        try {
            if (!needSetEccNumbers()) {
                value = "";
            }
            if (this.mGsmCdmaPhone.getSubId() <= 0) {
                SystemProperties.set("ril.ecclist", value);
            } else {
                SystemProperties.set("ril.ecclist1", value);
            }
        } catch (RuntimeException e) {
            loge("setEccNumbers RuntimeException: " + e);
        } catch (Exception e2) {
            loge("setEccNumbers Exception: " + e2);
        }
    }

    private boolean needSetEccNumbers() {
        boolean z = false;
        if (!TelephonyManager.getDefault().isMultiSimEnabled() || !SystemProperties.getBoolean("ro.config.hw_ecc_with_sim_card", false)) {
            return true;
        }
        boolean hasPresentCard = false;
        for (int i = 0; i < TelephonyManager.getDefault().getSimCount(); i++) {
            if (TelephonyManager.getDefault().getSimState(i) != 1) {
                hasPresentCard = true;
                break;
            }
        }
        int slotId = SubscriptionController.getInstance().getSlotId(this.mGsmCdmaPhone.getSubId());
        logd("needSetEccNumbers  slotId = " + slotId + " hasPresentCard = " + hasPresentCard);
        if (!(hasPresentCard && TelephonyManager.getDefault().getSimState(slotId) == 1)) {
            z = true;
        }
        return z;
    }
}
