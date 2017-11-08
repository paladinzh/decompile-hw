package com.android.contacts.model;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorDescription;
import android.accounts.OnAccountsUpdateListener;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.IContentService;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SyncAdapterType;
import android.content.SyncStatusObserver;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;
import android.provider.ContactsContract.Contacts;
import android.text.TextUtils;
import android.util.TimingLogger;
import com.android.contacts.MoreContactUtils;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.hap.EmuiFeatureManager;
import com.android.contacts.hap.sim.SimAccountType;
import com.android.contacts.hap.sim.SimConfigListener;
import com.android.contacts.hap.sim.SimFactory;
import com.android.contacts.hap.sim.SimFactoryManager;
import com.android.contacts.hap.sim.SimUtility;
import com.android.contacts.hap.util.MultiUsersUtils;
import com.android.contacts.list.ContactListFilterController;
import com.android.contacts.model.account.AccountType;
import com.android.contacts.model.account.AccountTypeWithDataSet;
import com.android.contacts.model.account.AccountWithDataSet;
import com.android.contacts.model.account.ExchangeAccountType;
import com.android.contacts.model.account.ExternalAccountType;
import com.android.contacts.model.account.FallbackAccountType;
import com.android.contacts.model.account.GoogleAccountType;
import com.android.contacts.model.account.PhoneAccountType;
import com.android.contacts.model.dataitem.DataKind;
import com.android.contacts.util.HwLog;
import com.android.contacts.util.Objects;
import com.google.android.gms.R;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

