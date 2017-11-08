package com.fyusion.sdk.camera;

import android.hardware.Camera;
import com.fyusion.sdk.camera.FyuseCamera.RotationDirection;
import com.fyusion.sdk.camera.impl.i;
import com.fyusion.sdk.common.DLog;
import com.fyusion.sdk.common.FyuseSDK;
import com.fyusion.sdk.common.a;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/* compiled from: Unknown */
public class FyuseRecorder {
    private static final String a = FyuseRecorder.class.getSimpleName();
    private boolean b = false;
    private FyuseCamera c;
    private Camera d;
    private MotionHintsListener e;
    private boolean f = false;
    private int g = -1;
    private List<RecordingProgressListener> h = new ArrayList();

    public FyuseRecorder(Camera camera, int i) throws FyuseCameraException {
        if (camera != null && i >= 0) {
            a.a().d();
            this.d = camera;
            this.g = i;
            this.c = new i(FyuseSDK.getContext(), 1);
            return;
        }
        throw new FyuseCameraException("Camera is NULL or CameraId < 0");
    }

    public void addCaptureEventListener(CaptureEventListener captureEventListener) {
        this.c.addCaptureEventListener(captureEventListener);
    }

    public void addMotionHintsListener(MotionHintsListener motionHintsListener) {
        this.e = motionHintsListener;
    }

    public void addRecordingProgressListener(RecordingProgressListener recordingProgressListener) {
        this.c.addRecordingProgressListener(recordingProgressListener);
    }

    public void removeCaptureEventListener(CaptureEventListener captureEventListener) {
        if (this.c != null) {
            this.c.removeCaptureEventListener(captureEventListener);
        }
    }

    public void removeRecordingProgressListener(RecordingProgressListener recordingProgressListener) {
        this.c.removeRecordingProgressListener(recordingProgressListener);
    }

    public void setTargetRotation(int i, float[] fArr, RotationDirection rotationDirection) {
        this.c.setTargetRotation(i, fArr, rotationDirection);
    }

    public void start(File file) throws FyuseCameraException {
        i iVar = (i) this.c;
        if (this.b) {
            DLog.d(a, "Target File dir name : " + file.getAbsolutePath());
        }
        if (file.getParentFile().exists()) {
            iVar.a(this.d, this.g);
            this.c.startRecording(this.e, file);
            return;
        }
        throw new FyuseCameraException("Directory path does not exist!");
    }

    public void stop() throws FyuseCameraException {
        if (this.c != null) {
            this.c.stopRecording();
        }
    }
}
