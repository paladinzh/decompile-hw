package android.app;

import android.R;
import android.accounts.AccountManager;
import android.accounts.IAccountManager.Stub;
import android.app.admin.DevicePolicyManager;
import android.app.job.IJobScheduler;
import android.app.job.JobScheduler;
import android.app.trust.TrustManager;
import android.app.usage.IUsageStatsManager;
import android.app.usage.NetworkStatsManager;
import android.app.usage.UsageStatsManager;
import android.appwidget.AppWidgetManager;
import android.bluetooth.BluetoothManager;
import android.common.HwFrameworkFactory;
import android.common.HwFrameworkFactory.IHwNetworkPolicyManager;
import android.common.HwFrameworkFactory.IHwWallpaperManager;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.IRestrictionsManager;
import android.content.RestrictionsManager;
import android.content.pm.LauncherApps;
import android.content.pm.ShortcutManager;
import android.content.res.Resources;
import android.hardware.ConsumerIrManager;
import android.hardware.ISerialManager;
import android.hardware.SensorManager;
import android.hardware.SerialManager;
import android.hardware.SystemSensorManager;
import android.hardware.camera2.CameraManager;
import android.hardware.display.DisplayManager;
import android.hardware.fingerprint.FingerprintManager;
import android.hardware.fingerprint.IFingerprintService;
import android.hardware.hdmi.HdmiControlManager;
import android.hardware.hdmi.IHdmiControlService;
import android.hardware.input.InputManager;
import android.hardware.location.ContextHubManager;
import android.hardware.radio.RadioManager;
import android.hardware.usb.IUsbManager;
import android.hardware.usb.UsbManager;
import android.location.CountryDetector;
import android.location.ICountryDetector;
import android.location.ILocationManager;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.MediaRouter;
import android.media.midi.IMidiManager;
import android.media.midi.MidiManager;
import android.media.projection.MediaProjectionManager;
import android.media.session.MediaSessionManager;
import android.media.soundtrigger.SoundTriggerManager;
import android.media.tv.ITvInputManager;
import android.media.tv.TvInputManager;
import android.net.ConnectivityManager;
import android.net.ConnectivityThread;
import android.net.EthernetManager;
import android.net.IConnectivityManager;
import android.net.IEthernetManager;
import android.net.INetworkPolicyManager;
import android.net.NetworkPolicyManager;
import android.net.NetworkScoreManager;
import android.net.nsd.INsdManager;
import android.net.nsd.NsdManager;
import android.net.wifi.IRttManager;
import android.net.wifi.IWifiManager;
import android.net.wifi.IWifiScanner;
import android.net.wifi.RttManager;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiScanner;
import android.net.wifi.nan.IWifiNanManager;
import android.net.wifi.nan.WifiNanManager;
import android.net.wifi.p2p.IWifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.nfc.NfcManager;
import android.os.BatteryManager;
import android.os.DropBoxManager;
import android.os.HardwarePropertiesManager;
import android.os.IBinder;
import android.os.IHardwarePropertiesManager;
import android.os.IPowerManager;
import android.os.IRecoverySystem;
import android.os.IUserManager;
import android.os.PowerManager;
import android.os.Process;
import android.os.RecoverySystem;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.SystemVibrator;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.Vibrator;
import android.os.health.SystemHealthManager;
import android.os.storage.StorageManager;
import android.print.IPrintManager;
import android.print.PrintManager;
import android.service.persistentdata.IPersistentDataBlockService;
import android.service.persistentdata.PersistentDataBlockManager;
import android.telecom.TelecomManager;
import android.telephony.CarrierConfigManager;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.view.WindowManagerImpl;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.CaptioningManager;
import android.view.inputmethod.InputMethodManager;
import android.view.textservice.TextServicesManager;
import android.vr.IVRManagerService;
import android.vr.VrServiceManager;
import android.vrsystem.IVRSystemServiceManager;
import com.android.internal.app.IAppOpsService;
import com.android.internal.app.ISoundTriggerService;
import com.android.internal.appwidget.IAppWidgetService;
import com.android.internal.os.IDropBoxManagerService;
import com.android.internal.policy.HwPolicyFactory;
import java.util.HashMap;

