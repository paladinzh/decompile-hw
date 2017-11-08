package com.android.systemui.usb;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.hardware.usb.IUsbManager;
import android.hardware.usb.IUsbManager.Stub;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbDevice;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.util.Log;
import android.widget.CheckBox;
import com.android.internal.app.ResolverActivity;
import com.android.internal.app.ResolverActivity.TargetInfo;
import com.android.systemui.R;
import java.util.ArrayList;

public class UsbResolverActivity extends ResolverActivity {
    private UsbAccessory mAccessory;
    private UsbDevice mDevice;
    private UsbDisconnectedReceiver mDisconnectedReceiver;

    protected void onCreate(Bundle savedInstanceState) {
        Intent intent = getIntent();
        Parcelable targetParcelable = intent.getParcelableExtra("android.intent.extra.INTENT");
        if (targetParcelable instanceof Intent) {
            Intent target = (Intent) targetParcelable;
            ArrayList<ResolveInfo> rList = intent.getParcelableArrayListExtra("rlist");
            super.onCreate(savedInstanceState, target, getResources().getText(17040256), null, rList, true);
            CheckBox alwaysUse = (CheckBox) findViewById(16909098);
            if (alwaysUse != null) {
                if (this.mDevice == null) {
                    alwaysUse.setText(R.string.always_use_accessory);
                } else {
                    alwaysUse.setText(R.string.always_use_device);
                }
            }
            this.mDevice = (UsbDevice) target.getParcelableExtra("device");
            if (this.mDevice != null) {
                this.mDisconnectedReceiver = new UsbDisconnectedReceiver((Activity) this, this.mDevice);
            } else {
                this.mAccessory = (UsbAccessory) target.getParcelableExtra("accessory");
                if (this.mAccessory == null) {
                    Log.e("UsbResolverActivity", "no device or accessory");
                    finish();
                    return;
                }
                this.mDisconnectedReceiver = new UsbDisconnectedReceiver((Activity) this, this.mAccessory);
            }
            return;
        }
        Log.w("UsbResolverActivity", "Target is not an intent: " + targetParcelable);
        finish();
    }

    protected void onDestroy() {
        if (this.mDisconnectedReceiver != null) {
            unregisterReceiver(this.mDisconnectedReceiver);
        }
        super.onDestroy();
    }

    protected boolean onTargetSelected(TargetInfo target, boolean alwaysCheck) {
        ResolveInfo ri = target.getResolveInfo();
        try {
            IUsbManager service = Stub.asInterface(ServiceManager.getService("usb"));
            int uid = ri.activityInfo.applicationInfo.uid;
            int userId = UserHandle.myUserId();
            if (this.mDevice != null) {
                service.grantDevicePermission(this.mDevice, uid);
                if (alwaysCheck) {
                    service.setDevicePackage(this.mDevice, ri.activityInfo.packageName, userId);
                } else {
                    service.setDevicePackage(this.mDevice, null, userId);
                }
            } else if (this.mAccessory != null) {
                service.grantAccessoryPermission(this.mAccessory, uid);
                if (alwaysCheck) {
                    service.setAccessoryPackage(this.mAccessory, ri.activityInfo.packageName, userId);
                } else {
                    service.setAccessoryPackage(this.mAccessory, null, userId);
                }
            }
            try {
                target.startAsUser(this, null, new UserHandle(userId));
            } catch (ActivityNotFoundException e) {
                Log.e("UsbResolverActivity", "startActivity failed", e);
            }
        } catch (RemoteException e2) {
            Log.e("UsbResolverActivity", "onIntentSelected failed", e2);
        }
        return true;
    }
}
