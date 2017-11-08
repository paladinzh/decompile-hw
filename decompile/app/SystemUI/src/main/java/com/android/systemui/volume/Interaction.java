package com.android.systemui.volume;

import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnGenericMotionListener;
import android.view.View.OnTouchListener;

public class Interaction {

    public interface Callback {
        void onInteraction();
    }

    public static void register(View v, final Callback callback) {
        v.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                callback.onInteraction();
                return false;
            }
        });
        v.setOnGenericMotionListener(new OnGenericMotionListener() {
            public boolean onGenericMotion(View v, MotionEvent event) {
                callback.onInteraction();
                return false;
            }
        });
    }
}
