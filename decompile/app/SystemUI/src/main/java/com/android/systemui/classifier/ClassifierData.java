package com.android.systemui.classifier;

import android.util.SparseArray;
import android.view.MotionEvent;
import java.util.ArrayList;

public class ClassifierData {
    private SparseArray<Stroke> mCurrentStrokes = new SparseArray();
    private final float mDpi;
    private ArrayList<Stroke> mEndingStrokes = new ArrayList();

    public ClassifierData(float dpi) {
        this.mDpi = dpi;
    }

    public void update(MotionEvent event) {
        int i;
        int id;
        this.mEndingStrokes.clear();
        int action = event.getActionMasked();
        if (!(action == 0 || action == 6)) {
            if (action == 3) {
            }
            i = 0;
            while (i < event.getPointerCount()) {
                id = event.getPointerId(i);
                if (this.mCurrentStrokes.get(id) == null) {
                    this.mCurrentStrokes.put(id, new Stroke(event.getEventTimeNano(), this.mDpi));
                }
                ((Stroke) this.mCurrentStrokes.get(id)).addPoint(event.getX(i), event.getY(i), event.getEventTimeNano());
                if (!(action == 1 || action == 3)) {
                    if (action == 6 && i == event.getActionIndex()) {
                    }
                    i++;
                }
                this.mEndingStrokes.add(getStroke(id));
                i++;
            }
        }
        this.mCurrentStrokes.clear();
        i = 0;
        while (i < event.getPointerCount()) {
            id = event.getPointerId(i);
            if (this.mCurrentStrokes.get(id) == null) {
                this.mCurrentStrokes.put(id, new Stroke(event.getEventTimeNano(), this.mDpi));
            }
            ((Stroke) this.mCurrentStrokes.get(id)).addPoint(event.getX(i), event.getY(i), event.getEventTimeNano());
            this.mEndingStrokes.add(getStroke(id));
            i++;
        }
    }

    public void cleanUp(MotionEvent event) {
        this.mEndingStrokes.clear();
        int action = event.getActionMasked();
        int i = 0;
        while (i < event.getPointerCount()) {
            int id = event.getPointerId(i);
            if (!(action == 1 || action == 3)) {
                if (action == 6 && i == event.getActionIndex()) {
                }
                i++;
            }
            this.mCurrentStrokes.remove(id);
            i++;
        }
    }

    public ArrayList<Stroke> getEndingStrokes() {
        return this.mEndingStrokes;
    }

    public Stroke getStroke(int id) {
        return (Stroke) this.mCurrentStrokes.get(id);
    }
}
