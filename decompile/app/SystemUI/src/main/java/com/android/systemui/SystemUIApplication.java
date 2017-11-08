package com.android.systemui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Process;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.util.Log;
import com.android.systemui.keyboard.KeyboardUI;
import com.android.systemui.keyguard.HwKeyguardViewMediator;
import com.android.systemui.media.RingtonePlayer;
import com.android.systemui.power.PowerUI;
import com.android.systemui.recents.Recents;
import com.android.systemui.shortcut.ShortcutKeyDispatcher;
import com.android.systemui.stackdivider.Divider;
import com.android.systemui.statusbar.SystemBars;
import com.android.systemui.tuner.TunerService;
import com.android.systemui.tv.pip.PipUI;
import com.android.systemui.usb.HwStorageNotification;
import com.android.systemui.utils.SystemUiUtil;
import com.android.systemui.volume.VolumeUI;
import com.huawei.systemui.BaseApplication;
import fyusion.vislib.BuildConfig;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class SystemUIApplication extends BaseApplication {
    private final Class<?>[] SERVICES = new Class[]{TunerService.class, HwKeyguardViewMediator.class, Recents.class, VolumeUI.class, Divider.class, SystemBars.class, HwStorageNotification.class, PowerUI.class, RingtonePlayer.class, KeyboardUI.class, PipUI.class, ShortcutKeyDispatcher.class};
    private final Class<?>[] SERVICES_PER_USER = new Class[]{Recents.class, PipUI.class};
    private boolean mBootCompleted;
    private final Map<Class<?>, Object> mComponents = new HashMap();
    private String mLanguage = BuildConfig.FLAVOR;
    private final SystemUI[] mServices = new SystemUI[this.SERVICES.length];
    protected boolean mServicesStarted;

    public void onCreate() {
        super.onCreate();
        setTheme(R.style.systemui_theme);
        SystemUIFactory.createFromConfig(this);
        if (!isScreenshotProcess()) {
            if (Process.myUserHandle().equals(UserHandle.SYSTEM)) {
                IntentFilter filter = new IntentFilter("android.intent.action.BOOT_COMPLETED");
                filter.setPriority(1000);
                registerReceiver(new BroadcastReceiver() {
                    public void onReceive(Context context, Intent intent) {
                        if (!SystemUIApplication.this.mBootCompleted) {
                            Log.v("SystemUIService", "BOOT_COMPLETED received");
                            SystemUIApplication.this.unregisterReceiver(this);
                            SystemUIApplication.this.mBootCompleted = true;
                            if (SystemUIApplication.this.mServicesStarted) {
                                for (SystemUI onBootCompleted : SystemUIApplication.this.mServices) {
                                    onBootCompleted.onBootCompleted();
                                }
                            }
                            if (SystemUiUtil.isMarketPlaceVersion()) {
                                SystemUiUtil.sendForMarketPlaceNotifcation(SystemUIApplication.this);
                                SystemUIApplication.this.mLanguage = Locale.getDefault().getLanguage();
                            }
                        }
                    }
                }, filter);
            } else {
                startServicesIfNeeded(this.SERVICES_PER_USER);
            }
        }
    }

    public void startServicesIfNeeded() {
        startServicesIfNeeded(this.SERVICES);
    }

    void startSecondaryUserServicesIfNeeded() {
        startServicesIfNeeded(this.SERVICES_PER_USER);
    }

    private void startServicesIfNeeded(Class<?>[] services) {
        Log.i("SystemUIService", "startServicesIfNeeded in");
        if (!this.mServicesStarted) {
            if (!this.mBootCompleted && "1".equals(SystemProperties.get("sys.boot_completed"))) {
                this.mBootCompleted = true;
                Log.v("SystemUIService", "BOOT_COMPLETED was already sent");
            }
            Log.v("SystemUIService", "Starting SystemUI services for user " + Process.myUserHandle().getIdentifier() + ".");
            int N = services.length;
            int i = 0;
            while (i < N) {
                Class<?> cl = services[i];
                Log.d("SystemUIService", "loading: " + cl);
                try {
                    Object newInstance;
                    Object newService = SystemUIFactory.getInstance().createInstance(cl);
                    SystemUI[] systemUIArr = this.mServices;
                    if (newService == null) {
                        newInstance = cl.newInstance();
                    } else {
                        newInstance = newService;
                    }
                    systemUIArr[i] = (SystemUI) newInstance;
                    this.mServices[i].mContext = this;
                    this.mServices[i].mComponents = this.mComponents;
                    Log.d("SystemUIService", "running: " + this.mServices[i]);
                    this.mServices[i].start();
                    if (this.mBootCompleted) {
                        this.mServices[i].onBootCompleted();
                    }
                    i++;
                } catch (IllegalAccessException ex) {
                    throw new RuntimeException(ex);
                } catch (InstantiationException ex2) {
                    throw new RuntimeException(ex2);
                }
            }
            this.mServicesStarted = true;
            Log.i("SystemUIService", "startServicesIfNeeded out");
        }
    }

    public void onConfigurationChanged(Configuration newConfig) {
        if (this.mServicesStarted) {
            int len = this.mServices.length;
            for (int i = 0; i < len; i++) {
                if (this.mServices[i] != null) {
                    this.mServices[i].onConfigurationChanged(newConfig);
                }
            }
            if (SystemUiUtil.isMarketPlaceVersion() && newConfig.locale != null && !newConfig.locale.getLanguage().equals(this.mLanguage)) {
                this.mLanguage = newConfig.locale.getLanguage();
                SystemUiUtil.sendForMarketPlaceNotifcation(this);
            }
        }
    }

    public <T> T getComponent(Class<T> interfaceType) {
        return this.mComponents.get(interfaceType);
    }

    public SystemUI[] getServices() {
        return this.mServices;
    }
}
