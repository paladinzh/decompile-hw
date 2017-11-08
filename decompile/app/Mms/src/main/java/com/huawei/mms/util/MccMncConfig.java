package com.huawei.mms.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Build;
import android.os.SystemProperties;
import android.preference.PreferenceManager;
import android.provider.Settings.System;
import android.telephony.MSimTelephonyManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import com.android.mms.MmsApp;
import com.android.mms.MmsConfig;
import com.android.mms.ui.HwCustMessageUtilsImpl;
import com.android.mms.ui.MessageUtils;
import com.google.android.gms.R;
import com.huawei.cspcommon.MLog;
import com.huawei.cust.HwCustUtils;
import com.huawei.mms.util.HwTelephony.HwSimStateListener;
import java.lang.reflect.Method;
import java.util.ArrayList;

public class MccMncConfig {
    private static int MCCMNC_MIN_LENGTH = 5;
    private static final int[] OP_NAMES_CHINA = new int[]{R.string.china_mobile, R.string.china_unicom, R.string.china_mobile, R.string.china_telecom, 0, R.string.china_telecom, R.string.china_unicom, R.string.china_mobile};
    private static String TAG = "MccMncConfig";
    private static HwCustMccMncConfig mHwCustMccMncConfig = ((HwCustMccMncConfig) HwCustUtils.createObj(HwCustMccMncConfig.class, new Object[]{MmsApp.getApplication().getApplicationContext()}));
    private static final String[][] mMccMncDefaultSettingsTable;
    private static BroadcastReceiver mSetGlobalParamDoneReceiver = new SetGlobalParamDoneReceiver();
    private static OperatorChecker sChecker = null;

    public interface operatorChangeListener {
        void onOperatorChange(OperatorChecker operatorChecker);
    }

    public static class OperatorChecker implements HwSimStateListener {
        private String mOperator = "";

        public OperatorChecker() {
            setOperator(HwTelephony.getDefault().getSimOperator());
        }

        protected boolean match(String... mccmncs) {
            for (String item : mccmncs) {
                if (MmsConfig.isStringStartWithPrefix(this.mOperator, item)) {
                    return true;
                }
            }
            return false;
        }

        public void setOperator(String operator) {
            MLog.d(MccMncConfig.TAG, "Set Single Operator " + operator);
            if (operator != null) {
                this.mOperator = operator;
            }
        }

        public String getOperator() {
            return this.mOperator;
        }

        public String getOperator(int subId) {
            return this.mOperator;
        }

        public boolean hasInnerOperator() {
            return this.mOperator.startsWith("46060");
        }

        protected String filtNumber(String number) {
            if (this.mOperator.startsWith("262")) {
                return number.replace("/", "");
            }
            return number;
        }

        private void setLocalSpecialFeature() {
            if (match("52505", "52503", "52501")) {
                MmsConfig.setUserAgent("Huawei_" + Build.PRODUCT + "-Android-Mms/2.0");
                return;
            }
            if (match("73406", "73404")) {
                MmsConfig.setForbiddenSetFrom(true);
            }
        }

        public void checkSettings() {
            MccMncConfig.parseMccMncDefaultSettingsTable(getOperator());
            if (MccMncConfig.mHwCustMccMncConfig != null) {
                MccMncConfig.mHwCustMccMncConfig.setMMSDeliveryReportsForBrazilCard();
                MccMncConfig.mHwCustMccMncConfig.setDefaultAutoRetrievalMmsForOperator();
            }
            setLocalSpecialFeature();
            MccMncConfig.init7bitsSettings();
            if (!TextUtils.isEmpty(MmsConfig.getRussiaMccBlankSMS())) {
                MmsConfig.setSendingBlankSMSEnabledForRussia();
            }
            if (MmsConfig.isEnabletMmsParamsFromGlobal()) {
                MccMncConfig.initMccMncParameterSettings();
            }
        }

        public boolean isOperatorLoaded() {
            return MccMncConfig.isValideOperator(this.mOperator);
        }

        public boolean isSub1AsDefault() {
            return true;
        }

        public void onSimStateChanged(int simState) {
            if (simState == 5 && !isOperatorLoaded()) {
                setOperator(HwTelephony.getDefault().getSimOperator());
                checkSettings();
            }
        }

        public void onSimStateChanged(int simState, int subId) {
        }
    }

    private static class MSimOperatorChecker extends OperatorChecker {
        private boolean mIsCard1Prior = true;
        private String mOperatorSub1 = "";
        private String mOperatorSub2 = "";

