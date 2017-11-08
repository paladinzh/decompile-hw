package com.android.systemui.classifier;

public class DirectionClassifier extends StrokeClassifier {
    public DirectionClassifier(ClassifierData classifierData) {
    }

    public String getTag() {
        return "DIR";
    }

    public float getFalseTouchEvaluation(int type, Stroke stroke) {
        Point firstPoint = (Point) stroke.getPoints().get(0);
        Point lastPoint = (Point) stroke.getPoints().get(stroke.getPoints().size() - 1);
        return DirectionEvaluator.evaluate(lastPoint.x - firstPoint.x, lastPoint.y - firstPoint.y, type);
    }
}
