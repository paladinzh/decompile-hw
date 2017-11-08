package com.android.contacts.speeddial;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import com.android.contacts.hap.CommonUtilMethods;
import com.huawei.android.provider.SettingsEx.Systemex;

public class HwCustSpeedDialerFragmentImpl extends HwCustSpeedDialerFragment {
    private static final int SPEED_DIALER_SLOT1 = 0;

    public HwCustSpeedDialerFragmentImpl(Context context) {
        super(context);
    }

    public Boolean isDisableCustomService() {
        String mcc_mnc = TelephonyManager.getDefault().getSimOperator();
        boolean result = true;
        String teleConfigString = Systemex.getString(this.mContext.getContentResolver(), "telefonica_custom_service");
        if (!TextUtils.isEmpty(teleConfigString)) {
            String[] custValues = teleConfigString.trim().split(",");
            int size = custValues.length;
            int i = 0;
            while (i < size) {
                if (!TextUtils.isEmpty(mcc_mnc) && mcc_mnc.equals(custValues[i])) {
                    result = false;
                    break;
                }
                i++;
            }
        } else {
            result = false;
        }
        return Boolean.valueOf(result);
    }

    public String getPredefinedSpeedDialNumbersByMccmnc(String singlePair) {
        if (!TextUtils.isEmpty(singlePair)) {
            String myMcc_mnc = "";
            boolean isFirstSimEnabled = CommonUtilMethods.getFirstSimEnabled();
            String[] tempPair = singlePair.split(":");
            if (tempPair.length <= 1) {
                return singlePair;
            }
            if (isFirstSimEnabled) {
                myMcc_mnc = TelephonyManager.getDefault().getSimOperator(0);
            } else {
                myMcc_mnc = TelephonyManager.getDefault().getSimOperator();
            }
            for (String mccmnc : tempPair[1].split("\\|")) {
                if (mccmnc.equals(myMcc_mnc)) {
                    return tempPair[0];
                }
            }
        }
        return "";
    }

    public boolean isPredefinedSpeedNumberEditable() {
        String mcc_mnc = "";
        if (CommonUtilMethods.getFirstSimEnabled()) {
            mcc_mnc = TelephonyManager.getDefault().getSimOperator(0);
        } else {
            mcc_mnc = TelephonyManager.getDefault().getSimOperator();
        }
        Log.d("HwCustSpeedDialerFragmentImpl", "mcc_mnc:" + mcc_mnc);
        String editableString = Systemex.getString(this.mContext.getContentResolver(), "predefined_speednum_editable");
        if (!TextUtils.isEmpty(editableString)) {
            if ("false".equals(editableString)) {
                return false;
            }
            if ("true".equals(editableString)) {
                return true;
            }
            String[] editablePairs = editableString.split(",");
            int size = editablePairs.length;
            int i = 0;
            while (i < size) {
                if (!TextUtils.isEmpty(mcc_mnc) && mcc_mnc.equals(editablePairs[i])) {
                    return false;
                }
                i++;
            }
        }
        return true;
    }
}
