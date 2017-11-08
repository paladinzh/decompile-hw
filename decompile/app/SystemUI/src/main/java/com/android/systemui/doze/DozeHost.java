package com.android.systemui.doze;

public interface DozeHost {

    public interface Callback {
        void onBuzzBeepBlinked();

        void onNewNotifications();

        void onNotificationLight(boolean z);

        void onPowerSaveChanged(boolean z);
    }

    public interface PulseCallback {
        void onPulseFinished();

        void onPulseStarted();
    }

    void addCallback(Callback callback);

    boolean isNotificationLightOn();

    boolean isPowerSaveActive();

    boolean isPulsingBlocked();

    void pulseWhileDozing(PulseCallback pulseCallback, int i);

    void removeCallback(Callback callback);

    void startDozing(Runnable runnable);

    void stopDozing();
}
