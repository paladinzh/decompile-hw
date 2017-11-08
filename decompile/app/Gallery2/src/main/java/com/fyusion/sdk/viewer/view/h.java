package com.fyusion.sdk.viewer.view;

import android.graphics.Matrix;

/* compiled from: Unknown */
public interface h {
    void a(float f, float f2);

    void a(float f, float f2, float f3);

    void cancelLongPress();

    void d();

    boolean e();

    int getDisplayRotation();

    i getTweeningRenderer();

    int getWidth();

    void setImageMatrixPending(Matrix matrix);
}
