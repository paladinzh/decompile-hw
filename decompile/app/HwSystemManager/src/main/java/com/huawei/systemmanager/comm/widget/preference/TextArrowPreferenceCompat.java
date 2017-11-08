package com.huawei.systemmanager.comm.widget.preference;

import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.TextArrowPreference;

public class TextArrowPreferenceCompat {
    TextArrowPreference perfer;

    private TextArrowPreferenceCompat(TextArrowPreference perfer) {
        this.perfer = perfer;
    }

    public void setDetail(CharSequence sequence) {
        this.perfer.setDetail(sequence);
    }

    public void setSummary(CharSequence sequence) {
        this.perfer.setSummary(sequence);
    }

    public void setNetherSummary(CharSequence sequence) {
        this.perfer.setNetherSummary(sequence);
    }

    public void setEnabled(boolean enable) {
        this.perfer.setEnabled(enable);
    }

    public void setOnPreferenceClickListener(OnPreferenceClickListener clicker) {
        this.perfer.setOnPreferenceClickListener(clicker);
    }

    public Preference getPrference() {
        return this.perfer;
    }

    public String getKey() {
        return this.perfer.getKey();
    }

    public static TextArrowPreferenceCompat createFromPerfer(Preference perfer) {
        return new TextArrowPreferenceCompat((TextArrowPreference) perfer);
    }
}
