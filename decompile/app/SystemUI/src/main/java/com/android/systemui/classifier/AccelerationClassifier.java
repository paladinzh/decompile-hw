package com.android.systemui.classifier;

import android.view.MotionEvent;
import com.android.systemui.utils.HwLog;
import java.util.HashMap;

public class AccelerationClassifier extends StrokeClassifier {
    private final HashMap<Stroke, Data> mStrokeMap = new HashMap();

    private static class Data {
        public float maxDistanceRatio = 0.0f;
        public float maxSpeedRatio = 0.0f;
        public float previousDistance = 0.0f;
        public Point previousPoint;
        public float previousSpeed = 0.0f;

        public Data(Point point) {
            this.previousPoint = point;
        }

        public void addPoint(Point point) {
            float distance = this.previousPoint.dist(point);
            float speed = distance / ((float) ((point.timeOffsetNano - this.previousPoint.timeOffsetNano) + 1));
            if (this.previousDistance != 0.0f) {
                this.maxDistanceRatio = Math.max(this.maxDistanceRatio, distance / this.previousDistance);
            }
            if (this.previousSpeed != 0.0f) {
                this.maxSpeedRatio = Math.max(this.maxSpeedRatio, speed / this.previousSpeed);
            }
            this.previousDistance = distance;
            this.previousSpeed = speed;
            this.previousPoint = point;
        }
    }

    public AccelerationClassifier(ClassifierData classifierData) {
        this.mClassifierData = classifierData;
    }

    public String getTag() {
        return "ACC";
    }

    public void onTouchEvent(MotionEvent event) {
        int i;
        Stroke stroke;
        Point point;
        int action = event.getActionMasked();
        if (!(action == 0 || action == 1 || action == 6)) {
            if (action == 3) {
            }
            for (i = 0; i < event.getPointerCount(); i++) {
                stroke = this.mClassifierData.getStroke(event.getPointerId(i));
                point = (Point) stroke.getPoints().get(stroke.getPoints().size() - 1);
                if (this.mStrokeMap.get(stroke) != null) {
                    this.mStrokeMap.put(stroke, new Data(point));
                } else {
                    ((Data) this.mStrokeMap.get(stroke)).addPoint(point);
                }
            }
        }
        HwLog.i(getTag(), "AccelerationClassifier clear onTouchEvent=" + action);
        this.mStrokeMap.clear();
        for (i = 0; i < event.getPointerCount(); i++) {
            stroke = this.mClassifierData.getStroke(event.getPointerId(i));
            point = (Point) stroke.getPoints().get(stroke.getPoints().size() - 1);
            if (this.mStrokeMap.get(stroke) != null) {
                ((Data) this.mStrokeMap.get(stroke)).addPoint(point);
            } else {
                this.mStrokeMap.put(stroke, new Data(point));
            }
        }
    }

    public float getFalseTouchEvaluation(int type, Stroke stroke) {
        Data data = (Data) this.mStrokeMap.get(stroke);
        return SpeedRatioEvaluator.evaluate(data.maxSpeedRatio) + DistanceRatioEvaluator.evaluate(data.maxDistanceRatio);
    }
}
