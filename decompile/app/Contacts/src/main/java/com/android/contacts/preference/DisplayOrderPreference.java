package com.android.contacts.preference;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.preference.ListPreference;
import android.util.AttributeSet;
import com.google.android.gms.R;

public final class DisplayOrderPreference extends ListPreference {
    private Context mContext;
    private ContactsPreferences mPreferences;

    public DisplayOrderPreference(Context context) {
        super(context);
        prepare();
    }

    public DisplayOrderPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        prepare();
    }

    private void prepare() {
        this.mContext = getContext();
        this.mPreferences = new ContactsPreferences(this.mContext);
        setEntries(new String[]{this.mContext.getString(R.string.display_options_view_given_name_first), this.mContext.getString(R.string.display_options_view_family_name_first)});
        setEntryValues(new String[]{String.valueOf(1), String.valueOf(2)});
        setValue(String.valueOf(this.mPreferences.getDisplayOrder()));
    }

    protected boolean shouldPersist() {
        return false;
    }

    public CharSequence getSummary() {
        switch (this.mPreferences.getDisplayOrder()) {
            case 1:
                return this.mContext.getString(R.string.display_options_view_given_name_first);
            case 2:
                return this.mContext.getString(R.string.display_options_view_family_name_first);
            default:
                return null;
        }
    }

    protected boolean persistString(String value) {
        int newValue = Integer.parseInt(value);
        if (newValue != this.mPreferences.getDisplayOrder()) {
            this.mPreferences.setDisplayOrder(newValue);
            notifyChanged();
        }
        return true;
    }

    protected void onPrepareDialogBuilder(Builder builder) {
        super.onPrepareDialogBuilder(builder);
        builder.setNegativeButton(null, null);
    }
}
