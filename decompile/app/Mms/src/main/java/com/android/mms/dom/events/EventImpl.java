package com.android.mms.dom.events;

import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventTarget;

public class EventImpl implements Event {
    private boolean mCanBubble;
    private boolean mCancelable;
    private EventTarget mCurrentTarget;
    private short mEventPhase;
    private String mEventType;
    private boolean mInitialized;
    private boolean mPreventDefault;
    private int mSeekTo;
    private boolean mStopPropagation;
    private EventTarget mTarget;
    private final long mTimeStamp = System.currentTimeMillis();

    public boolean getBubbles() {
        return this.mCanBubble;
    }

    public String getType() {
        return this.mEventType;
    }

    public void initEvent(String eventTypeArg, boolean canBubbleArg, boolean cancelableArg) {
        this.mEventType = eventTypeArg;
        this.mCanBubble = canBubbleArg;
        this.mCancelable = cancelableArg;
        this.mInitialized = true;
    }

    public void initEvent(String eventTypeArg, boolean canBubbleArg, boolean cancelableArg, int seekTo) {
        this.mSeekTo = seekTo;
        initEvent(eventTypeArg, canBubbleArg, cancelableArg);
    }

    boolean isInitialized() {
        return this.mInitialized;
    }

    boolean isPreventDefault() {
        return this.mPreventDefault;
    }

    boolean isPropogationStopped() {
        return this.mStopPropagation;
    }

    void setTarget(EventTarget target) {
        this.mTarget = target;
    }

    void setEventPhase(short eventPhase) {
        this.mEventPhase = eventPhase;
    }

    void setCurrentTarget(EventTarget currentTarget) {
        this.mCurrentTarget = currentTarget;
    }

    public int getSeekTo() {
        return this.mSeekTo;
    }
}
