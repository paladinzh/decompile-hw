package com.android.server.power;

import android.app.ActivityManagerNative;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.IActivityManager;
import android.app.ProgressDialog;
import android.bluetooth.IBluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioAttributes;
import android.media.AudioAttributes.Builder;
import android.nfc.INfcAdapter;
import android.os.FileUtils;
import android.os.Handler;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.RecoverySystem;
import android.os.RecoverySystem.ProgressListener;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.SystemVibrator;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.storage.IMountService;
import android.os.storage.IMountShutdownObserver;
import android.os.storage.IMountShutdownObserver.Stub;
import android.util.Log;
import com.android.internal.os.HwBootAnimationOeminfo;
import com.android.internal.telephony.ITelephony;
import com.android.server.HwServiceFactory;
import com.android.server.am.HwBroadcastRadarUtil;
import com.android.server.job.controllers.JobStatus;
import com.android.server.pm.PackageManagerService;
import java.io.File;
import java.io.IOException;

public final class ShutdownThread extends Thread {
    private static final String ACTION_ACTURAL_SHUTDOWN = "com.android.internal.app.SHUTDOWNBROADCAST";
    private static final int ACTIVITY_MANAGER_STOP_PERCENT = 4;
    public static final String AUDIT_SAFEMODE_PROPERTY = "persist.sys.audit_safemode";
    private static final int BROADCAST_STOP_PERCENT = 2;
    private static final int MAX_BROADCAST_TIME = 10000;
    private static final int MAX_RADIO_WAIT_TIME = 12000;
    private static final int MAX_SHUTDOWN_WAIT_TIME = 20000;
    private static final int MAX_UNCRYPT_WAIT_TIME = 900000;
    private static final int MOUNT_SERVICE_STOP_PERCENT = 20;
    private static final int PACKAGE_MANAGER_STOP_PERCENT = 6;
    private static final int PHONE_STATE_POLL_SLEEP_MSEC = 100;
    private static final int RADIO_STOP_PERCENT = 18;
    public static final String REBOOT_SAFEMODE_PROPERTY = "persist.sys.safemode";
    public static final String RO_SAFEMODE_PROPERTY = "ro.sys.safemode";
    public static final String SHUTDOWN_ACTION_PROPERTY = "sys.shutdown.requested";
    private static final int SHUTDOWN_VIBRATE_MS = 500;
    private static final String TAG = "ShutdownThread";
    private static final AudioAttributes VIBRATION_ATTRIBUTES = new Builder().setContentType(4).setUsage(13).build();
    private static IHwShutdownThread iHwShutdownThread = HwServiceFactory.getHwShutdownThread();
    private static String mReason;
    private static boolean mReboot;
    private static boolean mRebootHasProgressBar;
    private static boolean mRebootSafeMode;
    private static AlertDialog sConfirmDialog;
    private static final ShutdownThread sInstance = new ShutdownThread();
    private static boolean sIsStarted = false;
    private static Object sIsStartedGuard = new Object();
    private boolean mActionDone;
    private final Object mActionDoneSync = new Object();
    private Context mContext;
    private WakeLock mCpuWakeLock;
    private Handler mHandler;
    private PowerManager mPowerManager;
    private ProgressDialog mProgressDialog;
    private WakeLock mScreenWakeLock;

    public static class CloseDialogReceiver extends BroadcastReceiver implements OnDismissListener {
        public Dialog dialog;
        private Context mContext;

        public CloseDialogReceiver(Context context) {
            this.mContext = context;
            context.registerReceiver(this, new IntentFilter("android.intent.action.CLOSE_SYSTEM_DIALOGS"));
        }

        public void onReceive(Context context, Intent intent) {
            if (this.dialog != null) {
                this.dialog.cancel();
            }
        }

        public void onDismiss(DialogInterface unused) {
            this.mContext.unregisterReceiver(this);
        }
    }

    private ShutdownThread() {
    }

