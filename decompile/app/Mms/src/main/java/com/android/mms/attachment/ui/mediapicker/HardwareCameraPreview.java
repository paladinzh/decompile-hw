package com.android.mms.attachment.ui.mediapicker;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.TextureView;
import android.view.TextureView.SurfaceTextureListener;
import android.view.View;
import com.android.mms.attachment.ui.mediapicker.CameraPreview.CameraPreviewHost;
import java.io.IOException;

public class HardwareCameraPreview extends TextureView implements CameraPreviewHost {
    private CameraPreview mPreview = new CameraPreview(this);

    public HardwareCameraPreview(Context context, AttributeSet attrs) {
        super(context, attrs);
        setSurfaceTextureListener(new SurfaceTextureListener() {
            public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i2) {
                CameraManager.get().setSurface(HardwareCameraPreview.this.mPreview);
            }

            public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i2) {
                CameraManager.get().setSurface(HardwareCameraPreview.this.mPreview);
            }

            public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
                CameraManager.get().setSurface(null);
                return true;
            }

            public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
                CameraManager.get().setSurface(HardwareCameraPreview.this.mPreview);
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
        if (getContext().getResources().getConfiguration().orientation == 2) {
            heightMeasureSpec = this.mPreview.getHeightMeasureSpec(widthMeasureSpec, heightMeasureSpec);
            widthMeasureSpec = this.mPreview.getWidthMeasureSpec(widthMeasureSpec, heightMeasureSpec);
        } else {
            widthMeasureSpec = this.mPreview.getWidthMeasureSpec(widthMeasureSpec, heightMeasureSpec);
            heightMeasureSpec = this.mPreview.getHeightMeasureSpec(widthMeasureSpec, heightMeasureSpec);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public View getView() {
        return this;
    }

    public void startPreview(Camera camera) throws IOException {
        camera.setPreviewTexture(getSurfaceTexture());
    }

    public void onCameraPermissionGranted() {
        this.mPreview.onCameraPermissionGranted();
    }
}
