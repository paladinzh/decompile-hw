package com.android.settings.deviceinfo;

import android.content.Context;
import android.content.Intent;
import android.os.SystemProperties;
import com.android.settings.ItemUseStat;
import com.android.settings.Utils;

public class DefaultStorageLocation {
    private static boolean IS_REBOOTING = false;

    public static boolean isSdcard() {
        return SystemProperties.get("persist.sys.primarysd", "0").equals("1");
    }

    public static boolean isInternal() {
        return SystemProperties.get("persist.sys.primarysd", "0").equals("0");
    }

    public static boolean isRebooting() {
        return IS_REBOOTING;
    }

    public static void switchVolume(Context context) {
        if (isSdcard()) {
            switchVolume(context, "0");
        } else {
            switchVolume(context, "1");
        }
    }

    private static void switchVolume(Context context, String value) {
        SystemProperties.set("persist.sys.primarysd", value);
        Intent reboot = new Intent("android.intent.action.REBOOT");
        reboot.putExtra("android.intent.extra.KEY_CONFIRM", false);
        reboot.setFlags(268435456);
        if (!Utils.isMonkeyRunning()) {
            context.startActivity(reboot);
            IS_REBOOTING = true;
        }
        ItemUseStat.getInstance().handleClick(context, 2, "default_storage_location_changed", value);
    }

    public static void switchToInternal(Context context) {
        switchVolume(context, "0");
    }
}
