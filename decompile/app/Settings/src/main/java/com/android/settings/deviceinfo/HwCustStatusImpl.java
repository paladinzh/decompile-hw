package com.android.settings.deviceinfo;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SqliteWrapper;
import android.os.SystemProperties;
import android.provider.Settings.SettingNotFoundException;
import android.provider.Settings.System;
import android.provider.SettingsEx.Systemex;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.preference.PreferenceScreen;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import com.android.internal.telephony.Phone;
import com.android.settings.HwCustSettingsUtils;
import com.huawei.sprint.chameleon.provider.ChameleonContract;
import java.util.HashMap;
import java.util.HashSet;

public class HwCustStatusImpl extends HwCustStatus {
    private static final String ACTION_IMS_STATUS = "huawei.intent.action.IMS_SERVICE_STATE_CHANGED";
    public static final String CHAMELEON = "Chameleon";
    private static final String DEFAULT_OPERATOR_NAME = "Home";
    private static final boolean HIDE_OPERATOR_NAME = SystemProperties.getBoolean("ro.config.hw_hide_operator_name", false);
    private static final int IMEI_SV_LENGTH = 16;
    private static final boolean IMEI_SV_SHOW_TWO = SystemProperties.getBoolean("ro.config.hw_imei_sv_show_two", false);
    private static final int IMEI_SV_SUB_STRING = 14;
    private static final boolean LOG_DEBUG = false;
    private static final String MEID_DECIMAL_STRING = "000000000000000000";
    private static final boolean PLMN_TO_SETTINGS = SystemProperties.getBoolean("ro.config.plmn_to_settings", false);
    private static final int SHOW_NETWORK_TYPE_4G = 0;
    private static final int SHOW_NETWORK_TYPE_LTE = 1;
    public static final String SUMMARY_UNKNOWN = "unknown";
    private static final String SYSTEM_PROPERTIES_QUALIFIED_NAME = "com.sprint.internal.SystemProperties";
    private static final String SYSTEM_PROPRETIES_GET_METHOD_NAME = "getString";
    private static final String TAG = "HwCustStatusImpl";
    private static final boolean isHideRoamingIcon = SystemProperties.getBoolean("ro.config.hw_hide_roaming_icon", false);
    private static final boolean mLteShow4glte = SystemProperties.getBoolean("ro.config.lte_show_northamerica", false);
    private boolean imsIsRegistered = false;
    private String mDeviceSoftwareVersion;
    HashSet<String> mMccMnc = new HashSet();
    private Resources mRes;
    private Status mStatus = null;

    public HwCustStatusImpl(Status status) {
        super(status);
        this.mStatus = status;
    }

