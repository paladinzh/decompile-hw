package com.huawei.powergenie.debugtest;

import com.huawei.powergenie.integration.adapter.NativeAdapter;
import com.huawei.powergenie.integration.eventhub.Event;
import com.huawei.powergenie.integration.eventhub.EventListener;
import com.huawei.powergenie.integration.eventhub.HookEvent;
import com.huawei.powergenie.integration.eventhub.MsgEvent;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map.Entry;

public class DbgEventHub implements EventListener {
    private final HashMap<Integer, Integer> mHookEventCount = new HashMap();
    private final HashMap<Integer, String> mHookEventName = new HashMap();
    private final HashMap<Integer, Integer> mMsgEventCount = new HashMap();
    private final HashMap<Integer, String> mMsgEventName = new HashMap();

    protected DbgEventHub() {
        init();
    }

    private void init() {
        this.mHookEventCount.put(Integer.valueOf(106), Integer.valueOf(0));
        this.mHookEventCount.put(Integer.valueOf(107), Integer.valueOf(0));
        this.mHookEventCount.put(Integer.valueOf(110), Integer.valueOf(0));
        this.mHookEventCount.put(Integer.valueOf(111), Integer.valueOf(0));
        this.mHookEventCount.put(Integer.valueOf(113), Integer.valueOf(0));
        this.mHookEventCount.put(Integer.valueOf(114), Integer.valueOf(0));
        this.mHookEventCount.put(Integer.valueOf(115), Integer.valueOf(0));
        this.mHookEventCount.put(Integer.valueOf(117), Integer.valueOf(0));
        this.mHookEventCount.put(Integer.valueOf(118), Integer.valueOf(0));
        this.mHookEventCount.put(Integer.valueOf(119), Integer.valueOf(0));
        this.mHookEventCount.put(Integer.valueOf(120), Integer.valueOf(0));
        this.mHookEventCount.put(Integer.valueOf(121), Integer.valueOf(0));
        this.mHookEventCount.put(Integer.valueOf(122), Integer.valueOf(0));
        this.mHookEventCount.put(Integer.valueOf(123), Integer.valueOf(0));
        this.mHookEventCount.put(Integer.valueOf(125), Integer.valueOf(0));
        this.mHookEventCount.put(Integer.valueOf(126), Integer.valueOf(0));
        this.mHookEventCount.put(Integer.valueOf(128), Integer.valueOf(0));
        this.mHookEventCount.put(Integer.valueOf(129), Integer.valueOf(0));
        this.mHookEventCount.put(Integer.valueOf(130), Integer.valueOf(0));
        this.mHookEventCount.put(Integer.valueOf(134), Integer.valueOf(0));
        this.mHookEventCount.put(Integer.valueOf(135), Integer.valueOf(0));
        this.mHookEventCount.put(Integer.valueOf(139), Integer.valueOf(0));
        this.mHookEventCount.put(Integer.valueOf(140), Integer.valueOf(0));
        this.mHookEventCount.put(Integer.valueOf(141), Integer.valueOf(0));
        this.mHookEventCount.put(Integer.valueOf(142), Integer.valueOf(0));
        this.mHookEventCount.put(Integer.valueOf(143), Integer.valueOf(0));
        this.mHookEventCount.put(Integer.valueOf(144), Integer.valueOf(0));
        this.mHookEventCount.put(Integer.valueOf(146), Integer.valueOf(0));
        this.mHookEventCount.put(Integer.valueOf(147), Integer.valueOf(0));
        this.mHookEventCount.put(Integer.valueOf(148), Integer.valueOf(0));
        this.mHookEventCount.put(Integer.valueOf(151), Integer.valueOf(0));
        this.mHookEventCount.put(Integer.valueOf(152), Integer.valueOf(0));
        this.mHookEventCount.put(Integer.valueOf(153), Integer.valueOf(0));
        this.mHookEventCount.put(Integer.valueOf(154), Integer.valueOf(0));
        this.mHookEventCount.put(Integer.valueOf(155), Integer.valueOf(0));
        this.mHookEventCount.put(Integer.valueOf(156), Integer.valueOf(0));
        this.mHookEventCount.put(Integer.valueOf(157), Integer.valueOf(0));
        this.mHookEventCount.put(Integer.valueOf(158), Integer.valueOf(0));
        this.mHookEventCount.put(Integer.valueOf(159), Integer.valueOf(0));
        this.mHookEventCount.put(Integer.valueOf(160), Integer.valueOf(0));
        this.mHookEventCount.put(Integer.valueOf(161), Integer.valueOf(0));
        this.mHookEventCount.put(Integer.valueOf(162), Integer.valueOf(0));
        this.mHookEventCount.put(Integer.valueOf(163), Integer.valueOf(0));
        this.mHookEventCount.put(Integer.valueOf(164), Integer.valueOf(0));
        this.mHookEventCount.put(Integer.valueOf(165), Integer.valueOf(0));
        this.mHookEventCount.put(Integer.valueOf(166), Integer.valueOf(0));
        this.mHookEventCount.put(Integer.valueOf(167), Integer.valueOf(0));
        this.mHookEventCount.put(Integer.valueOf(168), Integer.valueOf(0));
        this.mHookEventCount.put(Integer.valueOf(169), Integer.valueOf(0));
        this.mHookEventCount.put(Integer.valueOf(170), Integer.valueOf(0));
        this.mHookEventCount.put(Integer.valueOf(171), Integer.valueOf(0));
        this.mHookEventCount.put(Integer.valueOf(172), Integer.valueOf(0));
        this.mHookEventCount.put(Integer.valueOf(173), Integer.valueOf(0));
        this.mHookEventCount.put(Integer.valueOf(174), Integer.valueOf(0));
        this.mHookEventCount.put(Integer.valueOf(175), Integer.valueOf(0));
        this.mHookEventCount.put(Integer.valueOf(176), Integer.valueOf(0));
        this.mHookEventCount.put(Integer.valueOf(177), Integer.valueOf(0));
        this.mHookEventCount.put(Integer.valueOf(178), Integer.valueOf(0));
        this.mHookEventCount.put(Integer.valueOf(179), Integer.valueOf(0));
        this.mHookEventCount.put(Integer.valueOf(180), Integer.valueOf(0));
        this.mHookEventCount.put(Integer.valueOf(181), Integer.valueOf(0));
        this.mHookEventCount.put(Integer.valueOf(182), Integer.valueOf(0));
        this.mHookEventCount.put(Integer.valueOf(183), Integer.valueOf(0));
        this.mHookEventCount.put(Integer.valueOf(184), Integer.valueOf(0));
        this.mMsgEventCount.put(Integer.valueOf(300), Integer.valueOf(0));
        this.mMsgEventCount.put(Integer.valueOf(301), Integer.valueOf(0));
        this.mMsgEventCount.put(Integer.valueOf(302), Integer.valueOf(0));
        this.mMsgEventCount.put(Integer.valueOf(303), Integer.valueOf(0));
        this.mMsgEventCount.put(Integer.valueOf(304), Integer.valueOf(0));
        this.mMsgEventCount.put(Integer.valueOf(305), Integer.valueOf(0));
        this.mMsgEventCount.put(Integer.valueOf(306), Integer.valueOf(0));
        this.mMsgEventCount.put(Integer.valueOf(307), Integer.valueOf(0));
        this.mMsgEventCount.put(Integer.valueOf(308), Integer.valueOf(0));
        this.mMsgEventCount.put(Integer.valueOf(309), Integer.valueOf(0));
        this.mMsgEventCount.put(Integer.valueOf(310), Integer.valueOf(0));
        this.mMsgEventCount.put(Integer.valueOf(311), Integer.valueOf(0));
        this.mMsgEventCount.put(Integer.valueOf(312), Integer.valueOf(0));
        this.mMsgEventCount.put(Integer.valueOf(313), Integer.valueOf(0));
        this.mMsgEventCount.put(Integer.valueOf(314), Integer.valueOf(0));
        this.mMsgEventCount.put(Integer.valueOf(315), Integer.valueOf(0));
        this.mMsgEventCount.put(Integer.valueOf(317), Integer.valueOf(0));
        this.mMsgEventCount.put(Integer.valueOf(318), Integer.valueOf(0));
        this.mMsgEventCount.put(Integer.valueOf(319), Integer.valueOf(0));
        this.mMsgEventCount.put(Integer.valueOf(320), Integer.valueOf(0));
        this.mMsgEventCount.put(Integer.valueOf(321), Integer.valueOf(0));
        this.mMsgEventCount.put(Integer.valueOf(322), Integer.valueOf(0));
        this.mMsgEventCount.put(Integer.valueOf(323), Integer.valueOf(0));
        this.mMsgEventCount.put(Integer.valueOf(325), Integer.valueOf(0));
        this.mMsgEventCount.put(Integer.valueOf(326), Integer.valueOf(0));
        this.mMsgEventCount.put(Integer.valueOf(327), Integer.valueOf(0));
        this.mMsgEventCount.put(Integer.valueOf(328), Integer.valueOf(0));
        this.mMsgEventCount.put(Integer.valueOf(329), Integer.valueOf(0));
        this.mMsgEventCount.put(Integer.valueOf(330), Integer.valueOf(0));
        this.mMsgEventCount.put(Integer.valueOf(333), Integer.valueOf(0));
        this.mMsgEventCount.put(Integer.valueOf(334), Integer.valueOf(0));
        this.mMsgEventCount.put(Integer.valueOf(350), Integer.valueOf(0));
        this.mMsgEventCount.put(Integer.valueOf(356), Integer.valueOf(0));
        this.mMsgEventCount.put(Integer.valueOf(357), Integer.valueOf(0));
        this.mMsgEventCount.put(Integer.valueOf(358), Integer.valueOf(0));
        this.mMsgEventCount.put(Integer.valueOf(359), Integer.valueOf(0));
        this.mMsgEventCount.put(Integer.valueOf(360), Integer.valueOf(0));
        this.mMsgEventCount.put(Integer.valueOf(361), Integer.valueOf(0));
        this.mMsgEventCount.put(Integer.valueOf(335), Integer.valueOf(0));
        this.mMsgEventCount.put(Integer.valueOf(362), Integer.valueOf(0));
        this.mMsgEventCount.put(Integer.valueOf(336), Integer.valueOf(0));
        this.mHookEventName.put(Integer.valueOf(111), "APP_PROCESS_START");
        this.mHookEventName.put(Integer.valueOf(106), "APP_3D_FRONT");
        this.mHookEventName.put(Integer.valueOf(107), "APP_3D_EXIT");
        this.mHookEventName.put(Integer.valueOf(110), "ALL_DOWNLOAD_FINISH");
        this.mHookEventName.put(Integer.valueOf(113), "APP_RUN_FRONT");
        this.mHookEventName.put(Integer.valueOf(114), "APP_RUN_BG");
        this.mHookEventName.put(Integer.valueOf(115), "ALARM_BLOCKED");
        this.mHookEventName.put(Integer.valueOf(117), "INPUT_START");
        this.mHookEventName.put(Integer.valueOf(118), "INPUT_END");
        this.mHookEventName.put(Integer.valueOf(119), "HW_PUSH_FINISH");
        this.mHookEventName.put(Integer.valueOf(120), "FULL_SCREEN_FRONT");
        this.mHookEventName.put(Integer.valueOf(121), "ALARM_START");
        this.mHookEventName.put(Integer.valueOf(122), "NOTIFICATION_NEW");
        this.mHookEventName.put(Integer.valueOf(123), "NOTIFICATION_CANCEL");
        this.mHookEventName.put(Integer.valueOf(125), "TOUCH_DOWN");
        this.mHookEventName.put(Integer.valueOf(126), "TOUCH_UP");
        this.mHookEventName.put(Integer.valueOf(128), "START_CHG_ROTATION");
        this.mHookEventName.put(Integer.valueOf(129), "CAMERA_START");
        this.mHookEventName.put(Integer.valueOf(130), "END_CHG_ROTATION");
        this.mHookEventName.put(Integer.valueOf(134), "CAMERA_END");
        this.mHookEventName.put(Integer.valueOf(135), "FULL_SCREEN_END");
        this.mHookEventName.put(Integer.valueOf(139), "APP_START_SPEEDUP");
        this.mHookEventName.put(Integer.valueOf(140), "MUSIC_AUDIO_PLAY");
        this.mHookEventName.put(Integer.valueOf(141), "SURFACEVIEW_CREATED");
        this.mHookEventName.put(Integer.valueOf(142), "SURFACEVIEW_DESTROYED");
        this.mHookEventName.put(Integer.valueOf(143), "ENABLE_SENSOR");
        this.mHookEventName.put(Integer.valueOf(144), "DISABLE_SENSOR");
        this.mHookEventName.put(Integer.valueOf(146), "COM_THERMAL_EVENT");
        this.mHookEventName.put(Integer.valueOf(147), "AUDIO_START_PLAY");
        this.mHookEventName.put(Integer.valueOf(148), "FREEZER_EXCEPTION");
        this.mHookEventName.put(Integer.valueOf(151), "ADD_VIEW");
        this.mHookEventName.put(Integer.valueOf(152), "REMOVE_VIEW");
        this.mHookEventName.put(Integer.valueOf(153), "ACTIVITY_DRAW_TYPE");
        this.mHookEventName.put(Integer.valueOf(154), "LIST_FLING_START");
        this.mHookEventName.put(Integer.valueOf(155), "LIST_FLING_END");
        this.mHookEventName.put(Integer.valueOf(156), "GPS_START");
        this.mHookEventName.put(Integer.valueOf(157), "GPS_END");
        this.mHookEventName.put(Integer.valueOf(158), "WIFI_SCAN_START");
        this.mHookEventName.put(Integer.valueOf(159), "WIFI_SCAN_END");
        this.mHookEventName.put(Integer.valueOf(160), "WAKELOCK_ACQUIRED");
        this.mHookEventName.put(Integer.valueOf(161), "WAKELOCK_RELEASED");
        this.mHookEventName.put(Integer.valueOf(162), "AUDIO_SESSION_ID_NEW");
        this.mHookEventName.put(Integer.valueOf(163), "AUDIO_SESSION_ID_RELEASE");
        this.mHookEventName.put(Integer.valueOf(164), "AUDIO_SESSION_START");
        this.mHookEventName.put(Integer.valueOf(165), "AUDIO_SESSION_STOP");
        this.mHookEventName.put(Integer.valueOf(166), "ADD_PROCESS_DEPENDENCY");
        this.mHookEventName.put(Integer.valueOf(167), "REMOVE_PROCESS_DEPENDENCY");
        this.mHookEventName.put(Integer.valueOf(168), "APPWIDGET_ENABLED");
        this.mHookEventName.put(Integer.valueOf(169), "LOW_POWER_AUDIO_START");
        this.mHookEventName.put(Integer.valueOf(170), "LOW_POWER_AUDIO_STOP");
        this.mHookEventName.put(Integer.valueOf(171), "LOW_POWER_AUDIO_RESET");
        this.mHookEventName.put(Integer.valueOf(172), "BLE_SOCKECT_CONNECTED");
        this.mHookEventName.put(Integer.valueOf(173), "BLE_SOCKECT_CLOSED");
        this.mHookEventName.put(Integer.valueOf(174), "HARD_KEY_EVENT");
        this.mHookEventName.put(Integer.valueOf(175), "SCREEN_SHOT_START");
        this.mHookEventName.put(Integer.valueOf(176), "SCREEN_SHOT_END");
        this.mHookEventName.put(Integer.valueOf(177), "MEDIA_RECORDER_START");
        this.mHookEventName.put(Integer.valueOf(178), "MEDIA_RECORDER_END");
        this.mHookEventName.put(Integer.valueOf(179), "SPEED_UP_START");
        this.mHookEventName.put(Integer.valueOf(180), "SPEED_UP_END");
        this.mHookEventName.put(Integer.valueOf(181), "BLE_ACTIVE_APP");
        this.mHookEventName.put(Integer.valueOf(182), "BLE_INACTIVE_APP");
        this.mHookEventName.put(Integer.valueOf(183), "TEXTUREVIEW_CREATED");
        this.mHookEventName.put(Integer.valueOf(184), "TEXTUREVIEW_DESTROYED");
        this.mMsgEventName.put(Integer.valueOf(300), "SCREEN_ON");
        this.mMsgEventName.put(Integer.valueOf(301), "SCREEN_OFF");
        this.mMsgEventName.put(Integer.valueOf(302), "BOOT_COMPLETED");
        this.mMsgEventName.put(Integer.valueOf(303), "SHUTDOWN");
        this.mMsgEventName.put(Integer.valueOf(304), "USER_PRESENT");
        this.mMsgEventName.put(Integer.valueOf(305), "PACKAGE_ADDED");
        this.mMsgEventName.put(Integer.valueOf(306), "PACKAGE_CHANGED");
        this.mMsgEventName.put(Integer.valueOf(307), "PACKAGE_REMOVED");
        this.mMsgEventName.put(Integer.valueOf(308), "BATTERY_CHANGED");
        this.mMsgEventName.put(Integer.valueOf(309), "USB_STATE");
        this.mMsgEventName.put(Integer.valueOf(310), "POWER_CONNECTED");
        this.mMsgEventName.put(Integer.valueOf(311), "POWER_DISCONNECTED");
        this.mMsgEventName.put(Integer.valueOf(312), "CONNECTIVITY_ACTION");
        this.mMsgEventName.put(Integer.valueOf(313), "TETHER_STATE_CHANGED");
        this.mMsgEventName.put(Integer.valueOf(314), "WIFI_NETWORK_CHANGED");
        this.mMsgEventName.put(Integer.valueOf(315), "WIFI_STATE_CHANGED");
        this.mMsgEventName.put(Integer.valueOf(317), "AIRPLANE_MODE_CHANGED");
        this.mMsgEventName.put(Integer.valueOf(318), "WALLPAPER_CHANGED");
        this.mMsgEventName.put(Integer.valueOf(319), "BATTERY_LOW");
        this.mMsgEventName.put(Integer.valueOf(320), "BATTERY_OKAY");
        this.mMsgEventName.put(Integer.valueOf(321), "LOCK_SCREEN_FRONT");
        this.mMsgEventName.put(Integer.valueOf(322), "CALL_BUSY");
        this.mMsgEventName.put(Integer.valueOf(323), "CALL_IDLE");
        this.mMsgEventName.put(Integer.valueOf(325), "BLUETOOTH_CONNECTED");
        this.mMsgEventName.put(Integer.valueOf(326), "BLUETOOTH_DISCONNECTED");
        this.mMsgEventName.put(Integer.valueOf(327), "SYS_BATTERY_OKAY");
        this.mMsgEventName.put(Integer.valueOf(328), "OPERATOR_CHINA");
        this.mMsgEventName.put(Integer.valueOf(329), "OPERATOR_NOTCHINA");
        this.mMsgEventName.put(Integer.valueOf(330), "SIM_STATE_CHANGED");
        this.mMsgEventName.put(Integer.valueOf(333), "BATTERY_CRITICAL");
        this.mMsgEventName.put(Integer.valueOf(334), "BATTERY_CRITICAL_OK");
        this.mMsgEventName.put(Integer.valueOf(350), "POWER_MODE_CHANGED");
        this.mMsgEventName.put(Integer.valueOf(356), "PENDING_ALARM_ACTION");
        this.mMsgEventName.put(Integer.valueOf(357), "SCROFF_CTRLSOCKET_ACTION");
        this.mMsgEventName.put(Integer.valueOf(358), "UPDATE_CONFIG_ACTION");
        this.mMsgEventName.put(Integer.valueOf(359), "USER_SWITCHED");
        this.mMsgEventName.put(Integer.valueOf(360), "BT_ACTIVE_APPS_RESULT");
        this.mMsgEventName.put(Integer.valueOf(361), "EXTREME_POWER_MODE");
        this.mMsgEventName.put(Integer.valueOf(335), "HEADSET_PLUG");
        this.mMsgEventName.put(Integer.valueOf(362), "MOCA_USER_DEVICE");
        this.mMsgEventName.put(Integer.valueOf(336), "BT_STATE_CHANGED");
    }

