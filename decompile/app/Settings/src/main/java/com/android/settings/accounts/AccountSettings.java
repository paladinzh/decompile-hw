package com.android.settings.accounts;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ActivityManager;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.UserInfo;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Process;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.SearchIndexableResource;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceClickListener;
import android.support.v7.preference.PreferenceGroup;
import android.support.v7.preference.PreferenceScreen;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import com.android.settings.AccessiblePreferenceCategory;
import com.android.settings.DimmableIconPreference;
import com.android.settings.ItemUseStat;
import com.android.settings.SettingsActivity;
import com.android.settings.SettingsExtUtils;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Index;
import com.android.settings.search.Indexable;
import com.android.settings.search.Indexable.SearchIndexProvider;
import com.android.settings.search.SearchIndexableRaw;
import com.android.settings.users.UserDialogs;
import com.android.settingslib.R$string;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.accounts.AuthenticatorHelper;
import com.android.settingslib.accounts.AuthenticatorHelper.OnAccountsUpdateListener;
import com.huawei.cust.HwCustUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AccountSettings extends SettingsPreferenceFragment implements OnAccountsUpdateListener, OnPreferenceClickListener, Indexable {
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        public List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean enabled) {
            new SearchIndexableResource(context).xmlResId = 2131230726;
            return Arrays.asList(new SearchIndexableResource[]{sir});
        }

        public List<SearchIndexableRaw> getRawDataToIndex(Context context, boolean enabled) {
            List<SearchIndexableRaw> result = new ArrayList();
            Resources res = context.getResources();
            String screenTitle = res.getString(2131624636);
            List<UserInfo> profiles = UserManager.get(context).getProfiles(UserHandle.myUserId());
            int profilesCount = profiles.size();
            for (int i = 0; i < profilesCount; i++) {
                UserInfo userInfo = (UserInfo) profiles.get(i);
                if (userInfo.isEnabled()) {
                    SearchIndexableRaw data;
                    if (!RestrictedLockUtils.hasBaseUserRestriction(context, "no_modify_accounts", userInfo.id)) {
                        data = new SearchIndexableRaw(context);
                        data.title = res.getString(2131626223);
                        data.screenTitle = screenTitle;
                        result.add(data);
                    }
                    if (userInfo.isManagedProfile()) {
                        data = new SearchIndexableRaw(context);
                        data.title = res.getString(2131626227);
                        data.screenTitle = screenTitle;
                        result.add(data);
                        data = new SearchIndexableRaw(context);
                        data.title = res.getString(2131627225);
                        data.screenTitle = screenTitle;
                        result.add(data);
                    }
                }
            }
            return result;
        }
    };
    private String[] mAuthorities;
    private int mAuthoritiesCount = 0;
    private Context mContext;
    private HwCustAccountSettings mCustAccountSettings;
    private ManagedProfileBroadcastReceiver mManagedProfileBroadcastReceiver = new ManagedProfileBroadcastReceiver();
    private Preference mProfileNotAvailablePreference;
    private SparseArray<ProfileData> mProfiles = new SparseArray();
    private UserManager mUm;

    private class AccountPreference extends Preference implements OnPreferenceClickListener {
        private Context mContext;
        private String mFragment;
        private final Bundle mFragmentArguments;
        private Intent mIntent;
        private final CharSequence mTitle;
        private int mTitleResId;
        private String mTitleResPackageName;

        public AccountPreference(Context context, CharSequence title, String titleResPackageName, int titleResId, String fragment, Bundle fragmentArguments, Drawable icon) {
            super(context);
            this.mTitle = title;
            this.mTitleResPackageName = titleResPackageName;
            this.mTitleResId = titleResId;
            this.mFragment = fragment;
            this.mFragmentArguments = fragmentArguments;
            AccountSettings.this.mCustAccountSettings = (HwCustAccountSettings) HwCustUtils.createObj(HwCustAccountSettings.class, new Object[0]);
            setLayoutResource(2130969013);
            setWidgetLayoutResource(2130968998);
            setTitle(title);
            setIcon(icon);
            setOnPreferenceClickListener(this);
        }

        public AccountPreference(Context context, CharSequence title, Intent intent, Bundle fragmentArguments, Drawable icon) {
            super(context);
            this.mTitle = title;
            this.mContext = context;
            this.mIntent = intent;
            this.mFragmentArguments = fragmentArguments;
            AccountSettings.this.mCustAccountSettings = (HwCustAccountSettings) HwCustUtils.createObj(HwCustAccountSettings.class, new Object[0]);
            setLayoutResource(2130969013);
            setWidgetLayoutResource(2130968998);
            setTitle(title);
            setIcon(icon);
            setOnPreferenceClickListener(this);
        }

        public boolean onPreferenceClick(Preference preference) {
            if (AccountSettings.this.mCustAccountSettings != null && AccountSettings.this.mCustAccountSettings.handleCustIntialization(this.mTitle, AccountSettings.this.getActivity().getBaseContext())) {
                return true;
            }
            if (this.mFragment != null) {
                Utils.startWithFragment(getContext(), this.mFragment, this.mFragmentArguments, null, 0, this.mTitleResPackageName, this.mTitleResId, null);
                return true;
            } else if (this.mIntent == null || this.mContext == null) {
                return false;
            } else {
                this.mContext.startActivity(this.mIntent);
                return true;
            }
        }
    }

    public static class ConfirmAutoSyncChangeFragment extends DialogFragment {
        private boolean mEnabling;
        private UserHandle mUserHandle;

        public static void show(AccountSettings parent, boolean enabling, UserHandle userHandle) {
            if (parent.isAdded()) {
                ConfirmAutoSyncChangeFragment dialog = new ConfirmAutoSyncChangeFragment();
                dialog.mEnabling = enabling;
                dialog.mUserHandle = userHandle;
                dialog.setTargetFragment(parent, 0);
                dialog.show(parent.getFragmentManager(), "confirmAutoSyncChange");
            }
        }

        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Context context = getActivity();
            if (savedInstanceState != null) {
                this.mEnabling = savedInstanceState.getBoolean("enabling");
                this.mUserHandle = (UserHandle) savedInstanceState.getParcelable("userHandle");
            }
            Builder builder = new Builder(context);
            if (this.mEnabling) {
                builder.setTitle(2131626298);
                builder.setMessage(2131627789);
            } else {
                builder.setTitle(2131627790);
                builder.setMessage(2131626334);
            }
            builder.setPositiveButton(17039370, new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    ContentResolver.setMasterSyncAutomaticallyAsUser(ConfirmAutoSyncChangeFragment.this.mEnabling, ConfirmAutoSyncChangeFragment.this.mUserHandle.getIdentifier());
                    if (ConfirmAutoSyncChangeFragment.this.mEnabling) {
                        ItemUseStat.getInstance().handleClick(ConfirmAutoSyncChangeFragment.this.getActivity(), 2, "auto_sync_data_checked");
                    } else {
                        ItemUseStat.getInstance().handleClick(ConfirmAutoSyncChangeFragment.this.getActivity(), 2, "auto_sync_data_not_checked");
                    }
                }
            });
            builder.setNegativeButton(17039360, null);
            return builder.create();
        }

        public void onSaveInstanceState(Bundle outState) {
            super.onSaveInstanceState(outState);
            outState.putBoolean("enabling", this.mEnabling);
            outState.putParcelable("userHandle", this.mUserHandle);
        }
    }

    private class ManagedProfileBroadcastReceiver extends BroadcastReceiver {
        private boolean listeningToManagedProfileEvents;

        private ManagedProfileBroadcastReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.v("AccountSettings", "Received broadcast: " + action);
            if (action.equals("android.intent.action.MANAGED_PROFILE_REMOVED") || action.equals("android.intent.action.MANAGED_PROFILE_ADDED")) {
                AccountSettings.this.stopListeningToAccountUpdates();
                AccountSettings.this.cleanUpPreferences();
                AccountSettings.this.updateUi();
                AccountSettings.this.listenToAccountUpdates();
                AccountSettings.this.getActivity().invalidateOptionsMenu();
                return;
            }
            Log.w("AccountSettings", "Cannot handle received broadcast: " + intent.getAction());
        }

        public void register(Context context) {
            if (!this.listeningToManagedProfileEvents) {
                IntentFilter intentFilter = new IntentFilter();
                intentFilter.addAction("android.intent.action.MANAGED_PROFILE_REMOVED");
                intentFilter.addAction("android.intent.action.MANAGED_PROFILE_ADDED");
                context.registerReceiver(this, intentFilter);
                this.listeningToManagedProfileEvents = true;
            }
        }

        public void unregister(Context context) {
            if (this.listeningToManagedProfileEvents) {
                context.unregisterReceiver(this);
                this.listeningToManagedProfileEvents = false;
            }
        }
    }

    private class MasterSyncStateClickListener implements OnMenuItemClickListener {
        private final UserHandle mUserHandle;

        public MasterSyncStateClickListener(UserHandle userHandle) {
            this.mUserHandle = userHandle;
        }

        public boolean onMenuItemClick(MenuItem item) {
            if (ActivityManager.isUserAMonkey()) {
                Log.d("AccountSettings", "ignoring monkey's attempt to flip sync state");
            } else {
                ConfirmAutoSyncChangeFragment.show(AccountSettings.this, !item.isChecked(), this.mUserHandle);
            }
            return true;
        }
    }

    private static class ProfileData {
        public DimmableIconPreference addAccountPreference;
        public AuthenticatorHelper authenticatorHelper;
        public Preference managedProfilePreference;
        public PreferenceGroup preferenceGroup;
        public Preference removeWorkProfilePreference;
        public UserInfo userInfo;

        private ProfileData() {
        }
    }

    protected int getMetricsCategory() {
        return 8;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mUm = (UserManager) getSystemService("user");
        this.mProfileNotAvailablePreference = new Preference(getPrefContext());
        this.mAuthorities = getActivity().getIntent().getStringArrayExtra("authorities");
        if (this.mAuthorities != null) {
            this.mAuthoritiesCount = this.mAuthorities.length;
        }
        setHasOptionsMenu(true);
        this.mContext = getContext();
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(2132017152, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    public void onPrepareOptionsMenu(Menu menu) {
        menu.findItem(2131887632).setVisible(false);
        menu.findItem(2131887633).setVisible(false);
        menu.findItem(2131887634).setVisible(false);
        menu.findItem(2131887635).setVisible(false);
        UserHandle currentProfile = Process.myUserHandle();
        if (this.mProfiles.size() == 1) {
            menu.findItem(2131887632).setVisible(true).setOnMenuItemClickListener(new MasterSyncStateClickListener(currentProfile)).setChecked(ContentResolver.getMasterSyncAutomaticallyAsUser(currentProfile.getIdentifier()));
            applyLowPowerMode(menu);
        } else if (this.mProfiles.size() > 1) {
            menu.findItem(2131887633).setVisible(true).setOnMenuItemClickListener(new MasterSyncStateClickListener(currentProfile)).setChecked(ContentResolver.getMasterSyncAutomaticallyAsUser(currentProfile.getIdentifier()));
            for (int i = 1; i < this.mProfiles.size(); i++) {
                UserHandle otherProfile = ((ProfileData) this.mProfiles.valueAt(i)).userInfo.getUserHandle();
                if (((ProfileData) this.mProfiles.valueAt(i)).userInfo.isClonedProfile()) {
                    menu.findItem(2131887635).setVisible(true).setOnMenuItemClickListener(new MasterSyncStateClickListener(otherProfile)).setChecked(ContentResolver.getMasterSyncAutomaticallyAsUser(otherProfile.getIdentifier()));
                } else {
                    menu.findItem(2131887634).setVisible(true).setOnMenuItemClickListener(new MasterSyncStateClickListener(otherProfile)).setChecked(ContentResolver.getMasterSyncAutomaticallyAsUser(otherProfile.getIdentifier()));
                }
            }
            applyLowPowerMode(menu);
        } else {
            Log.w("AccountSettings", "Method onPrepareOptionsMenu called before mProfiles was initialized");
        }
    }

    public void onResume() {
        super.onResume();
        cleanUpPreferences();
        updateUi();
        this.mManagedProfileBroadcastReceiver.register(getActivity());
        listenToAccountUpdates();
    }

    public void onPause() {
        super.onPause();
        stopListeningToAccountUpdates();
        this.mManagedProfileBroadcastReceiver.unregister(getActivity());
    }

    public void onAccountsUpdate(UserHandle userHandle) {
        ProfileData profileData = (ProfileData) this.mProfiles.get(userHandle.getIdentifier());
        if (profileData != null) {
            updateAccountTypes(profileData);
        } else {
            Log.w("AccountSettings", "Missing Settings screen for: " + userHandle.getIdentifier());
        }
    }

    public boolean onPreferenceClick(Preference preference) {
        int count = this.mProfiles.size();
        int i = 0;
        while (i < count) {
            ProfileData profileData = (ProfileData) this.mProfiles.valueAt(i);
            if (preference == profileData.addAccountPreference) {
                Intent intent = new Intent("android.settings.ADD_ACCOUNT_SETTINGS");
                intent.putExtra("android.intent.extra.USER", profileData.userInfo.getUserHandle());
                intent.putExtra("authorities", this.mAuthorities);
                startActivity(intent);
                ItemUseStat.getInstance().handleClick(getActivity(), 2, "add_account");
                SettingsExtUtils.setAnimationReflection(getActivity());
                return true;
            } else if (preference == profileData.removeWorkProfilePreference) {
                final int userId = profileData.userInfo.id;
                UserDialogs.createRemoveDialog(getActivity(), userId, new OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        AccountSettings.this.mUm.removeUser(userId);
                    }
                }).show();
                return true;
            } else if (preference == profileData.managedProfilePreference) {
                Bundle arguments = new Bundle();
                arguments.putParcelable("android.intent.extra.USER", profileData.userInfo.getUserHandle());
                ((SettingsActivity) getActivity()).startPreferencePanel(ManagedProfileSettings.class.getName(), arguments, 2131627225, null, null, 0);
                return true;
            } else {
                i++;
            }
        }
        return false;
    }

    private void updateUi() {
        addPreferencesFromResource(2131230726);
        if (Utils.isManagedProfile(this.mUm)) {
            Log.e("AccountSettings", "We should not be showing settings for a managed profile");
            finish();
            return;
        }
        int profilesCount;
        int i;
        PreferenceScreen preferenceScreen = (PreferenceScreen) findPreference("account");
        if (this.mUm.isLinkedUser()) {
            updateProfileUi(this.mUm.getUserInfo(UserHandle.myUserId()), false, preferenceScreen);
        } else {
            List<UserInfo> profiles = this.mUm.getProfiles(UserHandle.myUserId());
            profilesCount = profiles.size();
            boolean addCategory = profilesCount > 1;
            for (i = 0; i < profilesCount; i++) {
                updateProfileUi((UserInfo) profiles.get(i), addCategory, preferenceScreen);
            }
        }
        profilesCount = this.mProfiles.size();
        for (i = 0; i < profilesCount; i++) {
            ProfileData profileData = (ProfileData) this.mProfiles.valueAt(i);
            if (!profileData.preferenceGroup.equals(preferenceScreen)) {
                preferenceScreen.addPreference(profileData.preferenceGroup);
            }
            updateAccountTypes(profileData);
        }
    }

    private void updateProfileUi(UserInfo userInfo, boolean addCategory, PreferenceScreen parent) {
        Context context = getActivity();
        ProfileData profileData = new ProfileData();
        profileData.userInfo = userInfo;
        if (addCategory) {
            profileData.preferenceGroup = new AccessiblePreferenceCategory(getPrefContext());
            if (userInfo.isManagedProfile()) {
                profileData.preferenceGroup.setLayoutResource(2130969288);
                profileData.preferenceGroup.setTitle((int) R$string.category_work);
                profileData.preferenceGroup.setSummary((CharSequence) getWorkGroupSummary(context, userInfo));
                ((AccessiblePreferenceCategory) profileData.preferenceGroup).setContentDescription(getString(2131625143, new Object[]{workGroupSummary}));
                profileData.removeWorkProfilePreference = newRemoveWorkProfilePreference(context);
                profileData.managedProfilePreference = newManagedProfileSettings();
            } else if (userInfo.isClonedProfile()) {
                profileData.preferenceGroup.setTitle(2131628938);
                ((AccessiblePreferenceCategory) profileData.preferenceGroup).setContentDescription(getString(2131628939));
            } else {
                profileData.preferenceGroup.setTitle((int) R$string.category_personal);
                ((AccessiblePreferenceCategory) profileData.preferenceGroup).setContentDescription(getString(2131625144));
            }
            parent.addPreference(profileData.preferenceGroup);
        } else {
            profileData.preferenceGroup = parent;
        }
        if (userInfo.isEnabled()) {
            profileData.authenticatorHelper = new AuthenticatorHelper(context, userInfo.getUserHandle(), this);
            if (!RestrictedLockUtils.hasBaseUserRestriction(context, "no_modify_accounts", userInfo.id)) {
                profileData.addAccountPreference = newAddAccountPreference(context);
                profileData.addAccountPreference.checkRestrictionAndSetDisabled("no_modify_accounts", userInfo.id);
            }
        }
        this.mProfiles.put(userInfo.id, profileData);
        Index.getInstance(getActivity()).updateFromClassNameResource(AccountSettings.class.getName(), true, true);
    }

    private DimmableIconPreference newAddAccountPreference(Context context) {
        DimmableIconPreference preference = new DimmableIconPreference(getPrefContext());
        preference.setTitle(2131626223);
        preference.setLayoutResource(2130968908);
        preference.setOnPreferenceClickListener(this);
        preference.setOrder(1000);
        return preference;
    }

    private Preference newRemoveWorkProfilePreference(Context context) {
        Preference preference = new Preference(getPrefContext());
        preference.setTitle(2131626227);
        preference.setIcon(2130838279);
        preference.setLayoutResource(2130969013);
        preference.setWidgetLayoutResource(2130968998);
        preference.setOnPreferenceClickListener(this);
        preference.setOrder(1002);
        return preference;
    }

    private Preference newManagedProfileSettings() {
        Preference preference = new Preference(getPrefContext());
        preference.setTitle(2131627225);
        preference.setIcon(2130838325);
        preference.setOnPreferenceClickListener(this);
        preference.setOrder(1001);
        return preference;
    }

    private String getWorkGroupSummary(Context context, UserInfo userInfo) {
        PackageManager packageManager = context.getPackageManager();
        if (Utils.getAdminApplicationInfo(context, userInfo.id) == null) {
            return null;
        }
        return getString(2131626860, new Object[]{packageManager.getApplicationLabel(Utils.getAdminApplicationInfo(context, userInfo.id))});
    }

    private void cleanUpPreferences() {
        PreferenceScreen preferenceScreen = getPreferenceScreen();
        if (preferenceScreen != null) {
            preferenceScreen.removeAll();
        }
        this.mProfiles.clear();
    }

    private void listenToAccountUpdates() {
        int count = this.mProfiles.size();
        for (int i = 0; i < count; i++) {
            AuthenticatorHelper authenticatorHelper = ((ProfileData) this.mProfiles.valueAt(i)).authenticatorHelper;
            if (authenticatorHelper != null) {
                authenticatorHelper.listenToAccountUpdates();
            }
        }
    }

    private void stopListeningToAccountUpdates() {
        int count = this.mProfiles.size();
        for (int i = 0; i < count; i++) {
            AuthenticatorHelper authenticatorHelper = ((ProfileData) this.mProfiles.valueAt(i)).authenticatorHelper;
            if (authenticatorHelper != null) {
                authenticatorHelper.stopListeningToAccountUpdates();
            }
        }
    }

    private void updateAccountTypes(ProfileData profileData) {
        profileData.preferenceGroup.removeAll();
        if (profileData.userInfo.isEnabled()) {
            ArrayList<AccountPreference> preferences = getAccountTypePreferences(profileData.authenticatorHelper, profileData.userInfo.getUserHandle());
            int count = preferences.size();
            for (int i = 0; i < count; i++) {
                profileData.preferenceGroup.addPreference((Preference) preferences.get(i));
            }
            if (profileData.addAccountPreference != null) {
                profileData.preferenceGroup.addPreference(profileData.addAccountPreference);
            }
        } else {
            this.mProfileNotAvailablePreference.setEnabled(false);
            this.mProfileNotAvailablePreference.setIcon(2130837691);
            this.mProfileNotAvailablePreference.setTitle(null);
            this.mProfileNotAvailablePreference.setSummary(2131626224);
            profileData.preferenceGroup.addPreference(this.mProfileNotAvailablePreference);
        }
        if (profileData.removeWorkProfilePreference != null) {
            profileData.preferenceGroup.addPreference(profileData.removeWorkProfilePreference);
        }
        if (profileData.managedProfilePreference != null) {
            profileData.preferenceGroup.addPreference(profileData.managedProfilePreference);
        }
    }

    private ArrayList<AccountPreference> getAccountTypePreferences(AuthenticatorHelper helper, UserHandle userHandle) {
        String[] accountTypes = helper.getEnabledAccountTypes();
        ArrayList<AccountPreference> arrayList = new ArrayList(accountTypes.length);
        for (String accountType : accountTypes) {
            if (accountTypeHasAnyRequestedAuthorities(helper, accountType) && !AccountExtUtils.shouldBeIgnored(accountType)) {
                if ("com.huawei.hwid".equalsIgnoreCase(accountType) && AccountExtUtils.isUserLoggedInHwId(helper)) {
                    boolean isHwCloudServiceNewVersion = Utils.hasIntentActivity(getPackageManager(), new Intent("com.huawei.android.intent.action.settings.HICLOUD_ENTTRANCE_30"));
                    boolean isHwIdSupportNewVersion = Utils.hasIntentActivity(getPackageManager(), new Intent("com.huawei.hwid.ACTION_MAIN_SETTINGS").setPackage("com.huawei.hwid"));
                    if (Utils.hasNewVersionOfHwIDMainPage(getActivity()) || (isHwCloudServiceNewVersion && isHwIdSupportNewVersion)) {
                        Log.d("AccountSettings", "Hiding the HwId entry in new version.");
                    }
                }
                CharSequence label = helper.getLabelForType(getActivity(), accountType);
                if (label != null) {
                    String titleResPackageName = helper.getPackageForType(accountType);
                    int titleResId = helper.getLabelIdForType(accountType);
                    Account[] accounts = AccountManager.get(getActivity()).getAccountsByTypeAsUser(accountType, userHandle);
                    boolean skipToAccount = accounts.length == 1 ? !helper.hasAccountPreferences(accountType) : false;
                    if (skipToAccount) {
                        Bundle fragmentArguments = new Bundle();
                        fragmentArguments.putParcelable("account", accounts[0]);
                        fragmentArguments.putParcelable("android.intent.extra.USER", userHandle);
                        arrayList.add(new AccountPreference(getPrefContext(), label, titleResPackageName, titleResId, AccountSyncSettings.class.getName(), fragmentArguments, helper.getDrawableForType(getActivity(), accountType)));
                    } else if ("com.huawei.hwid".equalsIgnoreCase(accountType)) {
                        Intent hwIdIntent = new Intent("com.huawei.hwid.ACTION_MAIN_SETTINGS").setPackage("com.huawei.hwid");
                        if (Utils.hasIntentActivity(getPackageManager(), hwIdIntent)) {
                            hwIdIntent.putExtra("showLogout", true);
                            arrayList.add(new AccountPreference(getActivity(), label, hwIdIntent, null, helper.getDrawableForType(getActivity(), accountType)));
                            Log.d("AccountSettings", "Update HwId entry to main page.");
                        } else {
                            buildAndSaveAccountTypePreference(arrayList, accountType, label.toString(), titleResPackageName, titleResId, helper, userHandle);
                        }
                    } else {
                        buildAndSaveAccountTypePreference(arrayList, accountType, label.toString(), titleResPackageName, titleResId, helper, userHandle);
                    }
                    helper.preloadDrawableForType(getActivity(), accountType);
                }
            }
        }
        Collections.sort(arrayList, new Comparator<AccountPreference>() {
            public int compare(AccountPreference t1, AccountPreference t2) {
                return t1.mTitle.toString().compareTo(t2.mTitle.toString());
            }
        });
        return arrayList;
    }

    private void buildAndSaveAccountTypePreference(ArrayList<AccountPreference> preferenceList, String accountType, String label, String titleResPackageName, int titleResId, AuthenticatorHelper helper, UserHandle userHandle) {
        Bundle fragmentArguments = new Bundle();
        fragmentArguments.putString("account_type", accountType);
        fragmentArguments.putString("account_label", label);
        fragmentArguments.putParcelable("android.intent.extra.USER", userHandle);
        preferenceList.add(new AccountPreference(getPrefContext(), label, titleResPackageName, titleResId, ManageAccountsSettings.class.getName(), fragmentArguments, helper.getDrawableForType(getActivity(), accountType)));
    }

    private boolean accountTypeHasAnyRequestedAuthorities(AuthenticatorHelper helper, String accountType) {
        if (this.mAuthoritiesCount == 0) {
            return true;
        }
        ArrayList<String> authoritiesForType = helper.getAuthoritiesForAccountType(accountType);
        if (authoritiesForType == null) {
            Log.d("AccountSettings", "No sync authorities for account type: " + accountType);
            return false;
        }
        for (int j = 0; j < this.mAuthoritiesCount; j++) {
            if (authoritiesForType.contains(this.mAuthorities[j])) {
                return true;
            }
        }
        return false;
    }

    public void applyLowPowerMode(Menu menu) {
        if (Utils.isLowPowerMode(this.mContext)) {
            menu.setGroupEnabled(0, false);
        } else {
            menu.setGroupEnabled(0, true);
        }
    }
}
