package com.android.systemui.utils.analyze;

import android.content.Intent;
import android.net.Uri;
import com.android.systemui.utils.HwLog;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map.Entry;

public class MonitorReporter {
    public static void doMonitor(Intent intent) {
        if (intent != null) {
            if (-1 == intent.getIntExtra("EventId", -1)) {
                HwLog.e("SystemUI:MonitorReporter", "doMonitor::event id is invalid.");
                return;
            }
            try {
                HashMap<Short, Object> map = (HashMap) intent.getSerializableExtra("Infos");
                if (map == null) {
                    HwLog.e("SystemUI:MonitorReporter", "doMonitor::info map is null");
                    return;
                }
                Class<?> IMonitor = Class.forName("android.util.IMonitor");
                Class<?> EventStream = Class.forName("android.util.IMonitor$EventStream");
                Method openEventStream = IMonitor.getDeclaredMethod("openEventStream", new Class[]{Integer.TYPE});
                Method closeEventStream = IMonitor.getDeclaredMethod("closeEventStream", new Class[]{EventStream});
                Method sendEvent = IMonitor.getDeclaredMethod("sendEvent", new Class[]{EventStream});
                Object eStream = openEventStream.invoke(IMonitor, new Object[]{Integer.valueOf(eventId)});
                if (eStream != null) {
                    for (Entry<Short, Object> entry : map.entrySet()) {
                        short key = ((Short) entry.getKey()).shortValue();
                        Object value = entry.getValue();
                        EventStream.getDeclaredMethod("setParam", new Class[]{Short.TYPE, value.getClass()}).invoke(eStream, new Object[]{Short.valueOf(key), value});
                    }
                    sendEvent.invoke(IMonitor, new Object[]{eStream});
                    closeEventStream.invoke(IMonitor, new Object[]{eStream});
                }
            } catch (ClassNotFoundException e) {
                HwLog.e("SystemUI:MonitorReporter", "ClassNotFoundException doMonitor: " + e);
            } catch (NoSuchMethodException e2) {
                HwLog.e("SystemUI:MonitorReporter", "NoSuchMethodException doMonitor: " + e2);
            } catch (IllegalArgumentException e3) {
                HwLog.e("SystemUI:MonitorReporter", "IllegalArgumentException doMonitor: " + e3);
            } catch (IllegalAccessException e4) {
                HwLog.e("SystemUI:MonitorReporter", " IllegalAccessException doMonitor: " + e4);
            } catch (Exception e5) {
                HwLog.e("SystemUI:MonitorReporter", "Exception doMonitor: " + e5);
            }
        }
    }

    public static Intent createInfoIntent(int eventId, HashMap<Short, Object> infoMap) {
        Intent intent = new Intent();
        intent.putExtra("EventId", eventId);
        intent.putExtra("Infos", infoMap);
        return intent;
    }

    public static HashMap<Short, Object> createMapInfo(short key, String reason) {
        HashMap<Short, Object> infoMap = new HashMap();
        infoMap.put(Short.valueOf(key), reason);
        return infoMap;
    }

    public static HashMap<Short, Object> createSoundPlayerInfo(String reason, Uri uri) {
        Object obj = null;
        HashMap<Short, Object> infoMap = new HashMap();
        infoMap.put(Short.valueOf((short) 0), reason);
        Short valueOf = Short.valueOf((short) 1);
        if (uri != null) {
            obj = uri.toString();
        }
        infoMap.put(valueOf, obj);
        return infoMap;
    }

    public static HashMap<Short, Object> createNaviBarStateInfo(String reason, boolean state) {
        HashMap<Short, Object> infoMap = new HashMap();
        infoMap.put(Short.valueOf((short) 0), reason);
        infoMap.put(Short.valueOf((short) 1), Boolean.valueOf(state));
        return infoMap;
    }

    public static HashMap<Short, Object> createFlashlightStateInfo(String reason, boolean state) {
        HashMap<Short, Object> infoMap = new HashMap();
        infoMap.put(Short.valueOf((short) 0), reason);
        infoMap.put(Short.valueOf((short) 1), Boolean.valueOf(state));
        return infoMap;
    }
}
