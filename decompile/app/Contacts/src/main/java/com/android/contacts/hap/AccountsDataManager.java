package com.android.contacts.hap;

import android.content.Context;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import com.android.contacts.util.HwLog;

public abstract class AccountsDataManager {
    protected static final boolean ACC_DATA_MNGR_LOG_ENABLED = HwLog.HWDBG;
    protected ContentObserver mRawContactsObserver;

    public abstract Bitmap[] getAccountData(long j, String[] strArr);

    public abstract Bitmap getAccountIcon(String str);

    public abstract void preLoadAccountsDataInBackground();

    public abstract void setListener(AccountCacheUpdatedListener accountCacheUpdatedListener);

    public static AccountsDataManager getInstance(Context context) {
        if (ACC_DATA_MNGR_LOG_ENABLED) {
            HwLog.d("AccountDataManager", "getInstance(Context) called!!!");
        }
        return (AccountsDataManager) context.getApplicationContext().getSystemService("accountsData");
    }

    public static synchronized AccountsDataManager createAccountDataManager(Context context) {
        AccountsDataManager acountDataManagerImpl;
        synchronized (AccountsDataManager.class) {
            acountDataManagerImpl = new AcountDataManagerImpl(context);
        }
        return acountDataManagerImpl;
    }
}