final class SystemServiceRegistry {
    private static final HashMap<String, ServiceFetcher<?>> SYSTEM_SERVICE_FETCHERS = new HashMap();
    private static final HashMap<Class<?>, String> SYSTEM_SERVICE_NAMES = new HashMap();
    private static final String TAG = "SystemServiceRegistry";
    private static int sServiceCacheSize;

    interface ServiceFetcher<T> {
        T getService(ContextImpl contextImpl);
    }

    static abstract class CachedServiceFetcher<T> implements ServiceFetcher<T> {
        private final int mCacheIndex;

        public abstract T createService(ContextImpl contextImpl);

        public CachedServiceFetcher() {
            int -get0 = SystemServiceRegistry.sServiceCacheSize;
            SystemServiceRegistry.sServiceCacheSize = -get0 + 1;
            this.mCacheIndex = -get0;
        }

        public final T getService(ContextImpl ctx) {
            Object service;
            Object[] cache = ctx.mServiceCache;
            synchronized (cache) {
                service = cache[this.mCacheIndex];
                if (service == null) {
                    service = createService(ctx);
                    cache[this.mCacheIndex] = service;
                }
            }
            return service;
        }
    }

    static abstract class StaticServiceFetcher<T> implements ServiceFetcher<T> {
        private T mCachedInstance;

        public abstract T createService();

        StaticServiceFetcher() {
        }

        public final T getService(ContextImpl unused) {
            T t;
            synchronized (this) {
                if (this.mCachedInstance == null) {
                    this.mCachedInstance = createService();
                }
                t = this.mCachedInstance;
            }
            return t;
        }
    }

    static abstract class StaticApplicationContextServiceFetcher<T> implements ServiceFetcher<T> {
        private T mCachedInstance;

        public abstract T createService(Context context);

        StaticApplicationContextServiceFetcher() {
        }

        public final T getService(ContextImpl ctx) {
            T t;
            synchronized (this) {
                if (this.mCachedInstance == null) {
                    Context appContext = ctx.getApplicationContext();
                    if (appContext == null) {
                        appContext = ctx;
                    }
                    this.mCachedInstance = createService(appContext);
                }
                t = this.mCachedInstance;
            }
            return t;
        }
    }

