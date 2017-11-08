package com.android.settings;

import android.content.Context;
import android.net.Uri;
import android.os.SystemProperties;
import android.provider.Settings.System;
import android.provider.SettingsEx.Systemex;
import android.support.v7.preference.PreferenceCategory;
import android.telephony.HwTelephonyManager;
import android.telephony.MSimTelephonyManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import com.huawei.android.telephony.TelephonyManagerEx;
import java.util.ArrayList;
import java.util.Arrays;

public class HwCustApnSettingsHwBaseImpl extends HwCustApnSettingsHwBase {
    protected static final Uri APN_SIM1_URI1 = Uri.parse("content://telephony/carriers_sim1");
    protected static final Uri APN_SIM2_URI1 = Uri.parse("content://telephony/carriers_sim2");
    public static final String APN_URI1 = "content://telephony/carriers_sim1";
    public static final String APN_URI2 = "content://telephony/carriers_sim2";
    private static final int BEARER_EHRPD = 13;
    private static final int BEARER_LTE = 14;
    private static final int CHINA_CARD = 0;
    private static final String CHINA_MCC = "460";
    private static final int CHINA_NETWORK = 0;
    private static final String CHINA_TELECOM_WAP_NETWORK_NAME = "ctwap";
    private static final Uri DEFAULTAPN_URI1 = Uri.parse(RESTORE_CARRIERS_URI1);
    private static final Uri DEFAULTAPN_URI2 = Uri.parse(RESTORE_CARRIERS_URI2);
    private static final String EMPTY_OPERATOR_NUMRIC = "";
    private static final boolean IS_CHINA_TELECOM;
    private static final boolean IS_SPRINT;
    private static final boolean IS_SUPPORT_ORANGE_APN = SystemProperties.getBoolean("ro.config.support_orange_apn", false);
    private static final boolean IS_TRACFONE;
    private static final int MACAU_CARD = 1;
    private static final String MACAU_MCC = "455";
    private static final int MACAU_NETWORK = 1;
    private static final String MACAU_TELECOM_PLMN = "45502";
    private static final int NETWORK_LET_EHRPD = 4;
    private static final int OTHERWISE_CARD = 2;
    private static final int OTHER_NETWORK_CDMA = 2;
    private static final int OTHER_NETWORK_GSM = 3;
    protected static final Uri PREFERAPN_URI1 = Uri.parse(PREFERRED_APN1_URI);
    protected static final Uri PREFERAPN_URI2 = Uri.parse(PREFERRED_APN2_URI);
    public static final String PREFERRED_APN1_URI = "content://telephony/carriers_sim1/preferapn";
    public static final String PREFERRED_APN2_URI = "content://telephony/carriers_sim2/preferapn";
    private static final String PROPERTY_FULL_NETWORK_SUPPORT = "ro.config.full_network_support";
    public static final String RESTORE_CARRIERS_URI1 = "content://telephony/carriers_sim1/restore";
    public static final String RESTORE_CARRIERS_URI2 = "content://telephony/carriers_sim2/restore";
    private static final int SUB1 = 1;
    static final String TAG = "HwCustApnSettingsHwBaseImpl";
    private static final String WHERE_BEARER_UNSPECIFIED = " and bearer=0";
    private static boolean isFullNetSupport = SystemProperties.getBoolean(PROPERTY_FULL_NETWORK_SUPPORT, false);
    private static final boolean[][] plmn_table = new boolean[][]{new boolean[]{true, false, true}, new boolean[]{false, true, false}, new boolean[]{false, false, false}, new boolean[]{false, false, false}, new boolean[]{false, false, false}};
    private static final boolean[][] plmn_table_mms = new boolean[][]{new boolean[]{false, false, false}, new boolean[]{false, false, false}, new boolean[]{false, false, false}, new boolean[]{false, false, false}, new boolean[]{false, false, false}};

    static {
        boolean equals;
        if (SystemProperties.get("ro.config.hw_opta", "0").equals("92")) {
            equals = SystemProperties.get("ro.config.hw_optb", "0").equals("156");
        } else {
            equals = false;
        }
        IS_CHINA_TELECOM = equals;
        if (SystemProperties.get("ro.config.hw_opta", "0").equals("378")) {
            equals = SystemProperties.get("ro.config.hw_optb", "0").equals("840");
        } else {
            equals = false;
        }
        IS_TRACFONE = equals;
        if (SystemProperties.get("ro.config.hw_opta", "0").equals("237")) {
            equals = SystemProperties.get("ro.config.hw_optb", "0").equals("840");
        } else {
            equals = false;
        }
        IS_SPRINT = equals;
    }

