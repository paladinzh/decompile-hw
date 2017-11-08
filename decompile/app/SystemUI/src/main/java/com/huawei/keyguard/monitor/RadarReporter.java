package com.huawei.keyguard.monitor;

import com.huawei.keyguard.monitor.RadarUtils.RadarEventStream;
import fyusion.vislib.BuildConfig;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class RadarReporter {
    public static void reportRadar(int eventId, HashMap<Short, Object> map) {
        RadarEventStream eventStream = RadarUtils.openEventStream(eventId);
        eventStream.setParam(map);
        RadarUtils.sendEvent(eventStream);
        RadarUtils.closeEventStream(eventStream);
    }

    public static void uploadSdcardMountRadar() {
        String uploadErrorTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Long.valueOf(new Date().getTime()));
        HashMap<Short, Object> map = new HashMap();
        map.put(Short.valueOf((short) 0), "Sdcard is not mounted now!");
        map.put(Short.valueOf((short) 1), uploadErrorTime);
        map.put(Short.valueOf((short) 2), "unknown");
        reportRadar(907030008, map);
    }

    public static void uploadPWDExceptionRadar(int type) {
        HashMap<Short, Object> map = new HashMap();
        map.put(Short.valueOf((short) 0), Integer.valueOf(type));
        map.put(Short.valueOf((short) 1), BuildConfig.FLAVOR + type);
        reportRadar(907034001, map);
    }
}
