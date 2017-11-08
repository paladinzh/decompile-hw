package huawei.com.android.server.policy.fingersense;

import android.content.Context;
import android.os.Handler;
import android.util.Flog;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;

public class KnockGestureDetector {
    private static final boolean DEBUG = false;
    private static final long DOUBLE_KNOCK_RADIUS_MM = 10;
    private static final long DOUBLE_KNOCK_TIMEOUT_MS = 250;
    private static final long KNUCKLE_POINTER_COUNT_TIMEOUT_MS = 250;
    private static final long MS_DELAY_TO_DISABLE_DOUBLE_KNOCK = 250;
    private static final long MS_DELAY_TO_ENABLE_DOUBLE_KNOCK = 400;
    private static final String TAG = "KnockGestureDetector";
    private int consecutiveTouchCount = 1;
    private float doubleKnockRadiusPx;
    private final MotionEventRunnable doubleKnuckleDoubleKnockRunnable = new MotionEventRunnable() {
        public void run() {
            if (KnockGestureDetector.this.consecutiveTouchCount < 3) {
                KnockGestureDetector.this.notifyDoubleKnuckleDoubleKnock("fingersense_knuckle_gesture_double_knock", this.event);
            } else {
                KnockGestureDetector.this.dumpDebugVariables();
            }
        }
    };
    private int knucklePointerCount;
    private int knucklePointerId = -1;
    private float lastKnuckleDist;
    private long lastKnuckleEventTimeDiffMs;
    private long lastKnuckleEventTimeMs;
    private float lastKnuckleMoveDist;
    private float lastKnuckleMoveX;
    private float lastKnuckleMoveY;
    private float lastKnuckleX;
    private float lastKnuckleY;
    private long lastMotionEventTimeDiffMs;
    private long lastMotionEventTimeMs;
    private final Handler mHandler;
    private final OnKnockGestureListener mOnKnockGestureListener;
    private final MotionEventRunnable singleKnuckleDoubleKnockRunnable = new MotionEventRunnable() {
        public void run() {
            if (KnockGestureDetector.this.consecutiveTouchCount < 3) {
                KnockGestureDetector.this.notifySingleKnuckleDoubleKnock("fingersense_knuckle_gesture_knock", this.event);
            } else {
                KnockGestureDetector.this.dumpDebugVariables();
            }
        }
    };

    private static abstract class MotionEventRunnable implements Runnable {
        MotionEvent event;

        private MotionEventRunnable() {
            this.event = null;
        }
    }

    public interface OnKnockGestureListener {
        void onDoubleKnocksNotYetConfirmed(String str, MotionEvent motionEvent);

        void onDoubleKnuckleDoubleKnock(String str, MotionEvent motionEvent);

        void onSingleKnuckleDoubleKnock(String str, MotionEvent motionEvent);
    }

    public KnockGestureDetector(Context context, OnKnockGestureListener listener) {
        this.mOnKnockGestureListener = listener;
        this.mHandler = new Handler();
        this.doubleKnockRadiusPx = TypedValue.applyDimension(5, 10.0f, context.getResources().getDisplayMetrics());
    }

    public int getKnucklePointerCount() {
        return this.knucklePointerCount;
    }

    public boolean onKnuckleTouchEvent(MotionEvent motionEvent) {
        long thisKnuckleEventTimeMs = motionEvent.getEventTime();
        this.lastKnuckleEventTimeDiffMs = thisKnuckleEventTimeMs - this.lastKnuckleEventTimeMs;
        this.lastKnuckleEventTimeMs = thisKnuckleEventTimeMs;
        boolean ret = false;
        switch (motionEvent.getAction()) {
            case 0:
                ret = touchDown(motionEvent);
                break;
            case 1:
                ret = touchUp(motionEvent);
                break;
            case 2:
                ret = touchMove(motionEvent);
                break;
            case 3:
                ret = touchCancel(motionEvent);
                break;
        }
        int count = motionEvent.getPointerCount();
        if (count > this.knucklePointerCount) {
            this.knucklePointerCount = count;
        } else if (this.lastKnuckleEventTimeDiffMs < 0 || this.lastKnuckleEventTimeDiffMs > 250) {
            this.knucklePointerCount = count;
        }
        if (motionEvent.getAction() == 0) {
            checkForDoubleKnocks(motionEvent);
        }
        return ret;
    }

    private boolean touchDown(MotionEvent motionEvent) {
        float thisKnuckleX = motionEvent.getX();
        float thisKnuckleY = motionEvent.getY();
        this.lastKnuckleDist = (float) Math.sqrt(Math.pow((double) (thisKnuckleX - this.lastKnuckleX), 2.0d) + Math.pow((double) (thisKnuckleY - this.lastKnuckleY), 2.0d));
        this.lastKnuckleMoveX = thisKnuckleX;
        this.lastKnuckleX = thisKnuckleX;
        this.lastKnuckleMoveY = thisKnuckleY;
        this.lastKnuckleY = thisKnuckleY;
        if (this.lastKnuckleEventTimeDiffMs < 0 || this.lastKnuckleEventTimeDiffMs > 250) {
            this.lastKnuckleMoveDist = 0.0f;
            this.knucklePointerId = motionEvent.getPointerId(0);
        }
        return true;
    }