    public HwCustApnSettingsHwBaseImpl(ApnSettingsHwBase apnSettingsHwBase) {
        super(apnSettingsHwBase);
    }

    public String getApnDisplayTitle(Context context, String name) {
        return UtilsCustEx.getApnDisplayTitle(context, name);
    }

    public String getCustOperatorNumericSelection(int subscription) {
        String where = EMPTY_OPERATOR_NUMRIC;
        if (SystemProperties.getBoolean("ro.config.cmdm_apn_not_display", false)) {
            where = where + "and name != \"CMDM\"";
            Log.d(TAG, "ro.config.cmdm_apn_not_display is work, add cmdm apn not display");
        }
        if ((!IS_CHINA_TELECOM || (!isCurrentSlotSupportLTE(this.mApnSettingsHwBase.getActivity(), subscription) && (1 != subscription || !isDualModeCard(subscription)))) && !isCTCardForFullNet(subscription)) {
            return where;
        }
        String telecomSelection = " and ((visible = 1";
        MSimTelephonyManager.getDefault();
        switch (MSimTelephonyManager.getNetworkType(subscription)) {
            case 13:
            case 14:
                if (!SystemProperties.getBoolean("ro.config.apn_lte_ctnet", false) || !isNetworkRoaming(subscription)) {
                    telecomSelection = telecomSelection + " and (bearer=14 or bearer=13)";
                    break;
                }
                telecomSelection = telecomSelection + WHERE_BEARER_UNSPECIFIED;
                break;
                break;
            default:
                telecomSelection = telecomSelection + WHERE_BEARER_UNSPECIFIED;
                break;
        }
        return where + (telecomSelection + ") or visible is null)");
    }

    private boolean isDualModeCard(int subscription) {
        if (HwTelephonyManager.getDefault() != null) {
            int SubType = HwTelephonyManager.getDefault().getCardType(subscription);
            if (41 == SubType || 43 == SubType || 40 == SubType) {
                return true;
            }
        }
        return false;
    }

    public boolean isShowWapApn(String apn, String type, int subscription) {
        Log.d(TAG, "isShowWapApn: apn = " + apn + " type = " + type);
        if (checkShouldHideApn(apn)) {
            return false;
        }
        if (!isCurrentSlotSupportLTE(this.mApnSettingsHwBase.getActivity(), subscription) && !isFullNetSupport && CHINA_TELECOM_WAP_NETWORK_NAME.equals(apn)) {
            return false;
        }
        if ((!isCurrentSlotSupportLTE(this.mApnSettingsHwBase.getActivity(), subscription) && !isFullNetSupport) || !CHINA_TELECOM_WAP_NETWORK_NAME.equals(apn)) {
            return true;
        }
        int cardIndex = getCardType();
        int netIndex = getNetWork(subscription);
        Log.d(TAG, "cardIndex:" + cardIndex);
        Log.d(TAG, "netIndex :" + netIndex);
        if (type.equals("mms")) {
            return plmn_table_mms[netIndex][cardIndex];
        }
        return plmn_table[netIndex][cardIndex];
    }

    private static boolean isCurrentSlotSupportLTE(Context context, int subscription) {
        return subscription == Utils.getMainCardSlotId();
    }