    public void handleEvent(Event evt) {
        switch (evt.getType()) {
            case NativeAdapter.PLATFORM_MTK /*1*/:
                dbgMsgEvent((MsgEvent) evt);
                return;
            case NativeAdapter.PLATFORM_HI /*2*/:
                dbgHookEvent((HookEvent) evt);
                return;
            default:
                return;
        }
    }

    private void dbgMsgEvent(MsgEvent evt) {
        Integer count = (Integer) this.mMsgEventCount.get(Integer.valueOf(evt.getEventId()));
        if (count != null) {
            HashMap hashMap = this.mMsgEventCount;
            Integer valueOf = Integer.valueOf(evt.getEventId());
            int intValue = count.intValue() + 1;
            count = Integer.valueOf(intValue);
            hashMap.put(valueOf, Integer.valueOf(intValue));
        }
    }

    private void dbgHookEvent(HookEvent evt) {
        Integer count = (Integer) this.mHookEventCount.get(Integer.valueOf(evt.getEventId()));
        if (count != null) {
            HashMap hashMap = this.mHookEventCount;
            Integer valueOf = Integer.valueOf(evt.getEventId());
            int intValue = count.intValue() + 1;
            count = Integer.valueOf(intValue);
            hashMap.put(valueOf, Integer.valueOf(intValue));
        }
    }

    protected void dump(PrintWriter pw, String arg) {
        pw.println();
        if ("all".equals(arg) || "event".equals(arg)) {
            pw.println("\nHookEvents State!");
            for (Entry entry : this.mHookEventCount.entrySet()) {
                Integer eventId = (Integer) entry.getKey();
                String name = (String) this.mHookEventName.get(eventId);
                pw.println("  event:" + eventId + " count:" + ((Integer) entry.getValue()) + " name: " + name);
            }
        }
        if ("all".equals(arg) || "msg".equals(arg)) {
            pw.println("\nMsgEvents State!");
            for (Entry entry2 : this.mMsgEventCount.entrySet()) {
                eventId = (Integer) entry2.getKey();
                name = (String) this.mMsgEventName.get(eventId);
                pw.println("  msg:" + eventId + " count:" + ((Integer) entry2.getValue()) + " name: " + name);
            }
        }
    }
}
