package com.android.contacts.editor;

import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources.NotFoundException;
import android.text.TextUtils;
import android.view.View;
import com.android.contacts.model.AccountTypeManager;
import com.android.contacts.model.account.AccountType;
import com.android.contacts.model.account.AccountWithDataSet;
import com.android.contacts.test.NeededForTesting;
import com.android.contacts.util.HwLog;
import com.android.contacts.util.SharePreferenceUtil;
import com.google.android.gms.R;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ContactEditorUtils {
    private static final List<AccountWithDataSet> EMPTY_ACCOUNTS = ImmutableList.of();
    private static ContactEditorUtils sInstance;
    private final AccountTypeManager mAccountTypes;
    private final Context mContext;
    private boolean mExcludeSim;
    private boolean mExcludeSim1;
    private boolean mExcludeSim2;
    private final SharedPreferences mPrefs;

    private ContactEditorUtils(Context context) {
        this(context, AccountTypeManager.getInstance(context));
    }

    @VisibleForTesting
    ContactEditorUtils(Context context, AccountTypeManager accountTypes) {
        this.mContext = context.getApplicationContext();
        this.mPrefs = SharePreferenceUtil.getDefaultSp_de(this.mContext);
        this.mAccountTypes = accountTypes;
    }

    public static synchronized ContactEditorUtils getInstance(Context context) {
        ContactEditorUtils contactEditorUtils;
        synchronized (ContactEditorUtils.class) {
            if (sInstance == null) {
                sInstance = new ContactEditorUtils(context.getApplicationContext());
            }
            contactEditorUtils = sInstance;
        }
        return contactEditorUtils;
    }

    @NeededForTesting
    void cleanupForTest() {
        this.mPrefs.edit().remove("ContactEditorUtils_default_account").remove("ContactEditorUtils_known_accounts").remove("ContactEditorUtils_anything_saved").apply();
    }

    @NeededForTesting
    void removeDefaultAccountForTest() {
        this.mPrefs.edit().remove("ContactEditorUtils_default_account").apply();
    }

    private void resetPreferenceValues() {
        this.mPrefs.edit().putString("ContactEditorUtils_known_accounts", "").putString("ContactEditorUtils_default_account", "").apply();
    }

    public List<AccountWithDataSet> getWritableAccounts() {
        if (this.mExcludeSim) {
            return this.mAccountTypes.getAccountsExcludeSim(true);
        }
        if (this.mExcludeSim1 && !this.mExcludeSim2) {
            return this.mAccountTypes.getAccountsExcludeSim1(true);
        }
        if (this.mExcludeSim2 && !this.mExcludeSim1) {
            return this.mAccountTypes.getAccountsExcludeSim2(true);
        }
        if (this.mExcludeSim2 && this.mExcludeSim1) {
            return this.mAccountTypes.getAccountsExcludeBothSim(true);
        }
        return this.mAccountTypes.getAccounts(true);
    }

    private boolean isFirstLaunch() {
        return !this.mPrefs.getBoolean("ContactEditorUtils_anything_saved", false);
    }

    public void saveDefaultAndAllAccounts(AccountWithDataSet defaultAccount) {
        Editor editor = this.mPrefs.edit().putBoolean("ContactEditorUtils_anything_saved", true);
        if (defaultAccount == null) {
            editor.putString("ContactEditorUtils_known_accounts", "");
            editor.putString("ContactEditorUtils_default_account", "");
        } else {
            editor.putString("ContactEditorUtils_known_accounts", AccountWithDataSet.stringifyList(this.mAccountTypes.getAccounts(true)));
            editor.putString("ContactEditorUtils_default_account", defaultAccount.stringify());
        }
        editor.apply();
    }

    public AccountWithDataSet getDefaultAccount() {
        String saved = this.mPrefs.getString("ContactEditorUtils_default_account", null);
        if (TextUtils.isEmpty(saved)) {
            return null;
        }
        try {
            return AccountWithDataSet.unstringify(saved);
        } catch (IllegalArgumentException exception) {
            HwLog.e("ContactEditorUtils", "Error with retrieving default account " + exception.toString());
            resetPreferenceValues();
            return null;
        }
    }

    public AccountWithDataSet getPredefinedDefaultAccount(int aAccountId) {
        return this.mAccountTypes.getAccountWithDataSet(aAccountId);
    }

    @VisibleForTesting
    boolean isValidAccount(AccountWithDataSet account) {
        if (account == null) {
            return true;
        }
        return getWritableAccounts().contains(account);
    }

    @VisibleForTesting
    List<AccountWithDataSet> getSavedAccounts() {
        String saved = this.mPrefs.getString("ContactEditorUtils_known_accounts", null);
        if (TextUtils.isEmpty(saved)) {
            return EMPTY_ACCOUNTS;
        }
        try {
            return AccountWithDataSet.unstringifyList(saved);
        } catch (IllegalArgumentException exception) {
            HwLog.e("ContactEditorUtils", "Error with retrieving saved accounts " + exception.toString());
            resetPreferenceValues();
            return EMPTY_ACCOUNTS;
        }
    }

    public boolean shouldShowAccountChangedNotification() {
        if (isFirstLaunch()) {
            if (HwLog.HWFLOW) {
                HwLog.i("ContactEditorUtils", "first launch when create a contact");
            }
            return true;
        }
        List<AccountWithDataSet> savedAccounts = getSavedAccounts();
        List<AccountWithDataSet> currentWritableAccounts = getWritableAccounts();
        for (AccountWithDataSet account : currentWritableAccounts) {
            if (!"com.android.huawei.phone".equalsIgnoreCase(account.type) && !savedAccounts.contains(account)) {
                if (HwLog.HWFLOW) {
                    HwLog.i("ContactEditorUtils", "find a new account and account type is " + account.type + " when create a contact");
                }
                return true;
            }
        }
        AccountWithDataSet defaultAccount = getDefaultAccount();
        if (!isValidAccount(defaultAccount)) {
            if (HwLog.HWFLOW) {
                HwLog.i("ContactEditorUtils", "default account has been removed  when create a contact");
            }
            return true;
        } else if (defaultAccount != null || currentWritableAccounts.size() <= 0) {
            return false;
        } else {
            if (HwLog.HWFLOW) {
                HwLog.i("ContactEditorUtils", "Preferences file in an inconsistent state, request that the default account and current writable accounts be saved again");
            }
            return true;
        }
    }

    @VisibleForTesting
    String[] getWritableAccountTypeStrings() {
        Set<String> types = Sets.newHashSet();
        for (AccountType type : this.mAccountTypes.getAccountTypes(true)) {
            types.add(type.accountType);
        }
        return (String[]) types.toArray(new String[types.size()]);
    }

    String[] getWritableAccountTypeStringsExcludingPhoneSIM() {
        Set<String> types = Sets.newHashSet();
        for (AccountType type : this.mAccountTypes.getAccountTypes(true)) {
            if (!(type.accountType == null || "com.android.huawei.sim".equalsIgnoreCase(type.accountType) || "com.android.huawei.secondsim".equalsIgnoreCase(type.accountType) || "com.android.huawei.phone".equalsIgnoreCase(type.accountType))) {
                types.add(type.accountType);
            }
        }
        return (String[]) types.toArray(new String[types.size()]);
    }

    public Intent createAddWritableAccountIntentExcludePhoneAndSIM() {
        return AccountManager.newChooseAccountIntent(null, new ArrayList(), getWritableAccountTypeStringsExcludingPhoneSIM(), false, null, null, null, null);
    }

    public void setExcludeSim(boolean b) {
        this.mExcludeSim = b;
    }

    public void setExcludeSim1(boolean b) {
        this.mExcludeSim1 = b;
    }

    public void setExcludeSim2(boolean b) {
        this.mExcludeSim2 = b;
    }

    public boolean isExcludeSim() {
        return this.mExcludeSim;
    }

    public boolean isExcludeSim1() {
        return this.mExcludeSim1;
    }

    public boolean isExcludeSim2() {
        return this.mExcludeSim2;
    }

    public static void setViewMargin(Context context, View view, int orientation) {
        if (context != null && view != null && (orientation == 1 || orientation == 2)) {
            if (orientation == 2) {
                int left;
                float density = context.getResources().getDisplayMetrics().density;
                if (((double) density) == 2.0d) {
                    try {
                        left = (int) context.getResources().getDimension(R.dimen.normal_margin_left);
                    } catch (NotFoundException e) {
                        HwLog.e("ContactEditorUtils", "The Resources do not be found.");
                    } finally {
                        view.setPadding(0, 0, 0, 0);
                    }
                } else if (((double) density) < 2.0d) {
                    left = (int) context.getResources().getDimension(R.dimen.small_margin_left);
                } else {
                    left = (int) context.getResources().getDimension(R.dimen.large_margin_left);
                }
                view.setPadding(left, 0, left, 0);
            } else {
                view.setPadding(0, 0, 0, 0);
            }
        }
    }
}
