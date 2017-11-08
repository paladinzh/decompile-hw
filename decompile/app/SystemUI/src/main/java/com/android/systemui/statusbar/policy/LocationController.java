package com.android.systemui.statusbar.policy;

public interface LocationController {

    public interface LocationSettingsChangeCallback {
        void onLocationSettingsChanged(boolean z);
    }

    void addSettingsChangedCallback(LocationSettingsChangeCallback locationSettingsChangeCallback);

    boolean isLocationEnabled();

    void removeSettingsChangedCallback(LocationSettingsChangeCallback locationSettingsChangeCallback);

    boolean setLocationEnabled(boolean z);
}
