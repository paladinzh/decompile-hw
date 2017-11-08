package com.android.contacts;

import android.os.SystemProperties;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import java.util.ArrayList;
import java.util.Locale;

public class HwCustContactsUtilsImpl extends HwCustContactsUtils {
    private static final String ALL = "all";
    private static final int KEYMEMBERLENGTH = 3;
    private static final int KEYMEMBERLENGTH2 = 4;
    private static final String PROPERTY_GLOBAL_OPERATOR_NUMERIC = "ril.operator.numeric";
    private static String Po_mccmnc = null;
    private static final String TAG = "HwCustContactsUtilsImpl";
    private static String language;

    public ArrayList<String> getHotlineNumber(String[] hotlineNumber, String hwNumber) {
        ArrayList<String> strlist = new ArrayList();
        String preName = null;
        if (hotlineNumber == null) {
            return null;
        }
        if (hotlineNumber.length != 2) {
            return null;
        }
        String mcc_mnc = TelephonyManager.getDefault().getSimOperator();
        if (hwNumber.contains(":")) {
            String[] split = hotlineNumber[0].split("[:]");
            if (3 == split.length || 4 == split.length) {
                boolean noLanguageLimitOrElse;
                String hwLanguage = split[0].trim();
                String hwMccmnc = split[1].trim();
                hwNumber = split[2].trim();
                language = Locale.getDefault().getLanguage();
                if (ALL.contains(hwLanguage)) {
                    noLanguageLimitOrElse = true;
                } else {
                    noLanguageLimitOrElse = language.contains(hwLanguage);
                }
                boolean noSimLimitOrElse = !ALL.contains(hwMccmnc) ? mcc_mnc.contains(hwMccmnc) && TelephonyManager.getDefault().getSimState() == 5 : true;
                if (4 == split.length) {
                    if (CallInterceptDetails.BRANDED_STATE.equals(split[3].trim()) && (TextUtils.isEmpty(mcc_mnc) || !TelephonyManager.getDefault().hasIccCard())) {
                        mcc_mnc = SystemProperties.get(PROPERTY_GLOBAL_OPERATOR_NUMERIC, "");
                        setPoMccmnc(mcc_mnc);
                        if (!TextUtils.isEmpty(mcc_mnc)) {
                            if (ALL.contains(hwMccmnc)) {
                                noSimLimitOrElse = true;
                            } else {
                                noSimLimitOrElse = mcc_mnc.startsWith(hwMccmnc);
                            }
                        }
                    }
                }
                if (noLanguageLimitOrElse && r5) {
                    preName = hotlineNumber[1];
                }
            } else {
                preName = hotlineNumber[1];
            }
        } else {
            preName = hotlineNumber[1];
        }
        Log.d(TAG, "prename--:" + preName);
        strlist.add(preName);
        strlist.add(hwNumber);
        return strlist;
    }

    private void setPoMccmnc(String mccmnc) {
        Po_mccmnc = mccmnc;
    }

    public boolean isReinitHotlineNumber() {
        String currentlanuage = Locale.getDefault().getLanguage();
        boolean needReinitForOperatorChange = (Po_mccmnc == null || SystemProperties.get(PROPERTY_GLOBAL_OPERATOR_NUMERIC, "").equals(Po_mccmnc)) ? false : true;
        boolean needReinitForLanguageChange = (language == null || currentlanuage.equals(language)) ? false : true;
        return !needReinitForOperatorChange ? needReinitForLanguageChange : true;
    }
}
