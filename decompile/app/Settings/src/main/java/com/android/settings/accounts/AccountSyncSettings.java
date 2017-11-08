package com.android.settings.accounts;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorDescription;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SyncAdapterType;
import android.content.SyncInfo;
import android.content.SyncStatusInfo;
import android.content.pm.ProviderInfo;
import android.content.pm.UserInfo;
import android.os.Binder;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.UserHandle;
import android.os.UserManager;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceScreen;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.settings.MLog;
import com.android.settings.SettingsExtUtils;
import com.android.settings.Utils;
import com.android.settings.Utils.ImmersionIcon;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.RestrictedLockUtils.EnforcedAdmin;
import com.google.android.collect.Lists;
import com.huawei.cust.HwCustUtils;
import com.huawei.hsm.permission.StubController;
import com.huawei.permission.IHoldService;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class AccountSyncSettings extends AccountPreferenceBase implements OnPreferenceChangeListener {
    private Account mAccount;
    HwCustAccountSyncSettings mCust = ((HwCustAccountSyncSettings) HwCustUtils.createObj(HwCustAccountSyncSettings.class, new Object[0]));
    private LinearLayout mErrorMsgArea;
    private ArrayList<SyncAdapterType> mInvisibleAdapters = Lists.newArrayList();
    private boolean mIsOnlySyncEmail = false;
    private ImageView mProviderIcon;
    private TextView mProviderId;
    private ArrayList<SyncStateSwitchPreference> mSwitches = new ArrayList();
    private TextView mUserId;

    public /* bridge */ /* synthetic */ PreferenceScreen addPreferencesForType(String accountType, PreferenceScreen parent) {
        return super.addPreferencesForType(accountType, parent);
    }

    public /* bridge */ /* synthetic */ ArrayList getAuthoritiesForAccountType(String type) {
        return super.getAuthoritiesForAccountType(type);
    }

    public /* bridge */ /* synthetic */ void updateAuthDescriptions() {
        super.updateAuthDescriptions();
    }

    public Dialog onCreateDialog(int id) {
        if (id == 100) {
            return new Builder(getActivity()).setTitle(2131626254).setMessage(2131626255).setNegativeButton(17039360, null).setPositiveButton(2131626251, new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    if (AccountSyncSettings.this.mAccount == null) {
                        MLog.e("AccountSettings", "account is null, can not sync!");
                        AccountSyncSettings.this.finish();
                        return;
                    }
                    try {
                        Activity activity = AccountSyncSettings.this.getActivity();
                        AccountManager.get(activity).removeAccountAsUser(AccountSyncSettings.this.mAccount, activity, new AccountManagerCallback<Bundle>() {
                            public void run(AccountManagerFuture<Bundle> future) {
                                if (AccountSyncSettings.this.isResumed()) {
                                    boolean failed = true;
                                    try {
                                        if (((Bundle) future.getResult()).getBoolean("booleanResult")) {
                                            failed = false;
                                        }
                                    } catch (OperationCanceledException e) {
                                    } catch (IOException e2) {
                                    } catch (AuthenticatorException e3) {
                                    }
                                    if (failed) {
                                        try {
                                            if (!(AccountSyncSettings.this.getActivity() == null || AccountSyncSettings.this.getActivity().isFinishing())) {
                                                AccountSyncSettings.this.showDialog(101);
                                            }
                                        } catch (Exception e4) {
                                            e4.printStackTrace();
                                        }
                                    }
                                    AccountSyncSettings.this.finish();
                                }
                            }
                        }, null, AccountSyncSettings.this.mUserHandle);
                    } catch (NullPointerException e) {
                        AccountSyncSettings.this.finish();
                    }
                }
            }).create();
        }
        if (id == 101) {
            return new Builder(getActivity()).setTitle(2131626254).setPositiveButton(17039370, null).setMessage(2131626256).create();
        }
        if (id == 102) {
            return new Builder(getActivity()).setTitle(2131626259).setMessage(2131626260).setPositiveButton(17039370, null).create();
        }
        return null;
    }

    protected int getMetricsCategory() {
        return 9;
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setPreferenceScreen(null);
        addPreferencesFromResource(2131230727);
        setAccessibilityTitle();
        setHasOptionsMenu(true);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(2130968605, container, false);
        ViewGroup prefs_container = (ViewGroup) view.findViewById(2131886191);
        Utils.prepareCustomPreferencesList(container, view, prefs_container, false);
        prefs_container.addView(super.onCreateView(inflater, prefs_container, savedInstanceState));
        initializeUi(view);
        return view;
    }

    protected void initializeUi(View rootView) {
        this.mErrorMsgArea = (LinearLayout) rootView.findViewById(2131886192);
        this.mErrorMsgArea.setVisibility(8);
        this.mUserId = (TextView) rootView.findViewById(2131887253);
        this.mProviderId = (TextView) rootView.findViewById(2131887254);
        this.mProviderIcon = (ImageView) rootView.findViewById(2131887252);
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Bundle arguments = getArguments();
        if (arguments == null) {
            Log.e("AccountSettings", "No arguments provided when starting intent. ACCOUNT_KEY needed.");
            finish();
            return;
        }
        this.mAccount = (Account) arguments.getParcelable("account");
        if (accountExists(this.mAccount)) {
            if (Log.isLoggable("AccountSettings", 2)) {
                Log.v("AccountSettings", "Got account: " + this.mAccount);
            }
            this.mUserId.setText(this.mAccount.name);
            this.mProviderId.setText(this.mAccount.type);
            this.mIsOnlySyncEmail = arguments.getBoolean("only_sync_email");
            return;
        }
        Log.e("AccountSettings", "Account provided does not exist: " + this.mAccount);
        finish();
    }

    private void setAccessibilityTitle() {
        int i;
        UserInfo user = ((UserManager) getSystemService("user")).getUserInfo(this.mUserHandle.getIdentifier());
        boolean isManagedProfile = user != null ? user.isManagedProfile() : false;
        CharSequence currentTitle = getActivity().getTitle();
        if (isManagedProfile) {
            i = 2131625145;
        } else {
            i = 2131625146;
        }
        getActivity().setTitle(Utils.createAccessibleSequence(currentTitle, getString(i, new Object[]{currentTitle})));
    }

    public void onResume() {
        removePreference("dummy");
        this.mAuthenticatorHelper.listenToAccountUpdates();
        updateAuthDescriptions();
        onAccountsUpdate(Binder.getCallingUserHandle());
        super.onResume();
        if (this.mAccount == null) {
            MLog.e("AccountSettings", "account is null, can not sync!");
            finish();
        }
    }

    public void onPause() {
        super.onPause();
        this.mAuthenticatorHelper.stopListeningToAccountUpdates();
    }

    private void addSyncStateSwitch(Account account, String authority) {
        SyncStateSwitchPreference item = new SyncStateSwitchPreference(getPrefContext(), account, authority);
        item.setPersistent(false);
        ProviderInfo providerInfo = getPackageManager().resolveContentProviderAsUser(authority, 0, this.mUserHandle.getIdentifier());
        if (providerInfo != null) {
            if (TextUtils.isEmpty(providerInfo.loadLabel(getPackageManager()))) {
                Log.e("AccountSettings", "Provider needs a label for authority '" + authority + "'");
                return;
            }
            item.setTitle((CharSequence) getString(2131626258, new Object[]{providerInfo.loadLabel(getPackageManager())}));
            item.setKey(authority);
            item.setOnPreferenceChangeListener(this);
            this.mSwitches.add(item);
        }
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        MenuItem syncNow = menu.add(0, 1, 0, getString(2131626240)).setIcon(SettingsExtUtils.getAlphaStateListDrawable(getResources(), Utils.getImmersionIconId(getActivity(), ImmersionIcon.IMM_REFRESH)));
        MenuItem syncCancel = menu.add(0, 2, 0, getString(2131626241)).setIcon(SettingsExtUtils.getAlphaStateListDrawable(getResources(), Utils.getImmersionIconId(getActivity(), ImmersionIcon.IMM_CLOSE)));
        if (!(RestrictedLockUtils.hasBaseUserRestriction(getPrefContext(), "no_modify_accounts", this.mUserHandle.getIdentifier()) || this.mAccount == null)) {
            MenuItem removeAccount = menu.add(0, 3, 0, getString(2131626251)).setIcon(SettingsExtUtils.getAlphaStateListDrawable(getResources(), Utils.getImmersionIconId(getActivity(), ImmersionIcon.IMM_DELETE)));
            removeAccount.setShowAsAction(2);
            EnforcedAdmin admin = RestrictedLockUtils.checkIfRestrictionEnforced(getPrefContext(), "no_modify_accounts", this.mUserHandle.getIdentifier());
            if (admin == null) {
                admin = RestrictedLockUtils.checkIfAccountManagementDisabled(getPrefContext(), this.mAccount.type, this.mUserHandle.getIdentifier());
            }
            RestrictedLockUtils.setMenuItemAsDisabledByAdmin(getPrefContext(), removeAccount, admin);
        }
        syncNow.setShowAsAction(2);
        syncCancel.setShowAsAction(2);
        super.onCreateOptionsMenu(menu, inflater);
    }

    public void onPrepareOptionsMenu(Menu menu) {
        boolean z = true;
        super.onPrepareOptionsMenu(menu);
        boolean syncActive = false;
        for (SyncInfo syncInfo : ContentResolver.getCurrentSyncsAsUser(this.mUserHandle.getIdentifier())) {
            if (syncInfo.account.equals(this.mAccount)) {
                syncActive = true;
                break;
            }
        }
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
                startSyncForEnabledProviders();
                return true;
            case 2:
                cancelSyncForEnabledProviders();
                return true;
            case 3:
                showDialog(100);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public boolean handleSyncStateChange(Preference preference) {
        if (!(preference instanceof SyncStateSwitchPreference)) {
            return false;
        }
        SyncStateSwitchPreference syncPref = (SyncStateSwitchPreference) preference;
        String authority = syncPref.getAuthority();
        Account account = syncPref.getAccount();
        int userId = this.mUserHandle.getIdentifier();
        boolean syncAutomatically = ContentResolver.getSyncAutomaticallyAsUser(account, authority, userId);
        if (syncPref.isOneTimeSyncMode()) {
            requestOrCancelSync(account, authority, true);
        } else {
            boolean syncOn = syncPref.isChecked();
            boolean oldSyncState = syncAutomatically;
            if (syncOn != syncAutomatically) {
                ContentResolver.setSyncAutomaticallyAsUser(account, authority, syncOn, userId);
                if (this.mCust != null) {
                    this.mCust.customizeAutoSync(account, getActivity());
                }
                if (!(ContentResolver.getMasterSyncAutomaticallyAsUser(userId) && syncOn)) {
                    requestOrCancelSync(account, authority, syncOn);
                }
            }
        }
        return true;
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        return handleSyncStateChange(preference);
    }

    public boolean onPreferenceTreeClick(Preference preference) {
        if (handleSyncStateChange(preference)) {
            return true;
        }
        return super.onPreferenceTreeClick(preference);
    }

    private void startSyncForEnabledProviders() {
        requestOrCancelSyncForEnabledProviders(true);
        Activity activity = getActivity();
        if (activity != null) {
            activity.invalidateOptionsMenu();
        }
    }

    private void cancelSyncForEnabledProviders() {
        requestOrCancelSyncForEnabledProviders(false);
        Activity activity = getActivity();
        if (activity != null) {
            activity.invalidateOptionsMenu();
        }
    }

    private void requestOrCancelSyncForEnabledProviders(boolean startSync) {
        int count = getPreferenceScreen().getPreferenceCount();
        for (int i = 0; i < count; i++) {
            Preference pref = getPreferenceScreen().getPreference(i);
            if (pref instanceof SyncStateSwitchPreference) {
                SyncStateSwitchPreference syncPref = (SyncStateSwitchPreference) pref;
                if (syncPref.isChecked() || (this.mCust != null && this.mCust.shouldNotSkip(syncPref.getAccount()))) {
                    requestOrCancelSync(syncPref.getAccount(), syncPref.getAuthority(), startSync);
                }
            }
        }
        if (this.mAccount != null) {
            for (SyncAdapterType syncAdapter : this.mInvisibleAdapters) {
                requestOrCancelSync(this.mAccount, syncAdapter.authority, startSync);
            }
        }
    }

    private void requestOrCancelSync(Account account, String authority, boolean flag) {
        if (flag) {
            Bundle extras = new Bundle();
            extras.putBoolean("force", true);
            if (this.mCust != null) {
                this.mCust.customizeAccountSync(account, extras);
            }
            ContentResolver.requestSyncAsUser(account, authority, this.mUserHandle.getIdentifier(), extras);
            return;
        }
        ContentResolver.cancelSyncAsUser(account, authority, this.mUserHandle.getIdentifier());
    }

    private boolean isSyncing(List<SyncInfo> currentSyncs, Account account, String authority) {
        for (SyncInfo syncInfo : currentSyncs) {
            if (syncInfo.account.equals(account) && syncInfo.authority.equals(authority)) {
                return true;
            }
        }
        return false;
    }

    protected void onSyncStateUpdated() {
        if (isResumed() && this.mAccount != null) {
            setFeedsState();
            Activity activity = getActivity();
            if (activity != null) {
                activity.invalidateOptionsMenu();
            }
        }
    }

    private void setFeedsState() {
        int i;
        Date date = new Date();
        int userId = this.mUserHandle.getIdentifier();
        List<SyncInfo> currentSyncs = ContentResolver.getCurrentSyncsAsUser(userId);
        boolean syncIsFailing = false;
        updateAccountSwitches();
        int count = getPreferenceScreen().getPreferenceCount();
        for (int i2 = 0; i2 < count; i2++) {
            Preference pref = getPreferenceScreen().getPreference(i2);
            if (pref instanceof SyncStateSwitchPreference) {
                SyncStateSwitchPreference syncPref = (SyncStateSwitchPreference) pref;
                String authority = syncPref.getAuthority();
                Account account = syncPref.getAccount();
                SyncStatusInfo status = ContentResolver.getSyncStatusAsUser(account, authority, userId);
                boolean syncEnabled = ContentResolver.getSyncAutomaticallyAsUser(account, authority, userId);
                boolean z = status == null ? false : status.pending;
                boolean z2 = status == null ? false : status.initialize;
                boolean activelySyncing = isSyncing(currentSyncs, account, authority);
                boolean lastSyncFailed = (status == null || status.lastFailureTime == 0) ? false : status.getLastFailureMesgAsInt(0) != 1;
                if (!syncEnabled) {
                    lastSyncFailed = false;
                }
                if (!(!lastSyncFailed || activelySyncing || z)) {
                    syncIsFailing = true;
                }
                if (Log.isLoggable("AccountSettings", 2)) {
                    Log.d("AccountSettings", "Update sync status: " + account + " " + authority + " active = " + activelySyncing + " pend =" + z);
                }
                long successEndTime = status == null ? 0 : status.lastSuccessTime;
                if (!syncEnabled) {
                    syncPref.setSummary(2131627560);
                } else if (activelySyncing) {
                    syncPref.setSummary(2131626237);
                } else if (successEndTime != 0) {
                    date.setTime(successEndTime);
                    String timeString = formatSyncDate(date);
                    syncPref.setSummary((CharSequence) getResources().getString(2131626236, new Object[]{timeString}));
                } else {
                    syncPref.setSummary((CharSequence) "");
                }
                int syncState = ContentResolver.getIsSyncableAsUser(account, authority, userId);
                boolean z3 = (!activelySyncing || syncState < 0) ? false : !z2;
                syncPref.setActive(z3);
                z3 = (!z || syncState < 0) ? false : !z2;
                syncPref.setPending(z3);
                syncPref.setFailed(lastSyncFailed);
                boolean oneTimeSyncMode = !ContentResolver.getMasterSyncAutomaticallyAsUser(userId);
                syncPref.setOneTimeSyncMode(oneTimeSyncMode);
                if (oneTimeSyncMode) {
                    syncEnabled = true;
                }
                syncPref.setChecked(syncEnabled);
            }
        }
        LinearLayout linearLayout = this.mErrorMsgArea;
        if (syncIsFailing) {
            i = 0;
        } else {
            i = 8;
        }
        linearLayout.setVisibility(i);
    }

    public void onAccountsUpdate(UserHandle userHandle) {
        super.onAccountsUpdate(userHandle);
        if (accountExists(this.mAccount)) {
            updateAccountSwitches();
            onSyncStateUpdated();
            return;
        }
        finish();
    }

    private boolean accountExists(Account account) {
        if (account == null) {
            return false;
        }
        for (Account equals : AccountManager.get(getActivity()).getAccountsByTypeAsUser(account.type, this.mUserHandle)) {
            if (equals.equals(account)) {
                return true;
            }
        }
        return false;
    }

    private void updateAccountSwitches() {
        int i;
        this.mInvisibleAdapters.clear();
        String pkgName = getPackageForType(this.mAccount.type);
        removePreference(pkgName);
        SyncAdapterType[] syncAdapters = ContentResolver.getSyncAdapterTypesAsUser(this.mUserHandle.getIdentifier());
        ArrayList<String> authorities = new ArrayList();
        for (SyncAdapterType sa : syncAdapters) {
            if (sa.accountType.equals(this.mAccount.type)) {
                if (sa.isUserVisible()) {
                    if (Log.isLoggable("AccountSettings", 2)) {
                        Log.d("AccountSettings", "updateAccountSwitches: added authority " + sa.authority + " to accountType " + sa.accountType);
                    }
                    authorities.add(sa.authority);
                } else {
                    this.mInvisibleAdapters.add(sa);
                }
            }
        }
        int n = this.mSwitches.size();
        for (i = 0; i < n; i++) {
            getPreferenceScreen().removePreference((Preference) this.mSwitches.get(i));
        }
        this.mSwitches.clear();
        if (Log.isLoggable("AccountSettings", 2)) {
            Log.d("AccountSettings", "looking for sync adapters that match account " + this.mAccount);
        }
        int m = authorities.size();
        for (int j = 0; j < m; j++) {
            String authority = (String) authorities.get(j);
            if (!this.mIsOnlySyncEmail || "com.android.email.provider".equals(authority)) {
                int syncState = ContentResolver.getIsSyncableAsUser(this.mAccount, authority, this.mUserHandle.getIdentifier());
                Log.d("AccountSettings", "  found authority " + authority + " " + syncState);
                if (syncState > 0) {
                    addSyncStateSwitch(this.mAccount, authority);
                }
            }
        }
        Collections.sort(this.mSwitches);
        n = this.mSwitches.size();
        for (i = 0; i < n; i++) {
            getPreferenceScreen().addPreference((Preference) this.mSwitches.get(i));
        }
        if (getAppAutoStartupState(pkgName) == 2) {
            addPreference(pkgName);
        }
    }

    protected void onAuthDescriptionsUpdated() {
        super.onAuthDescriptionsUpdated();
        getPreferenceScreen().removeAll();
        if (this.mAccount != null) {
            this.mProviderIcon.setImageDrawable(getDrawableForType(this.mAccount.type));
            this.mProviderId.setText(getLabelForType(this.mAccount.type));
        }
    }

    public void onDialogShowing() {
        if (getDialogFragment() != null) {
            Dialog dialog = getDialogFragment().getDialog();
            if (dialog != null) {
                Button button = ((AlertDialog) dialog).getButton(-1);
                if (button != null) {
                    button.setTextColor(-65536);
                }
            }
        }
        super.onDialogShowing();
    }

    private String getPackageForType(String accountType) {
        Activity act = getActivity();
        if (act == null || accountType == null || AccountManager.get(act) == null) {
            return null;
        }
        AuthenticatorDescription[] authDescs = AccountManager.get(act).getAuthenticatorTypes();
        if (authDescs == null) {
            return null;
        }
        for (int i = 0; i < authDescs.length; i++) {
            if (TextUtils.equals(authDescs[i].type, accountType)) {
                return authDescs[i].packageName;
            }
        }
        return null;
    }

    private static int getAppAutoStartupState(String pkgName) {
        if (pkgName == null) {
            return 0;
        }
        try {
            IHoldService service = StubController.getHoldService();
            if (service == null) {
                Log.e("AccountSettings", "service is null");
                return 0;
            }
            Bundle bundle = new Bundle();
            bundle.putString("packageName", pkgName);
            Bundle res = service.callHsmService("GetAppAutoStartupState", bundle);
            if (res != null) {
                return res.getInt("result_code");
            }
            Log.e("AccountSettings", "res is null for pkgName = " + pkgName);
            return 0;
        } catch (RemoteException e) {
            return 0;
        } catch (Exception e2) {
            return 0;
        }
    }

    private void addPreference(String pkgname) {
        Activity act = getActivity();
        if (act != null) {
            Preference tips = new Preference(act);
            tips.setSummary(2131628872);
            tips.setLayoutResource(2130968987);
            tips.setKey(pkgname);
            getPreferenceScreen().addPreference(tips);
        }
    }
}
