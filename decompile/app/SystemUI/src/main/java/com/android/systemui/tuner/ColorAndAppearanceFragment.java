package com.android.systemui.tuner;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.support.v14.preference.PreferenceFragment;
import android.support.v7.preference.Preference;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import com.android.internal.logging.MetricsLogger;
import com.android.systemui.R;
import com.android.systemui.statusbar.policy.NightModeController;

public class ColorAndAppearanceFragment extends PreferenceFragment {
    private static final CharSequence KEY_NIGHT_MODE = "night_mode";
    private NightModeController mNightModeController;
    private final Runnable mResetColorMatrix = new Runnable() {
        public void run() {
            ((DialogFragment) ColorAndAppearanceFragment.this.getFragmentManager().findFragmentByTag("RevertWarning")).dismiss();
            Secure.putString(ColorAndAppearanceFragment.this.getContext().getContentResolver(), "accessibility_display_color_matrix", null);
        }
    };

    public static class CalibrateDialog extends DialogFragment implements OnClickListener {
        private NightModeController mNightModeController;
        private float[] mValues;

        public static void show(ColorAndAppearanceFragment fragment) {
            CalibrateDialog dialog = new CalibrateDialog();
            dialog.setTargetFragment(fragment, 0);
            dialog.show(fragment.getFragmentManager(), "Calibrate");
        }

        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            this.mNightModeController = new NightModeController(getContext());
            String customValues = this.mNightModeController.getCustomValues();
            if (customValues == null) {
                customValues = NightModeController.toString(NightModeController.IDENTITY_MATRIX);
            }
            this.mValues = NightModeController.toValues(customValues);
        }

        public Dialog onCreateDialog(Bundle savedInstanceState) {
            View v = LayoutInflater.from(getContext()).inflate(R.layout.calibrate_sliders, null);
            bindView(v.findViewById(R.id.r_group), 0);
            bindView(v.findViewById(R.id.g_group), 5);
            bindView(v.findViewById(R.id.b_group), 10);
            MetricsLogger.visible(getContext(), 305);
            return new Builder(getContext()).setTitle(R.string.calibrate_display).setView(v).setPositiveButton(R.string.color_apply, this).setNegativeButton(17039360, null).create();
        }

        public void onDismiss(DialogInterface dialog) {
            super.onDismiss(dialog);
            MetricsLogger.hidden(getContext(), 305);
        }

        private void bindView(View view, final int index) {
            SeekBar seekBar = (SeekBar) view.findViewById(16909257);
            seekBar.setMax(1000);
            seekBar.setProgress((int) (this.mValues[index] * 1000.0f));
            seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    CalibrateDialog.this.mValues[index] = ((float) progress) / 1000.0f;
                }

                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                public void onStopTrackingTouch(SeekBar seekBar) {
                }
            });
        }

        public void onClick(DialogInterface dialog, int which) {
            if (this.mValues[0] == 1.0f && this.mValues[5] == 1.0f && this.mValues[10] == 1.0f) {
                this.mNightModeController.setCustomValues(null);
                return;
            }
            ((ColorAndAppearanceFragment) getTargetFragment()).startRevertTimer();
            Secure.putString(getContext().getContentResolver(), "accessibility_display_color_matrix", NightModeController.toString(this.mValues));
            RevertWarning.show((ColorAndAppearanceFragment) getTargetFragment());
        }
    }

    public static class RevertWarning extends DialogFragment implements OnClickListener {
        public static void show(ColorAndAppearanceFragment fragment) {
            RevertWarning warning = new RevertWarning();
            warning.setTargetFragment(fragment, 0);
            warning.show(fragment.getFragmentManager(), "RevertWarning");
        }

        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog alertDialog = new Builder(getContext()).setTitle(R.string.color_revert_title).setMessage(R.string.color_revert_message).setPositiveButton(R.string.ok, this).create();
            alertDialog.setCanceledOnTouchOutside(true);
            return alertDialog;
        }

        public void onCancel(DialogInterface dialog) {
            super.onCancel(dialog);
            ((ColorAndAppearanceFragment) getTargetFragment()).onRevert();
        }

        public void onClick(DialogInterface dialog, int which) {
            ((ColorAndAppearanceFragment) getTargetFragment()).onApply();
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mNightModeController = new NightModeController(getContext());
    }

    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.color_and_appearance);
    }

    public void onResume() {
        super.onResume();
        MetricsLogger.visibility(getContext(), 306, true);
        getActivity().setTitle(R.string.color_and_appearance);
        findPreference(KEY_NIGHT_MODE).setSummary(this.mNightModeController.isEnabled() ? R.string.night_mode_on : R.string.night_mode_off);
    }

    public void onPause() {
        super.onPause();
        MetricsLogger.visibility(getContext(), 306, false);
    }

    public void onDisplayPreferenceDialog(Preference preference) {
        if (preference instanceof CalibratePreference) {
            CalibrateDialog.show(this);
        } else {
            super.onDisplayPreferenceDialog(preference);
        }
    }

    private void startRevertTimer() {
        getView().postDelayed(this.mResetColorMatrix, 10000);
    }

    private void onApply() {
        MetricsLogger.action(getContext(), 307);
        this.mNightModeController.setCustomValues(Secure.getString(getContext().getContentResolver(), "accessibility_display_color_matrix"));
        getView().removeCallbacks(this.mResetColorMatrix);
    }

    private void onRevert() {
        getView().removeCallbacks(this.mResetColorMatrix);
        this.mResetColorMatrix.run();
    }
}
