package com.android.settings.accounts;

import android.accounts.AccountManager;
import android.accounts.AuthenticatorDescription;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SyncAdapterType;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources.NotFoundException;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.UserManager;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceGroup;
import android.util.Log;
import com.android.internal.util.CharSequences;
import com.android.settings.ItemUseStat;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.RestrictedLockUtils.EnforcedAdmin;
import com.android.settingslib.accounts.AuthenticatorHelper;
import com.android.settingslib.accounts.AuthenticatorHelper.OnAccountsUpdateListener;
import com.google.android.collect.Maps;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class ChooseAccountActivity extends SettingsPreferenceFragment implements OnAccountsUpdateListener {
    private HashMap<String, ArrayList<String>> mAccountTypeToAuthorities = null;
    public HashSet<String> mAccountTypesFilter;
    private PreferenceGroup mAddAccountGroup;
    private AuthenticatorDescription[] mAuthDescs;
    private AuthenticatorHelper mAuthenticatorHelper;
    private String[] mAuthorities;
    private final ArrayList<ProviderEntry> mProviderList = new ArrayList();
    private Map<String, AuthenticatorDescription> mTypeToAuthDescription = new HashMap();
    private UserManager mUm;
    private UserHandle mUserHandle;

    private static class ProviderEntry implements Comparable<ProviderEntry> {
        private final CharSequence name;
        private final String type;

        ProviderEntry(CharSequence providerName, String accountType) {
            this.name = providerName;
            this.type = accountType;
        }

        public int compareTo(ProviderEntry another) {
            if (this.name == null) {
                return -1;
            }
            if (another.name == null) {
                return 1;
            }
            return CharSequences.compareToIgnoreCase(this.name, another.name);
        }
    }

    public void onAccountsUpdate(UserHandle userHandle) {
        updateAuthDescriptions();
    }

    protected int getMetricsCategory() {
        return 10;
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(2131230728);
        this.mAuthorities = getIntent().getStringArrayExtra("authorities");
        String[] accountTypesFilter = getIntent().getStringArrayExtra("account_types");
        if (accountTypesFilter != null) {
            this.mAccountTypesFilter = new HashSet();
            for (String accountType : accountTypesFilter) {
                this.mAccountTypesFilter.add(accountType);
            }
        }
        this.mAddAccountGroup = getPreferenceScreen();
        this.mUm = UserManager.get(getContext());
        this.mUserHandle = Utils.getSecureTargetUser(getActivity().getActivityToken(), this.mUm, null, getIntent().getExtras());
        this.mAuthenticatorHelper = new AuthenticatorHelper(getActivity(), this.mUserHandle, this);
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getActivity().getActionBar() != null) {
            getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    public void onResume() {
        super.onResume();
        if (this.mAuthenticatorHelper != null) {
            this.mAuthenticatorHelper.listenToAccountUpdates();
        }
        updateAuthDescriptions();
    }

    public void onPause() {
        super.onPause();
        if (this.mAuthenticatorHelper != null) {
            this.mAuthenticatorHelper.stopListeningToAccountUpdates();
        }
    }

    private void updateAuthDescriptions() {
        this.mAuthDescs = AccountManager.get(getContext()).getAuthenticatorTypesAsUser(this.mUserHandle.getIdentifier());
        for (int i = 0; i < this.mAuthDescs.length; i++) {
            this.mTypeToAuthDescription.put(this.mAuthDescs[i].type, this.mAuthDescs[i]);
        }
        onAuthDescriptionsUpdated();
    }

    private void onAuthDescriptionsUpdated() {
        this.mProviderList.clear();
        for (AuthenticatorDescription authenticatorDescription : this.mAuthDescs) {
            String accountType = authenticatorDescription.type;
            CharSequence providerName = getLabelForType(accountType);
            ArrayList<String> accountAuths = getAuthoritiesForAccountType(accountType);
            boolean addAccountPref = this.mAuthorities == null;
            if (this.mAuthorities != null && this.mAuthorities.length > 0 && accountAuths != null) {
                addAccountPref = false;
                for (String contains : this.mAuthorities) {
                    if (accountAuths.contains(contains)) {
                        addAccountPref = true;
                        break;
                    }
                }
            }
            if (!(!addAccountPref || this.mAccountTypesFilter == null || this.mAccountTypesFilter.contains(accountType))) {
                addAccountPref = false;
            }
            if (addAccountPref) {
                if (!(AccountExtUtils.shouldBeIgnored(accountType) || ("com.huawei.hwid".equalsIgnoreCase(accountType) && (Utils.hasNewVersionOfHwIDMainPage(getActivity()) || AccountExtUtils.isUserLoggedInHwId(this.mAuthenticatorHelper))))) {
                    this.mProviderList.add(new ProviderEntry(providerName, accountType));
                }
            } else if (Log.isLoggable("ChooseAccountActivity", 2)) {
                Log.v("ChooseAccountActivity", "Skipped pref " + providerName + ": has no authority we need");
            }
        }
        Context context = getPreferenceScreen().getContext();
        if (this.mProviderList.size() == 1) {
            EnforcedAdmin admin = RestrictedLockUtils.checkIfAccountManagementDisabled(context, ((ProviderEntry) this.mProviderList.get(0)).type, this.mUserHandle.getIdentifier());
            if (admin != null) {
                setResult(0, RestrictedLockUtils.getShowAdminSupportDetailsIntent(context, admin));
                finish();
                return;
            }
            finishWithAccountType(((ProviderEntry) this.mProviderList.get(0)).type);
        } else if (this.mProviderList.size() > 0) {
            Collections.sort(this.mProviderList);
            this.mAddAccountGroup.removeAll();
            for (ProviderEntry pref : this.mProviderList) {
                ProviderPreference p = new ProviderPreference(getPreferenceScreen().getContext(), pref.type, getDrawableForType(pref.type), pref.name);
                p.checkAccountManagementAndSetDisabled(this.mUserHandle.getIdentifier());
                this.mAddAccountGroup.addPreference(p);
            }
        } else {
            if (Log.isLoggable("ChooseAccountActivity", 2)) {
                StringBuilder auths = new StringBuilder();
                if (this.mAuthorities != null) {
                    for (String a : this.mAuthorities) {
                        auths.append(a);
                        auths.append(' ');
                    }
                }
                Log.v("ChooseAccountActivity", "No providers found for authorities: " + auths);
            }
            setResult(0);
            finish();
        }
    }

    public ArrayList<String> getAuthoritiesForAccountType(String type) {
        if (this.mAccountTypeToAuthorities == null) {
            this.mAccountTypeToAuthorities = Maps.newHashMap();
        }
        for (SyncAdapterType sa : ContentResolver.getSyncAdapterTypesAsUser(this.mUserHandle.getIdentifier())) {
            ArrayList<String> authorities = (ArrayList) this.mAccountTypeToAuthorities.get(sa.accountType);
            if (authorities == null) {
                authorities = new ArrayList();
                this.mAccountTypeToAuthorities.put(sa.accountType, authorities);
            }
            if (Log.isLoggable("ChooseAccountActivity", 2)) {
                Log.d("ChooseAccountActivity", "added authority " + sa.authority + " to accountType " + sa.accountType);
            }
            if (!authorities.contains(sa.authority)) {
                authorities.add(sa.authority);
            }
        }
        return (ArrayList) this.mAccountTypeToAuthorities.get(type);
    }

    protected Drawable getDrawableForType(String accountType) {
        Drawable icon = null;
        if (this.mTypeToAuthDescription.containsKey(accountType)) {
            try {
                AuthenticatorDescription desc = (AuthenticatorDescription) this.mTypeToAuthDescription.get(accountType);
                icon = getPackageManager().getUserBadgedIcon(getActivity().createPackageContextAsUser(desc.packageName, 0, this.mUserHandle).getDrawable(desc.iconId), this.mUserHandle);
            } catch (NameNotFoundException e) {
                Log.w("ChooseAccountActivity", "No icon name for account type " + accountType);
            } catch (NotFoundException e2) {
                Log.w("ChooseAccountActivity", "No icon resource for account type " + accountType);
            }
        }
        if (icon != null) {
            return icon;
        }
        return getPackageManager().getDefaultActivityIcon();
    }

    protected CharSequence getLabelForType(String accountType) {
        CharSequence label = null;
        if (this.mTypeToAuthDescription.containsKey(accountType)) {
            try {
                AuthenticatorDescription desc = (AuthenticatorDescription) this.mTypeToAuthDescription.get(accountType);
                label = getActivity().createPackageContextAsUser(desc.packageName, 0, this.mUserHandle).getResources().getText(desc.labelId);
            } catch (NameNotFoundException e) {
                Log.w("ChooseAccountActivity", "No label name for account type " + accountType);
            } catch (NotFoundException e2) {
                Log.w("ChooseAccountActivity", "No label resource for account type " + accountType);
            }
        }
        return label;
    }

    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference instanceof ProviderPreference) {
            ItemUseStat.getInstance().handleClick(getActivity(), 2, "accounts");
            ProviderPreference pref = (ProviderPreference) preference;
            if (Log.isLoggable("ChooseAccountActivity", 2)) {
                Log.v("ChooseAccountActivity", "Attempting to add account of type " + pref.getAccountType());
            }
            finishWithAccountType(pref.getAccountType());
        }
        return true;
    }

    private void finishWithAccountType(String accountType) {
        Intent intent = new Intent();
        intent.putExtra("selected_account", accountType);
        intent.putExtra("android.intent.extra.USER", this.mUserHandle);
        setResult(-1, intent);
        finish();
    }
}
