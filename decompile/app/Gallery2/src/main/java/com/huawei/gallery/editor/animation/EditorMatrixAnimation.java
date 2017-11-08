package com.huawei.gallery.editor.animation;

import android.graphics.Matrix;
import android.view.animation.AccelerateInterpolator;
import com.android.gallery3d.anim.Animation;

public class EditorMatrixAnimation extends Animation {
    private float mProgress;
    private Matrix mSourceMatrix;
    private Matrix mTargetMatrix;

    public EditorMatrixAnimation() {
        setInterpolator(new AccelerateInterpolator(2.0f));
        setDuration(300);
    }

    public void init(Matrix source, Matrix target) {
        this.mSourceMatrix = source;
        this.mTargetMatrix = target;
    }

    public Matrix getMatrix() {
        float[] valuesSource = new float[9];
        this.mSourceMatrix.getValues(valuesSource);
        float[] valuesTarget = new float[9];
        this.mTargetMatrix.getValues(valuesTarget);
        for (int i = 0; i < Math.min(valuesSource.length, valuesTarget.length); i++) {
            valuesSource[i] = valuesSource[i] + ((valuesTarget[i] - valuesSource[i]) * this.mProgress);
        }
        Matrix matrix = new Matrix();
        matrix.setValues(valuesSource);
        return matrix;
    }

    protected void onCalculate(float progress) {
        this.mProgress = progress;
    }
}
