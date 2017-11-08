package com.android.systemui.classifier;

public class DirectionEvaluator {
    public static float evaluate(float xDiff, float yDiff, int type) {
        boolean vertical = Math.abs(yDiff) >= Math.abs(xDiff);
        switch (type) {
            case 0:
            case 2:
                if (!vertical || ((double) yDiff) <= 0.0d) {
                    return 5.5f;
                }
            case 1:
                if (vertical) {
                    return 5.5f;
                }
                break;
            case 4:
                if (!vertical || ((double) yDiff) >= 0.0d) {
                    return 5.5f;
                }
            case 5:
                if (((double) xDiff) < 0.0d && ((double) yDiff) > 0.0d) {
                    return 5.5f;
                }
            case 6:
                if (((double) xDiff) > 0.0d && ((double) yDiff) > 0.0d) {
                    return 5.5f;
                }
        }
        return 0.0f;
    }
}
