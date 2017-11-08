package com.android.contacts.preference;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.preference.ListPreference;
import android.util.AttributeSet;
import com.google.android.gms.R;

public final class SortOrderPreference extends ListPreference {
    private Context mContext;
    private ContactsPreferences mPreferences;

    public SortOrderPreference(Context context) {
        super(context);
        prepare();
    }

    public SortOrderPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        prepare();
    }

    private void prepare() {
        this.mContext = getContext();
        this.mPreferences = new ContactsPreferences(this.mContext);
        setEntries(new String[]{this.mContext.getString(R.string.display_options_sort_by_given_name), this.mContext.getString(R.string.display_options_sort_by_family_name)});
        setEntryValues(new String[]{String.valueOf(1), String.valueOf(2)});
        setValue(String.valueOf(this.mPreferences.getSortOrder()));
    }

    protected boolean shouldPersist() {
        return false;
    }

    public CharSequence getSummary() {
        switch (this.mPreferences.getSortOrder()) {
            case 1:
                return this.mContext.getString(R.string.display_options_sort_by_given_name);
            case 2:
                return this.mContext.getString(R.string.display_options_sort_by_family_name);
            default:
                return null;
        }
    }

    protected boolean persistString(String value) {
        int newValue = Integer.parseInt(value);
        if (newValue != this.mPreferences.getSortOrder()) {
            this.mPreferences.setSortOrder(newValue);
            notifyChanged();
        }
        return true;
    }

    protected void onPrepareDialogBuilder(Builder builder) {
        super.onPrepareDialogBuilder(builder);
        builder.setNegativeButton(null, null);
    }
}
