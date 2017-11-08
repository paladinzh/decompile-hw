package com.android.settings;

import android.support.v14.preference.PreferenceFragment;
import com.android.internal.logging.MetricsLogger;

public abstract class InstrumentedPreferenceFragment extends PreferenceFragment {
    protected abstract int getMetricsCategory();

    public void onResume() {
        super.onResume();
        if (getMetricsCategory() != 100000) {
            MetricsLogger.visible(getActivity(), getMetricsCategory());
        }
    }

    public void onPause() {
        super.onPause();
        if (getMetricsCategory() != 100000) {
            MetricsLogger.hidden(getActivity(), getMetricsCategory());
        }
    }
}
