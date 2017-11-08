package com.android.settings;

import android.util.Log;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map.Entry;

public class RadarUtil {
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
                if (value != null) {
                    Class valueClass = value.getClass();
                    try {
                        if (value.getClass().equals(Integer.class)) {
                            valueClass = Integer.TYPE;
                        }
                        this.mEventStreamObj = this.mEventStreamObj.getClass().getDeclaredMethod("setParam", new Class[]{Short.TYPE, valueClass}).invoke(this.mEventStreamObj, new Object[]{Short.valueOf(key), value});
                        Log.d("RadarUtil", "setParam succeed");
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
        boolean z = false;
        try {
            z = ((Boolean) mClazzIMonitor.getMethod("sendEvent", new Class[]{mClazzEventStream}).invoke(mClazzIMonitor, new Object[]{eventStream.getEventStream()})).booleanValue();
            Log.d("RadarUtil", "sendEvent succeed");
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e2) {
            e2.printStackTrace();
        } catch (IllegalArgumentException e3) {
            e3.printStackTrace();
        } catch (InvocationTargetException e4) {
            e4.printStackTrace();
        }
        return z;
    }

    public static RadarEventStream openEventStream(int eventId) {
        IllegalAccessException e;
        NoSuchMethodException e2;
        IllegalArgumentException e3;
        InvocationTargetException e4;
        RadarEventStream radarEventStream = null;
        try {
            RadarEventStream eventStream = new RadarEventStream(mClazzIMonitor.getMethod("openEventStream", new Class[]{Integer.TYPE}).invoke(mClazzIMonitor, new Object[]{Integer.valueOf(eventId)}));
            try {
                Log.d("RadarUtil", "openEventStream succeed");
                return eventStream;
            } catch (IllegalAccessException e5) {
                e = e5;
                radarEventStream = eventStream;
                e.printStackTrace();
                return radarEventStream;
            } catch (NoSuchMethodException e6) {
                e2 = e6;
                radarEventStream = eventStream;
                e2.printStackTrace();
                return radarEventStream;
            } catch (IllegalArgumentException e7) {
                e3 = e7;
                radarEventStream = eventStream;
                e3.printStackTrace();
                return radarEventStream;
            } catch (InvocationTargetException e8) {
                e4 = e8;
                radarEventStream = eventStream;
                e4.printStackTrace();
                return radarEventStream;
            }
        } catch (IllegalAccessException e9) {
            e = e9;
            e.printStackTrace();
            return radarEventStream;
        } catch (NoSuchMethodException e10) {
            e2 = e10;
            e2.printStackTrace();
            return radarEventStream;
        } catch (IllegalArgumentException e11) {
            e3 = e11;
            e3.printStackTrace();
            return radarEventStream;
        } catch (InvocationTargetException e12) {
            e4 = e12;
            e4.printStackTrace();
            return radarEventStream;
        }
    }

    public static void closeEventStream(RadarEventStream eventStream) {
        if (eventStream != null) {
            try {
                mClazzIMonitor.getMethod("closeEventStream", new Class[]{mClazzEventStream}).invoke(mClazzIMonitor, new Object[]{eventStream.getEventStream()});
                Log.d("RadarUtil", "closeEventStream succeed");
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
