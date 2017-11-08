package com.android.systemui.observer;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.UserHandle;
import com.android.systemui.HwSystemUIApplication;
import com.android.systemui.utils.HwLog;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

@SuppressLint({"NewApi"})
public class SystemUIObserver {
    private static BroadcastReceiver mReceiver = null;
    private static HashMap<Integer, ObserverItem> observerItemMap = new HashMap();

    public static synchronized void init() {
        synchronized (SystemUIObserver.class) {
            release();
            initMaps();
            mReceiver = new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {
                    for (ObserverItem item : SystemUIObserver.getUniqueObserverItem()) {
                        item.onChange(false);
                    }
                }
            };
            Context context = HwSystemUIApplication.getContext();
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.USER_SWITCHED");
            context.registerReceiverAsUser(mReceiver, UserHandle.OWNER, filter, null, null);
        }
    }

    private static List<ObserverItem> getUniqueObserverItem() {
        return new ArrayList(new HashSet(observerItemMap.values()));
    }

    private static void initMaps() {
        Handler handler = new Handler();
        ObserverTimeSwitch timeSwitch = new ObserverTimeSwitch(handler);
        observerItemMap.put(Integer.valueOf(0), timeSwitch);
        observerItemMap.put(Integer.valueOf(1), timeSwitch);
        observerItemMap.put(Integer.valueOf(2), new ObserverAirplaneModeOn(handler));
        observerItemMap.put(Integer.valueOf(3), new ObserverDeviceProvisioned(handler));
        observerItemMap.put(Integer.valueOf(4), new ObserverHideVirtualKey(handler));
        observerItemMap.put(Integer.valueOf(5), new ObserverVirtualKeyType(handler));
        observerItemMap.put(Integer.valueOf(6), new ObserverSingleHand(handler));
        observerItemMap.put(Integer.valueOf(7), new ObserverZoomGesture(handler));
        observerItemMap.put(Integer.valueOf(8), new ObserverBatteryPercent(handler));
        observerItemMap.put(Integer.valueOf(9), new ObserverBatteryPercentIn(handler));
        observerItemMap.put(Integer.valueOf(10), new ObserverBatteryPluggedColor(handler));
        observerItemMap.put(Integer.valueOf(11), new ObserverNotificationIconShowType(handler));
        observerItemMap.put(Integer.valueOf(12), new ObserverDoubleTap(handler));
        observerItemMap.put(Integer.valueOf(13), new ObserverStudentMode(handler));
        observerItemMap.put(Integer.valueOf(14), new ObserverTrafficSwitch(handler));
        observerItemMap.put(Integer.valueOf(15), new ObserverTrafficData(handler));
        observerItemMap.put(Integer.valueOf(16), new ObserverInstantShare(handler));
        observerItemMap.put(Integer.valueOf(17), new ObserverEyeComfortMode(handler));
        observerItemMap.put(Integer.valueOf(18), new ObserverNavBarEnabled(handler));
        observerItemMap.put(Integer.valueOf(19), new ObserverVSimChanged(handler));
        observerItemMap.put(Integer.valueOf(20), new ObserverPowerSaveMode(handler));
        observerItemMap.put(Integer.valueOf(21), new ObserverDriveMode(handler));
        observerItemMap.put(Integer.valueOf(22), new ObserverVirtualKeyPosition(handler));
        observerItemMap.put(Integer.valueOf(23), new ObserverDataSwitchChanged(handler));
        observerItemMap.put(Integer.valueOf(24), new ObserverFPNavBarState(handler));
        for (ObserverItem item : getUniqueObserverItem()) {
            if (item.getUri() != null) {
                item.init();
            }
        }
    }

    public static Object get(int type) {
        ObserverItem<?> item = (ObserverItem) observerItemMap.get(Integer.valueOf(type));
        if (item == null) {
            return null;
        }
        return item.getValue(type);
    }

    public static ObserverItem<?> getObserver(int type) {
        ObserverItem<?> item = (ObserverItem) observerItemMap.get(Integer.valueOf(type));
        if (item == null) {
            return null;
        }
        return item;
    }

    public static Object get(int type, Object defaultVal) {
        ObserverItem<?> item = (ObserverItem) observerItemMap.get(Integer.valueOf(type));
        if (item != null) {
            return item.getValue(type, defaultVal);
        }
        HwLog.e("SystemUIObserver", "type:" + type + " is not in Map, use default!");
        return defaultVal;
    }

    public static synchronized void release() {
        synchronized (SystemUIObserver.class) {
            for (ObserverItem item : getUniqueObserverItem()) {
                if (item.getUri() != null) {
                    item.release();
                }
            }
            observerItemMap.clear();
            if (mReceiver != null) {
                try {
                    HwSystemUIApplication.getContext().unregisterReceiver(mReceiver);
                } catch (Exception e) {
                    HwLog.e("SystemUIObserver", "unregister receiver error");
                }
                mReceiver = null;
            }
        }
    }
}
