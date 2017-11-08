package com.android.settings.nfc;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.os.SystemProperties;

public class NfcRouteTableReceiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        if (intent != null && context != null) {
            Editor editor = context.getSharedPreferences("is_show_nfc_gsma", 0).edit();
            if ("nfc.intent.action.AID_ROUTING_TABLE_FULL".equals(intent.getAction()) && SystemProperties.getBoolean("ro.config.hw_nfc_gsma", false)) {
                editor.putBoolean("isShow", true);
                editor.apply();
                Intent nfcTableActivity = new Intent("android.intent.action.NFC_ROUTE_TABLE");
                nfcTableActivity.setFlags(805306368);
                context.startActivity(nfcTableActivity);
            }
        }
    }
}