    public String getCustomNetworkType(String networkType, TelephonyManager telephonyMgr, boolean isCAstate) {
        String customNetworkType = networkType;
        String currentMccMnc = telephonyMgr.getSimOperator();
        if (this.mStatus.getContext() == null) {
            return "";
        }
        String networkTypeSims = Systemex.getString(this.mStatus.getContext().getContentResolver(), "hw_sim_networktype");
        if (networkTypeSims != null && networkTypeSims.contains(currentMccMnc) && ("4G".equals(networkType) || "LTE".equals(networkType))) {
            int typeIndex = networkTypeSims.indexOf(currentMccMnc) + currentMccMnc.length();
            switch (Integer.valueOf(networkTypeSims.substring(typeIndex + 1, typeIndex + 2)).intValue()) {
                case 0:
                    customNetworkType = "4G";
                    break;
                case 1:
                    customNetworkType = "LTE";
                    break;
            }
        }
        customNetworkType = getCustomizedNetworkType(customNetworkType, currentMccMnc, isCAstate);
        if ("4G".equals(customNetworkType) || "LTE".equals(customNetworkType) || "4G+".equals(customNetworkType)) {
            if (isCustomMcc() && isCAstate) {
                customNetworkType = "LTE+";
            } else if (isCustomMcc()) {
                customNetworkType = "LTE";
            }
        }
        if (mLteShow4glte) {
            if ("UMTS".equals(customNetworkType) || "HSDPA".equals(customNetworkType) || "HSUPA".equals(customNetworkType) || "HSDPA+".equals(customNetworkType) || "HSUPA+".equals(customNetworkType) || "HSPAP".equals(customNetworkType) || "HSPA".equals(customNetworkType) || "HSPA+".equals(customNetworkType)) {
                customNetworkType = "4G";
            } else if ("LTE".equals(customNetworkType) || "CA".equals(customNetworkType)) {
                customNetworkType = "4G LTE";
            }
        }
        int netType = TelephonyManager.getDefault().getNetworkType();
        TelephonyManager.getDefault();
        String tempNetworkTypeName = TelephonyManager.getNetworkTypeName(netType);
        if (customizedNetworkType("hw_hspap_show_4g") && "HSPA+".equals(tempNetworkTypeName)) {
            customNetworkType = "4G";
        }
        if (customizedNetworkType("hw_show_lte") && (("4G".equals(tempNetworkTypeName) || "LTE".equals(tempNetworkTypeName)) && !(isCustomMcc() && isCAstate))) {
            customNetworkType = "LTE";
        }
        if (customizedNetworkType("hw_show_4_5G_for_mcc") && ("4G".equals(tempNetworkTypeName) || "LTE".equals(tempNetworkTypeName))) {
            customNetworkType = "4.5G";
        }
        if (("4G".equals(tempNetworkTypeName) || "LTE".equals(tempNetworkTypeName)) && SystemProperties.getBoolean("ro.config.hw_4g_to_4.5g", false)) {
            customNetworkType = "4.5G";
        }
        boolean IS_HH_ROAM_SHOW_3G = SystemProperties.getBoolean("ro.config.is_hspa_roam_show_3g", false);
        boolean isRoaming = TelephonyManager.getDefault().isNetworkRoaming();
        if (IS_HH_ROAM_SHOW_3G && isRoaming && ("HSDPA".equals(customNetworkType) || "HSPA".equals(customNetworkType) || "HSPA+".equals(customNetworkType) || "4G".equals(customNetworkType))) {
            customNetworkType = "3G";
        }
        return getIranCANetworkType(tempNetworkTypeName, customNetworkType, isCAstate);
    }

    private String getIranCANetworkType(String tempNetworkTypeName, String customNetworkType, boolean isCAstate) {
        if (!customizedNetworkType("hw_show_4g_to_4_5g_ca") || !isCAstate) {
            return customNetworkType;
        }
        if ("4G".equals(tempNetworkTypeName) || "LTE".equals(tempNetworkTypeName)) {
            return "4.5G";
        }
        return customNetworkType;
    }

    private boolean customizedNetworkType(String flag) {
        String strMccmnc = System.getString(this.mStatus.getContext().getContentResolver(), flag);
        String mCurrentMccmnc = TelephonyManager.getDefault().getSimOperator();
        if (TextUtils.isEmpty(strMccmnc)) {
            return false;
        }
        String[] mccmnc = strMccmnc.split(";");
        int i = 0;
        while (i < mccmnc.length) {
            if (mCurrentMccmnc.startsWith(mccmnc[i]) || mCurrentMccmnc.equalsIgnoreCase(mccmnc[i])) {
                return true;
            }
            i++;
        }
        return false;
    }

