package com.android.systemui;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import com.android.internal.widget.LockPatternUtils;
import com.android.keyguard.ViewMediatorCallback;
import com.android.systemui.statusbar.ScrimView;
import com.android.systemui.statusbar.phone.HwStatusBarKeyguardViewManager;
import com.android.systemui.statusbar.phone.KeyguardBouncer;
import com.android.systemui.statusbar.phone.PhoneStatusBar;
import com.android.systemui.statusbar.phone.QSTileHost;
import com.android.systemui.statusbar.phone.ScrimController;
import com.android.systemui.statusbar.phone.StatusBarIconController;
import com.android.systemui.statusbar.phone.StatusBarKeyguardViewManager;
import com.android.systemui.statusbar.phone.StatusBarWindowManager;
import com.android.systemui.statusbar.policy.BatteryController;
import com.android.systemui.statusbar.policy.BluetoothController;
import com.android.systemui.statusbar.policy.CastController;
import com.android.systemui.statusbar.policy.FlashlightController;
import com.android.systemui.statusbar.policy.HotspotController;
import com.android.systemui.statusbar.policy.KeyguardMonitor;
import com.android.systemui.statusbar.policy.LocationController;
import com.android.systemui.statusbar.policy.NetworkController;
import com.android.systemui.statusbar.policy.NextAlarmController;
import com.android.systemui.statusbar.policy.RotationLockController;
import com.android.systemui.statusbar.policy.SecurityController;
import com.android.systemui.statusbar.policy.UserInfoController;
import com.android.systemui.statusbar.policy.UserSwitcherController;
import com.android.systemui.statusbar.policy.ZenModeController;

public class SystemUIFactory {
    static SystemUIFactory mFactory;

    public static SystemUIFactory getInstance() {
        return mFactory;
    }

    public static void createFromConfig(Context context) {
        String clsName = context.getString(R.string.config_systemUIFactoryComponent);
        if (clsName == null || clsName.length() == 0) {
            throw new RuntimeException("No SystemUIFactory component configured");
        }
        try {
            mFactory = (SystemUIFactory) context.getClassLoader().loadClass(clsName).newInstance();
        } catch (Throwable t) {
            Log.w("SystemUIFactory", "Error creating SystemUIFactory component: " + clsName, t);
            RuntimeException runtimeException = new RuntimeException(t);
        }
    }

    public StatusBarKeyguardViewManager createStatusBarKeyguardViewManager(Context context, ViewMediatorCallback viewMediatorCallback, LockPatternUtils lockPatternUtils) {
        return new HwStatusBarKeyguardViewManager(context, viewMediatorCallback, lockPatternUtils);
    }

    public KeyguardBouncer createKeyguardBouncer(Context context, ViewMediatorCallback callback, LockPatternUtils lockPatternUtils, StatusBarWindowManager windowManager, ViewGroup container) {
        return new KeyguardBouncer(context, callback, lockPatternUtils, windowManager, container);
    }

    public ScrimController createScrimController(ScrimView scrimBehind, ScrimView scrimInFront, View headsUpScrim) {
        return new ScrimController(scrimBehind, scrimInFront, headsUpScrim);
    }

    public QSTileHost createQSTileHost(Context context, PhoneStatusBar statusBar, BluetoothController bluetooth, LocationController location, RotationLockController rotation, NetworkController network, ZenModeController zen, HotspotController hotspot, CastController cast, FlashlightController flashlight, UserSwitcherController userSwitcher, UserInfoController userInfo, KeyguardMonitor keyguard, SecurityController security, BatteryController battery, StatusBarIconController iconController, NextAlarmController nextAlarmController) {
        return new QSTileHost(context, statusBar, bluetooth, location, rotation, network, zen, hotspot, cast, flashlight, userSwitcher, userInfo, keyguard, security, battery, iconController, nextAlarmController);
    }

    public <T> T createInstance(Class<T> cls) {
        return null;
    }
}
