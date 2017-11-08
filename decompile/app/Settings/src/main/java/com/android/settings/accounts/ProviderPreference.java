package com.android.settings.accounts;

import android.content.Context;
import android.graphics.drawable.Drawable;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.RestrictedPreference;

public class ProviderPreference extends RestrictedPreference {
    private String mAccountType;

    public ProviderPreference(Context context, String accountType, Drawable icon, CharSequence providerName) {
        super(context);
        setLayoutResource(2130969013);
        this.mAccountType = accountType;
        setIcon(icon);
        setPersistent(false);
        setTitle(providerName);
        setWidgetLayoutResource(2130968998);
        useAdminDisabledSummary(true);
    }

    public String getAccountType() {
        return this.mAccountType;
    }

    public void checkAccountManagementAndSetDisabled(int userId) {
        setDisabledByAdmin(RestrictedLockUtils.checkIfAccountManagementDisabled(getContext(), getAccountType(), userId));
    }
}
