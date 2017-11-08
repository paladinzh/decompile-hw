package com.huawei.thermal;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.ServiceManager;
import android.util.Log;
import com.huawei.thermal.adapter.NativeAdapter;
import com.huawei.thermal.adapter.ThermalService;
import com.huawei.thermal.eventhub.MsgReceiver;
import com.huawei.thermal.eventhub.SceneReceiver;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;

public class ThermalCoreService implements TContext {
    private static HandlerThread mHandlerThread;
    private static ThermalCoreService sInstance = null;
    private Context mContext;
    private int mCrashCount = 0;
    private MainHandler mHandler;
    private LowBatteryManager mLowBattery;
    private MsgReceiver mMsg;
    private SceneReceiver mScene;
    private ThermalStateManager mThermal;
    private ThermalService mThermalService;
    private Watchdog mWatchdog;

    final class MainHandler extends Handler {
        public MainHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 100:
                    ThermalCoreService.this.mWatchdog.start();
                    return;
                case 101:
                    Log.w("HwThermalCoreService", "thermal: shutdown");
                    Intent shutdown = new Intent("android.intent.action.ACTION_REQUEST_SHUTDOWN");
                    shutdown.putExtra("android.intent.extra.KEY_CONFIRM", false);
                    shutdown.setFlags(268435456);
                    ThermalCoreService.this.mContext.startActivity(shutdown);
                    return;
                default:
                    return;
            }
        }
    }

    public static ThermalCoreService getInstance(Context context) {
        ThermalCoreService thermalCoreService;
        synchronized (ThermalCoreService.class) {
            if (sInstance == null) {
                sInstance = new ThermalCoreService(context);
            }
            thermalCoreService = sInstance;
        }
        return thermalCoreService;
    }

    private ThermalCoreService(Context context) {
        this.mContext = context;
    }

    private void startThermalCoreServices(Context context) {
        boolean z = false;
        Log.i("HwThermalCoreService", "start thermal core services");
        if (this.mCrashCount > 0) {
            z = true;
        }
        this.mThermal = new ThermalStateManager(this, z);
        this.mLowBattery = new LowBatteryManager(this);
        InputManager input = new InputManager(mHandlerThread.getLooper());
        input.registerListener(this.mThermal);
        input.registerListener(this.mLowBattery);
        this.mThermalService = new ThermalService(this, input);
        ServiceManager.addService("hwthermal", this.mThermalService);
        this.mMsg = new MsgReceiver(context, input);
        this.mScene = new SceneReceiver(context, input);
        this.mMsg.start();
        this.mThermal.start();
        this.mScene.start();
        this.mWatchdog = Watchdog.getInstance();
        this.mWatchdog.init(this.mHandler);
        this.mWatchdog.addThread(this.mHandler);
        this.mHandler.sendEmptyMessageDelayed(100, 30000);
        Log.i("HwThermalCoreService", "finish thermal core services");
    }

    public void onStart() {
        mHandlerThread = new HandlerThread("thermal-core-thread");
        mHandlerThread.start();
        this.mHandler = new MainHandler(mHandlerThread.getLooper());
        this.mHandler.post(new Runnable() {
            public void run() {
                ThermalCoreService.this.startThermalCoreServices(ThermalCoreService.this.mContext);
            }
        });
    }

    public boolean registerPGActions(ArrayList<Integer> scenes) {
        return this.mScene.registerPGActions(scenes);
    }

    public String getThermalInterface(String action) {
        return this.mThermal.getThermalInterface(action);
    }

    public void shutdownPhone(int delay) {
        this.mHandler.sendEmptyMessageDelayed(101, (long) delay);
    }

    public Context getContext() {
        return this.mContext;
    }

    public boolean isHisiPlatform() {
        if (NativeAdapter.getPlatformType() == 3 || NativeAdapter.getPlatformType() == 2) {
            return true;
        }
        return false;
    }

    public boolean isQcommPlatform() {
        return NativeAdapter.getPlatformType() == 0;
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        this.mThermal.dump(pw, args);
    }
}
