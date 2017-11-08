package com.android.settings.display;

import android.os.Bundle;
import com.android.internal.logging.MetricsLogger;

public class ScreenZoomPreferenceFragmentForSetupWizard extends ScreenZoomSettings {
    protected int getMetricsCategory() {
        return 370;
    }

    public void onStop() {
        if (this.mCurrentIndex != this.mInitialIndex) {
            MetricsLogger.action(getContext(), 370, this.mCurrentIndex);
        }
        super.onStop();
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().getWindow().addFlags(201326592);
        getActivity().getWindow().getDecorView().setSystemUiVisibility(4866);
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getActivity().getActionBar() != null) {
            getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }
}
