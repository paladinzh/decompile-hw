package com.huawei.mms.util;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources.NotFoundException;
import android.graphics.drawable.Drawable;
import android.location.Country;
import android.location.CountryDetector;
import android.location.CountryListener;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.Telephony.Sms;
import android.util.SparseArray;
import android.view.ContextThemeWrapper;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import cn.com.xy.sms.sdk.SmartSmsSdkUtil;
import com.android.messaging.util.OsUtil;
import com.android.mms.MmsApp;
import com.android.mms.MmsConfig;
import com.android.mms.data.Contact;
import com.android.mms.transaction.MessagingNotification;
import com.android.mms.transaction.MmsSystemEventReceiver;
import com.android.mms.ui.ComposeMessageActivity;
import com.android.mms.ui.ConversationListFragment;
import com.android.mms.ui.MessageUtils;
import com.android.mms.ui.PreferenceUtils;
import com.android.mms.util.RateController;
import com.android.mms.util.SmileyParser;
import com.android.mms.util.ThumbnailManager;
import com.google.android.gms.R;
import com.google.android.gms.location.places.Place;
import com.huawei.cspcommon.MLog;
import com.huawei.cspcommon.ex.HandlerEx;
import com.huawei.cspcommon.ex.MemCollector;
import com.huawei.cspcommon.ex.ThreadEx;
import com.huawei.cust.HwCustUtils;
import com.huawei.mms.util.HwSpecialUtils.HwDateUtils;
import com.huawei.mms.util.MccMncConfig.OperatorChecker;
import com.huawei.mms.util.MccMncConfig.operatorChangeListener;
import com.huawei.mms.util.NumberUtils.AddrMatcher;
import com.huawei.tmr.util.TMRManagerProxy;
import java.util.Locale;

public class HwBackgroundLoader {
    private static SparseArray<View> mConversationListViews = new SparseArray();
    private static HwCustHwBackgroundLoader mHwCustHwBackgroundLoader = ((HwCustHwBackgroundLoader) HwCustUtils.createObj(HwCustHwBackgroundLoader.class, new Object[0]));
    private static HwBackgroundLoader sInstance = null;
    private CountryDetector mCountryDetector;
    private String mCountryIso;
    private CountryListener mCountryListener;
    private CryptoHwBackgroundLoader mCryptoHwBackgroundLoader = new CryptoHwBackgroundLoader();
    private final Handler mDataHandler;
    private Integer mLoadStatus = Integer.valueOf(0);
    private final Handler mPreUiHandler;
    private PreferenceInitor mPreferenceInitor;
    private BroadcastReceiver mSimStateListener = new SimStateListener();
    private Object mStatusLocker = new Object();
    private final Handler mTaskHandler;
    private long mUIThreadId = 0;
    private final Handler mUiHanlder;
    DefaultSmsApp sDefaultSmsApp;

    public static class DefaultSmsApp {
        Drawable defaultAppIcon = null;
        Intent defaultAppIntent = null;
        String defaultAppName = null;
        CharSequence defautlAppLabel = null;

        public void fresh(final Context context) {
            if (OsUtil.IS_EMUI_LITE) {
                new AsyncTask<Void, Void, String>() {
                    protected String doInBackground(Void... params) {
                        return Sms.getDefaultSmsPackage(context);
                    }

                    protected void onPostExecute(String result) {
                        if (result != null) {
                            DefaultSmsApp.this.defaultAppName = result;
                        }
                    }
                }.executeOnExecutor(ThreadEx.getDefaultExecutor(), new Void[0]);
            } else {
                this.defaultAppName = Sms.getDefaultSmsPackage(context);
            }
            if (!isDefaultApp()) {
                PackageManager packageManager = context.getPackageManager();
                try {
                    ApplicationInfo smsAppInfo = packageManager.getApplicationInfo(this.defaultAppName, 0);
                    this.defautlAppLabel = smsAppInfo.loadLabel(packageManager);
                    this.defaultAppIntent = packageManager.getLaunchIntentForPackage(this.defaultAppName);
                    this.defaultAppIcon = packageManager.getApplicationIcon(smsAppInfo);
                } catch (NameNotFoundException e) {
                    MLog.e("MMS_BkLoader", "DefaultSmsApp fresh fail! " + this.defaultAppName + "; Exception" + e.getMessage());
                }
            }
        }

