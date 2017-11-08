package com.android.settings.vpn2;

import android.view.View;
import android.widget.TextView;
import com.android.internal.net.VpnProfile;

public class HwCustVpnAddFragmentImpl extends HwCustVpnAddFragment {
    public boolean isSupportL2TP() {
        return true;
    }

    public void setL2TPVisibility(View view, int res) {
        if (view != null && view.findViewById(res) != null) {
            view.findViewById(res).setVisibility(0);
        }
    }

    public void getL2TPText(VpnProfile profile, TextView textView) {
        if (profile != null && textView != null && textView.getText() != null) {
            profile.l2tpSecret = textView.getText().toString();
        }
    }
}
