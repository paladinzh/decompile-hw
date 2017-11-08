package com.android.settings.accounts;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Intent;
import android.database.ContentObserver;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.UserHandle;
import android.provider.Settings.Secure;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import com.android.settings.AccountPreference;
import com.android.settings.PrivacyModeManager;
import com.android.settings.SettingsExtUtils;
import com.android.settings.Utils;
import com.android.settings.Utils.ImmersionIcon;
import com.android.settingslib.accounts.AuthenticatorHelper.OnAccountsUpdateListener;
import com.huawei.cust.HwCustUtils;
import java.util.ArrayList;

public class SyncSettings extends AccountPreferenceBase implements OnAccountsUpdateListener {
    protected String[] mAuthorities;
    protected Preference mCloudServicePreference;
    protected HwCustSyncSettings mCust;
    private ContentObserver mPrivacyModeObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange) {
            SyncSettings.this.createHierarchy();
        }
    };

    public /* bridge */ /* synthetic */ PreferenceScreen addPreferencesForType(String accountType, PreferenceScreen parent) {
        return super.addPreferencesForType(accountType, parent);
    }

    public /* bridge */ /* synthetic */ ArrayList getAuthoritiesForAccountType(String type) {
        return super.getAuthoritiesForAccountType(type);
    }

    public /* bridge */ /* synthetic */ void onPause() {
        super.onPause();
    }

    public /* bridge */ /* synthetic */ void updateAuthDescriptions() {
        super.updateAuthDescriptions();
    }

    protected void initializeCloudServicePreference() {
        this.mCloudServicePreference = getPreferenceScreen().findPreference("cloud_service");
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        this.mCust = (HwCustSyncSettings) HwCustUtils.createObj(HwCustSyncSettings.class, new Object[0]);
        createHierarchy();
        registerPrivacyModeObserver();
        setHasOptionsMenu(true);
    }

    private void createHierarchy() {
        PreferenceScreen root = getPreferenceScreen();
        if (root != null) {
            root.removeAll();
        }
        addPreferencesFromResource(2131230907);
        initializeCloudServicePreference();
    }

    public void onDestroy() {
        unregisterPrivacyModeObserver();
        super.onDestroy();
    }

    public void onResume() {
        super.onResume();
        AccountExtUtils.removeRedundantMargin(getActivity());
        AccountExtUtils.updateHwCloudServicePreference(getPreferenceScreen(), getActivity(), this.mCloudServicePreference);
        updateAuthDescriptions();
        onAccountsUpdate(Binder.getCallingUserHandle());
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.add(0, 1, 0, 2131626223).setIcon(SettingsExtUtils.getAlphaStateListDrawable(getResources(), Utils.getImmersionIconId(getActivity(), ImmersionIcon.IMM_ADD))).setShowAsAction(2);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (1 == item.getItemId()) {
            Intent intent = new Intent("android.settings.ADD_ACCOUNT_SETTINGS");
            intent.putExtra("authorities", this.mAuthorities);
            startActivity(intent);
            SettingsExtUtils.setAnimationReflection(getActivity());
        }
        return super.onOptionsItemSelected(item);
    }

    public void onStart() {
        super.onStart();
        this.mAuthenticatorHelper.listenToAccountUpdates();
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.mAuthorities = getActivity().getIntent().getStringArrayExtra("authorities");
    }

    public void onStop() {
        super.onStop();
        this.mAuthenticatorHelper.stopListeningToAccountUpdates();
    }

    public boolean onPreferenceTreeClick(Preference preference) {
        super.onPreferenceTreeClick(preference);
        if (preference == this.mCloudServicePreference) {
            Intent intent = preference.getIntent();
            if (intent != null) {
                getActivity().startActivity(intent);
            }
            SettingsExtUtils.setAnimationReflection(getActivity());
            return true;
        } else if (!(preference instanceof AccountPreference)) {
            return false;
        } else {
            if (this.mCust == null || !this.mCust.startCustAccountSettings((AccountPreference) preference, getActivity())) {
                startAccountSettings((AccountPreference) preference);
            }
            return true;
        }
    }

    private void startAccountSettings(AccountPreference acctPref) {
        Intent intent = new Intent("android.settings.ACCOUNT_SYNC_SETTINGS");
        intent.putExtra("account", acctPref.getAccount());
        intent.setPackage("com.android.settings");
        startActivity(intent);
    }

    private void removeAccountPreferences() {
        PreferenceScreen parent = getPreferenceScreen();
        int i = 0;
        while (i < parent.getPreferenceCount()) {
            if (parent.getPreference(i) instanceof AccountPreference) {
                parent.removePreference(parent.getPreference(i));
            } else {
                i++;
            }
        }
    }

    public void onAccountsUpdate(UserHandle userHandle) {
        if (getActivity() != null) {
            removeAccountPreferences();
            for (Account account : AccountManager.get(getActivity()).getAccountsAsUser(this.mUserHandle.getIdentifier())) {
                ArrayList<String> auths = getAuthoritiesForAccountType(account.type);
                if (!AccountExtUtils.shouldBeIgnored(account.type)) {
                    boolean showAccount = this.mAuthorities == null;
                    if (this.mAuthorities != null && auths != null) {
                        showAccount = false;
                        for (String requestedAuthority : this.mAuthorities) {
                            if (auths.contains(requestedAuthority)) {
                                showAccount = true;
                                break;
                            }
                        }
                    }
                    if (showAccount) {
                        AccountPreference preference = new AccountPreference(getActivity(), account, getDrawableForType(account.type), auths, true);
                        getPreferenceScreen().addPreference(preference);
                        preference.setSummary(getLabelForType(account.type));
                    }
                }
            }
            onSyncStateUpdated();
        }
    }

    protected void onAuthDescriptionsUpdated() {
        for (int i = 0; i < getPreferenceScreen().getPreferenceCount(); i++) {
            if (getPreferenceScreen().getPreference(i) instanceof AccountPreference) {
                AccountPreference accPref = (AccountPreference) getPreferenceScreen().getPreference(i);
                accPref.setIcon(getDrawableForType(accPref.getAccount().type));
                accPref.setSummary(getLabelForType(accPref.getAccount().type));
            }
        }
    }

    private void registerPrivacyModeObserver() {
        if (PrivacyModeManager.isFeatrueSupported()) {
            getContentResolver().registerContentObserver(Secure.getUriFor("privacy_mode_state"), true, this.mPrivacyModeObserver);
        }
    }

    private void unregisterPrivacyModeObserver() {
        if (PrivacyModeManager.isFeatrueSupported()) {
            getContentResolver().unregisterContentObserver(this.mPrivacyModeObserver);
        }
    }

    protected int getMetricsCategory() {
        return 100000;
    }
}
