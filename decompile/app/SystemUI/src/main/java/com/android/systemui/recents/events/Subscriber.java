package com.android.systemui.recents.events;

import java.lang.ref.WeakReference;

/* compiled from: EventBus */
class Subscriber {
    private WeakReference<Object> mSubscriber;
    long registrationTime;

    Subscriber(Object subscriber, long registrationTime) {
        this.mSubscriber = new WeakReference(subscriber);
        this.registrationTime = registrationTime;
    }

    public String toString(int priority) {
        Object sub = this.mSubscriber.get();
        return sub.getClass().getSimpleName() + " [0x" + Integer.toHexString(System.identityHashCode(sub)) + ", P" + priority + "]";
    }

    public Object getReference() {
        return this.mSubscriber.get();
    }
}
