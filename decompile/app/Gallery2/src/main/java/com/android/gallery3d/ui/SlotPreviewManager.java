package com.android.gallery3d.ui;

import com.android.gallery3d.anim.Animation.AnimationListener;
import com.android.gallery3d.anim.FloatAnimation;
import com.android.gallery3d.common.Utils;
import com.huawei.gallery.animation.CubicBezierInterpolator;
import com.huawei.watermark.manager.parse.WMElement;

public class SlotPreviewManager {
    private SlotPreviewModeListener mListener;
    private SlotPreviewState mPreviewState = SlotPreviewState.INIT;
    private float mPreviewViewScale = 0.0f;
    private FloatAnimation mSlotPreviewAnimation;

    public interface SlotPreviewModeListener {
        void onEnterPreviewMode();

        void onLeavePreviewMode();
    }

    public enum SlotPreviewState {
        INIT(0),
        ANIM(1),
        SCALING(2),
        PREVIEW(3);
        
        private int mState;

        private SlotPreviewState(int state) {
            this.mState = state;
        }

        public boolean equal(SlotPreviewState other) {
            return this.mState == other.getState();
        }

        private int getState() {
            return this.mState;
        }
    }

    public void enterPreviewMode() {
        if (!inPreviewMode()) {
            this.mPreviewState = SlotPreviewState.ANIM;
            this.mPreviewViewScale = 0.0f;
            if (this.mListener != null) {
                this.mListener.onEnterPreviewMode();
            }
        }
    }

    public void leavePreviewMode() {
        if (inPreviewMode()) {
            this.mPreviewState = SlotPreviewState.INIT;
            this.mPreviewViewScale = 0.0f;
            if (this.mListener != null) {
                this.mListener.onLeavePreviewMode();
            }
        }
    }

    public boolean inPreviewMode() {
        return !SlotPreviewState.INIT.equal(this.mPreviewState);
    }

    public SlotPreviewState getPreviewState() {
        return this.mPreviewState;
    }

    public void setPreviewViewScale(float scale) {
        if (!SlotPreviewState.PREVIEW.equal(this.mPreviewState)) {
            this.mPreviewViewScale = Utils.clamp(scale, 0.0f, (float) WMElement.CAMERASIZEVALUE1B1);
            if (Float.compare(this.mPreviewViewScale, WMElement.CAMERASIZEVALUE1B1) == 0) {
                this.mPreviewState = SlotPreviewState.PREVIEW;
            }
        }
    }

    public float getPreviewViewScale() {
        if (SlotPreviewState.PREVIEW.equal(this.mPreviewState)) {
            return WMElement.CAMERASIZEVALUE1B1;
        }
        return this.mPreviewViewScale;
    }

    public FloatAnimation getSlotPreviewAnimation() {
        if (this.mSlotPreviewAnimation == null) {
            this.mSlotPreviewAnimation = new FloatAnimation(0.0f, WMElement.CAMERASIZEVALUE1B1, 100);
            this.mSlotPreviewAnimation.setInterpolator(new CubicBezierInterpolator(0.1f, 0.05f, 0.1f, WMElement.CAMERASIZEVALUE1B1));
            this.mSlotPreviewAnimation.setAnimationListener(new AnimationListener() {
                public void onAnimationEnd() {
                    SlotPreviewManager.this.setPreviewState(SlotPreviewState.SCALING);
                }
            });
        }
        return this.mSlotPreviewAnimation;
    }

    public void setSlotPreviewModeListener(SlotPreviewModeListener listener) {
        this.mListener = listener;
    }

    public void setPreviewState(SlotPreviewState state) {
        this.mPreviewState = state;
    }
}
