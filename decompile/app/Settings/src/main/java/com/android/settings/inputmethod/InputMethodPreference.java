package com.android.settings.inputmethod;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.UserHandle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.Preference.OnPreferenceClickListener;
import android.text.TextUtils;
import android.util.Log;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;
import com.android.internal.inputmethod.InputMethodUtils;
import com.android.settings.accessibility.AccessibilityExtUtils;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.RestrictedSwitchPreference;
import java.text.Collator;

class InputMethodPreference extends RestrictedSwitchPreference implements OnPreferenceClickListener, OnPreferenceChangeListener {
    private static final String TAG = InputMethodPreference.class.getSimpleName();
    private AlertDialog mDialog = null;
    private final boolean mHasPriorityInSorting;
    private final InputMethodInfo mImi;
    private final InputMethodSettingValuesWrapper mInputMethodSettingValues;
    private final boolean mIsAllowedByOrganization;
    private final OnSavePreferenceListener mOnSaveListener;

    interface OnSavePreferenceListener {
        void onSaveInputMethodPreference(InputMethodPreference inputMethodPreference);
    }

    InputMethodPreference(Context context, InputMethodInfo imi, boolean isImeEnabler, boolean isAllowedByOrganization, OnSavePreferenceListener onSaveListener) {
        boolean z = false;
        super(context);
        setLayoutResource(2130969013);
        setPersistent(false);
        this.mImi = imi;
        this.mIsAllowedByOrganization = isAllowedByOrganization;
        this.mOnSaveListener = onSaveListener;
        if (!isImeEnabler) {
            setWidgetLayoutResource(2130968998);
        }
        setSwitchTextOn("");
        setSwitchTextOff("");
        setKey(imi.getId());
        setTitle(imi.loadLabel(context.getPackageManager()));
        String settingsActivity = imi.getSettingsActivity();
        if (TextUtils.isEmpty(settingsActivity)) {
            setIntent(null);
        } else {
            Intent intent = new Intent("android.intent.action.MAIN");
            intent.setClassName(imi.getPackageName(), settingsActivity);
            setIntent(intent);
        }
        this.mInputMethodSettingValues = InputMethodSettingValuesWrapper.getInstance(context);
        if (InputMethodUtils.isSystemIme(imi)) {
            z = this.mInputMethodSettingValues.isValidSystemNonAuxAsciiCapableIme(imi, context);
        }
        this.mHasPriorityInSorting = z;
        setOnPreferenceClickListener(this);
        setOnPreferenceChangeListener(this);
    }

    public InputMethodInfo getInputMethodInfo() {
        return this.mImi;
    }

    private boolean isImeEnabler() {
        return getWidgetLayoutResource() != 2130968998;
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (!isImeEnabler()) {
            return false;
        }
        if (isChecked()) {
            setChecked(false);
            this.mOnSaveListener.onSaveInputMethodPreference(this);
            return false;
        } else if (InputMethodUtils.isSystemIme(this.mImi)) {
            setChecked(true);
            this.mOnSaveListener.onSaveInputMethodPreference(this);
            return false;
        } else {
            showSecurityWarnDialog(this.mImi);
            return false;
        }
    }

    public boolean onPreferenceClick(Preference preference) {
        if (isImeEnabler()) {
            return true;
        }
        Context context = getContext();
        try {
            Intent intent = getIntent();
            if (intent != null) {
                context.startActivity(intent);
            }
        } catch (ActivityNotFoundException e) {
            Log.d(TAG, "IME's Settings Activity Not Found", e);
            Toast.makeText(context, context.getString(2131625763, new Object[]{this.mImi.loadLabel(context.getPackageManager())}), 1).show();
        }
        return true;
    }

    void updatePreferenceViews() {
        if (this.mImi != null) {
            boolean isAlwaysChecked = this.mInputMethodSettingValues.isAlwaysCheckedIme(this.mImi, getContext());
            if (this.mImi.getPackageName() != null) {
                boolean isGoogleLatin;
                if (this.mImi.getPackageName().contains("com.android.inputmethod.latin")) {
                    isGoogleLatin = true;
                } else {
                    isGoogleLatin = this.mImi.getPackageName().contains("com.google.android.inputmethod.latin");
                }
                if (isGoogleLatin) {
                    isAlwaysChecked = true;
                }
            }
            if (isAlwaysChecked && isImeEnabler()) {
                setDisabledByAdmin(null);
                setEnabled(false);
            } else if (this.mIsAllowedByOrganization) {
                setEnabled(true);
            } else {
                setDisabledByAdmin(RestrictedLockUtils.checkIfInputMethodDisallowed(getContext(), this.mImi.getPackageName(), UserHandle.myUserId()));
            }
            setChecked(this.mInputMethodSettingValues.isEnabledImi(this.mImi));
            if (!isDisabledByAdmin()) {
                setSummary(AccessibilityExtUtils.toTitleCase(getSummaryString()));
            }
        }
    }

    private InputMethodManager getInputMethodManager() {
        return (InputMethodManager) getContext().getSystemService("input_method");
    }

    private String getSummaryString() {
        return InputMethodAndSubtypeUtil.getSubtypeLocaleNameListAsSentence(getInputMethodManager().getEnabledInputMethodSubtypeList(this.mImi, true), getContext(), this.mImi);
    }

    private void showSecurityWarnDialog(InputMethodInfo imi) {
        if (this.mDialog != null && this.mDialog.isShowing()) {
            this.mDialog.dismiss();
        }
        Context context = getContext();
        Builder builder = new Builder(context);
        builder.setCancelable(true);
        builder.setTitle(17039380);
        CharSequence label = imi.getServiceInfo().applicationInfo.loadLabel(context.getPackageManager());
        builder.setMessage(context.getString(2131625759, new Object[]{label}));
        builder.setPositiveButton(17039370, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                InputMethodPreference.this.setChecked(true);
                InputMethodPreference.this.mOnSaveListener.onSaveInputMethodPreference(InputMethodPreference.this);
                InputMethodPreference.this.notifyChanged();
            }
        });
        builder.setNegativeButton(17039360, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                InputMethodPreference.this.setChecked(false);
                InputMethodPreference.this.mOnSaveListener.onSaveInputMethodPreference(InputMethodPreference.this);
                InputMethodPreference.this.notifyChanged();
            }
        });
        this.mDialog = builder.create();
        this.mDialog.show();
    }

    int compareTo(InputMethodPreference rhs, Collator collator) {
        int i = -1;
        if (this == rhs) {
            return 0;
        }
        if (this.mHasPriorityInSorting == rhs.mHasPriorityInSorting) {
            CharSequence t0 = getTitle();
            CharSequence t1 = rhs.getTitle();
            if (TextUtils.isEmpty(t0)) {
                return 1;
            }
            if (TextUtils.isEmpty(t1)) {
                return -1;
            }
            return collator.compare(t0.toString(), t1.toString());
        }
        if (!this.mHasPriorityInSorting) {
            i = 1;
        }
        return i;
    }

    public void dismiss() {
        if (this.mDialog != null && this.mDialog.isShowing()) {
            this.mDialog.dismiss();
        }
    }
}
