package com.huawei.gallery.editor.step;

import android.graphics.Bitmap;
import com.huawei.gallery.editor.cache.BitmapCache;
import com.huawei.gallery.editor.pipeline.ImagePreset;
import java.util.Stack;

public class StepStack {
    private final Stack<EditorStep> mAppliedStack = new Stack();
    private final BitmapCache mBitmapCache;
    private final Stack<EditorStep> mRedoStack = new Stack();
    private final StackListener mStackListener;

    public interface StackListener {
        void onStackChanged(boolean z, boolean z2, boolean z3);
    }

    public StepStack(BitmapCache bitmapCache, StackListener listener) {
        this.mStackListener = listener;
        this.mBitmapCache = bitmapCache;
    }

    public synchronized void undo() {
        if (!this.mAppliedStack.empty()) {
            EditorStep step = (EditorStep) this.mAppliedStack.pop();
            step.resetCachedBitmap(this.mBitmapCache);
            this.mRedoStack.push(step);
            stackChanged();
        }
    }

    public synchronized void redo() {
        if (!this.mRedoStack.empty()) {
            pushEditorStepInternal((EditorStep) this.mRedoStack.pop());
        }
    }

    private void stackChanged() {
        if (this.mStackListener != null) {
            this.mStackListener.onStackChanged(!this.mAppliedStack.empty(), !this.mRedoStack.empty(), canCompare());
        }
    }

    private boolean canCompare() {
        for (EditorStep step : this.mAppliedStack) {
            if (!(step instanceof GeometryEditorStep)) {
                return true;
            }
        }
        return false;
    }

    private void pushEditorStepInternal(EditorStep step) {
        this.mAppliedStack.push(step);
        stackChanged();
    }

    public synchronized void pushEditorStep(EditorStep step) {
        for (EditorStep s : this.mRedoStack) {
            s.reset(this.mBitmapCache);
        }
        this.mRedoStack.clear();
        pushEditorStepInternal(step);
    }

    public synchronized Stack<EditorStep> getAppliedStack() {
        Stack<EditorStep> stepStack;
        stepStack = new Stack();
        for (EditorStep step : this.mAppliedStack) {
            stepStack.push(step.copy());
        }
        return stepStack;
    }

    public synchronized void clear() {
        for (EditorStep step : this.mAppliedStack) {
            step.reset(this.mBitmapCache);
        }
        for (EditorStep step2 : this.mRedoStack) {
            step2.reset(this.mBitmapCache);
        }
        this.mAppliedStack.clear();
        this.mRedoStack.clear();
    }

    public synchronized void cache(Bitmap bmp) {
        if (this.mAppliedStack.size() != 0) {
            for (EditorStep step : this.mAppliedStack) {
                step.resetCachedBitmap(this.mBitmapCache);
            }
            ((EditorStep) this.mAppliedStack.peek()).cache(this.mBitmapCache.getBitmapCopy(bmp));
        }
    }

    public synchronized boolean findCachedBitmap(ImagePreset preset) {
        if (preset == null) {
            return false;
        }
        Stack<EditorStep> steps = preset.getEditorStepStack();
        int findIndex = -1;
        int size = Math.min(steps.size(), this.mAppliedStack.size());
        int index = 0;
        while (index < size) {
            if (((EditorStep) steps.get(index)).equals(this.mAppliedStack.get(index)) && ((EditorStep) this.mAppliedStack.get(index)).getCachedBitmap() != null) {
                findIndex = index;
            }
            index++;
        }
        if (findIndex == -1) {
            return false;
        }
        ((EditorStep) steps.get(findIndex)).resetCachedBitmap(this.mBitmapCache);
        ((EditorStep) steps.get(findIndex)).cache(this.mBitmapCache.getBitmapCopy(((EditorStep) this.mAppliedStack.get(findIndex)).getCachedBitmap()));
        return true;
    }

    public void requestStackChangeCall() {
        stackChanged();
    }

    public synchronized Bitmap getLatestCachedBitmap() {
        if (this.mAppliedStack.size() == 0) {
            return null;
        }
        return ((EditorStep) this.mAppliedStack.peek()).getCachedBitmap();
    }
}
