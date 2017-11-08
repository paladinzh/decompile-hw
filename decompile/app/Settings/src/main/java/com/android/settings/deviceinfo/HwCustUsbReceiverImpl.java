package com.android.settings.deviceinfo;

import android.content.Context;
import android.os.SystemProperties;
import android.provider.Settings.Secure;
import android.util.Log;
import com.huawei.android.os.HwOemInfoCustEx;

public class HwCustUsbReceiverImpl extends HwCustUsbReceiver {
    private static final String NV_BT_ADDR_IS_NULL = "nv_bt_addr_is_null";
    private static final String TAG = "HwCustUsbReceiverImpl";

    public HwCustUsbReceiverImpl(UsbReceiver usbReceiver) {
        super(usbReceiver);
    }

    public boolean notStartUsbSettings(Context context) {
        if (!"factory".equals(SystemProperties.get("ro.runmode", "normal"))) {
            return false;
        }
        Log.i(TAG, "hide USB Settings interface in factory mode");
        return true;
    }

    private boolean bTAddressIsNull(Context context) {
        boolean isNull = true;
        if (!SystemProperties.getBoolean("ro.config.readBtAddress", false)) {
            return false;
        }
        if (1 != Secure.getInt(context.getContentResolver(), NV_BT_ADDR_IS_NULL, 1)) {
            isNull = false;
        }
        if (isNull) {
            isNull = HwOemInfoCustEx.bTAddressIsNull();
            if (!isNull) {
                Secure.putInt(context.getContentResolver(), NV_BT_ADDR_IS_NULL, 0);
            }
        }
        return isNull;
    }
}
