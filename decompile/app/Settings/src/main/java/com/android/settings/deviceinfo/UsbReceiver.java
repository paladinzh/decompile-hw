package com.android.settings.deviceinfo;

import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.hardware.usb.UsbManager;
import android.hardware.usb.UsbPort;
import android.hardware.usb.UsbPortStatus;
import android.os.SystemProperties;
import android.os.storage.StorageManager;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.util.Log;
import com.android.settings.MLog;
import com.android.settings.Utils;
import com.huawei.cust.HwCustUtils;

public class UsbReceiver extends BroadcastReceiver {
    private static final String sInecm = SystemProperties.get("ril.cdma.inecmmode", "false");
    Context mContext;
    private HwCustUsbReceiver mCustUsbReceiver = ((HwCustUsbReceiver) HwCustUtils.createObj(HwCustUsbReceiver.class, new Object[]{this}));
    private Editor mEeditor;
    Intent mServiceIntent;
    private SharedPreferences mSharedPre;
    private StorageManager mStorageManager = null;
    private boolean mUsbDataUnlocked = false;
    private UsbManager mUsbManager;

    public void onReceive(Context context, Intent intent) {
        this.mContext = context;
        if (UsbConnUtils.isPowerSupplyEnabled()) {
            Log.d("UsbReceiver", "initialize UsbSettingsService!");
            this.mServiceIntent = new Intent(this.mContext, UsbSettingsService.class);
        } else {
            Log.d("UsbReceiver", "initialize UsbConnectedService!");
            this.mServiceIntent = new Intent(this.mContext, UsbConnectedService.class);
        }
        this.mServiceIntent.putExtra("is_from_usb_receiver", true);
        String action = intent.getAction();
        this.mStorageManager = (StorageManager) context.getSystemService("storage");
        this.mUsbManager = (UsbManager) context.getSystemService("usb");
        this.mSharedPre = context.getSharedPreferences("com.android.settings_preferences", 0);
        if ("android.hardware.usb.action.USB_STATE".equals(action)) {
            onUsbStateReceive(context, intent);
        } else if ("android.intent.action.MEDIA_SHARED".equals(action)) {
            MLog.d("UsbReceiver", "MEDIA_SHARED");
            UsbMassStorageManager.getInstance(context).updateChangingState();
        } else if ("android.intent.action.MEDIA_UNSHARED".equals(action)) {
            MLog.d("UsbReceiver", "MEDIA_UNSHARED");
            UsbMassStorageManager.getInstance(context).updateChangingState();
        } else if ("android.hardware.usb.action.USB_PORT_CHANGED".equals(action)) {
            Log.d("UsbReceiver", "ACTION_USB_PORT_CHANGED arrived!");
            handleUsbPortChange(context, intent);
        } else if ("android.hardware.usb.action.USB_UPDATE".equals(action)) {
            Log.d("UsbReceiver", "ACTION_UPDATE_USB arrived!");
            handleUserSwitch(context, intent);
        }
    }

