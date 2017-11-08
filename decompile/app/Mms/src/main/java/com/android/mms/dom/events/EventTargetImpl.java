package com.android.mms.dom.events;

import android.util.Log;
import java.util.ArrayList;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventException;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;

public class EventTargetImpl implements EventTarget {
    private ArrayList<EventListenerEntry> mListenerEntries;
    private EventTarget mNodeTarget;

    static class EventListenerEntry {
        final EventListener mListener;
        final String mType;
        final boolean mUseCapture;

        EventListenerEntry(String type, EventListener listener, boolean useCapture) {
            this.mType = type;
            this.mListener = listener;
            this.mUseCapture = useCapture;
        }
    }

    public EventTargetImpl(EventTarget target) {
        this.mNodeTarget = target;
    }

    public void addEventListener(String type, EventListener listener, boolean useCapture) {
        if (type != null && !type.equals("") && listener != null) {
            removeEventListener(type, listener, useCapture);
            if (this.mListenerEntries == null) {
                this.mListenerEntries = new ArrayList();
            }
            this.mListenerEntries.add(new EventListenerEntry(type, listener, useCapture));
        }
    }

    public boolean dispatchEvent(Event evt) throws EventException {
        EventImpl eventImpl = (EventImpl) evt;
        if (!eventImpl.isInitialized()) {
            throw new EventException((short) 0, "Event not initialized");
        } else if (eventImpl.getType() == null || eventImpl.getType().equals("")) {
            throw new EventException((short) 0, "Unspecified even type");
        } else {
            eventImpl.setTarget(this.mNodeTarget);
            eventImpl.setEventPhase((short) 2);
            eventImpl.setCurrentTarget(this.mNodeTarget);
            if (!(eventImpl.isPropogationStopped() || this.mListenerEntries == null)) {
                for (int i = 0; i < this.mListenerEntries.size(); i++) {
                    EventListenerEntry listenerEntry = (EventListenerEntry) this.mListenerEntries.get(i);
                    if (!listenerEntry.mUseCapture && listenerEntry.mType.equals(eventImpl.getType())) {
                        try {
                            listenerEntry.mListener.handleEvent(eventImpl);
                        } catch (Exception e) {
                            Log.w("EventTargetImpl", "Catched EventListener exception", e);
                        }
                    }
                }
            }
            if (eventImpl.getBubbles()) {
            }
            return eventImpl.isPreventDefault();
        }
    }

    public void removeEventListener(String type, EventListener listener, boolean useCapture) {
        if (this.mListenerEntries != null) {
            for (int i = 0; i < this.mListenerEntries.size(); i++) {
                EventListenerEntry listenerEntry = (EventListenerEntry) this.mListenerEntries.get(i);
                if (listenerEntry.mUseCapture == useCapture && listenerEntry.mListener == listener && listenerEntry.mType.equals(type)) {
                    this.mListenerEntries.remove(i);
                    break;
                }
            }
        }
    }
}
