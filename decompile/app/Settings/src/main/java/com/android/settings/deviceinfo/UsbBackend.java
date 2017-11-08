package com.android.settings.deviceinfo;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbManager;
import android.hardware.usb.UsbPort;
import android.hardware.usb.UsbPortStatus;
import android.os.SystemProperties;
import android.os.UserManager;

public class UsbBackend {
    static final int[] DEFAULT_MODES = new int[]{0, 2, 4, 1, 6};
    private boolean mIsChargingOnly;
    private boolean mIsConnected;
    private boolean mIsUnlocked;
    private UsbPort mPort;
    private UsbPortStatus mPortStatus;
    private final boolean mRestricted;
    private UsbManager mUsbManager;
    private UserManager mUserManager;

    public UsbBackend(Context context) {
        Intent intent = context.registerReceiver(null, new IntentFilter("android.hardware.usb.action.USB_STATE"));
        if (intent != null) {
            this.mIsUnlocked = intent.getBooleanExtra("unlocked", false);
            this.mIsChargingOnly = intent.getBooleanExtra("only_charging", false);
            this.mIsConnected = intent.getBooleanExtra("connected", false);
        }
        this.mUserManager = UserManager.get(context);
        this.mUsbManager = (UsbManager) context.getSystemService(UsbManager.class);
        this.mRestricted = this.mUserManager.hasUserRestriction("no_usb_file_transfer");
        UsbPort[] ports = this.mUsbManager.getPorts();
        int N = ports.length;
        for (int i = 0; i < N; i++) {
            UsbPortStatus status = this.mUsbManager.getPortStatus(ports[i]);
            if (status.isConnected()) {
                this.mPort = ports[i];
                this.mPortStatus = status;
                return;
            }
        }
    }

    public int getCurrentMode() {
        if (this.mPort == null) {
            return getUsbDataMode() | 0;
        }
        return getUsbDataMode() | (this.mPortStatus.getCurrentPowerRole() == 1 ? 1 : 0);
    }

    public int getUsbDataMode() {
        if (!this.mIsUnlocked) {
            return 0;
        }
        if (this.mUsbManager.isFunctionEnabled("mtp") || this.mUsbManager.isFunctionEnabled("hisuite")) {
            return 2;
        }
        if (this.mUsbManager.isFunctionEnabled("ptp")) {
            return 4;
        }
        if (this.mUsbManager.isFunctionEnabled("midi")) {
            return 6;
        }
        return 0;
    }

    private void setUsbFunction(int mode) {
        switch (mode) {
            case 2:
                this.mUsbManager.setCurrentFunction("hisuite,mtp,mass_storage");
                this.mUsbManager.setUsbDataUnlocked(true);
                return;
            case 4:
                this.mUsbManager.setCurrentFunction("ptp");
                this.mUsbManager.setUsbDataUnlocked(true);
                return;
            case 6:
                this.mUsbManager.setCurrentFunction("midi");
                this.mUsbManager.setUsbDataUnlocked(true);
                return;
            default:
                this.mUsbManager.setCurrentFunction(null);
                this.mUsbManager.setUsbDataUnlocked(false);
                return;
        }
    }

    public void setMode(int mode) {
        if (this.mPort != null) {
            int powerRole = modeToPower(mode);
            int dataRole = ((mode & 6) == 0 && this.mPortStatus.isRoleCombinationSupported(powerRole, 1)) ? 1 : 2;
            this.mUsbManager.setPortRoles(this.mPort, powerRole, dataRole);
        }
        setUsbFunction(mode & 6);
    }

    private int modeToPower(int mode) {
        if ((mode & 1) == 1) {
            return 1;
        }
        return 2;
    }

    public boolean isModeSupported(int mode) {
        boolean z = true;
        if (this.mRestricted && (mode & 6) != 0 && (mode & 6) != 6) {
            return false;
        }
        if (this.mPort != null) {
            int power = modeToPower(mode);
            if ((mode & 6) != 0) {
                return this.mPortStatus.isRoleCombinationSupported(power, 2);
            }
            if (!this.mPortStatus.isRoleCombinationSupported(power, 2)) {
                z = this.mPortStatus.isRoleCombinationSupported(power, 1);
            }
            return z;
        }
        if ((mode & 1) == 1) {
            z = false;
        }
        return z;
    }

    public int getCurrentDataRole() {
        if (this.mPortStatus == null) {
            return -1;
        }
        return this.mPortStatus.getCurrentDataRole();
    }

    public boolean isDeviceConnected() {
        return isDeviceConnected(this.mIsConnected, this.mIsChargingOnly);
    }

    public boolean isHostConnected() {
        return isHostConnected(getCurrentDataRole());
    }

    public boolean isPortConnected() {
        return isPortConnected(getCurrentDataRole());
    }

    public static boolean isDeviceConnected(boolean connected, boolean chargingOnly) {
        return connected;
    }

    public static boolean isHostConnected(int role) {
        return role == 1;
    }

    public static boolean isPortConnected(int dataRole) {
        return dataRole != -1;
    }

    public static boolean isConnModeValid(int mode) {
        int[] modes = DEFAULT_MODES;
        int idx = 0;
        while (idx < modes.length) {
            if (modes[idx] == mode) {
                return mode != 6 || SystemProperties.getBoolean("ro.config.issupport.midi", true);
            } else {
                idx++;
            }
        }
        return false;
    }
}
