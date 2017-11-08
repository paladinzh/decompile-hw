package com.android.settings.accessibility;

import android.content.res.Resources;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.TwoStatePreference;
import android.view.View;
import com.android.settings.SeekBarPreference;

public class ToggleAutoclickPreferenceFragment extends ToggleFeaturePreferenceFragment implements OnPreferenceChangeListener {
    private static final int[] mAutoclickPreferenceSummaries = new int[]{2131689484, 2131689485, 2131689486, 2131689487, 2131689488};
    private SeekBarPreference mDelay;

    static CharSequence getAutoclickPreferenceSummary(Resources resources, int delay) {
        return resources.getQuantityString(mAutoclickPreferenceSummaries[getAutoclickPreferenceSummaryIndex(delay)], delay, new Object[]{Integer.valueOf(delay)});
    }

    private static int getAutoclickPreferenceSummaryIndex(int delay) {
        if (delay <= 200) {
            return 0;
        }
        if (delay >= 1000) {
            return mAutoclickPreferenceSummaries.length - 1;
        }
        return (delay - 200) / (800 / (mAutoclickPreferenceSummaries.length - 1));
    }

    protected void onPreferenceToggled(String preferenceKey, boolean enabled) {
        Secure.putInt(getContentResolver(), preferenceKey, enabled ? 1 : 0);
        this.mToggleSwitch.setTitle(getPrefContext().getResources().getString(enabled ? 2131625876 : 2131625877));
        this.mDelay.setEnabled(enabled);
    }

    protected int getMetricsCategory() {
        return 335;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(2131230722);
        int delay = Secure.getInt(getContentResolver(), "accessibility_autoclick_delay", 600);
        this.mDelay = (SeekBarPreference) findPreference("autoclick_delay");
        this.mDelay.setMax(delayToSeekBarProgress(1000));
        this.mDelay.setProgress(delayToSeekBarProgress(delay));
        this.mDelay.setOnPreferenceChangeListener(this);
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setTitle(getString(2131625871));
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == this.mDelay && (newValue instanceof Integer)) {
            Secure.putInt(getContentResolver(), "accessibility_autoclick_delay", seekBarProgressToDelay(((Integer) newValue).intValue()));
            return true;
        } else if (this.mToggleSwitch != preference) {
            return false;
        } else {
            onPreferenceToggled("accessibility_autoclick_enabled", ((Boolean) newValue).booleanValue());
            return true;
        }
    }

    protected void onInstallToggleSwitch() {
        boolean z;
        boolean z2 = true;
        int value = Secure.getInt(getContentResolver(), "accessibility_autoclick_enabled", 0);
        this.mToggleSwitch.setTitle(getPrefContext().getResources().getString(value == 1 ? 2131625876 : 2131625877));
        this.mToggleSwitch.setOnPreferenceChangeListener(this);
        TwoStatePreference twoStatePreference = this.mToggleSwitch;
        if (value == 1) {
            z = true;
        } else {
            z = false;
        }
        twoStatePreference.setChecked(z);
        if (this.mDelay != null) {
            SeekBarPreference seekBarPreference = this.mDelay;
            if (value != 1) {
                z2 = false;
            }
            seekBarPreference.setEnabled(z2);
        }
    }

    private int seekBarProgressToDelay(int progress) {
        return (progress * 100) + 200;
    }

    private int delayToSeekBarProgress(int delay) {
        return (delay - 200) / 100;
    }
}
