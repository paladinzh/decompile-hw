package com.android.systemui.classifier;

import android.hardware.SensorEvent;
import android.view.MotionEvent;

public class ProximityClassifier extends GestureClassifier {
    private float mAverageNear;
    private long mGestureStartTimeNano;
    private boolean mNear;
    private long mNearDuration;
    private long mNearStartTimeNano;

    public ProximityClassifier(ClassifierData classifierData) {
    }

    public String getTag() {
        return "PROX";
    }

    public void onSensorChanged(SensorEvent event) {
        boolean z = false;
        if (event.sensor.getType() == 8) {
            if (event.values[0] < event.sensor.getMaximumRange()) {
                z = true;
            }
            update(z, event.timestamp);
        }
    }

    public void onTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();
        if (action == 0) {
            this.mGestureStartTimeNano = event.getEventTimeNano();
            this.mNearStartTimeNano = event.getEventTimeNano();
            this.mNearDuration = 0;
        }
        if (action == 1 || action == 3) {
            update(this.mNear, event.getEventTimeNano());
            long duration = event.getEventTimeNano() - this.mGestureStartTimeNano;
            if (duration == 0) {
                float f;
                if (this.mNear) {
                    f = 1.0f;
                } else {
                    f = 0.0f;
                }
                this.mAverageNear = f;
                return;
            }
            this.mAverageNear = ((float) this.mNearDuration) / ((float) duration);
        }
    }

    private void update(boolean near, long timestampNano) {
        if (timestampNano > this.mNearStartTimeNano) {
            if (this.mNear) {
                this.mNearDuration += timestampNano - this.mNearStartTimeNano;
            }
            if (near) {
                this.mNearStartTimeNano = timestampNano;
            }
        }
        this.mNear = near;
    }

    public float getFalseTouchEvaluation(int type) {
        return ProximityEvaluator.evaluate(this.mAverageNear, type);
    }
}
