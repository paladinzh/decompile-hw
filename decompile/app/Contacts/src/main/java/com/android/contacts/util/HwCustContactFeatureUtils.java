package com.android.contacts.util;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.os.Build.VERSION;
import android.os.SystemProperties;
import android.provider.Settings.System;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.android.contacts.hap.CommonConstants;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.hap.HwCustCommonConstants;
import com.android.contacts.hap.sim.SimFactoryManager;
import com.google.android.gms.R;
import com.huawei.android.provider.SettingsEx.Systemex;
import com.huawei.android.telephony.TelephonyManagerEx;
import com.huawei.android.util.NoExtAPIException;
import com.huawei.contact.util.SettingsWrapper;
import java.lang.reflect.Method;
import java.util.ArrayList;

public class HwCustContactFeatureUtils {
    private static final boolean IS_2CK_NETWORK_LOCK_ENABLED = SystemProperties.getBoolean("ro.config.hw_enable2CkNwLock", false);
    public static final boolean IS_ATT_MY_INFO = SystemProperties.getBoolean("ro.config.my_info_supported", false);
    private static final boolean IS_ATT_SERVICE_GRP = SystemProperties.getBoolean("ro.config.pre_loads_group", false);
    private static final boolean IS_CUSTOM_MEID = SystemProperties.getBoolean("ro.config.hide_meid", false);
    private static final boolean IS_ICE_SUPPORTED = SystemProperties.getBoolean("ro.config.ice_grp_supported", false);
    private static final boolean IS_JOIN_ENABLED = SystemProperties.getBoolean("ro.config.hw_enable_join", false);
    private static final boolean IS_SHOW_IMEI_SVN = SystemProperties.getBoolean("ro.config.show_imei_svn", false);
    private static final boolean IS_SPRINT = SystemProperties.getBoolean("ro.config.sprint_pim_ext", false);
    private static final boolean IS_SUPPORT_CNAP = SystemProperties.getBoolean("ro.config.is_sup_cnap", false);
    private static final boolean IS_SUPPORT_PREDEFINED_RO = SystemProperties.getBoolean("ro.config.support_readonly", false);
    private static final boolean IS_TRACFONE;
    private static final boolean IS_US_CHANNEL;
    private static final String SDN_MCC_MNC_CONFIG = "hw_show_sdn_name_mccmnc_list";
    public static final String TAG = "HwCustContactFeatureUtils";
    private static final String VO_WIFI_API_NAME = (VERSION.SDK_INT > 23 ? "isWifiCallingAvailable" : "isWifiCallingEnabled");
    private static String VO_WIFI_MCCMNC_LIST = SystemProperties.get("ro.enable.vowifi", "");
    private static boolean mIsHiddenMenuListInitialized = false;
    private static ArrayList<String> mSDNMccMnc = null;
    private static ArrayList<String> mVoWifiMccMncList = null;
    private static ArrayList<String> sDisableHiddenMenuItemsList = null;

    static {
        boolean equals;
        if ("378".equals(SystemProperties.get("ro.config.hw_opta", ""))) {
            equals = "840".equals(SystemProperties.get("ro.config.hw_optb", ""));
        } else {
            equals = false;
        }
        IS_TRACFONE = equals;
        if ("567".equals(SystemProperties.get("ro.config.hw_opta", ""))) {
            equals = "840".equals(SystemProperties.get("ro.config.hw_optb", ""));
        } else {
            equals = false;
        }
        IS_US_CHANNEL = equals;
    }

    public static boolean disablePauseFromDialpad() {
        return IS_SPRINT;
    }

    public static boolean isSupportPhoneType() {
        return !IS_US_CHANNEL ? IS_TRACFONE : true;
    }

    public static boolean isAutoInsertSimNumberToProfile() {
        return !IS_SPRINT ? IS_ATT_MY_INFO : true;
    }

    public static boolean isSupportPostalExtendedFields() {
        return IS_SPRINT;
    }

    public static boolean isIncludeCallDurationDisplay() {
        return !IS_SPRINT;
    }

