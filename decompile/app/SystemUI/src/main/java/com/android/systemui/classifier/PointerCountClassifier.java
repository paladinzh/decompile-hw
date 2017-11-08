package com.android.systemui.classifier;

import android.view.MotionEvent;

public class PointerCountClassifier extends GestureClassifier {
    private int mCount = 0;

    public PointerCountClassifier(ClassifierData classifierData) {
    }

    public String getTag() {
        return "PTR_CNT";
    }

    public void onTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();
        if (action == 0) {
            this.mCount = 1;
        }
        if (action == 5) {
            this.mCount++;
        }
    }

    public float getFalseTouchEvaluation(int type) {
        return PointerCountEvaluator.evaluate(this.mCount);
    }
}
