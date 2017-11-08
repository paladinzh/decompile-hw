package com.android.systemui.classifier;

public class DurationCountClassifier extends StrokeClassifier {
    public DurationCountClassifier(ClassifierData classifierData) {
    }

    public String getTag() {
        return "DUR";
    }

    public float getFalseTouchEvaluation(int type, Stroke stroke) {
        return DurationCountEvaluator.evaluate(stroke.getDurationSeconds() / ((float) stroke.getCount()));
    }
}