        public CharSequence getDefaultApp() {
            return this.defaultAppName;
        }

        public boolean isDefaultApp() {
            if (this.defaultAppName == null || !this.defaultAppName.equals("com.android.mms")) {
                return false;
            }
            return true;
        }
    }

    private class PreferenceInitor implements operatorChangeListener {
        BroadcastReceiver mReceiver = null;

        PreferenceInitor() {
        }

        public void doCheck() {
            Context context = HwBackgroundLoader.this.getContext();
            SharedPreferences defaultValueSp = context.getSharedPreferences("_has_set_default_values", 0);
            int initState = defaultValueSp.getInt("mms_initialized_state", 0);
            OperatorChecker checker = MccMncConfig.getDefault();
            if (initState == 0) {
                PreferenceManager.setDefaultValues(context, R.xml.preferences, false);
                setSettingsVibrate();
                HwBackgroundLoader.this.setMmsPlayMode();
            }
            int loadState = checker.isOperatorLoaded() ? checker.isSub1AsDefault() ? 3 : 2 : 1;
            MLog.d("MMS_BkLoader", "do Check Preference with state init " + initState + " loaded " + loadState + " operator " + checker.getOperator());
            if (loadState > initState) {
                setCustomDefaultValues();
                defaultValueSp.edit().putInt("mms_initialized_state", loadState).apply();
            } else {
                syncVibrateValues(HwBackgroundLoader.this.getContext());
            }
            if (loadState <= 2) {
                registeOperatorChange();
            } else {
                unregisteOperatorChange();
            }
        }

        private void syncVibrateValues(Context context) {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
            if (!sp.getBoolean("key_sync_vibrate", false)) {
                Editor editor = sp.edit();
                boolean oldVibrateWhenNotification = sp.getBoolean("pref_key_vibrateWhen", true);
                editor.putBoolean("pref_key_vibrateWhen_sub0", oldVibrateWhenNotification);
                editor.putBoolean("pref_key_vibrateWhen_sub1", oldVibrateWhenNotification);
                editor.putBoolean("key_sync_vibrate", true);
                editor.apply();
            }
        }

        private void setCustomDefaultValues() {
            MLog.d("MMS_BkLoader", "setCustomDefaultValues");
            MmsConfig.setCustomDefaultValues(PreferenceManager.getDefaultSharedPreferences(HwBackgroundLoader.this.getContext()));
        }

        private void setSettingsVibrate() {
            int i = 1;
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(HwBackgroundLoader.this.getContext());
            ContentResolver contentResolver = HwBackgroundLoader.this.getContext().getContentResolver();
            String str = "vibrate_on_message";
            if (!sp.getBoolean("pref_key_vibrateWhen", true)) {
                i = 0;
            }
            MessageUtils.setSettingsVaules(contentResolver, str, i);
        }

        public void onOperatorChange(OperatorChecker checker) {
            doCheck();
        }

        private synchronized void registeOperatorChange() {
            if (this.mReceiver == null) {
                this.mReceiver = MccMncConfig.registerForOperatorChange(HwBackgroundLoader.this.getContext(), this);
            }
        }

        private synchronized void unregisteOperatorChange() {
            MLog.d("MMS_BkLoader", "unregistOperatorChange with receiver " + this.mReceiver);
            if (this.mReceiver != null) {
                HwBackgroundLoader.this.getContext().unregisterReceiver(this.mReceiver);
                this.mReceiver = null;
            }
        }
    }

