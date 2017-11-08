package com.android.settings;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemProperties;
import android.provider.Settings.System;
import android.util.Log;

public class GloveModeBootReceiver extends BroadcastReceiver {
    private Context mContext;

    public void onReceive(Context context, Intent intent) {
        this.mContext = context;
        if (intent == null) {
            Log.e("GloveModeBootReceiver", "Intent is null, so exiting");
            return;
        }
        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction()) && isSupportGloveButton()) {
            if (System.getInt(this.mContext.getContentResolver(), "glovemode_button_settings", 0) == 1) {
                System.putInt(this.mContext.getContentResolver(), "glove_file_node", 1);
                System.putInt(this.mContext.getContentResolver(), "glovemode_button_settings", 0);
            }
            if (isGloveModeOn()) {
                setGloveMode(false);
                setGloveMode(true);
            }
        }
    }

    private boolean isGloveModeOn() {
        boolean z = true;
        if (this.mContext == null) {
            return false;
        }
        Log.d("GloveModeBootReceiver", "isGloveModeOn");
        if (1 != System.getInt(this.mContext.getContentResolver(), "glove_file_node", 0)) {
            z = false;
        }
        return z;
    }

    private boolean isSupportGloveButton() {
        Log.d("GloveModeBootReceiver", "isSupportGloveButton");
        if (SystemProperties.getInt("ro.config.hw_glovemode_enabled", 0) == 1) {
            return true;
        }
        return false;
    }

    private void setGloveMode(boolean isGloveMode) {
        Log.d("GloveModeBootReceiver", "setGloveMode");
        if (this.mContext != null) {
            System.putInt(this.mContext.getContentResolver(), "glove_file_node", isGloveMode ? 1 : 0);
        }
    }
}