    private String getCustomizedNetworkType(String customNetworkType, String mccMnc, boolean isCAstate) {
        String mccMncList;
        String networkTypeList;
        String networkTypeStr;
        String customizedNetworkType = customNetworkType;
        String currentMccMnc = mccMnc;
        if ("4G".equals(customNetworkType) || "LTE".equals(customNetworkType)) {
            mccMncList = "hw_show_4g";
            networkTypeList = "hw_customized_networkType_4g";
        } else {
            mccMncList = "hw_customized_networkType_entry";
            networkTypeList = "hw_customized_networkType2";
        }
        String customEntry = Systemex.getString(this.mStatus.getContext().getContentResolver(), mccMncList);
        boolean bContainsEntry = false;
        if (!TextUtils.isEmpty(customEntry)) {
            for (CharSequence contains : customEntry.split(";")) {
                if (mccMnc.contains(contains)) {
                    bContainsEntry = true;
                    break;
                }
            }
        }
        if (bContainsEntry) {
            networkTypeStr = Systemex.getString(this.mStatus.getContext().getContentResolver(), networkTypeList);
        } else {
            networkTypeStr = Systemex.getString(this.mStatus.getContext().getContentResolver(), "hw_customized_networkType");
        }
        if (("4G".equals(customNetworkType) || "LTE".equals(customNetworkType)) && isCAstate && !bContainsEntry) {
            customNetworkType = "CA";
        }
        if (TextUtils.isEmpty(networkTypeStr)) {
            return customizedNetworkType;
        }
        String resultNetworkType = (String) parseCustomizedNetworkTypeString(networkTypeStr).get(customNetworkType);
        if (TextUtils.isEmpty(resultNetworkType)) {
            return customizedNetworkType;
        }
        return resultNetworkType;
    }

    private HashMap<String, String> parseCustomizedNetworkTypeString(String networkTypeStr) {
        HashMap<String, String> networkMap = new HashMap();
        String[] units = networkTypeStr.split(";");
        if (units.length > 0) {
            for (int i = 0; i < units.length; i++) {
                if (!TextUtils.isEmpty(units[i])) {
                    String[] networkUnit = units[i].split(":");
                    if (networkUnit.length == 2) {
                        networkMap.put(networkUnit[0], networkUnit[1]);
                    }
                }
            }
        }
        return networkMap;
    }

    public boolean isIMEISVShowTwo(TelephonyManager telephonyManager) {
        if (telephonyManager != null && IMEI_SV_SHOW_TWO) {
            this.mDeviceSoftwareVersion = telephonyManager.getDeviceSoftwareVersion();
            if (this.mDeviceSoftwareVersion != null && 16 == this.mDeviceSoftwareVersion.length()) {
                return true;
            }
        }
        return false;
    }

    public String getIMEISVSummaryText() {
        if (this.mDeviceSoftwareVersion != null) {
            return this.mDeviceSoftwareVersion.substring(14);
        }
        return null;
    }

    public String getCustNetworkName(ServiceState serviceState) {
        try {
            if (1 == Systemex.getInt(this.mStatus.getContext().getContentResolver(), "hw_customize_network")) {
                return SystemProperties.get("gsm.operator.alpha", "");
            }
            return serviceState.getOperatorAlphaLong();
        } catch (SettingNotFoundException e) {
            return null;
        }
    }

    public String getCustOperatorName(String operatorName, ServiceState serviceState) {
        if (operatorName != null && HIDE_OPERATOR_NAME) {
            return DEFAULT_OPERATOR_NAME;
        }
        if (this.mStatus.getContext() == null) {
            return "";
        }
        String networkName = operatorName;
        String mOperatorName = serviceState.getOperatorAlphaShort();
        String simMccmnc = TelephonyManager.getDefault().getSimOperator();
        String customizedOperatorName = System.getString(this.mStatus.getContext().getContentResolver(), "hw_customized_operator_name");
        if (!TextUtils.isEmpty(customizedOperatorName)) {
            networkName = customizedOperatorName;
        }
        if (PLMN_TO_SETTINGS) {
            String str = "";
            try {
                str = System.getString(this.mStatus.getContext().getContentResolver(), "0_plmn_servicestate_to_settings");
                Log.v(TAG, "getCustNetworkName plmn_servicestate_to_settings = " + str);
            } catch (Exception e) {
                Log.v(TAG, "Exception when got plmn_servicestate_to_settings value", e);
            }
            this.mRes = this.mStatus.getResources();
            if (operatorName.equals("") || operatorName.equals(this.mRes.getString(17040036)) || operatorName.equals(this.mRes.getString(17040012))) {
                str = this.mRes.getString(2131624355);
            }
            if (!TextUtils.isEmpty(str)) {
                networkName = str;
            }
        }
        if (isNotEqualToUI(simMccmnc)) {
            networkName = mOperatorName;
        }
        return networkName;
    }

