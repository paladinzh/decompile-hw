package com.android.settings;

import android.os.Bundle;
import android.support.v14.preference.PreferenceFragment;
import com.android.internal.logging.MetricsLogger;

public abstract class InstrumentedFragment extends PreferenceFragment {
    protected abstract int getMetricsCategory();

    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
    }

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
