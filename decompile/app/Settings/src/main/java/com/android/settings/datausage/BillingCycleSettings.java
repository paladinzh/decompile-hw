package com.android.settings.datausage;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.net.NetworkPolicy;
import android.net.NetworkTemplate;
import android.os.Bundle;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.text.format.Formatter;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Spinner;
import com.android.settingslib.NetworkPolicyEditor;
import com.android.settingslib.net.DataUsageController;

public class BillingCycleSettings extends DataUsageBase implements OnPreferenceChangeListener {
    private Preference mBillingCycle;
    private Preference mDataLimit;
    private DataUsageController mDataUsageController;
    private Preference mDataWarning;
    private SwitchPreference mEnableDataLimit;
    private NetworkTemplate mNetworkTemplate;

    public static class BytesEditorFragment extends DialogFragment implements OnClickListener {
        private View mView;

        public static void show(BillingCycleSettings parent, boolean isLimit) {
            if (parent.isAdded()) {
                Bundle args = new Bundle();
                args.putParcelable("template", parent.mNetworkTemplate);
                args.putBoolean("limit", isLimit);
                BytesEditorFragment dialog = new BytesEditorFragment();
                dialog.setArguments(args);
                dialog.setTargetFragment(parent, 0);
                dialog.show(parent.getFragmentManager(), "warningEditor");
            }
        }

        public Dialog onCreateDialog(Bundle savedInstanceState) {
            int i;
            Context context = getActivity();
            LayoutInflater dialogInflater = LayoutInflater.from(context);
            boolean isLimit = getArguments().getBoolean("limit");
            this.mView = dialogInflater.inflate(2130968719, null, false);
            setupPicker((EditText) this.mView.findViewById(2131886447), (Spinner) this.mView.findViewById(2131886448));
            Builder builder = new Builder(context);
            if (isLimit) {
                i = 2131626339;
            } else {
                i = 2131626338;
            }
            return builder.setTitle(i).setView(this.mView).setPositiveButton(2131626337, this).create();
        }

        private void setupPicker(EditText bytesPicker, Spinner type) {
            long bytes;
            NetworkPolicyEditor editor = ((BillingCycleSettings) getTargetFragment()).services.mPolicyEditor;
            NetworkTemplate template = (NetworkTemplate) getArguments().getParcelable("template");
            boolean isLimit = getArguments().getBoolean("limit");
            if (isLimit) {
                bytes = editor.getPolicyLimitBytes(template);
            } else {
                bytes = editor.getPolicyWarningBytes(template);
            }
            if (isLimit) {
            }
            if (((float) bytes) > 1.61061274E9f) {
                bytesPicker.setText(formatText(((float) bytes) / 1.07374182E9f));
                type.setSelection(1);
                return;
            }
            bytesPicker.setText(formatText(((float) bytes) / 1048576.0f));
            type.setSelection(0);
        }

        private String formatText(float v) {
            return String.valueOf(((float) Math.round(v * 100.0f)) / 100.0f);
        }

        public void onClick(DialogInterface dialog, int which) {
            if (which == -1) {
                BillingCycleSettings target = (BillingCycleSettings) getTargetFragment();
                NetworkPolicyEditor editor = target.services.mPolicyEditor;
                NetworkTemplate template = (NetworkTemplate) getArguments().getParcelable("template");
                boolean isLimit = getArguments().getBoolean("limit");
                Spinner spinner = (Spinner) this.mView.findViewById(2131886448);
                String bytesString = ((EditText) this.mView.findViewById(2131886447)).getText().toString();
                if (bytesString.isEmpty()) {
                    bytesString = "0";
                }
                long bytes = (long) (Float.valueOf(bytesString).floatValue() * ((float) (spinner.getSelectedItemPosition() == 0 ? 1048576 : 1073741824)));
                if (isLimit) {
                    editor.setPolicyLimitBytes(template, bytes);
                } else {
                    editor.setPolicyWarningBytes(template, bytes);
                }
                target.updatePrefs();
            }
        }
    }

    public static class ConfirmLimitFragment extends DialogFragment implements OnClickListener {
        public static void show(BillingCycleSettings parent) {
            if (parent.isAdded()) {
                NetworkPolicy policy = parent.services.mPolicyEditor.getPolicy(parent.mNetworkTemplate);
                if (policy != null) {
                    long minLimitBytes = (long) (((float) policy.warningBytes) * 1.2f);
                    CharSequence message = parent.getResources().getString(2131626341);
                    long limitBytes = Math.max(5368709120L, minLimitBytes);
                    Bundle args = new Bundle();
                    args.putCharSequence("message", message);
                    args.putLong("limitBytes", limitBytes);
                    ConfirmLimitFragment dialog = new ConfirmLimitFragment();
                    dialog.setArguments(args);
                    dialog.setTargetFragment(parent, 0);
                    dialog.show(parent.getFragmentManager(), "confirmLimit");
                }
            }
        }

        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Context context = getActivity();
            return new Builder(context).setTitle(2131626340).setMessage(getArguments().getCharSequence("message")).setPositiveButton(17039370, this).setNegativeButton(17039360, null).create();
        }

