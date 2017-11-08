package com.huawei.permissionmanager.utils;

import android.content.Context;
import android.content.res.Resources;
import android.location.ILocationManager.Stub;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.provider.Settings.Secure;
import java.util.ArrayList;

public class LocationUtils {
    public static final String LOCATION_PERMISSION = "android.permission-group.LOCATION";

    public static ArrayList<String> getLocationProviders() {
        ArrayList<String> providers = new ArrayList();
        Resources res = Resources.getSystem();
        providers.add(res.getString(17039423));
        for (String provider : res.getStringArray(17236013)) {
            providers.add(provider);
        }
        return providers;
    }

    public static boolean isLocationEnabled(Context context) {
        return Secure.getInt(context.getContentResolver(), "location_mode", 0) != 0;
    }

    public static boolean isLocationGroupAndProvider(String groupName, String packageName) {
        return "android.permission-group.LOCATION".equals(groupName) ? isNetworkLocationProvider(packageName) : false;
    }

    private static boolean isNetworkLocationProvider(String packageName) {
        try {
            return packageName.equals(Stub.asInterface(ServiceManager.getService("location")).getNetworkProviderPackage());
        } catch (RemoteException e) {
            return false;
        }
    }
}
