package com.fyusion.sdk.camera;

import android.support.annotation.Nullable;
import java.io.File;

/* compiled from: Unknown */
public interface FyuseCamera {

    /* compiled from: Unknown */
    public enum CameraType {
        FRONT_CAMERA,
        BACK_CAMERA
    }

    /* compiled from: Unknown */
    public enum RotationDirection {
        UNSPECIFIED(0),
        CLOCKWISE(1),
        COUNTERCLOCKWISE(-1);
        
        private int a;

        private RotationDirection(int i) {
            this.a = i;
        }

        public int getValue() {
            return this.a;
        }
    }

    void addCaptureEventListener(CaptureEventListener captureEventListener);

    void addRecordingProgressListener(RecordingProgressListener recordingProgressListener);

    FyuseCameraCapabilities getCameraCapabilities(CameraType cameraType);

    int getCameraIdForType(CameraType cameraType);

    int getCameraVersion();

    @Nullable
    CustomExposureControl<?> getManualExposureController() throws IllegalStateException;

    void open(CameraType cameraType, FyuseCameraCallback fyuseCameraCallback) throws FyuseCameraException;

    void open(FyuseCameraCallback fyuseCameraCallback) throws FyuseCameraException;

    void release();

    void removeCaptureEventListener(CaptureEventListener captureEventListener);

    void removeRecordingProgressListener(RecordingProgressListener recordingProgressListener);

    void setCameraParameters(FyuseCameraParameters fyuseCameraParameters) throws IllegalStateException;

    void setExposure(float f, float f2);

    void setExposureAndFocus(float f, float f2);

    void setFlash(boolean z);

    void setFocus(float f, float f2);

    void setTargetRotation(int i, float[] fArr, RotationDirection rotationDirection);

    void startRecording(MotionHintsListener motionHintsListener, File file) throws FyuseCameraException;

    void stopRecording() throws FyuseCameraException;

    void takeSnapShot(SnapShotCallback snapShotCallback);
}
