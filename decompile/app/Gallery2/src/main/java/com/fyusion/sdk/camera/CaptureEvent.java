package com.fyusion.sdk.camera;

import com.fyusion.sdk.camera.FyuseCamera.CameraType;

/* compiled from: Unknown */
public final class CaptureEvent {
    private long a;
    private String b;
    private CaptureStatus c;
    private String d;
    private String e;
    private CameraType f;
    private String g;

    /* compiled from: Unknown */
    public enum CaptureStatus {
        CAPTURE_IN_PROGRESS,
        CAPTURE_STOPPED,
        CAPTURE_FAILED,
        CAPTURE_COMPLETED
    }

    public CaptureEvent(CaptureStatus captureStatus, long j) {
        this.c = captureStatus;
        this.a = j;
    }

    public CaptureEvent(CaptureStatus captureStatus, long j, String str) {
        this.c = captureStatus;
        this.a = j;
        this.b = str;
    }

    public CaptureEvent(CaptureStatus captureStatus, long j, String str, CameraType cameraType) {
        this.c = captureStatus;
        this.a = j;
        this.b = str;
        this.f = cameraType;
    }

    private CaptureEvent(CaptureStatus captureStatus, String str) {
        this(captureStatus, System.currentTimeMillis());
        setDescription(str);
    }

    public static CaptureEvent createCompletedEvent(String str) {
        return new CaptureEvent(CaptureStatus.CAPTURE_COMPLETED, System.currentTimeMillis(), str);
    }

    public static CaptureEvent createFailedEvent(String str) {
        return new CaptureEvent(CaptureStatus.CAPTURE_FAILED, str);
    }

    public static CaptureEvent createInProgressEvent(String str) {
        return new CaptureEvent(CaptureStatus.CAPTURE_IN_PROGRESS, System.currentTimeMillis(), str);
    }

    public static CaptureEvent createStoppedEvent(String str) {
        return new CaptureEvent(CaptureStatus.CAPTURE_STOPPED, str);
    }

    public CameraType getCameraType() {
        return this.f;
    }

    public CaptureStatus getCaptureStatus() {
        return this.c;
    }

    public String getDescription() {
        return this.e;
    }

    public String getRecordedFyuseLocation() {
        return this.b;
    }

    public String getRecordingStatus() {
        return this.d;
    }

    public long getTimestamp() {
        return this.a;
    }

    public String getUid() {
        return this.g;
    }

    public void setCurrentActiveCamera(CameraType cameraType) {
        this.f = cameraType;
    }

    public void setDescription(String str) {
        this.e = str;
    }

    public void setRecordingStatus(String str) {
        this.d = str;
    }

    public void setUid(String str) {
        this.g = str;
    }
}
