package com.android.settings;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v14.preference.ListPreferenceDialogFragment;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import com.android.settings.CustomListPreference.CustomListPreferenceDialogFragment;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.RestrictedLockUtils.EnforcedAdmin;
import com.android.settingslib.RestrictedPreferenceHelper;
import java.util.ArrayList;
import java.util.List;

public class RestrictedListPreference extends CustomListPreference {
    private final RestrictedPreferenceHelper mHelper;
    private final List<RestrictedItem> mRestrictedItems = new ArrayList();

    public class RestrictedArrayAdapter extends ArrayAdapter<CharSequence> {
        private final int mSelectedIndex;

        public RestrictedArrayAdapter(Context context, CharSequence[] objects, int selectedIndex) {
            super(context, 2130969058, 2131887086, objects);
            this.mSelectedIndex = selectedIndex;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            boolean z = false;
            View root = super.getView(position, convertView, parent);
            CheckedTextView text = (CheckedTextView) root.findViewById(2131887086);
            ImageView padlock = (ImageView) root.findViewById(2131887087);
            if (RestrictedListPreference.this.isRestrictedForEntry((CharSequence) getItem(position))) {
                text.setEnabled(false);
                text.setChecked(false);
                padlock.setVisibility(0);
            } else {
                if (this.mSelectedIndex != -1) {
                    if (position == this.mSelectedIndex) {
                        z = true;
                    }
                    text.setChecked(z);
                }
                if (!text.isEnabled()) {
                    text.setEnabled(true);
                }
                padlock.setVisibility(8);
            }
            return root;
        }

        public boolean hasStableIds() {
            return true;
        }

        public long getItemId(int position) {
            return (long) position;
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

    public static class RestrictedListPreferenceDialogFragment extends CustomListPreferenceDialogFragment {
        private int mLastCheckedPosition = -1;

        public static ListPreferenceDialogFragment newInstance(String key) {
            ListPreferenceDialogFragment fragment = new RestrictedListPreferenceDialogFragment();
            Bundle b = new Bundle(1);
            b.putString("key", key);
            fragment.setArguments(b);
            return fragment;
        }

        private RestrictedListPreference getCustomizablePreference() {
            return (RestrictedListPreference) getPreference();
        }

        protected OnClickListener getOnItemClickListener() {
            return new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    RestrictedListPreference preference = RestrictedListPreferenceDialogFragment.this.getCustomizablePreference();
                    if (which >= 0 && which < preference.getEntryValues().length) {
                        RestrictedItem item = preference.getRestrictedItemForEntryValue(preference.getEntryValues()[which].toString());
                        if (item != null) {
                            ((AlertDialog) dialog).getListView().setItemChecked(RestrictedListPreferenceDialogFragment.this.getLastCheckedPosition(), true);
                            RestrictedLockUtils.sendShowAdminSupportDetailsIntent(RestrictedListPreferenceDialogFragment.this.getContext(), item.enforcedAdmin);
                        } else {
                            RestrictedListPreferenceDialogFragment.this.setClickedDialogEntryIndex(which);
                        }
                        if (RestrictedListPreferenceDialogFragment.this.getCustomizablePreference().isAutoClosePreference()) {
                            RestrictedListPreferenceDialogFragment.this.onClick(dialog, -1);
                            dialog.dismiss();
                        }
                    }
                }
            };
        }

        private int getLastCheckedPosition() {
            if (this.mLastCheckedPosition == -1) {
                this.mLastCheckedPosition = getCustomizablePreference().getSelectedValuePos();
            }
            return this.mLastCheckedPosition;
        }

        protected void setClickedDialogEntryIndex(int which) {
            super.setClickedDialogEntryIndex(which);
            this.mLastCheckedPosition = which;
        }
    }

    public RestrictedListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mHelper = new RestrictedPreferenceHelper(context, this, attrs);
    }

    public RestrictedListPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mHelper = new RestrictedPreferenceHelper(context, this, attrs);
    }

    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        this.mHelper.onBindViewHolder(holder);
    }

    public void performClick() {
        if (!this.mHelper.performClick()) {
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

    public boolean isRestrictedForEntry(CharSequence entry) {
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

    public void addRestrictedItem(RestrictedItem item) {
        this.mRestrictedItems.add(item);
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

    protected ListAdapter createListAdapter() {
        return new RestrictedArrayAdapter(getContext(), getEntries(), getSelectedValuePos());
    }

    public int getSelectedValuePos() {
        String selectedValue = getValue();
        return selectedValue == null ? -1 : findIndexOfValue(selectedValue);
    }

    protected void onPrepareDialogBuilder(Builder builder, OnClickListener listener) {
        builder.setAdapter(createListAdapter(), listener);
    }
}
