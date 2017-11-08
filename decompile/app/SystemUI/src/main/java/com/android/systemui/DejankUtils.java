package com.android.systemui;

import android.os.Handler;
import android.os.Looper;
import android.view.Choreographer;
import java.util.ArrayList;

public class DejankUtils {
    private static final Runnable sAnimationCallbackRunnable = new Runnable() {
        public void run() {
            for (int i = 0; i < DejankUtils.sPendingRunnables.size(); i++) {
                DejankUtils.sHandler.post((Runnable) DejankUtils.sPendingRunnables.get(i));
            }
            DejankUtils.sPendingRunnables.clear();
        }
    };
    private static final Choreographer sChoreographer = Choreographer.getInstance();
    private static final Handler sHandler = new Handler();
    private static final ArrayList<Runnable> sPendingRunnables = new ArrayList();

    public static void postAfterTraversal(Runnable r) {
        throwIfNotCalledOnMainThread();
        sPendingRunnables.add(r);
        postAnimationCallback();
    }

    public static void removeCallbacks(Runnable r) {
        throwIfNotCalledOnMainThread();
        sPendingRunnables.remove(r);
        sHandler.removeCallbacks(r);
    }

    private static void postAnimationCallback() {
        sChoreographer.postCallback(1, sAnimationCallbackRunnable, null);
    }

    private static void throwIfNotCalledOnMainThread() {
        if (!Looper.getMainLooper().isCurrentThread()) {
            throw new IllegalStateException("should be called from the main thread.");
        }
    }
}
