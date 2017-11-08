package com.android.contacts.model;

import android.content.Context;
import com.android.contacts.hap.AccountLoadListener;
import com.android.contacts.model.account.AccountType;
import com.android.contacts.model.account.AccountTypeWithDataSet;
import com.android.contacts.model.account.AccountWithDataSet;
import com.android.contacts.model.dataitem.DataKind;
import com.android.contacts.test.NeededForTesting;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class AccountTypeManager {
    private static AccountTypeManager mAccountTypeManager;
    private static final Object mInitializationLock = new Object();
    protected AccountLoadListener mAccountLoadListener;

    public abstract AccountType getAccountType(AccountTypeWithDataSet accountTypeWithDataSet);

    public abstract List<AccountType> getAccountTypes(boolean z);

    public abstract List<AccountWithDataSet> getAccounts(boolean z);

    public abstract List<AccountWithDataSet> getGroupWritableAccounts();

    public abstract Map<AccountTypeWithDataSet, AccountType> getUsableInvitableAccountTypes();

    public abstract void hiCloudServiceLogOnOff();

    public static AccountTypeManager getInstance(Context context) {
        AccountTypeManager accountTypeManager;
        synchronized (mInitializationLock) {
            if (mAccountTypeManager == null) {
                mAccountTypeManager = new AccountTypeManagerImpl(context.getApplicationContext());
            }
            accountTypeManager = mAccountTypeManager;
        }
        return accountTypeManager;
    }

    @NeededForTesting
    public static void setInstanceForTest(AccountTypeManager mockManager) {
        synchronized (mInitializationLock) {
            mAccountTypeManager = mockManager;
        }
    }

    public List<AccountWithDataSet> getAccountsExcludeSim(boolean contactWritableOnly) {
        return getAccounts(contactWritableOnly);
    }

    public List<AccountWithDataSet> getAccountsExcludeSim1(boolean contactWritableOnly) {
        return getAccounts(contactWritableOnly);
    }

    public List<AccountWithDataSet> getAccountsExcludeBothSim(boolean contactWritableOnly) {
        return getAccounts(contactWritableOnly);
    }

    public List<AccountWithDataSet> getAccountsExcludeSim2(boolean contactWritableOnly) {
        return getAccounts(contactWritableOnly);
    }

    public final AccountType getAccountType(String accountType, String dataSet) {
        return getAccountType(AccountTypeWithDataSet.get(accountType, dataSet));
    }

    public final AccountType getAccountTypeForAccount(AccountWithDataSet account) {
        if (account == null) {
            return null;
        }
        return getAccountType(account.getAccountTypeWithDataSet());
    }

    public DataKind getKindOrFallback(AccountType type, String mimeType) {
        return type == null ? null : type.getKindForMimetype(mimeType);
    }

    public boolean contains(AccountWithDataSet account, boolean contactWritableOnly) {
        for (AccountWithDataSet account_2 : new ArrayList(getAccounts(false))) {
            if (account.equals(account_2)) {
                return true;
            }
        }
        return false;
    }

    public void setAccountLoadListener(AccountLoadListener aListener) {
        this.mAccountLoadListener = aListener;
    }

    public void unregisterAccountLoadListener() {
        this.mAccountLoadListener = null;
    }

    public AccountWithDataSet getAccountWithDataSet(int aDefaultAccFromPref) {
        return null;
    }
}
