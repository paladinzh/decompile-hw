package com.android.mms;

import android.content.Context;
import android.os.Build;
import android.os.SystemProperties;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.text.TextUtils;
import android.util.Log;
import cn.com.xy.sms.sdk.db.entity.IccidInfoManager;
import com.huawei.android.provider.SettingsEx.Systemex;
import com.huawei.mms.util.HwCustMccMncConfigImpl;
import com.huawei.mms.util.MccMncConfig;

public class HwCustMmsConfigImpl extends HwCustMmsConfig {
    private static final String DEFAULT_UAPROF_FOR_CHAMELEON = "uaprofile.rdf";
    private static final int ENCRYPT_CALL_FEATURE_CLOSE = 0;
    private static final String ENCRYPT_CALL_FEATURE_KEY = "encrypt_version";
    private static final int ENCRYPT_CALL_FEATURE_OPEN = 1;
    private static final boolean ENCRYPT_PROP = SystemProperties.getBoolean("ro.config.encrypt_version", false);
    private static final String SUFFIX_UAPROF_FOR_CHAMELEON = ".rdf";
    protected static final String TAG = "HwCustMmsConfigImpl";
    private static Boolean mIsEncryptCallEnabled = null;
    private static Boolean mRemoveMms = Boolean.valueOf(false);

    public static boolean isShowConfirmDialog() {
        return MmsConfig.getMmsBoolConfig("isShowConfirmDialog", false);
    }

    public static boolean getEnableShowSmscNotEdit() {
        return MmsConfig.getMmsBoolConfig("enableShowSmscNotEdit", false);
    }

    public static String getCustVideoParam() {
        return MmsConfig.getMmsStringConfig("custVideoParam", null);
    }

    public static String getCustMccmncForShowSmscNotEdit() {
        return MmsConfig.getMmsStringConfig("custMccmncForShowSmscNotEdit", null);
    }

    public static boolean isAllowReportSpam() {
        return MmsConfig.getMmsBoolConfig("allowReportSpam", false);
    }

    public static String[] getReportSpamNumber() {
        return new String[]{MmsConfig.getMmsStringConfig("reportSpamNumber", null)};
    }

    public static boolean isEnableChangeClassZeroMessageShow() {
        return MmsConfig.getMmsBoolConfig("isChangeClassZeroMessageShow", false);
    }

    public static boolean isHttpHeaderUseCurrentLocale() {
        return MmsConfig.getMmsBoolConfig("httpHeaderUseCurrentLocale", false);
    }

    public static boolean isEnableCmasSettings() {
        return MmsConfig.getMmsBoolConfig("enableCmasSettings", false);
    }

    public static boolean isHideMmsAutoRetrieval() {
        return MmsConfig.getMmsBoolConfig("hideAutoRetrieval", false);
    }

    public static boolean isHideMmsAutoRetrievalRoaming() {
        return MmsConfig.getMmsBoolConfig("hideRetrievalDuringRoaming", false);
    }

    public static boolean isHideRetrievalWithoutRoaming() {
        return MmsConfig.getMmsBoolConfig("hideRetrievalWithoutRoaming", true);
    }

    public static boolean allowSubject() {
        return MmsConfig.getMmsBoolConfig("allowSubject", true);
    }

    public static boolean isHideKeyboard() {
        return MmsConfig.getMmsBoolConfig("hideKeyboardAfterSendMessage", false);
    }

    public static boolean isHeaderSprintCustom() {
        return MmsConfig.getMmsBoolConfig("headerSprintCustom", false);
    }

    public static boolean isUserAgentSrpintCustom() {
        return MmsConfig.getMmsBoolConfig("userAgentSprintCustom", false);
    }

    public static boolean isShowUserAgentWithNoDash() {
        return MmsConfig.getMmsBoolConfig("custUserAgentWithNoDash", false);
    }

    public static String getUserAgentCustString() {
        return MmsConfig.getMmsStringConfig("custUserAgentByProduct", null);
    }

    public static boolean allowFwdWapPushMsg() {
        return MmsConfig.getMmsBoolConfig("allowFwdWapPushMsg", true);
    }

    public static boolean isDiscardSmsBackslash() {
        return MmsConfig.getMmsBoolConfig("smsDiscardBackslash", false);
    }

    public static boolean showToastWhenSendError() {
        return MmsConfig.getMmsBoolConfig("isShow_Toast_When_SendError", false);
    }

    public static boolean isInvalidAddressRequestFocus() {
        return MmsConfig.getMmsBoolConfig("isInvalid_Address_RequestFocus", false);
    }

    public static boolean getEnableAlertLongSms() {
        return MmsConfig.getMmsBoolConfig("enableAlertLongSms", false);
    }