    public static boolean isSupportCallInterceptFeature() {
        return IS_SPRINT;
    }

    public static boolean isSupportADCnodeFeature() {
        return IS_SPRINT;
    }

    public static boolean isSupportPreloadContact() {
        return IS_SPRINT;
    }

    public static boolean isChameleonDBChangeObserver() {
        if (isSupportCallInterceptFeature() || isSupportADCnodeFeature()) {
            return true;
        }
        return isSupportPreloadContact();
    }

    public static boolean isSupportPredefinedReadOnlyFeature() {
        return IS_SUPPORT_PREDEFINED_RO;
    }

    public static boolean isSupportIceEmergencyContacts() {
        return !IS_SPRINT ? IS_ICE_SUPPORTED : true;
    }

    public static boolean isSupportRadioIDLabelCustomization() {
        return IS_SPRINT;
    }

    public static boolean isSupportAdditionalExchangePhoneFields() {
        return IS_SPRINT;
    }

    public static boolean isSupportHomeFaxLabelCustomization() {
        return IS_SPRINT;
    }

    public static boolean isSupportCallFeatureIcon() {
        return IS_SPRINT;
    }

    public static boolean isSupportNanpStateNameDisplay() {
        return IS_SPRINT;
    }

    public static boolean isSupportSprintEmergencyModeRedial() {
        return IS_SPRINT;
    }

    public static boolean allowRedialEmergencyMode(String number, Context context) {
        boolean isInEcm = Boolean.parseBoolean(SystemProperties.get("ril.cdma.inecmmode"));
        boolean isEmergencyNumber = CommonUtilMethods.isEmergencyNumber(number, SimFactoryManager.isDualSim());
        if (CommonConstants.LOG_DEBUG) {
            Log.d(TAG, " is dialed number is an emergency number :: ( " + isEmergencyNumber + " ) and isEmergencyMode is active :: ( " + isInEcm + ")");
        }
        if (isInEcm) {
            if (isEmergencyNumber) {
                if (CommonConstants.LOG_DEBUG) {
                    Log.d(TAG, "Enabling the redial of emergency number in the emergency mode");
                }
                return true;
            }
            if (CommonConstants.LOG_DEBUG) {
                Log.d(TAG, "Blocking the redial of normal number in the emergency mode");
            }
            Builder builder = new Builder(context);
            builder.setIconAttribute(17301543);
            builder.setTitle(R.string.sprint_emergency_alert_dialog_title);
            builder.setMessage(R.string.sprint_emergency_alert_dialog_message);
            builder.setPositiveButton(17039370, null);
            builder.create().show();
            return false;
        } else if (isEmergencyNumber) {
            if (CommonConstants.LOG_DEBUG) {
                Log.d(TAG, "Disabling the redial of emergency number in the normal mode");
            }
            Toast.makeText(context, R.string.sprint_emergency_toast, 1).show();
            return false;
        } else {
            if (CommonConstants.LOG_DEBUG) {
                Log.d(TAG, "Enabling the redial of normal number in the normal mode");
            }
            return true;
        }
    }

    public static boolean isSupportOtherEmergencyNetworkSignal() {
        return IS_SPRINT;
    }

    public static boolean isMoveShareContactsToMainMenu() {
        return IS_SPRINT;
    }

    public static boolean isSupportDialNumberFormat() {
        return IS_SPRINT;
    }

    public static boolean isSupportMyInfoAddressFields() {
        return IS_SPRINT;
    }

    public static boolean isVibrationPatternRequired() {
        return IS_SPRINT;
    }

    public static boolean isSupportCustomizedMeid() {
        return IS_SPRINT;
    }

    public static boolean isSupportCustomizedImei() {
        return IS_SPRINT;
    }

    public static boolean isBindOnlyNumberSwitch(Context mContext) {
        boolean z = false;
        boolean isFilterNumber = mContext.getSharedPreferences("com.android.contacts_preferences", 0).getBoolean("preference_contacts_only_phonenumber", false);
        if (!"true".equals(Systemex.getString(mContext.getContentResolver(), "hw_contacts_filter_number"))) {
            return false;
        }
        if (!isFilterNumber) {
            z = true;
        }
        return z;
    }

