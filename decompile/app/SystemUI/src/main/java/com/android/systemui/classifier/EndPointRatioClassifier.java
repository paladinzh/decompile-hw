package com.android.systemui.classifier;

public class EndPointRatioClassifier extends StrokeClassifier {
    public EndPointRatioClassifier(ClassifierData classifierData) {
        this.mClassifierData = classifierData;
    }

    public String getTag() {
        return "END_RTIO";
    }

    public float getFalseTouchEvaluation(int type, Stroke stroke) {
        float ratio;
        if (stroke.getTotalLength() == 0.0f) {
            ratio = 1.0f;
        } else {
            ratio = stroke.getEndPointLength() / stroke.getTotalLength();
        }
        return EndPointRatioEvaluator.evaluate(ratio);
    }
}
