package com.android.systemui.classifier;

public class DistanceRatioEvaluator {
    public static float evaluate(float value) {
        float evaluation = 0.0f;
        if (((double) value) <= 1.0d) {
            evaluation = 1.0f;
        }
        if (((double) value) <= 0.5d) {
            evaluation += 1.0f;
        }
        if (((double) value) > 4.0d) {
            evaluation += 1.0f;
        }
        if (((double) value) > 7.0d) {
            evaluation += 1.0f;
        }
        if (((double) value) > 14.0d) {
            return evaluation + 1.0f;
        }
        return evaluation;
    }
}
