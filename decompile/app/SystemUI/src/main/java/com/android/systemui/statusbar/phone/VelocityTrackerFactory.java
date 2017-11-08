package com.android.systemui.statusbar.phone;

import android.content.Context;
import com.android.systemui.R;

public class VelocityTrackerFactory {
    public static VelocityTrackerInterface obtain(Context ctx) {
        String tracker = ctx.getResources().getString(R.string.velocity_tracker_impl);
        if (tracker.equals("noisy")) {
            return NoisyVelocityTracker.obtain();
        }
        if (tracker.equals("platform")) {
            return PlatformVelocityTracker.obtain();
        }
        throw new IllegalStateException("Invalid tracker: " + tracker);
    }
}
