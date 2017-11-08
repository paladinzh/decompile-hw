package com.android.settings.wifi;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.SystemProperties;
import android.provider.SettingsEx.Systemex;
import android.support.v14.preference.SwitchPreference;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import com.android.settings.SettingsActivity;
import com.android.settings.UtilsCustEx;
import com.android.settings.wifi.p2p.WifiP2pSettings;
import com.android.settingslib.wifi.AccessPoint;
import com.huawei.android.wifi.p2p.WifiP2pManagerCustExt;
import java.io.File;
import java.util.Arrays;

public class HwCustWifiSettingsHwBaseImpl extends HwCustWifiSettingsHwBase {
    private static final String ATTWIFI = "attwifi";
    private static final String AUTOCONNECT = "auto_connect_att";
    private static final int AUTO_CONNECT_SWITCH_OPEN = 1;
    private static final int AUTO_CONNECT_SWITCH_VALUE = SystemProperties.getInt("ro.config.auto_connect_attwifi", 0);
    private static final String CONECTGUI_PROP_NAME = "sys.settings_is_connectgui";
    public static final String DATASWICH_ACTION_SETTING_CHANGED = "com.android.huawei.DATASERVICE_SETTING_CHANGED";
    private static final int DISABLE = 0;
    public static final String DMPROPERTY_DIRECTORY = "/data/OtaSave/Extensions/";
    public static final String DMPROPERTY_WIFI = "wifi.disable";
    private static final int MENU_ID_FORGET = 8;
    private static final int MENU_ID_MODIFY = 9;
    private static final String REQUEST_NETWORK_UP = "request_network_up";
    private static final String RequestMobileData_IS_FIRST_RUN = "RequestMobileFirstRun";
    public static final String SUPPLICANT_WAPI_EVENT = "android.net.wifi.supplicant.WAPI_EVENT";
    private static final String TAG = "HwCustWifiSettingsHwBaseImpl";
    public static final int WAPI_EVENT_AUTH_FAIL_CODE = 16;
    public static final int WAPI_EVENT_CERT_FAIL_CODE = 17;
    public static final String WIFI_ACTION_SETTING_CHANGED = "com.android.huawei.WIFI_ACTION_SETTING_CHANGED";
    private static final String WIFI_HOTSPOT_REDEFINDED = "Z736563757265";
    public static final String WIFI_P2P_ENABLE_CHANGED_ACTION = "android.net.wifi.p2p.ENABLE_CHANGED";
    private String isOnlyShowDialog = null;
    private String[] isOnlyShowDialogEntries = null;
    ConnectivityManager mConnectivityManager;
    CheckBox mDataBox;
    boolean mFlagForDsDisabled = false;
    private boolean mSetupMobileByFirstBoot;
    TelephonyManager mTelephonyManager;
    CheckBox mWifiBox;
    WifiManager mWifiManager;
    WifiP2pManagerCustExt mWifiP2pManagerCustExt;
    private WifiSettingsHwBase mWifiSettingsHwBase;

    public HwCustWifiSettingsHwBaseImpl(WifiSettingsHwBase wifiSettingsHwBase) {
        super(wifiSettingsHwBase);
        this.mWifiSettingsHwBase = wifiSettingsHwBase;
        if (this.mWifiP2pManagerCustExt == null) {
            this.mWifiP2pManagerCustExt = new WifiP2pManagerCustExt();
        }
    }

    public void handleEvent(Context context, Intent intent) {
        if (SUPPLICANT_WAPI_EVENT.equals(intent.getAction())) {
            int wapiGetEvent = intent.getIntExtra("wapi_string", 0);
            Log.w(TAG, "SUPPLICANT_WAPI_EVENT received: " + wapiGetEvent);
            switch (wapiGetEvent) {
                case 16:
                    Toast.makeText(context, 2131629083, 1).show();
                    return;
                default:
                    return;
            }
        }
    }

    public void addAction(IntentFilter mFilter) {
        mFilter.addAction(SUPPLICANT_WAPI_EVENT);
        mFilter.addAction(WIFI_P2P_ENABLE_CHANGED_ACTION);
        mFilter.addAction(DATASWICH_ACTION_SETTING_CHANGED);
        mFilter.addAction(WIFI_ACTION_SETTING_CHANGED);
    }

