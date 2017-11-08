package com.android.settings;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings.System;
import android.support.v7.preference.Preference;
import android.view.MenuItem;
import com.android.settings.fingerprint.HwCustFingerprintSettingsFragmentImpl;

public class InternationalRoaming extends SettingsPreferenceFragment {
    private static String HOTLINE_NUMBER = "";
    private Preference mMenuPref;
    private Preference mRestoreDefPref;
    private Preference mRoamingListPref;
    private Preference mRomaingHotlinePref;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(2131230835);
        if (getActivity().getActionBar() != null) {
            getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
        }
        this.mRoamingListPref = findPreference("pref_key_roaming_list");
        this.mRomaingHotlinePref = findPreference("pref_key_roaming_hotline");
        HOTLINE_NUMBER = System.getString(getContext().getContentResolver(), "hw_international_roaming_no");
        if (HOTLINE_NUMBER == null || "".equalsIgnoreCase(HOTLINE_NUMBER)) {
            HOTLINE_NUMBER = "+8618918910000";
        }
        this.mRomaingHotlinePref.setSummary(HOTLINE_NUMBER);
        this.mRestoreDefPref = findPreference("pref_key_restore_default");
        this.mMenuPref = findPreference("pref_key_menu");
        Preference networkSelectionPrefs = findPreference("pref_key_network_selection_settings");
        if (networkSelectionPrefs != null && !Utils.hasIntentActivity(getContext().getPackageManager(), networkSelectionPrefs.getIntent())) {
            getPreferenceScreen().removePreference(networkSelectionPrefs);
        }
    }

    protected int getMetricsCategory() {
        return 100000;
    }

    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference == this.mRoamingListPref) {
            showDialog(0);
        } else if (preference == this.mRomaingHotlinePref) {
            Intent intent = new Intent("android.intent.action.CALL_PRIVILEGED", Uri.fromParts(HwCustFingerprintSettingsFragmentImpl.TEL_PATTERN, HOTLINE_NUMBER, null));
            intent.setFlags(276824064);
            intent.putExtra("subscription", 0);
            startActivity(intent);
        } else if (preference == this.mRestoreDefPref) {
            showDialog(2);
        } else if (preference != this.mMenuPref) {
            return false;
        } else {
            showDialog(3);
        }
        return true;
    }

    public Dialog onCreateDialog(int id) {
        switch (id) {
            case 0:
                return new Builder(getContext()).setTitle(2131627376).setMessage(2131627379).setPositiveButton(2131625656, null).create();
            case 2:
                return new Builder(getContext()).setTitle(2131627377).setMessage(2131627380).setPositiveButton(2131624348, new OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        InternationalRoaming.this.showDialog(4);
                    }
                }).setNegativeButton(2131624349, null).setIcon(17301543).setInverseBackgroundForced(false).create();
            case 3:
                return new Builder(getContext()).setTitle(2131627375).setMessage(2131627381).setPositiveButton(2131625656, null).create();
            case 4:
                return new Builder(getContext()).setTitle(2131627378).setMessage(2131627382).create();
            default:
                return super.onCreateDialog(id);
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == 16908332) {
            getActivity().onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}
