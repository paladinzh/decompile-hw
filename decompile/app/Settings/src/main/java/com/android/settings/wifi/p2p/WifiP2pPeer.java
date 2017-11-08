package com.android.settings.wifi.p2p;

import android.content.Context;
import android.net.wifi.p2p.WifiP2pDevice;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.text.TextUtils;
import com.android.settings.wifi.WifiExtUtils;

public class WifiP2pPeer extends Preference {
    public WifiP2pDevice device;
    private final int mRssi = 60;

    public WifiP2pPeer(Context context, WifiP2pDevice dev) {
        super(context);
        this.device = dev;
        setLayoutResource(2130969014);
        setWidgetLayoutResource(2130968998);
        if (TextUtils.isEmpty(this.device.deviceName)) {
            setTitle(this.device.deviceAddress);
        } else {
            setTitle(this.device.deviceName);
        }
        setIcon(WifiExtUtils.getDrawable(this.device, this.mRssi));
        setSummary(context.getResources().getStringArray(2131361854)[this.device.status]);
    }

    public void onBindViewHolder(PreferenceViewHolder view) {
        super.onBindViewHolder(view);
    }

    public int compareTo(Preference preference) {
        int i = 1;
        if (!(preference instanceof WifiP2pPeer)) {
            return 1;
        }
        WifiP2pPeer other = (WifiP2pPeer) preference;
        if (this.device.status != other.device.status) {
            if (this.device.status < other.device.status) {
                i = -1;
            }
            return i;
        } else if (this.device.deviceName != null) {
            return this.device.deviceName.compareToIgnoreCase(other.device.deviceName);
        } else {
            return this.device.deviceAddress.compareToIgnoreCase(other.device.deviceAddress);
        }
    }
}
