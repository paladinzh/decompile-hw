package com.android.systemui.classifier;

public class DurationCountEvaluator {
    public static float evaluate(float value) {
        float evaluation = 0.0f;
        if (((double) value) < 0.0105d) {
            evaluation = 1.0f;
        }
        if (((double) value) < 0.00909d) {
            evaluation += 1.0f;
        }
        if (((double) value) < 0.00667d) {
            evaluation += 1.0f;
        }
        if (((double) value) > 0.0333d) {
            evaluation += 1.0f;
        }
        if (((double) value) > 0.05d) {
            return evaluation + 1.0f;
        }
        return evaluation;
    }
}