    public static boolean getRemoveMms() {
        if (getAdaptForbidMmsList() == null) {
            mRemoveMms = Boolean.valueOf(MmsConfig.getMmsBoolConfig("removeMms", false));
        }
        return mRemoveMms.booleanValue();
    }

    public static void setRemoveMms(boolean isRemove) {
        mRemoveMms = Boolean.valueOf(isRemove);
    }

    public static String getAdaptForbidMmsList() {
        return MmsConfig.getMmsStringConfig("custAdaptListForbidMms", null);
    }

    public static boolean getEnableMmsReportMoreStatus() {
        return MmsConfig.getMmsBoolConfig("enableMmsReportMoreStatus", false);
    }

    public static boolean check7DigitNumber() {
        return MmsConfig.getMmsBoolConfig("isCheck7DigitNumber", false);
    }

    public static boolean isDisableSmileyInputConnection() {
        return MmsConfig.getMmsBoolConfig("disableSmileyInputConnection", false);
    }

    public static int getMaxCTRoamingMultipartSms7Bit() {
        return MmsConfig.getMmsIntConfig("maxCTRoamingMultipartSms7Bit", -1) > -1 ? MmsConfig.getMmsIntConfig("maxCTRoamingMultipartSms7Bit", -1) : MmsConfig.getMaxTextLimit();
    }

    public static int getMaxCTRoamingMultipartSms16Bit() {
        return MmsConfig.getMmsIntConfig("maxCTRoamingMultipartSms16Bit", -1) > -1 ? MmsConfig.getMmsIntConfig("maxCTRoamingMultipartSms16Bit", -1) : MmsConfig.getMaxTextLimit();
    }

    public static boolean isCTRoamingMultipartSmsLimit() {
        return MmsConfig.getMmsBoolConfig("CTRoamingMultipartSmsLimit", false);
    }

    public static boolean getEnableToastInSlideshowWithVcardOrVcal() {
        return MmsConfig.getMmsBoolConfig("showToastinSlideshowwithVcardorVcal", false);
    }

    public static String getCustSMSCAddress() {
        return MmsConfig.getMmsStringConfig("custSMSCAddress", null);
    }

    public static boolean getEnableSlWapPushMessageOpenBrowser() {
        return MmsConfig.getMmsBoolConfig("enableSlWapPushMessageOpenBrowser", false);
    }

    public static boolean getEnableToastWhenRoamingDataClose() {
        return MmsConfig.getMmsBoolConfig("enableToastWhenRoamingDataClose", false);
    }

    public static boolean getEnablePeopleActionBarMultiLine() {
        return MmsConfig.getMmsBoolConfig("enablePeopleActionBarMultiLine", false);
    }

    public static boolean getEnableSmsDeliverToast() {
        return MmsConfig.getMmsBoolConfig("eableSmsDeliverToast", true);
    }

    public static boolean getEnableSmsNotifyInSilentMode() {
        return MmsConfig.getMmsBoolConfig("enableSmsNotifyInSilentMode", true);
    }

    public static boolean getConfigRoamingNationalAsLocal() {
        return MmsConfig.getMmsBoolConfig("configRoamingNationalAsLocal", false);
    }

    public static boolean enableMmsQueuedToast() {
        return MmsConfig.getMmsBoolConfig("enableMmsQueuedToast", false);
    }

    public static boolean getAddSubject() {
        return MmsConfig.getMmsBoolConfig("addSubjectForSimpleUI", false);
    }

    public static boolean getEnableCotaFeature() {
        return MmsConfig.getMmsBoolConfig("enableCotaFeatrue", false);
    }

    public static boolean getIsTitleChangeWhenRecepientsChange() {
        return MmsConfig.getMmsBoolConfig("isTitleChangeWhenRecepientsChange", false);
    }

    public static boolean isChameleon() {
        return MmsConfig.getMmsBoolConfig("is_chameleon_uaprof", false);
    }

    public static String getUaprofSoftVersion() {
        String displayId = Build.DISPLAY;
        return displayId != null ? displayId + SUFFIX_UAPROF_FOR_CHAMELEON : DEFAULT_UAPROF_FOR_CHAMELEON;
    }

    public static boolean allowSendSmsToEmail() {
        return MmsConfig.getMmsBoolConfig("eableSmsSendEmail", false) && MmsConfig.getEmailGateway() != null;
    }

    public static boolean allowMmsOverWifi() {
        return MmsConfig.getMmsBoolConfig("mmsOverWifi", false);
    }

    public static boolean isEnableReportAllowed() {
        return MmsConfig.getMmsBoolConfig("isenablereportallowed", false);
    }

    public static boolean isAllowAttachRecordSound() {
        return MmsConfig.getMmsBoolConfig("isallowattachrecordsound", false);
    }

