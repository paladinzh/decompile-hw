package com.android.settings;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v14.preference.PreferenceDialogFragment;
import android.support.v7.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;

public class CustomDialogPreference extends DialogPreference {
    private CustomPreferenceDialogFragment mFragment;

    public static class CustomPreferenceDialogFragment extends PreferenceDialogFragment {
        public static CustomPreferenceDialogFragment newInstance(String key) {
            CustomPreferenceDialogFragment fragment = new CustomPreferenceDialogFragment();
            Bundle b = new Bundle(1);
            b.putString("key", key);
            fragment.setArguments(b);
            return fragment;
        }

        private CustomDialogPreference getCustomizablePreference() {
            return (CustomDialogPreference) getPreference();
        }

        protected void onPrepareDialogBuilder(Builder builder) {
            super.onPrepareDialogBuilder(builder);
            if (getCustomizablePreference() != null) {
                getCustomizablePreference().setFragment(this);
                getCustomizablePreference().onPrepareDialogBuilder(builder, this);
            }
        }

        public void onDialogClosed(boolean positiveResult) {
            if (getCustomizablePreference() != null) {
                getCustomizablePreference().onDialogClosed(positiveResult);
            }
        }

        protected void onBindDialogView(View view) {
            super.onBindDialogView(view);
            if (getCustomizablePreference() != null) {
                getCustomizablePreference().onBindDialogView(view);
            }
        }

        public void onClick(DialogInterface dialog, int which) {
            super.onClick(dialog, which);
            if (getCustomizablePreference() != null) {
                getCustomizablePreference().onClick(dialog, which);
            }
        }

        protected boolean needInputMethod() {
            if (getCustomizablePreference() != null) {
                return getCustomizablePreference().needInputMethod();
            }
            return false;
        }

        public void onStart() {
            super.onStart();
            if (getCustomizablePreference() != null) {
                getCustomizablePreference().onDialogFragmentStart();
            }
        }

        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Dialog dialog = super.onCreateDialog(savedInstanceState);
            if (getCustomizablePreference() != null) {
                return getCustomizablePreference().onCreateDialog(savedInstanceState, dialog);
            }
            return dialog;
        }
    }

    public CustomDialogPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public CustomDialogPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public CustomDialogPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomDialogPreference(Context context) {
        super(context);
    }

    public Dialog getDialog() {
        return this.mFragment != null ? this.mFragment.getDialog() : null;
    }

    protected void onPrepareDialogBuilder(Builder builder, OnClickListener listener) {
    }

    protected void onDialogClosed(boolean positiveResult) {
    }

    protected void onClick(DialogInterface dialog, int which) {
    }

    protected void onBindDialogView(View view) {
    }

    public void onDialogFragmentStart() {
    }

    protected Dialog onCreateDialog(Bundle savedInstanceState, Dialog dialog) {
        return dialog;
    }

    private void setFragment(CustomPreferenceDialogFragment fragment) {
        this.mFragment = fragment;
    }

    protected boolean needInputMethod() {
        return false;
    }
}