    private boolean isCustomMcc() {
        initMccMnc();
        String currentMccMnc = TelephonyManager.getDefault().getSimOperator();
        if (this.mMccMnc.size() == 0 || currentMccMnc.length() < 3) {
            return false;
        }
        if (this.mMccMnc.contains(currentMccMnc.substring(0, 3)) || this.mMccMnc.contains(currentMccMnc)) {
            return true;
        }
        return false;
    }

    private void initMccMnc() {
        String strMccMnc = Systemex.getString(this.mStatus.getContext().getContentResolver(), "hw_mcc_mnc");
        if (strMccMnc != null && this.mMccMnc.size() == 0) {
            String[] mccmnc = strMccMnc.split(";");
            for (String trim : mccmnc) {
                this.mMccMnc.add(trim.trim());
            }
        }
    }

    public String getOperatorNameByMccmnc(ServiceState serviceState, String operatorName) {
        String networkName = operatorName;
        String mOperatorName = serviceState.getOperatorAlphaShort();
        String simMccmnc = TelephonyManager.getDefault().getSimOperator();
        Log.v(TAG, "mOperatorName=  " + mOperatorName);
        if (isNotEqualToUI(simMccmnc)) {
            return mOperatorName;
        }
        return networkName;
    }

    private boolean isNotEqualToUI(String mccmnc) {
        String custMccmncStrs = Systemex.getString(this.mStatus.getContext().getContentResolver(), "plmnNotToSettings");
        if (TextUtils.isEmpty(custMccmncStrs) || TextUtils.isEmpty(mccmnc)) {
            Log.v(TAG, "isNotEqualToUI: plmnNotToSettings or mccmnc is empty");
            return false;
        }
        for (String area : custMccmncStrs.split(",")) {
            if (area.equals(mccmnc)) {
                return true;
            }
        }
        return false;
    }

    public void updateCustomSatatusPreference(String meidHex, String meidHexKey, String imeiKey, TelephonyManager telephonyMgr) {
        if (HwCustSettingsUtils.IS_SPRINT) {
            PreferenceScreen root = this.mStatus.getPreferenceScreen();
            addMEIDDecimalPreference(root, meidHex, meidHexKey);
            root.findPreference(meidHexKey).setTitle(2131629213);
            addIMSIPreference(root, telephonyMgr, imeiKey);
            addBrandNamePreference(root);
        }
    }

    private void addMEIDDecimalPreference(PreferenceScreen root, String meidHex, String meidHexKey) {
        addCustPrefernce(root, 2130968980, 2131629214, convertMEIDHexToDecimal(meidHex), root.findPreference(meidHexKey).getOrder());
    }

    private void addIMSIPreference(PreferenceScreen root, TelephonyManager telephonyMgr, String imeiKey) {
        addCustPrefernce(root, 2130968980, 2131629216, telephonyMgr.getSubscriberId(), root.findPreference(imeiKey).getOrder());
    }

    private void addBrandNamePreference(PreferenceScreen root) {
        String brandName = getSprintBrandName();
        if (brandName == null || CHAMELEON.equalsIgnoreCase(brandName)) {
            brandName = SUMMARY_UNKNOWN;
        }
        int prefCount = root.getPreferenceCount();
        root.getPreference(prefCount - 1).setOrder(prefCount + 1);
        addCustPrefernce(root, 2130968980, 2131629215, brandName, prefCount);
    }

