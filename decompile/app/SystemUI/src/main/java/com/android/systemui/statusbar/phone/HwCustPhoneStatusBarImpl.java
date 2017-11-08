package com.android.systemui.statusbar.phone;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings.System;
import android.telephony.HwTelephonyManager;
import android.telephony.MSimTelephonyManager;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import com.android.systemui.R;
import com.android.systemui.qs.QSPanel;
import com.android.systemui.statusbar.policy.NetWorkUtils;
import com.android.systemui.utils.SystemUiUtil;
import com.huawei.android.provider.SettingsEx.Systemex;
import com.huawei.android.telephony.TelephonyManagerEx;
import fyusion.vislib.BuildConfig;
import java.util.List;
import java.util.Locale;

public class HwCustPhoneStatusBarImpl extends HwCustPhoneStatusBar {
    private static final /* synthetic */ int[] -com-android-systemui-statusbar-phone-HwCustPhoneStatusBarImpl$CustTypeSwitchesValues = null;
    private static final String ACTION_SIM_RECORDS_READY = "android.intent.action.ACTION_SIM_RECORDS_READY";
    private static final String CBS_CLASS_ACTIVITY = "com.android.cellbroadcastreceiver.ui.CellBroadcastAlertDialog";
    static final float CLOCK_FONT_SIZE = 32.0f;
    static final String CUST_ATT = "att";
    static final String CUST_EMO = "emo";
    static final String CUST_ORANGE = "orange";
    static final String CUST_OTHER = "other";
    static final String CUST_USA = "usa";
    static final float MINIMUN_FONT_SIZE = 12.0f;
    static final String OPERATORNAME = "OperatorName";
    private static final boolean PLMN_TO_SETTINGS = SystemProperties.getBoolean("ro.config.plmn_to_settings", false);
    private static final String START_PACKAGENAME = "com.celltick.lockscreen";
    static final String SUB_ID = "subId";
    static final String TAG = "HwCustPhoneStatusBar";
    private static final boolean UPDATE_NOTIFICATION_BAR_FONT = SystemProperties.getBoolean("ro.config.update_notif_bar_ui", false);
    private static boolean hw_sim2airplane = SystemProperties.getBoolean("ro.config.hw_sim2airplane", false);
    private static boolean hw_simplecover = SystemProperties.getBoolean("ro.config.hw_simplecover", false);
    public static final String mCustSimOperator = SystemProperties.get("ro.config.hw_cbs_mcc");
    private static final boolean mDisableHomeKey = SystemProperties.getBoolean("ro.config.disableHomeKey", false);
    private CustType mCustId = CustType.OTHER;
    private QSPanel mQSPanel;
    private BroadcastReceiver mSimCardReadyReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            int subId = intent.getIntExtra("subscription", SubscriptionManager.getDefaultSubscriptionId());
            Log.d(HwCustPhoneStatusBarImpl.TAG, "onReceive::action= " + action);
            if (HwCustPhoneStatusBarImpl.ACTION_SIM_RECORDS_READY.equals(action) && subId == TelephonyManagerEx.getDefault4GSlotId() && HwCustPhoneStatusBarImpl.this.isRemoveEnable4G(context)) {
                HwCustPhoneStatusBarImpl.this.mQSPanel.onTilesChanged();
            }
        }
    };

    enum CustType {
        OTHER,
        EMO,
        ATT,
        USA,
        ORANGE
    }

    private static /* synthetic */ int[] -getcom-android-systemui-statusbar-phone-HwCustPhoneStatusBarImpl$CustTypeSwitchesValues() {
        if (-com-android-systemui-statusbar-phone-HwCustPhoneStatusBarImpl$CustTypeSwitchesValues != null) {
            return -com-android-systemui-statusbar-phone-HwCustPhoneStatusBarImpl$CustTypeSwitchesValues;
        }
        int[] iArr = new int[CustType.values().length];
        try {
            iArr[CustType.ATT.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[CustType.EMO.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[CustType.ORANGE.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[CustType.OTHER.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[CustType.USA.ordinal()] = 5;
        } catch (NoSuchFieldError e5) {
        }
        -com-android-systemui-statusbar-phone-HwCustPhoneStatusBarImpl$CustTypeSwitchesValues = iArr;
        return iArr;
    }

    public HwCustPhoneStatusBarImpl(Context context) {
        super(context);
        init();
    }

    public HwCustPhoneStatusBarImpl() {
        init();
    }

    private void init() {
        String customForPLMN = SystemProperties.get("ro.sysui.plmn.cust", CUST_OTHER);
        if (customForPLMN.equals(CUST_EMO)) {
            this.mCustId = CustType.EMO;
        } else if (customForPLMN.equals(CUST_ATT)) {
            this.mCustId = CustType.ATT;
        } else if (customForPLMN.equals(CUST_USA)) {
            this.mCustId = CustType.USA;
        } else if (customForPLMN.equals(CUST_ORANGE)) {
            this.mCustId = CustType.ORANGE;
        } else {
            this.mCustId = CustType.OTHER;
        }
    }

    private boolean hasCustomForOperatorName() {
        switch (-getcom-android-systemui-statusbar-phone-HwCustPhoneStatusBarImpl$CustTypeSwitchesValues()[this.mCustId.ordinal()]) {
            case 1:
            case 2:
            case 3:
            case 5:
                return true;
            case 4:
                return false;
            default:
                return false;
        }
    }

    public String getCustomOperatorName(int sub, String str) {
        String newStr = new String(str);
        if (!hasCustomForOperatorName()) {
            return newStr;
        }
        Log.d(TAG, "getCustomOperatorName, sub:" + sub + " str:" + str);
        Bundle result = new Bundle();
        result.putInt(SUB_ID, sub);
        result.putString(OPERATORNAME, newStr);
        if (!getCustomForOperatorName(result)) {
            return newStr;
        }
        newStr = result.getString(OPERATORNAME, newStr);
        Log.d(TAG, "getCustomOperatorName, sub:" + sub + " newStr:" + newStr);
        return newStr;
    }

    private boolean getCustomForOperatorName(Bundle result) {
        if (result == null) {
            return false;
        }
        switch (-getcom-android-systemui-statusbar-phone-HwCustPhoneStatusBarImpl$CustTypeSwitchesValues()[this.mCustId.ordinal()]) {
            case 1:
                return getCustomForATT(result);
            case 2:
            case 4:
                return false;
            case 3:
                return getCustomForOrange(result);
            case 5:
                return getCustomForUSA(result);
            default:
                return false;
        }
    }

    private boolean getCustomForOrange(Bundle result) {
        if (isCardPresent()) {
            return false;
        }
        result.putString(OPERATORNAME, BuildConfig.FLAVOR);
        return true;
    }

    private boolean getCustomForATT(Bundle result) {
        TelephonyManager tm = (TelephonyManager) this.mContext.getSystemService("phone");
        if (!isCardPresent()) {
            result.putString(OPERATORNAME, this.mContext.getResources().getString(R.string.carrier_label_no_sim_card));
            return true;
        } else if (tm.getSimState() != 8) {
            return false;
        } else {
            result.putString(OPERATORNAME, this.mContext.getResources().getString(R.string.card_invalid));
            return true;
        }
    }

    private boolean getCustomForUSA(Bundle result) {
        if (((TelephonyManager) this.mContext.getSystemService("phone")).getSimState() != 8) {
            return false;
        }
        result.putString(OPERATORNAME, this.mContext.getResources().getString(R.string.card_invalid));
        return true;
    }

    private boolean isCardPresent() {
        boolean isCardpresent0;
        boolean isCardpresent1 = false;
        if (SystemUiUtil.isMulityCard(this.mContext)) {
            HwTelephonyManager htm = HwTelephonyManager.getDefault();
            isCardpresent0 = htm.isCardPresent(0);
            isCardpresent1 = htm.isCardPresent(1);
        } else {
            isCardpresent0 = ((TelephonyManager) this.mContext.getSystemService("phone")).getSimState() != 1;
        }
        if (isCardpresent0) {
            return true;
        }
        return isCardpresent1;
    }

    public void setPlmnToSettings(String networkName, int subscription) {
        if (PLMN_TO_SETTINGS && !TextUtils.isEmpty(networkName)) {
            Systemex.putString(this.mContext.getContentResolver(), subscription + "_plmn_servicestate_to_settings", networkName);
        }
    }

    public boolean hasIccCardVSimFixup(boolean present, int sub) {
        boolean bCardPresent = present;
        if (SystemUiUtil.isSupportVSim() && NetWorkUtils.getVSimSubId() == sub) {
            return true;
        }
        return bCardPresent;
    }

    public String getNetworkNameVSimFixup(String name, int sub) {
        String networkName = name;
        if (SystemUiUtil.isSupportVSim() && NetWorkUtils.getVSimSubId() == sub && NetWorkUtils.getVSimCurCardType() != 2) {
            return this.mContext.getString(R.string.carrier_label_no_service);
        }
        return networkName;
    }

    public boolean isEnableSimpleCover() {
        return hw_simplecover;
    }

    public boolean isNotShowJapaneseNoService(String networkName) {
        if (Systemex.getInt(this.mContext.getContentResolver(), "hw_not_show_ja_no_service", 0) == 1) {
            String language = Locale.getDefault().getLanguage();
            if ("ja".equals(language) && this.mContext.getString(17040012).equals(networkName)) {
                return true;
            }
            return "ru".equals(language) && this.mContext.getString(17040036).equals(networkName);
        }
    }

    public void updateNotificationFontSize(View parentView) {
        if (UPDATE_NOTIFICATION_BAR_FONT) {
            TextView clock = (TextView) parentView.findViewById(R.id.clock);
            TextView date = (TextView) parentView.findViewById(R.id.date);
            if (clock != null) {
                clock.setTextSize(2, CLOCK_FONT_SIZE);
            }
            if (date != null) {
                date.setTextSize(2, MINIMUN_FONT_SIZE);
            }
        }
    }

    public boolean isNotShowEmergencyForOrange(String networkName) {
        if (this.mCustId == CustType.ORANGE && (this.mContext.getString(17040036).equals(networkName) || this.mContext.getString(17040012).equals(networkName))) {
            return true;
        }
        return false;
    }

    public NavigationBarView getCustNavigationbarViewLayout(NavigationBarView currentView) {
        if (isSoftLockEnabled()) {
            return (NavigationBarView) View.inflate(this.mContext, R.layout.hw_navigation_bar_cust, null);
        }
        return currentView;
    }

    private boolean isSoftLockEnabled() {
        return SystemProperties.getBoolean("ro.config.soft_lock_enable", false);
    }

    public boolean disableNavigationKey() {
        if ((!isCustSimOperator(mCustSimOperator) && !mDisableHomeKey) || (!isTOPActivity(CBS_CLASS_ACTIVITY) && !isTOPActivity(START_PACKAGENAME))) {
            return false;
        }
        Log.d(TAG, "prevent recent or home click, return do nothing");
        return true;
    }

    public boolean isCustSimOperator(String mCustSimOperator) {
        if (TextUtils.isEmpty(mCustSimOperator)) {
            return false;
        }
        boolean flag;
        if (!SystemUiUtil.isMulityCard(this.mContext)) {
            flag = isCustPlmn(mCustSimOperator, MSimTelephonyManager.from(this.mContext).getSimOperator());
        } else if (isCustPlmn(mCustSimOperator, MSimTelephonyManager.from(this.mContext).getSimOperator(0))) {
            flag = true;
        } else {
            flag = isCustPlmn(mCustSimOperator, MSimTelephonyManager.from(this.mContext).getSimOperator(1));
        }
        return flag;
    }

    private boolean isCustPlmn(String custPlmnsString, String simMccMnc) {
        if (!(simMccMnc == null || simMccMnc.length() < 3 || custPlmnsString == null)) {
            String[] custPlmns = custPlmnsString.split(";");
            int i = 0;
            while (i < custPlmns.length) {
                if (simMccMnc.substring(0, 3).equals(custPlmns[i]) || simMccMnc.equals(custPlmns[i])) {
                    return true;
                }
                i++;
            }
        }
        return false;
    }

    private boolean isTOPActivity(String className) {
        try {
            List<RunningTaskInfo> tasks = ((ActivityManager) this.mContext.getSystemService("activity")).getRunningTasks(1);
            if (tasks == null || tasks.isEmpty()) {
                return false;
            }
            ComponentName componentInfo = ((RunningTaskInfo) tasks.get(0)).topActivity;
            return className.equals(componentInfo.getClassName()) || (mDisableHomeKey && className.equals(componentInfo.getPackageName()));
        } catch (Exception e) {
            Log.e(TAG, " Failure to get topActivity className " + e);
        }
    }

    public void registerReceivers(QSPanel qsPanel) {
        this.mQSPanel = qsPanel;
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_SIM_RECORDS_READY);
        this.mContext.registerReceiver(this.mSimCardReadyReceiver, intentFilter);
    }

    public void unregisterReceivers() {
        if (this.mSimCardReadyReceiver != null) {
            this.mContext.unregisterReceiver(this.mSimCardReadyReceiver);
            this.mSimCardReadyReceiver = null;
        }
    }

    public boolean isRemoveEnable4G(Context context) {
        boolean isRemove4G = false;
        String listOfMccMnc = System.getStringForUser(context.getContentResolver(), "hw_config_hide_4g_list", UserHandle.myUserId());
        if (TextUtils.isEmpty(listOfMccMnc)) {
            return false;
        }
        String currentMccMnc = TelephonyManager.getDefault().getSimOperator(TelephonyManagerEx.getDefault4GSlotId());
        if (!TextUtils.isEmpty(currentMccMnc)) {
            for (String mcc : listOfMccMnc.split(",")) {
                if (currentMccMnc.length() > 2 && (currentMccMnc.equals(mcc) || currentMccMnc.substring(0, 3).equals(mcc))) {
                    isRemove4G = true;
                    break;
                }
            }
        }
        return isRemove4G;
    }
}
