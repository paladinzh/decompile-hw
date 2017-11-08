package com.android.systemui.analytics;

import android.os.Build;
import android.view.MotionEvent;
import com.android.systemui.statusbar.phone.TouchAnalyticsProto$Session;
import com.android.systemui.statusbar.phone.TouchAnalyticsProto.Session.PhoneEvent;
import com.android.systemui.statusbar.phone.TouchAnalyticsProto.Session.SensorEvent;
import com.android.systemui.statusbar.phone.TouchAnalyticsProto.Session.TouchEvent;
import com.android.systemui.statusbar.phone.TouchAnalyticsProto.Session.TouchEvent.Pointer;
import java.util.ArrayList;

public class SensorLoggerSession {
    private long mEndTimestampMillis;
    private ArrayList<TouchEvent> mMotionEvents = new ArrayList();
    private ArrayList<PhoneEvent> mPhoneEvents = new ArrayList();
    private int mResult = 2;
    private ArrayList<SensorEvent> mSensorEvents = new ArrayList();
    private final long mStartSystemTimeNanos;
    private final long mStartTimestampMillis;
    private int mTouchAreaHeight;
    private int mTouchAreaWidth;
    private int mType;

    public SensorLoggerSession(long startTimestampMillis, long startSystemTimeNanos) {
        this.mStartTimestampMillis = startTimestampMillis;
        this.mStartSystemTimeNanos = startSystemTimeNanos;
        this.mType = 3;
    }

    public void end(long endTimestampMillis, int result) {
        this.mResult = result;
        this.mEndTimestampMillis = endTimestampMillis;
    }

    public void addMotionEvent(MotionEvent motionEvent) {
        this.mMotionEvents.add(motionEventToProto(motionEvent));
    }

    public void addSensorEvent(android.hardware.SensorEvent eventOrig, long systemTimeNanos) {
        this.mSensorEvents.add(sensorEventToProto(eventOrig, systemTimeNanos));
    }

    public void addPhoneEvent(int eventType, long systemTimeNanos) {
        this.mPhoneEvents.add(phoneEventToProto(eventType, systemTimeNanos));
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("Session{");
        sb.append("mStartTimestampMillis=").append(this.mStartTimestampMillis);
        sb.append(", mStartSystemTimeNanos=").append(this.mStartSystemTimeNanos);
        sb.append(", mEndTimestampMillis=").append(this.mEndTimestampMillis);
        sb.append(", mResult=").append(this.mResult);
        sb.append(", mTouchAreaHeight=").append(this.mTouchAreaHeight);
        sb.append(", mTouchAreaWidth=").append(this.mTouchAreaWidth);
        sb.append(", mMotionEvents=[size=").append(this.mMotionEvents.size()).append("]");
        sb.append(", mSensorEvents=[size=").append(this.mSensorEvents.size()).append("]");
        sb.append(", mPhoneEvents=[size=").append(this.mPhoneEvents.size()).append("]");
        sb.append('}');
        return sb.toString();
    }

    public TouchAnalyticsProto$Session toProto() {
        TouchAnalyticsProto$Session proto = new TouchAnalyticsProto$Session();
        proto.setStartTimestampMillis(this.mStartTimestampMillis);
        proto.setDurationMillis(this.mEndTimestampMillis - this.mStartTimestampMillis);
        proto.setBuild(Build.FINGERPRINT);
        proto.setResult(this.mResult);
        proto.setType(this.mType);
        proto.sensorEvents = (SensorEvent[]) this.mSensorEvents.toArray(proto.sensorEvents);
        proto.touchEvents = (TouchEvent[]) this.mMotionEvents.toArray(proto.touchEvents);
        proto.phoneEvents = (PhoneEvent[]) this.mPhoneEvents.toArray(proto.phoneEvents);
        proto.setTouchAreaWidth(this.mTouchAreaWidth);
        proto.setTouchAreaHeight(this.mTouchAreaHeight);
        return proto;
    }

    private PhoneEvent phoneEventToProto(int eventType, long sysTimeNanos) {
        PhoneEvent proto = new PhoneEvent();
        proto.setType(eventType);
        proto.setTimeOffsetNanos(sysTimeNanos - this.mStartSystemTimeNanos);
        return proto;
    }

    private SensorEvent sensorEventToProto(android.hardware.SensorEvent ev, long sysTimeNanos) {
        SensorEvent proto = new SensorEvent();
        proto.setType(ev.sensor.getType());
        proto.setTimeOffsetNanos(sysTimeNanos - this.mStartSystemTimeNanos);
        proto.setTimestamp(ev.timestamp);
        proto.values = (float[]) ev.values.clone();
        return proto;
    }

    private TouchEvent motionEventToProto(MotionEvent ev) {
        int count = ev.getPointerCount();
        TouchEvent proto = new TouchEvent();
        proto.setTimeOffsetNanos(ev.getEventTimeNano() - this.mStartSystemTimeNanos);
        proto.setAction(ev.getActionMasked());
        proto.setActionIndex(ev.getActionIndex());
        proto.pointers = new Pointer[count];
        for (int i = 0; i < count; i++) {
            Pointer p = new Pointer();
            p.setX(ev.getX(i));
            p.setY(ev.getY(i));
            p.setSize(ev.getSize(i));
            p.setPressure(ev.getPressure(i));
            p.setId(ev.getPointerId(i));
            proto.pointers[i] = p;
        }
        return proto;
    }

    public void setTouchArea(int width, int height) {
        this.mTouchAreaWidth = width;
        this.mTouchAreaHeight = height;
    }

    public int getResult() {
        return this.mResult;
    }

    public long getStartTimestampMillis() {
        return this.mStartTimestampMillis;
    }
}
