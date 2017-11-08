package com.android.contacts.hap;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorDescription;
import android.accounts.OnAccountsUpdateListener;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.net.Uri;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.HandlerThread;
import android.os.Message;
import android.provider.ContactsContract.RawContacts;
import com.android.contacts.hap.sim.SimFactoryManager;
import com.android.contacts.util.ContactDisplayUtils;
import com.android.contacts.util.HwLog;
import com.google.android.gms.R;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

/* compiled from: AccountsDataManager */
class AcountDataManagerImpl extends AccountsDataManager implements OnAccountsUpdateListener {
    private static final long ACCOUNT_UNIT_BIT_MASK = Long.valueOf(1).longValue();
    private static String sMoreAccountTypeDescription;
    private static Bitmap sMoreIcon;
    private HashMap<String, AccountDetailsHolder> mAccountIndexMap;
    private HashMap<Long, Long> mContactsAccInfoCache;
    private final Context mContext;
    private boolean mIsAccountsUpdating;
    private boolean mIsLoading;
    private boolean mIsShowIcons;
    private AccountCacheUpdatedListener mListener;
    private LoaderThread mLoaderThread;
    private Object mLock = new Object();
    private boolean mPaused;
    private Long mPendingRequestContactId;
    private StaleAccDataHolder mStaleAccDataHolder;
    private List<String> mWhiteList;

    /* compiled from: AccountsDataManager */
    private static class AccountDetailsHolder {
        Integer mAccIndex;
        Bitmap mAccountIcon;

        public AccountDetailsHolder(int aAccIndex, Bitmap aIcon) {
            this.mAccIndex = Integer.valueOf(aAccIndex);
            this.mAccountIcon = aIcon;
        }
    }

    /* compiled from: AccountsDataManager */
    private class LoaderThread extends HandlerThread implements Callback {
        private Handler mRequestHandler;
        private final ContentResolver mResolver;

