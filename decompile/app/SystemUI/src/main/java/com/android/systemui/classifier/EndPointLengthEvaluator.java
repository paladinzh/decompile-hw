package com.android.systemui.classifier;

public class EndPointLengthEvaluator {
    public static float evaluate(float value) {
        float evaluation = 0.0f;
        if (((double) value) < 0.05d) {
            evaluation = (float) 4611686018427387904;
        }
        if (((double) value) < 0.1d) {
            evaluation = (float) (((double) evaluation) + 2.0d);
        }
        if (((double) value) < 0.2d) {
            evaluation = (float) (((double) evaluation) + 2.0d);
        }
        if (((double) value) < 0.3d) {
            evaluation = (float) (((double) evaluation) + 2.0d);
        }
        if (((double) value) < 0.4d) {
            evaluation = (float) (((double) evaluation) + 2.0d);
        }
        if (((double) value) < 0.5d) {
            return (float) (((double) evaluation) + 2.0d);
        }
        return evaluation;
    }
}