        public void onClick(DialogInterface dialog, int which) {
            if (which == -1) {
                long limitBytes = getArguments().getLong("limitBytes");
                BillingCycleSettings target = (BillingCycleSettings) getTargetFragment();
                if (target != null) {
                    target.setPolicyLimitBytes(limitBytes);
                }
            }
        }
    }

    public static class CycleEditorFragment extends DialogFragment implements OnClickListener {
        private NumberPicker mCycleDayPicker;

        public static void show(BillingCycleSettings parent) {
            if (parent.isAdded()) {
                Bundle args = new Bundle();
                args.putParcelable("template", parent.mNetworkTemplate);
                CycleEditorFragment dialog = new CycleEditorFragment();
                dialog.setArguments(args);
                dialog.setTargetFragment(parent, 0);
                dialog.show(parent.getFragmentManager(), "cycleEditor");
            }
        }

        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Context context = getActivity();
            NetworkPolicyEditor editor = ((BillingCycleSettings) getTargetFragment()).services.mPolicyEditor;
            Builder builder = new Builder(context);
            View view = LayoutInflater.from(builder.getContext()).inflate(2130968721, null, false);
            this.mCycleDayPicker = (NumberPicker) view.findViewById(2131886456);
            int cycleDay = editor.getPolicyCycleDay((NetworkTemplate) getArguments().getParcelable("template"));
            this.mCycleDayPicker.setMinValue(1);
            this.mCycleDayPicker.setMaxValue(31);
            this.mCycleDayPicker.setValue(cycleDay);
            this.mCycleDayPicker.setWrapSelectorWheel(true);
            return builder.setTitle(2131626335).setView(view).setPositiveButton(2131626337, this).create();
        }

        public void onClick(DialogInterface dialog, int which) {
            NetworkTemplate template = (NetworkTemplate) getArguments().getParcelable("template");
            BillingCycleSettings target = (BillingCycleSettings) getTargetFragment();
            NetworkPolicyEditor editor = target.services.mPolicyEditor;
            this.mCycleDayPicker.clearFocus();
            editor.setPolicyCycleDay(template, this.mCycleDayPicker.getValue(), new Time().timezone);
            target.updatePrefs();
        }
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        this.mDataUsageController = new DataUsageController(getContext());
        this.mNetworkTemplate = (NetworkTemplate) getArguments().getParcelable("network_template");
        addPreferencesFromResource(2131230745);
        this.mBillingCycle = findPreference("billing_cycle");
        this.mDataWarning = findPreference("data_warning");
        this.mEnableDataLimit = (SwitchPreference) findPreference("set_data_limit");
        this.mEnableDataLimit.setOnPreferenceChangeListener(this);
        this.mDataLimit = findPreference("data_limit");
    }

    public void onResume() {
        super.onResume();
        updatePrefs();
    }

    private void updatePrefs() {
        int i;
        NetworkPolicy policy = this.services.mPolicyEditor.getPolicy(this.mNetworkTemplate);
        Preference preference = this.mBillingCycle;
        Object[] objArr = new Object[1];
        if (policy != null) {
            i = policy.cycleDay;
        } else {
            i = 1;
        }
        objArr[0] = Integer.valueOf(i);
        preference.setSummary(getString(2131627147, objArr));
        this.mDataWarning.setSummary(Formatter.formatFileSize(getContext(), policy != null ? policy.warningBytes : 2147483648L));
        if (policy == null || policy.limitBytes == -1) {
            this.mDataLimit.setSummary(null);
            this.mDataLimit.setEnabled(false);
            this.mEnableDataLimit.setChecked(false);
            return;
        }
        this.mDataLimit.setSummary(Formatter.formatFileSize(getContext(), policy.limitBytes));
        this.mDataLimit.setEnabled(true);
        this.mEnableDataLimit.setChecked(true);
    }

    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference == this.mBillingCycle) {
            CycleEditorFragment.show(this);
            return true;
        } else if (preference == this.mDataWarning) {
            BytesEditorFragment.show(this, false);
            return true;
        } else if (preference != this.mDataLimit) {
            return super.onPreferenceTreeClick(preference);
        } else {
            BytesEditorFragment.show(this, true);
            return true;
        }
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (this.mEnableDataLimit != preference) {
            return false;
        }
        if (((Boolean) newValue).booleanValue()) {
            ConfirmLimitFragment.show(this);
        } else {
            setPolicyLimitBytes(-1);
        }
        return true;
    }

    protected int getMetricsCategory() {
        return 342;
    }

    private void setPolicyLimitBytes(long limitBytes) {
        this.services.mPolicyEditor.setPolicyLimitBytes(this.mNetworkTemplate, limitBytes);
        updatePrefs();
    }
}
