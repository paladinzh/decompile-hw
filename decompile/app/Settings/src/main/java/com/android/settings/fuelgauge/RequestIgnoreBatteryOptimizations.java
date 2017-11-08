package com.android.settings.fuelgauge;

import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.os.IDeviceIdleController;
import android.os.IDeviceIdleController.Stub;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import com.android.internal.app.AlertActivity;
import com.android.internal.app.AlertController.AlertParams;

public class RequestIgnoreBatteryOptimizations extends AlertActivity implements OnClickListener {
    IDeviceIdleController mDeviceIdleService;
    String mPackageName;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mDeviceIdleService = Stub.asInterface(ServiceManager.getService("deviceidle"));
        Uri data = getIntent().getData();
        if (data == null) {
            Log.w("RequestIgnoreBatteryOptimizations", "No data supplied for IGNORE_BATTERY_OPTIMIZATION_SETTINGS in: " + getIntent());
            finish();
            return;
        }
        this.mPackageName = data.getSchemeSpecificPart();
        if (this.mPackageName == null) {
            Log.w("RequestIgnoreBatteryOptimizations", "No data supplied for IGNORE_BATTERY_OPTIMIZATION_SETTINGS in: " + getIntent());
            finish();
        } else if (((PowerManager) getSystemService(PowerManager.class)).isIgnoringBatteryOptimizations(this.mPackageName)) {
            Log.i("RequestIgnoreBatteryOptimizations", "Not should prompt, already ignoring optimizations: " + this.mPackageName);
            finish();
        } else {
            try {
                ApplicationInfo ai = getPackageManager().getApplicationInfo(this.mPackageName, 0);
                if (getPackageManager().checkPermission("android.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS", this.mPackageName) != 0) {
                    Log.w("RequestIgnoreBatteryOptimizations", "Requested package " + this.mPackageName + " does not hold permission " + "android.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS");
                    finish();
                    return;
                }
                AlertParams p = this.mAlertParams;
                p.mTitle = getText(2131626980);
                p.mMessage = getString(2131626981, new Object[]{ai.loadLabel(getPackageManager())});
                p.mPositiveButtonText = getText(2131624348);
                p.mNegativeButtonText = getText(2131624349);
                p.mPositiveButtonListener = this;
                p.mNegativeButtonListener = this;
                setupAlert();
            } catch (NameNotFoundException e) {
                Log.w("RequestIgnoreBatteryOptimizations", "Requested package doesn't exist: " + this.mPackageName);
                finish();
            }
        }
    }

    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case -1:
                try {
                    this.mDeviceIdleService.addPowerSaveWhitelistApp(this.mPackageName);
                } catch (RemoteException e) {
                    Log.w("RequestIgnoreBatteryOptimizations", "Unable to reach IDeviceIdleController", e);
                }
                setResult(-1);
                return;
            default:
                return;
        }
    }
}
