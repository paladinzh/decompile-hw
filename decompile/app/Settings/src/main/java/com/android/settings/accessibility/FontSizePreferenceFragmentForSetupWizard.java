package com.android.settings.accessibility;

import android.os.Bundle;
import com.android.internal.logging.MetricsLogger;
import com.android.settings.Utils;

public class FontSizePreferenceFragmentForSetupWizard extends ToggleFontSizePreferenceFragment {
    protected int getMetricsCategory() {
        return 369;
    }

    public void onStop() {
        if (this.mCurrentIndex != this.mInitialIndex) {
            MetricsLogger.action(getContext(), 369, this.mCurrentIndex);
        }
        super.onStop();
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!Utils.isTablet()) {
            getActivity().setRequestedOrientation(1);
        }
        getActivity().getWindow().addFlags(67108864);
        getActivity().getWindow().getDecorView().setSystemUiVisibility(4352);
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getActivity().getActionBar() != null) {
            getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    protected int getHelpResource() {
        return 0;
    }
}
