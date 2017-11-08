package com.huawei.gallery.phonestatus;

import android.content.Context;
import android.os.PowerManager;
import com.huawei.gallery.util.MyPrinter;

public class PhoneState {
    private static final MyPrinter LOG = new MyPrinter("PhoneState");
    private static int sBatteryLevel = -1;
    private static int sChargeState = -1;

    public static void setChargeState(int state) {
        sChargeState = state;
    }

    public static boolean isChargeIn(Context context) {
        return isChargeIn(context, true);
    }

    public static boolean isChargeIn(Context context, boolean logPrint) {
        if (sChargeState == -1) {
            PowerStateReceiver.registerBatteryBroadcastReceiver(context);
        }
        if (logPrint) {
            boolean z;
            MyPrinter myPrinter = LOG;
            StringBuilder append = new StringBuilder().append("charge is ");
            if (sChargeState == 0) {
                z = true;
            } else {
                z = false;
            }
            myPrinter.d(append.append(z).toString());
        }
        if (sChargeState == 0) {
            return true;
        }
        return false;
    }

    public static void setBatteryLevel(int level) {
        sBatteryLevel = level;
    }

    public static boolean isBatteryLevelOK(Context context) {
        return isBatteryLevelOK(context, true);
    }

    public static boolean isBatteryLevelOK(Context context, boolean logPrint) {
        if (sBatteryLevel == -1) {
            PowerStateReceiver.registerBatteryBroadcastReceiver(context);
        }
        if (logPrint) {
            LOG.d("battery level is " + sBatteryLevel);
        }
        return sBatteryLevel >= 10;
    }

    public static boolean isScreenOff(Context context) {
        return isScreenOff(context, true);
    }

    public static boolean isScreenOff(Context context, boolean logPrint) {
        boolean isScreenOn = ((PowerManager) context.getSystemService("power")).isInteractive();
        if (logPrint) {
            LOG.d("screen on " + isScreenOn);
        }
        if (isScreenOn) {
            ScreenStateReceiver.registerScreenOffBroadcast(context);
        }
        return !isScreenOn;
    }
}
