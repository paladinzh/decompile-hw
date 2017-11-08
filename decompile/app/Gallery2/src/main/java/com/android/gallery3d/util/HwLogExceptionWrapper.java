package com.android.gallery3d.util;

import com.android.gallery3d.util.Wrapper.InstanceMethodCaller;
import com.android.gallery3d.util.Wrapper.ReflectCaller;
import com.android.gallery3d.util.Wrapper.StaticMethodCaller;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map.Entry;

public class HwLogExceptionWrapper extends Wrapper {
    private ReflectCaller mCloseEventStream;
    private ReflectCaller mOpenEventStream;
    private ReflectCaller mSendMsg;
    private ReflectCaller mSetParam;

    private static class InstanceCreator implements ReflectCaller {
        private static Class<?> sEventStreamClass;
        private static Class<?> sIMonitorClass;
        private Method closeEventStream;
        private Method openEventStream;
        private Method sendEvent;
        private Method setParam;

        private InstanceCreator() {
        }

        static {
            try {
                sIMonitorClass = Class.forName("android.util.IMonitor");
                sEventStreamClass = Class.forName("android.util.IMonitor$EventStream");
            } catch (ClassNotFoundException e) {
                GalleryLog.e("HwLogExceptionWrapper", "load class android.util.HwLogException failed");
            }
        }

        public Object run(Object[] para) throws IllegalArgumentException, InstantiationException, InvocationTargetException, IllegalAccessException, NoSuchMethodException {
            if (sIMonitorClass == null) {
                return null;
            }
            Constructor<?> c = sIMonitorClass.getConstructor(new Class[0]);
            this.openEventStream = sIMonitorClass.getDeclaredMethod("openEventStream", new Class[]{Integer.TYPE});
            this.closeEventStream = sIMonitorClass.getDeclaredMethod("closeEventStream", new Class[]{sEventStreamClass});
            this.sendEvent = sIMonitorClass.getDeclaredMethod("sendEvent", new Class[]{sEventStreamClass});
            this.setParam = sEventStreamClass.getMethod("setParam", new Class[]{Short.TYPE, String.class});
            return c.newInstance(new Object[0]);
        }
    }

    public HwLogExceptionWrapper() {
        InstanceCreator creator = new InstanceCreator();
        Wrapper.runCaller(creator, new Object[0]);
        initMethod(creator);
    }

    private void initMethod(InstanceCreator creator) {
        this.mOpenEventStream = new StaticMethodCaller(creator.openEventStream);
        this.mCloseEventStream = new StaticMethodCaller(creator.closeEventStream);
        this.mSendMsg = new StaticMethodCaller(creator.sendEvent);
        this.mSetParam = new InstanceMethodCaller(creator.setParam);
    }

    public void reportEvent(HashMap<Short, Object> eventInfoMap, int eventId) {
        Object eventStream = openEventStream(eventId);
        if (eventStream == null) {
            GalleryLog.d("HwLogExceptionWrapper", "Can't reflect android.util.IMonitor$EventStream, report failed.");
            return;
        }
        for (Entry<Short, Object> entry : eventInfoMap.entrySet()) {
            Short key = (Short) entry.getKey();
            setParam(eventStream, key.shortValue(), entry.getValue());
        }
        sendMsg(eventStream);
        closeEventStream(eventStream);
    }

    private Object openEventStream(int eventId) {
        return Wrapper.runCaller(this.mOpenEventStream, Integer.valueOf(eventId));
    }

    private Object closeEventStream(Object eventStream) {
        return Wrapper.runCaller(this.mCloseEventStream, eventStream);
    }

    private Object sendMsg(Object eventStream) {
        return Wrapper.runCaller(this.mSendMsg, eventStream);
    }

    private Object setParam(Object eventStream, short key, Object value) {
        return Wrapper.runCaller(this.mSetParam, eventStream, Short.valueOf(key), value);
    }
}
