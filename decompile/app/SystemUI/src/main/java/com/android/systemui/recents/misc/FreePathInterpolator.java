package com.android.systemui.recents.misc;

import android.graphics.Path;
import android.view.animation.BaseInterpolator;

public class FreePathInterpolator extends BaseInterpolator {
    private float mArcLength;
    private float[] mX;
    private float[] mY;

    public FreePathInterpolator(Path path) {
        initPath(path);
    }

    private void initPath(Path path) {
        float[] pointComponents = path.approximate(0.002f);
        int numPoints = pointComponents.length / 3;
        this.mX = new float[numPoints];
        this.mY = new float[numPoints];
        this.mArcLength = 0.0f;
        float prevX = 0.0f;
        float prevY = 0.0f;
        float prevFraction = 0.0f;
        int i = 0;
        int componentIndex = 0;
        while (i < numPoints) {
            int componentIndex2 = componentIndex + 1;
            float fraction = pointComponents[componentIndex];
            componentIndex = componentIndex2 + 1;
            float x = pointComponents[componentIndex2];
            componentIndex2 = componentIndex + 1;
            float y = pointComponents[componentIndex];
            if (fraction == prevFraction && x != prevX) {
                throw new IllegalArgumentException("The Path cannot have discontinuity in the X axis.");
            } else if (x < prevX) {
                throw new IllegalArgumentException("The Path cannot loop back on itself.");
            } else {
                this.mX[i] = x;
                this.mY[i] = y;
                this.mArcLength = (float) (((double) this.mArcLength) + Math.hypot((double) (x - prevX), (double) (y - prevY)));
                prevX = x;
                prevY = y;
                prevFraction = fraction;
                i++;
                componentIndex = componentIndex2;
            }
        }
    }

    public float getInterpolation(float t) {
        int startIndex = 0;
        int endIndex = this.mX.length - 1;
        if (t <= 0.0f) {
            return this.mY[0];
        }
        if (t >= 1.0f) {
            return this.mY[endIndex];
        }
        while (endIndex - startIndex > 1) {
            int midIndex = (startIndex + endIndex) / 2;
            if (t < this.mX[midIndex]) {
                endIndex = midIndex;
            } else {
                startIndex = midIndex;
            }
        }
        float xRange = this.mX[endIndex] - this.mX[startIndex];
        if (xRange == 0.0f) {
            return this.mY[startIndex];
        }
        float fraction = (t - this.mX[startIndex]) / xRange;
        float startY = this.mY[startIndex];
        return ((this.mY[endIndex] - startY) * fraction) + startY;
    }

    public float getX(float y) {
        int startIndex = 0;
        int endIndex = this.mY.length - 1;
        if (y <= 0.0f) {
            return this.mX[endIndex];
        }
        if (y >= 1.0f) {
            return this.mX[0];
        }
        while (endIndex - startIndex > 1) {
            int midIndex = (startIndex + endIndex) / 2;
            if (y < this.mY[midIndex]) {
                startIndex = midIndex;
            } else {
                endIndex = midIndex;
            }
        }
        float yRange = this.mY[endIndex] - this.mY[startIndex];
        if (yRange == 0.0f) {
            return this.mX[startIndex];
        }
        float fraction = (y - this.mY[startIndex]) / yRange;
        float startX = this.mX[startIndex];
        return ((this.mX[endIndex] - startX) * fraction) + startX;
    }

    public float getArcLength() {
        return this.mArcLength;
    }
}
