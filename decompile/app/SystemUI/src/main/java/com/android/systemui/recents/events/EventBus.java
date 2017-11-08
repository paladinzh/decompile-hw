package com.android.systemui.recents.events;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Process;
import android.os.SystemClock;
import android.os.UserHandle;
import android.util.Log;
import android.util.MutableBoolean;
import com.android.systemui.recents.misc.ReferenceCountedTrigger;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class EventBus extends BroadcastReceiver {
    private static final Comparator<EventHandler> EVENT_HANDLER_COMPARATOR = new Comparator<EventHandler>() {
        public int compare(EventHandler h1, EventHandler h2) {
            if (h1.priority != h2.priority) {
                return h2.priority - h1.priority;
            }
            return Long.compare(h2.subscriber.registrationTime, h1.subscriber.registrationTime);
        }
    };
    private static EventBus sDefaultBus;
    private static final Object sLock = new Object();
    private HashMap<Class<? extends Event>, ArrayList<EventHandler>> mEventTypeMap = new HashMap();
    private Handler mHandler;
    private HashMap<String, Class<? extends InterprocessEvent>> mInterprocessEventNameMap = new HashMap();
    private HashMap<Class<? extends Object>, ArrayList<EventHandlerMethod>> mSubscriberTypeMap = new HashMap();
    private ArrayList<Subscriber> mSubscribers = new ArrayList();

    public static class Event implements Cloneable {
        boolean cancelled;
        boolean requiresPost;
        boolean trace;

        protected Event() {
        }

        void onPreDispatch() {
        }

        void onPostDispatch() {
        }

        protected Object clone() throws CloneNotSupportedException {
            Event evt = (Event) super.clone();
            evt.cancelled = false;
            return evt;
        }
    }

    public static class AnimatedEvent extends Event {
        private final ReferenceCountedTrigger mTrigger = new ReferenceCountedTrigger();

        protected AnimatedEvent() {
        }

        public ReferenceCountedTrigger getAnimationTrigger() {
            return this.mTrigger;
        }

        public void addPostAnimationCallback(Runnable r) {
            this.mTrigger.addLastDecrementRunnable(r);
        }

        void onPreDispatch() {
            this.mTrigger.increment();
        }

        void onPostDispatch() {
            this.mTrigger.decrement();
        }

        protected Object clone() throws CloneNotSupportedException {
            throw new CloneNotSupportedException();
        }
    }

    public static class InterprocessEvent extends Event {
    }

    public static class ReusableEvent extends Event {
        private int mDispatchCount;

        protected ReusableEvent() {
        }

        void onPostDispatch() {
            super.onPostDispatch();
            this.mDispatchCount++;
        }

        protected Object clone() throws CloneNotSupportedException {
            throw new CloneNotSupportedException();
        }
    }

    private EventBus(Looper looper) {
        this.mHandler = new Handler(looper);
    }

    public static EventBus getDefault() {
        if (sDefaultBus == null) {
            synchronized (sLock) {
                if (sDefaultBus == null) {
                    sDefaultBus = new EventBus(Looper.getMainLooper());
                }
            }
        }
        return sDefaultBus;
    }

    public void register(Object subscriber) {
        registerSubscriber(subscriber, 1, null);
    }

    public void register(Object subscriber, int priority) {
        registerSubscriber(subscriber, priority, null);
    }

    public void unregister(Object subscriber) {
        if (Thread.currentThread().getId() != this.mHandler.getLooper().getThread().getId()) {
            throw new RuntimeException("Can not unregister() a subscriber from a non-main thread.");
        } else if (findRegisteredSubscriber(subscriber, true)) {
            ArrayList<EventHandlerMethod> subscriberMethods = (ArrayList) this.mSubscriberTypeMap.get(subscriber.getClass());
            if (subscriberMethods != null) {
                for (EventHandlerMethod method : subscriberMethods) {
                    ArrayList<EventHandler> eventHandlers = (ArrayList) this.mEventTypeMap.get(method.eventType);
                    for (int i = eventHandlers.size() - 1; i >= 0; i--) {
                        if (((EventHandler) eventHandlers.get(i)).subscriber.getReference() == subscriber) {
                            eventHandlers.remove(i);
                        }
                    }
                }
            }
        }
    }

    public void send(Event event) {
        if (Thread.currentThread().getId() != this.mHandler.getLooper().getThread().getId()) {
            throw new RuntimeException("Can not send() a message from a non-main thread.");
        }
        event.requiresPost = false;
        event.cancelled = false;
        queueEvent(event);
    }

    public void post(Event event) {
        event.requiresPost = true;
        event.cancelled = false;
        queueEvent(event);
    }

    public void sendOntoMainThread(Event event) {
        if (Thread.currentThread().getId() != this.mHandler.getLooper().getThread().getId()) {
            post(event);
        } else {
            send(event);
        }
    }

    public void onReceive(Context context, Intent intent) {
        Bundle eventBundle = intent.getBundleExtra("interprocess_event_bundle");
        try {
            send((Event) ((Class) this.mInterprocessEventNameMap.get(intent.getAction())).getConstructor(new Class[]{Bundle.class}).newInstance(new Object[]{eventBundle}));
        } catch (ReflectiveOperationException e) {
            Log.e("EventBus", "Failed to create InterprocessEvent", e.getCause());
        }
    }

    public void dump(String prefix, PrintWriter writer) {
        writer.println(dumpInternal(prefix));
    }

    public String dumpInternal(String prefix) {
        int i;
        String innerPrefix = prefix + "  ";
        String innerInnerPrefix = innerPrefix + "  ";
        StringBuilder output = new StringBuilder();
        output.append(prefix);
        output.append("Registered class types:");
        output.append("\n");
        ArrayList<Class<?>> subsciberTypes = new ArrayList(this.mSubscriberTypeMap.keySet());
        Collections.sort(subsciberTypes, new Comparator<Class<?>>() {
            public int compare(Class<?> o1, Class<?> o2) {
                return o1.getSimpleName().compareTo(o2.getSimpleName());
            }
        });
        for (i = 0; i < subsciberTypes.size(); i++) {
            Class<?> clz = (Class) subsciberTypes.get(i);
            output.append(innerPrefix);
            output.append(clz.getSimpleName());
            output.append("\n");
        }
        output.append(prefix);
        output.append("Event map:");
        output.append("\n");
        ArrayList<Class<?>> classes = new ArrayList(this.mEventTypeMap.keySet());
        Collections.sort(classes, new Comparator<Class<?>>() {
            public int compare(Class<?> o1, Class<?> o2) {
                return o1.getSimpleName().compareTo(o2.getSimpleName());
            }
        });
        for (i = 0; i < classes.size(); i++) {
            clz = (Class) classes.get(i);
            output.append(innerPrefix);
            output.append(clz.getSimpleName());
            output.append(" -> ");
            output.append("\n");
            for (EventHandler handler : (ArrayList) this.mEventTypeMap.get(clz)) {
                Object subscriber = handler.subscriber.getReference();
                if (subscriber != null) {
                    String id = Integer.toHexString(System.identityHashCode(subscriber));
                    output.append(innerInnerPrefix);
                    output.append(subscriber.getClass().getSimpleName());
                    output.append(" [0x").append(id).append(", #").append(handler.priority).append("]");
                    output.append("\n");
                }
            }
        }
        return output.toString();
    }

    private void registerSubscriber(Object subscriber, int priority, MutableBoolean hasInterprocessEventsChangedOut) {
        if (Thread.currentThread().getId() != this.mHandler.getLooper().getThread().getId()) {
            throw new RuntimeException("Can not register() a subscriber from a non-main thread.");
        } else if (!findRegisteredSubscriber(subscriber, false)) {
            Subscriber subscriber2 = new Subscriber(subscriber, SystemClock.uptimeMillis());
            Class<?> subscriberType = subscriber.getClass();
            ArrayList<EventHandlerMethod> subscriberMethods = (ArrayList) this.mSubscriberTypeMap.get(subscriberType);
            ArrayList<EventHandler> eventTypeHandlers;
            EventHandlerMethod method;
            if (subscriberMethods != null) {
                for (EventHandlerMethod method2 : subscriberMethods) {
                    eventTypeHandlers = (ArrayList) this.mEventTypeMap.get(method2.eventType);
                    eventTypeHandlers.add(new EventHandler(subscriber2, method2, priority));
                    sortEventHandlersByPriority(eventTypeHandlers);
                }
                this.mSubscribers.add(subscriber2);
                return;
            }
            subscriberMethods = new ArrayList();
            this.mSubscriberTypeMap.put(subscriberType, subscriberMethods);
            this.mSubscribers.add(subscriber2);
            MutableBoolean isInterprocessEvent = new MutableBoolean(false);
            for (Method m : subscriberType.getDeclaredMethods()) {
                Class<?>[] parameterTypes = m.getParameterTypes();
                isInterprocessEvent.value = false;
                if (isValidEventBusHandlerMethod(m, parameterTypes, isInterprocessEvent)) {
                    Class<? extends Event> eventType = parameterTypes[0];
                    eventTypeHandlers = (ArrayList) this.mEventTypeMap.get(eventType);
                    if (eventTypeHandlers == null) {
                        eventTypeHandlers = new ArrayList();
                        this.mEventTypeMap.put(eventType, eventTypeHandlers);
                    }
                    if (isInterprocessEvent.value) {
                        try {
                            eventType.getConstructor(new Class[]{Bundle.class});
                            this.mInterprocessEventNameMap.put(eventType.getName(), eventType);
                            if (hasInterprocessEventsChangedOut != null) {
                                hasInterprocessEventsChangedOut.value = true;
                            }
                        } catch (NoSuchMethodException e) {
                            throw new RuntimeException("Expected InterprocessEvent to have a Bundle constructor");
                        }
                    }
                    method2 = new EventHandlerMethod(m, eventType);
                    eventTypeHandlers.add(new EventHandler(subscriber2, method2, priority));
                    subscriberMethods.add(method2);
                    sortEventHandlersByPriority(eventTypeHandlers);
                }
            }
        }
    }

    private void queueEvent(final Event event) {
        ArrayList<EventHandler> eventHandlers = (ArrayList) this.mEventTypeMap.get(event.getClass());
        if (eventHandlers != null) {
            boolean hasPostedEvent = false;
            event.onPreDispatch();
            eventHandlers = (ArrayList) eventHandlers.clone();
            int eventHandlerCount = eventHandlers.size();
            for (int i = 0; i < eventHandlerCount; i++) {
                final EventHandler eventHandler = (EventHandler) eventHandlers.get(i);
                if (eventHandler.subscriber.getReference() != null) {
                    if (event.requiresPost) {
                        this.mHandler.post(new Runnable() {
                            public void run() {
                                EventBus.this.processEvent(eventHandler, event);
                            }
                        });
                        hasPostedEvent = true;
                    } else {
                        processEvent(eventHandler, event);
                    }
                }
            }
            if (hasPostedEvent) {
                this.mHandler.post(new Runnable() {
                    public void run() {
                        event.onPostDispatch();
                    }
                });
            } else {
                event.onPostDispatch();
            }
        }
    }

    private void processEvent(EventHandler eventHandler, Event event) {
        if (event.cancelled) {
            if (event.trace) {
                logWithPid("Event dispatch cancelled");
            }
            return;
        }
        try {
            if (event.trace) {
                logWithPid(" -> " + eventHandler.toString());
            }
            Object sub = eventHandler.subscriber.getReference();
            if (sub != null) {
                eventHandler.method.invoke(sub, event);
            } else {
                Log.e("EventBus", "Failed to deliver event to null subscriber");
            }
        } catch (IllegalAccessException e) {
            Log.e("EventBus", "Failed to invoke method", e.getCause());
        } catch (InvocationTargetException e2) {
            throw new RuntimeException(e2.getCause());
        }
    }

    private boolean findRegisteredSubscriber(Object subscriber, boolean removeFoundSubscriber) {
        for (int i = this.mSubscribers.size() - 1; i >= 0; i--) {
            if (((Subscriber) this.mSubscribers.get(i)).getReference() == subscriber) {
                if (removeFoundSubscriber) {
                    this.mSubscribers.remove(i);
                }
                return true;
            }
        }
        return false;
    }

    private boolean isValidEventBusHandlerMethod(Method method, Class<?>[] parameterTypes, MutableBoolean isInterprocessEventOut) {
        int modifiers = method.getModifiers();
        if (Modifier.isPublic(modifiers) && Modifier.isFinal(modifiers) && method.getReturnType().equals(Void.TYPE) && parameterTypes.length == 1) {
            if (InterprocessEvent.class.isAssignableFrom(parameterTypes[0]) && method.getName().startsWith("onInterprocessBusEvent")) {
                isInterprocessEventOut.value = true;
                return true;
            } else if (Event.class.isAssignableFrom(parameterTypes[0]) && method.getName().startsWith("onBusEvent")) {
                isInterprocessEventOut.value = false;
                return true;
            }
        }
        return false;
    }

    private void sortEventHandlersByPriority(List<EventHandler> eventHandlers) {
        Collections.sort(eventHandlers, EVENT_HANDLER_COMPARATOR);
    }

    private static void logWithPid(String text) {
        Log.d("EventBus", "[" + Process.myPid() + ", u" + UserHandle.myUserId() + "] " + text);
    }
}
