package com.android.settings.deviceinfo;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.SystemProperties;
import android.provider.Settings.Secure;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;
import com.android.settings.UtilsCustEx;
import java.io.File;

public class HwCustUsbConnectedServiceImpl extends HwCustUsbConnectedService {
    private static final String ACTION_USBRESTRICTED = "com.huawei.android.intent.action.USB_RESTRICTED";
    private static final String DMPROPERTY_DIRECTORY = "/data/OtaSave/Extensions/";
    private static final String DMPROPERTY_USB_DRIVE = "usb_drive.disable";
    private static final String DMPROPERTY_USB_PORT = "usb_port.disable";
    private static final String TALKBACK_PACKAGE_NAME = "com.google.android.marvin.talkback";
    private String TAG = "HwCustUsbConnectedServiceImpl";
    private BroadcastReceiver mCustReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (HwCustUsbConnectedServiceImpl.ACTION_USBRESTRICTED.equals(intent.getAction())) {
                context.stopService(new Intent(context, UsbConnectedService.class));
            }
        }
    };
    private ContentObserver mSimActiviteUSB = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange) {
            boolean simState = Secure.getInt(HwCustUsbConnectedServiceImpl.this.mUsbConnectedService.getContentResolver(), "cmcc_usb_limit", 1) == 0;
            Log.e(HwCustUsbConnectedServiceImpl.this.TAG, "mSimActiviteUSB: " + simState);
            if (simState) {
                HwCustUsbConnectedServiceImpl.this.mUsbConnectedService.initRefreshNotification();
                HwCustUsbConnectedServiceImpl.this.mUsbConnectedService.handleMtpMode();
                HwCustUsbConnectedServiceImpl.this.mUsbConnectedService.refreshNotification();
            }
        }
    };

    public HwCustUsbConnectedServiceImpl(UsbConnectedService usbConnectedService) {
        super(usbConnectedService);
    }

    public boolean isSupportUsbLimit() {
        return "1".equals(SystemProperties.get("persist.sys.cmcc_usb_limit", "0"));
    }

    public void startSimUsbLimitActivity(Context context) {
        Intent helpIntent = new Intent("com.android.huawei.SIM_USB_LIMIT");
        helpIntent.setFlags(268435456);
        context.startActivity(helpIntent);
    }

    public void initControlAndStartSimUsbLimitActivity(RemoteViews mRemoteView, Context mContext) {
        mRemoteView.setViewVisibility(2131887349, 0);
        mRemoteView.setViewVisibility(2131887350, 8);
        Intent intent = new Intent("com.android.huawei.SIM_USB_LIMIT");
        intent.setFlags(268435456);
        mRemoteView.setOnClickPendingIntent(2131887349, PendingIntent.getActivity(mContext, 0, intent, 134217728));
        mRemoteView.removeAllViews(2131887351);
        mRemoteView.setTextViewText(2131887347, mContext.getText(2131627800));
        mRemoteView.setTextViewText(2131887348, mContext.getText(2131627464));
    }

    public CharSequence getChargeOnlyTitle(Context mContext) {
        return mContext.getText(2131628026);
    }

    public void registerSimActiviteUSBObserver(ContentResolver contentResolver) {
        contentResolver.registerContentObserver(Secure.getUriFor("cmcc_usb_limit"), true, this.mSimActiviteUSB);
    }

    public void unregisterSimActiviteUSBObserver(ContentResolver contentResolver) {
        contentResolver.unregisterContentObserver(this.mSimActiviteUSB);
        this.mSimActiviteUSB = null;
    }

    public void custRegisterUsbReceiver(Context context) {
        if (UtilsCustEx.IS_SPRINT) {
            IntentFilter custFilter = new IntentFilter();
            custFilter.addAction(ACTION_USBRESTRICTED);
            if (this.mCustReceiver != null) {
                context.registerReceiver(this.mCustReceiver, custFilter);
            }
        }
    }

    public void custUnRegisterUsbReceiver(Context context) {
        if (UtilsCustEx.IS_SPRINT && this.mCustReceiver != null && context != null) {
            context.unregisterReceiver(this.mCustReceiver);
            this.mCustReceiver = null;
        }
    }

    public boolean custHandleUsbRestriction(Context context) {
        if (!UtilsCustEx.IS_SPRINT || !isUSBRestricted()) {
            return false;
        }
        context.stopService(new Intent(context, UsbConnectedService.class));
        return true;
    }

    private boolean isUSBRestricted() {
        File usbDriveDisable = new File("/data/OtaSave/Extensions/usb_drive.disable");
        File usbPortDisable = new File("/data/OtaSave/Extensions/usb_port.disable");
        if (usbDriveDisable.exists() || usbPortDisable.exists()) {
            return true;
        }
        return false;
    }

    public void custUsbDisconnected(Context context) {
        if (SystemProperties.getBoolean("ro.config.hw_usb_discnt_talkbck", false) && isTalkBackServicesOn(context)) {
            Toast.makeText(context, 2131629262, 0).show();
        }
    }

    private boolean isTalkBackServicesOn(Context context) {
        if (context == null) {
            return false;
        }
        boolean accessibilityEnabled = Secure.getInt(context.getContentResolver(), "accessibility_enabled", 0) == 1;
        Log.d(this.TAG, "isTalkBackServicesOn: accessibilityEnabled is " + accessibilityEnabled);
        String enabledSerices = Secure.getString(context.getContentResolver(), "enabled_accessibility_services");
        Log.d(this.TAG, "isTalkBackServicesOn: enabledSerices is " + enabledSerices);
        boolean contains = enabledSerices != null ? enabledSerices.contains(TALKBACK_PACKAGE_NAME) : false;
        Log.d(this.TAG, "isTalkBackServicesOn: isContainsTalkBackService is " + contains);
        if (!accessibilityEnabled) {
            contains = false;
        }
        return contains;
    }

    public boolean isHideHisuiteSupport() {
        return SystemProperties.getBoolean("ro.config.hw_hideHisuite", false);
    }
}
