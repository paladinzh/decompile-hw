package com.android.settings.wifi;

import android.content.Context;
import android.net.LinkAddress;
import android.net.StaticIpConfiguration;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.TextView;
import com.android.settings.MLog;
import com.android.settings.TangibleButton;
import com.android.settings.wifi.cmcc.WifiExt;
import java.net.InetAddress;

public class WifiConfigControllerHwBase {
    protected int mAccessPointSecurity;
    protected WifiConfigUiBase mConfigUi;
    protected Context mContext;
    protected int mEapMethodFromSavedInstance;
    protected boolean mIsDialog = false;
    protected TextView mNetworkNetmaskView;
    protected TextView mPasswordView;
    protected int mPhase2FromSavedInstance;
    protected WifiExt mWifiExt = null;

    protected int validateNetMask(StaticIpConfiguration staticIpConfiguration, InetAddress inetAddr) {
        String netMask = this.mNetworkNetmaskView.getText().toString();
        try {
            int networkPrefixLength = WifiExt.getNetworkPrefixLengthFromNetmask(netMask);
            if (networkPrefixLength < 0 || networkPrefixLength > 32) {
                return -1;
            }
            staticIpConfiguration.ipAddress = new LinkAddress(inetAddr, networkPrefixLength);
            MLog.d("WifiConfigControllerHwBase", "set netMask=" + netMask + " prefix length=" + networkPrefixLength);
            return networkPrefixLength;
        } catch (IllegalArgumentException e) {
            MLog.e("WifiConfigControllerHwBase", "IllegalArgumentException, error msg: " + e.getMessage());
            return -1;
        }
    }

    protected void showPasswordTips() {
        if (this.mAccessPointSecurity == 2 && this.mPasswordView != null && this.mPasswordView.length() < 8) {
            this.mPasswordView.setError(this.mContext.getResources().getQuantityString(2131689532, 8, new Object[]{Integer.valueOf(8)}));
        }
    }

    protected void hidePasswordTips() {
        if (this.mAccessPointSecurity != 2 && this.mPasswordView != null) {
            this.mPasswordView.setError(null);
        }
    }

    protected void setOnTouchListenerForDisabledButton() {
        Button submitBtn = this.mConfigUi.getSubmitButton();
        if (submitBtn instanceof TangibleButton) {
            ((View) submitBtn.getParent()).setOnTouchListener(new OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() != 0) {
                        return true;
                    }
                    Button submitBtn = WifiConfigControllerHwBase.this.mConfigUi.getSubmitButton();
                    if (submitBtn != null && !submitBtn.isEnabled() && submitBtn.getVisibility() == 0 && event.getX() >= ((float) submitBtn.getLeft()) && event.getX() <= ((float) submitBtn.getRight()) && event.getY() >= ((float) submitBtn.getTop()) && event.getY() <= ((float) submitBtn.getBottom())) {
                        WifiConfigControllerHwBase.this.showPasswordTips();
                    }
                    return true;
                }
            });
        }
    }
}
