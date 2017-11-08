package com.android.systemui.tuner;

import android.app.Fragment;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;
import com.android.internal.logging.MetricsLogger;
import com.android.systemui.R;

public class PowerNotificationControlsFragment extends Fragment {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.power_notification_controls_settings, container, false);
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        CharSequence string;
        super.onViewCreated(view, savedInstanceState);
        View switchBar = view.findViewById(R.id.switch_bar);
        final Switch switchWidget = (Switch) switchBar.findViewById(16908352);
        final TextView switchText = (TextView) switchBar.findViewById(R.id.switch_text);
        switchWidget.setChecked(isEnabled());
        if (isEnabled()) {
            string = getString(R.string.switch_bar_on);
        } else {
            string = getString(R.string.switch_bar_off);
        }
        switchText.setText(string);
        switchWidget.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                CharSequence string;
                boolean newState = !PowerNotificationControlsFragment.this.isEnabled();
                MetricsLogger.action(PowerNotificationControlsFragment.this.getContext(), 393, newState);
                Secure.putInt(PowerNotificationControlsFragment.this.getContext().getContentResolver(), "show_importance_slider", newState ? 1 : 0);
                switchWidget.setChecked(newState);
                TextView textView = switchText;
                if (newState) {
                    string = PowerNotificationControlsFragment.this.getString(R.string.switch_bar_on);
                } else {
                    string = PowerNotificationControlsFragment.this.getString(R.string.switch_bar_off);
                }
                textView.setText(string);
            }
        });
    }

    public void onResume() {
        super.onResume();
        MetricsLogger.visibility(getContext(), 392, true);
    }

    public void onPause() {
        super.onPause();
        MetricsLogger.visibility(getContext(), 392, false);
    }

    private boolean isEnabled() {
        if (Secure.getInt(getContext().getContentResolver(), "show_importance_slider", 0) == 1) {
            return true;
        }
        return false;
    }
}
