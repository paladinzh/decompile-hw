package com.android.systemui.tuner;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v14.preference.PreferenceFragment;
import android.support.v14.preference.PreferenceFragment.OnPreferenceStartFragmentCallback;
import android.support.v14.preference.PreferenceFragment.OnPreferenceStartScreenCallback;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.util.Log;
import com.android.settingslib.drawer.SettingsDrawerActivity;
import com.android.systemui.R;

public class TunerActivity extends SettingsDrawerActivity implements OnPreferenceStartFragmentCallback, OnPreferenceStartScreenCallback {

    public static class SubSettingsFragment extends PreferenceFragment {
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferenceScreen((PreferenceScreen) ((PreferenceFragment) getTargetFragment()).getPreferenceScreen().findPreference(rootKey));
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getFragmentManager().findFragmentByTag("tuner") == null) {
            PreferenceFragment fragment;
            String action = getIntent().getAction();
            boolean showDemoMode = action != null ? action.equals("com.android.settings.action.DEMO_MODE") : false;
            if (getIntent().getBooleanExtra("show_night_mode", false)) {
                fragment = new NightModeFragment();
            } else if (showDemoMode) {
                fragment = new DemoModeFragment();
            } else {
                fragment = new TunerFragment();
            }
            getFragmentManager().beginTransaction().replace(R.id.content_frame, fragment, "tuner").commit();
        }
    }

    public void onBackPressed() {
        if (!getFragmentManager().popBackStackImmediate()) {
            super.onBackPressed();
        }
    }

    public boolean onPreferenceStartFragment(PreferenceFragment caller, Preference pref) {
        try {
            Fragment fragment = (Fragment) Class.forName(pref.getFragment()).newInstance();
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            setTitle(pref.getTitle());
            transaction.replace(R.id.content_frame, fragment);
            transaction.addToBackStack("PreferenceFragment");
            transaction.commit();
            return true;
        } catch (ReflectiveOperationException e) {
            Log.d("TunerActivity", "Problem launching fragment", e);
            return false;
        }
    }

    public boolean onPreferenceStartScreen(PreferenceFragment caller, PreferenceScreen pref) {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        SubSettingsFragment fragment = new SubSettingsFragment();
        Bundle b = new Bundle(1);
        b.putString("android.support.v7.preference.PreferenceFragmentCompat.PREFERENCE_ROOT", pref.getKey());
        fragment.setArguments(b);
        fragment.setTargetFragment(caller, 0);
        transaction.replace(R.id.content_frame, fragment);
        transaction.addToBackStack("PreferenceFragment");
        transaction.commit();
        return true;
    }
}
