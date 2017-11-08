package com.android.mms.attachment.ui.mediapicker;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnTouchListener;
import java.io.IOException;

public class CameraPreview {
    private int mCameraHeight = -1;
    private int mCameraWidth = -1;
    private final CameraPreviewHost mHost;

    public interface CameraPreviewHost {
        View getView();

        void onCameraPermissionGranted();

        void startPreview(Camera camera) throws IOException;
    }

    public CameraPreview(CameraPreviewHost host) {
        this.mHost = host;
    }

    public void setSize(Size size, int orientation) {
        switch (orientation) {
            case 0:
            case 180:
                this.mCameraWidth = size.width;
                this.mCameraHeight = size.height;
                break;
            default:
                this.mCameraWidth = size.height;
                this.mCameraHeight = size.width;
                break;
        }
        this.mHost.getView().requestLayout();
    }

    public int getWidthMeasureSpec(int widthMeasureSpec, int heightMeasureSpec) {
        if (this.mCameraHeight < 0) {
            return widthMeasureSpec;
        }
        int width;
        int orientation = getContext().getResources().getConfiguration().orientation;
        float aspectRatio = ((float) this.mCameraWidth) / ((float) this.mCameraHeight);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        if (orientation == 2) {
            width = (int) (((float) height) / aspectRatio);
        } else {
            width = MeasureSpec.getSize(widthMeasureSpec);
        }
        return MeasureSpec.makeMeasureSpec(width, 1073741824);
    }

    public int getHeightMeasureSpec(int widthMeasureSpec, int heightMeasureSpec) {
        if (this.mCameraHeight < 0) {
            return heightMeasureSpec;
        }
        int height;
        int orientation = getContext().getResources().getConfiguration().orientation;
        int width = MeasureSpec.getSize(widthMeasureSpec);
        float aspectRatio = ((float) this.mCameraWidth) / ((float) this.mCameraHeight);
        if (orientation == 2) {
            height = MeasureSpec.getSize(heightMeasureSpec);
        } else {
            height = (int) (((float) width) / aspectRatio);
        }
        return MeasureSpec.makeMeasureSpec(height, 1073741824);
    }

    public void onVisibilityChanged(int visibility) {
        if (!CameraManager.hasCameraPermission()) {
            return;
        }
        if (visibility == 0) {
            CameraManager.get().openCamera();
        } else {
            CameraManager.get().closeCamera();
        }
    }

    public Context getContext() {
        return this.mHost.getView().getContext();
    }

    public void setOnTouchListener(OnTouchListener listener) {
        this.mHost.getView().setOnTouchListener(listener);
    }

    public void onAttachedToWindow() {
        if (CameraManager.hasCameraPermission()) {
            CameraManager.get().openCamera();
        }
    }

    public void onDetachedFromWindow() {
        CameraManager.get().closeCamera();
    }

    public void onRestoreInstanceState() {
        if (CameraManager.hasCameraPermission()) {
            CameraManager.get().openCamera();
        }
    }

    public void onCameraPermissionGranted() {
        CameraManager.get().openCamera();
    }

    public void startPreview(Camera camera) throws IOException {
        this.mHost.startPreview(camera);
    }
}
