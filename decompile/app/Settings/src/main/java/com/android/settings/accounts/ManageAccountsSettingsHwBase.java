package com.android.settings.accounts;

import android.content.ContentResolver;
import android.content.SyncInfo;
import android.os.Bundle;
import android.os.UserHandle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceClickListener;
import android.support.v7.preference.PreferenceScreen;
import com.android.settings.SettingsExtUtils;
import java.util.ArrayList;

public class ManageAccountsSettingsHwBase extends AccountPreferenceBase {
    protected String mAccountType = "illegal_account_type";
    protected ArrayList<String> mAccountTypeRelated = new ArrayList();
    protected boolean mIsOnlySyncEmail = false;

    /* renamed from: com.android.settings.accounts.ManageAccountsSettingsHwBase$1 */
    class AnonymousClass1 implements OnPreferenceClickListener {
        final /* synthetic */ ManageAccountsSettingsHwBase this$0;

        public boolean onPreferenceClick(Preference preference) {
            try {
                this.this$0.getActivity().startActivity(preference.getIntent());
                SettingsExtUtils.setAnimationReflection(this.this$0.getActivity());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        }
    }

    public /* bridge */ /* synthetic */ PreferenceScreen addPreferencesForType(String accountType, PreferenceScreen parent) {
        return super.addPreferencesForType(accountType, parent);
    }

    public /* bridge */ /* synthetic */ ArrayList getAuthoritiesForAccountType(String type) {
        return super.getAuthoritiesForAccountType(type);
    }

    public /* bridge */ /* synthetic */ void onAccountsUpdate(UserHandle userHandle) {
        super.onAccountsUpdate(userHandle);
    }

    public /* bridge */ /* synthetic */ void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    public /* bridge */ /* synthetic */ void onCreate(Bundle icicle) {
        super.onCreate(icicle);
    }

    public /* bridge */ /* synthetic */ void onPause() {
        super.onPause();
    }

    public /* bridge */ /* synthetic */ void onResume() {
        super.onResume();
    }

    public /* bridge */ /* synthetic */ void updateAuthDescriptions() {
        super.updateAuthDescriptions();
    }

    protected void initializeArgs(String accountType) {
        if (this.mAccountTypeRelated != null) {
            this.mAccountTypeRelated.clear();
        } else {
            this.mAccountTypeRelated = new ArrayList();
        }
        if ("com.android.email".equals(accountType)) {
            this.mAccountTypeRelated.add("com.android.exchange");
            this.mIsOnlySyncEmail = true;
        }
    }

    protected void addSpecialExtra(Bundle args) {
        if (this.mIsOnlySyncEmail) {
            args.putBoolean("only_sync_email", this.mIsOnlySyncEmail);
        }
    }

    protected boolean isAccountShouldBeIgnored(String accountType) {
        if (AccountExtUtils.shouldBeIgnored(accountType)) {
            return true;
        }
        if (accountType == null || accountType.equals(this.mAccountType) || isRelatedAccount(accountType)) {
            return false;
        }
        return true;
    }

    protected boolean isSyncEnabled() {
        if (this.mAccountType == null) {
            return false;
        }
        boolean syncActive = false;
        String accountType = "";
        for (SyncInfo syncInfo : ContentResolver.getCurrentSyncsAsUser(this.mUserHandle.getIdentifier())) {
            accountType = syncInfo.account.type;
            if (!accountType.equals(this.mAccountType)) {
                if (isRelatedAccount(accountType) && "com.android.email.provider".equals(syncInfo.authority)) {
                    syncActive = true;
                    break;
                }
            } else {
                syncActive = true;
                break;
            }
        }
        return syncActive;
    }

    private boolean isRelatedAccount(String accountType) {
        if (this.mAccountTypeRelated != null) {
            return this.mAccountTypeRelated.contains(accountType);
        }
        return false;
    }

    protected int getMetricsCategory() {
        return 100000;
    }
}