        private void preloadAccountDataForContactIds() {
            /* JADX: method processing error */
/*
Error: java.util.NoSuchElementException
	at java.util.HashMap$HashIterator.nextNode(HashMap.java:1431)
	at java.util.HashMap$KeyIterator.next(HashMap.java:1453)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.applyRemove(BlockFinallyExtract.java:535)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.extractFinally(BlockFinallyExtract.java:175)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.processExceptionHandler(BlockFinallyExtract.java:80)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.visit(BlockFinallyExtract.java:51)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
*/
            /*
            r11 = this;
            r7 = 0;
            r0 = 2;
            r2 = new java.lang.String[r0];	 Catch:{ IllegalStateException -> 0x0040, all -> 0x0061 }
            r0 = "contact_id";	 Catch:{ IllegalStateException -> 0x0040, all -> 0x0061 }
            r1 = 0;	 Catch:{ IllegalStateException -> 0x0040, all -> 0x0061 }
            r2[r1] = r0;	 Catch:{ IllegalStateException -> 0x0040, all -> 0x0061 }
            r0 = "account_type";	 Catch:{ IllegalStateException -> 0x0040, all -> 0x0061 }
            r1 = 1;	 Catch:{ IllegalStateException -> 0x0040, all -> 0x0061 }
            r2[r1] = r0;	 Catch:{ IllegalStateException -> 0x0040, all -> 0x0061 }
            r3 = "deleted=0";	 Catch:{ IllegalStateException -> 0x0040, all -> 0x0061 }
            r0 = r11.mResolver;	 Catch:{ IllegalStateException -> 0x0040, all -> 0x0061 }
            r1 = android.provider.ContactsContract.RawContacts.CONTENT_URI;	 Catch:{ IllegalStateException -> 0x0040, all -> 0x0061 }
            r5 = "contact_id";	 Catch:{ IllegalStateException -> 0x0040, all -> 0x0061 }
            r4 = 0;	 Catch:{ IllegalStateException -> 0x0040, all -> 0x0061 }
            r7 = r0.query(r1, r2, r3, r4, r5);	 Catch:{ IllegalStateException -> 0x0040, all -> 0x0061 }
            if (r7 == 0) goto L_0x003a;	 Catch:{ IllegalStateException -> 0x0040, all -> 0x0061 }
        L_0x0021:
            r0 = r7.moveToFirst();	 Catch:{ IllegalStateException -> 0x0040, all -> 0x0061 }
            if (r0 == 0) goto L_0x003a;	 Catch:{ IllegalStateException -> 0x0040, all -> 0x0061 }
        L_0x0027:
            r0 = 0;	 Catch:{ IllegalStateException -> 0x0040, all -> 0x0061 }
            r8 = r7.getLong(r0);	 Catch:{ IllegalStateException -> 0x0040, all -> 0x0061 }
            r0 = 1;	 Catch:{ IllegalStateException -> 0x0040, all -> 0x0061 }
            r6 = r7.getString(r0);	 Catch:{ IllegalStateException -> 0x0040, all -> 0x0061 }
            r11.loadAccountDataCache(r8, r6);	 Catch:{ IllegalStateException -> 0x0040, all -> 0x0061 }
            r0 = r7.moveToNext();	 Catch:{ IllegalStateException -> 0x0040, all -> 0x0061 }
            if (r0 != 0) goto L_0x0027;
        L_0x003a:
            if (r7 == 0) goto L_0x003f;
        L_0x003c:
            r7.close();
        L_0x003f:
            return;
        L_0x0040:
            r10 = move-exception;
            r0 = "AccountDataManager";	 Catch:{ IllegalStateException -> 0x0040, all -> 0x0061 }
            r1 = new java.lang.StringBuilder;	 Catch:{ IllegalStateException -> 0x0040, all -> 0x0061 }
            r1.<init>();	 Catch:{ IllegalStateException -> 0x0040, all -> 0x0061 }
            r4 = "preloadAccountDataForContactIds ise:";	 Catch:{ IllegalStateException -> 0x0040, all -> 0x0061 }
            r1 = r1.append(r4);	 Catch:{ IllegalStateException -> 0x0040, all -> 0x0061 }
            r1 = r1.append(r10);	 Catch:{ IllegalStateException -> 0x0040, all -> 0x0061 }
            r1 = r1.toString();	 Catch:{ IllegalStateException -> 0x0040, all -> 0x0061 }
            com.android.contacts.util.HwLog.e(r0, r1);	 Catch:{ IllegalStateException -> 0x0040, all -> 0x0061 }
            if (r7 == 0) goto L_0x003f;
        L_0x005d:
            r7.close();
            goto L_0x003f;
        L_0x0061:
            r0 = move-exception;
            if (r7 == 0) goto L_0x0067;
        L_0x0064:
            r7.close();
        L_0x0067:
            throw r0;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.contacts.hap.AcountDataManagerImpl.LoaderThread.preloadAccountDataForContactIds():void");
        }

        public LoaderThread(ContentResolver resolver) {
            super("AccountDataLoader");
            this.mResolver = resolver;
        }

        public boolean handleMessage(Message msg) {
            AccountCacheUpdatedListener lListener;
            switch (msg.what) {
                case 1:
                    if (AcountDataManagerImpl.ACC_DATA_MNGR_LOG_ENABLED) {
                        HwLog.d("AccountDataManager", "+ handleMessage PRELOAD_ACCDATA");
                    }
                    populateAccountIndexMap();
                    preloadAccountDataForContactIds();
                    AcountDataManagerImpl.this.mIsAccountsUpdating = false;
                    break;
                case 2:
                    if (AcountDataManagerImpl.ACC_DATA_MNGR_LOG_ENABLED) {
                        HwLog.d("AccountDataManager", "+ handleMessage LOAD_ACCDATA_BY_CONTACT_ID");
                    }
                    boolean success = loadAccountDataForContactId(AcountDataManagerImpl.this.mPendingRequestContactId.longValue());
                    lListener = AcountDataManagerImpl.this.mListener;
                    if (success && lListener != null) {
                        if (AcountDataManagerImpl.ACC_DATA_MNGR_LOG_ENABLED) {
                            HwLog.d("AccountDataManager", "mListener onCacheUpdated is called!!!");
                        }
                        lListener.onCacheUpdated();
                    }
                    return true;
                case 3:
                    if (AcountDataManagerImpl.ACC_DATA_MNGR_LOG_ENABLED) {
                        HwLog.d("AccountDataManager", "+ handleMessage RELOAD_ACC_CACHE");
                    }
                    preloadAccountDataForContactIds();
                    break;
            }
            AcountDataManagerImpl.this.mIsLoading = false;
            lListener = AcountDataManagerImpl.this.mListener;
            if (lListener != null) {
                if (AcountDataManagerImpl.ACC_DATA_MNGR_LOG_ENABLED) {
                    HwLog.d("AccountDataManager", "mListener onCacheUpdated is called!!!");
                }
                lListener.onCacheUpdated();
            }
            AcountDataManagerImpl.this.clearStaleData();
            return true;
        }

        public void requestPreloading() {
            ensureHandler();
            if (!this.mRequestHandler.hasMessages(1)) {
                this.mRequestHandler.sendEmptyMessageDelayed(1, 1000);
            }
        }

        public void requestReloadingCache() {
            ensureHandler();
            if (!this.mRequestHandler.hasMessages(3)) {
                this.mRequestHandler.sendEmptyMessageDelayed(3, 1000);
            }
        }

        public void requestLoadAccCacheByContactId(long aContactId) {
            ensureHandler();
            if (!this.mRequestHandler.hasMessages(2) || AcountDataManagerImpl.this.mPendingRequestContactId.longValue() != aContactId) {
                AcountDataManagerImpl.this.mPendingRequestContactId = Long.valueOf(aContactId);
                Message msg = new Message();
                msg.what = 2;
                this.mRequestHandler.sendMessage(msg);
            }
        }

        public void ensureHandler() {
            if (this.mRequestHandler == null) {
                this.mRequestHandler = new Handler(getLooper(), this);
            }
        }

        private boolean loadAccountDataForContactId(long aContactId) {
            boolean success = true;
            Cursor cursor = null;
            try {
                cursor = this.mResolver.query(RawContacts.CONTENT_URI, new String[]{"contact_id", "account_type"}, "contact_id = " + aContactId, null, "contact_id");
                if (cursor != null && cursor.moveToFirst()) {
                    do {
                        if (!loadAccountDataCache((long) cursor.getInt(0), cursor.getString(1))) {
                            success = false;
                        }
                    } while (cursor.moveToNext());
                } else if (cursor != null) {
                    if (cursor.getCount() == 0) {
                        deleteEmptyContact(aContactId);
                        success = false;
                    }
                }
                if (cursor != null) {
                    cursor.close();
                }
                return success;
            } catch (Throwable th) {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }

        private int deleteEmptyContact(long contactId) {
            int rowsDeleted = -1;
            try {
                rowsDeleted = AcountDataManagerImpl.this.mContext.getContentResolver().delete(Uri.parse("content://com.android.contacts/contacts/" + contactId), null, null);
            } catch (RuntimeException e) {
            }
            return rowsDeleted;
        }

        private boolean loadAccountDataCache(long aContactId, String aAccType) {
            boolean foundAccountType = true;
            if (aAccType == null) {
                if (AcountDataManagerImpl.ACC_DATA_MNGR_LOG_ENABLED) {
                    HwLog.d("AccountDataManager", "relevant encoded account data for contactId = " + aContactId + " lAccData = 0");
                }
                synchronized (AcountDataManagerImpl.this.mLock) {
                    AcountDataManagerImpl.this.mContactsAccInfoCache.put(Long.valueOf(aContactId), Long.valueOf(0));
                }
                return false;
            }
            int lAccIndex = -1;
            synchronized (AcountDataManagerImpl.this.mLock) {
                AccountDetailsHolder holder = (AccountDetailsHolder) AcountDataManagerImpl.this.mAccountIndexMap.get(aAccType);
                if (holder != null) {
                    lAccIndex = holder.mAccIndex.intValue();
                }
            }
            if (holder != null) {
                long lAccData = AcountDataManagerImpl.ACCOUNT_UNIT_BIT_MASK << Integer.valueOf(lAccIndex - 1).intValue();
                if (AcountDataManagerImpl.ACC_DATA_MNGR_LOG_ENABLED) {
                    HwLog.d("AccountDataManager", "got Acc index " + lAccIndex + " for acc Type " + aAccType);
                }
                synchronized (AcountDataManagerImpl.this.mLock) {
                    if (AcountDataManagerImpl.this.mContactsAccInfoCache.containsKey(Long.valueOf(aContactId))) {
                        lAccData |= ((Long) AcountDataManagerImpl.this.mContactsAccInfoCache.get(Long.valueOf(aContactId))).longValue();
                    }
                    AcountDataManagerImpl.this.mContactsAccInfoCache.put(Long.valueOf(aContactId), Long.valueOf(lAccData));
                }
            } else {
                foundAccountType = false;
                if (HwLog.HWFLOW) {
                    HwLog.i("AccountDataManager", "Account index not found for type: " + aAccType);
                }
            }
            return foundAccountType;
        }

        private void populateAccountIndexMap() {
            AccountManager lAccManager = AccountManager.get(AcountDataManagerImpl.this.mContext);
            Account[] accounts = lAccManager.getAccounts();
            PackageManager pm = AcountDataManagerImpl.this.mContext.getPackageManager();
            AuthenticatorDescription[] auths = lAccManager.getAuthenticatorTypes();
            int accIndex = 1;
            for (Account account : accounts) {
                String accType = account.type;
                synchronized (AcountDataManagerImpl.this.mLock) {
                    if (AcountDataManagerImpl.this.mAccountIndexMap.get(accType) != null) {
                    } else {
                        Bitmap lAccIcon = null;
                        AuthenticatorDescription auth = findAuthenticator(auths, accType);
                        if (auth != null) {
                            lAccIcon = getScaledBitmap(pm, auth.packageName, auth.smallIconId, auth.iconId);
                        }
                        if (AcountDataManagerImpl.ACC_DATA_MNGR_LOG_ENABLED) {
                            HwLog.d("AccountDataManager", "Index accType = " + accType + " is accIndex = " + accIndex);
                        }
                        synchronized (AcountDataManagerImpl.this.mLock) {
                            AcountDataManagerImpl.this.mAccountIndexMap.put(accType, new AccountDetailsHolder(accIndex, lAccIcon));
                        }
                        accIndex++;
                    }
                }
            }
            synchronized (AcountDataManagerImpl.this.mLock) {
                AcountDataManagerImpl.this.mAccountIndexMap.put("com.android.huawei.phone", new AccountDetailsHolder(accIndex, null));
                accIndex++;
                AcountDataManagerImpl.this.mAccountIndexMap.put("com.android.huawei.sim", new AccountDetailsHolder(accIndex, null));
                accIndex++;
                if (SimFactoryManager.isDualSim()) {
                    AcountDataManagerImpl.this.mAccountIndexMap.put("com.android.huawei.secondsim", new AccountDetailsHolder(accIndex, null));
                    accIndex++;
                }
            }
        }

        private Bitmap getScaledBitmap(PackageManager pm, String aPkgname, int aSmallIconId, int aIconId) {
            Options lBitmapOptions = new Options();
            lBitmapOptions.inPurgeable = true;
            lBitmapOptions.inInputShareable = false;
            try {
                Bitmap lBitmap = BitmapFactory.decodeResource(pm.getResourcesForApplication(aPkgname), aSmallIconId, lBitmapOptions);
                if (lBitmap == null) {
                    lBitmap = BitmapFactory.decodeResource(pm.getResourcesForApplication(aPkgname), aIconId, lBitmapOptions);
                    if (lBitmap == null) {
                        lBitmap = BitmapFactory.decodeResource(AcountDataManagerImpl.this.mContext.getResources(), R.drawable.hwsns_icon, lBitmapOptions);
                    }
                }
                int accountIconSize = AcountDataManagerImpl.this.mContext.getResources().getDimensionPixelSize(R.dimen.account_icon_size);
                return Bitmap.createScaledBitmap(lBitmap, accountIconSize, accountIconSize, false);
            } catch (NameNotFoundException e) {
                e.printStackTrace();
                return null;
            } catch (RuntimeException e2) {
                HwLog.e("AccountDataManager", "exception in getScaledBitmap: " + e2);
                e2.printStackTrace();
                return null;
            }
        }

        private AuthenticatorDescription findAuthenticator(AuthenticatorDescription[] auths, String accountType) {
            for (AuthenticatorDescription auth : auths) {
                if (accountType.equals(auth.type)) {
                    return auth;
                }
            }
            return null;
        }
    }

    /* compiled from: AccountsDataManager */
    private static class StaleAccDataHolder {
        boolean isDataStaled;
        HashMap<String, AccountDetailsHolder> mStaleAccountIndexMap;
        HashMap<Long, Long> mStaleContactsAccInfoCache;

        public StaleAccDataHolder() {
            this.isDataStaled = false;
            this.isDataStaled = false;
        }

        private void setStaleAccData(HashMap<Long, Long> aCache, HashMap<String, AccountDetailsHolder> aIndexMap) {
            this.mStaleContactsAccInfoCache = new HashMap(aCache);
            this.mStaleAccountIndexMap = new HashMap(aIndexMap);
            this.isDataStaled = true;
        }

        private HashMap<Long, Long> getStaleAccCache() {
            return this.mStaleContactsAccInfoCache;
        }

        private HashMap<String, AccountDetailsHolder> getStaleAccIndexMap() {
            return this.mStaleAccountIndexMap;
        }

        private void clearStaleData() {
            this.isDataStaled = false;
            if (this.mStaleContactsAccInfoCache != null) {
                synchronized (this.mStaleContactsAccInfoCache) {
                    this.mStaleContactsAccInfoCache.clear();
                }
            }
            if (this.mStaleAccountIndexMap != null) {
                synchronized (this.mStaleAccountIndexMap) {
                    this.mStaleAccountIndexMap.clear();
                }
            }
            this.mStaleContactsAccInfoCache = null;
            this.mStaleAccountIndexMap = null;
        }
    }

    public AcountDataManagerImpl(Context aContext) {
        this.mContext = aContext;
        Resources res = this.mContext.getResources();
        this.mWhiteList = Arrays.asList(res.getStringArray(R.array.account_type_white_list));
        this.mIsShowIcons = res.getBoolean(R.bool.show_account_icons);
        this.mContactsAccInfoCache = new HashMap();
        this.mAccountIndexMap = new HashMap();
        sMoreIcon = BitmapFactory.decodeResource(res, R.drawable.csp_actionbar_number_circle);
        sMoreAccountTypeDescription = res.getString(R.string.contacts_title_menu);
        this.mRawContactsObserver = new ContentObserver(new Handler()) {
            public void onChange(boolean selfChange) {
                if (!AcountDataManagerImpl.this.mIsLoading && !AcountDataManagerImpl.this.mIsAccountsUpdating) {
                    if (AcountDataManagerImpl.ACC_DATA_MNGR_LOG_ENABLED) {
                        HwLog.d("AccountDataManager", "RawContact table is updated entered onChange");
                    }
                    AcountDataManagerImpl.this.refreshCache();
                    AcountDataManagerImpl.this.reloadAccCache();
                }
            }
        };
        registerObservers();
    }

    private void refreshCache() {
        this.mIsLoading = true;
        if (ACC_DATA_MNGR_LOG_ENABLED) {
            HwLog.d("AccountDataManager", " + refreshCache mIsAccountsUpdating = " + this.mIsAccountsUpdating);
        }
        synchronized (this.mLock) {
            this.mStaleAccDataHolder = new StaleAccDataHolder();
            this.mStaleAccDataHolder.setStaleAccData(this.mContactsAccInfoCache, this.mAccountIndexMap);
            this.mContactsAccInfoCache.clear();
            this.mContactsAccInfoCache = new HashMap();
        }
        if (this.mIsAccountsUpdating) {
            synchronized (this.mLock) {
                this.mAccountIndexMap.clear();
                this.mAccountIndexMap = new HashMap();
            }
        }
    }

    private void registerObservers() {
        AccountManager.get(this.mContext).addOnAccountsUpdatedListener(this, null, false);
        this.mContext.getContentResolver().registerContentObserver(RawContacts.CONTENT_URI, false, this.mRawContactsObserver);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public Bitmap[] getAccountData(long aContactId, String[] accountTypeDescriptions) {
        if (ACC_DATA_MNGR_LOG_ENABLED) {
            HwLog.d("AccountDataManager", " + getAccountData called for ContactId " + aContactId);
        }
        Bitmap[] lIconInfo = new Bitmap[3];
        synchronized (this.mLock) {
            HashMap<Long, Long> lAccInfoCache = this.mContactsAccInfoCache;
            HashMap<String, AccountDetailsHolder> lAccIndexMap = this.mAccountIndexMap;
        }
        if (this.mIsLoading && isStaleDataReady()) {
            if (ACC_DATA_MNGR_LOG_ENABLED) {
                HwLog.d("AccountDataManager", "getAccountData called from bindView while cache is updating");
            }
            synchronized (this.mLock) {
                StaleAccDataHolder lStaleAccDataHolder = this.mStaleAccDataHolder;
                if (lStaleAccDataHolder != null) {
                    lAccInfoCache = lStaleAccDataHolder.getStaleAccCache();
                    lAccIndexMap = lStaleAccDataHolder.getStaleAccIndexMap();
                }
            }
        }
        synchronized (this.mLock) {
            Long accData = (Long) lAccInfoCache.get(Long.valueOf(aContactId));
        }
        if (accData == null) {
            if (ACC_DATA_MNGR_LOG_ENABLED) {
                HwLog.d("AccountDataManager", " No Account Data found in the Cache for the Contact");
            }
            if (!this.mIsLoading) {
                loadAccountCacheByContactId(aContactId);
            }
            return null;
        } else if (0 == accData.longValue()) {
            if (ACC_DATA_MNGR_LOG_ENABLED) {
                HwLog.d("AccountDataManager", " No Account is assigned with this contact");
            }
            return null;
        } else {
            HashSet<String> accTypeiterator;
            synchronized (this.mLock) {
                accTypeiterator = new HashSet(lAccIndexMap.keySet());
            }
            int lUpdatedIconsCount = 0;
            Iterator<String> indexIterator = accTypeiterator.iterator();
            while (indexIterator != null && indexIterator.hasNext()) {
                String accountType = (String) indexIterator.next();
                if (this.mIsShowIcons || this.mWhiteList.contains(accountType)) {
                    synchronized (this.mLock) {
                        AccountDetailsHolder lAccountDetailsHolder = (AccountDetailsHolder) lAccIndexMap.get(accountType);
                        if (lAccountDetailsHolder != null) {
                            int accountIndex = lAccountDetailsHolder.mAccIndex.intValue();
                        } else {
                            return null;
                        }
                    }
                }
            }
            return lIconInfo;
        }
    }

    private String getAccountTypeDescription(String accountType) {
        String accountTypeDescription = "";
        if (accountType.equals("com.tencent.mm.account")) {
            return this.mContext.getString(R.string.contact_weichat);
        }
        if (accountType.equals("com.whatsapp")) {
            return "whatsapp";
        }
        if (accountType.equals("com.huawei.hwid")) {
            return "hwsns";
        }
        if (accountType.equals("com.skype.contacts.sync")) {
            return "skype";
        }
        if (!accountType.equals("com.tencent.qq.account")) {
            if (!accountType.equals("com.tencent.mobileqq.account")) {
                return accountTypeDescription;
            }
        }
        return this.mContext.getString(R.string.content_description_qq);
    }

    public Bitmap getAccountIcon(String aAccType) {
        if ((!this.mIsShowIcons && !this.mWhiteList.contains(aAccType)) || ContactDisplayUtils.isSimpleDisplayMode()) {
            return null;
        }
        HashMap<String, AccountDetailsHolder> lAccountIndexMap = this.mAccountIndexMap;
        if (this.mIsLoading && isStaleDataReady()) {
            synchronized (this.mLock) {
                StaleAccDataHolder lStaleAccDataHolder = this.mStaleAccDataHolder;
                if (lStaleAccDataHolder != null) {
                    lAccountIndexMap = lStaleAccDataHolder.mStaleAccountIndexMap;
                }
            }
        }
        if (lAccountIndexMap != null) {
            synchronized (this.mLock) {
                AccountDetailsHolder accountDetails = (AccountDetailsHolder) lAccountIndexMap.get(aAccType);
                if (accountDetails != null) {
                    Bitmap bitmap = accountDetails.mAccountIcon;
                    return bitmap;
                }
            }
        }
        return null;
    }

    private boolean isStaleDataReady() {
        boolean z;
        synchronized (this.mLock) {
            z = this.mStaleAccDataHolder != null ? this.mStaleAccDataHolder.isDataStaled : false;
        }
        return z;
    }

    public void preLoadAccountsDataInBackground() {
        this.mIsAccountsUpdating = true;
        refreshCache();
        if (!this.mPaused) {
            this.mIsLoading = true;
            ensureLoaderThread();
            this.mLoaderThread.requestPreloading();
        }
    }

    public void reloadAccCache() {
        if (ACC_DATA_MNGR_LOG_ENABLED) {
            HwLog.d("AccountDataManager", " + Account Cache reloaded for the changes in RawContact Table");
        }
        if (!this.mPaused) {
            this.mIsLoading = true;
            ensureLoaderThread();
            this.mLoaderThread.requestReloadingCache();
        }
    }

    public void loadAccountCacheByContactId(long aContactId) {
        if (!this.mPaused) {
            ensureLoaderThread();
            this.mLoaderThread.requestLoadAccCacheByContactId(aContactId);
        }
    }

    public void ensureLoaderThread() {
        if (ACC_DATA_MNGR_LOG_ENABLED) {
            HwLog.d("AccountDataManager", " + ensureLoaderThread");
        }
        if (this.mLoaderThread == null) {
            this.mLoaderThread = new LoaderThread(this.mContext.getContentResolver());
            this.mLoaderThread.start();
        }
    }

    public void onAccountsUpdated(Account[] accounts) {
        if (ACC_DATA_MNGR_LOG_ENABLED) {
            HwLog.d("AccountDataManager", " + onAccountsUpdated");
        }
        preLoadAccountsDataInBackground();
    }

    private void clearStaleData() {
        synchronized (this.mLock) {
            if (this.mStaleAccDataHolder != null) {
                this.mStaleAccDataHolder.clearStaleData();
                this.mStaleAccDataHolder = null;
            }
        }
    }

    public void setListener(AccountCacheUpdatedListener aListener) {
        this.mListener = aListener;
    }
}
