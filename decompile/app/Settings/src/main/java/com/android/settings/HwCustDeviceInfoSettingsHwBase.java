package com.android.settings;

import android.content.res.Resources;
import android.support.v7.preference.PreferenceGroup;

public class HwCustDeviceInfoSettingsHwBase {
    public DeviceInfoSettingsHwBase mDeviceInfoSettingsHwBase;

    public HwCustDeviceInfoSettingsHwBase(DeviceInfoSettingsHwBase deviceInfoSettingsHwBase) {
        this.mDeviceInfoSettingsHwBase = deviceInfoSettingsHwBase;
    }

    public String updateCustCupInfo(Resources res, String cpuInfo, String maxCpuFreq) {
        return res.getString(2131627757, new Object[]{cpuInfo, maxCpuFreq});
    }

    public void removeEmuiLogo(DeviceInfoSettingsHwBase context, PreferenceGroup preferenceGroup, String preference) {
    }

    public boolean isHideEmuiInfo() {
        return false;
    }

    public String checkSpareDigit(String meidStr) {
        return meidStr;
    }
}
