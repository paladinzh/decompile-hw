package com.android.settingslib;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v14.preference.SwitchPreference;
import android.support.v4.content.res.TypedArrayUtils;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.preference.PreferenceViewHolder;
import android.support.v7.preference.R$attr;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

public class RestrictedSwitchPreference extends SwitchPreference {
    RestrictedPreferenceHelper mHelper;
    String mRestrictedSwitchSummary;
    boolean mUseAdditionalSummary;

    public RestrictedSwitchPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mUseAdditionalSummary = false;
        this.mRestrictedSwitchSummary = null;
        setWidgetLayoutResource(R$layout.restricted_switch_widget);
        this.mHelper = new RestrictedPreferenceHelper(context, this, attrs);
        if (attrs != null) {
            TypedArray attributes = context.obtainStyledAttributes(attrs, R$styleable.RestrictedSwitchPreference);
            TypedValue useAdditionalSummary = attributes.peekValue(R$styleable.RestrictedSwitchPreference_useAdditionalSummary);
            if (useAdditionalSummary != null) {
                boolean z = useAdditionalSummary.type == 18 ? useAdditionalSummary.data != 0 : false;
                this.mUseAdditionalSummary = z;
            }
            TypedValue restrictedSwitchSummary = attributes.peekValue(R$styleable.RestrictedSwitchPreference_restrictedSwitchSummary);
            CharSequence data = null;
            if (restrictedSwitchSummary != null && restrictedSwitchSummary.type == 3) {
                data = restrictedSwitchSummary.resourceId != 0 ? context.getString(restrictedSwitchSummary.resourceId) : restrictedSwitchSummary.string;
            }
            this.mRestrictedSwitchSummary = data == null ? null : data.toString();
        }
        if (this.mRestrictedSwitchSummary == null) {
            this.mRestrictedSwitchSummary = context.getString(R$string.disabled_by_admin);
        }
        if (this.mUseAdditionalSummary) {
            setLayoutResource(R$layout.restricted_switch_preference);
            useAdminDisabledSummary(false);
        }
    }

    public RestrictedSwitchPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public RestrictedSwitchPreference(Context context, AttributeSet attrs) {
        this(context, attrs, TypedArrayUtils.getAttr(context, R$attr.switchPreferenceStyle, 16843629));
    }

    public void onBindViewHolder(PreferenceViewHolder holder) {
        int i;
        super.onBindViewHolder(holder);
        this.mHelper.onBindViewHolder(holder);
        View restrictedIcon = holder.findViewById(R$id.restricted_icon);
        View switchWidget = holder.findViewById(16908352);
        if (restrictedIcon != null) {
            if (isDisabledByAdmin()) {
                i = 0;
            } else {
                i = 8;
            }
            restrictedIcon.setVisibility(i);
        }
        if (switchWidget != null) {
            if (isDisabledByAdmin()) {
                i = 8;
            } else {
                i = 0;
            }
            switchWidget.setVisibility(i);
        }
        if (this.mUseAdditionalSummary) {
            TextView additionalSummaryView = (TextView) holder.findViewById(R$id.additional_summary);
            if (additionalSummaryView == null) {
                return;
            }
            if (isDisabledByAdmin()) {
                additionalSummaryView.setText(this.mRestrictedSwitchSummary);
                additionalSummaryView.setVisibility(0);
                return;
            }
            additionalSummaryView.setVisibility(8);
            return;
        }
        TextView summaryView = (TextView) holder.findViewById(16908304);
        if (summaryView != null && isDisabledByAdmin()) {
            summaryView.setText(this.mRestrictedSwitchSummary);
            summaryView.setVisibility(0);
        }
    }

    public void performClick() {
        if (!this.mHelper.performClick()) {
            super.performClick();
        }
    }

    public void useAdminDisabledSummary(boolean useSummary) {
        this.mHelper.useAdminDisabledSummary(useSummary);
    }

    protected void onAttachedToHierarchy(PreferenceManager preferenceManager) {
        this.mHelper.onAttachedToHierarchy();
        super.onAttachedToHierarchy(preferenceManager);
    }

    public void setEnabled(boolean enabled) {
        if (enabled && isDisabledByAdmin()) {
            this.mHelper.setDisabledByAdmin(null);
        } else {
            super.setEnabled(enabled);
        }
    }

    public boolean isDisabledByAdmin() {
        return this.mHelper.isDisabledByAdmin();
    }
}
