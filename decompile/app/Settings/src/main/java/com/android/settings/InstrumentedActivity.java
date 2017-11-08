package com.android.settings;

import android.app.Activity;
import com.android.internal.logging.MetricsLogger;

public abstract class InstrumentedActivity extends Activity {
    protected abstract int getMetricsCategory();

    public void onResume() {
        super.onResume();
        MetricsLogger.visible(this, getMetricsCategory());
    }

    public void onPause() {
        super.onPause();
        MetricsLogger.hidden(this, getMetricsCategory());
    }
}