    private static class SimStateListener extends BroadcastReceiver {
        private SimStateListener() {
        }

        public void onReceive(Context context, Intent intent) {
            if (MmsApp.getDefaultTelephonyManager().getSimState() == 5) {
                MLog.i("MMS_BkLoader", "sim ready, so we read the match num");
                AddrMatcher.decideMatchLengthMin();
                MessageUtils.set7bitsTableVenezuela();
            }
            if (intent != null) {
                String strState = intent.getStringExtra("ss");
                MLog.d("MMS_BkLoader", "strState:" + strState);
                if ("LOADED".equals(strState)) {
                    if (MmsConfig.isEnabletMmsParamsFromGlobal()) {
                        MLog.i("LOG_TAG", "SimStateListener initMccMncParameterSettings");
                        MccMncConfig.initMccMncParameterSettings();
                    }
                    if (!(HwBackgroundLoader.mHwCustHwBackgroundLoader == null || HwBackgroundLoader.mHwCustHwBackgroundLoader.getMmsParams() == null)) {
                        HwBackgroundLoader.mHwCustHwBackgroundLoader.refreshParameterSettings();
                    }
                    if (HwBackgroundLoader.mHwCustHwBackgroundLoader != null) {
                        HwBackgroundLoader.mHwCustHwBackgroundLoader.refreshRecipientLimitSettings(intent);
                        HwBackgroundLoader.mHwCustHwBackgroundLoader.refreshDeliveryReportsSettings(context);
                        HwBackgroundLoader.mHwCustHwBackgroundLoader.refreshReadReportsSettings(context);
                        HwBackgroundLoader.mHwCustHwBackgroundLoader.refreshReplyReadReportsSettings(context);
                    }
                    int subId = MessageUtils.getSimIdFromIntent(intent, -1);
                    MLog.d("MMS_BkLoader", "icc_loaded entry subId:" + subId);
                    MmsRadarInfoManager mmsRadarInfoManager = MmsRadarInfoManager.getInstance();
                    if (subId == 0 || 1 == subId) {
                        mmsRadarInfoManager.initSelfRepairPara(subId);
                    } else {
                        mmsRadarInfoManager.initSelfRepairPara();
                    }
                    HwBackgroundLoader.updateSmscNumberSharedPreferences(context, subId);
                }
            }
        }
    }

    private static class TMRWorker implements Runnable {
        private TMRWorker() {
        }

        public void run() {
            TMRManagerProxy tMRManagerProxy = new TMRManagerProxy();
        }
    }

