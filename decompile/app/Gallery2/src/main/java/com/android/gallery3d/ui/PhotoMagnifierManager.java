package com.android.gallery3d.ui;

import android.view.animation.Interpolator;
import com.android.gallery3d.anim.Animation.AnimationListener;
import com.android.gallery3d.anim.FloatAnimation;
import com.huawei.gallery.animation.CubicBezierInterpolator;
import com.huawei.watermark.manager.parse.WMElement;

public class PhotoMagnifierManager {
    private static PhotoMagnifierManager sManager;
    private MagnifierAnimation mAnimation;
    private boolean mEnableFlingAndScale = true;
    private AnimationListener mHideAnimListener = new AnimationListener() {
        public void onAnimationEnd() {
            if (PhotoMagnifierManager.this.mListener != null) {
                PhotoMagnifierManager.this.mListener.onHideAnimationEnd();
            }
        }
    };
    private Interpolator mInterpolator = new CubicBezierInterpolator(0.1f, 0.05f, 0.1f, WMElement.CAMERASIZEVALUE1B1);
    private PhotoMagnifierModeListener mListener;
    private PhotoMagnifierState mMagnifierState = PhotoMagnifierState.INIT;
    private AnimationListener mShowAnimListener = new AnimationListener() {
        public void onAnimationEnd() {
            PhotoMagnifierManager.this.mMagnifierState = PhotoMagnifierState.MAGNIFIER;
        }
    };

    public enum AnimationType {
        SHOW(0),
        HIDE(1);
        
        private int mType;

        private AnimationType(int type) {
            this.mType = type;
        }

        public boolean equal(AnimationType other) {
            return this.mType == other.getType();
        }

        private int getType() {
            return this.mType;
        }
    }

    public static class MagnifierAnimation extends FloatAnimation {
        private AnimationType mType;

        public MagnifierAnimation(float from, float to, int duration, AnimationType type) {
            super(from, to, duration);
            this.mType = type;
        }

        public AnimationType getType() {
            return this.mType;
        }
    }

    public interface PhotoMagnifierModeListener {
        void onEnterMagnifierMode();

        void onHideAnimationEnd();

        void onLeaveMagnifierMode();
    }

    public enum PhotoMagnifierState {
        INIT(0),
        ANIM(1),
        MAGNIFIER(2);
        
        private int mState;

        private PhotoMagnifierState(int state) {
            this.mState = state;
        }

        public boolean equal(PhotoMagnifierState other) {
            return this.mState == other.getState();
        }

        private int getState() {
            return this.mState;
        }
    }

    public static synchronized PhotoMagnifierManager getInstance() {
        PhotoMagnifierManager photoMagnifierManager;
        synchronized (PhotoMagnifierManager.class) {
            if (sManager == null) {
                sManager = new PhotoMagnifierManager();
            }
            photoMagnifierManager = sManager;
        }
        return photoMagnifierManager;
    }

    private PhotoMagnifierManager() {
    }

    public void enterMagnifierMode() {
        if (!inMagnifierMode()) {
            this.mMagnifierState = PhotoMagnifierState.ANIM;
            if (this.mListener != null) {
                this.mListener.onEnterMagnifierMode();
            }
        }
    }

    public void leaveMagnifierMode() {
        if (inMagnifierMode()) {
            this.mMagnifierState = PhotoMagnifierState.INIT;
            if (this.mListener != null) {
                this.mListener.onLeaveMagnifierMode();
            }
        }
    }

    public boolean inMagnifierMode() {
        return !PhotoMagnifierState.INIT.equal(this.mMagnifierState);
    }

    public void setMagnifieModeListener(PhotoMagnifierModeListener listener) {
        this.mListener = listener;
    }

    public void enableFlingAndScale(boolean enabled) {
        this.mEnableFlingAndScale = enabled;
    }

    public boolean isEnableFlingAndScale() {
        return this.mEnableFlingAndScale;
    }

    public boolean inAnimationState() {
        return PhotoMagnifierState.ANIM.equal(this.mMagnifierState);
    }

    public void setMagnifierState(PhotoMagnifierState state) {
        this.mMagnifierState = state;
    }

    public MagnifierAnimation getAnimation() {
        return this.mAnimation;
    }

    public MagnifierAnimation createAnimation(AnimationType type) {
        float fromValue = 0.0f;
        float toValue = WMElement.CAMERASIZEVALUE1B1;
        AnimationListener listener = this.mShowAnimListener;
        if (AnimationType.HIDE.equal(type)) {
            fromValue = WMElement.CAMERASIZEVALUE1B1;
            toValue = 0.0f;
            listener = this.mHideAnimListener;
        }
        MagnifierAnimation animation = new MagnifierAnimation(fromValue, toValue, 300, type);
        animation.setInterpolator(this.mInterpolator);
        animation.setAnimationListener(listener);
        this.mAnimation = animation;
        return this.mAnimation;
    }
}
