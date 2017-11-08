package com.android.settings.vpn2;

import android.content.Context;
import android.view.View;
import android.widget.TextView;
import com.android.internal.net.VpnProfile;

public class HwCustVpnDialog {
    public View getLayout(Context context) {
        return null;
    }

    public boolean isSupportL2TP() {
        return false;
    }

    public void setL2TPVisibility(View view, int res) {
    }

    public void getL2TPText(VpnProfile profile, TextView textView) {
    }
}
