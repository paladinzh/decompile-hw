package com.fyusion.sdk.viewer.internal;

import android.content.Context;
import android.graphics.PointF;
import android.support.annotation.Nullable;
import android.support.v4.view.MotionEventCompat;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.OnScaleGestureListener;
import com.huawei.watermark.manager.parse.WMElement;
import java.util.HashSet;

/* compiled from: Unknown */
public class d implements OnScaleGestureListener {
    private int a = 0;
    private HashSet<Integer> b = new HashSet();
    private boolean c = false;
    private PointF d = new PointF();
    private float e = 0.0f;
    private ScaleGestureDetector f = null;
    private a g = null;

    /* compiled from: Unknown */
    public interface a {
        void a(float f, float f2);

        void a(float f, float f2, float f3);
    }

    public d(Context context, @Nullable a aVar) {
        this.f = new ScaleGestureDetector(context, this);
        this.g = aVar;
    }

    public boolean a(MotionEvent motionEvent) {
        boolean z = true;
        this.f.onTouchEvent(motionEvent);
        if (MotionEventCompat.getActionMasked(motionEvent) == 6 && MotionEventCompat.getPointerCount(motionEvent) <= 2) {
            this.c = false;
        } else if (MotionEventCompat.getPointerCount(motionEvent) == 2) {
            if (this.c) {
                float focusX = this.f.getFocusX() - this.d.x;
                float focusY = this.f.getFocusY() - this.d.y;
                if (this.g != null) {
                    if (Math.abs(focusX) < WMElement.CAMERASIZEVALUE1B1) {
                        z = false;
                    }
                    if (z || Math.abs(focusY) >= WMElement.CAMERASIZEVALUE1B1) {
                        this.g.a(focusX, focusY);
                    }
                }
                this.d.set(this.f.getFocusX(), this.f.getFocusY());
            } else {
                this.d.set(this.f.getFocusX(), this.f.getFocusY());
                this.c = true;
            }
        }
        return this.c;
    }

    public boolean onScale(ScaleGestureDetector scaleGestureDetector) {
        float currentSpan = this.e > 0.0f ? scaleGestureDetector.getCurrentSpan() / this.e : 2.0f;
        this.e = scaleGestureDetector.getCurrentSpan();
        if (((currentSpan < 0.999f) || currentSpan > 1.001f) && this.g != null) {
            this.g.a(currentSpan, scaleGestureDetector.getFocusX(), scaleGestureDetector.getFocusY());
        }
        return true;
    }

    public boolean onScaleBegin(ScaleGestureDetector scaleGestureDetector) {
        this.e = scaleGestureDetector.getCurrentSpan();
        return true;
    }

    public void onScaleEnd(ScaleGestureDetector scaleGestureDetector) {
    }
}
