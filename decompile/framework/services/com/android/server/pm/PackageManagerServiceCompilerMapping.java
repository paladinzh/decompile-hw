package com.android.server.pm;

import android.os.SystemProperties;
import dalvik.system.DexFile;

class PackageManagerServiceCompilerMapping {
    static final String[] REASON_STRINGS = new String[]{"first-boot", "boot", "install", "bg-dexopt", "ab-ota", "nsys-library", "shared-apk", "forced-dexopt", "core-app"};

    PackageManagerServiceCompilerMapping() {
    }

    static {
        if (9 != REASON_STRINGS.length) {
            throw new IllegalStateException("REASON_STRINGS not correct");
        }
    }

    private static String getSystemPropertyName(int reason) {
        if (reason >= 0 && reason < REASON_STRINGS.length) {
            return "pm.dexopt." + REASON_STRINGS[reason];
        }
        throw new IllegalArgumentException("reason " + reason + " invalid");
    }

    private static String getAndCheckValidity(int reason) {
        String sysPropValue = SystemProperties.get(getSystemPropertyName(reason));
        if (sysPropValue == null || sysPropValue.isEmpty() || !DexFile.isValidCompilerFilter(sysPropValue)) {
            throw new IllegalStateException("Value \"" + sysPropValue + "\" not valid " + "(reason " + REASON_STRINGS[reason] + ")");
        }
        switch (reason) {
            case 6:
            case 7:
                if (DexFile.isProfileGuidedCompilerFilter(sysPropValue)) {
                    throw new IllegalStateException("\"" + sysPropValue + "\" is profile-guided, " + "but not allowed for " + REASON_STRINGS[reason]);
                }
                break;
        }
        return sysPropValue;
    }

    static void checkProperties() {
        RuntimeException toThrow = null;
        int reason = 0;
        while (reason <= 8) {
            try {
                String sysPropName = getSystemPropertyName(reason);
                if (sysPropName == null || sysPropName.isEmpty() || sysPropName.length() > 31) {
                    throw new IllegalStateException("Reason system property name \"" + sysPropName + "\" for reason " + REASON_STRINGS[reason]);
                }
                getAndCheckValidity(reason);
                reason++;
            } catch (Exception exc) {
                if (toThrow == null) {
                    toThrow = new IllegalStateException("PMS compiler filter settings are bad.");
                }
                toThrow.addSuppressed(exc);
            }
        }
        if (toThrow != null) {
            throw toThrow;
        }
    }

    public static String getCompilerFilterForReason(int reason) {
        return getAndCheckValidity(reason);
    }

    public static String getFullCompilerFilter() {
        String value = SystemProperties.get("dalvik.vm.dex2oat-filter");
        if (value == null || value.isEmpty()) {
            return "speed";
        }
        if (!DexFile.isValidCompilerFilter(value) || DexFile.isProfileGuidedCompilerFilter(value)) {
            return "speed";
        }
        return value;
    }

    public static String getNonProfileGuidedCompilerFilter(String filter) {
        return DexFile.getNonProfileGuidedCompilerFilter(filter);
    }
}
