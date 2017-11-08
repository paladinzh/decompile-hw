package com.android.settings.wifi;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v14.preference.SwitchPreference;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.android.settingslib.wifi.AccessPoint;

public class HwCustWifiSettingsHwBase {
    public WifiSettingsHwBase mWifiSettingsHwBase;

    public HwCustWifiSettingsHwBase(WifiSettingsHwBase wifiSettingsHwBase) {
        this.mWifiSettingsHwBase = wifiSettingsHwBase;
    }

    public void handleEvent(Context context, Intent intent) {
    }

    public void addAction(IntentFilter mFilter) {
    }

    public boolean dontShowWifiSkipDialog() {
        return false;
    }

    public void addActionForPromptDsDisabled(IntentFilter mFilter) {
    }

    public void handleNwSettingsChangedEvent(Context context, Intent intent) {
    }

    public void initService() {
    }

    public boolean getFlagForDsDisabled() {
        return false;
    }

    public void setFlagForDsDisabled(Intent intent) {
    }

    public View getDsDisabledView(LayoutInflater inflater, ViewGroup container) {
        return null;
    }

    public void setMovementMethod(TextView textView) {
    }

    public boolean isSkipWifiSettingWithNoPrompt() {
        return false;
    }

    public void disableWifiIfNeed() {
    }

    public void custContextMenu(AccessPoint accessPoint, ContextMenu menu) {
    }

    public boolean isSupportStaP2pCoexist() {
        return true;
    }

    public void initP2pEnableFunction() {
    }

    public boolean isWifiP2pEnabled() {
        return false;
    }

    public void setIsConnectguiProp(boolean isConnectgui) {
    }

    public boolean isNotEditWifi(AccessPoint mSelectedAccessPoint) {
        return false;
    }

    public boolean changeP2pModeCoexist() {
        return false;
    }

    public boolean isP2pEnableChangedAction(String action) {
        return false;
    }

    public void p2pEnableChangedActionHandle() {
    }

    public boolean isShowSkipWifiSettingDialog() {
        return false;
    }

    public boolean isShowDataCostTip() {
        return false;
    }

    public boolean getFlagForWifiDisabled(SwitchPreference mWifiSwitchPreference) {
        return false;
    }

    public boolean showAllAccessPoints() {
        return true;
    }

    public void remoeveModifyMenu(AccessPoint mAccessPoint, ContextMenu menu, int id) {
    }
}
