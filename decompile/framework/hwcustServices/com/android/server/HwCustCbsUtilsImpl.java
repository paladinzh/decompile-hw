package com.android.server;

import android.os.SystemProperties;
import android.telephony.MSimTelephonyManager;
import android.text.TextUtils;

public class HwCustCbsUtilsImpl extends HwCustCbsUtils {
    private static final String CELLBROADCAST_PACKAGE = "com.android.cellbroadcastreceiver";
    private static final int SIM_MCC_LEN = 3;

    public boolean isNotAllowPkg(String currentVibrationPkg) {
        return CELLBROADCAST_PACKAGE.equals(currentVibrationPkg) ? isChile() : false;
    }

    private boolean isChile() {
        return isCustSimOperator(SystemProperties.get("ro.config.hw_cbs_mcc"));
    }

    private boolean isCustSimOperator(String mCustMccForCBSPreference) {
        if (TextUtils.isEmpty(mCustMccForCBSPreference)) {
            return false;
        }
        boolean flag;
        if (!MSimTelephonyManager.getDefault().isMultiSimEnabled()) {
            flag = isCustPlmn(mCustMccForCBSPreference, MSimTelephonyManager.getDefault().getSimOperator());
        } else if (isCustPlmn(mCustMccForCBSPreference, MSimTelephonyManager.getDefault().getSimOperator(0))) {
            flag = true;
        } else {
            flag = isCustPlmn(mCustMccForCBSPreference, MSimTelephonyManager.getDefault().getSimOperator(1));
        }
        return flag;
    }

    private boolean isCustPlmn(String custPlmnsString, String simMccMnc) {
        if (!(simMccMnc == null || simMccMnc.length() < SIM_MCC_LEN || custPlmnsString == null)) {
            String[] custPlmns = custPlmnsString.split(";");
            int i = 0;
            while (i < custPlmns.length) {
                if (simMccMnc.substring(0, SIM_MCC_LEN).equals(custPlmns[i]) || simMccMnc.equals(custPlmns[i])) {
                    return true;
                }
                i++;
            }
        }
        return false;
    }
}