/* compiled from: AccountTypeManager */
class AccountTypeManagerImpl extends AccountTypeManager implements OnAccountsUpdateListener, SyncStatusObserver, SimConfigListener {
    private static final Comparator<Account> ACCOUNT_COMPARATOR = new Comparator<Account>() {
        public int compare(Account a, Account b) {
            String aDataSet = null;
            String bDataSet = null;
            if (a instanceof AccountWithDataSet) {
                aDataSet = ((AccountWithDataSet) a).dataSet;
            }
            if (b instanceof AccountWithDataSet) {
                bDataSet = ((AccountWithDataSet) b).dataSet;
            }
            if (Objects.equal(a.name, b.name) && Objects.equal(a.type, b.type) && Objects.equal(aDataSet, bDataSet)) {
                return 0;
            }
            if (b.name == null || b.type == null) {
                return -1;
            }
            if (a.name == null || a.type == null) {
                return 1;
            }
            int diff = a.name.compareTo(b.name);
            if (diff != 0) {
                return diff;
            }
            diff = a.type.compareTo(b.type);
            if (diff != 0) {
                return diff;
            }
            if (aDataSet == null) {
                return -1;
            }
            return bDataSet == null ? 1 : aDataSet.compareTo(bDataSet);
        }
    };
    private static final Map<AccountTypeWithDataSet, AccountType> EMPTY_UNMODIFIABLE_ACCOUNT_TYPE_MAP = Collections.unmodifiableMap(new HashMap());
    private static final Uri SAMPLE_CONTACT_URI = Contacts.getLookupUri(1, "xxx");
    private AccountManager mAccountManager;
    private Map<AccountTypeWithDataSet, AccountType> mAccountTypesWithDataSets = Maps.newHashMap();
    private List<AccountWithDataSet> mAccounts = Lists.newArrayList();
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            AccountTypeManagerImpl.this.mListenerHandler.sendMessage(AccountTypeManagerImpl.this.mListenerHandler.obtainMessage(1, intent));
        }
    };
    private final Runnable mCheckFilterValidityRunnable = new Runnable() {
        public void run() {
            ContactListFilterController.getInstance(AccountTypeManagerImpl.this.mContext).checkFilterValidity(true);
        }
    };
    private List<AccountWithDataSet> mContactWritableAccounts = Lists.newArrayList();
    private Context mContext;
    private AccountType mFallbackAccountType;
    private List<AccountWithDataSet> mGroupWritableAccounts = Lists.newArrayList();
    private volatile CountDownLatch mInitializationLatch = new CountDownLatch(1);
    private final InvitableAccountTypeCache mInvitableAccountTypeCache;
    private Map<AccountTypeWithDataSet, AccountType> mInvitableAccountTypes = EMPTY_UNMODIFIABLE_ACCOUNT_TYPE_MAP;
    private final AtomicBoolean mInvitablesCacheIsInitialized = new AtomicBoolean(false);
    private final AtomicBoolean mInvitablesTaskIsRunning = new AtomicBoolean(false);
    private Handler mListenerHandler;
    private HandlerThread mListenerThread;
    private final Handler mMainThreadHandler = new Handler(Looper.getMainLooper());

    /* compiled from: AccountTypeManager */
    private class FindInvitablesTask extends AsyncTask<Void, Void, Map<AccountTypeWithDataSet, AccountType>> {
        private FindInvitablesTask() {
        }

        protected Map<AccountTypeWithDataSet, AccountType> doInBackground(Void... params) {
            return AccountTypeManagerImpl.this.findUsableInvitableAccountTypes(AccountTypeManagerImpl.this.mContext);
        }

        protected void onPostExecute(Map<AccountTypeWithDataSet, AccountType> accountTypes) {
            AccountTypeManagerImpl.this.mInvitableAccountTypeCache.setCachedValue(accountTypes);
            AccountTypeManagerImpl.this.mInvitablesTaskIsRunning.set(false);
        }
    }

    /* compiled from: AccountTypeManager */
    private static final class InvitableAccountTypeCache {
        private Map<AccountTypeWithDataSet, AccountType> mInvitableAccountTypes;
        private long mTimeLastSet;

        private InvitableAccountTypeCache() {
        }

        public boolean isExpired() {
            return SystemClock.elapsedRealtime() - this.mTimeLastSet > 60000;
        }

        public Map<AccountTypeWithDataSet, AccountType> getCachedValue() {
            return this.mInvitableAccountTypes;
        }

        public void setCachedValue(Map<AccountTypeWithDataSet, AccountType> map) {
            this.mInvitableAccountTypes = map;
            this.mTimeLastSet = SystemClock.elapsedRealtime();
        }
    }

    public AccountTypeManagerImpl(Context context) {
        this.mContext = context;
        this.mFallbackAccountType = new FallbackAccountType(context);
        this.mAccountManager = AccountManager.get(this.mContext);
        this.mListenerThread = new HandlerThread("AccountChangeListener");
        this.mListenerThread.start();
        this.mListenerHandler = new Handler(this.mListenerThread.getLooper()) {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 0:
                        AccountTypeManagerImpl.this.loadAccountsInBackground();
                        return;
                    case 1:
                        AccountTypeManagerImpl.this.processBroadcastIntent((Intent) msg.obj);
                        return;
                    default:
                        return;
                }
            }
        };
        this.mInvitableAccountTypeCache = new InvitableAccountTypeCache();
        IntentFilter filter = new IntentFilter("android.intent.action.PACKAGE_ADDED");
        filter.addAction("android.intent.action.PACKAGE_REMOVED");
        filter.addAction("android.intent.action.PACKAGE_CHANGED");
        filter.addDataScheme("package");
        this.mContext.registerReceiver(this.mBroadcastReceiver, filter);
        IntentFilter sdFilter = new IntentFilter();
        sdFilter.addAction("android.intent.action.EXTERNAL_APPLICATIONS_AVAILABLE");
        sdFilter.addAction("android.intent.action.EXTERNAL_APPLICATIONS_UNAVAILABLE");
        this.mContext.registerReceiver(this.mBroadcastReceiver, sdFilter);
        this.mContext.registerReceiver(this.mBroadcastReceiver, new IntentFilter("android.intent.action.LOCALE_CHANGED"));
        this.mAccountManager.addOnAccountsUpdatedListener(this, this.mListenerHandler, false);
        ContentResolver.addStatusChangeListener(1, this);
        this.mListenerHandler.sendEmptyMessage(0);
        SimFactoryManager.setConfigChangeListener(this);
    }

    public void onStatusChanged(int which) {
        this.mListenerHandler.sendEmptyMessage(0);
    }

    public void processBroadcastIntent(Intent intent) {
        this.mListenerHandler.sendEmptyMessage(0);
    }

    public void onAccountsUpdated(Account[] accounts) {
        if (HwLog.HWFLOW) {
            HwLog.i("AccountTypeManager", "listen account update");
        }
        this.mListenerHandler.sendEmptyMessage(0);
        this.mContext.sendBroadcast(new Intent("com.android.contacts.favorites.updated"));
    }

    void ensureAccountsLoaded() {
        CountDownLatch latch = this.mInitializationLatch;
        if (latch != null) {
            while (true) {
                try {
                    latch.await();
                    break;
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    protected void loadAccountsInBackground() {
        AccountType accountType;
        if (HwLog.HWFLOW) {
            HwLog.i("ContactsPerf", "AccountTypeManager.loadAccountsInBackground start");
        }
        TimingLogger timingLogger = new TimingLogger("AccountTypeManager", "loadAccountsInBackground");
        long startTime = SystemClock.currentThreadTimeMillis();
        long startTimeWall = SystemClock.elapsedRealtime();
        Map<AccountTypeWithDataSet, AccountType> accountTypesByTypeAndDataSet = Maps.newHashMap();
        Map<String, List<AccountType>> accountTypesByType = Maps.newHashMap();
        List<AccountWithDataSet> allAccounts = Lists.newArrayList();
        List<AccountWithDataSet> contactWritableAccounts = Lists.newArrayList();
        List<AccountWithDataSet> groupWritableAccounts = Lists.newArrayList();
        Set<String> extensionPackages = Sets.newHashSet();
        initHwDefinedAccout(accountTypesByTypeAndDataSet, accountTypesByType, extensionPackages);
        AccountManager am = this.mAccountManager;
        IContentService cs = ContentResolver.getContentService();
        try {
            SyncAdapterType[] syncs = cs.getSyncAdapterTypes();
            AuthenticatorDescription[] auths = am.getAuthenticatorTypes();
            for (SyncAdapterType sync : syncs) {
                if ("com.android.contacts".equals(sync.authority)) {
                    String type = sync.accountType;
                    AuthenticatorDescription auth = findAuthenticator(auths, type);
                    if (auth == null) {
                        HwLog.w("AccountTypeManager", "No authenticator found for type=" + type + ", ignoring it.");
                    } else {
                        if ("com.google".equals(type)) {
                            accountType = new GoogleAccountType(this.mContext, auth.packageName);
                        } else if (ExchangeAccountType.isExchangeType(type)) {
                            accountType = new ExchangeAccountType(this.mContext, auth.packageName, type);
                        } else if (!("com.android.huawei.sim".equals(type) || "com.android.huawei.secondsim".equals(type) || CommonUtilMethods.isLocalDefaultAccount(type))) {
                            if (HwLog.HWDBG) {
                                HwLog.d("AccountTypeManager", "Registering external account type=" + type + ", packageName=" + auth.packageName);
                            }
                            accountType = new ExternalAccountType(this.mContext, auth.packageName, type, false);
                            if (!((ExternalAccountType) accountType).isInitialized()) {
                                HwLog.e("AccountTypeManager", "ExternalAccountType couldn't be initialized, account type=" + type);
                            }
                        }
                        accountType.accountType = auth.type;
                        accountType.titleRes = auth.labelId;
                        accountType.iconRes = auth.iconId;
                        if (HwLog.HWDBG) {
                            HwLog.d("AccountTypeManager", "Adding account type = " + accountType + " in the cache");
                        }
                        addAccountType(accountType, accountTypesByTypeAndDataSet, accountTypesByType);
                        extensionPackages.addAll(accountType.getExtensionPackageNames());
                    }
                }
            }
            if (!extensionPackages.isEmpty()) {
                if (HwLog.HWDBG) {
                    HwLog.d("AccountTypeManager", "Registering " + extensionPackages.size() + " extension packages");
                }
                for (String extensionPackage : extensionPackages) {
                    ExternalAccountType accountType2 = new ExternalAccountType(this.mContext, extensionPackage, true);
                    if (accountType2.isInitialized()) {
                        if (!accountType2.hasContactsMetadata()) {
                            HwLog.w("AccountTypeManager", "Skipping extension package " + extensionPackage + " because" + " it doesn't have the CONTACTS_STRUCTURE metadata");
                        } else if (TextUtils.isEmpty(accountType2.accountType)) {
                            HwLog.w("AccountTypeManager", "Skipping extension package " + extensionPackage + " because" + " the CONTACTS_STRUCTURE metadata doesn't have the accountType" + " attribute");
                        } else {
                            addAccountType(accountType2, accountTypesByTypeAndDataSet, accountTypesByType);
                        }
                    }
                }
            }
        } catch (RemoteException e) {
            HwLog.w("AccountTypeManager", "Problem loading accounts: " + e.toString());
        }
        timingLogger.addSplit("Loaded account types");
        addHwDefinedAccountType(accountTypesByType, allAccounts, contactWritableAccounts, groupWritableAccounts);
        Account[] accounts = this.mAccountManager.getAccounts();
        if (HwLog.HWDBG) {
            HwLog.d("AccountTypeManager", "AccountManager, account size is: " + accounts.length);
        }
        for (Account account : accounts) {
            boolean syncable = false;
            try {
                syncable = cs.getIsSyncable(account, "com.android.contacts") > 0;
            } catch (Throwable e2) {
                HwLog.e("AccountTypeManager", "Cannot obtain sync flag for account: ", e2);
            }
            List<AccountType> accountTypes;
            if (syncable) {
                accountTypes = (List) accountTypesByType.get(account.type);
                if (accountTypes != null) {
                    for (AccountType accountType3 : accountTypes) {
                        AccountWithDataSet accountWithDataSet = new AccountWithDataSet(account.name, account.type, accountType3.dataSet);
                        allAccounts.add(accountWithDataSet);
                        if (accountType3.areContactsWritable()) {
                            contactWritableAccounts.add(accountWithDataSet);
                        }
                        if (accountType3.isGroupMembershipEditable()) {
                            groupWritableAccounts.add(accountWithDataSet);
                        }
                    }
                }
            } else if (HwLog.HWDBG) {
                accountTypes = (List) accountTypesByType.get(account.type);
                if (accountTypes != null && accountTypes.size() > 0) {
                    HwLog.d("AccountTypeManager", "Error, not syncable found for type: " + account.type);
                }
            }
        }
        Collections.sort(allAccounts, ACCOUNT_COMPARATOR);
        Collections.sort(contactWritableAccounts, ACCOUNT_COMPARATOR);
        Collections.sort(groupWritableAccounts, ACCOUNT_COMPARATOR);
        timingLogger.addSplit("Loaded accounts");
        synchronized (this) {
            this.mAccountTypesWithDataSets = accountTypesByTypeAndDataSet;
            this.mAccounts = allAccounts;
            this.mContactWritableAccounts = contactWritableAccounts;
            this.mGroupWritableAccounts = groupWritableAccounts;
            this.mInvitableAccountTypes = findAllInvitableAccountTypes(this.mContext, allAccounts, accountTypesByTypeAndDataSet);
        }
        timingLogger.dumpToLog();
        long endTimeWall = SystemClock.elapsedRealtime();
        long endTime = SystemClock.currentThreadTimeMillis();
        if (HwLog.HWDBG) {
            synchronized (this) {
                HwLog.d("AccountTypeManager", "Loaded meta-data for " + this.mAccountTypesWithDataSets.size() + " account types, " + this.mAccounts.size() + " accounts in " + (endTimeWall - startTimeWall) + "ms(wall) " + (endTime - startTime) + "ms(cpu)");
            }
        }
        if (this.mInitializationLatch != null) {
            this.mInitializationLatch.countDown();
            this.mInitializationLatch = null;
        }
        if (HwLog.HWDBG) {
            HwLog.d("ContactsPerf", "AccountTypeManager.loadAccountsInBackground finish");
        }
        if (this.mAccountLoadListener != null) {
            this.mAccountLoadListener.onAccountsLoadCompleted();
        }
        this.mMainThreadHandler.post(this.mCheckFilterValidityRunnable);
    }

    private void addAccountType(AccountType accountType, Map<AccountTypeWithDataSet, AccountType> accountTypesByTypeAndDataSet, Map<String, List<AccountType>> accountTypesByType) {
        accountTypesByTypeAndDataSet.put(accountType.getAccountTypeAndDataSet(), accountType);
        List<AccountType> accountsForType = (List) accountTypesByType.get(accountType.accountType);
        if (accountsForType == null) {
            accountsForType = Lists.newArrayList();
        }
        accountsForType.add(accountType);
        accountTypesByType.put(accountType.accountType, accountsForType);
    }

    protected static AuthenticatorDescription findAuthenticator(AuthenticatorDescription[] auths, String accountType) {
        for (AuthenticatorDescription auth : auths) {
            if (accountType.equals(auth.type)) {
                return auth;
            }
        }
        return null;
    }

    public List<AccountWithDataSet> getAccounts(boolean contactWritableOnly) {
        ensureAccountsLoaded();
        return contactWritableOnly ? this.mContactWritableAccounts : this.mAccounts;
    }

    public List<AccountWithDataSet> getGroupWritableAccounts() {
        List<AccountWithDataSet> list;
        ensureAccountsLoaded();
        synchronized (this) {
            list = this.mGroupWritableAccounts;
        }
        return list;
    }

    public DataKind getKindOrFallback(AccountType type, String mimeType) {
        ensureAccountsLoaded();
        DataKind kind = null;
        synchronized (this) {
            if (type != null) {
                kind = type.getKindForMimetype(mimeType);
            }
        }
        if (kind == null) {
            kind = this.mFallbackAccountType.getKindForMimetype(mimeType);
        }
        if (kind == null && HwLog.HWDBG) {
            HwLog.d("AccountTypeManager", "Unknown type=" + type + ", mime=" + mimeType);
        }
        return kind;
    }

    public AccountType getAccountType(AccountTypeWithDataSet accountTypeWithDataSet) {
        AccountType type;
        ensureAccountsLoaded();
        synchronized (this) {
            type = (AccountType) this.mAccountTypesWithDataSets.get(accountTypeWithDataSet);
            if (type == null) {
                type = this.mFallbackAccountType;
            }
        }
        return type;
    }

    private Map<AccountTypeWithDataSet, AccountType> getAllInvitableAccountTypes() {
        ensureAccountsLoaded();
        return this.mInvitableAccountTypes;
    }

    public Map<AccountTypeWithDataSet, AccountType> getUsableInvitableAccountTypes() {
        ensureAccountsLoaded();
        if (!this.mInvitablesCacheIsInitialized.get()) {
            this.mInvitableAccountTypeCache.setCachedValue(findUsableInvitableAccountTypes(this.mContext));
            this.mInvitablesCacheIsInitialized.set(true);
        } else if (this.mInvitableAccountTypeCache.isExpired() && this.mInvitablesTaskIsRunning.compareAndSet(false, true)) {
            new FindInvitablesTask().execute(new Void[0]);
        }
        return this.mInvitableAccountTypeCache.getCachedValue();
    }

    @VisibleForTesting
    static Map<AccountTypeWithDataSet, AccountType> findAllInvitableAccountTypes(Context context, Collection<AccountWithDataSet> accounts, Map<AccountTypeWithDataSet, AccountType> accountTypesByTypeAndDataSet) {
        HashMap<AccountTypeWithDataSet, AccountType> result = Maps.newHashMap();
        for (AccountWithDataSet account : accounts) {
            AccountTypeWithDataSet accountTypeWithDataSet = account.getAccountTypeWithDataSet();
            AccountType type = (AccountType) accountTypesByTypeAndDataSet.get(accountTypeWithDataSet);
            if (!(type == null || result.containsKey(accountTypeWithDataSet) || TextUtils.isEmpty(type.getInviteContactActivityClassName()))) {
                result.put(accountTypeWithDataSet, type);
            }
        }
        return Collections.unmodifiableMap(result);
    }

    private Map<AccountTypeWithDataSet, AccountType> findUsableInvitableAccountTypes(Context context) {
        Map<AccountTypeWithDataSet, AccountType> allInvitables = getAllInvitableAccountTypes();
        if (allInvitables.isEmpty()) {
            return EMPTY_UNMODIFIABLE_ACCOUNT_TYPE_MAP;
        }
        HashMap<AccountTypeWithDataSet, AccountType> result = Maps.newHashMap();
        result.putAll(allInvitables);
        PackageManager packageManager = context.getPackageManager();
        for (Entry<AccountTypeWithDataSet, AccountType> entry : allInvitables.entrySet()) {
            AccountTypeWithDataSet accountTypeWithDataSet = (AccountTypeWithDataSet) entry.getKey();
            Intent invitableIntent = MoreContactUtils.getInvitableIntent((AccountType) entry.getValue(), SAMPLE_CONTACT_URI);
            if (invitableIntent == null) {
                result.remove(accountTypeWithDataSet);
            } else if (packageManager.resolveActivity(invitableIntent, 65536) == null) {
                result.remove(accountTypeWithDataSet);
            } else if (!accountTypeWithDataSet.hasData(context)) {
                result.remove(accountTypeWithDataSet);
            }
        }
        return Collections.unmodifiableMap(result);
    }

    public List<AccountType> getAccountTypes(boolean contactWritableOnly) {
        ensureAccountsLoaded();
        List<AccountType> accountTypes = Lists.newArrayList();
        synchronized (this) {
            for (AccountType type : this.mAccountTypesWithDataSets.values()) {
                if (!contactWritableOnly || type.areContactsWritable()) {
                    accountTypes.add(type);
                }
            }
        }
        return accountTypes;
    }

    public void configChanged() {
        this.mListenerHandler.sendEmptyMessage(0);
    }

    public List<AccountWithDataSet> getAccountsExcludeSim(boolean contactWritableOnly) {
        ensureAccountsLoaded();
        List<AccountWithDataSet> list = Lists.newArrayList();
        List<AccountWithDataSet> accountList;
        if (contactWritableOnly) {
            accountList = this.mContactWritableAccounts;
        } else {
            accountList = this.mAccounts;
        }
        for (AccountWithDataSet account : accountList) {
            if (!CommonUtilMethods.isSimAccount(account.type)) {
                list.add(account);
            }
        }
        return list;
    }

    public List<AccountWithDataSet> getAccountsExcludeSim1(boolean contactWritableOnly) {
        ensureAccountsLoaded();
        List<AccountWithDataSet> list = Lists.newArrayList();
        List<AccountWithDataSet> accountList;
        if (contactWritableOnly) {
            accountList = this.mContactWritableAccounts;
        } else {
            accountList = this.mAccounts;
        }
        for (AccountWithDataSet account : accountList) {
            if (!CommonUtilMethods.isSim1Account(account.type)) {
                list.add(account);
            }
        }
        return list;
    }

    public List<AccountWithDataSet> getAccountsExcludeBothSim(boolean contactWritableOnly) {
        ensureAccountsLoaded();
        List<AccountWithDataSet> list = Lists.newArrayList();
        List<AccountWithDataSet> accountList;
        if (contactWritableOnly) {
            accountList = this.mContactWritableAccounts;
        } else {
            accountList = this.mAccounts;
        }
        for (AccountWithDataSet account : accountList) {
            if (!(CommonUtilMethods.isSim1Account(account.type) || CommonUtilMethods.isSim2Account(account.type))) {
                list.add(account);
            }
        }
        return list;
    }

    public List<AccountWithDataSet> getAccountsExcludeSim2(boolean contactWritableOnly) {
        ensureAccountsLoaded();
        List<AccountWithDataSet> list = Lists.newArrayList();
        List<AccountWithDataSet> accountList;
        if (contactWritableOnly) {
            accountList = this.mContactWritableAccounts;
        } else {
            accountList = this.mAccounts;
        }
        for (AccountWithDataSet account : accountList) {
            if (!CommonUtilMethods.isSim2Account(account.type)) {
                list.add(account);
            }
        }
        return list;
    }

    public AccountWithDataSet getAccountWithDataSet(int aId) {
        ensureAccountsLoaded();
        switch (aId) {
            case 0:
                for (AccountWithDataSet account : this.mAccounts) {
                    if ("com.android.huawei.phone".equalsIgnoreCase(account.type)) {
                        return account;
                    }
                }
                break;
            case 1:
                for (AccountWithDataSet account2 : this.mAccounts) {
                    if (CommonUtilMethods.isSim1Account(account2.type)) {
                        return account2;
                    }
                }
                break;
            case 2:
                for (AccountWithDataSet account22 : this.mAccounts) {
                    if (CommonUtilMethods.isSim2Account(account22.type)) {
                        return account22;
                    }
                }
                break;
        }
        return null;
    }

    public void hiCloudServiceLogOnOff() {
        if (!this.mListenerHandler.hasMessages(0)) {
            this.mListenerHandler.sendEmptyMessage(0);
        }
    }

    private void initHwDefinedAccout(Map<AccountTypeWithDataSet, AccountType> accountTypesByTypeAndDataSet, Map<String, List<AccountType>> accountTypesByType, Set<String> extensionPackages) {
        initPhoneAccountType(accountTypesByTypeAndDataSet, accountTypesByType, extensionPackages);
        if (SimFactoryManager.isDualSim()) {
            boolean isSim1Present = SimFactoryManager.isSIM1CardPresent();
            boolean isSim2Present = SimFactoryManager.isSIM2CardPresent();
            boolean isSimEnabled = isSim1Present ? SimFactoryManager.isSimEnabled(0) : false;
            boolean isSimEnabled2 = isSim2Present ? SimFactoryManager.isSimEnabled(1) : false;
            if (HwLog.HWDBG) {
                HwLog.d("AccountTypeManager", "isSim1Present=" + isSim1Present + ", isSim1Enabled=" + isSimEnabled);
                HwLog.d("AccountTypeManager", "isSim2Present=" + isSim2Present + ", isSim2Enabled=" + isSimEnabled2);
            }
            if (isSimEnabled) {
                initSimAccountType(accountTypesByTypeAndDataSet, accountTypesByType, extensionPackages, "com.android.huawei.sim");
            }
            if (isSimEnabled2) {
                initSimAccountType(accountTypesByTypeAndDataSet, accountTypesByType, extensionPackages, "com.android.huawei.secondsim");
            }
        } else if (SimUtility.isSimReady(-1)) {
            initSimAccountType(accountTypesByTypeAndDataSet, accountTypesByType, extensionPackages, "com.android.huawei.sim");
        }
    }

    private void initPhoneAccountType(Map<AccountTypeWithDataSet, AccountType> accountTypesByTypeAndDataSet, Map<String, List<AccountType>> accountTypesByType, Set<String> extensionPackages) {
        if (HwLog.HWDBG) {
            HwLog.d("AccountTypeManager", "Adding default phone account type to the cache");
        }
        AccountType phoneAccountType = new ExternalAccountType(this.mContext, "com.android.contacts", "com.android.huawei.phone", false);
        if (!phoneAccountType.isInitialized()) {
            HwLog.e("AccountTypeManager", "phoneAccountType couldn't be initialized by load xml");
            phoneAccountType = new PhoneAccountType(this.mContext, "com.android.contacts");
        }
        phoneAccountType.titleRes = R.string.str_phoneaccount_name;
        phoneAccountType.iconRes = R.drawable.csp_actionbar_number_circle_light;
        addAccountType(phoneAccountType, accountTypesByTypeAndDataSet, accountTypesByType);
        extensionPackages.addAll(phoneAccountType.getExtensionPackageNames());
    }

    private void initSimAccountType(Map<AccountTypeWithDataSet, AccountType> accountTypesByTypeAndDataSet, Map<String, List<AccountType>> accountTypesByType, Set<String> extensionPackages, String type) {
        if (HwLog.HWDBG) {
            HwLog.d("AccountTypeManager", "Adding SIM account type to the cache, type=" + type);
        }
        SimFactory factory = SimFactoryManager.getSimFactory(type);
        if (factory != null) {
            SimAccountType simAccountType = factory.getSimAccountType();
            simAccountType.setConfigChangeListener(this);
            if ("com.android.huawei.sim".equals(type)) {
                simAccountType.titleRes = R.string.sim_one_account_name;
                simAccountType.iconRes = R.drawable.dial_num_0_blk;
            } else {
                simAccountType.titleRes = R.string.sim_two_account_name;
                simAccountType.iconRes = R.drawable.dial_num_0_blk_press;
            }
            addAccountType(simAccountType, accountTypesByTypeAndDataSet, accountTypesByType);
            extensionPackages.addAll(simAccountType.getExtensionPackageNames());
        } else if (HwLog.HWDBG) {
            HwLog.d("AccountTypeManager", "SIM factory unavailable in account type manager for type:" + type);
        }
    }

    private void addHwDefinedAccountType(Map<String, List<AccountType>> accountTypesByType, List<AccountWithDataSet> allAccounts, List<AccountWithDataSet> contactWritableAccounts, List<AccountWithDataSet> groupWritableAccounts) {
        addPhoneAccountType(accountTypesByType, allAccounts, contactWritableAccounts, groupWritableAccounts);
        if (SimFactoryManager.isDualSim()) {
            boolean isSim1Present = SimFactoryManager.isSIM1CardPresent();
            boolean isSim2Present = SimFactoryManager.isSIM2CardPresent();
            boolean isSimEnabled = isSim1Present ? SimFactoryManager.isSimEnabled(0) : false;
            boolean isSimEnabled2 = isSim2Present ? SimFactoryManager.isSimEnabled(1) : false;
            if (isSimEnabled) {
                addSimAccountType(accountTypesByType, allAccounts, contactWritableAccounts, groupWritableAccounts, "com.android.huawei.sim");
            }
            if (isSimEnabled2) {
                addSimAccountType(accountTypesByType, allAccounts, contactWritableAccounts, groupWritableAccounts, "com.android.huawei.secondsim");
            }
        } else if (SimFactoryManager.isSimReady(-1)) {
            addSimAccountType(accountTypesByType, allAccounts, contactWritableAccounts, groupWritableAccounts, "com.android.huawei.sim");
        }
    }

    private void setSimLoadingStateFinished(int slotId) {
        if (EmuiFeatureManager.isPreLoadingSimContactsEnabled()) {
            SimFactoryManager.setSimLoadingState(slotId, true);
            if (HwLog.HWDBG) {
                HwLog.d("AccountTypeManager", "setSimLoadingState to true, slodId=" + slotId);
            }
        }
    }

    private void addPhoneAccountType(Map<String, List<AccountType>> accountTypesByType, List<AccountWithDataSet> allAccounts, List<AccountWithDataSet> contactWritableAccounts, List<AccountWithDataSet> groupWritableAccounts) {
        List<AccountType> accountTypes = (List) accountTypesByType.get("com.android.huawei.phone");
        if (accountTypes != null) {
            for (AccountType accountType : accountTypes) {
                AccountWithDataSet accountWithDataSet = new AccountWithDataSet("Phone", "com.android.huawei.phone", accountType.dataSet);
                allAccounts.add(accountWithDataSet);
                if (accountType.areContactsWritable()) {
                    contactWritableAccounts.add(accountWithDataSet);
                }
                if (accountType.isGroupMembershipEditable()) {
                    groupWritableAccounts.add(accountWithDataSet);
                }
            }
        }
    }

    private void addSimAccountType(Map<String, List<AccountType>> accountTypesByType, List<AccountWithDataSet> allAccounts, List<AccountWithDataSet> contactWritableAccounts, List<AccountWithDataSet> groupWritableAccounts, String simAccountType) {
        List<AccountType> accountTypes = (List) accountTypesByType.get(simAccountType);
        if (accountTypes != null) {
            String simAccountName = SimFactoryManager.getAccountName(simAccountType);
            setSimLoadingStateFinished(SimFactoryManager.getSlotIdBasedOnAccountType(simAccountType));
            if (MultiUsersUtils.isCurrentUserOwner()) {
                for (AccountType accountType : accountTypes) {
                    AccountWithDataSet accountWithDataSet = new AccountWithDataSet(simAccountName, simAccountType, accountType.dataSet);
                    allAccounts.add(accountWithDataSet);
                    if (accountType.areContactsWritable()) {
                        contactWritableAccounts.add(accountWithDataSet);
                    }
                    if (accountType.isGroupMembershipEditable()) {
                        groupWritableAccounts.add(accountWithDataSet);
                    }
                }
            }
        }
    }
}
