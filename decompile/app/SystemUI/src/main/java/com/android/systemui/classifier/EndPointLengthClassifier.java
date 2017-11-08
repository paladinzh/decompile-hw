package com.android.systemui.classifier;

public class EndPointLengthClassifier extends StrokeClassifier {
    public EndPointLengthClassifier(ClassifierData classifierData) {
    }

    public String getTag() {
        return "END_LNGTH";
    }

    public float getFalseTouchEvaluation(int type, Stroke stroke) {
        return EndPointLengthEvaluator.evaluate(stroke.getEndPointLength());
    }
}
