package com.android.server.pm;

import android.content.Context;
import android.os.Binder;
import android.os.SystemProperties;
import android.provider.Settings.Secure;
import android.provider.SettingsEx.Systemex;
import android.util.Log;

public class HwCustHwPackageManagerServiceImpl extends HwCustHwPackageManagerService {
    private static final boolean IS_COTA_FEATURE = SystemProperties.getBoolean("ro.config.hw_cota", false);
    private static final boolean IS_REGIONAL_PHONE_FEATURE = SystemProperties.getBoolean("ro.config.region_phone_feature", false);
    private static final String TAG = "HwCustHwPackageManagerServiceImpl";

    public boolean isReginalPhoneFeature() {
        return IS_REGIONAL_PHONE_FEATURE;
    }

    public boolean isSupportThemeRestore() {
        return !IS_REGIONAL_PHONE_FEATURE ? IS_COTA_FEATURE : true;
    }

    public boolean isCustChange(Context context) {
        try {
            if (IS_REGIONAL_PHONE_FEATURE) {
                String originalVendorCountry = Secure.getString(context.getContentResolver(), "vendor_country");
                String currentVendorCountry = SystemProperties.get("ro.hw.custPath", "");
                if (originalVendorCountry == null) {
                    Secure.putString(context.getContentResolver(), "vendor_country", currentVendorCountry);
                    return false;
                } else if (!originalVendorCountry.equals(currentVendorCountry)) {
                    Secure.putString(context.getContentResolver(), "vendor_country", currentVendorCountry);
                    return true;
                }
            } else if (IS_COTA_FEATURE) {
                String originalCustVersion = Secure.getString(context.getContentResolver(), "custCDVersion");
                String custCVersion = SystemProperties.get("ro.product.CustCVersion", "");
                String custDVersion = SystemProperties.get("ro.product.CustDVersion", "");
                if (originalCustVersion == null) {
                    Secure.putString(context.getContentResolver(), "custCDVersion", custCVersion + custDVersion);
                    return false;
                } else if (!(custCVersion + custDVersion).equals(originalCustVersion)) {
                    Secure.putString(context.getContentResolver(), "custCDVersion", custCVersion + custDVersion);
                    return true;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "check cust Exception e : " + e);
        }
        return false;
    }

    public void changeTheme(String path, Context context) {
        String themePath = Systemex.getString(context.getContentResolver(), "hw_def_theme");
        if (path != null && !path.equals(themePath)) {
            long identity = Binder.clearCallingIdentity();
            Secure.putString(context.getContentResolver(), "isUserChangeTheme", "true");
            Binder.restoreCallingIdentity(identity);
        }
    }

    public boolean isThemeChange(Context context) {
        return "true".equals(Secure.getString(context.getContentResolver(), "isUserChangeTheme"));
    }
}
