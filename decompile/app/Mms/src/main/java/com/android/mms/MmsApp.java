package com.android.mms;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.drm.DrmManagerClient;
import android.net.Uri;
import android.os.Handler;
import android.os.Process;
import android.provider.SearchRecentSuggestions;
import android.provider.Settings.System;
import android.telephony.MSimTelephonyManager;
import android.telephony.TelephonyManager;
import cn.com.xy.sms.sdk.SmartSmsPublicinfoUtil;
import cn.com.xy.sms.sdk.SmartSmsSdkUtil;
import com.android.messaging.util.OsUtil;
import com.android.mms.attachment.FactoryImpl;
import com.android.mms.data.Contact;
import com.android.mms.data.RecipientIdCache;
import com.android.mms.layout.LayoutManager;
import com.android.mms.provider.NewMessageContentObserver;
import com.android.mms.ui.PreferenceUtils;
import com.android.mms.util.DownloadManager;
import com.android.mms.util.DraftCache;
import com.android.mms.util.PduLoaderManager;
import com.android.mms.util.ThumbnailManager;
import com.android.rcs.RcsCommonConfig;
import com.android.rcs.RcsMMSApp;
import com.huawei.cspcommon.BaseApp;
import com.huawei.cspcommon.MLog;
import com.huawei.cspcommon.ex.ExceptionMonitor;
import com.huawei.cust.HwCustUtils;
import com.huawei.mms.util.HwBackgroundLoader;
import com.huawei.mms.util.HwDualCardNameHelper;
import com.huawei.mms.util.HwMessageUtils;
import com.huawei.mms.util.HwSimpleImageLoader;
import com.huawei.mms.util.LoadCarrierConfigUtil;
import com.huawei.mms.util.MmsExceptionHandler;
import com.huawei.mms.util.PrivacyModeReceiver.PrivacyStateListener;
import com.huawei.mms.util.ResEx;
import com.huawei.mms.util.SortCursor;
import com.huawei.mms.util.StatisticalHelper;
import com.huawei.rcs.CarrierConfigChangeBroadcastReceiver;
import com.huawei.rcs.CarrierConfigChangeBroadcastReceiver.ChangeListener;
import com.huawei.rcs.util.RcsFeatureEnabler;
import com.huawei.rcs.util.RcsXmlParser;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MmsApp extends BaseApp {
    private static MmsApp sMmsApp = null;
    private HwCustMMSApp mCustMmsApp;
    private DrmManagerClient mDrmManagerClient;
    protected float mFontScale;
    private HwSimpleImageLoader mHwSimpleImageLoader;
    private BroadcastReceiver mIntentReceiver = null;
    private boolean mIsContactCacheProcess = false;
    private MSimTelephonyManager mMSimTelephonyManager = null;
    private PduLoaderManager mPduLoaderManager;
    ChangeListener mRcsChangeListener = new ChangeListener() {
        public void onChange(boolean enableChanged) {
            if (enableChanged) {
                System.exit(0);
            }
        }
    };
    private RcsMMSApp mRcsMMSApp;
    private TelephonyManager mTelephonyManager = null;
    private ThumbnailManager mThumbnailManager;
    private NewMessageContentObserver newMessageContentObserver;

    private static class MmsBroadcastReceiver extends BroadcastReceiver {
        private MmsBroadcastReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.CONFIGURATION_CHANGED".equals(intent.getAction())) {
                MLog.i("MSG_APP_Mms", "Broadcast ACTION_CONFIGURATION_CHANGED received.");
                ResEx.self().clearCachedRes();
                ResEx.self().initResColor();
            }
        }
    }

    static {
        HwBackgroundLoader.init();
    }

    public void onCreate() {
        MLog.d("CspApp", "MmsApp onCreate");
        super.onCreate();
        if (RcsFeatureEnabler.getInstance().isRcsPropertiesConfigOn()) {
            RcsXmlParser.initParser(getApplicationContext());
            setRcsConfig();
        }
        FactoryImpl.register(getApplicationContext(), this);
        ExceptionMonitor.setExtendExceptionChecker(new MmsExceptionHandler());
        setApplication(this);
        SmartSmsSdkUtil.initContext(this);
        Context context = getApplicationContext();
        HwMessageUtils.calculateDeviceSize(context);
        this.mIsContactCacheProcess = isContactCacheProcess();
        if (!this.mIsContactCacheProcess) {
            Contact.init(context);
            RecipientIdCache.init(context);
            DraftCache.init(context);
            ResEx.init(context);
            LayoutManager.init(context);
            DownloadManager.init(context);
            HwBackgroundLoader.getInst().sendPreUITask(1);
            HwBackgroundLoader.getInst().sendTask(1);
            registerMmsBroadcastReceiver();
            HwDualCardNameHelper.init(context);
            PrivacyStateListener.self().registerSecureDatabasesChanged(context);
            this.mCustMmsApp = (HwCustMMSApp) HwCustUtils.createObj(HwCustMMSApp.class, new Object[]{getApplicationContext()});
            if (RcsCommonConfig.isRCSSwitchOn() && this.mRcsMMSApp == null) {
                this.mRcsMMSApp = new RcsMMSApp(getApplicationContext());
            }
            if (this.mCustMmsApp != null) {
                this.mCustMmsApp.registerCustDbObserver();
            }
            if (this.mRcsMMSApp != null) {
                this.mRcsMMSApp.init();
            }
            try {
                if ("true".equals(System.getString(context.getContentResolver(), "custom_number_delete_bracket"))) {
                    System.setProperty("custom_number_formatter", "true");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            MLog.d("CspApp", "User " + getApplicationInfo().uid + "; isOwner " + OsUtil.isOwner() + "; isSmsAllowed " + OsUtil.isSmsDisabledForMe(context) + "; isSupportMultiuser " + OsUtil.isSupportMultiUser(context) + "; isInLoginUser " + OsUtil.isInLoginUser());
            MLog.d("CspApp", "MmsApp onCreate finsh");
            if (context.getResources() != null) {
                this.mFontScale = context.getResources().getConfiguration().fontScale;
            }
            if (this.mCustMmsApp != null) {
                this.mCustMmsApp.registerPhoneServiceStateListener();
            }
            this.newMessageContentObserver = new NewMessageContentObserver(context, new Handler());
            this.newMessageContentObserver.registerUpdaterObserver(true);
        }
    }

    public static synchronized MmsApp getApplication() {
        MmsApp mmsApp;
        synchronized (MmsApp.class) {
            mmsApp = sMmsApp;
        }
        return mmsApp;
    }

    private static synchronized void setApplication(MmsApp mmsApp) {
        synchronized (MmsApp.class) {
            sMmsApp = mmsApp;
        }
    }

    public void setThumbnailManager(ThumbnailManager tbm) {
        this.mThumbnailManager = tbm;
    }

    public void onTerminate() {
        super.onTerminate();
        HwBackgroundLoader.getInst().onTerminate();
        SortCursor.unRegisterContactsObserver();
        if (this.mRcsMMSApp != null) {
            this.mRcsMMSApp.deInit();
        }
        unRegisterMmsBroadcastReceiver();
        HwDualCardNameHelper.self().unregisterReceiver();
        PrivacyStateListener.self().unregisterSecureDatabasesChanged(getApplicationContext());
        if (this.mCustMmsApp != null) {
            this.mCustMmsApp.unRegisterCustDbObserver();
        }
        if (MmsConfig.getSupportSmartSmsFeature()) {
            SmartSmsPublicinfoUtil.clearPublicInfo();
        }
        if (this.mCustMmsApp != null) {
            this.mCustMmsApp.unregisterPhoneServiceStateListener();
        }
        if (this.newMessageContentObserver != null) {
            this.newMessageContentObserver.unregisterUpdaterObserver();
        }
    }

    public void onLowMemory() {
        super.onLowMemory();
        if (this.mPduLoaderManager != null) {
            this.mPduLoaderManager.onLowMemory();
        }
        if (this.mThumbnailManager != null) {
            this.mThumbnailManager.onLowMemory();
        }
        if (this.mHwSimpleImageLoader != null) {
            this.mHwSimpleImageLoader.onLowMemory();
        }
    }

    public PduLoaderManager getPduLoaderManager() {
        PduLoaderManager pduLoaderManager;
        synchronized (PduLoaderManager.class) {
            if (this.mPduLoaderManager == null) {
                this.mPduLoaderManager = new PduLoaderManager(getApplicationContext(), HwBackgroundLoader.getUIHandler());
            }
            pduLoaderManager = this.mPduLoaderManager;
        }
        return pduLoaderManager;
    }

    public HwSimpleImageLoader getHwSimpleImageLoader() {
        HwSimpleImageLoader hwSimpleImageLoader;
        synchronized (HwSimpleImageLoader.class) {
            if (this.mHwSimpleImageLoader == null) {
                this.mHwSimpleImageLoader = new HwSimpleImageLoader(getApplicationContext(), HwBackgroundLoader.getUIHandler());
            }
            hwSimpleImageLoader = this.mHwSimpleImageLoader;
        }
        return hwSimpleImageLoader;
    }

    public ThumbnailManager getThumbnailManager() {
        return this.mThumbnailManager;
    }

    public void removeThumbnail(Uri uri) {
        if (uri != null && this.mThumbnailManager != null) {
            this.mThumbnailManager.removeThumbnail(uri);
        }
    }

    public void clearThumbnail() {
        if (this.mThumbnailManager != null) {
            this.mThumbnailManager.clear();
        }
    }

    public void onConfigurationChanged(Configuration newConfig) {
        MLog.d("MSG_APP_Mms", "onConfigurationChanged...");
        super.onConfigurationChanged(newConfig);
        LayoutManager.getInstance().onConfigurationChanged(newConfig);
        checkSystemConfigurationFontScaleChanged(newConfig.fontScale);
        if (MmsConfig.getSupportSmartSmsFeature()) {
            SmartSmsSdkUtil.setThemeMode(newConfig.extraConfig.getConfigItem(1));
        }
        if (newConfig.orientation == 2) {
            StatisticalHelper.incrementReportCount(getApplicationContext(), 2220);
        }
    }

    public TelephonyManager getTelephonyManager() {
        if (this.mTelephonyManager == null) {
            try {
                this.mTelephonyManager = TelephonyManager.from(this);
            } catch (Exception ex) {
                MLog.e("MSG_APP_Mms", "getTelephonyManager fail. ", (Throwable) ex);
            }
            if (this.mTelephonyManager == null) {
                this.mTelephonyManager = TelephonyManager.getDefault();
            }
        }
        return this.mTelephonyManager;
    }

    public MSimTelephonyManager getMSimTelephonyManager() {
        if (this.mMSimTelephonyManager == null) {
            try {
                this.mMSimTelephonyManager = MSimTelephonyManager.from(this);
            } catch (Exception ex) {
                MLog.e("MSG_APP_Mms", "getMSimTelephonyManager fail. ", (Throwable) ex);
            }
            if (this.mMSimTelephonyManager == null) {
                this.mMSimTelephonyManager = MSimTelephonyManager.getDefault();
            }
        }
        return this.mMSimTelephonyManager;
    }

    public static TelephonyManager getDefaultTelephonyManager() {
        return getApplication().getTelephonyManager();
    }

    public static MSimTelephonyManager getDefaultMSimTelephonyManager() {
        return getApplication().getMSimTelephonyManager();
    }

    public NewMessageContentObserver getNewMessageContentObserver() {
        return this.newMessageContentObserver;
    }

    public SearchRecentSuggestions getRecentSuggestions() {
        return null;
    }

    public String getCurrentCountryIso() {
        return HwBackgroundLoader.getInst().getCurrentCountryIso();
    }

    public DrmManagerClient getDrmManagerClient() {
        if (this.mDrmManagerClient == null) {
            this.mDrmManagerClient = new DrmManagerClient(getApplicationContext());
        }
        return this.mDrmManagerClient;
    }

    private void registerMmsBroadcastReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.CONFIGURATION_CHANGED");
        if (this.mIntentReceiver == null) {
            this.mIntentReceiver = new MmsBroadcastReceiver();
        }
        registerReceiver(this.mIntentReceiver, intentFilter);
    }

    private void unRegisterMmsBroadcastReceiver() {
        unregisterReceiver(this.mIntentReceiver);
        this.mIntentReceiver = null;
    }

    private boolean isContactCacheProcess() {
        int pid = Process.myPid();
        String myProcessName = null;
        try {
            List<RunningAppProcessInfo> procInfos = ((ActivityManager) getApplicationContext().getSystemService("activity")).getRunningAppProcesses();
            if (procInfos == null || procInfos.size() == 0) {
                return false;
            }
            for (RunningAppProcessInfo procInfo : procInfos) {
                if (pid == procInfo.pid) {
                    myProcessName = procInfo.processName;
                    break;
                }
            }
            if (myProcessName != null && myProcessName.contains(":")) {
                return true;
            }
            return false;
        } catch (Exception e) {
            MLog.e("CspApp", "defined whether isContactCacheProcess occur exception: " + e);
        }
    }

    private void checkSystemConfigurationFontScaleChanged(float newFontScale) {
        if (((double) Math.abs(this.mFontScale - newFontScale)) >= 1.0E-7d) {
            this.mFontScale = newFontScale;
            PreferenceUtils.setPreferenceFloat(getApplicationContext(), "pref_key_sms_font_scale", this.mFontScale);
        }
    }

    private void setRcsConfig() {
        Set<String> configs = new HashSet();
        LoadCarrierConfigUtil loadCarrierConfigUtil = new LoadCarrierConfigUtil();
        configs.add("ro.config.hw_rcs_product");
        configs.add("ro.config.hw_rcs_vendor");
        configs.add("huawei_rcs_enabler");
        configs.add("show_rcs_disconnect_notify_in_MMS");
        configs.add("CONFIG_GROUPCHAT_NICKNAME_ENABLE");
        configs.add("CONFIG_GROUPCHAT_MEMBER_TOPIC_ENABLE");
        configs.add("mms_disconnected_notify_enable");
        configs.add("GROUP_NICKNAME_SUPPORT_ALL_CHARACTERS");
        configs.add("support_capability_validity");
        configs.add("is_cmcc_rcs_cust");
        configs.add("video_recording_time_limit");
        configs.add("sms_port");
        configs.add("is_show_undelivered_icon");
        configs.add("CONFIG_GROUPMESSAGE_DELIVERY_REPORT_SETTING_SHOW");
        configs.add("CONFIG_GROUPCHAT_AUTOACCEPT_SETTING_SHOW");
        configs.add("is_enable_group_silentmode");
        configs.add("first_time_login_mode");
        configs.add("once_again_login_mode");
        configs.add("CONFIG_RCS_GROUP_DETAIL_SHOW_ICON");
        configs.add("is_support_ft_outdate");
        configs.add("ObligateStoreSize");
        configs.add("hw_rcs_version");
        configs.add("is_support_LocationShare");
        configs.add("cust_image_ft_max_size");
        configs.add("cust_video_ft_max_size");
        configs.add("cust_other_ft_max_size");
        CarrierConfigChangeBroadcastReceiver.setOwnConfigs(configs, this.mRcsChangeListener);
        loadCarrierConfigUtil.setOwnConfigs(configs);
        loadCarrierConfigUtil.loadCarrierConfig(getApplicationContext());
    }
}
