package com.android.settings.backup;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.backup.IBackupManager;
import android.app.backup.IBackupManager.Stub;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.provider.Settings.Secure;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import com.android.settings.SettingsActivity;
import com.android.settings.SettingsPreferenceFragment;

public class ToggleBackupSettingFragment extends SettingsPreferenceFragment implements OnClickListener, OnDismissListener, OnPreferenceChangeListener {
    private IBackupManager mBackupManager;
    private Dialog mConfirmDialog;
    private Preference mSummaryPreference;
    protected SwitchPreference mSwitch;
    private boolean mWaitingForConfirmationDialog = false;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mBackupManager = Stub.asInterface(ServiceManager.getService("backup"));
        PreferenceScreen preferenceScreen = getPreferenceManager().createPreferenceScreen(getActivity());
        setPreferenceScreen(preferenceScreen);
        this.mSwitch = new SwitchPreference(getPrefContext());
        this.mSwitch.setTitle(2131626150);
        preferenceScreen.addPreference(this.mSwitch);
        this.mSummaryPreference = new Preference(getPrefContext()) {
            public void onBindViewHolder(PreferenceViewHolder view) {
                super.onBindViewHolder(view);
                ((TextView) view.findViewById(16908304)).setText(getSummary());
            }
        };
        this.mSummaryPreference.setPersistent(false);
        this.mSummaryPreference.setLayoutResource(2130969211);
        preferenceScreen.addPreference(this.mSummaryPreference);
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SettingsActivity activity = (SettingsActivity) getActivity();
        if (Secure.getInt(getContentResolver(), "user_full_data_backup_aware", 0) != 0) {
            this.mSummaryPreference.setSummary(2131626161);
        } else {
            this.mSummaryPreference.setSummary(2131626151);
        }
        try {
            this.mSwitch.setChecked(this.mBackupManager == null ? false : this.mBackupManager.isBackupEnabled());
        } catch (RemoteException e) {
            this.mSwitch.setEnabled(false);
        }
        getActivity().setTitle(2131626150);
    }

    public void onDestroyView() {
        super.onDestroyView();
        this.mSwitch.setOnPreferenceChangeListener(null);
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.mSwitch.setOnPreferenceChangeListener(this);
    }

    public void onStop() {
        if (this.mConfirmDialog != null && this.mConfirmDialog.isShowing()) {
            this.mConfirmDialog.dismiss();
        }
        this.mConfirmDialog = null;
        super.onStop();
    }

    public void onClick(DialogInterface dialog, int which) {
        if (which == -1) {
            this.mWaitingForConfirmationDialog = false;
            setBackupEnabled(false);
            this.mSwitch.setChecked(false);
        } else if (which == -2) {
            this.mWaitingForConfirmationDialog = false;
            setBackupEnabled(true);
            this.mSwitch.setChecked(true);
        }
    }

    public void onDismiss(DialogInterface dialog) {
        if (this.mWaitingForConfirmationDialog) {
            setBackupEnabled(true);
            this.mSwitch.setChecked(true);
        }
    }

    private void showEraseBackupDialog() {
        CharSequence msg;
        if (Secure.getInt(getContentResolver(), "user_full_data_backup_aware", 0) != 0) {
            msg = getResources().getText(2131626160);
        } else {
            msg = getResources().getText(2131626159);
        }
        this.mWaitingForConfirmationDialog = true;
        this.mConfirmDialog = new Builder(getActivity()).setMessage(msg).setTitle(2131626158).setPositiveButton(17039370, this).setNegativeButton(17039360, this).setOnDismissListener(this).show();
    }

    protected int getMetricsCategory() {
        return 81;
    }

    private void setBackupEnabled(boolean enable) {
        if (this.mBackupManager != null) {
            try {
                this.mBackupManager.setBackupEnabled(enable);
            } catch (RemoteException e) {
                Log.e("ToggleBackupSettingFragment", "Error communicating with BackupManager", e);
            }
        }
    }

    public boolean onPreferenceChange(Preference pref, Object newValue) {
        if (((Boolean) newValue).booleanValue()) {
            setBackupEnabled(true);
            this.mSwitch.setChecked(true);
            return true;
        }
        showEraseBackupDialog();
        return true;
    }

    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
    }
}