    public static String getCustomMccMncMmsExpiry() {
        return MmsConfig.getMmsStringConfig("custMccMncForMmsExpiry", null);
    }

    public int getMccMncMmsExpiry(int lMmsExpiry) {
        String operator = MccMncConfig.getDefault().getOperator();
        String operatorMmsExpirylist = getCustomMccMncMmsExpiry();
        if (!(TextUtils.isEmpty(operatorMmsExpirylist) || TextUtils.isEmpty(operator))) {
            String[] custValues = operatorMmsExpirylist.split(",");
            for (String split : custValues) {
                String[] tempValues = split.split(":");
                if (tempValues[0].equals(operator)) {
                    return Integer.parseInt(tempValues[1]);
                }
            }
        }
        return lMmsExpiry;
    }

    public static boolean isDiscardSms() {
        return MmsConfig.getMmsBoolConfig("discardSMSFrom3311", false);
    }

    public static String getCustMccForBrazilMMSDeliveryReports() {
        return MmsConfig.getMmsStringConfig("custMccForBrazilMMSDeliveryReports", null);
    }

    public boolean getCustDefaultMMSDeliveryReports(boolean defaultMMSDeliveryReports) {
        boolean flag = defaultMMSDeliveryReports;
        String custPlmnsString = getCustMccForBrazilMMSDeliveryReports();
        Log.d(TAG, "getCustDefaultMMSDeliveryReports called >>>begin: flag=" + defaultMMSDeliveryReports);
        if (custPlmnsString != null) {
            String brazilFlag = Systemex.getString(MmsApp.getApplication().getApplicationContext().getContentResolver(), HwCustMccMncConfigImpl.BRAZIL_MMS_DELIVERY_REPORTS_FLAG);
            if (!TextUtils.isEmpty(brazilFlag) && "true".equals(brazilFlag)) {
                flag = true;
            }
        }
        Log.d(TAG, "getCustDefaultMMSDeliveryReports called >>>end: flag=" + flag);
        return flag;
    }

    public static boolean getSaveMmsEmailAdress() {
        return MmsConfig.getMmsBoolConfig("enableSaveMmsEmailAdress", false);
    }

    public static boolean isHideDeliveryReportsItem() {
        return MmsConfig.getMmsBoolConfig("isHideDeliveryReports", false);
    }

    public static String getMmsParams() {
        return MmsConfig.getMmsStringConfig("mmsParams", null);
    }

    public static boolean isEnableFdnCheckForMms() {
        return MmsConfig.getMmsBoolConfig("enableFdnCheckForMms", false);
    }

    public static boolean isNotLimitToRoamingState() {
        return MmsConfig.getMmsBoolConfig("isNotLimitToRoamingState", false);
    }

    public boolean custEnableForwardMessageFrom(boolean defaultValue) {
        return MmsConfig.getMmsBoolConfig("custEnableForwardMessageFrom", defaultValue);
    }

    public int getCustRecipientLimit(boolean isMms, int defaultValue) {
        if (isMms) {
            return MmsConfig.getMmsIntConfig("custMmsRecipientLimit", defaultValue);
        }
        return defaultValue;
    }

    public static boolean supportReplyInGroupMessage() {
        return MmsConfig.getMmsBoolConfig("supportReplyInGroupMessage", false);
    }

    public boolean isNotifyMsgtypeChangeEnable(boolean defaultValue) {
        return MmsConfig.getMmsBoolConfig("notifyMsgtypeChange", defaultValue);
    }

    public boolean isRefreshRxNumByMccMnc(boolean defaultValue) {
        if (defaultValue) {
            return true;
        }
        String operator = MccMncConfig.getDefault().getOperator();
        String mccMncList = MmsConfig.getMmsStringConfig("isRefreshRxNum", null);
        if (!(TextUtils.isEmpty(mccMncList) || TextUtils.isEmpty(operator))) {
            for (String mccMnc : mccMncList.split(",")) {
                if (mccMnc.equals(operator)) {
                    return true;
                }
            }
        }
        return defaultValue;
    }

    public static String getCustRecipientLimitBySimCard() {
        return MmsConfig.getMmsStringConfig("custMmsRecipientLimitBySimCard", null);
    }

    public static boolean supportAutoDelete() {
        return MmsConfig.getMmsBoolConfig("supportAutoDelete", false);
    }

    public static boolean isEnableLocalTime() {
        return MmsConfig.getMmsBoolConfig("enableUseLocalTime", true);
    }

    public static boolean supportEmptyFWDSubject() {
        return MmsConfig.getMmsBoolConfig("supportEmptyForwardSubject", true);
    }

    public static boolean isPoundCharValid() {
        return MmsConfig.getMmsBoolConfig("isPoundCharValid", false);
    }

