package com.huawei.keyguard.inf;

public interface IFlashlightController {

    public interface FlashlightListener {
        void onFlashlightAvailabilityChanged(boolean z);

        void onFlashlightChanged(boolean z);

        void onFlashlightError();
    }

    void addListener(FlashlightListener flashlightListener);

    boolean isAvailable();

    boolean isEnabled();

    void removeListener(FlashlightListener flashlightListener);

    void setFlashlight(boolean z);
}
