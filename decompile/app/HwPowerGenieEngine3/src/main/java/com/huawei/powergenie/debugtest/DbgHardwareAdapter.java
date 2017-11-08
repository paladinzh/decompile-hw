package com.huawei.powergenie.debugtest;

import android.util.Log;
import com.huawei.powergenie.api.ICoreContext;
import com.huawei.powergenie.api.IThermal;
import com.huawei.powergenie.integration.adapter.HardwareAdapter;
import java.io.PrintWriter;

public class DbgHardwareAdapter extends DbgBaseAdapter {
    private ICoreContext mICoreContext;
    private final IThermal mIThermal = ((IThermal) this.mICoreContext.getService("thermal"));

    DbgHardwareAdapter(ICoreContext context) {
        this.mICoreContext = context;
    }

    protected void startTest(PrintWriter pw) {
        super.startTest(pw);
        Log.i("DbgHardwareAdapter", "Hardware Adapter Test!");
        pw.println("\nHardware Adapter Test!");
        String chargingPath = this.mIThermal.getThermalInterface("battery");
        String wlanPath = this.mIThermal.getThermalInterface("wlan");
        if (chargingPath != null) {
            printlnResult("setChargingLimit", getResult(HardwareAdapter.setChargingLimit(0, chargingPath)));
        }
        if (wlanPath != null) {
        }
        if (HardwareAdapter.supportCinemaMode()) {
            printlnResult("setCinemaMode(false)", getResult(HardwareAdapter.setCinemaMode(false)));
        }
        printlnResult("setCameraFps(0)", getResult(HardwareAdapter.setCameraFps(0)));
    }
}
