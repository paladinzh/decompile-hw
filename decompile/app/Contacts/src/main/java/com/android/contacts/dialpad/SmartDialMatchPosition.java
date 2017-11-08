package com.android.contacts.dialpad;

import java.util.ArrayList;

public class SmartDialMatchPosition {
    private static final String TAG = SmartDialMatchPosition.class.getSimpleName();
    public int end;
    public int start;

    public SmartDialMatchPosition(int start, int end) {
        this.start = start;
        this.end = end;
    }

    private void advance(int toAdvance) {
        this.start += toAdvance;
        this.end += toAdvance;
    }

    public static void advanceMatchPositions(ArrayList<SmartDialMatchPosition> inList, int toAdvance) {
        for (int i = 0; i < inList.size(); i++) {
            ((SmartDialMatchPosition) inList.get(i)).advance(toAdvance);
        }
    }
}
