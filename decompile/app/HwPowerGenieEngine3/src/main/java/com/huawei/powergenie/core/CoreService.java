package com.huawei.powergenie.core;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Debug;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.util.Log;
import com.huawei.powergenie.api.ICoreContext;
import com.huawei.powergenie.api.IDeviceState;
import com.huawei.powergenie.core.app.AppManager;
import com.huawei.powergenie.core.battery.BatteryMonitor;
import com.huawei.powergenie.core.contextaware.UserStateManager;
import com.huawei.powergenie.core.device.DeviceStateService;
import com.huawei.powergenie.core.modulesmanager.ModuleManager;
import com.huawei.powergenie.core.policy.PolicyService;
import com.huawei.powergenie.core.powerstats.AppPowerStatsService;
import com.huawei.powergenie.core.scenario.ScenarioService;
import com.huawei.powergenie.core.server.SdkService;
import com.huawei.powergenie.core.thermal.ThermalStateManager;
import com.huawei.powergenie.core.thermal.ThermalStateService;
import com.huawei.powergenie.debugtest.DbgTestService;
import com.huawei.powergenie.debugtest.DbgUtils;
import com.huawei.powergenie.integration.adapter.BroadcastAdapter;
import com.huawei.powergenie.integration.eventhub.EventHubFactory;

public class CoreService extends Service {
    private static final boolean mHwThermalEnable = SystemProperties.getBoolean("sys.huawei.thermal.enable", false);
    private Context mContext;
    private int mCrashCount = 0;
    private MainHandler mHandler;
    private HandlerThread mHandlerThread;
    private CoreContext mICoreContext;
    private Watchdog mWatchdog;

    final class MainHandler extends Handler {
        public MainHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 100:
                    ((DeviceStateService) CoreService.this.mICoreContext.getService("device")).allReady();
                    return;
                case 101:
                    CoreService.this.mWatchdog.start();
                    return;
                case 102:
                    CoreService.this.doGc();
                    return;
                default:
                    return;
            }
        }
    }

    private void startCoreServices(ICoreContext context) {
        Log.i("CoreService", "start core services");
        long start = SystemClock.uptimeMillis();
        AppPowerStatsService aps = new AppPowerStatsService(context);
        this.mICoreContext.registerService("powerstats", aps);
        aps.start();
        PolicyService policy = new PolicyService(context, this.mCrashCount);
        this.mICoreContext.registerService("policy", policy);
        policy.start();
        SdkService sdk = new SdkService(context);
        this.mICoreContext.registerService("sdk", sdk);
        AppManager app = new AppManager(context);
        this.mICoreContext.registerService("appmamager", app);
        DeviceStateService device = new DeviceStateService(context, this.mCrashCount);
        this.mICoreContext.registerService("device", device);
        ScenarioService scenario = new ScenarioService(context);
        this.mICoreContext.registerService("scenario", scenario);
        ThermalStateManager thermal = null;
        ThermalStateService thermalStateService = null;
        if (mHwThermalEnable) {
            ThermalStateService thermalStateService2 = new ThermalStateService(context);
            this.mICoreContext.registerService("thermalstate", thermalStateService2);
        } else {
            ThermalStateManager thermalStateManager = new ThermalStateManager(context, policy, device);
            this.mICoreContext.registerService("thermal", thermalStateManager);
        }
        KStateManager kstate = new KStateManager(context);
        this.mICoreContext.registerService("kstate", kstate);
        kstate.start();
        ModuleManager module = new ModuleManager(context);
        this.mICoreContext.registerService("module", module);
        UserStateManager userStateManager = new UserStateManager(context);
        this.mICoreContext.registerService("ca", userStateManager);
        device.start();
        app.start();
        scenario.start();
        if (mHwThermalEnable) {
            thermalStateService.start();
        } else {
            thermal.start();
        }
        module.start();
        BatteryMonitor battery = new BatteryMonitor(context);
        InputManager input = new InputManager(this.mHandlerThread.getLooper());
        input.registerListener(scenario);
        input.registerListener(device);
        input.registerListener(app);
        if (mHwThermalEnable) {
            input.registerListener(thermalStateService);
        } else {
            input.registerListener(thermal);
        }
        input.registerListener(userStateManager);
        input.registerListener(policy);
        input.registerListener(battery);
        input.registerListener(aps);
        EventHubFactory.startAllEventHubs(this.mContext, input);
        sdk.start();
        this.mHandler.sendEmptyMessageDelayed(100, 1000);
        Log.i("CoreService", "Init PG Watchdog");
        this.mWatchdog = Watchdog.getInstance();
        this.mWatchdog.init(this.mHandler);
        this.mWatchdog.addThread(this.mHandler);
        this.mHandler.sendEmptyMessageDelayed(101, 30000);
        Log.i("CoreService", "finish core services, expend: " + (SystemClock.uptimeMillis() - start) + "ms");
    }

    private void startDbgTestService(ICoreContext context) {
        if (DbgUtils.TEST) {
            DbgTestService dts = new DbgTestService(context);
            this.mICoreContext.registerService("dbgtest", dts);
            dts.start();
            Log.i("CoreService", "start dbg test service");
        }
    }

    public void onCreate() {
        Log.i("CoreService", "onCreate");
        this.mContext = this;
        this.mICoreContext = new CoreContext(this.mContext);
        DbgUtils.init(this.mContext);
        this.mHandlerThread = new HandlerThread("core");
        this.mHandlerThread.start();
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        this.mCrashCount = startId / 2;
        Log.i("CoreService", "startid: " + startId + " crashCount: " + this.mCrashCount);
        if (this.mHandler == null) {
            this.mHandler = new MainHandler(this.mHandlerThread.getLooper());
            this.mHandler.post(new Runnable() {
                public void run() {
                    CoreService.this.startCoreServices(CoreService.this.mICoreContext);
                    CoreService.this.startDbgTestService(CoreService.this.mICoreContext);
                    BroadcastAdapter.startIAware(CoreService.this.mContext);
                    if (CoreService.mHwThermalEnable) {
                        BroadcastAdapter.startThermal(CoreService.this.mContext);
                    }
                }
            });
            this.mHandler.sendEmptyMessageDelayed(102, 120000);
        }
        return 1;
    }

    private void doGc() {
        long pss = Debug.getPss();
        IDeviceState deviceState = (IDeviceState) this.mICoreContext.getService("device");
        Log.i("CoreService", "CoreService pss : " + pss + ", charging:" + deviceState.isCharging());
        if (pss > 15000 && deviceState.isCharging()) {
            Runtime.getRuntime().gc();
        }
    }

    public IBinder onBind(Intent arg0) {
        Log.e("CoreService", "onBind not implemented.");
        return null;
    }

    public void onDestroy() {
        Log.i("CoreService", "onDestroy");
    }
}
