package com.android.setupwizardlib.util;

import android.content.Intent;

public class WizardManagerHelper {
    public static boolean isSetupWizardIntent(Intent intent) {
        return intent.getBooleanExtra("firstRun", false);
    }

    public static boolean isLightTheme(Intent intent, boolean def) {
        return isLightTheme(intent.getStringExtra("theme"), def);
    }

    public static boolean isLightTheme(String theme, boolean def) {
        if ("holo_light".equals(theme) || "material_light".equals(theme) || "material_blue_light".equals(theme) || "glif_light".equals(theme)) {
            return true;
        }
        if ("holo".equals(theme) || "material".equals(theme) || "material_blue".equals(theme) || "glif".equals(theme)) {
            return false;
        }
        return def;
    }
}
