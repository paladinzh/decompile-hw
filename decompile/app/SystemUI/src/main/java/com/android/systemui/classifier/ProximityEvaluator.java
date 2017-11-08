package com.android.systemui.classifier;

public class ProximityEvaluator {
    public static float evaluate(float value, int type) {
        float threshold = 0.1f;
        if (type == 0) {
            threshold = 1.0f;
        }
        if (value >= threshold) {
            return (float) 2.0d;
        }
        return 0.0f;
    }
}