    public boolean dontShowWifiSkipDialog() {
        return SystemProperties.getBoolean("ro.config.hw_wifipopup_off", false);
    }

    public void handleNwSettingsChangedEvent(Context context, Intent intent) {
        String action = intent.getAction();
        if (WIFI_ACTION_SETTING_CHANGED.equals(action) || "android.net.wifi.WIFI_STATE_CHANGED".equals(action)) {
            if (this.mWifiBox != null && this.mWifiManager != null) {
                int wifiState = this.mWifiManager.getWifiState();
                boolean isEnabled = wifiState == 3;
                boolean isDisabled = wifiState == 1;
                if (isEnabled || isDisabled) {
                    this.mWifiBox.setChecked(isEnabled);
                }
                CheckBox checkBox = this.mWifiBox;
                if (isEnabled) {
                    isDisabled = true;
                }
                checkBox.setEnabled(isDisabled);
            }
        } else if (DATASWICH_ACTION_SETTING_CHANGED.equals(action) && this.mDataBox != null && this.mConnectivityManager != null) {
            this.mDataBox.setChecked(this.mConnectivityManager.getMobileDataEnabled());
        }
    }

    public void initService() {
        this.mConnectivityManager = (ConnectivityManager) this.mWifiSettingsHwBase.getActivity().getSystemService("connectivity");
        this.mWifiManager = (WifiManager) this.mWifiSettingsHwBase.getActivity().getSystemService("wifi");
        this.mTelephonyManager = (TelephonyManager) this.mWifiSettingsHwBase.getActivity().getSystemService("phone");
    }

    public boolean getFlagForDsDisabled() {
        if (this.mFlagForDsDisabled || this.mSetupMobileByFirstBoot) {
            return true;
        }
        return false;
    }

    public void setFlagForDsDisabled(Intent intent) {
        this.mFlagForDsDisabled = intent.getBooleanExtra(REQUEST_NETWORK_UP, false);
        this.mSetupMobileByFirstBoot = intent.getBooleanExtra(RequestMobileData_IS_FIRST_RUN, false);
    }