    public static boolean isInEncrypt(Context context) {
        boolean z = true;
        if (!ENCRYPT_PROP) {
            return false;
        }
        if (context == null || context.getContentResolver() == null) {
            Log.i(TAG, "isInEncrypt getContentResolver fail !");
            return false;
        }
        if (mIsEncryptCallEnabled == null) {
            int encryptCallStatus = Secure.getInt(context.getContentResolver(), ENCRYPT_CALL_FEATURE_KEY, 0);
            Log.i(TAG, "isInEncrypt encryptCallStatus = " + encryptCallStatus);
            if (1 != encryptCallStatus) {
                z = false;
            }
            mIsEncryptCallEnabled = Boolean.valueOf(z);
        }
        return mIsEncryptCallEnabled.booleanValue();
    }

    public static String getCustomMccMncMmsRetrive() {
        return MmsConfig.getMmsStringConfig("custMccMncForMmsRetrive", null);
    }

    public int getMccMncMmsRetrive(int lDefaultAutoRetrievalMms) {
        String operatorMmsModelist = getCustomMccMncMmsRetrive();
        if (TextUtils.isEmpty(operatorMmsModelist)) {
            return lDefaultAutoRetrievalMms;
        }
        String operator = MccMncConfig.getDefault().getOperator();
        if (!MccMncConfig.isValideOperator(operator)) {
            return lDefaultAutoRetrievalMms;
        }
        String[] custValues = operatorMmsModelist.split(",");
        for (String split : custValues) {
            String[] tempValues = split.split(":");
            if (tempValues[0].equals(operator)) {
                lDefaultAutoRetrievalMms = Integer.parseInt(tempValues[1]);
                Log.i(TAG, IccidInfoManager.OPERATOR + operator);
                return lDefaultAutoRetrievalMms;
            }
        }
        return lDefaultAutoRetrievalMms;
    }

    public static String getCustReplaceSMSCAddressByCard() {
        return MmsConfig.getMmsStringConfig("custReplaceSMSCAddressByCard", null);
    }

    public static boolean isEnableContactName() {
        return MmsConfig.getMmsBoolConfig("enableShowContactName", false);
    }

    public static String getMccDefault7BitOff() {
        return MmsConfig.getMmsStringConfig("mccDefault7BitOff", null);
    }

    public static boolean isEnableUaPrefixHuawei() {
        return MmsConfig.getMmsBoolConfig("enableUaPrefixHuawei", true);
    }

    public static boolean getSupportSearchConversation() {
        return MmsConfig.getMmsBoolConfig("supportSearchConversation", false);
    }

    public boolean isSupportSubjectForSimpleUI() {
        return MmsConfig.getMmsBoolConfig("addSubjectForSimpleUI", false);
    }

    public static String getCustConfigForDeliveryReports() {
        return MmsConfig.getMmsStringConfig("custConfigForDeliveryReports", null);
    }

    public static String getCustConfigForMmsReadReports() {
        return MmsConfig.getMmsStringConfig("custConfigForMmsReadReports", null);
    }

    public static String getCustConfigForMmsReplyReadReports() {
        return MmsConfig.getMmsStringConfig("custConfigForMmsReplyReadReports", null);
    }

    public int getCustConfigForDeliveryReports(int defaultDeliveryReports) {
        int result = defaultDeliveryReports;
        if (TextUtils.isEmpty(getCustConfigForDeliveryReports())) {
            return result;
        }
        int dbFlag = System.getInt(MmsApp.getApplication().getApplicationContext().getContentResolver(), "enable_delivery_reports", 0);
        if (dbFlag > 0) {
            return dbFlag;
        }
        return result;
    }

    public boolean getCustConfigForMmsReadReports(boolean defaultMmsReadReports) {
        boolean result = defaultMmsReadReports;
        if (TextUtils.isEmpty(getCustConfigForMmsReadReports())) {
            return result;
        }
        String dbFlag = System.getString(MmsApp.getApplication().getApplicationContext().getContentResolver(), "enable_mms_read_reports");
        if (TextUtils.isEmpty(dbFlag) || !"true".equals(dbFlag)) {
            return result;
        }
        return true;
    }

    public boolean getCustConfigForMmsReplyReadReports(boolean defaultMmsReplyReadReports) {
        boolean result = defaultMmsReplyReadReports;
        if (TextUtils.isEmpty(getCustConfigForMmsReplyReadReports())) {
            return result;
        }
        String dbFlag = System.getString(MmsApp.getApplication().getApplicationContext().getContentResolver(), "enable_mms_reply_read_reports");
        if (TextUtils.isEmpty(dbFlag) || !"true".equals(dbFlag)) {
            return result;
        }
        return true;
    }

    public static String getDisableSmsDeliverToastByCard() {
        return MmsConfig.getMmsStringConfig("disableSmsDeliverToastByCard", null);
    }
}
