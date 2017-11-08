package com.android.systemui.statusbar.policy;

public interface SoundVibrationController {

    public interface RingModeChangeCallback {
        void onRingModeChanged(int i, boolean z);
    }

    void addRingModeChangedCallback(RingModeChangeCallback ringModeChangeCallback);

    int getRingMode();

    boolean isVibrationEnable();

    void removeRingModeChangedCallback(RingModeChangeCallback ringModeChangeCallback);

    void setRingMode(int i);

    void setVibrationState(boolean z);
}