    public View getDsDisabledView(LayoutInflater inflater, ViewGroup container) {
        this.mWifiSettingsHwBase.getActivity().setTitle(2131629113);
        View view = inflater.inflate(2130969122, container, false);
        if (this.mSetupMobileByFirstBoot) {
            if (this.mFlagForDsDisabled) {
                view = inflater.inflate(2130969122, container, false);
            } else {
                view = inflater.inflate(2130969121, container, false);
            }
        }
        this.mDataBox = (CheckBox) view.findViewById(2131887170);
        this.mWifiBox = (CheckBox) view.findViewById(2131887176);
        Button mButton = (Button) view.findViewById(2131886371);
        if (!(this.mDataBox == null || mButton == null || this.mWifiBox == null || this.mConnectivityManager == null || this.mWifiManager == null)) {
            this.mDataBox.setChecked(this.mConnectivityManager.getMobileDataEnabled());
            this.mDataBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (HwCustWifiSettingsHwBaseImpl.this.mTelephonyManager != null) {
                        HwCustWifiSettingsHwBaseImpl.this.mTelephonyManager.setDataEnabled(isChecked);
                    }
                }
            });
            int wifiState = this.mWifiManager.getWifiState();
            boolean isEnabled = wifiState == 3;
            boolean isDisabled = wifiState == 1;
            this.mWifiBox.setChecked(isEnabled);
            CheckBox checkBox = this.mWifiBox;
            if (isEnabled) {
                isDisabled = true;
            }
            checkBox.setEnabled(isDisabled);
            this.mWifiBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (HwCustWifiSettingsHwBaseImpl.this.mWifiManager != null) {
                        HwCustWifiSettingsHwBaseImpl.this.mWifiManager.setWifiEnabled(isChecked);
                    }
                }
            });
            mButton.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    HwCustWifiSettingsHwBaseImpl.this.mWifiSettingsHwBase.getActivity().finish();
                }
            });
        }
        return view;
    }

    public void setMovementMethod(TextView textView) {
        textView.setMovementMethod(ScrollingMovementMethod.getInstance());
    }

    public boolean isSkipWifiSettingWithNoPrompt() {
        if (!"true".equals(Systemex.getString(this.mWifiSettingsHwBase.getActivity().getContentResolver(), "wifi_skip_not_display"))) {
            return false;
        }
        disableWifiIfNeed();
        this.mWifiSettingsHwBase.getActivity().setResult(1);
        this.mWifiSettingsHwBase.getActivity().finish();
        return true;
    }

    public void disableWifiIfNeed() {
        String wifiOffDefault = Systemex.getString(this.mWifiSettingsHwBase.getActivity().getContentResolver(), "wifi_off_default");
        if (this.mWifiManager == null) {
            this.mWifiManager = (WifiManager) this.mWifiSettingsHwBase.getActivity().getSystemService("wifi");
        }
        if ("true".equals(wifiOffDefault) && this.mWifiManager.isWifiEnabled()) {
            this.mWifiManager.setWifiEnabled(false);
        }
    }

    public boolean isSupportStaP2pCoexist() {
        if (SystemProperties.get("ro.connectivity.chiptype").equals("hi110x")) {
            return false;
        }
        return true;
    }

    public void initP2pEnableFunction() {
        Log.d(TAG, "initP2pEnableFunction is called!");
        this.mWifiP2pManagerCustExt.setWifiP2pEnabled(1);
    }

    public boolean isWifiP2pEnabled() {
        Log.d(TAG, "isWifiP2pEnabled is called!");
        if (isSupportStaP2pCoexist()) {
            return false;
        }
        return this.mWifiP2pManagerCustExt.isWifiP2pEnabled();
    }

    public void setIsConnectguiProp(boolean isConnectgui) {
        if (isConnectgui) {
            SystemProperties.set(CONECTGUI_PROP_NAME, "true");
        } else {
            SystemProperties.set(CONECTGUI_PROP_NAME, "false");
        }
    }

    public boolean isNotEditWifi(AccessPoint mSelectedAccessPoint) {
        String wifi_notdel_notedit = Systemex.getString(this.mWifiSettingsHwBase.getActivity().getContentResolver(), "wifi_notdel_notedit");
        if (wifi_notdel_notedit == null) {
            return false;
        }
        for (String ssid : wifi_notdel_notedit.split(";")) {
            if (ssid.equals(mSelectedAccessPoint.getSsidStr()) && mSelectedAccessPoint.getSecurity() == 3) {
                return true;
            }
        }
        return false;
    }

    public void custContextMenu(AccessPoint accessPoint, ContextMenu menu) {
        if (AUTO_CONNECT_SWITCH_VALUE == 1 && accessPoint != null) {
            String selectedAccessSSID = accessPoint.getSsidStr();
            if (ATTWIFI.equals(selectedAccessSSID) || WIFI_HOTSPOT_REDEFINDED.equals(selectedAccessSSID)) {
                menu.removeItem(8);
                menu.removeItem(9);
            }
        }
        if (accessPoint != null && isNotEditWifi(accessPoint)) {
            menu.removeItem(8);
            menu.removeItem(9);
        }
    }

    public boolean changeP2pModeCoexist() {
        if (this.mWifiManager == null) {
            this.mWifiManager = (WifiManager) this.mWifiSettingsHwBase.getActivity().getSystemService("wifi");
        }
        if (!this.mWifiManager.isWifiEnabled()) {
            if (!this.mWifiManager.isWifiApEnabled()) {
                jumpToP2p();
            }
            initP2pEnableFunction();
            return true;
        } else if (isWifiP2pEnabled()) {
            return true;
        } else {
            initP2pEnableFunction();
            return true;
        }
    }

    public void jumpToP2p() {
        Log.d(TAG, "jumpToP2p is called!");
        if (this.mWifiSettingsHwBase.getActivity() instanceof SettingsActivity) {
            ((SettingsActivity) this.mWifiSettingsHwBase.getActivity()).startPreferencePanel(WifiP2pSettings.class.getCanonicalName(), null, 2131625037, null, this.mWifiSettingsHwBase, 0);
            return;
        }
        this.mWifiSettingsHwBase.startFragment(this.mWifiSettingsHwBase, WifiP2pSettings.class.getCanonicalName(), 2131625037, -1, null);
    }

    public boolean isP2pEnableChangedAction(String action) {
        return WIFI_P2P_ENABLE_CHANGED_ACTION.equals(action);
    }

    public void p2pEnableChangedActionHandle() {
        if (!isSupportStaP2pCoexist()) {
            jumpToP2p();
        }
    }

    public boolean showAllAccessPoints() {
        return SystemProperties.getBoolean("ro.config.show_all_ap", false);
    }

    public boolean isShowSkipWifiSettingDialog() {
        String mccmncList = Systemex.getString(this.mWifiSettingsHwBase.getActivity().getContentResolver(), "show_skip_wifi_dialog_list");
        if (!TextUtils.isEmpty(mccmncList) && mccmncList.equals("all")) {
            return true;
        }
        String currentMccmnc = SystemProperties.get("gsm.sim.operator.numeric", "0");
        Log.i(TAG, "isShowSkipWifiSettingDialog--->mccmncList = " + mccmncList + ", currentMccmnc = " + currentMccmnc);
        if (TextUtils.isEmpty(mccmncList) || currentMccmnc.equals("0")) {
            return false;
        }
        String[] mccmncs = currentMccmnc.split(",");
        if (mccmncs.length == 2 && !mccmncs[0].equals("") && (mccmncList.contains(mccmncs[0]) || mccmncList.contains(mccmncs[1]))) {
            return true;
        }
        if (mccmncs.length == 2 && mccmncs[0].equals("") && mccmncList.contains(mccmncs[1])) {
            return true;
        }
        return mccmncs.length == 1 && mccmncList.contains(mccmncs[0]);
    }

    public boolean isShowDataCostTip() {
        String mccmncList = Systemex.getString(this.mWifiSettingsHwBase.getActivity().getContentResolver(), "show_data_cost_tip_list");
        if (!TextUtils.isEmpty(mccmncList) && mccmncList.equals("all")) {
            return true;
        }
        String currentMccmnc = SystemProperties.get("gsm.sim.operator.numeric", "0");
        Log.i(TAG, "isShowDataCostTip--->mccmncList = " + mccmncList + ", currentMccmnc = " + currentMccmnc);
        if (TextUtils.isEmpty(mccmncList) || currentMccmnc.equals("0")) {
            return false;
        }
        String[] mccmncs = currentMccmnc.split(",");
        if (mccmncs.length == 2 && !mccmncs[0].equals("") && (mccmncList.contains(mccmncs[0]) || mccmncList.contains(mccmncs[1]))) {
            return true;
        }
        if (mccmncs.length == 2 && mccmncs[0].equals("") && mccmncList.contains(mccmncs[1])) {
            return true;
        }
        return mccmncs.length == 1 && mccmncList.contains(mccmncs[0]);
    }

    public boolean getFlagForWifiDisabled(SwitchPreference wifiSwitchPreference) {
        if (!UtilsCustEx.IS_SPRINT || wifiSwitchPreference == null || !isWifiRestricted()) {
            return false;
        }
        wifiSwitchPreference.setEnabled(false);
        wifiSwitchPreference.setSummary(2131629256);
        return true;
    }

    private boolean isWifiRestricted() {
        if (new File("/data/OtaSave/Extensions/wifi.disable").exists()) {
            return true;
        }
        return false;
    }

    public void remoeveModifyMenu(AccessPoint mAccessPoint, ContextMenu menu, int id) {
        String notModifyWifi = SystemProperties.get("ro.config.hw_not_modify_wifi");
        if (!(notModifyWifi == null || mAccessPoint == null || mAccessPoint.getSecurity() != 3)) {
            for (String ssid : notModifyWifi.split(",")) {
                if (ssid.equals(mAccessPoint.getSsidStr())) {
                    menu.removeItem(id);
                    return;
                }
            }
        }
    }

    public boolean isShowOnlyDialog(AccessPoint mAccessPoint, Context context) {
        this.isOnlyShowDialog = Systemex.getString(context.getContentResolver(), "hw_eap_show_dialog");
        if (!TextUtils.isEmpty(this.isOnlyShowDialog)) {
            this.isOnlyShowDialogEntries = this.isOnlyShowDialog.split(";");
        }
        if (this.isOnlyShowDialogEntries == null || mAccessPoint == null) {
            return false;
        }
        boolean isSecurityEap = mAccessPoint.getSecurity() == 3;
        if (!Arrays.asList(this.isOnlyShowDialogEntries).contains(mAccessPoint.getSsidStr())) {
            isSecurityEap = false;
        }
        return isSecurityEap;
    }
}
