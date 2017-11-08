package com.android.gallery3d.util;

import android.annotation.TargetApi;
import android.graphics.Matrix;
import android.view.MotionEvent;
import android.view.MotionEvent.PointerCoords;
import com.android.gallery3d.common.ApiHelper;

public final class MotionEventHelper {
    private MotionEventHelper() {
    }

    public static MotionEvent transformEvent(MotionEvent e, Matrix m) {
        if (ApiHelper.HAS_MOTION_EVENT_TRANSFORM) {
            return transformEventNew(e, m);
        }
        return transformEventOld(e, m);
    }

    @TargetApi(11)
    private static MotionEvent transformEventNew(MotionEvent e, Matrix m) {
        MotionEvent newEvent = MotionEvent.obtain(e);
        newEvent.transform(m);
        return newEvent;
    }

    private static MotionEvent transformEventOld(MotionEvent e, Matrix m) {
        int i;
        long downTime = e.getDownTime();
        long eventTime = e.getEventTime();
        int action = e.getAction();
        int pointerCount = e.getPointerCount();
        int[] pointerIds = getPointerIds(e);
        PointerCoords[] pointerCoords = getPointerCoords(e);
        int metaState = e.getMetaState();
        float xPrecision = e.getXPrecision();
        float yPrecision = e.getYPrecision();
        int deviceId = e.getDeviceId();
        int edgeFlags = e.getEdgeFlags();
        int source = e.getSource();
        int flags = e.getFlags();
        float[] xy = new float[(pointerCoords.length * 2)];
        for (i = 0; i < pointerCount; i++) {
            xy[i * 2] = pointerCoords[i].x;
            xy[(i * 2) + 1] = pointerCoords[i].y;
        }
        m.mapPoints(xy);
        for (i = 0; i < pointerCount; i++) {
            pointerCoords[i].x = xy[i * 2];
            pointerCoords[i].y = xy[(i * 2) + 1];
            pointerCoords[i].orientation = transformAngle(m, pointerCoords[i].orientation);
        }
        return MotionEvent.obtain(downTime, eventTime, action, pointerCount, pointerIds, pointerCoords, metaState, xPrecision, yPrecision, deviceId, edgeFlags, source, flags);
    }

    private static int[] getPointerIds(MotionEvent e) {
        int n = e.getPointerCount();
        int[] r = new int[n];
        for (int i = 0; i < n; i++) {
            r[i] = e.getPointerId(i);
        }
        return r;
    }

    private static PointerCoords[] getPointerCoords(MotionEvent e) {
        int n = e.getPointerCount();
        PointerCoords[] r = new PointerCoords[n];
        for (int i = 0; i < n; i++) {
            r[i] = new PointerCoords();
            e.getPointerCoords(i, r[i]);
        }
        return r;
    }

    private static float transformAngle(Matrix m, float angleRadians) {
        float[] v = new float[]{(float) Math.sin((double) angleRadians), (float) (-Math.cos((double) angleRadians))};
        m.mapVectors(v);
        float result = (float) Math.atan2((double) v[0], (double) (-v[1]));
        if (((double) result) < -1.5707963267948966d) {
            return (float) (((double) result) + 3.141592653589793d);
        }
        if (((double) result) > 1.5707963267948966d) {
            return (float) (((double) result) - 3.141592653589793d);
        }
        return result;
    }
}
