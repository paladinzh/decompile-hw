package com.huawei.keyguard.monitor;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map.Entry;

public class RadarUtils {
    private static Class<?> mClazzEventStream;
    private static Class<?> mClazzIMonitor;

    public static class RadarEventStream {
        private Object mEventStreamObj;

        public RadarEventStream(Object eventStreamObj) {
            this.mEventStreamObj = eventStreamObj;
        }

        public Object getEventStream() {
            return this.mEventStreamObj;
        }

        public RadarEventStream setParam(HashMap<Short, Object> map) {
            for (Entry<Short, Object> entry : map.entrySet()) {
                short key = ((Short) entry.getKey()).shortValue();
                Object value = entry.getValue();
                try {
                    Class cls = this.mEventStreamObj.getClass();
                    String str = "setParam";
                    Class[] clsArr = new Class[2];
                    clsArr[0] = Short.TYPE;
                    clsArr[1] = value.getClass().equals(Integer.class) ? Integer.TYPE : value.getClass();
                    this.mEventStreamObj = cls.getDeclaredMethod(str, clsArr).invoke(this.mEventStreamObj, new Object[]{Short.valueOf(key), value});
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (NoSuchMethodException e2) {
                    e2.printStackTrace();
                } catch (IllegalArgumentException e3) {
                    e3.printStackTrace();
                } catch (InvocationTargetException e4) {
                    e4.printStackTrace();
                }
            }
            return this;
        }
    }

    static {
        try {
            mClazzIMonitor = Class.forName("android.util.IMonitor");
            mClazzEventStream = Class.forName("android.util.IMonitor$EventStream");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static boolean sendEvent(RadarEventStream eventStream) {
        if (eventStream == null) {
            return false;
        }
        boolean ret = false;
        try {
            ret = ((Boolean) mClazzIMonitor.getMethod("sendEvent", new Class[]{mClazzEventStream}).invoke(mClazzIMonitor, new Object[]{eventStream.getEventStream()})).booleanValue();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e2) {
            e2.printStackTrace();
        } catch (IllegalArgumentException e3) {
            e3.printStackTrace();
        } catch (InvocationTargetException e4) {
            e4.printStackTrace();
        }
        return ret;
    }

    public static RadarEventStream openEventStream(int eventId) {
        try {
            return new RadarEventStream(mClazzIMonitor.getMethod("openEventStream", new Class[]{Integer.TYPE}).invoke(mClazzIMonitor, new Object[]{Integer.valueOf(eventId)}));
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        } catch (NoSuchMethodException e2) {
            e2.printStackTrace();
            return null;
        } catch (IllegalArgumentException e3) {
            e3.printStackTrace();
            return null;
        } catch (InvocationTargetException e4) {
            e4.printStackTrace();
            return null;
        }
    }

    public static void closeEventStream(RadarEventStream eventStream) {
        if (eventStream != null) {
            try {
                mClazzIMonitor.getMethod("closeEventStream", new Class[]{mClazzEventStream}).invoke(mClazzIMonitor, new Object[]{eventStream.getEventStream()});
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e2) {
                e2.printStackTrace();
            } catch (IllegalArgumentException e3) {
                e3.printStackTrace();
            } catch (InvocationTargetException e4) {
                e4.printStackTrace();
            }
        }
    }
}
