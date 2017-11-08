package com.android.settings.applications;

import android.content.Context;
import android.widget.Button;

public class HwCustInstalledAppDetails {
    public InstalledAppDetails mInstalledAppDetails;

    public HwCustInstalledAppDetails(InstalledAppDetails installedAppDetails) {
        this.mInstalledAppDetails = installedAppDetails;
    }

    public boolean isEnableSpecialDisableButton(Context context) {
        return true;
    }

    public boolean isUmsStorageMounted(Context context) {
        return false;
    }

    public boolean getUninstallBtnEnableState(boolean enabled) {
        return enabled;
    }

    public void custUpdateForceStopButton(Button forceStopButton) {
    }

    public boolean isForbidDisablableBtn(Context context) {
        return false;
    }
}
