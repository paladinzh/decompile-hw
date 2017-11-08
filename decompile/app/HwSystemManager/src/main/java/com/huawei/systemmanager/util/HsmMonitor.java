package com.huawei.systemmanager.util;

import java.lang.reflect.InvocationTargetException;
import java.util.Date;

public class HsmMonitor {
    public static final int BASE_EVENTID = 907021000;
    private static final String IMONITORRADAR_CLASSNAME = "android.util.IMonitor";
    private static final String IMONITORRADAR_EVENTSTREAM_CLASSNAME = "android.util.IMonitor$EventStream";
    private static final String IMONITORRADAR_METHOD_closeEventStream = "closeEventStream";
    private static final String IMONITORRADAR_METHOD_openEventStream = "openEventStream";
    private static final String IMONITORRADAR_METHOD_sendEvent = "sendEvent";
    private static final String IMONITORRADAR_METHOD_setParam = "setParam";
    public static final int MAX_EVENTID = 907023999;
    private static final String TAG = "IMonitorMms";
    private static Class<?> mClazzEventStream;
    private static Class<?> mClazzIMonitor;

    public static class EVENTID {
        public static final int NETASSISTANT_OPERATOR_FAILED = 907021001;
    }

    public static class EventStream {
        private Object mEventStreamObj;

        public EventStream(Object eventStreamObj) {
            this.mEventStreamObj = eventStreamObj;
        }

        public boolean sendEvent() {
            boolean z = false;
            try {
                z = ((Boolean) HsmMonitor.mClazzIMonitor.getMethod(HsmMonitor.IMONITORRADAR_METHOD_sendEvent, new Class[]{HsmMonitor.mClazzEventStream}).invoke(HsmMonitor.mClazzIMonitor, new Object[]{this.mEventStreamObj})).booleanValue();
                HsmMonitor.mClazzIMonitor.getMethod(HsmMonitor.IMONITORRADAR_METHOD_closeEventStream, new Class[]{HsmMonitor.mClazzEventStream}).invoke(HsmMonitor.mClazzIMonitor, new Object[]{this.mEventStreamObj});
                return z;
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                return z;
            } catch (NoSuchMethodException e2) {
                e2.printStackTrace();
                return z;
            } catch (IllegalArgumentException e3) {
                e3.printStackTrace();
                return z;
            } catch (InvocationTargetException e4) {
                e4.printStackTrace();
                return z;
            }
        }

        public EventStream setParam(short paramID, Date value) {
            if (this.mEventStreamObj == null) {
                HwLog.e(HsmMonitor.TAG, "setParam failed for EventStream is null !");
                return this;
            }
            try {
                this.mEventStreamObj = this.mEventStreamObj.getClass().getDeclaredMethod(HsmMonitor.IMONITORRADAR_METHOD_setParam, new Class[]{Short.TYPE, Date.class}).invoke(this.mEventStreamObj, new Object[]{Short.valueOf(paramID), value});
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e2) {
                e2.printStackTrace();
            } catch (IllegalArgumentException e3) {
                e3.printStackTrace();
            } catch (InvocationTargetException e4) {
                e4.printStackTrace();
            }
            return this;
        }

        public EventStream setParam(short paramID, Boolean value) {
            if (this.mEventStreamObj == null) {
                HwLog.e(HsmMonitor.TAG, "setParam failed for EventStream is null !");
                return this;
            }
            try {
                this.mEventStreamObj = this.mEventStreamObj.getClass().getDeclaredMethod(HsmMonitor.IMONITORRADAR_METHOD_setParam, new Class[]{Short.TYPE, Boolean.class}).invoke(this.mEventStreamObj, new Object[]{Short.valueOf(paramID), value});
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e2) {
                e2.printStackTrace();
            } catch (IllegalArgumentException e3) {
                e3.printStackTrace();
            } catch (InvocationTargetException e4) {
                e4.printStackTrace();
            }
            return this;
        }

        public EventStream setParam(short paramID, byte value) {
            if (this.mEventStreamObj == null) {
                HwLog.e(HsmMonitor.TAG, "setParam failed for EventStream is null !");
                return this;
            }
            try {
                this.mEventStreamObj = this.mEventStreamObj.getClass().getDeclaredMethod(HsmMonitor.IMONITORRADAR_METHOD_setParam, new Class[]{Short.TYPE, Byte.TYPE}).invoke(this.mEventStreamObj, new Object[]{Short.valueOf(paramID), Byte.valueOf(value)});
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e2) {
                e2.printStackTrace();
            } catch (IllegalArgumentException e3) {
                e3.printStackTrace();
            } catch (InvocationTargetException e4) {
                e4.printStackTrace();
            }
            return this;
        }