    public static boolean isShowVisualMailBox() {
        return HwCustCommonConstants.IS_SHOW_VVM;
    }

    public static boolean isShowMyInfoForMyProfile() {
        return !HwCustCommonConstants.IS_AAB_ATT ? IS_ATT_MY_INFO : true;
    }

    public static boolean isShowAccServiceGrp() {
        return !IS_ATT_SERVICE_GRP ? HwCustCommonConstants.IS_AAB_ATT : true;
    }

    public static boolean isVOWifiFeatureEnabled() {
        int i = 0;
        if (TextUtils.isEmpty(VO_WIFI_MCCMNC_LIST)) {
            return false;
        }
        if (mVoWifiMccMncList == null) {
            mVoWifiMccMncList = new ArrayList();
            String[] lMccMncArray = VO_WIFI_MCCMNC_LIST.split(";");
            if (lMccMncArray != null && lMccMncArray.length > 0) {
                int length = lMccMncArray.length;
                while (i < length) {
                    mVoWifiMccMncList.add(lMccMncArray[i]);
                    i++;
                }
            }
        }
        return mVoWifiMccMncList.contains(TelephonyManager.getDefault().getSimOperator());
    }

    public static boolean isWifiCallEnabled(Context aContext) {
        if (aContext == null || !isVOWifiFeatureEnabled() || !canMakeWifiCall(aContext)) {
            return false;
        }
        Log.i(TAG, "Wifi calling is enabled.");
        return true;
    }

    private static boolean canMakeWifiCall(Context aContext) {
        boolean ret = false;
        if (aContext == null) {
            return false;
        }
        TelephonyManager lSimTelephonyManager = (TelephonyManager) aContext.getApplicationContext().getSystemService("phone");
        if (lSimTelephonyManager != null) {
            try {
                Method isWifiCallingEnabled = TelephonyManager.class.getDeclaredMethod(VO_WIFI_API_NAME, new Class[0]);
                isWifiCallingEnabled.setAccessible(true);
                Boolean isEnabled = (Boolean) isWifiCallingEnabled.invoke(lSimTelephonyManager, (Object[]) null);
                Method isImsRegistered = TelephonyManager.class.getDeclaredMethod("isImsRegistered", new Class[0]);
                isImsRegistered.setAccessible(true);
                Boolean isRegistered = (Boolean) isImsRegistered.invoke(lSimTelephonyManager, (Object[]) null);
                if (!(isEnabled == null || isRegistered == null)) {
                    ret = isEnabled.booleanValue() ? isRegistered.booleanValue() : false;
                }
                return ret;
            } catch (Exception aEx) {
                aEx.printStackTrace();
            }
        }
        return false;
    }

    public static boolean checkAndInitCall(Context aContext, String number) {
        if (aContext == null || !isVOWifiFeatureEnabled()) {
            return false;
        }
        if (CommonUtilMethods.isEmergencyNumber(number, false)) {
            Log.i(TAG, "Emergency call.");
            return false;
        } else if (!HwCustPhoneServiceStateListener.isNetworkNotAvailable()) {
            Log.i(TAG, "Network is available.");
            return false;
        } else if (canMakeWifiCall(aContext)) {
            Log.i(TAG, "Wifi calling is enabled");
            return false;
        } else {
            String lTitle = aContext.getString(R.string.wifi_call_alert_dialog_title);
            String lMsg = aContext.getString(R.string.wifi_call_alert_dialog_text);
            Builder builder = new Builder(aContext).setTitle(lTitle).setPositiveButton(17039370, null).setCancelable(false);
            LayoutInflater inflator = (LayoutInflater) aContext.getSystemService("layout_inflater");
            if (inflator != null) {
                View view = inflator.inflate(R.layout.alert_dialog_content, null);
                ((TextView) view.findViewById(R.id.alert_dialog_content)).setText(lMsg);
                builder.setView(view);
            } else {
                builder.setMessage(lMsg);
            }
            builder.show();
            return true;
        }
    }

