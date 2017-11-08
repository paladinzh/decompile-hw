package com.android.settings.accounts;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorDescription;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SyncAdapterType;
import android.content.SyncInfo;
import android.content.SyncStatusInfo;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.UserHandle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceClickListener;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceGroup;
import android.support.v7.preference.PreferenceScreen;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.android.settings.AccountPreference;
import com.android.settings.ItemUseStat;
import com.android.settings.MLog;
import com.android.settings.SettingsActivity;
import com.android.settings.SettingsExtUtils;
import com.android.settings.Utils;
import com.android.settings.Utils.ImmersionIcon;
import com.android.settings.location.LocationSettings;
import com.android.settingslib.accounts.AuthenticatorHelper.OnAccountsUpdateListener;
import com.huawei.cust.HwCustUtils;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

public class ManageAccountsSettings extends ManageAccountsSettingsHwBase implements OnAccountsUpdateListener {
    private String[] mAuthorities;
    HwCustAccountSyncSettings mCust = ((HwCustAccountSyncSettings) HwCustUtils.createObj(HwCustAccountSyncSettings.class, new Object[0]));
    private LinearLayout mErrorMsgArea;
    private Account mFirstAccount;

    private class FragmentStarter implements OnPreferenceClickListener {
        private final String mClass;
        private final int mTitleRes;

        public FragmentStarter(String className, int title) {
            this.mClass = className;
            this.mTitleRes = title;
        }

        public boolean onPreferenceClick(Preference preference) {
            ((SettingsActivity) ManageAccountsSettings.this.getActivity()).startPreferencePanel(this.mClass, null, this.mTitleRes, null, null, 0);
            if (this.mClass.equals(LocationSettings.class.getName())) {
                ManageAccountsSettings.this.getActivity().sendBroadcast(new Intent("com.android.settings.accounts.LAUNCHING_LOCATION_SETTINGS"), "android.permission.WRITE_SECURE_SETTINGS");
            }
            return true;
        }
    }

    protected int getMetricsCategory() {
        return 11;
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        Bundle args = getArguments();
        if (args != null && args.containsKey("account_type")) {
            this.mAccountType = args.getString("account_type");
            initializeArgs(this.mAccountType);
        }
        addPreferencesFromResource(2131230812);
        setHasOptionsMenu(true);
    }

    public void onResume() {
        super.onResume();
        this.mAuthenticatorHelper.listenToAccountUpdates();
        updateAuthDescriptions();
        showAccountsIfNeeded();
        showSyncState();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(2130968859, container, false);
        ViewGroup prefs_container = (ViewGroup) view.findViewById(2131886191);
        Utils.prepareCustomPreferencesList(container, view, prefs_container, false);
        prefs_container.addView(super.onCreateView(inflater, prefs_container, savedInstanceState));
        return view;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Activity activity = getActivity();
        this.mErrorMsgArea = (LinearLayout) getView().findViewById(2131886192);
        this.mErrorMsgArea.setVisibility(8);
        this.mAuthorities = activity.getIntent().getStringArrayExtra("authorities");
        Bundle args = getArguments();
        if (args != null && args.containsKey("account_label")) {
            getActivity().setTitle(args.getString("account_label"));
        }
    }

    public void onPause() {
        super.onPause();
        this.mAuthenticatorHelper.stopListeningToAccountUpdates();
    }

    public void onStop() {
        super.onStop();
        Activity activity = getActivity();
        activity.getActionBar().setDisplayOptions(0, 16);
        activity.getActionBar().setCustomView(null);
    }

    public boolean onPreferenceTreeClick(Preference preference) {
        if (!(preference instanceof AccountPreference)) {
            return false;
        }
        ItemUseStat.getInstance().handleClick(getActivity(), 2, "account");
        startAccountSettings((AccountPreference) preference);
        return true;
    }