        public MSimOperatorChecker() {
            MSimTelephonyManager phone = MmsApp.getDefaultMSimTelephonyManager();
            int state1 = phone.getSimState(0);
            if (phone.getSimState(1) == 5) {
                this.mOperatorSub2 = phone.getSimOperator(1);
                this.mIsCard1Prior = false;
            }
            if (state1 == 5) {
                this.mOperatorSub1 = phone.getSimOperator(0);
                this.mIsCard1Prior = true;
            }
        }

        public boolean hasInnerOperator() {
            if (this.mOperatorSub1.startsWith("46060")) {
                return true;
            }
            return this.mOperatorSub2.startsWith("46060");
        }

        public void setOperator(String operator) {
        }

        public String getOperator() {
            return this.mIsCard1Prior ? this.mOperatorSub1 : this.mOperatorSub2;
        }

        protected boolean matchAny(String number) {
            return !this.mOperatorSub1.startsWith(number) ? this.mOperatorSub2.startsWith(number) : true;
        }

        protected boolean match(String... mccmncs) {
            for (String item : mccmncs) {
                if (getOperator().startsWith(item)) {
                    return true;
                }
            }
            return false;
        }

        protected String filtNumber(String number) {
            if (matchAny("262")) {
                return number.replace("/", "");
            }
            return number;
        }

        public void setOperator(String operator, int sub) {
            boolean z = false;
            MLog.d(MccMncConfig.TAG, "Set multy sub-" + sub + " Operator " + operator);
            if (sub == 0) {
                this.mOperatorSub1 = operator;
                this.mIsCard1Prior = true;
                return;
            }
            this.mOperatorSub2 = operator;
            if (!TextUtils.isEmpty(this.mOperatorSub1)) {
                z = true;
            }
            this.mIsCard1Prior = z;
        }

        public boolean isOperatorLoaded() {
            return !MccMncConfig.isValideOperator(this.mOperatorSub1) ? MccMncConfig.isValideOperator(this.mOperatorSub2) : true;
        }

        public String getOperator(int subId) {
            if (subId == 1) {
                return this.mOperatorSub2;
            }
            return this.mOperatorSub1;
        }

        public boolean isOperatorLoaded(int subId) {
            return MccMncConfig.isValideOperator(getOperator(subId));
        }

        public boolean isSub1AsDefault() {
            return this.mIsCard1Prior;
        }

        public void onSimStateChanged(int simState, int subId) {
            if (simState == 5 && !isOperatorLoaded(subId)) {
                if (subId == 1 && isOperatorLoaded(0)) {
                    MLog.d(MccMncConfig.TAG, "onSimStateChanged for sub2 and sub1 is loaded");
                }
                setOperator(MmsApp.getDefaultMSimTelephonyManager().getSimOperator(subId), subId);
                checkSettings();
            }
        }
    }

