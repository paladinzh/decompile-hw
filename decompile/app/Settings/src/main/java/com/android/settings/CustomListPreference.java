package com.android.settings;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v14.preference.ListPreferenceDialogFragment;
import android.support.v7.preference.ListPreference;
import android.util.AttributeSet;

public class CustomListPreference extends ListPreference {

    public static class CustomListPreferenceDialogFragment extends ListPreferenceDialogFragment {
        private int mClickedDialogEntryIndex;

        public static ListPreferenceDialogFragment newInstance(String key) {
            ListPreferenceDialogFragment fragment = new CustomListPreferenceDialogFragment();
            Bundle b = new Bundle(1);
            b.putString("key", key);
            fragment.setArguments(b);
            return fragment;
        }

        private CustomListPreference getCustomizablePreference() {
            return (CustomListPreference) getPreference();
        }

        protected void onPrepareDialogBuilder(Builder builder) {
            super.onPrepareDialogBuilder(builder);
            if (getCustomizablePreference() != null) {
                this.mClickedDialogEntryIndex = getCustomizablePreference().findIndexOfValue(getCustomizablePreference().getValue());
                getCustomizablePreference().onPrepareDialogBuilder(builder, getOnItemClickListener());
                if (!getCustomizablePreference().isAutoClosePreference()) {
                    builder.setPositiveButton(2131624573, new OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            CustomListPreferenceDialogFragment.this.onClick(dialog, -1);
                            dialog.dismiss();
                        }
                    });
                }
            }
        }

        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Dialog dialog = super.onCreateDialog(savedInstanceState);
            if (savedInstanceState != null) {
                this.mClickedDialogEntryIndex = savedInstanceState.getInt("settings.CustomListPrefDialog.KEY_CLICKED_ENTRY_INDEX", this.mClickedDialogEntryIndex);
            }
            if (getCustomizablePreference() != null) {
                getCustomizablePreference().onDialogCreated(dialog);
            }
            return dialog;
        }

        public void onSaveInstanceState(Bundle outState) {
            super.onSaveInstanceState(outState);
            outState.putInt("settings.CustomListPrefDialog.KEY_CLICKED_ENTRY_INDEX", this.mClickedDialogEntryIndex);
        }

        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            if (getCustomizablePreference() != null) {
                getCustomizablePreference().onDialogStateRestored(getDialog(), savedInstanceState);
            }
        }

        protected OnClickListener getOnItemClickListener() {
            return new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    CustomListPreferenceDialogFragment.this.setClickedDialogEntryIndex(which);
                    if (CustomListPreferenceDialogFragment.this.getCustomizablePreference().isAutoClosePreference()) {
                        CustomListPreferenceDialogFragment.this.onClick(dialog, -1);
                        dialog.dismiss();
                    }
                }
            };
        }

        protected void setClickedDialogEntryIndex(int which) {
            this.mClickedDialogEntryIndex = which;
        }

        public void onDialogClosed(boolean positiveResult) {
            if (getCustomizablePreference() != null) {
                getCustomizablePreference().onDialogClosed(positiveResult);
                ListPreference preference = getCustomizablePreference();
                if (positiveResult && this.mClickedDialogEntryIndex >= 0 && preference.getEntryValues() != null) {
                    String value = preference.getEntryValues()[this.mClickedDialogEntryIndex].toString();
                    if (preference.callChangeListener(value)) {
                        preference.setValue(value);
                    }
                }
            }
        }
    }

    public CustomListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomListPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    protected void onPrepareDialogBuilder(Builder builder, OnClickListener listener) {
    }

    protected void onDialogClosed(boolean positiveResult) {
    }

    protected void onDialogCreated(Dialog dialog) {
    }

    protected boolean isAutoClosePreference() {
        return true;
    }

    protected void onDialogStateRestored(Dialog dialog, Bundle savedInstanceState) {
    }
}
