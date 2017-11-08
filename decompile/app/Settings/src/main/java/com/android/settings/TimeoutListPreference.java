package com.android.settings;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.DialogInterface.OnClickListener;
import android.support.v7.preference.PreferenceViewHolder;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.RestrictedLockUtils.EnforcedAdmin;
import java.util.ArrayList;

public class TimeoutListPreference extends RestrictedListPreference {
    private EnforcedAdmin mAdmin;
    private CharSequence[] mInitialEntries = getEntries();
    private final CharSequence[] mInitialValues = getEntryValues();
    private CharSequence mNetherSummary;

    public TimeoutListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected void onPrepareDialogBuilder(Builder builder, OnClickListener listener) {
        super.onPrepareDialogBuilder(builder, listener);
        if (this.mAdmin != null) {
            builder.setView(2130968614);
        } else {
            builder.setView(null);
        }
    }

    protected void onDialogCreated(Dialog dialog) {
        super.onDialogCreated(dialog);
        dialog.create();
        if (this.mAdmin != null) {
            dialog.findViewById(2131886209).findViewById(2131886210).setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    RestrictedLockUtils.sendShowAdminSupportDetailsIntent(TimeoutListPreference.this.getContext(), TimeoutListPreference.this.mAdmin);
                }
            });
        }
    }

    public void removeUnusableTimeouts(long maxTimeout, EnforcedAdmin admin) {
        if (((DevicePolicyManager) getContext().getSystemService("device_policy")) != null) {
            if (admin != null || this.mAdmin != null || isDisabledByAdmin()) {
                if (admin == null) {
                    maxTimeout = Long.MAX_VALUE;
                }
                ArrayList<CharSequence> revisedEntries = new ArrayList();
                ArrayList<CharSequence> revisedValues = new ArrayList();
                if (this.mInitialEntries == null || this.mInitialEntries.length == 0) {
                    this.mInitialEntries = getEntries();
                }
                if (this.mInitialEntries != null && this.mInitialEntries.length > 0 && this.mInitialValues != null && this.mInitialValues.length > 0) {
                    for (int i = 0; i < this.mInitialValues.length; i++) {
                        if (Long.parseLong(this.mInitialValues[i].toString()) <= maxTimeout) {
                            revisedEntries.add(this.mInitialEntries[i]);
                            revisedValues.add(this.mInitialValues[i]);
                        }
                    }
                }
                if (revisedValues.size() == 0) {
                    setDisabledByAdmin(admin);
                    return;
                }
                setDisabledByAdmin(null);
                if (revisedEntries.size() != getEntries().length) {
                    int userPreference = Integer.parseInt(getValue());
                    setEntries((CharSequence[]) revisedEntries.toArray(new CharSequence[0]));
                    setEntryValues((CharSequence[]) revisedValues.toArray(new CharSequence[0]));
                    this.mAdmin = admin;
                    if (((long) userPreference) <= maxTimeout) {
                        setValue(String.valueOf(userPreference));
                    } else if (revisedValues.size() > 0 && Long.parseLong(((CharSequence) revisedValues.get(revisedValues.size() - 1)).toString()) == maxTimeout) {
                        setValue(String.valueOf(maxTimeout));
                    }
                }
            }
        }
    }

    public void onBindViewHolder(PreferenceViewHolder view) {
        TextView netherSummaryView = (TextView) view.findViewById(2131886914);
        if (netherSummaryView != null) {
            CharSequence summary = getNetherSummary();
            if (TextUtils.isEmpty(summary)) {
                netherSummaryView.setVisibility(8);
            } else {
                netherSummaryView.setText(summary);
                netherSummaryView.setVisibility(0);
            }
        }
        super.onBindViewHolder(view);
    }

    public CharSequence getNetherSummary() {
        return this.mNetherSummary;
    }

    public void setNetherSummary(CharSequence summary) {
        if (summary != null || this.mNetherSummary == null) {
            if (summary == null) {
                return;
            }
            if (summary.equals(this.mNetherSummary)) {
                return;
            }
        }
        this.mNetherSummary = summary;
        notifyChanged();
    }
}