    private String convertMEIDHexToDecimal(String meidHex) {
        if (meidHex.length() < 14) {
            return null;
        }
        String meid_substring_0_to_8 = meidHex.substring(0, 8);
        String tempMeidDecimal = Long.toString(Long.parseLong(meidHex.substring(8, 14), 16) + (100000000 * Long.parseLong(meid_substring_0_to_8, 16)));
        return (MEID_DECIMAL_STRING + tempMeidDecimal).substring(tempMeidDecimal.length());
    }

    private void addCustPrefernce(PreferenceScreen root, int layoutResId, int titleResId, String summary, int order) {
        Preference prefernce = new Preference(this.mStatus.getActivity());
        prefernce.setLayoutResource(layoutResId);
        prefernce.setTitle(titleResId);
        if (summary != null) {
            prefernce.setSummary((CharSequence) summary);
        } else {
            prefernce.setSummary(SUMMARY_UNKNOWN);
        }
        prefernce.setOrder(order);
        root.addPreference(prefernce);
    }

    private String getSprintBrandName() {
        String str = null;
        Cursor chameleonCursor = SqliteWrapper.query(this.mStatus.getContext(), this.mStatus.getContext().getContentResolver(), ChameleonContract.CONTENT_URI_CHAMELEON, new String[]{"value"}, "_index= ?", new String[]{"500"}, null);
        if (chameleonCursor != null) {
            try {
                if (chameleonCursor.moveToFirst()) {
                    str = chameleonCursor.getString(chameleonCursor.getColumnIndex("value"));
                }
            } catch (SQLException sqle) {
                Log.e(TAG, "getSprintBrandName -> not able to get sprint brand name " + sqle);
                if (chameleonCursor != null) {
                    chameleonCursor.close();
                }
            } catch (Throwable th) {
                if (chameleonCursor != null) {
                    chameleonCursor.close();
                }
            }
        } else {
            Log.e(TAG, "getSprintBrandName -> not able to get sprint brand name ");
        }
        if (chameleonCursor != null) {
            chameleonCursor.close();
        }
        return str;
    }

    public String updateCustPhoneNumber(Phone aPhone, String rawNumber) {
        if (HwCustSettingsUtils.IS_SPRINT) {
            String lMSIDNNumber = aPhone.getMsisdn();
            if (lMSIDNNumber != null) {
                return lMSIDNNumber;
            }
        }
        return rawNumber;
    }

    public boolean isHideRoaming() {
        return isHideRoamingIcon;
    }

    public void addImsPreference(Context context, PreferenceScreen prefRoot) {
        if (prefRoot != null && context != null) {
            this.imsIsRegistered = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("imsIsRegistered", false);
            Preference mImsPreference = new Preference(this.mStatus.getActivity());
            mImsPreference.setLayoutResource(2130968980);
            mImsPreference.setTitle(2131629319);
            if (this.imsIsRegistered) {
                mImsPreference.setSummary(2131629320);
            } else {
                mImsPreference.setSummary(2131629321);
            }
            int prefCount = prefRoot.getPreferenceCount();
            prefRoot.getPreference(prefCount - 1).setOrder(prefCount + 1);
            mImsPreference.setOrder(prefCount);
            prefRoot.addPreference(mImsPreference);
        }
    }

    public boolean isDisplayIms() {
        return SystemProperties.getBoolean("ro.config.hw_display_ims", false);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setImsStatus(Context context, Intent intent) {
        if (!(context == null || intent == null || !ACTION_IMS_STATUS.equals(intent.getAction()))) {
            this.imsIsRegistered = "REGISTERED".equals(intent.getExtra("state", "").toString());
            Editor mEditor = PreferenceManager.getDefaultSharedPreferences(context).edit();
            mEditor.putBoolean("imsIsRegistered", this.imsIsRegistered);
            mEditor.apply();
        }
    }
}
