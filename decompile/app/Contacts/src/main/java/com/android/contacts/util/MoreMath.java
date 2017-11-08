package com.android.contacts.util;

public class MoreMath {
    public static float clamp(float input, float lowerBound, float upperBound) {
        if (input < lowerBound) {
            return lowerBound;
        }
        if (input > upperBound) {
            return upperBound;
        }
        return input;
    }
}
