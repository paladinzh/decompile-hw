package com.android.settings.applications;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import com.android.settings.ChooseLockSettingsHelper;
import com.android.settings.SettingsActivity;
import com.android.settings.SettingsPreferenceFragment;

public class ConvertToFbe extends SettingsPreferenceFragment {
    private boolean runKeyguardConfirmation(int request) {
        return new ChooseLockSettingsHelper(getActivity(), this).launchConfirmationActivity(request, getActivity().getResources().getText(2131624194));
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(2130968693, null);
        ((Button) rootView.findViewById(2131886414)).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (!ConvertToFbe.this.runKeyguardConfirmation(55)) {
                    ConvertToFbe.this.convert();
                }
            }
        });
        return rootView;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 55 && resultCode == -1) {
            convert();
        }
    }

    private void convert() {
        ((SettingsActivity) getActivity()).startPreferencePanel(ConfirmConvertToFbe.class.getName(), null, 2131624194, null, null, 0);
    }

    protected int getMetricsCategory() {
        return 402;
    }
}
