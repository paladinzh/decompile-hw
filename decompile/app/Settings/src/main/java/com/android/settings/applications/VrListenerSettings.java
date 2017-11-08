package com.android.settings.applications;

import com.android.settings.utils.ManagedServiceSettings;
import com.android.settings.utils.ManagedServiceSettings.Config;

public class VrListenerSettings extends ManagedServiceSettings {
    private static final Config CONFIG = getVrListenerConfig();
    private static final String TAG = VrListenerSettings.class.getSimpleName();

    private static final Config getVrListenerConfig() {
        Config c = new Config();
        c.tag = TAG;
        c.setting = "enabled_vr_listeners";
        c.intentAction = "android.service.vr.VrListenerService";
        c.permission = "android.permission.BIND_VR_LISTENER_SERVICE";
        c.noun = "vr listener";
        c.warningDialogTitle = 2131626761;
        c.warningDialogSummary = 2131626762;
        c.emptyText = 2131626760;
        return c;
    }

    protected Config getConfig() {
        return CONFIG;
    }

    protected int getMetricsCategory() {
        return 334;
    }
}
