package com.android.settings.nfc;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.nfc.NfcAdapter;
import android.nfc.cardemulation.ApduServiceInfo;
import android.nfc.cardemulation.CardEmulation;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings.Secure;
import android.provider.Settings.SettingNotFoundException;
import com.android.internal.content.PackageMonitor;
import com.android.settings.MLog;
import java.util.ArrayList;
import java.util.List;

public class PaymentBackend {
    private static PaymentBackend sInstance = null;
    private final NfcAdapter mAdapter;
    private ArrayList<PaymentAppInfo> mAppInfos;
    private ArrayList<Callback> mCallbacks = new ArrayList();
    private CardEmulation mCardEmuManager;
    private final Context mContext;
    private PaymentAppInfo mDefaultAppInfo;
    private final Handler mHandler = new Handler() {
        public void dispatchMessage(Message msg) {
            PaymentBackend.this.refresh();
        }
    };
    private final PackageMonitor mSettingsPackageMonitor = new SettingsPackageMonitor();

    public interface Callback {
        void onPaymentAppsChanged();
    }

    public static class PaymentAppInfo {
        Drawable banner;
        public ComponentName componentName;
        CharSequence description;
        boolean isDefault;
        boolean isOnHost;
        CharSequence label;
        public ComponentName settingsComponent;
    }

    private class SettingsPackageMonitor extends PackageMonitor {
        private SettingsPackageMonitor() {
        }

        public void onPackageAdded(String packageName, int uid) {
            PaymentBackend.this.mHandler.obtainMessage().sendToTarget();
        }

        public void onPackageAppeared(String packageName, int reason) {
            PaymentBackend.this.mHandler.obtainMessage().sendToTarget();
        }

        public void onPackageDisappeared(String packageName, int reason) {
            PaymentBackend.this.mHandler.obtainMessage().sendToTarget();
        }

        public void onPackageRemoved(String packageName, int uid) {
            PaymentBackend.this.mHandler.obtainMessage().sendToTarget();
        }
    }

    public PaymentBackend(Context context) {
        this.mContext = context;
        this.mAdapter = NfcAdapter.getDefaultAdapter(context);
        try {
            if (this.mAdapter == null) {
                this.mCardEmuManager = null;
                return;
            }
            this.mCardEmuManager = CardEmulation.getInstance(this.mAdapter);
            refresh();
        } catch (UnsupportedOperationException e) {
            MLog.e("Settings.PaymentBackend", "This device does not support card emulation");
            this.mCardEmuManager = null;
        }
    }

    public void onPause() {
        this.mSettingsPackageMonitor.unregister();
    }

    public void onResume() {
        this.mSettingsPackageMonitor.register(this.mContext, this.mContext.getMainLooper(), false);
        refresh();
    }

    public void refresh() {
        PackageManager pm = this.mContext.getPackageManager();
        Iterable serviceInfos = null;
        if (this.mCardEmuManager != null) {
            serviceInfos = this.mCardEmuManager.getServices("payment");
        }
        ArrayList<PaymentAppInfo> appInfos = new ArrayList();
        if (r7 == null) {
            makeCallbacks();
            return;
        }
        ComponentName defaultAppName = getDefaultPaymentApp();
        PaymentAppInfo foundDefaultApp = null;
        for (ApduServiceInfo service : r7) {
            PaymentAppInfo appInfo = new PaymentAppInfo();
            appInfo.label = service.loadLabel(pm);
            if (appInfo.label == null) {
                appInfo.label = service.loadAppLabel(pm);
            }
            appInfo.isDefault = service.getComponent().equals(defaultAppName);
            if (appInfo.isDefault) {
                foundDefaultApp = appInfo;
            }
            appInfo.componentName = service.getComponent();
            String settingsActivity = service.getSettingsActivityName();
            if (settingsActivity != null) {
                appInfo.settingsComponent = new ComponentName(appInfo.componentName.getPackageName(), settingsActivity);
            } else {
                appInfo.settingsComponent = null;
            }
            appInfo.description = service.getDescription();
            appInfo.banner = service.loadBanner(pm);
            appInfo.isOnHost = service.isOnHost();
            appInfos.add(appInfo);
        }
        this.mAppInfos = appInfos;
        this.mDefaultAppInfo = foundDefaultApp;
        makeCallbacks();
    }

    public void registerCallback(Callback callback) {
        this.mCallbacks.add(callback);
    }

    public List<PaymentAppInfo> getPaymentAppInfos() {
        return this.mAppInfos;
    }

    public PaymentAppInfo getDefaultApp() {
        return this.mDefaultAppInfo;
    }

    void makeCallbacks() {
        for (Callback callback : this.mCallbacks) {
            callback.onPaymentAppsChanged();
        }
    }

    boolean isForegroundMode() {
        boolean z = false;
        try {
            if (Secure.getInt(this.mContext.getContentResolver(), "nfc_payment_foreground") != 0) {
                z = true;
            }
            return z;
        } catch (SettingNotFoundException e) {
            return false;
        }
    }

    void setForegroundMode(boolean foreground) {
        Secure.putInt(this.mContext.getContentResolver(), "nfc_payment_foreground", foreground ? 1 : 0);
    }

    ComponentName getDefaultPaymentApp() {
        String componentString = Secure.getString(this.mContext.getContentResolver(), "nfc_payment_default_component");
        if (componentString != null) {
            return ComponentName.unflattenFromString(componentString);
        }
        return null;
    }

    public void setDefaultPaymentApp(ComponentName app) {
        String str = null;
        ContentResolver contentResolver = this.mContext.getContentResolver();
        String str2 = "nfc_payment_default_component";
        if (app != null) {
            str = app.flattenToString();
        }
        Secure.putString(contentResolver, str2, str);
        refresh();
    }
}