    private static class SetGlobalParamDoneReceiver extends BroadcastReceiver {
        private SetGlobalParamDoneReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            String mccmnc = intent.getStringExtra("mccMnc");
            if ("android.intent.action.ACTION_SET_GLOBAL_AUTO_PARAM_DONE".equals(action)) {
                MLog.i(MccMncConfig.TAG, "received ACTION_SET_GLOBAL_AUTO_PARAM_DONE ");
                if (MccMncConfig.isValideOperator(mccmnc)) {
                    if (!(MccMncConfig.mHwCustMccMncConfig == null || MccMncConfig.mHwCustMccMncConfig.getCustForbidMmsList() == null)) {
                        MccMncConfig.mHwCustMccMncConfig.initForbidMmsFeature(context, mccmnc);
                    }
                    if (MmsConfig.getSmsOptimizationCharacters()) {
                        MessageUtils.setAlwaysShowSmsOptimization(mccmnc);
                        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(MmsApp.getApplication());
                        Editor editor = sp.edit();
                        int[] temp = new int[1];
                        if (!sp.getString("pref_last_mccmnc", "").equals(mccmnc)) {
                            temp[0] = SystemProperties.getInt("gsm.sms.coding.national", 0);
                            MLog.i(MccMncConfig.TAG, "SetGlobalParamDoneReceiver mccmnc changed, sms coding = " + temp[0]);
                            try {
                                if (!System.putInt(context.getContentResolver(), "sms_coding_national_backup", temp[0])) {
                                    MLog.e(MccMncConfig.TAG, "Settings System putInt sms_coding_national_backup failed, this may incur error");
                                }
                            } catch (Exception e) {
                                MLog.e(MccMncConfig.TAG, "Settings System putInt sms_coding_national_backup error");
                            }
                            MmsConfig.setDefault7bitOptionValue();
                            MessageUtils.reset7BitEnabledValue();
                            editor.putString("pref_last_mccmnc", mccmnc).commit();
                            editor.putBoolean("pref_key_sms_optimization_characters", MmsConfig.getDefault7bitOptionValue());
                            if (temp[0] != 0) {
                                if (context.getApplicationContext().checkSelfPermission("android.permission.SEND_SMS") == 0) {
                                    MessageUtils.setSmsCodingNationalCode(String.valueOf(temp[0]));
                                    MessageUtils.setSingleShiftTable(temp);
                                    editor.putBoolean("pref_key_sms_optimization_characters", true);
                                } else {
                                    return;
                                }
                            }
                            editor.commit();
                        }
                        MmsConfig.setDefault7bitOptionValue();
                        MessageUtils.reset7BitEnabledValue();
                        temp[0] = System.getInt(MmsApp.getApplication().getContentResolver(), "sms_coding_national_backup", 0);
                        if (temp[0] == 0 || !sp.getBoolean("pref_key_sms_optimization_characters", false)) {
                            temp[0] = 0;
                            if (context.getApplicationContext().checkSelfPermission("android.permission.SEND_SMS") == 0) {
                                MessageUtils.setSmsCodingNationalCode("0");
                                MessageUtils.setSingleShiftTable(temp);
                            }
                        }
                    }
                }
            }
        }
    }

    static {
        r0 = new String[8][];
        r0[0] = new String[]{"232", "defaultSMSDeliveryReports", "false"};
        r0[1] = new String[]{"232", "defaultMMSDeliveryReports", "false"};
        r0[2] = new String[]{"234", "defaultSMSDeliveryReports", "false"};
        r0[3] = new String[]{"234", "defaultMMSDeliveryReports", "false"};
        r0[4] = new String[]{"293", "defaultSMSDeliveryReports", "false"};
        r0[5] = new String[]{"293", "defaultMMSDeliveryReports", "false"};
        r0[6] = new String[]{HwCustMessageUtilsImpl.MCCMNC_PLAY, "defaultSMSDeliveryReports", "false"};
        r0[7] = new String[]{HwCustMessageUtilsImpl.MCCMNC_PLAY, "defaultMMSDeliveryReports", "false"};
        mMccMncDefaultSettingsTable = r0;
    }

    public static boolean init() {
        Log.logPerformance("MccMncConfig init start");
        if (getDefault().isOperatorLoaded()) {
            getDefault().checkSettings();
            return true;
        }
        MLog.d(TAG, "MccMncConfig init Fail ");
        return false;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static OperatorChecker getDefault() {
        synchronized (MccMncConfig.class) {
            if (sChecker != null) {
                OperatorChecker operatorChecker = sChecker;
                return operatorChecker;
            }
        }
    }

    public static BroadcastReceiver registerForOperatorChange(Context context, final operatorChangeListener l) {
        return HwTelephony.registeSimChange(context, new HwSimStateListener() {
            public void onSimStateChanged(int simState) {
                MccMncConfig.getDefault().onSimStateChanged(simState);
                l.onOperatorChange(MccMncConfig.getDefault());
            }

            public void onSimStateChanged(int simState, int subId) {
                MccMncConfig.getDefault().onSimStateChanged(simState, subId);
                l.onOperatorChange(MccMncConfig.getDefault());
            }
        });
    }

    public static void registerSimReadyChange() {
        IntentFilter globalParamsSetFilter = new IntentFilter();
        globalParamsSetFilter.addAction("android.intent.action.ACTION_SET_GLOBAL_AUTO_PARAM_DONE");
        MmsApp.getApplication().getApplicationContext().registerReceiver(mSetGlobalParamDoneReceiver, globalParamsSetFilter);
    }

    public static void unRegisterSimReadyChange() {
        MmsApp.getApplication().getApplicationContext().unregisterReceiver(mSetGlobalParamDoneReceiver);
    }

    public static boolean isValideOperator(String operator) {
        if (operator == null || operator.length() < MCCMNC_MIN_LENGTH) {
            return false;
        }
        return true;
    }

    public static boolean is7bitEnable() {
        boolean sms7BitEnabled;
        if (!MmsConfig.isSms7BitEabled() || isNorthEastEuropeMCC()) {
            sms7BitEnabled = is7bitsEnableInGlobalAutoAdapt();
        } else {
            sms7BitEnabled = true;
        }
        if (!MmsConfig.getSmsOptimizationCharacters()) {
            return sms7BitEnabled;
        }
        if (MessageUtils.isSmsOptimizationEnabled(MmsApp.getApplication())) {
            sms7BitEnabled = true;
        } else {
            sms7BitEnabled = false;
        }
        if (MmsConfig.isLossless7bit()) {
            return false;
        }
        return sms7BitEnabled;
    }

    private static boolean isNorthEastEuropeMCC() {
        ArrayList<String> norEastEuroMCCList = MmsConfig.getNorthEastEuropeMccList();
        if (norEastEuroMCCList == null || norEastEuroMCCList.isEmpty()) {
            return false;
        }
        String currentMccmnc = getDefault().getOperator();
        if (isValideOperator(currentMccmnc)) {
            return norEastEuroMCCList.contains(currentMccmnc.substring(0, 3));
        }
        return false;
    }

    private static void init7bitsSettings() {
        if (!MmsConfig.isEuropeCust() || !is7bitsEnableInGlobalAutoAdapt()) {
            String alphabetFromHwdefaults = MmsConfig.getChar_7bit();
            if (!(alphabetFromHwdefaults == null || "".equals(alphabetFromHwdefaults))) {
                MessageUtils.set7bitsTable(alphabetFromHwdefaults);
                MmsConfig.setHas7BitAlaphsetInHwDefaults(true);
            }
        }
    }

    private static boolean is7bitsEnableInGlobalAutoAdapt() {
        boolean sms7BitEnabled = false;
        if (MmsApp.getDefaultTelephonyManager() != null) {
            try {
                Method get7BitEnabled = TelephonyManager.getDefault().getClass().getDeclaredMethod("isSms7BitEnabled", new Class[0]);
                get7BitEnabled.setAccessible(true);
                sms7BitEnabled = ((Boolean) get7BitEnabled.invoke(MmsApp.getDefaultTelephonyManager(), new Object[0])).booleanValue();
            } catch (RuntimeException e) {
                MLog.e(TAG, "RuntimeException occured in ComposeMessageActivity.TextWatcher");
            } catch (Exception e2) {
                MLog.e(TAG, "No function to isSms7BitEnabled");
            }
        }
        return sms7BitEnabled;
    }

    protected static void parseMccMncDefaultSettingsTable(String operator) {
        MLog.d(TAG, "parseMccMncDefaultSettingsTable called >> phone mccmc" + operator);
        for (String[] parameterSet : mMccMncDefaultSettingsTable) {
            String mccmnc = parameterSet[0];
            if (MmsConfig.isStringStartWithPrefix(operator, mccmnc)) {
                String paraName = parameterSet[1];
                boolean paraDefaultsSet = Boolean.valueOf(parameterSet[2]).booleanValue();
                MLog.d(TAG, "configuration  >> mccmc" + mccmnc + " " + paraName + " = " + paraDefaultsSet);
                if (MmsConfig.isStringEqual("defaultSMSDeliveryReports", paraName)) {
                    MmsConfig.setDefaultSMSDeliveryReports(paraDefaultsSet);
                } else if (MmsConfig.isStringEqual("defaultMMSDeliveryReports", paraName)) {
                    MmsConfig.setDefaultMMSDeliveryReports(paraDefaultsSet);
                }
            }
        }
    }

    public static String getFilterNumberByMCCMNC(String number) {
        return getDefault().filtNumber(number);
    }

    public static void initMccMncParameterSettings() {
        int maxMessageSize = SystemProperties.getInt("gsm.sms.max.message.size", 0);
        int smsToMmsTextThreshold = SystemProperties.getInt("gsm.sms.to.mms.textthreshold", 0);
        MLog.d(TAG, "gsm.sms value smsToMmsTextThreshold = " + smsToMmsTextThreshold + "  maxMessageSize = " + maxMessageSize);
        if (maxMessageSize == 0) {
            maxMessageSize = MmsConfig.getInitMmsIntConfig("maxMessageSize", 307200);
        }
        if (smsToMmsTextThreshold == 0) {
            smsToMmsTextThreshold = MmsConfig.getInitMmsIntConfig("smsToMmsTextThreshold", 11);
        }
        if (smsToMmsTextThreshold == -1) {
            MmsConfig.setMultipartSmsEnabled(true);
        } else {
            MmsConfig.setMultipartSmsEnabled(false);
        }
        MLog.d(TAG, "smsToMmsTextThreshold = " + smsToMmsTextThreshold + "  maxMessageSize = " + maxMessageSize);
        MmsConfig.setSmsToMmsTextThreshold(smsToMmsTextThreshold);
        MmsConfig.setMaxMessageSize(maxMessageSize);
    }

    public static boolean isChinaMobieOperator(String operatorCode) {
        if (operatorCode != null && operatorCode.startsWith("4600") && (operatorCode.endsWith("0") || operatorCode.endsWith("2") || operatorCode.endsWith("7"))) {
            return true;
        }
        return false;
    }

    public static boolean isChinaUnicomOperator(String operatorCode) {
        if (operatorCode != null && operatorCode.startsWith("4600") && (operatorCode.endsWith("1") || operatorCode.endsWith("6") || operatorCode.endsWith("9"))) {
            return true;
        }
        return false;
    }
}
