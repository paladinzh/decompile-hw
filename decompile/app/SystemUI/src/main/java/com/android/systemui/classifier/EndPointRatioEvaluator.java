package com.android.systemui.classifier;

public class EndPointRatioEvaluator {
    public static float evaluate(float value) {
        float evaluation = 0.0f;
        if (((double) value) < 0.85d) {
            evaluation = 1.0f;
        }
        if (((double) value) < 0.75d) {
            evaluation += 1.0f;
        }
        if (((double) value) < 0.65d) {
            evaluation += 1.0f;
        }
        if (((double) value) < 0.55d) {
            evaluation += 1.0f;
        }
        if (((double) value) < 0.45d) {
            evaluation += 1.0f;
        }
        if (((double) value) < 0.35d) {
            return evaluation + 1.0f;
        }
        return evaluation;
    }
}
