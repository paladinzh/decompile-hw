package com.android.systemui.recents.events;

import com.android.systemui.recents.events.EventBus.Event;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/* compiled from: EventBus */
class EventHandlerMethod {
    Class<? extends Event> eventType;
    private Method mMethod;

    EventHandlerMethod(Method method, Class<? extends Event> eventType) {
        this.mMethod = method;
        this.mMethod.setAccessible(true);
        this.eventType = eventType;
    }

    public void invoke(Object target, Event event) throws InvocationTargetException, IllegalAccessException {
        this.mMethod.invoke(target, new Object[]{event});
    }

    public String toString() {
        return this.mMethod.getName() + "(" + this.eventType.getSimpleName() + ")";
    }
}