    public void clearLoadMark(int r1) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.mms.util.HwBackgroundLoader.clearLoadMark(int):void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:116)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:249)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:569)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:102)
	... 7 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.mms.util.HwBackgroundLoader.clearLoadMark(int):void");
    }

    private Context getContext() {
        return MmsApp.getApplication().getApplicationContext();
    }

    HwBackgroundLoader() {
        HandlerThread taskThread = new HandlerThread("Task_handler", 1);
        taskThread.start();
        HandlerThread dataThread = new HandlerThread("Cache-Handler", 10);
        dataThread.start();
        HandlerThread preUIThread = new HandlerThread("PerUI-Handler", 10);
        preUIThread.start();
        this.mPreUiHandler = new HandlerEx(preUIThread.getLooper()) {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        HwBackgroundLoader.cacheConversationListViews(HwBackgroundLoader.this.getContext());
                        return;
                    default:
                        return;
                }
            }
        };
        this.mUiHanlder = new HandlerEx();
        this.mUIThreadId = Thread.currentThread().getId();
        this.mTaskHandler = new HandlerEx(taskThread.getLooper()) {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        HwBackgroundLoader.this.onAppStart();
                        return;
                    case 2:
                        HwBackgroundLoader.this.onCLStart();
                        return;
                    case 3:
                        HwBackgroundLoader.this.onNewMsg();
                        return;
                    case 4:
                        HwBackgroundLoader.this.onContactChanged();
                        return;
                    default:
                        return;
                }
            }

            protected long getMsgMaxRunningTime(Message msg) {
                if (msg.what == 1) {
                    return 100;
                }
                return super.getMsgMaxRunningTime(msg);
            }
        };
        this.mDataHandler = new HandlerEx(dataThread.getLooper()) {
            /* JADX WARNING: inconsistent code. */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            public void handleMessage(Message msg) {
                int type = msg.what;
                if (type == 32) {
                    Contact.freshCacheNumber();
                }
                synchronized (HwBackgroundLoader.this.mStatusLocker) {
                    if ((HwBackgroundLoader.this.mLoadStatus.intValue() & type) != 0) {
                        MLog.i("HwBackgroundLoader", " Data already loaded " + type);
                    }
                }
            }

            protected long getMsgMaxRunningTime(Message msg) {
                switch (msg.what) {
                    case 1:
                        return 50;
                    case 2:
                        return 200;
                    case 8:
                        return 500;
                    case 16:
                        return 100;
                    case Place.TYPE_SUBLOCALITY_LEVEL_2 /*1024*/:
                        return 800;
                    default:
                        return super.getMsgMaxRunningTime(msg);
                }
            }
        };
    }

    public static void init() {
        synchronized (HwBackgroundLoader.class) {
            if (sInstance == null) {
                sInstance = new HwBackgroundLoader();
            }
        }
    }

    public boolean isInUiThread() {
        return Thread.currentThread().getId() == this.mUIThreadId;
    }

    public static final Handler getUIHandler() {
        return getInst().mUiHanlder;
    }

    public static final Handler getBackgroundHandler() {
        return getInst().mTaskHandler;
    }

    public static final HwBackgroundLoader getInst() {
        HwBackgroundLoader hwBackgroundLoader;
        synchronized (HwBackgroundLoader.class) {
            if (sInstance == null) {
                init();
                MLog.e("MMS_BkLoader", "getInst with sInstance not initialized");
            }
            hwBackgroundLoader = sInstance;
        }
        return hwBackgroundLoader;
    }

    public void loadData(int loadType) {
        this.mDataHandler.removeMessages(loadType);
        this.mDataHandler.sendEmptyMessage(loadType);
    }

    public void loadDataDelayed(int loadType, long delay) {
        this.mDataHandler.removeMessages(loadType);
        this.mDataHandler.sendEmptyMessageDelayed(loadType, delay);
    }

    public void reloadDataDelayed(int loadType, long delay) {
        clearLoadMark(loadType);
        loadDataDelayed(loadType, delay);
    }

    public void reloadData(int loadType) {
        clearLoadMark(loadType);
        loadData(loadType);
    }

    public void sendPreUITask(int task) {
        this.mPreUiHandler.removeMessages(task);
        this.mPreUiHandler.sendEmptyMessage(task);
    }

    public void sendTask(int task) {
        this.mTaskHandler.removeMessages(task);
        this.mTaskHandler.sendEmptyMessage(task);
    }

    public void postTask(Runnable r) {
        this.mTaskHandler.post(r);
    }

    public void postTaskDelayed(Runnable r, long delay) {
        this.mTaskHandler.removeCallbacks(r);
        this.mTaskHandler.postDelayed(r, delay);
    }

    private void onAppStart() {
        postTask(new Runnable() {
            public void run() {
                HwBackgroundLoader.this.initAppSettings();
            }
        });
    }

    public static void cacheConversationListViews(Context context) {
        try {
            LayoutInflater inflator = (LayoutInflater) new ContextThemeWrapper(context, context.getResources().getIdentifier("androidhwext:style/Theme.Emui", null, null)).getSystemService("layout_inflater");
            if (inflator != null) {
                clearConvListViewCache();
                try {
                    View conversationListView = inflator.inflate(R.layout.conversation_list_layout, null);
                    View conversationListFragmentView = inflator.inflate(R.layout.conversation_list_fragment, null);
                    synchronized (mConversationListViews) {
                        mConversationListViews.append(0, conversationListView);
                        mConversationListViews.append(1, conversationListFragmentView);
                    }
                } catch (NotFoundException e) {
                    MLog.e("MMS_BkLoader", "layout xml not found", (Throwable) e);
                } catch (InflateException e2) {
                    MLog.e("MMS_BkLoader", "cacheConversationListViews :: InflateException : ", (Throwable) e2);
                }
            }
        } catch (NotFoundException e3) {
            MLog.e("MMS_BkLoader", "theme not found", (Throwable) e3);
        }
    }

    public static View getCachedConversationListViews(int item) {
        View view;
        synchronized (mConversationListViews) {
            view = mConversationListViews.size() > 0 ? (View) mConversationListViews.get(item) : null;
        }
        return view;
    }

    public static void clearConvListViewCache() {
        synchronized (mConversationListViews) {
            mConversationListViews.clear();
        }
    }

    private void onContactChanged() {
    }

    private void onCLStart() {
        this.mTaskHandler.removeMessages(2);
        loadData(16);
        loadDataDelayed(8, 1000);
        loadDataDelayed(4, 2000);
    }

    private void onNewMsg() {
        this.mTaskHandler.removeMessages(2);
        loadData(4);
        loadData(8);
        loadData(16);
    }

    private void initAppSettings() {
        Log.logPerformance("HwBackgroundLoader initAppSettings start");
        Context context = getContext();
        HwTelephony.init(context);
        RateController.init(context);
        if (!OsUtil.isAppStart()) {
            OsUtil.setAppStart(true);
        }
        MemCollector.addCriticalClass(ConversationListFragment.class);
        MemCollector.addCriticalClass(ComposeMessageActivity.class);
        loadData(1);
        initCountryArea();
        MmsApp.getApplication().getPduLoaderManager();
        MmsApp.getApplication().setThumbnailManager(new ThumbnailManager(context, this.mUiHanlder));
        SmileyParser.init(context);
        MmsConfig.init(context);
        MccMncConfig.init();
        MccMncConfig.registerSimReadyChange();
        MmsConfig.setLocal(MmsApp.getApplication().getCurrentCountryIso());
        this.mPreferenceInitor = new PreferenceInitor();
        this.mPreferenceInitor.doCheck();
        HwDateUtils.init();
        if (MmsConfig.getSupportSmartSmsFeature()) {
            SmartSmsSdkUtil.init(context);
        }
        FloatMmsRequsetReceiver.stopPopupMsgAcitvity(context);
        MmsSystemEventReceiver.registerForDraftChanges(context);
        MmsSystemEventReceiver.registerForConnectivityChanges(context);
        MessagingNotification.init(context);
        PrivacyModeReceiver.checkPrivacyState(context);
        getDefaultSmsApp();
        Contact.registerForContactChange(context);
        if (MmsConfig.isSupportCNAddress()) {
            postTaskDelayed(new TMRWorker(), 500);
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.SIM_STATE_CHANGED");
        MmsApp.getApplication().getApplicationContext().registerReceiver(this.mSimStateListener, filter);
        this.mCryptoHwBackgroundLoader.initAppSettings();
        VerifitionSmsManager.getInstance().registerListener();
        PreferenceUtils.setVerifitionSmsProtectEnable(context, PreferenceManager.getDefaultSharedPreferences(context).getBoolean("pref_key_verifition_sms_protect_enable", true));
        registerChangeFollowFlagReceiver(context);
        Log.logPerformance("HwBackgroundLoader initAppSettings end");
    }

    public void registerChangeFollowFlagReceiver(Context context) {
        if (mHwCustHwBackgroundLoader != null) {
            mHwCustHwBackgroundLoader.registerChangeFollowFlagReceiver(context);
        }
    }

    private void initCountryArea() {
        MLog.i("MMS_BkLoader", " initCountryArea start");
        Context context = getContext();
        synchronized (this) {
            this.mCountryDetector = (CountryDetector) context.getSystemService("country_detector");
            this.mCountryListener = new CountryListener() {
                public void onCountryDetected(Country country) {
                    synchronized (HwBackgroundLoader.this) {
                        if (!(HwBackgroundLoader.this.mCountryIso == null || HwBackgroundLoader.this.mCountryIso.equals(country.getCountryIso()))) {
                            HwBackgroundLoader.getInst().loadDataDelayed(32, 1000);
                        }
                        HwBackgroundLoader.this.mCountryIso = country.getCountryIso();
                    }
                }
            };
            if (this.mCountryDetector != null) {
                this.mCountryDetector.addCountryListener(this.mCountryListener, context.getMainLooper());
                MLog.i("MMS_BkLoader", "mCountryDetector.detectCountry()");
                Country country = this.mCountryDetector.detectCountry();
                synchronized (this) {
                    if (country != null) {
                        this.mCountryIso = country.getCountryIso();
                    }
                }
            }
            synchronized (this) {
                if (this.mCountryIso == null) {
                    this.mCountryIso = Locale.getDefault().getCountry();
                }
            }
            MLog.i("MMS_BkLoader", " initCountryArea end");
        }
        MLog.i("MMS_BkLoader", " initCountryArea end");
    }

    private DefaultSmsApp getDefaultSmsAppInner() {
        if (this.sDefaultSmsApp == null) {
            this.sDefaultSmsApp = new DefaultSmsApp();
            this.sDefaultSmsApp.fresh(getContext());
        }
        return this.sDefaultSmsApp;
    }

    public static DefaultSmsApp getDefaultSmsApp() {
        return getInst().getDefaultSmsAppInner();
    }

    public String getCurrentCountryIso() {
        synchronized (this) {
            String country;
            if (this.mCountryIso == null) {
                Country country2;
                if (this.mCountryDetector == null) {
                    country2 = null;
                } else {
                    country2 = this.mCountryDetector.detectCountry();
                }
                if (country2 == null) {
                    country = Locale.getDefault().getCountry();
                    return country;
                }
                this.mCountryIso = country2.getCountryIso();
            }
            country = this.mCountryIso;
            return country;
        }
    }

    public void onTerminate() {
        unRegisterChangeFollowFlagReceiver(getContext());
        synchronized (this) {
            if (this.mCountryDetector != null) {
                this.mCountryDetector.removeCountryListener(this.mCountryListener);
            }
            MmsApp.getApplication().getApplicationContext().unregisterReceiver(this.mSimStateListener);
            MccMncConfig.unRegisterSimReadyChange();
        }
        this.mCryptoHwBackgroundLoader.onTerminate();
        VerifitionSmsManager.getInstance().unregisterListener();
    }

    public void unRegisterChangeFollowFlagReceiver(Context context) {
        if (mHwCustHwBackgroundLoader != null) {
            mHwCustHwBackgroundLoader.unRegisterChangeFollowFlagReceiver(context);
        }
    }

    private void setMmsPlayMode() {
        SharedPreferences sharePref = PreferenceManager.getDefaultSharedPreferences(getContext());
        if ("notfound".equals(sharePref.getString("pref_key_play_mode", "notfound"))) {
            Editor editor = sharePref.edit();
            editor.putString("pref_key_play_mode", MmsConfig.getPrefPlaymode());
            editor.apply();
        }
    }

    private static void updateSmscNumberSharedPreferences(Context context, int subId) {
        new AsyncTask<Object, Void, String>() {
            protected String doInBackground(Object... params) {
                HwSIMCardChangedHelper.checkSimWasReplacedForSmscNumber((Context) params[0], ((Integer) params[1]).intValue());
                return null;
            }
        }.executeOnExecutor(ThreadEx.getDefaultExecutor(), new Object[]{context, Integer.valueOf(subId)});
    }
}
