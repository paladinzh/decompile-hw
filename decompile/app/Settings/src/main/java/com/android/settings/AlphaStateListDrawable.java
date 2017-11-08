package com.android.settings;

import android.graphics.drawable.StateListDrawable;

public class AlphaStateListDrawable extends StateListDrawable {
    protected boolean onStateChange(int[] states) {
        boolean found = false;
        for (int state : states) {
            if (state == 16842919) {
                setAlpha(128);
            } else if (state == 16842910) {
                found = true;
                setAlpha(255);
            }
        }
        if (!found && states.length > 0) {
            setAlpha(77);
        }
        return super.onStateChange(states);
    }
}