    private int getNetWork(int subscription) {
        int netIndex;
        String networkOperator = MSimTelephonyManager.getDefault().getNetworkOperator(subscription);
        boolean isCdmaType = false;
        try {
            isCdmaType = 2 == MSimTelephonyManager.getDefault().getCurrentPhoneType(subscription);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (networkOperator.startsWith(CHINA_MCC)) {
            netIndex = 0;
        } else if (networkOperator.startsWith(MACAU_MCC)) {
            netIndex = 1;
        } else if (isCdmaType) {
            netIndex = 2;
        } else {
            netIndex = 3;
        }
        MSimTelephonyManager.getDefault();
        int radioTech = MSimTelephonyManager.getNetworkType(subscription);
        if (radioTech == 13 || radioTech == 14) {
            return 4;
        }
        return netIndex;
    }

    private int getCardType() {
        String simOperator = TelephonyManager.getDefault().getSimOperator();
        if (SystemProperties.get("ro.ct_card.mccmnc", "46003,46012").contains(simOperator)) {
            return 0;
        }
        if (MACAU_TELECOM_PLMN.equals(simOperator)) {
            return 1;
        }
        return 2;
    }

    public boolean isSortbyId() {
        if (Systemex.getInt(this.mApnSettingsHwBase.getContentResolver(), "apn_sort_byid", 0) == 1) {
            return true;
        }
        return false;
    }

    public Uri getPreferredApnUri(Uri prefer_apn_uri, int sub) {
        Uri perfer_apn_uri_tepm = prefer_apn_uri;
        if (SystemProperties.getBoolean("ro.config.mtk_platform_apn", false)) {
            if (sub == 0) {
                perfer_apn_uri_tepm = PREFERAPN_URI1;
            } else if (sub == 1) {
                perfer_apn_uri_tepm = PREFERAPN_URI2;
            }
        }
        Log.i(TAG, "ro.config.mtk_platform_apn = " + SystemProperties.getBoolean("ro.config.mtk_platform_apn", false) + ",perfer_apn_uri_tepm = " + perfer_apn_uri_tepm + ",sub = " + sub);
        return perfer_apn_uri_tepm;
    }

    public Uri getApnUri(Uri apn_uri, int sub) {
        Uri apn_uri_temp = apn_uri;
        if (SystemProperties.getBoolean("ro.config.mtk_platform_apn", false)) {
            if (sub == 0) {
                apn_uri_temp = APN_SIM1_URI1;
            } else if (sub == 1) {
                apn_uri_temp = APN_SIM2_URI1;
            }
        }
        Log.i(TAG, "ro.config.mtk_platform_apn = " + SystemProperties.getBoolean("ro.config.mtk_platform_apn", false) + ",apn_uri_temp = " + apn_uri_temp + ",sub = " + sub);
        return apn_uri_temp;
    }

    public Uri getRestoreAPnUri(Uri restore_apn_uri, int sub) {
        Uri restore_apn_uri_temp = restore_apn_uri;
        if (SystemProperties.getBoolean("ro.config.mtk_platform_apn", false)) {
            if (sub == 0) {
                restore_apn_uri_temp = DEFAULTAPN_URI1;
            } else if (sub == 1) {
                restore_apn_uri_temp = DEFAULTAPN_URI2;
            }
        }
        Log.i(TAG, "ro.config.mtk_platform_apn = " + SystemProperties.getBoolean("ro.config.mtk_platform_apn", false) + ",restore_apn_uri_temp = " + restore_apn_uri_temp + ",sub = " + sub);
        return restore_apn_uri_temp;
    }

    private static boolean isCTCardForFullNet(int subscription) {
        if (isFullNetSupport) {
            return TelephonyManagerEx.isCTSimCard(subscription);
        }
        return false;
    }

    public boolean checkShouldHideApn(String apn) {
        int i = 0;
        boolean ret = false;
        String shouldHideApnString = Systemex.getString(this.mApnSettingsHwBase.getContentResolver(), "should_hide_apn_name");
        Log.d(TAG, "shouldHideApnString cust: " + shouldHideApnString);
        if (TextUtils.isEmpty(shouldHideApnString) || TextUtils.isEmpty(apn)) {
            return false;
        }
        String[] shouldHideApnArray = shouldHideApnString.split(";");
        int length = shouldHideApnArray.length;
        while (i < length) {
            if (apn.equalsIgnoreCase(shouldHideApnArray[i])) {
                ret = true;
                break;
            }
            i++;
        }
        return ret;
    }

    public String getOperatorNumeric(int subscription) {
        boolean isCdmaType = false;
        try {
            isCdmaType = MSimTelephonyManager.getDefault().getCurrentPhoneType(subscription) == 2;
        } catch (Exception e) {
            e.printStackTrace();
        }
        if ((isCurrentSlotSupportLTE(this.mApnSettingsHwBase.getActivity(), subscription) && (isCdmaType || IS_CHINA_TELECOM)) || isCTCardForFullNet(subscription)) {
            return SystemProperties.get("ro.cdma.home.operator.numeric", "46003");
        }
        String numeric = MSimTelephonyManager.getTelephonyProperty("gsm.sim.operator.numeric", subscription, EMPTY_OPERATOR_NUMRIC);
        String operator = SystemProperties.get("ro.hwpp_plmn_sub2", "0");
        if (!IS_CHINA_TELECOM || numeric == null || numeric.equals(EMPTY_OPERATOR_NUMRIC) || !operator.contains(numeric)) {
            return numeric;
        }
        Log.d(TAG, "sub2 is dobule mode card.");
        return SystemProperties.get("gsm.national_roaming.apn", numeric);
    }

    private boolean isNetworkRoaming(int sub) {
        if (TelephonyManager.getDefault().isMultiSimEnabled()) {
            return TelephonyManager.getDefault().isNetworkRoaming(sub);
        }
        return TelephonyManager.getDefault().isNetworkRoaming();
    }

    public ArrayList<String> getOperatorNumeric(Context context, String numeric, ArrayList<String> result) {
        int activePhone = TelephonyManager.from(context).getPhoneType();
        Log.d(TAG, "getOperatorNumeric: numeric is " + numeric + " activePhone is " + activePhone);
        if (IS_CHINA_TELECOM) {
            result.remove(numeric);
            result.add(SystemProperties.get("ro.cdma.home.operator.numeric", "46003"));
        } else if ((IS_TRACFONE || IS_SPRINT) && activePhone == 2) {
            result.remove(numeric);
            int dataNetworkType = TelephonyManager.getDefault().getDataNetworkType();
            String numeric_sim = SystemProperties.get("gsm.apn.sim.operator.numeric", EMPTY_OPERATOR_NUMRIC);
            String numeric_ruim = SystemProperties.get("net.cdma.ruim.operator.numeric", EMPTY_OPERATOR_NUMRIC);
            Log.d(TAG, "getOperatorNumeric: dataNetworkType is " + dataNetworkType + " numeric_sim is " + numeric_sim + "numeric_ruim is " + numeric_ruim);
            if (EMPTY_OPERATOR_NUMRIC.equals(numeric_sim) || EMPTY_OPERATOR_NUMRIC.equals(numeric_ruim)) {
                if (EMPTY_OPERATOR_NUMRIC.equals(numeric_sim) && !EMPTY_OPERATOR_NUMRIC.equals(numeric_ruim)) {
                    result.add(numeric_ruim);
                } else if (EMPTY_OPERATOR_NUMRIC.equals(numeric_sim) || !EMPTY_OPERATOR_NUMRIC.equals(numeric_ruim)) {
                    Log.w(TAG, "getOperatorNumeric: both apn numeric from sim and ruim are empty ");
                    result.add(numeric_sim);
                } else {
                    result.add(numeric_sim);
                }
            } else if (dataNetworkType == 13 || dataNetworkType == 14) {
                result.add(numeric_sim);
                result.add(numeric_ruim);
            } else {
                result.add(numeric_ruim);
                result.add(numeric_sim);
            }
        }
        return result;
    }

    public void addOrangeSpecialPreference(String curApnName, String name, PreferenceCategory mCategory_apn_general, ApnPreference pref, String preferredApnName) {
        if (!IS_SUPPORT_ORANGE_APN || !"netgprs.com".equals(curApnName) || !"netgprs.com".equals(name)) {
            mCategory_apn_general.addPreference(pref);
        } else if (preferredApnName != null && "netgprs.com".equals(preferredApnName)) {
            mCategory_apn_general.addPreference(pref);
        }
    }

    public boolean isHideSpecialAPN(String mccmnc, String apn) {
        if (TextUtils.isEmpty(mccmnc) || TextUtils.isEmpty(apn)) {
            return false;
        }
        String shouldHideApnString = System.getString(this.mApnSettingsHwBase.getContentResolver(), "hide_sepcial_apn_form_list");
        if (TextUtils.isEmpty(shouldHideApnString)) {
            return false;
        }
        for (String item : shouldHideApnString.split(";")) {
            String[] itemDatas = item.split(":");
            if (mccmnc.equals(itemDatas[0])) {
                for (String apnItem : itemDatas[1].split(",")) {
                    if (apn.equals(apnItem)) {
                        return true;
                    }
                }
                continue;
            }
        }
        return false;
    }

    public boolean hideApnCustbyPreferred(String mccmnc, String preferredApn, String apn) {
        String cust = System.getString(this.mApnSettingsHwBase.getContentResolver(), "hw_apn_hide_by_plmn");
        if (!(TextUtils.isEmpty(cust) || TextUtils.isEmpty(mccmnc) || TextUtils.isEmpty(preferredApn))) {
            String[] custArray = cust.trim().split(";");
            for (String trim : custArray) {
                String[] item = trim.trim().split(":");
                String hPlmn = item[0];
                String preApn = item[1];
                String[] filterApnArray = item[2].trim().split(",");
                if (mccmnc.equals(hPlmn) && preferredApn.equals(preApn) && Arrays.asList(filterApnArray).contains(apn)) {
                    return true;
                }
            }
        }
        return false;
    }
}
