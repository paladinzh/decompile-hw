package com.huawei.mms.util;

import com.huawei.cspcommon.MLog;
import java.lang.reflect.InvocationTargetException;

public class MonitorMms {
    private static Class<?> mClazzEventStream;
    private static Class<?> mClazzIMonitor;

    public static class EventStreamMms {
        private Object mEventStreamObj;

        public EventStreamMms(Object eventStreamObj) {
            this.mEventStreamObj = eventStreamObj;
        }

        public Object getEventStream() {
            return this.mEventStreamObj;
        }

        public EventStreamMms setParam(short paramID, int value) {
            if (this.mEventStreamObj == null) {
                MLog.e("MonitorMms", "setParam failed for EventStream is null !");
                return this;
            }
            try {
                this.mEventStreamObj = this.mEventStreamObj.getClass().getDeclaredMethod("setParam", new Class[]{Short.TYPE, Integer.TYPE}).invoke(this.mEventStreamObj, new Object[]{Short.valueOf(paramID), Integer.valueOf(value)});
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e2) {
                e2.printStackTrace();
            } catch (IllegalArgumentException e3) {
                e3.printStackTrace();
            } catch (IllegalAccessException e4) {
                e4.printStackTrace();
            }
            return this;
        }

        public EventStreamMms setParam(short paramID, String value) {
            if (this.mEventStreamObj == null) {
                MLog.e("MonitorMms", "setParam failed for EventStream is null !");
                return this;
            } else if (value == null) {
                return this;
            } else {
                try {
                    this.mEventStreamObj = this.mEventStreamObj.getClass().getDeclaredMethod("setParam", new Class[]{Short.TYPE, String.class}).invoke(this.mEventStreamObj, new Object[]{Short.valueOf(paramID), value});
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
    }

    static {
        try {
            mClazzIMonitor = Class.forName("android.util.IMonitor");
            mClazzEventStream = Class.forName("android.util.IMonitor$EventStream");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static EventStreamMms openEventStream(int eventId) {
        try {
            return new EventStreamMms(mClazzIMonitor.getMethod("openEventStream", new Class[]{Integer.TYPE}).invoke(mClazzIMonitor, new Object[]{Integer.valueOf(eventId)}));
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

    public static boolean sendEvent(EventStreamMms eventStream) {
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

    public static void closeEventStream(EventStreamMms eventStream) {
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
