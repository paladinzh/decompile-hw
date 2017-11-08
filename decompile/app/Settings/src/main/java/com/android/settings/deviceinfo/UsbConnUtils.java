package com.android.settings.deviceinfo;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.bluetooth.BluetoothAddressNative;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.SystemProperties;
import android.provider.Settings.Secure;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import com.android.settings.SettingsExtUtils;
import com.android.settings.Utils;

public class UsbConnUtils {
    public static Intent getSimLimitHelpIntent() {
        Intent intent = new Intent("com.android.huawei.SIM_USB_LIMIT");
        intent.setFlags(268435456);
        intent.setPackage("com.android.settings");
        return intent;
    }

    public static Intent getModeChooserIntent() {
        Intent intent = new Intent("android.deviceinfo.chooser.action.LAUNCH");
        intent.setPackage("com.android.settings");
        return intent;
    }

    public static Intent getUpdateChooserIntent() {
        Intent intent = new Intent("com.android.settings.usb.UPDATE_CHOOSERS");
        intent.setPackage("com.android.settings");
        return intent;
    }

    public static boolean untetherUsb(Context context, String[] usbRegex) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService("connectivity");
        String[] tethered = cm.getTetheredIfaces();
        Log.d("UsbConnUtils", "untetherUsb start!");
        String usbIface = findIface(tethered, usbRegex);
        if (usbIface == null) {
            Log.w("UsbConnUtils", "usbIface is null!");
            return true;
        } else if (cm.untether(usbIface) != 0) {
            Log.w("UsbConnUtils", "untether error!");
            return true;
        } else {
            Log.d("UsbConnUtils", "untetherUsb end!");
            return false;
        }
    }

    public static boolean isPowerSupplyEnabled() {
        return SystemProperties.getInt("ro.config.hw_usb_pwr_on", 1) == 1;
    }

    public static boolean isMtpDialogEnabled(Context context) {
        boolean z = true;
        if (Utils.isFactoryMode()) {
            Log.d("UsbConnUtils", "do not show usb prompt in factory mode.");
            return false;
        } else if (context == null) {
            return true;
        } else {
            if (SettingsExtUtils.isStartupGuideMode(context.getContentResolver())) {
                Log.d("UsbConnUtils", "do not show usb prompt in OOBE.");
                return false;
            } else if (needSkipSetupPhase()) {
                Log.d("UsbConnUtils", "do not show usb prompt when bluetooth address is empty for CW test!");
                return false;
            } else {
                if (Secure.getInt(context.getContentResolver(), "usb_conn_prompt", 1) == 0) {
                    z = false;
                }
                return z;
            }
        }
    }

    public static AlertDialog createMtpCautionDialog(Context context, OnClickListener allowClickListener, OnClickListener cancelClickListener) {
        View view = ((LayoutInflater) context.getSystemService("layout_inflater")).inflate(2130969227, null);
        Button cancelButton = (Button) view.findViewById(2131887307);
        ((Button) view.findViewById(2131887306)).setOnClickListener(allowClickListener);
        cancelButton.setOnClickListener(cancelClickListener);
        AlertDialog cautionDialog = new Builder(context).setView(view).setCancelable(false).setTitle(context.getString(2131628518)).create();
        cautionDialog.getWindow().setType(2003);
        return cautionDialog;
    }

    private static String findIface(String[] ifaces, String[] regexes) {
        for (String iface : ifaces) {
            for (String regex : regexes) {
                if (iface.matches(regex)) {
                    return iface;
                }
            }
        }
        return null;
    }

    public static boolean needSkipSetupPhase() {
        return BluetoothAddressNative.isLibReady() ? TextUtils.isEmpty(BluetoothAddressNative.getMacAddress()) : false;
    }

    public static boolean isSupportUsbLimit() {
        return 1 == SystemProperties.getInt("persist.sys.cmcc_usb_limit", 0);
    }
}