    public static boolean is2CkNetworkLockEnabled() {
        return IS_2CK_NETWORK_LOCK_ENABLED;
    }

    public static boolean isCustomHideMeid() {
        return IS_CUSTOM_MEID;
    }

    public static boolean isCNAPFeatureSupported(Context context) {
        return !IS_SUPPORT_CNAP ? isMccCnap(context) : true;
    }

    public static boolean isJoinFeatureEnabled() {
        return IS_JOIN_ENABLED;
    }

    private static void prepareHiddenMenuItemsList(String disableHiddenMenuItemsList) {
        if (!TextUtils.isEmpty(disableHiddenMenuItemsList)) {
            sDisableHiddenMenuItemsList = new ArrayList();
            for (String code : disableHiddenMenuItemsList.split(",")) {
                sDisableHiddenMenuItemsList.add(code);
            }
        }
    }

    public static boolean isDisabledHiddenMenuCode(Context context, String inputString) {
        boolean z = false;
        if (context == null || TextUtils.isEmpty(inputString)) {
            return false;
        }
        if (!mIsHiddenMenuListInitialized && sDisableHiddenMenuItemsList == null) {
            prepareHiddenMenuItemsList(SystemProperties.get("ro.config.hw_disable_hiddenmenu", ""));
            mIsHiddenMenuListInitialized = true;
        }
        if (sDisableHiddenMenuItemsList != null) {
            z = sDisableHiddenMenuItemsList.contains(inputString);
        }
        return z;
    }

    public static boolean isSDNNameRequired(Context context, int subId) {
        if (context == null) {
            return true;
        }
        updateSdnMccMnc(context);
        String currentMccMnc = "";
        if (5 == SimFactoryManager.getSimState(subId)) {
            currentMccMnc = TelephonyManager.getDefault().getSimOperator(subId);
        }
        if (mSDNMccMnc == null || TextUtils.isEmpty(currentMccMnc)) {
            return true;
        }
        return mSDNMccMnc.contains(currentMccMnc);
    }

    private static void updateSdnMccMnc(Context context) {
        if (mSDNMccMnc == null) {
            String sdnMccMnc = SettingsWrapper.getString(context.getContentResolver(), SDN_MCC_MNC_CONFIG);
            if (!TextUtils.isEmpty(sdnMccMnc)) {
                mSDNMccMnc = new ArrayList();
                String[] sdnMccMncArray = sdnMccMnc.split(",");
                if (sdnMccMncArray != null && sdnMccMncArray.length > 0) {
                    for (String mccmnc : sdnMccMncArray) {
                        mSDNMccMnc.add(mccmnc);
                    }
                }
            }
        }
    }

    public static boolean isMccCnap(Context context) {
        if (context == null) {
            return false;
        }
        String configString = System.getString(context.getContentResolver(), "hw_is_sup_cnap");
        if (TextUtils.isEmpty(configString)) {
            return false;
        }
        if ("ALL".equals(configString)) {
            return true;
        }
        boolean result = false;
        int mSwitchDualCardSlot = 0;
        if (TelephonyManager.getDefault().isMultiSimEnabled()) {
            try {
                mSwitchDualCardSlot = TelephonyManagerEx.getDefault4GSlotId();
            } catch (NoExtAPIException e) {
                Log.v(TAG, "TelephonyManagerEx.getDefault4GSlotId()->NoExtAPIException!");
            }
        }
        String mccmnc = TelephonyManager.from(context).getSimOperatorNumericForPhone(mSwitchDualCardSlot);
        if (!TextUtils.isEmpty(mccmnc)) {
            String[] custValues = configString.trim().split(";");
            int size = custValues.length;
            int i = 0;
            while (i < size) {
                if (mccmnc.startsWith(custValues[i]) || mccmnc.equalsIgnoreCase(custValues[i])) {
                    result = true;
                    break;
                }
                i++;
            }
        }
        return result;
    }

    public static boolean isShowImeiSvn() {
        return IS_SHOW_IMEI_SVN;
    }
}