    private void startAccountSettings(AccountPreference acctPref) {
        Bundle args = new Bundle();
        args.putParcelable("account", acctPref.getAccount());
        addSpecialExtra(args);
        args.putParcelable("android.intent.extra.USER", this.mUserHandle);
        ((SettingsActivity) getActivity()).startPreferencePanel(AccountSyncSettings.class.getCanonicalName(), args, 2131626221, acctPref.getAccount().name, this, 1);
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.add(0, 1, 0, getString(2131627559)).setIcon(SettingsExtUtils.getAlphaStateListDrawable(getResources(), Utils.getImmersionIconId(getActivity(), ImmersionIcon.IMM_REFRESH))).setShowAsAction(2);
        menu.add(0, 2, 0, getString(2131626241)).setIcon(SettingsExtUtils.getAlphaStateListDrawable(getResources(), Utils.getImmersionIconId(getActivity(), ImmersionIcon.IMM_CLOSE))).setShowAsAction(2);
        super.onCreateOptionsMenu(menu, inflater);
    }

    public void onPrepareOptionsMenu(Menu menu) {
        boolean z = true;
        super.onPrepareOptionsMenu(menu);
        boolean syncActive = isSyncEnabled();
        MenuItem findItem = menu.findItem(1);
        if (syncActive) {
            z = false;
        }
        findItem.setVisible(z);
        menu.findItem(2).setVisible(syncActive);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 1:
                ItemUseStat.getInstance().handleClick(getActivity(), 2, "sync_all_accounts");
                requestOrCancelSyncForAccounts(true);
                getActivity().invalidateOptionsMenu();
                return true;
            case 2:
                ItemUseStat.getInstance().handleClick(getActivity(), 2, "cancel_sync_all_accounts");
                requestOrCancelSyncForAccounts(false);
                getActivity().invalidateOptionsMenu();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void requestOrCancelSyncForAccounts(boolean sync) {
        int userId = this.mUserHandle.getIdentifier();
        SyncAdapterType[] syncAdapters = ContentResolver.getSyncAdapterTypesAsUser(userId);
        Bundle extras = new Bundle();
        extras.putBoolean("force", true);
        int count = getPreferenceScreen().getPreferenceCount();
        for (int i = 0; i < count; i++) {
            Preference pref = getPreferenceScreen().getPreference(i);
            if (pref instanceof AccountPreference) {
                Account account = ((AccountPreference) pref).getAccount();
                for (int j = 0; j < syncAdapters.length; j++) {
                    String authroity = this.mIsOnlySyncEmail ? "com.android.email.provider" : syncAdapters[j].authority;
                    if (syncAdapters[j].accountType.equals(account.type) && (ContentResolver.getSyncAutomaticallyAsUser(account, authroity, userId) || (this.mCust != null && this.mCust.shouldNotSkip(account)))) {
                        if (sync) {
                            MLog.d("ManageAccountsSettings", "Request sync for account: " + account.type + ", authority: " + authroity);
                            if (this.mCust != null) {
                                this.mCust.customizeAccountSync(account, extras);
                            }
                            ContentResolver.requestSyncAsUser(account, authroity, userId, extras);
                        } else {
                            ContentResolver.cancelSyncAsUser(account, authroity, userId);
                        }
                    }
                }
            }
        }
    }

    protected void onSyncStateUpdated() {
        showSyncState();
        Activity activity = getActivity();
        if (activity != null) {
            activity.invalidateOptionsMenu();
        }
    }

    private void showSyncState() {
        if (getActivity() != null && !getActivity().isFinishing()) {
            int i;
            getActivity().invalidateOptionsMenu();
            int userId = this.mUserHandle.getIdentifier();
            List<SyncInfo> currentSyncs = ContentResolver.getCurrentSyncsAsUser(userId);
            boolean anySyncFailed = false;
            Date date = new Date();
            SyncAdapterType[] syncAdapters = ContentResolver.getSyncAdapterTypesAsUser(userId);
            HashSet<String> userFacing = new HashSet();
            for (SyncAdapterType sa : syncAdapters) {
                if (sa.isUserVisible()) {
                    userFacing.add(sa.authority);
                }
            }
            int count = getPreferenceScreen().getPreferenceCount();
            for (int i2 = 0; i2 < count; i2++) {
                Preference pref = getPreferenceScreen().getPreference(i2);
                if (pref instanceof AccountPreference) {
                    AccountPreference accountPref = (AccountPreference) pref;
                    Account account = accountPref.getAccount();
                    int syncCount = 0;
                    long lastSuccessTime = 0;
                    boolean syncIsFailing = false;
                    ArrayList<String> authorities = accountPref.getAuthorities();
                    boolean syncingNow = false;
                    if (authorities != null) {
                        for (String authority : authorities) {
                            boolean lastSyncFailed;
                            SyncStatusInfo status = ContentResolver.getSyncStatusAsUser(account, authority, userId);
                            boolean syncEnabled = isSyncEnabled(userId, account, authority);
                            boolean authorityIsPending = ContentResolver.isSyncPending(account, authority);
                            boolean activelySyncing = isSyncing(currentSyncs, account, authority);
                            if (status == null || !syncEnabled || status.lastFailureTime == 0) {
                                lastSyncFailed = false;
                            } else {
                                lastSyncFailed = status.getLastFailureMesgAsInt(0) != 1;
                            }
                            if (!(!lastSyncFailed || activelySyncing || authorityIsPending)) {
                                syncIsFailing = true;
                                anySyncFailed = true;
                            }
                            syncingNow |= activelySyncing;
                            if (status != null && lastSuccessTime < status.lastSuccessTime) {
                                lastSuccessTime = status.lastSuccessTime;
                            }
                            if (syncEnabled && userFacing.contains(authority)) {
                                i = 1;
                            } else {
                                i = 0;
                            }
                            syncCount += i;
                        }
                    } else if (Log.isLoggable("ManageAccountsSettings", 2)) {
                        Log.v("ManageAccountsSettings", "no syncadapters found for " + account);
                    }
                    if (syncIsFailing) {
                        accountPref.setSyncStatus(2, true);
                    } else if (syncCount == 0) {
                        accountPref.setSyncStatus(1, true);
                    } else if (syncCount <= 0) {
                        accountPref.setSyncStatus(1, true);
                    } else if (syncingNow) {
                        accountPref.setSyncStatus(3, true);
                    } else {
                        accountPref.setSyncStatus(0, true);
                        if (lastSuccessTime > 0) {
                            accountPref.setSyncStatus(0, false);
                            date.setTime(lastSuccessTime);
                            String timeString = formatSyncDate(date);
                            accountPref.setSummary((CharSequence) getResources().getString(2131626236, new Object[]{timeString}));
                        }
                    }
                }
            }
            LinearLayout linearLayout = this.mErrorMsgArea;
            if (anySyncFailed) {
                i = 0;
            } else {
                i = 8;
            }
            linearLayout.setVisibility(i);
        }
    }

    private boolean isSyncing(List<SyncInfo> currentSyncs, Account account, String authority) {
        int count = currentSyncs.size();
        for (int i = 0; i < count; i++) {
            SyncInfo syncInfo = (SyncInfo) currentSyncs.get(i);
            if (syncInfo.account.equals(account) && syncInfo.authority.equals(authority)) {
                return true;
            }
        }
        return false;
    }

    private boolean isSyncEnabled(int userId, Account account, String authority) {
        if (ContentResolver.getSyncAutomaticallyAsUser(account, authority, userId) && ContentResolver.getMasterSyncAutomaticallyAsUser(userId) && ContentResolver.getIsSyncableAsUser(account, authority, userId) > 0) {
            return true;
        }
        return false;
    }

    public void onAccountsUpdate(UserHandle userHandle) {
        showAccountsIfNeeded();
        onSyncStateUpdated();
    }

    private void showAccountsIfNeeded() {
        if (getActivity() != null) {
            Account[] accounts = AccountManager.get(getActivity()).getAccountsAsUser(this.mUserHandle.getIdentifier());
            getPreferenceScreen().removeAll();
            this.mFirstAccount = null;
            addPreferencesFromResource(2131230812);
            for (Account account : accounts) {
                if (!isAccountShouldBeIgnored(account.type)) {
                    ArrayList<String> auths;
                    if ("com.android.email".equals(this.mAccountType) && "com.android.exchange".equals(account.type)) {
                        auths = new ArrayList();
                        auths.add("com.android.email.provider");
                    } else {
                        auths = getAuthoritiesForAccountType(account.type);
                    }
                    boolean showAccount = true;
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
                        getPreferenceScreen().addPreference(new AccountPreference(getPrefContext(), account, getDrawableForType(account.type), auths, false));
                        if (this.mFirstAccount == null) {
                            this.mFirstAccount = account;
                        }
                    }
                }
            }
            if (this.mAccountType == null || this.mFirstAccount == null) {
                finish();
            } else {
                addAuthenticatorSettings();
            }
        }
    }

