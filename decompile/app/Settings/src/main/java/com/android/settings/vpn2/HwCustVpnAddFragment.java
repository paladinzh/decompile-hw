package com.android.settings.vpn2;

import android.view.View;
import android.widget.TextView;
import com.android.internal.net.VpnProfile;

public class HwCustVpnAddFragment {
    public boolean isSupportL2TP() {
        return false;
    }

    public void setL2TPVisibility(View view, int res) {
    }

    public void getL2TPText(VpnProfile profile, TextView textView) {
    }
}
