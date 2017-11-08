package com.android.settings.accessibility;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.ComponentName;
import android.content.pm.ServiceInfo;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.text.TextUtils;
import android.view.accessibility.AccessibilityManager;
import com.android.settings.DialogCreatable;
import com.android.settings.SettingsPreferenceFragment;

public class AccessibilitySettingsForSetupWizard extends SettingsPreferenceFragment implements DialogCreatable, OnPreferenceChangeListener {
    private static final String TAG = AccessibilitySettingsForSetupWizard.class.getSimpleName();
    private Preference mDisplayMagnificationPreference;
    private Preference mScreenReaderPreference;

    protected int getMetricsCategory() {
        return 367;
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(2131230725);
        this.mDisplayMagnificationPreference = findPreference("screen_magnification_preference");
        this.mScreenReaderPreference = findPreference("screen_reader_preference");
    }

    public void onResume() {
        super.onResume();
        updateScreenReaderPreference();
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(false);
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        return false;
    }

    public boolean onPreferenceTreeClick(Preference preference) {
        boolean z = true;
        if (this.mDisplayMagnificationPreference == preference) {
            Bundle extras = this.mDisplayMagnificationPreference.getExtras();
            extras.putString("title", getString(2131625853));
            extras.putCharSequence("summary", getText(2131625855));
            String str = "checked";
            if (Secure.getInt(getContentResolver(), "accessibility_display_magnification_enabled", 0) != 1) {
                z = false;
            }
            extras.putBoolean(str, z);
        }
        return super.onPreferenceTreeClick(preference);
    }

    private AccessibilityServiceInfo findFirstServiceWithSpokenFeedback() {
        for (AccessibilityServiceInfo info : ((AccessibilityManager) getActivity().getSystemService(AccessibilityManager.class)).getInstalledAccessibilityServiceList()) {
            if ((info.feedbackType & 1) != 0) {
                return info;
            }
        }
        return null;
    }

    private void updateScreenReaderPreference() {
        AccessibilityServiceInfo info = findFirstServiceWithSpokenFeedback();
        if (info == null) {
            this.mScreenReaderPreference.setEnabled(false);
            return;
        }
        this.mScreenReaderPreference.setEnabled(true);
        ServiceInfo serviceInfo = info.getResolveInfo().serviceInfo;
        CharSequence title = info.getResolveInfo().loadLabel(getPackageManager()).toString();
        this.mScreenReaderPreference.setTitle(title);
        ComponentName componentName = new ComponentName(serviceInfo.packageName, serviceInfo.name);
        this.mScreenReaderPreference.setKey(componentName.flattenToString());
        Bundle extras = this.mScreenReaderPreference.getExtras();
        extras.putParcelable("component_name", componentName);
        extras.putString("preference_key", this.mScreenReaderPreference.getKey());
        extras.putString("title", title);
        String description = info.loadDescription(getPackageManager());
        if (TextUtils.isEmpty(description)) {
            description = getString(2131625922);
        }
        extras.putString("summary", description);
    }
}
