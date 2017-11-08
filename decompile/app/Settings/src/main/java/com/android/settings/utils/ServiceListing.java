package com.android.settings.utils;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.provider.Settings.Secure;
import android.util.Slog;
import com.android.settings.utils.ManagedServiceSettings.Config;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class ServiceListing {
    private final List<Callback> mCallbacks = new ArrayList();
    private final Config mConfig;
    private final ContentResolver mContentResolver;
    private final Context mContext;
    private final HashSet<ComponentName> mEnabledServices = new HashSet();
    private boolean mListening;
    private final BroadcastReceiver mPackageReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            ServiceListing.this.reload();
        }
    };
    private final List<ServiceInfo> mServices = new ArrayList();
    private final ContentObserver mSettingsObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange, Uri uri) {
            ServiceListing.this.reload();
        }
    };

    public interface Callback {
        void onServicesReloaded(List<ServiceInfo> list);
    }

    public ServiceListing(Context context, Config config) {
        this.mContext = context;
        this.mConfig = config;
        this.mContentResolver = context.getContentResolver();
    }

    public void addCallback(Callback callback) {
        this.mCallbacks.add(callback);
    }

    public void setListening(boolean listening) {
        if (this.mListening != listening) {
            this.mListening = listening;
            if (this.mListening) {
                IntentFilter filter = new IntentFilter();
                filter.addAction("android.intent.action.PACKAGE_ADDED");
                filter.addAction("android.intent.action.PACKAGE_CHANGED");
                filter.addAction("android.intent.action.PACKAGE_REMOVED");
                filter.addAction("android.intent.action.PACKAGE_REPLACED");
                filter.addDataScheme("package");
                this.mContext.registerReceiver(this.mPackageReceiver, filter);
                this.mContentResolver.registerContentObserver(Secure.getUriFor(this.mConfig.setting), false, this.mSettingsObserver);
            } else {
                this.mContext.unregisterReceiver(this.mPackageReceiver);
                this.mContentResolver.unregisterContentObserver(this.mSettingsObserver);
            }
        }
    }

    protected static int getServices(Config c, List<ServiceInfo> list, PackageManager pm) {
        int services = 0;
        if (list != null) {
            list.clear();
        }
        List<ResolveInfo> installedServices = pm.queryIntentServicesAsUser(new Intent(c.intentAction), 132, ActivityManager.getCurrentUser());
        int count = installedServices.size();
        for (int i = 0; i < count; i++) {
            ServiceInfo info = ((ResolveInfo) installedServices.get(i)).serviceInfo;
            if (c.permission.equals(info.permission)) {
                if (list != null) {
                    list.add(info);
                }
                services++;
            } else {
                Slog.w(c.tag, "Skipping " + c.noun + " service " + info.packageName + "/" + info.name + ": it does not require the permission " + c.permission);
            }
        }
        return services;
    }

    private void saveEnabledServices() {
        StringBuilder sb = null;
        for (ComponentName cn : this.mEnabledServices) {
            if (sb == null) {
                sb = new StringBuilder();
            } else {
                sb.append(':');
            }
            sb.append(cn.flattenToString());
        }
        Secure.putString(this.mContentResolver, this.mConfig.setting, sb != null ? sb.toString() : "");
    }

    private void loadEnabledServices() {
        this.mEnabledServices.clear();
        String flat = Secure.getString(this.mContentResolver, this.mConfig.setting);
        if (flat != null && !"".equals(flat)) {
            String[] names = flat.split(":");
            for (String unflattenFromString : names) {
                ComponentName cn = ComponentName.unflattenFromString(unflattenFromString);
                if (cn != null) {
                    this.mEnabledServices.add(cn);
                }
            }
        }
    }

    public List<ServiceInfo> reload() {
        loadEnabledServices();
        getServices(this.mConfig, this.mServices, this.mContext.getPackageManager());
        for (Callback callback : this.mCallbacks) {
            callback.onServicesReloaded(this.mServices);
        }
        return this.mServices;
    }

    public boolean isEnabled(ComponentName cn) {
        return this.mEnabledServices.contains(cn);
    }

    public void setEnabled(ComponentName cn, boolean enabled) {
        if (enabled) {
            this.mEnabledServices.add(cn);
        } else {
            this.mEnabledServices.remove(cn);
        }
        saveEnabledServices();
    }
}