    private boolean touchMove(MotionEvent motionEvent) {
        int pointerIndex = motionEvent.findPointerIndex(this.knucklePointerId);
        if (pointerIndex < 0) {
            return true;
        }
        float thisKnuckleX = motionEvent.getX(pointerIndex);
        float thisKnuckleY = motionEvent.getY(pointerIndex);
        this.lastKnuckleMoveDist += (float) Math.sqrt(Math.pow((double) (thisKnuckleX - this.lastKnuckleMoveX), 2.0d) + Math.pow((double) (thisKnuckleY - this.lastKnuckleMoveY), 2.0d));
        this.lastKnuckleMoveX = thisKnuckleX;
        this.lastKnuckleMoveY = thisKnuckleY;
        return true;
    }

    private boolean touchUp(MotionEvent motionEvent) {
        return true;
    }

    private boolean touchCancel(MotionEvent motionEvent) {
        return true;
    }

    public boolean onAnyTouchEvent(MotionEvent motionEvent) {
        if (motionEvent.getAction() == 0) {
            long thisMotionEventTimeMs = motionEvent.getEventTime();
            this.lastMotionEventTimeDiffMs = thisMotionEventTimeMs - this.lastMotionEventTimeMs;
            this.lastMotionEventTimeMs = thisMotionEventTimeMs;
            if (this.lastMotionEventTimeDiffMs <= 0 || this.lastMotionEventTimeDiffMs >= MS_DELAY_TO_ENABLE_DOUBLE_KNOCK) {
                this.consecutiveTouchCount = 1;
            } else {
                this.consecutiveTouchCount++;
                if (this.consecutiveTouchCount > 2) {
                    Flog.i(1503, "Multiple rapid taps detecting, aborting Knuckle Gestures.");
                    this.mHandler.removeCallbacks(this.singleKnuckleDoubleKnockRunnable);
                }
            }
        }
        return true;
    }

    public boolean checkForDoubleKnocks(final MotionEvent e) {
        if (this.lastKnuckleEventTimeDiffMs >= 250) {
            Flog.i(1503, "lastKnuckleEventTimeDiffMs too large, not triggering knock: " + this.lastKnuckleEventTimeDiffMs + " > " + 250);
        } else if (this.lastKnuckleMoveDist < this.doubleKnockRadiusPx * 2.0f) {
            MotionEvent motionEvent = e;
            this.mHandler.postDelayed(new Runnable() {
                public void run() {
                    if (KnockGestureDetector.this.knucklePointerCount != 1) {
                        KnockGestureDetector.this.onDoubleKnocks(e, "fingersense_knuckle_gesture_double_knock");
                    } else if (KnockGestureDetector.this.lastKnuckleDist < KnockGestureDetector.this.doubleKnockRadiusPx) {
                        KnockGestureDetector.this.onDoubleKnocks(e, "fingersense_knuckle_gesture_knock");
                    } else {
                        Log.w(KnockGestureDetector.TAG, "Double knock, but knock too far apart, no screenshot.");
                    }
                }
            }, 25);
        } else {
            Flog.i(1503, "lastKnuckleMoveDist too far, not triggering knock: " + this.lastKnuckleMoveDist + " > " + this.doubleKnockRadiusPx);
        }
        return false;
    }

    public void onDoubleKnocks(MotionEvent event, String knuckleGesture) {
        notifyOnDoubleKnocksNotYetConfirmed(knuckleGesture, event);
        if (knuckleGesture != null && knuckleGesture.equals("fingersense_knuckle_gesture_knock")) {
            Flog.i(1503, "Single Knuckle Double Knock captured");
            this.singleKnuckleDoubleKnockRunnable.event = event;
            this.mHandler.postDelayed(this.singleKnuckleDoubleKnockRunnable, 250);
        } else if (knuckleGesture == null || !knuckleGesture.equals("fingersense_knuckle_gesture_double_knock")) {
            Flog.w(1503, "FingerSense Gesture Unrecognized Knock Pattern.");
        } else {
            Flog.i(1503, "Double Knuckle Double Knock captured");
            this.doubleKnuckleDoubleKnockRunnable.event = event;
            this.mHandler.postDelayed(this.doubleKnuckleDoubleKnockRunnable, 250);
        }
    }

    private void notifyDoubleKnuckleDoubleKnock(String gestureName, MotionEvent event) {
        Log.w(TAG, "Notifying Double Knuckle Double Knock");
        if (this.mOnKnockGestureListener != null) {
            this.mOnKnockGestureListener.onDoubleKnuckleDoubleKnock(gestureName, event);
        }
    }

    private void notifySingleKnuckleDoubleKnock(String gestureName, MotionEvent event) {
        Log.w(TAG, "Notifying Single Knuckle Double Knock");
        if (this.mOnKnockGestureListener != null) {
            this.mOnKnockGestureListener.onSingleKnuckleDoubleKnock(gestureName, event);
        }
    }

    private void notifyOnDoubleKnocksNotYetConfirmed(String gestureName, MotionEvent event) {
        if (this.mOnKnockGestureListener != null) {
            this.mOnKnockGestureListener.onDoubleKnocksNotYetConfirmed(gestureName, event);
        }
    }

    private void dumpDebugVariables() {
        Flog.i(1503, "lastMotionEventTimeDiffMs: " + this.lastMotionEventTimeDiffMs + " lastKnuckleEventTimeDiffMs: " + this.lastKnuckleEventTimeDiffMs + " lastKnuckleDist: " + this.lastKnuckleDist + " lastKnuckleMoveDist: " + this.lastKnuckleMoveDist + " knucklePointerCount: " + this.knucklePointerCount + " consecutiveTouchCount: " + this.consecutiveTouchCount);
    }
}
