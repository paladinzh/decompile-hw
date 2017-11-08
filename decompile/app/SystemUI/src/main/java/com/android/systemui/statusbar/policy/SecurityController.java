package com.android.systemui.statusbar.policy;

public interface SecurityController {

    public interface SecurityControllerCallback {
        void onStateChanged();
    }

    void addCallback(SecurityControllerCallback securityControllerCallback);

    boolean isVpnEnabled();

    void removeCallback(SecurityControllerCallback securityControllerCallback);
}