    private void onUsbStateReceive(Context context, Intent intent) {
        if (this.mUsbManager != null) {
            boolean usbConnected = intent.getBooleanExtra("connected", false);
            boolean chargingOnly = !intent.getBooleanExtra("unlocked", true);
            Log.d("UsbReceiver", "onUsbStateReceive chargingOnly = " + chargingOnly + ", usbConnected = " + usbConnected);
            Log.d("UsbReceiver", "ACTION_USB_STATE:usbConnected = " + usbConnected + ", usbState=" + SystemProperties.get("sys.usb.state") + ", chargingOnly = " + chargingOnly + ", isLegacyCharging = " + Utils.isLegacyChargingMode());
            ContentResolver cr = context.getContentResolver();
            if (this.mEeditor != null) {
                this.mEeditor.putBoolean("usb_connect_state", usbConnected);
                this.mEeditor.commit();
            }
            UsbMassStorageManager umsm = UsbMassStorageManager.getInstance(context);
            this.mServiceIntent.putExtra("is_charge_only", chargingOnly);
            this.mServiceIntent.putExtra("ncm_requested", intent.getBooleanExtra("ncm_requested", false));
            if (usbConnected) {
                this.mContext.startService(this.mServiceIntent);
            } else {
                umsm.quitThread();
            }
            if (Utils.isLegacyChargingMode() && !usbConnected && chargingOnly) {
                this.mContext.startService(this.mServiceIntent);
            }
            boolean changed = false;
            String PRE_USB_CONNECTED = "pre_usb_connected";
            boolean z = this.mSharedPre != null ? this.mSharedPre.getBoolean("pre_usb_connected", false) : false;
            String progress = SystemProperties.get("vold.encrypt_progress");
            if (progress == null) {
                progress = "";
            }
            boolean booleanExtra = SystemProperties.get("sys.usb.state").equals("none") ? intent.getBooleanExtra("only_charging", false) : false;
            if (usbConnected != z || booleanExtra) {
                changed = true;
            }
            KeyguardManager kgm = (KeyguardManager) context.getSystemService("keyguard");
            if (sInecm.equals("false") && ((usbConnected || booleanExtra) && changed && !z && "".equals(progress) && 1 == System.getInt(cr, "ask_on_plug", 1) && !kgm.inKeyguardRestrictedInputMode())) {
                if (this.mEeditor != null) {
                    this.mEeditor.putBoolean("pre_usb_connected", true);
                    this.mEeditor.commit();
                }
                if (this.mCustUsbReceiver != null && !this.mCustUsbReceiver.notStartUsbSettings(context)) {
                    this.mContext.startService(this.mServiceIntent);
                } else if (this.mCustUsbReceiver == null) {
                    this.mContext.startService(this.mServiceIntent);
                }
            } else if (sInecm.equals("false") && ((usbConnected || booleanExtra) && changed && !z && "".equals(progress) && 1 == System.getInt(cr, "ask_on_plug", 1) && (kgm.inKeyguardRestrictedInputMode() || kgm.isKeyguardLocked()))) {
                this.mContext.startService(this.mServiceIntent);
            } else if (!(usbConnected || this.mEeditor == null)) {
                this.mEeditor.putBoolean("pre_usb_connected", false);
                this.mEeditor.commit();
            }
            String usbType = SystemProperties.get("persist.sys.usb.config", "");
            if (usbType != null && usbType.startsWith("hisuite")) {
                Secure.putInt(cr, "suitestate", 0);
            }
        }
    }

    private void handleUsbPortChange(Context context, Intent intent) {
        UsbPort port = (UsbPort) intent.getParcelableExtra("port");
        UsbPortStatus status = (UsbPortStatus) intent.getParcelableExtra("portStatus");
        int dataRole = status == null ? -1 : status.getCurrentDataRole();
        boolean powerSupplyEnabled = UsbConnUtils.isPowerSupplyEnabled();
        Log.i("UsbReceiver", "Usb port changed, start service. data role = " + dataRole + ", port = " + port + ", powerSupplyEnabled = " + powerSupplyEnabled);
        if (dataRole == 1 && powerSupplyEnabled) {
            this.mContext.startService(this.mServiceIntent);
        }
    }

    private void handleUserSwitch(Context context, Intent intent) {
        UsbBackend usbBackend = new UsbBackend(context);
        boolean powerSupplyEnabled = UsbConnUtils.isPowerSupplyEnabled();
        Log.i("UsbReceiver", "User switched, isDeviceConnected = " + usbBackend.isDeviceConnected() + ", isHostConnected = " + usbBackend.isHostConnected() + ", dataRole = " + usbBackend.getCurrentDataRole() + ", powerSupplyEnabled = " + powerSupplyEnabled);
        if (usbBackend.isDeviceConnected() || (usbBackend.isHostConnected() && powerSupplyEnabled)) {
            context.startService(this.mServiceIntent);
        }
    }
}
