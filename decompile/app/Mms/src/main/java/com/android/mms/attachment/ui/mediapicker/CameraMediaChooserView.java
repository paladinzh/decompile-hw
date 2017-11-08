package com.android.mms.attachment.ui.mediapicker;

import android.content.Context;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import com.android.mms.attachment.ui.PersistentInstanceState;
import com.android.mms.attachment.utils.ThreadUtil;
import com.google.android.gms.R;

public class CameraMediaChooserView extends FrameLayout implements PersistentInstanceState {
    private boolean mIsSoftwareFallbackActive;

    public CameraMediaChooserView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putInt("camera_index", CameraManager.get().getCameraIndex());
        return bundle;
    }

    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            CameraManager.get().selectCameraByIndex(((Bundle) state).getInt("camera_index"));
        }
    }

    public Parcelable saveState() {
        return onSaveInstanceState();
    }

    public void restoreState(Parcelable restoredState) {
        onRestoreInstanceState(restoredState);
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!canvas.isHardwareAccelerated() && !this.mIsSoftwareFallbackActive) {
            this.mIsSoftwareFallbackActive = true;
            ThreadUtil.getMainThreadHandler().post(new Runnable() {
                public void run() {
                    HardwareCameraPreview cameraPreview = (HardwareCameraPreview) CameraMediaChooserView.this.findViewById(R.id.camera_preview);
                    if (cameraPreview != null) {
                        ViewGroup parent = (ViewGroup) cameraPreview.getParent();
                        int index = parent.indexOfChild(cameraPreview);
                        SoftwareCameraPreview softwareCameraPreview = new SoftwareCameraPreview(CameraMediaChooserView.this.getContext());
                        parent.removeView(cameraPreview);
                        parent.addView(softwareCameraPreview, index);
                    }
                }
            });
        }
    }
}