    public static void shutdown(Context context, String reason, boolean confirm) {
        mReboot = false;
        mRebootSafeMode = false;
        mReason = reason;
        iHwShutdownThread.resetValues();
        shutdownInner(context, confirm);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    static void shutdownInner(final Context context, boolean confirm) {
        synchronized (sIsStartedGuard) {
            if (sIsStarted) {
                Log.d(TAG, "Request to shutdown already running, returning.");
            }
        }
    }

    public static void reboot(Context context, String reason, boolean confirm) {
        mReboot = true;
        mRebootSafeMode = false;
        mRebootHasProgressBar = false;
        mReason = reason;
        shutdownInner(context, confirm);
    }

    public static void rebootSafeMode(Context context, boolean confirm) {
        if (!((UserManager) context.getSystemService("user")).hasUserRestriction("no_safe_boot")) {
            mReboot = true;
            mRebootSafeMode = true;
            mRebootHasProgressBar = false;
            mReason = null;
            shutdownInner(context, confirm);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static void beginShutdownSequence(Context context) {
        context.sendBroadcast(new Intent(ACTION_ACTURAL_SHUTDOWN));
        synchronized (sIsStartedGuard) {
            if (sIsStarted) {
                Log.d(TAG, "Shutdown sequence already running, returning.");
                return;
            }
            sIsStarted = true;
        }
        sInstance.mHandler = new Handler() {
        };
        sInstance.start();
        sInstance.mScreenWakeLock = null;
        if (sInstance.mPowerManager.isScreenOn()) {
            try {
                sInstance.mScreenWakeLock = sInstance.mPowerManager.newWakeLock(26, "ShutdownThread-screen");
                sInstance.mScreenWakeLock.setReferenceCounted(false);
                sInstance.mScreenWakeLock.acquire();
            } catch (SecurityException e) {
                Log.w(TAG, "No permission to acquire wake lock", e);
                sInstance.mScreenWakeLock = null;
            }
        }
        sInstance.mHandler = /* anonymous class already generated */;
        sInstance.start();
    }

    void actionDone() {
        synchronized (this.mActionDoneSync) {
            this.mActionDone = true;
            this.mActionDoneSync.notifyAll();
        }
    }

    public void run() {
        String str;
        BroadcastReceiver br = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                ShutdownThread.this.actionDone();
            }
        };
        AlarmManager alarmManager = (AlarmManager) sInstance.mContext.getSystemService("alarm");
        if (alarmManager != null) {
            Log.i(TAG, "shutdownThread setHwairPlaneStateProp");
            alarmManager.setHwAirPlaneStateProp();
        }
        StringBuilder append = new StringBuilder().append(mReboot ? "1" : "0");
        if (mReason != null) {
            str = mReason;
        } else {
            str = "";
        }
        SystemProperties.set(SHUTDOWN_ACTION_PROPERTY, append.append(str).toString());
        if (mRebootSafeMode) {
            SystemProperties.set(REBOOT_SAFEMODE_PROPERTY, "1");
        }
        Log.i(TAG, "Sending shutdown broadcast...");
        long shutDownBegin = SystemClock.elapsedRealtime();
        this.mActionDone = false;
        Intent intent = new Intent("android.intent.action.ACTION_SHUTDOWN");
        intent.addFlags(268435456);
        this.mContext.sendOrderedBroadcastAsUser(intent, UserHandle.ALL, null, br, this.mHandler, 0, null, null);
        long endTime = SystemClock.elapsedRealtime() + JobStatus.DEFAULT_TRIGGER_UPDATE_DELAY;
        synchronized (this.mActionDoneSync) {
            while (!this.mActionDone) {
                long delay = endTime - SystemClock.elapsedRealtime();
                if (delay <= 0) {
                    Log.w(TAG, "Shutdown broadcast timed out");
                    break;
                }
                if (mRebootHasProgressBar) {
                    sInstance.setRebootProgress((int) (((((double) (JobStatus.DEFAULT_TRIGGER_UPDATE_DELAY - delay)) * 1.0d) * 2.0d) / 10000.0d), null);
                }
                try {
                    this.mActionDoneSync.wait(Math.min(delay, 100));
                } catch (InterruptedException e) {
                }
            }
        }
        if (mRebootHasProgressBar) {
            sInstance.setRebootProgress(2, null);
        }
        Log.i(TAG, "Shutting down activity manager...");
        IActivityManager am = ActivityManagerNative.asInterface(ServiceManager.checkService("activity"));
        if (am != null) {
            try {
                am.shutdown(10000);
            } catch (RemoteException e2) {
            }
        }
        if (mRebootHasProgressBar) {
            sInstance.setRebootProgress(4, null);
        }
        Log.i(TAG, "Shutting down package manager...");
        PackageManagerService pm = (PackageManagerService) ServiceManager.getService(HwBroadcastRadarUtil.KEY_PACKAGE);
        if (pm != null) {
            pm.shutdown();
        }
        if (mRebootHasProgressBar) {
            sInstance.setRebootProgress(6, null);
        }
        shutdownRadios(MAX_RADIO_WAIT_TIME);
        if (mRebootHasProgressBar) {
            sInstance.setRebootProgress(18, null);
        }
        IMountShutdownObserver anonymousClass4 = new Stub() {
            public void onShutDownComplete(int statusCode) throws RemoteException {
                Log.w(ShutdownThread.TAG, "Result code " + statusCode + " from MountService.shutdown");
                ShutdownThread.this.actionDone();
            }
        };
        Log.i(TAG, "Shutting down MountService");
        this.mActionDone = false;
        long endShutTime = SystemClock.elapsedRealtime() + 20000;
        synchronized (this.mActionDoneSync) {
            try {
                IMountService mount = IMountService.Stub.asInterface(ServiceManager.checkService("mount"));
                if (mount != null) {
                    mount.shutdown(anonymousClass4);
                } else {
                    Log.w(TAG, "MountService unavailable for shutdown");
                }
            } catch (Throwable e3) {
                Log.e(TAG, "Exception during MountService shutdown", e3);
            }
            while (!this.mActionDone) {
                delay = endShutTime - SystemClock.elapsedRealtime();
                if (delay <= 0) {
                    Log.w(TAG, "Shutdown wait timed out");
                    break;
                }
                if (mRebootHasProgressBar) {
                    sInstance.setRebootProgress(((int) (((((double) (20000 - delay)) * 1.0d) * 2.0d) / 20000.0d)) + 18, null);
                }
                try {
                    this.mActionDoneSync.wait(Math.min(delay, 100));
                } catch (InterruptedException e4) {
                }
            }
        }
        if (mRebootHasProgressBar) {
            sInstance.setRebootProgress(20, null);
            uncrypt();
        }
        rebootOrShutdown(this.mContext, mReboot, mReason, shutDownBegin);
    }

    private void setRebootProgress(final int progress, final CharSequence message) {
        this.mHandler.post(new Runnable() {
            public void run() {
                if (ShutdownThread.this.mProgressDialog != null) {
                    ShutdownThread.this.mProgressDialog.setProgress(progress);
                    if (message != null) {
                        ShutdownThread.this.mProgressDialog.setMessage(message);
                    }
                }
            }
        });
    }

    private void shutdownRadios(int timeout) {
        final long endTime = SystemClock.elapsedRealtime() + ((long) timeout);
        final boolean[] done = new boolean[1];
        final int i = timeout;
        Thread t = new Thread() {
            public void run() {
                boolean nfcOff;
                boolean bluetoothOff;
                boolean radioOff;
                long delay;
                INfcAdapter nfc = INfcAdapter.Stub.asInterface(ServiceManager.checkService("nfc"));
                ITelephony phone = ITelephony.Stub.asInterface(ServiceManager.checkService("phone"));
                IBluetoothManager bluetooth = IBluetoothManager.Stub.asInterface(ServiceManager.checkService("bluetooth_manager"));
                if (nfc != null) {
                    try {
                        nfcOff = nfc.getState() == 1;
                    } catch (RemoteException ex) {
                        Log.e(ShutdownThread.TAG, "RemoteException during NFC shutdown", ex);
                        nfcOff = true;
                    }
                } else {
                    nfcOff = true;
                }
                if (!nfcOff) {
                    Log.w(ShutdownThread.TAG, "Turning off NFC...");
                    nfc.disable(false);
                }
                if (bluetooth != null) {
                    try {
                        if (bluetooth.isEnabled()) {
                            bluetoothOff = false;
                            if (!bluetoothOff) {
                                Log.w(ShutdownThread.TAG, "Disabling Bluetooth...");
                                bluetooth.disable(false);
                            }
                            if (phone != null) {
                                try {
                                    if (phone.needMobileRadioShutdown()) {
                                        radioOff = false;
                                        if (!radioOff) {
                                            Log.w(ShutdownThread.TAG, "Turning off cellular radios...");
                                            phone.shutdownMobileRadios();
                                        }
                                        Log.i(ShutdownThread.TAG, "Waiting for NFC, Bluetooth and Radio...");
                                        delay = endTime - SystemClock.elapsedRealtime();
                                        while (delay > 0) {
                                            if (ShutdownThread.mRebootHasProgressBar) {
                                                ShutdownThread.sInstance.setRebootProgress(((int) (((((double) (((long) i) - delay)) * 1.0d) * 12.0d) / ((double) i))) + 6, null);
                                            }
                                            if (!bluetoothOff) {
                                                try {
                                                    bluetoothOff = bluetooth.isEnabled();
                                                } catch (RemoteException ex2) {
                                                    Log.e(ShutdownThread.TAG, "RemoteException during bluetooth shutdown", ex2);
                                                    bluetoothOff = true;
                                                }
                                                if (bluetoothOff) {
                                                    Log.i(ShutdownThread.TAG, "Bluetooth turned off.");
                                                }
                                            }
                                            if (!radioOff) {
                                                try {
                                                    radioOff = phone.needMobileRadioShutdown();
                                                } catch (RemoteException ex22) {
                                                    Log.e(ShutdownThread.TAG, "RemoteException during radio shutdown", ex22);
                                                    radioOff = true;
                                                }
                                                if (radioOff) {
                                                    Log.i(ShutdownThread.TAG, "Radio turned off.");
                                                }
                                            }
                                            if (!nfcOff) {
                                                try {
                                                    nfcOff = nfc.getState() != 1;
                                                } catch (RemoteException ex222) {
                                                    Log.e(ShutdownThread.TAG, "RemoteException during NFC shutdown", ex222);
                                                    nfcOff = true;
                                                }
                                                if (nfcOff) {
                                                    Log.i(ShutdownThread.TAG, "NFC turned off.");
                                                }
                                            }
                                            if (!radioOff && bluetoothOff && nfcOff) {
                                                Log.i(ShutdownThread.TAG, "NFC, Radio and Bluetooth shutdown complete.");
                                                done[0] = true;
                                                return;
                                            }
                                            SystemClock.sleep(100);
                                            delay = endTime - SystemClock.elapsedRealtime();
                                        }
                                    }
                                } catch (RemoteException ex2222) {
                                    Log.e(ShutdownThread.TAG, "RemoteException during radio shutdown", ex2222);
                                    radioOff = true;
                                }
                            }
                            radioOff = true;
                            if (radioOff) {
                                Log.w(ShutdownThread.TAG, "Turning off cellular radios...");
                                phone.shutdownMobileRadios();
                            }
                            Log.i(ShutdownThread.TAG, "Waiting for NFC, Bluetooth and Radio...");
                            delay = endTime - SystemClock.elapsedRealtime();
                            while (delay > 0) {
                                if (ShutdownThread.mRebootHasProgressBar) {
                                    ShutdownThread.sInstance.setRebootProgress(((int) (((((double) (((long) i) - delay)) * 1.0d) * 12.0d) / ((double) i))) + 6, null);
                                }
                                if (bluetoothOff) {
                                    if (bluetooth.isEnabled()) {
                                    }
                                    if (bluetoothOff) {
                                        Log.i(ShutdownThread.TAG, "Bluetooth turned off.");
                                    }
                                }
                                if (radioOff) {
                                    if (phone.needMobileRadioShutdown()) {
                                    }
                                    if (radioOff) {
                                        Log.i(ShutdownThread.TAG, "Radio turned off.");
                                    }
                                }
                                if (nfcOff) {
                                    if (nfc.getState() != 1) {
                                    }
                                    if (nfcOff) {
                                        Log.i(ShutdownThread.TAG, "NFC turned off.");
                                    }
                                }
                                if (!radioOff) {
                                }
                                SystemClock.sleep(100);
                                delay = endTime - SystemClock.elapsedRealtime();
                            }
                        }
                    } catch (RemoteException ex22222) {
                        Log.e(ShutdownThread.TAG, "RemoteException during bluetooth shutdown", ex22222);
                        bluetoothOff = true;
                    }
                }
                bluetoothOff = true;
                if (bluetoothOff) {
                    Log.w(ShutdownThread.TAG, "Disabling Bluetooth...");
                    bluetooth.disable(false);
                }
                if (phone != null) {
                    if (phone.needMobileRadioShutdown()) {
                        radioOff = false;
                        if (radioOff) {
                            Log.w(ShutdownThread.TAG, "Turning off cellular radios...");
                            phone.shutdownMobileRadios();
                        }
                        Log.i(ShutdownThread.TAG, "Waiting for NFC, Bluetooth and Radio...");
                        delay = endTime - SystemClock.elapsedRealtime();
                        while (delay > 0) {
                            if (ShutdownThread.mRebootHasProgressBar) {
                                ShutdownThread.sInstance.setRebootProgress(((int) (((((double) (((long) i) - delay)) * 1.0d) * 12.0d) / ((double) i))) + 6, null);
                            }
                            if (bluetoothOff) {
                                if (bluetooth.isEnabled()) {
                                }
                                if (bluetoothOff) {
                                    Log.i(ShutdownThread.TAG, "Bluetooth turned off.");
                                }
                            }
                            if (radioOff) {
                                if (phone.needMobileRadioShutdown()) {
                                }
                                if (radioOff) {
                                    Log.i(ShutdownThread.TAG, "Radio turned off.");
                                }
                            }
                            if (nfcOff) {
                                if (nfc.getState() != 1) {
                                }
                                if (nfcOff) {
                                    Log.i(ShutdownThread.TAG, "NFC turned off.");
                                }
                            }
                            if (!radioOff) {
                            }
                            SystemClock.sleep(100);
                            delay = endTime - SystemClock.elapsedRealtime();
                        }
                    }
                }
                radioOff = true;
                if (radioOff) {
                    Log.w(ShutdownThread.TAG, "Turning off cellular radios...");
                    phone.shutdownMobileRadios();
                }
                Log.i(ShutdownThread.TAG, "Waiting for NFC, Bluetooth and Radio...");
                delay = endTime - SystemClock.elapsedRealtime();
                while (delay > 0) {
                    if (ShutdownThread.mRebootHasProgressBar) {
                        ShutdownThread.sInstance.setRebootProgress(((int) (((((double) (((long) i) - delay)) * 1.0d) * 12.0d) / ((double) i))) + 6, null);
                    }
                    if (bluetoothOff) {
                        if (bluetooth.isEnabled()) {
                        }
                        if (bluetoothOff) {
                            Log.i(ShutdownThread.TAG, "Bluetooth turned off.");
                        }
                    }
                    if (radioOff) {
                        if (phone.needMobileRadioShutdown()) {
                        }
                        if (radioOff) {
                            Log.i(ShutdownThread.TAG, "Radio turned off.");
                        }
                    }
                    if (nfcOff) {
                        if (nfc.getState() != 1) {
                        }
                        if (nfcOff) {
                            Log.i(ShutdownThread.TAG, "NFC turned off.");
                        }
                    }
                    if (!radioOff) {
                    }
                    SystemClock.sleep(100);
                    delay = endTime - SystemClock.elapsedRealtime();
                }
            }
        };
        t.start();
        try {
            t.join((long) timeout);
        } catch (InterruptedException e) {
        }
        if (!done[0]) {
            Log.w(TAG, "Timed out waiting for NFC, Radio and Bluetooth shutdown.");
        }
    }

    public static void rebootOrShutdown(Context context, boolean reboot, String reason) {
        rebootOrShutdown(context, reboot, reason, -1);
    }

    private static void rebootOrShutdown(Context context, boolean reboot, String reason, long shutDownBegin) {
        deviceRebootOrShutdown(reboot, reason);
        int shutdownFlag = HwBootAnimationOeminfo.getBootAnimShutFlag();
        if (-1 == shutdownFlag) {
            Log.e(TAG, "shutdownThread: getBootAnimShutFlag error");
        }
        if (shutdownFlag == 0) {
            Log.d(TAG, "rebootOrShutdown: " + reboot);
            try {
                if (HwBootAnimationOeminfo.setBootAnimShutFlag(1) != 0) {
                    Log.e(TAG, "shutdownThread: writeBootAnimShutFlag error");
                }
            } catch (Exception ex) {
                Log.e(TAG, ex.toString());
            }
        }
        iHwShutdownThread.waitShutdownAnimationComplete(context, shutDownBegin);
        if (reboot) {
            Log.i(TAG, "Rebooting, reason: " + reason);
            PowerManagerService.lowLevelReboot(reason);
            Log.e(TAG, "Reboot failed, will attempt shutdown instead");
            reason = null;
        } else if (context != null) {
            try {
                new SystemVibrator(context).vibrate(500, VIBRATION_ATTRIBUTES);
            } catch (Exception e) {
                Log.w(TAG, "Failed to vibrate during shutdown.", e);
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e2) {
            }
        }
        Log.i(TAG, "Performing low-level shutdown...");
        PowerManagerService.lowLevelShutdown(reason);
    }

    private void uncrypt() {
        Log.i(TAG, "Calling uncrypt and monitoring the progress...");
        final ProgressListener progressListener = new ProgressListener() {
            public void onProgress(int status) {
                if (status >= 0 && status < 100) {
                    ShutdownThread.sInstance.setRebootProgress(((int) ((((double) status) * 80.0d) / 100.0d)) + 20, ShutdownThread.this.mContext.getText(17039645));
                } else if (status == 100) {
                    ShutdownThread.sInstance.setRebootProgress(status, ShutdownThread.this.mContext.getText(17039646));
                }
            }
        };
        final boolean[] done = new boolean[]{false};
        Thread t = new Thread() {
            public void run() {
                RecoverySystem rs = (RecoverySystem) ShutdownThread.this.mContext.getSystemService("recovery");
                try {
                    RecoverySystem.processPackage(ShutdownThread.this.mContext, new File(FileUtils.readTextFile(RecoverySystem.UNCRYPT_PACKAGE_FILE, 0, null)), progressListener);
                } catch (IOException e) {
                    Log.e(ShutdownThread.TAG, "Error uncrypting file", e);
                }
                done[0] = true;
            }
        };
        t.start();
        try {
            t.join(900000);
        } catch (InterruptedException e) {
        }
        if (!done[0]) {
            Log.w(TAG, "Timed out waiting for uncrypt.");
        }
    }

    private static void deviceRebootOrShutdown(boolean reboot, String reason) {
        String deviceShutdownClassName = "com.qti.server.power.ShutdownOem";
        try {
            Class<?> cl = Class.forName(deviceShutdownClassName);
            try {
                cl.getMethod("rebootOrShutdown", new Class[]{Boolean.TYPE, String.class}).invoke(cl.newInstance(), new Object[]{Boolean.valueOf(reboot), reason});
            } catch (NoSuchMethodException e) {
                Log.e(TAG, "rebootOrShutdown method not found in class " + deviceShutdownClassName);
            } catch (Exception e2) {
                Log.e(TAG, "Unknown exception hit while trying to invode rebootOrShutdown");
            }
        } catch (ClassNotFoundException e3) {
            Log.e(TAG, "Unable to find class " + deviceShutdownClassName);
        }
    }

    public static AlertDialog getsConfirmDialog() {
        return sConfirmDialog;
    }
}
