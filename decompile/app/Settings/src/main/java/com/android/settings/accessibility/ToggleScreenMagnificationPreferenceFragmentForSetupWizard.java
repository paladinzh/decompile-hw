package com.android.settings.accessibility;

import android.os.Bundle;
import com.android.internal.logging.MetricsLogger;

public class ToggleScreenMagnificationPreferenceFragmentForSetupWizard extends ToggleScreenMagnificationPreferenceFragment {
    private boolean mToggleSwitchWasInitiallyChecked;

    protected void onProcessArguments(Bundle arguments) {
        super.onProcessArguments(arguments);
        this.mToggleSwitchWasInitiallyChecked = this.mToggleSwitch.isChecked();
    }

    protected int getMetricsCategory() {
        return 368;
    }

    public void onStop() {
        if (this.mToggleSwitch.isChecked() != this.mToggleSwitchWasInitiallyChecked) {
            MetricsLogger.action(getContext(), 368, this.mToggleSwitch.isChecked());
        }
        super.onStop();
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
