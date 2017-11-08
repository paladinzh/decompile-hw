package com.android.systemui.recents.events;

/* compiled from: EventBus */
class EventHandler {
    EventHandlerMethod method;
    int priority;
    Subscriber subscriber;

    EventHandler(Subscriber subscriber, EventHandlerMethod method, int priority) {
        this.subscriber = subscriber;
        this.method = method;
        this.priority = priority;
    }

    public String toString() {
        return this.subscriber.toString(this.priority) + " " + this.method.toString();
    }
}
