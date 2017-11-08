package com.android.server;

import android.app.ActivityManagerNative;
import android.app.ActivityThread;
import android.app.INotificationManager;
import android.app.usage.UsageStatsManagerInternal;
import android.common.HwFrameworkFactory;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources.Theme;
import android.hsm.HwSystemManager;
import android.os.BaseBundle;
import android.os.Build;
import android.os.Environment;
import android.os.FactoryTest;
import android.os.FileUtils;
import android.os.IPowerManager;
import android.os.Looper;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.StrictMode;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.Trace;
import android.os.UserHandle;
import android.os.storage.IMountService;
import android.os.storage.IMountService.Stub;
import android.provider.Settings.Global;
import android.util.DisplayMetrics;
import android.util.EventLog;
import android.util.Jlog;
import android.util.Slog;
import android.view.WindowManager;
import android.vr.VRManagerService;
import com.android.internal.os.BinderInternal;
import com.android.internal.os.HwBootCheck;
import com.android.internal.os.HwBootFail;
import com.android.internal.os.SamplingProfilerIntegration;
import com.android.internal.widget.ILockSettings;
import com.android.server.HwServiceFactory.IHwAttestationServiceFactory;
import com.android.server.HwServiceFactory.IHwFingerprintService;
import com.android.server.HwServiceFactory.IHwForceRotationManagerServiceWrapper;
import com.android.server.HwServiceFactory.IHwLocationManagerService;
import com.android.server.HwServiceFactory.IHwTelephonyRegistry;
import com.android.server.HwServiceFactory.IJankShieldServiceFactory;
import com.android.server.accessibility.AccessibilityManagerService;
import com.android.server.am.ActivityManagerService;
import com.android.server.am.ActivityManagerService.Lifecycle;
import com.android.server.audio.AudioService;
import com.android.server.camera.CameraService;
import com.android.server.clipboard.ClipboardService;
import com.android.server.connectivity.MetricsLoggerService;
import com.android.server.devicepolicy.DevicePolicyManagerService;
import com.android.server.display.DisplayManagerService;
import com.android.server.dreams.DreamManagerService;
import com.android.server.fingerprint.FingerprintService;
import com.android.server.hdmi.HdmiControlService;
import com.android.server.input.InputManagerService;
import com.android.server.job.JobSchedulerService;
import com.android.server.job.controllers.JobStatus;
import com.android.server.lights.LightsService;
import com.android.server.media.MediaResourceMonitorService;
import com.android.server.media.MediaRouterService;
import com.android.server.media.MediaSessionService;
import com.android.server.media.projection.MediaProjectionManagerService;
import com.android.server.net.NetworkPolicyManagerService;
import com.android.server.net.NetworkStatsService;
import com.android.server.notification.NotificationManagerService;
import com.android.server.os.SchedulingPolicyService;
import com.android.server.pg.PGManagerService;
import com.android.server.pm.BackgroundDexOptService;
import com.android.server.pm.Installer;
import com.android.server.pm.LauncherAppsService;
import com.android.server.pm.OtaDexoptService;
import com.android.server.pm.PackageManagerService;
import com.android.server.pm.ShortcutService;
import com.android.server.pm.UserManagerService.LifeCycle;
import com.android.server.power.PowerManagerService;
import com.android.server.power.ShutdownThread;
import com.android.server.restrictions.RestrictionsManagerService;
import com.android.server.sensor.MagnBracketObserver;
import com.android.server.soundtrigger.SoundTriggerService;
import com.android.server.telecom.TelecomLoaderService;
import com.android.server.trust.TrustManagerService;
import com.android.server.tv.TvInputManagerService;
import com.android.server.tv.TvRemoteService;
import com.android.server.twilight.TwilightService;
import com.android.server.usage.UsageStatsService;
import com.android.server.vr.VrManagerService;
import com.android.server.webkit.WebViewUpdateService;
import com.android.server.wm.WindowManagerService;
import dalvik.system.VMRuntime;
import huawei.android.app.HwCustEmergDataManager;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public final class SystemServer {
    private static final String ACCOUNT_SERVICE_CLASS = "com.android.server.accounts.AccountManagerService$Lifecycle";
    private static final String APPWIDGET_SERVICE_CLASS = "com.android.server.appwidget.AppWidgetService";
    private static final String BACKUP_MANAGER_SERVICE_CLASS = "com.android.server.backup.BackupManagerService$Lifecycle";
    private static final String BLOCK_MAP_FILE = "/cache/recovery/block.map";
    private static final String CONTENT_SERVICE_CLASS = "com.android.server.content.ContentService$Lifecycle";
    private static final int DEFAULT_SYSTEM_THEME = 16974143;
    private static final String DESKCLOCK_PACKAGENAME = "com.android.deskclock";
    private static final long EARLIEST_SUPPORTED_TIME = 86400000;
    private static final String ENCRYPTED_STATE = "1";
    private static final String ENCRYPTING_STATE = "trigger_restart_min_framework";
    private static final String ETHERNET_SERVICE_CLASS = "com.android.server.ethernet.EthernetService";
    private static final String JOB_SCHEDULER_SERVICE_CLASS = "com.android.server.job.JobSchedulerService";
    private static final boolean LOCAL_LOGV = true;
    private static final String LOCK_SETTINGS_SERVICE_CLASS = "com.android.server.LockSettingsService$Lifecycle";
    private static final String MIDI_SERVICE_CLASS = "com.android.server.midi.MidiService$Lifecycle";
    private static final String MOUNT_SERVICE_CLASS = "com.android.server.MountService$Lifecycle";
    private static final String PERSISTENT_DATA_BLOCK_PROP = "ro.frp.pst";
    private static final String PRINT_MANAGER_SERVICE_CLASS = "com.android.server.print.PrintManagerService";
    private static final String SEARCH_MANAGER_SERVICE_CLASS = "com.android.server.search.SearchManagerService$Lifecycle";
    private static final long SNAPSHOT_INTERVAL = 3600000;
    private static final String TAG = "SystemServer";
    private static final String THERMAL_OBSERVER_CLASS = "com.google.android.clockwork.ThermalObserver";
    private static final String UNCRYPT_PACKAGE_FILE = "/cache/recovery/uncrypt_file";
    private static final String USB_SERVICE_CLASS = "com.android.server.usb.UsbService$Lifecycle";
    private static final String VOICE_RECOGNITION_MANAGER_SERVICE_CLASS = "com.android.server.voiceinteraction.VoiceInteractionManagerService";
    private static final String WEAR_BLUETOOTH_SERVICE_CLASS = "com.google.android.clockwork.bluetooth.WearBluetoothService";
    private static final String WIFI_NAN_SERVICE_CLASS = "com.android.server.wifi.nan.WifiNanService";
    private static final String WIFI_P2P_SERVICE_CLASS = "com.android.server.wifi.p2p.WifiP2pService";
    private static final String WIFI_SERVICE_CLASS = "com.android.server.wifi.WifiService";
    private static final int sMaxBinderThreads = 31;
    final Thread fingerprintStartThread = new Thread(new Runnable() {
        public void run() {
            Slog.i(SystemServer.TAG, "start Finger Print Service async");
            Class serviceClass = null;
            try {
                IHwFingerprintService ifs = HwServiceFactory.getHwFingerprintService();
                if (ifs != null) {
                    serviceClass = ifs.createServiceClass();
                }
                if (serviceClass != null) {
                    SystemServer.this.mSystemServiceManager.startService(serviceClass);
                } else {
                    SystemServer.this.mSystemServiceManager.startService(FingerprintService.class);
                }
                Slog.i(SystemServer.TAG, "FingerPrintService ready");
            } catch (Throwable e) {
                Slog.e(SystemServer.TAG, "Start fingerprintservice error", e);
            }
        }
    });
    private ActivityManagerService mActivityManagerService;
    private ContentResolver mContentResolver;
    private DisplayManagerService mDisplayManagerService;
    private EntropyMixer mEntropyMixer;
    private final int mFactoryTestMode = FactoryTest.getMode();
    private boolean mFirstBoot;
    private boolean mOnlyCore;
    private PGManagerService mPGManagerService;
    private PackageManager mPackageManager;
    private PackageManagerService mPackageManagerService;
    private PowerManagerService mPowerManagerService;
    private Timer mProfilerSnapshotTimer;
    private Context mSystemContext;
    private SystemServiceManager mSystemServiceManager;
    private WebViewUpdateService mWebViewUpdateService;

    private static native void startSensorService();

    public static void main(String[] args) {
        new SystemServer().run();
    }

    public SystemServer() {
        if (this.mFactoryTestMode != 0) {
            Jlog.d(26, "JL_FIRST_BOOT");
        }
    }

    private void run() {
        FileNotFoundException e;
        int userType;
        int adbEnable;
        IOException ex;
        Exception ea;
        Throwable th;
        try {
            Trace.traceBegin(524288, "InitBeforeStartServices");
            if (System.currentTimeMillis() < 86400000) {
                Slog.w(TAG, "System clock is before 1970; setting to 1970.");
                SystemClock.setCurrentTimeMillis(86400000);
            }
            if (!SystemProperties.get("persist.sys.language").isEmpty()) {
                SystemProperties.set("persist.sys.locale", Locale.getDefault().toLanguageTag());
                SystemProperties.set("persist.sys.language", "");
                SystemProperties.set("persist.sys.country", "");
                SystemProperties.set("persist.sys.localevar", "");
            }
            Slog.i(TAG, "Entered the Android system server!");
            EventLog.writeEvent(EventLogTags.BOOT_PROGRESS_SYSTEM_RUN, SystemClock.uptimeMillis());
            Jlog.d(30, "JL_BOOT_PROGRESS_SYSTEM_RUN");
            SystemProperties.set("persist.sys.dalvik.vm.lib.2", VMRuntime.getRuntime().vmLibrary());
            if (SamplingProfilerIntegration.isEnabled()) {
                SamplingProfilerIntegration.start();
                this.mProfilerSnapshotTimer = new Timer();
                this.mProfilerSnapshotTimer.schedule(new TimerTask() {
                    public void run() {
                        SamplingProfilerIntegration.writeSnapshot("system_server", null);
                    }
                }, SNAPSHOT_INTERVAL, SNAPSHOT_INTERVAL);
            }
            VMRuntime.getRuntime().clearGrowthLimit();
            VMRuntime.getRuntime().setTargetHeapUtilization(0.8f);
            Build.ensureFingerprintProperty();
            Environment.setUserRequired(LOCAL_LOGV);
            BaseBundle.setShouldDefuse(LOCAL_LOGV);
            BinderInternal.disableBackgroundScheduling(LOCAL_LOGV);
            BinderInternal.setMaxThreads(31);
            Process.setThreadPriority(-2);
            Process.setCanSelfBackground(false);
            Looper.prepareMainLooper();
            System.loadLibrary("android_servers");
            performPendingShutdown();
            createSystemContext();
            this.mSystemServiceManager = new SystemServiceManager(this.mSystemContext);
            LocalServices.addService(SystemServiceManager.class, this.mSystemServiceManager);
            try {
                Trace.traceBegin(524288, "StartServices");
                startBootstrapServices();
                startCoreServices();
                startOtherServices();
                Trace.traceEnd(524288);
                if (StrictMode.conditionallyEnableDebugLogging()) {
                    Slog.i(TAG, "Enabled StrictMode for system server main thread.");
                }
                int logSwitch = 0;
                BufferedReader bufferedReader = null;
                try {
                    BufferedReader hwLogReader = new BufferedReader(new InputStreamReader(new FileInputStream("/dev/hwlog_switch"), "UTF-8"));
                    try {
                        String tempString = hwLogReader.readLine();
                        if (tempString != null) {
                            logSwitch = Integer.parseInt(tempString);
                        }
                        Slog.i(TAG, "/dev/hwlog_switch=" + logSwitch);
                        if (hwLogReader != null) {
                            try {
                                hwLogReader.close();
                            } catch (IOException e2) {
                                Slog.i(TAG, "hwLogReader close failed", e2);
                            }
                        }
                        bufferedReader = hwLogReader;
                    } catch (FileNotFoundException e3) {
                        e = e3;
                        bufferedReader = hwLogReader;
                        Slog.e(TAG, "/dev/hwlog_switch not exist", e);
                        if (bufferedReader != null) {
                            try {
                                bufferedReader.close();
                            } catch (IOException e22) {
                                Slog.i(TAG, "hwLogReader close failed", e22);
                            }
                        }
                        userType = SystemProperties.getInt("ro.logsystem.usertype", 0);
                        adbEnable = Global.getInt(this.mSystemContext.getContentResolver(), "adb_enabled", 0);
                        SystemProperties.set("sys.logbuffer.disable", "true");
                        SmartShrinker.reclaim(Process.myPid(), 3);
                        Looper.loop();
                        throw new RuntimeException("Main thread loop unexpectedly exited");
                    } catch (IOException e4) {
                        ex = e4;
                        bufferedReader = hwLogReader;
                        Slog.i(TAG, "logswitch read failed", ex);
                        if (bufferedReader != null) {
                            try {
                                bufferedReader.close();
                            } catch (IOException e222) {
                                Slog.i(TAG, "hwLogReader close failed", e222);
                            }
                        }
                        userType = SystemProperties.getInt("ro.logsystem.usertype", 0);
                        adbEnable = Global.getInt(this.mSystemContext.getContentResolver(), "adb_enabled", 0);
                        SystemProperties.set("sys.logbuffer.disable", "true");
                        SmartShrinker.reclaim(Process.myPid(), 3);
                        Looper.loop();
                        throw new RuntimeException("Main thread loop unexpectedly exited");
                    } catch (Exception e5) {
                        ea = e5;
                        bufferedReader = hwLogReader;
                        try {
                            Slog.i(TAG, "logswitch read exception", ea);
                            if (bufferedReader != null) {
                                try {
                                    bufferedReader.close();
                                } catch (IOException e2222) {
                                    Slog.i(TAG, "hwLogReader close failed", e2222);
                                }
                            }
                            userType = SystemProperties.getInt("ro.logsystem.usertype", 0);
                            adbEnable = Global.getInt(this.mSystemContext.getContentResolver(), "adb_enabled", 0);
                            SystemProperties.set("sys.logbuffer.disable", "true");
                            SmartShrinker.reclaim(Process.myPid(), 3);
                            Looper.loop();
                            throw new RuntimeException("Main thread loop unexpectedly exited");
                        } catch (Throwable th2) {
                            th = th2;
                            if (bufferedReader != null) {
                                try {
                                    bufferedReader.close();
                                } catch (IOException e22222) {
                                    Slog.i(TAG, "hwLogReader close failed", e22222);
                                }
                            }
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        bufferedReader = hwLogReader;
                        if (bufferedReader != null) {
                            bufferedReader.close();
                        }
                        throw th;
                    }
                } catch (FileNotFoundException e6) {
                    e = e6;
                    Slog.e(TAG, "/dev/hwlog_switch not exist", e);
                    if (bufferedReader != null) {
                        bufferedReader.close();
                    }
                    userType = SystemProperties.getInt("ro.logsystem.usertype", 0);
                    adbEnable = Global.getInt(this.mSystemContext.getContentResolver(), "adb_enabled", 0);
                    SystemProperties.set("sys.logbuffer.disable", "true");
                    SmartShrinker.reclaim(Process.myPid(), 3);
                    Looper.loop();
                    throw new RuntimeException("Main thread loop unexpectedly exited");
                } catch (IOException e7) {
                    ex = e7;
                    Slog.i(TAG, "logswitch read failed", ex);
                    if (bufferedReader != null) {
                        bufferedReader.close();
                    }
                    userType = SystemProperties.getInt("ro.logsystem.usertype", 0);
                    adbEnable = Global.getInt(this.mSystemContext.getContentResolver(), "adb_enabled", 0);
                    SystemProperties.set("sys.logbuffer.disable", "true");
                    SmartShrinker.reclaim(Process.myPid(), 3);
                    Looper.loop();
                    throw new RuntimeException("Main thread loop unexpectedly exited");
                } catch (Exception e8) {
                    ea = e8;
                    Slog.i(TAG, "logswitch read exception", ea);
                    if (bufferedReader != null) {
                        bufferedReader.close();
                    }
                    userType = SystemProperties.getInt("ro.logsystem.usertype", 0);
                    adbEnable = Global.getInt(this.mSystemContext.getContentResolver(), "adb_enabled", 0);
                    SystemProperties.set("sys.logbuffer.disable", "true");
                    SmartShrinker.reclaim(Process.myPid(), 3);
                    Looper.loop();
                    throw new RuntimeException("Main thread loop unexpectedly exited");
                }
                userType = SystemProperties.getInt("ro.logsystem.usertype", 0);
                adbEnable = Global.getInt(this.mSystemContext.getContentResolver(), "adb_enabled", 0);
                if (logSwitch != 1 && ((1 == userType || 6 == userType) && adbEnable <= 0)) {
                    SystemProperties.set("sys.logbuffer.disable", "true");
                }
                SmartShrinker.reclaim(Process.myPid(), 3);
                Looper.loop();
                throw new RuntimeException("Main thread loop unexpectedly exited");
            } catch (Throwable th4) {
                Trace.traceEnd(524288);
            }
        } finally {
            Trace.traceEnd(524288);
        }
    }

    private void reportWtf(String msg, Throwable e) {
        Slog.w(TAG, "***********************************************");
        Slog.wtf(TAG, "BOOT FAILURE " + msg, e);
    }

    private void performPendingShutdown() {
        String shutdownAction = SystemProperties.get(ShutdownThread.SHUTDOWN_ACTION_PROPERTY, "");
        if (shutdownAction != null && shutdownAction.length() > 0) {
            String substring;
            boolean reboot = shutdownAction.charAt(0) == '1' ? LOCAL_LOGV : false;
            if (shutdownAction.length() > 1) {
                substring = shutdownAction.substring(1, shutdownAction.length());
            } else {
                substring = null;
            }
            if ("recovery-update".equals(substring)) {
                File packageFile = new File(UNCRYPT_PACKAGE_FILE);
                if (packageFile.exists()) {
                    String filename = null;
                    try {
                        filename = FileUtils.readTextFile(packageFile, 0, null);
                    } catch (IOException e) {
                        Slog.e(TAG, "Error reading uncrypt package file", e);
                    }
                    if (!(filename == null || !filename.startsWith("/data") || new File(BLOCK_MAP_FILE).exists())) {
                        Slog.e(TAG, "Can't find block map file, uncrypt failed or unexpected runtime restart?");
                        return;
                    }
                }
            }
            ShutdownThread.rebootOrShutdown(null, reboot, substring);
        }
    }

    private void createSystemContext() {
        this.mSystemContext = ActivityThread.systemMain().getSystemContext();
        this.mSystemContext.setTheme(DEFAULT_SYSTEM_THEME);
    }

    private void startBootstrapServices() {
        Installer installer = (Installer) this.mSystemServiceManager.startService(Installer.class);
        this.mActivityManagerService = ((Lifecycle) this.mSystemServiceManager.startService(Lifecycle.class)).getService();
        this.mActivityManagerService.setSystemServiceManager(this.mSystemServiceManager);
        this.mActivityManagerService.setInstaller(installer);
        try {
            this.mPowerManagerService = (PowerManagerService) this.mSystemServiceManager.startService("com.android.server.power.HwPowerManagerService");
        } catch (RuntimeException e) {
            Slog.w(TAG, "create HwPowerManagerService failed");
            this.mPowerManagerService = (PowerManagerService) this.mSystemServiceManager.startService(PowerManagerService.class);
        }
        try {
            Slog.i(TAG, "PG Manager service");
            this.mPGManagerService = PGManagerService.getInstance(this.mSystemContext);
        } catch (Throwable e2) {
            reportWtf("PG Manager service", e2);
        }
        Trace.traceBegin(524288, "InitPowerManagement");
        this.mActivityManagerService.initPowerManagement();
        Trace.traceEnd(524288);
        try {
            this.mSystemServiceManager.startService("com.android.server.lights.HwLightsService");
        } catch (RuntimeException e3) {
            Slog.w(TAG, "create HwLightsService failed");
            this.mSystemServiceManager.startService(LightsService.class);
        }
        this.mDisplayManagerService = (DisplayManagerService) this.mSystemServiceManager.startService(DisplayManagerService.class);
        try {
            this.mSystemServiceManager.startService("com.android.server.security.HwSecurityService");
            Slog.i(TAG, "HwSecurityService start success");
        } catch (Exception e4) {
            Slog.e(TAG, "can't start HwSecurityService service");
        }
        this.mSystemServiceManager.startBootPhase(100);
        String cryptState = SystemProperties.get("vold.decrypt");
        if (ENCRYPTING_STATE.equals(cryptState)) {
            Slog.w(TAG, "Detected encryption in progress - only parsing core apps");
            this.mOnlyCore = LOCAL_LOGV;
        } else if (ENCRYPTED_STATE.equals(cryptState)) {
            Slog.w(TAG, "Device encrypted - only parsing core apps");
            this.mOnlyCore = LOCAL_LOGV;
        }
        this.mActivityManagerService.bootSceneEnd(100);
        HwBootFail.setBootStage(83886088);
        traceBeginAndSlog("StartPackageManagerService");
        Slog.i(TAG, "Package Manager");
        HwCustEmergDataManager emergDataManager = HwCustEmergDataManager.getDefault();
        if (emergDataManager != null && emergDataManager.isEmergencyState()) {
            this.mOnlyCore = LOCAL_LOGV;
            if (emergDataManager.isEmergencyMountState()) {
                emergDataManager.backupEmergencyDataFile();
            }
        }
        this.mPackageManagerService = PackageManagerService.main(this.mSystemContext, installer, this.mFactoryTestMode != 0 ? LOCAL_LOGV : false, this.mOnlyCore);
        this.mFirstBoot = this.mPackageManagerService.isFirstBoot();
        this.mPackageManager = this.mSystemContext.getPackageManager();
        Trace.traceEnd(524288);
        HwBootCheck.addBootInfo("[bootinfo]\nisFirstBoot: " + this.mFirstBoot + "\n" + "isUpgrade: " + this.mPackageManagerService.isUpgrade());
        this.mActivityManagerService.bootSceneStart(101, JobStatus.DEFAULT_TRIGGER_MAX_DELAY);
        HwBootFail.setBootStage(83886089);
        if (!(this.mOnlyCore || SystemProperties.getBoolean("config.disable_otadexopt", false))) {
            traceBeginAndSlog("StartOtaDexOptService");
            try {
                OtaDexoptService.main(this.mSystemContext, this.mPackageManagerService);
            } catch (Throwable e22) {
                reportWtf("starting OtaDexOptService", e22);
            } finally {
                Trace.traceEnd(524288);
            }
        }
        traceBeginAndSlog("StartUserManagerService");
        this.mSystemServiceManager.startService(LifeCycle.class);
        Trace.traceEnd(524288);
        if (this.mFirstBoot && this.mPackageManagerService.isUpgrade()) {
            Jlog.d(26, "JL_FIRST_BOOT");
        }
        AttributeCache.init(this.mSystemContext);
        this.mActivityManagerService.setSystemProcess();
        startSensorService();
    }

    private void startCoreServices() {
        try {
            this.mSystemServiceManager.startService("com.android.server.HwBatteryService");
        } catch (RuntimeException e) {
            Slog.w(TAG, "create HwBatteryService failed");
            this.mSystemServiceManager.startService(BatteryService.class);
        }
        this.mSystemServiceManager.startService(UsageStatsService.class);
        this.mActivityManagerService.setUsageStatsManager((UsageStatsManagerInternal) LocalServices.getService(UsageStatsManagerInternal.class));
        this.mWebViewUpdateService = (WebViewUpdateService) this.mSystemServiceManager.startService(WebViewUpdateService.class);
    }

    private void startOtherServices() {
        Throwable e;
        ConsumerIrService consumerIrService;
        Throwable e2;
        LocationManagerService location;
        CountryDetectorService countryDetectorService;
        ILockSettings iLockSettings;
        AssetAtlasService assetAtlasService;
        MediaRouterService mediaRouterService;
        NetworkScoreService networkScoreService;
        INotificationManager notification;
        IHwLocationManagerService hwLocation;
        CountryDetectorService countryDetectorService2;
        SerialService serialService;
        SerialService serialService2;
        HardwarePropertiesManagerService hardwarePropertiesManagerService;
        HardwarePropertiesManagerService hardwarePropertiesManagerService2;
        IHwAttestationServiceFactory attestation;
        NetworkTimeUpdateService networkTimeUpdateService;
        CommonTimeManagementService commonTimeManagementService;
        CertBlacklister certBlacklister;
        AssetAtlasService assetAtlasService2;
        MediaRouterService mediaRouterService2;
        IJankShieldServiceFactory jankshield;
        boolean safeMode;
        final MmsServiceBroker mmsService;
        Configuration config;
        DisplayMetrics metrics;
        Theme systemTheme;
        final NetworkManagementService networkManagementF;
        final NetworkStatsService networkStatsF;
        final NetworkPolicyManagerService networkPolicyF;
        final ConnectivityService connectivityF;
        final NetworkScoreService networkScoreF;
        final LocationManagerService locationF;
        final CountryDetectorService countryDetectorF;
        final NetworkTimeUpdateService networkTimeUpdaterF;
        final CommonTimeManagementService commonTimeMgmtServiceF;
        final AssetAtlasService atlasF;
        final InputManagerService inputManagerF;
        final TelephonyRegistry telephonyRegistryF;
        final MediaRouterService mediaRouterF;
        MmsServiceBroker mmsServiceF;
        final Context context;
        ConsumerIrService consumerIr;
        Context context2 = this.mSystemContext;
        VibratorService vibratorService = null;
        IMountService mountService = null;
        NetworkManagementService networkManagement = null;
        NetworkStatsService networkStats = null;
        NetworkPolicyManagerService networkPolicy = null;
        ConnectivityService connectivity = null;
        NetworkScoreService networkScoreService2 = null;
        WindowManagerService windowManagerService = null;
        NetworkTimeUpdateService networkTimeUpdateService2 = null;
        CommonTimeManagementService commonTimeManagementService2 = null;
        InputManagerService inputManager = null;
        TelephonyRegistry telephonyRegistry = null;
        HwCustEmergDataManager emergDataManager = HwCustEmergDataManager.getDefault();
        if (!(emergDataManager == null || emergDataManager.isEmergencyState())) {
            HwServiceFactory.activePlaceFile();
        }
        boolean disableStorage = SystemProperties.getBoolean("config.disable_storage", false);
        boolean disableBluetooth = SystemProperties.getBoolean("config.disable_bluetooth", false);
        boolean disableLocation = SystemProperties.getBoolean("config.disable_location", false);
        boolean disableSystemUI = SystemProperties.getBoolean("config.disable_systemui", false);
        boolean disableNonCoreServices = SystemProperties.getBoolean("config.disable_noncore", false);
        boolean disableNetwork = SystemProperties.getBoolean("config.disable_network", false);
        boolean disableNetworkTime = SystemProperties.getBoolean("config.disable_networktime", false);
        boolean disableRtt = SystemProperties.getBoolean("config.disable_rtt", false);
        boolean disableMediaProjection = SystemProperties.getBoolean("config.disable_mediaproj", false);
        boolean disableSerial = SystemProperties.getBoolean("config.disable_serial", false);
        boolean disableSearchManager = SystemProperties.getBoolean("config.disable_searchmanager", false);
        boolean disableTrustManager = SystemProperties.getBoolean("config.disable_trustmanager", false);
        boolean disableTextServices = SystemProperties.getBoolean("config.disable_textservices", false);
        boolean disableSamplingProfiler = SystemProperties.getBoolean("config.disable_samplingprof", false);
        boolean isEmulator = SystemProperties.get("ro.kernel.qemu").equals(ENCRYPTED_STATE);
        boolean enableRms = SystemProperties.getBoolean("ro.config.enable_rms", false);
        final boolean enableIaware = SystemProperties.getBoolean("persist.sys.enable_iaware", false);
        boolean tuiEnable = SystemProperties.getBoolean("ro.tui.service", false);
        boolean vrDisplayEnable = SystemProperties.getBoolean("ro.vr_display.service", false);
        boolean isChinaArea = "CN".equalsIgnoreCase(SystemProperties.get("ro.product.locale.region", ""));
        boolean isSupportedSecIme = isChinaArea;
        if (!disableNonCoreServices) {
            try {
                if (this.mPackageManager.hasSystemFeature("android.hardware.fingerprint")) {
                    this.fingerprintStartThread.start();
                }
            } catch (Throwable e3) {
                Slog.e(TAG, "Start fingerprintservice thread error", e3);
            }
        }
        try {
            AlarmManagerService almService;
            Slog.i(TAG, "Reading configuration...");
            SystemConfig.getInstance();
            traceBeginAndSlog("StartSchedulingPolicyService");
            ServiceManager.addService("scheduling_policy", new SchedulingPolicyService());
            Trace.traceEnd(524288);
            this.mSystemServiceManager.startService(TelecomLoaderService.class);
            traceBeginAndSlog("StartTelephonyRegistry");
            Slog.i(TAG, "Telephony Registry");
            if (HwSystemManager.mPermissionEnabled == 0) {
                telephonyRegistry = new TelephonyRegistry(context2);
            } else {
                IHwTelephonyRegistry itr = HwServiceFactory.getHwTelephonyRegistry();
                if (itr != null) {
                    telephonyRegistry = itr.getInstance(context2);
                } else {
                    telephonyRegistry = new TelephonyRegistry(context2);
                }
            }
            ServiceManager.addService("telephony.registry", telephonyRegistry);
            Trace.traceEnd(524288);
            traceBeginAndSlog("StartEntropyMixer");
            this.mEntropyMixer = new EntropyMixer(context2);
            Trace.traceEnd(524288);
            this.mContentResolver = context2.getContentResolver();
            Slog.i(TAG, "Camera Service");
            this.mSystemServiceManager.startService(CameraService.class);
            traceBeginAndSlog("StartAccountManagerService");
            this.mSystemServiceManager.startService(ACCOUNT_SERVICE_CLASS);
            Trace.traceEnd(524288);
            traceBeginAndSlog("StartContentService");
            this.mSystemServiceManager.startService(CONTENT_SERVICE_CLASS);
            Trace.traceEnd(524288);
            traceBeginAndSlog("InstallSystemProviders");
            this.mActivityManagerService.installSystemProviders();
            Trace.traceEnd(524288);
            traceBeginAndSlog("StartVibratorService");
            VibratorService vibratorService2 = new VibratorService(context2);
            try {
                ServiceManager.addService("vibrator", vibratorService2);
                Trace.traceEnd(524288);
                traceBeginAndSlog("StartConsumerIrService");
                consumerIrService = new ConsumerIrService(context2);
                ServiceManager.addService("consumer_ir", consumerIrService);
                Trace.traceEnd(524288);
                try {
                    almService = (AlarmManagerService) this.mSystemServiceManager.startService("com.android.server.HwAlarmManagerService");
                } catch (Exception e4) {
                    this.mSystemServiceManager.startService(AlarmManagerService.class);
                    almService = null;
                }
            } catch (RuntimeException e5) {
                e2 = e5;
                vibratorService = vibratorService2;
                Slog.e("System", "******************************************");
                Slog.e("System", "************ Failure starting core service", e2);
                location = null;
                countryDetectorService = null;
                iLockSettings = null;
                assetAtlasService = null;
                mediaRouterService = null;
                if (this.mFactoryTestMode != 1) {
                    traceBeginAndSlog("StartAccessibilityManagerService");
                    try {
                        Slog.i(TAG, "Input Method Service");
                        this.mSystemServiceManager.startService(InputMethodManagerService.Lifecycle.class);
                    } catch (Throwable e32) {
                        reportWtf("starting Input Manager Service", e32);
                    }
                    if (isChinaArea) {
                        try {
                            Slog.i(TAG, "Secure Input Method Service");
                            this.mSystemServiceManager.startService("com.android.server.HwSecureInputMethodManagerService$MyLifecycle");
                        } catch (Throwable e322) {
                            reportWtf("starting Secure Input Manager Service", e322);
                        }
                    }
                    try {
                        ServiceManager.addService("accessibility", new AccessibilityManagerService(context2));
                    } catch (Throwable e3222) {
                        reportWtf("starting Accessibility Manager", e3222);
                    }
                    Trace.traceEnd(524288);
                }
                windowManagerService.displayReady();
                try {
                    this.mSystemServiceManager.startService(MOUNT_SERVICE_CLASS);
                    mountService = Stub.asInterface(ServiceManager.getService("mount"));
                } catch (Throwable e32222) {
                    reportWtf("starting Mount Service", e32222);
                }
                this.mSystemServiceManager.startService(UiModeManagerService.class);
                this.mActivityManagerService.bootSceneEnd(101);
                HwBootFail.setBootStage(83886090);
                if (!this.mOnlyCore) {
                    Trace.traceBegin(524288, "UpdatePackagesIfNeeded");
                    try {
                        this.mPackageManagerService.updatePackagesIfNeeded();
                    } catch (Throwable e322222) {
                        reportWtf("update packages", e322222);
                    }
                    Trace.traceEnd(524288);
                }
                Trace.traceBegin(524288, "PerformFstrimIfNeeded");
                this.mPackageManagerService.performFstrimIfNeeded();
                Trace.traceEnd(524288);
                this.mActivityManagerService.bootSceneStart(HdmiCecKeycode.CEC_KEYCODE_RESTORE_VOLUME_FUNCTION, JobStatus.DEFAULT_TRIGGER_MAX_DELAY);
                HwBootFail.setBootStage(83886091);
                ActivityManagerNative.getDefault().showBootMessage(context2.getResources().getText(17040291), false);
                if (this.mFactoryTestMode != 1) {
                    startForceRotation(context2);
                    if (!disableNonCoreServices) {
                        traceBeginAndSlog("StartLockSettingsService");
                        try {
                            this.mSystemServiceManager.startService(LOCK_SETTINGS_SERVICE_CLASS);
                            iLockSettings = ILockSettings.Stub.asInterface(ServiceManager.getService("lock_settings"));
                        } catch (Throwable e3222222) {
                            reportWtf("starting LockSettingsService service", e3222222);
                        }
                        Trace.traceEnd(524288);
                        if (!SystemProperties.get(PERSISTENT_DATA_BLOCK_PROP).equals("")) {
                            this.mSystemServiceManager.startService(PersistentDataBlockService.class);
                        }
                        this.mSystemServiceManager.startService(DeviceIdleController.class);
                        this.mSystemServiceManager.startService(DevicePolicyManagerService.Lifecycle.class);
                    }
                    if (!disableSystemUI) {
                        traceBeginAndSlog("StartStatusBarManagerService");
                        try {
                            Slog.i(TAG, "Status Bar");
                            ServiceManager.addService("statusbar", HwServiceFactory.createHwStatusBarManagerService(context2, windowManagerService));
                        } catch (Throwable e32222222) {
                            reportWtf("starting StatusBarManagerService", e32222222);
                        }
                        Trace.traceEnd(524288);
                    }
                    if (!disableNonCoreServices) {
                        traceBeginAndSlog("StartClipboardService");
                        try {
                            ServiceManager.addService("clipboard", new ClipboardService(context2));
                        } catch (Throwable e322222222) {
                            reportWtf("starting Clipboard Service", e322222222);
                        }
                        Trace.traceEnd(524288);
                    }
                    if (!disableNetwork) {
                        traceBeginAndSlog("StartNetworkManagementService");
                        try {
                            networkManagement = NetworkManagementService.create(context2);
                            ServiceManager.addService("network_management", networkManagement);
                        } catch (Throwable e3222222222) {
                            reportWtf("starting NetworkManagement Service", e3222222222);
                        }
                        Trace.traceEnd(524288);
                    }
                    this.mSystemServiceManager.startService(TextServicesManagerService.Lifecycle.class);
                    if (!disableNetwork) {
                        traceBeginAndSlog("StartNetworkScoreService");
                        try {
                            networkScoreService = new NetworkScoreService(context2);
                            try {
                                ServiceManager.addService("network_score", networkScoreService);
                                networkScoreService2 = networkScoreService;
                            } catch (Throwable th) {
                                e3222222222 = th;
                                networkScoreService2 = networkScoreService;
                                reportWtf("starting Network Score Service", e3222222222);
                                Trace.traceEnd(524288);
                                traceBeginAndSlog("StartNetworkStatsService");
                                networkStats = NetworkStatsService.create(context2, networkManagement);
                                ServiceManager.addService("netstats", networkStats);
                                Trace.traceEnd(524288);
                                traceBeginAndSlog("StartNetworkPolicyManagerService");
                                networkPolicy = HwServiceFactory.getHwNetworkPolicyManagerService().getInstance(context2, this.mActivityManagerService, (IPowerManager) ServiceManager.getService("power"), networkStats, networkManagement);
                                ServiceManager.addService("netpolicy", networkPolicy);
                                Trace.traceEnd(524288);
                                if (context2.getPackageManager().hasSystemFeature("android.hardware.wifi.nan")) {
                                    this.mSystemServiceManager.startService(WIFI_NAN_SERVICE_CLASS);
                                } else {
                                    Slog.i(TAG, "No Wi-Fi NAN Service (NAN support Not Present)");
                                }
                                this.mSystemServiceManager.startService(WIFI_P2P_SERVICE_CLASS);
                                this.mSystemServiceManager.startService(WIFI_SERVICE_CLASS);
                                this.mSystemServiceManager.startService("com.android.server.wifi.scanner.WifiScanningService");
                                if (!disableRtt) {
                                    this.mSystemServiceManager.startService("com.android.server.wifi.RttService");
                                }
                                this.mSystemServiceManager.startService(ETHERNET_SERVICE_CLASS);
                                traceBeginAndSlog("StartConnectivityService");
                                Slog.i(TAG, "Connectivity Service");
                                connectivity = HwServiceFactory.getHwConnectivityManager().createHwConnectivityService(context2, networkManagement, networkStats, networkPolicy);
                                ServiceManager.addService("connectivity", connectivity);
                                networkStats.bindConnectivityManager(connectivity);
                                networkPolicy.bindConnectivityManager(connectivity);
                                Trace.traceEnd(524288);
                                traceBeginAndSlog("StartNsdService");
                                ServiceManager.addService("servicediscovery", NsdService.create(context2));
                                Trace.traceEnd(524288);
                                if (!disableNonCoreServices) {
                                    traceBeginAndSlog("StartUpdateLockService");
                                    try {
                                        ServiceManager.addService("updatelock", new UpdateLockService(context2));
                                    } catch (Throwable e32222222222) {
                                        reportWtf("starting UpdateLockService", e32222222222);
                                    }
                                    Trace.traceEnd(524288);
                                }
                                if (!disableNonCoreServices) {
                                    this.mSystemServiceManager.startService(RecoverySystemService.class);
                                }
                                Trace.traceBegin(524288, "WaitForAsecScan");
                                mountService.waitForAsecScan();
                                Trace.traceEnd(524288);
                                this.mSystemServiceManager.startService("com.android.server.notification.HwNotificationManagerService");
                                notification = INotificationManager.Stub.asInterface(ServiceManager.getService("notification"));
                                if (networkPolicy != null) {
                                    networkPolicy.bindNotificationManager(notification);
                                }
                                this.mSystemServiceManager.startService(HwServiceFactory.getDeviceStorageMonitorServiceClassName());
                                Slog.i(TAG, "TUI Connect enable " + tuiEnable);
                                if (tuiEnable) {
                                    try {
                                        ServiceManager.addService("tui", new TrustedUIService(context2));
                                    } catch (Throwable e322222222222) {
                                        Slog.e(TAG, "Failure starting TUI Service ", e322222222222);
                                    }
                                }
                                if (vrDisplayEnable) {
                                    Slog.i(TAG, "VR Display enable " + vrDisplayEnable);
                                    try {
                                        ServiceManager.addService("vr_display", new VRManagerService(context2));
                                    } catch (Throwable e3222222222222) {
                                        Slog.e(TAG, "Failure starting VR Service ", e3222222222222);
                                    }
                                }
                                if (!disableLocation) {
                                    traceBeginAndSlog("StartLocationManagerService");
                                    try {
                                        Slog.i(TAG, "Location Manager");
                                        hwLocation = HwServiceFactory.getHwLocationManagerService();
                                        if (hwLocation == null) {
                                            location = new LocationManagerService(context2);
                                        } else {
                                            location = hwLocation.getInstance(context2);
                                        }
                                        ServiceManager.addService("location", location);
                                    } catch (Throwable e32222222222222) {
                                        reportWtf("starting Location Manager", e32222222222222);
                                    }
                                    Trace.traceEnd(524288);
                                    traceBeginAndSlog("StartCountryDetectorService");
                                    try {
                                        countryDetectorService2 = new CountryDetectorService(context2);
                                        try {
                                            ServiceManager.addService("country_detector", countryDetectorService2);
                                            countryDetectorService = countryDetectorService2;
                                        } catch (Throwable th2) {
                                            e32222222222222 = th2;
                                            countryDetectorService = countryDetectorService2;
                                            reportWtf("starting Country Detector", e32222222222222);
                                            Trace.traceEnd(524288);
                                            traceBeginAndSlog("StartSearchManagerService");
                                            this.mSystemServiceManager.startService(SEARCH_MANAGER_SERVICE_CLASS);
                                            Trace.traceEnd(524288);
                                            this.mSystemServiceManager.startService(DropBoxManagerService.class);
                                            traceBeginAndSlog("StartWallpaperManagerService");
                                            this.mSystemServiceManager.startService(HwServiceFactory.getWallpaperManagerServiceClassName());
                                            Trace.traceEnd(524288);
                                            traceBeginAndSlog("StartAudioService");
                                            this.mSystemServiceManager.startService(AudioService.Lifecycle.class);
                                            Trace.traceEnd(524288);
                                            if (!disableNonCoreServices) {
                                                this.mSystemServiceManager.startService(DockObserver.class);
                                                if (context2.getPackageManager().hasSystemFeature("android.hardware.type.watch")) {
                                                    this.mSystemServiceManager.startService(THERMAL_OBSERVER_CLASS);
                                                }
                                            }
                                            traceBeginAndSlog("StartWiredAccessoryManager");
                                            inputManager.setWiredAccessoryCallbacks(new WiredAccessoryManager(context2, inputManager));
                                            Trace.traceEnd(524288);
                                            if (!disableNonCoreServices) {
                                                if (this.mPackageManager.hasSystemFeature("android.software.midi")) {
                                                    this.mSystemServiceManager.startService(MIDI_SERVICE_CLASS);
                                                }
                                                Trace.traceBegin(524288, "StartUsbService");
                                                this.mSystemServiceManager.startService(USB_SERVICE_CLASS);
                                                Trace.traceEnd(524288);
                                                if (!disableSerial) {
                                                    traceBeginAndSlog("StartSerialService");
                                                    try {
                                                        serialService = new SerialService(context2);
                                                        try {
                                                            ServiceManager.addService("serial", serialService);
                                                            serialService2 = serialService;
                                                        } catch (Throwable th3) {
                                                            e32222222222222 = th3;
                                                            serialService2 = serialService;
                                                            Slog.e(TAG, "Failure starting SerialService", e32222222222222);
                                                            Trace.traceEnd(524288);
                                                            Trace.traceBegin(524288, "StartHardwarePropertiesManagerService");
                                                            hardwarePropertiesManagerService = new HardwarePropertiesManagerService(context2);
                                                            try {
                                                                ServiceManager.addService("hardware_properties", hardwarePropertiesManagerService);
                                                                hardwarePropertiesManagerService2 = hardwarePropertiesManagerService;
                                                            } catch (Throwable th4) {
                                                                e32222222222222 = th4;
                                                                hardwarePropertiesManagerService2 = hardwarePropertiesManagerService;
                                                                Slog.e(TAG, "Failure starting HardwarePropertiesManagerService", e32222222222222);
                                                                Trace.traceEnd(524288);
                                                                this.mSystemServiceManager.startService(TwilightService.class);
                                                                this.mSystemServiceManager.startService("com.android.server.job.HwJobSchedulerService");
                                                                this.mSystemServiceManager.startService(SoundTriggerService.class);
                                                                if (!disableNonCoreServices) {
                                                                    if (this.mPackageManager.hasSystemFeature("android.software.backup")) {
                                                                        this.mSystemServiceManager.startService(BACKUP_MANAGER_SERVICE_CLASS);
                                                                    }
                                                                    this.mSystemServiceManager.startService(APPWIDGET_SERVICE_CLASS);
                                                                    if (this.mPackageManager.hasSystemFeature("android.software.voice_recognizers")) {
                                                                        this.mSystemServiceManager.startService(VOICE_RECOGNITION_MANAGER_SERVICE_CLASS);
                                                                    }
                                                                    if (GestureLauncherService.isGestureLauncherEnabled(context2.getResources())) {
                                                                        Slog.i(TAG, "Gesture Launcher Service");
                                                                        this.mSystemServiceManager.startService(GestureLauncherService.class);
                                                                    }
                                                                    this.mSystemServiceManager.startService(SensorNotificationService.class);
                                                                    this.mSystemServiceManager.startService(ContextHubSystemService.class);
                                                                }
                                                                HwServiceFactory.setupHwServices(context2);
                                                                traceBeginAndSlog("StartDiskStatsService");
                                                                ServiceManager.addService("diskstats", new DiskStatsService(context2));
                                                                Trace.traceEnd(524288);
                                                                if (!disableSamplingProfiler) {
                                                                    traceBeginAndSlog("StartSamplingProfilerService");
                                                                    try {
                                                                        ServiceManager.addService("samplingprofiler", new SamplingProfilerService(context2));
                                                                    } catch (Throwable e322222222222222) {
                                                                        reportWtf("starting SamplingProfiler Service", e322222222222222);
                                                                    }
                                                                    Trace.traceEnd(524288);
                                                                }
                                                                Slog.i(TAG, "attestation Service");
                                                                attestation = HwServiceFactory.getHwAttestationService();
                                                                if (attestation != null) {
                                                                    ServiceManager.addService("attestation_service", attestation.getInstance(context2));
                                                                }
                                                                traceBeginAndSlog("StartNetworkTimeUpdateService");
                                                                networkTimeUpdateService = new NetworkTimeUpdateService(context2);
                                                                ServiceManager.addService("network_time_update_service", networkTimeUpdateService);
                                                                networkTimeUpdateService2 = networkTimeUpdateService;
                                                                Trace.traceEnd(524288);
                                                                traceBeginAndSlog("StartCommonTimeManagementService");
                                                                commonTimeManagementService = new CommonTimeManagementService(context2);
                                                                ServiceManager.addService("commontime_management", commonTimeManagementService);
                                                                commonTimeManagementService2 = commonTimeManagementService;
                                                                Trace.traceEnd(524288);
                                                                if (!disableNetwork) {
                                                                    traceBeginAndSlog("CertBlacklister");
                                                                    try {
                                                                        certBlacklister = new CertBlacklister(context2);
                                                                    } catch (Throwable e3222222222222222) {
                                                                        reportWtf("starting CertBlacklister", e3222222222222222);
                                                                    }
                                                                    Trace.traceEnd(524288);
                                                                }
                                                                if (!disableNonCoreServices) {
                                                                    this.mSystemServiceManager.startService(DreamManagerService.class);
                                                                }
                                                                if (!disableNonCoreServices) {
                                                                    traceBeginAndSlog("StartAssetAtlasService");
                                                                    try {
                                                                        assetAtlasService2 = new AssetAtlasService(context2);
                                                                        try {
                                                                            ServiceManager.addService(AssetAtlasService.ASSET_ATLAS_SERVICE, assetAtlasService2);
                                                                            assetAtlasService = assetAtlasService2;
                                                                        } catch (Throwable th5) {
                                                                            e3222222222222222 = th5;
                                                                            assetAtlasService = assetAtlasService2;
                                                                            reportWtf("starting AssetAtlasService", e3222222222222222);
                                                                            Trace.traceEnd(524288);
                                                                            if (!disableNonCoreServices) {
                                                                                ServiceManager.addService(GraphicsStatsService.GRAPHICS_STATS_SERVICE, new GraphicsStatsService(context2));
                                                                            }
                                                                            if (this.mPackageManager.hasSystemFeature("android.software.print")) {
                                                                                this.mSystemServiceManager.startService(PRINT_MANAGER_SERVICE_CLASS);
                                                                            }
                                                                            this.mSystemServiceManager.startService(RestrictionsManagerService.class);
                                                                            this.mSystemServiceManager.startService(MediaSessionService.class);
                                                                            if (this.mPackageManager.hasSystemFeature("android.hardware.hdmi.cec")) {
                                                                                this.mSystemServiceManager.startService(HdmiControlService.class);
                                                                            }
                                                                            if (this.mPackageManager.hasSystemFeature("android.software.live_tv")) {
                                                                                this.mSystemServiceManager.startService(TvInputManagerService.class);
                                                                            }
                                                                            if (this.mPackageManager.hasSystemFeature("android.software.picture_in_picture")) {
                                                                                this.mSystemServiceManager.startService(MediaResourceMonitorService.class);
                                                                            }
                                                                            if (this.mPackageManager.hasSystemFeature("android.software.leanback")) {
                                                                                this.mSystemServiceManager.startService(TvRemoteService.class);
                                                                            }
                                                                            if (!disableNonCoreServices) {
                                                                                traceBeginAndSlog("StartMediaRouterService");
                                                                                try {
                                                                                    mediaRouterService2 = new MediaRouterService(context2);
                                                                                    try {
                                                                                        ServiceManager.addService("media_router", mediaRouterService2);
                                                                                        mediaRouterService = mediaRouterService2;
                                                                                    } catch (Throwable th6) {
                                                                                        e3222222222222222 = th6;
                                                                                        mediaRouterService = mediaRouterService2;
                                                                                        reportWtf("starting MediaRouterService", e3222222222222222);
                                                                                        Trace.traceEnd(524288);
                                                                                        if (!disableTrustManager) {
                                                                                            this.mSystemServiceManager.startService(TrustManagerService.class);
                                                                                        }
                                                                                        traceBeginAndSlog("StartBackgroundDexOptService");
                                                                                        BackgroundDexOptService.schedule(context2);
                                                                                        Trace.traceEnd(524288);
                                                                                        this.mSystemServiceManager.startService(ShortcutService.Lifecycle.class);
                                                                                        this.mSystemServiceManager.startService(LauncherAppsService.class);
                                                                                        this.mSystemServiceManager.startService(MediaProjectionManagerService.class);
                                                                                        if (!disableNonCoreServices) {
                                                                                            try {
                                                                                                this.mSystemServiceManager.startService("com.android.server.NonHardwareAcceleratedPackagesManagerService");
                                                                                            } catch (RuntimeException e6) {
                                                                                                Slog.e(TAG, e6.toString());
                                                                                            }
                                                                                        }
                                                                                        if (!disableNonCoreServices) {
                                                                                            jankshield = HwServiceFactory.getJankShieldService();
                                                                                            if (jankshield != null) {
                                                                                                ServiceManager.addService("jankshield", jankshield.getInstance(context2));
                                                                                            }
                                                                                        }
                                                                                        if (context2.getPackageManager().hasSystemFeature("android.hardware.type.watch")) {
                                                                                            this.mSystemServiceManager.startService(WEAR_BLUETOOTH_SERVICE_CLASS);
                                                                                        }
                                                                                        safeMode = windowManagerService.detectSafeMode();
                                                                                        this.mSystemServiceManager.setSafeMode(safeMode);
                                                                                        if (safeMode) {
                                                                                            this.mActivityManagerService.enterSafeMode();
                                                                                            VMRuntime.getRuntime().disableJitCompilation();
                                                                                        } else {
                                                                                            VMRuntime.getRuntime().startJitCompilation();
                                                                                        }
                                                                                        mmsService = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                                                                                        this.mSystemServiceManager.startService("com.android.server.HwCoreAppHelperService");
                                                                                        if ("true".equals(SystemProperties.get("bastet.service.enable", "false"))) {
                                                                                            try {
                                                                                                this.mSystemServiceManager.startService("com.android.server.HwBastetService");
                                                                                            } catch (Exception e7) {
                                                                                                Slog.w(TAG, "HwBastetService not exists.");
                                                                                            }
                                                                                        }
                                                                                        this.mSystemServiceManager.startService("com.android.server.HwPPTBService");
                                                                                        if ("true".equals(SystemProperties.get("ro.config.hw_emcom", "false"))) {
                                                                                            startEmcomService();
                                                                                        }
                                                                                        Trace.traceBegin(524288, "MakeVibratorServiceReady");
                                                                                        vibratorService.systemReady();
                                                                                        Trace.traceEnd(524288);
                                                                                        Trace.traceBegin(524288, "MakeLockSettingsServiceReady");
                                                                                        if (iLockSettings != null) {
                                                                                            try {
                                                                                                iLockSettings.systemReady();
                                                                                            } catch (Throwable e32222222222222222) {
                                                                                                reportWtf("making Lock Settings Service ready", e32222222222222222);
                                                                                            }
                                                                                        }
                                                                                        Trace.traceEnd(524288);
                                                                                        this.mSystemServiceManager.startBootPhase(SystemService.PHASE_LOCK_SETTINGS_READY);
                                                                                        this.mSystemServiceManager.startBootPhase(SystemService.PHASE_SYSTEM_SERVICES_READY);
                                                                                        Trace.traceBegin(524288, "MakeWindowManagerServiceReady");
                                                                                        windowManagerService.systemReady();
                                                                                        Trace.traceEnd(524288);
                                                                                        if (safeMode) {
                                                                                            this.mActivityManagerService.showSafeModeOverlay();
                                                                                        }
                                                                                        config = windowManagerService.computeNewConfiguration();
                                                                                        metrics = new DisplayMetrics();
                                                                                        ((WindowManager) context2.getSystemService("window")).getDefaultDisplay().getMetrics(metrics);
                                                                                        context2.getResources().updateConfiguration(config, metrics);
                                                                                        systemTheme = context2.getTheme();
                                                                                        if (systemTheme.getChangingConfigurations() != 0) {
                                                                                            systemTheme.rebase();
                                                                                        }
                                                                                        Trace.traceBegin(524288, "MakePowerManagerServiceReady");
                                                                                        this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                                                                                        Trace.traceEnd(524288);
                                                                                        Trace.traceEnd(524288);
                                                                                        this.mPGManagerService.systemReady(this.mActivityManagerService, this.mPowerManagerService, location);
                                                                                        Trace.traceBegin(524288, "MakePackageManagerServiceReady");
                                                                                        this.mPackageManagerService.systemReady();
                                                                                        Trace.traceEnd(524288);
                                                                                        Trace.traceBegin(524288, "MakeDisplayManagerServiceReady");
                                                                                        this.mDisplayManagerService.systemReady(safeMode, this.mOnlyCore);
                                                                                        Trace.traceEnd(524288);
                                                                                        networkManagementF = networkManagement;
                                                                                        networkStatsF = networkStats;
                                                                                        networkPolicyF = networkPolicy;
                                                                                        connectivityF = connectivity;
                                                                                        networkScoreF = networkScoreService2;
                                                                                        locationF = location;
                                                                                        countryDetectorF = countryDetectorService;
                                                                                        networkTimeUpdaterF = networkTimeUpdateService2;
                                                                                        commonTimeMgmtServiceF = commonTimeManagementService2;
                                                                                        atlasF = assetAtlasService;
                                                                                        inputManagerF = inputManager;
                                                                                        telephonyRegistryF = telephonyRegistry;
                                                                                        mediaRouterF = mediaRouterService;
                                                                                        mmsServiceF = mmsService;
                                                                                        context = context2;
                                                                                        this.mActivityManagerService.systemReady(new Runnable() {
                                                                                            public void run() {
                                                                                                Slog.i(SystemServer.TAG, "Making services ready");
                                                                                                SystemServer.this.mSystemServiceManager.startBootPhase(SystemService.PHASE_ACTIVITY_MANAGER_READY);
                                                                                                Trace.traceBegin(524288, "PhaseActivityManagerReady");
                                                                                                Trace.traceBegin(524288, "StartObservingNativeCrashes");
                                                                                                try {
                                                                                                    SystemServer.this.mActivityManagerService.startObservingNativeCrashes();
                                                                                                } catch (Throwable e) {
                                                                                                    SystemServer.this.reportWtf("observing native crashes", e);
                                                                                                }
                                                                                                Trace.traceEnd(524288);
                                                                                                if (!SystemServer.this.mOnlyCore) {
                                                                                                    Slog.i(SystemServer.TAG, "WebViewFactory preparation");
                                                                                                    Trace.traceBegin(524288, "WebViewFactoryPreparation");
                                                                                                    SystemServer.this.mWebViewUpdateService.prepareWebViewInSystemServer();
                                                                                                    Trace.traceEnd(524288);
                                                                                                }
                                                                                                try {
                                                                                                    Slog.i(SystemServer.TAG, "FingerprintService");
                                                                                                    Slog.i(SystemServer.TAG, "AuthenticationService");
                                                                                                    Intent authIntent = new Intent("com.huawei.securitymgr.AuthenticationService");
                                                                                                    authIntent.setPackage("com.huawei.securitymgr");
                                                                                                    context.startServiceAsUser(authIntent, UserHandle.OWNER);
                                                                                                    Slog.i(SystemServer.TAG, "fingerprint and securityMgr service start success");
                                                                                                } catch (Exception e2) {
                                                                                                    Slog.e(SystemServer.TAG, "can't start fingerprint or securityMgr service");
                                                                                                }
                                                                                                Trace.traceBegin(524288, "StartSystemUI");
                                                                                                try {
                                                                                                    SystemServer.startSystemUi(context);
                                                                                                } catch (Throwable e3) {
                                                                                                    SystemServer.this.reportWtf("starting System UI", e3);
                                                                                                }
                                                                                                Trace.traceEnd(524288);
                                                                                                try {
                                                                                                    HwFrameworkFactory.getAudioEffectLowPowerTask(context);
                                                                                                } catch (Throwable e32) {
                                                                                                    Slog.e(SystemServer.TAG, "AudioEffectLowPowerTask occure error:", e32);
                                                                                                }
                                                                                                Trace.traceBegin(524288, "MakeNetworkScoreReady");
                                                                                                try {
                                                                                                    if (networkScoreF != null) {
                                                                                                        networkScoreF.systemReady();
                                                                                                    }
                                                                                                } catch (Throwable e322) {
                                                                                                    SystemServer.this.reportWtf("making Network Score Service ready", e322);
                                                                                                }
                                                                                                Trace.traceEnd(524288);
                                                                                                Trace.traceBegin(524288, "MakeNetworkManagementServiceReady");
                                                                                                try {
                                                                                                    if (networkManagementF != null) {
                                                                                                        networkManagementF.systemReady();
                                                                                                    }
                                                                                                } catch (Throwable e3222) {
                                                                                                    SystemServer.this.reportWtf("making Network Managment Service ready", e3222);
                                                                                                }
                                                                                                Trace.traceEnd(524288);
                                                                                                Trace.traceBegin(524288, "MakeNetworkStatsServiceReady");
                                                                                                try {
                                                                                                    if (networkStatsF != null) {
                                                                                                        networkStatsF.systemReady();
                                                                                                    }
                                                                                                } catch (Throwable e32222) {
                                                                                                    SystemServer.this.reportWtf("making Network Stats Service ready", e32222);
                                                                                                }
                                                                                                Trace.traceEnd(524288);
                                                                                                Trace.traceBegin(524288, "MakeNetworkPolicyServiceReady");
                                                                                                try {
                                                                                                    if (networkPolicyF != null) {
                                                                                                        networkPolicyF.systemReady();
                                                                                                    }
                                                                                                } catch (Throwable e322222) {
                                                                                                    SystemServer.this.reportWtf("making Network Policy Service ready", e322222);
                                                                                                }
                                                                                                Trace.traceEnd(524288);
                                                                                                Trace.traceBegin(524288, "MakeConnectivityServiceReady");
                                                                                                try {
                                                                                                    if (connectivityF != null) {
                                                                                                        connectivityF.systemReady();
                                                                                                    }
                                                                                                } catch (Throwable e3222222) {
                                                                                                    SystemServer.this.reportWtf("making Connectivity Service ready", e3222222);
                                                                                                }
                                                                                                Trace.traceEnd(524288);
                                                                                                Watchdog.getInstance().start();
                                                                                                Trace.traceEnd(524288);
                                                                                                Trace.traceBegin(524288, "PhaseThirdPartyAppsCanStart");
                                                                                                SystemServer.this.mSystemServiceManager.startBootPhase(600);
                                                                                                try {
                                                                                                    if (locationF != null) {
                                                                                                        locationF.systemRunning();
                                                                                                    }
                                                                                                } catch (Throwable e32222222) {
                                                                                                    SystemServer.this.reportWtf("Notifying Location Service running", e32222222);
                                                                                                }
                                                                                                try {
                                                                                                    if (countryDetectorF != null) {
                                                                                                        countryDetectorF.systemRunning();
                                                                                                    }
                                                                                                } catch (Throwable e322222222) {
                                                                                                    SystemServer.this.reportWtf("Notifying CountryDetectorService running", e322222222);
                                                                                                }
                                                                                                try {
                                                                                                    if (networkTimeUpdaterF != null) {
                                                                                                        networkTimeUpdaterF.systemRunning();
                                                                                                    }
                                                                                                } catch (Throwable e3222222222) {
                                                                                                    SystemServer.this.reportWtf("Notifying NetworkTimeService running", e3222222222);
                                                                                                }
                                                                                                try {
                                                                                                    if (commonTimeMgmtServiceF != null) {
                                                                                                        commonTimeMgmtServiceF.systemRunning();
                                                                                                    }
                                                                                                } catch (Throwable e32222222222) {
                                                                                                    SystemServer.this.reportWtf("Notifying CommonTimeManagementService running", e32222222222);
                                                                                                }
                                                                                                try {
                                                                                                    if (atlasF != null) {
                                                                                                        atlasF.systemRunning();
                                                                                                    }
                                                                                                } catch (Throwable e322222222222) {
                                                                                                    SystemServer.this.reportWtf("Notifying AssetAtlasService running", e322222222222);
                                                                                                }
                                                                                                try {
                                                                                                    if (inputManagerF != null) {
                                                                                                        inputManagerF.systemRunning();
                                                                                                    }
                                                                                                } catch (Throwable e3222222222222) {
                                                                                                    SystemServer.this.reportWtf("Notifying InputManagerService running", e3222222222222);
                                                                                                }
                                                                                                try {
                                                                                                    if (telephonyRegistryF != null) {
                                                                                                        telephonyRegistryF.systemRunning();
                                                                                                    }
                                                                                                } catch (Throwable e32222222222222) {
                                                                                                    SystemServer.this.reportWtf("Notifying TelephonyRegistry running", e32222222222222);
                                                                                                }
                                                                                                try {
                                                                                                    if (mediaRouterF != null) {
                                                                                                        mediaRouterF.systemRunning();
                                                                                                    }
                                                                                                } catch (Throwable e322222222222222) {
                                                                                                    SystemServer.this.reportWtf("Notifying MediaRouterService running", e322222222222222);
                                                                                                }
                                                                                                if ("true".equals(SystemProperties.get("ro.poweroff_alarm", "true"))) {
                                                                                                    SystemServer.this.setAirplaneMode(context);
                                                                                                }
                                                                                                try {
                                                                                                    if (mmsService != null) {
                                                                                                        mmsService.systemRunning();
                                                                                                    }
                                                                                                } catch (Throwable e3222222222222222) {
                                                                                                    SystemServer.this.reportWtf("Notifying MmsService running", e3222222222222222);
                                                                                                }
                                                                                                try {
                                                                                                    if (networkScoreF != null) {
                                                                                                        networkScoreF.systemRunning();
                                                                                                    }
                                                                                                } catch (Throwable e32222222222222222) {
                                                                                                    SystemServer.this.reportWtf("Notifying NetworkScoreService running", e32222222222222222);
                                                                                                }
                                                                                                Trace.traceEnd(524288);
                                                                                                try {
                                                                                                    if (enableIaware) {
                                                                                                        ServiceManager.addService("multi_task", HwServiceFactory.getMultiTaskManagerService().getInstance(context));
                                                                                                    } else {
                                                                                                        Slog.e(SystemServer.TAG, "can not start multitask because the prop is false");
                                                                                                    }
                                                                                                } catch (Throwable e322222222222222222) {
                                                                                                    SystemServer.this.reportWtf("starting MultiTaskManagerService", e322222222222222222);
                                                                                                }
                                                                                                HwServiceFactory.addHwFmService(context);
                                                                                                MagnBracketObserver.getInstance(SystemServer.this.mSystemContext);
                                                                                            }
                                                                                        });
                                                                                    }
                                                                                } catch (Throwable th7) {
                                                                                    e32222222222222222 = th7;
                                                                                    reportWtf("starting MediaRouterService", e32222222222222222);
                                                                                    Trace.traceEnd(524288);
                                                                                    if (disableTrustManager) {
                                                                                        this.mSystemServiceManager.startService(TrustManagerService.class);
                                                                                    }
                                                                                    traceBeginAndSlog("StartBackgroundDexOptService");
                                                                                    BackgroundDexOptService.schedule(context2);
                                                                                    Trace.traceEnd(524288);
                                                                                    this.mSystemServiceManager.startService(ShortcutService.Lifecycle.class);
                                                                                    this.mSystemServiceManager.startService(LauncherAppsService.class);
                                                                                    this.mSystemServiceManager.startService(MediaProjectionManagerService.class);
                                                                                    if (disableNonCoreServices) {
                                                                                        this.mSystemServiceManager.startService("com.android.server.NonHardwareAcceleratedPackagesManagerService");
                                                                                    }
                                                                                    if (disableNonCoreServices) {
                                                                                        jankshield = HwServiceFactory.getJankShieldService();
                                                                                        if (jankshield != null) {
                                                                                            ServiceManager.addService("jankshield", jankshield.getInstance(context2));
                                                                                        }
                                                                                    }
                                                                                    if (context2.getPackageManager().hasSystemFeature("android.hardware.type.watch")) {
                                                                                        this.mSystemServiceManager.startService(WEAR_BLUETOOTH_SERVICE_CLASS);
                                                                                    }
                                                                                    safeMode = windowManagerService.detectSafeMode();
                                                                                    this.mSystemServiceManager.setSafeMode(safeMode);
                                                                                    if (safeMode) {
                                                                                        VMRuntime.getRuntime().startJitCompilation();
                                                                                    } else {
                                                                                        this.mActivityManagerService.enterSafeMode();
                                                                                        VMRuntime.getRuntime().disableJitCompilation();
                                                                                    }
                                                                                    mmsService = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                                                                                    this.mSystemServiceManager.startService("com.android.server.HwCoreAppHelperService");
                                                                                    if ("true".equals(SystemProperties.get("bastet.service.enable", "false"))) {
                                                                                        this.mSystemServiceManager.startService("com.android.server.HwBastetService");
                                                                                    }
                                                                                    this.mSystemServiceManager.startService("com.android.server.HwPPTBService");
                                                                                    if ("true".equals(SystemProperties.get("ro.config.hw_emcom", "false"))) {
                                                                                        startEmcomService();
                                                                                    }
                                                                                    Trace.traceBegin(524288, "MakeVibratorServiceReady");
                                                                                    vibratorService.systemReady();
                                                                                    Trace.traceEnd(524288);
                                                                                    Trace.traceBegin(524288, "MakeLockSettingsServiceReady");
                                                                                    if (iLockSettings != null) {
                                                                                        iLockSettings.systemReady();
                                                                                    }
                                                                                    Trace.traceEnd(524288);
                                                                                    this.mSystemServiceManager.startBootPhase(SystemService.PHASE_LOCK_SETTINGS_READY);
                                                                                    this.mSystemServiceManager.startBootPhase(SystemService.PHASE_SYSTEM_SERVICES_READY);
                                                                                    Trace.traceBegin(524288, "MakeWindowManagerServiceReady");
                                                                                    windowManagerService.systemReady();
                                                                                    Trace.traceEnd(524288);
                                                                                    if (safeMode) {
                                                                                        this.mActivityManagerService.showSafeModeOverlay();
                                                                                    }
                                                                                    config = windowManagerService.computeNewConfiguration();
                                                                                    metrics = new DisplayMetrics();
                                                                                    ((WindowManager) context2.getSystemService("window")).getDefaultDisplay().getMetrics(metrics);
                                                                                    context2.getResources().updateConfiguration(config, metrics);
                                                                                    systemTheme = context2.getTheme();
                                                                                    if (systemTheme.getChangingConfigurations() != 0) {
                                                                                        systemTheme.rebase();
                                                                                    }
                                                                                    Trace.traceBegin(524288, "MakePowerManagerServiceReady");
                                                                                    this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                                                                                    Trace.traceEnd(524288);
                                                                                    Trace.traceEnd(524288);
                                                                                    this.mPGManagerService.systemReady(this.mActivityManagerService, this.mPowerManagerService, location);
                                                                                    Trace.traceBegin(524288, "MakePackageManagerServiceReady");
                                                                                    this.mPackageManagerService.systemReady();
                                                                                    Trace.traceEnd(524288);
                                                                                    Trace.traceBegin(524288, "MakeDisplayManagerServiceReady");
                                                                                    this.mDisplayManagerService.systemReady(safeMode, this.mOnlyCore);
                                                                                    Trace.traceEnd(524288);
                                                                                    networkManagementF = networkManagement;
                                                                                    networkStatsF = networkStats;
                                                                                    networkPolicyF = networkPolicy;
                                                                                    connectivityF = connectivity;
                                                                                    networkScoreF = networkScoreService2;
                                                                                    locationF = location;
                                                                                    countryDetectorF = countryDetectorService;
                                                                                    networkTimeUpdaterF = networkTimeUpdateService2;
                                                                                    commonTimeMgmtServiceF = commonTimeManagementService2;
                                                                                    atlasF = assetAtlasService;
                                                                                    inputManagerF = inputManager;
                                                                                    telephonyRegistryF = telephonyRegistry;
                                                                                    mediaRouterF = mediaRouterService;
                                                                                    mmsServiceF = mmsService;
                                                                                    context = context2;
                                                                                    this.mActivityManagerService.systemReady(/* anonymous class already generated */);
                                                                                }
                                                                                Trace.traceEnd(524288);
                                                                                if (disableTrustManager) {
                                                                                    this.mSystemServiceManager.startService(TrustManagerService.class);
                                                                                }
                                                                                traceBeginAndSlog("StartBackgroundDexOptService");
                                                                                try {
                                                                                    BackgroundDexOptService.schedule(context2);
                                                                                } catch (Throwable e322222222222222222) {
                                                                                    reportWtf("starting BackgroundDexOptService", e322222222222222222);
                                                                                }
                                                                                Trace.traceEnd(524288);
                                                                            }
                                                                            this.mSystemServiceManager.startService(ShortcutService.Lifecycle.class);
                                                                            this.mSystemServiceManager.startService(LauncherAppsService.class);
                                                                            this.mSystemServiceManager.startService(MediaProjectionManagerService.class);
                                                                            if (disableNonCoreServices) {
                                                                                this.mSystemServiceManager.startService("com.android.server.NonHardwareAcceleratedPackagesManagerService");
                                                                            }
                                                                            if (disableNonCoreServices) {
                                                                                jankshield = HwServiceFactory.getJankShieldService();
                                                                                if (jankshield != null) {
                                                                                    ServiceManager.addService("jankshield", jankshield.getInstance(context2));
                                                                                }
                                                                            }
                                                                            if (context2.getPackageManager().hasSystemFeature("android.hardware.type.watch")) {
                                                                                this.mSystemServiceManager.startService(WEAR_BLUETOOTH_SERVICE_CLASS);
                                                                            }
                                                                            safeMode = windowManagerService.detectSafeMode();
                                                                            this.mSystemServiceManager.setSafeMode(safeMode);
                                                                            if (safeMode) {
                                                                                VMRuntime.getRuntime().startJitCompilation();
                                                                            } else {
                                                                                this.mActivityManagerService.enterSafeMode();
                                                                                VMRuntime.getRuntime().disableJitCompilation();
                                                                            }
                                                                            mmsService = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                                                                            this.mSystemServiceManager.startService("com.android.server.HwCoreAppHelperService");
                                                                            if ("true".equals(SystemProperties.get("bastet.service.enable", "false"))) {
                                                                                this.mSystemServiceManager.startService("com.android.server.HwBastetService");
                                                                            }
                                                                            this.mSystemServiceManager.startService("com.android.server.HwPPTBService");
                                                                            if ("true".equals(SystemProperties.get("ro.config.hw_emcom", "false"))) {
                                                                                startEmcomService();
                                                                            }
                                                                            Trace.traceBegin(524288, "MakeVibratorServiceReady");
                                                                            vibratorService.systemReady();
                                                                            Trace.traceEnd(524288);
                                                                            Trace.traceBegin(524288, "MakeLockSettingsServiceReady");
                                                                            if (iLockSettings != null) {
                                                                                iLockSettings.systemReady();
                                                                            }
                                                                            Trace.traceEnd(524288);
                                                                            this.mSystemServiceManager.startBootPhase(SystemService.PHASE_LOCK_SETTINGS_READY);
                                                                            this.mSystemServiceManager.startBootPhase(SystemService.PHASE_SYSTEM_SERVICES_READY);
                                                                            Trace.traceBegin(524288, "MakeWindowManagerServiceReady");
                                                                            windowManagerService.systemReady();
                                                                            Trace.traceEnd(524288);
                                                                            if (safeMode) {
                                                                                this.mActivityManagerService.showSafeModeOverlay();
                                                                            }
                                                                            config = windowManagerService.computeNewConfiguration();
                                                                            metrics = new DisplayMetrics();
                                                                            ((WindowManager) context2.getSystemService("window")).getDefaultDisplay().getMetrics(metrics);
                                                                            context2.getResources().updateConfiguration(config, metrics);
                                                                            systemTheme = context2.getTheme();
                                                                            if (systemTheme.getChangingConfigurations() != 0) {
                                                                                systemTheme.rebase();
                                                                            }
                                                                            Trace.traceBegin(524288, "MakePowerManagerServiceReady");
                                                                            this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                                                                            Trace.traceEnd(524288);
                                                                            Trace.traceEnd(524288);
                                                                            this.mPGManagerService.systemReady(this.mActivityManagerService, this.mPowerManagerService, location);
                                                                            Trace.traceBegin(524288, "MakePackageManagerServiceReady");
                                                                            this.mPackageManagerService.systemReady();
                                                                            Trace.traceEnd(524288);
                                                                            Trace.traceBegin(524288, "MakeDisplayManagerServiceReady");
                                                                            this.mDisplayManagerService.systemReady(safeMode, this.mOnlyCore);
                                                                            Trace.traceEnd(524288);
                                                                            networkManagementF = networkManagement;
                                                                            networkStatsF = networkStats;
                                                                            networkPolicyF = networkPolicy;
                                                                            connectivityF = connectivity;
                                                                            networkScoreF = networkScoreService2;
                                                                            locationF = location;
                                                                            countryDetectorF = countryDetectorService;
                                                                            networkTimeUpdaterF = networkTimeUpdateService2;
                                                                            commonTimeMgmtServiceF = commonTimeManagementService2;
                                                                            atlasF = assetAtlasService;
                                                                            inputManagerF = inputManager;
                                                                            telephonyRegistryF = telephonyRegistry;
                                                                            mediaRouterF = mediaRouterService;
                                                                            mmsServiceF = mmsService;
                                                                            context = context2;
                                                                            this.mActivityManagerService.systemReady(/* anonymous class already generated */);
                                                                        }
                                                                    } catch (Throwable th8) {
                                                                        e322222222222222222 = th8;
                                                                        reportWtf("starting AssetAtlasService", e322222222222222222);
                                                                        Trace.traceEnd(524288);
                                                                        if (disableNonCoreServices) {
                                                                            ServiceManager.addService(GraphicsStatsService.GRAPHICS_STATS_SERVICE, new GraphicsStatsService(context2));
                                                                        }
                                                                        if (this.mPackageManager.hasSystemFeature("android.software.print")) {
                                                                            this.mSystemServiceManager.startService(PRINT_MANAGER_SERVICE_CLASS);
                                                                        }
                                                                        this.mSystemServiceManager.startService(RestrictionsManagerService.class);
                                                                        this.mSystemServiceManager.startService(MediaSessionService.class);
                                                                        if (this.mPackageManager.hasSystemFeature("android.hardware.hdmi.cec")) {
                                                                            this.mSystemServiceManager.startService(HdmiControlService.class);
                                                                        }
                                                                        if (this.mPackageManager.hasSystemFeature("android.software.live_tv")) {
                                                                            this.mSystemServiceManager.startService(TvInputManagerService.class);
                                                                        }
                                                                        if (this.mPackageManager.hasSystemFeature("android.software.picture_in_picture")) {
                                                                            this.mSystemServiceManager.startService(MediaResourceMonitorService.class);
                                                                        }
                                                                        if (this.mPackageManager.hasSystemFeature("android.software.leanback")) {
                                                                            this.mSystemServiceManager.startService(TvRemoteService.class);
                                                                        }
                                                                        if (disableNonCoreServices) {
                                                                            traceBeginAndSlog("StartMediaRouterService");
                                                                            mediaRouterService2 = new MediaRouterService(context2);
                                                                            ServiceManager.addService("media_router", mediaRouterService2);
                                                                            mediaRouterService = mediaRouterService2;
                                                                            Trace.traceEnd(524288);
                                                                            if (disableTrustManager) {
                                                                                this.mSystemServiceManager.startService(TrustManagerService.class);
                                                                            }
                                                                            traceBeginAndSlog("StartBackgroundDexOptService");
                                                                            BackgroundDexOptService.schedule(context2);
                                                                            Trace.traceEnd(524288);
                                                                        }
                                                                        this.mSystemServiceManager.startService(ShortcutService.Lifecycle.class);
                                                                        this.mSystemServiceManager.startService(LauncherAppsService.class);
                                                                        this.mSystemServiceManager.startService(MediaProjectionManagerService.class);
                                                                        if (disableNonCoreServices) {
                                                                            this.mSystemServiceManager.startService("com.android.server.NonHardwareAcceleratedPackagesManagerService");
                                                                        }
                                                                        if (disableNonCoreServices) {
                                                                            jankshield = HwServiceFactory.getJankShieldService();
                                                                            if (jankshield != null) {
                                                                                ServiceManager.addService("jankshield", jankshield.getInstance(context2));
                                                                            }
                                                                        }
                                                                        if (context2.getPackageManager().hasSystemFeature("android.hardware.type.watch")) {
                                                                            this.mSystemServiceManager.startService(WEAR_BLUETOOTH_SERVICE_CLASS);
                                                                        }
                                                                        safeMode = windowManagerService.detectSafeMode();
                                                                        this.mSystemServiceManager.setSafeMode(safeMode);
                                                                        if (safeMode) {
                                                                            this.mActivityManagerService.enterSafeMode();
                                                                            VMRuntime.getRuntime().disableJitCompilation();
                                                                        } else {
                                                                            VMRuntime.getRuntime().startJitCompilation();
                                                                        }
                                                                        mmsService = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                                                                        this.mSystemServiceManager.startService("com.android.server.HwCoreAppHelperService");
                                                                        if ("true".equals(SystemProperties.get("bastet.service.enable", "false"))) {
                                                                            this.mSystemServiceManager.startService("com.android.server.HwBastetService");
                                                                        }
                                                                        this.mSystemServiceManager.startService("com.android.server.HwPPTBService");
                                                                        if ("true".equals(SystemProperties.get("ro.config.hw_emcom", "false"))) {
                                                                            startEmcomService();
                                                                        }
                                                                        Trace.traceBegin(524288, "MakeVibratorServiceReady");
                                                                        vibratorService.systemReady();
                                                                        Trace.traceEnd(524288);
                                                                        Trace.traceBegin(524288, "MakeLockSettingsServiceReady");
                                                                        if (iLockSettings != null) {
                                                                            iLockSettings.systemReady();
                                                                        }
                                                                        Trace.traceEnd(524288);
                                                                        this.mSystemServiceManager.startBootPhase(SystemService.PHASE_LOCK_SETTINGS_READY);
                                                                        this.mSystemServiceManager.startBootPhase(SystemService.PHASE_SYSTEM_SERVICES_READY);
                                                                        Trace.traceBegin(524288, "MakeWindowManagerServiceReady");
                                                                        windowManagerService.systemReady();
                                                                        Trace.traceEnd(524288);
                                                                        if (safeMode) {
                                                                            this.mActivityManagerService.showSafeModeOverlay();
                                                                        }
                                                                        config = windowManagerService.computeNewConfiguration();
                                                                        metrics = new DisplayMetrics();
                                                                        ((WindowManager) context2.getSystemService("window")).getDefaultDisplay().getMetrics(metrics);
                                                                        context2.getResources().updateConfiguration(config, metrics);
                                                                        systemTheme = context2.getTheme();
                                                                        if (systemTheme.getChangingConfigurations() != 0) {
                                                                            systemTheme.rebase();
                                                                        }
                                                                        Trace.traceBegin(524288, "MakePowerManagerServiceReady");
                                                                        this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                                                                        Trace.traceEnd(524288);
                                                                        Trace.traceEnd(524288);
                                                                        this.mPGManagerService.systemReady(this.mActivityManagerService, this.mPowerManagerService, location);
                                                                        Trace.traceBegin(524288, "MakePackageManagerServiceReady");
                                                                        this.mPackageManagerService.systemReady();
                                                                        Trace.traceEnd(524288);
                                                                        Trace.traceBegin(524288, "MakeDisplayManagerServiceReady");
                                                                        this.mDisplayManagerService.systemReady(safeMode, this.mOnlyCore);
                                                                        Trace.traceEnd(524288);
                                                                        networkManagementF = networkManagement;
                                                                        networkStatsF = networkStats;
                                                                        networkPolicyF = networkPolicy;
                                                                        connectivityF = connectivity;
                                                                        networkScoreF = networkScoreService2;
                                                                        locationF = location;
                                                                        countryDetectorF = countryDetectorService;
                                                                        networkTimeUpdaterF = networkTimeUpdateService2;
                                                                        commonTimeMgmtServiceF = commonTimeManagementService2;
                                                                        atlasF = assetAtlasService;
                                                                        inputManagerF = inputManager;
                                                                        telephonyRegistryF = telephonyRegistry;
                                                                        mediaRouterF = mediaRouterService;
                                                                        mmsServiceF = mmsService;
                                                                        context = context2;
                                                                        this.mActivityManagerService.systemReady(/* anonymous class already generated */);
                                                                    }
                                                                    Trace.traceEnd(524288);
                                                                }
                                                                if (disableNonCoreServices) {
                                                                    ServiceManager.addService(GraphicsStatsService.GRAPHICS_STATS_SERVICE, new GraphicsStatsService(context2));
                                                                }
                                                                if (this.mPackageManager.hasSystemFeature("android.software.print")) {
                                                                    this.mSystemServiceManager.startService(PRINT_MANAGER_SERVICE_CLASS);
                                                                }
                                                                this.mSystemServiceManager.startService(RestrictionsManagerService.class);
                                                                this.mSystemServiceManager.startService(MediaSessionService.class);
                                                                if (this.mPackageManager.hasSystemFeature("android.hardware.hdmi.cec")) {
                                                                    this.mSystemServiceManager.startService(HdmiControlService.class);
                                                                }
                                                                if (this.mPackageManager.hasSystemFeature("android.software.live_tv")) {
                                                                    this.mSystemServiceManager.startService(TvInputManagerService.class);
                                                                }
                                                                if (this.mPackageManager.hasSystemFeature("android.software.picture_in_picture")) {
                                                                    this.mSystemServiceManager.startService(MediaResourceMonitorService.class);
                                                                }
                                                                if (this.mPackageManager.hasSystemFeature("android.software.leanback")) {
                                                                    this.mSystemServiceManager.startService(TvRemoteService.class);
                                                                }
                                                                if (disableNonCoreServices) {
                                                                    traceBeginAndSlog("StartMediaRouterService");
                                                                    mediaRouterService2 = new MediaRouterService(context2);
                                                                    ServiceManager.addService("media_router", mediaRouterService2);
                                                                    mediaRouterService = mediaRouterService2;
                                                                    Trace.traceEnd(524288);
                                                                    if (disableTrustManager) {
                                                                        this.mSystemServiceManager.startService(TrustManagerService.class);
                                                                    }
                                                                    traceBeginAndSlog("StartBackgroundDexOptService");
                                                                    BackgroundDexOptService.schedule(context2);
                                                                    Trace.traceEnd(524288);
                                                                }
                                                                this.mSystemServiceManager.startService(ShortcutService.Lifecycle.class);
                                                                this.mSystemServiceManager.startService(LauncherAppsService.class);
                                                                this.mSystemServiceManager.startService(MediaProjectionManagerService.class);
                                                                if (disableNonCoreServices) {
                                                                    this.mSystemServiceManager.startService("com.android.server.NonHardwareAcceleratedPackagesManagerService");
                                                                }
                                                                if (disableNonCoreServices) {
                                                                    jankshield = HwServiceFactory.getJankShieldService();
                                                                    if (jankshield != null) {
                                                                        ServiceManager.addService("jankshield", jankshield.getInstance(context2));
                                                                    }
                                                                }
                                                                if (context2.getPackageManager().hasSystemFeature("android.hardware.type.watch")) {
                                                                    this.mSystemServiceManager.startService(WEAR_BLUETOOTH_SERVICE_CLASS);
                                                                }
                                                                safeMode = windowManagerService.detectSafeMode();
                                                                this.mSystemServiceManager.setSafeMode(safeMode);
                                                                if (safeMode) {
                                                                    this.mActivityManagerService.enterSafeMode();
                                                                    VMRuntime.getRuntime().disableJitCompilation();
                                                                } else {
                                                                    VMRuntime.getRuntime().startJitCompilation();
                                                                }
                                                                mmsService = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                                                                this.mSystemServiceManager.startService("com.android.server.HwCoreAppHelperService");
                                                                if ("true".equals(SystemProperties.get("bastet.service.enable", "false"))) {
                                                                    this.mSystemServiceManager.startService("com.android.server.HwBastetService");
                                                                }
                                                                this.mSystemServiceManager.startService("com.android.server.HwPPTBService");
                                                                if ("true".equals(SystemProperties.get("ro.config.hw_emcom", "false"))) {
                                                                    startEmcomService();
                                                                }
                                                                Trace.traceBegin(524288, "MakeVibratorServiceReady");
                                                                vibratorService.systemReady();
                                                                Trace.traceEnd(524288);
                                                                Trace.traceBegin(524288, "MakeLockSettingsServiceReady");
                                                                if (iLockSettings != null) {
                                                                    iLockSettings.systemReady();
                                                                }
                                                                Trace.traceEnd(524288);
                                                                this.mSystemServiceManager.startBootPhase(SystemService.PHASE_LOCK_SETTINGS_READY);
                                                                this.mSystemServiceManager.startBootPhase(SystemService.PHASE_SYSTEM_SERVICES_READY);
                                                                Trace.traceBegin(524288, "MakeWindowManagerServiceReady");
                                                                windowManagerService.systemReady();
                                                                Trace.traceEnd(524288);
                                                                if (safeMode) {
                                                                    this.mActivityManagerService.showSafeModeOverlay();
                                                                }
                                                                config = windowManagerService.computeNewConfiguration();
                                                                metrics = new DisplayMetrics();
                                                                ((WindowManager) context2.getSystemService("window")).getDefaultDisplay().getMetrics(metrics);
                                                                context2.getResources().updateConfiguration(config, metrics);
                                                                systemTheme = context2.getTheme();
                                                                if (systemTheme.getChangingConfigurations() != 0) {
                                                                    systemTheme.rebase();
                                                                }
                                                                Trace.traceBegin(524288, "MakePowerManagerServiceReady");
                                                                this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                                                                Trace.traceEnd(524288);
                                                                Trace.traceEnd(524288);
                                                                this.mPGManagerService.systemReady(this.mActivityManagerService, this.mPowerManagerService, location);
                                                                Trace.traceBegin(524288, "MakePackageManagerServiceReady");
                                                                this.mPackageManagerService.systemReady();
                                                                Trace.traceEnd(524288);
                                                                Trace.traceBegin(524288, "MakeDisplayManagerServiceReady");
                                                                this.mDisplayManagerService.systemReady(safeMode, this.mOnlyCore);
                                                                Trace.traceEnd(524288);
                                                                networkManagementF = networkManagement;
                                                                networkStatsF = networkStats;
                                                                networkPolicyF = networkPolicy;
                                                                connectivityF = connectivity;
                                                                networkScoreF = networkScoreService2;
                                                                locationF = location;
                                                                countryDetectorF = countryDetectorService;
                                                                networkTimeUpdaterF = networkTimeUpdateService2;
                                                                commonTimeMgmtServiceF = commonTimeManagementService2;
                                                                atlasF = assetAtlasService;
                                                                inputManagerF = inputManager;
                                                                telephonyRegistryF = telephonyRegistry;
                                                                mediaRouterF = mediaRouterService;
                                                                mmsServiceF = mmsService;
                                                                context = context2;
                                                                this.mActivityManagerService.systemReady(/* anonymous class already generated */);
                                                            }
                                                            Trace.traceEnd(524288);
                                                            this.mSystemServiceManager.startService(TwilightService.class);
                                                            this.mSystemServiceManager.startService("com.android.server.job.HwJobSchedulerService");
                                                            this.mSystemServiceManager.startService(SoundTriggerService.class);
                                                            if (disableNonCoreServices) {
                                                                if (this.mPackageManager.hasSystemFeature("android.software.backup")) {
                                                                    this.mSystemServiceManager.startService(BACKUP_MANAGER_SERVICE_CLASS);
                                                                }
                                                                this.mSystemServiceManager.startService(APPWIDGET_SERVICE_CLASS);
                                                                if (this.mPackageManager.hasSystemFeature("android.software.voice_recognizers")) {
                                                                    this.mSystemServiceManager.startService(VOICE_RECOGNITION_MANAGER_SERVICE_CLASS);
                                                                }
                                                                if (GestureLauncherService.isGestureLauncherEnabled(context2.getResources())) {
                                                                    Slog.i(TAG, "Gesture Launcher Service");
                                                                    this.mSystemServiceManager.startService(GestureLauncherService.class);
                                                                }
                                                                this.mSystemServiceManager.startService(SensorNotificationService.class);
                                                                this.mSystemServiceManager.startService(ContextHubSystemService.class);
                                                            }
                                                            HwServiceFactory.setupHwServices(context2);
                                                            traceBeginAndSlog("StartDiskStatsService");
                                                            ServiceManager.addService("diskstats", new DiskStatsService(context2));
                                                            Trace.traceEnd(524288);
                                                            if (disableSamplingProfiler) {
                                                                traceBeginAndSlog("StartSamplingProfilerService");
                                                                ServiceManager.addService("samplingprofiler", new SamplingProfilerService(context2));
                                                                Trace.traceEnd(524288);
                                                            }
                                                            Slog.i(TAG, "attestation Service");
                                                            attestation = HwServiceFactory.getHwAttestationService();
                                                            if (attestation != null) {
                                                                ServiceManager.addService("attestation_service", attestation.getInstance(context2));
                                                            }
                                                            traceBeginAndSlog("StartNetworkTimeUpdateService");
                                                            networkTimeUpdateService = new NetworkTimeUpdateService(context2);
                                                            ServiceManager.addService("network_time_update_service", networkTimeUpdateService);
                                                            networkTimeUpdateService2 = networkTimeUpdateService;
                                                            Trace.traceEnd(524288);
                                                            traceBeginAndSlog("StartCommonTimeManagementService");
                                                            commonTimeManagementService = new CommonTimeManagementService(context2);
                                                            ServiceManager.addService("commontime_management", commonTimeManagementService);
                                                            commonTimeManagementService2 = commonTimeManagementService;
                                                            Trace.traceEnd(524288);
                                                            if (disableNetwork) {
                                                                traceBeginAndSlog("CertBlacklister");
                                                                certBlacklister = new CertBlacklister(context2);
                                                                Trace.traceEnd(524288);
                                                            }
                                                            if (disableNonCoreServices) {
                                                                this.mSystemServiceManager.startService(DreamManagerService.class);
                                                            }
                                                            if (disableNonCoreServices) {
                                                                traceBeginAndSlog("StartAssetAtlasService");
                                                                assetAtlasService2 = new AssetAtlasService(context2);
                                                                ServiceManager.addService(AssetAtlasService.ASSET_ATLAS_SERVICE, assetAtlasService2);
                                                                assetAtlasService = assetAtlasService2;
                                                                Trace.traceEnd(524288);
                                                            }
                                                            if (disableNonCoreServices) {
                                                                ServiceManager.addService(GraphicsStatsService.GRAPHICS_STATS_SERVICE, new GraphicsStatsService(context2));
                                                            }
                                                            if (this.mPackageManager.hasSystemFeature("android.software.print")) {
                                                                this.mSystemServiceManager.startService(PRINT_MANAGER_SERVICE_CLASS);
                                                            }
                                                            this.mSystemServiceManager.startService(RestrictionsManagerService.class);
                                                            this.mSystemServiceManager.startService(MediaSessionService.class);
                                                            if (this.mPackageManager.hasSystemFeature("android.hardware.hdmi.cec")) {
                                                                this.mSystemServiceManager.startService(HdmiControlService.class);
                                                            }
                                                            if (this.mPackageManager.hasSystemFeature("android.software.live_tv")) {
                                                                this.mSystemServiceManager.startService(TvInputManagerService.class);
                                                            }
                                                            if (this.mPackageManager.hasSystemFeature("android.software.picture_in_picture")) {
                                                                this.mSystemServiceManager.startService(MediaResourceMonitorService.class);
                                                            }
                                                            if (this.mPackageManager.hasSystemFeature("android.software.leanback")) {
                                                                this.mSystemServiceManager.startService(TvRemoteService.class);
                                                            }
                                                            if (disableNonCoreServices) {
                                                                traceBeginAndSlog("StartMediaRouterService");
                                                                mediaRouterService2 = new MediaRouterService(context2);
                                                                ServiceManager.addService("media_router", mediaRouterService2);
                                                                mediaRouterService = mediaRouterService2;
                                                                Trace.traceEnd(524288);
                                                                if (disableTrustManager) {
                                                                    this.mSystemServiceManager.startService(TrustManagerService.class);
                                                                }
                                                                traceBeginAndSlog("StartBackgroundDexOptService");
                                                                BackgroundDexOptService.schedule(context2);
                                                                Trace.traceEnd(524288);
                                                            }
                                                            this.mSystemServiceManager.startService(ShortcutService.Lifecycle.class);
                                                            this.mSystemServiceManager.startService(LauncherAppsService.class);
                                                            this.mSystemServiceManager.startService(MediaProjectionManagerService.class);
                                                            if (disableNonCoreServices) {
                                                                this.mSystemServiceManager.startService("com.android.server.NonHardwareAcceleratedPackagesManagerService");
                                                            }
                                                            if (disableNonCoreServices) {
                                                                jankshield = HwServiceFactory.getJankShieldService();
                                                                if (jankshield != null) {
                                                                    ServiceManager.addService("jankshield", jankshield.getInstance(context2));
                                                                }
                                                            }
                                                            if (context2.getPackageManager().hasSystemFeature("android.hardware.type.watch")) {
                                                                this.mSystemServiceManager.startService(WEAR_BLUETOOTH_SERVICE_CLASS);
                                                            }
                                                            safeMode = windowManagerService.detectSafeMode();
                                                            this.mSystemServiceManager.setSafeMode(safeMode);
                                                            if (safeMode) {
                                                                this.mActivityManagerService.enterSafeMode();
                                                                VMRuntime.getRuntime().disableJitCompilation();
                                                            } else {
                                                                VMRuntime.getRuntime().startJitCompilation();
                                                            }
                                                            mmsService = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                                                            this.mSystemServiceManager.startService("com.android.server.HwCoreAppHelperService");
                                                            if ("true".equals(SystemProperties.get("bastet.service.enable", "false"))) {
                                                                this.mSystemServiceManager.startService("com.android.server.HwBastetService");
                                                            }
                                                            this.mSystemServiceManager.startService("com.android.server.HwPPTBService");
                                                            if ("true".equals(SystemProperties.get("ro.config.hw_emcom", "false"))) {
                                                                startEmcomService();
                                                            }
                                                            Trace.traceBegin(524288, "MakeVibratorServiceReady");
                                                            vibratorService.systemReady();
                                                            Trace.traceEnd(524288);
                                                            Trace.traceBegin(524288, "MakeLockSettingsServiceReady");
                                                            if (iLockSettings != null) {
                                                                iLockSettings.systemReady();
                                                            }
                                                            Trace.traceEnd(524288);
                                                            this.mSystemServiceManager.startBootPhase(SystemService.PHASE_LOCK_SETTINGS_READY);
                                                            this.mSystemServiceManager.startBootPhase(SystemService.PHASE_SYSTEM_SERVICES_READY);
                                                            Trace.traceBegin(524288, "MakeWindowManagerServiceReady");
                                                            windowManagerService.systemReady();
                                                            Trace.traceEnd(524288);
                                                            if (safeMode) {
                                                                this.mActivityManagerService.showSafeModeOverlay();
                                                            }
                                                            config = windowManagerService.computeNewConfiguration();
                                                            metrics = new DisplayMetrics();
                                                            ((WindowManager) context2.getSystemService("window")).getDefaultDisplay().getMetrics(metrics);
                                                            context2.getResources().updateConfiguration(config, metrics);
                                                            systemTheme = context2.getTheme();
                                                            if (systemTheme.getChangingConfigurations() != 0) {
                                                                systemTheme.rebase();
                                                            }
                                                            Trace.traceBegin(524288, "MakePowerManagerServiceReady");
                                                            this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                                                            Trace.traceEnd(524288);
                                                            Trace.traceEnd(524288);
                                                            this.mPGManagerService.systemReady(this.mActivityManagerService, this.mPowerManagerService, location);
                                                            Trace.traceBegin(524288, "MakePackageManagerServiceReady");
                                                            this.mPackageManagerService.systemReady();
                                                            Trace.traceEnd(524288);
                                                            Trace.traceBegin(524288, "MakeDisplayManagerServiceReady");
                                                            this.mDisplayManagerService.systemReady(safeMode, this.mOnlyCore);
                                                            Trace.traceEnd(524288);
                                                            networkManagementF = networkManagement;
                                                            networkStatsF = networkStats;
                                                            networkPolicyF = networkPolicy;
                                                            connectivityF = connectivity;
                                                            networkScoreF = networkScoreService2;
                                                            locationF = location;
                                                            countryDetectorF = countryDetectorService;
                                                            networkTimeUpdaterF = networkTimeUpdateService2;
                                                            commonTimeMgmtServiceF = commonTimeManagementService2;
                                                            atlasF = assetAtlasService;
                                                            inputManagerF = inputManager;
                                                            telephonyRegistryF = telephonyRegistry;
                                                            mediaRouterF = mediaRouterService;
                                                            mmsServiceF = mmsService;
                                                            context = context2;
                                                            this.mActivityManagerService.systemReady(/* anonymous class already generated */);
                                                        }
                                                    } catch (Throwable th9) {
                                                        e322222222222222222 = th9;
                                                        Slog.e(TAG, "Failure starting SerialService", e322222222222222222);
                                                        Trace.traceEnd(524288);
                                                        Trace.traceBegin(524288, "StartHardwarePropertiesManagerService");
                                                        hardwarePropertiesManagerService = new HardwarePropertiesManagerService(context2);
                                                        ServiceManager.addService("hardware_properties", hardwarePropertiesManagerService);
                                                        hardwarePropertiesManagerService2 = hardwarePropertiesManagerService;
                                                        Trace.traceEnd(524288);
                                                        this.mSystemServiceManager.startService(TwilightService.class);
                                                        this.mSystemServiceManager.startService("com.android.server.job.HwJobSchedulerService");
                                                        this.mSystemServiceManager.startService(SoundTriggerService.class);
                                                        if (disableNonCoreServices) {
                                                            if (this.mPackageManager.hasSystemFeature("android.software.backup")) {
                                                                this.mSystemServiceManager.startService(BACKUP_MANAGER_SERVICE_CLASS);
                                                            }
                                                            this.mSystemServiceManager.startService(APPWIDGET_SERVICE_CLASS);
                                                            if (this.mPackageManager.hasSystemFeature("android.software.voice_recognizers")) {
                                                                this.mSystemServiceManager.startService(VOICE_RECOGNITION_MANAGER_SERVICE_CLASS);
                                                            }
                                                            if (GestureLauncherService.isGestureLauncherEnabled(context2.getResources())) {
                                                                Slog.i(TAG, "Gesture Launcher Service");
                                                                this.mSystemServiceManager.startService(GestureLauncherService.class);
                                                            }
                                                            this.mSystemServiceManager.startService(SensorNotificationService.class);
                                                            this.mSystemServiceManager.startService(ContextHubSystemService.class);
                                                        }
                                                        HwServiceFactory.setupHwServices(context2);
                                                        traceBeginAndSlog("StartDiskStatsService");
                                                        ServiceManager.addService("diskstats", new DiskStatsService(context2));
                                                        Trace.traceEnd(524288);
                                                        if (disableSamplingProfiler) {
                                                            traceBeginAndSlog("StartSamplingProfilerService");
                                                            ServiceManager.addService("samplingprofiler", new SamplingProfilerService(context2));
                                                            Trace.traceEnd(524288);
                                                        }
                                                        Slog.i(TAG, "attestation Service");
                                                        attestation = HwServiceFactory.getHwAttestationService();
                                                        if (attestation != null) {
                                                            ServiceManager.addService("attestation_service", attestation.getInstance(context2));
                                                        }
                                                        traceBeginAndSlog("StartNetworkTimeUpdateService");
                                                        networkTimeUpdateService = new NetworkTimeUpdateService(context2);
                                                        ServiceManager.addService("network_time_update_service", networkTimeUpdateService);
                                                        networkTimeUpdateService2 = networkTimeUpdateService;
                                                        Trace.traceEnd(524288);
                                                        traceBeginAndSlog("StartCommonTimeManagementService");
                                                        commonTimeManagementService = new CommonTimeManagementService(context2);
                                                        ServiceManager.addService("commontime_management", commonTimeManagementService);
                                                        commonTimeManagementService2 = commonTimeManagementService;
                                                        Trace.traceEnd(524288);
                                                        if (disableNetwork) {
                                                            traceBeginAndSlog("CertBlacklister");
                                                            certBlacklister = new CertBlacklister(context2);
                                                            Trace.traceEnd(524288);
                                                        }
                                                        if (disableNonCoreServices) {
                                                            this.mSystemServiceManager.startService(DreamManagerService.class);
                                                        }
                                                        if (disableNonCoreServices) {
                                                            traceBeginAndSlog("StartAssetAtlasService");
                                                            assetAtlasService2 = new AssetAtlasService(context2);
                                                            ServiceManager.addService(AssetAtlasService.ASSET_ATLAS_SERVICE, assetAtlasService2);
                                                            assetAtlasService = assetAtlasService2;
                                                            Trace.traceEnd(524288);
                                                        }
                                                        if (disableNonCoreServices) {
                                                            ServiceManager.addService(GraphicsStatsService.GRAPHICS_STATS_SERVICE, new GraphicsStatsService(context2));
                                                        }
                                                        if (this.mPackageManager.hasSystemFeature("android.software.print")) {
                                                            this.mSystemServiceManager.startService(PRINT_MANAGER_SERVICE_CLASS);
                                                        }
                                                        this.mSystemServiceManager.startService(RestrictionsManagerService.class);
                                                        this.mSystemServiceManager.startService(MediaSessionService.class);
                                                        if (this.mPackageManager.hasSystemFeature("android.hardware.hdmi.cec")) {
                                                            this.mSystemServiceManager.startService(HdmiControlService.class);
                                                        }
                                                        if (this.mPackageManager.hasSystemFeature("android.software.live_tv")) {
                                                            this.mSystemServiceManager.startService(TvInputManagerService.class);
                                                        }
                                                        if (this.mPackageManager.hasSystemFeature("android.software.picture_in_picture")) {
                                                            this.mSystemServiceManager.startService(MediaResourceMonitorService.class);
                                                        }
                                                        if (this.mPackageManager.hasSystemFeature("android.software.leanback")) {
                                                            this.mSystemServiceManager.startService(TvRemoteService.class);
                                                        }
                                                        if (disableNonCoreServices) {
                                                            traceBeginAndSlog("StartMediaRouterService");
                                                            mediaRouterService2 = new MediaRouterService(context2);
                                                            ServiceManager.addService("media_router", mediaRouterService2);
                                                            mediaRouterService = mediaRouterService2;
                                                            Trace.traceEnd(524288);
                                                            if (disableTrustManager) {
                                                                this.mSystemServiceManager.startService(TrustManagerService.class);
                                                            }
                                                            traceBeginAndSlog("StartBackgroundDexOptService");
                                                            BackgroundDexOptService.schedule(context2);
                                                            Trace.traceEnd(524288);
                                                        }
                                                        this.mSystemServiceManager.startService(ShortcutService.Lifecycle.class);
                                                        this.mSystemServiceManager.startService(LauncherAppsService.class);
                                                        this.mSystemServiceManager.startService(MediaProjectionManagerService.class);
                                                        if (disableNonCoreServices) {
                                                            this.mSystemServiceManager.startService("com.android.server.NonHardwareAcceleratedPackagesManagerService");
                                                        }
                                                        if (disableNonCoreServices) {
                                                            jankshield = HwServiceFactory.getJankShieldService();
                                                            if (jankshield != null) {
                                                                ServiceManager.addService("jankshield", jankshield.getInstance(context2));
                                                            }
                                                        }
                                                        if (context2.getPackageManager().hasSystemFeature("android.hardware.type.watch")) {
                                                            this.mSystemServiceManager.startService(WEAR_BLUETOOTH_SERVICE_CLASS);
                                                        }
                                                        safeMode = windowManagerService.detectSafeMode();
                                                        this.mSystemServiceManager.setSafeMode(safeMode);
                                                        if (safeMode) {
                                                            VMRuntime.getRuntime().startJitCompilation();
                                                        } else {
                                                            this.mActivityManagerService.enterSafeMode();
                                                            VMRuntime.getRuntime().disableJitCompilation();
                                                        }
                                                        mmsService = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                                                        this.mSystemServiceManager.startService("com.android.server.HwCoreAppHelperService");
                                                        if ("true".equals(SystemProperties.get("bastet.service.enable", "false"))) {
                                                            this.mSystemServiceManager.startService("com.android.server.HwBastetService");
                                                        }
                                                        this.mSystemServiceManager.startService("com.android.server.HwPPTBService");
                                                        if ("true".equals(SystemProperties.get("ro.config.hw_emcom", "false"))) {
                                                            startEmcomService();
                                                        }
                                                        Trace.traceBegin(524288, "MakeVibratorServiceReady");
                                                        vibratorService.systemReady();
                                                        Trace.traceEnd(524288);
                                                        Trace.traceBegin(524288, "MakeLockSettingsServiceReady");
                                                        if (iLockSettings != null) {
                                                            iLockSettings.systemReady();
                                                        }
                                                        Trace.traceEnd(524288);
                                                        this.mSystemServiceManager.startBootPhase(SystemService.PHASE_LOCK_SETTINGS_READY);
                                                        this.mSystemServiceManager.startBootPhase(SystemService.PHASE_SYSTEM_SERVICES_READY);
                                                        Trace.traceBegin(524288, "MakeWindowManagerServiceReady");
                                                        windowManagerService.systemReady();
                                                        Trace.traceEnd(524288);
                                                        if (safeMode) {
                                                            this.mActivityManagerService.showSafeModeOverlay();
                                                        }
                                                        config = windowManagerService.computeNewConfiguration();
                                                        metrics = new DisplayMetrics();
                                                        ((WindowManager) context2.getSystemService("window")).getDefaultDisplay().getMetrics(metrics);
                                                        context2.getResources().updateConfiguration(config, metrics);
                                                        systemTheme = context2.getTheme();
                                                        if (systemTheme.getChangingConfigurations() != 0) {
                                                            systemTheme.rebase();
                                                        }
                                                        Trace.traceBegin(524288, "MakePowerManagerServiceReady");
                                                        this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                                                        Trace.traceEnd(524288);
                                                        Trace.traceEnd(524288);
                                                        this.mPGManagerService.systemReady(this.mActivityManagerService, this.mPowerManagerService, location);
                                                        Trace.traceBegin(524288, "MakePackageManagerServiceReady");
                                                        this.mPackageManagerService.systemReady();
                                                        Trace.traceEnd(524288);
                                                        Trace.traceBegin(524288, "MakeDisplayManagerServiceReady");
                                                        this.mDisplayManagerService.systemReady(safeMode, this.mOnlyCore);
                                                        Trace.traceEnd(524288);
                                                        networkManagementF = networkManagement;
                                                        networkStatsF = networkStats;
                                                        networkPolicyF = networkPolicy;
                                                        connectivityF = connectivity;
                                                        networkScoreF = networkScoreService2;
                                                        locationF = location;
                                                        countryDetectorF = countryDetectorService;
                                                        networkTimeUpdaterF = networkTimeUpdateService2;
                                                        commonTimeMgmtServiceF = commonTimeManagementService2;
                                                        atlasF = assetAtlasService;
                                                        inputManagerF = inputManager;
                                                        telephonyRegistryF = telephonyRegistry;
                                                        mediaRouterF = mediaRouterService;
                                                        mmsServiceF = mmsService;
                                                        context = context2;
                                                        this.mActivityManagerService.systemReady(/* anonymous class already generated */);
                                                    }
                                                    Trace.traceEnd(524288);
                                                }
                                                Trace.traceBegin(524288, "StartHardwarePropertiesManagerService");
                                                try {
                                                    hardwarePropertiesManagerService = new HardwarePropertiesManagerService(context2);
                                                    ServiceManager.addService("hardware_properties", hardwarePropertiesManagerService);
                                                    hardwarePropertiesManagerService2 = hardwarePropertiesManagerService;
                                                } catch (Throwable th10) {
                                                    e322222222222222222 = th10;
                                                    Slog.e(TAG, "Failure starting HardwarePropertiesManagerService", e322222222222222222);
                                                    Trace.traceEnd(524288);
                                                    this.mSystemServiceManager.startService(TwilightService.class);
                                                    this.mSystemServiceManager.startService("com.android.server.job.HwJobSchedulerService");
                                                    this.mSystemServiceManager.startService(SoundTriggerService.class);
                                                    if (disableNonCoreServices) {
                                                        if (this.mPackageManager.hasSystemFeature("android.software.backup")) {
                                                            this.mSystemServiceManager.startService(BACKUP_MANAGER_SERVICE_CLASS);
                                                        }
                                                        this.mSystemServiceManager.startService(APPWIDGET_SERVICE_CLASS);
                                                        if (this.mPackageManager.hasSystemFeature("android.software.voice_recognizers")) {
                                                            this.mSystemServiceManager.startService(VOICE_RECOGNITION_MANAGER_SERVICE_CLASS);
                                                        }
                                                        if (GestureLauncherService.isGestureLauncherEnabled(context2.getResources())) {
                                                            Slog.i(TAG, "Gesture Launcher Service");
                                                            this.mSystemServiceManager.startService(GestureLauncherService.class);
                                                        }
                                                        this.mSystemServiceManager.startService(SensorNotificationService.class);
                                                        this.mSystemServiceManager.startService(ContextHubSystemService.class);
                                                    }
                                                    HwServiceFactory.setupHwServices(context2);
                                                    traceBeginAndSlog("StartDiskStatsService");
                                                    ServiceManager.addService("diskstats", new DiskStatsService(context2));
                                                    Trace.traceEnd(524288);
                                                    if (disableSamplingProfiler) {
                                                        traceBeginAndSlog("StartSamplingProfilerService");
                                                        ServiceManager.addService("samplingprofiler", new SamplingProfilerService(context2));
                                                        Trace.traceEnd(524288);
                                                    }
                                                    Slog.i(TAG, "attestation Service");
                                                    attestation = HwServiceFactory.getHwAttestationService();
                                                    if (attestation != null) {
                                                        ServiceManager.addService("attestation_service", attestation.getInstance(context2));
                                                    }
                                                    traceBeginAndSlog("StartNetworkTimeUpdateService");
                                                    networkTimeUpdateService = new NetworkTimeUpdateService(context2);
                                                    ServiceManager.addService("network_time_update_service", networkTimeUpdateService);
                                                    networkTimeUpdateService2 = networkTimeUpdateService;
                                                    Trace.traceEnd(524288);
                                                    traceBeginAndSlog("StartCommonTimeManagementService");
                                                    commonTimeManagementService = new CommonTimeManagementService(context2);
                                                    ServiceManager.addService("commontime_management", commonTimeManagementService);
                                                    commonTimeManagementService2 = commonTimeManagementService;
                                                    Trace.traceEnd(524288);
                                                    if (disableNetwork) {
                                                        traceBeginAndSlog("CertBlacklister");
                                                        certBlacklister = new CertBlacklister(context2);
                                                        Trace.traceEnd(524288);
                                                    }
                                                    if (disableNonCoreServices) {
                                                        this.mSystemServiceManager.startService(DreamManagerService.class);
                                                    }
                                                    if (disableNonCoreServices) {
                                                        traceBeginAndSlog("StartAssetAtlasService");
                                                        assetAtlasService2 = new AssetAtlasService(context2);
                                                        ServiceManager.addService(AssetAtlasService.ASSET_ATLAS_SERVICE, assetAtlasService2);
                                                        assetAtlasService = assetAtlasService2;
                                                        Trace.traceEnd(524288);
                                                    }
                                                    if (disableNonCoreServices) {
                                                        ServiceManager.addService(GraphicsStatsService.GRAPHICS_STATS_SERVICE, new GraphicsStatsService(context2));
                                                    }
                                                    if (this.mPackageManager.hasSystemFeature("android.software.print")) {
                                                        this.mSystemServiceManager.startService(PRINT_MANAGER_SERVICE_CLASS);
                                                    }
                                                    this.mSystemServiceManager.startService(RestrictionsManagerService.class);
                                                    this.mSystemServiceManager.startService(MediaSessionService.class);
                                                    if (this.mPackageManager.hasSystemFeature("android.hardware.hdmi.cec")) {
                                                        this.mSystemServiceManager.startService(HdmiControlService.class);
                                                    }
                                                    if (this.mPackageManager.hasSystemFeature("android.software.live_tv")) {
                                                        this.mSystemServiceManager.startService(TvInputManagerService.class);
                                                    }
                                                    if (this.mPackageManager.hasSystemFeature("android.software.picture_in_picture")) {
                                                        this.mSystemServiceManager.startService(MediaResourceMonitorService.class);
                                                    }
                                                    if (this.mPackageManager.hasSystemFeature("android.software.leanback")) {
                                                        this.mSystemServiceManager.startService(TvRemoteService.class);
                                                    }
                                                    if (disableNonCoreServices) {
                                                        traceBeginAndSlog("StartMediaRouterService");
                                                        mediaRouterService2 = new MediaRouterService(context2);
                                                        ServiceManager.addService("media_router", mediaRouterService2);
                                                        mediaRouterService = mediaRouterService2;
                                                        Trace.traceEnd(524288);
                                                        if (disableTrustManager) {
                                                            this.mSystemServiceManager.startService(TrustManagerService.class);
                                                        }
                                                        traceBeginAndSlog("StartBackgroundDexOptService");
                                                        BackgroundDexOptService.schedule(context2);
                                                        Trace.traceEnd(524288);
                                                    }
                                                    this.mSystemServiceManager.startService(ShortcutService.Lifecycle.class);
                                                    this.mSystemServiceManager.startService(LauncherAppsService.class);
                                                    this.mSystemServiceManager.startService(MediaProjectionManagerService.class);
                                                    if (disableNonCoreServices) {
                                                        this.mSystemServiceManager.startService("com.android.server.NonHardwareAcceleratedPackagesManagerService");
                                                    }
                                                    if (disableNonCoreServices) {
                                                        jankshield = HwServiceFactory.getJankShieldService();
                                                        if (jankshield != null) {
                                                            ServiceManager.addService("jankshield", jankshield.getInstance(context2));
                                                        }
                                                    }
                                                    if (context2.getPackageManager().hasSystemFeature("android.hardware.type.watch")) {
                                                        this.mSystemServiceManager.startService(WEAR_BLUETOOTH_SERVICE_CLASS);
                                                    }
                                                    safeMode = windowManagerService.detectSafeMode();
                                                    this.mSystemServiceManager.setSafeMode(safeMode);
                                                    if (safeMode) {
                                                        VMRuntime.getRuntime().startJitCompilation();
                                                    } else {
                                                        this.mActivityManagerService.enterSafeMode();
                                                        VMRuntime.getRuntime().disableJitCompilation();
                                                    }
                                                    mmsService = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                                                    this.mSystemServiceManager.startService("com.android.server.HwCoreAppHelperService");
                                                    if ("true".equals(SystemProperties.get("bastet.service.enable", "false"))) {
                                                        this.mSystemServiceManager.startService("com.android.server.HwBastetService");
                                                    }
                                                    this.mSystemServiceManager.startService("com.android.server.HwPPTBService");
                                                    if ("true".equals(SystemProperties.get("ro.config.hw_emcom", "false"))) {
                                                        startEmcomService();
                                                    }
                                                    Trace.traceBegin(524288, "MakeVibratorServiceReady");
                                                    vibratorService.systemReady();
                                                    Trace.traceEnd(524288);
                                                    Trace.traceBegin(524288, "MakeLockSettingsServiceReady");
                                                    if (iLockSettings != null) {
                                                        iLockSettings.systemReady();
                                                    }
                                                    Trace.traceEnd(524288);
                                                    this.mSystemServiceManager.startBootPhase(SystemService.PHASE_LOCK_SETTINGS_READY);
                                                    this.mSystemServiceManager.startBootPhase(SystemService.PHASE_SYSTEM_SERVICES_READY);
                                                    Trace.traceBegin(524288, "MakeWindowManagerServiceReady");
                                                    windowManagerService.systemReady();
                                                    Trace.traceEnd(524288);
                                                    if (safeMode) {
                                                        this.mActivityManagerService.showSafeModeOverlay();
                                                    }
                                                    config = windowManagerService.computeNewConfiguration();
                                                    metrics = new DisplayMetrics();
                                                    ((WindowManager) context2.getSystemService("window")).getDefaultDisplay().getMetrics(metrics);
                                                    context2.getResources().updateConfiguration(config, metrics);
                                                    systemTheme = context2.getTheme();
                                                    if (systemTheme.getChangingConfigurations() != 0) {
                                                        systemTheme.rebase();
                                                    }
                                                    Trace.traceBegin(524288, "MakePowerManagerServiceReady");
                                                    this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                                                    Trace.traceEnd(524288);
                                                    Trace.traceEnd(524288);
                                                    this.mPGManagerService.systemReady(this.mActivityManagerService, this.mPowerManagerService, location);
                                                    Trace.traceBegin(524288, "MakePackageManagerServiceReady");
                                                    this.mPackageManagerService.systemReady();
                                                    Trace.traceEnd(524288);
                                                    Trace.traceBegin(524288, "MakeDisplayManagerServiceReady");
                                                    this.mDisplayManagerService.systemReady(safeMode, this.mOnlyCore);
                                                    Trace.traceEnd(524288);
                                                    networkManagementF = networkManagement;
                                                    networkStatsF = networkStats;
                                                    networkPolicyF = networkPolicy;
                                                    connectivityF = connectivity;
                                                    networkScoreF = networkScoreService2;
                                                    locationF = location;
                                                    countryDetectorF = countryDetectorService;
                                                    networkTimeUpdaterF = networkTimeUpdateService2;
                                                    commonTimeMgmtServiceF = commonTimeManagementService2;
                                                    atlasF = assetAtlasService;
                                                    inputManagerF = inputManager;
                                                    telephonyRegistryF = telephonyRegistry;
                                                    mediaRouterF = mediaRouterService;
                                                    mmsServiceF = mmsService;
                                                    context = context2;
                                                    this.mActivityManagerService.systemReady(/* anonymous class already generated */);
                                                }
                                                Trace.traceEnd(524288);
                                            }
                                            this.mSystemServiceManager.startService(TwilightService.class);
                                            this.mSystemServiceManager.startService("com.android.server.job.HwJobSchedulerService");
                                            this.mSystemServiceManager.startService(SoundTriggerService.class);
                                            if (disableNonCoreServices) {
                                                if (this.mPackageManager.hasSystemFeature("android.software.backup")) {
                                                    this.mSystemServiceManager.startService(BACKUP_MANAGER_SERVICE_CLASS);
                                                }
                                                this.mSystemServiceManager.startService(APPWIDGET_SERVICE_CLASS);
                                                if (this.mPackageManager.hasSystemFeature("android.software.voice_recognizers")) {
                                                    this.mSystemServiceManager.startService(VOICE_RECOGNITION_MANAGER_SERVICE_CLASS);
                                                }
                                                if (GestureLauncherService.isGestureLauncherEnabled(context2.getResources())) {
                                                    Slog.i(TAG, "Gesture Launcher Service");
                                                    this.mSystemServiceManager.startService(GestureLauncherService.class);
                                                }
                                                this.mSystemServiceManager.startService(SensorNotificationService.class);
                                                this.mSystemServiceManager.startService(ContextHubSystemService.class);
                                            }
                                            HwServiceFactory.setupHwServices(context2);
                                            traceBeginAndSlog("StartDiskStatsService");
                                            ServiceManager.addService("diskstats", new DiskStatsService(context2));
                                            Trace.traceEnd(524288);
                                            if (disableSamplingProfiler) {
                                                traceBeginAndSlog("StartSamplingProfilerService");
                                                ServiceManager.addService("samplingprofiler", new SamplingProfilerService(context2));
                                                Trace.traceEnd(524288);
                                            }
                                            Slog.i(TAG, "attestation Service");
                                            attestation = HwServiceFactory.getHwAttestationService();
                                            if (attestation != null) {
                                                ServiceManager.addService("attestation_service", attestation.getInstance(context2));
                                            }
                                            traceBeginAndSlog("StartNetworkTimeUpdateService");
                                            networkTimeUpdateService = new NetworkTimeUpdateService(context2);
                                            ServiceManager.addService("network_time_update_service", networkTimeUpdateService);
                                            networkTimeUpdateService2 = networkTimeUpdateService;
                                            Trace.traceEnd(524288);
                                            traceBeginAndSlog("StartCommonTimeManagementService");
                                            commonTimeManagementService = new CommonTimeManagementService(context2);
                                            ServiceManager.addService("commontime_management", commonTimeManagementService);
                                            commonTimeManagementService2 = commonTimeManagementService;
                                            Trace.traceEnd(524288);
                                            if (disableNetwork) {
                                                traceBeginAndSlog("CertBlacklister");
                                                certBlacklister = new CertBlacklister(context2);
                                                Trace.traceEnd(524288);
                                            }
                                            if (disableNonCoreServices) {
                                                this.mSystemServiceManager.startService(DreamManagerService.class);
                                            }
                                            if (disableNonCoreServices) {
                                                traceBeginAndSlog("StartAssetAtlasService");
                                                assetAtlasService2 = new AssetAtlasService(context2);
                                                ServiceManager.addService(AssetAtlasService.ASSET_ATLAS_SERVICE, assetAtlasService2);
                                                assetAtlasService = assetAtlasService2;
                                                Trace.traceEnd(524288);
                                            }
                                            if (disableNonCoreServices) {
                                                ServiceManager.addService(GraphicsStatsService.GRAPHICS_STATS_SERVICE, new GraphicsStatsService(context2));
                                            }
                                            if (this.mPackageManager.hasSystemFeature("android.software.print")) {
                                                this.mSystemServiceManager.startService(PRINT_MANAGER_SERVICE_CLASS);
                                            }
                                            this.mSystemServiceManager.startService(RestrictionsManagerService.class);
                                            this.mSystemServiceManager.startService(MediaSessionService.class);
                                            if (this.mPackageManager.hasSystemFeature("android.hardware.hdmi.cec")) {
                                                this.mSystemServiceManager.startService(HdmiControlService.class);
                                            }
                                            if (this.mPackageManager.hasSystemFeature("android.software.live_tv")) {
                                                this.mSystemServiceManager.startService(TvInputManagerService.class);
                                            }
                                            if (this.mPackageManager.hasSystemFeature("android.software.picture_in_picture")) {
                                                this.mSystemServiceManager.startService(MediaResourceMonitorService.class);
                                            }
                                            if (this.mPackageManager.hasSystemFeature("android.software.leanback")) {
                                                this.mSystemServiceManager.startService(TvRemoteService.class);
                                            }
                                            if (disableNonCoreServices) {
                                                traceBeginAndSlog("StartMediaRouterService");
                                                mediaRouterService2 = new MediaRouterService(context2);
                                                ServiceManager.addService("media_router", mediaRouterService2);
                                                mediaRouterService = mediaRouterService2;
                                                Trace.traceEnd(524288);
                                                if (disableTrustManager) {
                                                    this.mSystemServiceManager.startService(TrustManagerService.class);
                                                }
                                                traceBeginAndSlog("StartBackgroundDexOptService");
                                                BackgroundDexOptService.schedule(context2);
                                                Trace.traceEnd(524288);
                                            }
                                            this.mSystemServiceManager.startService(ShortcutService.Lifecycle.class);
                                            this.mSystemServiceManager.startService(LauncherAppsService.class);
                                            this.mSystemServiceManager.startService(MediaProjectionManagerService.class);
                                            if (disableNonCoreServices) {
                                                this.mSystemServiceManager.startService("com.android.server.NonHardwareAcceleratedPackagesManagerService");
                                            }
                                            if (disableNonCoreServices) {
                                                jankshield = HwServiceFactory.getJankShieldService();
                                                if (jankshield != null) {
                                                    ServiceManager.addService("jankshield", jankshield.getInstance(context2));
                                                }
                                            }
                                            if (context2.getPackageManager().hasSystemFeature("android.hardware.type.watch")) {
                                                this.mSystemServiceManager.startService(WEAR_BLUETOOTH_SERVICE_CLASS);
                                            }
                                            safeMode = windowManagerService.detectSafeMode();
                                            this.mSystemServiceManager.setSafeMode(safeMode);
                                            if (safeMode) {
                                                VMRuntime.getRuntime().startJitCompilation();
                                            } else {
                                                this.mActivityManagerService.enterSafeMode();
                                                VMRuntime.getRuntime().disableJitCompilation();
                                            }
                                            mmsService = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                                            this.mSystemServiceManager.startService("com.android.server.HwCoreAppHelperService");
                                            if ("true".equals(SystemProperties.get("bastet.service.enable", "false"))) {
                                                this.mSystemServiceManager.startService("com.android.server.HwBastetService");
                                            }
                                            this.mSystemServiceManager.startService("com.android.server.HwPPTBService");
                                            if ("true".equals(SystemProperties.get("ro.config.hw_emcom", "false"))) {
                                                startEmcomService();
                                            }
                                            Trace.traceBegin(524288, "MakeVibratorServiceReady");
                                            vibratorService.systemReady();
                                            Trace.traceEnd(524288);
                                            Trace.traceBegin(524288, "MakeLockSettingsServiceReady");
                                            if (iLockSettings != null) {
                                                iLockSettings.systemReady();
                                            }
                                            Trace.traceEnd(524288);
                                            this.mSystemServiceManager.startBootPhase(SystemService.PHASE_LOCK_SETTINGS_READY);
                                            this.mSystemServiceManager.startBootPhase(SystemService.PHASE_SYSTEM_SERVICES_READY);
                                            Trace.traceBegin(524288, "MakeWindowManagerServiceReady");
                                            windowManagerService.systemReady();
                                            Trace.traceEnd(524288);
                                            if (safeMode) {
                                                this.mActivityManagerService.showSafeModeOverlay();
                                            }
                                            config = windowManagerService.computeNewConfiguration();
                                            metrics = new DisplayMetrics();
                                            ((WindowManager) context2.getSystemService("window")).getDefaultDisplay().getMetrics(metrics);
                                            context2.getResources().updateConfiguration(config, metrics);
                                            systemTheme = context2.getTheme();
                                            if (systemTheme.getChangingConfigurations() != 0) {
                                                systemTheme.rebase();
                                            }
                                            Trace.traceBegin(524288, "MakePowerManagerServiceReady");
                                            this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                                            Trace.traceEnd(524288);
                                            Trace.traceEnd(524288);
                                            this.mPGManagerService.systemReady(this.mActivityManagerService, this.mPowerManagerService, location);
                                            Trace.traceBegin(524288, "MakePackageManagerServiceReady");
                                            this.mPackageManagerService.systemReady();
                                            Trace.traceEnd(524288);
                                            Trace.traceBegin(524288, "MakeDisplayManagerServiceReady");
                                            this.mDisplayManagerService.systemReady(safeMode, this.mOnlyCore);
                                            Trace.traceEnd(524288);
                                            networkManagementF = networkManagement;
                                            networkStatsF = networkStats;
                                            networkPolicyF = networkPolicy;
                                            connectivityF = connectivity;
                                            networkScoreF = networkScoreService2;
                                            locationF = location;
                                            countryDetectorF = countryDetectorService;
                                            networkTimeUpdaterF = networkTimeUpdateService2;
                                            commonTimeMgmtServiceF = commonTimeManagementService2;
                                            atlasF = assetAtlasService;
                                            inputManagerF = inputManager;
                                            telephonyRegistryF = telephonyRegistry;
                                            mediaRouterF = mediaRouterService;
                                            mmsServiceF = mmsService;
                                            context = context2;
                                            this.mActivityManagerService.systemReady(/* anonymous class already generated */);
                                        }
                                    } catch (Throwable th11) {
                                        e322222222222222222 = th11;
                                        reportWtf("starting Country Detector", e322222222222222222);
                                        Trace.traceEnd(524288);
                                        traceBeginAndSlog("StartSearchManagerService");
                                        this.mSystemServiceManager.startService(SEARCH_MANAGER_SERVICE_CLASS);
                                        Trace.traceEnd(524288);
                                        this.mSystemServiceManager.startService(DropBoxManagerService.class);
                                        traceBeginAndSlog("StartWallpaperManagerService");
                                        this.mSystemServiceManager.startService(HwServiceFactory.getWallpaperManagerServiceClassName());
                                        Trace.traceEnd(524288);
                                        traceBeginAndSlog("StartAudioService");
                                        this.mSystemServiceManager.startService(AudioService.Lifecycle.class);
                                        Trace.traceEnd(524288);
                                        if (disableNonCoreServices) {
                                            this.mSystemServiceManager.startService(DockObserver.class);
                                            if (context2.getPackageManager().hasSystemFeature("android.hardware.type.watch")) {
                                                this.mSystemServiceManager.startService(THERMAL_OBSERVER_CLASS);
                                            }
                                        }
                                        traceBeginAndSlog("StartWiredAccessoryManager");
                                        inputManager.setWiredAccessoryCallbacks(new WiredAccessoryManager(context2, inputManager));
                                        Trace.traceEnd(524288);
                                        if (disableNonCoreServices) {
                                            if (this.mPackageManager.hasSystemFeature("android.software.midi")) {
                                                this.mSystemServiceManager.startService(MIDI_SERVICE_CLASS);
                                            }
                                            Trace.traceBegin(524288, "StartUsbService");
                                            this.mSystemServiceManager.startService(USB_SERVICE_CLASS);
                                            Trace.traceEnd(524288);
                                            if (disableSerial) {
                                                traceBeginAndSlog("StartSerialService");
                                                serialService = new SerialService(context2);
                                                ServiceManager.addService("serial", serialService);
                                                serialService2 = serialService;
                                                Trace.traceEnd(524288);
                                            }
                                            Trace.traceBegin(524288, "StartHardwarePropertiesManagerService");
                                            hardwarePropertiesManagerService = new HardwarePropertiesManagerService(context2);
                                            ServiceManager.addService("hardware_properties", hardwarePropertiesManagerService);
                                            hardwarePropertiesManagerService2 = hardwarePropertiesManagerService;
                                            Trace.traceEnd(524288);
                                        }
                                        this.mSystemServiceManager.startService(TwilightService.class);
                                        this.mSystemServiceManager.startService("com.android.server.job.HwJobSchedulerService");
                                        this.mSystemServiceManager.startService(SoundTriggerService.class);
                                        if (disableNonCoreServices) {
                                            if (this.mPackageManager.hasSystemFeature("android.software.backup")) {
                                                this.mSystemServiceManager.startService(BACKUP_MANAGER_SERVICE_CLASS);
                                            }
                                            this.mSystemServiceManager.startService(APPWIDGET_SERVICE_CLASS);
                                            if (this.mPackageManager.hasSystemFeature("android.software.voice_recognizers")) {
                                                this.mSystemServiceManager.startService(VOICE_RECOGNITION_MANAGER_SERVICE_CLASS);
                                            }
                                            if (GestureLauncherService.isGestureLauncherEnabled(context2.getResources())) {
                                                Slog.i(TAG, "Gesture Launcher Service");
                                                this.mSystemServiceManager.startService(GestureLauncherService.class);
                                            }
                                            this.mSystemServiceManager.startService(SensorNotificationService.class);
                                            this.mSystemServiceManager.startService(ContextHubSystemService.class);
                                        }
                                        HwServiceFactory.setupHwServices(context2);
                                        traceBeginAndSlog("StartDiskStatsService");
                                        ServiceManager.addService("diskstats", new DiskStatsService(context2));
                                        Trace.traceEnd(524288);
                                        if (disableSamplingProfiler) {
                                            traceBeginAndSlog("StartSamplingProfilerService");
                                            ServiceManager.addService("samplingprofiler", new SamplingProfilerService(context2));
                                            Trace.traceEnd(524288);
                                        }
                                        Slog.i(TAG, "attestation Service");
                                        attestation = HwServiceFactory.getHwAttestationService();
                                        if (attestation != null) {
                                            ServiceManager.addService("attestation_service", attestation.getInstance(context2));
                                        }
                                        traceBeginAndSlog("StartNetworkTimeUpdateService");
                                        networkTimeUpdateService = new NetworkTimeUpdateService(context2);
                                        ServiceManager.addService("network_time_update_service", networkTimeUpdateService);
                                        networkTimeUpdateService2 = networkTimeUpdateService;
                                        Trace.traceEnd(524288);
                                        traceBeginAndSlog("StartCommonTimeManagementService");
                                        commonTimeManagementService = new CommonTimeManagementService(context2);
                                        ServiceManager.addService("commontime_management", commonTimeManagementService);
                                        commonTimeManagementService2 = commonTimeManagementService;
                                        Trace.traceEnd(524288);
                                        if (disableNetwork) {
                                            traceBeginAndSlog("CertBlacklister");
                                            certBlacklister = new CertBlacklister(context2);
                                            Trace.traceEnd(524288);
                                        }
                                        if (disableNonCoreServices) {
                                            this.mSystemServiceManager.startService(DreamManagerService.class);
                                        }
                                        if (disableNonCoreServices) {
                                            traceBeginAndSlog("StartAssetAtlasService");
                                            assetAtlasService2 = new AssetAtlasService(context2);
                                            ServiceManager.addService(AssetAtlasService.ASSET_ATLAS_SERVICE, assetAtlasService2);
                                            assetAtlasService = assetAtlasService2;
                                            Trace.traceEnd(524288);
                                        }
                                        if (disableNonCoreServices) {
                                            ServiceManager.addService(GraphicsStatsService.GRAPHICS_STATS_SERVICE, new GraphicsStatsService(context2));
                                        }
                                        if (this.mPackageManager.hasSystemFeature("android.software.print")) {
                                            this.mSystemServiceManager.startService(PRINT_MANAGER_SERVICE_CLASS);
                                        }
                                        this.mSystemServiceManager.startService(RestrictionsManagerService.class);
                                        this.mSystemServiceManager.startService(MediaSessionService.class);
                                        if (this.mPackageManager.hasSystemFeature("android.hardware.hdmi.cec")) {
                                            this.mSystemServiceManager.startService(HdmiControlService.class);
                                        }
                                        if (this.mPackageManager.hasSystemFeature("android.software.live_tv")) {
                                            this.mSystemServiceManager.startService(TvInputManagerService.class);
                                        }
                                        if (this.mPackageManager.hasSystemFeature("android.software.picture_in_picture")) {
                                            this.mSystemServiceManager.startService(MediaResourceMonitorService.class);
                                        }
                                        if (this.mPackageManager.hasSystemFeature("android.software.leanback")) {
                                            this.mSystemServiceManager.startService(TvRemoteService.class);
                                        }
                                        if (disableNonCoreServices) {
                                            traceBeginAndSlog("StartMediaRouterService");
                                            mediaRouterService2 = new MediaRouterService(context2);
                                            ServiceManager.addService("media_router", mediaRouterService2);
                                            mediaRouterService = mediaRouterService2;
                                            Trace.traceEnd(524288);
                                            if (disableTrustManager) {
                                                this.mSystemServiceManager.startService(TrustManagerService.class);
                                            }
                                            traceBeginAndSlog("StartBackgroundDexOptService");
                                            BackgroundDexOptService.schedule(context2);
                                            Trace.traceEnd(524288);
                                        }
                                        this.mSystemServiceManager.startService(ShortcutService.Lifecycle.class);
                                        this.mSystemServiceManager.startService(LauncherAppsService.class);
                                        this.mSystemServiceManager.startService(MediaProjectionManagerService.class);
                                        if (disableNonCoreServices) {
                                            this.mSystemServiceManager.startService("com.android.server.NonHardwareAcceleratedPackagesManagerService");
                                        }
                                        if (disableNonCoreServices) {
                                            jankshield = HwServiceFactory.getJankShieldService();
                                            if (jankshield != null) {
                                                ServiceManager.addService("jankshield", jankshield.getInstance(context2));
                                            }
                                        }
                                        if (context2.getPackageManager().hasSystemFeature("android.hardware.type.watch")) {
                                            this.mSystemServiceManager.startService(WEAR_BLUETOOTH_SERVICE_CLASS);
                                        }
                                        safeMode = windowManagerService.detectSafeMode();
                                        this.mSystemServiceManager.setSafeMode(safeMode);
                                        if (safeMode) {
                                            this.mActivityManagerService.enterSafeMode();
                                            VMRuntime.getRuntime().disableJitCompilation();
                                        } else {
                                            VMRuntime.getRuntime().startJitCompilation();
                                        }
                                        mmsService = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                                        this.mSystemServiceManager.startService("com.android.server.HwCoreAppHelperService");
                                        if ("true".equals(SystemProperties.get("bastet.service.enable", "false"))) {
                                            this.mSystemServiceManager.startService("com.android.server.HwBastetService");
                                        }
                                        this.mSystemServiceManager.startService("com.android.server.HwPPTBService");
                                        if ("true".equals(SystemProperties.get("ro.config.hw_emcom", "false"))) {
                                            startEmcomService();
                                        }
                                        Trace.traceBegin(524288, "MakeVibratorServiceReady");
                                        vibratorService.systemReady();
                                        Trace.traceEnd(524288);
                                        Trace.traceBegin(524288, "MakeLockSettingsServiceReady");
                                        if (iLockSettings != null) {
                                            iLockSettings.systemReady();
                                        }
                                        Trace.traceEnd(524288);
                                        this.mSystemServiceManager.startBootPhase(SystemService.PHASE_LOCK_SETTINGS_READY);
                                        this.mSystemServiceManager.startBootPhase(SystemService.PHASE_SYSTEM_SERVICES_READY);
                                        Trace.traceBegin(524288, "MakeWindowManagerServiceReady");
                                        windowManagerService.systemReady();
                                        Trace.traceEnd(524288);
                                        if (safeMode) {
                                            this.mActivityManagerService.showSafeModeOverlay();
                                        }
                                        config = windowManagerService.computeNewConfiguration();
                                        metrics = new DisplayMetrics();
                                        ((WindowManager) context2.getSystemService("window")).getDefaultDisplay().getMetrics(metrics);
                                        context2.getResources().updateConfiguration(config, metrics);
                                        systemTheme = context2.getTheme();
                                        if (systemTheme.getChangingConfigurations() != 0) {
                                            systemTheme.rebase();
                                        }
                                        Trace.traceBegin(524288, "MakePowerManagerServiceReady");
                                        this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                                        Trace.traceEnd(524288);
                                        Trace.traceEnd(524288);
                                        this.mPGManagerService.systemReady(this.mActivityManagerService, this.mPowerManagerService, location);
                                        Trace.traceBegin(524288, "MakePackageManagerServiceReady");
                                        this.mPackageManagerService.systemReady();
                                        Trace.traceEnd(524288);
                                        Trace.traceBegin(524288, "MakeDisplayManagerServiceReady");
                                        this.mDisplayManagerService.systemReady(safeMode, this.mOnlyCore);
                                        Trace.traceEnd(524288);
                                        networkManagementF = networkManagement;
                                        networkStatsF = networkStats;
                                        networkPolicyF = networkPolicy;
                                        connectivityF = connectivity;
                                        networkScoreF = networkScoreService2;
                                        locationF = location;
                                        countryDetectorF = countryDetectorService;
                                        networkTimeUpdaterF = networkTimeUpdateService2;
                                        commonTimeMgmtServiceF = commonTimeManagementService2;
                                        atlasF = assetAtlasService;
                                        inputManagerF = inputManager;
                                        telephonyRegistryF = telephonyRegistry;
                                        mediaRouterF = mediaRouterService;
                                        mmsServiceF = mmsService;
                                        context = context2;
                                        this.mActivityManagerService.systemReady(/* anonymous class already generated */);
                                    }
                                    Trace.traceEnd(524288);
                                }
                                traceBeginAndSlog("StartSearchManagerService");
                                this.mSystemServiceManager.startService(SEARCH_MANAGER_SERVICE_CLASS);
                                Trace.traceEnd(524288);
                                this.mSystemServiceManager.startService(DropBoxManagerService.class);
                                traceBeginAndSlog("StartWallpaperManagerService");
                                this.mSystemServiceManager.startService(HwServiceFactory.getWallpaperManagerServiceClassName());
                                Trace.traceEnd(524288);
                                traceBeginAndSlog("StartAudioService");
                                this.mSystemServiceManager.startService(AudioService.Lifecycle.class);
                                Trace.traceEnd(524288);
                                if (disableNonCoreServices) {
                                    this.mSystemServiceManager.startService(DockObserver.class);
                                    if (context2.getPackageManager().hasSystemFeature("android.hardware.type.watch")) {
                                        this.mSystemServiceManager.startService(THERMAL_OBSERVER_CLASS);
                                    }
                                }
                                traceBeginAndSlog("StartWiredAccessoryManager");
                                inputManager.setWiredAccessoryCallbacks(new WiredAccessoryManager(context2, inputManager));
                                Trace.traceEnd(524288);
                                if (disableNonCoreServices) {
                                    if (this.mPackageManager.hasSystemFeature("android.software.midi")) {
                                        this.mSystemServiceManager.startService(MIDI_SERVICE_CLASS);
                                    }
                                    Trace.traceBegin(524288, "StartUsbService");
                                    this.mSystemServiceManager.startService(USB_SERVICE_CLASS);
                                    Trace.traceEnd(524288);
                                    if (disableSerial) {
                                        traceBeginAndSlog("StartSerialService");
                                        serialService = new SerialService(context2);
                                        ServiceManager.addService("serial", serialService);
                                        serialService2 = serialService;
                                        Trace.traceEnd(524288);
                                    }
                                    Trace.traceBegin(524288, "StartHardwarePropertiesManagerService");
                                    hardwarePropertiesManagerService = new HardwarePropertiesManagerService(context2);
                                    ServiceManager.addService("hardware_properties", hardwarePropertiesManagerService);
                                    hardwarePropertiesManagerService2 = hardwarePropertiesManagerService;
                                    Trace.traceEnd(524288);
                                }
                                this.mSystemServiceManager.startService(TwilightService.class);
                                this.mSystemServiceManager.startService("com.android.server.job.HwJobSchedulerService");
                                this.mSystemServiceManager.startService(SoundTriggerService.class);
                                if (disableNonCoreServices) {
                                    if (this.mPackageManager.hasSystemFeature("android.software.backup")) {
                                        this.mSystemServiceManager.startService(BACKUP_MANAGER_SERVICE_CLASS);
                                    }
                                    this.mSystemServiceManager.startService(APPWIDGET_SERVICE_CLASS);
                                    if (this.mPackageManager.hasSystemFeature("android.software.voice_recognizers")) {
                                        this.mSystemServiceManager.startService(VOICE_RECOGNITION_MANAGER_SERVICE_CLASS);
                                    }
                                    if (GestureLauncherService.isGestureLauncherEnabled(context2.getResources())) {
                                        Slog.i(TAG, "Gesture Launcher Service");
                                        this.mSystemServiceManager.startService(GestureLauncherService.class);
                                    }
                                    this.mSystemServiceManager.startService(SensorNotificationService.class);
                                    this.mSystemServiceManager.startService(ContextHubSystemService.class);
                                }
                                HwServiceFactory.setupHwServices(context2);
                                traceBeginAndSlog("StartDiskStatsService");
                                ServiceManager.addService("diskstats", new DiskStatsService(context2));
                                Trace.traceEnd(524288);
                                if (disableSamplingProfiler) {
                                    traceBeginAndSlog("StartSamplingProfilerService");
                                    ServiceManager.addService("samplingprofiler", new SamplingProfilerService(context2));
                                    Trace.traceEnd(524288);
                                }
                                Slog.i(TAG, "attestation Service");
                                attestation = HwServiceFactory.getHwAttestationService();
                                if (attestation != null) {
                                    ServiceManager.addService("attestation_service", attestation.getInstance(context2));
                                }
                                traceBeginAndSlog("StartNetworkTimeUpdateService");
                                networkTimeUpdateService = new NetworkTimeUpdateService(context2);
                                try {
                                    ServiceManager.addService("network_time_update_service", networkTimeUpdateService);
                                    networkTimeUpdateService2 = networkTimeUpdateService;
                                } catch (Throwable th12) {
                                    e322222222222222222 = th12;
                                    networkTimeUpdateService2 = networkTimeUpdateService;
                                    reportWtf("starting NetworkTimeUpdate service", e322222222222222222);
                                    Trace.traceEnd(524288);
                                    traceBeginAndSlog("StartCommonTimeManagementService");
                                    commonTimeManagementService = new CommonTimeManagementService(context2);
                                    ServiceManager.addService("commontime_management", commonTimeManagementService);
                                    commonTimeManagementService2 = commonTimeManagementService;
                                    Trace.traceEnd(524288);
                                    if (disableNetwork) {
                                        traceBeginAndSlog("CertBlacklister");
                                        certBlacklister = new CertBlacklister(context2);
                                        Trace.traceEnd(524288);
                                    }
                                    if (disableNonCoreServices) {
                                        this.mSystemServiceManager.startService(DreamManagerService.class);
                                    }
                                    if (disableNonCoreServices) {
                                        traceBeginAndSlog("StartAssetAtlasService");
                                        assetAtlasService2 = new AssetAtlasService(context2);
                                        ServiceManager.addService(AssetAtlasService.ASSET_ATLAS_SERVICE, assetAtlasService2);
                                        assetAtlasService = assetAtlasService2;
                                        Trace.traceEnd(524288);
                                    }
                                    if (disableNonCoreServices) {
                                        ServiceManager.addService(GraphicsStatsService.GRAPHICS_STATS_SERVICE, new GraphicsStatsService(context2));
                                    }
                                    if (this.mPackageManager.hasSystemFeature("android.software.print")) {
                                        this.mSystemServiceManager.startService(PRINT_MANAGER_SERVICE_CLASS);
                                    }
                                    this.mSystemServiceManager.startService(RestrictionsManagerService.class);
                                    this.mSystemServiceManager.startService(MediaSessionService.class);
                                    if (this.mPackageManager.hasSystemFeature("android.hardware.hdmi.cec")) {
                                        this.mSystemServiceManager.startService(HdmiControlService.class);
                                    }
                                    if (this.mPackageManager.hasSystemFeature("android.software.live_tv")) {
                                        this.mSystemServiceManager.startService(TvInputManagerService.class);
                                    }
                                    if (this.mPackageManager.hasSystemFeature("android.software.picture_in_picture")) {
                                        this.mSystemServiceManager.startService(MediaResourceMonitorService.class);
                                    }
                                    if (this.mPackageManager.hasSystemFeature("android.software.leanback")) {
                                        this.mSystemServiceManager.startService(TvRemoteService.class);
                                    }
                                    if (disableNonCoreServices) {
                                        traceBeginAndSlog("StartMediaRouterService");
                                        mediaRouterService2 = new MediaRouterService(context2);
                                        ServiceManager.addService("media_router", mediaRouterService2);
                                        mediaRouterService = mediaRouterService2;
                                        Trace.traceEnd(524288);
                                        if (disableTrustManager) {
                                            this.mSystemServiceManager.startService(TrustManagerService.class);
                                        }
                                        traceBeginAndSlog("StartBackgroundDexOptService");
                                        BackgroundDexOptService.schedule(context2);
                                        Trace.traceEnd(524288);
                                    }
                                    this.mSystemServiceManager.startService(ShortcutService.Lifecycle.class);
                                    this.mSystemServiceManager.startService(LauncherAppsService.class);
                                    this.mSystemServiceManager.startService(MediaProjectionManagerService.class);
                                    if (disableNonCoreServices) {
                                        this.mSystemServiceManager.startService("com.android.server.NonHardwareAcceleratedPackagesManagerService");
                                    }
                                    if (disableNonCoreServices) {
                                        jankshield = HwServiceFactory.getJankShieldService();
                                        if (jankshield != null) {
                                            ServiceManager.addService("jankshield", jankshield.getInstance(context2));
                                        }
                                    }
                                    if (context2.getPackageManager().hasSystemFeature("android.hardware.type.watch")) {
                                        this.mSystemServiceManager.startService(WEAR_BLUETOOTH_SERVICE_CLASS);
                                    }
                                    safeMode = windowManagerService.detectSafeMode();
                                    this.mSystemServiceManager.setSafeMode(safeMode);
                                    if (safeMode) {
                                        VMRuntime.getRuntime().startJitCompilation();
                                    } else {
                                        this.mActivityManagerService.enterSafeMode();
                                        VMRuntime.getRuntime().disableJitCompilation();
                                    }
                                    mmsService = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                                    this.mSystemServiceManager.startService("com.android.server.HwCoreAppHelperService");
                                    if ("true".equals(SystemProperties.get("bastet.service.enable", "false"))) {
                                        this.mSystemServiceManager.startService("com.android.server.HwBastetService");
                                    }
                                    this.mSystemServiceManager.startService("com.android.server.HwPPTBService");
                                    if ("true".equals(SystemProperties.get("ro.config.hw_emcom", "false"))) {
                                        startEmcomService();
                                    }
                                    Trace.traceBegin(524288, "MakeVibratorServiceReady");
                                    vibratorService.systemReady();
                                    Trace.traceEnd(524288);
                                    Trace.traceBegin(524288, "MakeLockSettingsServiceReady");
                                    if (iLockSettings != null) {
                                        iLockSettings.systemReady();
                                    }
                                    Trace.traceEnd(524288);
                                    this.mSystemServiceManager.startBootPhase(SystemService.PHASE_LOCK_SETTINGS_READY);
                                    this.mSystemServiceManager.startBootPhase(SystemService.PHASE_SYSTEM_SERVICES_READY);
                                    Trace.traceBegin(524288, "MakeWindowManagerServiceReady");
                                    windowManagerService.systemReady();
                                    Trace.traceEnd(524288);
                                    if (safeMode) {
                                        this.mActivityManagerService.showSafeModeOverlay();
                                    }
                                    config = windowManagerService.computeNewConfiguration();
                                    metrics = new DisplayMetrics();
                                    ((WindowManager) context2.getSystemService("window")).getDefaultDisplay().getMetrics(metrics);
                                    context2.getResources().updateConfiguration(config, metrics);
                                    systemTheme = context2.getTheme();
                                    if (systemTheme.getChangingConfigurations() != 0) {
                                        systemTheme.rebase();
                                    }
                                    Trace.traceBegin(524288, "MakePowerManagerServiceReady");
                                    this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                                    Trace.traceEnd(524288);
                                    Trace.traceEnd(524288);
                                    this.mPGManagerService.systemReady(this.mActivityManagerService, this.mPowerManagerService, location);
                                    Trace.traceBegin(524288, "MakePackageManagerServiceReady");
                                    this.mPackageManagerService.systemReady();
                                    Trace.traceEnd(524288);
                                    Trace.traceBegin(524288, "MakeDisplayManagerServiceReady");
                                    this.mDisplayManagerService.systemReady(safeMode, this.mOnlyCore);
                                    Trace.traceEnd(524288);
                                    networkManagementF = networkManagement;
                                    networkStatsF = networkStats;
                                    networkPolicyF = networkPolicy;
                                    connectivityF = connectivity;
                                    networkScoreF = networkScoreService2;
                                    locationF = location;
                                    countryDetectorF = countryDetectorService;
                                    networkTimeUpdaterF = networkTimeUpdateService2;
                                    commonTimeMgmtServiceF = commonTimeManagementService2;
                                    atlasF = assetAtlasService;
                                    inputManagerF = inputManager;
                                    telephonyRegistryF = telephonyRegistry;
                                    mediaRouterF = mediaRouterService;
                                    mmsServiceF = mmsService;
                                    context = context2;
                                    this.mActivityManagerService.systemReady(/* anonymous class already generated */);
                                }
                                Trace.traceEnd(524288);
                                traceBeginAndSlog("StartCommonTimeManagementService");
                                commonTimeManagementService = new CommonTimeManagementService(context2);
                                try {
                                    ServiceManager.addService("commontime_management", commonTimeManagementService);
                                    commonTimeManagementService2 = commonTimeManagementService;
                                } catch (Throwable th13) {
                                    e322222222222222222 = th13;
                                    commonTimeManagementService2 = commonTimeManagementService;
                                    reportWtf("starting CommonTimeManagementService service", e322222222222222222);
                                    Trace.traceEnd(524288);
                                    if (disableNetwork) {
                                        traceBeginAndSlog("CertBlacklister");
                                        certBlacklister = new CertBlacklister(context2);
                                        Trace.traceEnd(524288);
                                    }
                                    if (disableNonCoreServices) {
                                        this.mSystemServiceManager.startService(DreamManagerService.class);
                                    }
                                    if (disableNonCoreServices) {
                                        traceBeginAndSlog("StartAssetAtlasService");
                                        assetAtlasService2 = new AssetAtlasService(context2);
                                        ServiceManager.addService(AssetAtlasService.ASSET_ATLAS_SERVICE, assetAtlasService2);
                                        assetAtlasService = assetAtlasService2;
                                        Trace.traceEnd(524288);
                                    }
                                    if (disableNonCoreServices) {
                                        ServiceManager.addService(GraphicsStatsService.GRAPHICS_STATS_SERVICE, new GraphicsStatsService(context2));
                                    }
                                    if (this.mPackageManager.hasSystemFeature("android.software.print")) {
                                        this.mSystemServiceManager.startService(PRINT_MANAGER_SERVICE_CLASS);
                                    }
                                    this.mSystemServiceManager.startService(RestrictionsManagerService.class);
                                    this.mSystemServiceManager.startService(MediaSessionService.class);
                                    if (this.mPackageManager.hasSystemFeature("android.hardware.hdmi.cec")) {
                                        this.mSystemServiceManager.startService(HdmiControlService.class);
                                    }
                                    if (this.mPackageManager.hasSystemFeature("android.software.live_tv")) {
                                        this.mSystemServiceManager.startService(TvInputManagerService.class);
                                    }
                                    if (this.mPackageManager.hasSystemFeature("android.software.picture_in_picture")) {
                                        this.mSystemServiceManager.startService(MediaResourceMonitorService.class);
                                    }
                                    if (this.mPackageManager.hasSystemFeature("android.software.leanback")) {
                                        this.mSystemServiceManager.startService(TvRemoteService.class);
                                    }
                                    if (disableNonCoreServices) {
                                        traceBeginAndSlog("StartMediaRouterService");
                                        mediaRouterService2 = new MediaRouterService(context2);
                                        ServiceManager.addService("media_router", mediaRouterService2);
                                        mediaRouterService = mediaRouterService2;
                                        Trace.traceEnd(524288);
                                        if (disableTrustManager) {
                                            this.mSystemServiceManager.startService(TrustManagerService.class);
                                        }
                                        traceBeginAndSlog("StartBackgroundDexOptService");
                                        BackgroundDexOptService.schedule(context2);
                                        Trace.traceEnd(524288);
                                    }
                                    this.mSystemServiceManager.startService(ShortcutService.Lifecycle.class);
                                    this.mSystemServiceManager.startService(LauncherAppsService.class);
                                    this.mSystemServiceManager.startService(MediaProjectionManagerService.class);
                                    if (disableNonCoreServices) {
                                        this.mSystemServiceManager.startService("com.android.server.NonHardwareAcceleratedPackagesManagerService");
                                    }
                                    if (disableNonCoreServices) {
                                        jankshield = HwServiceFactory.getJankShieldService();
                                        if (jankshield != null) {
                                            ServiceManager.addService("jankshield", jankshield.getInstance(context2));
                                        }
                                    }
                                    if (context2.getPackageManager().hasSystemFeature("android.hardware.type.watch")) {
                                        this.mSystemServiceManager.startService(WEAR_BLUETOOTH_SERVICE_CLASS);
                                    }
                                    safeMode = windowManagerService.detectSafeMode();
                                    this.mSystemServiceManager.setSafeMode(safeMode);
                                    if (safeMode) {
                                        this.mActivityManagerService.enterSafeMode();
                                        VMRuntime.getRuntime().disableJitCompilation();
                                    } else {
                                        VMRuntime.getRuntime().startJitCompilation();
                                    }
                                    mmsService = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                                    this.mSystemServiceManager.startService("com.android.server.HwCoreAppHelperService");
                                    if ("true".equals(SystemProperties.get("bastet.service.enable", "false"))) {
                                        this.mSystemServiceManager.startService("com.android.server.HwBastetService");
                                    }
                                    this.mSystemServiceManager.startService("com.android.server.HwPPTBService");
                                    if ("true".equals(SystemProperties.get("ro.config.hw_emcom", "false"))) {
                                        startEmcomService();
                                    }
                                    Trace.traceBegin(524288, "MakeVibratorServiceReady");
                                    vibratorService.systemReady();
                                    Trace.traceEnd(524288);
                                    Trace.traceBegin(524288, "MakeLockSettingsServiceReady");
                                    if (iLockSettings != null) {
                                        iLockSettings.systemReady();
                                    }
                                    Trace.traceEnd(524288);
                                    this.mSystemServiceManager.startBootPhase(SystemService.PHASE_LOCK_SETTINGS_READY);
                                    this.mSystemServiceManager.startBootPhase(SystemService.PHASE_SYSTEM_SERVICES_READY);
                                    Trace.traceBegin(524288, "MakeWindowManagerServiceReady");
                                    windowManagerService.systemReady();
                                    Trace.traceEnd(524288);
                                    if (safeMode) {
                                        this.mActivityManagerService.showSafeModeOverlay();
                                    }
                                    config = windowManagerService.computeNewConfiguration();
                                    metrics = new DisplayMetrics();
                                    ((WindowManager) context2.getSystemService("window")).getDefaultDisplay().getMetrics(metrics);
                                    context2.getResources().updateConfiguration(config, metrics);
                                    systemTheme = context2.getTheme();
                                    if (systemTheme.getChangingConfigurations() != 0) {
                                        systemTheme.rebase();
                                    }
                                    Trace.traceBegin(524288, "MakePowerManagerServiceReady");
                                    this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                                    Trace.traceEnd(524288);
                                    Trace.traceEnd(524288);
                                    this.mPGManagerService.systemReady(this.mActivityManagerService, this.mPowerManagerService, location);
                                    Trace.traceBegin(524288, "MakePackageManagerServiceReady");
                                    this.mPackageManagerService.systemReady();
                                    Trace.traceEnd(524288);
                                    Trace.traceBegin(524288, "MakeDisplayManagerServiceReady");
                                    this.mDisplayManagerService.systemReady(safeMode, this.mOnlyCore);
                                    Trace.traceEnd(524288);
                                    networkManagementF = networkManagement;
                                    networkStatsF = networkStats;
                                    networkPolicyF = networkPolicy;
                                    connectivityF = connectivity;
                                    networkScoreF = networkScoreService2;
                                    locationF = location;
                                    countryDetectorF = countryDetectorService;
                                    networkTimeUpdaterF = networkTimeUpdateService2;
                                    commonTimeMgmtServiceF = commonTimeManagementService2;
                                    atlasF = assetAtlasService;
                                    inputManagerF = inputManager;
                                    telephonyRegistryF = telephonyRegistry;
                                    mediaRouterF = mediaRouterService;
                                    mmsServiceF = mmsService;
                                    context = context2;
                                    this.mActivityManagerService.systemReady(/* anonymous class already generated */);
                                }
                                Trace.traceEnd(524288);
                                if (disableNetwork) {
                                    traceBeginAndSlog("CertBlacklister");
                                    certBlacklister = new CertBlacklister(context2);
                                    Trace.traceEnd(524288);
                                }
                                if (disableNonCoreServices) {
                                    this.mSystemServiceManager.startService(DreamManagerService.class);
                                }
                                if (disableNonCoreServices) {
                                    traceBeginAndSlog("StartAssetAtlasService");
                                    assetAtlasService2 = new AssetAtlasService(context2);
                                    ServiceManager.addService(AssetAtlasService.ASSET_ATLAS_SERVICE, assetAtlasService2);
                                    assetAtlasService = assetAtlasService2;
                                    Trace.traceEnd(524288);
                                }
                                if (disableNonCoreServices) {
                                    ServiceManager.addService(GraphicsStatsService.GRAPHICS_STATS_SERVICE, new GraphicsStatsService(context2));
                                }
                                if (this.mPackageManager.hasSystemFeature("android.software.print")) {
                                    this.mSystemServiceManager.startService(PRINT_MANAGER_SERVICE_CLASS);
                                }
                                this.mSystemServiceManager.startService(RestrictionsManagerService.class);
                                this.mSystemServiceManager.startService(MediaSessionService.class);
                                if (this.mPackageManager.hasSystemFeature("android.hardware.hdmi.cec")) {
                                    this.mSystemServiceManager.startService(HdmiControlService.class);
                                }
                                if (this.mPackageManager.hasSystemFeature("android.software.live_tv")) {
                                    this.mSystemServiceManager.startService(TvInputManagerService.class);
                                }
                                if (this.mPackageManager.hasSystemFeature("android.software.picture_in_picture")) {
                                    this.mSystemServiceManager.startService(MediaResourceMonitorService.class);
                                }
                                if (this.mPackageManager.hasSystemFeature("android.software.leanback")) {
                                    this.mSystemServiceManager.startService(TvRemoteService.class);
                                }
                                if (disableNonCoreServices) {
                                    traceBeginAndSlog("StartMediaRouterService");
                                    mediaRouterService2 = new MediaRouterService(context2);
                                    ServiceManager.addService("media_router", mediaRouterService2);
                                    mediaRouterService = mediaRouterService2;
                                    Trace.traceEnd(524288);
                                    if (disableTrustManager) {
                                        this.mSystemServiceManager.startService(TrustManagerService.class);
                                    }
                                    traceBeginAndSlog("StartBackgroundDexOptService");
                                    BackgroundDexOptService.schedule(context2);
                                    Trace.traceEnd(524288);
                                }
                                this.mSystemServiceManager.startService(ShortcutService.Lifecycle.class);
                                this.mSystemServiceManager.startService(LauncherAppsService.class);
                                this.mSystemServiceManager.startService(MediaProjectionManagerService.class);
                                if (disableNonCoreServices) {
                                    this.mSystemServiceManager.startService("com.android.server.NonHardwareAcceleratedPackagesManagerService");
                                }
                                if (disableNonCoreServices) {
                                    jankshield = HwServiceFactory.getJankShieldService();
                                    if (jankshield != null) {
                                        ServiceManager.addService("jankshield", jankshield.getInstance(context2));
                                    }
                                }
                                if (context2.getPackageManager().hasSystemFeature("android.hardware.type.watch")) {
                                    this.mSystemServiceManager.startService(WEAR_BLUETOOTH_SERVICE_CLASS);
                                }
                                safeMode = windowManagerService.detectSafeMode();
                                this.mSystemServiceManager.setSafeMode(safeMode);
                                if (safeMode) {
                                    this.mActivityManagerService.enterSafeMode();
                                    VMRuntime.getRuntime().disableJitCompilation();
                                } else {
                                    VMRuntime.getRuntime().startJitCompilation();
                                }
                                mmsService = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                                this.mSystemServiceManager.startService("com.android.server.HwCoreAppHelperService");
                                if ("true".equals(SystemProperties.get("bastet.service.enable", "false"))) {
                                    this.mSystemServiceManager.startService("com.android.server.HwBastetService");
                                }
                                this.mSystemServiceManager.startService("com.android.server.HwPPTBService");
                                if ("true".equals(SystemProperties.get("ro.config.hw_emcom", "false"))) {
                                    startEmcomService();
                                }
                                Trace.traceBegin(524288, "MakeVibratorServiceReady");
                                vibratorService.systemReady();
                                Trace.traceEnd(524288);
                                Trace.traceBegin(524288, "MakeLockSettingsServiceReady");
                                if (iLockSettings != null) {
                                    iLockSettings.systemReady();
                                }
                                Trace.traceEnd(524288);
                                this.mSystemServiceManager.startBootPhase(SystemService.PHASE_LOCK_SETTINGS_READY);
                                this.mSystemServiceManager.startBootPhase(SystemService.PHASE_SYSTEM_SERVICES_READY);
                                Trace.traceBegin(524288, "MakeWindowManagerServiceReady");
                                windowManagerService.systemReady();
                                Trace.traceEnd(524288);
                                if (safeMode) {
                                    this.mActivityManagerService.showSafeModeOverlay();
                                }
                                config = windowManagerService.computeNewConfiguration();
                                metrics = new DisplayMetrics();
                                ((WindowManager) context2.getSystemService("window")).getDefaultDisplay().getMetrics(metrics);
                                context2.getResources().updateConfiguration(config, metrics);
                                systemTheme = context2.getTheme();
                                if (systemTheme.getChangingConfigurations() != 0) {
                                    systemTheme.rebase();
                                }
                                Trace.traceBegin(524288, "MakePowerManagerServiceReady");
                                this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                                Trace.traceEnd(524288);
                                Trace.traceEnd(524288);
                                this.mPGManagerService.systemReady(this.mActivityManagerService, this.mPowerManagerService, location);
                                Trace.traceBegin(524288, "MakePackageManagerServiceReady");
                                this.mPackageManagerService.systemReady();
                                Trace.traceEnd(524288);
                                Trace.traceBegin(524288, "MakeDisplayManagerServiceReady");
                                this.mDisplayManagerService.systemReady(safeMode, this.mOnlyCore);
                                Trace.traceEnd(524288);
                                networkManagementF = networkManagement;
                                networkStatsF = networkStats;
                                networkPolicyF = networkPolicy;
                                connectivityF = connectivity;
                                networkScoreF = networkScoreService2;
                                locationF = location;
                                countryDetectorF = countryDetectorService;
                                networkTimeUpdaterF = networkTimeUpdateService2;
                                commonTimeMgmtServiceF = commonTimeManagementService2;
                                atlasF = assetAtlasService;
                                inputManagerF = inputManager;
                                telephonyRegistryF = telephonyRegistry;
                                mediaRouterF = mediaRouterService;
                                mmsServiceF = mmsService;
                                context = context2;
                                this.mActivityManagerService.systemReady(/* anonymous class already generated */);
                            }
                        } catch (Throwable th14) {
                            e322222222222222222 = th14;
                            reportWtf("starting Network Score Service", e322222222222222222);
                            Trace.traceEnd(524288);
                            traceBeginAndSlog("StartNetworkStatsService");
                            networkStats = NetworkStatsService.create(context2, networkManagement);
                            ServiceManager.addService("netstats", networkStats);
                            Trace.traceEnd(524288);
                            traceBeginAndSlog("StartNetworkPolicyManagerService");
                            networkPolicy = HwServiceFactory.getHwNetworkPolicyManagerService().getInstance(context2, this.mActivityManagerService, (IPowerManager) ServiceManager.getService("power"), networkStats, networkManagement);
                            ServiceManager.addService("netpolicy", networkPolicy);
                            Trace.traceEnd(524288);
                            if (context2.getPackageManager().hasSystemFeature("android.hardware.wifi.nan")) {
                                this.mSystemServiceManager.startService(WIFI_NAN_SERVICE_CLASS);
                            } else {
                                Slog.i(TAG, "No Wi-Fi NAN Service (NAN support Not Present)");
                            }
                            this.mSystemServiceManager.startService(WIFI_P2P_SERVICE_CLASS);
                            this.mSystemServiceManager.startService(WIFI_SERVICE_CLASS);
                            this.mSystemServiceManager.startService("com.android.server.wifi.scanner.WifiScanningService");
                            if (disableRtt) {
                                this.mSystemServiceManager.startService("com.android.server.wifi.RttService");
                            }
                            this.mSystemServiceManager.startService(ETHERNET_SERVICE_CLASS);
                            traceBeginAndSlog("StartConnectivityService");
                            Slog.i(TAG, "Connectivity Service");
                            connectivity = HwServiceFactory.getHwConnectivityManager().createHwConnectivityService(context2, networkManagement, networkStats, networkPolicy);
                            ServiceManager.addService("connectivity", connectivity);
                            networkStats.bindConnectivityManager(connectivity);
                            networkPolicy.bindConnectivityManager(connectivity);
                            Trace.traceEnd(524288);
                            traceBeginAndSlog("StartNsdService");
                            ServiceManager.addService("servicediscovery", NsdService.create(context2));
                            Trace.traceEnd(524288);
                            if (disableNonCoreServices) {
                                traceBeginAndSlog("StartUpdateLockService");
                                ServiceManager.addService("updatelock", new UpdateLockService(context2));
                                Trace.traceEnd(524288);
                            }
                            if (disableNonCoreServices) {
                                this.mSystemServiceManager.startService(RecoverySystemService.class);
                            }
                            Trace.traceBegin(524288, "WaitForAsecScan");
                            mountService.waitForAsecScan();
                            Trace.traceEnd(524288);
                            this.mSystemServiceManager.startService("com.android.server.notification.HwNotificationManagerService");
                            notification = INotificationManager.Stub.asInterface(ServiceManager.getService("notification"));
                            if (networkPolicy != null) {
                                networkPolicy.bindNotificationManager(notification);
                            }
                            this.mSystemServiceManager.startService(HwServiceFactory.getDeviceStorageMonitorServiceClassName());
                            Slog.i(TAG, "TUI Connect enable " + tuiEnable);
                            if (tuiEnable) {
                                ServiceManager.addService("tui", new TrustedUIService(context2));
                            }
                            if (vrDisplayEnable) {
                                Slog.i(TAG, "VR Display enable " + vrDisplayEnable);
                                ServiceManager.addService("vr_display", new VRManagerService(context2));
                            }
                            if (disableLocation) {
                                traceBeginAndSlog("StartLocationManagerService");
                                Slog.i(TAG, "Location Manager");
                                hwLocation = HwServiceFactory.getHwLocationManagerService();
                                if (hwLocation == null) {
                                    location = hwLocation.getInstance(context2);
                                } else {
                                    location = new LocationManagerService(context2);
                                }
                                ServiceManager.addService("location", location);
                                Trace.traceEnd(524288);
                                traceBeginAndSlog("StartCountryDetectorService");
                                countryDetectorService2 = new CountryDetectorService(context2);
                                ServiceManager.addService("country_detector", countryDetectorService2);
                                countryDetectorService = countryDetectorService2;
                                Trace.traceEnd(524288);
                            }
                            traceBeginAndSlog("StartSearchManagerService");
                            this.mSystemServiceManager.startService(SEARCH_MANAGER_SERVICE_CLASS);
                            Trace.traceEnd(524288);
                            this.mSystemServiceManager.startService(DropBoxManagerService.class);
                            traceBeginAndSlog("StartWallpaperManagerService");
                            this.mSystemServiceManager.startService(HwServiceFactory.getWallpaperManagerServiceClassName());
                            Trace.traceEnd(524288);
                            traceBeginAndSlog("StartAudioService");
                            this.mSystemServiceManager.startService(AudioService.Lifecycle.class);
                            Trace.traceEnd(524288);
                            if (disableNonCoreServices) {
                                this.mSystemServiceManager.startService(DockObserver.class);
                                if (context2.getPackageManager().hasSystemFeature("android.hardware.type.watch")) {
                                    this.mSystemServiceManager.startService(THERMAL_OBSERVER_CLASS);
                                }
                            }
                            traceBeginAndSlog("StartWiredAccessoryManager");
                            inputManager.setWiredAccessoryCallbacks(new WiredAccessoryManager(context2, inputManager));
                            Trace.traceEnd(524288);
                            if (disableNonCoreServices) {
                                if (this.mPackageManager.hasSystemFeature("android.software.midi")) {
                                    this.mSystemServiceManager.startService(MIDI_SERVICE_CLASS);
                                }
                                Trace.traceBegin(524288, "StartUsbService");
                                this.mSystemServiceManager.startService(USB_SERVICE_CLASS);
                                Trace.traceEnd(524288);
                                if (disableSerial) {
                                    traceBeginAndSlog("StartSerialService");
                                    serialService = new SerialService(context2);
                                    ServiceManager.addService("serial", serialService);
                                    serialService2 = serialService;
                                    Trace.traceEnd(524288);
                                }
                                Trace.traceBegin(524288, "StartHardwarePropertiesManagerService");
                                hardwarePropertiesManagerService = new HardwarePropertiesManagerService(context2);
                                ServiceManager.addService("hardware_properties", hardwarePropertiesManagerService);
                                hardwarePropertiesManagerService2 = hardwarePropertiesManagerService;
                                Trace.traceEnd(524288);
                            }
                            this.mSystemServiceManager.startService(TwilightService.class);
                            this.mSystemServiceManager.startService("com.android.server.job.HwJobSchedulerService");
                            this.mSystemServiceManager.startService(SoundTriggerService.class);
                            if (disableNonCoreServices) {
                                if (this.mPackageManager.hasSystemFeature("android.software.backup")) {
                                    this.mSystemServiceManager.startService(BACKUP_MANAGER_SERVICE_CLASS);
                                }
                                this.mSystemServiceManager.startService(APPWIDGET_SERVICE_CLASS);
                                if (this.mPackageManager.hasSystemFeature("android.software.voice_recognizers")) {
                                    this.mSystemServiceManager.startService(VOICE_RECOGNITION_MANAGER_SERVICE_CLASS);
                                }
                                if (GestureLauncherService.isGestureLauncherEnabled(context2.getResources())) {
                                    Slog.i(TAG, "Gesture Launcher Service");
                                    this.mSystemServiceManager.startService(GestureLauncherService.class);
                                }
                                this.mSystemServiceManager.startService(SensorNotificationService.class);
                                this.mSystemServiceManager.startService(ContextHubSystemService.class);
                            }
                            HwServiceFactory.setupHwServices(context2);
                            traceBeginAndSlog("StartDiskStatsService");
                            ServiceManager.addService("diskstats", new DiskStatsService(context2));
                            Trace.traceEnd(524288);
                            if (disableSamplingProfiler) {
                                traceBeginAndSlog("StartSamplingProfilerService");
                                ServiceManager.addService("samplingprofiler", new SamplingProfilerService(context2));
                                Trace.traceEnd(524288);
                            }
                            Slog.i(TAG, "attestation Service");
                            attestation = HwServiceFactory.getHwAttestationService();
                            if (attestation != null) {
                                ServiceManager.addService("attestation_service", attestation.getInstance(context2));
                            }
                            traceBeginAndSlog("StartNetworkTimeUpdateService");
                            networkTimeUpdateService = new NetworkTimeUpdateService(context2);
                            ServiceManager.addService("network_time_update_service", networkTimeUpdateService);
                            networkTimeUpdateService2 = networkTimeUpdateService;
                            Trace.traceEnd(524288);
                            traceBeginAndSlog("StartCommonTimeManagementService");
                            commonTimeManagementService = new CommonTimeManagementService(context2);
                            ServiceManager.addService("commontime_management", commonTimeManagementService);
                            commonTimeManagementService2 = commonTimeManagementService;
                            Trace.traceEnd(524288);
                            if (disableNetwork) {
                                traceBeginAndSlog("CertBlacklister");
                                certBlacklister = new CertBlacklister(context2);
                                Trace.traceEnd(524288);
                            }
                            if (disableNonCoreServices) {
                                this.mSystemServiceManager.startService(DreamManagerService.class);
                            }
                            if (disableNonCoreServices) {
                                traceBeginAndSlog("StartAssetAtlasService");
                                assetAtlasService2 = new AssetAtlasService(context2);
                                ServiceManager.addService(AssetAtlasService.ASSET_ATLAS_SERVICE, assetAtlasService2);
                                assetAtlasService = assetAtlasService2;
                                Trace.traceEnd(524288);
                            }
                            if (disableNonCoreServices) {
                                ServiceManager.addService(GraphicsStatsService.GRAPHICS_STATS_SERVICE, new GraphicsStatsService(context2));
                            }
                            if (this.mPackageManager.hasSystemFeature("android.software.print")) {
                                this.mSystemServiceManager.startService(PRINT_MANAGER_SERVICE_CLASS);
                            }
                            this.mSystemServiceManager.startService(RestrictionsManagerService.class);
                            this.mSystemServiceManager.startService(MediaSessionService.class);
                            if (this.mPackageManager.hasSystemFeature("android.hardware.hdmi.cec")) {
                                this.mSystemServiceManager.startService(HdmiControlService.class);
                            }
                            if (this.mPackageManager.hasSystemFeature("android.software.live_tv")) {
                                this.mSystemServiceManager.startService(TvInputManagerService.class);
                            }
                            if (this.mPackageManager.hasSystemFeature("android.software.picture_in_picture")) {
                                this.mSystemServiceManager.startService(MediaResourceMonitorService.class);
                            }
                            if (this.mPackageManager.hasSystemFeature("android.software.leanback")) {
                                this.mSystemServiceManager.startService(TvRemoteService.class);
                            }
                            if (disableNonCoreServices) {
                                traceBeginAndSlog("StartMediaRouterService");
                                mediaRouterService2 = new MediaRouterService(context2);
                                ServiceManager.addService("media_router", mediaRouterService2);
                                mediaRouterService = mediaRouterService2;
                                Trace.traceEnd(524288);
                                if (disableTrustManager) {
                                    this.mSystemServiceManager.startService(TrustManagerService.class);
                                }
                                traceBeginAndSlog("StartBackgroundDexOptService");
                                BackgroundDexOptService.schedule(context2);
                                Trace.traceEnd(524288);
                            }
                            this.mSystemServiceManager.startService(ShortcutService.Lifecycle.class);
                            this.mSystemServiceManager.startService(LauncherAppsService.class);
                            this.mSystemServiceManager.startService(MediaProjectionManagerService.class);
                            if (disableNonCoreServices) {
                                this.mSystemServiceManager.startService("com.android.server.NonHardwareAcceleratedPackagesManagerService");
                            }
                            if (disableNonCoreServices) {
                                jankshield = HwServiceFactory.getJankShieldService();
                                if (jankshield != null) {
                                    ServiceManager.addService("jankshield", jankshield.getInstance(context2));
                                }
                            }
                            if (context2.getPackageManager().hasSystemFeature("android.hardware.type.watch")) {
                                this.mSystemServiceManager.startService(WEAR_BLUETOOTH_SERVICE_CLASS);
                            }
                            safeMode = windowManagerService.detectSafeMode();
                            this.mSystemServiceManager.setSafeMode(safeMode);
                            if (safeMode) {
                                VMRuntime.getRuntime().startJitCompilation();
                            } else {
                                this.mActivityManagerService.enterSafeMode();
                                VMRuntime.getRuntime().disableJitCompilation();
                            }
                            mmsService = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                            this.mSystemServiceManager.startService("com.android.server.HwCoreAppHelperService");
                            if ("true".equals(SystemProperties.get("bastet.service.enable", "false"))) {
                                this.mSystemServiceManager.startService("com.android.server.HwBastetService");
                            }
                            this.mSystemServiceManager.startService("com.android.server.HwPPTBService");
                            if ("true".equals(SystemProperties.get("ro.config.hw_emcom", "false"))) {
                                startEmcomService();
                            }
                            Trace.traceBegin(524288, "MakeVibratorServiceReady");
                            vibratorService.systemReady();
                            Trace.traceEnd(524288);
                            Trace.traceBegin(524288, "MakeLockSettingsServiceReady");
                            if (iLockSettings != null) {
                                iLockSettings.systemReady();
                            }
                            Trace.traceEnd(524288);
                            this.mSystemServiceManager.startBootPhase(SystemService.PHASE_LOCK_SETTINGS_READY);
                            this.mSystemServiceManager.startBootPhase(SystemService.PHASE_SYSTEM_SERVICES_READY);
                            Trace.traceBegin(524288, "MakeWindowManagerServiceReady");
                            windowManagerService.systemReady();
                            Trace.traceEnd(524288);
                            if (safeMode) {
                                this.mActivityManagerService.showSafeModeOverlay();
                            }
                            config = windowManagerService.computeNewConfiguration();
                            metrics = new DisplayMetrics();
                            ((WindowManager) context2.getSystemService("window")).getDefaultDisplay().getMetrics(metrics);
                            context2.getResources().updateConfiguration(config, metrics);
                            systemTheme = context2.getTheme();
                            if (systemTheme.getChangingConfigurations() != 0) {
                                systemTheme.rebase();
                            }
                            Trace.traceBegin(524288, "MakePowerManagerServiceReady");
                            this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                            Trace.traceEnd(524288);
                            Trace.traceEnd(524288);
                            this.mPGManagerService.systemReady(this.mActivityManagerService, this.mPowerManagerService, location);
                            Trace.traceBegin(524288, "MakePackageManagerServiceReady");
                            this.mPackageManagerService.systemReady();
                            Trace.traceEnd(524288);
                            Trace.traceBegin(524288, "MakeDisplayManagerServiceReady");
                            this.mDisplayManagerService.systemReady(safeMode, this.mOnlyCore);
                            Trace.traceEnd(524288);
                            networkManagementF = networkManagement;
                            networkStatsF = networkStats;
                            networkPolicyF = networkPolicy;
                            connectivityF = connectivity;
                            networkScoreF = networkScoreService2;
                            locationF = location;
                            countryDetectorF = countryDetectorService;
                            networkTimeUpdaterF = networkTimeUpdateService2;
                            commonTimeMgmtServiceF = commonTimeManagementService2;
                            atlasF = assetAtlasService;
                            inputManagerF = inputManager;
                            telephonyRegistryF = telephonyRegistry;
                            mediaRouterF = mediaRouterService;
                            mmsServiceF = mmsService;
                            context = context2;
                            this.mActivityManagerService.systemReady(/* anonymous class already generated */);
                        }
                        Trace.traceEnd(524288);
                        traceBeginAndSlog("StartNetworkStatsService");
                        try {
                            networkStats = NetworkStatsService.create(context2, networkManagement);
                            ServiceManager.addService("netstats", networkStats);
                        } catch (Throwable e3222222222222222222) {
                            reportWtf("starting NetworkStats Service", e3222222222222222222);
                        }
                        Trace.traceEnd(524288);
                        traceBeginAndSlog("StartNetworkPolicyManagerService");
                        try {
                            networkPolicy = HwServiceFactory.getHwNetworkPolicyManagerService().getInstance(context2, this.mActivityManagerService, (IPowerManager) ServiceManager.getService("power"), networkStats, networkManagement);
                            ServiceManager.addService("netpolicy", networkPolicy);
                        } catch (Throwable e32222222222222222222) {
                            reportWtf("starting NetworkPolicy Service", e32222222222222222222);
                        }
                        Trace.traceEnd(524288);
                        if (context2.getPackageManager().hasSystemFeature("android.hardware.wifi.nan")) {
                            this.mSystemServiceManager.startService(WIFI_NAN_SERVICE_CLASS);
                        } else {
                            Slog.i(TAG, "No Wi-Fi NAN Service (NAN support Not Present)");
                        }
                        this.mSystemServiceManager.startService(WIFI_P2P_SERVICE_CLASS);
                        this.mSystemServiceManager.startService(WIFI_SERVICE_CLASS);
                        this.mSystemServiceManager.startService("com.android.server.wifi.scanner.WifiScanningService");
                        if (disableRtt) {
                            this.mSystemServiceManager.startService("com.android.server.wifi.RttService");
                        }
                        this.mSystemServiceManager.startService(ETHERNET_SERVICE_CLASS);
                        traceBeginAndSlog("StartConnectivityService");
                        try {
                            Slog.i(TAG, "Connectivity Service");
                            connectivity = HwServiceFactory.getHwConnectivityManager().createHwConnectivityService(context2, networkManagement, networkStats, networkPolicy);
                            ServiceManager.addService("connectivity", connectivity);
                            networkStats.bindConnectivityManager(connectivity);
                            networkPolicy.bindConnectivityManager(connectivity);
                        } catch (Throwable e322222222222222222222) {
                            reportWtf("starting Connectivity Service", e322222222222222222222);
                        }
                        Trace.traceEnd(524288);
                        traceBeginAndSlog("StartNsdService");
                        try {
                            ServiceManager.addService("servicediscovery", NsdService.create(context2));
                        } catch (Throwable e3222222222222222222222) {
                            reportWtf("starting Service Discovery Service", e3222222222222222222222);
                        }
                        Trace.traceEnd(524288);
                    }
                    if (disableNonCoreServices) {
                        traceBeginAndSlog("StartUpdateLockService");
                        ServiceManager.addService("updatelock", new UpdateLockService(context2));
                        Trace.traceEnd(524288);
                    }
                    if (disableNonCoreServices) {
                        this.mSystemServiceManager.startService(RecoverySystemService.class);
                    }
                    Trace.traceBegin(524288, "WaitForAsecScan");
                    try {
                        mountService.waitForAsecScan();
                    } catch (RemoteException e8) {
                    }
                    Trace.traceEnd(524288);
                    try {
                        this.mSystemServiceManager.startService("com.android.server.notification.HwNotificationManagerService");
                    } catch (RuntimeException e9) {
                        this.mSystemServiceManager.startService(NotificationManagerService.class);
                    }
                    notification = INotificationManager.Stub.asInterface(ServiceManager.getService("notification"));
                    if (networkPolicy != null) {
                        networkPolicy.bindNotificationManager(notification);
                    }
                    this.mSystemServiceManager.startService(HwServiceFactory.getDeviceStorageMonitorServiceClassName());
                    Slog.i(TAG, "TUI Connect enable " + tuiEnable);
                    if (tuiEnable) {
                        ServiceManager.addService("tui", new TrustedUIService(context2));
                    }
                    if (vrDisplayEnable) {
                        Slog.i(TAG, "VR Display enable " + vrDisplayEnable);
                        ServiceManager.addService("vr_display", new VRManagerService(context2));
                    }
                    if (disableLocation) {
                        traceBeginAndSlog("StartLocationManagerService");
                        Slog.i(TAG, "Location Manager");
                        hwLocation = HwServiceFactory.getHwLocationManagerService();
                        if (hwLocation == null) {
                            location = hwLocation.getInstance(context2);
                        } else {
                            location = new LocationManagerService(context2);
                        }
                        ServiceManager.addService("location", location);
                        Trace.traceEnd(524288);
                        traceBeginAndSlog("StartCountryDetectorService");
                        countryDetectorService2 = new CountryDetectorService(context2);
                        ServiceManager.addService("country_detector", countryDetectorService2);
                        countryDetectorService = countryDetectorService2;
                        Trace.traceEnd(524288);
                    }
                    traceBeginAndSlog("StartSearchManagerService");
                    try {
                        this.mSystemServiceManager.startService(SEARCH_MANAGER_SERVICE_CLASS);
                    } catch (Throwable e32222222222222222222222) {
                        reportWtf("starting Search Service", e32222222222222222222222);
                    }
                    Trace.traceEnd(524288);
                    this.mSystemServiceManager.startService(DropBoxManagerService.class);
                    traceBeginAndSlog("StartWallpaperManagerService");
                    this.mSystemServiceManager.startService(HwServiceFactory.getWallpaperManagerServiceClassName());
                    Trace.traceEnd(524288);
                    traceBeginAndSlog("StartAudioService");
                    this.mSystemServiceManager.startService(AudioService.Lifecycle.class);
                    Trace.traceEnd(524288);
                    if (disableNonCoreServices) {
                        this.mSystemServiceManager.startService(DockObserver.class);
                        if (context2.getPackageManager().hasSystemFeature("android.hardware.type.watch")) {
                            this.mSystemServiceManager.startService(THERMAL_OBSERVER_CLASS);
                        }
                    }
                    traceBeginAndSlog("StartWiredAccessoryManager");
                    try {
                        inputManager.setWiredAccessoryCallbacks(new WiredAccessoryManager(context2, inputManager));
                    } catch (Throwable e322222222222222222222222) {
                        reportWtf("starting WiredAccessoryManager", e322222222222222222222222);
                    }
                    Trace.traceEnd(524288);
                    if (disableNonCoreServices) {
                        if (this.mPackageManager.hasSystemFeature("android.software.midi")) {
                            this.mSystemServiceManager.startService(MIDI_SERVICE_CLASS);
                        }
                        Trace.traceBegin(524288, "StartUsbService");
                        this.mSystemServiceManager.startService(USB_SERVICE_CLASS);
                        Trace.traceEnd(524288);
                        if (disableSerial) {
                            traceBeginAndSlog("StartSerialService");
                            serialService = new SerialService(context2);
                            ServiceManager.addService("serial", serialService);
                            serialService2 = serialService;
                            Trace.traceEnd(524288);
                        }
                        Trace.traceBegin(524288, "StartHardwarePropertiesManagerService");
                        hardwarePropertiesManagerService = new HardwarePropertiesManagerService(context2);
                        ServiceManager.addService("hardware_properties", hardwarePropertiesManagerService);
                        hardwarePropertiesManagerService2 = hardwarePropertiesManagerService;
                        Trace.traceEnd(524288);
                    }
                    this.mSystemServiceManager.startService(TwilightService.class);
                    try {
                        this.mSystemServiceManager.startService("com.android.server.job.HwJobSchedulerService");
                    } catch (RuntimeException e10) {
                        Slog.w(TAG, "create HwJobSchedulerService failed");
                        this.mSystemServiceManager.startService(JobSchedulerService.class);
                    }
                    this.mSystemServiceManager.startService(SoundTriggerService.class);
                    if (disableNonCoreServices) {
                        if (this.mPackageManager.hasSystemFeature("android.software.backup")) {
                            this.mSystemServiceManager.startService(BACKUP_MANAGER_SERVICE_CLASS);
                        }
                        this.mSystemServiceManager.startService(APPWIDGET_SERVICE_CLASS);
                        if (this.mPackageManager.hasSystemFeature("android.software.voice_recognizers")) {
                            this.mSystemServiceManager.startService(VOICE_RECOGNITION_MANAGER_SERVICE_CLASS);
                        }
                        if (GestureLauncherService.isGestureLauncherEnabled(context2.getResources())) {
                            Slog.i(TAG, "Gesture Launcher Service");
                            this.mSystemServiceManager.startService(GestureLauncherService.class);
                        }
                        this.mSystemServiceManager.startService(SensorNotificationService.class);
                        this.mSystemServiceManager.startService(ContextHubSystemService.class);
                    }
                    HwServiceFactory.setupHwServices(context2);
                    traceBeginAndSlog("StartDiskStatsService");
                    try {
                        ServiceManager.addService("diskstats", new DiskStatsService(context2));
                    } catch (Throwable e3222222222222222222222222) {
                        reportWtf("starting DiskStats Service", e3222222222222222222222222);
                    }
                    Trace.traceEnd(524288);
                    if (disableSamplingProfiler) {
                        traceBeginAndSlog("StartSamplingProfilerService");
                        ServiceManager.addService("samplingprofiler", new SamplingProfilerService(context2));
                        Trace.traceEnd(524288);
                    }
                    try {
                        Slog.i(TAG, "attestation Service");
                        attestation = HwServiceFactory.getHwAttestationService();
                        if (attestation != null) {
                            ServiceManager.addService("attestation_service", attestation.getInstance(context2));
                        }
                    } catch (Throwable e32222222222222222222222222) {
                        Slog.i(TAG, "attestation_service failed");
                        reportWtf("attestation Service", e32222222222222222222222222);
                    }
                    traceBeginAndSlog("StartNetworkTimeUpdateService");
                    try {
                        networkTimeUpdateService = new NetworkTimeUpdateService(context2);
                        ServiceManager.addService("network_time_update_service", networkTimeUpdateService);
                        networkTimeUpdateService2 = networkTimeUpdateService;
                    } catch (Throwable th15) {
                        e32222222222222222222222222 = th15;
                        reportWtf("starting NetworkTimeUpdate service", e32222222222222222222222222);
                        Trace.traceEnd(524288);
                        traceBeginAndSlog("StartCommonTimeManagementService");
                        commonTimeManagementService = new CommonTimeManagementService(context2);
                        ServiceManager.addService("commontime_management", commonTimeManagementService);
                        commonTimeManagementService2 = commonTimeManagementService;
                        Trace.traceEnd(524288);
                        if (disableNetwork) {
                            traceBeginAndSlog("CertBlacklister");
                            certBlacklister = new CertBlacklister(context2);
                            Trace.traceEnd(524288);
                        }
                        if (disableNonCoreServices) {
                            this.mSystemServiceManager.startService(DreamManagerService.class);
                        }
                        if (disableNonCoreServices) {
                            traceBeginAndSlog("StartAssetAtlasService");
                            assetAtlasService2 = new AssetAtlasService(context2);
                            ServiceManager.addService(AssetAtlasService.ASSET_ATLAS_SERVICE, assetAtlasService2);
                            assetAtlasService = assetAtlasService2;
                            Trace.traceEnd(524288);
                        }
                        if (disableNonCoreServices) {
                            ServiceManager.addService(GraphicsStatsService.GRAPHICS_STATS_SERVICE, new GraphicsStatsService(context2));
                        }
                        if (this.mPackageManager.hasSystemFeature("android.software.print")) {
                            this.mSystemServiceManager.startService(PRINT_MANAGER_SERVICE_CLASS);
                        }
                        this.mSystemServiceManager.startService(RestrictionsManagerService.class);
                        this.mSystemServiceManager.startService(MediaSessionService.class);
                        if (this.mPackageManager.hasSystemFeature("android.hardware.hdmi.cec")) {
                            this.mSystemServiceManager.startService(HdmiControlService.class);
                        }
                        if (this.mPackageManager.hasSystemFeature("android.software.live_tv")) {
                            this.mSystemServiceManager.startService(TvInputManagerService.class);
                        }
                        if (this.mPackageManager.hasSystemFeature("android.software.picture_in_picture")) {
                            this.mSystemServiceManager.startService(MediaResourceMonitorService.class);
                        }
                        if (this.mPackageManager.hasSystemFeature("android.software.leanback")) {
                            this.mSystemServiceManager.startService(TvRemoteService.class);
                        }
                        if (disableNonCoreServices) {
                            traceBeginAndSlog("StartMediaRouterService");
                            mediaRouterService2 = new MediaRouterService(context2);
                            ServiceManager.addService("media_router", mediaRouterService2);
                            mediaRouterService = mediaRouterService2;
                            Trace.traceEnd(524288);
                            if (disableTrustManager) {
                                this.mSystemServiceManager.startService(TrustManagerService.class);
                            }
                            traceBeginAndSlog("StartBackgroundDexOptService");
                            BackgroundDexOptService.schedule(context2);
                            Trace.traceEnd(524288);
                        }
                        this.mSystemServiceManager.startService(ShortcutService.Lifecycle.class);
                        this.mSystemServiceManager.startService(LauncherAppsService.class);
                        this.mSystemServiceManager.startService(MediaProjectionManagerService.class);
                        if (disableNonCoreServices) {
                            this.mSystemServiceManager.startService("com.android.server.NonHardwareAcceleratedPackagesManagerService");
                        }
                        if (disableNonCoreServices) {
                            jankshield = HwServiceFactory.getJankShieldService();
                            if (jankshield != null) {
                                ServiceManager.addService("jankshield", jankshield.getInstance(context2));
                            }
                        }
                        if (context2.getPackageManager().hasSystemFeature("android.hardware.type.watch")) {
                            this.mSystemServiceManager.startService(WEAR_BLUETOOTH_SERVICE_CLASS);
                        }
                        safeMode = windowManagerService.detectSafeMode();
                        this.mSystemServiceManager.setSafeMode(safeMode);
                        if (safeMode) {
                            this.mActivityManagerService.enterSafeMode();
                            VMRuntime.getRuntime().disableJitCompilation();
                        } else {
                            VMRuntime.getRuntime().startJitCompilation();
                        }
                        mmsService = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                        this.mSystemServiceManager.startService("com.android.server.HwCoreAppHelperService");
                        if ("true".equals(SystemProperties.get("bastet.service.enable", "false"))) {
                            this.mSystemServiceManager.startService("com.android.server.HwBastetService");
                        }
                        this.mSystemServiceManager.startService("com.android.server.HwPPTBService");
                        if ("true".equals(SystemProperties.get("ro.config.hw_emcom", "false"))) {
                            startEmcomService();
                        }
                        Trace.traceBegin(524288, "MakeVibratorServiceReady");
                        vibratorService.systemReady();
                        Trace.traceEnd(524288);
                        Trace.traceBegin(524288, "MakeLockSettingsServiceReady");
                        if (iLockSettings != null) {
                            iLockSettings.systemReady();
                        }
                        Trace.traceEnd(524288);
                        this.mSystemServiceManager.startBootPhase(SystemService.PHASE_LOCK_SETTINGS_READY);
                        this.mSystemServiceManager.startBootPhase(SystemService.PHASE_SYSTEM_SERVICES_READY);
                        Trace.traceBegin(524288, "MakeWindowManagerServiceReady");
                        windowManagerService.systemReady();
                        Trace.traceEnd(524288);
                        if (safeMode) {
                            this.mActivityManagerService.showSafeModeOverlay();
                        }
                        config = windowManagerService.computeNewConfiguration();
                        metrics = new DisplayMetrics();
                        ((WindowManager) context2.getSystemService("window")).getDefaultDisplay().getMetrics(metrics);
                        context2.getResources().updateConfiguration(config, metrics);
                        systemTheme = context2.getTheme();
                        if (systemTheme.getChangingConfigurations() != 0) {
                            systemTheme.rebase();
                        }
                        Trace.traceBegin(524288, "MakePowerManagerServiceReady");
                        this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                        Trace.traceEnd(524288);
                        Trace.traceEnd(524288);
                        this.mPGManagerService.systemReady(this.mActivityManagerService, this.mPowerManagerService, location);
                        Trace.traceBegin(524288, "MakePackageManagerServiceReady");
                        this.mPackageManagerService.systemReady();
                        Trace.traceEnd(524288);
                        Trace.traceBegin(524288, "MakeDisplayManagerServiceReady");
                        this.mDisplayManagerService.systemReady(safeMode, this.mOnlyCore);
                        Trace.traceEnd(524288);
                        networkManagementF = networkManagement;
                        networkStatsF = networkStats;
                        networkPolicyF = networkPolicy;
                        connectivityF = connectivity;
                        networkScoreF = networkScoreService2;
                        locationF = location;
                        countryDetectorF = countryDetectorService;
                        networkTimeUpdaterF = networkTimeUpdateService2;
                        commonTimeMgmtServiceF = commonTimeManagementService2;
                        atlasF = assetAtlasService;
                        inputManagerF = inputManager;
                        telephonyRegistryF = telephonyRegistry;
                        mediaRouterF = mediaRouterService;
                        mmsServiceF = mmsService;
                        context = context2;
                        this.mActivityManagerService.systemReady(/* anonymous class already generated */);
                    }
                    Trace.traceEnd(524288);
                    traceBeginAndSlog("StartCommonTimeManagementService");
                    try {
                        commonTimeManagementService = new CommonTimeManagementService(context2);
                        ServiceManager.addService("commontime_management", commonTimeManagementService);
                        commonTimeManagementService2 = commonTimeManagementService;
                    } catch (Throwable th16) {
                        e32222222222222222222222222 = th16;
                        reportWtf("starting CommonTimeManagementService service", e32222222222222222222222222);
                        Trace.traceEnd(524288);
                        if (disableNetwork) {
                            traceBeginAndSlog("CertBlacklister");
                            certBlacklister = new CertBlacklister(context2);
                            Trace.traceEnd(524288);
                        }
                        if (disableNonCoreServices) {
                            this.mSystemServiceManager.startService(DreamManagerService.class);
                        }
                        if (disableNonCoreServices) {
                            traceBeginAndSlog("StartAssetAtlasService");
                            assetAtlasService2 = new AssetAtlasService(context2);
                            ServiceManager.addService(AssetAtlasService.ASSET_ATLAS_SERVICE, assetAtlasService2);
                            assetAtlasService = assetAtlasService2;
                            Trace.traceEnd(524288);
                        }
                        if (disableNonCoreServices) {
                            ServiceManager.addService(GraphicsStatsService.GRAPHICS_STATS_SERVICE, new GraphicsStatsService(context2));
                        }
                        if (this.mPackageManager.hasSystemFeature("android.software.print")) {
                            this.mSystemServiceManager.startService(PRINT_MANAGER_SERVICE_CLASS);
                        }
                        this.mSystemServiceManager.startService(RestrictionsManagerService.class);
                        this.mSystemServiceManager.startService(MediaSessionService.class);
                        if (this.mPackageManager.hasSystemFeature("android.hardware.hdmi.cec")) {
                            this.mSystemServiceManager.startService(HdmiControlService.class);
                        }
                        if (this.mPackageManager.hasSystemFeature("android.software.live_tv")) {
                            this.mSystemServiceManager.startService(TvInputManagerService.class);
                        }
                        if (this.mPackageManager.hasSystemFeature("android.software.picture_in_picture")) {
                            this.mSystemServiceManager.startService(MediaResourceMonitorService.class);
                        }
                        if (this.mPackageManager.hasSystemFeature("android.software.leanback")) {
                            this.mSystemServiceManager.startService(TvRemoteService.class);
                        }
                        if (disableNonCoreServices) {
                            traceBeginAndSlog("StartMediaRouterService");
                            mediaRouterService2 = new MediaRouterService(context2);
                            ServiceManager.addService("media_router", mediaRouterService2);
                            mediaRouterService = mediaRouterService2;
                            Trace.traceEnd(524288);
                            if (disableTrustManager) {
                                this.mSystemServiceManager.startService(TrustManagerService.class);
                            }
                            traceBeginAndSlog("StartBackgroundDexOptService");
                            BackgroundDexOptService.schedule(context2);
                            Trace.traceEnd(524288);
                        }
                        this.mSystemServiceManager.startService(ShortcutService.Lifecycle.class);
                        this.mSystemServiceManager.startService(LauncherAppsService.class);
                        this.mSystemServiceManager.startService(MediaProjectionManagerService.class);
                        if (disableNonCoreServices) {
                            this.mSystemServiceManager.startService("com.android.server.NonHardwareAcceleratedPackagesManagerService");
                        }
                        if (disableNonCoreServices) {
                            jankshield = HwServiceFactory.getJankShieldService();
                            if (jankshield != null) {
                                ServiceManager.addService("jankshield", jankshield.getInstance(context2));
                            }
                        }
                        if (context2.getPackageManager().hasSystemFeature("android.hardware.type.watch")) {
                            this.mSystemServiceManager.startService(WEAR_BLUETOOTH_SERVICE_CLASS);
                        }
                        safeMode = windowManagerService.detectSafeMode();
                        this.mSystemServiceManager.setSafeMode(safeMode);
                        if (safeMode) {
                            this.mActivityManagerService.enterSafeMode();
                            VMRuntime.getRuntime().disableJitCompilation();
                        } else {
                            VMRuntime.getRuntime().startJitCompilation();
                        }
                        mmsService = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                        this.mSystemServiceManager.startService("com.android.server.HwCoreAppHelperService");
                        if ("true".equals(SystemProperties.get("bastet.service.enable", "false"))) {
                            this.mSystemServiceManager.startService("com.android.server.HwBastetService");
                        }
                        this.mSystemServiceManager.startService("com.android.server.HwPPTBService");
                        if ("true".equals(SystemProperties.get("ro.config.hw_emcom", "false"))) {
                            startEmcomService();
                        }
                        Trace.traceBegin(524288, "MakeVibratorServiceReady");
                        vibratorService.systemReady();
                        Trace.traceEnd(524288);
                        Trace.traceBegin(524288, "MakeLockSettingsServiceReady");
                        if (iLockSettings != null) {
                            iLockSettings.systemReady();
                        }
                        Trace.traceEnd(524288);
                        this.mSystemServiceManager.startBootPhase(SystemService.PHASE_LOCK_SETTINGS_READY);
                        this.mSystemServiceManager.startBootPhase(SystemService.PHASE_SYSTEM_SERVICES_READY);
                        Trace.traceBegin(524288, "MakeWindowManagerServiceReady");
                        windowManagerService.systemReady();
                        Trace.traceEnd(524288);
                        if (safeMode) {
                            this.mActivityManagerService.showSafeModeOverlay();
                        }
                        config = windowManagerService.computeNewConfiguration();
                        metrics = new DisplayMetrics();
                        ((WindowManager) context2.getSystemService("window")).getDefaultDisplay().getMetrics(metrics);
                        context2.getResources().updateConfiguration(config, metrics);
                        systemTheme = context2.getTheme();
                        if (systemTheme.getChangingConfigurations() != 0) {
                            systemTheme.rebase();
                        }
                        Trace.traceBegin(524288, "MakePowerManagerServiceReady");
                        this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                        Trace.traceEnd(524288);
                        Trace.traceEnd(524288);
                        this.mPGManagerService.systemReady(this.mActivityManagerService, this.mPowerManagerService, location);
                        Trace.traceBegin(524288, "MakePackageManagerServiceReady");
                        this.mPackageManagerService.systemReady();
                        Trace.traceEnd(524288);
                        Trace.traceBegin(524288, "MakeDisplayManagerServiceReady");
                        this.mDisplayManagerService.systemReady(safeMode, this.mOnlyCore);
                        Trace.traceEnd(524288);
                        networkManagementF = networkManagement;
                        networkStatsF = networkStats;
                        networkPolicyF = networkPolicy;
                        connectivityF = connectivity;
                        networkScoreF = networkScoreService2;
                        locationF = location;
                        countryDetectorF = countryDetectorService;
                        networkTimeUpdaterF = networkTimeUpdateService2;
                        commonTimeMgmtServiceF = commonTimeManagementService2;
                        atlasF = assetAtlasService;
                        inputManagerF = inputManager;
                        telephonyRegistryF = telephonyRegistry;
                        mediaRouterF = mediaRouterService;
                        mmsServiceF = mmsService;
                        context = context2;
                        this.mActivityManagerService.systemReady(/* anonymous class already generated */);
                    }
                    Trace.traceEnd(524288);
                    if (disableNetwork) {
                        traceBeginAndSlog("CertBlacklister");
                        certBlacklister = new CertBlacklister(context2);
                        Trace.traceEnd(524288);
                    }
                    if (disableNonCoreServices) {
                        this.mSystemServiceManager.startService(DreamManagerService.class);
                    }
                    if (disableNonCoreServices) {
                        traceBeginAndSlog("StartAssetAtlasService");
                        assetAtlasService2 = new AssetAtlasService(context2);
                        ServiceManager.addService(AssetAtlasService.ASSET_ATLAS_SERVICE, assetAtlasService2);
                        assetAtlasService = assetAtlasService2;
                        Trace.traceEnd(524288);
                    }
                    if (disableNonCoreServices) {
                        ServiceManager.addService(GraphicsStatsService.GRAPHICS_STATS_SERVICE, new GraphicsStatsService(context2));
                    }
                    if (this.mPackageManager.hasSystemFeature("android.software.print")) {
                        this.mSystemServiceManager.startService(PRINT_MANAGER_SERVICE_CLASS);
                    }
                    this.mSystemServiceManager.startService(RestrictionsManagerService.class);
                    this.mSystemServiceManager.startService(MediaSessionService.class);
                    if (this.mPackageManager.hasSystemFeature("android.hardware.hdmi.cec")) {
                        this.mSystemServiceManager.startService(HdmiControlService.class);
                    }
                    if (this.mPackageManager.hasSystemFeature("android.software.live_tv")) {
                        this.mSystemServiceManager.startService(TvInputManagerService.class);
                    }
                    if (this.mPackageManager.hasSystemFeature("android.software.picture_in_picture")) {
                        this.mSystemServiceManager.startService(MediaResourceMonitorService.class);
                    }
                    if (this.mPackageManager.hasSystemFeature("android.software.leanback")) {
                        this.mSystemServiceManager.startService(TvRemoteService.class);
                    }
                    if (disableNonCoreServices) {
                        traceBeginAndSlog("StartMediaRouterService");
                        mediaRouterService2 = new MediaRouterService(context2);
                        ServiceManager.addService("media_router", mediaRouterService2);
                        mediaRouterService = mediaRouterService2;
                        Trace.traceEnd(524288);
                        if (disableTrustManager) {
                            this.mSystemServiceManager.startService(TrustManagerService.class);
                        }
                        traceBeginAndSlog("StartBackgroundDexOptService");
                        BackgroundDexOptService.schedule(context2);
                        Trace.traceEnd(524288);
                    }
                    this.mSystemServiceManager.startService(ShortcutService.Lifecycle.class);
                    this.mSystemServiceManager.startService(LauncherAppsService.class);
                }
                this.mSystemServiceManager.startService(MediaProjectionManagerService.class);
                if (disableNonCoreServices) {
                    this.mSystemServiceManager.startService("com.android.server.NonHardwareAcceleratedPackagesManagerService");
                }
                if (disableNonCoreServices) {
                    jankshield = HwServiceFactory.getJankShieldService();
                    if (jankshield != null) {
                        ServiceManager.addService("jankshield", jankshield.getInstance(context2));
                    }
                }
                if (context2.getPackageManager().hasSystemFeature("android.hardware.type.watch")) {
                    this.mSystemServiceManager.startService(WEAR_BLUETOOTH_SERVICE_CLASS);
                }
                safeMode = windowManagerService.detectSafeMode();
                this.mSystemServiceManager.setSafeMode(safeMode);
                if (safeMode) {
                    VMRuntime.getRuntime().startJitCompilation();
                } else {
                    this.mActivityManagerService.enterSafeMode();
                    VMRuntime.getRuntime().disableJitCompilation();
                }
                mmsService = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                this.mSystemServiceManager.startService("com.android.server.HwCoreAppHelperService");
                if ("true".equals(SystemProperties.get("bastet.service.enable", "false"))) {
                    this.mSystemServiceManager.startService("com.android.server.HwBastetService");
                }
                this.mSystemServiceManager.startService("com.android.server.HwPPTBService");
                if ("true".equals(SystemProperties.get("ro.config.hw_emcom", "false"))) {
                    startEmcomService();
                }
                Trace.traceBegin(524288, "MakeVibratorServiceReady");
                vibratorService.systemReady();
                Trace.traceEnd(524288);
                Trace.traceBegin(524288, "MakeLockSettingsServiceReady");
                if (iLockSettings != null) {
                    iLockSettings.systemReady();
                }
                Trace.traceEnd(524288);
                this.mSystemServiceManager.startBootPhase(SystemService.PHASE_LOCK_SETTINGS_READY);
                this.mSystemServiceManager.startBootPhase(SystemService.PHASE_SYSTEM_SERVICES_READY);
                Trace.traceBegin(524288, "MakeWindowManagerServiceReady");
                windowManagerService.systemReady();
                Trace.traceEnd(524288);
                if (safeMode) {
                    this.mActivityManagerService.showSafeModeOverlay();
                }
                config = windowManagerService.computeNewConfiguration();
                metrics = new DisplayMetrics();
                ((WindowManager) context2.getSystemService("window")).getDefaultDisplay().getMetrics(metrics);
                context2.getResources().updateConfiguration(config, metrics);
                systemTheme = context2.getTheme();
                if (systemTheme.getChangingConfigurations() != 0) {
                    systemTheme.rebase();
                }
                Trace.traceBegin(524288, "MakePowerManagerServiceReady");
                this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                Trace.traceEnd(524288);
                Trace.traceEnd(524288);
                this.mPGManagerService.systemReady(this.mActivityManagerService, this.mPowerManagerService, location);
                Trace.traceBegin(524288, "MakePackageManagerServiceReady");
                this.mPackageManagerService.systemReady();
                Trace.traceEnd(524288);
                Trace.traceBegin(524288, "MakeDisplayManagerServiceReady");
                this.mDisplayManagerService.systemReady(safeMode, this.mOnlyCore);
                Trace.traceEnd(524288);
                networkManagementF = networkManagement;
                networkStatsF = networkStats;
                networkPolicyF = networkPolicy;
                connectivityF = connectivity;
                networkScoreF = networkScoreService2;
                locationF = location;
                countryDetectorF = countryDetectorService;
                networkTimeUpdaterF = networkTimeUpdateService2;
                commonTimeMgmtServiceF = commonTimeManagementService2;
                atlasF = assetAtlasService;
                inputManagerF = inputManager;
                telephonyRegistryF = telephonyRegistry;
                mediaRouterF = mediaRouterService;
                mmsServiceF = mmsService;
                context = context2;
                this.mActivityManagerService.systemReady(/* anonymous class already generated */);
            }
            try {
                traceBeginAndSlog("StartAlarmManagerService");
                Trace.traceEnd(524288);
                this.mActivityManagerService.setAlarmManager(almService);
                this.mActivityManagerService.setAlarmManagerExt(almService);
                Slog.i(TAG, "Init Watchdog");
                Watchdog.getInstance().init(context2, this.mActivityManagerService);
                Trace.traceEnd(524288);
                traceBeginAndSlog("StartInputManagerService");
                Slog.i(TAG, "Input Manager");
                inputManager = HwServiceFactory.getHwInputManagerService().getInstance(context2, null);
                if (enableRms || enableIaware) {
                    try {
                        this.mSystemServiceManager.startService("com.android.server.rms.HwSysResManagerService");
                    } catch (Throwable e322222222222222222222222222) {
                        Slog.e(TAG, e322222222222222222222222222.toString());
                    }
                }
                Trace.traceEnd(524288);
                traceBeginAndSlog("StartWindowManagerService");
                windowManagerService = WindowManagerService.main(context2, inputManager, this.mFactoryTestMode != 1 ? LOCAL_LOGV : false, this.mFirstBoot ? false : LOCAL_LOGV, this.mOnlyCore);
                restoreRogMode(windowManagerService, context2);
                int dpi = SystemProperties.getInt("persist.sys.dpi", 0);
                if (SystemProperties.getInt("persist.sys.rog.width", 0) > 0) {
                    dpi = DisplayMetrics.DENSITY_DEVICE;
                }
                if (dpi > 0) {
                    windowManagerService.setForcedDisplayDensity(0, dpi);
                }
                ServiceManager.addService("window", windowManagerService);
                ServiceManager.addService("input", inputManager);
                Trace.traceEnd(524288);
                traceBeginAndSlog("StartVrManagerService");
                this.mSystemServiceManager.startService(VrManagerService.class);
                Trace.traceEnd(524288);
                this.mActivityManagerService.setWindowManager(windowManagerService);
                inputManager.setWindowManagerCallbacks(windowManagerService.getInputMonitor());
                inputManager.start();
                this.mDisplayManagerService.windowManagerAndInputReady();
                if (isEmulator) {
                    Slog.i(TAG, "No Bluetooth Service (emulator)");
                } else if (this.mFactoryTestMode == 1) {
                    Slog.i(TAG, "No Bluetooth Service (factory test)");
                } else if (!context2.getPackageManager().hasSystemFeature("android.hardware.bluetooth")) {
                    Slog.i(TAG, "No Bluetooth Service (Bluetooth Hardware Not Present)");
                } else if (disableBluetooth) {
                    Slog.i(TAG, "Bluetooth Service disabled by config");
                } else {
                    this.mSystemServiceManager.startService(BluetoothService.class);
                }
                traceBeginAndSlog("ConnectivityMetricsLoggerService");
                this.mSystemServiceManager.startService(MetricsLoggerService.class);
                Trace.traceEnd(524288);
                traceBeginAndSlog("PinnerService");
                this.mSystemServiceManager.startService(PinnerService.class);
                Trace.traceEnd(524288);
                consumerIr = consumerIrService;
                vibratorService = vibratorService2;
            } catch (RuntimeException e11) {
                e2 = e11;
                consumerIr = consumerIrService;
                vibratorService = vibratorService2;
                Slog.e("System", "******************************************");
                Slog.e("System", "************ Failure starting core service", e2);
                location = null;
                countryDetectorService = null;
                iLockSettings = null;
                assetAtlasService = null;
                mediaRouterService = null;
                if (this.mFactoryTestMode != 1) {
                    traceBeginAndSlog("StartAccessibilityManagerService");
                    Slog.i(TAG, "Input Method Service");
                    this.mSystemServiceManager.startService(InputMethodManagerService.Lifecycle.class);
                    if (isChinaArea) {
                        Slog.i(TAG, "Secure Input Method Service");
                        this.mSystemServiceManager.startService("com.android.server.HwSecureInputMethodManagerService$MyLifecycle");
                    }
                    ServiceManager.addService("accessibility", new AccessibilityManagerService(context2));
                    Trace.traceEnd(524288);
                }
                windowManagerService.displayReady();
                this.mSystemServiceManager.startService(MOUNT_SERVICE_CLASS);
                mountService = Stub.asInterface(ServiceManager.getService("mount"));
                this.mSystemServiceManager.startService(UiModeManagerService.class);
                this.mActivityManagerService.bootSceneEnd(101);
                HwBootFail.setBootStage(83886090);
                if (this.mOnlyCore) {
                    Trace.traceBegin(524288, "UpdatePackagesIfNeeded");
                    this.mPackageManagerService.updatePackagesIfNeeded();
                    Trace.traceEnd(524288);
                }
                Trace.traceBegin(524288, "PerformFstrimIfNeeded");
                this.mPackageManagerService.performFstrimIfNeeded();
                Trace.traceEnd(524288);
                this.mActivityManagerService.bootSceneStart(HdmiCecKeycode.CEC_KEYCODE_RESTORE_VOLUME_FUNCTION, JobStatus.DEFAULT_TRIGGER_MAX_DELAY);
                HwBootFail.setBootStage(83886091);
                ActivityManagerNative.getDefault().showBootMessage(context2.getResources().getText(17040291), false);
                if (this.mFactoryTestMode != 1) {
                    startForceRotation(context2);
                    if (disableNonCoreServices) {
                        traceBeginAndSlog("StartLockSettingsService");
                        this.mSystemServiceManager.startService(LOCK_SETTINGS_SERVICE_CLASS);
                        iLockSettings = ILockSettings.Stub.asInterface(ServiceManager.getService("lock_settings"));
                        Trace.traceEnd(524288);
                        if (SystemProperties.get(PERSISTENT_DATA_BLOCK_PROP).equals("")) {
                            this.mSystemServiceManager.startService(PersistentDataBlockService.class);
                        }
                        this.mSystemServiceManager.startService(DeviceIdleController.class);
                        this.mSystemServiceManager.startService(DevicePolicyManagerService.Lifecycle.class);
                    }
                    if (disableSystemUI) {
                        traceBeginAndSlog("StartStatusBarManagerService");
                        Slog.i(TAG, "Status Bar");
                        ServiceManager.addService("statusbar", HwServiceFactory.createHwStatusBarManagerService(context2, windowManagerService));
                        Trace.traceEnd(524288);
                    }
                    if (disableNonCoreServices) {
                        traceBeginAndSlog("StartClipboardService");
                        ServiceManager.addService("clipboard", new ClipboardService(context2));
                        Trace.traceEnd(524288);
                    }
                    if (disableNetwork) {
                        traceBeginAndSlog("StartNetworkManagementService");
                        networkManagement = NetworkManagementService.create(context2);
                        ServiceManager.addService("network_management", networkManagement);
                        Trace.traceEnd(524288);
                    }
                    this.mSystemServiceManager.startService(TextServicesManagerService.Lifecycle.class);
                    if (disableNetwork) {
                        traceBeginAndSlog("StartNetworkScoreService");
                        networkScoreService = new NetworkScoreService(context2);
                        ServiceManager.addService("network_score", networkScoreService);
                        networkScoreService2 = networkScoreService;
                        Trace.traceEnd(524288);
                        traceBeginAndSlog("StartNetworkStatsService");
                        networkStats = NetworkStatsService.create(context2, networkManagement);
                        ServiceManager.addService("netstats", networkStats);
                        Trace.traceEnd(524288);
                        traceBeginAndSlog("StartNetworkPolicyManagerService");
                        networkPolicy = HwServiceFactory.getHwNetworkPolicyManagerService().getInstance(context2, this.mActivityManagerService, (IPowerManager) ServiceManager.getService("power"), networkStats, networkManagement);
                        ServiceManager.addService("netpolicy", networkPolicy);
                        Trace.traceEnd(524288);
                        if (context2.getPackageManager().hasSystemFeature("android.hardware.wifi.nan")) {
                            Slog.i(TAG, "No Wi-Fi NAN Service (NAN support Not Present)");
                        } else {
                            this.mSystemServiceManager.startService(WIFI_NAN_SERVICE_CLASS);
                        }
                        this.mSystemServiceManager.startService(WIFI_P2P_SERVICE_CLASS);
                        this.mSystemServiceManager.startService(WIFI_SERVICE_CLASS);
                        this.mSystemServiceManager.startService("com.android.server.wifi.scanner.WifiScanningService");
                        if (disableRtt) {
                            this.mSystemServiceManager.startService("com.android.server.wifi.RttService");
                        }
                        this.mSystemServiceManager.startService(ETHERNET_SERVICE_CLASS);
                        traceBeginAndSlog("StartConnectivityService");
                        Slog.i(TAG, "Connectivity Service");
                        connectivity = HwServiceFactory.getHwConnectivityManager().createHwConnectivityService(context2, networkManagement, networkStats, networkPolicy);
                        ServiceManager.addService("connectivity", connectivity);
                        networkStats.bindConnectivityManager(connectivity);
                        networkPolicy.bindConnectivityManager(connectivity);
                        Trace.traceEnd(524288);
                        traceBeginAndSlog("StartNsdService");
                        ServiceManager.addService("servicediscovery", NsdService.create(context2));
                        Trace.traceEnd(524288);
                    }
                    if (disableNonCoreServices) {
                        traceBeginAndSlog("StartUpdateLockService");
                        ServiceManager.addService("updatelock", new UpdateLockService(context2));
                        Trace.traceEnd(524288);
                    }
                    if (disableNonCoreServices) {
                        this.mSystemServiceManager.startService(RecoverySystemService.class);
                    }
                    Trace.traceBegin(524288, "WaitForAsecScan");
                    mountService.waitForAsecScan();
                    Trace.traceEnd(524288);
                    this.mSystemServiceManager.startService("com.android.server.notification.HwNotificationManagerService");
                    notification = INotificationManager.Stub.asInterface(ServiceManager.getService("notification"));
                    if (networkPolicy != null) {
                        networkPolicy.bindNotificationManager(notification);
                    }
                    this.mSystemServiceManager.startService(HwServiceFactory.getDeviceStorageMonitorServiceClassName());
                    Slog.i(TAG, "TUI Connect enable " + tuiEnable);
                    if (tuiEnable) {
                        ServiceManager.addService("tui", new TrustedUIService(context2));
                    }
                    if (vrDisplayEnable) {
                        Slog.i(TAG, "VR Display enable " + vrDisplayEnable);
                        ServiceManager.addService("vr_display", new VRManagerService(context2));
                    }
                    if (disableLocation) {
                        traceBeginAndSlog("StartLocationManagerService");
                        Slog.i(TAG, "Location Manager");
                        hwLocation = HwServiceFactory.getHwLocationManagerService();
                        if (hwLocation == null) {
                            location = new LocationManagerService(context2);
                        } else {
                            location = hwLocation.getInstance(context2);
                        }
                        ServiceManager.addService("location", location);
                        Trace.traceEnd(524288);
                        traceBeginAndSlog("StartCountryDetectorService");
                        countryDetectorService2 = new CountryDetectorService(context2);
                        ServiceManager.addService("country_detector", countryDetectorService2);
                        countryDetectorService = countryDetectorService2;
                        Trace.traceEnd(524288);
                    }
                    traceBeginAndSlog("StartSearchManagerService");
                    this.mSystemServiceManager.startService(SEARCH_MANAGER_SERVICE_CLASS);
                    Trace.traceEnd(524288);
                    this.mSystemServiceManager.startService(DropBoxManagerService.class);
                    traceBeginAndSlog("StartWallpaperManagerService");
                    this.mSystemServiceManager.startService(HwServiceFactory.getWallpaperManagerServiceClassName());
                    Trace.traceEnd(524288);
                    traceBeginAndSlog("StartAudioService");
                    this.mSystemServiceManager.startService(AudioService.Lifecycle.class);
                    Trace.traceEnd(524288);
                    if (disableNonCoreServices) {
                        this.mSystemServiceManager.startService(DockObserver.class);
                        if (context2.getPackageManager().hasSystemFeature("android.hardware.type.watch")) {
                            this.mSystemServiceManager.startService(THERMAL_OBSERVER_CLASS);
                        }
                    }
                    traceBeginAndSlog("StartWiredAccessoryManager");
                    inputManager.setWiredAccessoryCallbacks(new WiredAccessoryManager(context2, inputManager));
                    Trace.traceEnd(524288);
                    if (disableNonCoreServices) {
                        if (this.mPackageManager.hasSystemFeature("android.software.midi")) {
                            this.mSystemServiceManager.startService(MIDI_SERVICE_CLASS);
                        }
                        Trace.traceBegin(524288, "StartUsbService");
                        this.mSystemServiceManager.startService(USB_SERVICE_CLASS);
                        Trace.traceEnd(524288);
                        if (disableSerial) {
                            traceBeginAndSlog("StartSerialService");
                            serialService = new SerialService(context2);
                            ServiceManager.addService("serial", serialService);
                            serialService2 = serialService;
                            Trace.traceEnd(524288);
                        }
                        Trace.traceBegin(524288, "StartHardwarePropertiesManagerService");
                        hardwarePropertiesManagerService = new HardwarePropertiesManagerService(context2);
                        ServiceManager.addService("hardware_properties", hardwarePropertiesManagerService);
                        hardwarePropertiesManagerService2 = hardwarePropertiesManagerService;
                        Trace.traceEnd(524288);
                    }
                    this.mSystemServiceManager.startService(TwilightService.class);
                    this.mSystemServiceManager.startService("com.android.server.job.HwJobSchedulerService");
                    this.mSystemServiceManager.startService(SoundTriggerService.class);
                    if (disableNonCoreServices) {
                        if (this.mPackageManager.hasSystemFeature("android.software.backup")) {
                            this.mSystemServiceManager.startService(BACKUP_MANAGER_SERVICE_CLASS);
                        }
                        this.mSystemServiceManager.startService(APPWIDGET_SERVICE_CLASS);
                        if (this.mPackageManager.hasSystemFeature("android.software.voice_recognizers")) {
                            this.mSystemServiceManager.startService(VOICE_RECOGNITION_MANAGER_SERVICE_CLASS);
                        }
                        if (GestureLauncherService.isGestureLauncherEnabled(context2.getResources())) {
                            Slog.i(TAG, "Gesture Launcher Service");
                            this.mSystemServiceManager.startService(GestureLauncherService.class);
                        }
                        this.mSystemServiceManager.startService(SensorNotificationService.class);
                        this.mSystemServiceManager.startService(ContextHubSystemService.class);
                    }
                    HwServiceFactory.setupHwServices(context2);
                    traceBeginAndSlog("StartDiskStatsService");
                    ServiceManager.addService("diskstats", new DiskStatsService(context2));
                    Trace.traceEnd(524288);
                    if (disableSamplingProfiler) {
                        traceBeginAndSlog("StartSamplingProfilerService");
                        ServiceManager.addService("samplingprofiler", new SamplingProfilerService(context2));
                        Trace.traceEnd(524288);
                    }
                    Slog.i(TAG, "attestation Service");
                    attestation = HwServiceFactory.getHwAttestationService();
                    if (attestation != null) {
                        ServiceManager.addService("attestation_service", attestation.getInstance(context2));
                    }
                    traceBeginAndSlog("StartNetworkTimeUpdateService");
                    networkTimeUpdateService = new NetworkTimeUpdateService(context2);
                    ServiceManager.addService("network_time_update_service", networkTimeUpdateService);
                    networkTimeUpdateService2 = networkTimeUpdateService;
                    Trace.traceEnd(524288);
                    traceBeginAndSlog("StartCommonTimeManagementService");
                    commonTimeManagementService = new CommonTimeManagementService(context2);
                    ServiceManager.addService("commontime_management", commonTimeManagementService);
                    commonTimeManagementService2 = commonTimeManagementService;
                    Trace.traceEnd(524288);
                    if (disableNetwork) {
                        traceBeginAndSlog("CertBlacklister");
                        certBlacklister = new CertBlacklister(context2);
                        Trace.traceEnd(524288);
                    }
                    if (disableNonCoreServices) {
                        this.mSystemServiceManager.startService(DreamManagerService.class);
                    }
                    if (disableNonCoreServices) {
                        traceBeginAndSlog("StartAssetAtlasService");
                        assetAtlasService2 = new AssetAtlasService(context2);
                        ServiceManager.addService(AssetAtlasService.ASSET_ATLAS_SERVICE, assetAtlasService2);
                        assetAtlasService = assetAtlasService2;
                        Trace.traceEnd(524288);
                    }
                    if (disableNonCoreServices) {
                        ServiceManager.addService(GraphicsStatsService.GRAPHICS_STATS_SERVICE, new GraphicsStatsService(context2));
                    }
                    if (this.mPackageManager.hasSystemFeature("android.software.print")) {
                        this.mSystemServiceManager.startService(PRINT_MANAGER_SERVICE_CLASS);
                    }
                    this.mSystemServiceManager.startService(RestrictionsManagerService.class);
                    this.mSystemServiceManager.startService(MediaSessionService.class);
                    if (this.mPackageManager.hasSystemFeature("android.hardware.hdmi.cec")) {
                        this.mSystemServiceManager.startService(HdmiControlService.class);
                    }
                    if (this.mPackageManager.hasSystemFeature("android.software.live_tv")) {
                        this.mSystemServiceManager.startService(TvInputManagerService.class);
                    }
                    if (this.mPackageManager.hasSystemFeature("android.software.picture_in_picture")) {
                        this.mSystemServiceManager.startService(MediaResourceMonitorService.class);
                    }
                    if (this.mPackageManager.hasSystemFeature("android.software.leanback")) {
                        this.mSystemServiceManager.startService(TvRemoteService.class);
                    }
                    if (disableNonCoreServices) {
                        traceBeginAndSlog("StartMediaRouterService");
                        mediaRouterService2 = new MediaRouterService(context2);
                        ServiceManager.addService("media_router", mediaRouterService2);
                        mediaRouterService = mediaRouterService2;
                        Trace.traceEnd(524288);
                        if (disableTrustManager) {
                            this.mSystemServiceManager.startService(TrustManagerService.class);
                        }
                        traceBeginAndSlog("StartBackgroundDexOptService");
                        BackgroundDexOptService.schedule(context2);
                        Trace.traceEnd(524288);
                    }
                    this.mSystemServiceManager.startService(ShortcutService.Lifecycle.class);
                    this.mSystemServiceManager.startService(LauncherAppsService.class);
                }
                this.mSystemServiceManager.startService(MediaProjectionManagerService.class);
                if (disableNonCoreServices) {
                    this.mSystemServiceManager.startService("com.android.server.NonHardwareAcceleratedPackagesManagerService");
                }
                if (disableNonCoreServices) {
                    jankshield = HwServiceFactory.getJankShieldService();
                    if (jankshield != null) {
                        ServiceManager.addService("jankshield", jankshield.getInstance(context2));
                    }
                }
                if (context2.getPackageManager().hasSystemFeature("android.hardware.type.watch")) {
                    this.mSystemServiceManager.startService(WEAR_BLUETOOTH_SERVICE_CLASS);
                }
                safeMode = windowManagerService.detectSafeMode();
                this.mSystemServiceManager.setSafeMode(safeMode);
                if (safeMode) {
                    VMRuntime.getRuntime().startJitCompilation();
                } else {
                    this.mActivityManagerService.enterSafeMode();
                    VMRuntime.getRuntime().disableJitCompilation();
                }
                mmsService = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                this.mSystemServiceManager.startService("com.android.server.HwCoreAppHelperService");
                if ("true".equals(SystemProperties.get("bastet.service.enable", "false"))) {
                    this.mSystemServiceManager.startService("com.android.server.HwBastetService");
                }
                this.mSystemServiceManager.startService("com.android.server.HwPPTBService");
                if ("true".equals(SystemProperties.get("ro.config.hw_emcom", "false"))) {
                    startEmcomService();
                }
                Trace.traceBegin(524288, "MakeVibratorServiceReady");
                vibratorService.systemReady();
                Trace.traceEnd(524288);
                Trace.traceBegin(524288, "MakeLockSettingsServiceReady");
                if (iLockSettings != null) {
                    iLockSettings.systemReady();
                }
                Trace.traceEnd(524288);
                this.mSystemServiceManager.startBootPhase(SystemService.PHASE_LOCK_SETTINGS_READY);
                this.mSystemServiceManager.startBootPhase(SystemService.PHASE_SYSTEM_SERVICES_READY);
                Trace.traceBegin(524288, "MakeWindowManagerServiceReady");
                windowManagerService.systemReady();
                Trace.traceEnd(524288);
                if (safeMode) {
                    this.mActivityManagerService.showSafeModeOverlay();
                }
                config = windowManagerService.computeNewConfiguration();
                metrics = new DisplayMetrics();
                ((WindowManager) context2.getSystemService("window")).getDefaultDisplay().getMetrics(metrics);
                context2.getResources().updateConfiguration(config, metrics);
                systemTheme = context2.getTheme();
                if (systemTheme.getChangingConfigurations() != 0) {
                    systemTheme.rebase();
                }
                Trace.traceBegin(524288, "MakePowerManagerServiceReady");
                this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                Trace.traceEnd(524288);
                Trace.traceEnd(524288);
                this.mPGManagerService.systemReady(this.mActivityManagerService, this.mPowerManagerService, location);
                Trace.traceBegin(524288, "MakePackageManagerServiceReady");
                this.mPackageManagerService.systemReady();
                Trace.traceEnd(524288);
                Trace.traceBegin(524288, "MakeDisplayManagerServiceReady");
                this.mDisplayManagerService.systemReady(safeMode, this.mOnlyCore);
                Trace.traceEnd(524288);
                networkManagementF = networkManagement;
                networkStatsF = networkStats;
                networkPolicyF = networkPolicy;
                connectivityF = connectivity;
                networkScoreF = networkScoreService2;
                locationF = location;
                countryDetectorF = countryDetectorService;
                networkTimeUpdaterF = networkTimeUpdateService2;
                commonTimeMgmtServiceF = commonTimeManagementService2;
                atlasF = assetAtlasService;
                inputManagerF = inputManager;
                telephonyRegistryF = telephonyRegistry;
                mediaRouterF = mediaRouterService;
                mmsServiceF = mmsService;
                context = context2;
                this.mActivityManagerService.systemReady(/* anonymous class already generated */);
            }
        } catch (RuntimeException e12) {
            e2 = e12;
            Slog.e("System", "******************************************");
            Slog.e("System", "************ Failure starting core service", e2);
            location = null;
            countryDetectorService = null;
            iLockSettings = null;
            assetAtlasService = null;
            mediaRouterService = null;
            if (this.mFactoryTestMode != 1) {
                traceBeginAndSlog("StartAccessibilityManagerService");
                Slog.i(TAG, "Input Method Service");
                this.mSystemServiceManager.startService(InputMethodManagerService.Lifecycle.class);
                if (isChinaArea) {
                    Slog.i(TAG, "Secure Input Method Service");
                    this.mSystemServiceManager.startService("com.android.server.HwSecureInputMethodManagerService$MyLifecycle");
                }
                ServiceManager.addService("accessibility", new AccessibilityManagerService(context2));
                Trace.traceEnd(524288);
            }
            windowManagerService.displayReady();
            this.mSystemServiceManager.startService(MOUNT_SERVICE_CLASS);
            mountService = Stub.asInterface(ServiceManager.getService("mount"));
            this.mSystemServiceManager.startService(UiModeManagerService.class);
            this.mActivityManagerService.bootSceneEnd(101);
            HwBootFail.setBootStage(83886090);
            if (this.mOnlyCore) {
                Trace.traceBegin(524288, "UpdatePackagesIfNeeded");
                this.mPackageManagerService.updatePackagesIfNeeded();
                Trace.traceEnd(524288);
            }
            Trace.traceBegin(524288, "PerformFstrimIfNeeded");
            this.mPackageManagerService.performFstrimIfNeeded();
            Trace.traceEnd(524288);
            this.mActivityManagerService.bootSceneStart(HdmiCecKeycode.CEC_KEYCODE_RESTORE_VOLUME_FUNCTION, JobStatus.DEFAULT_TRIGGER_MAX_DELAY);
            HwBootFail.setBootStage(83886091);
            ActivityManagerNative.getDefault().showBootMessage(context2.getResources().getText(17040291), false);
            if (this.mFactoryTestMode != 1) {
                startForceRotation(context2);
                if (disableNonCoreServices) {
                    traceBeginAndSlog("StartLockSettingsService");
                    this.mSystemServiceManager.startService(LOCK_SETTINGS_SERVICE_CLASS);
                    iLockSettings = ILockSettings.Stub.asInterface(ServiceManager.getService("lock_settings"));
                    Trace.traceEnd(524288);
                    if (SystemProperties.get(PERSISTENT_DATA_BLOCK_PROP).equals("")) {
                        this.mSystemServiceManager.startService(PersistentDataBlockService.class);
                    }
                    this.mSystemServiceManager.startService(DeviceIdleController.class);
                    this.mSystemServiceManager.startService(DevicePolicyManagerService.Lifecycle.class);
                }
                if (disableSystemUI) {
                    traceBeginAndSlog("StartStatusBarManagerService");
                    Slog.i(TAG, "Status Bar");
                    ServiceManager.addService("statusbar", HwServiceFactory.createHwStatusBarManagerService(context2, windowManagerService));
                    Trace.traceEnd(524288);
                }
                if (disableNonCoreServices) {
                    traceBeginAndSlog("StartClipboardService");
                    ServiceManager.addService("clipboard", new ClipboardService(context2));
                    Trace.traceEnd(524288);
                }
                if (disableNetwork) {
                    traceBeginAndSlog("StartNetworkManagementService");
                    networkManagement = NetworkManagementService.create(context2);
                    ServiceManager.addService("network_management", networkManagement);
                    Trace.traceEnd(524288);
                }
                this.mSystemServiceManager.startService(TextServicesManagerService.Lifecycle.class);
                if (disableNetwork) {
                    traceBeginAndSlog("StartNetworkScoreService");
                    networkScoreService = new NetworkScoreService(context2);
                    ServiceManager.addService("network_score", networkScoreService);
                    networkScoreService2 = networkScoreService;
                    Trace.traceEnd(524288);
                    traceBeginAndSlog("StartNetworkStatsService");
                    networkStats = NetworkStatsService.create(context2, networkManagement);
                    ServiceManager.addService("netstats", networkStats);
                    Trace.traceEnd(524288);
                    traceBeginAndSlog("StartNetworkPolicyManagerService");
                    networkPolicy = HwServiceFactory.getHwNetworkPolicyManagerService().getInstance(context2, this.mActivityManagerService, (IPowerManager) ServiceManager.getService("power"), networkStats, networkManagement);
                    ServiceManager.addService("netpolicy", networkPolicy);
                    Trace.traceEnd(524288);
                    if (context2.getPackageManager().hasSystemFeature("android.hardware.wifi.nan")) {
                        Slog.i(TAG, "No Wi-Fi NAN Service (NAN support Not Present)");
                    } else {
                        this.mSystemServiceManager.startService(WIFI_NAN_SERVICE_CLASS);
                    }
                    this.mSystemServiceManager.startService(WIFI_P2P_SERVICE_CLASS);
                    this.mSystemServiceManager.startService(WIFI_SERVICE_CLASS);
                    this.mSystemServiceManager.startService("com.android.server.wifi.scanner.WifiScanningService");
                    if (disableRtt) {
                        this.mSystemServiceManager.startService("com.android.server.wifi.RttService");
                    }
                    this.mSystemServiceManager.startService(ETHERNET_SERVICE_CLASS);
                    traceBeginAndSlog("StartConnectivityService");
                    Slog.i(TAG, "Connectivity Service");
                    connectivity = HwServiceFactory.getHwConnectivityManager().createHwConnectivityService(context2, networkManagement, networkStats, networkPolicy);
                    ServiceManager.addService("connectivity", connectivity);
                    networkStats.bindConnectivityManager(connectivity);
                    networkPolicy.bindConnectivityManager(connectivity);
                    Trace.traceEnd(524288);
                    traceBeginAndSlog("StartNsdService");
                    ServiceManager.addService("servicediscovery", NsdService.create(context2));
                    Trace.traceEnd(524288);
                }
                if (disableNonCoreServices) {
                    traceBeginAndSlog("StartUpdateLockService");
                    ServiceManager.addService("updatelock", new UpdateLockService(context2));
                    Trace.traceEnd(524288);
                }
                if (disableNonCoreServices) {
                    this.mSystemServiceManager.startService(RecoverySystemService.class);
                }
                Trace.traceBegin(524288, "WaitForAsecScan");
                mountService.waitForAsecScan();
                Trace.traceEnd(524288);
                this.mSystemServiceManager.startService("com.android.server.notification.HwNotificationManagerService");
                notification = INotificationManager.Stub.asInterface(ServiceManager.getService("notification"));
                if (networkPolicy != null) {
                    networkPolicy.bindNotificationManager(notification);
                }
                this.mSystemServiceManager.startService(HwServiceFactory.getDeviceStorageMonitorServiceClassName());
                Slog.i(TAG, "TUI Connect enable " + tuiEnable);
                if (tuiEnable) {
                    ServiceManager.addService("tui", new TrustedUIService(context2));
                }
                if (vrDisplayEnable) {
                    Slog.i(TAG, "VR Display enable " + vrDisplayEnable);
                    ServiceManager.addService("vr_display", new VRManagerService(context2));
                }
                if (disableLocation) {
                    traceBeginAndSlog("StartLocationManagerService");
                    Slog.i(TAG, "Location Manager");
                    hwLocation = HwServiceFactory.getHwLocationManagerService();
                    if (hwLocation == null) {
                        location = new LocationManagerService(context2);
                    } else {
                        location = hwLocation.getInstance(context2);
                    }
                    ServiceManager.addService("location", location);
                    Trace.traceEnd(524288);
                    traceBeginAndSlog("StartCountryDetectorService");
                    countryDetectorService2 = new CountryDetectorService(context2);
                    ServiceManager.addService("country_detector", countryDetectorService2);
                    countryDetectorService = countryDetectorService2;
                    Trace.traceEnd(524288);
                }
                traceBeginAndSlog("StartSearchManagerService");
                this.mSystemServiceManager.startService(SEARCH_MANAGER_SERVICE_CLASS);
                Trace.traceEnd(524288);
                this.mSystemServiceManager.startService(DropBoxManagerService.class);
                traceBeginAndSlog("StartWallpaperManagerService");
                this.mSystemServiceManager.startService(HwServiceFactory.getWallpaperManagerServiceClassName());
                Trace.traceEnd(524288);
                traceBeginAndSlog("StartAudioService");
                this.mSystemServiceManager.startService(AudioService.Lifecycle.class);
                Trace.traceEnd(524288);
                if (disableNonCoreServices) {
                    this.mSystemServiceManager.startService(DockObserver.class);
                    if (context2.getPackageManager().hasSystemFeature("android.hardware.type.watch")) {
                        this.mSystemServiceManager.startService(THERMAL_OBSERVER_CLASS);
                    }
                }
                traceBeginAndSlog("StartWiredAccessoryManager");
                inputManager.setWiredAccessoryCallbacks(new WiredAccessoryManager(context2, inputManager));
                Trace.traceEnd(524288);
                if (disableNonCoreServices) {
                    if (this.mPackageManager.hasSystemFeature("android.software.midi")) {
                        this.mSystemServiceManager.startService(MIDI_SERVICE_CLASS);
                    }
                    Trace.traceBegin(524288, "StartUsbService");
                    this.mSystemServiceManager.startService(USB_SERVICE_CLASS);
                    Trace.traceEnd(524288);
                    if (disableSerial) {
                        traceBeginAndSlog("StartSerialService");
                        serialService = new SerialService(context2);
                        ServiceManager.addService("serial", serialService);
                        serialService2 = serialService;
                        Trace.traceEnd(524288);
                    }
                    Trace.traceBegin(524288, "StartHardwarePropertiesManagerService");
                    hardwarePropertiesManagerService = new HardwarePropertiesManagerService(context2);
                    ServiceManager.addService("hardware_properties", hardwarePropertiesManagerService);
                    hardwarePropertiesManagerService2 = hardwarePropertiesManagerService;
                    Trace.traceEnd(524288);
                }
                this.mSystemServiceManager.startService(TwilightService.class);
                this.mSystemServiceManager.startService("com.android.server.job.HwJobSchedulerService");
                this.mSystemServiceManager.startService(SoundTriggerService.class);
                if (disableNonCoreServices) {
                    if (this.mPackageManager.hasSystemFeature("android.software.backup")) {
                        this.mSystemServiceManager.startService(BACKUP_MANAGER_SERVICE_CLASS);
                    }
                    this.mSystemServiceManager.startService(APPWIDGET_SERVICE_CLASS);
                    if (this.mPackageManager.hasSystemFeature("android.software.voice_recognizers")) {
                        this.mSystemServiceManager.startService(VOICE_RECOGNITION_MANAGER_SERVICE_CLASS);
                    }
                    if (GestureLauncherService.isGestureLauncherEnabled(context2.getResources())) {
                        Slog.i(TAG, "Gesture Launcher Service");
                        this.mSystemServiceManager.startService(GestureLauncherService.class);
                    }
                    this.mSystemServiceManager.startService(SensorNotificationService.class);
                    this.mSystemServiceManager.startService(ContextHubSystemService.class);
                }
                HwServiceFactory.setupHwServices(context2);
                traceBeginAndSlog("StartDiskStatsService");
                ServiceManager.addService("diskstats", new DiskStatsService(context2));
                Trace.traceEnd(524288);
                if (disableSamplingProfiler) {
                    traceBeginAndSlog("StartSamplingProfilerService");
                    ServiceManager.addService("samplingprofiler", new SamplingProfilerService(context2));
                    Trace.traceEnd(524288);
                }
                Slog.i(TAG, "attestation Service");
                attestation = HwServiceFactory.getHwAttestationService();
                if (attestation != null) {
                    ServiceManager.addService("attestation_service", attestation.getInstance(context2));
                }
                traceBeginAndSlog("StartNetworkTimeUpdateService");
                networkTimeUpdateService = new NetworkTimeUpdateService(context2);
                ServiceManager.addService("network_time_update_service", networkTimeUpdateService);
                networkTimeUpdateService2 = networkTimeUpdateService;
                Trace.traceEnd(524288);
                traceBeginAndSlog("StartCommonTimeManagementService");
                commonTimeManagementService = new CommonTimeManagementService(context2);
                ServiceManager.addService("commontime_management", commonTimeManagementService);
                commonTimeManagementService2 = commonTimeManagementService;
                Trace.traceEnd(524288);
                if (disableNetwork) {
                    traceBeginAndSlog("CertBlacklister");
                    certBlacklister = new CertBlacklister(context2);
                    Trace.traceEnd(524288);
                }
                if (disableNonCoreServices) {
                    this.mSystemServiceManager.startService(DreamManagerService.class);
                }
                if (disableNonCoreServices) {
                    traceBeginAndSlog("StartAssetAtlasService");
                    assetAtlasService2 = new AssetAtlasService(context2);
                    ServiceManager.addService(AssetAtlasService.ASSET_ATLAS_SERVICE, assetAtlasService2);
                    assetAtlasService = assetAtlasService2;
                    Trace.traceEnd(524288);
                }
                if (disableNonCoreServices) {
                    ServiceManager.addService(GraphicsStatsService.GRAPHICS_STATS_SERVICE, new GraphicsStatsService(context2));
                }
                if (this.mPackageManager.hasSystemFeature("android.software.print")) {
                    this.mSystemServiceManager.startService(PRINT_MANAGER_SERVICE_CLASS);
                }
                this.mSystemServiceManager.startService(RestrictionsManagerService.class);
                this.mSystemServiceManager.startService(MediaSessionService.class);
                if (this.mPackageManager.hasSystemFeature("android.hardware.hdmi.cec")) {
                    this.mSystemServiceManager.startService(HdmiControlService.class);
                }
                if (this.mPackageManager.hasSystemFeature("android.software.live_tv")) {
                    this.mSystemServiceManager.startService(TvInputManagerService.class);
                }
                if (this.mPackageManager.hasSystemFeature("android.software.picture_in_picture")) {
                    this.mSystemServiceManager.startService(MediaResourceMonitorService.class);
                }
                if (this.mPackageManager.hasSystemFeature("android.software.leanback")) {
                    this.mSystemServiceManager.startService(TvRemoteService.class);
                }
                if (disableNonCoreServices) {
                    traceBeginAndSlog("StartMediaRouterService");
                    mediaRouterService2 = new MediaRouterService(context2);
                    ServiceManager.addService("media_router", mediaRouterService2);
                    mediaRouterService = mediaRouterService2;
                    Trace.traceEnd(524288);
                    if (disableTrustManager) {
                        this.mSystemServiceManager.startService(TrustManagerService.class);
                    }
                    traceBeginAndSlog("StartBackgroundDexOptService");
                    BackgroundDexOptService.schedule(context2);
                    Trace.traceEnd(524288);
                }
                this.mSystemServiceManager.startService(ShortcutService.Lifecycle.class);
                this.mSystemServiceManager.startService(LauncherAppsService.class);
            }
            this.mSystemServiceManager.startService(MediaProjectionManagerService.class);
            if (disableNonCoreServices) {
                this.mSystemServiceManager.startService("com.android.server.NonHardwareAcceleratedPackagesManagerService");
            }
            if (disableNonCoreServices) {
                jankshield = HwServiceFactory.getJankShieldService();
                if (jankshield != null) {
                    ServiceManager.addService("jankshield", jankshield.getInstance(context2));
                }
            }
            if (context2.getPackageManager().hasSystemFeature("android.hardware.type.watch")) {
                this.mSystemServiceManager.startService(WEAR_BLUETOOTH_SERVICE_CLASS);
            }
            safeMode = windowManagerService.detectSafeMode();
            this.mSystemServiceManager.setSafeMode(safeMode);
            if (safeMode) {
                this.mActivityManagerService.enterSafeMode();
                VMRuntime.getRuntime().disableJitCompilation();
            } else {
                VMRuntime.getRuntime().startJitCompilation();
            }
            mmsService = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
            this.mSystemServiceManager.startService("com.android.server.HwCoreAppHelperService");
            if ("true".equals(SystemProperties.get("bastet.service.enable", "false"))) {
                this.mSystemServiceManager.startService("com.android.server.HwBastetService");
            }
            this.mSystemServiceManager.startService("com.android.server.HwPPTBService");
            if ("true".equals(SystemProperties.get("ro.config.hw_emcom", "false"))) {
                startEmcomService();
            }
            Trace.traceBegin(524288, "MakeVibratorServiceReady");
            vibratorService.systemReady();
            Trace.traceEnd(524288);
            Trace.traceBegin(524288, "MakeLockSettingsServiceReady");
            if (iLockSettings != null) {
                iLockSettings.systemReady();
            }
            Trace.traceEnd(524288);
            this.mSystemServiceManager.startBootPhase(SystemService.PHASE_LOCK_SETTINGS_READY);
            this.mSystemServiceManager.startBootPhase(SystemService.PHASE_SYSTEM_SERVICES_READY);
            Trace.traceBegin(524288, "MakeWindowManagerServiceReady");
            windowManagerService.systemReady();
            Trace.traceEnd(524288);
            if (safeMode) {
                this.mActivityManagerService.showSafeModeOverlay();
            }
            config = windowManagerService.computeNewConfiguration();
            metrics = new DisplayMetrics();
            ((WindowManager) context2.getSystemService("window")).getDefaultDisplay().getMetrics(metrics);
            context2.getResources().updateConfiguration(config, metrics);
            systemTheme = context2.getTheme();
            if (systemTheme.getChangingConfigurations() != 0) {
                systemTheme.rebase();
            }
            Trace.traceBegin(524288, "MakePowerManagerServiceReady");
            this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
            Trace.traceEnd(524288);
            Trace.traceEnd(524288);
            this.mPGManagerService.systemReady(this.mActivityManagerService, this.mPowerManagerService, location);
            Trace.traceBegin(524288, "MakePackageManagerServiceReady");
            this.mPackageManagerService.systemReady();
            Trace.traceEnd(524288);
            Trace.traceBegin(524288, "MakeDisplayManagerServiceReady");
            this.mDisplayManagerService.systemReady(safeMode, this.mOnlyCore);
            Trace.traceEnd(524288);
            networkManagementF = networkManagement;
            networkStatsF = networkStats;
            networkPolicyF = networkPolicy;
            connectivityF = connectivity;
            networkScoreF = networkScoreService2;
            locationF = location;
            countryDetectorF = countryDetectorService;
            networkTimeUpdaterF = networkTimeUpdateService2;
            commonTimeMgmtServiceF = commonTimeManagementService2;
            atlasF = assetAtlasService;
            inputManagerF = inputManager;
            telephonyRegistryF = telephonyRegistry;
            mediaRouterF = mediaRouterService;
            mmsServiceF = mmsService;
            context = context2;
            this.mActivityManagerService.systemReady(/* anonymous class already generated */);
        }
        location = null;
        countryDetectorService = null;
        iLockSettings = null;
        assetAtlasService = null;
        mediaRouterService = null;
        if (this.mFactoryTestMode != 1) {
            traceBeginAndSlog("StartAccessibilityManagerService");
            Slog.i(TAG, "Input Method Service");
            this.mSystemServiceManager.startService(InputMethodManagerService.Lifecycle.class);
            if (isChinaArea) {
                Slog.i(TAG, "Secure Input Method Service");
                this.mSystemServiceManager.startService("com.android.server.HwSecureInputMethodManagerService$MyLifecycle");
            }
            ServiceManager.addService("accessibility", new AccessibilityManagerService(context2));
            Trace.traceEnd(524288);
        }
        try {
            windowManagerService.displayReady();
        } catch (Throwable e3222222222222222222222222222) {
            reportWtf("making display ready", e3222222222222222222222222222);
        }
        if (!(this.mFactoryTestMode == 1 || disableStorage || "0".equals(SystemProperties.get("system_init.startmountservice")))) {
            this.mSystemServiceManager.startService(MOUNT_SERVICE_CLASS);
            mountService = Stub.asInterface(ServiceManager.getService("mount"));
        }
        this.mSystemServiceManager.startService(UiModeManagerService.class);
        this.mActivityManagerService.bootSceneEnd(101);
        HwBootFail.setBootStage(83886090);
        if (this.mOnlyCore) {
            Trace.traceBegin(524288, "UpdatePackagesIfNeeded");
            this.mPackageManagerService.updatePackagesIfNeeded();
            Trace.traceEnd(524288);
        }
        Trace.traceBegin(524288, "PerformFstrimIfNeeded");
        try {
            this.mPackageManagerService.performFstrimIfNeeded();
        } catch (Throwable e32222222222222222222222222222) {
            reportWtf("performing fstrim", e32222222222222222222222222222);
        }
        Trace.traceEnd(524288);
        this.mActivityManagerService.bootSceneStart(HdmiCecKeycode.CEC_KEYCODE_RESTORE_VOLUME_FUNCTION, JobStatus.DEFAULT_TRIGGER_MAX_DELAY);
        HwBootFail.setBootStage(83886091);
        try {
            ActivityManagerNative.getDefault().showBootMessage(context2.getResources().getText(17040291), false);
        } catch (RemoteException e13) {
        }
        if (this.mFactoryTestMode != 1) {
            startForceRotation(context2);
            if (disableNonCoreServices) {
                traceBeginAndSlog("StartLockSettingsService");
                this.mSystemServiceManager.startService(LOCK_SETTINGS_SERVICE_CLASS);
                iLockSettings = ILockSettings.Stub.asInterface(ServiceManager.getService("lock_settings"));
                Trace.traceEnd(524288);
                if (SystemProperties.get(PERSISTENT_DATA_BLOCK_PROP).equals("")) {
                    this.mSystemServiceManager.startService(PersistentDataBlockService.class);
                }
                this.mSystemServiceManager.startService(DeviceIdleController.class);
                this.mSystemServiceManager.startService(DevicePolicyManagerService.Lifecycle.class);
            }
            if (disableSystemUI) {
                traceBeginAndSlog("StartStatusBarManagerService");
                Slog.i(TAG, "Status Bar");
                ServiceManager.addService("statusbar", HwServiceFactory.createHwStatusBarManagerService(context2, windowManagerService));
                Trace.traceEnd(524288);
            }
            if (disableNonCoreServices) {
                traceBeginAndSlog("StartClipboardService");
                ServiceManager.addService("clipboard", new ClipboardService(context2));
                Trace.traceEnd(524288);
            }
            if (disableNetwork) {
                traceBeginAndSlog("StartNetworkManagementService");
                networkManagement = NetworkManagementService.create(context2);
                ServiceManager.addService("network_management", networkManagement);
                Trace.traceEnd(524288);
            }
            if (!(disableNonCoreServices || disableTextServices)) {
                this.mSystemServiceManager.startService(TextServicesManagerService.Lifecycle.class);
            }
            if (disableNetwork) {
                traceBeginAndSlog("StartNetworkScoreService");
                networkScoreService = new NetworkScoreService(context2);
                ServiceManager.addService("network_score", networkScoreService);
                networkScoreService2 = networkScoreService;
                Trace.traceEnd(524288);
                traceBeginAndSlog("StartNetworkStatsService");
                networkStats = NetworkStatsService.create(context2, networkManagement);
                ServiceManager.addService("netstats", networkStats);
                Trace.traceEnd(524288);
                traceBeginAndSlog("StartNetworkPolicyManagerService");
                networkPolicy = HwServiceFactory.getHwNetworkPolicyManagerService().getInstance(context2, this.mActivityManagerService, (IPowerManager) ServiceManager.getService("power"), networkStats, networkManagement);
                ServiceManager.addService("netpolicy", networkPolicy);
                Trace.traceEnd(524288);
                if (context2.getPackageManager().hasSystemFeature("android.hardware.wifi.nan")) {
                    this.mSystemServiceManager.startService(WIFI_NAN_SERVICE_CLASS);
                } else {
                    Slog.i(TAG, "No Wi-Fi NAN Service (NAN support Not Present)");
                }
                this.mSystemServiceManager.startService(WIFI_P2P_SERVICE_CLASS);
                this.mSystemServiceManager.startService(WIFI_SERVICE_CLASS);
                this.mSystemServiceManager.startService("com.android.server.wifi.scanner.WifiScanningService");
                if (disableRtt) {
                    this.mSystemServiceManager.startService("com.android.server.wifi.RttService");
                }
                if (this.mPackageManager.hasSystemFeature("android.hardware.ethernet") || this.mPackageManager.hasSystemFeature("android.hardware.usb.host")) {
                    this.mSystemServiceManager.startService(ETHERNET_SERVICE_CLASS);
                }
                traceBeginAndSlog("StartConnectivityService");
                Slog.i(TAG, "Connectivity Service");
                connectivity = HwServiceFactory.getHwConnectivityManager().createHwConnectivityService(context2, networkManagement, networkStats, networkPolicy);
                ServiceManager.addService("connectivity", connectivity);
                networkStats.bindConnectivityManager(connectivity);
                networkPolicy.bindConnectivityManager(connectivity);
                Trace.traceEnd(524288);
                traceBeginAndSlog("StartNsdService");
                ServiceManager.addService("servicediscovery", NsdService.create(context2));
                Trace.traceEnd(524288);
            }
            if (disableNonCoreServices) {
                traceBeginAndSlog("StartUpdateLockService");
                ServiceManager.addService("updatelock", new UpdateLockService(context2));
                Trace.traceEnd(524288);
            }
            if (disableNonCoreServices) {
                this.mSystemServiceManager.startService(RecoverySystemService.class);
            }
            if (!(mountService == null || this.mOnlyCore)) {
                Trace.traceBegin(524288, "WaitForAsecScan");
                mountService.waitForAsecScan();
                Trace.traceEnd(524288);
            }
            this.mSystemServiceManager.startService("com.android.server.notification.HwNotificationManagerService");
            notification = INotificationManager.Stub.asInterface(ServiceManager.getService("notification"));
            if (networkPolicy != null) {
                networkPolicy.bindNotificationManager(notification);
            }
            this.mSystemServiceManager.startService(HwServiceFactory.getDeviceStorageMonitorServiceClassName());
            Slog.i(TAG, "TUI Connect enable " + tuiEnable);
            if (tuiEnable) {
                ServiceManager.addService("tui", new TrustedUIService(context2));
            }
            if (vrDisplayEnable) {
                Slog.i(TAG, "VR Display enable " + vrDisplayEnable);
                ServiceManager.addService("vr_display", new VRManagerService(context2));
            }
            if (disableLocation) {
                traceBeginAndSlog("StartLocationManagerService");
                Slog.i(TAG, "Location Manager");
                hwLocation = HwServiceFactory.getHwLocationManagerService();
                if (hwLocation == null) {
                    location = hwLocation.getInstance(context2);
                } else {
                    location = new LocationManagerService(context2);
                }
                ServiceManager.addService("location", location);
                Trace.traceEnd(524288);
                traceBeginAndSlog("StartCountryDetectorService");
                countryDetectorService2 = new CountryDetectorService(context2);
                ServiceManager.addService("country_detector", countryDetectorService2);
                countryDetectorService = countryDetectorService2;
                Trace.traceEnd(524288);
            }
            if (!(disableNonCoreServices || disableSearchManager)) {
                traceBeginAndSlog("StartSearchManagerService");
                this.mSystemServiceManager.startService(SEARCH_MANAGER_SERVICE_CLASS);
                Trace.traceEnd(524288);
            }
            this.mSystemServiceManager.startService(DropBoxManagerService.class);
            if (!disableNonCoreServices && context2.getResources().getBoolean(17956944)) {
                traceBeginAndSlog("StartWallpaperManagerService");
                this.mSystemServiceManager.startService(HwServiceFactory.getWallpaperManagerServiceClassName());
                Trace.traceEnd(524288);
            }
            traceBeginAndSlog("StartAudioService");
            this.mSystemServiceManager.startService(AudioService.Lifecycle.class);
            Trace.traceEnd(524288);
            if (disableNonCoreServices) {
                this.mSystemServiceManager.startService(DockObserver.class);
                if (context2.getPackageManager().hasSystemFeature("android.hardware.type.watch")) {
                    this.mSystemServiceManager.startService(THERMAL_OBSERVER_CLASS);
                }
            }
            traceBeginAndSlog("StartWiredAccessoryManager");
            inputManager.setWiredAccessoryCallbacks(new WiredAccessoryManager(context2, inputManager));
            Trace.traceEnd(524288);
            if (disableNonCoreServices) {
                if (this.mPackageManager.hasSystemFeature("android.software.midi")) {
                    this.mSystemServiceManager.startService(MIDI_SERVICE_CLASS);
                }
                if (this.mPackageManager.hasSystemFeature("android.hardware.usb.host") || this.mPackageManager.hasSystemFeature("android.hardware.usb.accessory")) {
                    Trace.traceBegin(524288, "StartUsbService");
                    this.mSystemServiceManager.startService(USB_SERVICE_CLASS);
                    Trace.traceEnd(524288);
                }
                if (disableSerial) {
                    traceBeginAndSlog("StartSerialService");
                    serialService = new SerialService(context2);
                    ServiceManager.addService("serial", serialService);
                    serialService2 = serialService;
                    Trace.traceEnd(524288);
                }
                Trace.traceBegin(524288, "StartHardwarePropertiesManagerService");
                hardwarePropertiesManagerService = new HardwarePropertiesManagerService(context2);
                ServiceManager.addService("hardware_properties", hardwarePropertiesManagerService);
                hardwarePropertiesManagerService2 = hardwarePropertiesManagerService;
                Trace.traceEnd(524288);
            }
            this.mSystemServiceManager.startService(TwilightService.class);
            this.mSystemServiceManager.startService("com.android.server.job.HwJobSchedulerService");
            this.mSystemServiceManager.startService(SoundTriggerService.class);
            if (disableNonCoreServices) {
                if (this.mPackageManager.hasSystemFeature("android.software.backup")) {
                    this.mSystemServiceManager.startService(BACKUP_MANAGER_SERVICE_CLASS);
                }
                if (this.mPackageManager.hasSystemFeature("android.software.app_widgets") || context2.getResources().getBoolean(17957050)) {
                    this.mSystemServiceManager.startService(APPWIDGET_SERVICE_CLASS);
                }
                if (this.mPackageManager.hasSystemFeature("android.software.voice_recognizers")) {
                    this.mSystemServiceManager.startService(VOICE_RECOGNITION_MANAGER_SERVICE_CLASS);
                }
                if (GestureLauncherService.isGestureLauncherEnabled(context2.getResources())) {
                    Slog.i(TAG, "Gesture Launcher Service");
                    this.mSystemServiceManager.startService(GestureLauncherService.class);
                }
                this.mSystemServiceManager.startService(SensorNotificationService.class);
                this.mSystemServiceManager.startService(ContextHubSystemService.class);
            }
            HwServiceFactory.setupHwServices(context2);
            traceBeginAndSlog("StartDiskStatsService");
            ServiceManager.addService("diskstats", new DiskStatsService(context2));
            Trace.traceEnd(524288);
            if (disableSamplingProfiler) {
                traceBeginAndSlog("StartSamplingProfilerService");
                ServiceManager.addService("samplingprofiler", new SamplingProfilerService(context2));
                Trace.traceEnd(524288);
            }
            Slog.i(TAG, "attestation Service");
            attestation = HwServiceFactory.getHwAttestationService();
            if (attestation != null) {
                ServiceManager.addService("attestation_service", attestation.getInstance(context2));
            }
            if (!(disableNetwork || disableNetworkTime)) {
                traceBeginAndSlog("StartNetworkTimeUpdateService");
                networkTimeUpdateService = new NetworkTimeUpdateService(context2);
                ServiceManager.addService("network_time_update_service", networkTimeUpdateService);
                networkTimeUpdateService2 = networkTimeUpdateService;
                Trace.traceEnd(524288);
            }
            traceBeginAndSlog("StartCommonTimeManagementService");
            commonTimeManagementService = new CommonTimeManagementService(context2);
            ServiceManager.addService("commontime_management", commonTimeManagementService);
            commonTimeManagementService2 = commonTimeManagementService;
            Trace.traceEnd(524288);
            if (disableNetwork) {
                traceBeginAndSlog("CertBlacklister");
                certBlacklister = new CertBlacklister(context2);
                Trace.traceEnd(524288);
            }
            if (disableNonCoreServices) {
                this.mSystemServiceManager.startService(DreamManagerService.class);
            }
            if (disableNonCoreServices) {
                traceBeginAndSlog("StartAssetAtlasService");
                assetAtlasService2 = new AssetAtlasService(context2);
                ServiceManager.addService(AssetAtlasService.ASSET_ATLAS_SERVICE, assetAtlasService2);
                assetAtlasService = assetAtlasService2;
                Trace.traceEnd(524288);
            }
            if (disableNonCoreServices) {
                ServiceManager.addService(GraphicsStatsService.GRAPHICS_STATS_SERVICE, new GraphicsStatsService(context2));
            }
            if (this.mPackageManager.hasSystemFeature("android.software.print")) {
                this.mSystemServiceManager.startService(PRINT_MANAGER_SERVICE_CLASS);
            }
            this.mSystemServiceManager.startService(RestrictionsManagerService.class);
            this.mSystemServiceManager.startService(MediaSessionService.class);
            if (this.mPackageManager.hasSystemFeature("android.hardware.hdmi.cec")) {
                this.mSystemServiceManager.startService(HdmiControlService.class);
            }
            if (this.mPackageManager.hasSystemFeature("android.software.live_tv")) {
                this.mSystemServiceManager.startService(TvInputManagerService.class);
            }
            if (this.mPackageManager.hasSystemFeature("android.software.picture_in_picture")) {
                this.mSystemServiceManager.startService(MediaResourceMonitorService.class);
            }
            if (this.mPackageManager.hasSystemFeature("android.software.leanback")) {
                this.mSystemServiceManager.startService(TvRemoteService.class);
            }
            if (disableNonCoreServices) {
                traceBeginAndSlog("StartMediaRouterService");
                mediaRouterService2 = new MediaRouterService(context2);
                ServiceManager.addService("media_router", mediaRouterService2);
                mediaRouterService = mediaRouterService2;
                Trace.traceEnd(524288);
                if (disableTrustManager) {
                    this.mSystemServiceManager.startService(TrustManagerService.class);
                }
                traceBeginAndSlog("StartBackgroundDexOptService");
                BackgroundDexOptService.schedule(context2);
                Trace.traceEnd(524288);
            }
            this.mSystemServiceManager.startService(ShortcutService.Lifecycle.class);
            this.mSystemServiceManager.startService(LauncherAppsService.class);
        }
        if (!(disableNonCoreServices || disableMediaProjection)) {
            this.mSystemServiceManager.startService(MediaProjectionManagerService.class);
        }
        if (disableNonCoreServices) {
            this.mSystemServiceManager.startService("com.android.server.NonHardwareAcceleratedPackagesManagerService");
        }
        if (disableNonCoreServices) {
            jankshield = HwServiceFactory.getJankShieldService();
            if (jankshield != null) {
                ServiceManager.addService("jankshield", jankshield.getInstance(context2));
            }
        }
        if (context2.getPackageManager().hasSystemFeature("android.hardware.type.watch")) {
            this.mSystemServiceManager.startService(WEAR_BLUETOOTH_SERVICE_CLASS);
        }
        safeMode = windowManagerService.detectSafeMode();
        this.mSystemServiceManager.setSafeMode(safeMode);
        if (safeMode) {
            this.mActivityManagerService.enterSafeMode();
            VMRuntime.getRuntime().disableJitCompilation();
        } else {
            VMRuntime.getRuntime().startJitCompilation();
        }
        mmsService = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
        try {
            this.mSystemServiceManager.startService("com.android.server.HwCoreAppHelperService");
        } catch (Exception e14) {
            Slog.w(TAG, "HwCoreAppHelperService not exists.");
        }
        if ("true".equals(SystemProperties.get("bastet.service.enable", "false"))) {
            this.mSystemServiceManager.startService("com.android.server.HwBastetService");
        }
        try {
            this.mSystemServiceManager.startService("com.android.server.HwPPTBService");
        } catch (Exception e15) {
            Slog.w(TAG, "HwPPTBService not exists.");
        }
        if ("true".equals(SystemProperties.get("ro.config.hw_emcom", "false"))) {
            startEmcomService();
        }
        Trace.traceBegin(524288, "MakeVibratorServiceReady");
        try {
            vibratorService.systemReady();
        } catch (Throwable e322222222222222222222222222222) {
            reportWtf("making Vibrator Service ready", e322222222222222222222222222222);
        }
        Trace.traceEnd(524288);
        Trace.traceBegin(524288, "MakeLockSettingsServiceReady");
        if (iLockSettings != null) {
            iLockSettings.systemReady();
        }
        Trace.traceEnd(524288);
        this.mSystemServiceManager.startBootPhase(SystemService.PHASE_LOCK_SETTINGS_READY);
        this.mSystemServiceManager.startBootPhase(SystemService.PHASE_SYSTEM_SERVICES_READY);
        Trace.traceBegin(524288, "MakeWindowManagerServiceReady");
        try {
            windowManagerService.systemReady();
        } catch (Throwable e3222222222222222222222222222222) {
            reportWtf("making Window Manager Service ready", e3222222222222222222222222222222);
        }
        Trace.traceEnd(524288);
        if (safeMode) {
            this.mActivityManagerService.showSafeModeOverlay();
        }
        config = windowManagerService.computeNewConfiguration();
        metrics = new DisplayMetrics();
        ((WindowManager) context2.getSystemService("window")).getDefaultDisplay().getMetrics(metrics);
        context2.getResources().updateConfiguration(config, metrics);
        systemTheme = context2.getTheme();
        if (systemTheme.getChangingConfigurations() != 0) {
            systemTheme.rebase();
        }
        Trace.traceBegin(524288, "MakePowerManagerServiceReady");
        try {
            this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
            Trace.traceEnd(524288);
        } catch (Throwable e32222222222222222222222222222222) {
            reportWtf("making Power Manager Service ready", e32222222222222222222222222222222);
        }
        Trace.traceEnd(524288);
        try {
            this.mPGManagerService.systemReady(this.mActivityManagerService, this.mPowerManagerService, location);
        } catch (Throwable e322222222222222222222222222222222) {
            reportWtf("making PG Manager Service ready", e322222222222222222222222222222222);
        }
        Trace.traceBegin(524288, "MakePackageManagerServiceReady");
        try {
            this.mPackageManagerService.systemReady();
        } catch (Throwable e3222222222222222222222222222222222) {
            reportWtf("making Package Manager Service ready", e3222222222222222222222222222222222);
        }
        Trace.traceEnd(524288);
        Trace.traceBegin(524288, "MakeDisplayManagerServiceReady");
        try {
            this.mDisplayManagerService.systemReady(safeMode, this.mOnlyCore);
        } catch (Throwable e32222222222222222222222222222222222) {
            reportWtf("making Display Manager Service ready", e32222222222222222222222222222222222);
        }
        Trace.traceEnd(524288);
        networkManagementF = networkManagement;
        networkStatsF = networkStats;
        networkPolicyF = networkPolicy;
        connectivityF = connectivity;
        networkScoreF = networkScoreService2;
        locationF = location;
        countryDetectorF = countryDetectorService;
        networkTimeUpdaterF = networkTimeUpdateService2;
        commonTimeMgmtServiceF = commonTimeManagementService2;
        atlasF = assetAtlasService;
        inputManagerF = inputManager;
        telephonyRegistryF = telephonyRegistry;
        mediaRouterF = mediaRouterService;
        mmsServiceF = mmsService;
        context = context2;
        this.mActivityManagerService.systemReady(/* anonymous class already generated */);
    }

    private void sendAirplaneModeChangedBroadcast(Context context, boolean on) {
        Intent intent = new Intent("android.intent.action.AIRPLANE_MODE");
        intent.addFlags(67108864);
        intent.putExtra(AudioService.CONNECT_INTENT_KEY_STATE, on);
        context.sendBroadcast(intent);
    }

    private void setAirplaneMode(Context context) {
        boolean z = LOCAL_LOGV;
        try {
            int lastAirplaneMode = Global.getInt(context.getContentResolver(), "airplane_mode_on", 0);
            Slog.v(TAG, "lastAirplaneMode= " + lastAirplaneMode);
            if (!"RTC".equals(SystemProperties.get("persist.sys.powerup_reason", "NORMAL")) || SystemProperties.getLong("ro.runtime.firstboot", 0) != 0) {
                SystemProperties.set("persist.sys.hwairplanestate", "error");
                Slog.d(TAG, "systemserver,hw airplane prop = error");
                int userChangeAirplane = Global.getInt(context.getContentResolver(), "user_set_airplane", -1);
                Slog.v(TAG, "normal power on");
                Slog.v(TAG, "userSetAirplane= " + userChangeAirplane);
                if (-1 != userChangeAirplane) {
                    Global.putInt(context.getContentResolver(), "airplane_mode_on", userChangeAirplane);
                    if (userChangeAirplane != 1) {
                        z = false;
                    }
                    sendAirplaneModeChangedBroadcast(context, z);
                }
            } else if (DESKCLOCK_PACKAGENAME.equals(SystemProperties.get("persist.sys.hwairplanestate", "error"))) {
                Slog.v(TAG, "RTC power on");
                Global.putInt(context.getContentResolver(), "airplane_mode_on", 1);
                Global.putInt(context.getContentResolver(), "user_set_airplane", lastAirplaneMode);
                sendAirplaneModeChangedBroadcast(context, LOCAL_LOGV);
            }
        } catch (Throwable e) {
            Slog.e(TAG, "power off alarm occure error:", e);
        }
    }

    private void startEmcomService() {
        Trace.traceBegin(524288, "EmcomManagerService");
        try {
            this.mSystemServiceManager.startService("com.android.server.emcom.EmcomManagerService");
        } catch (Exception e) {
            Slog.w(TAG, "EmcomManagerService not exists");
        }
        Trace.traceEnd(524288);
    }

    static final void startSystemUi(Context context) {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.android.systemui", "com.android.systemui.SystemUIService"));
        intent.addFlags(256);
        context.startServiceAsUser(intent, UserHandle.SYSTEM);
    }

    private static void traceBeginAndSlog(String name) {
        Trace.traceBegin(524288, name);
        Slog.i(TAG, name);
    }

    private void restoreRogMode(WindowManagerService wm, Context context) {
        if (wm != null) {
            if (SystemProperties.getInt("persist.sys.rog.configmode", 0) == 1) {
                Slog.i(TAG, "rog 2.0 funciton is open, resotre it");
                SystemProperties.set("persist.sys.realdpi", "");
                SystemProperties.set("persist.sys.rog.width", "");
                SystemProperties.set("persist.sys.rog.height", "");
                Global.putString(context.getContentResolver(), "display_size_forced", "");
                wm.setForcedDisplayDensity(0, DisplayMetrics.DENSITY_DEVICE);
                SystemProperties.set("persist.sys.rog.configmode", "0");
            } else if (SystemProperties.getInt("persist.sys.rog.width", 0) != 0) {
                SystemProperties.set("persist.sys.rog.width", "");
                SystemProperties.set("persist.sys.rog.height", "");
            }
        }
    }

    private void startForceRotation(Context context) {
        if (HwFrameworkFactory.getForceRotationManager().isForceRotationSupported()) {
            try {
                Slog.i(TAG, "Force rotation Service, name = forceRotationService");
                IHwForceRotationManagerServiceWrapper ifrsw = HwServiceFactory.getForceRotationManagerServiceWrapper();
                if (ifrsw != null) {
                    ServiceManager.addService("forceRotationService", ifrsw.getServiceInstance(context, UiThread.getHandler()));
                }
            } catch (Throwable e) {
                reportWtf("starting Force rotation service", e);
            }
        }
    }
}