    private void addAuthenticatorSettings() {
        PreferenceScreen prefs = addPreferencesForType(this.mAccountType, getPreferenceScreen());
        if (prefs != null) {
            updatePreferenceIntents(prefs);
        }
    }

    private void updatePreferenceIntents(PreferenceGroup prefs) {
        final PackageManager pm = getActivity().getPackageManager();
        int i = 0;
        while (i < prefs.getPreferenceCount()) {
            Preference pref = prefs.getPreference(i);
            if (pref instanceof PreferenceCategory) {
                pref.setLayoutResource(2130968916);
            }
            if (pref instanceof PreferenceGroup) {
                updatePreferenceIntents((PreferenceGroup) pref);
            }
            Intent intent = pref.getIntent();
            if (intent != null) {
                pref.setWidgetLayoutResource(2130968998);
                if (intent.getAction().equals("android.settings.LOCATION_SOURCE_SETTINGS")) {
                    pref.setOnPreferenceClickListener(new FragmentStarter(LocationSettings.class.getName(), 2131624635));
                } else if (pm.resolveActivityAsUser(intent, 65536, this.mUserHandle.getIdentifier()) == null) {
                    prefs.removePreference(pref);
                } else {
                    intent.putExtra("account", this.mFirstAccount);
                    pref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                        public boolean onPreferenceClick(Preference preference) {
                            Intent prefIntent = preference.getIntent();
                            if (ManageAccountsSettings.this.isSafeIntent(pm, prefIntent)) {
                                ManageAccountsSettings.this.getActivity().startActivityAsUser(prefIntent, ManageAccountsSettings.this.mUserHandle);
                            } else {
                                Log.e("ManageAccountsSettings", "Refusing to launch authenticator intent because it exploits Settings permissions: " + prefIntent);
                            }
                            return true;
                        }
                    });
                }
            }
            i++;
        }
    }

    private boolean isSafeIntent(PackageManager pm, Intent intent) {
        boolean z = true;
        AuthenticatorDescription authDesc = this.mAuthenticatorHelper.getAccountTypeDescription(this.mAccountType);
        ResolveInfo resolveInfo = pm.resolveActivityAsUser(intent, 0, this.mUserHandle.getIdentifier());
        if (resolveInfo == null) {
            return false;
        }
        ActivityInfo resolvedActivityInfo = resolveInfo.activityInfo;
        ApplicationInfo resolvedAppInfo = resolvedActivityInfo.applicationInfo;
        try {
            if (resolvedActivityInfo.exported && (resolvedActivityInfo.permission == null || pm.checkPermission(resolvedActivityInfo.permission, authDesc.packageName) == 0)) {
                return true;
            }
            if (resolvedAppInfo.uid != pm.getApplicationInfo(authDesc.packageName, 0).uid) {
                z = false;
            }
            return z;
        } catch (NameNotFoundException e) {
            Log.e("ManageAccountsSettings", "Intent considered unsafe due to exception.", e);
            return false;
        }
    }

    protected void onAuthDescriptionsUpdated() {
        for (int i = 0; i < getPreferenceScreen().getPreferenceCount(); i++) {
            Preference pref = getPreferenceScreen().getPreference(i);
            if (pref instanceof AccountPreference) {
                AccountPreference accPref = (AccountPreference) pref;
                accPref.setSummary(getLabelForType(accPref.getAccount().type));
            }
        }
    }
}
