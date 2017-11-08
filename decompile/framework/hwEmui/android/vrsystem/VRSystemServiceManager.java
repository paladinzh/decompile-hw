package android.vrsystem;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.util.Log;
import android.vrsystem.IVRSystemService.Stub;

public class VRSystemServiceManager implements IVRSystemServiceManager {
    private static final int MODE_NO_VR = 2;
    private static final int MODE_VR = 1;
    private static final String SYSTEMUI = "com.android.systemui";
    private static final String TAG = "VRSystemServiceManager";
    private static final String VR_METADATA_NAME = "com.huawei.android.vr.application.mode";
    private static final String VR_METADATA_VALUE = "vr_only";
    private static final boolean VR_SWITCH = SystemProperties.getBoolean("ro.vr.surport", false);
    private static VRSystemServiceManager sInstance;
    private IVRSystemService mVRM;

    private VRSystemServiceManager(IVRSystemService ivrm) {
        this.mVRM = ivrm;
    }

    public static VRSystemServiceManager getInstance() {
        VRSystemServiceManager vRSystemServiceManager;
        synchronized (VRSystemServiceManager.class) {
            if (sInstance == null || !sInstance.isValid()) {
                sInstance = new VRSystemServiceManager(Stub.asInterface(ServiceManager.getService("vr_system")));
            }
            vRSystemServiceManager = sInstance;
        }
        return vRSystemServiceManager;
    }

    private boolean checkServiceValid() {
        this.mVRM = Stub.asInterface(ServiceManager.getService("vr_system"));
        if (this.mVRM == null) {
            Log.w(TAG, "vr service is not alive");
            return false;
        }
        boolean valid = false;
        try {
            this.mVRM.isVRmode();
            valid = true;
        } catch (Exception ex) {
            Log.w(TAG, "vr service exception, please check", ex);
        }
        return valid;
    }

    private boolean isValid() {
        return VR_SWITCH ? checkServiceValid() : false;
    }

    public boolean isVRMode() {
        if (!isValid()) {
            return false;
        }
        boolean isVR = false;
        try {
            isVR = this.mVRM.isVRmode();
        } catch (Exception ex) {
            Log.w(TAG, "vr state query exception!", ex);
        }
        return isVR;
    }

    public boolean isVRApplication(Context context, String packageName) {
        if (!isValid() || context == null || packageName == null || packageName.equals("")) {
            return false;
        }
        if (SYSTEMUI.equals(packageName)) {
            return true;
        }
        boolean allowStart;
        ApplicationInfo appinfo = null;
        try {
            appinfo = context.getPackageManager().getApplicationInfo(packageName, 128);
        } catch (Exception e) {
            Log.e(TAG, "getApplicationInfo exception", e);
        }
        if (appinfo == null || appinfo.metaData == null) {
            allowStart = false;
        } else {
            allowStart = VR_METADATA_VALUE.equals(appinfo.metaData.getString(VR_METADATA_NAME));
        }
        if (!allowStart) {
            Log.i(TAG, "no vr metaData");
        }
        return allowStart;
    }

    public String getContactName(Context context, String num) {
        if (!isValid() || context == null) {
            return null;
        }
        if (isVRApplication(context, context.getPackageName())) {
            String name = null;
            try {
                name = this.mVRM.getContactName(num);
            } catch (Exception ex) {
                Log.w(TAG, "vr state query exception!", ex);
            }
            return name;
        }
        Log.i(TAG, "Client is not vr");
        return null;
    }

    public void registerVRListener(Context context, IVRListener vrlistener) {
        if (!isValid() || context == null) {
            return;
        }
        if (isVRApplication(context, context.getPackageName())) {
            try {
                this.mVRM.addVRListener(vrlistener);
            } catch (RemoteException e) {
                Log.w(TAG, "add listener exception", e);
            }
            return;
        }
        Log.i(TAG, "Client is not vr");
    }

    public void unregisterVRListener(Context context, IVRListener vrlistener) {
        if (isValid() && context != null) {
            try {
                this.mVRM.deleteVRListener(vrlistener);
            } catch (RemoteException e) {
                Log.w(TAG, "delete listener exception", e);
            }
        }
    }

    public void acceptInCall(Context context) {
        if (!isValid() || context == null) {
            return;
        }
        if (isVRApplication(context, context.getPackageName())) {
            try {
                this.mVRM.acceptInCall();
            } catch (Exception ex) {
                Log.w(TAG, "acceptInCall request exception!", ex);
            }
            return;
        }
        Log.i(TAG, "Client is not vr");
    }

    public void endInCall(Context context) {
        if (!isValid() || context == null) {
            return;
        }
        if (isVRApplication(context, context.getPackageName())) {
            try {
                this.mVRM.endInCall();
            } catch (Exception ex) {
                Log.w(TAG, "endInCall request exception!", ex);
            }
            return;
        }
        Log.i(TAG, "Client is not vr");
    }
}
