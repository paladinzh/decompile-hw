package com.fyusion.sdk.camera;

/* compiled from: Unknown */
public interface FyuseCameraCallback {
    void cameraReady();

    void insufficientCamera2Support();

    void onAutoFocus(boolean z, String str);

    void onExposureLock(boolean z, String str);
}
