package com.android.settings;

import com.android.settings.RadarUtil.RadarEventStream;
import java.util.HashMap;

public class RadarReporter {
    public static void reportRadar(int eventId, HashMap<Short, Object> map) {
        RadarEventStream eventStream = RadarUtil.openEventStream(eventId);
        eventStream.setParam(map);
        RadarUtil.sendEvent(eventStream);
        RadarUtil.closeEventStream(eventStream);
    }
}