        public EventStream setParam(short paramID, short value) {
            if (this.mEventStreamObj == null) {
                HwLog.e(HsmMonitor.TAG, "setParam failed for EventStream is null !");
                return this;
            }
            try {
                this.mEventStreamObj = this.mEventStreamObj.getClass().getDeclaredMethod(HsmMonitor.IMONITORRADAR_METHOD_setParam, new Class[]{Short.TYPE, Short.TYPE}).invoke(this.mEventStreamObj, new Object[]{Short.valueOf(paramID), Short.valueOf(value)});
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e2) {
                e2.printStackTrace();
            } catch (IllegalArgumentException e3) {
                e3.printStackTrace();
            } catch (InvocationTargetException e4) {
                e4.printStackTrace();
            }
            return this;
        }

        public EventStream setParam(short paramID, int value) {
            if (this.mEventStreamObj == null) {
                HwLog.e(HsmMonitor.TAG, "setParam failed for EventStream is null !");
                return this;
            }
            try {
                this.mEventStreamObj = this.mEventStreamObj.getClass().getDeclaredMethod(HsmMonitor.IMONITORRADAR_METHOD_setParam, new Class[]{Short.TYPE, Integer.TYPE}).invoke(this.mEventStreamObj, new Object[]{Short.valueOf(paramID), Integer.valueOf(value)});
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e2) {
                e2.printStackTrace();
            } catch (IllegalArgumentException e3) {
                e3.printStackTrace();
            } catch (InvocationTargetException e4) {
                e4.printStackTrace();
            }
            return this;
        }

        public EventStream setParam(short paramID, long value) {
            if (this.mEventStreamObj == null) {
                HwLog.e(HsmMonitor.TAG, "setParam failed for EventStream is null !");
                return this;
            }
            try {
                this.mEventStreamObj = this.mEventStreamObj.getClass().getDeclaredMethod(HsmMonitor.IMONITORRADAR_METHOD_setParam, new Class[]{Short.TYPE, Long.TYPE}).invoke(this.mEventStreamObj, new Object[]{Short.valueOf(paramID), Long.valueOf(value)});
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e2) {
                e2.printStackTrace();
            } catch (IllegalArgumentException e3) {
                e3.printStackTrace();
            } catch (InvocationTargetException e4) {
                e4.printStackTrace();
            }
            return this;
        }

        public EventStream setParam(short paramID, float value) {
            if (this.mEventStreamObj == null) {
                HwLog.e(HsmMonitor.TAG, "setParam failed for EventStream is null !");
                return this;
            }
            try {
                this.mEventStreamObj = this.mEventStreamObj.getClass().getDeclaredMethod(HsmMonitor.IMONITORRADAR_METHOD_setParam, new Class[]{Short.TYPE, Float.TYPE}).invoke(this.mEventStreamObj, new Object[]{Short.valueOf(paramID), Float.valueOf(value)});
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e2) {
                e2.printStackTrace();
            } catch (IllegalArgumentException e3) {
                e3.printStackTrace();
            } catch (InvocationTargetException e4) {
                e4.printStackTrace();
            }
            return this;
        }

        public EventStream setParam(short paramID, String value) {
            if (this.mEventStreamObj == null) {
                HwLog.e(HsmMonitor.TAG, "setParam failed for EventStream is null !");
                return this;
            }
            try {
                this.mEventStreamObj = this.mEventStreamObj.getClass().getDeclaredMethod(HsmMonitor.IMONITORRADAR_METHOD_setParam, new Class[]{Short.TYPE, String.class}).invoke(this.mEventStreamObj, new Object[]{Short.valueOf(paramID), value});
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e2) {
                e2.printStackTrace();
            } catch (IllegalArgumentException e3) {
                e3.printStackTrace();
            } catch (InvocationTargetException e4) {
                e4.printStackTrace();
            }
            return this;
        }
    }

    public static class PARAMETER {
        public static final short E907021001_DEVICEID_VARCHAR = (short) 0;
        public static final short E907021001_SDK_VERSION_VARCHAR = (short) 1;
    }

    static {
        try {
            mClazzIMonitor = Class.forName(IMONITORRADAR_CLASSNAME);
            mClazzEventStream = Class.forName(IMONITORRADAR_EVENTSTREAM_CLASSNAME);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static EventStream openEventStream(int eventId) {
        try {
            return new EventStream(mClazzIMonitor.getMethod(IMONITORRADAR_METHOD_openEventStream, new Class[]{Integer.TYPE}).invoke(mClazzIMonitor, new Object[]{Integer.valueOf(eventId)}));
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
}
