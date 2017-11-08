package com.android.settings.wifi;

import android.os.SystemProperties;

public class HwCustWifiAddFragmentImpl extends HwCustWifiAddFragment {
    private static final String CONECTGUI_PROP_NAME = "sys.settings_is_connectgui";

    public boolean isSupportStaP2pCoexist() {
        if (SystemProperties.get("ro.connectivity.chiptype").equals("hi110x")) {
            return false;
        }
        return true;
    }

    public void setIsConnectguiProp(boolean isConnectgui) {
        if (isConnectgui) {
            SystemProperties.set(CONECTGUI_PROP_NAME, "true");
        } else {
            SystemProperties.set(CONECTGUI_PROP_NAME, "false");
        }
    }
}
