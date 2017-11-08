package com.android.systemui.statusbar.policy;

public interface RotationLockController {

    public interface RotationLockControllerCallback {
        void onRotationLockStateChanged(boolean z, boolean z2);
    }

    void addRotationLockControllerCallback(RotationLockControllerCallback rotationLockControllerCallback);

    int getRotationLockOrientation();

    boolean isRotationLocked();

    void removeRotationLockControllerCallback(RotationLockControllerCallback rotationLockControllerCallback);

    void setRotationLocked(boolean z);
}
