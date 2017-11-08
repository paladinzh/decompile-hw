package com.android.settings.notification;

import android.content.Context;
import android.support.v7.preference.DropDownPreference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import com.android.settingslib.R$id;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.RestrictedLockUtils.EnforcedAdmin;
import com.android.settingslib.RestrictedPreferenceHelper;
import java.util.ArrayList;
import java.util.List;

public class RestrictedDropDownPreference extends DropDownPreference {
    private final RestrictedPreferenceHelper mHelper;
    private final OnItemSelectedListener mItemSelectedListener = new OnItemSelectedListener() {
        public void onItemSelected(AdapterView<?> adapterView, View v, int position, long id) {
            if (RestrictedDropDownPreference.this.mUserClicked) {
                RestrictedDropDownPreference.this.mUserClicked = false;
                if (position >= 0 && position < RestrictedDropDownPreference.this.getEntryValues().length) {
                    String value = RestrictedDropDownPreference.this.getEntryValues()[position].toString();
                    RestrictedItem item = RestrictedDropDownPreference.this.getRestrictedItemForEntryValue(value);
                    if (item != null) {
                        RestrictedLockUtils.sendShowAdminSupportDetailsIntent(RestrictedDropDownPreference.this.getContext(), item.enforcedAdmin);
                        RestrictedDropDownPreference.this.mSpinner.setSelection(RestrictedDropDownPreference.this.findIndexOfValue(RestrictedDropDownPreference.this.getValue()));
                    } else if (!value.equals(RestrictedDropDownPreference.this.getValue()) && RestrictedDropDownPreference.this.callChangeListener(value)) {
                        RestrictedDropDownPreference.this.setValue(value);
                    }
                }
            }
        }

        public void onNothingSelected(AdapterView<?> adapterView) {
        }
    };
    private List<RestrictedItem> mRestrictedItems = new ArrayList();
    private ReselectionSpinner mSpinner;
    private boolean mUserClicked = false;

    public static class ReselectionSpinner extends Spinner {
        private RestrictedDropDownPreference pref;

        public ReselectionSpinner(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        public void setPreference(RestrictedDropDownPreference pref) {
            this.pref = pref;
        }

        public void setSelection(int position) {
            int previousSelectedPosition = getSelectedItemPosition();
            super.setSelection(position);
            if (position == previousSelectedPosition && this.pref.isUserClicked()) {
                this.pref.setUserClicked(false);
                RestrictedItem item = this.pref.getRestrictedItemForPosition(position);
                if (item != null) {
                    RestrictedLockUtils.sendShowAdminSupportDetailsIntent(getContext(), item.enforcedAdmin);
                }
            }
        }
    }

    private class RestrictedArrayItemAdapter extends ArrayAdapter<String> {
        public RestrictedArrayItemAdapter(Context context) {
            super(context, 2130969145, 16908308);
        }

        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            int i = 0;
            View rootView = super.getView(position, convertView, parent);
            boolean isEntryRestricted = RestrictedDropDownPreference.this.isRestrictedForEntry((CharSequence) getItem(position));
            TextView text = (TextView) rootView.findViewById(16908308);
            if (text != null) {
                text.setEnabled(!isEntryRestricted);
            }
            View restrictedIcon = rootView.findViewById(R$id.restricted_icon);
            if (restrictedIcon != null) {
                if (!isEntryRestricted) {
                    i = 8;
                }
                restrictedIcon.setVisibility(i);
            }
            return rootView;
        }
    }

    public static class RestrictedItem {
        public final EnforcedAdmin enforcedAdmin;
        public final CharSequence entry;
        public final CharSequence entryValue;

        public RestrictedItem(CharSequence entry, CharSequence entryValue, EnforcedAdmin enforcedAdmin) {
            this.entry = entry;
            this.entryValue = entryValue;
            this.enforcedAdmin = enforcedAdmin;
        }
    }

    public RestrictedDropDownPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayoutResource(2130969061);
        setWidgetLayoutResource(2130969059);
        this.mHelper = new RestrictedPreferenceHelper(context, this, attrs);
    }

    protected ArrayAdapter createAdapter() {
        return new RestrictedArrayItemAdapter(getContext());
    }

    public void setValue(String value) {
        if (getRestrictedItemForEntryValue(value) == null) {
            super.setValue(value);
        }
    }

    public void onBindViewHolder(PreferenceViewHolder view) {
        this.mSpinner = (ReselectionSpinner) view.itemView.findViewById(android.support.v7.preference.R$id.spinner);
        this.mSpinner.setPreference(this);
        super.onBindViewHolder(view);
        this.mHelper.onBindViewHolder(view);
        this.mSpinner.setOnItemSelectedListener(this.mItemSelectedListener);
        View restrictedIcon = view.findViewById(R$id.restricted_icon);
        if (restrictedIcon != null) {
            int i;
            if (isDisabledByAdmin()) {
                i = 0;
            } else {
                i = 8;
            }
            restrictedIcon.setVisibility(i);
        }
    }

    private boolean isRestrictedForEntry(CharSequence entry) {
        if (entry == null) {
            return false;
        }
        for (RestrictedItem item : this.mRestrictedItems) {
            if (entry.equals(item.entry)) {
                return true;
            }
        }
        return false;
    }

    private RestrictedItem getRestrictedItemForEntryValue(CharSequence entryValue) {
        if (entryValue == null) {
            return null;
        }
        for (RestrictedItem item : this.mRestrictedItems) {
            if (entryValue.equals(item.entryValue)) {
                return item;
            }
        }
        return null;
    }

    private RestrictedItem getRestrictedItemForPosition(int position) {
        if (position < 0 || position >= getEntryValues().length) {
            return null;
        }
        return getRestrictedItemForEntryValue(getEntryValues()[position]);
    }

    public void addRestrictedItem(RestrictedItem item) {
        this.mRestrictedItems.add(item);
    }

    public void clearRestrictedItems() {
        this.mRestrictedItems.clear();
    }

    public void performClick() {
        if (!this.mHelper.performClick()) {
            this.mUserClicked = true;
            super.performClick();
        }
    }

    public void setEnabled(boolean enabled) {
        if (enabled && isDisabledByAdmin()) {
            this.mHelper.setDisabledByAdmin(null);
        } else {
            super.setEnabled(enabled);
        }
    }

    public void setDisabledByAdmin(EnforcedAdmin admin) {
        if (this.mHelper.setDisabledByAdmin(admin)) {
            notifyChanged();
        }
    }

    public boolean isDisabledByAdmin() {
        return this.mHelper.isDisabledByAdmin();
    }

    private void setUserClicked(boolean userClicked) {
        this.mUserClicked = userClicked;
    }

    private boolean isUserClicked() {
        return this.mUserClicked;
    }
}
