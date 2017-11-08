package com.huawei.powergenie.integration.eventhub;

import android.util.Log;
import java.util.ArrayList;

public abstract class EventHub {
    private final ArrayList<EventListener> mListenerEntries = new ArrayList();

    protected abstract boolean start();

    protected boolean addEventListener(EventListener listener) {
        if (listener == null) {
            Log.e("EventHub", "null is a invalid listener.");
            return false;
        }
        if (this.mListenerEntries.contains(listener)) {
            Log.i("EventHub", "listener has already been added.");
        } else {
            this.mListenerEntries.add(listener);
        }
        return true;
    }

    protected boolean dispatchEvent(Event evt) {
        if (this.mListenerEntries.size() == 0) {
            Log.w("EventHub", "no any listener. event id:" + evt.getEventId());
            return false;
        }
        for (EventListener listenerEntry : this.mListenerEntries) {
            listenerEntry.handleEvent(evt);
        }
        return true;
    }
}
