package com.android.settingslib;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.UserHandle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.TextView;
import com.android.settingslib.RestrictedLockUtils.EnforcedAdmin;

public class RestrictedPreferenceHelper {
    private String mAttrUserRestriction = null;
    private final Context mContext;
    private boolean mDisabledByAdmin;
    private EnforcedAdmin mEnforcedAdmin;
    private final Preference mPreference;
    private boolean mUseAdminDisabledSummary = false;

    public RestrictedPreferenceHelper(Context context, Preference preference, AttributeSet attrs) {
        this.mContext = context;
        this.mPreference = preference;
        if (attrs != null) {
            TypedArray attributes = context.obtainStyledAttributes(attrs, R$styleable.RestrictedPreference);
            TypedValue userRestriction = attributes.peekValue(R$styleable.RestrictedPreference_userRestriction);
            CharSequence data = null;
            if (userRestriction != null && userRestriction.type == 3) {
                data = userRestriction.resourceId != 0 ? context.getText(userRestriction.resourceId) : userRestriction.string;
            }
            this.mAttrUserRestriction = data == null ? null : data.toString();
            if (RestrictedLockUtils.hasBaseUserRestriction(this.mContext, this.mAttrUserRestriction, UserHandle.myUserId())) {
                this.mAttrUserRestriction = null;
                return;
            }
            TypedValue useAdminDisabledSummary = attributes.peekValue(R$styleable.RestrictedPreference_useAdminDisabledSummary);
            if (useAdminDisabledSummary != null) {
                boolean z = useAdminDisabledSummary.type == 18 ? useAdminDisabledSummary.data != 0 : false;
                this.mUseAdminDisabledSummary = z;
            }
        }
    }

    public void onBindViewHolder(PreferenceViewHolder holder) {
        if (this.mDisabledByAdmin) {
            holder.itemView.setEnabled(true);
        }
        if (this.mUseAdminDisabledSummary) {
            TextView summaryView = (TextView) holder.findViewById(16908304);
            if (summaryView == null) {
                return;
            }
            if (this.mDisabledByAdmin) {
                summaryView.setText(R$string.disabled_by_admin_summary_text);
                summaryView.setVisibility(0);
                return;
            }
            summaryView.setVisibility(8);
        }
    }

    public void useAdminDisabledSummary(boolean useSummary) {
        this.mUseAdminDisabledSummary = useSummary;
    }

    public boolean performClick() {
        if (!this.mDisabledByAdmin) {
            return false;
        }
        RestrictedLockUtils.sendShowAdminSupportDetailsIntent(this.mContext, this.mEnforcedAdmin);
        return true;
    }

    public void onAttachedToHierarchy() {
        if (this.mAttrUserRestriction != null) {
            checkRestrictionAndSetDisabled(this.mAttrUserRestriction, UserHandle.myUserId());
        }
    }

    public void checkRestrictionAndSetDisabled(String userRestriction, int userId) {
        setDisabledByAdmin(RestrictedLockUtils.checkIfRestrictionEnforced(this.mContext, userRestriction, userId));
    }

    public boolean setDisabledByAdmin(EnforcedAdmin admin) {
        boolean disabled = admin != null;
        this.mEnforcedAdmin = admin;
        boolean changed = false;
        if (this.mDisabledByAdmin != disabled) {
            this.mDisabledByAdmin = disabled;
            changed = true;
        }
        this.mPreference.setEnabled(!disabled);
        return changed;
    }

    public boolean isDisabledByAdmin() {
        return this.mDisabledByAdmin;
    }
}
