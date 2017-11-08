package com.android.settings;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import com.android.setupwizardlib.util.SystemBarHelper;
import com.android.setupwizardlib.util.WizardManagerHelper;

public class SetupWizardUtils {
    public static int getTheme(Intent intent) {
        if (WizardManagerHelper.isLightTheme(intent, true)) {
            return 2131755542;
        }
        return 2131755541;
    }

    public static int getTransparentTheme(Intent intent) {
        if (WizardManagerHelper.isLightTheme(intent, true)) {
            return 2131755544;
        }
        return 2131755543;
    }

    public static void setImmersiveMode(Activity activity) {
        if (activity.getIntent().getBooleanExtra("useImmersiveMode", false)) {
            SystemBarHelper.hideSystemBars(activity.getWindow());
        }
    }

    public static void applyImmersiveFlags(Dialog dialog) {
        SystemBarHelper.hideSystemBars(dialog);
    }

    public static void copySetupExtras(Intent fromIntent, Intent toIntent) {
        toIntent.putExtra("theme", fromIntent.getStringExtra("theme"));
        toIntent.putExtra("useImmersiveMode", fromIntent.getBooleanExtra("useImmersiveMode", false));
    }
}
