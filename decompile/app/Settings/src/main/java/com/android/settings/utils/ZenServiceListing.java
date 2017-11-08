package com.android.settings.utils;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.provider.Settings.Secure;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.Slog;
import com.android.settings.utils.ManagedServiceSettings.Config;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class ZenServiceListing {
    private final Set<ServiceInfo> mApprovedServices = new ArraySet();
    private final Config mConfig;
    private final ContentResolver mContentResolver;
    private final Context mContext;
    private final List<Callback> mZenCallbacks = new ArrayList();

    public interface Callback {
        void onServicesReloaded(Set<ServiceInfo> set);
    }

    public ZenServiceListing(Context context, Config config) {
        this.mContext = context;
        this.mConfig = config;
        this.mContentResolver = context.getContentResolver();
    }

    public ServiceInfo findService(ComponentName cn) {
        for (ServiceInfo service : this.mApprovedServices) {
            if (new ComponentName(service.packageName, service.name).equals(cn)) {
                return service;
            }
        }
        return null;
    }

    public void removeZenCallback(Callback callback) {
        this.mZenCallbacks.remove(callback);
    }

    public void reloadApprovedServices() {
        int i = 0;
        this.mApprovedServices.clear();
        String[] settings = new String[]{this.mConfig.setting, this.mConfig.secondarySetting};
        int length = settings.length;
        while (i < length) {
            String setting = settings[i];
            if (!TextUtils.isEmpty(setting)) {
                String flat = Secure.getString(this.mContentResolver, setting);
                if (!TextUtils.isEmpty(flat)) {
                    List<String> names = Arrays.asList(flat.split(":"));
                    List<ServiceInfo> services = new ArrayList();
                    getServices(this.mConfig, services, this.mContext.getPackageManager());
                    for (ServiceInfo service : services) {
                        if (matchesApprovedPackage(names, service.getComponentName())) {
                            this.mApprovedServices.add(service);
                        }
                    }
                }
            }
            i++;
        }
        if (!this.mApprovedServices.isEmpty()) {
            for (Callback callback : this.mZenCallbacks) {
                callback.onServicesReloaded(this.mApprovedServices);
            }
        }
    }

    private boolean matchesApprovedPackage(List<String> approved, ComponentName serviceOwner) {
        if (approved.contains(serviceOwner.flattenToString()) || approved.contains(serviceOwner.getPackageName())) {
            return true;
        }
        for (String entry : approved) {
            if (!TextUtils.isEmpty(entry)) {
                ComponentName approvedComponent = ComponentName.unflattenFromString(entry);
                if (approvedComponent != null && approvedComponent.getPackageName().equals(serviceOwner.getPackageName())) {
                    return true;
                }
            }
        }
        return false;
    }

    private static int getServices(Config c, List<ServiceInfo> list, PackageManager pm) {
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
}
