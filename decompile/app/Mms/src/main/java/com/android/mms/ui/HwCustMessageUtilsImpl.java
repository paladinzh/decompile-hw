package com.android.mms.ui;

import android.telephony.MSimTelephonyManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import com.android.mms.HwCustMmsConfigImpl;
import com.android.mms.MmsApp;
import com.android.mms.data.Contact;
import com.huawei.cspcommon.MLog;
import com.huawei.mms.util.MccMncConfig;
import java.util.HashMap;

public class HwCustMessageUtilsImpl extends HwCustMessageUtils {
    public static final String MCCMNC_PLAY = "26006";
    public static final String MCC_POLAND = "260";
    private static final String TAG = "HwCustMessageUtilsImpl";
    private static HashMap<String, String> custPredefinedSMSCMap = new HashMap();

    public boolean configRoamingNationalAsLocal() {
        return HwCustMmsConfigImpl.getConfigRoamingNationalAsLocal();
    }

    public boolean isRoamingNationalP4(int subscription) {
        String simMccMnc = MSimTelephonyManager.getDefault().getSimOperator(subscription);
        String networkMccMnc = MSimTelephonyManager.getDefault().getNetworkOperator(subscription);
        MLog.i(TAG, "simMccMnc = " + simMccMnc);
        MLog.i(TAG, "networkMccMnc = " + networkMccMnc);
        if (MccMncConfig.isValideOperator(simMccMnc) && MccMncConfig.isValideOperator(networkMccMnc) && simMccMnc.equals(MCCMNC_PLAY) && !simMccMnc.equals(networkMccMnc) && networkMccMnc.startsWith(MCC_POLAND)) {
            return true;
        }
        return false;
    }

    public boolean isPoundChar(StringBuilder builder, char c) {
        if (!HwCustMmsConfigImpl.isPoundCharValid() || (c != '#' && c != '*')) {
            return false;
        }
        builder.append(c);
        return true;
    }

    public static HashMap<String, String> parseConfigToMap(String configStr) {
        if (TextUtils.isEmpty(configStr)) {
            MLog.w(TAG, "parseConfigToMap,the configStr is empty!");
            return null;
        }
        HashMap<String, String> custMap = new HashMap();
        String[] units = configStr.split(";");
        if (units.length > 0) {
            for (int i = 0; i < units.length; i++) {
                if (!TextUtils.isEmpty(units[i])) {
                    String[] custUnit = units[i].split(":");
                    if (custUnit.length == 2) {
                        custMap.put(custUnit[0], custUnit[1]);
                    }
                }
            }
        }
        return custMap;
    }

    public static String getOperatorMccMnc(int subID) {
        TelephonyManager tm = (TelephonyManager) MmsApp.getApplication().getSystemService("phone");
        if (subID < 0) {
            return tm.getSimOperator();
        }
        return tm.getSimOperator(subID);
    }

    public static String getCustReplaceSmsCenterNumber(int subID) {
        if (custPredefinedSMSCMap != null && custPredefinedSMSCMap.size() == 0) {
            custPredefinedSMSCMap = parseConfigToMap(HwCustMmsConfigImpl.getCustReplaceSMSCAddressByCard());
        }
        if (custPredefinedSMSCMap == null || custPredefinedSMSCMap.size() <= 0) {
            return null;
        }
        String currentMccmnc = getOperatorMccMnc(subID);
        if (TextUtils.isEmpty(currentMccmnc) || !custPredefinedSMSCMap.containsKey(currentMccmnc)) {
            return null;
        }
        return (String) custPredefinedSMSCMap.get(currentMccmnc);
    }

    public String getContactName(String addr) {
        if (!HwCustMmsConfigImpl.isEnableContactName() || TextUtils.isEmpty(addr)) {
            return addr;
        }
        String contact = null;
        if (addr != null) {
            Contact c = Contact.get(addr, false);
            if (c != null) {
                contact = c.getName();
            }
        }
        if (contact != null) {
            return contact;
        }
        return addr;
    }

    public boolean isAlwaysShowSmsOptimization(String mccmnc) {
        String custMcc = HwCustMmsConfigImpl.getMccDefault7BitOff();
        boolean isShowFlag = false;
        if (TextUtils.isEmpty(custMcc)) {
            return false;
        }
        for (String mcc : custMcc.split(",")) {
            if (mccmnc.startsWith(mcc)) {
                isShowFlag = true;
                break;
            }
        }
        MLog.i(TAG, "Settings show 7bit off item flag is " + isShowFlag);
        return isShowFlag;
    }
}
