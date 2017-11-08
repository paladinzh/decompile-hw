package com.huawei.powergenie.debugtest;

import android.util.Log;
import com.huawei.powergenie.integration.adapter.NativeAdapter;
import java.io.PrintWriter;

public class DbgNativeAdapter extends DbgBaseAdapter {
    protected void startTest(PrintWriter pw) {
        boolean z;
        super.startTest(pw);
        Log.i("DbgNativeAdapter", "Native Adapter Test!");
        pw.println("\nNative Adapter Test!");
        printlnResult("getPlatformType", getPlatformName(NativeAdapter.getPlatformType()));
        String str = "getCpuCores";
        if (NativeAdapter.getCpuCores() != -1) {
            z = true;
        } else {
            z = false;
        }
        printlnResult(str, getResult(z));
        printlnResult("setCABCMode(MOVING)", getResult(NativeAdapter.setCABC(true)));
        printlnResult("setCABCMode(UI)", getResult(NativeAdapter.setCABC(false)));
        printlnResult("setChargeHotLimit(USB_CURRENT,0)", getResult(NativeAdapter.setChargeHotLimit(1, 0)));
        printlnResult("setChargeHotLimit(AC_CURRENT,0)", getResult(NativeAdapter.setChargeHotLimit(2, 0)));
        printlnResult("setChargeHotLimit(USB_CURRENT_AUX,0)", getResult(NativeAdapter.setChargeHotLimit(3, 0)));
        printlnResult("setChargeHotLimit(AC_CURRENT_AUX,0)", getResult(NativeAdapter.setChargeHotLimit(4, 0)));
        printlnResult("setFlashLimit(FLASH_LIGHT, true)", getResult(NativeAdapter.setFlashLimit(false, true)));
        if (NativeAdapter.getPlatformType() == 0) {
            printlnResult("setFlashLimit(FLASH_LIGHT_FRONT, true)", getResult(NativeAdapter.setFlashLimit(true, true)) + " //only support QCOM");
            printlnResult("writeGpuFreq(0)", getResult(NativeAdapter.writeGpuFreq(0)) + "//only support QCOM");
        }
    }

    private String getPlatformName(int platform) {
        switch (platform) {
            case NativeAdapter.PLATFORM_QCOM /*0*/:
                return "QCOM";
            case NativeAdapter.PLATFORM_MTK /*1*/:
                return "MTK";
            case NativeAdapter.PLATFORM_HI /*2*/:
                return "Hi";
            case NativeAdapter.PLATFORM_K3V3 /*3*/:
                return "K3V3&K3V5";
            default:
                return "unknow";
        }
    }
}
