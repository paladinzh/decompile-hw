package com.android.systemui.statusbar;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.IBinder.DeathRecipient;
import android.os.Message;
import android.os.RemoteException;
import android.provider.Settings.Secure;
import android.util.Log;
import java.util.Arrays;

public class ServiceMonitor {
    private boolean mBound;
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String pkg = intent.getData().getSchemeSpecificPart();
            if (ServiceMonitor.this.mServiceName != null && ServiceMonitor.this.mServiceName.getPackageName().equals(pkg)) {
                ServiceMonitor.this.mHandler.sendMessage(ServiceMonitor.this.mHandler.obtainMessage(4, intent));
            }
        }
    };
    private final Callbacks mCallbacks;
    private final Context mContext;
    private final boolean mDebug;
    private final Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    ServiceMonitor.this.startService();
                    return;
                case 2:
                    ServiceMonitor.this.continueStartService();
                    return;
                case 3:
                    ServiceMonitor.this.stopService();
                    return;
                case 4:
                    ServiceMonitor.this.packageIntent((Intent) msg.obj);
                    return;
                case 5:
                    ServiceMonitor.this.checkBound();
                    return;
                case 6:
                    ServiceMonitor.this.serviceDisconnected((ComponentName) msg.obj);
                    return;
                default:
                    return;
            }
        }
    };
    private SC mServiceConnection;
    private ComponentName mServiceName;
    private final String mSettingKey;
    private final ContentObserver mSettingObserver = new ContentObserver(this.mHandler) {
        public void onChange(boolean selfChange) {
            onChange(selfChange, null);
        }

        public void onChange(boolean selfChange, Uri uri) {
            if (ServiceMonitor.this.mDebug) {
                Log.d(ServiceMonitor.this.mTag, "onChange selfChange=" + selfChange + " uri=" + uri);
            }
            ComponentName cn = ServiceMonitor.this.getComponentNameFromSetting();
            if (!(cn == null && ServiceMonitor.this.mServiceName == null) && (cn == null || !cn.equals(ServiceMonitor.this.mServiceName))) {
                if (ServiceMonitor.this.mBound) {
                    ServiceMonitor.this.mHandler.sendEmptyMessage(3);
                }
                ServiceMonitor.this.mHandler.sendEmptyMessageDelayed(1, 500);
                return;
            }
            if (ServiceMonitor.this.mDebug) {
                Log.d(ServiceMonitor.this.mTag, "skipping no-op restart");
            }
        }
    };
    private final String mTag;

    public interface Callbacks {
        void onNoService();

        long onServiceStartAttempt();
    }

    private final class SC implements ServiceConnection, DeathRecipient {
        private ComponentName mName;
        private IBinder mService;

        private SC() {
        }

        public void onServiceConnected(ComponentName name, IBinder service) {
            if (ServiceMonitor.this.mDebug) {
                Log.d(ServiceMonitor.this.mTag, "onServiceConnected name=" + name + " service=" + service);
            }
            this.mName = name;
            this.mService = service;
            try {
                service.linkToDeath(this, 0);
            } catch (RemoteException e) {
                Log.w(ServiceMonitor.this.mTag, "Error linking to death", e);
            }
        }

        public void onServiceDisconnected(ComponentName name) {
            if (ServiceMonitor.this.mDebug) {
                Log.d(ServiceMonitor.this.mTag, "onServiceDisconnected name=" + name);
            }
            boolean unlinked = this.mService.unlinkToDeath(this, 0);
            if (ServiceMonitor.this.mDebug) {
                Log.d(ServiceMonitor.this.mTag, "  unlinked=" + unlinked);
            }
            ServiceMonitor.this.mHandler.sendMessage(ServiceMonitor.this.mHandler.obtainMessage(6, this.mName));
        }

        public void binderDied() {
            if (ServiceMonitor.this.mDebug) {
                Log.d(ServiceMonitor.this.mTag, "binderDied");
            }
            ServiceMonitor.this.mHandler.sendMessage(ServiceMonitor.this.mHandler.obtainMessage(6, this.mName));
        }
    }

    public ServiceMonitor(String ownerTag, boolean debug, Context context, String settingKey, Callbacks callbacks) {
        this.mTag = ownerTag + ".ServiceMonitor";
        this.mDebug = debug;
        this.mContext = context;
        this.mSettingKey = settingKey;
        this.mCallbacks = callbacks;
    }

    public void start() {
        this.mContext.getContentResolver().registerContentObserver(Secure.getUriFor(this.mSettingKey), false, this.mSettingObserver, -1);
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.PACKAGE_ADDED");
        filter.addAction("android.intent.action.PACKAGE_CHANGED");
        filter.addAction("android.intent.action.PACKAGE_REMOVED");
        filter.addDataScheme("package");
        this.mContext.registerReceiver(this.mBroadcastReceiver, filter);
        this.mHandler.sendEmptyMessage(1);
    }

    private ComponentName getComponentNameFromSetting() {
        String cn = Secure.getStringForUser(this.mContext.getContentResolver(), this.mSettingKey, -2);
        if (cn == null) {
            return null;
        }
        return ComponentName.unflattenFromString(cn);
    }

    private void packageIntent(Intent intent) {
        if (this.mDebug) {
            Log.d(this.mTag, "packageIntent intent=" + intent + " extras=" + bundleToString(intent.getExtras()));
        }
        if ("android.intent.action.PACKAGE_ADDED".equals(intent.getAction())) {
            this.mHandler.sendEmptyMessage(1);
        } else if ("android.intent.action.PACKAGE_CHANGED".equals(intent.getAction()) || "android.intent.action.PACKAGE_REMOVED".equals(intent.getAction())) {
            PackageManager pm = this.mContext.getPackageManager();
            boolean serviceEnabled = (!isPackageAvailable() || pm.getApplicationEnabledSetting(this.mServiceName.getPackageName()) == 2) ? false : pm.getComponentEnabledSetting(this.mServiceName) != 2;
            if (this.mBound && !serviceEnabled) {
                stopService();
                scheduleCheckBound();
            } else if (!this.mBound && serviceEnabled) {
                startService();
            }
        }
    }

    private void stopService() {
        if (this.mDebug) {
            Log.d(this.mTag, "stopService");
        }
        boolean stopped = this.mContext.stopService(new Intent().setComponent(this.mServiceName));
        if (this.mDebug) {
            Log.d(this.mTag, "  stopped=" + stopped);
        }
        this.mContext.unbindService(this.mServiceConnection);
        this.mBound = false;
    }

    private void startService() {
        this.mServiceName = getComponentNameFromSetting();
        if (this.mDebug) {
            Log.d(this.mTag, "startService mServiceName=" + this.mServiceName);
        }
        if (this.mServiceName == null) {
            this.mBound = false;
            this.mCallbacks.onNoService();
            return;
        }
        this.mHandler.sendEmptyMessageDelayed(2, this.mCallbacks.onServiceStartAttempt());
    }

    private void continueStartService() {
        if (this.mDebug) {
            Log.d(this.mTag, "continueStartService");
        }
        Intent intent = new Intent().setComponent(this.mServiceName);
        try {
            this.mServiceConnection = new SC();
            this.mBound = this.mContext.bindService(intent, this.mServiceConnection, 1);
            if (this.mDebug) {
                Log.d(this.mTag, "mBound: " + this.mBound);
            }
        } catch (Throwable t) {
            Log.w(this.mTag, "Error binding to service: " + this.mServiceName, t);
        }
        if (!this.mBound) {
            this.mCallbacks.onNoService();
        }
    }

    private void serviceDisconnected(ComponentName serviceName) {
        if (this.mDebug) {
            Log.d(this.mTag, "serviceDisconnected serviceName=" + serviceName + " mServiceName=" + this.mServiceName);
        }
        if (serviceName.equals(this.mServiceName)) {
            this.mBound = false;
            scheduleCheckBound();
        }
    }

    private void checkBound() {
        if (this.mDebug) {
            Log.d(this.mTag, "checkBound mBound=" + this.mBound);
        }
        if (!this.mBound) {
            startService();
        }
    }

    private void scheduleCheckBound() {
        this.mHandler.removeMessages(5);
        this.mHandler.sendEmptyMessageDelayed(5, 2000);
    }

    private static String bundleToString(Bundle bundle) {
        if (bundle == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder(123);
        for (String key : bundle.keySet()) {
            if (sb.length() > 1) {
                sb.append(',');
            }
            Object v = bundle.get(key);
            if (v instanceof String[]) {
                v = Arrays.asList((String[]) v);
            }
            sb.append(key).append('=').append(v);
        }
        return sb.append('}').toString();
    }

    public ComponentName getComponent() {
        return getComponentNameFromSetting();
    }

    public void setComponent(ComponentName component) {
        Secure.putStringForUser(this.mContext.getContentResolver(), this.mSettingKey, component == null ? null : component.flattenToShortString(), -2);
    }

    public boolean isPackageAvailable() {
        ComponentName component = getComponent();
        if (component == null) {
            return false;
        }
        try {
            return this.mContext.getPackageManager().isPackageAvailable(component.getPackageName());
        } catch (RuntimeException e) {
            Log.w(this.mTag, "Error checking package availability", e);
            return false;
        }
    }
}
