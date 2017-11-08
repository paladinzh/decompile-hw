package com.android.systemui.statusbar.policy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.util.Log;
import com.android.systemui.statusbar.policy.BatteryController.BatteryStateChangeCallback;
import com.android.systemui.utils.HwLog;
import com.android.systemui.utils.Proguard;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;

public class BatteryControllerImpl extends BroadcastReceiver implements BatteryController {
    private static final boolean DEBUG = Log.isLoggable("BatteryController", 3);
    private final ArrayList<BatteryStateChangeCallback> mChangeCallbacks = new ArrayList();
    protected boolean mCharged;
    protected boolean mCharging;
    private final Context mContext;
    private boolean mDemoMode;
    private final Handler mHandler;
    protected int mLevel;
    protected boolean mPluggedIn;
    private final PowerManager mPowerManager;
    protected boolean mPowerSave;
    private boolean mTestmode = false;

    public BatteryControllerImpl(Context context) {
        this.mContext = context;
        this.mHandler = new Handler();
        this.mPowerManager = (PowerManager) context.getSystemService("power");
        registerReceiver();
        updatePowerSave();
    }

    private void registerReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.BATTERY_CHANGED");
        filter.addAction("android.os.action.POWER_SAVE_MODE_CHANGED");
        filter.addAction("android.os.action.POWER_SAVE_MODE_CHANGING");
        filter.addAction("com.android.systemui.BATTERY_LEVEL_TEST");
        this.mContext.registerReceiver(this, filter);
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("BatteryController state:");
        pw.print("  mLevel=");
        pw.println(this.mLevel);
        pw.print("  mPluggedIn=");
        pw.println(this.mPluggedIn);
        pw.print("  mCharging=");
        pw.println(this.mCharging);
        pw.print("  mCharged=");
        pw.println(this.mCharged);
        pw.print("  mPowerSave=");
        pw.println(this.mPowerSave);
    }

    public void setPowerSaveMode(boolean powerSave) {
        this.mPowerManager.setPowerSaveMode(powerSave);
    }

    public void addStateChangedCallback(BatteryStateChangeCallback cb) {
        this.mChangeCallbacks.add(cb);
        cb.onBatteryLevelChanged(this.mLevel, this.mPluggedIn, this.mCharging);
        cb.onPowerSaveChanged(this.mPowerSave);
    }

    public void removeStateChangedCallback(BatteryStateChangeCallback cb) {
        this.mChangeCallbacks.remove(cb);
    }

    public void onReceive(final Context context, Intent intent) {
        boolean z = true;
        String action = intent.getAction();
        HwLog.i("BatteryController", " onReceive:" + Proguard.get(intent));
        if (action.equals("android.intent.action.BATTERY_CHANGED")) {
            if (!this.mTestmode || intent.getBooleanExtra("testmode", false)) {
                boolean z2;
                this.mLevel = (int) ((((float) intent.getIntExtra("level", 0)) * 100.0f) / ((float) intent.getIntExtra("scale", 100)));
                if (intent.getIntExtra("plugged", 0) != 0) {
                    z2 = true;
                } else {
                    z2 = false;
                }
                this.mPluggedIn = z2;
                int status = intent.getIntExtra("status", 1);
                if (status == 5) {
                    z2 = true;
                } else {
                    z2 = false;
                }
                this.mCharged = z2;
                if (!(this.mCharged || status == 2)) {
                    z = false;
                }
                this.mCharging = z;
                fireBatteryLevelChanged();
            }
        } else if (action.equals("android.os.action.POWER_SAVE_MODE_CHANGED")) {
            updatePowerSave();
        } else if (action.equals("android.os.action.POWER_SAVE_MODE_CHANGING")) {
            setPowerSave(intent.getBooleanExtra("mode", false));
        } else if (action.equals("com.android.systemui.BATTERY_LEVEL_TEST")) {
            this.mTestmode = true;
            this.mHandler.post(new Runnable() {
                int curLevel = 0;
                Intent dummy = new Intent("android.intent.action.BATTERY_CHANGED");
                int incr = 1;
                int saveLevel = BatteryControllerImpl.this.mLevel;
                boolean savePlugged = BatteryControllerImpl.this.mPluggedIn;

                public void run() {
                    int i = 0;
                    if (this.curLevel < 0) {
                        BatteryControllerImpl.this.mTestmode = false;
                        this.dummy.putExtra("level", this.saveLevel);
                        this.dummy.putExtra("plugged", this.savePlugged);
                        this.dummy.putExtra("testmode", false);
                    } else {
                        this.dummy.putExtra("level", this.curLevel);
                        Intent intent = this.dummy;
                        String str = "plugged";
                        if (this.incr > 0) {
                            i = 1;
                        }
                        intent.putExtra(str, i);
                        this.dummy.putExtra("testmode", true);
                    }
                    context.sendBroadcast(this.dummy);
                    if (BatteryControllerImpl.this.mTestmode) {
                        this.curLevel += this.incr;
                        if (this.curLevel == 100) {
                            this.incr *= -1;
                        }
                        BatteryControllerImpl.this.mHandler.postDelayed(this, 200);
                    }
                }
            });
        }
    }

    public boolean isPowerSave() {
        return this.mPowerSave;
    }

    private void updatePowerSave() {
        setPowerSave(this.mPowerManager.isPowerSaveMode());
    }

    private void setPowerSave(boolean powerSave) {
        if (powerSave != this.mPowerSave) {
            this.mPowerSave = powerSave;
            if (DEBUG) {
                Log.d("BatteryController", "Power save is " + (this.mPowerSave ? "on" : "off"));
            }
            firePowerSaveChanged();
        }
    }

    protected void fireBatteryLevelChanged() {
        int N = this.mChangeCallbacks.size();
        int i = 0;
        while (i < N) {
            try {
                ((BatteryStateChangeCallback) this.mChangeCallbacks.get(i)).onBatteryLevelChanged(this.mLevel, this.mPluggedIn, this.mCharging);
                i++;
            } catch (IndexOutOfBoundsException e) {
                HwLog.e("BatteryController", "fireBatteryLevelChanged IndexOutOfBoundsException " + i);
                return;
            }
        }
    }

    private void firePowerSaveChanged() {
        int N = this.mChangeCallbacks.size();
        int i = 0;
        while (i < N) {
            try {
                ((BatteryStateChangeCallback) this.mChangeCallbacks.get(i)).onPowerSaveChanged(this.mPowerSave);
                i++;
            } catch (IndexOutOfBoundsException e) {
                return;
            }
        }
    }

    public void dispatchDemoCommand(String command, Bundle args) {
        if (!this.mDemoMode && command.equals("enter")) {
            this.mDemoMode = true;
            this.mContext.unregisterReceiver(this);
        } else if (this.mDemoMode && command.equals("exit")) {
            this.mDemoMode = false;
            registerReceiver();
            updatePowerSave();
        } else if (this.mDemoMode && command.equals("battery")) {
            String level = args.getString("level");
            String plugged = args.getString("plugged");
            if (level != null) {
                this.mLevel = Math.min(Math.max(Integer.parseInt(level), 0), 100);
            }
            if (plugged != null) {
                this.mPluggedIn = Boolean.parseBoolean(plugged);
            }
            fireBatteryLevelChanged();
        }
    }
}
