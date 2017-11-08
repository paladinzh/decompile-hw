package com.huawei.keyguard.monitor;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.SparseArray;
import java.util.HashMap;

public class StateMonitor {
    private static final StateMonitor sInst = new StateMonitor();
    private Handler mBackgroundHandler;
    @SuppressLint({"UseSparseArrays"})
    private SparseArray<Integer> mEventList = new SparseArray();
    @SuppressLint({"UseSparseArrays"})
    private SparseArray<Integer> mResponseList = new SparseArray();

    private static class DelayHandler extends Handler {
        private DelayHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            HashMap<Short, Object> map = new HashMap();
            switch (msg.what) {
                case 101:
                    map.put(Short.valueOf((short) 0), "unknown");
                    map.put(Short.valueOf((short) 1), "pwd");
                    RadarReporter.reportRadar(907030001, map);
                    return;
                case 102:
                    map.put(Short.valueOf((short) 0), "unknown");
                    map.put(Short.valueOf((short) 1), "pattern");
                    RadarReporter.reportRadar(907030001, map);
                    return;
                case 301:
                    map.put(Short.valueOf((short) 0), "unknown");
                    RadarReporter.reportRadar(907030003, map);
                    return;
                case 401:
                    map.put(Short.valueOf((short) 0), "unknown");
                    map.put(Short.valueOf((short) 1), "play");
                    RadarReporter.reportRadar(907030004, map);
                    return;
                case 402:
                    map.put(Short.valueOf((short) 0), "unknown");
                    map.put(Short.valueOf((short) 1), "pause");
                    RadarReporter.reportRadar(907030004, map);
                    return;
                case 403:
                    map.put(Short.valueOf((short) 0), "unknown");
                    map.put(Short.valueOf((short) 1), "prev");
                    RadarReporter.reportRadar(907030004, map);
                    return;
                case 404:
                    map.put(Short.valueOf((short) 0), "unknown");
                    map.put(Short.valueOf((short) 1), "next");
                    RadarReporter.reportRadar(907030004, map);
                    return;
                case 501:
                    map.put(Short.valueOf((short) 0), "unknown");
                    map.put(Short.valueOf((short) 1), "press");
                    RadarReporter.reportRadar(907030005, map);
                    return;
                default:
                    return;
            }
        }
    }

    public static StateMonitor getInst() {
        return sInst;
    }

    private StateMonitor() {
        HandlerThread backgroundThread = new HandlerThread("KG_Background_Handle_Thread", 1);
        backgroundThread.start();
        this.mBackgroundHandler = new DelayHandler(backgroundThread.getLooper());
        this.mResponseList.put(111, Integer.valueOf(101));
        this.mResponseList.put(112, Integer.valueOf(102));
        this.mResponseList.put(311, Integer.valueOf(301));
        this.mResponseList.put(411, Integer.valueOf(401));
        this.mResponseList.put(412, Integer.valueOf(402));
        this.mResponseList.put(413, Integer.valueOf(403));
        this.mResponseList.put(414, Integer.valueOf(404));
        this.mResponseList.put(511, Integer.valueOf(501));
        this.mEventList.put(101, Integer.valueOf(2000));
        this.mEventList.put(102, Integer.valueOf(2000));
        this.mEventList.put(301, Integer.valueOf(4000));
        this.mEventList.put(401, Integer.valueOf(3000));
        this.mEventList.put(402, Integer.valueOf(3000));
        this.mEventList.put(403, Integer.valueOf(3000));
        this.mEventList.put(501, Integer.valueOf(2000));
    }

    public void cancelEvent(int actionId) {
        Integer eventId = (Integer) this.mResponseList.get(actionId);
        if (eventId != null) {
            this.mBackgroundHandler.removeMessages(eventId.intValue());
        }
    }

    public void triggerEvent(int eventId) {
        if (this.mBackgroundHandler.hasMessages(eventId)) {
            this.mBackgroundHandler.removeMessages(eventId);
        }
        Integer getDelay = (Integer) this.mEventList.get(eventId);
        int delayForEvent = 0;
        if (getDelay != null) {
            delayForEvent = getDelay.intValue();
        }
        this.mBackgroundHandler.sendEmptyMessageDelayed(eventId, System.currentTimeMillis() + ((long) (delayForEvent == 0 ? 2000 : delayForEvent)));
    }
}