    static {
        registerService(Context.ACCESSIBILITY_SERVICE, AccessibilityManager.class, new CachedServiceFetcher<AccessibilityManager>() {
            public AccessibilityManager createService(ContextImpl ctx) {
                return AccessibilityManager.getInstance(ctx);
            }
        });
        registerService(Context.CAPTIONING_SERVICE, CaptioningManager.class, new CachedServiceFetcher<CaptioningManager>() {
            public CaptioningManager createService(ContextImpl ctx) {
                return new CaptioningManager(ctx);
            }
        });
        registerService("account", AccountManager.class, new CachedServiceFetcher<AccountManager>() {
            public AccountManager createService(ContextImpl ctx) {
                return new AccountManager(ctx, Stub.asInterface(ServiceManager.getService("account")));
            }
        });
        registerService("activity", ActivityManager.class, new CachedServiceFetcher<ActivityManager>() {
            public ActivityManager createService(ContextImpl ctx) {
                return new ActivityManager(ctx.getOuterContext(), ctx.mMainThread.getHandler());
            }
        });
        registerService("alarm", AlarmManager.class, new CachedServiceFetcher<AlarmManager>() {
            public AlarmManager createService(ContextImpl ctx) {
                return new AlarmManager(IAlarmManager.Stub.asInterface(ServiceManager.getService("alarm")), ctx);
            }
        });
        registerService(Context.AUDIO_SERVICE, AudioManager.class, new CachedServiceFetcher<AudioManager>() {
            public AudioManager createService(ContextImpl ctx) {
                return new AudioManager(ctx);
            }
        });
        registerService(Context.MEDIA_ROUTER_SERVICE, MediaRouter.class, new CachedServiceFetcher<MediaRouter>() {
            public MediaRouter createService(ContextImpl ctx) {
                return new MediaRouter(ctx);
            }
        });
        registerService("bluetooth", BluetoothManager.class, new CachedServiceFetcher<BluetoothManager>() {
            public BluetoothManager createService(ContextImpl ctx) {
                return new BluetoothManager(ctx);
            }
        });
        registerService("vr_system", IVRSystemServiceManager.class, new CachedServiceFetcher<IVRSystemServiceManager>() {
            public IVRSystemServiceManager createService(ContextImpl ctx) {
                return HwFrameworkFactory.getVRSystemServiceManager();
            }
        });
        if (SystemProperties.getBoolean("ro.vr_display.service", false)) {
            registerService("vr_display", VrServiceManager.class, new CachedServiceFetcher<VrServiceManager>() {
                public VrServiceManager createService(ContextImpl ctx) {
                    return new VrServiceManager(IVRManagerService.Stub.asInterface(ServiceManager.getService("vr_display")), ctx);
                }
            });
        }
        registerService(Context.HDMI_CONTROL_SERVICE, HdmiControlManager.class, new StaticServiceFetcher<HdmiControlManager>() {
            public HdmiControlManager createService() {
                return new HdmiControlManager(IHdmiControlService.Stub.asInterface(ServiceManager.getService(Context.HDMI_CONTROL_SERVICE)));
            }
        });
        registerService(Context.CLIPBOARD_SERVICE, ClipboardManager.class, new CachedServiceFetcher<ClipboardManager>() {
            public ClipboardManager createService(ContextImpl ctx) {
                return new ClipboardManager(ctx.getOuterContext(), ctx.mMainThread.getHandler());
            }
        });
        SYSTEM_SERVICE_NAMES.put(android.text.ClipboardManager.class, Context.CLIPBOARD_SERVICE);
        registerService(Context.CONNECTIVITY_SERVICE, ConnectivityManager.class, new StaticApplicationContextServiceFetcher<ConnectivityManager>() {
            public ConnectivityManager createService(Context context) {
                return new ConnectivityManager(context, IConnectivityManager.Stub.asInterface(ServiceManager.getService(Context.CONNECTIVITY_SERVICE)));
            }
        });
        registerService(Context.COUNTRY_DETECTOR, CountryDetector.class, new StaticServiceFetcher<CountryDetector>() {
            public CountryDetector createService() {
                return new CountryDetector(ICountryDetector.Stub.asInterface(ServiceManager.getService(Context.COUNTRY_DETECTOR)));
            }
        });
        registerService(Context.DEVICE_POLICY_SERVICE, DevicePolicyManager.class, new CachedServiceFetcher<DevicePolicyManager>() {
            public DevicePolicyManager createService(ContextImpl ctx) {
                return DevicePolicyManager.create(ctx);
            }
        });
        registerService(Context.DOWNLOAD_SERVICE, DownloadManager.class, new CachedServiceFetcher<DownloadManager>() {
            public DownloadManager createService(ContextImpl ctx) {
                return new DownloadManager(ctx);
            }
        });
        registerService(Context.BATTERY_SERVICE, BatteryManager.class, new StaticServiceFetcher<BatteryManager>() {
            public BatteryManager createService() {
                return new BatteryManager();
            }
        });
        registerService("nfc", NfcManager.class, new CachedServiceFetcher<NfcManager>() {
            public NfcManager createService(ContextImpl ctx) {
                return new NfcManager(ctx);
            }
        });
        registerService(Context.DROPBOX_SERVICE, DropBoxManager.class, new CachedServiceFetcher<DropBoxManager>() {
            public DropBoxManager createService(ContextImpl ctx) {
                IDropBoxManagerService service = IDropBoxManagerService.Stub.asInterface(ServiceManager.getService(Context.DROPBOX_SERVICE));
                if (service == null) {
                    return null;
                }
                return new DropBoxManager(ctx, service);
            }
        });
        registerService("input", InputManager.class, new StaticServiceFetcher<InputManager>() {
            public InputManager createService() {
                return InputManager.getInstance();
            }
        });
        registerService(Context.DISPLAY_SERVICE, DisplayManager.class, new CachedServiceFetcher<DisplayManager>() {
            public DisplayManager createService(ContextImpl ctx) {
                return new DisplayManager(ctx.getOuterContext());
            }
        });
        registerService(Context.INPUT_METHOD_SERVICE, InputMethodManager.class, new StaticServiceFetcher<InputMethodManager>() {
            public InputMethodManager createService() {
                return InputMethodManager.getInstance();
            }
        });
        registerService(Context.TEXT_SERVICES_MANAGER_SERVICE, TextServicesManager.class, new StaticServiceFetcher<TextServicesManager>() {
            public TextServicesManager createService() {
                return TextServicesManager.getInstance();
            }
        });
        registerService(Context.KEYGUARD_SERVICE, KeyguardManager.class, new StaticServiceFetcher<KeyguardManager>() {
            public KeyguardManager createService() {
                return new KeyguardManager();
            }
        });
        registerService(Context.LAYOUT_INFLATER_SERVICE, LayoutInflater.class, new CachedServiceFetcher<LayoutInflater>() {
            public LayoutInflater createService(ContextImpl ctx) {
                return HwPolicyFactory.getHwPhoneLayoutInflater(ctx.getOuterContext());
            }
        });
        registerService("location", LocationManager.class, new CachedServiceFetcher<LocationManager>() {
            public LocationManager createService(ContextImpl ctx) {
                return new LocationManager(ctx, ILocationManager.Stub.asInterface(ServiceManager.getService("location")));
            }
        });
        registerService(Context.NETWORK_POLICY_SERVICE, NetworkPolicyManager.class, new CachedServiceFetcher<NetworkPolicyManager>() {
            public NetworkPolicyManager createService(ContextImpl ctx) {
                IHwNetworkPolicyManager inpm = HwFrameworkFactory.getHwNetworkPolicyManager();
                if (inpm != null) {
                    return inpm.getInstance(ctx, INetworkPolicyManager.Stub.asInterface(ServiceManager.getService(Context.NETWORK_POLICY_SERVICE)));
                }
                return new NetworkPolicyManager(ctx, INetworkPolicyManager.Stub.asInterface(ServiceManager.getService(Context.NETWORK_POLICY_SERVICE)));
            }
        });
        registerService(Context.NOTIFICATION_SERVICE, NotificationManager.class, new CachedServiceFetcher<NotificationManager>() {
            public NotificationManager createService(ContextImpl ctx) {
                Context outerContext = ctx.getOuterContext();
                return new NotificationManager(new ContextThemeWrapper(outerContext, Resources.selectSystemTheme(0, outerContext.getApplicationInfo().targetSdkVersion, R.style.Theme_Dialog, R.style.Theme_Holo_Dialog, R.style.Theme_DeviceDefault_Dialog, R.style.Theme_DeviceDefault_Light_Dialog)), ctx.mMainThread.getHandler());
            }
        });
        registerService(Context.NSD_SERVICE, NsdManager.class, new CachedServiceFetcher<NsdManager>() {
            public NsdManager createService(ContextImpl ctx) {
                return new NsdManager(ctx.getOuterContext(), INsdManager.Stub.asInterface(ServiceManager.getService(Context.NSD_SERVICE)));
            }
        });
        registerService(Context.POWER_SERVICE, PowerManager.class, new CachedServiceFetcher<PowerManager>() {
            public PowerManager createService(ContextImpl ctx) {
                IPowerManager service = IPowerManager.Stub.asInterface(ServiceManager.getService(Context.POWER_SERVICE));
                if (service == null) {
                    Log.wtf(SystemServiceRegistry.TAG, "Failed to get power manager service.");
                }
                return new PowerManager(ctx.getOuterContext(), service, ctx.mMainThread.getHandler());
            }
        });
        registerService("recovery", RecoverySystem.class, new CachedServiceFetcher<RecoverySystem>() {
            public RecoverySystem createService(ContextImpl ctx) {
                IRecoverySystem service = IRecoverySystem.Stub.asInterface(ServiceManager.getService("recovery"));
                if (service == null) {
                    Log.wtf(SystemServiceRegistry.TAG, "Failed to get recovery service.");
                }
                return new RecoverySystem(service);
            }
        });
        registerService("search", SearchManager.class, new CachedServiceFetcher<SearchManager>() {
            public SearchManager createService(ContextImpl ctx) {
                return new SearchManager(ctx.getOuterContext(), ctx.mMainThread.getHandler());
            }
        });
        registerService(Context.SENSOR_SERVICE, SensorManager.class, new CachedServiceFetcher<SensorManager>() {
            public SensorManager createService(ContextImpl ctx) {
                return new SystemSensorManager(ctx.getOuterContext(), ctx.mMainThread.getHandler().getLooper());
            }
        });
        registerService(Context.STATUS_BAR_SERVICE, StatusBarManager.class, new CachedServiceFetcher<StatusBarManager>() {
            public StatusBarManager createService(ContextImpl ctx) {
                return new StatusBarManager(ctx.getOuterContext());
            }
        });
        registerService(Context.STORAGE_SERVICE, StorageManager.class, new CachedServiceFetcher<StorageManager>() {
            public StorageManager createService(ContextImpl ctx) {
                return new StorageManager(ctx, ctx.mMainThread.getHandler().getLooper());
            }
        });
        registerService("phone", TelephonyManager.class, new CachedServiceFetcher<TelephonyManager>() {
            public TelephonyManager createService(ContextImpl ctx) {
                return new TelephonyManager(ctx.getOuterContext());
            }
        });
        registerService(Context.TELEPHONY_SUBSCRIPTION_SERVICE, SubscriptionManager.class, new CachedServiceFetcher<SubscriptionManager>() {
            public SubscriptionManager createService(ContextImpl ctx) {
                return new SubscriptionManager(ctx.getOuterContext());
            }
        });
        registerService(Context.CARRIER_CONFIG_SERVICE, CarrierConfigManager.class, new CachedServiceFetcher<CarrierConfigManager>() {
            public CarrierConfigManager createService(ContextImpl ctx) {
                return new CarrierConfigManager();
            }
        });
        registerService(Context.TELECOM_SERVICE, TelecomManager.class, new CachedServiceFetcher<TelecomManager>() {
            public TelecomManager createService(ContextImpl ctx) {
                return new TelecomManager(ctx.getOuterContext());
            }
        });
        registerService(Context.UI_MODE_SERVICE, UiModeManager.class, new CachedServiceFetcher<UiModeManager>() {
            public UiModeManager createService(ContextImpl ctx) {
                return new UiModeManager();
            }
        });
        registerService(Context.USB_SERVICE, UsbManager.class, new CachedServiceFetcher<UsbManager>() {
            public UsbManager createService(ContextImpl ctx) {
                return new UsbManager(ctx, IUsbManager.Stub.asInterface(ServiceManager.getService(Context.USB_SERVICE)));
            }
        });
        registerService(Context.SERIAL_SERVICE, SerialManager.class, new CachedServiceFetcher<SerialManager>() {
            public SerialManager createService(ContextImpl ctx) {
                return new SerialManager(ctx, ISerialManager.Stub.asInterface(ServiceManager.getService(Context.SERIAL_SERVICE)));
            }
        });
        registerService(Context.VIBRATOR_SERVICE, Vibrator.class, new CachedServiceFetcher<Vibrator>() {
            public Vibrator createService(ContextImpl ctx) {
                return new SystemVibrator(ctx);
            }
        });
        registerService("wallpaper", WallpaperManager.class, new CachedServiceFetcher<WallpaperManager>() {
            public WallpaperManager createService(ContextImpl ctx) {
                IHwWallpaperManager iwm = HwFrameworkFactory.getHuaweiWallpaperManager();
                if (iwm != null) {
                    return iwm.getInstance(ctx.getOuterContext(), ctx.mMainThread.getHandler());
                }
                return new WallpaperManager(ctx.getOuterContext(), ctx.mMainThread.getHandler());
            }
        });
        registerService("wifi", WifiManager.class, new CachedServiceFetcher<WifiManager>() {
            public WifiManager createService(ContextImpl ctx) {
                return new WifiManager(ctx.getOuterContext(), IWifiManager.Stub.asInterface(ServiceManager.getService("wifi")), ConnectivityThread.getInstanceLooper());
            }
        });
        registerService(Context.WIFI_P2P_SERVICE, WifiP2pManager.class, new StaticServiceFetcher<WifiP2pManager>() {
            public WifiP2pManager createService() {
                return new WifiP2pManager(IWifiP2pManager.Stub.asInterface(ServiceManager.getService(Context.WIFI_P2P_SERVICE)));
            }
        });
        registerService(Context.WIFI_NAN_SERVICE, WifiNanManager.class, new StaticServiceFetcher<WifiNanManager>() {
            public WifiNanManager createService() {
                IWifiNanManager service = IWifiNanManager.Stub.asInterface(ServiceManager.getService(Context.WIFI_NAN_SERVICE));
                if (service == null) {
                    return null;
                }
                return new WifiNanManager(service);
            }
        });
        registerService(Context.WIFI_SCANNING_SERVICE, WifiScanner.class, new CachedServiceFetcher<WifiScanner>() {
            public WifiScanner createService(ContextImpl ctx) {
                return new WifiScanner(ctx.getOuterContext(), IWifiScanner.Stub.asInterface(ServiceManager.getService(Context.WIFI_SCANNING_SERVICE)), ConnectivityThread.getInstanceLooper());
            }
        });
        registerService(Context.WIFI_RTT_SERVICE, RttManager.class, new CachedServiceFetcher<RttManager>() {
            public RttManager createService(ContextImpl ctx) {
                return new RttManager(ctx.getOuterContext(), IRttManager.Stub.asInterface(ServiceManager.getService(Context.WIFI_RTT_SERVICE)), ConnectivityThread.getInstanceLooper());
            }
        });
        registerService(Context.ETHERNET_SERVICE, EthernetManager.class, new CachedServiceFetcher<EthernetManager>() {
            public EthernetManager createService(ContextImpl ctx) {
                return new EthernetManager(ctx.getOuterContext(), IEthernetManager.Stub.asInterface(ServiceManager.getService(Context.ETHERNET_SERVICE)));
            }
        });
        registerService("window", WindowManager.class, new CachedServiceFetcher<WindowManager>() {
            public WindowManager createService(ContextImpl ctx) {
                return new WindowManagerImpl(ctx);
            }
        });
        registerService(Context.USER_SERVICE, UserManager.class, new CachedServiceFetcher<UserManager>() {
            public UserManager createService(ContextImpl ctx) {
                return new UserManager(ctx, IUserManager.Stub.asInterface(ServiceManager.getService(Context.USER_SERVICE)));
            }
        });
        registerService(Context.APP_OPS_SERVICE, AppOpsManager.class, new CachedServiceFetcher<AppOpsManager>() {
            public AppOpsManager createService(ContextImpl ctx) {
                return new AppOpsManager(ctx, IAppOpsService.Stub.asInterface(ServiceManager.getService(Context.APP_OPS_SERVICE)));
            }
        });
        registerService(Context.CAMERA_SERVICE, CameraManager.class, new CachedServiceFetcher<CameraManager>() {
            public CameraManager createService(ContextImpl ctx) {
                return new CameraManager(ctx);
            }
        });
        registerService(Context.LAUNCHER_APPS_SERVICE, LauncherApps.class, new CachedServiceFetcher<LauncherApps>() {
            public LauncherApps createService(ContextImpl ctx) {
                return new LauncherApps(ctx);
            }
        });
        registerService(Context.RESTRICTIONS_SERVICE, RestrictionsManager.class, new CachedServiceFetcher<RestrictionsManager>() {
            public RestrictionsManager createService(ContextImpl ctx) {
                return new RestrictionsManager(ctx, IRestrictionsManager.Stub.asInterface(ServiceManager.getService(Context.RESTRICTIONS_SERVICE)));
            }
        });
        registerService(Context.PRINT_SERVICE, PrintManager.class, new CachedServiceFetcher<PrintManager>() {
            public PrintManager createService(ContextImpl ctx) {
                return new PrintManager(ctx.getOuterContext(), IPrintManager.Stub.asInterface(ServiceManager.getService(Context.PRINT_SERVICE)), UserHandle.myUserId(), UserHandle.getAppId(Process.myUid()));
            }
        });
        registerService(Context.CONSUMER_IR_SERVICE, ConsumerIrManager.class, new CachedServiceFetcher<ConsumerIrManager>() {
            public ConsumerIrManager createService(ContextImpl ctx) {
                return new ConsumerIrManager(ctx);
            }
        });
        registerService(Context.MEDIA_SESSION_SERVICE, MediaSessionManager.class, new CachedServiceFetcher<MediaSessionManager>() {
            public MediaSessionManager createService(ContextImpl ctx) {
                return new MediaSessionManager(ctx);
            }
        });
        registerService(Context.TRUST_SERVICE, TrustManager.class, new StaticServiceFetcher<TrustManager>() {
            public TrustManager createService() {
                return new TrustManager(ServiceManager.getService(Context.TRUST_SERVICE));
            }
        });
        registerService(Context.FINGERPRINT_SERVICE, FingerprintManager.class, new CachedServiceFetcher<FingerprintManager>() {
            public FingerprintManager createService(ContextImpl ctx) {
                return new FingerprintManager(ctx.getOuterContext(), IFingerprintService.Stub.asInterface(ServiceManager.getService(Context.FINGERPRINT_SERVICE)));
            }
        });
        registerService(Context.TV_INPUT_SERVICE, TvInputManager.class, new StaticServiceFetcher<TvInputManager>() {
            public TvInputManager createService() {
                return new TvInputManager(ITvInputManager.Stub.asInterface(ServiceManager.getService(Context.TV_INPUT_SERVICE)), UserHandle.myUserId());
            }
        });
        registerService(Context.NETWORK_SCORE_SERVICE, NetworkScoreManager.class, new CachedServiceFetcher<NetworkScoreManager>() {
            public NetworkScoreManager createService(ContextImpl ctx) {
                return new NetworkScoreManager(ctx);
            }
        });
        registerService(Context.USAGE_STATS_SERVICE, UsageStatsManager.class, new CachedServiceFetcher<UsageStatsManager>() {
            public UsageStatsManager createService(ContextImpl ctx) {
                return new UsageStatsManager(ctx.getOuterContext(), IUsageStatsManager.Stub.asInterface(ServiceManager.getService(Context.USAGE_STATS_SERVICE)));
            }
        });
        registerService(Context.NETWORK_STATS_SERVICE, NetworkStatsManager.class, new CachedServiceFetcher<NetworkStatsManager>() {
            public NetworkStatsManager createService(ContextImpl ctx) {
                return new NetworkStatsManager(ctx.getOuterContext());
            }
        });
        registerService(Context.JOB_SCHEDULER_SERVICE, JobScheduler.class, new StaticServiceFetcher<JobScheduler>() {
            public JobScheduler createService() {
                return new JobSchedulerImpl(IJobScheduler.Stub.asInterface(ServiceManager.getService(Context.JOB_SCHEDULER_SERVICE)));
            }
        });
        registerService(Context.PERSISTENT_DATA_BLOCK_SERVICE, PersistentDataBlockManager.class, new StaticServiceFetcher<PersistentDataBlockManager>() {
            public PersistentDataBlockManager createService() {
                IPersistentDataBlockService persistentDataBlockService = IPersistentDataBlockService.Stub.asInterface(ServiceManager.getService(Context.PERSISTENT_DATA_BLOCK_SERVICE));
                if (persistentDataBlockService != null) {
                    return new PersistentDataBlockManager(persistentDataBlockService);
                }
                return null;
            }
        });
        registerService(Context.MEDIA_PROJECTION_SERVICE, MediaProjectionManager.class, new CachedServiceFetcher<MediaProjectionManager>() {
            public MediaProjectionManager createService(ContextImpl ctx) {
                return new MediaProjectionManager(ctx);
            }
        });
        registerService(Context.APPWIDGET_SERVICE, AppWidgetManager.class, new CachedServiceFetcher<AppWidgetManager>() {
            public AppWidgetManager createService(ContextImpl ctx) {
                return new AppWidgetManager(ctx, IAppWidgetService.Stub.asInterface(ServiceManager.getService(Context.APPWIDGET_SERVICE)));
            }
        });
        registerService("midi", MidiManager.class, new CachedServiceFetcher<MidiManager>() {
            public MidiManager createService(ContextImpl ctx) {
                IBinder b = ServiceManager.getService("midi");
                if (b == null) {
                    return null;
                }
                return new MidiManager(IMidiManager.Stub.asInterface(b));
            }
        });
        registerService(Context.RADIO_SERVICE, RadioManager.class, new CachedServiceFetcher<RadioManager>() {
            public RadioManager createService(ContextImpl ctx) {
                return new RadioManager(ctx);
            }
        });
        registerService(Context.HARDWARE_PROPERTIES_SERVICE, HardwarePropertiesManager.class, new CachedServiceFetcher<HardwarePropertiesManager>() {
            public HardwarePropertiesManager createService(ContextImpl ctx) {
                IHardwarePropertiesManager service = IHardwarePropertiesManager.Stub.asInterface(ServiceManager.getService(Context.HARDWARE_PROPERTIES_SERVICE));
                if (service != null) {
                    return new HardwarePropertiesManager(ctx, service);
                }
                Log.wtf(SystemServiceRegistry.TAG, "Failed to get hardwareproperties service.");
                return null;
            }
        });
        registerService(Context.SOUND_TRIGGER_SERVICE, SoundTriggerManager.class, new CachedServiceFetcher<SoundTriggerManager>() {
            public SoundTriggerManager createService(ContextImpl ctx) {
                return new SoundTriggerManager(ctx, ISoundTriggerService.Stub.asInterface(ServiceManager.getService(Context.SOUND_TRIGGER_SERVICE)));
            }
        });
        registerService("shortcut", ShortcutManager.class, new CachedServiceFetcher<ShortcutManager>() {
            public ShortcutManager createService(ContextImpl ctx) {
                return new ShortcutManager(ctx);
            }
        });
        registerService(Context.SYSTEM_HEALTH_SERVICE, SystemHealthManager.class, new CachedServiceFetcher<SystemHealthManager>() {
            public SystemHealthManager createService(ContextImpl ctx) {
                return new SystemHealthManager();
            }
        });
        registerService(Context.CONTEXTHUB_SERVICE, ContextHubManager.class, new CachedServiceFetcher<ContextHubManager>() {
            public ContextHubManager createService(ContextImpl ctx) {
                return new ContextHubManager(ctx.getOuterContext(), ctx.mMainThread.getHandler().getLooper());
            }
        });
    }

    private SystemServiceRegistry() {
    }

    public static Object[] createServiceCache() {
        return new Object[sServiceCacheSize];
    }

    public static Object getSystemService(ContextImpl ctx, String name) {
        ServiceFetcher<?> fetcher = (ServiceFetcher) SYSTEM_SERVICE_FETCHERS.get(name);
        if (fetcher != null) {
            return fetcher.getService(ctx);
        }
        return null;
    }

    public static String getSystemServiceName(Class<?> serviceClass) {
        return (String) SYSTEM_SERVICE_NAMES.get(serviceClass);
    }

    private static <T> void registerService(String serviceName, Class<T> serviceClass, ServiceFetcher<T> serviceFetcher) {
        SYSTEM_SERVICE_NAMES.put(serviceClass, serviceName);
        SYSTEM_SERVICE_FETCHERS.put(serviceName, serviceFetcher);
    }
}
