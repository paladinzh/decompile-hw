package com.huawei.systemmanager.mainscreen.view;

import android.graphics.Canvas;

public interface DrawStatus {

    public static abstract class SimpleDrawStatus implements DrawStatus {
        public void onDrawBegin() {
        }

        public boolean doDraw(Canvas canvas) {
            return false;
        }

        public void setProgress(float progress) {
        }

        public void setScore(int score) {
        }

        public void setScoreNew(int score) {
        }
    }

    boolean doDraw(Canvas canvas);

    String getStateName();

    void onDrawBegin();

    void setProgress(float f);

    void setScore(int i);

    void setScoreNew(int i);
}
