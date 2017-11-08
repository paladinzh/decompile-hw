package com.android.mms.attachment.ui.mediapicker;

import android.content.Context;
import android.hardware.Camera;
import android.os.Parcelable;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import com.android.mms.attachment.ui.mediapicker.CameraPreview.CameraPreviewHost;
import java.io.IOException;

public class SoftwareCameraPreview extends SurfaceView implements CameraPreviewHost {
    private final CameraPreview mPreview = new CameraPreview(this);

    public SoftwareCameraPreview(Context context) {
        super(context);
        getHolder().addCallback(new Callback() {
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                CameraManager.get().setSurface(SoftwareCameraPreview.this.mPreview);
            }

            public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {
                CameraManager.get().setSurface(SoftwareCameraPreview.this.mPreview);
            }

            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                CameraManager.get().setSurface(null);
            }
        });
    }

    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        this.mPreview.onVisibilityChanged(visibility);
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.mPreview.onDetachedFromWindow();
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.mPreview.onAttachedToWindow();
    }

    protected void onRestoreInstanceState(Parcelable state) {
        super.onRestoreInstanceState(state);
        this.mPreview.onRestoreInstanceState();
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        widthMeasureSpec = this.mPreview.getWidthMeasureSpec(widthMeasureSpec, heightMeasureSpec);
        super.onMeasure(widthMeasureSpec, this.mPreview.getHeightMeasureSpec(widthMeasureSpec, heightMeasureSpec));
    }

    public View getView() {
        return this;
    }

    public void startPreview(Camera camera) throws IOException {
        camera.setPreviewDisplay(getHolder());
    }

    public void onCameraPermissionGranted() {
        this.mPreview.onCameraPermissionGranted();
    }
}
