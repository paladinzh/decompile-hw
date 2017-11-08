package com.android.contacts;

import android.app.Activity;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.content.res.ConfigurationEx;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.os.StrictMode;
import android.os.StrictMode.ThreadPolicy.Builder;
import android.os.SystemProperties;
import android.os.UserManager;
import android.provider.ContactsContract.Contacts;
import android.provider.Settings.System;
import android.util.Log;
import com.android.contacts.activities.ContactDetailLayoutCache;
import com.android.contacts.calllog.CallTypeIconsView;
import com.android.contacts.compatibility.CountryMonitor;
import com.android.contacts.compatibility.QueryUtil;
import com.android.contacts.group.GroupEditorFragment.Member;
import com.android.contacts.hap.AccountsDataManager;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.hap.EmuiFeatureManager;
import com.android.contacts.hap.birthday.Utils;
import com.android.contacts.hap.camcard.bcr.CCSaveService;
import com.android.contacts.hap.numbermark.YellowPageDataManager;
import com.android.contacts.hap.optimize.BackgroundCacheHdlr;
import com.android.contacts.hap.rcs.RcsCLIRBroadCastHelper;
import com.android.contacts.hap.rcs.RcsContactsUtils;
import com.android.contacts.hap.rcs.RcseProfile;
import com.android.contacts.hap.rcs.service.RcsService;
import com.android.contacts.hap.sim.SimFactoryManager;
import com.android.contacts.hap.util.BackgroundViewCacher;
import com.android.contacts.hap.util.HapEncryptCallUtils;
import com.android.contacts.hap.util.ReflelctionConstant;
import com.android.contacts.hap.util.SeparatedFeatureDelegate;
import com.android.contacts.hap.utils.BackgroundGenricHandler;
import com.android.contacts.hap.utils.SimContactsCache;
import com.android.contacts.hap.utils.VtLteUtils;
import com.android.contacts.list.ContactListFilterController;
import com.android.contacts.model.AccountTypeManager;
import com.android.contacts.model.Contact;
import com.android.contacts.statistical.StatisticalHelper;
import com.android.contacts.test.InjectedServices;
import com.android.contacts.util.Constants;
import com.android.contacts.util.ContactsThreadPool;
import com.android.contacts.util.DateUtils;
import com.android.contacts.util.EncryptCallUtils;
import com.android.contacts.util.HwLog;
import com.android.contacts.util.PhoneCapabilityTester;
import com.android.contacts.util.SharePreferenceUtil;
import com.google.android.gms.R;
import com.google.common.annotations.VisibleForTesting;
import com.huawei.cspcommon.performance.PLog;
import com.huawei.cspcommon.util.DialerHighlighter;
import com.huawei.cust.HwCustUtils;
import com.huawei.numberlocation.NLContentProvider;
import com.huawei.rcs.CarrierConfigChangeBroadcastReceiver;
import com.huawei.rcs.CarrierConfigChangeBroadcastReceiver.ChangeListener;
import com.huawei.rcs.util.RcsXmlParser;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public final class ContactsApplication extends Application {
    private static final FilenameFilter mFileFilter = new FilenameFilter() {
        public boolean accept(File dir, String name) {
            return name.matches("share_contacts*");
        }
    };
    private static boolean mIsAdminToGuestMode = false;
    private static Context sAppContext;
    private static InjectedServices sInjectedServices;
    private static int sPrivateMode = -1;
    private static final Runnable sRunnable = new Runnable() {
        public void run() {
            try {
                Class.forName("com.android.contacts.activities.PeopleActivity");
                Class.forName("com.android.contacts.ContactSaveService");
                Class.forName("com.android.contacts.hap.widget.HapViewPager");
                Class.forName("com.android.contacts.hap.widget.AlphaIndexerPinnedHeaderListView");
            } catch (ClassNotFoundException e) {
                HwLog.e("ClassNotFoundException", "error");
            }
        }
    };
    public boolean bFirstOpen = true;
    private boolean isImsSwitchObserverRegisted = false;
    private AccountsDataManager mAccountsDataManager;
    private Contact mContact;
    private ContactListFilterController mContactListFilterController;
    private ContactPhotoManager mContactPhotoManager;
    public Cursor mContactsDataCursor;
    Context mContext;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 5000:
                    if ((EmuiFeatureManager.isChinaArea() || (ContactsApplication.this.mHwCustContactApplicationHelper != null && ContactsApplication.this.mHwCustContactApplicationHelper.installYellowPages())) && ContactsApplication.this.mContext != null) {
                        new YellowPageDataManager(ContactsApplication.this.mContext).prepareYellowPageDataAsync(false);
                    }
                    ContactsThreadPool.getInstance().execute(new Runnable() {
                        public void run() {
                            if (EmuiFeatureManager.isCamcardEnabled() && ContactsApplication.this.mContext != null) {
                                Intent intent = CCSaveService.createMultiRecognizeIntent(ContactsApplication.this.getApplicationContext());
                                intent.setAction("initCardService");
                                intent.putExtra("key_ver_3", Long.valueOf(ContactsApplication.this.mContext.getString(R.string.pre_ver_cc)));
                                ContactsApplication.this.startService(intent);
                            }
                            if (EmuiFeatureManager.isChinaArea() && ContactsApplication.this.mContext != null) {
                                NLContentProvider.initFile(ContactsApplication.this.mContext);
                            }
                        }
                    });
                    return;
                case 5001:
                    if ((ContactsApplication.this.mHwCustContactApplicationHelper == null || ContactsApplication.this.mHwCustContactApplicationHelper.preloadContactFeatureEnabled()) && ContactsApplication.this.mContext != null) {
                        CommonUtilMethods.loadPredifeContactsFromCustOtaUpdate(ContactsApplication.this.mContext);
                        return;
                    }
                    return;
                case 5002:
                    DialerHighlighter.dialerHighlighterInit();
                    return;
                case 10001:
                    ContactsApplication.this.registNetworkSwitchBroadcast();
                    return;
                case 10002:
                    ContactsApplication.this.mContactPhotoManager.preloadPhotosInBackground();
                    return;
                default:
                    super.handleMessage(msg);
                    return;
            }
        }
    };
    HwCustContactApplicationHelper mHwCustContactApplicationHelper = null;
    ContentObserver mImsSwitchObserver = new ContentObserver(this.mHandler) {
        public void onChange(boolean selfChange) {
            boolean switchOn = true;
            if (System.getInt(ContactsApplication.this.getContentResolver(), "hw_volte_user_switch", 0) != 1) {
                switchOn = false;
            }
            VtLteUtils.setImsSwitchOn(switchOn);
        }
    };
    private boolean mIsContactsLaunching;
    private String mLastActivityId;
    private String mLastLanguage;
    private int mLastScreenMode;
    private int mLastThemeId;
    private int mLaunchState = 0;
    private final ArrayList<NetWorkSwitchListener> mListeners = new ArrayList();
    private Object mLock = new Object();
    private RcsCLIRBroadCastHelper mRcsCLIRBroadCastHelper = null;
    private RcsChangeListener mRcsConfigChangeListener = new RcsChangeListener();
    private RcsService mRcsService;
    private long mReqId;
    private ArrayList<Member> mSelectedMembersList = new ArrayList();
    private BroadcastReceiver mSpnBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            HwLog.i("ContactsAppilcation", "intent:" + intent.getAction());
            if (CommonUtilMethods.getSimNameFromBroadcast(context, intent)) {
                for (NetWorkSwitchListener listener : ContactsApplication.this.mListeners) {
                    if (listener != null) {
                        listener.notifyUpdate();
                    }
                }
            }
        }
    };
    private int mWaitCouter;

    private final class BackgroundRunnable implements Runnable {
        private ContactsApplication mApplication;

        private BackgroundRunnable(ContactsApplication application) {
            this.mApplication = null;
            this.mApplication = application;
        }

        public void run() {
            EmuiFeatureManager.isShowCamCard(this.mApplication);
            PLog.open(this.mApplication);
            PLog.d(1, "Application onCreate begin");
            EmuiFeatureManager.loadEmailAnrSupportFlag(this.mApplication);
            EmuiFeatureManager.initSystemVoiceCapableFlag(this.mApplication);
            EmuiFeatureManager.initSystemSMSCapableFlag(this.mApplication);
            EmuiFeatureManager.loadRussiaNumberRelevanceFeatureFlag(this.mApplication);
            EmuiFeatureManager.initRussiaNumberSearchEnabled(this.mApplication);
            EmuiFeatureManager.initNeedSplitScreenFlag(this.mApplication);
            Context context = this.mApplication.getApplicationContext();
            SharedPreferences sharPre = SharePreferenceUtil.getDefaultSp_de(context);
            if (sharPre.getBoolean("contact_boot_key", false) && BackgroundCacheHdlr.haveNotBeenInflate()) {
                BackgroundCacheHdlr.inflateLayoutsInBackground(context);
            }
            if (!EmuiFeatureManager.isEnableContactsWithNumberOnlyFeature()) {
                Editor editor = sharPre.edit();
                editor.putBoolean("preference_contacts_only_phonenumber", false);
                editor.commit();
            }
            AccountsDataManager.getInstance(this.mApplication.getApplicationContext());
            this.mApplication.initImsSwitchAndReisterObserver(this.mApplication.getApplicationContext());
            DialerHighlighter.loadSelf();
            EmuiFeatureManager.initIsSimpleDisplayMode(context);
            if (SystemProperties.getBoolean("ro.config.hw_opt_pre_contact", false)) {
                CommonUtilMethods.predefineCust(this.mApplication.getApplicationContext(), false);
            }
            if (CommonUtilMethods.calcIfNeedSplitScreen()) {
                BackgroundViewCacher.getInstance(this.mApplication.getApplicationContext()).startInflatring();
            }
            if (CommonUtilMethods.getIsLiteFeatureProducts()) {
                this.mApplication.mHandler.sendEmptyMessageDelayed(5002, 1200);
            }
            BackgroundGenricHandler.getInstance().postDelayed(new CacheCleanupRunnable(this.mApplication.getApplicationContext().getCacheDir()), 3000);
            if (EmuiFeatureManager.isProductCustFeatureEnable()) {
                EncryptCallUtils.getCust();
            }
            HapEncryptCallUtils.init(context);
            UserManager userManager = (UserManager) ContactsApplication.getContext().getSystemService("user");
            if (userManager != null && userManager.isUserUnlocked()) {
                this.mApplication.mHandler.sendEmptyMessageDelayed(5000, 2000);
                SeparatedFeatureDelegate.initAsync(this.mApplication.mContext);
                QueryUtil.checkProviderHwSearchFeature(context);
            }
            this.mApplication.initSimAsync();
            BackgroundGenricHandler.getInstance().postDelayed(new DelayedInitializer(), 3000);
            this.mApplication.mHandler.sendEmptyMessageDelayed(5001, 2000);
            this.mApplication.mHandler.sendEmptyMessageDelayed(10001, 2000);
        }
    }

    static class CacheCleanupRunnable implements Runnable {
        private File mRootFolder;

        public CacheCleanupRunnable(File aFile) {
            this.mRootFolder = aFile;
        }

        public void run() {
            if (this.mRootFolder.exists()) {
                File[] files = this.mRootFolder.listFiles(ContactsApplication.mFileFilter);
                if (files != null && files.length > 0) {
                    for (File file : files) {
                        if (!file.delete()) {
                            Log.i("ContactsAppilcation", "Some cache file not deleted successfully.");
                        }
                    }
                }
            }
        }
    }

    private class DelayedInitializer implements Runnable {
        private DelayedInitializer() {
        }

        public void run() {
            Context context = ContactsApplication.this;
            PhoneCapabilityTester.isGeoCodeFeatureEnabled(context);
            Utils.createBirthdayAccount(context);
            if (context.checkCallingOrSelfPermission("android.permission.GET_ACCOUNTS") == 0) {
                AccountTypeManager.getInstance(context);
            }
            ContactsApplication.this.getContentResolver().getType(ContentUris.withAppendedId(Contacts.CONTENT_URI, 1));
            if (DateUtils.isinitialized()) {
                HwLog.d("ContactsAppilcation", "DateUtil is initialized");
            }
            ReflelctionConstant.cacheData();
            ContactsApplication.setNotConvertPhoneNumber();
        }
    }

    public interface NetWorkSwitchListener {
        void notifyUpdate();
    }

    public static class RcsChangeListener implements ChangeListener {
        public void onChange(boolean enableChanged) {
            HwLog.i("ContactsAppilcation", "CarrierConfigChangeBroadcastReceiver enableChanged:" + enableChanged);
            if (enableChanged) {
                HwLog.i("ContactsAppilcation", "================rcs config change, kill contacts application============");
                Process.killProcess(Process.myPid());
            }
        }
    }

    private void initHwCustContactsApplication() {
        this.mHwCustContactApplicationHelper = (HwCustContactApplicationHelper) HwCustUtils.createObj(HwCustContactApplicationHelper.class, new Object[0]);
        if (this.mHwCustContactApplicationHelper != null) {
            this.mHwCustContactApplicationHelper.handleCustomizationsOnCreate(this);
        }
    }

    private void initRceService() {
        this.mRcsService = new RcsService();
        this.mRcsService.initCapabilityService(this.mContext);
        RcseProfile.init(this.mContext);
        if (RcsContactsUtils.isSupportCLIR()) {
            this.mRcsCLIRBroadCastHelper = RcsCLIRBroadCastHelper.getInstance(this.mContext);
            this.mRcsCLIRBroadCastHelper.registerRcsCLIRReceiver(this.mContext);
            this.mRcsCLIRBroadCastHelper.getRcsCLIRStatus();
        }
    }

    public static void addNetWorkSwitchListener(Activity activity, NetWorkSwitchListener listener) {
        if (activity != null) {
            Application lApp = activity.getApplication();
            if ((lApp instanceof ContactsApplication) && listener != null) {
                ((ContactsApplication) lApp).mListeners.add(listener);
            }
        }
    }

    private void cleanNetWorkSwitchListeners() {
        this.mListeners.clear();
    }

    public static void removeSwitchNetWorkListener(Activity activity, NetWorkSwitchListener listener) {
        if (activity != null) {
            Application lApp = activity.getApplication();
            if ((lApp instanceof ContactsApplication) && listener != null) {
                ((ContactsApplication) lApp).mListeners.remove(listener);
            }
        }
    }

    public static boolean isPrivateModeOn() {
        if (-1 == sPrivateMode) {
            int i;
            if (CommonUtilMethods.isPrivacyModeEnabled(sAppContext)) {
                i = 1;
            } else {
                i = 0;
            }
            sPrivateMode = i;
        }
        if (1 == sPrivateMode) {
            return true;
        }
        return false;
    }

    public static boolean isAdminToGuestMode() {
        if (!mIsAdminToGuestMode) {
            return false;
        }
        mIsAdminToGuestMode = false;
        return true;
    }

    @VisibleForTesting
    public static void injectServices(InjectedServices services) {
        sInjectedServices = services;
    }

    public static InjectedServices getInjectedServices() {
        return sInjectedServices;
    }

    public ContentResolver getContentResolver() {
        if (sInjectedServices != null) {
            ContentResolver resolver = sInjectedServices.getContentResolver();
            if (resolver != null) {
                return resolver;
            }
        }
        return super.getContentResolver();
    }

    public SharedPreferences getSharedPreferences(String name, int mode) {
        if (sInjectedServices != null) {
            SharedPreferences prefs = sInjectedServices.getSharedPreferences();
            if (prefs != null) {
                return prefs;
            }
        }
        return super.getSharedPreferences(name, mode);
    }

    public Object getSystemService(String name) {
        if (sInjectedServices != null) {
            Object service = sInjectedServices.getSystemService(name);
            if (service != null) {
                return service;
            }
        }
        if ("contactPhotos".equals(name)) {
            ContactPhotoManager contactPhotoManager;
            synchronized (this) {
                if (this.mContactPhotoManager == null) {
                    this.mContactPhotoManager = ContactPhotoManager.createContactPhotoManager(this);
                    registerComponentCallbacks(this.mContactPhotoManager);
                    this.mHandler.sendEmptyMessageDelayed(10002, 2000);
                }
                contactPhotoManager = this.mContactPhotoManager;
            }
            return contactPhotoManager;
        } else if ("contactListFilter".equals(name)) {
            if (this.mContactListFilterController == null) {
                this.mContactListFilterController = ContactListFilterController.createContactListFilterController(this);
            }
            return this.mContactListFilterController;
        } else if (!"accountsData".equals(name)) {
            return super.getSystemService(name);
        } else {
            AccountsDataManager accountsDataManager;
            synchronized (this) {
                if (this.mAccountsDataManager == null) {
                    this.mAccountsDataManager = AccountsDataManager.createAccountDataManager(this);
                    this.mAccountsDataManager.preLoadAccountsDataInBackground();
                }
                accountsDataManager = this.mAccountsDataManager;
            }
            return accountsDataManager;
        }
    }

    private synchronized void setAppContext(Context aAppContext) {
        if (aAppContext == null) {
            HwLog.e("ContactsAppilcation", "Contacts AppContext is NULL");
        }
        sAppContext = aAppContext;
    }

    public static Context getContext() {
        return sAppContext;
    }

    public void onCreate() {
        this.mContext = getApplicationContext();
        setAppContext(this.mContext);
        RcsXmlParser.initParser(this.mContext);
        setRcsConfig();
        SimFactoryManager.initDualSim(this.mContext);
        new Thread(new BackgroundRunnable(this), "ApponCreate").start();
        BackgroundGenricHandler.getInstance().post(sRunnable);
        super.onCreate();
        StatisticalHelper.getInstance().init(getApplicationContext());
        if (SystemProperties.getBoolean("contact.config.prod_cust.enable", true)) {
            initHwCustContactsApplication();
        }
        Constants.updateFontSizeSettings(this);
        QueryUtil.init(this.mContext);
        if (Log.isLoggable("ContactsStrictMode", 3)) {
            StrictMode.setThreadPolicy(new Builder().detectAll().penaltyLog().build());
        }
        ContactPhotoManager.initOnlyResource(getResources());
        if (EmuiFeatureManager.isRcsFeatureEnable()) {
            initRceService();
        }
        this.mLastThemeId = getCurrentThemeId();
        PLog.d(0, "Application onCreate end");
    }

    public void reInitSync() {
        this.mHandler.sendEmptyMessageDelayed(5000, 2000);
        SeparatedFeatureDelegate.initAsync(this.mContext);
    }

    private void initSimAsync() {
        if (this.mContext != null && this.mContext.checkCallingOrSelfPermission("android.permission.READ_PHONE_STATE") == 0) {
            SimFactoryManager.initSimFactoryManager();
        }
    }

    private void registNetworkSwitchBroadcast() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.ACTION_DSDS_SUB1_OPERATOR_CHANGED");
        filter.addAction("android.intent.action.ACTION_DSDS_SUB2_OPERATOR_CHANGED");
        filter.addAction("android.provider.Telephony.SPN_STRINGS_UPDATED");
        this.mContext.registerReceiver(this.mSpnBroadcastReceiver, filter);
    }

    protected int getCurrentThemeId() {
        int i = 0;
        ConfigurationEx extraConfig = null;
        if (this.mContext == null) {
            return 0;
        }
        try {
            extraConfig = new com.huawei.android.content.res.ConfigurationEx(this.mContext.getResources().getConfiguration()).getExtraConfig();
        } catch (Exception e) {
            HwLog.e("ContactsAppilcation", "get configuration error, error msg: " + e.getMessage());
            e.printStackTrace();
        }
        if (extraConfig != null) {
            i = extraConfig.hwtheme;
        }
        return i;
    }

    public void onConfigurationChanged(Configuration newConfig) {
        String lastLanguage = newConfig.locale.getLanguage();
        int currentThemeId = getCurrentThemeId();
        if (!(this.mLastThemeId == currentThemeId && lastLanguage.equals(this.mLastLanguage))) {
            BackgroundCacheHdlr.clearCallLogBackgroundCache();
            BackgroundCacheHdlr.clearAllListViewCache();
            ContactPhotoManager.refreshDefaultImageCache(this.mContext);
            SimContactsCache.clearSimSmallBitmapCache();
            ContactDetailLayoutCache.clearDetailViewCache();
            BackgroundViewCacher.getInstance(this).clearViewCache();
            CallTypeIconsView.resetCallTypeResources(this.mContext);
            this.mLastThemeId = currentThemeId;
            if (!lastLanguage.equals(this.mLastLanguage)) {
                CountryMonitor.getInstance(getApplicationContext()).loadCountryIso();
            }
            this.mLastLanguage = lastLanguage;
        }
        Constants.updateFontSizeSettings(this);
        super.onConfigurationChanged(newConfig);
    }

    public void setSelectedMemberList(ArrayList<Member> aSelectedMembersList) {
        this.mSelectedMembersList.clear();
        this.mSelectedMembersList.addAll(aSelectedMembersList);
    }

    public void clearSelectedMemberList() {
        this.mSelectedMembersList.clear();
    }

    public ArrayList<Member> getSelectedMemberList() {
        return this.mSelectedMembersList;
    }

    public void onTerminate() {
        super.onTerminate();
        try {
            this.mContext.unregisterReceiver(this.mSpnBroadcastReceiver);
        } catch (Exception e) {
        }
        cleanNetWorkSwitchListeners();
        BackgroundGenricHandler.destroy();
        if (!QueryUtil.isHAPProviderInstalled()) {
            SimContactsCache.getInstance(this).stop();
        }
        if (this.mHwCustContactApplicationHelper != null) {
            this.mHwCustContactApplicationHelper.handleCustomizationsOnTerminate(this.mContext);
        }
        if (EmuiFeatureManager.isRcsFeatureEnable() && this.mRcsService != null) {
            this.mRcsService.endCapabilityService();
            RcseProfile.deInit();
            if (this.mRcsCLIRBroadCastHelper != null) {
                this.mRcsCLIRBroadCastHelper.unRegisterRcsCLIRReceiver(this.mContext);
            }
        }
        unResiterImsSwitchObserver(this.mContext);
    }

    public void setContactResultForDetail(Contact aContact, long aReqId) {
        this.mContact = aContact;
        this.mReqId = aReqId;
    }

    public Contact getContactAndReset(long aReqId) {
        if (aReqId == this.mReqId) {
            this.mReqId = 0;
            return this.mContact;
        }
        this.mContact = null;
        this.mReqId = 0;
        return null;
    }

    public void resetContact() {
        this.mContact = null;
        this.mReqId = 0;
    }

    public Contact getContact(long aReqId) {
        if (aReqId != this.mReqId) {
            return null;
        }
        this.mReqId = 0;
        return this.mContact;
    }

    public boolean isRequestIDSame(long aReqID) {
        return this.mReqId == aReqID;
    }

    private static void setNotConvertPhoneNumber() {
        try {
            Class<?> vcardConfig = Class.forName("com.android.vcard.VCardConfig");
            vcardConfig.getDeclaredMethod("setNotConvertPhoneNumber", null).invoke(vcardConfig, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setIsLaunching(boolean isLaunching) {
        this.mIsContactsLaunching = isLaunching;
    }

    public boolean getLaunchingStat() {
        return this.mIsContactsLaunching;
    }

    public void markLaunch() {
        this.mIsContactsLaunching = true;
        this.mLaunchState = 0;
    }

    public void setLaunchProgress(int val) {
        this.mLaunchState |= val;
        if (CommonUtilMethods.isSimplifiedModeEnabled() || this.mLaunchState < 15) {
            if (!CommonUtilMethods.isSimplifiedModeEnabled() || this.mLaunchState < 12) {
                return;
            }
        }
        this.mIsContactsLaunching = false;
        synchronized (this.mLock) {
            this.mLock.notifyAll();
        }
    }

    public void waitForLaunch() {
        if (HwLog.HWDBG) {
            HwLog.d("ContactsAppilcation", " waitForLaunch ");
        }
        this.mWaitCouter = 0;
        while (this.mIsContactsLaunching) {
            if ((this.mLaunchState < 15 && !CommonUtilMethods.isSimplifiedModeEnabled()) || (this.mLaunchState < 12 && CommonUtilMethods.isSimplifiedModeEnabled())) {
                if (HwLog.HWDBG) {
                    HwLog.d("ContactsAppilcation", " waiting for 1000ms ..mIsContactsLaunching :" + this.mIsContactsLaunching + ", mLaunchState:" + this.mLaunchState + ", counter:" + this.mWaitCouter);
                }
                synchronized (this.mLock) {
                    this.mLock.wait(1000);
                    try {
                    } catch (InterruptedException e) {
                        return;
                    }
                }
                this.mWaitCouter++;
                if (this.mWaitCouter > 15) {
                    return;
                }
            }
            return;
        }
    }

    public String getLastActivityId() {
        return this.mLastActivityId;
    }

    public void setLastActivityId(String lastActivityId) {
        this.mLastActivityId = lastActivityId;
    }

    public int getLastScreenMode() {
        return this.mLastScreenMode;
    }

    public void setLastScreenMode(int lastScreenMode) {
        this.mLastScreenMode = lastScreenMode;
    }

    private void initImsSwitchAndReisterObserver(Context context) {
        boolean z = false;
        if (System.getInt(context.getContentResolver(), "hw_volte_user_switch", 0) == 1) {
            z = true;
        }
        VtLteUtils.setImsSwitchOn(z);
        context.getContentResolver().registerContentObserver(System.getUriFor("hw_volte_user_switch"), true, this.mImsSwitchObserver);
        this.isImsSwitchObserverRegisted = true;
    }

    private void unResiterImsSwitchObserver(Context context) {
        if (this.isImsSwitchObserverRegisted) {
            context.getContentResolver().unregisterContentObserver(this.mImsSwitchObserver);
        }
    }

    public boolean getIsFirstStartContacts() {
        return this.bFirstOpen;
    }

    public void setFirstStartContactsStatus(boolean bflag) {
        this.bFirstOpen = bflag;
    }

    public Cursor getPreLoadContactsCursor() {
        Cursor tempCursor = this.mContactsDataCursor;
        this.mContactsDataCursor = null;
        return tempCursor;
    }

    public void setPreLoadContactsCursor(Cursor mCursor) {
        this.mContactsDataCursor = mCursor;
    }

    private void setRcsConfig() {
        Set<String> configs = new HashSet();
        configs.add("ro.config.hw_rcs_product");
        configs.add("ro.config.hw_rcs_vendor");
        configs.add("hw_rcs_contact_icon_on");
        configs.add("huawei_rcs_enabler");
        configs.add("hw_rcs_version");
        CarrierConfigChangeBroadcastReceiver.setOwnConfigs(configs, this.mRcsConfigChangeListener);
    }
}
