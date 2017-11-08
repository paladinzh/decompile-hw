package com.android.settings.wifi.cmcc;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkUtils;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.provider.Settings.System;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceGroup;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import com.android.settings.CustomSwitchPreference;
import com.android.settingslib.wifi.AccessPoint;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.List;

public class WifiExt {
    private ContentObserver mAirplaneModeObserver;
    private CustomSwitchPreference mCmccConnectDetection;
    private ListPreference mConnectTypePref;
    private ContextThemeWrapper mContext;
    private OnPreferenceChangeListener mPreferenceChangeListener = new OnPreferenceChangeListener() {
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            String key = preference.getKey();
            Log.d("WifiExt", "key=" + key);
            if ("connect_type".equals(key)) {
                Log.d("WifiExt", "Wifi connect type is " + newValue);
                try {
                    System.putInt(WifiExt.this.mContext.getContentResolver(), "wifi_connect_type", Integer.parseInt((String) newValue));
                    if (WifiExt.this.mConnectTypePref != null) {
                        WifiExt.this.mConnectTypePref.setSummary((String) WifiExt.this.mContext.getResources().getTextArray(2131361938)[Integer.parseInt((String) newValue)]);
                    }
                } catch (NumberFormatException e) {
                    Log.d("WifiExt", "set Wifi connect type error");
                    return false;
                }
            }
            return true;
        }
    };
    private Preference mPrioritySettingPref;
    private SwitchPreference mSwitch;
    private CustomSwitchPreference mWifiTowifiPop;

    public WifiExt(Context context) {
        this.mContext = new ContextThemeWrapper(context, context.getResources().getIdentifier("androidhwext:style/Theme.Emui", null, null));
        if (Features.shouldDisableWifiOnAirplaneMode()) {
            this.mAirplaneModeObserver = new ContentObserver(new Handler()) {
                public void onChange(boolean selfChange) {
                    Log.d("WifiExt", "onChange(),switch new state:" + WifiExt.this.getSwitchState());
                    if (WifiExt.this.mSwitch != null) {
                        WifiExt.this.mSwitch.setEnabled(WifiExt.this.getSwitchState());
                    }
                }
            };
        }
    }

    public static boolean shouldSetDisconnectButton(Context context) {
        if (context == null) {
            return false;
        }
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService("connectivity");
        return cm != null && cm.getNetworkInfo(1).isConnected() && Features.shouldSetDisconnectButton();
    }

    public void registerAirplaneModeObserver(SwitchPreference iSwitch) {
        if (Features.shouldDisableWifiOnAirplaneMode()) {
            Log.d("WifiExt", "registerAirplaneModeObserver()");
            this.mSwitch = iSwitch;
            this.mContext.getContentResolver().registerContentObserver(System.getUriFor("airplane_mode_on"), true, this.mAirplaneModeObserver);
        }
    }

    public void unRegisterAirplaneObserver() {
        if (Features.shouldDisableWifiOnAirplaneMode()) {
            Log.d("WifiExt", "unRegisterObserver()");
            this.mContext.getContentResolver().unregisterContentObserver(this.mAirplaneModeObserver);
        }
    }

    public boolean getSwitchState() {
        boolean z = false;
        if (!Features.shouldDisableWifiOnAirplaneMode()) {
            return true;
        }
        boolean z2;
        boolean state = System.getInt(this.mContext.getContentResolver(), "airplane_mode_on", 0) != 0;
        String str = "WifiExt";
        StringBuilder append = new StringBuilder().append("getSwitchState():");
        if (state) {
            z2 = false;
        } else {
            z2 = true;
        }
        Log.d(str, append.append(z2).toString());
        if (!state) {
            z = true;
        }
        return z;
    }

    public void initSwitchState(SwitchPreference iSwitch) {
        if (Features.shouldDisableWifiOnAirplaneMode()) {
            this.mSwitch = iSwitch;
            this.mSwitch.setEnabled(getSwitchState());
        }
    }

    public void initNetworkPrefixAndMaskView(View networkPrefix, View netmask) {
        if (Features.shouldDisplayNetmask()) {
            netmask.setVisibility(0);
        } else {
            networkPrefix.setVisibility(0);
        }
    }

    public void refreshNetworkInfoView(PreferenceGroup group) {
        if (Features.shouldDisplayNetmask()) {
            DhcpInfo dhcpInfo = ((WifiManager) this.mContext.getSystemService("wifi")).getDhcpInfo();
            CharSequence charSequence = null;
            String netmask = null;
            if (dhcpInfo != null) {
                charSequence = ipTransfer(dhcpInfo.gateway);
                netmask = ipTransfer(dhcpInfo.netmask);
            }
            CharSequence unavailableStatus = this.mContext.getString(2131625250);
            Preference gatewayPref = group.findPreference("wifi_gateway");
            Preference netmaskPref = group.findPreference("wifi_network_mask");
            if (gatewayPref != null) {
                if (charSequence == null) {
                    charSequence = unavailableStatus;
                }
                gatewayPref.setSummary(charSequence);
            }
            if (netmaskPref != null) {
                if (netmask != null) {
                    Object unavailableStatus2 = netmask;
                }
                netmaskPref.setSummary(unavailableStatus);
            }
            return;
        }
        removePreference(group, "wifi_gateway");
        removePreference(group, "wifi_network_mask");
    }

    private String ipTransfer(int value) {
        if (value == 0) {
            return null;
        }
        if (value < 0) {
            value = (int) (((long) value) + 4294967296L);
        }
        return String.format("%d.%d.%d.%d", new Object[]{Integer.valueOf(value & 255), Integer.valueOf((value >> 8) & 255), Integer.valueOf((value >> 16) & 255), Integer.valueOf((value >> 24) & 255)});
    }

    public static int getNetworkPrefixLengthFromNetmask(String netmask) throws IllegalArgumentException {
        if (netmask == null || "".equals(netmask.trim())) {
            throw new IllegalArgumentException("Empty Netmask");
        }
        InetAddress address = NetworkUtils.numericToInetAddress(netmask);
        if (!(address instanceof Inet4Address)) {
            return -1;
        }
        String netmaskBinaryStr = Integer.toBinaryString(Integer.reverseBytes(NetworkUtils.inetAddressToInt((Inet4Address) address)));
        if (netmaskBinaryStr.length() >= 32) {
            return netmaskBinaryStr.indexOf("0");
        }
        throw new IllegalArgumentException("Invalid Netmask");
    }

    public boolean shouldDisplayNetmask() {
        return Features.shouldDisplayNetmask();
    }

    public void initConnectView(Activity activity, PreferenceGroup screen) {
        boolean z = true;
        if (Features.shouldDisplayConnectionSetting()) {
            this.mConnectTypePref = new ListPreference(activity);
            this.mConnectTypePref.setWidgetLayoutResource(2130968998);
            this.mConnectTypePref.setTitle(activity.getString(2131627525));
            this.mConnectTypePref.setDialogTitle(activity.getString(2131627525));
            this.mConnectTypePref.setEntries(activity.getResources().getTextArray(2131361938));
            this.mConnectTypePref.setEntryValues(activity.getResources().getTextArray(2131361939));
            this.mConnectTypePref.setKey("connect_type");
            this.mConnectTypePref.setOnPreferenceChangeListener(this.mPreferenceChangeListener);
            screen.addPreference(this.mConnectTypePref);
        }
        if (Features.shouldDisplayPrioritySetting()) {
            this.mPrioritySettingPref = new Preference(activity);
            this.mPrioritySettingPref.setWidgetLayoutResource(2130968998);
            this.mPrioritySettingPref.setTitle(2131627519);
            this.mPrioritySettingPref.setSummary(2131627520);
            this.mPrioritySettingPref.setKey("priority_settings");
            this.mPrioritySettingPref.setFragment("com.android.settings.wifi.cmcc.WifiPrioritySettings");
            screen.addPreference(this.mPrioritySettingPref);
        }
        if (Features.shouldDisplayCmccWarning()) {
            this.mCmccConnectDetection = new CustomSwitchPreference(this.mContext);
            this.mCmccConnectDetection.setKey("wifi_cmcc_connect_remind");
            this.mCmccConnectDetection.setTitle(2131627794);
            this.mCmccConnectDetection.setSummary(2131627795);
            this.mCmccConnectDetection.setPersistent(false);
            this.mCmccConnectDetection.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if ("wifi_cmcc_connect_remind".equals(preference.getKey())) {
                        int i;
                        Boolean valueObj = (Boolean) newValue;
                        WifiExt.this.mCmccConnectDetection.setChecked(valueObj.booleanValue());
                        ContentResolver contentResolver = WifiExt.this.mContext.getContentResolver();
                        String str = "wifi_cmcc_connected_remind";
                        if (valueObj.booleanValue()) {
                            i = 1;
                        } else {
                            i = 0;
                        }
                        System.putInt(contentResolver, str, i);
                    }
                    return false;
                }
            });
            this.mCmccConnectDetection.setChecked(1 == System.getInt(this.mContext.getContentResolver(), "wifi_cmcc_connected_remind", 1));
            screen.addPreference(this.mCmccConnectDetection);
        }
        if (Features.shouldDisplayWifiToWifiPop()) {
            this.mWifiTowifiPop = new CustomSwitchPreference(this.mContext);
            this.mWifiTowifiPop.setKey("wifi_to_wifi_pop");
            this.mWifiTowifiPop.setTitle(2131627796);
            this.mWifiTowifiPop.setSummary(2131627797);
            this.mWifiTowifiPop.setPersistent(false);
            this.mWifiTowifiPop.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if ("wifi_to_wifi_pop".equals(preference.getKey())) {
                        int i;
                        Boolean valueObj = (Boolean) newValue;
                        WifiExt.this.mWifiTowifiPop.setChecked(valueObj.booleanValue());
                        ContentResolver contentResolver = WifiExt.this.mContext.getContentResolver();
                        String str = "not_show_wifi_to_wifi_pop";
                        if (valueObj.booleanValue()) {
                            i = 1;
                        } else {
                            i = 0;
                        }
                        System.putInt(contentResolver, str, i);
                    }
                    return false;
                }
            });
            CustomSwitchPreference customSwitchPreference = this.mWifiTowifiPop;
            if (1 != System.getInt(this.mContext.getContentResolver(), "not_show_wifi_to_wifi_pop", 0)) {
                z = false;
            }
            customSwitchPreference.setChecked(z);
            screen.addPreference(this.mWifiTowifiPop);
        }
    }

    public void initPreference(ContentResolver contentResolver) {
        if (Features.shouldDisplayConnectionSetting() && this.mConnectTypePref != null) {
            int value = System.getInt(contentResolver, "wifi_connect_type", 0);
            this.mConnectTypePref.setValue(String.valueOf(value));
            this.mConnectTypePref.setSummary((String) this.mContext.getResources().getTextArray(2131361938)[value]);
        }
    }

    public void hideWifiConfigInfo(List<Object> list) {
        if (Features.isCmccCommonFeatureEnabled() && list != null) {
            int listSize = list.size();
            if (listSize >= 4) {
                String ssid = (String) list.get(0);
                int security = ((Integer) list.get(1)).intValue();
                int networkId = ((Integer) list.get(2)).intValue();
                int mode = ((Integer) list.get(3)).intValue();
                if (ssid == null || !"CMCC".equals(ssid) || 3 != security) {
                    return;
                }
                if (networkId == -1 || mode == 2) {
                    for (int i = 4; i < listSize; i++) {
                        ((View) list.get(i)).setVisibility(8);
                    }
                }
            }
        }
    }

    public static boolean showCmccWarring(Context context, String ssid) {
        if (!Features.shouldDisplayCmccWarning()) {
            return false;
        }
        if ((!"\"CMCC\"".equals(ssid) && !"\"CMCC-AUTO\"".equals(ssid) && !"\"CMCC-WEB\"".equals(ssid)) || System.getInt(context.getContentResolver(), "wifi_cmcc_connected_remind", 1) == 0) {
            return false;
        }
        Intent intent = new Intent(context, CmccWarningDialog.class);
        intent.setFlags(268435456);
        context.startActivity(intent);
        return true;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void applyCmccEapMethod(Spinner spinner, AccessPoint accessPoint) {
        if (Features.isCmccCommonFeatureEnabled() && spinner != null && accessPoint != null && "CMCC".equals(accessPoint.getSsidStr()) && 3 == accessPoint.getSecurity()) {
            ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(this.mContext, 2131361852, 17367048);
            spinnerAdapter.setDropDownViewResource(17367049);
            int formerSelectedItemPosition = spinner.getSelectedItemPosition();
            spinner.setAdapter(spinnerAdapter);
            if (formerSelectedItemPosition < spinnerAdapter.getCount()) {
                spinner.setSelection(formerSelectedItemPosition);
            }
        }
    }

    public int getEapMethodbySpinnerPos(int spinnerPosition, String ssid, int security) {
        if (Features.isCmccCommonFeatureEnabled()) {
            if (ssid == null || !"CMCC".equals(ssid) || 3 != security) {
                return spinnerPosition;
            }
            if (2 == spinnerPosition) {
                spinnerPosition = 5;
            } else if (1 == spinnerPosition) {
                spinnerPosition = 4;
            } else {
                spinnerPosition = 0;
            }
        }
        return spinnerPosition;
    }

    public int getCustomizeEapMethod(int eapMethod, String ssid, int security) {
        if (!Features.isCmccCommonFeatureEnabled() || ssid == null || !"CMCC".equals(ssid) || 3 != security) {
            return eapMethod;
        }
        if (4 == eapMethod) {
            return 1;
        }
        return 0;
    }

    protected boolean removePreference(PreferenceGroup group, String keyToRemove) {
        if (group == null) {
            return false;
        }
        Preference pref = group.findPreference(keyToRemove);
        if (pref != null) {
            return group.removePreference(pref);
        }
        return false;
    }
}
