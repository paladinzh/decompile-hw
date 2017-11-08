package com.android.gallery3d.anim;

import android.graphics.Matrix;
import android.graphics.Rect;
import com.huawei.watermark.manager.parse.WMElement;

public abstract class BaseTransition {
    boolean mAlphaMode = true;
    String mAnimationType = null;
    int mBreakTimes = 1;
    int mOrientation = 0;
    protected Rect mRectOld = null;
    public TransformationInfo mTransformationInfo = new TransformationInfo();

    public static class TransformationInfo {
        public float mAlpha = WMElement.CAMERASIZEVALUE1B1;
        public boolean mAlphaDirty = false;
        public final Rect mBounds = new Rect();
        public final Matrix mMatrix = new Matrix();
        public float[] mMatrix3D = new float[16];
        public boolean mMatrixDirty = false;
        public float mPivotX = 0.0f;
        public float mPivotY = 0.0f;
        public float mPivotZ = 0.0f;
        public float mRotation = 0.0f;
        public float mRotationX = 0.0f;
        public float mRotationY = 0.0f;
        public float mScaleX = WMElement.CAMERASIZEVALUE1B1;
        public float mScaleY = WMElement.CAMERASIZEVALUE1B1;
        public float mScaleZ = WMElement.CAMERASIZEVALUE1B1;
        public float mTranslationX = 0.0f;
        public float mTranslationY = 0.0f;
        public float mTranslationZ = 0.0f;

        private void clearDirty() {
            this.mAlphaDirty = false;
            this.mAlpha = WMElement.CAMERASIZEVALUE1B1;
            this.mMatrixDirty = false;
            clearMatrix();
        }

        private void clearMatrix() {
            this.mRotation = 0.0f;
            this.mRotationX = 0.0f;
            this.mRotationY = 0.0f;
            this.mScaleX = WMElement.CAMERASIZEVALUE1B1;
            this.mScaleY = WMElement.CAMERASIZEVALUE1B1;
            this.mScaleZ = WMElement.CAMERASIZEVALUE1B1;
            this.mTranslationX = 0.0f;
            this.mTranslationY = 0.0f;
            this.mTranslationZ = 0.0f;
        }
    }

    public TransformationInfo getTransformation3D(float scrollProgress, Rect targetRt) {
        this.mTransformationInfo.clearDirty();
        if (!transform(scrollProgress, targetRt)) {
            return null;
        }
        updateMatrix3D();
        if (!this.mAlphaMode) {
            this.mTransformationInfo.mAlphaDirty = false;
        }
        return this.mTransformationInfo;
    }

    public void setOldRect(Rect rt) {
        this.mRectOld = rt;
    }

    private void updateMatrix3D() {
        TransformationInfo info = this.mTransformationInfo;
        if (info.mMatrixDirty) {
            android.opengl.Matrix.setIdentityM(info.mMatrix3D, 0);
            android.opengl.Matrix.translateM(info.mMatrix3D, 0, info.mPivotX + info.mTranslationX, info.mPivotY + info.mTranslationY, info.mPivotZ + info.mTranslationZ);
            if (info.mRotation != 0.0f) {
                android.opengl.Matrix.rotateM(info.mMatrix3D, 0, info.mRotation, 0.0f, 0.0f, WMElement.CAMERASIZEVALUE1B1);
            } else if (info.mRotationX != 0.0f) {
                android.opengl.Matrix.rotateM(info.mMatrix3D, 0, info.mRotationX, WMElement.CAMERASIZEVALUE1B1, 0.0f, 0.0f);
            } else if (info.mRotationY != 0.0f) {
                android.opengl.Matrix.rotateM(info.mMatrix3D, 0, info.mRotationY, 0.0f, WMElement.CAMERASIZEVALUE1B1, 0.0f);
            }
            android.opengl.Matrix.translateM(info.mMatrix3D, 0, -info.mPivotX, -info.mPivotY, -info.mPivotZ);
            android.opengl.Matrix.scaleM(info.mMatrix3D, 0, info.mScaleX, info.mScaleY, info.mScaleZ);
        }
    }

    public boolean transform(float scrollProgress, Rect targetRt) {
        return false;
    }
}
