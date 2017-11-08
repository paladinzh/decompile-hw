package android.hdm;

import android.common.HwFrameworkFactory;
import android.content.Intent;
import android.util.Log;
import java.util.List;

public class HwDeviceManager {
    public static final int DISABLED_ADB = 11;
    public static final int DISABLED_BACK = 16;
    public static final int DISABLED_BLUETOOTH = 8;
    public static final int DISABLED_GPS = 13;
    public static final int DISABLED_HOME = 14;
    public static final int DISABLED_INSTALL_SOURCE = 2;
    public static final int DISABLED_SAFEMODE = 10;
    public static final int DISABLED_TASK = 15;
    public static final int DISABLED_USBOTG = 12;
    public static final int DISABLED_VOICE = 1;
    public static final int DISABLED_WIFI = 0;
    public static final int DISABLE_CHANGE_LAUNCHER = 17;
    public static final int IS_ADBORSDCARD_INSTALL_RESTRICTED = 6;
    public static final int IS_ALLOWED_INSTALL_PACKAGE = 7;
    public static final int IS_DISABLED_DEACTIVATE_MDM_PACKAGE = 18;
    public static final int IS_DISALLOWED_RUNNINGAPP = 4;
    public static final int IS_DISALLOWED_UNINSTALL_PACKAGE = 5;
    public static final int IS_PERSISTENT_APP = 3;
    public static final int NETWORK_ACCESS_WHITELIST = 9;
    private static final String TAG = "HwDeviceManager";
    private static IHwDeviceManager sInstance = null;

    public interface IHwDeviceManager {
        List<String> getNetworkAccessWhitelist();

        boolean isAdbDisabled();

        boolean isAdbOrSDCardInstallRestricted();

        boolean isAllowedInstallPackage(String str);

        boolean isBackButtonDisabled();

        boolean isBluetoothDisabled();

        boolean isChangeLauncherDisable();

        boolean isDisabledDeactivateMdmPackage(String str);

        boolean isDisallowedRunningApp(String str);

        boolean isDisallowedUninstallPackage(String str);

        boolean isGPSDisabled();

        boolean isHomeButtonDisabled();

        boolean isInstallSourceDisabled();

        boolean isIntentFromAllowedInstallSource(Intent intent);

        boolean isPersistentApp(String str);

        boolean isSafeModeDisabled();

        boolean isTaskButtonDisabled();

        boolean isUSBOtgDisabled();

        boolean isVoiceDisabled();

        boolean isWifiDisabled();
    }

    private static IHwDeviceManager getImplObject() {
        if (sInstance == null) {
            sInstance = HwFrameworkFactory.getHuaweiDevicePolicyManager();
        }
        return sInstance;
    }

    public static boolean disallowOp(int type) {
        IHwDeviceManager instance = getImplObject();
        if (instance == null) {
            Log.w(TAG, "Can not get the instance of IHwDeviceManager object.");
            return false;
        }
        boolean bDisabled = false;
        switch (type) {
            case 0:
                try {
                    bDisabled = instance.isWifiDisabled();
                    break;
                } catch (Exception e) {
                    Log.e(TAG, "Disallow operation " + type + " exception: " + e.getMessage());
                    break;
                }
            case 1:
                bDisabled = instance.isVoiceDisabled();
                break;
            case 2:
                bDisabled = instance.isInstallSourceDisabled();
                break;
            case 6:
                bDisabled = instance.isAdbOrSDCardInstallRestricted();
                break;
            case 8:
                bDisabled = instance.isBluetoothDisabled();
                break;
            case 10:
                bDisabled = instance.isSafeModeDisabled();
                break;
            case 11:
                bDisabled = instance.isAdbDisabled();
                break;
            case 12:
                bDisabled = instance.isUSBOtgDisabled();
                break;
            case 13:
                bDisabled = instance.isGPSDisabled();
                break;
            case 14:
                bDisabled = instance.isHomeButtonDisabled();
                break;
            case 15:
                bDisabled = instance.isTaskButtonDisabled();
                break;
            case 16:
                bDisabled = instance.isBackButtonDisabled();
                break;
            case 17:
                bDisabled = instance.isChangeLauncherDisable();
                break;
        }
        return bDisabled;
    }

    public static boolean disallowOp(int type, String param) {
        IHwDeviceManager instance = getImplObject();
        if (instance == null) {
            Log.w(TAG, "Can not get the instance of IHwDeviceManager object.");
            return false;
        }
        boolean bDisabled = false;
        switch (type) {
            case 3:
                try {
                    bDisabled = instance.isPersistentApp(param);
                    break;
                } catch (Exception e) {
                    Log.e(TAG, "Disallow operation " + type + " exception: " + e.getMessage());
                    break;
                }
            case 4:
                bDisabled = instance.isDisallowedRunningApp(param);
                break;
            case 5:
                bDisabled = instance.isDisallowedUninstallPackage(param);
                break;
            case 7:
                if (!instance.isAllowedInstallPackage(param)) {
                    bDisabled = true;
                    break;
                }
                bDisabled = false;
                break;
            case 18:
                bDisabled = instance.isDisabledDeactivateMdmPackage(param);
                break;
        }
        return bDisabled;
    }

    public static boolean disallowOp(Intent installSource) {
        IHwDeviceManager instance = getImplObject();
        if (instance == null) {
            Log.w(TAG, "Can not get the instance of IHwDeviceManager object.");
            return false;
        }
        boolean bDisabled = false;
        try {
            bDisabled = !instance.isIntentFromAllowedInstallSource(installSource);
        } catch (Exception e) {
            Log.e(TAG, "Disallow operation " + installSource.getAction() + " exception: " + e.getMessage());
        }
        return bDisabled;
    }

    public static List<String> getList(int type) {
        IHwDeviceManager instance = getImplObject();
        if (instance == null) {
            Log.w(TAG, "Can not get the instance of IHwDeviceManager object.");
            return null;
        }
        List<String> list = null;
        switch (type) {
            case 9:
                try {
                    list = instance.getNetworkAccessWhitelist();
                    break;
                } catch (Exception e) {
                    Log.e(TAG, "Get list " + type + " exception: " + e.getMessage());
                    break;
                }
        }
        return list;
    }
}
